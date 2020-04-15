package org.covidwatch.android.domain

sealed class UserFlow

object FirstTimeUser : UserFlow()

object Setup : UserFlow()

object ReturnUser : UserFlow()