package org.covidwatch.android.domain

import android.content.SharedPreferences

private const val IS_CONTACT_EVENT_LOGGING_ENABLED_KEY = "preference_is_contact_event_logging_enabled"

class EnableContactEventLoggingUseCase(
    private val preferences: SharedPreferences
) {

    fun execute() {
        val isContactLoggingEnabled: Boolean = preferences.getBoolean(IS_CONTACT_EVENT_LOGGING_ENABLED_KEY, false)
        if (isContactLoggingEnabled) return

        preferences
            .edit()
            .putBoolean(IS_CONTACT_EVENT_LOGGING_ENABLED_KEY, true)
            .apply()
    }
}