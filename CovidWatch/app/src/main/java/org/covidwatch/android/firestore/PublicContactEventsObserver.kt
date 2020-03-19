package org.covidwatch.android.firestore

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import java.util.*

class PublicContactEventsObserver(var context: Context) {

    var registration: ListenerRegistration? = null

    private fun startObservingPublicContactEvents() {
        if (registration != null) {
            registration!!.remove()
        }
        val db = FirebaseFirestore.getInstance()
        registration = db.collection("contact_events").addSnapshotListener { queryDocumentSnapshots, e ->
            if (e != null) {
                Log.w(
                    TAG,
                    "Listening for realtime updates of contact events failed",
                    e
                )
                return@addSnapshotListener
            }
            Log.d(
                TAG,
                "Listened for realtime updates of " + queryDocumentSnapshots!!.size() + " contact event(s)"
            )
            val addedDocumentChanges: MutableList<DocumentChange> =
                ArrayList()
            val removedDocumentChanges: MutableList<DocumentChange> =
                ArrayList()
            for (dc in queryDocumentSnapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> addedDocumentChanges.add(
                        dc
                    )
                    DocumentChange.Type.MODIFIED -> {
                    }
                    DocumentChange.Type.REMOVED -> removedDocumentChanges.add(
                        dc
                    )
                }
            }
            markLocalContactEvents(addedDocumentChanges, true)
            markLocalContactEvents(removedDocumentChanges, false)

        }
    }

    private fun markLocalContactEvents(
        documentChanges: List<DocumentChange>,
        infectious: Boolean
    ) {
        if (documentChanges.isEmpty()) {
            return
        }
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val dao: ContactEventDAO =
                CovidWatchDatabase.getInstance(context).contactEventDAO()
            val identifiers =
                ArrayList<String>()
            for (obj in documentChanges) {
                identifiers.add(obj.document.id)
            }
            // TODO: Handle "SQLiteException too many SQL variables (Sqlite code 1)" in the case of more than 999 parameters
            dao.update(identifiers, infectious)
        }
    }

    companion object {
        private const val TAG = "PublicContactEventsObserver"
    }

    init {
        startObservingPublicContactEvents()
    }
}