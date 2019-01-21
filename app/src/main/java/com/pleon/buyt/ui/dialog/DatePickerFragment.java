package com.pleon.buyt.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatDialogFragment;

public class DatePickerFragment extends AppCompatDialogFragment {

    private DatePickerDialog.OnDateSetListener callback;

    /**
     * The caller fragment should pass getChildFragmentManager() as the fragmentManager argument of
     * dialog show() method.
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            callback = (DatePickerDialog.OnDateSetListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement OnDateSetListener interface");
        }

        // Use the current date as the default date in the picker
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getContext(), callback, year, month, day);
    }
}
