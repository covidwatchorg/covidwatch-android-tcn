package org.covidwatch.android.data

data class CovidExposureSummary(
    val daySinceLastExposure: Int,
    val matchedKeyCount: Int,
    val maximumRiskScore: Int
)
