package com.github.gfx.hankei_n;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Set;

public class Prefs {
    final private SharedPreferences sharedPrefs;

    public Prefs(Context context) {
        sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean get(String key, boolean defValue) {
        return sharedPrefs.getBoolean(key, defValue);
    }

    public void put(String key, long value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public long get(String key, long defValue) {
        return sharedPrefs.getLong(key, defValue);
    }

    public void put(String key, int value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int get(String key, int defValue) {
        return sharedPrefs.getInt(key, defValue);
    }

    public void put(String key, float value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public float get(String key, float defValue) {
        return sharedPrefs.getFloat(key, defValue);
    }

    public void put(String key, String value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String get(String key, String defValue) {
        return sharedPrefs.getString(key, defValue);
    }

    public void put(String key, Set<String> value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public Set<String> get(String key, Set<String> defValue) {
        return sharedPrefs.getStringSet(key, defValue);
    }

    public void resetAll() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        for (String key : sharedPrefs.getAll().keySet()) {
            editor.remove(key);
        }
        editor.commit();
    }
}
