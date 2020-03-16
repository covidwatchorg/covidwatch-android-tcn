package com.riskre.covidwatch.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.riskre.covidwatch.UUIDs.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC;
import static com.riskre.covidwatch.UUIDs.CONTACT_EVENT_IDENTIFIER_DESCRIPTOR;
import static com.riskre.covidwatch.UUIDs.CONTACT_EVENT_SERVICE;

public class BLEContactEvent {

    private static final String TAG = BLEContactEvent.class.getSimpleName();

    /**
     * Return a configured {@link BluetoothGattService} instance for the
     * Contact Event Service
     *
     * @returns BluetoothGattService for the CEN Orchestration
     */
    public static BluetoothGattService createContactEventService() {

        // Contact Event Service
        BluetoothGattService service = new BluetoothGattService(
                CONTACT_EVENT_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Contact Event Identifier Characteristic
        BluetoothGattCharacteristic contactEventCharacteristic = new BluetoothGattCharacteristic(
                CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        // Descriptor for the CEN
        BluetoothGattDescriptor UUIDDescriptor = new BluetoothGattDescriptor(
                CONTACT_EVENT_IDENTIFIER_DESCRIPTOR,
                BluetoothGattDescriptor.PERMISSION_READ);


        contactEventCharacteristic.addDescriptor(UUIDDescriptor);
        service.addCharacteristic(contactEventCharacteristic);

        return service;
    }

    /**
     * Generates a new Contact Event number. Currently generates a random UUID
     * and returns the 16 bytes
     *
     * @return  byte[] containing the UUID in big endian
     */
    public static byte[] getNewContactEventNumber() {

            UUID uuid = UUID.randomUUID();

            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());

            return bb.array();
    }
}
