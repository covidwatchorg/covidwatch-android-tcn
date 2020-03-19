package org.covidwatch.android.ui.contactevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.covidwatch.android.data.ContactEventDAO

class ContactEventsViewModelFactory(
    private val contactEventDAO: ContactEventDAO
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactEventsViewModel(contactEventDAO) as T
    }
}
