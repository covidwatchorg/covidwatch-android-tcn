package org.covidwatch.android.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "temporary_contact_numbers")
@TypeConverters(DateConverter::class)
class TemporaryContactNumber {

    @PrimaryKey
    @ColumnInfo(name = "bytes", typeAffinity = ColumnInfo.BLOB)
    var bytes: ByteArray = ByteArray(0)

    @ColumnInfo(name = "closest_estimated_distance_meters")
    var closestEstimatedDistanceMeters: Double = Double.MAX_VALUE

    @ColumnInfo(name = "found_date")
    var foundDate: Date = Date()

    @ColumnInfo(name = "last_seen_date")
    var lastSeenDate: Date = Date()

    @ColumnInfo(name = "was_potentially_infectious")
    var wasPotentiallyInfectious: Boolean = false
}