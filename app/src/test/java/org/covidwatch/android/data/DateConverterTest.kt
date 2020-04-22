package org.covidwatch.android.data

import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import java.util.*

internal class DateConverterTest {

    private val dateConv: DateConverter = DateConverter()

    @Nested
    inner class ToDate() {
        @Test
        fun `Convert Long to Date`() {
            assertThat(dateConv.toDate(205465402)).isEqualTo(Date(205465402))
        }
        @Test
        fun `Convert Null to Date(Null)`() {
            assertThat(dateConv.toDate(null)).isEqualTo(null)
        }
    }

    @Nested
    inner class FromDate() {
        @Test
        fun `Convert Date to Long`() {
            assertThat(dateConv.fromDate(Date(205465402))).isEqualTo(205465402)
        }
        @Test
        fun `Convert Null to Long(Null)`() {
            assertThat(dateConv.fromDate(null)).isEqualTo(null)
        }
    }

}