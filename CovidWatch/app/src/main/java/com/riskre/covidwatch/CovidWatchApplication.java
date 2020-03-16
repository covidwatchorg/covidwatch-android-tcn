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

import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleException;
import com.riskre.covidwatch.ble.BLEAdvertiser;
import com.riskre.covidwatch.ble.BLEContactEvent;
import com.riskre.covidwatch.ble.BLEScanner;

import java.util.Set;
import java.util.UUID;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;


public class CovidWatchApplication extends Application {

    // Constants
    private static final String TAG = CovidWatchApplication.class.getSimpleName();

    // BLE
    private BLEAdvertiser BleAdvertiser;
    private BLEScanner BleScanner;
    private BluetoothGattService service;

    public BLEAdvertiser getBleAdvertiser(){
        return BleAdvertiser;
    }

    public BLEScanner getBleScanner(){
        return BleScanner;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RxBleClient rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.DEBUG)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );

        // inject scanner and advertiser with respective dependencies
        BleScanner = new BLEScanner(this, rxBleClient);
        BleAdvertiser = new BLEAdvertiser(this, BluetoothAdapter.getDefaultAdapter());

        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException) {
                    Log.v("SampleApplication", "Suppressed UndeliverableException: " + throwable.toString());
                    return; // ignore BleExceptions as they were surely delivered at least once
                }
                // add other custom handlers if needed
                throw new RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable);
            }
        });
    }

    // GAT
    private BluetoothManager manager;
    private BluetoothGattServer server;

    BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                int offset, BluetoothGattCharacteristic characteristic) {

            Log.i(TAG, "Tried to read characteristic: " + characteristic);
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            server.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    BLEContactEvent.getNewContactEventNumber());
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattDescriptor descriptor) {

            Log.i(TAG, "Tried to read descriptor: " + descriptor);
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            server.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    BLEContactEvent.getNewContactEventNumber());
        }
    };

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from BLEContactEvent
     *
     * @param Context the context from the running activity
     */
    public void startGattServer(Context context) {

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
