package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.annotation.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Memo {

    LatLng latLng;

    String title;

    public Memo(@NonNull String title, @NonNull LatLng latLng) {
        this.latLng = latLng;
        this.title = title;
    }

    public MarkerOptions buildMarkerOptions() {
        return new MarkerOptions()
                .title(title)
                .position(latLng);
    }

}
