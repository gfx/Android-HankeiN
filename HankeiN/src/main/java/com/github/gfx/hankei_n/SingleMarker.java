package com.github.gfx.hankei_n;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class SingleMarker {

    @NonNull
    final private GoogleMap map;

    final private int markerColor;

    private double radius;

    @Nullable
    private Marker mapMarker;

    @Nullable
    private Circle mapCircle;

    public SingleMarker(GoogleMap map, double radiusInMeter, int markerColor) {
        this.map = map;
        this.radius = radiusInMeter;
        this.markerColor = markerColor;
    }


    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        if (mapCircle != null) {
            mapCircle.setRadius(radius * 1000);
        }
    }

    public void setTitle(String title) {
        if (mapMarker != null) {
            mapMarker.setTitle(title);
        }
    }

    public void move(LatLng latLng) {
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (mapMarker != null) {
            mapMarker.remove();
        }
        mapMarker = map.addMarker(markerOptions);

        final CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius * 1000);
        circleOptions.strokeWidth(2);
        circleOptions.strokeColor(addColorAlpha(markerColor, 0xdd));
        circleOptions.fillColor(addColorAlpha(markerColor, 0x1f));
        if (mapCircle != null) {
            mapCircle.remove();
        }
        mapCircle = map.addCircle(circleOptions);
    }

    private int addColorAlpha(int color, int alpha) {
        return (color & 0xFFFFFF) | (alpha << 24);
    }
}
