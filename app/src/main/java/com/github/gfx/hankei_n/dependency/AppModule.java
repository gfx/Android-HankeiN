package com.github.gfx.hankei_n.dependency;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;

import com.github.gfx.hankei_n.BuildConfig;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.event.LocationMemoChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoRemovedEvent;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.model.PlaceEngine;
import com.github.gfx.hankei_n.model.Prefs;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

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

    static final String DB_NAME = "main.db";

    final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    Application provideApplication() {
        return (Application) context.getApplicationContext();
    }

    @Provides
    Context provideContext() {
        return context;
    }

    @Singleton
    @Provides
    GoogleAnalytics provideGoogleAnalytics(Application application) {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(application);
        ga.enableAutoActivityReports(application);
        return ga;
    }

    @Singleton
    @Provides
    Tracker provideTracker(Context context, GoogleAnalytics ga) {
        Tracker tracker = ga.newTracker(BuildConfig.GA_TRACKING_ID);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
        return tracker;
    }

    @Provides
    Prefs providePrefs(Context context) {
        return new Prefs(context);
    }

    @Provides
    Geocoder provideGeocoder(Context context) {
        return new Geocoder(context, Locale.getDefault());
    }

    @Provides
    PlaceEngine providePlacesEngine(Context context, Geocoder geocoder) {
        return new PlaceEngine(context, geocoder);
    }

    @Provides
    Vibrator provideVibrator(Context context) {
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    GoogleApiAvailability provideGoogleApiAvailability() {
        return GoogleApiAvailability.getInstance();
    }

    @Singleton
    @Provides
    LocationMemoManager provideLocationMemoList(Context context) {
        return new LocationMemoManager(context, DB_NAME);
    }

    @Provides
    MarkerHueAllocator provideMarkerHue(Prefs prefs) {
        return new MarkerHueAllocator(prefs);
    }

    @Singleton
    @Provides
    BehaviorSubject<LocationChangedEvent> provideLocationChangedEventSubject() {
        return BehaviorSubject.create();
    }

    @Singleton
    @Provides
    BehaviorSubject<LocationMemoAddedEvent> provideLocationMemoAddedEventSubject() {
        return BehaviorSubject.create();
    }

    @Singleton
    @Provides
    BehaviorSubject<LocationMemoRemovedEvent> provideLocationMemoRemovedEventSubject() {
        return BehaviorSubject.create();
    }

    @Singleton
    @Provides
    BehaviorSubject<LocationMemoChangedEvent> provideLocationMemoChangedEventSubject() {
        return BehaviorSubject.create();
    }
}
