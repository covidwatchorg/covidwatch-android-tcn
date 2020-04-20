package org.covidwatch.android.di

import android.content.Context
import org.covidwatch.android.NotificationFactory
import org.covidwatch.android.TcnManager
import org.covidwatch.android.ble.BluetoothManager
import org.covidwatch.android.ble.BluetoothManagerImpl
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.TestedRepositoryImpl
import org.covidwatch.android.data.UserFlowRepositoryImpl
import org.covidwatch.android.domain.MaybeEnableContactEventLoggingUseCase
import org.covidwatch.android.domain.NotifyAboutPossibleExposureUseCase
import org.covidwatch.android.domain.TestedRepository
import org.covidwatch.android.domain.UserFlowRepository
import org.covidwatch.android.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.tcncoalition.tcnclient.TcnKeys

@Suppress("USELESS_CAST")
val appModule = module {

    factory {
        UserFlowRepositoryImpl(
            preferences = get()
        ) as UserFlowRepository
    }

    factory {
        val context = androidContext()

        context.getSharedPreferences(
            "org.covidwatch.android.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE
        )
    }

    viewModel {
        HomeViewModel(
            userFlowRepository = get(),
            testedRepository = get(),
            enableContactEventLoggingUseCase = get(),
            contactEventDAO = get()
        )
    }

    single {
        CovidWatchDatabase.getInstance(androidContext())
    }

    single {
        val database: CovidWatchDatabase = get()

        database.contactEventDAO()
    }

    factory {
        TestedRepositoryImpl(
            preferences = get()
        ) as TestedRepository
    }

    factory {
        EnableContactEventLoggingUseCase(
            preferences = get()
        )
    }

    factory {
        NotifyAboutPossibleExposureUseCase(
            context = androidContext(),
            notificationFactory = get(),
            testedRepository = get(),
            contactEventDAO = get()
        )
    }

    factory {
        NotificationFactory(androidContext())
    }

    single {
        BluetoothManagerImpl(androidApplication()) as BluetoothManager
    }

    single {
        TcnManager(
            context = androidApplication(),
            tcnKeys = TcnKeys(androidApplication()),
            bluetoothManager = get(),
            contactEventDAO = get(),
            sharedPreferences = get()
        )
    }
}