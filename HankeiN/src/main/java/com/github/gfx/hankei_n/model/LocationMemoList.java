package com.github.gfx.hankei_n.model;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Iterator;

public class LocationMemoList implements Iterable<LocationMemo> {

    static final String STORAGE_NAME = LocationMemoList.class.getSimpleName();

    final ArrayList<LocationMemo> memos = new ArrayList<>();

    public void add(LocationMemo memo) {
        memos.add(memo);
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return memos.iterator();
    }

    public void save(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        prefs.edit().putString("", "").apply();

    }
}
