package freerunningapps.veggietizer.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.*;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.controller.adapter.SourcesAdapter;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The "About" page.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class AboutActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Resources res = getResources();

        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT,
                new TextView[]{
                        ((TextView) findViewById(R.id.textView_about_app_name)),
                        ((TextView) findViewById(R.id.textView_about_app_developer)),
                        ((TextView) findViewById(R.id.textView_about_app_version)),
                        ((TextView) findViewById(R.id.textView_about_feedback)),
                        ((TextView) findViewById(R.id.textView_about_sources)),
                });
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                res.getString(R.string.title_activity_about));

        fillSourcesList();
        setAppVersion();
    }

    /**
      * Sets the app version number.
      */
    private void setAppVersion() {
        Resources res = getResources();
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            throw new Error(e);
        }
        TextView versionTextView = (TextView) findViewById(R.id.textView_about_app_version);
        versionTextView.setText(res.getString(R.string.app_version) + " " + version);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private void fillSourcesList() {
        Resources res = getResources();

        // Creates the upper list level
        List<Map<String, String>> groupData = new LinkedList<>();
        Map<String, String> co2Group = new HashMap<>(1);
        Map<String, String> waterGroup = new HashMap<>(1);
        Map<String, String> feedGroup = new HashMap<>(1);
        Map<String, String> dietGroup = new HashMap<>(1);
        Map<String, String> othersGroup = new HashMap<>(1);
        Map<String, String> imagesGroup = new HashMap<>(1);
        String groupName = "GroupName";
        String[] groupFrom = new String[] {groupName};
        int[] groupTo = new int[] {R.id.component_source_group};

        co2Group.put(groupName, res.getString(R.string.co_two));
        waterGroup.put(groupName, res.getString(R.string.water));
        feedGroup.put(groupName, res.getString(R.string.feed));
        dietGroup.put(groupName,res.getString(R.string.diet));
        othersGroup.put(groupName, res.getString(R.string.others));
        imagesGroup.put(groupName, res.getString(R.string.image_sources));

        groupData.add(co2Group);
        groupData.add(waterGroup);
        groupData.add(feedGroup);
        groupData.add(dietGroup);
        groupData.add(othersGroup);
        groupData.add(imagesGroup);

        // Creates the lower list level
        List<List<Map<String, String>>> childData = new LinkedList<>();
        String url = "URL";
        String date = "date";
        String[] childFrom = new String[] {url, date};
        int[] childTo = new int[] {R.id.textView_source_url, R.id.textView_source_date};

        List<Map<String, String>> co2Entries = new LinkedList<>();
        List<Map<String, String>> waterEntries = new LinkedList<>();
        List<Map<String, String>> feedEntries = new LinkedList<>();
        List<Map<String, String>> dietEntries = new LinkedList<>();
        List<Map<String, String>> othersEntries = new LinkedList<>();
        List<Map<String, String>> imagesEntries = new LinkedList<>();

        childData.add(co2Entries);
        childData.add(waterEntries);
        childData.add(feedEntries);
        childData.add(dietEntries);
        childData.add(othersEntries);
        childData.add(imagesEntries);

        List<String[][]> sources = new LinkedList<>();
        String[] sourcesCo2Urls = res.getStringArray(R.array.about_sources_cotwo_urls);
        String[] sourcesCo2Dates = res.getStringArray(R.array.about_sources_cotwo_dates);
        String[] sourcesWaterUrls = res.getStringArray(R.array.about_sources_water_urls);
        String[] sourcesWaterDates = res.getStringArray(R.array.about_sources_water_dates);
        String[] sourcesFeedUrls = res.getStringArray(R.array.about_sources_feed_urls);
        String[] sourcesFeedDates = res.getStringArray(R.array.about_sources_feed_dates);
        String[] sourcesDietUrls = res.getStringArray(R.array.about_sources_diet_urls);
        String[] sourcesDietDates = res.getStringArray(R.array.about_sources_diet_dates);
        String[] sourcesOthersUrls = res.getStringArray(R.array.about_sources_others_urls);
        String[] sourcesOthersDates = res.getStringArray(R.array.about_sources_others_dates);
        String[] sourcesImagesUrls = res.getStringArray(R.array.about_sources_images_urls);
        String[] sourcesImagesDates = res.getStringArray(R.array.about_sources_images_dates);

        sources.add(new String[][] {sourcesCo2Urls, sourcesCo2Dates});
        sources.add(new String[][] {sourcesWaterUrls, sourcesWaterDates});
        sources.add(new String[][] {sourcesFeedUrls, sourcesFeedDates});
        sources.add(new String[][] {sourcesDietUrls, sourcesDietDates});
        sources.add(new String[][] {sourcesOthersUrls, sourcesOthersDates});
        sources.add(new String[][] {sourcesImagesUrls, sourcesImagesDates});

        int k = 0;
        for (String[][] sourceGroup : sources) {
            for (int i = 0; i < sourceGroup[0].length; ++i) {
                Map<String, String> entry = new HashMap<>(2);

                entry.put(url, sourceGroup[0][i]);
                entry.put(date, sourceGroup[1][i]);
                childData.get(k).add(entry);
            }
            ++k;
        }

        // Adds the list adapter
        ExpandableListView sourcesList = (ExpandableListView) findViewById(R.id.expandablelistview_about_sources);
        ExpandableListAdapter adapter = new SourcesAdapter(this, groupData,
                groupFrom, groupTo, childData, childFrom, childTo);
        sourcesList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.about, menu);
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
}
