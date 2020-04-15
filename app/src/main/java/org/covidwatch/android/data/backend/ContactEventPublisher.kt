package org.covidwatch.android.data.backend

import org.covidwatch.android.data.ContactEvent

/**
 * Publisher for contact events.
 */
interface ContactEventPublisher {

    /**
     * Upload the given contact events to a server.
     * @param contactEvents The events to update.
     */
    fun uploadContactEvents(contactEvents: List<ContactEvent>)
}