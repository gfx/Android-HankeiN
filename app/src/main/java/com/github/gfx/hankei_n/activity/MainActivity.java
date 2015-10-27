package com.github.gfx.hankei_n.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import com.cookpad.android.rxt4a.operators.OperatorAddToCompositeSubscription;
import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.cookpad.android.rxt4a.subscriptions.AndroidCompositeSubscription;
import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.ActivityMainBinding;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.model.MyLocationState;
import com.github.gfx.hankei_n.model.PlaceEngine;
import com.github.gfx.hankei_n.model.Prefs;
import com.github.gfx.hankei_n.model.SingleMarker;
import com.github.gfx.hankei_n.toolbox.RuntimePermissions;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class MainActivity extends AppCompatActivity {

    static final float MAP_ZOOM = 14f;

    static final float DEFAULT_RADIUS = 1.5f;

    static final int MARKER_COLOR = 0x00ff66;

    private static final String TAG = MainActivity.class.getSimpleName();

    final AndroidCompositeSubscription subscription = new AndroidCompositeSubscription();

    @Inject
    PlaceEngine placeEngine;

    @Inject
    Prefs prefs;

    @Inject
    Vibrator vibrator;

    @Inject
    Tracker tracker;

    @Inject
    GoogleApiAvailability googleApiAvailability;

    /**
     * An event stream to tell where the location marker is.
     */
    @Inject
    BehaviorSubject<LocationChangedEvent> locationChangedSubject;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @Inject
    LocationMemoManager locationMemos;

    @Inject
    MyLocationState myLocationState;

    final RuntimePermissions runtimePermissions = new RuntimePermissions(this);

    ActivityMainBinding binding;

    boolean cameraInitialized = false;

    GoogleMap map;

    SingleMarker marker;

    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        long t0 = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        HankeiNApplication.getAppComponent(this).inject(this);

        setAppTitle(getRadius());

        setupDrawer();
        checkGooglePlayServices();
        runtimePermissions.confirm();
        setupMap();

        tracker.send(
                new HitBuilders.TimingBuilder()
                        .setCategory(TAG)
                        .setVariable("onCreate")
                        .setValue(System.currentTimeMillis() - t0)
                        .build());
    }

    void setupDrawer() {
        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this,
                binding.drawer,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        binding.drawer.setDrawerListener(drawerToggle);
    }


    void setupMap() {
        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Timber.d("onMapReady");

                map = googleMap;
                map.setMyLocationEnabled(true);

                map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        MainActivity.this.onMyLocationChange(location);
                    }
                });
                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        MainActivity.this.onMapLongClick(latLng);
                    }
                });

                marker = new SingleMarker(map, getRadius(), MARKER_COLOR);

                load();
            }
        });
    }

    void checkGooglePlayServices() {
        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.showErrorNotification(this, errorCode);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    void load() {
        assert map != null;

        LatLng latLng = myLocationState.getLatLng();
        if (latLng.latitude != 0.0f || latLng.longitude != 0.0) {
            updateCameraPosition(latLng, myLocationState.getCameraZoom(), false);
        }

        final String addressName = prefs.get("addressName", (String) null);
        if (addressName != null) {
            setStatusText(addressName);
        }

        final float pointedLatitude = prefs.get("pointedLatitude", 0.0f);
        final float pointedLongitude = prefs.get("pointedLongitude", 0.0f);

        if (pointedLatitude != 0.0f || pointedLongitude != 0.0) {
            updatePoint(new LatLng(pointedLatitude, pointedLongitude));
        }

        for (LocationMemo memo : locationMemos.all()) {
            addLocationMemo(memo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationMemoAddedSubject
                .lift(new OperatorAddToCompositeSubscription<LocationMemoAddedEvent>(subscription))
                .subscribe(new Subscriber<LocationMemoAddedEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LocationMemoAddedEvent locationMemoAddedEvent) {
                        LocationMemo memo = locationMemoAddedEvent.memo;
                        addLocationMemo(memo);
                    }
                });

    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraInitialized = false;

        subscription.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                return openSettingView();
            case R.id.action_reset:
                return openResetDialog();
            case R.id.action_about:
                return openAboutThisApp();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean openSettingView() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.main_setting_radius_km);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(getRadius()));
        dialog.setView(input);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    final float value = Float.parseFloat(String.valueOf(input.getText()));
                    setRadius(value);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, R.string.errro_it_must_be_a_number, Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
        return true;
    }

    private boolean openResetDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.action_reset);

        dialog.setMessage(R.string.message_reset);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
        return true;
    }

    private void reset() {
        prefs.resetAll();

        Toast.makeText(this, R.string.message_reset_done, Toast.LENGTH_SHORT).show();

        restart();
    }

    private void restart() {
        final PackageManager pm = getPackageManager();
        assert pm != null;
        final Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        assert intent != null;
        startActivity(intent);
    }

    private boolean openAboutThisApp() {
        final Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(getString(R.string.project_url)));
        startActivity(intent);

        return true;
    }

    public void onMyLocationChange(Location location) {
        if (cameraInitialized) {
            return;
        }
        cameraInitialized = true;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateCameraPosition(latLng, myLocationState.getCameraZoom(), true);
    }

    public void onMapLongClick(LatLng latLng) {
        vibrator.vibrate(100);

        prefs.put("pointedLatitude", (float) latLng.latitude);
        prefs.put("pointedLongitude", (float) latLng.longitude);

        updatePoint(latLng);
    }

    private String defaultPlaceName(LatLng latLng) {
        return String.format(Locale.getDefault(), "(%.02f, %.02f)", latLng.latitude, latLng.longitude);
    }

    private void updatePoint(final LatLng latLng) {
        marker.move(latLng);

        placeEngine.getAddressFromLocation(latLng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return placeEngine.getAddressFromLocation(latLng); // retry once
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                        setStatusText(s);
                        prefs.put("addressName", s);
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.wtf(TAG, e);

                        String s = defaultPlaceName(latLng);
                        setStatusText(s);
                        prefs.put("addressName", s);
                    }

                });
    }

    private void setStatusText(String addressName) {
        marker.setTitle(addressName);
        binding.status.setText(addressName);
    }

    private void updateCameraPosition(LatLng latLng, float zoom, boolean animation) {
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);

        if (animation) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
        locationChangedSubject.onNext(new LocationChangedEvent(latLng));

        myLocationState.save(latLng, zoom);

        Timber.d("updateCameraPosition lat/lng: (%.02f, %.02f)", latLng.latitude, latLng.longitude);
    }

    private void setAppTitle(float radius) {
        setTitle(getResources().getString(R.string.app_title_template, radius));
    }

    private float getRadius() {
        return prefs.get("radius", DEFAULT_RADIUS);
    }

    private void setRadius(float radius) {
        marker.setRadius(radius);
        setAppTitle(radius);
        prefs.put("radius", radius);

        final LatLng latLng = new LatLng(prefs.get("pointedLatitude", 0f), prefs.get("pointedLongitude", 0f));
        updatePoint(latLng);
    }

    void addLocationMemo(LocationMemo memo) {
        map.addMarker(memo.buildMarkerOptions());
    }
}
