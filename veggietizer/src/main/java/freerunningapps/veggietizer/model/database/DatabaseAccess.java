package freerunningapps.veggietizer.model.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.widget.Toast;
import freerunningapps.veggietizer.BuildConfig;
import freerunningapps.veggietizer.model.util.DateParser;
import freerunningapps.veggietizer.model.enums.Meat;
import freerunningapps.veggietizer.model.MeatDish;
import freerunningapps.veggietizer.model.database.DatabaseContract.MeatDishContract;


/**
 * Encapsulates a DB connection and provides DB access methods.
 * This is a singleton class. That is, this class can only be instantiated once, using the provided factory method.
 * After that, the factory method returns the reference of the existing instance instead of creating a new one.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 * @see DatabaseAccess#getInstance(Context)
 */
public class DatabaseAccess extends SQLiteOpenHelper {
    /**
     * Indicates the current DB version.
     * On modifying the DB scheme, increment the version number.
     */
    private static final int DB_VERSION = 1;

    private static final String SQL_CHECK_SORT_OF_MEAT = "CHECK(" + MeatDishContract.COLUMN_NAME_SORT_OF_MEAT + " IN ("
            + "'" + Meat.PORK + "', "
            + "'" + Meat.BEEF + "', "
            + "'" + Meat.POULTRY + "', "
            + "'" + Meat.SHEEP_GOAT + "', "
            + "'" + Meat.FISH + "'"
            + "))";

    private static final String SQL_CHECK_DATE = "CHECK(" + MeatDishContract.COLUMN_NAME_DATE + " LIKE '____-__-__')";

    private static final String SQL_CHECK_AMOUNT = "CHECK(" + MeatDishContract.COLUMN_NAME_AMOUNT + " > 0)";

    /**
     * Defines the DB scheme as an SQL CREATE TABLE statement.
     */
    private static final String SQL_CREATE_TABLE_MEAT_DISH = "CREATE TABLE IF NOT EXISTS "
            + MeatDishContract.TABLE_NAME + " ("
            + MeatDishContract._ID + " INTEGER PRIMARY KEY, "
            + MeatDishContract.COLUMN_NAME_DATE + " TEXT "+ SQL_CHECK_DATE + ", "
            + MeatDishContract.COLUMN_NAME_SORT_OF_MEAT + " TEXT " + SQL_CHECK_SORT_OF_MEAT + ", "
            + MeatDishContract.COLUMN_NAME_AMOUNT + " INTEGER " + SQL_CHECK_AMOUNT
            + ")";

    @SuppressWarnings("unused")
    private static final String SQL_DROP_TABLE_MEAT_DISH = "DROP TABLE IF EXISTS " + MeatDishContract.TABLE_NAME;

    private static final String SQL_QUERY_AMOUNT_BY_SORT_OF_MEAT = "SELECT SUM("
            + MeatDishContract.COLUMN_NAME_AMOUNT + ") "
            + "FROM " + MeatDishContract.TABLE_NAME + " "
            + "WHERE " + MeatDishContract.COLUMN_NAME_SORT_OF_MEAT + " = ?";

    @SuppressWarnings("unused")
    private static final String SQL_QUERY_SIZE_TABLE_MEAT_DISH = "SELECT COUNT(*) FROM " + MeatDishContract.TABLE_NAME;

    private static final String SQL_QUERY_MEAT_DISH_BY_IDS_FRAGMENT = "SELECT * FROM "
            + MeatDishContract.TABLE_NAME + " "
            + "WHERE ";

    private static final String SQL_QUERY_MEAT_DISHES_WITHIN_PERIOD = "SELECT * FROM "
            + MeatDishContract.TABLE_NAME + " "
            + "WHERE " + MeatDishContract.COLUMN_NAME_DATE + " >= ? "
            + "AND " + MeatDishContract.COLUMN_NAME_DATE + " <= ? "
            + "ORDER BY " + MeatDishContract.COLUMN_NAME_DATE + " DESC";

    /**
     * Keeps track of the amount of meat made up by each sort of meat.
     * This is a cheap but valuable optimisation as many calculations are based on these values.
     */
    private Map<Meat, Integer> meatDishAmounts;

    /**
     * <code>true</code> as soon as {@link DatabaseAccess#meatDishAmounts} have been initialised.
     * Needed for synchronisation.
     */
    private boolean isInitialised;

    private static DatabaseAccess singletonInstance = null;

    /**
     * Create the DatabaseAccess.
     * The constructor is private so that a DatabaseAccess can only be instantiated using the
     * factory method.
     *
     * @param context To use to open or create the database.
     * @see DatabaseAccess#getInstance(Context)
     */
    private DatabaseAccess(Context context) {
        super(context, DatabaseContract.DB_NAME, null, DB_VERSION);

        meatDishAmounts = null;
        isInitialised = false;
    }

    /**
     * Reads the total amount of stored meat for each meat category from the DB and stores it in this instance's
     * field.
     */
    private synchronized void initAmounts() {
        if (!isInitialised) {
            Meat[] sortsOfMeat = Meat.values();
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = null;

            if(BuildConfig.DEBUG && meatDishAmounts != null) {
                throw new AssertionError("The amounts by sort of meat have already been initialised");
            }

            meatDishAmounts = new HashMap<>(sortsOfMeat.length);

            for (Meat m : sortsOfMeat) {
                String[] selectionArgs = new String[] {m.toString()};

                cursor = db.rawQuery(SQL_QUERY_AMOUNT_BY_SORT_OF_MEAT, selectionArgs);
                cursor.moveToFirst();
                meatDishAmounts.put(m, cursor.getInt(0));
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        isInitialised = true;
    }

    /**
     * Updates the field that stores the totals of meat consumption by sort of meat.
     *
     * @param meat The sort of meat which is updated.
     * @param amount The amount of meat that is added or deleted.
     * @param isAdded <code>false</code> if the specified amount should be removed.
     */
    private synchronized void updateAmounts(Meat meat, int amount, boolean isAdded) {
        if (!isInitialised) {
            initAmounts();
        } else {
            int meatSortAmount = meatDishAmounts.get(meat);

            if (isAdded) {
                meatSortAmount += amount;
                meatDishAmounts.put(meat, meatSortAmount);
            } else {
                meatSortAmount -= amount;
                meatDishAmounts.put(meat, meatSortAmount);
            }
        }
    }

    /**
     * Factory method to get the singleton instance of this class.
     *
     * Do NEVER call <code>close()</code> on this instance. This would raise errors on further calls of methods that
     * access the DB. On shutting down, the app will close this <code>DatabaseAccess</code> automatically.
     *
     * @param context To use to open or create the database.
     * @return The singleton instance.
     */
    public static DatabaseAccess getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new DatabaseAccess(context.getApplicationContext());
        }

        return singletonInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_MEAT_DISH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Currently unneeded.
    }

    /**
     * Provides the total weight of the specified sort of meat stored so far.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param meat The sort of meat.
     * @return the total amount of meat in grammes.
     */
    public int getMeatAmount(Meat meat) {
        if (!isInitialised) {
            initAmounts(); // synchronised
        }

        return meatDishAmounts.get(meat);
    }

    /**
     * Adds the specified meat dish to the {@link MeatDishContract} table.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     * You can override <code>AsyncTask.onPreExecute()</code> to disable UI features and
     * <code>AsyncTask.onPostExecute()</code> to re-enable them and to display a status message (e.g., a {@link Toast}).
     *
     * @param date The meat dish's date.
     * @param meat The meat dish's sort of meat.
     * @param amount The amount of meat in grammes.
     * @return the ID of the inserted meat dish or <code>-1</code> if an error occurred.
     */
    public synchronized long insert(Date date, Meat meat, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be positive");
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat(DatabaseContract.DB_DATE_FORMAT);
        long insertedRowId;

        values.put(MeatDishContract.COLUMN_NAME_DATE, dateFormat.format(date));
        values.put(MeatDishContract.COLUMN_NAME_SORT_OF_MEAT, meat.toString());
        values.put(MeatDishContract.COLUMN_NAME_AMOUNT, amount);

        insertedRowId = db.insert(MeatDishContract.TABLE_NAME, null, values);

        if (insertedRowId >= 0) {
            updateAmounts(meat, amount, true);
        }

        return insertedRowId;
    }

    /**
     * Removes the specified meat dishes from the {@link MeatDishContract} table.
     * <p />
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     * You can override <code>AsyncTask.onPreExecute()</code> to disable UI features and
     * <code>AsyncTask.onPostExecute()</code> to re-enable them and to display a status message
     * (e.g., as a {@link Toast}).
     *
     * @param meatDishIds The meat dish IDs.
     */
    public synchronized void delete(Collection<Long> meatDishIds) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder whereClause = new StringBuilder();

        int i = 0;
        for (Long id : meatDishIds) {
            whereClause.append(BaseColumns._ID + " = " + id);

            if (i < meatDishIds.size() - 1) {
                whereClause.append(" OR ");
            }
            ++i;
        }

        int numOfAffectedRows;
        Cursor cursor = db.rawQuery(SQL_QUERY_MEAT_DISH_BY_IDS_FRAGMENT + whereClause, null);
        int delAmount;
        String sort;
        Meat meat;

        if (cursor.getCount() < 1) {
            throw new IllegalArgumentException("There is no meat dish with one of the specified IDs.");
        }

        while (cursor.moveToNext()) {
            delAmount = cursor.getInt(cursor.getColumnIndex(MeatDishContract.COLUMN_NAME_AMOUNT));
            sort = cursor.getString(cursor.getColumnIndex(MeatDishContract.COLUMN_NAME_SORT_OF_MEAT));
            meat = Meat.valueOf(sort);

            updateAmounts(meat, delAmount, false);
        }

        numOfAffectedRows = db.delete(MeatDishContract.TABLE_NAME, whereClause.toString(), null);

        if (BuildConfig.DEBUG && numOfAffectedRows != meatDishIds.size()) {
            throw new AssertionError(numOfAffectedRows + " were affected. The delete() method should have affected "
                    + "exactly " + meatDishIds.size() + " rows.");
        }

        cursor.close();
    }

    /**
     * Returns a cursor on all stored meat dishes that belong to the specified period of time.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param periodStart The first day of the period (inclusive) or <code>null</code> if no start limit.
     * @param periodEnd The last day of the period (inclusive) or <code>null</code> if no end limit.
     * @return A {@link Cursor} instance to iterate over all currently stored meat dishes,
     *          ordered by date (latest first).
     */
    public Cursor getMeatDishesCursor(Date periodStart, Date periodEnd) {
        SQLiteDatabase db = getWritableDatabase();
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat(DatabaseContract.DB_DATE_FORMAT);
        String farPast = "0";
        String farFuture = "9";
        String periodStartStr = periodStart == null ? farPast : dateFormat.format(periodStart);
        String periodEndStr = periodEnd == null ? farFuture : dateFormat.format(periodEnd);
        String[] selectionArgs = new String[] {periodStartStr, periodEndStr};

        return db.rawQuery(SQL_QUERY_MEAT_DISHES_WITHIN_PERIOD, selectionArgs);
    }
    
    /**
     * Returns all stored meat dishes that belong to the specified period of time.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param periodStart The first day of the period (inclusive) or <code>null</code> if no start limit.
     * @param periodEnd The last day of the period (inclusive) or <code>null</code> if no end limit.
     * @return The stored meat dishes, ordered by date (latest first), or an empty array if none available.
     */
    public MeatDish[] getMeatDishes(Date periodStart, Date periodEnd) {
        Cursor cursor = getMeatDishesCursor(periodStart, periodEnd);
        MeatDish[] meatDishes = new MeatDish[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; i < meatDishes.length; ++i) {
            int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            String dateStr = cursor.getString(cursor.getColumnIndex(MeatDishContract.COLUMN_NAME_DATE));
            String sort = cursor.getString(cursor.getColumnIndex(MeatDishContract.COLUMN_NAME_SORT_OF_MEAT));
            int amount = cursor.getShort(cursor.getColumnIndex(MeatDishContract.COLUMN_NAME_AMOUNT));
            Date date = DateParser.parseISO2014(dateStr, null);
            Meat meat = Meat.valueOf(sort);
            meatDishes[i] = new MeatDish(id, date, meat, amount);
            cursor.moveToNext();
        }

        cursor.close();       
        return meatDishes;
    }
}
