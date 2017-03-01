package freerunningapps.veggietizer.controller;

import android.content.AsyncTaskLoader;
import android.content.ContentProvider;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

/**
 * A {@link Cursor} loader that (in contrast to {@link CursorLoader}) can be used without a {@link ContentProvider}.
 * <p />
 * Note that, other than Android's {@link CursorLoader}, this one does not handle changes that occur on the cursor's
 * underlying data. This also implies that the cursor will be re-queried each time the owner activity is created.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    /**
     * The cursor instance to manage.
     */
    private Cursor cursor;

    public SimpleCursorLoader(Context context) {
        super(context);
    }

    @Override
    public abstract Cursor loadInBackground();

    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        Cursor oldCursor = this.cursor;
        this.cursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != this.cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the data. When the result is ready, the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid,
     * the result may be passed to the callbacks immediately.
     */
    @Override
    protected void onStartLoading() {
        if (cursor != null) {
            deliverResult(cursor);
        }
        if (takeContentChanged() || cursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempts to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensures the loader is stopped
        onStopLoading();

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = null;
    }
}