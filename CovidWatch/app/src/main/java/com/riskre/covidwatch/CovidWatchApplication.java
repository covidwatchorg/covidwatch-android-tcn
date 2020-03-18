package com.riskre.covidwatch;

import android.app.Application;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;

import com.riskre.covidwatch.ble.BLEAdvertiser;
import com.riskre.covidwatch.ble.BLEScanner;

import java.util.UUID;

public class CovidWatchApplication extends Application {

    // BLE
    private BLEAdvertiser BleAdvertiser;
    private BLEScanner BleScanner;
    public static UUID currentAdvertisingUUID;

    // GAT
    private BluetoothManager manager;
    private BluetoothGattServer server;
    private BluetoothGattService service;

    public UUID getCurrentAdvertisingUUID() {
        return currentAdvertisingUUID;
    }

    public void setCurrentAdvertisingUUID(UUID uuid) {
        currentAdvertisingUUID = uuid;
    }

    public BLEAdvertiser getBleAdvertiser() {
        return BleAdvertiser;
    }

    public void setBleAdvertiser(BLEAdvertiser bleAdvertiser) {
        BleAdvertiser = bleAdvertiser;
    }

    public BLEScanner getBleScanner() {
        return BleScanner;
    }

    public void setBleScanner(BLEScanner bleScanner) {
        BleScanner = bleScanner;
    }

    public BluetoothManager getManager() {
        return manager;
    }

    public void setManager(BluetoothManager manager) {
        this.manager = manager;
    }

    public BluetoothGattServer getServer() {
        return server;
    }

    public void setServer(BluetoothGattServer server) {
        this.server = server;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }
}
