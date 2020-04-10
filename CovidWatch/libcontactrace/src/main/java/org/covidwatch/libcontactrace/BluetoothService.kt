package org.covidwatch.libcontactrace

import android.app.Notification
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import org.covidwatch.libcontactrace.cen.CenGenerator
import org.covidwatch.libcontactrace.cen.CenVisitor
import java.util.*

class BluetoothService : LifecycleService() {

    private var config: ServiceConfiguration? = null
    private var cenAdvertiser: CenAdvertiser? = null
    private var cenScanner: CenScanner? = null
    private val binder: IBinder = LocalBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        cenAdvertiser?.stopAdvertiser()
        cenScanner?.stopScanning()
        super.onDestroy()
    }


    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    private fun BluetoothAdapter.supportsAdvertising() =
        isMultipleAdvertisementSupported && bluetoothLeAdvertiser != null

    fun start() {
        config?.apply {
            startForeground(NOTIFICATION_ID, foregroundNotification)
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            // advertise CEN
            cenAdvertiser = CenAdvertiser(
                this@BluetoothService,
                bluetoothAdapter.bluetoothLeAdvertiser,
                CONTACT_EVENT_SERVICE,
                CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC,
                cenVisitor,
                cenGenerator
            )

            // scan CENs
            cenScanner = CenScanner(
                this@BluetoothService,
                bluetoothAdapter.bluetoothLeScanner,
                CONTACT_EVENT_SERVICE,
                cenVisitor
            )
            cenAdvertiser?.startAdvertiser(CONTACT_EVENT_SERVICE)
            cenScanner?.startScanning(arrayOf(CONTACT_EVENT_SERVICE), 10)
        }
    }

    fun configure(configuration: ServiceConfiguration) {
        this.config = configuration
    }

    fun updateCen() {
        cenAdvertiser?.updateCEN()
    }

    fun startAdvertiser() {
        cenAdvertiser?.startAdvertiser(CONTACT_EVENT_SERVICE)
    }

    fun stopAdvertiser() {
        cenAdvertiser?.stopAdvertiser()
        cenScanner?.stopScanning()
    }

    data class ServiceConfiguration(
        val cenGenerator: CenGenerator,
        val cenVisitor: CenVisitor,
        var foregroundNotification: Notification
    )

    inner class LocalBinder : Binder() {
        val service = this@BluetoothService
    }

    companion object {
        const val NOTIFICATION_ID = 0

        // The string representation of the UUID for the primary peripheral service
        val CONTACT_EVENT_SERVICE: UUID = UUID.fromString("0000C019-0000-1000-8000-00805F9B34FB")

        // The string representation of the UUID for the contact event identifier characteristic
        val CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC: UUID =
            UUID.fromString("D61F4F27-3D6B-4B04-9E46-C9D2EA617F62")
    }
}
