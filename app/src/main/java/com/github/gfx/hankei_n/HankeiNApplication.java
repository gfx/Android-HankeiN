package com.github.gfx.hankei_n;

import com.github.gfx.hankei_n.debug.ActivityLifecycleLogger;
import com.github.gfx.hankei_n.dependency.AppComponent;
import com.github.gfx.hankei_n.dependency.AppModule;
import com.github.gfx.hankei_n.dependency.DaggerAppComponent;

import android.app.Application;
import android.content.Context;

import javax.annotation.ParametersAreNonnullByDefault;

import timber.log.Timber;

@ParametersAreNonnullByDefault
public class HankeiNApplication extends Application {

    AppComponent component;

    public static AppComponent getAppComponent(Context context) {
        HankeiNApplication application = (HankeiNApplication) context.getApplicationContext();
        return application.getAppComponent();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        registerActivityLifecycleCallbacks(new ActivityLifecycleLogger());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public AppComponent getAppComponent() {
        return component;
    }
}
