package com.riskre.covidwatch.data;
//  Created by Zsombor SZABO on 17/03/2020.
//  Copyright © IZE. All rights reserved.
//  See LICENSE.txt for licensing information.
//  

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ContactEvent.class}, version = 1)
public abstract class CovidWatchDatabase extends RoomDatabase {

    private static CovidWatchDatabase INSTANCE;

    public abstract ContactEventDAO contactEventDAO();

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static CovidWatchDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CovidWatchDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CovidWatchDatabase.class, "covidwatch_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
