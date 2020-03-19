package org.covidwatch.android.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import org.covidwatch.android.utils.UUIDs.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC
import org.covidwatch.android.utils.UUIDs.CONTACT_EVENT_IDENTIFIER_DESCRIPTOR
import org.covidwatch.android.utils.UUIDs.CONTACT_EVENT_SERVICE
import java.nio.ByteBuffer
import java.util.*


object BLEContactEvent {
    private val TAG = BLEContactEvent::class.java.simpleName

    /**
     * Return a configured [BluetoothGattService] instance for the
     * Contact Event Service
     *
     * @returns BluetoothGattService for the CEN Orchestration
     */
    fun createContactEventService(): BluetoothGattService {

        // Contact Event Service
        val service = BluetoothGattService(
            CONTACT_EVENT_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        // Contact Event Identifier Characteristic
        val contactEventCharacteristic = BluetoothGattCharacteristic(
            CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Descriptor for the CEN
        val UUIDDescriptor = BluetoothGattDescriptor(
            CONTACT_EVENT_IDENTIFIER_DESCRIPTOR,
            BluetoothGattDescriptor.PERMISSION_READ
        )
        contactEventCharacteristic.addDescriptor(UUIDDescriptor)
        service.addCharacteristic(contactEventCharacteristic)
        return service
    }

    /**
     * Generates a new Contact Event number. Currently generates a random UUID
     * and returns the 16 bytes
     *
     * @return  byte[] containing the UUID in big endian
     */
    val newContactEventNumber: ByteArray
        get() {
            val uuid = UUID.randomUUID()
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            return bb.array()
        }
}