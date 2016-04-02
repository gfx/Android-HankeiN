package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

public class CameraState {

    static final String kLatitude = "prevLatitude";

    static final String kLongitude = "prevLongitude";

    static final String kZoom = "prevCameraZoom";

    public static final float ZOOM = 14.0f;

    public static final float ZOOM_FOR_NON_ACCURATE_LOCATION = 5.0f;

    final Prefs prefs;

    @Inject
    public CameraState(Prefs prefs) {
        this.prefs = prefs;
    }

    public void setZoom(float zoom) {
        prefs.edit()
                .putFloat(kZoom, zoom)
                .apply();
    }

    public void setLatLng(LatLng latLng) {
        prefs.edit()
                .putFloat(kLatitude, (float) latLng.latitude)
                .putFloat(kLongitude, (float) latLng.longitude)
                .apply();
    }

    public void save(LatLng latLng, float zoom) {
        prefs.edit()
                .putFloat(kLatitude, (float) latLng.latitude)
                .putFloat(kLongitude, (float) latLng.longitude)
                .putFloat(kZoom, zoom)
                .apply();
    }

    public void save(CameraPosition position) {
        save(position.target, position.zoom);
    }

    public LatLng getLatLng() {
        return new LatLng(prefs.get(kLatitude, 0.0f), prefs.get(kLongitude, 0.0f));
    }

    public float getCameraZoom() {
        return prefs.get(kZoom, ZOOM);
    }

    public CameraUpdate updateCamera(LatLng latLng) {
        setLatLng(latLng);
        return CameraUpdateFactory.newLatLngZoom(latLng, getCameraZoom());
    }

    public CameraUpdate updateCamera(LatLng latLng, float zoom) {
        save(latLng, zoom);
        return CameraUpdateFactory.newLatLngZoom(latLng, zoom);
    }
}
