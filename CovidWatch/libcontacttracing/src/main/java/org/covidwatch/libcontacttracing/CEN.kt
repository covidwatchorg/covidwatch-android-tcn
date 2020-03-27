package org.covidwatch.libcontacttracing

import java.util.*

/**
 * A CEN or a ContactEventNumber is the number that is advertised/received
 * by phones using a CENAdvertiser and CENScanner
 *
 * @param number A unsigned byte array (strict size of 16)
 *        or can be constructed with a UUID (which is also 16 bytes)
 * @param timestamp The timestamp when the detection was generated
 */
data class CEN(
    var number: ByteArray,
    var timestamp: String
) {
    init {
        require(number.size == MAX_BLE_DATA_LIMIT)
    }

    companion object {
        const val MAX_BLE_DATA_LIMIT = 16
    }

    /**
     * Construct CEN from UUID
     */
    constructor(uuid: UUID, timestamp: String) : this(uuid.toBytes(), timestamp)

    /**
     * Utils
     */
    fun toUUID() = number.toUUID();
}
