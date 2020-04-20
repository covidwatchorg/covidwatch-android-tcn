package org.covidwatch.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.contactevent.ContactEventsDownloadWorker
import org.covidwatch.android.data.contactevent.LocalContactEventsUploader
import org.covidwatch.android.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit


class CovidWatchApplication : Application() {

    private lateinit var localContactEventsUploader: LocalContactEventsUploader

    private var sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                getString(R.string.preference_is_current_user_sick) -> {
                    val isCurrentUserSick = sharedPreferences.getBoolean(
                        getString(R.string.preference_is_current_user_sick),
                        false
                    )
                    Log.i(TAG, "Current user is sick=$isCurrentUserSick")
                    if (isCurrentUserSick) {
                        Log.i(TAG, "Marking all local contact events as potentially infectious...")
                        CovidWatchDatabase.databaseWriteExecutor.execute {
                            CovidWatchDatabase.getInstance(this)
                                .contactEventDAO()
                                .markAllAsPotentiallyInfectious()
                            Log.i(TAG, "Marked all local contact events as potentially infectious")
                        }
                    }
                }
            }
        }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        localContactEventsUploader = LocalContactEventsUploader(this)
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

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ContactEventsDownloadWorker.WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            downloadRequest
        )
    }

    companion object {
        private const val TAG = "CovidWatchApplication"
    }
}
