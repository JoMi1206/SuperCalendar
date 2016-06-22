package de.haw.yumiii.supercalendar.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yumiii on 08.06.16.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String ARG_YEAR = "year";
    public static final String ARG_MONTH = "month";
    public static final String ARG_DAY = "day";

    OnFragmentDateSetListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentDateSetListener) {
            mListener = (OnFragmentDateSetListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnFragmentDateSetListener.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // argumente uerbegeben bei erzeugung des Fragments
        Bundle args = getArguments();
        int year = args.getInt(ARG_YEAR);
        int month = args.getInt(ARG_MONTH);
        int day = args.getInt(ARG_DAY);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);

        mListener.onFragmentDateSetListener(cal.getTime());
    }

    public interface OnFragmentDateSetListener {
        void onFragmentDateSetListener(Date newDate);
    }
}
