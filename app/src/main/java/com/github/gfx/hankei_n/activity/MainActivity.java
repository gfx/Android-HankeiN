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
import com.github.gfx.hankei_n.event.LocationMemoChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoRemovedEvent;
import com.github.gfx.hankei_n.fragment.EditLocationMemoFragment;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.model.MyLocationState;
import com.github.gfx.hankei_n.model.PlaceEngine;
import com.github.gfx.hankei_n.model.Prefs;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String CATEGORY_LOCATION_MEMO = "LocationMemo";

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

    @Inject
    BehaviorSubject<LocationChangedEvent> locationChangedSubject;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @Inject
    BehaviorSubject<LocationMemoRemovedEvent> locationMemoRemovedSubject;

    @Inject
    BehaviorSubject<LocationMemoChangedEvent> locationMemoChangedSubject;

    @Inject
    LocationMemoManager locationMemos;

    @Inject
    MyLocationState myLocationState;

    @Inject
    MarkerHueAllocator markerHueAllocator;

    final AndroidCompositeSubscription subscription = new AndroidCompositeSubscription();

    ActivityMainBinding binding;

    boolean cameraInitialized = false;

    GoogleMap map;

    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        long t0 = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        HankeiNApplication.getAppComponent(this).inject(this);

        setupDrawer();
        checkGooglePlayServices();
        setupMap();

        tracker.send(new HitBuilders.TimingBuilder()
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
        binding.drawer.addDrawerListener(drawerToggle);
    }


    void setupMap() {
        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Timber.d("onMapReady");

                MainActivityPermissionsDispatcher.initMapWithCheck(MainActivity.this, googleMap);
            }
        });
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void initMap(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            throw new AssertionError("never reached");
        }
        map.setMyLocationEnabled(true);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MainActivity.this.onMapLongClick(latLng);
            }
        });

        loadInitialData();
    }

    @OnShowRationale({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showRationaleForLocation(final PermissionRequest request) {
        request.proceed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
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

    void loadInitialData() {
        LatLng latLng = myLocationState.getLatLng();
        if (latLng.latitude != 0.0f || latLng.longitude != 0.0) {
            updateCameraPosition(latLng, myLocationState.getCameraZoom(), false);
        }

        for (LocationMemo memo : locationMemos.all()) {
            addLocationMemo(memo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        placeEngine.getMyLocationChangedObservable()
                .lift(new OperatorAddToCompositeSubscription<LocationChangedEvent>(subscription))
                .subscribe(new Action1<LocationChangedEvent>() {
            @Override
            public void call(LocationChangedEvent locationChangedEvent) {
                onMyLocationChange(locationChangedEvent.location);
            }
        });

        placeEngine.start();

        locationMemoAddedSubject
                .lift(new OperatorAddToCompositeSubscription<LocationMemoAddedEvent>(subscription))
                .subscribe(new Action1<LocationMemoAddedEvent>() {
                    @Override
                    public void call(LocationMemoAddedEvent locationMemoAddedEvent) {
                        addLocationMemo(locationMemoAddedEvent.memo);
                    }
                });

        locationMemoRemovedSubject
                .lift(new OperatorAddToCompositeSubscription<LocationMemoRemovedEvent>(subscription))
                .subscribe(new Action1<LocationMemoRemovedEvent>() {
                    @Override
                    public void call(LocationMemoRemovedEvent locationMemoRemovedEvent) {
                        removeLocationMemo(locationMemoRemovedEvent.memo);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();

        placeEngine.stop();

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
        locationMemos.clear();

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

    public void onMyLocationChange(LatLng latLng) {
        if (cameraInitialized) {
            return;
        }
        cameraInitialized = true;

        updateCameraPosition(latLng, myLocationState.getCameraZoom(), true);
    }

    public void onMapLongClick(final LatLng latLng) {
        vibrator.vibrate(100);

        LocationMemo memo = new LocationMemo("", "", latLng, 0, markerHueAllocator.allocate());
        final EditLocationMemoFragment fragment = EditLocationMemoFragment.newInstance(memo);
        fragment.show(getSupportFragmentManager(), "edit_location_memo");

        placeEngine.getAddressFromLocation(latLng)
                .lift(new OperatorAddToCompositeSubscription<String>(subscription))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .onErrorReturn(new Func1<Throwable, String>() {
                    @Override
                    public String call(Throwable throwable) {
                        Timber.w(throwable, "Failed to get address from location");
                        return "";
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        fragment.initAddress(s);
                    }
                });
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

    void addLocationMemo(LocationMemo memo) {
        locationMemos.upsert(memo);
        memo.addMarkerToMap(map);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_LOCATION_MEMO)
                .setAction("add")
                .build());

        locationMemoChangedSubject.onNext(new LocationMemoChangedEvent());
    }

    void removeLocationMemo(LocationMemo memo) {
        locationMemos.remove(memo);

        if (locationMemos.count() == 0) {
            markerHueAllocator.reset();
        }

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_LOCATION_MEMO)
                .setAction("remove")
                .build());

        locationMemoChangedSubject.onNext(new LocationMemoChangedEvent());
    }
}
