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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contactEvent: ContactEvent)

    @Update
    fun update(contactEvent: ContactEvent)

    @Update
    fun update(contactEvents: List<ContactEvent>)

    @Query("UPDATE contact_events SET was_potentially_infectious = :wasPotentiallyInfectious WHERE identifier IN (:identifiers)")
    fun update(identifiers: List<String>, wasPotentiallyInfectious: Boolean)

    @Query("UPDATE contact_events SET was_potentially_infectious = '1'")
    fun markAllAsPotentiallyInfectious()

    @Query("UPDATE contact_events SET upload_state = :uploadState WHERE identifier IN (:identifiers)")
    fun update(identifiers: List<String>, uploadState: Int)

    @Query("DELETE FROM contact_events")
    fun deleteAll()
}