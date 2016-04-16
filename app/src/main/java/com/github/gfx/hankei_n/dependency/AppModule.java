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
import com.github.gfx.hankei_n.model.OrmaDatabase;

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

    final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    public Application provideApplication() {
        return application;
    }

    @Provides
    public Context provideApplicationContext() {
        return application;
    }

    @Singleton
    @Provides
    public GoogleAnalytics provideGoogleAnalytics(Application application) {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(application);
        ga.enableAutoActivityReports(application);
        ga.enableAdvertisingIdCollection(true);
        return ga;
    }

    @Singleton
    @Provides
    public Tracker provideTracker(GoogleAnalytics ga) {
        Tracker tracker = ga.newTracker(BuildConfig.GA_TRACKING_ID);
        tracker.setAnonymizeIp(true);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
        return tracker;
    }

    @Provides
    public GoogleApiAvailability provideGoogleApiAvailability() {
        return GoogleApiAvailability.getInstance();
    }

    @Provides
    public Locale providesLocale() {
        return Locale.getDefault();
    }

    @Provides
    public Geocoder provideGeocoder(Context context, Locale locale) {
        return new Geocoder(context, locale);
    }

    @Provides
    public GoogleApiClient provideGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
    }

    @Provides
    public Vibrator provideVibrator(Context context) {
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    public Resources provideResources(Context context) {
        return context.getResources();
    }


    @Provides
    public DisplayMetrics provideDisplayMetrics(Resources resources) {
        return resources.getDisplayMetrics();
    }

    @Singleton
    @Provides
    public OrmaDatabase provideOrmaDatabase(Context context) {
        return OrmaDatabase.builder(context)
                .name(DB_NAME)
                .build();
    }

    @Singleton
    @Provides
    public PublishSubject<LocationChangedEvent> provideLocationChangedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    public PublishSubject<LocationMemoAddedEvent> provideLocationMemoAddedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    public PublishSubject<LocationMemoRemovedEvent> provideLocationMemoRemovedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    public PublishSubject<LocationMemoChangedEvent> provideLocationMemoChangedEventSubject() {
        return PublishSubject.create();
    }

    @Singleton
    @Provides
    public PublishSubject<LocationMemoFocusedEvent> provideLocationMemoFocusedEventSubject() {
        return PublishSubject.create();
    }
}
