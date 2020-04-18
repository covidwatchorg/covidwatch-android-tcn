package org.covidwatch.android.domain

interface UserFlowRepository {

    fun getUserFlow(): UserFlow

    fun updateFirstTimeUserFlow()

    fun updateSetupUserFlow()
}