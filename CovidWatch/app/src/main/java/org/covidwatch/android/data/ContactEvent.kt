package org.covidwatch.android.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "contact_events")
@TypeConverters(DateConverter::class, UploadStateConverter::class)
class ContactEvent {
    @PrimaryKey
    var identifier: String = UUID.randomUUID().toString().toUpperCase()

    @ColumnInfo(name = "timestamp")
    var timestamp: Date = Date()

    @ColumnInfo(name = "upload_state")
    var uploadState: UploadState = UploadState.NOTUPLOADED

    @ColumnInfo(name = "was_potentially_infectious")
    var wasPotentiallyInfectious: Boolean = false

    /**
     * Constructor
     *
     * @param CEI The contact event number
     */
    constructor(CEI: String) {
        identifier = CEI.toUpperCase()
    }

    constructor() {}

    enum class UploadState(val code: Int) {
        NOTUPLOADED(0), UPLOADING(1), UPLOADED(2);
    }
}