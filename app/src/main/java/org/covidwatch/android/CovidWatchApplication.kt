package org.covidwatch.android

import android.app.Application
import org.covidwatch.android.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CovidWatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }
    }
}
