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
    public long id;

    @SerializedName("address")
    @NonNull
    public String address;

    @SerializedName("note")
    public String note;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("radius")
    public double radius;

    @SerializedName("marker_hue")
    public double markerHue;

    public LocationMemo(long id, @NonNull String address, @NonNull String note, @NonNull LatLng location) {
        this.id = id;
        this.address = address;
        this.note = note;
        this.latitude = location.latitude;
        this.longitude = location.longitude;

        this.radius = 1.5;
        this.markerHue = BitmapDescriptorFactory.HUE_GREEN;
    }

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location) {
        this(0, address, note, location);
    }

    public LatLng buildLocation() {
        return new LatLng(latitude, longitude);
    }

    public MarkerOptions buildMarkerOptions() {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker((float) markerHue);

        return new MarkerOptions()
                .title(address)
                .snippet(note)
                .position(buildLocation())
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
