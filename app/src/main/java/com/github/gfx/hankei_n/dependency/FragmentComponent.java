package com.github.gfx.hankei_n.dependency;

import com.github.gfx.hankei_n.dependency.scope.FragmentScope;
import com.github.gfx.hankei_n.fragment.EditLocationMemoFragment;
import com.github.gfx.hankei_n.fragment.SidemenuFragment;

import dagger.Subcomponent;

@FragmentScope
@Subcomponent(modules = {FragmentModule.class})
public interface FragmentComponent {

    void inject(EditLocationMemoFragment fragment);

    void inject(SidemenuFragment fragment);
}
