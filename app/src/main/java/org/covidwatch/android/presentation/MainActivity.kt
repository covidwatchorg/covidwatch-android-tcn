package org.covidwatch.android.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.covidwatch.android.R
import org.covidwatch.android.presentation.util.EventObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CHANNEL_ID = "CurrentUserExposureNotifier"

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            createNotificationChannel()
        }
        mainViewModel.possibleExposureNotificationEvent.observe(this, EventObserver {
            notifyUserOfPossibleExposure()
        })
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = getString(R.string.channel_description)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notifyUserOfPossibleExposure() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(application.getString(R.string.notification_current_user_was_exposed))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        NotificationManagerCompat.from(this).notify(0, notification)
    }
}
