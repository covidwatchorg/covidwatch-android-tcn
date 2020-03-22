package org.covidwatch.android.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.utils.UUIDs
import org.covidwatch.android.utils.toBytes
import java.util.*

/**
 * BLEAdvertiser is responsible for advertising the bluetooth services.
 * Only one instance of this class is to be constructed, but its not enforced. (for now)
 * You have been warned!
 */
class BLEAdvertiser(private val context: Context, adapter: BluetoothAdapter) {
    // BLE
    private val advertiser: BluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser

    // CONSTANTS
    companion object {
        private const val TAG = "BLEContactTracing"
    }

    /**
     * Callback when advertisements start and stops
     */
    private val advertisingCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.w("BLE", "Advertising onStartSuccess: $settingsInEffect")
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE", "Advertising onStartFailure: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

    /**
     * Starts the advertiser, with the given UUID. We advertise with MEDIUM power to get
     * reasonable range, but this will need to be experimentally determined later.
     * ADVERTISE_MODE_LOW_LATENCY is a must as the other nodes are not real-time.
     *
     * @param serviceUUID The UUID to advertise the service
     * @param contactEventUUID The UUID that indicates the contact event
     */
    fun startAdvertiser(
        serviceUUID: UUID?,
        contactEventUUID: UUID?
    ) {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .addServiceData(ParcelUuid(serviceUUID), contactEventUUID?.toBytes())
            .build()
        advertiser.startAdvertising(settings, data, advertisingCallback)
    }

    /**
     * Stops all BLE related activity
     */
    fun stopAdvertiser() {
        advertiser.stopAdvertising(advertisingCallback)
    }

    /**
     * Changes the CEN to a new random valid UUID
     * NOTE: This will stop/start the advertiser
     */
    fun changeContactEventNumber() {
        Log.i(TAG, "Changing the contact event identifier...")
        stopAdvertiser()
        val newContactEventIdentifier = UUID.randomUUID()
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(context).contactEventDAO()
            val contactEvent = ContactEvent(newContactEventIdentifier.toString())
            val isCurrentUserSick = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).getBoolean(context.getString(R.string.preference_is_current_user_sick), false)
            contactEvent.wasPotentiallyInfectious = isCurrentUserSick
            dao.insert(contactEvent)
        }
        startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE, newContactEventIdentifier)
    }

}