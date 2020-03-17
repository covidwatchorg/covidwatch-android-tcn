package com.riskre.covidwatch.data;
//  Created by Zsombor SZABO on 17/03/2020.
//  Copyright © IZE. All rights reserved.
//  See LICENSE.txt for licensing information.
//  

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "contact_events")
@TypeConverters(DateConverter.class)
public class ContactEvent {
    @PrimaryKey
    @NonNull
    public String identifier;

    @ColumnInfo(name = "timestamp")
    public Date timestamp;

    @ColumnInfo(name = "upload_state", defaultValue = "0")
    public int uploadState;

    @ColumnInfo(name = "was_potentially_infectious", defaultValue = "false")
    public Boolean wasPotentiallyInfectious;

    public ContactEvent() {
        identifier = UUID.randomUUID().toString();
        timestamp = new Date(System.currentTimeMillis());
        uploadState = 0;
        wasPotentiallyInfectious = false;
    }

}
