package com.riskre.covidwatch.data;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ContactEventViewModel extends AndroidViewModel {

    private CovidWatchRepository repository;
    private LiveData<List<ContactEvent>> allEvents;

    public ContactEventViewModel(Application application) {
        super(application);
        repository = new CovidWatchRepository(application);
        allEvents = repository.getAllEvents();
    }

    public LiveData<List<ContactEvent>> getAllEvents() {
        return allEvents;
    }

    public void insert(ContactEvent cen) {
        repository.insert(cen);
    }
}
