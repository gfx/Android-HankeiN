package com.github.gfx.hankei_n;

import com.github.gfx.hankei_n.dependency.AppComponent;
import com.github.gfx.hankei_n.dependency.AppModule;
import com.github.gfx.hankei_n.dependency.DaggerAppComponent;

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
