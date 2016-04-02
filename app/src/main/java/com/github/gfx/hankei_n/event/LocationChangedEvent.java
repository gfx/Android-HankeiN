package com.github.gfx.hankei_n.event;

import com.google.android.gms.maps.model.LatLng;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationChangedEvent {

    public final LatLng location;

    public final boolean accurate;

    public LocationChangedEvent(LatLng latLng, boolean accurate) {
        location = latLng;
        this.accurate = accurate;
    }

    public LocationChangedEvent(double latitude, double longitude) {
        this(new LatLng(latitude, longitude), true);
    }
}
