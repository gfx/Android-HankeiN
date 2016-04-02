package com.github.gfx.hankei_n.toolbox;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;

import javax.inject.Inject;

public class Colors {

    @Inject
    public Colors() {
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
}
