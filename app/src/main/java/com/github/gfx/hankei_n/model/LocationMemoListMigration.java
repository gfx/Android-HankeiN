package com.github.gfx.hankei_n.model;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

    LocationMemoManager locationMemoManager;

    public static void run(Context context) {
        new LocationMemoListMigration(context).migrate();
    }

    LocationMemoListMigration(Context context) {
        this.context = context;
    }

    void migrate() {
        migratePointedLocation();
        migrateLocationList();
    }

    synchronized LocationMemoManager getLocationMemoManager() {
        if (locationMemoManager == null) {
            OrmaDatabase orma = OrmaDatabase.builder(context).name("main.db").build();
            locationMemoManager = new LocationMemoManager(orma, new MarkerManager());
        }
        return locationMemoManager;
    }

    void migratePointedLocation() {
        Prefs prefs = new Prefs(context);

        float latitude = prefs.get("pointedLatitude", 0.0f);
        float longitude = prefs.get("pointedLongitude", 0.0f);
        float radius = prefs.get("radius", 1.5f);
        String address = prefs.get("addressName", "");

        if (latitude == 0 || longitude == 0) {
            return;
        }

        LocationMemoManager locationMemoManager = getLocationMemoManager();

        LocationMemo memo = new LocationMemo(address, "", new LatLng(latitude, longitude), radius, radius > 0,
                BitmapDescriptorFactory.HUE_ORANGE);
        Timber.d("migrate the pointed location as %s", memo);
        locationMemoManager.upsert(memo);

        prefs.edit()
                .remove("pointedLatitude")
                .remove("pointedLongitude")
                .remove("radius")
                .remove("addressName")
                .apply();
    }

    void migrateLocationList() {
        SharedPreferences preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);

        String json = preferences.getString(kEntity, null);
        if (json == null) {
            Timber.d("no LocationMemoList migration needed.");
            return;
        }
        Timber.d("start LocationMemoList migration for: %s", json);

        LocationMemoManager locationMemoManager = getLocationMemoManager();

        List<LocationMemo> memos = gson.fromJson(json, LocationMemoList.class).memos;

        for (LocationMemo memo : memos) {
            Timber.d("%d/%s (%.02f, %.02f)", memo.id, memo.address, memo.latitude, memo.longitude);
            memo.markerHue = BitmapDescriptorFactory.HUE_GREEN;
            locationMemoManager.upsert(memo);
        }

        preferences.edit().clear().apply();

        Timber.d("finish LocationMemoList migration");
    }


    static class LocationMemoList {

        @SerializedName("memos")
        ArrayList<LocationMemo> memos;
    }
}
