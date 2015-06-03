package com.github.gfx.hankei_n.event;

import com.google.android.gms.maps.model.LatLng;

public class LocationChangedEvent {

    public final LatLng location;

    public LocationChangedEvent(LatLng latLng) {
        location = latLng;
    }

    public LocationChangedEvent(double latitude, double longitude) {
        location = new LatLng(latitude, longitude);
    }
}
