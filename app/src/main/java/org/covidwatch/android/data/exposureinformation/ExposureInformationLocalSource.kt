package org.covidwatch.android.data.exposureinformation

import org.covidwatch.android.data.AppDatabase
import org.covidwatch.android.data.CovidExposureInformation

class ExposureInformationLocalSource(private val database: AppDatabase) {
    suspend fun saveExposureInformation(exposureInformation: List<CovidExposureInformation>) {
        database.exposureInformationDao().saveExposureInformation(exposureInformation)
    }

    fun exposureInformation() =
        database.exposureInformationDao().exposureInformation()
}