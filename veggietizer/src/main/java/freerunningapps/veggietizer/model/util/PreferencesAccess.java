package freerunningapps.veggietizer.model.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides methods to access app preferences.
 *
 * @see android.content.SharedPreferences
 */
public final class PreferencesAccess {
    public static final String ACHIEVEMENT_PREFS = "freerunningapps.veggietizer.preferences.achievement";
    public static final String NOTIFICATION_PREFS = "freerunningapps.veggietizer.preferences.notification";

    public static final String KEY_DATE_LAST_APP_LAUNCH
            = "freerunningapps.veggietizer.notification.date_last_app_launch";
    public static final String KEY_DATE_REMINDER_LAST_SHOWN
            = "freerunningapps.veggietizer.notification.date_reminder_last_shown";

    @SuppressWarnings("SameParameterValue")
    public static void clearDate(Context context, String preferencesId, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, "");
        editor.apply();
    }

    /**
     * Stores a date the specified {@link SharedPreferences}.
     *
     * @param context The context.
     * @param preferencesId The ID of the SharedPreferences to use.
     * @param key The key to look up the date.
     * @param date The date to store.
     */
    public static void storeDate(Context context, String preferencesId, String key, Date date) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesId, Context.MODE_PRIVATE);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat(DateParser.DATE_PATTERN);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, dateFormat.format(date));
        editor.apply();
    }

    /**
     * Reads a date stored in the specified {@link SharedPreferences}.
     * The daytime is set to midnight (00:00:00:000 a.m.).
     *
     * @param context The context.
     * @param preferencesId The ID of the SharedPreferences to use.
     * @param key The key to look up the date.
     * @return The date or <code>null</code> if not available.
     */
    public static Date readDate(Context context, String preferencesId, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesId, Context.MODE_PRIVATE);
        String storedDate = sharedPref.getString(key, "");

        if(storedDate.equals(""))
            return null;
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateParser.parseISO2014(storedDate, null));

            return calendar.getTime();
        }
    }
}
