package org.covidwatch.libcontactrace.cen

/**
 * ObservedCEN
 *
 * A contact event number that was observed/scanned by this device
 * Implements CEN
 *
 * @param data The data to put in the cen
 */
open class ObservedCen(override val data: ByteArray) : Cen {

    /**
     * accept CENVisitor
     */
    fun accept(visitor: CenVisitor){
        visitor.visit(this)
    }

}
