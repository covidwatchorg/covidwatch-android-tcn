package org.covidwatch.android.data

import android.content.SharedPreferences
import org.covidwatch.android.domain.*

private const val IS_FIRST_TIME_USER_KEY = "is_first_time_user"
private const val IS_SETUP_FINISHED_KEY = "is_setup_finished"

class UserFlowRepositoryImpl(
    private val preferences: SharedPreferences
) : UserFlowRepository {

    override fun getUserFlow(): UserFlow {
        val isFirstTimeUser = preferences.getBoolean(IS_FIRST_TIME_USER_KEY, true)
        val isSetupFinished = preferences.getBoolean(IS_SETUP_FINISHED_KEY, false)

        return when {
            !isSetupFinished -> {
                Setup
            }
            isFirstTimeUser -> {
                FirstTimeUser
            }
            else -> {
                ReturnUser
            }
        }
    }

    override fun updateFirstTimeUserFlow() {
        preferences.edit()
            .putBoolean(IS_FIRST_TIME_USER_KEY, false)
            .apply()
    }

    override fun updateSetupUserFlow() {
        preferences.edit()
            .putBoolean(IS_SETUP_FINISHED_KEY, true)
            .apply()
    }
}