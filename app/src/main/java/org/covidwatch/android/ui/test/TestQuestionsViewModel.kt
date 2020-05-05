package org.covidwatch.android.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.covidwatch.android.ui.test.model.TestDate
import org.covidwatch.android.ui.util.combineAndCompute
import java.text.SimpleDateFormat
import java.util.*

class TestQuestionsViewModel : ViewModel() {

    private val _isTested = MutableLiveData<Boolean>()
    val isTested: LiveData<Boolean> get() = _isTested

    private val simpleDateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    }
    private val _testDate = MutableLiveData<TestDate>()
    val testDate: LiveData<TestDate> get() = _testDate

    private val _isReportButtonVisible: MediatorLiveData<Boolean>
    val isReportButtonVisible: LiveData<Boolean> get() = _isReportButtonVisible

    init {
        _isReportButtonVisible = _isTested.combineAndCompute(_testDate) { isTested, testDate ->
            isTested && testDate.isChecked
        }
        _testDate.value = TestDate(simpleDateFormat.format(Date()), false)
    }

    fun onDateSelected(date: Date) {
        _testDate.value = TestDate(simpleDateFormat.format(date), true)
    }

    fun onRadioButtonClicked(isTested: Boolean) {
        _isTested.value = isTested
    }
}