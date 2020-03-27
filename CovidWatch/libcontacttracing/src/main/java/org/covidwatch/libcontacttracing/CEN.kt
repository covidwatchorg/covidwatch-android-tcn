package org.covidwatch.libcontacttracing

import java.text.DateFormat
import java.util.*

/**
 * A CEN or a ContactEventNumber is the number that is advertised/received
 * by phones using a CENAdvertiser and a CENScanner
 *
 * @param number A unsigned byte array (strict size of 16)
 *        or can be constructed with a UUID (which is also 16 bytes)
 */
data class CEN(var number: ByteArray) {

    companion object {
        const val MAX_BLE_DATA_LIMIT = 16

    }

    init {
        require(number.size == MAX_BLE_DATA_LIMIT)
    }

    /**
     * Construct CEN from UUID
     */
    constructor( uuid: UUID) : this(uuid.toBytes())

    /**
     * Utils
     */
    fun toUUID() = number.toUUID();
}
