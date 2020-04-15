package org.covidwatch.android.data.backend

import android.app.Application
import androidx.lifecycle.Observer
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.backend.firebase.FireBaseContactEventPublisher

class LocalContactEventsUploader(var application: Application) {

    private val viewModel: LocalContactEventsViewModel = LocalContactEventsViewModel(
        CovidWatchDatabase.getInstance(application).contactEventDAO(),
        application
    )

    private val contactEventDAO = CovidWatchDatabase.getInstance(application).contactEventDAO()

    // TODO: Get ContactEventPublisher via DI
    private val publisher: ContactEventPublisher = FireBaseContactEventPublisher(contactEventDAO)

    fun startUploading() {
        viewModel.contactEvents.observeForever(Observer {
            uploadContactEventsIfNeeded(it)
        })
    }

    private fun uploadContactEventsIfNeeded(contactEvents: List<ContactEvent>) {
        val contactEventsToUpload = contactEvents.filter {
            it.wasPotentiallyInfectious && it.uploadState == ContactEvent.UploadState.NOTUPLOADED
        }

        publisher.uploadContactEvents(contactEventsToUpload)
    }
}