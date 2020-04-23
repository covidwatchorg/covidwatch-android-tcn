package org.covidwatch.android.data.contactevent

import android.app.Application
import androidx.lifecycle.Observer
import org.covidwatch.android.data.ContactEvent
import org.koin.core.KoinComponent
import org.koin.core.inject

class LocalContactEventsUploader(var application: Application) : KoinComponent {

    private val viewModel: LocalContactEventsViewModel = LocalContactEventsViewModel(application)
    private val publisher: ContactEventPublisher by inject()

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