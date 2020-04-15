package org.covidwatch.android.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.covidwatch.android.domain.FirstTimeUser
import org.covidwatch.android.domain.UserFlow
import org.covidwatch.android.domain.UserFlowRepository

class HomeViewModel(
    private val userFlowRepository: UserFlowRepository
) : ViewModel() {

    private val _userFlow = MutableLiveData<UserFlow>()
    val userFlow: LiveData<UserFlow> get() = _userFlow

    fun setup() {
        val userFlow = userFlowRepository.getUserFlow()
        if (userFlow is FirstTimeUser) {
            userFlowRepository.updateFirstTimeUserFlow()
        }
        _userFlow.value = userFlow
    }
}