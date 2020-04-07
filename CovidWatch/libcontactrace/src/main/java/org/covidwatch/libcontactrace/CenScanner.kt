package org.covidwatch.libcontactrace

import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import org.covidwatch.libcontactrace.cen.CenVisitor
import org.covidwatch.libcontactrace.cen.ObservedCen
import java.util.*

/**
 * CENScanner
 *
 * Responsible for triggering the CENHandler when there has been contact with an other
 * device running the CENAdvertiser, or simply advertising the same service UUID.
 * The CENHandler callback gets a CEN for every device that was found to be advertising CENs
 *
 * @param ctx The android Context this object is constructed in
 * @param scanner The bluetooth adapter to get the bluetoothLeScanner from
 * @param serviceUUID The UUID to listen to in the background
 * @param cenVisitor The visitor that handles the appropriate CEN
 *
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CenScanner(
    private val ctx: Context,
    private val scanner: BluetoothLeScanner,
    private val serviceUUID: UUID,
    private val cenVisitor: CenVisitor
) {

    companion object {
        private const val TAG = "libcontacttracing"
        private const val SECONDS_TO_MS = 1000
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
                // as we may have picked up a stray BLE device
                val scanRecord = it.scanRecord ?: return@next_scan
                val data = scanRecord.serviceData[
                        ParcelUuid(serviceUUID)]?.toUUID()?.toBytes() ?: return@next_scan

                // handle contact event
                cenVisitor.visit(ObservedCen(data))
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
     * @param reportDelaySeconds The interval to batch up the results before
     *                      triggering the callback, mainly to avoid multiple calls
     *                      to the handler if the other device is nearby.
     *                      Default is set to 10s
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startScanning(serviceUUIDs: Array<UUID>?, reportDelaySeconds: Long = 10) {

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
            .setReportDelay(reportDelaySeconds * SECONDS_TO_MS)
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
