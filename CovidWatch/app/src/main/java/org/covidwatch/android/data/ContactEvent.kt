package org.covidwatch.android.data

import androidx.room.*
import java.util.*

@Entity(tableName = "contact_events")
@TypeConverters(DateConverter::class)
class ContactEvent {
    @PrimaryKey
    var identifier: String = UUID.randomUUID().toString().capitalize()

    @ColumnInfo(name = "timestamp")
    var timestamp: Date = Date()

    @ColumnInfo(name = "upload_state")
    var uploadState = 0

    @ColumnInfo(name = "was_potentially_infectious")
    var wasPotentiallyInfectious: Boolean = false

    /**
     * Constructor
     *
     * @param CEN The contact event number
     */
    constructor(CEN: String) {
        identifier = CEN.capitalize()
    }
    constructor() {}
}