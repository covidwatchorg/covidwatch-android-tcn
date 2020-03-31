package org.covidwatch.android.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.firestore.FirestoreConstants
import org.covidwatch.android.utils.toUUID
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class BLEScanner(val context: Context, adapter: BluetoothAdapter) {

    companion object {
        private const val TAG = "BluetoothLeScanner"
    }

    private val scanner: BluetoothLeScanner? = adapter.bluetoothLeScanner

    var isScanning: Boolean = false

    private var handler = Handler()

    private var scanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.i(TAG, "onScanFailed errorCode=$errorCode")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.i(TAG, "onBatchScanResults results=$results")
            results?.forEach { processScanResult(it) }
        }

        private fun processScanResult(result: ScanResult) {
            val scanRecord = result.scanRecord ?: return

            val contactEventIdentifier =
                scanRecord.serviceData[ParcelUuid(BluetoothService.CONTACT_EVENT_SERVICE)]?.toUUID()

            if (contactEventIdentifier == null) {
                Log.i(
                    TAG,
                    "Scan result device.address=${result.device.address} RSSI=${result.rssi} CEN=N/A"
                )
            } else {
                Log.i(
                    TAG,
                    "Scan result device.address=${result.device.address} RSSI=${result.rssi} CEN=${contactEventIdentifier.toString()
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startScanning(serviceUUIDs: Array<UUID>?) {
        if (isScanning) return

        try {
//            val scanFilters = serviceUUIDs?.map {
//                ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build()
//            }
            val scanFilters = null

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(1000)
                .build()

            isScanning = true
            scanner?.startScan(scanFilters, scanSettings, scanCallback)
            Log.i(TAG, "Started scan")
        } catch (exception: Exception) {
            Log.e(TAG, "Start scan failed: $exception")
        }

        // Bug workaround: Restart periodically so the Bluetooth daemon won't get into a borked state
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isScanning) {
                Log.i(TAG, "Restarting scan...")
                stopScanning()
                startScanning(serviceUUIDs)
            }
        }, TimeUnit.SECONDS.toMillis(10))
    }

    fun stopScanning() {
        if (!isScanning) return

        try {
            isScanning = false
            scanner?.stopScan(scanCallback)
            Log.i(TAG, "Stopped scan")
        } catch (exception: Exception) {
            Log.e(TAG, "Stop scan failed: $exception")
        }
    }

}