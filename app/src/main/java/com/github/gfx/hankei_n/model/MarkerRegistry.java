package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Pair;

public class MarkerRegistry {

    public static final MarkerRegistry INSTANCE = new MarkerRegistry();

    private final LongSparseArray<Pair<Marker, Circle>> registry = new LongSparseArray<>();

    private MarkerRegistry() {
    }

    public void register(double latitude, double longitude, @NonNull Marker marker, @Nullable Circle circle) {
        remove(latitude, longitude);
        registry.append(createKey(latitude, longitude), Pair.create(marker, circle));
    }

    public void remove(double latitude, double longitude) {
        long key = createKey(latitude, longitude);
        Pair<Marker, Circle> pair = registry.get(key);
        if (pair != null) {
            registry.remove(key);

            pair.first.remove();
            if (pair.second != null) {
                pair.second.remove();
            }
        }
    }
    
    long createKey(double latitude, double longitude) {
        long hi = (long)(latitude * 100) << 32;
        long lo = (long)(longitude * 100);
        return hi | lo;
    }
}
