package com.github.gfx.hankei_n.dependency;


import com.github.gfx.hankei_n.activity.MainActivity;

import dagger.Component;

@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(MainActivity activity);
}
