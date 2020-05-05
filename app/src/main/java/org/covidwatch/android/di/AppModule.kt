package org.covidwatch.android.di

import android.content.Context
import okhttp3.OkHttpClient
import org.covidwatch.android.data.TestedRepositoryImpl
import org.covidwatch.android.data.UserFlowRepositoryImpl
import org.covidwatch.android.domain.TestedRepository
import org.covidwatch.android.domain.UserFlowRepository
import org.covidwatch.android.presentation.home.EnsureTcnIsStartedUseCase
import org.covidwatch.android.presentation.home.HomeViewModel
import org.covidwatch.android.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

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

    factory {
        EnsureTcnIsStartedUseCase(
            context = androidContext()
        )
    }

    viewModel {
        HomeViewModel(
            userFlowRepository = get(),
            testedRepository = get(),
            ensureTcnIsStartedUseCase = get()
        )
    }

    viewModel {
        SettingsViewModel(androidApplication())
    }

    single { OkHttpClient() }

    factory {
        TestedRepositoryImpl(
            preferences = get()
        ) as TestedRepository
    }
}