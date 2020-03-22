package org.covidwatch.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.firestore.LocalContactEventsViewModel

class CurrentUserExposureNotifier(var application: Application) {

    private val viewModel: LocalContactEventsViewModel =
        LocalContactEventsViewModel(
            CovidWatchDatabase.getInstance(application).contactEventDAO(),
            application
        )

    fun startObservingLocalContactEvents() {

        createNotificationChannel()

        viewModel.contactEvents.observeForever(Observer {
            val isCurrentUserSick = application.getSharedPreferences(
                application.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).getBoolean(application.getString(R.string.preference_is_current_user_sick), false)

            // No need to notify current user of exposure if they are already sick
            if (isCurrentUserSick) return@Observer
            // No need to notify if there aren't any potentially infectious contact events
            it.firstOrNull { contactEvent -> contactEvent.wasPotentiallyInfectious }
                ?: return@Observer

            notifyCurrentUserOfExposure()
        })
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = application.getString(R.string.channel_name)
            val descriptionText = application.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notifyCurrentUserOfExposure() {
        var builder = NotificationCompat.Builder(application, CHANNEL_ID)
            .setContentText(application.getString(R.string.notification_current_user_was_exposed))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
        with(NotificationManagerCompat.from(application)) {
            notify(0, builder.build())
        }
    }

    companion object {
        private const val CHANNEL_ID = "CurrentUserExposureNotifier"
    }

}

