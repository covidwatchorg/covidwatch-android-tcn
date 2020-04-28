package org.covidwatch.android

import android.app.Application
import org.covidwatch.android.data.signedreport.SignedReportsDownloader
import org.covidwatch.android.data.signedreport.firestore.SignedReportsUploader
import org.covidwatch.android.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.tcncoalition.tcnclient.TcnClient

class CovidWatchApplication : Application() {

    private val tcnManager: CovidWatchTcnManager by inject()
    private val signedReportsUploader: SignedReportsUploader by inject()
    private val signedReportsDownloader: SignedReportsDownloader by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        TcnClient.init(tcnManager)
        signedReportsUploader.startUploading()
        signedReportsDownloader.schedulePeriodicPublicSignedReportsRefresh()
    }
}
