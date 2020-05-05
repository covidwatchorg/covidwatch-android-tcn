package org.covidwatch.android.data

import androidx.room.Database
import androidx.room.RoomDatabase
import org.covidwatch.android.data.exposureinformation.ExposureInformationDao

@Database(entities = [CovidExposureInformation::class], version = 0, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exposureInformationDao(): ExposureInformationDao
}