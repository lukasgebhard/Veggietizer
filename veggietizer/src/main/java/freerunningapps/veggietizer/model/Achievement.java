package freerunningapps.veggietizer.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.model.enums.Category;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.model.util.PreferencesAccess;

import java.util.Calendar;
import java.util.Date;

/**
 * An achievement.
 *
 * @author Matthias Heim <freerunningapps@gmail.com>
 */
public class Achievement implements Parcelable {
	private String description,  heading, shareText;
	private int iconLockedID, iconUnlockedID;
	private Criterion criterion;
	private Context context;

    public static final Parcelable.Creator<Achievement> CREATOR = new Parcelable.Creator<Achievement>() {
        public Achievement createFromParcel(Parcel in) {
            return new Achievement(in);
        }

        public Achievement[] newArray(int size) {
            return new Achievement[size];
        }
    };

    private Achievement(Parcel in) {
        String[] strings = in.createStringArray();
        int[] ints = in.createIntArray();

        description = strings[0];
        heading = strings[1];
        shareText = strings[2];
        iconLockedID = ints[0];
        iconUnlockedID = ints[1];
        criterion = in.readParcelable(Criterion.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(new String[]{description, heading, shareText});
        out.writeIntArray(new int[]{iconLockedID, iconUnlockedID});
        out.writeParcelable(criterion, flags);
    }

	/**
	 * An achievement to unlock by matching the inherited criterion.
     *
     * @param heading The heading.
     * @param description The description.
     * @param shareText The text used when sharing this achievement.
     * @param iconLockedID The ID of the icon to be displayed if the achievement is locked.
     * @param iconUnlockedID The ID of the icon to be displayed if the achievement is unlocked.
     * @param context The application context.
     * @param criterion The criterion to meet to unlock this achievement.
	 */
	public Achievement(String heading, String description, String shareText, int iconLockedID, int iconUnlockedID,
                       Context context, Criterion criterion) {

		this.heading = heading;
		this.description = description;
        this.shareText = shareText;
		this.iconLockedID = iconLockedID;
		this.iconUnlockedID = iconUnlockedID;
		this.criterion = criterion;
		this.context = context;
	}

    public void setContext(Context context) {
        this.context = context;
        criterion.setContext(context);
    }

	/**
	 * Checks if the achievement is unlocked.
	 *
	 * @return <coce>true</coce> if unlocked.
	 */
	public boolean checkAchievement() {
	    Date unlockDate = PreferencesAccess.readDate(context, PreferencesAccess.ACHIEVEMENT_PREFS, heading);
	    if(unlockDate == null) {
	        //Check achievement from today
    		if(criterion.checkCriterion(Calendar.getInstance().getTime())) {
    		    PreferencesAccess.storeDate(context, PreferencesAccess.ACHIEVEMENT_PREFS, heading,
                        Calendar.getInstance().getTime());
    		    return true;
    		}
    		else
    		    return false;
	    }
	    else {
	        if(criterion.checkCriterion(unlockDate)) {
                return true;
            }
            else {
                PreferencesAccess.clearDate(context, PreferencesAccess.ACHIEVEMENT_PREFS, heading);
                return false;
            }
	    }
	}
	
	/**
	 * Reads if an unlock date is set, and returns true if so
	 * @return true if unlocked
	 */
	public boolean isUnlocked() {
	    return PreferencesAccess.readDate(context, PreferencesAccess.ACHIEVEMENT_PREFS, heading) != null;
	}

    @Override
	public String toString() {
		return heading;
	}

	/**
	 * @return the heading
	 */
	public String getHeading() {
		return heading;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Builds a string like that: 300g Rindfleisch (innerhalb der letzten 10 Tage)
     * @return the requirements
     */
    private String getRequirements(Criterion crit) {
        Resources res = crit.getContext().getResources();
        String req = "";
        req += "\u2022 " + crit.getAmountAndCategory() + " ";

        if(crit.getDays() == 1) {
            req += res.getString(R.string.achievement_today_and_yesterday);
        }
        else if(crit.getDays() > 0) {
            req += String.format(res.getString(R.string.achievement_recent_days), crit.getDays());
        }
        else if(crit.getDays() == 0) {
            req += res.getString(R.string.achievement_today);
        }
        
        req += '\n';

        if(crit.getNextCriterion() != null)
            req += getRequirements(crit.getNextCriterion());

        return req;
    }

    /**
     * Returns all criterion as string nicely formatted for the achievements popup
     *
     * @return the requirements as nice string
     */
    public String getRequirements() {
        return getRequirements(criterion);
    }

	/**
	 * @return the iconLockedID
	 */
	public int getIconLockedID() {
		return iconLockedID;
	}

	/**
	 * @return the iconUnlockedID
	 */
	public int getIconUnlockedID() {
		return iconUnlockedID;
	}

    public String getShareText() {
        return shareText;
    }

    /**
     * Class for describing the criterion to match to unlock an achievement
     *
     */
    public static class Criterion implements Parcelable {
    	private Category category;
    	private int amount;
    	private int duration;
    	private Criterion nextCriterion;
		private Context context;

        public static final Parcelable.Creator<Criterion> CREATOR = new Parcelable.Creator<Criterion>() {
            public Criterion createFromParcel(Parcel in) {
                return new Criterion(in);
            }
            public Criterion[] newArray(int size) {
                return new Criterion[size];
            }
        };

        private Criterion(Parcel in) {
            int[] ints = in.createIntArray();

            amount = ints[0];
            duration = ints[1];
            category = Category.values()[ints[2]];
            nextCriterion = in.readParcelable(Criterion.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeIntArray(new int[] {amount, duration, category.ordinal()});
            out.writeParcelable(nextCriterion, flags);
        }

    	/**
    	 * Constructor for a criterion
    	 * The units for amount depend on the category:
    	 *     All meat category's: gram
    	 *     Water: millilitres
    	 *     CO2, Feed: milligram
         *
    	 * @param amount The minimum amount the criterion has to match
    	 * @param duration Check just this amount of days into the past, 0 for today, negative number means not considered
    	 * @param context The applications context
    	 * @param nextCriterion The next additional criterion, null if last criterion
    	 */
    	public Criterion(Category category, int amount, int duration, Context context,
                         Criterion nextCriterion) {
    		this.category = category;
    		this.amount = amount;
    		this.duration = duration;
    		this.nextCriterion = nextCriterion;
    		this.context = context;
    	}

    	public Criterion getNextCriterion() {
            return nextCriterion;
        }

		public void setContext(Context context) {
			this.context = context;

            if (nextCriterion != null) {
                nextCriterion.setContext(context);
            }
		}

        public int getDays() {
            return duration;
        }

        public Context getContext() {
            return context;
        }

        /**
         * Returns a string containing the amount and category.
         *
         * @return The string.
         */
        public String getAmountAndCategory() {
    	    String requirement;
    	    String kg = context.getResources().getString(R.string.unitKilogrammes);
    	    String g = context.getResources().getString(R.string.unitGrammes);
    	    String l = context.getResources().getString(R.string.unitLitres);
    	    String ml = context.getResources().getString(R.string.unitMillilitres);

            switch (category) {
            case BEEF:
            case PORK:
            case POULTRY:
            case SHEEP_GOAT:
            case FISH:
            case MEAT:
                requirement = Formatter.format(amount, g, kg, Formatter.KILO, -1);
                break;
            case CO2:
            case FEED:
                requirement = Formatter.format(amount / Formatter.KILO, g, kg, Formatter.KILO, -1);
                break;
            case WATER:
                requirement = Formatter.format(amount, ml, l, Formatter.KILO, -1);
                break;
            default:
                throw new IllegalStateException("Unsupported category '" + category + "'");
            }
            //Category
            requirement += " " + Category.toString(context, category);

            return requirement;
        }

        /**
    	 * Checks all criterion recursively.
         *
         * @param unlockDate The date at which the achievement could be unlocked.
    	 * @return <code>true</code> if all criterion are matched.
    	 */
    	public boolean checkCriterion(Date unlockDate) {
            return Model.getSaved(context, category, duration, unlockDate) >= amount
                    && (nextCriterion == null || nextCriterion.checkCriterion(unlockDate));
        }
    }
}
