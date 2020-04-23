package org.covidwatch.android.data.contactevent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.koin.core.KoinComponent
import org.koin.core.inject

class LocalContactEventsViewModel(
    application: Application
) : AndroidViewModel(application), KoinComponent {

    private val contactEventDAO: ContactEventDAO by inject()
    val contactEvents: LiveData<List<ContactEvent>> = contactEventDAO.allSortedByDescTimestamp
}
