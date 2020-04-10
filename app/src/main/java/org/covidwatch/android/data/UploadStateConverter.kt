package org.covidwatch.android.data

import androidx.room.TypeConverter

class UploadStateConverter {

    @TypeConverter
    fun toUploadState(status: Int): ContactEvent.UploadState? {
        return when (status) {
            ContactEvent.UploadState.NOTUPLOADED.code -> {
                ContactEvent.UploadState.NOTUPLOADED
            }
            ContactEvent.UploadState.UPLOADING.code -> {
                ContactEvent.UploadState.UPLOADING
            }
            ContactEvent.UploadState.UPLOADED.code -> {
                ContactEvent.UploadState.UPLOADED
            }
            else -> {
                throw IllegalArgumentException("Could not recognize status")
            }
        }
    }

    @TypeConverter
    fun toInteger(status: ContactEvent.UploadState): Int? {
        return status.code
    }
}

