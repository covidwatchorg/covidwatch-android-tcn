package org.covidwatch.android

import android.content.Context
import android.content.SharedPreferences
import org.covidwatch.android.ble.BluetoothManager
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.tcncoalition.tcnclient.TcnKeys
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothServiceCallback
import org.tcncoalition.tcnclient.toUUID

//TODO: Use constants for preference keys
class TcnManager(
    private val context: Context,
    private val tcnKeys: TcnKeys,
    private val bluetoothManager: BluetoothManager
) {

    private var sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                context.getString(R.string.preference_is_contact_event_logging_enabled) -> {
                    val isContactEventLoggingEnabled = sharedPreferences.getBoolean(
                        context.getString(R.string.preference_is_contact_event_logging_enabled),
                        true
                    )
                    configureContactTracing(isContactEventLoggingEnabled)
                }
            }
        }

    fun start() {
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        bluetoothManager.setCallback(object : TcnBluetoothServiceCallback {
            override fun generateTcn() = tcnKeys.generateTcn()

            override fun onTcnFound(tcn: ByteArray, estimatedDistance: Double?) = logTcn(tcn)
        })
    }

    private fun configureContactTracing(enabled: Boolean) {
        if (enabled) {
            bluetoothManager.startService()
        } else {
            bluetoothManager.stopService()
        }
    }

    private fun logTcn(tcn: ByteArray) {
        CovidWatchDatabase.databaseWriteExecutor.execute {

            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(context).contactEventDAO()
            val contactEvent = ContactEvent(tcn.toUUID().toString())
            val isCurrentUserSick = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).getBoolean(context.getString(R.string.preference_is_current_user_sick), false)
            contactEvent.wasPotentiallyInfectious = isCurrentUserSick
            dao.insert(contactEvent)
        }
    }
}