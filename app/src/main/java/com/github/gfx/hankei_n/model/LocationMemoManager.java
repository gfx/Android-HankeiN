package com.github.gfx.hankei_n.model;

import android.content.Context;

import java.util.List;

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

    public void upsert(LocationMemo memo) {
        if (memo.id == 0) {
            memo.id = relation.inserter().execute(memo);
            items.add(memo);
        } else {
            relation.upserter().execute(memo);
        }
    }

    public void remove(LocationMemo memo) {
        relation.deleter().idEq(memo.id);

        memo.removeFromMap();
        items.remove(memo);
    }

    public void clear() {
        relation.deleter().execute();
        items.clear();
    }
}
