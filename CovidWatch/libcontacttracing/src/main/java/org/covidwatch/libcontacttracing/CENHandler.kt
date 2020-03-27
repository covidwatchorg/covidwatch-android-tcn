package org.covidwatch.libcontacttracing

/**
 * Interface for CENHandlers
 */
interface CENHandler {

    /**
     * Handles the detected CEN from a neighbouring device
     * @param cen The CEN to handle
     * @return Unit (nothing)
     */
    fun handleCEN(cen: CEN)
}