package org.covidwatch.android.presentation.home

interface EnsureTcnIsStartedPresenter {

    fun showLocationPermissionBanner()

    fun showEnableBluetoothBanner()

    fun hideBanner()
}