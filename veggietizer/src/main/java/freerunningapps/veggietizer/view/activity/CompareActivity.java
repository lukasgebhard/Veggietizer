package freerunningapps.veggietizer.view.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.enums.Category;
import freerunningapps.veggietizer.model.enums.SavingsType;
import freerunningapps.veggietizer.model.enums.VeggieFood;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.view.Popup;
import freerunningapps.veggietizer.model.*;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class CompareActivity extends ActionBarActivity {
    private static final String KEY_DETAILS_INFO_POPUP_OPEN
            = "freerunningapps.veggietizer.activity.details_info_popup_open";

    private SavingsType savingsType;

    private boolean isDetailsInfoPopupOpen;
    private Dialog detailsInfoPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        Intent intent = getIntent();
        String savingsTypeStr = intent.getStringExtra(ViewConstants.SAVINGS_TYPE);
        savingsType = SavingsType.valueOf(savingsTypeStr);

        TextView heading = ((TextView) findViewById(R.id.textView_compare_savingsType));
        TextView subHeading = ((TextView) findViewById(R.id.textView_compare_savingsAmount));

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{heading, subHeading});
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.title_activity_compare));

        layoutComponents();

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

    private  void layoutComponents() {
        TextView heading = (TextView) findViewById(R.id.textView_compare_savingsType);
        TextView subHeading = (TextView) findViewById(R.id.textView_compare_savingsAmount);
        CharSequence textCompareTop;
        CharSequence textCompareBottom;
        int animIdTop;
        int animIdBottom;
        Context context = getApplicationContext();
        String wheatAmount;
        String fruitAmount;
        String potatoAmount;
        String tomatoAmount;
        String kilogrammes = getResources().getString(R.string.unitKilogrammes);
        String grammes = getResources().getString(R.string.unitGrammes);
        String kilometres = getResources().getString(R.string.unitKilometres);
        String squaremetres = getResources().getString(R.string.unitSquaremetres);
        String squaredecimetres = getResources().getString(R.string.unitSquaredecimetres);
        String metres = getResources().getString(R.string.unitKilometres);
        int numDecimalsText = 1;

        switch (this.savingsType) {
            case CO2:
                String distance = Formatter.format(Model.getTotalCarbonImpactInKilometres(context)
                        * Formatter.KILO, metres, kilometres, Formatter.KILO, numDecimalsText);

                wheatAmount = Formatter.format(Model.getTotalCarbonImpactAsFood(context, VeggieFood.WHEAT)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                fruitAmount = Formatter.format(Model.getTotalCarbonImpactAsFood(context, VeggieFood.FRUITS)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                potatoAmount = Formatter.format(Model.getTotalCarbonImpactAsFood(context, VeggieFood.POTATOES)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                textCompareTop = Html.fromHtml(
                        String.format(getString(R.string.textView_compare_cotwo_top), distance));
                textCompareBottom = Html.fromHtml(
                        String.format(getString(R.string.textView_compare_cotwo_bottom),
                        wheatAmount, fruitAmount, potatoAmount));
                animIdTop = R.drawable.anim_car;
                animIdBottom = R.drawable.anim_apple;
                break;
            case WATER:
                String timePeriodWater = Formatter.formatDays(Model.getTotalWaterImpactInDays(context), context,
                        R.plurals.days_water, numDecimalsText);

                wheatAmount = Formatter.format(Model.getTotalWaterImpactAsFood(context, VeggieFood.WHEAT)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                potatoAmount = Formatter.format(Model.getTotalWaterImpactAsFood(context, VeggieFood.POTATOES)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                tomatoAmount = Formatter.format(Model.getTotalWaterImpactAsFood(context, VeggieFood.TOMATOES)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                textCompareTop = Html.fromHtml(
                        String.format(getString(R.string.textView_compare_water_top),
                                timePeriodWater));
                textCompareBottom = Html.fromHtml(
                        String.format(getString(R.string.textView_compare_water_bottom),
                        wheatAmount, potatoAmount, tomatoAmount));
                animIdTop = R.drawable.anim_shower;
                animIdBottom = R.drawable.anim_tomato;
                break;
            case FEED:
                String timePeriodVeggie = Formatter.formatDays(Model.getTotalFeedEnergyImpactInDays(context), context,
                        R.plurals.days_feed, numDecimalsText);
                String timePeriodMeat = Formatter.formatDays(Model.getMeatEnergyInDays(context), context,
                        R.plurals.days_feed, numDecimalsText);
                String landArea = Formatter.format(Model.getTotalLandImpact(context)
                        * Formatter.CENTI, squaredecimetres, squaremetres, Formatter.CENTI, numDecimalsText);

                potatoAmount = Formatter.format(Model.getTotalLandImpactAsFood(context, VeggieFood.POTATOES)
                        * Formatter.KILO, grammes, kilogrammes, Formatter.KILO, numDecimalsText);
                textCompareTop = Html.fromHtml(
                        String.format(getString(R.string.textView_compare_feed_top),
                        timePeriodVeggie,
                        timePeriodMeat));
                textCompareBottom = Html.fromHtml(
                        String.format(getString(R.string.textView_compare_feed_bottom),
                        landArea, potatoAmount));
                animIdTop = R.drawable.anim_grain;
                animIdBottom = R.drawable.anim_fries;
                break;
            default:
                throw new IllegalStateException("Unsupported type of savings " + this.savingsType);
        }

        //Set heading text
        heading.setText(SavingsType.toString(getApplicationContext(), this.savingsType));
        subHeading.setText(Utility.getSavingsAmount(getApplicationContext(), 2, 0, Category.valueOf(savingsType)));

        ImageView imageViewAnimationTop = (ImageView) findViewById(R.id.imageView_animation_top);
        ImageView imageViewAnimationBottom = (ImageView) findViewById(R.id.imageView_animation_bottom);

        imageViewAnimationTop.setBackgroundResource(animIdTop);
        imageViewAnimationBottom.setBackgroundResource(animIdBottom);

        TextView textViewTop = (TextView) findViewById(R.id.textView_compare_top);
        TextView textViewBottom = (TextView) findViewById(R.id.textView_compare_bottom);

        textViewTop.setText(textCompareTop);
        textViewBottom.setText(textCompareBottom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.compare, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        ImageView imageViewAnimTop = (ImageView) findViewById(R.id.imageView_animation_top);
        ImageView imageViewAnimBottom = (ImageView) findViewById(R.id.imageView_animation_bottom);

        AnimationDrawable animTop = (AnimationDrawable) imageViewAnimTop.getBackground();
        AnimationDrawable animBottom = (AnimationDrawable) imageViewAnimBottom.getBackground();

        animTop.start();
        animBottom.start();

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        String detailsType = getIntent().getStringExtra(ViewConstants.SAVINGS_TYPE);

        switch (id) {
            case R.id.action_history:
                intent = new Intent(this, HistoryActivity.class);
                intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.COMPARE_ACTIVITY);
                intent.putExtra(ViewConstants.SAVINGS_TYPE, detailsType);
                startActivity(intent);
                break;
            case R.id.action_achievements:
                intent = new Intent(this, AchievementsActivity.class);
                intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.COMPARE_ACTIVITY);
                intent.putExtra(ViewConstants.SAVINGS_TYPE, detailsType);
                startActivity(intent);
                break;
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.COMPARE_ACTIVITY);
                intent.putExtra(ViewConstants.SAVINGS_TYPE, detailsType);
                startActivity(intent);
                break;
            case R.id.action_info:
                showDetailsInfoPopup();
                break;
            case R.id.action_barchart:
                intent = new Intent(this, BarChartActivity.class);
                intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.COMPARE_ACTIVITY);
                intent.putExtra(ViewConstants.SAVINGS_TYPE, detailsType);
                startActivity(intent);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_DETAILS_INFO_POPUP_OPEN, isDetailsInfoPopupOpen);
    }
}
