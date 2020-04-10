package org.covidwatch.android

//import android.support.v4.app.DialogFragment
//import android.support.v4.app.Fragment
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import org.covidwatch.android.ui.MainActivity
import java.util.*

/**
 * A simple [Fragment] subclass for the date picker.
 */
class DatePickerFragment : DialogFragment(), OnDateSetListener {
    /**
     * Creates the date picker dialog with the current date from Calendar.
     *
     * @param savedInstanceState    Saved instance state bundle
     * @return DatePickerDialog     The date picker dialog
     */
    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker.
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        // Create a new instance of DatePickerDialog and return it.
//        return DatePickerDialog(getActivity(), this, year, month, day)
//        return DatePickerDialog(
//            activity!!,
//            targetFragment as OnDateSetListener?,
//            year,
//            month,
//            day
//        )
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    /**
     * Grabs the date and passes it to processDatePickerResult().
     *
     * @param datePicker  The date picker view
     * @param year  The year chosen
     * @param month The month chosen
     * @param day   The day chosen
     */
    override fun onDateSet(
        datePicker: DatePicker,
        year: Int,
        month: Int,
        day: Int
    ) {
        // Set the activity to the Main Activity.
        val activity: MainActivity = getActivity() as MainActivity
        // Invoke Main Activity's processDatePickerResult() method.
        activity.processDatePickerResult(year, month, day)
    }
}