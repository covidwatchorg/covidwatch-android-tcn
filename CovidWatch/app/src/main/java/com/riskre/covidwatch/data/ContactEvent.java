package com.riskre.covidwatch.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
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

    @ColumnInfo(name = "signal_strength")
    public int signalStrength;

    @ColumnInfo(name = "upload_state", defaultValue = "0")
    public int uploadState;

    @ColumnInfo(name = "was_potentially_infectious", defaultValue = "false")
    public Boolean wasPotentiallyInfectious;

    public ContactEvent() {}

    public ContactEvent(UUID contactEventUUID, int RSSI) {
        identifier = contactEventUUID.toString();
        signalStrength = RSSI;
        timestamp = new Date(System.currentTimeMillis());
        uploadState = 0;
        wasPotentiallyInfectious = false;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }
}
