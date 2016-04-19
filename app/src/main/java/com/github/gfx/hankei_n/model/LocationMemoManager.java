package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

import android.content.Context;

import java.util.Iterator;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Singleton;

@ParametersAreNonnullByDefault
@Singleton
public class LocationMemoManager implements Iterable<LocationMemo> {

    public static final String kDefaultDrawCircle = "default_draw_circle"; // boolean

    final LocationMemo_Relation relation;

    int count;

    @Inject
    public LocationMemoManager(OrmaDatabase orma) {
        relation = orma.relationOfLocationMemo()
                .orderByIdAsc();
        count = relation.count();
    }

    public int count() {
        assert count == relation.count();
        return count;
    }

    public LocationMemo get(int i) {
        return relation.get(i);
    }

    public LocationMemo newMemo(Context context, MarkerHueAllocator markerHueAllocator, LatLng latLng) {
        double radius = Double.parseDouble(context.getString(R.string.default_radius));
        return new LocationMemo("", "", latLng, radius, getDefaultDrawCircle(context), markerHueAllocator.allocate());
    }

    public static boolean getDefaultDrawCircle(Context context) {
        return new Prefs(context).get(kDefaultDrawCircle, true);
    }

    public static void setDefaultDrawCircle(Context context, boolean value) {
        new Prefs(context).put(kDefaultDrawCircle, value);
    }

    public void upsert(LocationMemo memo) {
        assert memo.id != -1;
        if (memo.id == 0) {
            memo.id = relation.inserter().execute(memo);
        } else {
            relation.upserter().execute(memo);
        }
        count = relation.count();
    }

    public void remove(LocationMemo memo) {
        relation.deleter().idEq(memo.id).execute();
        memo.id = -1;
        count = relation.count();
    }

    public void clear() {
        relation.deleter().execute();
        count = 0;
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return relation.iterator();
    }
}
