package org.covidwatch.android.requirements.test_environment

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.work.*
import androidx.work.impl.WorkManagerImpl
import io.mockk.every
import io.mockk.mockk
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.contactevent.ContactEventsDownloadWorker
import org.covidwatch.android.data.contactevent.LocalContactEventsUploader
import org.covidwatch.android.di.appModule
import org.covidwatch.android.presentation.HomeFragment
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class TestApplication(private val testContext: TestContext) {

    fun receiveTCNFromBackend(tcn: ContactEvent) {
        val oldTcns = testContext.allContactEventsSortedByDescTimestamp.value ?: listOf()
        testContext.allContactEventsSortedByDescTimestamp.setLiveValue(oldTcns + tcn)
    }

    fun publishTCNs(tcns: List<ContactEvent>) {
        //TODO()
    }

    fun receiveProximityEvent(tcn: ContactEvent) {
        //TODO()
    }

    private val contextMock = mockk<Context>(relaxed = false, relaxUnitFun = true).apply {
        every { applicationContext } returns this
        every { getApplicationContext() } returns this
    }

    private val taskExecutorMock = mockk<TaskExecutor>(relaxed = false, relaxUnitFun = true).apply {
        every { isMainThread } returns true
    }

    private val applicationMock = mockk<Application>(relaxed = false, relaxUnitFun = true).apply {
        every { applicationContext } returns contextMock
        every { getApplicationContext() } returns contextMock
    }

    private val localContactEventsUploader: LocalContactEventsUploader

    private val workManagerMock = mockk<WorkManagerImpl>(relaxed = false, relaxUnitFun = true).apply {
        every { enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk(relaxed = false, relaxUnitFun = true)
    }

    private val homeFragment: FragmentTestBed<HomeFragment>

    init {
        ArchTaskExecutor.getInstance().setDelegate(taskExecutorMock)
        WorkManagerImpl.setDelegate(workManagerMock)

        startKoin {
            androidContext(contextMock)
            modules(appModule, testAppModule(testContext))
        }

        localContactEventsUploader = LocalContactEventsUploader(applicationMock)
        localContactEventsUploader.startUploading()

        schedulePeriodicPublicContactEventsRefresh()

        homeFragment = FragmentTestBed(HomeFragment())
    }

    private fun schedulePeriodicPublicContactEventsRefresh() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest =
            PeriodicWorkRequestBuilder<ContactEventsDownloadWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(contextMock).enqueueUniquePeriodicWork(
            ContactEventsDownloadWorker.WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            downloadRequest
        )
    }
}