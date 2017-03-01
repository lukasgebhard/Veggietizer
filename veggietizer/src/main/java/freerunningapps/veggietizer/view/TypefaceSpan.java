package freerunningapps.veggietizer.view;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.text.Spannable;
import freerunningapps.veggietizer.controller.FontManager;

/**
 * A {@link Spannable} with a custom {@link Typeface}.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
@SuppressWarnings("WeakerAccess")
public class TypefaceSpan extends MetricAffectingSpan {
    private Typeface mTypeface;

    /**
     * Loads the {@link Typeface} and apply to a {@link Spannable}.
     */
    public TypefaceSpan(FontManager.Font font) {
        mTypeface = FontManager.getInstance().getFont(font);
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
