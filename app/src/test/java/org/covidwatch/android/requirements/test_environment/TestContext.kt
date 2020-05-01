package org.covidwatch.android.requirements.test_environment

import androidx.lifecycle.LiveData
import org.covidwatch.android.data.ContactEvent

class TestContext {
    val allContactEventsSortedByDescTimestamp = TestLiveData<List<ContactEvent>>()
    var uploadedTcns = listOf<ContactEvent>()
}