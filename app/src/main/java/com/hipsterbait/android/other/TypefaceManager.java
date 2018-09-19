package com.hipsterbait.android.other;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.hipsterbait.android.R;

public class TypefaceManager {

    private static TypefaceManager singleton = null;

    private final static int ROBOTO_CONDENSED_REGULAR = 0;
    private final static int ROBOTO_CONDENSED_BOLD = 1;
    private final static int ROBOTO_CONDENSED_ITALIC = 2;
    private final static int ROBOTO_CONDENSED_LIGHT = 3;
    private final static int ROBOTO_CONDENSED_BOLDITALIC = 4;
    private final static int ROBOTO_CONDENSED_LIGHTITALIC = 5;

    private Typeface robotoCondensed = null;
    private Typeface robotoCondensedBold = null;
    private Typeface robotoCondensedItalic = null;
    private Typeface robotoCondensedLight = null;
    private Typeface robotoCondensedBoldItalic = null;
    private Typeface robotoCondensedLightItalic = null;

    public static TypefaceManager getInstance() {
        if (singleton == null) {
            singleton = new TypefaceManager();
        }
        return singleton;
    }

    public Typeface getTypeface(Context context, int value) {

        switch (value) {
            case ROBOTO_CONDENSED_REGULAR: default:
                if (robotoCondensed == null) {
                    robotoCondensed = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.roboto_condensed));
                }
                return robotoCondensed;

            case ROBOTO_CONDENSED_BOLD:
                if (robotoCondensedBold == null) {
                    robotoCondensedBold = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.roboto_condensed_bold));
                }
                return robotoCondensedBold;

            case ROBOTO_CONDENSED_ITALIC:
                if (robotoCondensedItalic == null) {
                    robotoCondensedItalic = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.roboto_condensed_italic));
                }
                return robotoCondensedItalic;

            case ROBOTO_CONDENSED_LIGHT:
                if (robotoCondensedLight == null) {
                    robotoCondensedLight = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.roboto_condensed_light));
                }
                return robotoCondensedLight;

            case ROBOTO_CONDENSED_BOLDITALIC:
                if (robotoCondensedBoldItalic == null) {
                    robotoCondensedBoldItalic = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.roboto_condensed_bolditalic));
                }
                return robotoCondensedBoldItalic;

            case ROBOTO_CONDENSED_LIGHTITALIC:
                if (robotoCondensedLightItalic == null) {
                    robotoCondensedLightItalic = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.roboto_condensed_lightitalic));
                }
                return robotoCondensedLightItalic;

        }
    }
}
