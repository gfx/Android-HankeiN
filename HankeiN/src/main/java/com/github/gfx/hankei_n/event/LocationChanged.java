package com.github.gfx.hankei_n.event;

import com.google.android.gms.maps.model.LatLng;

public class LocationChanged {

    public final LatLng location;

    public LocationChanged(LatLng latLng) {
        location = latLng;
    }

    public LocationChanged(double latitude, double longitude) {
        location = new LatLng(latitude, longitude);
    }
}
