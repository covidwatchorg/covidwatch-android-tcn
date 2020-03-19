package org.covidwatch.android

import android.app.Application
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import org.covidwatch.android.ble.BLEAdvertiser
import org.covidwatch.android.ble.BLEScanner


class CovidWatchApplication : Application() {
    // BLE
    var bleAdvertiser: BLEAdvertiser? = null
    var bleScanner: BLEScanner? = null

    // GAT
    var manager: BluetoothManager? = null
    var server: BluetoothGattServer? = null
    var service: BluetoothGattService? = null

}