package org.covidwatch.android.data.signedreport.firestore

import java.util.*
import java.util.concurrent.TimeUnit

object FirestoreConstants {

    const val COLLECTION_SIGNED_REPORTS: String = "signed_reports"

    const val FIELD_TEMPORARY_CONTACT_KEY_BYTES: String = "temporary_contact_key_bytes"
    const val FIELD_END_INDEX: String = "end_index"
    const val FIELD_MEMO_DATA: String = "memo_data"
    const val FIELD_MEMO_TYPE: String = "memo_type"
    const val FIELD_REPORT_VERIFICATION_PUBLIC_KEY_BYTES: String = "report_verification_public_key_bytes"
    const val FIELD_SIGNATURE_BYTES: String = "signature_bytes"
    const val FIELD_START_INDEX: String = "start_index"
    const val FIELD_TIMESTAMP: String = "timestamp"

    // Only fetch signed reports from the past 2 weeks
    private const val OLDEST_PUBLIC_TEMPORARY_CONTACT_NUMBERS_TO_FETCH_SECONDS: Long = 60 * 60 * 24 * 7 * 2

    fun lastFetchTime(): Date {
        val fetchTime = Date()
        fetchTime.time -= TimeUnit.SECONDS.toMillis(
            OLDEST_PUBLIC_TEMPORARY_CONTACT_NUMBERS_TO_FETCH_SECONDS
        )
        return fetchTime
    }
}
