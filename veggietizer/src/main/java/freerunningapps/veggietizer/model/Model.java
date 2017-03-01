package freerunningapps.veggietizer.model;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.widget.Toast;
import freerunningapps.veggietizer.model.database.DatabaseAccess;
import freerunningapps.veggietizer.model.enums.*;

/**
 * Utility class implementing the business logic.
 * All interaction with the model can and should be done through this class.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public final class Model {
    /**
     * The factors describing the water impact of food consumption.
     * For each consumed kilogramme of the given sort of food, the impact factor tells how many litres of water is
     * needed to produce it.
     * The impact of fish production is not considered and therefore set to 0.
     */
    private static final Map<Food, Integer> IMPACT_WATER;

    /**
     * The factors describing the carbon impact of food consumption.
     * For each consumed kilogramme of the given sort of food, the impact factor tells how many grammes of CO2 goes into
     * the atmosphere.
     */
    private static final Map<Food, Integer> IMPACT_CARBON;

    /**
     * The factors describing the feed impact of meat consumption.
     * For each consumed kilogramme of the given sort of meat, the impact factor tells how many grammes of corn/ soy is
     * needed to feed the animals.
     * The impact of fish production is not considered and therefore set to 0.
     */
    private static final Map<Meat, Integer> IMPACT_FEED;

    /**
     * The factors describing the feed energy impact of meat consumption.
     * For each consumed kilogramme of a given sort of meat, the impact factor tells how many kcal of corn/ soy is
     * needed to feed the animals.
     * The impact of fish production is not considered and therefore set to 0.
     */
    private static final Map<Meat, Integer> IMPACT_FEED_ENERGY;

    /**
     * The factors to convert meat into energy.
     * For each kilogramme of a given sort of meat, the factor tells how many kcal of energy it contains.
     * Fish is not considered and the factor is therefore set to 0.
     */
    private static final Map<Meat, Integer> ENERGY_PER_KILOGRAMME;

    /**
     * The factors describing the impact of food consumption on land consumption.
     * For each consumed kilogramme of the given sort of food, the impact factor tells how many square metres
     * are needed to grow the animal feed (including grass).
     * The impact of fish production is not considered and therefore set to 0.
     */
    private static final Map<Food, Float> IMPACT_LAND;

    /**
     * The daily energy demand of adults in kilo-calories.
     */
    private static final int DAILY_ENERGY_DEMAND = 2000;

    /**
     * The amount of water in litres each German consumes directly (by cooking, washing, ...) every day.
     */
    private static final int DIRECT_DAILY_WATER_CONSUMPTION = 121;

    /**
     * The average CO2 emissions in g/km of cars introduced in Germany in 2013.
     */
    private static final int AVERAGE_CO2_EMISSIONS = 135;

    static {
        int mapSize = Meat.values().length + VeggieFood.values().length;

        IMPACT_WATER = new HashMap<>(mapSize);
        IMPACT_WATER.put(Meat.BEEF, 16216);
        IMPACT_WATER.put(Meat.PORK, 3697);
        IMPACT_WATER.put(Meat.POULTRY, 2347);
        IMPACT_WATER.put(Meat.FISH, 0);
        IMPACT_WATER.put(Meat.SHEEP_GOAT, 4893);
        IMPACT_WATER.put(VeggieFood.POTATOES, 226);
        IMPACT_WATER.put(VeggieFood.WHEAT, 1639);
        IMPACT_WATER.put(VeggieFood.TOMATOES, 171);

        IMPACT_CARBON = new HashMap<>(mapSize);
        IMPACT_CARBON.put(Meat.BEEF, 20650);
        IMPACT_CARBON.put(Meat.PORK, 7990);
        IMPACT_CARBON.put(Meat.POULTRY, 4220);
        IMPACT_CARBON.put(Meat.FISH, 4120);
        IMPACT_CARBON.put(Meat.SHEEP_GOAT, 14900);
        IMPACT_CARBON.put(VeggieFood.POTATOES, 620);
        IMPACT_CARBON.put(VeggieFood.WHEAT, 1680);
        IMPACT_CARBON.put(VeggieFood.FRUITS, 980);

        IMPACT_FEED = new HashMap<>(mapSize);
        IMPACT_FEED.put(Meat.BEEF, 2419);
        IMPACT_FEED.put(Meat.PORK, 4702);
        IMPACT_FEED.put(Meat.POULTRY, 4395);
        IMPACT_FEED.put(Meat.FISH, 0);
        IMPACT_FEED.put(Meat.SHEEP_GOAT, 4426);

        IMPACT_FEED_ENERGY = new HashMap<>(mapSize);
        IMPACT_FEED_ENERGY.put(Meat.BEEF, 7191);
        IMPACT_FEED_ENERGY.put(Meat.PORK, 14009);
        IMPACT_FEED_ENERGY.put(Meat.POULTRY, 11688);
        IMPACT_FEED_ENERGY.put(Meat.FISH, 0);
        IMPACT_FEED_ENERGY.put(Meat.SHEEP_GOAT, 11474);

        IMPACT_LAND = new HashMap<>(mapSize);
        IMPACT_LAND.put(Meat.BEEF, 39.14F);
        IMPACT_LAND.put(Meat.PORK, 12.33F);
        IMPACT_LAND.put(Meat.POULTRY, 13.55F);
        IMPACT_LAND.put(Meat.FISH, 0.0F);
        IMPACT_LAND.put(Meat.SHEEP_GOAT, 40.5F);
        IMPACT_LAND.put(VeggieFood.POTATOES, 0.25F);
        IMPACT_LAND.put(VeggieFood.WHEAT, 1.45F);

        ENERGY_PER_KILOGRAMME = new HashMap<>(mapSize);
        ENERGY_PER_KILOGRAMME.put(Meat.BEEF, 1928);
        ENERGY_PER_KILOGRAMME.put(Meat.PORK, 1757);
        ENERGY_PER_KILOGRAMME.put(Meat.POULTRY, 1953);
        ENERGY_PER_KILOGRAMME.put(Meat.FISH, 0);
        ENERGY_PER_KILOGRAMME.put(Meat.SHEEP_GOAT, 1705);
    }

    /**
     * Prevents from instantiating this class.
     */
    private Model() {}

    /**
     * Returns the total carbon impact of <code>requestedDishes</code>.
     * The carbon impact is defined as the amount of CO2 in milligrammes that goes into the atmosphere on producing
     * a certain amount of a certain sort of meat.
     *
     * @param requestedDishes The meat dishes to be considered.
     * @return The carbon impact of the specified meat dishes.
     */
    private static int getTotalCarbonImpact(MeatDish[] requestedDishes) {
        int totalCarbonImpact = 0;
        Map<Meat, Integer> amounts = new HashMap<>(Meat.values().length);
        int impactFactor;

        for (Meat m : Meat.values()) {
            amounts.put(m, getMeat(m, requestedDishes));
        }

        for (Meat m : Meat.values()) {
            impactFactor = IMPACT_CARBON.get(m);
            totalCarbonImpact += amounts.get(m) * impactFactor;
        }

        return totalCarbonImpact;
    }

    /**
     * Returns the total water impact of <code>requestedDishes</code>.
     * The water impact is defined as the amount of water in millilitres needed to produce a certain amount of a certain
     * sort of meat. The impact of fish production is not considered.
     *
     * @param requestedDishes The meat dishes to be considered.
     * @return The water impact of the specified meat dishes.
     */
    private static int getTotalWaterImpact(MeatDish[] requestedDishes) {
        int totalWaterImpact = 0;
        Map<Meat, Integer> amounts = new HashMap<>(Meat.values().length);
        int impactFactor;

        for (Meat m : Meat.values()) {
            amounts.put(m, getMeat(m, requestedDishes));
        }

        for (Meat m : Meat.values()) {
            impactFactor = IMPACT_WATER.get(m);
            totalWaterImpact += amounts.get(m) * impactFactor;
        }

        return totalWaterImpact;
    }

    /**
     * Returns the total feed impact of <code>requestedDishes</code>.
     * The feed impact is defined as the amount of feed in milligrammes that is needed to produce
     * a certain amount of a certain sort of meat. The impact of fish production is not considered.
     *
     * @param requestedDishes The meat dished to be considered.
     * @return The feed impact of the specified meat dishes.
     */
    private static int getTotalFeedImpact(MeatDish[] requestedDishes) {
        int totalFeedImpact = 0;
        Map<Meat, Integer> amounts = new HashMap<>(Meat.values().length);
        int impactFactor;

        for (Meat m : Meat.values()) {
            amounts.put(m, getMeat(m, requestedDishes));
        }

        for (Meat m : Meat.values()) {
            impactFactor = IMPACT_FEED.get(m);
            totalFeedImpact += amounts.get(m) * impactFactor;
        }

        return totalFeedImpact;
    }

    /**
     * Provides the total weight of the specified sort of <code>meat</code> that belongs to <code>requestedDishes</code>.
     *
     * @param requestedDishes The meat dished to be considered.
     * @param meat The sort of meat.
     * @return The amount of the specified sort of <code>meat</code> that belongs to the dishes in grammes.
     */
    private static int getMeat(Meat meat, MeatDish[] requestedDishes) {
        int amount = 0;
        for (MeatDish dish : requestedDishes) {
            if (dish.getMeat() == meat) {
                amount += dish.getAmount();
            }
        }
        return amount;
    }

    /**
     * Provides the total weight of meat that belongs to <code>requestedDishes</code>.
     *
     * @param requestedDishes The meat dished to be considered.
     * @return the total amount of meat in grammes.
     */
    private static int getTotalMeat(MeatDish[] requestedDishes) {
        int amount = 0;
        for (MeatDish dish : requestedDishes) {
            amount += dish.getAmount();
        }
        return amount;
    }
    
    /**
     * Depending on the specified <code>category</code>, returns the number of units abstained from.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param category The category.
     * @return The number of units abstained from.
     */
    private static int getSaved(Context context, Category category) {
        switch (category) {
        case PORK:
        case BEEF:
        case POULTRY:
        case SHEEP_GOAT:
        case FISH:
            return getMeatSaved(context, Meat.valueOf(category));
        case MEAT:
            return getMeatSavedTotal(context);
        case CO2:
            return getTotalCarbonImpact(context);
        case WATER:
            return getTotalWaterImpact(context);
        case FEED:
            return getTotalFeedImpact(context);
        default:
            throw new IllegalStateException("Unsupported category '" + category + "'");
        }
    }

    /**
     * Returns the carbon impact of the given amount of <code>meat</code>.
     * The carbon impact is defined as the amount of CO2 in milligrammes that goes into the atmosphere on producing
     * a certain amount of a certain sort of meat.
     *
     * @param meat The sort of meat.
     * @param amount The amount of meat in grammes.
     * @return The carbon impact of the specified meat dish.
     */
    public static int getCarbonImpact(Meat meat, int amount) {
        int impactFactor = IMPACT_CARBON.get(meat);
        
        return amount * impactFactor;
    }
    
    /**
     * Returns the water impact of the given amount of <code>meat</code>.
     * The water impact is defined as the amount of water in millilitres needed to produce a certain amount of a certain
     * sort of meat. The impact of fish production is not considered.
     *
     * @param meat The sort of meat.
     * @param amount The amount of meat in grammes.
     * @return The water impact of the specified meat dish.
     */
    public static int getWaterImpact(Meat meat, int amount) {
        int impactFactor = IMPACT_WATER.get(meat);
        
        return amount * impactFactor;
    }
    
    /**
     * Returns the feed impact of the given amount of <code>meat</code>.
     * The feed impact is defined as the amount of feed in milligrammes that is needed to produce
     * a certain amount of a certain sort of meat. The impact of fish production is not considered.
     *
     * @param meat The sort of meat.
     * @param amount The amount of meat in grammes.
     * @return The feed impact of the specified meat dish.
     */
    public static int getFeedImpact(Meat meat, int amount) {
        int impactFactor = IMPACT_FEED.get(meat);
        
        return amount * impactFactor;
    }

    /**
     * Returns the land impact of the given amount of <code>meat</code>.
     * The land impact is defined as the area of land in square metres that is needed to grow a certain amount of
     * vegetarian food, or the animal feed for a certain amount of a certain sort of meat, respectively.
     * The impact of fish production is not considered.
     *
     * @param meat The sort of meat.
     * @param amount The amount of meat in grammes.
     * @return The land impact of the specified meat dish.
     */
    @SuppressWarnings({"unused"})
    private static float getLandImpact(Meat meat, int amount) {
        float impactFactor = IMPACT_LAND.get(meat);

        return (amount / 1000.0F) * impactFactor;
    }

    /**
     * Provides the total weight of meat abstained from so far.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @return the total amount of meat in grammes.
     */
    public static int getMeatSavedTotal(Context context) {
        int total = 0;

        for (Meat m : Meat.values()) {
    		total += Model.getMeatSaved(context, m);
        }

        return total;
    }

    /**
     * Provides the total weight of the specified sort of <code>meat</code> abstained from so far.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param meat The sort of meat.
     * @return The total amount of the specified sort of <code>meat</code> in grammes.
     */
    public static int getMeatSaved(Context context, Meat meat) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);

        return databaseAccess.getMeatAmount(meat);
    }

    /**
     * Depending on the specified <code>category</code>, returns the number of units abstained from.
     * <p />
     * In case <code>numOfPastDays</code> is negative, no time condition is applied and <code>periodEnd</code>
     * is ignored.
     * <p />
     * Otherwise, <code>numOfPastDays</code> days ending with <code>periodEnd</code> are taken into account.
     * <p />
     * E.g., assuming <code>periodEnd</code> is today's date: If <code>numOfPastDays</code> is 1,
     * the amount belonging to today and yesterday is returned. If it is 0, only today's amount is returned.
     * Assuming <code>periodEnd</code> is yesterday's date: If <code>numOfPastDays</code> is 1,
     * the amount belonging to yesterday and the day before yesterday is returned.
     * If it is 0, only yesterday's amount is returned.
     * <p />
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param category The category of interest.
     * @param periodEnd The final day (inclusively) of the time period to be considered.
     * @param numOfPastDays The number of past days from <code>periodEnd</code> to be considered. <code>periodEnd</code>
     *          is not counted.
     * @return The number of units abstained from.
     */
    public static int getSaved(Context context, Category category, int numOfPastDays, Date periodEnd) {
        Date periodStart;
        MeatDish[] requestedDishes;
        Calendar calendar = Calendar.getInstance();

        if (numOfPastDays < 0) { // Applies no time period
            return getSaved(context, category);
        }

        if (calendar.getTime().compareTo(periodEnd) < 0) { // periodEnd is in the future
            throw new IllegalArgumentException("Cannot apply a future time period");
        }

        calendar.setTime(periodEnd);
        calendar.add(Calendar.DAY_OF_YEAR, - numOfPastDays);
        periodStart = calendar.getTime(); // periodEnd - numOfDays

        requestedDishes = getMeatDishes(context, periodStart, periodEnd);

    	switch (category) {
    	case PORK:
    	case BEEF:
    	case POULTRY:
    	case SHEEP_GOAT:
    	case FISH:
    	    return getMeat(Meat.valueOf(category), requestedDishes);
    	case MEAT:
    	    return getTotalMeat(requestedDishes);
    	case CO2:
    	    return getTotalCarbonImpact(requestedDishes);
    	case WATER:
    	    return getTotalWaterImpact(requestedDishes);
    	case FEED:
    	    return getTotalFeedImpact(requestedDishes);
    	default:
    	    throw new IllegalStateException("Unsupported category '" + category + "'");
    	}
    }

    /**
     * Returns the current water impact of the specified sort of <code>meat</code>.
     * The water impact is defined as the amount of water in millilitres needed to produce a certain amount of a certain
     * sort of meat. The impact of fish production is not considered. Therefore, this method will always return 0 if called
     * with <code>Meat.FISH</code> as the sort of <code>meat</code>.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param meat The sort of meat.
     * @return The current water impact for <code>meat</code>.
     */
    public static int getWaterImpact(Context context, Meat meat) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        int impactFactor = IMPACT_WATER.get(meat);
        int waterImpact = databaseAccess.getMeatAmount(meat) * impactFactor;

        return waterImpact;
    }

    /**
     * Returns the current carbon impact of the specified sort of <code>meat</code>.
     * The carbon impact is defined as the amount of CO2 in milligrammes that goes into the atmosphere on producing
     * a certain amount of a certain sort of meat. The impact of fish production is not considered. Therefore, this method
     * will always return 0 if called with <code>Meat.FISH</code> as the sort of <code>meat</code>.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param meat The sort of meat.
     * @return The current water impact for <code>meat</code>.
     */
    public static int getCarbonImpact(Context context, Meat meat) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        int impactFactor = IMPACT_CARBON.get(meat);
        int carbonImpact = databaseAccess.getMeatAmount(meat) * impactFactor;

        return carbonImpact;
    }

    /**
     * Returns the current feed impact of the specified sort of <code>meat</code>.
     * The feed impact is defined as the amount of feed in milligrammes that is needed to produce
     * a certain amount of a certain sort of meat. The impact of fish production is not considered.
     * Therefore, this method will always return 0 if called with <code>Meat.FISH</code> as the sort of <code>meat</code>.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param meat The sort of meat.
     * @return The current feed impact for <code>meat</code>.
     */
    public static int getFeedImpact(Context context, Meat meat) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        int impactFactor = IMPACT_FEED.get(meat);
        int feedImpact = databaseAccess.getMeatAmount(meat) * impactFactor;

        return feedImpact;
    }

    /**
     * Returns the current feed energy impact of the specified sort of <code>meat</code>.
     * The feed energy impact is defined as the energy in kilo-calories of soy/corn that is needed to produce
     * a certain amount of a certain sort of meat. The impact of fish production is not considered.
     * Therefore, this method will always return 0 if called with <code>Meat.FISH</code> as the sort of <code>meat</code>.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param meat The sort of meat.
     * @return The current feed energy impact for <code>meat</code>.
     */
    private static float getFeedEnergyImpact(Context context, Meat meat) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        int impactFactor = IMPACT_FEED_ENERGY.get(meat);
        float feedEnergyImpact = (databaseAccess.getMeatAmount(meat) / 1000.0F) * impactFactor;

        return feedEnergyImpact;
    }

    /**
     * Returns the current land impact of the specified sort of <code>meat</code>.
     * The land impact is defined as the area of land in square metres that is needed to grow a certain amount of
     * vegetarian food, or the animal feed for a certain amount of a certain sort of meat, respectively.
     * Therefore, this method will always return 0 if called with <code>Meat.FISH</code> as the sort of <code>meat</code>.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param meat The sort of meat.
     * @return The current land impact for <code>meat</code>.
     */
    private static float getLandImpact(Context context, Meat meat) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        float impactFactor = IMPACT_LAND.get(meat);
        float landImpact = (databaseAccess.getMeatAmount(meat) / 1000.0F) * impactFactor;

        return landImpact;
    }

    /**
     * Returns the current total water impact.
     * The water impact is defined as the amount of water in millilitres needed to produce a certain amount of a certain
     * sort of meat.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @return The sum of all water impacts.
     */
    public static int getTotalWaterImpact(Context context) {
        int total = 0;

        for (Meat m : Meat.values()) {
    		total += Model.getWaterImpact(context, m);
        }

        return total;
    }

    /**
     * Returns the current total carbon impact.
     * The carbon impact is defined as the amount of CO2 in milligrammes that goes into the atmosphere on producing
     * a certain amount of a certain sort of meat.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @return The sum of current carbon impacts.
     */
    public static int getTotalCarbonImpact(Context context) {
        int total = 0;

        for (Meat m : Meat.values()) {
			total += Model.getCarbonImpact(context, m);
        }

        return total;
    }

    /**
     * Returns the current total feed impact.
     * The feed impact is defined as the amount of feed in milligrammes that is needed to produce
     * a certain amount of a certain sort of meat.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @return The sum of current feed impacts.
     */
    public static int getTotalFeedImpact(Context context) {
        int total = 0;

        for (Meat m : Meat.values()) {
			total += Model.getFeedImpact(context, m);
        }

        return total;
    }

    /**
     * Returns the current total land impact.
     * The land impact is defined as the area of land in square metres that is needed to grow a certain amount of
     * vegetarian food, or the animal feed for a certain amount of a certain sort of meat, respectively.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @return The sum of current land impacts.
     */
    public static float getTotalLandImpact(Context context) {
        float total = 0;

        for (Meat m : Meat.values()) {
            total += Model.getLandImpact(context, m);
        }

        return total;
    }

    /**
     * Returns the current total feed energy impact.
     * The feed energy impact is defined as the energy in kilo-calories of soy/corn needed to produce
     * a certain amount of a certain sort of meat.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @return The sum of current feed energy impacts.
     */
    @SuppressWarnings("WeakerAccess")
    public static float getTotalFeedEnergyImpact(Context context) {
        float total = 0;

        for (Meat m : Meat.values()) {
            total += Model.getFeedEnergyImpact(context, m);
        }

        return total;
    }

    /**
     * Returns all stored meat dishes that belong to the specified period of time.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     *
     * @param context To use to open or create the database.
     * @param periodStart The first day of the period (inclusive) or <code>null</code> if no start limit.
     * @param periodEnd The last day of the period (inclusive) or <code>null</code> if no end limit.
     * @return The stored meat dishes, ordered by date (latest first), or an empty array if none available.
     */
    @SuppressWarnings("WeakerAccess")
    public static MeatDish[] getMeatDishes(Context context, Date periodStart, Date periodEnd) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        
        return databaseAccess.getMeatDishes(periodStart, periodEnd);
    }

    /**
     * Returns a cursor on all stored meat dishes that belong to the specified period of time.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     * Use this method only if you want to apply a {@link CursorAdapter} onto the returned cursor.
     * In all other cases, a {@link Model#getMeatDishes(Context, Date, Date)} should be preferred.
     *
     * @param periodStart The first day of the period (inclusive) or <code>null</code> if no start limit.
     * @param periodEnd The last day of the period (inclusive) or <code>null</code> if no end limit.
     * @return A {@link Cursor} instance to iterate over all currently stored meat dishes,
     *          ordered by date (latest first).
     * @see Model#getMeatDishes(Context, Date, Date)
     */
    @SuppressWarnings("SameParameterValue")
    public static Cursor getMeatDishesCursor(Context context, Date periodStart, Date periodEnd) {
        /*
         * Actually, returning a Cursor instance violates the layered system structure.
         * However, activities displaying a dynamic list of elements will need the raw Cursor instance.
         */
        
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);

        return databaseAccess.getMeatDishesCursor(periodStart, periodEnd);
    }

    /**
     * Permanently stores the specified meat dish.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     * You can override <code>AsyncTask.onPreExecute()</code> to disable UI features and
     * <code>AsyncTask.onPostExecute()</code> to re-enable them and to display a status message (e.g., a {@link Toast}).
     *
     * @param context To use to open or create the database.
     * @param date The meat dish's date.
     * @param meat The meat dish's sort of meat.
     * @param amount The amount of meat in grammes.
     * @return <code>false</code> if an error occurred on storing the data.
     */
    public static boolean storeMeatDish(Context context, Date date, Meat meat, int amount) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);

        return databaseAccess.insert(date, meat, amount) >= 0;
    }

    /**
     * Removes the specified meat dishes.
     * Consider calling this method from within an {@link AsyncTask} to prevent the UI from freezing.
     * You can override <code>AsyncTask.onPreExecute()</code> to disable UI features and
     * <code>AsyncTask.onPostExecute()</code> to re-enable them and to display a status message
     * (e.g., as a {@link Toast}).
     *
     * @param context To use to open or create the database.
     * @param meatDishIds The meat dish IDs.
     */
    public static void deleteMeatDishes(Context context, Collection<Long> meatDishIds) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);

        databaseAccess.delete(meatDishIds);
    }

    /**
     * Returns the total carbon impact as a distance.
     * That is, it returns the distance in kilometres that can be covered when driving so that CO2 emissions equal
     * {@link freerunningapps.veggietizer.model.Model#getTotalCarbonImpact(android.content.Context)}.
     *
     * @param context To use to open or create the database.
     * @return The total carbon impact in kilometres.
     */
    public static float getTotalCarbonImpactInKilometres(Context context) {
        float carbonSaved = getTotalCarbonImpact(context) / 1000.0F;

        return carbonSaved / (float) AVERAGE_CO2_EMISSIONS;
    }

    /**
     * Returns the total carbon impact as an amount of <code>food</code> in kilogrammes.
     * That is, it returns the amount of <code>food</code> one can produce to match
     * {@link freerunningapps.veggietizer.model.Model#getTotalCarbonImpact(android.content.Context)}.
     *
     * @param context To use to open or create the database.
     * @param food The sort of food to consider.
     * @return The total carbon impact as an amount of <code>food</code> in kilogrammes or <code>0</code> if the sort of
     * <code>food</code> is not supported.
     */
    public static float getTotalCarbonImpactAsFood(Context context, VeggieFood food) {
        if (!IMPACT_CARBON.containsKey(food)) {
            return 0;
        }

        float carbonSaved = getTotalCarbonImpact(context) / 1000.0F;
        float impactFactor = IMPACT_CARBON.get(food);

        return carbonSaved / impactFactor;
    }

    /**
     * Returns the total water impact as a period of time.
     * That is, it returns the number of days after that a German would have directly consumed the total water saved
     * as given by {@link freerunningapps.veggietizer.model.Model#getTotalWaterImpact(android.content.Context)}.
     *
     * @param context To use to open or create the database.
     * @return The total water impact in litres.
     */
    public static float getTotalWaterImpactInDays(Context context) {
        float waterSaved = getTotalWaterImpact(context) / 1000.0F;

        return waterSaved / (float) DIRECT_DAILY_WATER_CONSUMPTION;
    }

    /**
     * Returns the total water impact as an amount of <code>food</code> in kilogrammes.
     * That is, it returns the amount of <code>food</code> one can produce to match
     * {@link freerunningapps.veggietizer.model.Model#getTotalCarbonImpact(android.content.Context)}.
     *
     * @param context To use to open or create the database.
     * @param food The sort of food to consider.
     * @return The total carbon impact as an amount of <code>food</code> in kilogrammes or <code>0</code> if the sort of
     * <code>food</code> is not supported.
     */
    public static float getTotalWaterImpactAsFood(Context context, VeggieFood food) {
        if (!IMPACT_WATER.containsKey(food)) {
            return 0;
        }

        float waterSaved = getTotalWaterImpact(context) / 1000.0F;
        float impactFactor = IMPACT_WATER.get(food);

        return waterSaved / impactFactor;
    }

    /**
     * Returns the total land impact as an amount of <code>food</code> in kilogrammes.
     * That is, it returns the amount of <code>food</code> one can produce to match
     * {@link freerunningapps.veggietizer.model.Model#getTotalLandImpact(android.content.Context)}.
     *
     * @param context To use to open or create the database.
     * @param food The sort of food to consider.
     * @return The total land impact as an amount of <code>food</code> in kilogrammes or <code>0</code> if the sort of
     * <code>food</code> is not supported.
     */
    @SuppressWarnings("SameParameterValue")
    public static float getTotalLandImpactAsFood(Context context, VeggieFood food) {
        if (!IMPACT_LAND.containsKey(food)) {
            return 0;
        }

        float landSaved = getTotalLandImpact(context);
        float impactFactor = IMPACT_LAND.get(food);

        return landSaved / impactFactor;
    }

    /**
     * Returns the total feed energy impact as a number of days.
     * That is, it returns how many days an adult could eat from the saved feed in terms of food energy.
     *
     * @param context To use to open or create the database.
     * @return The total feed energy impact as a number of days.
     * @see freerunningapps.veggietizer.model.Model#getTotalFeedEnergyImpact(android.content.Context)
     */
    public static float getTotalFeedEnergyImpactInDays(Context context) {
        float feedEnergySaved = getTotalFeedEnergyImpact(context);

        return feedEnergySaved / (float) DAILY_ENERGY_DEMAND;
    }

    /**
     * Returns how many days an adult could eat from the saved meat in terms of food energy.
     *
     * @param context To use to open or create the database.
     * @return The number of days.
     */
    public static float getMeatEnergyInDays(Context context) {
        float totalMeatEnergy = 0;

        for (Meat m : Meat.values()) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
            float amount = databaseAccess.getMeatAmount(m) / 1000.0F;

            totalMeatEnergy += amount * ENERGY_PER_KILOGRAMME.get(m);
        }

        return totalMeatEnergy / (float) DAILY_ENERGY_DEMAND;
    }
}
