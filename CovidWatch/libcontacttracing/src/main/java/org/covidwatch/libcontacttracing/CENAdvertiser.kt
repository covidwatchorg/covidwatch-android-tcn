package org.covidwatch.libcontacttracing

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

/**
 * CENAdvertiser
 *
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CENAdvertiser(
    private val ctx: Context,
    private val advertiser: BluetoothLeAdvertiser,
    private val serviceUUID: UUID,
    private val charactersticUUID: UUID,
    private val cenHandler: CENHandler,
    private val cenGenerator: CENGenerator
) {
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var advertisedCEN: CEN? = null

    companion object {
        private const val TAG = "libcontacttracing"
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
                    if (characteristic?.uuid == serviceUUID) {
                        if (offset != 0) {
                            result = BluetoothGatt.GATT_INVALID_OFFSET
                            return
                        }

                        cenHandler(cenGenerator.generateCEN())

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
                    if (characteristic?.uuid == charactersticUUID) {
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
                        "onCharacteristicWriteRequest result=$result device=$device" +
                                "requestId=$requestId characteristic=$characteristic " +
                                "preparedWrite=$preparedWrite responseNeeded=$responseNeeded" +
                                "offset=$offset value=$value"
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
     * Starts the advertiser, with the given service UUID. We advertise with MEDIUM power to get
     * reasonable range, but this will need to be experimentally determined later.
     * ADVERTISE_MODE_LOW_LATENCY is a must as the other nodes are not real-time.
     *
     * The CENGenerator is called to get a new CEN to advertise
     *
     * @param serviceUUID The UUID of the service to advertise
     * @param cenToAdvertise The CEN to advertise with the service UUID
     */
    fun startAdvertiser(
        serviceUUID: UUID?,
        cenToAdvertise: CEN
    ) {
        advertisedCEN = contactEventUUID

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .addServiceData(ParcelUuid(serviceUUID), cenToAdvertise.number)
            .build()

        (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).let { bluetoothManager ->

            bluetoothGattServer = bluetoothManager.openGattServer(ctx, bluetoothGattServerCallback)

            val service = BluetoothGattService(
                serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY
            )

            service.addCharacteristic(
                BluetoothGattCharacteristic(
                    charactersticUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ
                            or BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_READ
                            or BluetoothGattCharacteristic.PERMISSION_WRITE
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
        startAdvertiser(serviceUUID,)
    }
}
