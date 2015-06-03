package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemo implements Serializable, Comparable<LocationMemo> {

    @NonNull
    public final String address; // primary key

    public final String note;

    public final LatLng location;

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location) {
        this.address = address;
        this.note = note;
        this.location = location;
    }

    public MarkerOptions buildMarkerOptions() {
        return new MarkerOptions()
                .title(address)
                .position(location);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocationMemo that = (LocationMemo) o;

        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public int compareTo(LocationMemo another) {
        return another.address.compareTo(this.address);
    }
}
