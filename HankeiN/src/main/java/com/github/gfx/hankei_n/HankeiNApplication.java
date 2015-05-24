package com.github.gfx.hankei_n;

import android.app.Application;

public class HankeiNApplication extends Application {

    public static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }
}
