package com.github.gfx.hankei_n;

import com.google.firebase.crash.FirebaseCrash;

import com.github.gfx.hankei_n.debug.ActivityLifecycleLogger;
import com.github.gfx.hankei_n.debug.ExtDebugTree;
import com.github.gfx.hankei_n.debug.StethoDelegator;
import com.github.gfx.hankei_n.dependency.DependencyContainer;
import com.github.gfx.hankei_n.model.LocationMemoListMigration;

import android.app.Application;
import android.util.Log;

import javax.annotation.ParametersAreNonnullByDefault;

import timber.log.Timber;

@ParametersAreNonnullByDefault
public class HankeiNApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DependencyContainer.initialize(this);

        Timber.plant(new Timber.Tree() {
            @Override
            protected void log(int priority, String tag, String message, Throwable t) {
                if (priority == Log.WARN || priority == Log.ERROR) {
                    FirebaseCrash.log(tag + "/" + message);
                    FirebaseCrash.report(t);
                }
            }
        });

        if (BuildConfig.DEBUG) {
            StethoDelegator.initialize(this);

            registerActivityLifecycleCallbacks(new ActivityLifecycleLogger());
            Timber.plant(new ExtDebugTree());
            Timber.d("start application");
        }

        LocationMemoListMigration.run(this);
    }
}
