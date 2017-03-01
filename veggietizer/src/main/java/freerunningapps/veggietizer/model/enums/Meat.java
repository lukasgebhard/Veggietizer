package freerunningapps.veggietizer.model.enums;

import android.content.Context;
import android.content.res.Resources;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.model.Food;

/**
 * The sorts of meat supported by the app.
 * Provides string conversion methods to handle localised strings.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public enum Meat implements Food {
    PORK,
    BEEF,
    POULTRY,
    SHEEP_GOAT,
    FISH;

    /**
     * Converts the <code>category</code> to the corresponding sort of {@link Meat}.
     *
     * @param category The category.
     * @return The corresponding sort of meat.
     */
    public static Meat valueOf(Category category) {
        switch (category) {
        case PORK:
            return Meat.PORK;
        case BEEF:
            return Meat.BEEF;
        case POULTRY:
            return Meat.POULTRY;
        case SHEEP_GOAT:
            return Meat.SHEEP_GOAT;
        case FISH:
            return Meat.FISH;
        default:
            throw new IllegalArgumentException(category + " cannot be converted to a sort of meat");
        }
    }

    /**
     * Returns the localised name of the sort of <code>meat</code>.
     *
     * @param context The application context.
     * @param meat The sort of meat.
     * @return The localised name of <code>meat</code>.
     */
    public static String toString(Context context, Meat meat) {
        switch (meat) {
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
        default:
            throw new IllegalStateException("Unsupported sort of meat '" + meat + "'");
        }
    }

    /**
     * Returns the sort of meat.
     *
     * @param context The application context
     * @param meat The sort of meat as a localised string.
     * @return The sort.
     */
    public static Meat valueOf(Context context, String meat) {
        Resources res = context.getResources();
        String pork = res.getString(R.string.meat_pork);
        String beef = res.getString(R.string.meat_beef);
        String poultry = res.getString(R.string.meat_poultry);
        String sheepGoat = res.getString(R.string.meat_sheep_goat);
        String fish = res.getString(R.string.meat_fish);

        if (meat.equals(pork)) {
            return Meat.PORK;
        } else if (meat.equals(beef)) {
            return Meat.BEEF;
        } else if (meat.equals(poultry)) {
            return Meat.POULTRY;
        } else if (meat.equals(sheepGoat)) {
            return Meat.SHEEP_GOAT;
        } else if (meat.equals(fish)) {
            return Meat.FISH;
        } else {
            throw new IllegalArgumentException("Cannot parse '" + meat + "'");
        }
    }
}
