package org.covidwatch.android.presentation

import androidx.lifecycle.*
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.domain.TestedRepository
import org.covidwatch.android.presentation.util.Event
import org.covidwatch.android.presentation.util.getDistinct

class MainViewModel(
    private val testedRepository: TestedRepository,
    contactEventDAO: ContactEventDAO
) : ViewModel() {

    private val isUserTestedPositive: Boolean get() = testedRepository.isUserTestedPositive()

    private val hasPossiblyInteractedWithInfected: LiveData<Boolean> =
        Transformations
            .map(contactEventDAO.allSortedByDescTimestamp) { cenList ->
                cenList.fold(initial = false) { isInfected: Boolean, event: ContactEvent ->
                    isInfected || event.wasPotentiallyInfectious
                }
            }
            .getDistinct()

    private val interactedWithInfectedObserver =
        Observer<Boolean> { hasPossiblyInteractedWithInfected ->
            if (hasPossiblyInteractedWithInfected && !isUserTestedPositive) {
                _possibleExposureNotificationEvent.value = Event(Unit)
            }
        }

    private val _possibleExposureNotificationEvent = MutableLiveData<Event<Unit>>()
    val possibleExposureNotificationEvent: LiveData<Event<Unit>> get() = _possibleExposureNotificationEvent

    init {
        hasPossiblyInteractedWithInfected.observeForever(interactedWithInfectedObserver)
    }

    override fun onCleared() {
        super.onCleared()
        hasPossiblyInteractedWithInfected.removeObserver(interactedWithInfectedObserver)
    }
}