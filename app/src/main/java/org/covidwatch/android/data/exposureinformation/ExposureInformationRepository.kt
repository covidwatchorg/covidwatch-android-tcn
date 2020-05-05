package org.covidwatch.android.data.exposureinformation

import org.covidwatch.android.data.CovidExposureInformation

class ExposureInformationRepository(private val local: ExposureInformationLocalSource) {
    suspend fun saveExposureInformation(exposureInformation: List<CovidExposureInformation>) {
        local.saveExposureInformation(exposureInformation)
    }
    fun exposureInformation() = local.exposureInformation()
}