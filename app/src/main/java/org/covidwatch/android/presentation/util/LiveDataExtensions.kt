package org.covidwatch.android.presentation.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Sets the value to the result of a function that is called when both `LiveData`s have data
 * or when they receive updates after that.
 */
fun <T, A, B> LiveData<A>.combineAndCompute(
    other: LiveData<B>,
    onChange: (A, B) -> T
): MediatorLiveData<T> {

    var firstSourceEmitted = false
    var secondSourceEmitted = false

    val result = MediatorLiveData<T>()

    val mergeF = {
        val firstSourceValue = this.value
        val secondSourceValue = other.value

        if (firstSourceEmitted && secondSourceEmitted) {
            result.value = onChange.invoke(firstSourceValue!!, secondSourceValue!!)
        }
    }

    result.addSource(this) {
        firstSourceEmitted = true
        mergeF.invoke()
    }
    result.addSource(other) {
        secondSourceEmitted = true
        mergeF.invoke()
    }

    return result
}