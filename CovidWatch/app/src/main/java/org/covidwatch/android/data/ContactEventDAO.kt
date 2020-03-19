package org.covidwatch.android.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Dao
interface ContactEventDAO {
    @get:Query("SELECT * FROM contact_events")
    val all: List<ContactEvent>

    @get:Query("SELECT * FROM contact_events ORDER BY timestamp DESC")
    val allSortedByDescTimestamp: LiveData<List<ContactEvent>>

    @get:Query("SELECT * FROM contact_events ORDER BY timestamp DESC")
    val pagedAllSortedByDescTimestamp: DataSource.Factory<Int, ContactEvent>

    @Query("SELECT * FROM contact_events WHERE identifier = :identifier")
    fun findByPrimaryKey(identifier: String): ContactEvent

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contactEvent: ContactEvent)

    @Update
    fun update(contactEvent: ContactEvent)

    @Query("UPDATE contact_events SET was_potentially_infectious = :wasPotentiallyInfectious WHERE identifier IN (:identifiers)")
    fun update(
        identifiers: List<String>,
        wasPotentiallyInfectious: Boolean
    )

    @Query("DELETE FROM contact_events")
    fun deleteAll()
}