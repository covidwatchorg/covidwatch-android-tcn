package org.covidwatch.android.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ContactEventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CovidWatchRepository = CovidWatchRepository(application)

    /**
     * TODO
     * @return
     */
    val allEvents: LiveData<List<ContactEvent>>

    /**
     * TODO
     * @param application
     */
    init {
        allEvents = repository.allEvents
    }
}