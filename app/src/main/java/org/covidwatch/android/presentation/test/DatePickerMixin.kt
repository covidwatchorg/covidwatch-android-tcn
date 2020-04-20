package org.covidwatch.android.presentation.test

import android.app.DatePickerDialog
import androidx.fragment.app.Fragment
import java.util.*

interface DatePickerMixin {
    val testQuestionsViewModel: TestQuestionsViewModel
    fun showDatePicker(fragment: Fragment) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            fragment.requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                testQuestionsViewModel.onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = Date().time

        dialog.show()
    }
}