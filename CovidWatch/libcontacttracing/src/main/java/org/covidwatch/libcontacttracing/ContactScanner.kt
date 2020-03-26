package org.covidwatch.libcontacttracing

import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

/**
 * ContactScanner
 *
 * Responsible for triggering the ContactHandler when there has been Contact with an other
 * device running the ContactAdvertiser. The ContactHandler callback takes the provided context,
 * and a android.bluetooth.le.ScanResult and will be called on every device found that
 * is advertising with the provided service UUID.
 *
 * @param ctx The android Context this object is constructed in
 * @param scanner The bluetooth adapter to get the bluetoothLeScanner from
 * @param ContactEventHandler A callback to run on every device that was
 *                               discovered advertising the proper service UUID
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ContactScanner(
    private val ctx: Context,
    private val scanner: BluetoothLeScanner,
    private val ContactEventHandler: (ctx: Context, scanresult: ScanResult) -> Unit
) {

    companion object {
        private const val TAG = "libcontacttracing"
        private const val MIN_TO_MS = 60000
    }

    /**
     * ScanCallback that triggers the ContactHandler
     */
    private var scanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "onScanFailed errorCode=$errorCode")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "onBatchScanResults results=$results")
            results?.forEach { ContactEventHandler(ctx, it) }
        }
    }

    /**
     * startContactScanner, will start the low power BLE scanner with the
     * given report delay and service UUIDs.
     *
     * @param serviceUUIDs An array of UUIDs for the services to listen to in the
     *                     background, NOTE: if this array is empty, the callback
     *                     will not be triggered.
     *
     * @param reportDelaySeconds The interval to batch up the results before triggering the callback,
     *                     mainly to avoid multiple calls if the other device is nearby.
     *                     Default is set to 10s
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startContactScanning(serviceUUIDs: Array<UUID>?, reportDelaySeconds: Long) {

        val scanFilters = serviceUUIDs?.map {
            ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build()
        }

        // we use low power scan mode to conserve battery.
        // NOTE: Although it would be nice to have setMatchMode and setNumOfMatches
        // configured, that requires API Level 23, instead of API Level 21
        // which will cut out 15% of all android users. So until this becomes a problem,
        // its best to not configure those settings and include the most amount of people
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setReportDelay(reportDelaySeconds * MIN_TO_MS)
            .build()

        // The scan filter is incredibly important to allow
        // android to run scans in the background
        scanner.startScan(scanFilters, scanSettings, scanCallback)
        Log.i(TAG, "Started scanning")
    }

    /**
     * stopContactScanner stops contact scanning
     */
    fun stopContactScanning() {
        scanner.stopScan(scanCallback)
        Log.i(TAG, "Stopped Contact Scanning")
    }
}
