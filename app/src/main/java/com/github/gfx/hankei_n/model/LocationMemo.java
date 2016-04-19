package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.SerializedName;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.hankei_n.toolbox.Locations;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

@Table("addresses")
@ParametersAreNonnullByDefault
public class LocationMemo implements Serializable, Comparable<LocationMemo> {

    @ColorInt
    static final int CIRCLE_COLOR = 0x00ff66;

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

    @SerializedName("draw_circle")
    @Column(defaultExpr = "1")
    public boolean drawCircle;

    @SerializedName("marker_hue")
    @Column
    public float markerHue;

    @Setter
    public LocationMemo(long id, @NonNull String address, @NonNull String note, double latitude, double longitude,
            double radius,  boolean drawCircle, float markerHue) {
        this.id = id;
        this.address = address;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.drawCircle  = drawCircle;
        this.markerHue = markerHue;
    }

    public LocationMemo(long id, @NonNull String address, @NonNull String note, @NonNull LatLng location,
            double radius, boolean drawCircle, float markerHue) {
        this(id, address, note, location.latitude, location.longitude, radius, drawCircle, markerHue);
    }

    public LocationMemo(@NonNull String address, @NonNull String note, @NonNull LatLng location,
            double radius, boolean drawCircle, float markerHue) {
        this(0, address, note, location, radius, drawCircle, markerHue);
    }

    public LocationMemo copy() {
        return new LocationMemo(id, address, note, latitude, longitude, radius, drawCircle, markerHue);
    }

    public boolean isPointingSomewhere() {
        return Locations.isSomewhere(latitude, longitude);
    }

    public boolean isPointingNowhere() {
        return Locations.isNowhere(latitude, longitude);
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
                .strokeWidth(3)
                .strokeColor(makeAlpha(CIRCLE_COLOR, 0xdd))
                .fillColor(makeAlpha(CIRCLE_COLOR, 0x1c));
    }

    @ColorInt
    private static int makeAlpha(@ColorInt int color, int alpha) {
        return (color & 0xFFFFFF) | (alpha << 24);
    }

    public void update(LocationMemo memo) {
        id = memo.id;
        address = memo.address;
        note = memo.note;
        latitude = memo.latitude;
        longitude = memo.longitude;
        radius = memo.radius;
        markerHue = memo.markerHue;
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

    public boolean contentEquals(LocationMemo memo) {
        return toString().equals(memo.toString());
    }

    @Override
    public int hashCode() {
        return (int) (id % Integer.MAX_VALUE);
    }

    @Override
    public int compareTo(LocationMemo another) {
        return another.address.compareTo(this.address);
    }

    @Override
    public String toString() {
        return "LocationMemo{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", note='" + note + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                ", drawCircle=" + drawCircle +
                ", markerHue=" + markerHue +
                '}';
    }
}
