package org.covidwatch.android.data.exposureinformation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.covidwatch.android.data.CovidExposureInformation

@Dao
interface ExposureInformationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveExposureInformation(exposureInformation: List<CovidExposureInformation>)

    @Query("SELECT * FROM exposure_information")
    fun exposureInformation(): LiveData<List<CovidExposureInformation>>
}