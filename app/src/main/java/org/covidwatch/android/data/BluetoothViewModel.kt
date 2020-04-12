package org.covidwatch.android.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel: ViewModel() {
    val permissionRequestResultLiveData = MutableLiveData<Boolean>(false)
}