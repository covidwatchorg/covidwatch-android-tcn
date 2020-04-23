package org.covidwatch.android.requirements.test_environment

import android.app.Application
import android.content.Context
import androidx.work.*
import io.mockk.mockk
import org.covidwatch.android.data.contactevent.ContactEventsDownloadWorker
import org.covidwatch.android.data.contactevent.LocalContactEventsUploader
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class TestApplication {

    val application: Application = mockk(relaxed = true)
    val applicationContext: Context = mockk(relaxed = true)

    private val localContactEventsUploader: LocalContactEventsUploader

    init {
        startKoin {
            androidContext(applicationContext)
            modules(testAppModule)
        }

        localContactEventsUploader = LocalContactEventsUploader(application)
        localContactEventsUploader.startUploading()

        schedulePeriodicPublicContactEventsRefresh()
    }

    private fun schedulePeriodicPublicContactEventsRefresh() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest =
            PeriodicWorkRequestBuilder<ContactEventsDownloadWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            ContactEventsDownloadWorker.WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            downloadRequest
        )
    }
}