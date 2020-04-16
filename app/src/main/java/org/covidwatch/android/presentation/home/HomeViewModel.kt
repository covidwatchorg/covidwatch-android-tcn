package org.covidwatch.android.presentation.home

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.covidwatch.android.R
import org.covidwatch.android.domain.*
import org.covidwatch.android.presentation.util.Event

class HomeViewModel(
    private val userFlowRepository: UserFlowRepository
) : ViewModel() {

    private val _turnOnBluetoothAction = MutableLiveData<Event<Unit>>()
    val turnOnBluetoothAction: LiveData<Event<Unit>> = _turnOnBluetoothAction

    private val _banner = MutableLiveData<Banner>()
    val banner: LiveData<Banner> get() = _banner

    private val _userFlow = MutableLiveData<UserFlow>()
    val userFlow: LiveData<UserFlow> get() = _userFlow

    fun setup() {
        val userFlow = userFlowRepository.getUserFlow()
        if (userFlow is FirstTimeUser) {
            userFlowRepository.updateFirstTimeUserFlow()
        }
        if (userFlow !is Setup) {
            ensureBluetoothIsOn()
        }
        _userFlow.value = userFlow
    }

    fun onBannerClicked() {
        val bannerAction = _banner.value?.action ?: return

        when (bannerAction) {
            is BannerAction.TurnOnBluetooth -> {
                _turnOnBluetoothAction.value = Event(Unit)
            }
        }
    }

    fun bluetoothIsOn() {
        _banner.value = Banner.Empty
    }

    private fun ensureBluetoothIsOn() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            _banner.value = Banner.Info(R.string.turn_bluetooth_on, BannerAction.TurnOnBluetooth)
        }
    }
}