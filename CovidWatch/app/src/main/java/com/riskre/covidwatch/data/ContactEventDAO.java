package com.riskre.covidwatch.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactEventDAO {

    @Query("SELECT * FROM contact_events")
    List<ContactEvent> getAll();

    @Query("SELECT * FROM contact_events ORDER BY timestamp DESC")
    LiveData<List<ContactEvent>> getAllSortedByDescTimestamp();

    @Query("SELECT * FROM contact_events WHERE identifier = :identifier")
    ContactEvent findByPrimaryKey(String identifier);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContactEvent contactEvent);

    @Update
    void update(ContactEvent contactEvent);

    @Query("UPDATE contact_events SET was_potentially_infectious = :wasPotentiallyInfectious WHERE identifier IN (:identifiers)")
    void update(List<String> identifiers, Boolean wasPotentiallyInfectious);

    @Query("DELETE FROM contact_events")
    void deleteAll();
}
