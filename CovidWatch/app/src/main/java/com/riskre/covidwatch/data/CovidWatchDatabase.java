package com.riskre.covidwatch.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ContactEvent.class}, version = 1)
public abstract class CovidWatchDatabase extends RoomDatabase {

    private static CovidWatchDatabase INSTANCE;

    public abstract ContactEventDAO contactEventDAO();

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
//
//            // If you want to keep data through app restarts,
//            // comment out the following block
//            databaseWriteExecutor.execute(() -> {
//                // Populate the database in the background.
//                // If you want to start with more words, just add them.
//                ContactEventDAO dao = INSTANCE.contactEventDAO();
//                dao.deleteAll();
//
//                ContactEvent word = new ContactEvent(UUID.randomUUID(), 69);
//                dao.insert(word);
//                word = new ContactEvent(UUID.randomUUID(), 70);
//                dao.insert(word);
//            });
        }
    };

    static CovidWatchDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CovidWatchDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CovidWatchDatabase.class, "covidwatch_database")
                            .addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

}
