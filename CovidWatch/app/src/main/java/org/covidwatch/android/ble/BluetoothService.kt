package org.covidwatch.android.ble

import java.util.*

object BluetoothService {

    /// The string representation of the UUID for the primary peripheral service
    var CONTACT_EVENT_SERVICE: UUID =
        UUID.fromString("0000C019-0000-1000-8000-00805F9B34FB")

    /// The string representation of the UUID for the contact event identifier characteristic
    var CONTACT_EVENT_IDENTIFIER_CHARACTERISTIC: UUID =
        UUID.fromString("D61F4F27-3D6B-4B04-9E46-C9D2EA617F62")

    const val CONTACT_EVENT_NUMBER_CHANGE_INTERVAL_MIN: Int = 15
}