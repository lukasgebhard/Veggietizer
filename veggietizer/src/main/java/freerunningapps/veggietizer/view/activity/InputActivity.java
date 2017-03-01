package freerunningapps.veggietizer.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.Model;
import freerunningapps.veggietizer.model.util.DateParser;
import freerunningapps.veggietizer.model.enums.Meat;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.model.util.PreferencesAccess;
import freerunningapps.veggietizer.view.Popup;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;
import freerunningapps.veggietizer.view.fragment.DatePickerFragment;
import freerunningapps.veggietizer.view.fragment.WeightPickerFragment;
import freerunningapps.veggietizer.view.fragment.WeightPickerFragment.WeightPickerDialogListener;

import java.util.Calendar;
import java.util.Date;

/**
 * The app's input mask.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class InputActivity extends ActionBarActivity implements WeightPickerDialogListener, OnItemSelectedListener {
    private static final String KEY_MEAT_MODIFIED = "freerunningapps.veggietizer.input.meat_modified";
    private static final String KEY_SUBMIT_DATA_ENABLED = "freerunningapps.veggietizer.input.submit_data_enabled";
    private static final String KEY_DATE = "freerunningapps.veggietizer.input.date";
    private static final String KEY_WEIGHT = "freerunningapps.veggietizer.input.weight";
    private static final String KEY_INPUT_HELP_OPEN
            = "freerunningapps.veggietizer.activity.input.help_open";

    private static final int WEIGHT_CHICKEN = 500;
    private static final int WEIGHT_BEACON = 20;
    private static final int WEIGHT_HOTDOG = 80;
    private static final int WEIGHT_KEBAP = 200;
    private static final int WEIGHT_STEAK = 200;
    private static final int WEIGHT_FISH = 150;
    private static final int WEIGHT_LAMB = 370;
    private static final int WEIGHT_TURKEY = 120;
    private static final int WEIGHT_PIZZA = 50;
    private static final int WEIGHT_BOLOGNESE = 130;
    private static final int WEIGHT_SCHNITZEL = 150;
    private static final int WEIGHT_SCHNITZEL_PORK = 150;
    private static final int WEIGHT_HAMBURGER = 100;
    private static final int WEIGHT_SWABIAN_POCKETS = 100;
    private static final int WEIGHT_HUNTER_SCHNITZEL = 200;
    private static final int WEIGHT_PORK_ROAST = 350;
    private static final int WEIGHT_SAUERBRATEN = 250;
    private static final int WEIGHT_GOULASH = 150;
    private static final int WEIGHT_CURRY_SAUSAGE = 100;
    private static final int WEIGHT_SMOKED_PORK_CHOP = 250;
    private static final int WEIGHT_ROULADE = 200;

    /**
     * <code>true</code> if and only if {@link InputActivity.StoreTask}
     * is executing currently.
     */
    public static boolean isStoreTaskExecuting = false;

    private Date dateSelected;      
    private Meat meatSelected;
    private int weightSelected;
    private boolean isInputHelpPopupOpen;

    private AlertDialog inputHelp;

    /**
     * <code>false</code> if the current input data have been set by selecting a default meal.
     * <code>true</code> if the user edited the sort of meat manually.
     */
    private boolean isMeatModified;

    /**
     * Counts the number of times a meal from the meals spinner is selected.
     * <p />
     * This is needed to filter out the first meal selection which happens due to an (unwanted) system call.
     */
    private int mealSelectionsCount;

    /**
     * <code>false</code> if the input mask contains empty entries.
     */
    private boolean isSubmitDataEnabled;

    /**
     * Stores the data inserted by the user using the {@link InputActivity}.
     * While storing, the menu item that triggered this task, is disabled.
     * On task completion, a {@link Toast} is shown.
     *
     * @author Lukas Gebhard <freerunningapps@gmail.com>
     */
    private class StoreTask extends AsyncTask<Void, Void, Boolean> {
        private MenuItem toDisable;

        public StoreTask(MenuItem toDisable) {
            super();

            this.toDisable = toDisable;
        }

        /**
         * Stores the specified meat dish to the DB.
         *
         * @param params Unused.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            return Model.storeMeatDish(getApplicationContext(), dateSelected, meatSelected, weightSelected);
        }

        /**
         * Disables the menu item that triggered this task.
         * This avoids an overload of the DB layer. The button is re-enabled as soon as the data are stored.
         */
        @Override
        protected void onPreExecute() {
            toDisable.setEnabled(false);
            isStoreTaskExecuting = true;

            super.onPreExecute();
        }

        /**
         * Re-enables the menu item that triggered this task and shows a {@link Toast}.
         */
        @Override
        protected void onPostExecute(Boolean result) {
            Toast toast;

            if (result) {
                toast = Toast.makeText(getApplicationContext(), R.string.toast_store_success,
                        Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(getApplicationContext(), R.string.toast_store_fail,
                        Toast.LENGTH_LONG);
            }

            toast.show();
            toDisable.setEnabled(true);
            isStoreTaskExecuting = false;
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initRadioGroup();
        isSubmitDataEnabled = true;
        isStoreTaskExecuting = false;
        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        // This is only relevant in case the app was launched by clicking the reminder notification.
        PreferencesAccess.storeDate(this, PreferencesAccess.NOTIFICATION_PREFS,
                PreferencesAccess.KEY_DATE_LAST_APP_LAUNCH, Calendar.getInstance().getTime());

        Spinner mealsSpinner = (Spinner) findViewById(R.id.spinner_meals);
        MealsAdapter spinnerAdapter = new MealsAdapter(this,
                getResources().getStringArray(R.array.spinner_meals));

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealsSpinner.setAdapter(spinnerAdapter);
        mealsSpinner.setOnItemSelectedListener(this);

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{
                ((TextView) findViewById(R.id.textView_input_description)),
                ((RadioButton) findViewById(R.id.radioButton_meat_beef)),
                ((RadioButton) findViewById(R.id.radioButton_meat_pork)),
                ((RadioButton) findViewById(R.id.radioButton_meat_poultry)),
                ((RadioButton) findViewById(R.id.radioButton_meat_sheep_goat)),
                ((RadioButton) findViewById(R.id.radioButton_meat_fish)),
                ((TextView) findViewById(R.id.textView_select_meat)),
                ((TextView) findViewById(R.id.textView_select_amount)),
                ((TextView) findViewById(R.id.textView_select_date)),
                ((TextView) findViewById(R.id.textview_weight)),
                ((TextView) findViewById(R.id.textview_date))
        });
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.title_activity_input));

        if (savedInstanceState == null) {
            isInputHelpPopupOpen = false;
            isMeatModified = false;
            isSubmitDataEnabled = true;
            mealSelectionsCount = 0;
            weightSelected = 20 * WeightPickerFragment.ACCURACY; // "Steak" default weight
        } else {
            TextView textViewDate = (TextView) findViewById(R.id.textview_date);
            textViewDate.setText(savedInstanceState.getString(KEY_DATE));

            isInputHelpPopupOpen = savedInstanceState.getBoolean(KEY_INPUT_HELP_OPEN);
            isMeatModified = savedInstanceState.getBoolean(KEY_MEAT_MODIFIED);
            isSubmitDataEnabled = savedInstanceState.getBoolean(KEY_SUBMIT_DATA_ENABLED);
            weightSelected = savedInstanceState.getInt(KEY_WEIGHT);
        }

        TextView textViewWeight = (TextView) findViewById(R.id.textview_weight);
        textViewWeight.setText(Formatter.format(weightSelected,
                getResources().getString(R.string.unitGrammes),
                getResources().getString(R.string.unitKilogrammes),
                Formatter.KILO, 2));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isInputHelpPopupOpen) {
            onInputHelpRequested(null);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (isInputHelpPopupOpen) {
            inputHelp.dismiss();
        }
    }

    /**
     * Adapter for the meals spinner to customize the font.
     */
    public class MealsAdapter extends ArrayAdapter<CharSequence> {
        public MealsAdapter(Context context, CharSequence[] objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);

            ((TextView) view).setTypeface(font);
            return view;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);

            ((TextView) view).setTypeface(font);
            return view;
        }
    }

    private void initRadioGroup() {
        RadioButton meatButtonPork = (RadioButton) findViewById(R.id.radioButton_meat_pork);
        RadioButton meatButtonBeef = (RadioButton) findViewById(R.id.radioButton_meat_beef);
        RadioButton meatButtonPoultry = (RadioButton) findViewById(R.id.radioButton_meat_poultry);
        RadioButton meatButtonSheepGoat = (RadioButton) findViewById(R.id.radioButton_meat_sheep_goat);
        RadioButton meatButtonFish = (RadioButton) findViewById(R.id.radioButton_meat_fish);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int orientation = getResources().getConfiguration().orientation;
        int displayHeight = displayMetrics.heightPixels;
        int iconHeight = orientation == Configuration.ORIENTATION_PORTRAIT ? displayHeight / 20 : displayHeight / 12;
        @SuppressWarnings("SuspiciousNameCombination") int iconWidth = iconHeight;
        //Set maximum Icon Height
        int iconMaxHeight = getResources().getDimensionPixelSize(R.dimen.IconChartIconSize);
        if(iconHeight > iconMaxHeight)
            iconHeight = iconMaxHeight;

        int icPaddingRight = iconHeight / 3;
        BitmapDrawable icDrawPig;
        BitmapDrawable icDrawCow;
        BitmapDrawable icDrawRooster;
        BitmapDrawable icDrawSheep;
        BitmapDrawable icDrawFish;
        Bitmap icPig = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pig);
        Bitmap icCow = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cow);
        Bitmap icRooster = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rooster);
        Bitmap icSheep = BitmapFactory.decodeResource(getResources(), R.drawable.ic_sheep);
        Bitmap icFish = BitmapFactory.decodeResource(getResources(), R.drawable.ic_fish);

        icPig = Bitmap.createScaledBitmap(icPig, iconWidth, iconHeight, false);
        icCow = Bitmap.createScaledBitmap(icCow, iconWidth, iconHeight, false);
        icRooster = Bitmap.createScaledBitmap(icRooster, iconWidth, iconHeight, false);
        icSheep = Bitmap.createScaledBitmap(icSheep, iconWidth, iconHeight, false);
        icFish = Bitmap.createScaledBitmap(icFish, iconWidth, iconHeight, false);

        icDrawPig = new BitmapDrawable(getResources(), icPig);
        icDrawCow = new BitmapDrawable(getResources(), icCow);
        icDrawRooster = new BitmapDrawable(getResources(), icRooster);
        icDrawSheep = new BitmapDrawable(getResources(), icSheep);
        icDrawFish = new BitmapDrawable(getResources(), icFish);

        icDrawPig.setBounds(0, 0, icDrawPig.getIntrinsicWidth(), icDrawPig.getIntrinsicHeight());
        icDrawCow.setBounds(0, 0, icDrawCow.getIntrinsicWidth(), icDrawCow.getIntrinsicHeight());
        icDrawRooster.setBounds(0, 0, icDrawRooster.getIntrinsicWidth(), icDrawRooster.getIntrinsicHeight());
        icDrawSheep.setBounds(0, 0, icDrawSheep.getIntrinsicWidth(), icDrawSheep.getIntrinsicHeight());
        icDrawFish.setBounds(0, 0, icDrawFish.getIntrinsicWidth(), icDrawFish.getIntrinsicHeight());

        meatButtonPork.setCompoundDrawables(icDrawPig, null, null, null);
        meatButtonBeef.setCompoundDrawables(icDrawCow, null, null, null);
        meatButtonPoultry.setCompoundDrawables(icDrawRooster, null, null, null);
        meatButtonSheepGoat.setCompoundDrawables(icDrawSheep, null, null, null);
        meatButtonFish.setCompoundDrawables(icDrawFish, null, null, null);

        meatButtonPork.setCompoundDrawablePadding(icPaddingRight);
        meatButtonBeef.setCompoundDrawablePadding(icPaddingRight);
        meatButtonPoultry.setCompoundDrawablePadding(icPaddingRight);
        meatButtonSheepGoat.setCompoundDrawablePadding(icPaddingRight);
        meatButtonFish.setCompoundDrawablePadding(icPaddingRight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.input, menu);

        return true;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        /*
         * As this activity can be reached both through the overview and the details activity,
         * here the up-navigation must be dynamic. By overriding this method, the up-button always returns the
         * user to the activity where he/she came from.
         */

        Intent intent = getIntent();
        String callerActivity = intent.getStringExtra(ViewConstants.CALLER_ACTIVITY);

        Intent upIntent;

        if (callerActivity == null) { // Intent coming from the reminder notification
            upIntent = new Intent(this, OverviewActivity.class);
        } else {
            switch (callerActivity) {
                case ViewConstants.OVERVIEW_ACTIVITY:
                    upIntent = new Intent(this, OverviewActivity.class);
                    break;
                case ViewConstants.HISTORY_ACTIVITY:
                    upIntent = new Intent(this, HistoryActivity.class);
                    upIntent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.INPUT_ACTIVITY);
                    break;
                default:
                    throw new IllegalStateException("This activity was called by an activity which is not designated to "
                            + "do so");
            }
        }

        return upIntent;
    }

    @SuppressWarnings("UnusedParameters")
    public void showWeightPicker(View view) {
        DialogFragment weightPickerDialog = new WeightPickerFragment();

        weightPickerDialog.show(getSupportFragmentManager(), ViewConstants.WEIGHT_PICKER_FRAGMENT);
    }

    @SuppressWarnings("UnusedParameters")
    public void showDatePicker(View view) {
        DialogFragment datePickerDialog = new DatePickerFragment();
        datePickerDialog.show(getSupportFragmentManager(), ViewConstants.DATE_PICKER_FRAGMENT);
    }

    /**
     * Invoked when the user modified the sort of meat.
     * This changes the spinner's meal to 'other'.
     *
     * @param view The radio button that refers to the selected sort of meat.
     */
    @SuppressWarnings({"unchecked", "UnusedParameters"})
    public void onMeatSelected(View view) {
        Spinner mealsSpinner = (Spinner) findViewById(R.id.spinner_meals);
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mealsSpinner.getAdapter();
        String otherMeal = getResources().getString(R.string.spinner_meals_other);
        TextView textViewWeight = (TextView) findViewById(R.id.textview_weight);

        isMeatModified = true;
        mealsSpinner.setSelection(adapter.getPosition(otherMeal), true);

        // If the input mask is refilled now, this re-enables submitting the data.
        if (!isSubmitDataEnabled && !textViewWeight.getText().equals("")) {
            isSubmitDataEnabled = true;
        }
    }

    /**
     * Invoked when the user modified the weight.
     *
     * @param weightSelected The weight selected.
     */  
    @Override
    public void onWeightSelected(int weightSelected) {
        TextView weightButton = (TextView) findViewById(R.id.textview_weight);
        RadioGroup meats = (RadioGroup) findViewById(R.id.radioGroup_meat);

        this.weightSelected = weightSelected;
        weightButton.setText(Formatter.format(weightSelected, getResources().getString(R.string.unitGrammes),
                getResources().getString(R.string.unitKilogrammes), Formatter.KILO, 2));

        // If the input mask is refilled now, this re-enables submitting the data.
        if (!isSubmitDataEnabled && meats.getCheckedRadioButtonId() >= 0) {
            isSubmitDataEnabled = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
        case R.id.action_accept:
            if (isSubmitDataEnabled) {
                onInputSubmitted(item);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), getSubmitDeniedMessageId(), Toast.LENGTH_LONG);
                toast.show();
            }
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onInputSubmitted(MenuItem item) {
        Intent intent;
        AsyncTask<Void, Void, Boolean> storeTask = new StoreTask(item);                
        TextView textViewDate = (TextView) findViewById(R.id.textview_date);
        RadioGroup meatGroup = (RadioGroup) findViewById(R.id.radioGroup_meat);
        String dateStr = textViewDate.getText().toString();
        RadioButton meat = (RadioButton) findViewById(meatGroup.getCheckedRadioButtonId());
        String meatStr = meat.getText().toString();
        String today = getResources().getString(R.string.today);
        int carbonSaved;
        int waterSaved;
        int feedSaved;

        dateSelected = DateParser.parseDE(dateStr, today);
        meatSelected = Meat.valueOf(getApplicationContext(), meatStr);
        
        carbonSaved = Model.getCarbonImpact(meatSelected, weightSelected);
        waterSaved = Model.getWaterImpact(meatSelected, weightSelected);
        feedSaved = Model.getFeedImpact(meatSelected, weightSelected);
        
        storeTask.execute();
        
        intent = new Intent(this, OverviewActivity.class); // Back to overview
        intent.putExtra(ViewConstants.MEAT_SAVED, weightSelected);
        intent.putExtra(ViewConstants.CARBON_SAVED, carbonSaved);
        intent.putExtra(ViewConstants.WATER_SAVED, waterSaved);
        intent.putExtra(ViewConstants.FEED_SAVED, feedSaved);

        startActivity(intent);
    }

    private int getSubmitDeniedMessageId() {
        TextView textViewWeight = (TextView) findViewById(R.id.textview_weight);
        RadioGroup meats = (RadioGroup) findViewById(R.id.radioGroup_meat);
        int messageId = -1;

        if (textViewWeight.getText().equals("")) {
            if (meats.getCheckedRadioButtonId() < 0) {
                messageId = R.string.toast_missing_amount_meat;
            } else {
                messageId = R.string.toast_missing_amount;
            }
        } else if (meats.getCheckedRadioButtonId() < 0) {
            messageId = R.string.toast_missing_meat;
        }
        return messageId;
    }

    /**
     * Invoked after the item of the meals spinner has been changed.
     * This can happen in two cases:
     * First, the user selected a default meal. In this case, the sort of meat and the weight is adapted automatically.
     * For the 'other' default the values are emptied and the submit functionality is disabled.
     * Second, the user modified the sort of meat and/ or the weight so that the meal switches to 'other'. In this case,
     * this method does nothing.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final String steak = getResources().getString(R.string.spinner_meals_steak);
        final String kebap = getResources().getString(R.string.spinner_meals_kebap);
        final String hotdog = getResources().getString(R.string.spinner_meals_hotdog);
        final String beacon = getResources().getString(R.string.spinner_meals_beacon);
        final String chicken = getResources().getString(R.string.spinner_meals_chicken);
        final String fish = getResources().getString(R.string.spinner_meals_fish);
        final String lamb = getResources().getString(R.string.spinner_meals_lamb);
        final String turkey = getResources().getString(R.string.spinner_meals_turkey);
        final String pizza = getResources().getString(R.string.spinner_meals_pizza);
        final String bolognese = getResources().getString(R.string.spinner_meals_bolognese);
        final String schnitzel = getResources().getString(R.string.spinner_meals_schnitzel);
        final String schnitzel_pork = getResources().getString(R.string.spinner_meals_schnitzel_pork);
        final String hamburger = getResources().getString(R.string.spinner_meals_hamburger);
        final String swabian_pockets = getResources().getString(R.string.spinner_meals_swabian_pockets);
        final String hunter_schnitzel = getResources().getString(R.string.spinner_meals_hunter_schnitzel);
        final String pork_roast = getResources().getString(R.string.spinner_meals_pork_roast);
        final String sauerbraten = getResources().getString(R.string.spinner_meals_sauerbraten);
        final String goulash = getResources().getString(R.string.spinner_meals_goulash);
        final String curry_sausage = getResources().getString(R.string.spinner_meals_curry_sausage);
        final String smoked_pork_chop = getResources().getString(R.string.spinner_meals_smoked_pork_chop);
        final String roulade = getResources().getString(R.string.spinner_meals_roulade);
        final String other = getResources().getString(R.string.spinner_meals_other);

        final RadioButton buttonPork = (RadioButton) findViewById(R.id.radioButton_meat_pork);
        final RadioButton buttonBeef = (RadioButton) findViewById(R.id.radioButton_meat_beef);
        final RadioButton buttonPoultry = (RadioButton) findViewById(R.id.radioButton_meat_poultry);
        final RadioButton buttonSheepGoat = (RadioButton) findViewById(R.id.radioButton_meat_sheep_goat);
        final RadioButton buttonFish = (RadioButton) findViewById(R.id.radioButton_meat_fish);

        String selected = (String) parent.getItemAtPosition(position);
        RadioButton meatToSelect = null;
        TextView textViewWeight = (TextView) findViewById(R.id.textview_weight);
        int weight = - 1;
        boolean clearWeight = false;

        ++mealSelectionsCount;

        if (mealSelectionsCount <= 1) { // System automatically called this method
            // Initially, steak is the default.
            meatToSelect = buttonBeef;
            weight = weightSelected; // might be taken from savedInstanceState.
        } else {
            if (!selected.equals(other)) {
                isMeatModified = false;
            } // else this might be called from the onMeatSelected method

            if (selected.equals(steak)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_STEAK;
            } else if (selected.equals(kebap)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_KEBAP;
            } else if (selected.equals(hotdog)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_HOTDOG;
            } else if (selected.equals(beacon)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_BEACON;
            } else if (selected.equals(chicken)) {
                meatToSelect = buttonPoultry;
                weight = WEIGHT_CHICKEN;
            } else if (selected.equals(fish)) {
                meatToSelect = buttonFish;
                weight = WEIGHT_FISH;
            } else if (selected.equals(lamb)) {
                meatToSelect = buttonSheepGoat;
                weight = WEIGHT_LAMB;
            } else if (selected.equals(turkey)) {
                meatToSelect = buttonPoultry;
                weight = WEIGHT_TURKEY;
            } else if (selected.equals(pizza)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_PIZZA;
            } else if (selected.equals(bolognese)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_BOLOGNESE;
            } else if (selected.equals(schnitzel)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_SCHNITZEL;
            } else if (selected.equals(schnitzel_pork)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_SCHNITZEL_PORK;
            } else if (selected.equals(hamburger)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_HAMBURGER;
            } else if (selected.equals(swabian_pockets)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_SWABIAN_POCKETS;
            } else if (selected.equals(hunter_schnitzel)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_HUNTER_SCHNITZEL;
            } else if (selected.equals(pork_roast)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_PORK_ROAST;
            } else if (selected.equals(sauerbraten)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_SAUERBRATEN;
            } else if (selected.equals(goulash)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_GOULASH;
            } else if (selected.equals(curry_sausage)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_CURRY_SAUSAGE;
            } else if (selected.equals(smoked_pork_chop)) {
                meatToSelect = buttonPork;
                weight = WEIGHT_SMOKED_PORK_CHOP;
            } else if (selected.equals(roulade)) {
                meatToSelect = buttonBeef;
                weight = WEIGHT_ROULADE;
            } else if (selected.equals(other)) {
                clearWeight = onOtherItemSelected();
            } else {
                throw new IllegalStateException("'" + selected + "' was selected as a meal. However, that meal is not "
                        + "registered in the spinner's event handler.");
            }
        }

        if (!isMeatModified) { // A default from the spinner was selected manually
            weightSelected = weight;

            textViewWeight.setText(clearWeight ? "" : Formatter.format(weightSelected,
                    getResources().getString(R.string.unitGrammes),
                    getResources().getString(R.string.unitKilogrammes),
                    Formatter.KILO, 2));

            if (!selected.equals(other)) {
                assert meatToSelect != null;
                meatToSelect.setChecked(true);
            }
        }

    }

    /**
     * Invoked after the spinner's default 'other' has been set.
     *
     * @return <code>true-</code> if the weight in the input mask should be cleared.
     */
    private boolean onOtherItemSelected() {
        boolean clearWeight = false;

        if (!isMeatModified) { // Spinner's default "other" selected
            RadioGroup meats = (RadioGroup) findViewById(R.id.radioGroup_meat);

            meats.clearCheck();
            clearWeight = true;
            isSubmitDataEnabled = false; // The user must enter values before submitting them.
        } // else custom data selected - so nothing to modify.

        return clearWeight;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Unneeded. Refers to the meals spinner.
    }
    
    @SuppressWarnings("UnusedParameters")
    public void onInputHelpRequested(View view) {
        showInputHelp();
    }

    private void showInputHelp() {
        isInputHelpPopupOpen = true;

        inputHelp = Popup.show(getResources().getString(R.string.popup_input_help_title),
                getResources().getString(R.string.popup_input_help_content), this,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isInputHelpPopupOpen = false;
                    }
                });
    }

    public int getWeightSelected() {
        return weightSelected;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        TextView textViewDate = (TextView) findViewById(R.id.textview_date);

        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_INPUT_HELP_OPEN, isInputHelpPopupOpen);
        outState.putBoolean(KEY_MEAT_MODIFIED, isMeatModified);
        outState.putBoolean(KEY_SUBMIT_DATA_ENABLED, isSubmitDataEnabled);
        outState.putString(KEY_DATE, textViewDate.getText().toString());
        outState.putInt(KEY_WEIGHT, weightSelected);
    }
}
