package freerunningapps.veggietizer.view.activity;


import android.content.DialogInterface;
import freerunningapps.veggietizer.R;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.enums.Category;
import freerunningapps.veggietizer.model.enums.Meat;
import freerunningapps.veggietizer.model.enums.SavingsType;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.view.Popup;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;
import freerunningapps.veggietizer.view.chart.BarChart;
import freerunningapps.veggietizer.model.*;

public class BarChartActivity extends ActionBarActivity {
    private static final String KEY_DETAILS_INFO_POPUP_OPEN
            = "freerunningapps.veggietizer.activity.details_info_popup_open";

	private SavingsType savingsType;

    private boolean isDetailsInfoPopupOpen;
    private DialogInterface detailsInfoPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barchart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        TextView heading = ((TextView) findViewById(R.id.textView_barchart_heading));
        TextView subHeading = ((TextView) findViewById(R.id.textView_barchart_savingsAmount));
        Intent intent = getIntent();
        String savingsTypeStr = intent.getStringExtra(ViewConstants.SAVINGS_TYPE);

        savingsType = SavingsType.valueOf(savingsTypeStr);

        //Set heading text
        heading.setText(SavingsType.toString(getApplicationContext(), savingsType));
        subHeading.setText(Utility.getSavingsAmount(getApplicationContext(), 2, 0, Category.valueOf(savingsType)));

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[] {heading, subHeading});
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.title_activity_barchart));

        //There are CO2 data for fish
        if(savingsType == SavingsType.CO2)
            findViewById(R.id.BarChartFish).setVisibility(View.VISIBLE);

        updateData();

        isDetailsInfoPopupOpen = savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_DETAILS_INFO_POPUP_OPEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isDetailsInfoPopupOpen) {
            showDetailsInfoPopup();
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
        getMenuInflater().inflate(R.menu.barchart, menu);
        return true;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        /*
         * The bar chart activity is accessed through the compare activity. Therefore, when using up navigation, it
         * should return the user to the compare activity, showing the appropriate type of savings.
         */

        Intent intent = getIntent();
        String savingsType = intent.getStringExtra(ViewConstants.SAVINGS_TYPE);
        Intent upIntent = new Intent(this, CompareActivity.class);

        upIntent.putExtra(ViewConstants.SAVINGS_TYPE, savingsType);

        return upIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
        case R.id.action_info:
            showDetailsInfoPopup();
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

	private void showDetailsInfoPopup() {
        isDetailsInfoPopupOpen = true;

		detailsInfoPopup = Popup.showDetailsInfo(this, Category.valueOf(savingsType),
                new DialogInterface.OnClickListener() {
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
        	float total, beef, pork, poultry, sheep_goat, fish = 0;
        	Context appContext = getApplicationContext();
			Resources resources = appContext.getResources();
			String grammes = resources.getString(R.string.unitGrammes);
			String kilogrammes = resources.getString(R.string.unitKilogrammes);
			String litres = resources.getString(R.string.unitLitres);
			String millilitres = resources.getString(R.string.unitMillilitres);
            int numberOfDecimals = 2;
            int numberOfDecimalsWater = 0;

        	//Load Data
        	switch (savingsType) {
	        	case CO2:
	                total = Model.getTotalCarbonImpact(appContext);
	                beef = Model.getCarbonImpact(appContext, Meat.BEEF);
	                pork = Model.getCarbonImpact(appContext, Meat.PORK);
	                poultry = Model.getCarbonImpact(appContext, Meat.POULTRY);
	                sheep_goat = Model.getCarbonImpact(appContext, Meat.SHEEP_GOAT);
                    fish = Model.getCarbonImpact(appContext, Meat.FISH);
	        	    break;
	        	case WATER:
	        	    total = Model.getTotalWaterImpact(appContext);
	                beef = Model.getWaterImpact(appContext, Meat.BEEF);
	                pork = Model.getWaterImpact(appContext, Meat.PORK);
	                poultry = Model.getWaterImpact(appContext, Meat.POULTRY);
	                sheep_goat = Model.getWaterImpact(appContext, Meat.SHEEP_GOAT);
	        	    break;
	        	case FEED:
	        	    total = Model.getTotalFeedImpact(getApplicationContext());
	                beef = Model.getFeedImpact(getApplicationContext(), Meat.BEEF);
	                pork = Model.getFeedImpact(getApplicationContext(), Meat.PORK);
	                poultry = Model.getFeedImpact(getApplicationContext(), Meat.POULTRY);
	                sheep_goat = Model.getFeedImpact(getApplicationContext(), Meat.SHEEP_GOAT);
	        	    break;
	        	default:
	        	    throw new IllegalStateException("Unsupported chart type " + savingsType);
        	}

        	if (savingsType == SavingsType.WATER) {
        		//Beef
	        	BarChart chart_beef = (BarChart) findViewById(R.id.BarChartBeef);
				chart_beef.setPercentage(beef / total * 100);
				chart_beef.setTitleValue(beef, millilitres, litres, Formatter.KILO, numberOfDecimalsWater);

	        	//Pork
				BarChart chart_pork = (BarChart) findViewById(R.id.BarChartPork);
				chart_pork.setPercentage(pork / total * 100);
				chart_pork.setTitleValue(pork, millilitres, litres, Formatter.KILO, numberOfDecimalsWater);

	        	//Poultry
	        	BarChart chart_poultry = (BarChart) findViewById(R.id.BarChartPoultry);
	        	chart_poultry.setPercentage(poultry / total * 100);
	        	chart_poultry.setTitleValue(poultry, millilitres, litres, Formatter.KILO, numberOfDecimalsWater);

	        	//Sheep Goat
	        	BarChart chart_sheep_goat = (BarChart) findViewById(R.id.BarChartSheepGoat);
	        	chart_sheep_goat.setPercentage(sheep_goat / total * 100);
	        	chart_sheep_goat.setTitleValue(sheep_goat, millilitres, litres, Formatter.KILO, numberOfDecimalsWater);
        	}
        	else {
	        	//Beef
	        	BarChart chart_beef = (BarChart) findViewById(R.id.BarChartBeef);
				chart_beef.setPercentage(beef / total * 100);
				chart_beef.setTitleValue(beef / Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

	        	//Pork
				BarChart chart_pork = (BarChart) findViewById(R.id.BarChartPork);
				chart_pork.setPercentage(pork / total * 100);
				chart_pork.setTitleValue(pork / Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

	        	//Poultry
	        	BarChart chart_poultry = (BarChart) findViewById(R.id.BarChartPoultry);
	        	chart_poultry.setPercentage(poultry / total * 100);
	        	chart_poultry.setTitleValue(poultry / Formatter.KILO, grammes, kilogrammes, Formatter.KILO,
                        numberOfDecimals);

	        	//Sheep Goat
	        	BarChart chart_sheep_goat = (BarChart) findViewById(R.id.BarChartSheepGoat);
	        	chart_sheep_goat.setPercentage(sheep_goat / total * 100);
	        	chart_sheep_goat.setTitleValue(sheep_goat / Formatter.KILO, grammes, kilogrammes, Formatter.KILO,
                        numberOfDecimals);

                if(savingsType == SavingsType.CO2) {
                    //Fish
                    BarChart chart_fish = (BarChart) findViewById(R.id.BarChartFish);
                    chart_fish.setPercentage(fish / total * 100);
                    chart_fish.setTitleValue(fish / Formatter.KILO, grammes, kilogrammes, Formatter.KILO,
                            numberOfDecimals);
                }
        	}

			return null;
		}

        @Override
        protected void onPostExecute(Void params) {
        	((BarChart) findViewById(R.id.BarChartBeef)).animateBar();
        	((BarChart) findViewById(R.id.BarChartPork)).animateBar();
        	((BarChart) findViewById(R.id.BarChartPoultry)).animateBar();
        	((BarChart) findViewById(R.id.BarChartSheepGoat)).animateBar();
            ((BarChart) findViewById(R.id.BarChartFish)).animateBar();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_DETAILS_INFO_POPUP_OPEN, isDetailsInfoPopupOpen);
    }
}
