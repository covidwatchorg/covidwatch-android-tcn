package org.covidwatch.android.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
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
    private var scanCallback: ScanCallback? = null

    // CONSTANTS
    companion object {
        private const val TAG = "BLEScanner"
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startScanning(serviceUUIDs: Array<UUID>?) {

        var filters: MutableList<ScanFilter?>? = null

        // construct filters from serviceUUIDs
        if (serviceUUIDs != null) {
            filters = ArrayList()
            for (serviceUUID in serviceUUIDs) {
                val filter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(serviceUUID))
                    .build()
                filters.add(filter)
            }
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
            .setReportDelay(10000)
            .build()

        // The scan filter is incredibly important to allow android to run scans
        // in the background
        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback)
            Log.i(TAG, "scan started")
        } else {
            Log.e(TAG, "could not get scanner object")
            // TODO error handling
        }
    }

    fun stopScanning() {
        scanner!!.stopScan(scanCallback)
    }

    init {
        scanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: List<ScanResult>) {
                for (result in results) {

                    Log.w(
                        TAG,
                        "Contact Event number: " + result.scanRecord!!.serviceData.toString()
                    )
                    Log.w(TAG, "Signal strength: " + result.rssi)
                    Log.w(TAG, "Found another human: " + result.device.address)

                    val uuidBytes =
                        result.scanRecord!!.serviceData[ParcelUuid(UUIDs.CONTACT_EVENT_SERVICE)]
                            ?: continue

                    val contactEventNumber: UUID? = uuidBytes.toUUID()

                    if (contactEventNumber != null) {
                        CovidWatchDatabase.databaseWriteExecutor.execute {
                            val dao: ContactEventDAO =
                                CovidWatchDatabase.getInstance(context).contactEventDAO()
                            val cen = ContactEvent(contactEventNumber.toString())
                            dao.insert(cen)
                        }
                    }
                }
            }
        }
    }
}