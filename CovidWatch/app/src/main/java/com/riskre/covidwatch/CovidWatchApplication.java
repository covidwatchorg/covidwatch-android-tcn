package com.riskre.covidwatch;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.util.UUID;


public class CovidWatchApplication extends Application {

    // Constants
    private static final String TAG = "CovidWatchApplication";

    // BLE
    private BLEContactTracer contactTracer =
            new BLEContactTracer(BluetoothAdapter.getDefaultAdapter());

    // GAT
    private BluetoothManager manager;
    private BluetoothGattServer server;
    private BluetoothGattService service;

    public BLEContactTracer getContactTracer(){
        return contactTracer;
    }

    /**
     *
     *
     */
     public void initGattServer(Context context){
         manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
         server = manager.openGattServer(context, bluetoothGattServerCallback);
         service = new BluetoothGattService(
                        UUID.fromString(getString(R.string.peripheral_service_uuid)),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);
         server.addService(BLEContactEvent.createContactEventService());

     }

    BluetoothGattServerCallback bluetoothGattServerCallback= new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }
    };

}
