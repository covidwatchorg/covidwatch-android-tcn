package org.covidwatch.android

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import org.covidwatch.android.ble.BLEAdvertiser
import org.covidwatch.android.ble.BLEForegroundService
import org.covidwatch.android.ble.BLEScanner
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.firestore.ContactEventsDownloadWorker
import org.covidwatch.android.firestore.LocalContactEventsUploader
import org.covidwatch.android.firestore.PublicContactEventsObserver
import java.util.concurrent.TimeUnit

class CovidWatchApplication : Application() {

    var bleAdvertiser: BLEAdvertiser? = null
    var bleScanner: BLEScanner? = null

    var sharedPreferenceChangeListener =
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
                getString(R.string.preference_is_contact_event_logging_enabled) -> {
                    val isContactEventLoggingEnabled = sharedPreferences.getBoolean(
                        getString(R.string.preference_is_contact_event_logging_enabled),
                        false
                    )
                    configureAdvertising(isContactEventLoggingEnabled)
                }
            }
        }

    private fun configureAdvertising(enabled: Boolean) {
        Intent(this, BLEForegroundService::class.java).also { intent ->
            if (enabled) {
                startService(intent)
            } else {
                stopService(intent)
            }
        }
    }

    private lateinit var localContactEventsUploader: LocalContactEventsUploader

    private lateinit var publicContactEventsObserver: PublicContactEventsObserver

    private lateinit var currentUserExposureNotifier: CurrentUserExposureNotifier

    override fun onCreate() {
        super.onCreate()

        getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        localContactEventsUploader = LocalContactEventsUploader(this)
        localContactEventsUploader.startUploading()

        schedulePeriodicPublicContactEventsRefresh()

        currentUserExposureNotifier =
            CurrentUserExposureNotifier(this)
        currentUserExposureNotifier.startObservingLocalContactEvents()

        val isContactEventLoggingEnabled = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ).getBoolean(
            getString(R.string.preference_is_contact_event_logging_enabled),
            false
        )
        configureAdvertising(isContactEventLoggingEnabled)
    }

    private fun schedulePeriodicPublicContactEventsRefresh() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest =
            PeriodicWorkRequestBuilder<ContactEventsDownloadWorker>(3, TimeUnit.HOURS)
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