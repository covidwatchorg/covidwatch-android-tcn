package org.covidwatch.android.ui.home

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.covidwatch.android.R
import org.covidwatch.android.domain.*

class HomeViewModel(
    private val userFlowRepository: UserFlowRepository,
    private val testedRepository: TestedRepository,
    private val ensureTcnIsStartedUseCase: EnsureTcnIsStartedUseCase
) : ViewModel(), EnsureTcnIsStartedPresenter {

    private val isUserTestedPositive: Boolean get() = testedRepository.isUserTestedPositive()
    private val _userTestedPositive = MutableLiveData<Unit>()
    val userTestedPositive: LiveData<Unit> get() = _userTestedPositive

    private val _infoBannerState = MutableLiveData<InfoBannerState>()
    val infoBannerState: LiveData<InfoBannerState> get() = _infoBannerState

    private val _warningBannerState = MutableLiveData<WarningBannerState>()
    val warningBannerState: LiveData<WarningBannerState> get() = _warningBannerState

    private val _userFlow = MutableLiveData<UserFlow>()
    val userFlow: LiveData<UserFlow> get() = _userFlow

    private val _isRefreshing = MediatorLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    private val hasPossiblyInteractedWithInfected: LiveData<Boolean> = MutableLiveData()

    private val interactedWithInfectedObserver =
        Observer<Boolean> { hasPossiblyInteractedWithInfected ->
            if (hasPossiblyInteractedWithInfected && !isUserTestedPositive) {
                _warningBannerState.value = WarningBannerState.Visible(R.string.contact_alert_text)
            }
        }

    init {
        hasPossiblyInteractedWithInfected.observeForever(interactedWithInfectedObserver)
    }

    override fun onCleared() {
        super.onCleared()
        hasPossiblyInteractedWithInfected.removeObserver(interactedWithInfectedObserver)
    }

    override fun showLocationPermissionBanner() {
        _infoBannerState.postValue(InfoBannerState.Visible(R.string.allow_location_access))
    }

    override fun showEnableBluetoothBanner() {
        _infoBannerState.postValue(InfoBannerState.Visible(R.string.turn_bluetooth_on))
    }

    override fun hideBanner() {
        _infoBannerState.postValue(InfoBannerState.Hidden)
    }

    fun onStart() {
        val userFlow = userFlowRepository.getUserFlow()
        if (userFlow is FirstTimeUser) {
            userFlowRepository.updateFirstTimeUserFlow()
        }
        if (userFlow !is Setup) {
            checkIfUserTestedPositive()
            ensureTcnIsStarted()
        }
        _userFlow.value = userFlow
    }

    fun onRefreshRequested() {
        TODO()
    }

    private fun checkIfUserTestedPositive() {
        if (isUserTestedPositive) {
            _userTestedPositive.value = Unit
            _warningBannerState.value = WarningBannerState.Visible(R.string.reported_alert_text)
        }
    }

    private fun ensureTcnIsStarted() {
        viewModelScope.launch(Dispatchers.IO) {
            ensureTcnIsStartedUseCase.execute(this@HomeViewModel)
        }
    }
}