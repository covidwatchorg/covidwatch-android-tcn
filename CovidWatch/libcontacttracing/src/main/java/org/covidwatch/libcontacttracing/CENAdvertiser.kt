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
 * Advertises CENs using the serviceData field in the advertisement, as well
 * as provide implementation for the GATT Server to allow for iOS devices to connect
 * and read/write CENs.
 *
 * @param ctx The context this class is constructed in
 * @param advertiser The BluetoothLeAdvertiser in the BluetoothAdapter
 * @param serviceUUID The serviceUUID to advertise
 * @param cenHandler The handler for CENs when they are written to the GATT Server
 * @param cenGenerator The generator for new CENs when advertising
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
                    if (characteristic?.uuid == charactersticUUID) {
                        if (offset != 0) {
                            result = BluetoothGatt.GATT_INVALID_OFFSET
                            return
                        }

                        val newCEN = cenGenerator.generateCEN()
                        cenHandler.handleCEN(newCEN)
                        value = newCEN.number.toUUID()!!.toBytes()

                    } else {
                        result = BluetoothGatt.GATT_FAILURE
                    }
                } catch (exception: Exception) {
                    result = BluetoothGatt.GATT_FAILURE
                    value = null
                } finally {

                    Log.i(
                        TAG,
                        "onCharacteristicReadRequest result=$result device=$device " +
                                "requestId=$requestId offset=$offset characteristic=$characteristic"
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

                        val data = value?.toUUID()

                        if (data == null) {
                            result = BluetoothGatt.GATT_FAILURE
                            return
                        }

                        cenHandler.handleCEN(CEN(data))

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
     * The CENGenerator is called to get a new CEN to advertise, and te CENHandle is called
     * on that CEN
     *
     * @param serviceUUID The UUID of the service to advertise
     */
    fun startAdvertiser(
        serviceUUID: UUID?
    ) {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        // generate and handle cen before advertising
        val cenToAdvertise = cenGenerator.generateCEN()
        cenHandler.handleCEN(cenToAdvertise)

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
     * Changes the CEN to a new random valid CEN in the service data field
     * NOTE: This will also log the CEN and stop/start the advertiser
     */
    fun updateCEN() {
        Log.i(TAG, "Toggling CENAdvertiser to update CEN")
        stopAdvertiser()
        startAdvertiser(serviceUUID)
    }
}
