package org.covidwatch.android.data

import java.util.Date

class FirebaseService {
    suspend fun diagnosisKeys(since: Date): List<PositiveDiagnosis> {
        TODO("not implemented")
    }

    suspend fun uploadDiagnosisKeys(keys: PositiveDiagnosis) {
        TODO("not implemented")
    }

    suspend fun isNumberValid(phaNumber: String) = true
}