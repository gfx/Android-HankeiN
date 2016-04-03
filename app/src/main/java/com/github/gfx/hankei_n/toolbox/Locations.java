package com.github.gfx.hankei_n.toolbox;

import com.google.android.gms.maps.model.LatLng;

import android.support.annotation.NonNull;

public class Locations {

    public static final LatLng NOWHERE = new LatLng(0, 0);

    public static boolean isSomewhere(double latitude, double longitude) {
        return latitude > 0 && longitude > 0;
    }

    public static boolean isNowhere(double latitude, double longitude) {
        return !isSomewhere(latitude, longitude);
    }

    public static boolean isSomewhere(@NonNull LatLng latLng) {
        return isSomewhere(latLng.latitude, latLng.longitude);
    }

    public static boolean isNowhere(@NonNull LatLng latLng) {
        return !isSomewhere(latLng);
    }
}
