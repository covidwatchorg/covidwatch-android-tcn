package com.riskre.covidwatch;

import android.app.Application;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;

import com.riskre.covidwatch.ble.BLEAdvertiser;
import com.riskre.covidwatch.ble.BLEScanner;

public class CovidWatchApplication extends Application {

    // TODO expose getters/setters
    // BLE
    public BLEAdvertiser BleAdvertiser;
    public BLEScanner BleScanner;

    // GAT
    public BluetoothManager manager;
    public BluetoothGattServer server;
    public BluetoothGattService service;
}
