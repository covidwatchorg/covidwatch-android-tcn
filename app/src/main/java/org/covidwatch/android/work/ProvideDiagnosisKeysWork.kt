package org.covidwatch.android.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.data.CovidExposureSummary
import org.covidwatch.android.data.asTemporaryExposureKey
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.util.Date

class ProvideDiagnosisKeysWork(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val exposureNotification by inject(ExposureNotificationManager::class.java)
    private val diagnosisRepository by inject(PositiveDiagnosisRepository::class.java)
    private val preferenceStorage by inject(PreferenceStorage::class.java)

    @SuppressLint("BinaryOperationInTimber")
    override suspend fun doWork(): Result {
        val maxKeysResult = exposureNotification.getMaxDiagnosisKeys()
        val maxKeys = maxKeysResult.right ?: return failure(maxKeysResult.left)

        val diagnosisKeys = diagnosisRepository.diagnosisKeys(Date())
        Timber.d("Adding ${diagnosisKeys.size} positive diagnoses to exposure notification framework")

        diagnosisKeys.forEach { diagnosis ->
            diagnosis.diagnosisKeys
                .map { it.asTemporaryExposureKey() }
                .chunked(maxKeys)
                .forEach {
                    Timber.d(
                        "Processing the ${it.size} diagnosis keys of the positive diagnosis " +
                            "with permission number ${diagnosis.phaPermissionNumber}"
                    )

                    val result = exposureNotification.provideDiagnosisKeys(it)
                    result.left?.let { status -> return failure(status) }
                }
        }

        val exposureSummaryResult = exposureNotification.getExposureSummary()
        val exposureSummary =
            exposureSummaryResult.right ?: return failure(exposureSummaryResult.left)

        preferenceStorage.exposureSummary = CovidExposureSummary(
            exposureSummary.daysSinceLastExposure,
            exposureSummary.matchedKeyCount,
            exposureSummary.maximumRiskScore
        )

        return Result.success()
    }

    private fun failure(status: Int?) =
        Result.failure(Data.Builder().putInt(FAILURE, status ?: UNKNOWN_FAILURE).build())

    companion object {
        const val FAILURE = "status"
        const val UNKNOWN_FAILURE = -1
    }
}