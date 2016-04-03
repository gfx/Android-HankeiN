package com.github.gfx.hankei_n.dependency;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import com.github.gfx.hankei_n.BuildConfig;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.event.LocationMemoChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoFocusedEvent;
import com.github.gfx.hankei_n.event.LocationMemoRemovedEvent;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.model.Prefs;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.location.Geocoder;
import android.os.Vibrator;
import android.util.DisplayMetrics;

import java.util.Locale;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.subjects.PublishSubject;

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
    GoogleApiClient provideGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
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

    @Provides
    DisplayMetrics provideDisplayMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }

    @Singleton
    @Provides
    PublishSubject<LocationChangedEvent> provideLocationChangedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    PublishSubject<LocationMemoAddedEvent> provideLocationMemoAddedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    PublishSubject<LocationMemoRemovedEvent> provideLocationMemoRemovedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    PublishSubject<LocationMemoChangedEvent> provideLocationMemoChangedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    PublishSubject<LocationMemoFocusedEvent> provideLocationMemoFocusedEventSubject() {
        return PublishSubject.create();
    }
}
