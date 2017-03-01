package freerunningapps.veggietizer.view.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.view.activity.InputActivity;

/**
 * A dialog to pick a date.
 * <p />
 * This implementation does not allow to pick a future date.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class DatePickerFragment extends DialogFragment implements OnDateSetListener {
    private Activity hostActivity;

    /**
     * Number of calls of {@link DatePickerFragment#onDateSet(DatePicker, int, int, int)}.
     * Needed due to a known Android bug: When calling this method is executed twice.
     */
    private int numOfCalls;

    public DatePickerFragment() { // Default constructor needed to avoid crashes on rotation
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        hostActivity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Uses the current date as the default date in the picker
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        DatePicker datePicker = dialog.getDatePicker();

        datePicker.setCalendarViewShown(false);
        numOfCalls = 0;

        return dialog;
    }

    /**
     * Invoked when the user modified the date.
     * If the date is in the future, this re-opens the date picker and prompts the user to select another date
     * (a {@link Toast} is shown).
     * Else, the new date is set in the input mask.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (numOfCalls < 1) {
            TextView textViewDate = (TextView) hostActivity.findViewById(R.id.textview_date);
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
            Date today;
            Date selectedDate;
            String todayStr = getResources().getString(R.string.today);

            // Needed so that the selected date can be compared to today's date.
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            today = calendar.getTime();

            calendar.set(year, monthOfYear, dayOfMonth);
            selectedDate = calendar.getTime();

            if (selectedDate.compareTo(today) > 0) { // Selected date is in the future
                Toast toast = Toast.makeText(hostActivity.getApplicationContext(), R.string.toast_datepicker_future,
                        Toast.LENGTH_LONG);
                ((InputActivity) hostActivity).showDatePicker(textViewDate);
                toast.show();
            } else if ((selectedDate.compareTo(today) < 0)) {
                textViewDate.setText(dateFormat.format(selectedDate));
            } else {
                textViewDate.setText(todayStr);
            }
        }
        ++numOfCalls;
    }
}
