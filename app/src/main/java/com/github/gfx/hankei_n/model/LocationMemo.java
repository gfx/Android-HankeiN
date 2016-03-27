package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.SerializedName;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemo implements Serializable, Comparable<LocationMemo> {

    static final int MARKER_COLOR = 0x00ff66;

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
    public float markerHue;

    transient Marker marker;

    transient Circle circle;

    public LocationMemo(long id, @NonNull String address, @NonNull String note, @NonNull LatLng location, double radius,
            float markerHue) {
        this.id = id;
        this.address = address;
        this.note = note;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.radius = radius;
        this.markerHue = markerHue;
    }

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location, double radius,
            float markerHue) {
        this(0, address, note, location, radius, markerHue);
    }

    public LatLng buildLocation() {
        return new LatLng(latitude, longitude);
    }

    public MarkerOptions buildMarkerOptions() {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(markerHue);

        return new MarkerOptions()
                .title(address)
                .snippet(note)
                .position(buildLocation())
                .icon(icon);
    }

    public void addMarkerToMap(GoogleMap map) {
        final MarkerOptions markerOptions = buildMarkerOptions();
        if (marker != null) {
            marker.remove();
        }
        marker = map.addMarker(markerOptions);

        if (circle != null) {
            circle.remove();
        }

        if (radius != 0) {
            final CircleOptions circleOptions = new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(radius * 1000)
                    .strokeWidth(2)
                    .strokeColor(makeAlpha(MARKER_COLOR, 0xdd))
                    .fillColor(makeAlpha(MARKER_COLOR, 0x1f));
            circle = map.addCircle(circleOptions);
        }
    }

    private int makeAlpha(int color, int alpha) {
        return (color & 0xFFFFFF) | (alpha << 24);
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
