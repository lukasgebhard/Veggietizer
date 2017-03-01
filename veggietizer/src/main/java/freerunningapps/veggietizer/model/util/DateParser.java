package freerunningapps.veggietizer.model.util;

import java.util.Calendar;
import java.util.Date;

import freerunningapps.veggietizer.BuildConfig;

/**
 * A utility class to parse dates.
 */
public final class DateParser {
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Parses a <code>date</code> string.
     * If the string is equal to <code>today</code>, today's date is returned.
     * Otherwise, the string is assumed to be formatted as YYYY-MM-DD (ISO-2014).
     * The daytime is set to midnight (00:00:00:000 a.m.).
     *
     * @param date The date string to parse.
     * @param today A string representing today or <code>null</code> if to be ignored.
     */
    public static Date parseISO2014(String date, String today) {
        if (today != null && date.equals(today)) {
            return Calendar.getInstance().getTime();
        }

        if (BuildConfig.DEBUG && date.length() != 10
                || date.charAt(4) != '-'
                || date.charAt(7) != '-') {
            throw new AssertionError(date + " does not specify a valid date");
        }

        String yearStr = date.substring(0, 4);
        String monthStr = date.substring(5, 7);
        String dayStr = date.substring(8);
        return parse(date, yearStr, monthStr, dayStr);
    }

    /**
     * Parses a <code>date</code> string.
     * If the string is equal to <code>today</code>, today's date is returned.
     * The string is assumed to be formatted as DD.MM.YYYY (German format).
     * The daytime is set to midnight (00:00:00:000 a.m.).
     *
     * @param date The date string to parse.
     *      * @param today A string representing today or <code>null</code> if to be ignored.
     * @return The parsed date instance.
     */
    public static Date parseDE(String date, String today) {
        if (today != null && date.equals(today)) {
            return Calendar.getInstance().getTime();
        }

        if (BuildConfig.DEBUG && date.length() != 10
                || date.charAt(2) != '.'
                || date.charAt(5) != '.') {
            throw new AssertionError(date + " does not specify a valid date");
        }

        String dayStr = date.substring(0, 2);
        String monthStr = date.substring(3, 5);
        String yearStr = date.substring(6);
        return parse(date, yearStr, monthStr, dayStr);
    }

    private static Date parse(String date, String yearStr, String monthStr, String dayStr) {
        int year;
        int month;
        int day;
        Calendar c = Calendar.getInstance();

        try {
            year = Integer.valueOf(yearStr);
            month = Integer.valueOf(monthStr);
            day = Integer.valueOf(dayStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(date + " does not specify a valid date", e);
        }

        --month;

        if (BuildConfig.DEBUG && (year < 0
                || month < Calendar.JANUARY || month > Calendar.DECEMBER
                || day < 0 || day > 31)) {
            throw new AssertionError(date + " does not specify a valid date");
        }

        c.set(year, month, day);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);

        return c.getTime();
    }
}
