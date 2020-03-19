package org.covidwatch.android.ui.contactevents

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO

class ContactEventsViewModel(contactEventDAO: ContactEventDAO) : ViewModel() {

    val contactEvents: LiveData<PagedList<ContactEvent>> =
        contactEventDAO.pagedAllSortedByDescTimestamp.toLiveData(pageSize = 50)
}