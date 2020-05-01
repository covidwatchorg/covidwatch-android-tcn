package org.covidwatch.android.requirements.test_environment

import androidx.lifecycle.LiveData

class TestLiveData<T> : LiveData<T>() {
    fun setLiveValue(newValue: T) { value = newValue }
}