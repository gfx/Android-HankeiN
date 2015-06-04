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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemoList implements Iterable<LocationMemo> {

    static final String STORAGE_NAME = LocationMemoList.class.getSimpleName();

    static final String ENTITY_NAME = "LocationMemoList.entity";

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

    public void remove(int index) {
        memos.remove(index);
    }

    public void clear() {
        memos.clear();
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return memos.iterator();
    }

    @SuppressLint("CommitPrefEdits")
    public void save(Context context) {
        Gson gson = createGson();

        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(ENTITY_NAME, gson.toJson(this)).commit();
    }

    public static LocationMemoList load(Context context) {
        Gson gson = createGson();

        String json = getSharedPreferences(context).getString(ENTITY_NAME, null);
        if (json != null) {
            return gson.fromJson(json, LocationMemoList.class);
        } else {
            return new LocationMemoList();
        }
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

