package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

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

    static final String kEntity = "entity";

    static final String kLocationMemoId = "id";

    static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LatLng.class, new LatLngTypeAdapter())
            .create();

    transient SharedPreferences preferences;

    @SerializedName("memos")
    final ArrayList<LocationMemo> memos = new ArrayList<>();

    private LocationMemoList(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public static LocationMemoList load(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);

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
        long nextId = getCurrentId() + 1;
        preferences.edit()
                .putLong(kLocationMemoId, nextId)
                .apply();
        return nextId;
    }

    public boolean contains(LocationMemo memo) {
        return memos.contains(memo);
    }

    public void upsert(LocationMemo memo) {
        int index = memos.indexOf(memo);
        if (index == -1 || memo.id == 0) {
            memos.add(new LocationMemo(generateNextId(), memo.address, memo.note, memo.location));
        } else {
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
        preferences.edit()
                .putString(kEntity, gson.toJson(this))
                .commit();
    }
}
