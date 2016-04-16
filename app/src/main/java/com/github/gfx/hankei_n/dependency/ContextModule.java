package com.github.gfx.hankei_n.dependency;

import android.content.Context;

import dagger.Module;

@Module
public class ContextModule {

    final Context context;

    public ContextModule(Context context) {
        this.context = context;
    }
}
