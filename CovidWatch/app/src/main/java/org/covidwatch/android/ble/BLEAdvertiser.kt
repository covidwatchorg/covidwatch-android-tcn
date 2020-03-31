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
import org.covidwatch.android.utils.toBytes
import org.covidwatch.android.utils.toUUID
import java.util.*

class BLEAdvertiser(val context: Context, adapter: BluetoothAdapter) {

    companion object {
        private const val TAG = "BluetoothLeAdvertiser"
    }

    private val advertiser: BluetoothLeAdvertiser? = adapter.bluetoothLeAdvertiser

    private var bluetoothGattServer: BluetoothGattServer? = null

    private var advertisedContactEventIdentifier: UUID? = null

    private val advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.w(TAG, "onStartSuccess settingsInEffect=$settingsInEffect")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "onStartFailure errorCode=$errorCode")
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
                    if (characteristic?.uuid == BluetoothService.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC) {
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
                    if (characteristic?.uuid == BluetoothService.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC) {
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

    fun startAdvertising(
        serviceUUID: UUID?,
        contactEventIdentifier: UUID?
    ) {
        try {
            advertisedContactEventIdentifier = contactEventIdentifier

            val advertiseSettings = AdvertiseSettings.Builder()
                    // Use low latency mode so the chance of being discovered is higher
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    // Use low power so the discoverability range is short
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                    // Use true so devices can connect to our GATT server
                .setConnectable(true)
                    // Advertise forever
                .setTimeout(0)
                .build()

            val advertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(ParcelUuid(serviceUUID))
                .addServiceData(ParcelUuid(serviceUUID), contactEventIdentifier?.toBytes())
//                .addServiceData(ParcelUuid(serviceUUID), ByteArray(20))
                .build()

            (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager).let { bluetoothManager ->

                bluetoothGattServer =
                    bluetoothManager?.openGattServer(context, bluetoothGattServerCallback)

                val service = BluetoothGattService(
                    BluetoothService.CONTACT_EVENT_SERVICE,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
                service.addCharacteristic(
                    BluetoothGattCharacteristic(
                        BluetoothService.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC,
                        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                    )
                )

                bluetoothGattServer?.clearServices()
                bluetoothGattServer?.addService(service)
            }

            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)

            Log.i(TAG, "Started advertising")
        } catch (exception: java.lang.Exception) {
            Log.e(TAG, "Start advertising failed: $exception")
        }
    }

    fun stopAdvertising() {
        try {
            advertiser?.stopAdvertising(advertiseCallback)
            bluetoothGattServer?.apply {
                clearServices()
                close()
            }
            bluetoothGattServer = null

            Log.i(TAG, "Stopped advertising")
        } catch (exception: java.lang.Exception) {
            Log.e(TAG, "Stop advertising failed: $exception")
        }
    }

    /**
     * Changes the CEN to a new random UUID in the service data field
     * NOTE: This will also log the CEN and stop/start the advertiser
     */
    fun changeContactEventIdentifierInServiceDataField() {
        Log.i(TAG, "Changing the contact event identifier in service data field...")
        stopAdvertising()
        val newContactEventIdentifier = UUID.randomUUID()
        logContactEventIdentifier(newContactEventIdentifier)
        startAdvertising(BluetoothService.CONTACT_EVENT_SERVICE, newContactEventIdentifier)
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