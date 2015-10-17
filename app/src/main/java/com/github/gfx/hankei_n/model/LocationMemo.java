package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.SerializedName;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemo implements Serializable, Comparable<LocationMemo> {

    @SerializedName("id")
    public final long id;

    @SerializedName("address")
    @NonNull
    public final String address;

    @SerializedName("note")
    public final String note;

    @SerializedName("location")
    public final LatLng location;

    public LocationMemo(long id, @NonNull String address, @NonNull String note, @NonNull LatLng location) {
        this.id = id;
        this.address = address;
        this.note = note;
        this.location = location;
    }

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location) {
        this(0, address, note, location);
    }

    public MarkerOptions buildMarkerOptions() {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

        return new MarkerOptions()
                .title(address)
                .snippet(note)
                .position(location)
                .icon(icon);
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

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id % Integer.MAX_VALUE);
    }

    @Override
    public int compareTo(LocationMemo another) {
        return another.address.compareTo(this.address);
    }
}
