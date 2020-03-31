package org.covidwatch.android.firestore

import java.util.*
import java.util.concurrent.TimeUnit

object FirestoreConstants {

    const val COLLECTION_CONTACT_EVENTS: String = "contact_events"

    const val FIELD_TIMESTAMP: String = "timestamp"

    // Only fetch contact events from the past 2 weeks
    private const val OLDEST_PUBLIC_CONTACT_EVENTS_TO_FETCH_SECONDS: Long = 60 * 60 * 24 * 7 * 2

    fun lastFetchTime(): Date {
        val fetchTime = Date()
        fetchTime.time -= TimeUnit.SECONDS.toMillis(OLDEST_PUBLIC_CONTACT_EVENTS_TO_FETCH_SECONDS)
        return fetchTime
    }
}
