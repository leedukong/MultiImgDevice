package com.releasetech.multidevice.Tool;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    DatePickerDialog.OnDateSetListener onDateSetListener;

    int initialYear;
    int initialMonth;
    int initialDay;

    public DatePickerFragment(int year, int month, int day) {
        initialYear = year;
        initialMonth = month - 1;
        initialDay = day;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), this, initialYear, initialMonth, initialDay);
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        onDateSetListener.onDateSet(datePicker, year, month + 1, day);
    }
}