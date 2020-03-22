package org.covidwatch.android.firestore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO

class LocalContactEventsViewModel(
    contactEventDAO: ContactEventDAO,
    application: Application
) : AndroidViewModel(application) {

    val contactEvents: LiveData<List<ContactEvent>> = contactEventDAO.allSortedByDescTimestamp
}
