package freerunningapps.veggietizer.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.controller.adapter.AchievementAdapter;
import freerunningapps.veggietizer.view.Popup;
import freerunningapps.veggietizer.model.AchievementSet;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class AchievementsActivity extends ActionBarActivity {
    private static final String ACHIEVEMENT_OPEN
            = "freerunningapps.veggietizer.achievement.achievement_open";
    private static final String REQUIREMENTS_OPEN
            = "freerunningapps.veggietizer.achievement.requirements_open";
    private static final String SELECTED_POSITION
            = "freerunningapps.veggietizer.achievement.selected_position";
	private AchievementSet allAchievements;
    private boolean isAchievementOpen;
    private boolean isRequirementsOpen;
    private int selectedPosition;
    private AlertDialog achievementOpened;
    private AlertDialog requirementsOpened;
    private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            if (allAchievements.isUnlocked(position)) {
                showAchievement(position);
            } else {
                showRequirements(position);
            }
        }
    };

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.title_activity_achievements));

        if (savedInstanceState == null) {
            isAchievementOpen = false;
            isRequirementsOpen = false;
            selectedPosition = - 1;
        } else {
            isAchievementOpen = savedInstanceState.getBoolean(ACHIEVEMENT_OPEN);
            isRequirementsOpen = savedInstanceState.getBoolean(REQUIREMENTS_OPEN);
            selectedPosition = savedInstanceState.getInt(SELECTED_POSITION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initAchievementList();

        if (isAchievementOpen) {
            showAchievement(selectedPosition);
        }
        if (isRequirementsOpen) {
            showRequirements(selectedPosition);
        }
    }

    private void initAchievementList() {
		allAchievements = AchievementSet.getInstance(getApplicationContext());
        AchievementAdapter adapter = new AchievementAdapter(this,
                allAchievements.getAchievements());
		ListView listView = (ListView) findViewById(R.id.ListView_achievements);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		listView.setOnItemClickListener(mMessageClickedHandler);
	}

    private void showRequirements(int position) {
        isRequirementsOpen = true;
        selectedPosition = position;
        requirementsOpened = Popup.show(getResources().getString(R.string.achievement_requirements_heading_locked),
                allAchievements.getAchievement(position).getRequirements(), this,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isRequirementsOpen = false;
                    }
                });
    }

    private void showAchievement(int position) {
        isAchievementOpen = true;
        selectedPosition = position;
        achievementOpened = Popup.showAchievement(allAchievements.getAchievement(position), this,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isAchievementOpen = false;
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isRequirementsOpen) {
            requirementsOpened.dismiss();
        }
        if (isAchievementOpen) {
            achievementOpened.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.achievements, menu);
        return true;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        /*
         * As this activity can be reached through several activities,
         * here the up-navigation must be dynamic. By overriding this method, the up-button always returns the
         * user to the activity where he/she came from.
         */
        Intent intent = getIntent();
        String callerActivity = intent.getStringExtra(ViewConstants.CALLER_ACTIVITY);
        String savingsType;
        Intent upIntent;

        switch (callerActivity) {
        case ViewConstants.OVERVIEW_ACTIVITY:
            upIntent = new Intent(this, OverviewActivity.class);
            break;
        case ViewConstants.COMPARE_ACTIVITY:
            savingsType = intent.getStringExtra(ViewConstants.SAVINGS_TYPE);
            upIntent = new Intent(this, CompareActivity.class);
            upIntent.putExtra(ViewConstants.SAVINGS_TYPE, savingsType);
            break;
        case ViewConstants.BARCHART_ACTIVITY:
            savingsType = intent.getStringExtra(ViewConstants.SAVINGS_TYPE);
            upIntent = new Intent(this, BarChartActivity.class);
            upIntent.putExtra(ViewConstants.SAVINGS_TYPE, savingsType);
            break;
        case ViewConstants.ANIMALCHART_ACTIVITY:
            upIntent = new Intent(this, AnimalChartActivity.class);
            break;
        default:
            throw new IllegalStateException("This activity was called by an activity which is not designated to "
                    + "do so");
        }

        return upIntent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(ACHIEVEMENT_OPEN, isAchievementOpen);
        outState.putBoolean(REQUIREMENTS_OPEN, isRequirementsOpen);
        outState.putInt(SELECTED_POSITION, selectedPosition);
    }
}
