package org.covidwatch.libcontacttracing

/**
 * A CENDetection contains the data obtained when a CEN is detected.
 * Overrides comparators to sort based on RSSI
 *
 * @param cen The contact event number for this detection
 * @param timestamp The timestamp when the detection was generated
 * @param RSSI The Received Signal Strength Indicator to indicate signal strength to the phone
 *        advertising the CEN (less negative = stronger signal)
 */
data class CENDetection(
    var cen: CEN,
    var timestamp: String,
    var RSSI: Int
) {

    /**
     * Equality check two CENDetection, compares down to the
     * time and RSSI
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CENDetection

        if (!cen.number.contentEquals(other.cen.number)) return false
        if (timestamp != other.timestamp) return false
        if (RSSI != other.RSSI) return false

        return true
    }

    /**
     * Compare Operator overriding to implement RSSI based comparison
     * The lower the RSSI value, the closer the CENDetection is to the current
     * user.
     *
     * Ex. CE1 > CE2 returns true if CE1 has a stronger signal strength
     */
    operator fun compareTo(other: CENDetection): Int = when {
        RSSI != other.RSSI -> (RSSI - other.RSSI)
        else -> (RSSI - other.RSSI)
    }

    /**
     * Hash
     */
    override fun hashCode(): Int {
        var result = cen.number.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + RSSI
        return result
    }
}
