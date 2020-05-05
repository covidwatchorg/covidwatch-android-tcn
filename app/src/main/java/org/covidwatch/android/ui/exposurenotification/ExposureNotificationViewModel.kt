package org.covidwatch.android.ui.exposurenotification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.covidwatch.android.ENStatus
import org.covidwatch.android.ExposureNotificationManager
import org.covidwatch.android.data.CovidExposureInformation
import org.covidwatch.android.data.CovidExposureSummary
import org.covidwatch.android.data.PositiveDiagnosis
import org.covidwatch.android.data.asDiagnosisKey
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.data.toCovidExposureInformation
import org.covidwatch.android.domain.ProvideDiagnosisKeysUseCase
import org.covidwatch.android.extension.doOnNext
import org.covidwatch.android.extension.launchUseCase
import org.covidwatch.android.functional.Either

class ExposureNotificationViewModel(
    private val enManager: ExposureNotificationManager,
    private val diagnosisRepository: PositiveDiagnosisRepository,
    private val exposureInformationRepository: ExposureInformationRepository,
    private val provideDiagnosisKeysUseCase: ProvideDiagnosisKeysUseCase,
    preferenceStorage: PreferenceStorage
) : ViewModel() {

    private val _exposureServiceRunning = MutableLiveData<Boolean>()
    val exposureServiceRunning: LiveData<Boolean> = _exposureServiceRunning

    val exposureInfo: LiveData<List<CovidExposureInformation>> =
        exposureInformationRepository.exposureInformation()

    val exposureSummary: LiveData<CovidExposureSummary?> =
        preferenceStorage.observableExposureSummary.doOnNext {
            _showLoadButton.value = it != null
        }

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _showLoadButton = MutableLiveData<Boolean>()
    val showLoadButton: LiveData<Boolean> = _showLoadButton

    fun uploadDiagnosis(phaNumber: String) {
        viewModelScope.launch {
            if (diagnosisRepository.isNumberValid(phaNumber)) {
                enManager.temporaryExposureKeyHistory().success {
                    val diagnosisKeys = it.map { key -> key.asDiagnosisKey() }
                    val positiveDiagnosis =
                        PositiveDiagnosis(
                            diagnosisKeys,
                            phaNumber
                        )
                    diagnosisRepository.uploadDiagnosisKeys(positiveDiagnosis)
                }
            }
        }
    }

    fun startStopService() {
        viewModelScope.launch {
            // Check if we need to stop the service
            if (enManager.isEnabled().result() == true) {
                enManager.stop()
            } else { // Otherwise run the service
                enManager.start().result()
            }

            // Handle the case of is service running or not simply by asking the manager
            _exposureServiceRunning.value = enManager.isEnabled().result()
        }
    }

    fun downloadDiagnosisKeys() {
        viewModelScope.launchUseCase(provideDiagnosisKeysUseCase) {
            handleError(it.left)
            _isRefreshing.value = false
        }
    }

    fun loadExposureInformation() {
        viewModelScope.launch {
            enManager.getExposureInformation().success {
                val exposureInformation = it.map { information ->
                    information.toCovidExposureInformation()
                }
                exposureInformationRepository.saveExposureInformation(exposureInformation)
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            enManager.resetAllData()
        }
    }

    private suspend fun <R : ENStatus, L> Either<R, L>.success(block: suspend (value: L) -> Unit = {}) {
        right?.let { block(it) }
        left?.let { handleError(it) }
    }

    private fun <R : ENStatus, L> Either<R, L>.result(): L? {
        left?.let { handleError(it) }
        return right
    }

    private fun handleError(status: ENStatus?) {
        when (status) {
            ENStatus.SUCCESS -> TODO()
            ENStatus.FailedRejectedOptIn -> TODO()
            ENStatus.FailedServiceDisabled -> TODO()
            ENStatus.FailedBluetoothScanningDisabled -> TODO()
            ENStatus.FailedTemporarilyDisabled -> TODO()
            ENStatus.FailedInsufficientStorage -> TODO()
            ENStatus.FailedInternal -> TODO()
        }
    }
}
