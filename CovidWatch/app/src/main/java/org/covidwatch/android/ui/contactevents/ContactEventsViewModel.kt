package org.covidwatch.android.ui.contactevents

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO

class ContactEventsViewModel(contactEventDAO: ContactEventDAO, application: Application) : AndroidViewModel(application) {

    val contactEvents: LiveData<PagedList<ContactEvent>> =
        contactEventDAO.pagedAllSortedByDescTimestamp.toLiveData(pageSize = 50)

    var isContactEventLoggingEnabled = MutableLiveData<Boolean>().apply {
        val isEnabled = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getBoolean(
            application.getString(R.string.preference_is_contact_event_logging_enabled),
            false
        )
        value = isEnabled
    }

}