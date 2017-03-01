package freerunningapps.veggietizer.controller;

import android.content.Context;
import android.graphics.Typeface;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A cache for custom fonts.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class FontManager implements Serializable {
    public enum Font { ROBOTO_LIGHT }

    private static final String PATH_FONT_ROBOTO_LIGHT = "fonts/Roboto-Light.ttf";

    private HashMap<Font, Typeface> cache;
    private HashMap<Font, String> fontPaths;
    private static FontManager instance = null;

    private static final int SIZE = 3;

    private FontManager() {
        cache = new HashMap<>(SIZE);
        fontPaths = new HashMap<>(SIZE);

        fontPaths.put(Font.ROBOTO_LIGHT, PATH_FONT_ROBOTO_LIGHT);
    }

    /**
     * @return A singleton.
     */
    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    /**
     * Caches the specified font.
     * <p />
     * If it is already cached, nothing is done.
     *
     * @param context The context.
     * @param font The font to cache.
     * @return The FontManager instance.
     */
    @SuppressWarnings("SameParameterValue")
    public FontManager cacheFont(Context context, Font font) {
        if (cache.get(font) == null) {
            cache.put(font, Typeface.createFromAsset(context.getAssets(), fontPaths.get(font)));
        }

        return instance;
    }

    public Typeface getFont(Font font) {
        return cache.get(font);
    }
}
