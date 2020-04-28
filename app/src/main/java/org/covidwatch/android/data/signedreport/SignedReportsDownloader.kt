package org.covidwatch.android.data.signedreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.work.*
import org.covidwatch.android.data.signedreport.firestore.SignedReportsDownloadWorker
import java.util.concurrent.TimeUnit

private const val PERIODIC_REFRESH = "SignedReportsDownloadWorkerPeriodicRefresh"
private const val ONE_TIME_REFRESH = "SignedReportsDownloadWorkerOneTimeRefresh"

class SignedReportsDownloader(private val workManager: WorkManager) {

    fun schedulePeriodicPublicSignedReportsRefresh() {
        val downloadRequest =
            PeriodicWorkRequestBuilder<SignedReportsDownloadWorker>(1, TimeUnit.HOURS)
                .setConstraints(getRefreshConstraints())
                .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_REFRESH,
            ExistingPeriodicWorkPolicy.REPLACE,
            downloadRequest
        )
    }

    fun executePublicSignedReportsRefresh(): LiveData<Boolean> {
        val downloadRequest = OneTimeWorkRequestBuilder<SignedReportsDownloadWorker>()
            .setConstraints(getRefreshConstraints())
            .build()

        workManager.enqueueUniqueWork(
            ONE_TIME_REFRESH,
            ExistingWorkPolicy.REPLACE,
            downloadRequest
        )

        val stateLiveData = MediatorLiveData<Boolean>()
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(downloadRequest.id)
        stateLiveData.addSource(workInfoLiveData) {
            when (it.state) {
                WorkInfo.State.SUCCEEDED,
                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> { stateLiveData.value = true; stateLiveData.removeSource(workInfoLiveData) }
                else -> stateLiveData.value = false
            }
        }

        return stateLiveData
    }

    private fun getRefreshConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}