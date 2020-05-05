package org.covidwatch.android.data

import androidx.room.Entity
import androidx.room.TypeConverters
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.covidwatch.android.data.converter.DateConverter
import org.covidwatch.android.data.converter.DiagnosisKeysConverter
import java.sql.Date

@Entity(tableName = "positive_diagnosis")
@TypeConverters(DateConverter::class, DiagnosisKeysConverter::class)
data class PositiveDiagnosis(
    val diagnosisKeys: List<DiagnosisKey>,
    val phaPermissionNumber: String,
    val timestamp: Date? = null
)

class DiagnosisKey(
    val keyData: ByteArray,
    val rollingStartIntervalNumber: Int,
    val transmissionRiskLevel: Int
)

fun DiagnosisKey.asTemporaryExposureKey(): TemporaryExposureKey =
    TemporaryExposureKey.TemporaryExposureKeyBuilder()
        .setKeyData(keyData)
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel)
        .build()

fun TemporaryExposureKey.asDiagnosisKey() = DiagnosisKey(
    keyData,
    rollingStartIntervalNumber,
    transmissionRiskLevel
)