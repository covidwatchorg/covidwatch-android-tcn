package org.covidwatch.android.presentation.home

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.covidwatch.android.R
import org.covidwatch.android.TcnManager
import pub.devrel.easypermissions.EasyPermissions

class InfoBannerViewModel(
    application: Application,
    private val tcnManager: TcnManager
) : AndroidViewModel(application) {

    private val hasLocationPermission: Boolean
        get() {
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            return EasyPermissions.hasPermissions(getApplication(), *perms)
        }

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val isBluetoothEnabled: Boolean get() = bluetoothAdapter?.isEnabled == true

    private val _infoBanner = MutableLiveData<InfoBanner>()
    val infoBanner: LiveData<InfoBanner> get() = _infoBanner

    fun onStart() {
        when {
            !hasLocationPermission -> {
                tcnManager.stop()
                _infoBanner.value = InfoBanner.Show(R.string.allow_location_access)
            }
            !isBluetoothEnabled -> {
                tcnManager.stop()
                _infoBanner.value = InfoBanner.Show(R.string.turn_bluetooth_on)
            }
            else -> {
                tcnManager.start()
                _infoBanner.value = InfoBanner.Hide
            }
        }
    }
}