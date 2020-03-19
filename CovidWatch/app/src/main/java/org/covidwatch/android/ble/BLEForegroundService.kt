package org.covidwatch.android.ble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.covidwatch.android.CovidWatchApplication
import org.covidwatch.android.MainActivity
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.ContactEventViewModel
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.utils.UUIDs
import java.util.*

class BLEForegroundService : Service() {
    private val CHANNEL_ID = "CovidBluetoothContactChannel"
    private val CONTACT_EVENT_NUMBER_INTERVAL_MIN = 1
    private val MS_TO_MIN = 60000

    // APP
    var app: CovidWatchApplication? = null
    var timer: Timer? = null
    private val cenViewModel: ContactEventViewModel? = null

    override fun onCreate() {
        super.onCreate()
        val application = (this.application as? CovidWatchApplication)
        if (application != null) {
            app = application
            app?.bleAdvertiser = BLEAdvertiser(this, BluetoothAdapter.getDefaultAdapter())
            app?.bleScanner = BLEScanner(this, BluetoothAdapter.getDefaultAdapter())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.putExtra("toggle", true)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CovidWatch passively logging")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(6, notification)

        // scheduler a new timer to start changing the contact event numbers
        timer = Timer()
        timer!!.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    app?.bleAdvertiser?.changeContactEventNumber()
                }
            },
            MS_TO_MIN * CONTACT_EVENT_NUMBER_INTERVAL_MIN.toLong(),
            MS_TO_MIN * CONTACT_EVENT_NUMBER_INTERVAL_MIN.toLong()
        )

        // generate random UUID, update the global Advertising UUID and start advertising
        val newContactEventUUID = UUID.randomUUID()
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(this).contactEventDAO()
            val cen = ContactEvent(newContactEventUUID.toString())
            dao.insert(cen)
        }
        app?.bleAdvertiser?.startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE, newContactEventUUID)
        app?.bleScanner?.startScanning(arrayOf<UUID>(UUIDs.CONTACT_EVENT_SERVICE))
        return START_STICKY
    }

    override fun onDestroy() {
        app?.bleAdvertiser?.stopAdvertiser()
        app?.bleScanner?.stopScanning()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * This notification channel is only required for android versions above
     * android O. This creates the necessary notification channel for the foregroundService
     * to function.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
    // The following GATT Server code works but is not being used because of many reasons:
    //
    // 1) GATT Clients on android are really buggy when connecting/disconnecting quickly
    // 2) RxAndroidBle which is a library that solves those problems is only available for API 26 and
    //      up which is only 30% of the android users (we want to be as accessible as possible)
    // 3) We can achieve the same results by logging the advertised CEN and then logging the received CEN
    //      without ever connecting. (asymmetric connection model)
    //
    // The following code has been marked as Deprecated but still works so potentially can be used
    // in the future.
    /**
     * Initialize the GATT server instance with the services/characteristics
     * from BLEContactEvent
     *
     * @param context the context from the running activity
     */
    @Deprecated("")
    fun startGattServer(context: Context) {
//        app?.manager = (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
//        app?.server =
//        app.setServer(app.getManager().openGattServer(context, bluetoothGattServerCallback))
//        app.setService(
//            BluetoothGattService(
//                UUID.fromString(getString(R.string.peripheral_service_uuid)),
//                BluetoothGattService.SERVICE_TYPE_PRIMARY
//            )
//        )
//        app.getServer().addService(BLEContactEvent.createContactEventService())
    }

    /**
     * Shut down the GATT server.
     */
    @Deprecated("")
    private fun stopServer() {
//        if (app.getServer() == null) return
//        app.getServer().close()
    }

    /**
     * Callback to run when a client connects to this devices gatt server
     */
//    var bluetoothGattServerCallback: BluetoothGattServerCallback =
//        object : BluetoothGattServerCallback() {
//            override fun onConnectionStateChange(
//                device: BluetoothDevice,
//                status: Int,
//                newState: Int
//            ) {
//                super.onConnectionStateChange(device, status, newState)
//            }
//
//            override fun onCharacteristicReadRequest(
//                device: BluetoothDevice, requestId: Int,
//                offset: Int, characteristic: BluetoothGattServerCallback
//            ) {
//                Log.i(
//                    TAG,
//                    "Tried to read characteristic: $characteristic"
//                )
//                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
//            }
//
//            override fun onDescriptorReadRequest(
//                device: BluetoothDevice, requestId: Int,
//                offset: Int, descriptor: BluetoothGattDescriptor
//            ) {
//                Log.i(
//                    TAG,
//                    "Tried to read descriptor: $descriptor"
//                )
//                super.onDescriptorReadRequest(device, requestId, offset, descriptor)
//                app.getServer().sendResponse(
//                    device,
//                    requestId,
//                    BluetoothGatt.GATT_SUCCESS,
//                    0,
//                    BLEContactEvent.getNewContactEventNumber()
//                )
//            }
//        }

    companion object {
        // CONSTANTS
        private const val TAG = "BLEForegroundService"
    }
}