package com.github.gfx.hankei_n.dependency;


import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    ContextComponent plus(ContextModule module);
}

