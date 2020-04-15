package org.covidwatch.android.data.backend.firebase

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlin.ranges.ClosedRange
import org.covidwatch.android.R
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.backend.ContactEventFetcher
import org.covidwatch.android.data.backend.BackendConstants
import org.covidwatch.android.data.backend.InfectionState
import java.util.*

/**
 * Firebase implementation of the ContactEventFetcher interface.
 */
class FirebaseContactEventFetcher(
    private val context: Context
) : ContactEventFetcher {

    companion object {
        private const val TAG = "ResultFetcher"
    }

    override fun fetch(
        timeWindow: ClosedRange<Date>,
        markLocalContactEventsCb: List<String>.(InfectionState) -> Unit
    ) {
        val task = FirebaseFirestore.getInstance()
            .collection(BackendConstants.COLLECTION_CONTACT_EVENTS)
            .whereGreaterThan(
                BackendConstants.FIELD_TIMESTAMP,
                Timestamp(timeWindow.start)
            )
            .get()
            .continueWith(
                CovidWatchDatabase.databaseWriteExecutor,
                Continuation<QuerySnapshot, Unit> {
                    it.result?.handleQueryResult(timeWindow, markLocalContactEventsCb)
                })


        // TODO #14 RE-ADD THIS AFTER FIREBASE QUOTA INCREASED
        Tasks.await(task)
    }

    private fun QuerySnapshot.handleQueryResult(
        timeWindow: ClosedRange<Date>,
        markLocalContactEventsCb: List<String>.(InfectionState) -> Unit
    ) {
        Log.i(
            TAG,
            "Downloaded ${size()} contact event(s)"
        )
        try {
            val addedDocumentChanges =
                documentChanges.filter {
                    it.type == DocumentChange.Type.ADDED
                }
            val removedDocumentChanges =
                documentChanges.filter {
                    it.type == DocumentChange.Type.REMOVED
                }
            addedDocumentChanges.markLocalContactEvents {
                markLocalContactEventsCb(InfectionState.PotentiallyInfectious)
            }
            removedDocumentChanges.markLocalContactEvents {
                markLocalContactEventsCb(InfectionState.Healthy)
            }

            with(
                context.getSharedPreferences(
                    context.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                ).edit()
            ) {
                putLong(
                    context.getString(R.string.preference_last_contact_events_download_date),
                    timeWindow.endInclusive.time
                )
                commit()
            }

            ListenableWorker.Result.success()

        } catch (exception: Exception) {
            ListenableWorker.Result.failure()
        }
    }

    private fun List<DocumentChange>.markLocalContactEvents(
        markLocalContactEvents: List<String>.() -> Unit
    ) {
        if (isEmpty()) return
        val identifiers = map { it.document.id }
        identifiers.markLocalContactEvents()
    }
}