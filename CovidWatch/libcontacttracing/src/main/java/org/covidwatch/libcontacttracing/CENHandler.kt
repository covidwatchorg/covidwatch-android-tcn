package org.covidwatch.libcontacttracing

/**
 * Interface for CENHandlers
 */
interface CENHandler {

    /**
     * Handles the detected CEN from a neighbouring device
     * @return Unit
     */
    fun handleCEN(cen: CEN)
}