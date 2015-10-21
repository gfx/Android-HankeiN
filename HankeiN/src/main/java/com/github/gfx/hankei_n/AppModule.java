package com.github.gfx.hankei_n;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

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

    Application application;

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
        tracker.enableAutoActivityTracking(true);
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
}
