package org.covidwatch.libcontactrace.cen

/**
 * GeneratedCEN
 *
 * A contact event number that was generated locally on this device.
 * Implements CEN
 *
 * @param data The data to put in the cen
 */
open class GeneratedCEN(override val data: ByteArray) : CEN {

    /**
     * accept CENVisitor
     */
    fun accept(visitor: CENVisitor){
        visitor.visit(this)
    }

}
