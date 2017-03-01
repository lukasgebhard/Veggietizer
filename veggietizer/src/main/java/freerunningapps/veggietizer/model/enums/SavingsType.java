package freerunningapps.veggietizer.model.enums;

import android.content.Context;
import android.content.res.Resources;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.view.chart.BarChart;

/**
 * The detail categories shown as a {@link BarChart} in this app.
 * Provides string conversion methods to handle localised strings.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public enum SavingsType {
    CO2,
    WATER,
    FEED;

    /**
     * Converts the <code>category</code> to the corresponding {@link SavingsType}.
     *
     * @param category The category.
     * @return The corresponding savings type.
     */
    @SuppressWarnings("unused")
    public static SavingsType valueOf(Category category) {
        switch (category) {
        case CO2:
            return SavingsType.CO2;
        case WATER:
            return SavingsType.WATER;
        case FEED:
            return SavingsType.FEED;
        default:
            throw new IllegalArgumentException(category + " cannot be converted to a savings type");
        }
    }

    /**
     * Returns the localised name of the <code>savingsType</code>.
     *
     * @param context The application context.
     * @param savingsType The savings type.
     * @return The localised name of <code>savingsType</code>.
     */
    public static String toString(Context context, SavingsType savingsType) {
        switch (savingsType) {
        case CO2:
            return context.getResources().getString(R.string.co_two);
        case WATER:
            return context.getResources().getString(R.string.water);
        case FEED:
            return context.getResources().getString(R.string.feed);
        default:
            throw new IllegalStateException("Unsupported savings type '" + savingsType + "'");
        }
    }

    /**
     * Returns the savings type.
     *
     * @param context The application context.
     * @param savingsType The savings type as a localised string.
     * @return The savings type.
     */
    @SuppressWarnings("unused")
    public static SavingsType valueOf(Context context, String savingsType) {
        Resources res = context.getResources();
        String co2 = res.getString(R.string.co_two);
        String water = res.getString(R.string.water);
        String feed = res.getString(R.string.feed);

        if (savingsType.equals(co2)) {
            return SavingsType.CO2;
        } else if (savingsType.equals(water)) {
            return SavingsType.WATER;
        } else if (savingsType.equals(feed)) {
            return SavingsType.FEED;
        } else {
            throw new IllegalArgumentException("Cannot parse '" + savingsType + "'");
        }
    }
}
