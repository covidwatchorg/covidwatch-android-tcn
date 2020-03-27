package org.covidwatch.libcontacttracing

/**
 * Interface for CENGenerators
 */
interface CENGenerator {

    /**
     * Generates a new CEN to be consumed
     * @returns the new generated CEN
     */
    fun generateCEN(): CEN

}