package com.pleon.buyt.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import java.util.*

class DatePickerDialogFragment : AppCompatDialogFragment() {

    private var callback: DatePickerDialog.OnDateSetListener? = null

    /**
     * The caller fragment should pass getChildFragmentManager() as the fragmentManager argument of
     * dialog show() method.
     *
     * @param savedState
     * @return
     */
    override fun onCreateDialog(savedState: Bundle?): Dialog {
        try {
            callback = parentFragment as DatePickerDialog.OnDateSetListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement $callback interface")
        }

        // Use the current date as the default date in the picker
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireContext(), callback, year, month, day)
    }
}
