package com.github.gfx.hankei_n.debug;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import timber.log.Timber;

public class ActivityLifecycleLogger implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Timber.tag(activity.getClass().getSimpleName());
        Timber.v("onCreate");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Timber.tag(activity.getClass().getSimpleName());
        Timber.v("onStart");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Timber.tag(activity.getClass().getSimpleName());
        Timber.v("onResume");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Timber.tag(activity.getClass().getSimpleName());
        Timber.v("onPause");

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Timber.tag(activity.getClass().getSimpleName());
        Timber.v("onStop");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
