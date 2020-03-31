package org.covidwatch.android.ble

import android.app.*
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import org.covidwatch.android.CovidWatchApplication
import org.covidwatch.android.MainActivity
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.firestore.FirestoreConstants
import java.util.*
import java.util.concurrent.TimeUnit

class BLEForegroundService : LifecycleService() {

    // APP
    private var app: CovidWatchApplication? = null
    private var timer: Timer? = null

    companion object {
        // CONSTANTS
        private const val CHANNEL_ID = "CovidBluetoothContactChannel"
        private const val TAG = "BLEForegroundService"
    }

    override fun onCreate() {
        super.onCreate()
        val application = (application as? CovidWatchApplication) ?: return
        app = application
        app?.bleAdvertiser = BLEAdvertiser(this, BluetoothAdapter.getDefaultAdapter())
        app?.bleScanner = BLEScanner(this, BluetoothAdapter.getDefaultAdapter())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        createNotificationChannelIfNeeded()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tags is logging")
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
                    app?.bleAdvertiser?.changeContactEventIdentifierInServiceDataField()
                }
            },
            TimeUnit.MINUTES.toMillis(BluetoothService.CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN.toLong()),
            TimeUnit.MINUTES.toMillis(BluetoothService.CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN.toLong())
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
        app?.bleAdvertiser?.startAdvertising(BluetoothService.CONTACT_EVENT_SERVICE, newContactEventUUID)
        app?.bleScanner?.startScanning(arrayOf<UUID>(BluetoothService.CONTACT_EVENT_SERVICE))

        return START_STICKY
    }

    override fun onDestroy() {
        app?.bleAdvertiser?.stopAdvertising()
        app?.bleScanner?.stopScanning()
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