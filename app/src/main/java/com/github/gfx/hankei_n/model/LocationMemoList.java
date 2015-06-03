package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.github.gfx.hankei_n.model.gson.LatLngTypeAdapter;

import android.annotation.SuppressLint;
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

    public int size() {
        return memos.size();
    }

    public LocationMemo get(int index) {
        return memos.get(index);
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return memos.iterator();
    }

    @SuppressLint("CommitPrefEdits")
    public void save(Context context) {
        Gson gson = createGson();

        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        for (LocationMemo memo : this) {
            editor.putString(memo.address, gson.toJson(memo));
        }

        editor.commit();
    }

    public static LocationMemoList load(Context context) {
        Gson gson = createGson();

        LocationMemoList memos = new LocationMemoList();

        for (Object value : getSharedPreferences(context).getAll().values()) {
            String json = (String) value;
            memos.add(gson.fromJson(json, LocationMemo.class));
        }

        return memos;
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LatLng.class, new LatLngTypeAdapter())
                .create();
    }
}

