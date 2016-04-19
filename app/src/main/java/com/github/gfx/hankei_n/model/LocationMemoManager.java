package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.dependency.scope.ContextScope;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

import android.content.Context;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

@ParametersAreNonnullByDefault
@ContextScope
public class LocationMemoManager implements Iterable<LocationMemo> {

    public static final String kDefaultDrawCircle = "default_draw_circle"; // boolean

    final OrmaDatabase orma;

    final MarkerManager markerManager;

    final LocationMemo_Relation relation;

    @Inject
    public LocationMemoManager(OrmaDatabase orma, MarkerManager markerManager) {
        this.orma = orma;

        relation = orma.relationOfLocationMemo()
                .orderByIdAsc();

        this.markerManager = markerManager;
    }

    public int count() {
        return relation.count();
    }

    public LocationMemo get(int i) {
        return relation.get(i);
    }

    public LocationMemo newMemo(Context context, MarkerHueAllocator markerHueAllocator, LatLng latLng) {
        double radius = Double.parseDouble(context.getString(R.string.default_radius));
        Prefs prefs = new Prefs(context);
        return new LocationMemo("", "", latLng, radius, prefs.get(kDefaultDrawCircle, true), markerHueAllocator.allocate());
    }

    public static void setDefaultDrawCircle(Context context, boolean value) {
        new Prefs(context).put(kDefaultDrawCircle, value);
    }

    public void upsert(LocationMemo memo) {
        if (memo.id == 0) {
            memo.id = relation.inserter().execute(memo);
        } else {
            relation.upserter().execute(memo);
        }
    }

    public void remove(LocationMemo memo) {
        relation.deleter().idEq(memo.id).execute();
        markerManager.remove(memo);
        memo.id = -1;
    }

    public void clear() {
        relation.deleter().execute();
    }

    public LocationMemo findMemoByMarker(Marker marker) {
        for (LocationMemo item : relation) {
            if (marker.equals(markerManager.get(item))) {
                return item;
            }
        }
        throw new NoSuchElementException("Marker not found for id=" + marker.getId());
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return relation.iterator();
    }
}
