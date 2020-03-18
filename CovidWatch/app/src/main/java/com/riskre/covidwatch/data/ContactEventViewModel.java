package com.riskre.covidwatch.data;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * TODO
 */
public class ContactEventViewModel extends AndroidViewModel {

    private CovidWatchRepository repository;
    private LiveData<List<ContactEvent>> allEvents;

    /**
     * TODO
     * @param application
     */
    public ContactEventViewModel(Application application) {
        super(application);
        repository = new CovidWatchRepository(application);
        allEvents = repository.getAllEvents();
    }

    /**
     * TODO
     * @return
     */
    public LiveData<List<ContactEvent>> getAllEvents() {
        return allEvents;
    }

}
