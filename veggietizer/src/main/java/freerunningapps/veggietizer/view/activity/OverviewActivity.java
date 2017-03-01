package freerunningapps.veggietizer.view.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.BootReceiver;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.controller.NotificationService;
import freerunningapps.veggietizer.model.Achievement;
import freerunningapps.veggietizer.model.AchievementSet;
import freerunningapps.veggietizer.model.Model;
import freerunningapps.veggietizer.model.enums.SavingsType;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.model.util.PreferencesAccess;
import freerunningapps.veggietizer.view.Popup;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;
import freerunningapps.veggietizer.view.chart.IconChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class OverviewActivity extends ActionBarActivity {
    private static final String KEY_ACHIEVEMENT_OPEN = "freerunningapps.veggietizer.overview.achievement_open";
    private static final String KEY_START_GUIDE_OPEN = "freerunningapps.veggietizer.overview.start_guide_open";
    private static final String KEY_UPDATE_NOTIFIER_OPEN = "freerunningapps.veggietizer.overview.update_notifier_open";
    private static final String KEY_INSTANT_FEEDBACK_OPEN
            = "freerunningapps.veggietizer.overview.instant_feedback_open";
    private static final String KEY_INSTANT_FEEDBACK_SHOWN
            = "freerunningapps.veggietizer.overview.instant_feedback_shown";
    private static final String KEY_ALL_ACHIEVEMENTS
            = "freerunningapps.veggietizer.overview.all_achievements";
    private static final String KEY_ALL_ACHIEVEMENTS_SHOWN
            = "freerunningapps.veggietizer.overview.all_achievements_shown";
    private static final String KEY_UPDATE_NOTIFIER_SHOWN
            = "freerunningapps.veggietizer.overview.update_notifier_shown";
    private static final String KEY_START_GUIDE_SHOWN
            = "freerunningapps.veggietizer.overview.start_guide_shown";

    private boolean isAchievementOpen;
    private boolean isStartGuideOpen;
    private boolean isUpdateNotifierOpen;
    private boolean isInstantFeedbackOpen;

    /**
     * <code>true</code> as soon as the user closed the instant feedback popup.
     */
    private boolean wasInstantFeedbackShown;

    /**
     * <code>true</code> as soon as the user closed the last newly unlocked achievement.
     */
    private boolean wereAllAchievementsShown;

    /**
     * <code>true</code> as soon as the user closed the start guide.
     */
    private boolean wasStartGuideShown;

    /**
     * <code>true</code> as soon as the user closed the update notifier.
     */
    private boolean wasUpdateNotifierShown;

    /**
     * The achievement currently shown to the user.
     */
    private AlertDialog currentAchievementOpen;

    /**
     * The chain of just unlocked achievements that have not been shown to the user yet.
     */
    private List<Achievement> achievementsToShow;

    private AlertDialog startGuide;
    private AlertDialog updateNotifier;
    private AlertDialog instantFeedback;
    
    /**
     * <code>true</code> as soon as this activity is fully initialised.
     */
    private boolean isInitialised;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        PreferencesAccess.storeDate(this, PreferencesAccess.NOTIFICATION_PREFS,
                PreferencesAccess.KEY_DATE_LAST_APP_LAUNCH, Calendar.getInstance().getTime());
        cancelNotificationAlarm();

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]
                {((TextView) findViewById(R.id.textView_overview_heading))});
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.app_name));

        if (savedInstanceState == null) {
            isAchievementOpen = false;
            isStartGuideOpen = false;
            isUpdateNotifierOpen = false;
            isInstantFeedbackOpen = false;
            wasInstantFeedbackShown = false;
            wereAllAchievementsShown = false;
            wasUpdateNotifierShown = false;
            wasStartGuideShown = false;
            achievementsToShow = null;
        } else {
            Parcelable[] achievements = savedInstanceState.getParcelableArray(KEY_ALL_ACHIEVEMENTS);
            Achievement[] achievementsConverted = achievements == null ? null
                    : Arrays.copyOf(achievements, achievements.length, Achievement[].class);

            isAchievementOpen = savedInstanceState.getBoolean(KEY_ACHIEVEMENT_OPEN);
            isStartGuideOpen = savedInstanceState.getBoolean(KEY_START_GUIDE_OPEN);
            isUpdateNotifierOpen = savedInstanceState.getBoolean(KEY_UPDATE_NOTIFIER_OPEN);
            isInstantFeedbackOpen = savedInstanceState.getBoolean(KEY_INSTANT_FEEDBACK_OPEN);
            wasInstantFeedbackShown = savedInstanceState.getBoolean(KEY_INSTANT_FEEDBACK_SHOWN);
            wereAllAchievementsShown = savedInstanceState.getBoolean(KEY_ALL_ACHIEVEMENTS_SHOWN);
            wasStartGuideShown = savedInstanceState.getBoolean(KEY_START_GUIDE_SHOWN);
            wasUpdateNotifierShown = savedInstanceState.getBoolean(KEY_UPDATE_NOTIFIER_SHOWN);
            achievementsToShow = achievements == null ? null
                : new ArrayList<>(Arrays.asList(achievementsConverted));

            // Restores the context for each achievement.
            if (achievementsToShow != null) {
                for (Achievement a : achievementsToShow) {
                    a.setContext(getApplicationContext());
                }
            }
        }

        Intent intent = getIntent();
        float meatSaved = (float) getIntent().getIntExtra(ViewConstants.MEAT_SAVED, 0);
        float carbonSaved = (float) intent.getIntExtra(ViewConstants.CARBON_SAVED, 0) / 1000.0F;
        float waterSaved = (float) intent.getIntExtra(ViewConstants.WATER_SAVED, 0) / 1.0F;
        float feedSaved = (float) intent.getIntExtra(ViewConstants.FEED_SAVED, 0) / 1000.0F;

        if (meatSaved > 0 && !wasInstantFeedbackShown) {
            showInstantFeedback(meatSaved, carbonSaved, waterSaved, feedSaved);
        }

        isInitialised = false;
    }

    /**
     * Cancels any notification alarm scheduled by the {@link freerunningapps.veggietizer.controller.BootReceiver}.
     * <p />
     * Once the user has opened the Veggietizer anyway, no reminder notification has to be triggered for today anymore.
     */
    private void cancelNotificationAlarm() {
        Intent notifyIntent = new Intent(getApplicationContext(), NotificationService.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                BootReceiver.ALARM_REQUEST_ID, notifyIntent, 0);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * Checks if Google Play Services is installed, activated and up to date.
     * If not, the user is asked to install, activate and update it, respectively.
     */
    @Override
    protected void onResume() {
        float meatSaved = (float) getIntent().getIntExtra(ViewConstants.MEAT_SAVED, 0);

        if (isInitialised) {
            if (isInstantFeedbackOpen && !wasInstantFeedbackShown) {
                Intent intent = getIntent();
                float carbonSaved = (float) intent.getIntExtra(ViewConstants.CARBON_SAVED, 0) / 1000.0F;
                float waterSaved = (float) intent.getIntExtra(ViewConstants.WATER_SAVED, 0) / 1.0F;
                float feedSaved = (float) intent.getIntExtra(ViewConstants.FEED_SAVED, 0) / 1000.0F;

                showInstantFeedback(meatSaved, carbonSaved, waterSaved, feedSaved);
            }
        } else {
            if (meatSaved == 0) {
                initAchievements();
            }

            waitForStoreTask();
            isInitialised = true;
        }

        if (isStartGuideOpen && !wasStartGuideShown) {
            showStartGuide();
        }
        if (isUpdateNotifierOpen && !wasUpdateNotifierShown) {
            showUpdateNotifier();
        }
        if (isAchievementOpen && !wereAllAchievementsShown) {
            showAchievement();
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isStartGuideOpen) {
            startGuide.dismiss();
        }
        if (isUpdateNotifierOpen) {
            updateNotifier.dismiss();
        }
        if (isInstantFeedbackOpen) {
            instantFeedback.dismiss();
        }
        if (isAchievementOpen) {
            currentAchievementOpen.dismiss();
        }
    }

    /**
     * Shows a pop-up on app startup, in the following cases:
     * At the first app launch, a start guide is shown.
     * After a major update, this shows a notifier informing about the new features.
     * A major update is considered as a change in the first digit in the app's version name,
     * e.g. from 1.2 to 2.0.
     *
     * @param totalMeat The current total of meat abstained from.
     */
    private void showStartupPopup(Integer totalMeat) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currentVersion;
        String lastVersion;
        char currentMajorVersion;
        char lastMajorVersion;

        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new Error(e);
        }
        currentMajorVersion = currentVersion.charAt(0);

        lastVersion = prefs.getString(ViewConstants.VERSION_NAME_BEFORE_UPDATE, "0.0");
        lastMajorVersion = lastVersion.charAt(0);

        if (currentMajorVersion > lastMajorVersion) {
            prefs.edit().putString(ViewConstants.VERSION_NAME_BEFORE_UPDATE, currentVersion).apply();

            if (lastMajorVersion == '0') { // App newly installed or updated to version 1.3
                if (totalMeat <= 0 && !wasStartGuideShown) {
                    showStartGuide();
                }
            } else if (!wasUpdateNotifierShown) { // Major update
                showUpdateNotifier();
            }
        }
    }

    private void showStartGuide() {
        isStartGuideOpen = true;
        startGuide = Popup.show(getResources().getString(R.string.startguide_heading),
                getResources().getString(R.string.startguide_description), this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isStartGuideOpen = false;
                        wasStartGuideShown = true;
                    }
                });
    }

    private void showUpdateNotifier() {
        isUpdateNotifierOpen = true;
        updateNotifier = Popup.show(getResources().getString(R.string.update_notifier_heading),
                getResources().getString(R.string.update_notifier_description), this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isUpdateNotifierOpen = false;
                        wasUpdateNotifierShown = true;
                    }
                });
    }

    private void showInstantFeedback(float meatSaved, float carbonSaved, float waterSaved, float feedSaved) {
        isInstantFeedbackOpen = true;
        instantFeedback = Popup.showFeedbackOnInsert(meatSaved, carbonSaved,
                waterSaved, feedSaved, this,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        initAchievements();
                        isInstantFeedbackOpen = false;
                        wasInstantFeedbackShown = true;
                    }
                });
    }

    /**
     * Shows the first achievement from the list of newly unlocked achievements the user has not seen yet.
     * <p />
     * When the user closes the achievement, the next one from the list is shown (if available).
     */
    private void showAchievement() {
        isAchievementOpen = true;

        currentAchievementOpen = Popup.showNewAchievement(this, achievementsToShow.get(0),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isAchievementOpen = false;
                        achievementsToShow.remove(0);

                        if (achievementsToShow.isEmpty()) {
                            wereAllAchievementsShown = true;
                        } else {
                            showAchievement();
                        }
                    }
                });
    }

    private void waitForStoreTask() {
        if (InputActivity.isStoreTaskExecuting) {
            Handler handler = new Handler();

            // Waits 300 ms before trying again
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waitForStoreTask();
                }
            }, 300);
        } else {
            updateData();
        }
    }

    /**
	 * Initializes Achievements and checks if new achievements are unlocked
	 */
	private void initAchievements() {
		AchievementSet achievementSet = AchievementSet.getInstance(getApplicationContext());

        if (achievementsToShow == null) { // Acitivty has just been (re-)created
            achievementsToShow = achievementSet.checkAchievementsAfterInsert();
        }

        if (achievementsToShow.isEmpty()) {
            wereAllAchievementsShown = true;
        } else {
            showAchievement();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.overview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
        case R.id.action_add:
            intent = new Intent(this, InputActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.OVERVIEW_ACTIVITY);
            startActivity(intent);
            break;
        case R.id.action_history:
            intent = new Intent(this, HistoryActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.OVERVIEW_ACTIVITY);
            startActivity(intent);
            break;
        case R.id.action_achievements:
            intent = new Intent(this, AchievementsActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.OVERVIEW_ACTIVITY);
            startActivity(intent);
            break;
        case R.id.action_about:
            intent = new Intent(this, AboutActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.OVERVIEW_ACTIVITY);
            startActivity(intent);
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateData() {
        new LoadDataFromModel().execute();
    }

    private class LoadDataFromModel extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            Context context = getApplicationContext();
            Resources resources = context.getResources();
            String grammes = resources.getString(R.string.unitGrammes);
            String kilogrammes = resources.getString(R.string.unitKilogrammes);
            String litres = resources.getString(R.string.unitLitres);
            String millilitres = resources.getString(R.string.unitMillilitres);
            int numberOfDecimals = 2;

            //Meat
            int meat = Model.getMeatSavedTotal(context);
            IconChart chart_beef = (IconChart) findViewById(R.id.OverviewAmount);
            chart_beef.setAmount(meat, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

            //CO2
            int co2 = Model.getTotalCarbonImpact(context);
            IconChart chart_co2 = (IconChart) findViewById(R.id.OverviewCO2);
            chart_co2.setAmount(co2 / Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

            //Water
            int water = Model.getTotalWaterImpact(context);
            IconChart chart_water = (IconChart) findViewById(R.id.OverviewWater);
            chart_water.setAmount(water, millilitres, litres, Formatter.KILO, 0);

            //Feed
            int feed = Model.getTotalFeedImpact(context);
            IconChart chart_feed = (IconChart) findViewById(R.id.OverviewFeed);
            chart_feed.setAmount(feed / Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

            return meat;
        }

        @Override
        protected void onPostExecute(Integer totalMeat) {
            showStartupPopup(totalMeat);

            if (totalMeat > 0) {
                ((IconChart) findViewById(R.id.OverviewAmount)).refreshView();
                ((IconChart) findViewById(R.id.OverviewCO2)).refreshView();
                ((IconChart) findViewById(R.id.OverviewWater)).refreshView();
                ((IconChart) findViewById(R.id.OverviewFeed)).refreshView();

                ((IconChart) findViewById(R.id.OverviewAmount)).animateIcons();
                ((IconChart) findViewById(R.id.OverviewCO2)).animateIcons();
                ((IconChart) findViewById(R.id.OverviewWater)).animateIcons();
                ((IconChart) findViewById(R.id.OverviewFeed)).animateIcons();
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void showCompareCOtwo(View view) {
        Intent intent = new Intent(this, CompareActivity.class);
        intent.putExtra(ViewConstants.SAVINGS_TYPE, SavingsType.CO2.toString());
        startActivity(intent);
    }

    @SuppressWarnings("UnusedParameters")
    public void showCompareWater(View view) {
        Intent intent = new Intent(this, CompareActivity.class);
        intent.putExtra(ViewConstants.SAVINGS_TYPE, SavingsType.WATER.toString());
        startActivity(intent);
    }

    @SuppressWarnings("UnusedParameters")
    public void showCompareFeed(View view) {
        Intent intent = new Intent(this, CompareActivity.class);
        intent.putExtra(ViewConstants.SAVINGS_TYPE, SavingsType.FEED.toString());
        startActivity(intent);
    }

    @SuppressWarnings("UnusedParameters")
    public void showAnimalChart(View view) {
        Intent intent = new Intent(this, AnimalChartActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Parcelable[] achievementsConverted = null;

        if (achievementsToShow != null) {
            Object[] achievements = achievementsToShow.toArray();
            achievementsConverted = Arrays.copyOf(achievements, achievements.length, Parcelable[].class);
        }

        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_ACHIEVEMENT_OPEN, isAchievementOpen);
        outState.putBoolean(KEY_START_GUIDE_OPEN, isStartGuideOpen);
        outState.putBoolean(KEY_UPDATE_NOTIFIER_OPEN, isUpdateNotifierOpen);
        outState.putBoolean(KEY_INSTANT_FEEDBACK_OPEN, isInstantFeedbackOpen);
        outState.putBoolean(KEY_INSTANT_FEEDBACK_SHOWN, wasInstantFeedbackShown);
        outState.putBoolean(KEY_ALL_ACHIEVEMENTS_SHOWN, wereAllAchievementsShown);
        outState.putBoolean(KEY_START_GUIDE_SHOWN, wasStartGuideShown);
        outState.putBoolean(KEY_UPDATE_NOTIFIER_SHOWN, wasUpdateNotifierShown);
        outState.putParcelableArray(KEY_ALL_ACHIEVEMENTS, achievementsConverted);
    }
}
