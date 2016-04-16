package com.github.gfx.hankei_n;

import com.github.gfx.hankei_n.debug.ActivityLifecycleLogger;
import com.github.gfx.hankei_n.debug.ExtDebugTree;
import com.github.gfx.hankei_n.debug.StethoDelegator;
import com.github.gfx.hankei_n.dependency.DependencyContainer;
import com.github.gfx.hankei_n.model.LocationMemoListMigration;

import android.app.Application;

import javax.annotation.ParametersAreNonnullByDefault;

import timber.log.Timber;

@ParametersAreNonnullByDefault
public class HankeiNApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DependencyContainer.initialize(this);

        if (BuildConfig.DEBUG) {
            StethoDelegator.initialize(this);

            registerActivityLifecycleCallbacks(new ActivityLifecycleLogger());
            Timber.plant(new ExtDebugTree());
            Timber.d("start application");
        }

        LocationMemoListMigration.run(this);
    }
}
