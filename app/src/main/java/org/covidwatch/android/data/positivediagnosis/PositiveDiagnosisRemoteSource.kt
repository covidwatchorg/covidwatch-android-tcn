package org.covidwatch.android.data.positivediagnosis

import org.covidwatch.android.data.FirebaseService
import org.covidwatch.android.data.PositiveDiagnosis
import java.util.Date

class PositiveDiagnosisRemoteSource(private val firebaseService: FirebaseService) {
    suspend fun diagnosisKeys(since: Date): List<PositiveDiagnosis> =
        firebaseService.diagnosisKeys(since)

    suspend fun uploadDiagnosisKeys(keys: PositiveDiagnosis) {
        firebaseService.uploadDiagnosisKeys(keys)
    }

    suspend fun isNumberValid(phaNumber: String) = firebaseService.isNumberValid(phaNumber)
}