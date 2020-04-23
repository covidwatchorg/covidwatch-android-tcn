package org.covidwatch.android.requirements

import org.covidwatch.android.requirements.test_environment.TestApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

class ReceivingTCNs {

    private val app = TestApplication()

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TCNs are stored in the local database`() {

    }
}