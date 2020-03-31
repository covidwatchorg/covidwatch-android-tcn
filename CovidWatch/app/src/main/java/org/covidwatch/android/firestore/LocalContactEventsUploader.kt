package org.covidwatch.android.firestore

import android.app.Application
import android.util.Log
import androidx.lifecycle.Observer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.CovidWatchDatabase

class LocalContactEventsUploader(var application: Application) {

    private val viewModel: LocalContactEventsViewModel = LocalContactEventsViewModel(
        CovidWatchDatabase.getInstance(application).contactEventDAO(),
        application
    )

    private val contactEventDAO =
        CovidWatchDatabase.getInstance(application).contactEventDAO()

    fun startUploading() {
        viewModel.contactEvents.observeForever(Observer {
            uploadContactEventsIfNeeded(it)
        })
    }

    private fun uploadContactEventsIfNeeded(contactEvents: List<ContactEvent>) {
        val contactEventsToUpload = contactEvents.filter {
            it.wasPotentiallyInfectious && it.uploadState == ContactEvent.UploadState.NOTUPLOADED
        }
        uploadContactEvents(contactEventsToUpload)
    }

    private fun uploadContactEvents(contactEvents: List<ContactEvent>) {
        if (contactEvents.isEmpty()) return
        Log.i(TAG, "Uploading ${contactEvents.size} contact event(s)...")
        CovidWatchDatabase.databaseWriteExecutor.execute {
            contactEvents.forEach { it.uploadState = ContactEvent.UploadState.UPLOADING }
            contactEventDAO.update(contactEvents)
        }
        val db = FirebaseFirestore.getInstance()
        db.runBatch { batch ->
            contactEvents.forEach {
                batch.set(
                    db.collection(FirestoreConstants.COLLECTION_CONTACT_EVENTS)
                        .document(it.identifier),
                    hashMapOf(FirestoreConstants.FIELD_TIMESTAMP to Timestamp(it.timestamp))
                )
            }
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Uploaded ${contactEvents.size} contact event(s)")
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    contactEvents.forEach { it.uploadState = ContactEvent.UploadState.UPLOADED }
                    contactEventDAO.update(contactEvents)
                }
            } else {
                Log.d(
                    TAG,
                    "Uploading ${contactEvents.size} contact event(s) failed: ${task.exception}"
                )
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    contactEvents.forEach { it.uploadState = ContactEvent.UploadState.NOTUPLOADED }
                    contactEventDAO.update(contactEvents)
                }
            }
        }
    }

    companion object {
        private const val TAG = "LocalContactEventsUploader"
    }

}
