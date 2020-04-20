package org.covidwatch.android.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import org.covidwatch.android.R
import org.covidwatch.android.presentation.MainActivity
import org.tcncoalition.tcnclient.bluetooth.BluetoothStateListener
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothService
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothService.LocalBinder
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothServiceCallback

interface BluetoothManager {
    fun startService()
    fun stopService()
    fun setCallback(bluetoothServiceCallback: TcnBluetoothServiceCallback)
}

class BluetoothManagerImpl(
    private val context: Context
) : BluetoothManager, BluetoothStateListener {

    private lateinit var tcnBluetoothServiceCallback: TcnBluetoothServiceCallback
    private var service: TcnBluetoothService? = null
    private var binded = false

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            this@BluetoothManagerImpl.service = (service as LocalBinder).service.apply {
                val notification = foregroundNotification(
                    context.getString(R.string.foreground_notification_title)
                )
                setForegroundNotification(NOTIFICATION_ID, notification)
                setBluetoothStateListener(this@BluetoothManagerImpl)
                startTcnExchange(tcnBluetoothServiceCallback)
            }
            binded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binded = false
        }
    }

    private fun foregroundNotification(title: String): Notification {
        createNotificationChannelIfNeeded()

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    override fun startService() {
        context.bindService(
            Intent(context, TcnBluetoothService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun stopService() {
        if (binded) {
            service?.stopTcnExchange()
            context.unbindService(serviceConnection)
            binded = false
        }
    }

    override fun setCallback(bluetoothServiceCallback: TcnBluetoothServiceCallback) {
        tcnBluetoothServiceCallback = bluetoothServiceCallback
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
                context, NotificationManager::class.java
            )
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun bluetoothStateChanged(bluetoothOn: Boolean) {
        val title = if (bluetoothOn) {
            R.string.foreground_notification_title
        } else {
            R.string.foreground_notification_ble_off
        }

        getSystemService(
            context,
            NotificationManager::class.java
        )?.notify(
            NOTIFICATION_ID,
            foregroundNotification(context.getString(title))
        )
    }

    companion object {
        private const val CHANNEL_ID = "CovidWatchContactTracingNotificationChannel"
        const val NOTIFICATION_ID = 42
    }
}
