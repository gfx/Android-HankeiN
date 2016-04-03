package com.github.gfx.hankei_n.debug;

import com.facebook.stetho.Stetho;

import android.content.Context;

public class StethoDelegator {

    public static void initialize(Context context) {
        if (runOnAndroid()) {
            Stetho.initializeWithDefaults(context);
        }
    }


    private static boolean runOnAndroid() {
        return System.getProperty("java.vm.name").equals("Dalvik");
    }
}
