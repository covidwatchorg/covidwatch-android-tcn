package org.covidwatch.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [ContactEvent::class], version = 1)
abstract class CovidWatchDatabase : RoomDatabase() {

    abstract fun contactEventDAO(): ContactEventDAO

    companion object {
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor: ExecutorService =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        @Volatile
        private var INSTANCE: CovidWatchDatabase? = null

        fun getInstance(context: Context): CovidWatchDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CovidWatchDatabase::class.java, "covidwatch.db"
            ).fallbackToDestructiveMigration().build()
    }
}