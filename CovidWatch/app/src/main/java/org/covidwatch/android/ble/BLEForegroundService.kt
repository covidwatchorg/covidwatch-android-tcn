package org.covidwatch.android.ble

import android.app.*
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import org.covidwatch.android.CovidWatchApplication
import org.covidwatch.android.MainActivity
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.utils.UUIDs
import java.util.*
import org.covidwatch.libcontactrace.*
import org.covidwatch.libcontactrace.cen.*

class BLEForegroundService : LifecycleService() {

    // APP
    private var app: CovidWatchApplication? = null
    private var timer: Timer? = null

    companion object {
        // CONSTANTS
        private const val CHANNEL_ID = "CovidBluetoothContactChannel"
        private const val CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN = 1
        private const val MS_TO_MIN = 60000
        private const val TAG = "BLEForegroundService"
    }

    class DefaultCENVisitor(val ctx: Context) : CENVisitor {
        fun handleCEN(cen: CEN) {
            CovidWatchDatabase.databaseWriteExecutor.execute {
                val dao: ContactEventDAO = CovidWatchDatabase.getInstance(ctx).contactEventDAO()
                val contactEvent = ContactEvent(cen.data.toString())
                val isCurrentUserSick = ctx.getSharedPreferences(
                    ctx.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                ).getBoolean(ctx.getString(R.string.preference_is_current_user_sick), false)
                contactEvent.wasPotentiallyInfectious = isCurrentUserSick
                dao.insert(contactEvent)
            }
        }

        override fun visit(cen: GeneratedCEN) {
            Log.i(TAG, "Handling generated CEN")
            handleCEN(cen)
        }

        override fun visit(cen: ObservedCEN) {
            Log.i(TAG, "Handling observed CEN")
            handleCEN(cen)
        }
    }

    class DefaultCENGenerator : CENGenerator {
        override fun generate(): GeneratedCEN {
            return GeneratedCEN(UUID.randomUUID().toBytes())
        }
    }

    private val cenGenerator = DefaultCENGenerator()
    private val cenVisitor = DefaultCENVisitor(this)

    override fun onCreate() {
        super.onCreate()
        val application = (application as? CovidWatchApplication) ?: return
        app = application

        // advertise CEN
        app?.cenAdvertiser = CENAdvertiser(
            this,
            BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser,
            UUIDs.CONTACT_EVENT_SERVICE,
            UUIDs.CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC,
            cenVisitor,
            cenGenerator
        )

        // scan CENs
        app?.cenScanner = CENScanner(
            this,
            BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner,
            UUIDs.CONTACT_EVENT_SERVICE,
            cenVisitor
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        createNotificationChannelIfNeeded()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CovidWatch passively logging")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(6, notification)

        // scheduler a new timer to start changing the contact event numbers
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    app?.cenAdvertiser?.updateCEN()
                }
            },
            MS_TO_MIN * CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN.toLong(),
            MS_TO_MIN * CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN.toLong()
        )

        // start contact logging
        app!!.cenAdvertiser!!.startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE)
        app!!.cenScanner!!.startScanning(arrayOf(UUIDs.CONTACT_EVENT_SERVICE), 10)

        return START_STICKY
    }

    override fun onDestroy() {
        app?.cenAdvertiser?.stopAdvertiser()
        app?.cenScanner?.stopScanning()
        timer?.apply {
            cancel()
            purge()
        }
        super.onDestroy()
    }


    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /**
     * This notification channel is only required for android versions above
     * android O. This creates the necessary notification channel for the foregroundService
     * to function.
     */
    private fun createNotificationChannelIfNeeded() {
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
}
