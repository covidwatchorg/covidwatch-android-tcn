package org.covidwatch.android.requirements.test_environment

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.covidwatch.android.data.ContactEvent

class TestContext {
    private val _preferences = mutableMapOf<String, Any?>()
    val allContactEventsSortedByDescTimestamp = TestLiveData<List<ContactEvent>>()
    var uploadedTcns = listOf<ContactEvent>()
    val sharedPreferences: SharedPreferences = mockk(relaxed = false) {
        every { getBoolean(any(), any()) } answers {
            (_preferences[args[0]] ?: args[1]) as Boolean
        }
    }
}