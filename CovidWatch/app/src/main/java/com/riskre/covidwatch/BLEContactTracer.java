package com.riskre.covidwatch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * BLEContactTracer is responsible for advertising and scanning for the
 * bluetooth services to make this possible. Only one instance of this
 * class is to be constructed, but its not enforced. You have been warned!
 *
 */
public class BLEContactTracer {

    // Constants
    private static final String TAG = "BLEContactTracing";

    // BLE
    private BluetoothLeScanner scanner;
    private BluetoothLeAdvertiser advertiser;

    /**
     * Initializes the BluetoothLeScanner and BluetoothLeAdvertiser
     * from the given BluetoothAdapter
     *
     * @param adapter The default adapter to use for BLE
     */
    public BLEContactTracer(BluetoothAdapter adapter){
        scanner =  adapter.getBluetoothLeScanner();
        advertiser = adapter.getBluetoothLeAdvertiser();
    }

    /**
     * Callback when scanning start and stops
     */
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // TODO we may or maynot want to use this feature
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult result: results){
                BluetoothDevice device = result.getDevice();
                Log.w(TAG, "Found another device w/ app: "+device.getAddress());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            // TODO error handling
        }
    };

    /**
     * Callback when advertisements start and stops
     */
    private final AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };

    /**
     * Create the necessary filters for the BLE Services and begin
     * scanning for advertisements on those Service UUID's
     *
     * @param serviceUUIDs The UUIDs to look for when scanning
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startContactTracingScanner(UUID[] serviceUUIDs){

        List<ScanFilter> filters = null;

        // construct filters from serviceUUIDs
        if(serviceUUIDs != null) {
            filters = new ArrayList<>();
            for (UUID serviceUUID : serviceUUIDs) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(serviceUUID))
                        .build();
                filters.add(filter);
            }
        }

        // we use low power scan mode to conserve battery,
        // CALLBACK_TYPE_ALL_MATCHES will run the callback for every discovery
        // instead of batching them up. MATCH_MODE_AGGRESSIVE will try to connect
        // even with 1 advertisement.
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(1000)
                .build();

        // The scan filter is incredibly important to allow android to run scans
        // in the background
        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.i(TAG, "scan started");
        }  else {
            Log.e(TAG, "could not get scanner object");
            // TODO error handling
        }
    }

    /**
     * Starts the advertiser, with the given UUID. We advertise with MEDIUM power to get
     * reasonable range, but this will need to be experimentally determined later.
     *
     * ADVERTISE_MODE_LOW_LATENCY is a must as the other nodes are not real-time.
     *
     * @param UUID serviceUUID The UUID to advertise the service
     */
    public void startContactTracingAdvertiser(UUID serviceUUID){

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(new ParcelUuid(serviceUUID))
                .build();

        advertiser.startAdvertising( settings, data, advertisingCallback );
    }

    /**
     * Stops all BLE related activity
     */
    public void stopContactTracing(){
        scanner.stopScan(scanCallback);
        advertiser.stopAdvertising(advertisingCallback);
    }
}
