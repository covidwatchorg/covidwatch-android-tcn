package com.riskre.covidwatch.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;

import android.os.ParcelUuid;
import android.util.Log;

import com.riskre.covidwatch.CovidWatchApplication;
import com.riskre.covidwatch.utils.UUIDAdapter;
import com.riskre.covidwatch.utils.UUIDs;

import java.util.UUID;

/**
 * BLEAdvertiser is responsible for advertising the bluetooth services.
 * Only one instance of this class is to be constructed, but its not enforced. (for now)
 * You have been warned!
 */
public class BLEAdvertiser {

    // Constants
    private static final String TAG = "BLEContactTracing";

    // BLE
    private BluetoothLeAdvertiser advertiser;
    private Context context;

    /**
     * Initializes the BluetoothLeScanner and BluetoothLeAdvertiser
     * from the given BluetoothAdapter
     *
     * @param ctx The context this object is in
     * @param adapter The default adapter to use for BLE
     */
    public BLEAdvertiser(Context ctx, BluetoothAdapter adapter) {
        context = ctx;
        advertiser = adapter.getBluetoothLeAdvertiser();
    }

    /**
     * Callback when advertisements start and stops
     */
    private final AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.w("BLE", "Advertising success!: " + settingsInEffect);
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
        }
    };

    /**
     * Starts the advertiser, with the given UUID. We advertise with MEDIUM power to get
     * reasonable range, but this will need to be experimentally determined later.
     * ADVERTISE_MODE_LOW_LATENCY is a must as the other nodes are not real-time.
     *
     * @param serviceUUID The UUID to advertise the service
     * @param contactEventUUID The UUID that indicates the contact event
     */
    public void startAdvertiser(UUID serviceUUID, UUID contactEventUUID) {

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(new ParcelUuid(serviceUUID))
                .addServiceData(new ParcelUuid(serviceUUID),
                        new UUIDAdapter().getBytesFromUUID(contactEventUUID))
                .build();

        advertiser.startAdvertising(settings, data, advertisingCallback);
    }

    /**
     * Stops all BLE related activity
     */
    public void stopAdvertiser() {
        advertiser.stopAdvertising(advertisingCallback);
    }

    /**
     * Changes the CEN to a new random valid UUID
     * NOTE: This will stop/start the advertiser
     */
    public void changeContactEventNumber() {
        Log.i(TAG, "Changing the contact event number!");
        this.stopAdvertiser();

        UUID new_cen = UUID.randomUUID();
        ((CovidWatchApplication)context.getApplicationContext()).setCurrentAdvertisingUUID(new_cen);
        this.startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE, new_cen);
    }
}
