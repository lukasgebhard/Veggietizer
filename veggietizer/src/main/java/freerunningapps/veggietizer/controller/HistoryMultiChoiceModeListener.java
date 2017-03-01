package freerunningapps.veggietizer.controller;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import freerunningapps.veggietizer.BuildConfig;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.view.activity.HistoryActivity;

/**
 * Currently unused.
 * <p />
 * Shows an ActionBar overlay containing the delete button each time a history entry is selected.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
@SuppressWarnings("ALL")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HistoryMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
    HistoryActivity historyActivity;
    ActionMode activeMode;
    ListView history;

    public HistoryMultiChoiceModeListener(HistoryActivity historyActivity, ListView history) {
        this.historyActivity = historyActivity;
        this.history = history;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // Unused.
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = historyActivity.getMenuInflater();

        inflater.inflate(R.menu.history, menu);
        activeMode = mode;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (BuildConfig.DEBUG && item.getItemId() != R.id.action_discard) {
            throw new IllegalStateException("No action except 'discard' implemented currently");
        }

        // historyActivity.deleteSelectedEntries();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        activeMode = null;
    }
}
