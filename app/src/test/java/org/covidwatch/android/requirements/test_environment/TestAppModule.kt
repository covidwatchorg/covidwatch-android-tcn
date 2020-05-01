package org.covidwatch.android.requirements.test_environment

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.covidwatch.android.data.*
import org.covidwatch.android.data.contactevent.ContactEventFetcher
import org.covidwatch.android.data.contactevent.ContactEventPublisher
import org.koin.dsl.module
import org.tcncoalition.tcnclient.TcnKeys

@Suppress("USELESS_CAST")
fun testAppModule(testContext: TestContext) = module(override = true) {
    single {
        mockk<ContactEventDAO>(relaxed = false) {
            every { allSortedByDescTimestamp } answers {
                testContext.allContactEventsSortedByDescTimestamp
            }
        }
    }

    single {
        mockk<TcnKeys>(relaxed = false)
    }

    single {
        mockk<ContactEventFetcher>(relaxed = false)
    }

    single {
        mockk<ContactEventPublisher>(relaxed = false) {
            every { uploadContactEvents(any()) } answers {
                testContext.uploadedTcns += arg<List<ContactEvent>>(0)
            }
        }
    }

    factory {
        mockk<SharedPreferences>(relaxed = false).apply {
            every { getBoolean(any(), any()) } answers { args[1] as Boolean }
        }
    }
}