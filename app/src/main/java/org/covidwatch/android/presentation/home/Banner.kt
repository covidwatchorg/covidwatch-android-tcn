package org.covidwatch.android.presentation.home

import androidx.annotation.StringRes

sealed class WarningBanner {

    data class Show(@StringRes val text: Int) : WarningBanner()

    object Hide : WarningBanner()
}

sealed class InfoBanner {

    data class Show(@StringRes val text: Int) : InfoBanner()

    object Hide : InfoBanner()
}