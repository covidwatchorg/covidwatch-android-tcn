package org.covidwatch.android.ui.home

import androidx.annotation.StringRes

sealed class WarningBannerState {

    data class Visible(@StringRes val text: Int) : WarningBannerState()

    object Hidden : WarningBannerState()
}

sealed class InfoBannerState {

    data class Visible(@StringRes val text: Int) : InfoBannerState()

    object Hidden : InfoBannerState()
}