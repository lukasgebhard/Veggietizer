package freerunningapps.veggietizer.view.fragment;

import android.support.annotation.NonNull;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.view.activity.InputActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

/**
 * A dialog that allows to pick a weight from a certain range.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class WeightPickerFragment extends DialogFragment {
    /**
     * The minimal weight in grammes that can be picked.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MIN_WEIGHT = 10;

    /**
     * An upper bound for the weight in grammes that can be picked.
     * The actual maximal weight that can be picked may be less, depending on the specified accuracy.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_WEIGHT = 2000;

    /**
     * Defines in the distance between to weights in grammes.
     */
    public static final int ACCURACY = 10;

    /**
     * This dialog's layout. Needed by the positive button's click listener.
     */
    private View dialogLayout;

    /**
     * References the host activity.
     */
    private WeightPickerDialogListener dialogListener;

    private InputActivity inputActivity;

    /**
     * The listener referring to {@link WeightPickerFragment}.
     * Must be implemented the activity that wants to be informed about events fired by
     * {@link WeightPickerFragment}.
     *
     * @author Lukas Gebhard <freerunningapps@gmail.com>
     *
     */
    public interface WeightPickerDialogListener {
        /**
         * Invoked when the user clicks the {@link WeightPickerFragment}'s positive button.
         *
         * @param weightSelected The weight the user picked.
         */
        void onWeightSelected(int weightSelected);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        inputActivity = (InputActivity) activity;

        // Verifies that the host activity implements the callback interface.
        try {
            // Instantiates the listener so that events can be sent to the host.
            dialogListener = (WeightPickerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + getClass().getName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initWeightPicker();

        builder.setView(dialogLayout);
        builder.setTitle(R.string.title_fragment_weightpicker);
        builder.setPositiveButton(R.string.weightpicker_accept, new DialogInterface.OnClickListener() {
                   @Override
                public void onClick(DialogInterface dialog, int id) {
                       NumberPicker weightPicker = (NumberPicker) dialogLayout.findViewById(R.id.weightPicker);
                       int weightIndex = weightPicker.getValue();
                       int weight = ++weightIndex * ACCURACY; // Applies the value mapping.

                       dialogListener.onWeightSelected(weight);
                   }
               });
        builder.setNegativeButton(R.string.weightpicker_cancel, new DialogInterface.OnClickListener() {
                   @Override
                public void onClick(DialogInterface dialog, int id) {
                       // The system automatically dismisses this dialog on clicking a button.
                   }
               });

        return builder.create();
    }

    private void initWeightPicker() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        int numOfWeights = (int) Math.floor((MAX_WEIGHT - MIN_WEIGHT) / ACCURACY + 1);
        String[] weights = new String[numOfWeights];
        NumberPicker weightPicker;
        int defaultWeight = inputActivity.getWeightSelected();
        int defaultValue = defaultWeight / ACCURACY - 1; // Undoes the mapping.

        // Does not apply a parent view (param null) since this is a dialog and, thus, has its own layout.
        dialogLayout = inflater.inflate(R.layout.fragment_weightpicker, null);

        weightPicker = (NumberPicker) dialogLayout.findViewById(R.id.weightPicker);

        /*
         * Creates the weights to pick. The user may select a weight between 10 and 990 grammes, in steps of 10
         * grammes.
         */
        for (int i = 0; i < weights.length; ++i) {
            String number = Integer.toString((i + 1) * ACCURACY);
            weights[i] = number;
        }

        weightPicker.setMinValue(0); // Maps to MIN_WEIGHT.
        weightPicker.setMaxValue(weights.length - 1); // Maps to MAX_WEIGHT.
        weightPicker.setValue(defaultValue); // Maps to a weight of (defaultValue + 1) * ACCURACY.
        weightPicker.setDisplayedValues(weights);

        weightPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }
}
