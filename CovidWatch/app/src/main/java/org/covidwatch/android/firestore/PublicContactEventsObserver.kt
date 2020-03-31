package org.covidwatch.android.firestore

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase

/* DEPRECATED */
class PublicContactEventsObserver(var context: Context) {

    var registration: ListenerRegistration? = null

    fun startObserving() {
        registration?.remove()
//        val twoWeeksPastNow = Date()
//        twoWeeksPastNow.time += -60 * 60 * 24 * 7 * 2
        registration = FirebaseFirestore.getInstance().collection("contact_events")
//            .whereGreaterThan("timestamp", Timestamp(twoWeeksPastNow))
            .addSnapshotListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listening for realtime updates of contact events failed ", e)
                    return@addSnapshotListener
                }
                val queryDocumentSnapshot = queryDocumentSnapshots
                    ?: return@addSnapshotListener
                Log.d(
                    TAG,
                    "Listened for realtime updates of ${queryDocumentSnapshots.size()} contact event(s)"
                )
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    val addedDocumentChanges =
                        queryDocumentSnapshot.documentChanges.filter {
                            it.type == DocumentChange.Type.ADDED
                        }
                    val removedDocumentChanges =
                        queryDocumentSnapshot.documentChanges.filter {
                            it.type == DocumentChange.Type.REMOVED
                        }
                    markLocalContactEvents(addedDocumentChanges, true)
                    markLocalContactEvents(removedDocumentChanges, false)
                }
            }
    }

    private fun markLocalContactEvents(
        documentChanges: List<DocumentChange>,
        wasPotentiallyInfectious: Boolean
    ) {
        if (documentChanges.isEmpty()) return
        Log.d(
            TAG,
            "Marking ${documentChanges.size} contact event(s) as potentially infectious=$wasPotentiallyInfectious ..."
        )
        val dao: ContactEventDAO = CovidWatchDatabase.getInstance(context).contactEventDAO()
        val identifiers = documentChanges.map { it.document.id }
        val chunkSize = 998 // SQLITE_MAX_VARIABLE_NUMBER - 1
        identifiers.chunked(chunkSize).forEach {
            dao.update(it, wasPotentiallyInfectious)
            Log.d(
                TAG,
                "Marked ${it.size} contact event(s) as potentially infectious=$wasPotentiallyInfectious"
            )
        }
    }

    companion object {
        private const val TAG = "PublicContactEventsObserver"
    }

}