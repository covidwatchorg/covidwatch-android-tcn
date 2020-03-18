package com.riskre.covidwatch.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.riskre.covidwatch.CovidWatchApplication;

import java.util.List;

class CovidWatchRepository {

    private ContactEventDAO cenDAO;
    private LiveData<List<ContactEvent>> allEvents;

    CovidWatchRepository(Application application) {
        CovidWatchDatabase db = CovidWatchDatabase.getDatabase(application);
        cenDAO = db.contactEventDAO();
        allEvents = cenDAO.getAllSortedByDescTimestamp();
    }

    /**
     * Room executes all queries on a separate thread.
     * Observed LiveData will notify the observer when the data has changed.
     */
    LiveData<List<ContactEvent>> getAllEvents() {
        return allEvents;
    }

    /**
     * You must call this on a non-UI thread or your app will throw an exception. Room ensures
     * that you're not doing any long running operations on the main thread, blocking the UI.
     */
    void insert(ContactEvent cen) {
        CovidWatchDatabase.databaseWriteExecutor.execute(() -> {
            cenDAO.insert(cen);
        });
    }
}