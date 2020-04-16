package org.covidwatch.android.di

import android.content.Context
import org.covidwatch.android.domain.UserFlowRepository
import org.covidwatch.android.data.UserFlowRepositoryImpl
import org.covidwatch.android.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory {
        UserFlowRepositoryImpl(
            preferences = get()
        ) as UserFlowRepository
    }

    factory {
        val context = androidContext()

        context.getSharedPreferences("org.covidwatch.android.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
    }

    viewModel {
        HomeViewModel(
            userFlowRepository = get()
        )
    }
}