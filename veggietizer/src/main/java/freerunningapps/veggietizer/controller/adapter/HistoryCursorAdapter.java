package freerunningapps.veggietizer.controller.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.enums.Meat;
import freerunningapps.veggietizer.model.util.DateParser;
import freerunningapps.veggietizer.model.util.Formatter;
import freerunningapps.veggietizer.view.activity.HistoryActivity;
import freerunningapps.veggietizer.model.database.DatabaseContract.MeatDishContract;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A cursor adapter to be used by the {@link HistoryActivity}.
 * <p />
 * Note that the history activity's list adapter could not use a {@link ViewBinder} instead.
 * This is because a ViewBinder binds cursor columns to views as one-to-one.
 * However, here the {@link MeatDishContract#COLUMN_NAME_SORT_OF_MEAT} column
 * is used both by a list entry's icon and the text field that displays the sort of meat.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class HistoryCursorAdapter extends SimpleCursorAdapter {
    private Context context;
    private Meat curMeat;
    private ImageView curImageView;

    /**
     * Cached icons.
     */
    private Map<Meat, Bitmap> icons;

    /**
     * <code>true</code> if the current ImageView has not been initialised yet.
     */
    private boolean isCurImageViewNew;

    /**
     * {@inheritDoc}
     *
     * @param context The {@link HistoryActivity}.
     */
    public HistoryCursorAdapter(Context context, String[] from, int[] to) {
        super(context, R.layout.component_history_entry, null, from, to, 0);

        this.context = context;
        curMeat = null;
        curImageView = null;
        isCurImageViewNew = true;

        icons = new HashMap<>(5);
        icons.put(Meat.BEEF, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cow_big));
        icons.put(Meat.PORK, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pig_big));
        icons.put(Meat.POULTRY, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_rooster_big));
        icons.put(Meat.SHEEP_GOAT, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_sheep_big));
        icons.put(Meat.FISH, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_fish_big));
    }

    @Override
    public void setViewImage(ImageView imageView, String meatDishId) {
        setMeatDishId(imageView, meatDishId);

        /*
         * If this is called before setMeatView(), the curMeat field has to be updated before the ImageView can be
         * initialised. So setMeatView() should initialise the ImageView.
         */
        if (isCurImageViewNew) {
            isCurImageViewNew = false;
            curImageView = imageView;
        }

        // Initialises the ImageView.
        else {
            setImageView(imageView);
        }
    }

    @Override
    public void setViewText(TextView textView, String text) {
        //Set font
        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        textView.setTypeface(font);

        int id = textView.getId();

        if (id == R.id.textView_history_date) {
            setDateView(textView, text);
        } else if (id == R.id.textView_history_meat) {
            setMeatView(textView, text);
        } else if (id == R.id.textView_history_amount) {
            setAmountView(textView, text);
        }
    }

    private void setAmountView(TextView amountView, String amount) {
        Resources res = context.getResources();

        amountView.setText(Formatter.format(Integer.valueOf(amount),
                res.getString(R.string.unitGrammes),
                res.getString(R.string.unitKilogrammes),
                Formatter.KILO, 2));
    }

    private void setMeatView(TextView meatView, String sortOfMeat) {
        Meat meat = Meat.valueOf(sortOfMeat);
        String localisedMeat = Meat.toString(context.getApplicationContext(), meat);

        meatView.setText(localisedMeat);

        curMeat = meat;

        /*
         * If this is called before setViewImage(), setViewImage() can initialise the ImageView using the
         * updated field curMeat.
         */
        if (isCurImageViewNew) {
            isCurImageViewNew = false;
        } else { // setViewImage() could not do the work. So it has to be done now.
            setImageView(curImageView);
        }
    }

    private void setDateView(TextView dateView, String dateStr) {
        String today = context.getResources().getString(R.string.today);
        Date date = DateParser.parseISO2014(dateStr, today);
        String dateFormatStr = context.getResources().getString(R.string.date_format);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        String localisedDate = dateFormat.format(date);

        dateView.setText(localisedDate);
    }

    private void setImageView(ImageView imageView) {
        Bitmap icon;
        BitmapDrawable icDraw;
        int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
        int orientation = context.getResources().getConfiguration().orientation;
        int iconHeight = orientation == Configuration.ORIENTATION_PORTRAIT ? displayHeight / 13 : displayHeight / 8;
        @SuppressWarnings("SuspiciousNameCombination") int iconWidth = iconHeight;

        //Sets maximum icon height
        int maxIconHeight = context.getResources().getDimensionPixelSize(R.dimen.historyMaxIconSize);
        if (iconHeight > maxIconHeight) {
            iconHeight = maxIconHeight;
        }

        int icPaddingRight = orientation == Configuration.ORIENTATION_PORTRAIT ? iconHeight / 3 : iconHeight / 2;
        int icPaddingBottom = iconHeight / 10;
        int icPaddingLeft = 1;
        int icPaddingTop = iconHeight / 10;

        icon = Bitmap.createScaledBitmap(icons.get(curMeat), iconWidth, iconHeight, false);
        icDraw = new BitmapDrawable(context.getResources(), icon);
        imageView.setImageDrawable(icDraw);
        imageView.setPadding(icPaddingLeft, icPaddingTop, icPaddingRight, icPaddingBottom);

        isCurImageViewNew = true;
    }

    /**
     * Stores the meat dish's ID as the ImageView's content description in the list entry.
     * The ID is needed if the user decides to delete the meat dish later on.
     *
     * @param imageView The ImageView.
     * @param meatDishId The meat dish's ID.
     */
    private void setMeatDishId(ImageView imageView, String meatDishId) {
        imageView.setContentDescription(String.valueOf(meatDishId));
    }
}
