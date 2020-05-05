package org.covidwatch.android.ui.settings

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pub.devrel.easypermissions.EasyPermissions

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    private val hasLocationPermission: Boolean
        get() {
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            return EasyPermissions.hasPermissions(getApplication(), *perms)
        }

    private val isBluetoothEnabled: Boolean get() = bluetoothAdapter?.isEnabled == true

    private val _hasLocationPermissionLiveData = MutableLiveData(hasLocationPermission)
    val hasLocationPermissionLiveData: LiveData<Boolean> = _hasLocationPermissionLiveData

    private val _isBluetoothEnabledLiveData = MutableLiveData(isBluetoothEnabled)
    val isBluetoothEnabledLiveData: LiveData<Boolean> = _isBluetoothEnabledLiveData

    fun onLocationPermissionResult() {
        _hasLocationPermissionLiveData.value = hasLocationPermission
    }

    fun onBluetoothResult() {
        _isBluetoothEnabledLiveData.value = isBluetoothEnabled
    }
}