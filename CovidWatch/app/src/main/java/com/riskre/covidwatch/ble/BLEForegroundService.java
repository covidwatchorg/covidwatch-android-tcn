package com.riskre.covidwatch.ble;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.polidea.rxandroidble2.RxBleClient;
import com.riskre.covidwatch.CovidWatchApplication;
import com.riskre.covidwatch.MainActivity;
import com.riskre.covidwatch.R;
import com.riskre.covidwatch.UUIDs;

import java.util.UUID;


public class BLEForegroundService extends Service {

    // CONSTANTS
    private static final String TAG = "BLEForegroundService";
    private final String CHANNEL_ID = "CovidBluetoothContactChannel";

    // APP
    CovidWatchApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = (CovidWatchApplication) this.getApplication();
        app.BleAdvertiser = new BLEAdvertiser(this, BluetoothAdapter.getDefaultAdapter());
        app.BleScanner = new BLEScanner(this, RxBleClient.create(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CovidWatch passively logging")
                .setContentText("CovidWatch")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(6, notification);

        startGattServer(this);
        app.BleAdvertiser.startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE, UUID.randomUUID());
        app.BleScanner.startScanning();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopServer();
        app.BleAdvertiser.stopAdvertiser();
        app.BleScanner.stopScanning();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This notification channel is only required for android versions above
     * android O. This creates the necessary notification channel for the foregroundService
     * to function.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from BLEContactEvent
     *
     * @param context the context from the running activity
     */
    public void startGattServer(Context context) {

        app.manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        app.server = app.manager.openGattServer(context, bluetoothGattServerCallback);

        app.service = new BluetoothGattService(
                UUID.fromString(getString(R.string.peripheral_service_uuid)),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        app.server.addService(BLEContactEvent.createContactEventService());

    }

    /**
     * Shut down the GATT server.
     */
    private void stopServer() {
        if (app.server == null)
            return;
        app.server.close();
    }

    /**
     * Callback to run when a client connects to this devices gatt server
     */
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
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattDescriptor descriptor) {

            Log.i(TAG, "Tried to read descriptor: " + descriptor);
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            app.server.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    BLEContactEvent.getNewContactEventNumber());
        }
    };
}
