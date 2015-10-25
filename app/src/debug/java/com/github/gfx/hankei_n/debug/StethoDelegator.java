package com.github.gfx.hankei_n.debug;

import com.facebook.stetho.Stetho;

import android.content.Context;

public class StethoDelegator {
    public static void initialize(Context context) {
        Stetho.initializeWithDefaults(context);
    }
}
