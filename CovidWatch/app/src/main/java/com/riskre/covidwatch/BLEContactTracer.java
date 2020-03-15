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
 * TODO class responsibility
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
     * TODO Doc
     */
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.w(TAG, "test"+device.getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // Ignore for now
        }

        @Override
        public void onScanFailed(int errorCode) {
            // Ignore for now
        }
    };

    /**
     * TODO: Doc
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
     * TODO
     * @param serviceUUIDs
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startContactTracingScanner(UUID[] serviceUUIDs){
        List<ScanFilter> filters = null;
        if(serviceUUIDs != null) {
            filters = new ArrayList<>();
            for (UUID serviceUUID : serviceUUIDs) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(serviceUUID))
                        .build();
                filters.add(filter);
            }
        }
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();
        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        }  else {
            Log.e(TAG, "could not get scanner object");
        }
    }

    /**
     * TODO
     * @param serviceUUID
     */
    public void startContactTracingAdvertiser(UUID serviceUUID){
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable( true )
                .build();

        ParcelUuid pUuid = new ParcelUuid(serviceUUID);

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( false )
                .addServiceUuid( pUuid )
                .addServiceData( pUuid, "D".getBytes( Charset.forName( "UTF-8" ) ) )
                .build();

        advertiser.startAdvertising( settings, data, advertisingCallback );
    }

    public void stopContactTracing(){
        scanner.stopScan(scanCallback);
        advertiser.stopAdvertising(advertisingCallback);
    }
}
