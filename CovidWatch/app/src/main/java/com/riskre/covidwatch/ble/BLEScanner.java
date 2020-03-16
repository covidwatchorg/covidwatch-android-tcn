package com.riskre.covidwatch.ble;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.BackgroundScanner;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.riskre.covidwatch.UUIDs;

public class BLEScanner {

    private RxBleClient rxBleClient;
    private PendingIntent callbackIntent;
    private static final int SCAN_REQUEST_CODE = 42;

    /**
     * TODO
     *
     * @param ctx
     * @param client
     */
    public BLEScanner(Context ctx, RxBleClient client) {
        rxBleClient = client;
        callbackIntent = PendingIntent.getBroadcast(ctx, SCAN_REQUEST_CODE,
                new Intent(ctx, BLEScanReceiver.class), 0);
    }

    public void startScanning() {
            rxBleClient.getBackgroundScanner().scanBleDeviceInBackground(
                    callbackIntent,
                    new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build(),
                    new ScanFilter.Builder()
                            .setServiceUuid(new ParcelUuid(UUIDs.CONTACT_EVENT_SERVICE))
                            .build()
            );
    }

    public void stopScanning() {
        rxBleClient.getBackgroundScanner().stopBackgroundBleScan(callbackIntent);
    }

    public BackgroundScanner getBackgroundScanner(){
        return rxBleClient.getBackgroundScanner();
    }
}

