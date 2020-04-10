package org.covidwatch.libcontactrace.cen

/**
 * GeneratedCEN
 *
 * A contact event number that was generated locally on this device.
 * Implements CEN
 *
 * @param data The data to put in the cen
 */
open class GeneratedCen(override val data: ByteArray) : Cen {

    /**
     * accept CENVisitor
     */
    fun accept(visitor: CenVisitor){
        visitor.visit(this)
    }

}
