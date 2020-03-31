package org.covidwatch.libcontactrace.gattserver

import android.bluetooth.*
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.covidwatch.libcontactrace.cen.CENGenerator
import org.covidwatch.libcontactrace.cen.CENVisitor
import java.util.*

/**
 * CENGattServerCallback
 *
 * @param characteristicUUID The UUID that that is associated with the CEN Characteristic
 * @param cenGenerator Generates CENs to be served to iOS device
 * @param bluetoothGattServer An reference to the open GattServer (lateinit)
 *        due to circular dependency*
 * @param cenVisitor TODO
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class CENGattServerCallback(
     private val characteristicUUID: UUID,
     private val cenGenerator: CENGenerator,
     private val cenVisitor: CENVisitor
) : BluetoothGattServerCallback() {

    var bluetoothGattServer: BluetoothGattServer? = null;

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int, offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

        var result: Int?
        var value: ByteArray? = null

        when {
            offset != 0 ->{
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

        Log.i(TAG, "CENGattCallback result=$result device=$device " +
                    "requestId=$requestId offset=$offset characteristic=$characteristic"
        )

        bluetoothGattServer?.sendResponse(device, requestId, result, offset, value)
    }
}