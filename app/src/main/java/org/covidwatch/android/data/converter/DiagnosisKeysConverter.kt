package org.covidwatch.android.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.covidwatch.android.data.DiagnosisKey

class DiagnosisKeysConverter {
    private val gson = Gson()
    private val key = object : TypeToken<List<DiagnosisKey>>() {}.type

    @TypeConverter
    fun toString(diagnosis: List<DiagnosisKey>) = gson.toJson(diagnosis)

    @TypeConverter
    fun fromString(diagnosis: String): List<DiagnosisKey> = diagnosis.let {
        gson.fromJson(it, key)
    }
}
