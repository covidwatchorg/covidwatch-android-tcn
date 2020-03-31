package org.covidwatch.libcontactrace.cen

/**
 * ObservedCEN
 *
 * A contact event number that was observed/scanned by this device
 * Implements CEN
 *
 * @param data The data to put in the cen
 */
open class ObservedCEN(override val data: ByteArray) : CEN {

    /**
     * accept CENVisitor
     */
    fun accept(visitor: CENVisitor){
        visitor.visit(this)
    }

}
