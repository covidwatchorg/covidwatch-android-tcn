package org.covidwatch.android.data.backend.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.backend.BackendConstants
import org.covidwatch.android.data.backend.ContactEventPublisher

/**
 * Firebase implementaton of the ContactEventPublisher interface.
 */
class FireBaseContactEventPublisher(
    private val contactEventDAO: ContactEventDAO
) : ContactEventPublisher {

    companion object {
        private const val TAG = "FireBaseContactEventPublisher"
    }

    override fun uploadContactEvents(contactEvents: List<ContactEvent>) {
        if (contactEvents.isEmpty()) return
        Log.i(
            TAG,
            "Uploading ${contactEvents.size} contact event(s)..."
        )
        CovidWatchDatabase.databaseWriteExecutor.execute {
            contactEvents.forEach {
                it.uploadState =
                    ContactEvent.UploadState.UPLOADING
            }
            contactEventDAO.update(contactEvents)
        }
        val db = FirebaseFirestore.getInstance()
        db.runBatch { batch ->
            contactEvents.forEach {
                batch.set(
                    db.collection(BackendConstants.COLLECTION_CONTACT_EVENTS)
                        .document(it.identifier),
                    hashMapOf(
                        BackendConstants.FIELD_TIMESTAMP to Timestamp(
                            it.timestamp
                        )
                    )
                )
            }
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(
                    TAG,
                    "Uploaded ${contactEvents.size} contact event(s)"
                )
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    contactEvents.forEach {
                        it.uploadState =
                            ContactEvent.UploadState.UPLOADED
                    }
                    contactEventDAO.update(contactEvents)
                }
            } else {
                Log.d(
                    TAG,
                    "Uploading ${contactEvents.size} contact event(s) failed: ${task.exception}"
                )
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    contactEvents.forEach {
                        it.uploadState =
                            ContactEvent.UploadState.NOTUPLOADED
                    }
                    contactEventDAO.update(contactEvents)
                }
            }
        }
    }
}