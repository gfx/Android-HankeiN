package com.github.gfx.hankei_n.toolbox;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class RuntimePermissions {

    static final int REQUEST_CODE_PERMISSIONS = 1000;

    public static final String[] LOCATIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    final Activity activity;

    public RuntimePermissions(Activity activity) {
        this.activity = activity;
    }

    public void confirm() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, LOCATIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
}
