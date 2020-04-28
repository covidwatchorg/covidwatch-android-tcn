package org.covidwatch.android.data.signedreport

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SignedReportDAO {

    @Query("SELECT * FROM signed_reports")
    fun all(): Flow<List<SignedReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signedReport: SignedReport)

    @Update
    fun update(signedReport: SignedReport)

    @Update
    fun update(signedReport: List<SignedReport>)

}
