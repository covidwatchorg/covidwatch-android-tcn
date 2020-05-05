package org.covidwatch.android.data.positivediagnosis

import org.covidwatch.android.data.PositiveDiagnosis
import java.util.Date

class PositiveDiagnosisRepository(private val remote: PositiveDiagnosisRemoteSource) {

    suspend fun diagnosisKeys(since: Date) = remote.diagnosisKeys(since)

    suspend fun isNumberValid(phaNumber: String) = remote.isNumberValid(phaNumber)

    suspend fun uploadDiagnosisKeys(positiveDiagnosis: PositiveDiagnosis) =
        remote.uploadDiagnosisKeys(positiveDiagnosis)
}