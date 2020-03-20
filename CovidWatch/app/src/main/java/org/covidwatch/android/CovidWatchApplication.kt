package org.covidwatch.android

import android.app.Application
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import org.covidwatch.android.ble.BLEAdvertiser
import org.covidwatch.android.ble.BLEScanner

class CovidWatchApplication : Application() {
    var bleAdvertiser: BLEAdvertiser? = null
    var bleScanner: BLEScanner? = null
}