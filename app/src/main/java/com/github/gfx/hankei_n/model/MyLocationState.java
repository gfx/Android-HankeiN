package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

public class MyLocationState {

    static final String kLatitude = "prevLatitude";
    static final String kLongitude = "prevLongitude";
    static final String kZoom = "prevCameraZoom";

    static final float kDefaultZoo = 14.0f;

    final Prefs prefs;

    @Inject
    public MyLocationState(Prefs prefs) {
        this.prefs = prefs;
    }

    public void save(LatLng latLng, float zoom) {
        prefs.edit()
                .putFloat(kLatitude, (float)latLng.latitude)
                .putFloat(kLongitude, (float)latLng.longitude)
                .putFloat(kZoom, zoom)
                .apply();
    }

    public LatLng getLatLng() {
        return new LatLng(prefs.get(kLatitude, 0.0f), prefs.get(kLongitude, 0.0f));
    }

    public float getCameraZoom() {
        return prefs.get(kZoom, kDefaultZoo);
    }
    public CameraUpdate getCameraUpdate() {
      return CameraUpdateFactory.newLatLngZoom(getLatLng(), getCameraZoom());
    }
}
