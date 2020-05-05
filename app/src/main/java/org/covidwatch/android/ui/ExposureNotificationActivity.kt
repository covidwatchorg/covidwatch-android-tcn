package org.covidwatch.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.covidwatch.android.R
import org.covidwatch.android.ui.exposurenotification.ExposureNotificationFragment

class ExposureNotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exposure_notification)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ExposureNotificationFragment.newInstance())
                .commitNow()
        }
    }
}
