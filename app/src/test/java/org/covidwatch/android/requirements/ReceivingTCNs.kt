package org.covidwatch.android.requirements

import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.requirements.test_environment.TestApplication
import org.covidwatch.android.requirements.test_environment.TestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

class ReceivingTCNs {

    private val testContext = TestContext()
    private val app = TestApplication(testContext)

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TCNs are stored in the local database`() {
        val tcn = ContactEvent().apply { wasPotentiallyInfectious = true }
        app.receiveProximityEvent(tcn)
        app.receiveTCNFromBackend(tcn)
    }
}