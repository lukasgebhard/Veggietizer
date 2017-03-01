package freerunningapps.veggietizer.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.view.Popup;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;
import freerunningapps.veggietizer.view.chart.IconChart;
import freerunningapps.veggietizer.model.enums.Category;
import freerunningapps.veggietizer.model.enums.Meat;
import freerunningapps.veggietizer.model.Model;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class AnimalChartActivity extends ActionBarActivity {
    private static final String KEY_DETAILS_INFO_POPUP_OPEN
            = "freerunningapps.veggietizer.activity.details_info_popup_open";

    private boolean isDetailsInfoPopupOpen;
    private AlertDialog detailsInfoPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animalchart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        TextView heading = ((TextView) findViewById(R.id.textView_animal_chart_heading));
        TextView subHeading = ((TextView) findViewById(R.id.textView_animalchart_savingsAmount));

        //Set heading text
        heading.setText(Category.toString(this, Category.MEAT));
        subHeading.setText(Utility.getSavingsAmount(getApplicationContext(), 2, 0, Category.MEAT));

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{heading, subHeading});
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.title_activity_animalchart));

        updateData();

        isDetailsInfoPopupOpen = savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_DETAILS_INFO_POPUP_OPEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isDetailsInfoPopupOpen) {
            detailsInfoPopup = showDetailsInfoPopup();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isDetailsInfoPopupOpen) {
            detailsInfoPopup.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.animalchart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
        case R.id.action_history:
            intent = new Intent(this, HistoryActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.ANIMALCHART_ACTIVITY);
            startActivity(intent);
            break;
        case R.id.action_achievements:
            intent = new Intent(this, AchievementsActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.ANIMALCHART_ACTIVITY);
            startActivity(intent);
            break;
        case R.id.action_about:
            intent = new Intent(this, AboutActivity.class);
            intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.ANIMALCHART_ACTIVITY);
            startActivity(intent);
            break;
        case R.id.action_info:
            detailsInfoPopup = showDetailsInfoPopup();
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog showDetailsInfoPopup() {
        isDetailsInfoPopupOpen = true;

        return Popup.showDetailsInfo(this, Category.MEAT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDetailsInfoPopupOpen = false;
            }
        });
    }

    private void updateData() {
        new LoadDataFromModel().execute();
    }

    private class LoadDataFromModel extends AsyncTask<Void, Void, Void> {
        @Override
		protected Void doInBackground(Void... params) {
            Context appContext = getApplicationContext();
            Resources resources = appContext.getResources();
            String grammes = resources.getString(R.string.unitGrammes);
            String kilogrammes = resources.getString(R.string.unitKilogrammes);
            int numberOfDecimals = 2;

        	//Beef
        	int beef = Model.getMeatSaved(getApplicationContext(), Meat.BEEF);
        	IconChart chart_beef = (IconChart) findViewById(R.id.DetailsBeef);
        	chart_beef.setAmount(beef, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

        	//Pork
        	int pork = Model.getMeatSaved(getApplicationContext(), Meat.PORK);
        	IconChart chart_pork = (IconChart) findViewById(R.id.DetailsPork);
        	chart_pork.setAmount(pork, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

        	//Poultry
        	int poultry = Model.getMeatSaved(getApplicationContext(), Meat.POULTRY);
        	IconChart chart_poultry = (IconChart) findViewById(R.id.DetailsPoultry);
        	chart_poultry.setAmount(poultry, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

        	//Sheep / Goat
        	int sheep = Model.getMeatSaved(getApplicationContext(), Meat.SHEEP_GOAT);
        	IconChart chart_sheep = (IconChart) findViewById(R.id.DetailsSheepGoat);
        	chart_sheep.setAmount(sheep, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

        	//Fish
        	int fish = Model.getMeatSaved(getApplicationContext(), Meat.FISH);
        	IconChart chart_fish = (IconChart) findViewById(R.id.DetailsFish);
        	chart_fish.setAmount(fish, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

			return null;
		}

        @Override
        protected void onPostExecute(Void params) {
            IconChart iconChartBeef = (IconChart) findViewById(R.id.DetailsBeef);
            IconChart iconChartPork = (IconChart) findViewById(R.id.DetailsPork);
            IconChart iconChartPoultry = (IconChart) findViewById(R.id.DetailsPoultry);
            IconChart iconChartSheepGoat = (IconChart) findViewById(R.id.DetailsSheepGoat);
            IconChart iconChartFish = (IconChart) findViewById(R.id.DetailsFish);

            invalidateIconCharts();

            iconChartBeef.animateIcons();
            iconChartPork.animateIcons();
        	iconChartPoultry.animateIcons();
        	iconChartSheepGoat.animateIcons();
            iconChartFish.animateIcons();
        }

        /**
         * Manually invalidates the background blocks since Android's invalidate() does not work for some reason.
        */
        private void invalidateIconCharts() {
            IconChart iconChartBeef = (IconChart) findViewById(R.id.DetailsBeef);
            IconChart iconChartPork = (IconChart) findViewById(R.id.DetailsPork);
            IconChart iconChartPoultry = (IconChart) findViewById(R.id.DetailsPoultry);
            IconChart iconChartSheepGoat = (IconChart) findViewById(R.id.DetailsSheepGoat);
            IconChart iconChartFish = (IconChart) findViewById(R.id.DetailsFish);

            int paddingTop = (int) getResources().getDimension(R.dimen.block_vertical_padding);
            int paddingBottom = (int) getResources().getDimension(R.dimen.block_vertical_padding_large);

            iconChartBeef.setBackgroundResource(0);
            iconChartPork.setBackgroundResource(0);
            iconChartPoultry.setBackgroundResource(0);
            iconChartSheepGoat.setBackgroundResource(0);
            iconChartFish.setBackgroundResource(0);

            iconChartBeef.setBackgroundResource(R.drawable.iconchart_inset_not_pressed);
            iconChartPork.setBackgroundResource(R.drawable.iconchart_inset_not_pressed);
            iconChartPoultry.setBackgroundResource(R.drawable.iconchart_inset_not_pressed);
            iconChartSheepGoat.setBackgroundResource(R.drawable.iconchart_inset_not_pressed);
            iconChartFish.setBackgroundResource(R.drawable.iconchart_inset_not_pressed);

            iconChartBeef.setPadding(0, paddingTop, 0, paddingBottom);
            iconChartPork.setPadding(0, paddingTop, 0, paddingBottom);
            iconChartPoultry.setPadding(0, paddingTop, 0, paddingBottom);
            iconChartSheepGoat.setPadding(0, paddingTop, 0, paddingBottom);
            iconChartFish.setPadding(0, paddingTop, 0, paddingBottom);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_DETAILS_INFO_POPUP_OPEN, isDetailsInfoPopupOpen);
    }
}
