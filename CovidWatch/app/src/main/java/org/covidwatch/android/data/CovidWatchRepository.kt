package org.covidwatch.android.data

import android.app.Application
import androidx.lifecycle.LiveData


internal class CovidWatchRepository(application: Application) {

    private val contactEventDAO: ContactEventDAO

    /**
     * TODO
     * Room executes all queries on a separate thread.
     * Observed LiveData will notify the observer when the data has changed.
     *
     * @return allEvents, all the contact events in the DB
     */
    val allEvents: LiveData<List<ContactEvent>>

    /**
     * You must call this on a non-UI thread or your app will throw an exception. Room ensures
     * that you're not doing any long running operations on the main thread, blocking the UI.
     */
    fun insert(cen: ContactEvent) {
        CovidWatchDatabase.databaseWriteExecutor.execute { contactEventDAO.insert(cen) }
    }

    init {
        val db = CovidWatchDatabase.getInstance(application)
        contactEventDAO = db.contactEventDAO()
        allEvents = contactEventDAO.allSortedByDescTimestamp
    }
}