package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import com.github.gfx.hankei_n.model.gson.LatLngTypeAdapter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class LocationMemoListMigration {

    static final String STORAGE_NAME = "LocationMemoList";

    static final String kEntity = "entity";

    final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LatLng.class, new LatLngTypeAdapter())
            .create();

    final Context context;
    final SharedPreferences preferences;

    public static void run(Context context) {
        new LocationMemoListMigration(context).migrate();
    }

    LocationMemoListMigration(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    void migrate() {
        String json = preferences.getString(kEntity, null);
        if (json == null) {
            Timber.d("no LocationMemoList needed.");
            return;
        }
        Timber.d("start LocationMemoList migration.");

        LocationMemoManager locationMemoManager = new LocationMemoManager(context, "main.db");

        List<LocationMemo> memos = gson.fromJson(json, LocationMemoList.class).memos;

        for (LocationMemo memo : memos) {
            Timber.d("%d/%s", memo.id, memo.address);
            locationMemoManager.upsert(memo);
        }

        locationMemoManager.close();

        preferences.edit().clear().apply();

        Timber.d("finish LocationMemoList migration");
    }


    static class LocationMemoList {

        @SerializedName("memos")
        ArrayList<LocationMemo> memos;
    }
}
