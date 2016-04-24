package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

import com.github.gfx.hankei_n.dependency.scope.ContextScope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

@ContextScope
public class MarkerManager {

    final LongSparseArray<MarkerHolder> markerHolders = new LongSparseArray<>();

    @Inject
    public MarkerManager() {
    }

    @NonNull
    public Marker get(@NonNull LocationMemo memo) {
        Marker marker = getOrNull(memo);
        if (marker == null) {
            throw new NoSuchElementException("No MarkerHolder for " + memo + " in " + this);
        }
        return marker;
    }

    @Nullable
    private Marker getOrNull(@NonNull LocationMemo memo) {
        assert memo.isPersistent();
        MarkerHolder markerHolder = markerHolders.get(memo.id);
        if (markerHolder == null) {
            return null;
        }
        return markerHolder.marker;
    }

    public LocationMemo findMemoByMarker(Iterable<LocationMemo> memos, Marker marker) {
        for (LocationMemo item : memos) {
            // Use getOrNull() because all the markers are not necessarily prepared
            if (marker.equals(getOrNull(item))) {
                return item;
            }
        }
        throw new NoSuchElementException("Marker not found for id=" + marker.getId());
    }

    public Marker create(@NonNull GoogleMap map, @NonNull LocationMemo memo) {
        assert memo.id > 0;

        remove(memo);

        Marker marker = map.addMarker(memo.buildMarkerOptions());
        Circle circle = null;
        if (memo.radius > 0 && memo.drawCircle) {
            circle = map.addCircle(memo.buildCircleOptions());
        }

        markerHolders.put(memo.id, new MarkerHolder(marker, circle));

        return marker;
    }

    public void remove(LocationMemo memo) {
        assert memo.id > 0;
        MarkerHolder holder = markerHolders.get(memo.id);
        if (holder != null) {
            holder.removeFromMap();
            markerHolders.remove(memo.id);
        }
    }

    public void clear() {
        for (int i = 0; i < markerHolders.size(); i++) {
            long id = markerHolders.keyAt(i);
            markerHolders.get(id).removeFromMap();
        }
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < this.markerHolders.size(); i++) {
            long id = markerHolders.keyAt(i);
            list.add(id + "=" + markerHolders.get(id));
        }
        return "MarkerManager{" + TextUtils.join(", ", list) + "}";
    }

    static class MarkerHolder {

        Marker marker;

        Circle circle;

        public MarkerHolder(Marker marker, Circle circle) {
            this.marker = marker;
            this.circle = circle;
        }

        void removeFromMap() {
            if (marker != null) {
                marker.remove();
            }
            if (circle != null) {
                circle.remove();
            }
        }

        @Override
        public String toString() {
            return marker.getId() + "/" + marker.getTitle();
        }
    }
}
