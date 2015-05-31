package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.annotation.NonNull;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemo implements Serializable, Comparable<LocationMemo> {

    long order = 0;

    LatLng location;

    String address;

    String note;

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location) {
        this.location = location;
        this.address = address;
        this.note = note;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public MarkerOptions buildMarkerOptions() {
        return new MarkerOptions()
                .title(address)
                .position(location);
    }

    @Override
    public int compareTo(LocationMemo another) {
        if (this.order != another.order) {
            return this.order > another.order ? -1 : 1;
        }

        return another.address.compareTo(this.address);
    }
}
