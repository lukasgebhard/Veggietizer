package freerunningapps.veggietizer.model.database;

import android.content.Context;
import android.provider.BaseColumns;
import freerunningapps.veggietizer.model.enums.Meat;

/**
 * The DB contract.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public final class DatabaseContract {
    public static final String DB_NAME = "vegginator.db";
    static final String DB_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Prevents from instantiating this class.
     */
    private DatabaseContract() {}

    /**
     * The contract for the meat_dish table.
     * For performance reason, this is a stand-alone table, meaning that it does not contain any foreign keys.
     * The sort_of_meat column only accepts sorts as specified by
     * {@link freerunningapps.veggietizer.model.enums.Meat#toString(Context, Meat)}.
     * The date is meant to have the format {@link DatabaseContract#DB_DATE_FORMAT}.
     *
     * @author Lukas Gebhard <freerunningapps@gmail.com>
     */
    public static abstract class MeatDishContract implements BaseColumns {

        public static final String TABLE_NAME = "meat_dish";

        public static final String COLUMN_NAME_DATE = "meat_dish_date";

        public static final String COLUMN_NAME_SORT_OF_MEAT = "sort_of_meat";

        public static final String COLUMN_NAME_AMOUNT = "amount_of_meat";
    }
}
