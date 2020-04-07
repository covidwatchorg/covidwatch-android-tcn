package org.covidwatch.libcontactrace

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import org.covidwatch.libcontactrace.cen.CenGenerator
import org.covidwatch.libcontactrace.cen.CenVisitor
import java.util.*

/**
 * CENAdvertiserConfig
 *
 * The CENAdvertiser advertises CENs using the serviceData field in the advertisement, as well
 * as provide implementation for the GATT Server to allow for iOS devices to connect
 * and read/write CENs.
 *
 * The CENAdvertiserConfig object used to configure the CENAdvertiser is defined as follows.
 *
 * @param ctx The context this class is constructed in
 * @param advertiser The BluetoothLeAdvertiser in the BluetoothAdapter
 * @param serviceUUID The UUID of the service
 * @param characteristicUUID The UUID of the characteristic
 * @param cenVisitor The visitor that will visit the CENs
 * @param cenGenerator The generator for new CENs when advertising
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CenAdvertiser(
    private val ctx: Context,
    private val advertiser: BluetoothLeAdvertiser,
    private val serviceUUID: UUID,
    private val characteristicUUID: UUID,
    private val cenVisitor: CenVisitor,
    private val cenGenerator: CenGenerator
) {

    var bluetoothGattServer: BluetoothGattServer? = null

    /**
     * Starts the advertiser, with the given service UUID. We advertise with MEDIUM power to get
     * reasonable range, but this will need to be experimentally determined later.
     *
     * ADVERTISE_MODE_LOW_LATENCY is a must as the other nodes are not real-time.
     *
     * The GATT Server is created and a handle to it is returned
     *
     * @param serviceUUID The UUID of the service to advertise
     * @returns the open GATT Server
     */
    fun startAdvertiser(
        serviceUUID: UUID?
    ) {

        // create the settings
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val gencen = cenGenerator.generate()
        cenVisitor.visit(gencen)

        // advertisement data
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .addServiceData(ParcelUuid(serviceUUID), gencen.data)
            .build()

        // create the GATTServer and open it
        initBleGattServer(
            (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager),
            serviceUUID
        )

        advertiser.startAdvertising(settings, data, advertisingCallback)
    }

    private fun initBleGattServer(
        bluetoothManager: BluetoothManager,
        serviceUUID: UUID?
    ): Boolean? {
        bluetoothGattServer = bluetoothManager.openGattServer(ctx,
            object : BluetoothGattServerCallback() {
                override fun onCharacteristicReadRequest(
                    device: BluetoothDevice?,
                    requestId: Int,
                    offset: Int,
                    characteristic: BluetoothGattCharacteristic?
                ) {
                    super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                    val result: Int?
                    var value: ByteArray? = null

                    when {
                        offset != 0 -> {
                            result = BluetoothGatt.GATT_INVALID_OFFSET
                        }
                        characteristic?.uuid == characteristicUUID -> {
                            val cen = cenGenerator.generate()
                            cenVisitor.visit(cen)
                            value = cen.data
                            result = BluetoothGatt.GATT_SUCCESS
                        }
                        else -> result = BluetoothGatt.GATT_FAILURE
                    }

                    Log.i(
                        ContentValues.TAG, "CENGattCallback result=$result device=$device " +
                                "requestId=$requestId offset=$offset characteristic=$characteristic"
                    )

                    bluetoothGattServer?.sendResponse(device, requestId, result, offset, value)
                }
            })

        bluetoothGattServer?.clearServices()

        // add CEN service
        val service = BluetoothGattService(
            serviceUUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        service.addCharacteristic(
            BluetoothGattCharacteristic(
                characteristicUUID,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
            )
        )

        return bluetoothGattServer?.addService(service)
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

    companion object {
        private const val TAG = "libcontacttracing"
    }
}
