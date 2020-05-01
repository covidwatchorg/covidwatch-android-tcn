package org.covidwatch.android.requirements.test_environment

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentHostCallback
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelStore
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

    private val homeFragment: HomeFragment

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

        homeFragment = HomeFragment()
        homeFragment["mHost"] = mockk<FragmentHostCallback<*>>(relaxed = false)

        val viewModelStore = ViewModelStore()

        val fragmentManagerMock = mockk<FragmentManager>(relaxed = false)
        every { fragmentManagerMock["isStateAtLeast"](1) } returns true
        every { fragmentManagerMock["getViewModelStore"](homeFragment) } returns viewModelStore

        homeFragment["mChildFragmentManager"] = fragmentManagerMock
        homeFragment["mFragmentManager"] = fragmentManagerMock

        homeFragment.onCreate(null)
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