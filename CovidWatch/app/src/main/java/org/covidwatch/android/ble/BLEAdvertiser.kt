package org.covidwatch.android.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.utils.UUIDs
import org.covidwatch.android.utils.toBytes
import org.covidwatch.android.utils.toUUID
import java.util.*

/**
 * BLEAdvertiser is responsible for advertising the bluetooth services.
 * Only one instance of this class is to be constructed, but its not enforced. (for now)
 * You have been warned!
 */
class BLEAdvertiser(private val context: Context, private val adapter: BluetoothAdapter) {
    // BLE
    private val advertiser: BluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var advertisedContactEventUUID: UUID? = null

    // CONSTANTS
    companion object {
        private const val TAG = "BLEAdvertiser"
    }

    /**
     * Callback when advertisements start and stops
     */
    private val advertisingCallback: AdvertiseCallback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.w(TAG, "onStartSuccess settingsInEffect=$settingsInEffect")
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "onStartFailure errorCode=$errorCode")
            super.onStartFailure(errorCode)
        }
    }

    private var bluetoothGattServerCallback: BluetoothGattServerCallback =
        object : BluetoothGattServerCallback() {

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                Log.i(TAG, "onServiceAdded status=$status service=$service")
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                var result = BluetoothGatt.GATT_SUCCESS
                var value: ByteArray? = null

                try {
                    if (characteristic?.uuid == UUIDs.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC) {
                        if (offset != 0) {
                            result = BluetoothGatt.GATT_INVALID_OFFSET
                            return
                        }

                        val newContactEventIdentifier = UUID.randomUUID()

                        logContactEventIdentifier(newContactEventIdentifier)

                        value = newContactEventIdentifier.toBytes()
                    } else {
                        result = BluetoothGatt.GATT_FAILURE
                    }
                } catch (exception: Exception) {
                    result = BluetoothGatt.GATT_FAILURE
                    value = null
                } finally {
                    Log.i(
                        TAG,
                        "onCharacteristicReadRequest result=$result device=$device requestId=$requestId offset=$offset characteristic=$characteristic"
                    )
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        result,
                        offset,
                        value
                    )
                }
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                super.onCharacteristicWriteRequest(
                    device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value
                )

                var result = BluetoothGatt.GATT_SUCCESS
                try {
                    if (characteristic?.uuid == UUIDs.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC) {
                        if (offset != 0) {
                            result = BluetoothGatt.GATT_INVALID_OFFSET
                            return
                        }

                        val newContactEventIdentifier = value?.toUUID()
                        if (newContactEventIdentifier == null) {
                            result = BluetoothGatt.GATT_FAILURE
                            return
                        }

                        logContactEventIdentifier(newContactEventIdentifier)
                    } else {
                        result = BluetoothGatt.GATT_FAILURE
                    }
                } catch (exception: Exception) {
                    result = BluetoothGatt.GATT_FAILURE
                } finally {
                    Log.i(
                        TAG,
                        "onCharacteristicWriteRequest result=$result device=$device requestId=$requestId characteristic=$characteristic preparedWrite=$preparedWrite responseNeeded=$responseNeeded offset=$offset value=$value"
                    )
                    if (responseNeeded) {
                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            result,
                            offset,
                            null
                        )
                    }
                }
            }
        }

    /**
     * Starts the advertiser, with the given UUID. We advertise with MEDIUM power to get
     * reasonable range, but this will need to be experimentally determined later.
     * ADVERTISE_MODE_LOW_LATENCY is a must as the other nodes are not real-time.
     *
     * @param serviceUUID The UUID to advertise the service
     * @param contactEventUUID The UUID that indicates the contact event
     */
    fun startAdvertiser(
        serviceUUID: UUID?,
        contactEventUUID: UUID?
    ) {
        advertisedContactEventUUID = contactEventUUID

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

//        val testServiceDataMaxLength = ByteArray(20)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .addServiceData(ParcelUuid(serviceUUID), contactEventUUID?.toBytes())
//            .addServiceData(ParcelUuid(serviceUUID), testServiceDataMaxLength)
            .build()

        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).let { bluetoothManager ->

            bluetoothGattServer =
                bluetoothManager.openGattServer(context, bluetoothGattServerCallback)

            val service = BluetoothGattService(
                UUIDs.CONTACT_EVENT_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
            service.addCharacteristic(
                BluetoothGattCharacteristic(
                    UUIDs.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC,
                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                )
            )

            bluetoothGattServer?.clearServices()
            bluetoothGattServer?.addService(service)
        }

        advertiser.startAdvertising(settings, data, advertisingCallback)
    }

    /**
     * Stops all BLE related activity
     */
    fun stopAdvertiser() {
        advertiser.stopAdvertising(advertisingCallback)
        bluetoothGattServer?.clearServices()
        bluetoothGattServer?.close()
        bluetoothGattServer = null
    }

    /**
     * Changes the CEI to a new random valid UUID in the service data field
     * NOTE: This will also log the CEI and stop/start the advertiser
     */
    fun changeContactEventIdentifierInServiceDataField() {
        Log.i(TAG, "Changing the contact event identifier in service data field...")
        stopAdvertiser()
        val newContactEventIdentifier = UUID.randomUUID()
        logContactEventIdentifier(newContactEventIdentifier)
        startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE, newContactEventIdentifier)
    }

    fun logContactEventIdentifier(identifier: UUID) {
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(context).contactEventDAO()
            val contactEvent = ContactEvent(identifier.toString())
            val isCurrentUserSick = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).getBoolean(context.getString(R.string.preference_is_current_user_sick), false)
            contactEvent.wasPotentiallyInfectious = isCurrentUserSick
            dao.insert(contactEvent)
        }
    }

}