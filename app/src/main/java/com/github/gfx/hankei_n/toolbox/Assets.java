package com.github.gfx.hankei_n.toolbox;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.util.DisplayMetrics;

import javax.inject.Inject;

public class Assets {

    @Inject
    DisplayMetrics displayMetrics;

    @Inject
    public Assets() {
    }

    @ColorInt
    public int hueToColor(float hue) {
        float[] hsv = {hue, 1.0f, 1.0f};
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public int makeDark(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public Drawable createMarkerDrawable(float hue) {
        int color = hueToColor(hue);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setStroke(dpToPx(1), makeDark(color, 0.7f));
        return drawable;
    }


    public int dpToPx(int dp) {
        return (int) (dp * displayMetrics.density);
    }

}
