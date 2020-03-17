package com.riskre.covidwatch.data;
//  Created by Zsombor SZABO on 17/03/2020.
//  Copyright © IZE. All rights reserved.
//  See LICENSE.txt for licensing information.
//  

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContactEventDAO {
    @Query("SELECT * FROM contact_events")
    List<ContactEvent> getAll();

    @Query("SELECT * FROM contact_events ORDER BY timestamp DESC")
    LiveData<List<ContactEvent>> getAllSortedByDescTimestamp();

    @Insert
    void insertAll(ContactEvent... contactEvents);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContactEvent contactEvent);

    @Delete
    void delete(ContactEvent contactEvent);

    @Query("DELETE FROM contact_events")
    void deleteAll();
}
