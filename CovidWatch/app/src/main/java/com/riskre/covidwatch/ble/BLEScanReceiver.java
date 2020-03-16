package com.riskre.covidwatch.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresApi;

import android.util.Log;

import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.BackgroundScanner;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.riskre.covidwatch.CovidWatchApplication;

import java.util.List;

public class BLEScanReceiver extends BroadcastReceiver {

    @RequiresApi(26 /* Build.VERSION_CODES.O */)
    @Override
    public void onReceive(Context context, Intent intent) {

        CovidWatchApplication application = (CovidWatchApplication) context.getApplicationContext();
        BackgroundScanner backgroundScanner = application.getBleScanner().getBackgroundScanner();

        try {
            final List<ScanResult> scanResults = backgroundScanner.onScanResultReceived(intent);
            Log.i("ScanReceiver", "Scan results received: " + scanResults);
        } catch (BleScanException exception) {
            Log.w("ScanReceiver", "Failed to scan devices", exception);
        }
    }
}