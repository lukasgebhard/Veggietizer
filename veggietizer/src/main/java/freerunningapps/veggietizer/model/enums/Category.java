package freerunningapps.veggietizer.model.enums;

import android.content.Context;
import android.content.res.Resources;
import freerunningapps.veggietizer.R;

/**
 * The categories supported by the app.
 * These categories are needed to check if an achievement has been unlocked.
 * Provides string conversion methods to handle localised strings.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public enum Category {
    BEEF,
    PORK,
    POULTRY,
    SHEEP_GOAT,
    FISH,
    MEAT,
	CO2,
	WATER,
	FEED;

    /**
     * Returns the localised name of the category.
     *
     * @param context The application context.
     * @param category The category.
     * @return The localised name of the <code>category</code>.
     */
    public static String toString(Context context, Category category) {
        switch (category) {
        case BEEF:
            return context.getResources().getString(R.string.meat_beef);
        case PORK:
            return context.getResources().getString(R.string.meat_pork);
        case POULTRY:
            return context.getResources().getString(R.string.meat_poultry);
        case SHEEP_GOAT:
            return context.getResources().getString(R.string.meat_sheep_goat);
        case FISH:
            return context.getResources().getString(R.string.meat_fish);
        case MEAT:
            return context.getResources().getString(R.string.meat);
        case CO2:
            return context.getResources().getString(R.string.co_two);
        case WATER:
            return context.getResources().getString(R.string.water);
        case FEED:
            return context.getResources().getString(R.string.feed);
        default:
            throw new IllegalStateException("Unsupported category '" + category + "'");
        }
    }

    /**
     * Converts the <code>savingsType</code> to the corresponding {@link Category}.
     *
     * @param savingsType The savings type.
     * @return The corresponding category.
     */
    public static Category valueOf(SavingsType savingsType) {
        switch (savingsType) {
            case CO2:
                return Category.CO2;
            case WATER:
                return Category.WATER;
            case FEED:
                return Category.FEED;
            default:
                throw new IllegalStateException("Unsupported savings type '" + savingsType + "'");
        }
    }

    /**
     * Returns the category.
     *
     * @param context The application context
     * @param category The category as a localised string.
     * @return The category.
     */
    @SuppressWarnings("unused")
    public static Category valueOf(Context context, String category) {
        Resources res = context.getResources();
        String pork = res.getString(R.string.meat_pork);
        String beef = res.getString(R.string.meat_beef);
        String poultry = res.getString(R.string.meat_poultry);
        String sheepGoat = res.getString(R.string.meat_sheep_goat);
        String fish = res.getString(R.string.meat_fish);
        String meat = res.getString(R.string.meat);
        String co2 = res.getString(R.string.co_two);
        String water = res.getString(R.string.water);
        String feed = res.getString(R.string.feed);

        if (category.equals(pork)) {
            return Category.PORK;
        } else if (category.equals(beef)) {
            return Category.BEEF;
        } else if (category.equals(poultry)) {
            return Category.POULTRY;
        } else if (category.equals(sheepGoat)) {
            return Category.SHEEP_GOAT;
        } else if (category.equals(fish)) {
            return Category.FISH;
        } else if (category.equals(meat)) {
            return Category.MEAT;
        } else if (category.equals(co2)) {
            return Category.CO2;
        } else if (category.equals(water)) {
            return Category.WATER;
        } else if (category.equals(feed)) {
            return Category.FEED;
        } else {
            throw new IllegalArgumentException("Cannot parse '" + category + "'");
        }
    }
}
