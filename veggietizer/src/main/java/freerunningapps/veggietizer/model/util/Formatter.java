package freerunningapps.veggietizer.model.util;

import android.content.Context;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * A utility class to format strings.
 */
public final class Formatter {
    public static final float KILO = 1000.0f;
    public static final float CENTI = 100.f;
    private static final String PATTERN_BASE = "###,###,###";

    /**
     * Formats an <code>amount</code> of a certain unit <code>lowerUnit</code> as a string.
     * <br /><br />
     * If <code>numberOfDecimals >= 0</code>, the amount is rounded to <code>numberOfDecimals</code>
     * decimals with respect to <code>greaterUnit</code>. If <code>amount >= conversionFactor</code>, the amount
     * is represented with exactly <code>numberOfDecimals</code> decimals, else no decimals are used. <br />
     * If <code>numberOfDecimals < 0</code>, the amount is not rounded
     * and represented with at most two decimals (only if necessary). <br /><br />
     * If the <code>amount < conversionFactor</code>, it is represented with respect to
     * <code>lowerUnit</code>. Otherwise, the amount is converted to <code>greaterUnit</code> with respect to
     * <code>conversionFactor</code>.
     * <br /><br />
     * Examples: <br />
     * For <code>numberOfDecimals = 2, </code>
     * <code>153.4 g</code> will be formatted as <code>150 g</code>, <br />
     * <code>12.3456 kg</code> will be formatted as <code>12.35 kg</code>
     *
     * @param amount The amount to represent.
     * @param lowerUnit The amount's unit.
     * @param greaterUnit The amount's unit after multiplying with <code>conversionFactor</code>.
     * @param conversionFactor The ratio <code>greaterUnit / lowerUnit</code>
     * @param numberOfDecimals The rounding accuracy and number of decimals to use with respect to
     *                         <code>greaterUnit</code>. If negative, no rounding is done and at most two decimals are
     *                         used.
     * @return The string representation.
     */
    public static String format(float amount, String lowerUnit, String greaterUnit, float conversionFactor,
                                int numberOfDecimals) {
        String unit = lowerUnit;
        String pattern = PATTERN_BASE;

        if (numberOfDecimals < 0) { // Rounding mode off
            pattern += ".##"; // At most two decimals

            if (amount >= conversionFactor) {
                // Represents the amount with respect to the greater unit.
                amount /= conversionFactor;
                unit = greaterUnit;
            }
        } else {
            // Rounding mode needs amount first to be represented with respect to the greater unit to work.
            amount /= conversionFactor;

            float k = (float) Math.pow(10, numberOfDecimals);
            amount = Math.round(amount * k) / k;

            // Redoes conversion to greater unit.
            amount *= conversionFactor;

            if (amount >= conversionFactor) {
                // Represents the amount with respect to the greater unit.
                amount /= conversionFactor;
                unit = greaterUnit;

                // Sets the fixed number of decimals
                StringBuilder builder = new StringBuilder(PATTERN_BASE);
                if (numberOfDecimals > 0) builder.append(".");
                for (int i = 0; i < numberOfDecimals; ++i) {
                    builder.append("0");
                }
                pattern = builder.toString();
            }
        }

        DecimalFormat format = new DecimalFormat(pattern);
        return format.format(amount) + '\u00A0' + unit;
    }

    /**
     * Rounds <code>days</code> to <code>numDecimals</code> decimals and appends the pluralised "days" string.
     *
     * @param days The number of days to format.
     * @param context The application context.
     * @param quantityId The ID of the pluralised "days" string.
     * @param numDecimals The maximum number of decimals to use. If more would be needed, the days will be rounded.
     * @return The string representation.
     */
    public static String formatDays(float days, Context context, int quantityId, int numDecimals) {
        StringBuilder builder = new StringBuilder(PATTERN_BASE + ".");
        for (int i = 0; i < numDecimals; ++i) {
            builder.append("#");
        }
        DecimalFormat decimalFormat = new DecimalFormat(builder.toString());
        int quantity = Math.round(days * 10.0f) == 10 ? 1 : 2;

        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(days) + '\u00A0' + context.getResources().getQuantityText(quantityId, quantity);
    }
}
