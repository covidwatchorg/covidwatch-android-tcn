package org.covidwatch.android.ui.util

import android.app.DatePickerDialog
import androidx.fragment.app.Fragment
import java.util.*

fun Fragment.showDatePicker(callback: (date: Date) -> Unit) {
    val calendar = Calendar.getInstance()
    val dialog = DatePickerDialog(
        requireContext(),
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            callback(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    dialog.datePicker.maxDate = Date().time

    dialog.show()
}