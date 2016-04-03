package com.github.gfx.hankei_n;

import com.github.gfx.hankei_n.debug.ActivityLifecycleLogger;
import com.github.gfx.hankei_n.debug.ExtDebugTree;
import com.github.gfx.hankei_n.debug.StethoDelegator;
import com.github.gfx.hankei_n.dependency.AppComponent;
import com.github.gfx.hankei_n.dependency.AppModule;
import com.github.gfx.hankei_n.dependency.DaggerAppComponent;
import com.github.gfx.hankei_n.model.LocationMemoListMigration;

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

        if (BuildConfig.DEBUG) {
            StethoDelegator.initialize(this);

            registerActivityLifecycleCallbacks(new ActivityLifecycleLogger());
            Timber.plant(new ExtDebugTree());
            Timber.d("start application");
        }

        LocationMemoListMigration.run(this);
    }

    public AppComponent getAppComponent() {
        return component;
    }
}
