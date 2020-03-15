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
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class CovidWatchApplication extends Application {

    // Constants
    private static final String TAG = CovidWatchApplication.class.getSimpleName();

    // BLE
    private BLEContactTracer contactTracer =
            new BLEContactTracer(BluetoothAdapter.getDefaultAdapter());
    // GAT
    private BluetoothManager manager;
    private BluetoothGattServer server;

    BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            Log.i(TAG, "Tried to read charactersitic!" + characteristic);
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, BLEContactEvent.getNewContactEventNumber());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.i(TAG, "Tried to read descriptor!" + descriptor);
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            server.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    BLEContactEvent.getNewContactEventNumber());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }
    };

    private BluetoothGattService service;
    private Set<BluetoothDevice> registered_devices = new HashSet<>();

    public BLEContactTracer getContactTracer() {
        return contactTracer;
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from BLEContactEvent
     *
     * @param Context the context from the running activity
     */
    public void initGattServer(Context context) {

        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        server = manager.openGattServer(context, bluetoothGattServerCallback);

        service = new BluetoothGattService(
                UUID.fromString(getString(R.string.peripheral_service_uuid)),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        server.addService(BLEContactEvent.createContactEventService());

    }

    /**
     * Shut down the GATT server.
     */
    private void stopServer() {
        if (server == null)
            return;

        server.close();
    }

}
