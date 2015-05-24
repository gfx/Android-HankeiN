package com.github.gfx.hankei_n;


import dagger.Component;

@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(MainActivity activity);
}
