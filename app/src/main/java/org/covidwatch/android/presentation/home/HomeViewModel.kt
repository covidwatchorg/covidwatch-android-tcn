package org.covidwatch.android.presentation.home

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.covidwatch.android.R
import org.covidwatch.android.data.TemporaryContactNumberDAO
import org.covidwatch.android.domain.*
import org.covidwatch.android.presentation.util.Event

class HomeViewModel(
    private val userFlowRepository: UserFlowRepository,
    private val testedRepository: TestedRepository,
    private val tcnDao: TemporaryContactNumberDAO
) : ViewModel() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    private val isUserTestedPositive: Boolean get() = testedRepository.isUserTestedPositive()
    private val _userTestedPositive = MutableLiveData<Unit>()
    val userTestedPositive: LiveData<Unit> get() = _userTestedPositive

    private val _locationPermissionAction = MutableLiveData<Event<Unit>>()
    val locationPermissionAction: LiveData<Event<Unit>> = _locationPermissionAction

    private val _turnOnBluetoothAction = MutableLiveData<Event<Unit>>()
    val turnOnBluetoothAction: LiveData<Event<Unit>> = _turnOnBluetoothAction

    private val _banner = MutableLiveData<Banner>()
    val banner: LiveData<Banner> get() = _banner

    private val _userFlow = MutableLiveData<UserFlow>()
    val userFlow: LiveData<UserFlow> get() = _userFlow

    private val _potentialRiskAction = MutableLiveData<Event<Unit>>()
    val potentialRiskAction: LiveData<Event<Unit>> get() = _potentialRiskAction

    fun setup() {
        viewModelScope.launch(Dispatchers.IO) {
            tcnDao.allSortedByDescTimestamp()
                .map { it.fold(false) { infected, tcn -> infected || tcn.wasPotentiallyInfectious } }
                .collect { potentiallyInfected ->
                    if (potentiallyInfected && !isUserTestedPositive) {
                        _banner.postValue(
                            Banner.Warning(
                                R.string.contact_alert_text,
                                BannerAction.PotentialRisk
                            )
                        )
                    } else if (isUserTestedPositive) {
                        _banner.postValue(
                            Banner.Warning(
                                R.string.reported_alert_text,
                                BannerAction.PotentialRisk
                            )
                        )
                    }
                }
        }

        val userFlow = userFlowRepository.getUserFlow()
        when (userFlow) {
            !is Setup -> {
                _locationPermissionAction.value = Event(Unit)
                ensureBluetoothIsOn()
                checkIfTestedPositive()
            }
            is FirstTimeUser -> {
                userFlowRepository.updateFirstTimeUserFlow()
            }
        }

        _userFlow.value = userFlow
    }

    fun onBannerClicked() {
        val bannerAction = _banner.value?.action ?: return

        when (bannerAction) {
            is BannerAction.TurnOnBluetooth -> {
                _turnOnBluetoothAction.value = Event(Unit)
            }
            is BannerAction.PotentialRisk -> {
                _potentialRiskAction.value = Event(Unit)
            }
        }
    }

    fun bluetoothIsOn() {
        _banner.value = Banner.Empty
    }

    private fun ensureBluetoothIsOn() {
        if (bluetoothAdapter?.isEnabled == false) {
            _banner.value = Banner.Info(R.string.turn_bluetooth_on, BannerAction.TurnOnBluetooth)
        }
    }

    private fun checkIfTestedPositive() {
        if (isUserTestedPositive) {
            _userTestedPositive.value = Unit
        }
    }
}