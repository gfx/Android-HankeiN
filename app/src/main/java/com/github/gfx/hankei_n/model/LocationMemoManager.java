package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

import android.content.Context;

import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemoManager {

    final OrmaDatabase orma;

    final List<LocationMemo> items;

    final LocationMemo_Relation relation;

    public LocationMemoManager(Context context, String name) {
        orma = OrmaDatabase.builder(context)
                .name(name)
                .build();

        relation = orma.relationOfLocationMemo()
                .orderByIdAsc();
        items = relation.selector().toList();
    }

    public int count() {
        return items.size();
    }

    public List<LocationMemo> all() {
        return items;
    }

    public LocationMemo get(int i) {
        return items.get(i);
    }

    public LocationMemo reload(LocationMemo memo) {
        for (LocationMemo item : items) {
            if (item.equals(memo)) {
                return item;
            }
        }
        throw new NoSuchElementException("LocationMemo not found");
    }

    public LocationMemo newMemo(Context context, MarkerHueAllocator markerHueAllocator, LatLng latLng) {
        double radius = Double.parseDouble(context.getString(R.string.default_radius));
        return new LocationMemo("", "", latLng, radius, markerHueAllocator.allocate());
    }

    public void upsert(LocationMemo memo) {
        if (memo.id == 0) {
            memo.id = relation.inserter().execute(memo);
            items.add(memo);
        } else {
            relation.upserter().execute(memo);
            int index = items.indexOf(memo);
            items.get(index).update(memo);
        }
    }

    public void remove(LocationMemo memo) {
        relation.deleter().idEq(memo.id).execute();

        reload(memo).removeFromMap();
        items.remove(memo);
    }

    public void clear() {
        relation.deleter().execute();
        items.clear();
    }

    public LocationMemo findMemoByMarker(Marker marker) {
        for (LocationMemo item : items) {
            if (marker.equals(item.marker)) {
                return item;
            }
        }
        throw new NoSuchElementException("Marker not found");
    }
}
