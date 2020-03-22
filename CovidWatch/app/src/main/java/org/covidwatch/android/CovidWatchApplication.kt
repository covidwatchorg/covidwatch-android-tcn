package org.covidwatch.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.covidwatch.android.ble.BLEAdvertiser
import org.covidwatch.android.ble.BLEScanner
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.firestore.LocalContactEventsUploader
import org.covidwatch.android.firestore.PublicContactEventsObserver

class CovidWatchApplication : Application() {

    var bleAdvertiser: BLEAdvertiser? = null
    var bleScanner: BLEScanner? = null

    var sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == getString(R.string.preference_is_current_user_sick)) {
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

        publicContactEventsObserver = PublicContactEventsObserver(this)
        publicContactEventsObserver.startObserving()

        currentUserExposureNotifier =
            CurrentUserExposureNotifier(this)
        currentUserExposureNotifier.startObservingLocalContactEvents()
    }

    companion object {
        private const val TAG = "CovidWatchApplication"
    }
}