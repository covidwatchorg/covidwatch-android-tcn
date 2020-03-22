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
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.utils.UUIDs
import java.util.*

class BLEForegroundService : Service() {

    // APP
    private var app: CovidWatchApplication? = null
    private var timer: Timer? = null

    companion object {
        // CONSTANTS
        private const val CHANNEL_ID = "CovidBluetoothContactChannel"
        private const val CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN = 5
        private const val MS_TO_MIN = 60000
    }

    override fun onCreate() {
        super.onCreate()
        val application = (application as? CovidWatchApplication) ?: return
        app = application
        app?.bleAdvertiser = BLEAdvertiser(this, BluetoothAdapter.getDefaultAdapter())
        app?.bleScanner = BLEScanner(this, BluetoothAdapter.getDefaultAdapter())
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
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
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    app?.bleAdvertiser?.changeContactEventNumber()
                }
            },
            MS_TO_MIN * CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN.toLong(),
            MS_TO_MIN * CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN.toLong()
        )

        val newContactEventUUID = UUID.randomUUID()
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(this).contactEventDAO()
            val contactEvent = ContactEvent(newContactEventUUID.toString())
            val isCurrentUserSick = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).getBoolean(getString(R.string.preference_is_current_user_sick), false)
            contactEvent.wasPotentiallyInfectious = isCurrentUserSick
            dao.insert(contactEvent)
        }
        app?.bleAdvertiser?.startAdvertiser(UUIDs.CONTACT_EVENT_SERVICE, newContactEventUUID)
        app?.bleScanner?.startScanning(arrayOf<UUID>(UUIDs.CONTACT_EVENT_SERVICE))

        return START_STICKY
    }

    override fun onDestroy() {
        app?.bleAdvertiser?.stopAdvertiser()
        app?.bleScanner?.stopScanning()
        timer?.apply {
            cancel()
            purge()
        }
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
}