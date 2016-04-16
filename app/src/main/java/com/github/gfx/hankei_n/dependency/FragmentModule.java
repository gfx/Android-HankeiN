package com.github.gfx.hankei_n.dependency;

import android.support.v4.app.Fragment;

import dagger.Module;

@Module
public class FragmentModule {

    final Fragment fragment;

    public FragmentModule(Fragment fragment) {
        assert fragment.getContext() != null;
        this.fragment = fragment;
    }
}
