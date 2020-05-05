package org.covidwatch.android.domain

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import org.covidwatch.android.ENStatus
import org.covidwatch.android.functional.Either
import org.covidwatch.android.work.ProvideDiagnosisKeysWork
import org.covidwatch.android.work.ProvideDiagnosisKeysWork.Companion.UNKNOWN_FAILURE

class ProvideDiagnosisKeysUseCase(
    private val workManager: WorkManager,
    dispatchers: AppCoroutineDispatchers
) : UseCase<Unit, Unit>(dispatchers) {
    override suspend fun run(params: Unit?): Either<ENStatus, Unit> {
        val downloadRequest = OneTimeWorkRequestBuilder<ProvideDiagnosisKeysWork>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresDeviceIdle(true)
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            downloadRequest
        )
        val workInfoLiveData = workManager.getWorkInfoById(downloadRequest.id)
        val workInfo = workInfoLiveData.await()

        return when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> Either.Right(Unit)
            else -> Either.Left(
                ENStatus(
                    workInfo.outputData.getInt(
                        ProvideDiagnosisKeysWork.FAILURE,
                        UNKNOWN_FAILURE
                    )
                )
            )
        }
    }

    companion object {
        const val WORK_NAME = "provide_diagnosis_keys"
    }
}