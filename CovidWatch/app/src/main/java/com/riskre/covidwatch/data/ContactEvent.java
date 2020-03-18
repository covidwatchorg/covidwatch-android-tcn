package com.riskre.covidwatch.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "contact_events")
@TypeConverters(DateConverter.class)
public class ContactEvent {
    @PrimaryKey
    @NonNull
    public String identifier;

    @ColumnInfo(name = "advertising_cen")
    public String advertisingCEN;

    @ColumnInfo(name = "timestamp")
    public Date timestamp;

    @ColumnInfo(name = "signal_strength")
    public int signalStrength;

    @ColumnInfo(name = "upload_state", defaultValue = "0")
    public int uploadState;

    @ColumnInfo(name = "was_potentially_infectious", defaultValue = "false")
    public Boolean wasPotentiallyInfectious;

    public ContactEvent() {}

    /**
     *
     * @param scannedCEN
     * @param advertisedCEN
     * @param RSSI
     */
    public ContactEvent(String scannedCEN, String advertisedCEN, int RSSI) {
        identifier = scannedCEN;
        advertisingCEN = advertisedCEN;
        signalStrength = RSSI;
        timestamp = new Date(System.currentTimeMillis());
        uploadState = 0;
        wasPotentiallyInfectious = false;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@NonNull String identifier) {
        this.identifier = identifier;
    }

    public String getAdvertisingCEN() {
        return advertisingCEN;
    }

    public void setAdvertisingCEN(String advertisingCEN) {
        this.advertisingCEN = advertisingCEN;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }

    public Boolean getWasPotentiallyInfectious() {
        return wasPotentiallyInfectious;
    }

    public void setWasPotentiallyInfectious(Boolean wasPotentiallyInfectious) {
        this.wasPotentiallyInfectious = wasPotentiallyInfectious;
    }
}
