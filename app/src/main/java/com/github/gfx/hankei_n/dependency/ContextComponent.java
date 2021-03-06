package com.github.gfx.hankei_n.dependency;

import com.github.gfx.hankei_n.activity.AboutActivity;
import com.github.gfx.hankei_n.activity.MainActivity;
import com.github.gfx.hankei_n.dependency.scope.ContextScope;

import dagger.Subcomponent;

@ContextScope
@Subcomponent(modules = {ContextModule.class})
public interface ContextComponent {

    void inject(MainActivity activity);

    void inject(AboutActivity activity);

    FragmentComponent plus(FragmentModule module);
}
