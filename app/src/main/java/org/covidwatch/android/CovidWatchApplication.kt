package org.covidwatch.android

import android.app.Application
import androidx.work.*
import org.covidwatch.android.data.signedreport.firestore.SignedReportsDownloadWorker
import org.covidwatch.android.data.signedreport.firestore.SignedReportsUploader
import org.covidwatch.android.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.tcncoalition.tcnclient.TcnClient
import java.util.concurrent.TimeUnit

class CovidWatchApplication : Application() {

    private val tcnManager: CovidWatchTcnManager by inject()
    private val signedReportsUploader: SignedReportsUploader by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        TcnClient.init(tcnManager)
        signedReportsUploader.startUploading()
        schedulePeriodicPublicContactEventsRefresh()
    }

    private fun schedulePeriodicPublicContactEventsRefresh() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest =
            PeriodicWorkRequestBuilder<SignedReportsDownloadWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SignedReportsDownloadWorker.WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            downloadRequest
        )
    }
}
