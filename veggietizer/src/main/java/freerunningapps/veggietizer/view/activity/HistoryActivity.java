package freerunningapps.veggietizer.view.activity;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.controller.SimpleCursorLoader;
import freerunningapps.veggietizer.controller.adapter.HistoryCursorAdapter;
import freerunningapps.veggietizer.model.AchievementSet;
import freerunningapps.veggietizer.model.Model;
import freerunningapps.veggietizer.model.database.DatabaseContract.MeatDishContract;
import freerunningapps.veggietizer.view.Utility;
import freerunningapps.veggietizer.view.ViewConstants;

import java.util.LinkedList;
import java.util.List;

/**
 * Displays the history of consumed meat dishes in chronological order.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class HistoryActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {
    private static final String ENTRY_CHECKED = "freerunningapps.veggietizer.history.entry_checked";
    private static final int HISTORY_LOADER = 0;
    private SimpleCursorAdapter adapter;
    private boolean isEntryChecked;
    private ListView listView;
    private LoaderCallbacks<Cursor> loaderCallbacks;

    /**
     * Deletes the meat dish that is linked to the selected list entry.
     * While deleting, the menu item that triggered this task, is disabled.
     * On task completion, a {@link Toast} is shown.
     *
     * @author Lukas Gebhard <freerunningapps@gmail.com>
     */
    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        private MenuItem toDisable;
        private int entriesToDelete;

        public DeleteTask(MenuItem toDisable) {
            super();

            this.toDisable = toDisable;
        }

        /**
         * Deletes the selected meat dish from the DB.
         *
         * @param params Unused.
         * @return Unused.
         */
        @Override
        protected Void doInBackground(Void... params) {
            SparseBooleanArray checkedEntries = listView.getCheckedItemPositions();
            Adapter adapter = listView.getAdapter();
            List<View> viewsToDelete = new LinkedList<>();
            List<Long> meatDishesToDelete = new LinkedList<>();

            for (int i = 0; i < adapter.getCount(); i++) {
                if (checkedEntries.get(i)) {
                    viewsToDelete.add(adapter.getView(i, null, null));
                }
            }

            entriesToDelete = viewsToDelete.size();

            for (View v : viewsToDelete) {
                String meatDishesToDeleteStr = v.findViewById(R.id.imageView_history)
                        .getContentDescription().toString();

                meatDishesToDelete.add(Long.valueOf(meatDishesToDeleteStr));
            }

            Model.deleteMeatDishes(getApplicationContext(), meatDishesToDelete);
            return null;
        }

        /**
         * Disables the menu item that triggered this task.
         * This prevents errors related to multiple deletion of the same data.
         * The button is re-enabled as soon as the data are deleted.
         */
        @Override
        protected void onPreExecute() {
            toDisable.setEnabled(false);

            super.onPreExecute();
        }

        /**
         * Updates the View after the deletion.
         * That is, re-enables the menu item that triggered this task, shows a {@link Toast} and refreshes the
         * action bar as well as the list itself.
         */
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            String toastText = entriesToDelete + " "
                    + getResources().getQuantityString(R.plurals.entry, entriesToDelete) + " "
                    + getResources().getString(R.string.toast_delete_success);
            Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);

            //Check achievements for relocked achievements after deletion
            AchievementSet.getInstance(getApplicationContext()).checkAchievements();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                /*
                 * For some reason, at older Android versions, ListView#getCheckedItemCount()
                 * sums up previous selections even if those
                 * have already been deleted - even though calling listView.clearChoices() after deleting.
                 *
                 * The only way to clear all selections seems to restart the activity...
                 */
                startActivity(getIntent());
                toast.show();
            } else {
                isEntryChecked = false;
                LoaderManager loaderManager = getLoaderManager();
                loaderManager.destroyLoader(HISTORY_LOADER);
                loaderManager.initLoader(HISTORY_LOADER, null, loaderCallbacks); // Reloads the list.
                toast.show();
                toDisable.setEnabled(true);
                invalidateOptionsMenu();
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isEntryChecked = savedInstanceState != null && savedInstanceState.getBoolean(ENTRY_CHECKED);
        listView = null;
        loaderCallbacks = this;
        setContentView(R.layout.activity_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FontManager.getInstance().cacheFont(this, FontManager.Font.ROBOTO_LIGHT);
        getLoaderManager().initLoader(HISTORY_LOADER, null, this);
        listView = (ListView) findViewById(R.id.listview_history);
        initAdapter();
        initDeleteListener();

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{
                ((TextView) findViewById(R.id.textView_history_empty)),
                ((Button) findViewById(R.id.button_history_add))
        });
        Utility.styleActionBar(getSupportActionBar(), FontManager.Font.ROBOTO_LIGHT,
                getResources().getString(R.string.title_activity_history));
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
    }

    private void initDeleteListener() {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isEntryCheckedOld = isEntryChecked;

                isEntryChecked = listView.getCheckedItemCount() > 0;

                if (isEntryCheckedOld != isEntryChecked) {
                    invalidateOptionsMenu();
                }
            }
        });
    }

    @SuppressWarnings("UnusedParameters")
    public void onAddButtonClicked(View view) {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.HISTORY_ACTIVITY);
        startActivity(intent);
    }

    private void initAdapter() {
        String[] fromColumns = {BaseColumns._ID,
                MeatDishContract.COLUMN_NAME_DATE,
                MeatDishContract.COLUMN_NAME_SORT_OF_MEAT,
                MeatDishContract.COLUMN_NAME_AMOUNT};
        int[] toViews = {R.id.imageView_history,
                R.id.textView_history_date,
                R.id.textView_history_meat,
                R.id.textView_history_amount};

        adapter = new HistoryCursorAdapter(this, fromColumns, toViews);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar.
        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem discardItem = menu.getItem(0);

        discardItem.setVisible(isEntryChecked);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_discard) {
            DeleteTask deleteTask = new DeleteTask(item);

            deleteTask.execute();
        }

        return super.onOptionsItemSelected(item);
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
        case ViewConstants.INPUT_ACTIVITY: // History is empty; User clicked on "Add" button and then returned here.
            upIntent = new Intent(this, OverviewActivity.class); // Avoids navigation cycles
            break;
        default:
            throw new IllegalStateException("This activity was called by an activity which is not designated to "
                    + "do so");
        }

        return upIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != HISTORY_LOADER) {
            throw new IllegalStateException("Unknown loader created.");
        }

        return new SimpleCursorLoader(getApplicationContext()) {
            @Override
            public Cursor loadInBackground() {
                return Model.getMeatDishesCursor(getApplicationContext(), null, null);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // The framework will take care of closing the old cursor on returning.
        adapter.swapCursor(data);

        if (data.getCount() == 0) {
            TextView textViewHistoryEmpty = (TextView) findViewById(R.id.textView_history_empty);
            Button textViewHistoryAdd = (Button) findViewById(R.id.button_history_add);
            ListView listViewHistory = (ListView) findViewById(R.id.listview_history);

            textViewHistoryEmpty.setVisibility(View.VISIBLE);
            textViewHistoryAdd.setVisibility(View.VISIBLE);
            listViewHistory.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished() is about to be closed.
        adapter.swapCursor(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(ENTRY_CHECKED, isEntryChecked);
    }
}