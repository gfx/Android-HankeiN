package com.github.gfx.hankei_n.dependency;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;

import com.github.gfx.hankei_n.Prefs;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.model.LocationMemoList;

import android.app.Application;
import android.content.Context;
import android.location.Geocoder;
import android.os.Vibrator;

import java.util.Locale;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    Application getApplication() {
        return application;
    }

    @Provides
    Context getContext() {
        return application;
    }

    @Singleton
    @Provides
    GoogleAnalytics getGoogleAnalytics() {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(application);
        ga.enableAutoActivityReports(application);
        return ga;
    }

    @Singleton
    @Provides
    Tracker getTracker(GoogleAnalytics ga) {
        Tracker tracker = ga.newTracker(application.getString(R.string.ga_tracking_id));
        tracker.enableExceptionReporting(true);
        return tracker;
    }

    @Provides
    Prefs getPrefs(Context context) {
        return new Prefs(context);
    }

    @Provides
    Geocoder getGeocoder(Context context) {
        return new Geocoder(context, Locale.getDefault());
    }

    @Provides
    Vibrator getVibrator(Context context) {
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    GoogleApiAvailability getGoogleApiAvailability() {
        return GoogleApiAvailability.getInstance();
    }

    @Provides
    LocationMemoList getMemoList() {
        return new LocationMemoList();
    }
}
