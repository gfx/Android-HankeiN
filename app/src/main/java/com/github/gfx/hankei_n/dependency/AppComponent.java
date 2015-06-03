package com.github.gfx.hankei_n.dependency;


import com.github.gfx.hankei_n.activity.MainActivity;
import com.github.gfx.hankei_n.fragment.EditLocationMemoFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(EditLocationMemoFragment fragment);
}
