package com.github.gfx.hankei_n.dependency;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class DependencyContainer {

    static AppComponent appComponent;

    public static void initialize(Application app) {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(app))
                .build();
    }

    @NonNull
    public static FragmentComponent getComponent(@NonNull Fragment fragment) {
        return getComponent(fragment.getContext())
                .plus(new FragmentModule(fragment));
    }

    @NonNull
    public static ContextComponent getComponent(@NonNull Context context) {
        return appComponent.plus(new ContextModule(context));
    }
}
