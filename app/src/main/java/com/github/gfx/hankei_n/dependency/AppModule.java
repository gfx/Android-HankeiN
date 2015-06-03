package com.github.gfx.hankei_n.dependency;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;

import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.LocationMemoList;
import com.github.gfx.hankei_n.model.PlacesEngine;
import com.github.gfx.hankei_n.model.Prefs;

import android.app.Application;
import android.content.Context;
import android.location.Geocoder;
import android.os.Vibrator;

import java.util.Locale;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.subjects.BehaviorSubject;

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
    GoogleAnalytics getGoogleAnalytics(Application application) {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(application);
        ga.enableAutoActivityReports(application);
        return ga;
    }

    @Singleton
    @Provides
    Tracker getTracker(Context context, GoogleAnalytics ga) {
        Tracker tracker = ga.newTracker(context.getString(R.string.ga_tracking_id));
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

    @Singleton
    @Provides
    PlacesEngine getPlacesEngine(Context context, Geocoder geocoder,
            BehaviorSubject<LocationChangedEvent> locationChangedEventObservable) {
        return new PlacesEngine(context, geocoder, locationChangedEventObservable);
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

    @Singleton
    @Provides
    BehaviorSubject<LocationChangedEvent> getOnMyLocationChangedSubject() {
        return BehaviorSubject.create();
    }

    @Singleton
    @Provides
    BehaviorSubject<LocationMemoAddedEvent> getOnLocationMemoChangedSubject() {
        return BehaviorSubject.create();
    }
}
