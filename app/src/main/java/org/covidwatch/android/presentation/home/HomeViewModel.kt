package org.covidwatch.android.presentation.home

import androidx.lifecycle.*
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.domain.*
import org.covidwatch.android.presentation.util.getDistinct

class HomeViewModel(
    private val userFlowRepository: UserFlowRepository,
    private val testedRepository: TestedRepository,
    contactEventDAO: ContactEventDAO
) : ViewModel() {

    private val isUserTestedPositive: Boolean get() = testedRepository.isUserTestedPositive()
    private val _userTestedPositive = MutableLiveData<Unit>()
    val userTestedPositive: LiveData<Unit> get() = _userTestedPositive

    private val _warningBanner = MutableLiveData<WarningBanner>()
    val warningBanner: LiveData<WarningBanner> get() = _warningBanner

    private val _userFlow = MutableLiveData<UserFlow>()
    val userFlow: LiveData<UserFlow> get() = _userFlow

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
                _warningBanner.value = WarningBanner.Show(R.string.contact_alert_text)
            }
        }

    init {
        hasPossiblyInteractedWithInfected.observeForever(interactedWithInfectedObserver)
    }

    override fun onCleared() {
        super.onCleared()
        hasPossiblyInteractedWithInfected.removeObserver(interactedWithInfectedObserver)
    }

    fun onStart() {
        val userFlow = userFlowRepository.getUserFlow()
        if (userFlow is FirstTimeUser) {
            userFlowRepository.updateFirstTimeUserFlow()
        }
        if (userFlow !is Setup) {
            checkIfTestedPositive()
        }
        _userFlow.value = userFlow
    }

    private fun checkIfTestedPositive() {
        if (isUserTestedPositive) {
            _userTestedPositive.value = Unit
            _warningBanner.value = WarningBanner.Show(R.string.reported_alert_text)
        }
    }
}