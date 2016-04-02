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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

@Table("addresses")
@ParametersAreNonnullByDefault
public class LocationMemo implements Serializable, Comparable<LocationMemo>, Cloneable {

    static final int MARKER_COLOR = 0x00ff66;

    @SerializedName("id")
    @PrimaryKey(autoincrement = true)
    public long id;

    @SerializedName("address")
    @Column
    @NonNull
    public String address;

    @SerializedName("note")
    @Column
    @NonNull
    public String note;

    @SerializedName("latitude")
    @Column
    public double latitude;

    @SerializedName("longitude")
    @Column
    public double longitude;

    @SerializedName("radius")
    @Column
    public double radius;

    @SerializedName("marker_hue")
    @Column
    public float markerHue;

    private transient Marker marker;

    private transient Circle circle;

    @Setter
    public LocationMemo(long id, @NonNull String address, @NonNull String note, double latitude, double longitude, double radius,
            float markerHue) {
        this.id = id;
        this.address = address;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.markerHue = markerHue;
    }

    public LocationMemo(long id, @NonNull String address, @NonNull String note, @NonNull LatLng location, double radius,
            float markerHue) {
        this(id, address, note, location.latitude, location.longitude, radius, markerHue);
    }

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location, double radius,
            float markerHue) {
        this(0, address, note, location, radius, markerHue);
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public MarkerOptions buildMarkerOptions() {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(markerHue % 360.0f);

        return new MarkerOptions()
                .title(address)
                .snippet(note)
                .position(getLatLng())
                .icon(icon);
    }

    public CircleOptions buildCircleOptions() {
        return new CircleOptions()
                .center(getLatLng())
                .radius(radius * 1000)
                .strokeWidth(2)
                .strokeColor(makeAlpha(MARKER_COLOR, 0xdd))
                .fillColor(makeAlpha(MARKER_COLOR, 0x1f));
    }

    public void addMarkerToMap(@NonNull GoogleMap map) {
        removeFromMap();
        marker = map.addMarker(buildMarkerOptions());
        if (radius != 0) {
            circle = map.addCircle(buildCircleOptions());
        }
    }

    public void removeFromMap() {
        if (marker != null) {
            marker.remove();
        }
        if (circle != null) {
            circle.remove();
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
