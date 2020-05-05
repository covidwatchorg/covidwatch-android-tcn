package org.covidwatch.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import kotlinx.coroutines.GlobalScope
import org.covidwatch.android.domain.ProvideDiagnosisKeysUseCase
import org.covidwatch.android.domain.UpdateExposureStateUseCase
import org.covidwatch.android.extension.launchUseCase
import org.koin.java.KoinJavaComponent.inject

class ExposureNotificationReceiver : BroadcastReceiver() {
    private val provideDiagnosisKeysUseCase by inject(
        ProvideDiagnosisKeysUseCase::class.java
    )

    private val updateExposureStateUseCase by inject(
        UpdateExposureStateUseCase::class.java
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED -> {
                GlobalScope.launchUseCase(updateExposureStateUseCase)
            }
            ExposureNotificationClient.ACTION_REQUEST_DIAGNOSIS_KEYS -> {
                GlobalScope.launchUseCase(provideDiagnosisKeysUseCase)
            }
        }
    }
}