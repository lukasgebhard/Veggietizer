package freerunningapps.veggietizer.view;


import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.Model;
import freerunningapps.veggietizer.model.enums.Category;
import freerunningapps.veggietizer.model.util.Formatter;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public final class Utility {
    /**
     * Returns the total amount of savings of the specified <code>category</code>.
     *
     * @param context The context.
     * @param numberOfDecimalsWeight The number of decimals to use for weight amounts.
     * @param numberOfDecimalsVolume The number of decimals to use for volume amounts.
     * @param category The category of savings.
     * @return The total amount.
     */
    @SuppressWarnings("SameParameterValue")
    public static String getSavingsAmount(Context context, int numberOfDecimalsWeight,
                                          int numberOfDecimalsVolume, Category category) {
        Resources res = context.getResources();
        String kilogrammes = res.getString(R.string.unitKilogrammes);
        String grammes = res.getString(R.string.unitGrammes);
        String millilitres = res.getString(R.string.unitMillilitres);
        String litres = res.getString(R.string.unitLitres);
        String savingsAmount;

        switch (category) {
            case CO2:
                savingsAmount = Formatter.format(Model.getTotalCarbonImpact(context)
                                / Formatter.KILO, grammes, kilogrammes,
                        Formatter.KILO, numberOfDecimalsWeight);
                break;
            case WATER:
                savingsAmount = Formatter.format(Model.getTotalWaterImpact(context),
                        millilitres, litres, Formatter.KILO, numberOfDecimalsVolume);
                break;
            case FEED:
                savingsAmount = Formatter.format(Model.getTotalFeedImpact(context)
                                / Formatter.KILO, grammes, kilogrammes,
                        Formatter.KILO, numberOfDecimalsWeight);
                break;
            case MEAT:
                savingsAmount = Formatter.format(Model.getMeatSavedTotal(context),
                        grammes, kilogrammes,
                        Formatter.KILO, numberOfDecimalsWeight);
                break;
            default:
                throw new IllegalStateException("Unsupported category " + category);
        }

        return savingsAmount;
    }

    /**
     * Sets <code>font</code> as the font of each TextView from <code>views</code>.
     *
     * @param font The font to use.
     * @param views The views to style.
     */
    @SuppressWarnings("SameParameterValue")
    public static void setFont(FontManager.Font font, TextView[] views) {
        for (TextView v : views) {
            v.setTypeface(FontManager.getInstance().getFont(font));
        }
    }

    /**
     * Sets the action bar title using the specified font.
     *
     * @param font The font to use.
     * @param title The title to set.
     */
    @SuppressWarnings("SameParameterValue")
    public static void styleActionBar(ActionBar actionBar, FontManager.Font font, String title) {
        SpannableString actionBarTitle = new SpannableString(title);

        // ActionBar font
        actionBarTitle.setSpan(new TypefaceSpan(font),
                0, actionBarTitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(actionBarTitle);
    }
}
