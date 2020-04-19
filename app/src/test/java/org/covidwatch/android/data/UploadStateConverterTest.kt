package org.covidwatch.android.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested

import org.assertj.core.api.Assertions.*
import java.lang.IllegalArgumentException

internal class UploadStateConverterTest {

    private val uploadStateConv: UploadStateConverter = UploadStateConverter()

    @Nested
    inner class ToUploadState() {

        @Test
        fun `Convert Long to NOTUPLOADED`() {
            assertThat(uploadStateConv.toUploadState(ContactEvent.UploadState.NOTUPLOADED.code))
                .isEqualTo(ContactEvent.UploadState.NOTUPLOADED)
        }
        @Test
        fun `Convert Long to UPLOADING`() {
            assertThat(uploadStateConv.toUploadState(ContactEvent.UploadState.UPLOADING.code))
                .isEqualTo(ContactEvent.UploadState.UPLOADING)
        }
        @Test
        fun `Convert Long to UPLOADED`() {
            assertThat(uploadStateConv.toUploadState(ContactEvent.UploadState.UPLOADED.code))
                .isEqualTo(ContactEvent.UploadState.UPLOADED)
        }
        @Test
        fun `Input Invalid Long Throw IllegalArgumentException`() {
            assertThatThrownBy {uploadStateConv.toUploadState(-1)}
                .isExactlyInstanceOf(IllegalArgumentException::class.java)
        }

    }

    @Nested
    inner class ToInteger() {

        @Test
        fun `Convert NOTUPLOADED to Long`() {
            assertThat(uploadStateConv.toInteger(ContactEvent.UploadState.NOTUPLOADED))
                .isEqualTo(ContactEvent.UploadState.NOTUPLOADED.code)
        }
        @Test
        fun `Convert UPLOADING to Long`() {
            assertThat(uploadStateConv.toInteger(ContactEvent.UploadState.UPLOADING))
                .isEqualTo(ContactEvent.UploadState.UPLOADING.code)
        }
        @Test
        fun `Convert UPLOADED to Long`() {
            assertThat(uploadStateConv.toInteger(ContactEvent.UploadState.UPLOADED))
                .isEqualTo(ContactEvent.UploadState.UPLOADED.code)
        }

    }
}