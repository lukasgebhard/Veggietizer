package freerunningapps.veggietizer.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.model.Achievement.Criterion;
import freerunningapps.veggietizer.model.enums.Category;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class AchievementSet {

	private static AchievementSet achievementSet;
	private Achievement[] achievements;

	private AchievementSet(Context context) {
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();

		achievements = new Achievement[] {
		        new Achievement(res.getString(R.string.ach_heading_0),
                        res.getString(R.string.ach_description_0),
                        res.getString(R.string.ach_share_text_0),
                        R.drawable.ic_umdenker_locked, R.drawable.ic_umdenker, context,
                        new Criterion(Category.MEAT, 10, -1, context, null)), // Der Umdenker
                new Achievement(res.getString(R.string.ach_heading_2), res.getString(R.string.ach_description_2),
                        res.getString(R.string.ach_share_text_2),
                        R.drawable.ic_schweinepriester_locked, R.drawable.ic_schweinepriester, context,
                        new Criterion(Category.PORK, 500, -1, context, null)), // Der Schweinepriester
                new Achievement(res.getString(R.string.ach_heading_16), res.getString(R.string.ach_description_16),
                        res.getString(R.string.ach_share_text_16),
                        R.drawable.ic_huehnerauge_locked, R.drawable.ic_huehnerauge, context,
                        new Criterion(Category.POULTRY, 300, -1, context, null)), // Hühnerauge
                new Achievement(res.getString(R.string.ach_heading_12), res.getString(R.string.ach_description_12),
                        res.getString(R.string.ach_share_text_12),
                        R.drawable.ic_wild_locked, R.drawable.ic_wild, context,
                        new Criterion(Category.MEAT, 500, 10, context, null)), // Wild entschlossen
                new Achievement(res.getString(R.string.ach_heading_8), res.getString(R.string.ach_description_8),
                        res.getString(R.string.ach_share_text_8),
                        R.drawable.ic_meerjungfrau_locked, R.drawable.ic_meerjungfrau, context,
                        new Criterion(Category.FISH, 400, -1, context, null)), // Die Meerjungfrau
                new Achievement(res.getString(R.string.ach_heading_1), res.getString(R.string.ach_description_1),
                        res.getString(R.string.ach_share_text_1),
                        R.drawable.ic_huehnerfreund_locked, R.drawable.ic_huehnerfreund, context,
                        new Criterion(Category.POULTRY, 1000, -1, context, null)), // Der Hühnerfreund
                new Achievement(res.getString(R.string.ach_heading_9), res.getString(R.string.ach_description_9),
                        res.getString(R.string.ach_share_text_9),
                        R.drawable.ic_lamm_locked, R.drawable.ic_lamm, context,
                        new Criterion(Category.SHEEP_GOAT, 150, -1, context, null)), // Das unschuldige Lamm
                new Achievement(res.getString(R.string.ach_heading_14), res.getString(R.string.ach_description_14),
                        res.getString(R.string.ach_share_text_14),
                        R.drawable.ic_kuhl_locked, R.drawable.ic_kuhl, context,
                        new Criterion(Category.BEEF, 1500, -1, context, null)), // Kuh-ler Typ
                new Achievement(res.getString(R.string.ach_heading_10), res.getString(R.string.ach_description_10),
                        res.getString(R.string.ach_share_text_10),
                        R.drawable.ic_bbq_locked, R.drawable.ic_bbq, context,
                        new Criterion(Category.BEEF, 150, 0, context,
                        new Criterion(Category.PORK, 100, 0, context, null))), // Der BBQ-Held
                new Achievement(res.getString(R.string.ach_heading_3), res.getString(R.string.ach_description_3),
                        res.getString(R.string.ach_share_text_3),
                        R.drawable.ic_doolittle_locked, R.drawable.ic_doolittle, context,
                        new Criterion(Category.BEEF, 20, -1, context,
                        new Criterion(Category.PORK, 20, -1, context,
                        new Criterion(Category.POULTRY, 20, -1, context,
                        new Criterion(Category.FISH, 20, -1, context, null))))), // Dr. Dolittle
                new Achievement(res.getString(R.string.ach_heading_5), res.getString(R.string.ach_description_5),
                        res.getString(R.string.ach_share_text_5),
                        R.drawable.ic_klimaretter_locked, R.drawable.ic_klimaretter, context,
                        new Criterion(Category.CO2, 25000000, -1, context, null)), // Der Klimaretter
		        new Achievement(res.getString(R.string.ach_heading_6), res.getString(R.string.ach_description_6),
                        res.getString(R.string.ach_share_text_6),
                        R.drawable.ic_vollkorn_locked, R.drawable.ic_vollkorn, context,
                        new Criterion(Category.FEED, 5000000, -1, context, null)), // Voll-Korn
		        new Achievement(res.getString(R.string.ach_heading_7), res.getString(R.string.ach_description_7),
                        res.getString(R.string.ach_share_text_7),
                        R.drawable.ic_bademeister_locked, R.drawable.ic_bademeister, context,
                        new Criterion(Category.WATER, 20000000, -1, context, null)), // Der Bademeister
                new Achievement(res.getString(R.string.ach_heading_11), res.getString(R.string.ach_description_11),
                        res.getString(R.string.ach_share_text_11),
                        R.drawable.ic_veggienator_locked, R.drawable.ic_veggienator, context,
                        new Criterion(Category.MEAT, 1000, 7, context, null)), // Der Veggienator
                new Achievement(res.getString(R.string.ach_heading_15), res.getString(R.string.ach_description_15),
                        res.getString(R.string.ach_share_text_15),
                        R.drawable.ic_satansbraten_locked, R.drawable.ic_satansbraten, context,
                        new Criterion(Category.BEEF, 800, -1, context,
                        new Criterion(Category.PORK, 800, -1, context,
                        new Criterion(Category.POULTRY, 400, -1, context, null)))), // Satansbraten
                new Achievement(res.getString(R.string.ach_heading_4), res.getString(R.string.ach_description_4),
                        res.getString(R.string.ach_share_text_4),
                        R.drawable.ic_saugut_locked, R.drawable.ic_saugut, context,
                        new Criterion(Category.PORK, 2500, -1, context, null)), // Sau-Gut
                new Achievement(res.getString(R.string.ach_heading_13), res.getString(R.string.ach_description_13),
                        res.getString(R.string.ach_share_text_13),
                        R.drawable.ic_wurstsalat_locked, R.drawable.ic_wurstsalat, context,
                        new Criterion(Category.MEAT, 3500, -1, context,
                        new Criterion(Category.WATER, 3500000, 5, context,
                        new Criterion(Category.FEED, 3000000, 10, context, null)))),  // Wurst? Salat!
                new Achievement(res.getString(R.string.ach_heading_17), res.getString(R.string.ach_description_17),
                        res.getString(R.string.ach_share_text_17),
                        R.drawable.ic_vegetarier_locked, R.drawable.ic_vegetarier, context,
                        new Criterion(Category.MEAT, 15000, 90, context, null)) // Der Vegetarier
		};

		this.checkAchievements();
	}

	/**
	 * Checks all achievements if achieved
	 */
	public void checkAchievements() {
		for (Achievement a : achievements) {
			a.checkAchievement();
		}
	}

	/**
	 * Returns all achievements in an array
	 * @return All achievements
	 */
	public Achievement[] getAchievements() {
		return achievements;
	}

	/**
     * Returns the achievements at arrary-position index
     * @return the achievement
     */
    public Achievement getAchievement(int index) {
        return achievements[index];
    }

	/**
	 * Checks all locked achievements if achieved and returns a list of all newly unlocked achievements.
     *
     * @return The achievements that have just been unlocked.
	 */
	public List<Achievement> checkAchievementsAfterInsert() {
		ArrayList<Achievement> newAchievements = new ArrayList<>();

		for (Achievement a : achievements) {
			if (!a.isUnlocked())
				if (a.checkAchievement())
					newAchievements.add(a);
		}
		return newAchievements;
	}

	/**
	 * Checks if the achievement at index is unlocked
	 * @param index The index of the achievement to check
	 * @return true if unlocked
	 */
	public boolean isUnlocked(int index)
	  throws IndexOutOfBoundsException {
		return achievements[index].checkAchievement();
	}

	/**
	 * Returns the description of the achievement at the given index
	 * @param index index of the achievement
	 * @return The description of the achievement
	 */
	@SuppressWarnings("unused")
    public String getDescription(int index) {
		return achievements[index].getDescription();
	}

	public static AchievementSet getInstance(Context context) {
		if(achievementSet == null)
			achievementSet = new AchievementSet(context);
		
        return achievementSet;
    }

}
