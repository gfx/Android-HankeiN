package com.github.gfx.hankei_n.event;

import com.google.android.gms.maps.model.LatLng;

public class MyLocationChangedEvent {

    public final LatLng location;

    public MyLocationChangedEvent(LatLng latLng) {
        location = latLng;
    }
}
