package com.github.gfx.hankei_n.event;

import com.google.android.gms.maps.model.LatLng;

public class MyLocationChanged {

    public final LatLng location;

    public MyLocationChanged(LatLng latLng) {
        location = latLng;
    }
}
