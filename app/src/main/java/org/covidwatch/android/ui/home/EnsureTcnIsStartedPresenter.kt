package org.covidwatch.android.ui.home

interface EnsureTcnIsStartedPresenter {

    fun showLocationPermissionBanner()

    fun showEnableBluetoothBanner()

    fun hideBanner()
}