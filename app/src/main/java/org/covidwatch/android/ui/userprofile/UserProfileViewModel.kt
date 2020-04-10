package org.covidwatch.android.ui.userprofile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.covidwatch.android.R

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    var isCurrentUserSick = MutableLiveData<Boolean>().apply {
        val isSick = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getBoolean(application.getString(R.string.preference_is_current_user_sick), false)
        value = isSick
    }

}