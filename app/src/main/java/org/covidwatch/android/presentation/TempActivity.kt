package org.covidwatch.android.presentation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.covidwatch.android.R

class TempActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)
        setContactEventLogging(true)
    }

    private fun setContactEventLogging(enabled: Boolean) {

        val application = this?.applicationContext ?: return
        val sharedPref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ) ?: return
        with(sharedPref.edit()) {
            putBoolean(
                application.getString(org.covidwatch.android.R.string.preference_is_contact_event_logging_enabled),
                enabled
            )
            commit()
        }
    }

}
