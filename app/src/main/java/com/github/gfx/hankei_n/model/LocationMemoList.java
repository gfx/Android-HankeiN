package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import com.github.gfx.hankei_n.model.gson.LatLngTypeAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemoList implements Iterable<LocationMemo> {

    static final String STORAGE_NAME = LocationMemoList.class.getSimpleName();

    static final String kEntity = "entity";

    static final String kLocationMemoId = "id";

    transient SharedPreferences preferences;

    @SerializedName("memos")
    final ArrayList<LocationMemo> memos = new ArrayList<>();

    private LocationMemoList(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public static LocationMemoList load(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        Gson gson = createGson();

        String json = preferences.getString(kEntity, null);
        LocationMemoList instance;
        if (json != null) {
            instance = gson.fromJson(json, LocationMemoList.class);
            instance.preferences = preferences;
        } else {
            instance = new LocationMemoList(preferences);
        }
        return instance;
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    public long getCurrentId() {
        return preferences.getLong(kLocationMemoId, 1L);
    }

    public synchronized long generateNextId() {
        long id = preferences.getLong(kLocationMemoId, 1L) + 1;
        preferences.edit()
                .putLong(kLocationMemoId, id)
                .apply();
        return id;
    }

    static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LatLng.class, new LatLngTypeAdapter())
                .create();
    }

    public boolean contains(LocationMemo memo) {
        return memos.contains(memo);
    }

    public void upsert(LocationMemo memo) {
        int index = memos.indexOf(memo);
        if (index == -1) {
            Log.d("XXX", "add location memo for " + memo.address);
            memos.add(new LocationMemo(generateNextId(), memo.address, memo.note, memo.location));
        } else {
            Log.d("XXX", "update location memo for " + memo.address);
            long id = memos.get(index).id;
            memos.set(index, new LocationMemo(id, memo.address, memo.note, memo.location));
        }
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

    public void removeItem(LocationMemo memo) {
        memos.remove(memo);
    }

    public void clear() {
        memos.clear();
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return memos.iterator();
    }

    @SuppressLint("CommitPrefEdits")
    public void save() {
        Gson gson = createGson();

        preferences.edit()
                .putString(kEntity, gson.toJson(this))
                .commit();
    }
}

