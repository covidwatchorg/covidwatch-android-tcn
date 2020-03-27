package org.covidwatch.libcontacttracing

import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.DateFormat
import java.util.*

/**
 * CENScanner
 *
 * Responsible for triggering the CENHandler when there has been contact with an other
 * device running the CENAdvertiser, or simply advertising the same service UUID.
 * The CENHandler callback takes the provided context, and a
 * android.bluetooth.le.ScanResult and will be called on every device found that
 * is advertising with the provided service UUID.
 *
 * @param ctx The android Context this object is constructed in
 * @param scanner The bluetooth adapter to get the bluetoothLeScanner from
 * @param serviceUUID The UUID to listen to in the background
 * @param CENHandler A callback to run on every CEN that was
 *                   discovered advertising the proper service UUID
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CENScanner(
    private val ctx: Context,
    private val scanner: BluetoothLeScanner,
    private val serviceUUID: UUID,
    private val cenHandler: CENHandler
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

            results?.forEach next_scan@{

                // if the scanRecord is null or if there is no data,
                // we skip over that result as it does not provide any benefit
                val scanRecord = it.scanRecord ?: return@next_scan
                val data = scanRecord.serviceData[
                        ParcelUuid(serviceUUID)]?.toUUID()?.toBytes() ?: return@next_scan

                // handle contact event
                cenHandler.handleCEN(CEN(data))
            }
        }
    }

    /**
     * startScanning, will start the low power BLE scanner with the
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
    fun startScanning(serviceUUIDs: Array<UUID>?, reportDelaySeconds: Long) {

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
     * stopScanning stops contact scanning
     */
    fun stopScanning() {
        scanner.stopScan(scanCallback)
        Log.i(TAG, "Stopped Contact Scanning")
    }
}
