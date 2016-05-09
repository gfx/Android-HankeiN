package com.github.gfx.hankei_n.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.github.gfx.hankei_n.BuildConfig;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.ActivityAboutBinding;
import com.github.gfx.hankei_n.dependency.DependencyContainer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import javax.inject.Inject;

public class AboutActivity extends AppCompatActivity {

    static final String TAG = AboutActivity.class.getSimpleName();

    @Inject
    Tracker tracker;

    public static Intent createIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DependencyContainer.getComponent(this).inject(this);

        ActivityAboutBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        binding.setActivity(this);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        binding.textAboutThisApp.setText(getString(R.string.about_app_description, getString(R.string.app_name),
                BuildConfig.VERSION_NAME));
    }

    public void openProjectPage(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(getString(R.string.project_url)));
        startActivity(intent);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("openProjectPage")
                .build());
    }

    public void openAppInfo(View view) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("openAppInfo")
                .build());
    }


    public void openWithPlayStore(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("market://details?id=" + getPackageName().replace(".debug", "")));
        startActivity(intent);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("openWithPlayStore")
                .build());
    }

}
