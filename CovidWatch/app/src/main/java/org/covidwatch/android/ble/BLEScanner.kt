package org.covidwatch.android.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.utils.UUIDs
import org.covidwatch.android.utils.toUUID
import java.util.*

class BLEScanner(ctx: Context, adapter: BluetoothAdapter) {

    // BLE
    private val scanner: BluetoothLeScanner? = adapter.bluetoothLeScanner

    // CONTEXT
    var context: Context = ctx

    // CALLBACKS
    private var scanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.i(TAG, "onScanFailed errorCode=$errorCode")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.i(TAG, "onBatchScanResults results=$results")
            results?.forEach { handleScanResult(it) }
        }

        private fun handleScanResult(result: ScanResult) {
            val scanRecord = result.scanRecord ?: return

            val contactEventIdentifier =
                scanRecord.serviceData[ParcelUuid(UUIDs.CONTACT_EVENT_SERVICE)]?.toUUID()

            if (contactEventIdentifier == null) {
                Log.i(
                    TAG,
                    "Scan result device.address=${result.device.address} RSSI=${result.rssi} CEI=N/A"
                )
                // TODO: Handle case when CEI cannot be extracted from scan record.
            } else {
                Log.i(
                    TAG,
                    "Scan result device.address=${result.device.address} RSSI=${result.rssi} CEI=${contactEventIdentifier.toString()
                        .toUpperCase()}"
                )
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    val dao: ContactEventDAO =
                        CovidWatchDatabase.getInstance(context).contactEventDAO()
                    val contactEvent = ContactEvent(contactEventIdentifier.toString())
                    val isCurrentUserSick = context.getSharedPreferences(
                        context.getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE
                    ).getBoolean(
                        context.getString(R.string.preference_is_current_user_sick),
                        false
                    )
                    contactEvent.wasPotentiallyInfectious = isCurrentUserSick
                    dao.insert(contactEvent)
                }
            }
        }
    }

    // CONSTANTS
    companion object {
        private const val TAG = "BLEScanner"
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startScanning(serviceUUIDs: Array<UUID>?) {

        val scanner = scanner ?: return

        val scanFilters = serviceUUIDs?.map {
            ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build()
        }

        // we use low power scan mode to conserve battery,
        // CALLBACK_TYPE_ALL_MATCHES will run the callback for every discovery
        // instead of batching them up. MATCH_MODE_AGGRESSIVE will try to connect
        // even with 1 advertisement.
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(1000)
            .build()

        // The scan filter is incredibly important to allow android to run scans
        // in the background
        scanner.startScan(scanFilters, scanSettings, scanCallback)
        Log.i(TAG, "Started scanning")
    }

    fun stopScanning() {
        scanner?.stopScan(scanCallback)
        Log.i(TAG, "Stopped scanning")
    }

}