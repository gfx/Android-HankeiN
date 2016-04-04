package com.github.gfx.hankei_n.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.cookpad.android.rxt4a.operators.OperatorAddToCompositeSubscription;
import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.cookpad.android.rxt4a.subscriptions.AndroidCompositeSubscription;
import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.ActivityMainBinding;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.event.LocationMemoChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoFocusedEvent;
import com.github.gfx.hankei_n.event.LocationMemoRemovedEvent;
import com.github.gfx.hankei_n.fragment.EditLocationMemoFragment;
import com.github.gfx.hankei_n.model.CameraState;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.model.PlaceEngine;
import com.github.gfx.hankei_n.model.Prefs;
import com.github.gfx.hankei_n.toolbox.Locations;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
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

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String CATEGORY_LOCATION_MEMO = "LocationMemo";

    public static final String CATEGORY_PERMISSIONS = "Permissions";

    public static final int RC_PERMISSIONS = 0x01;

    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


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
    PublishSubject<LocationChangedEvent> locationChangedSubject;

    @Inject
    PublishSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @Inject
    PublishSubject<LocationMemoRemovedEvent> locationMemoRemovedSubject;

    @Inject
    PublishSubject<LocationMemoChangedEvent> locationMemoChangedSubject;

    @Inject
    PublishSubject<LocationMemoFocusedEvent> locationMemoFocusedSubject;

    @Inject
    LocationMemoManager locationMemos;

    @Inject
    CameraState cameraState;

    @Inject
    MarkerHueAllocator markerHueAllocator;

    final AndroidCompositeSubscription subscription = new AndroidCompositeSubscription();

    ActivityMainBinding binding;

    boolean cameraInitialized = false;

    @Nullable
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
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                initMap(googleMap);
            }
        });
    }

    @DebugLog
    void initMap(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setIndoorEnabled(false); // to avoid StackOverflowError

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addPoint(latLng);
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LocationMemo memo = locationMemos.findMemoByMarker(marker);
                showEditDialog(memo);
                return true;
            }
        });
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                placeEngine.setLocation(cameraPosition.target);
                cameraState.save(cameraPosition);
            }
        });

        loadInitialData();
        setMyLocationEnabled();
    }

    void setMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, RC_PERMISSIONS);
            return;
        }
        assert map != null;
        int extraPadding = (int) getResources().getDimension(R.dimen.toolbar_margin);
        map.setPadding(0, binding.toolbar.getHeight() + extraPadding, extraPadding, 0);
        map.setMyLocationEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSIONS) {
            onRequestLocationPermissionsResult(permissions, grantResults);
        }
    }

    @DebugLog
    void onRequestLocationPermissionsResult(String[] permissions, int[] grantResults) {
        int[] granted2 = {PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED};
        if (Arrays.equals(permissions, PERMISSIONS) && Arrays.equals(grantResults, granted2)) {
            setMyLocationEnabled();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(CATEGORY_PERMISSIONS)
                    .setAction("granted")
                    .build());
        } else {
            Toast.makeText(this, R.string.launched_without_location, Toast.LENGTH_LONG).show();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(CATEGORY_PERMISSIONS)
                    .setAction("denied")
                    .build());
        }
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

    @DebugLog
    void loadInitialData() {
        assert map != null;

        LatLng latLng = cameraState.getLatLng();
        if (Locations.isSomewhere(latLng)) {
            updateCameraPosition(latLng, false);
        }

        for (final LocationMemo memo : locationMemos.all()) {
            if (memo.isPointingNowhere()) {
                placeEngine.getLocationFromAddress(memo.address)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry(1)
                        .subscribe(new Action1<LatLng>() {
                            @Override
                            public void call(LatLng latLng) {
                                memo.latitude = latLng.latitude;
                                memo.longitude = latLng.longitude;
                                addLocationMemo(memo);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Timber.w(throwable, "no location found for %s", memo.address);
                            }
                        });
            } else {
                memo.addMarkerToMap(map);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        placeEngine.start();

        placeEngine.getMyLocationChangedObservable()
                .lift(new OperatorAddToCompositeSubscription<LocationChangedEvent>(subscription))
                .subscribe(new Action1<LocationChangedEvent>() {
                    @Override
                    public void call(LocationChangedEvent event) {
                        onMyLocationChanged(event);
                    }
                });

        locationMemoAddedSubject
                .lift(new OperatorAddToCompositeSubscription<LocationMemoAddedEvent>(subscription))
                .subscribe(new Action1<LocationMemoAddedEvent>() {
                    @Override
                    public void call(LocationMemoAddedEvent event) {
                        addLocationMemo(event.memo);
                    }
                });

        locationMemoRemovedSubject
                .lift(new OperatorAddToCompositeSubscription<LocationMemoRemovedEvent>(subscription))
                .subscribe(new Action1<LocationMemoRemovedEvent>() {
                    @Override
                    public void call(LocationMemoRemovedEvent event) {
                        removeLocationMemo(event.memo);
                    }
                });

        locationMemoFocusedSubject
                .lift(new OperatorAddToCompositeSubscription<LocationMemoFocusedEvent>(subscription))
                .subscribe(new Action1<LocationMemoFocusedEvent>() {
                    @Override
                    public void call(LocationMemoFocusedEvent event) {
                        focusOnMemo(event.memo);
                    }
                });
    }

    private void focusOnMemo(LocationMemo memo) {
        memo.showInfoWindow();
        updateCameraPosition(memo.getLatLng(), true);
        binding.drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onPause() {
        super.onPause();

        placeEngine.stop();
        subscription.unsubscribe();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            case R.id.action_add_memo:
                return newLocationMemo();
            case R.id.action_toggle_satellite:
                return toggleSatellite(item);
            case R.id.action_reset:
                return openResetDialog();
            case R.id.action_about:
                return openAboutThisApp();
            case R.id.action_manage_app:
                return openManageApp();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean newLocationMemo() {
        vibrator.vibrate(60);
        EditLocationMemoFragment.newInstance()
                .show(getSupportFragmentManager(), "edit_location_memo");
        return true;
    }

    private boolean toggleSatellite(MenuItem item) {
        boolean satellite = !item.isChecked();
        item.setChecked(satellite);
        assert map != null;
        map.setMapType(satellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("toggleSatellite")
                .build());
        return true;
    }

    private boolean openResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_reset)
                .setMessage(R.string.message_reset)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reset();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("openResetDialog")
                .build());
        return true;
    }

    private void reset() {
        prefs.resetAll();
        locationMemos.clear();
        cameraInitialized = false;
        restart();

        Toast.makeText(this, R.string.message_reset_done, Toast.LENGTH_SHORT).show();
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
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("openAboutThisApp")
                .build());
        return true;
    }

    private boolean openManageApp() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("openManageApp")
                .build());
        return true;
    }

    public void onMyLocationChanged(LocationChangedEvent event) {
        if (map == null) {
            return;
        }

        if (cameraInitialized) {
            return;
        }
        cameraInitialized = true;

        CameraUpdate update = cameraState.updateCamera(
                event.location,
                event.accurate ? CameraState.ZOOM : CameraState.ZOOM_FOR_NON_ACCURATE_LOCATION
        );
        map.moveCamera(update);
    }

    public void addPoint(LatLng latLng) {
        vibrator.vibrate(100);

        final LocationMemo memo = locationMemos.newMemo(this, markerHueAllocator, latLng);
        addLocationMemo(memo);

        placeEngine.getAddressFromLocation(latLng)
                .retry(1)
                .lift(new OperatorAddToCompositeSubscription<String>(subscription))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                        if (!s.isEmpty()) {
                            memo.address = s;
                            addLocationMemo(memo);
                        }
                    }
                });
    }

    @DebugLog
    private void updateCameraPosition(LatLng latLng, boolean animation) {
        assert map != null;

        cameraInitialized = true;

        final CameraUpdate update = cameraState.updateCamera(latLng);

        if (animation) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
    }

    void showEditDialog(LocationMemo memo) {
        EditLocationMemoFragment.newInstance(memo)
                .show(getSupportFragmentManager(), "edit_location_memo");

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(MainActivity.CATEGORY_LOCATION_MEMO)
                .setAction("showEditDialog")
                .setLabel(TAG)
                .build());
    }

    @DebugLog
    void addLocationMemo(LocationMemo memo) {
        if (memo.id != 0) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(CATEGORY_LOCATION_MEMO)
                    .setAction("add")
                    .build());
        }

        locationMemos.upsert(memo);
        memo = locationMemos.reload(memo);

        // FIXME: ensure GoogleMap is ready
        memo.addMarkerToMap(map);
        memo.showInfoWindow();

        locationMemoChangedSubject.onNext(new LocationMemoChangedEvent());
    }

    @DebugLog
    void removeLocationMemo(LocationMemo memo) {
        locationMemos.remove(memo);

        if (locationMemos.count() == 0) {
            markerHueAllocator.reset();
        }

        locationMemoChangedSubject.onNext(new LocationMemoChangedEvent());

        Toast.makeText(this, R.string.memo_removed, Toast.LENGTH_SHORT).show();

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_LOCATION_MEMO)
                .setAction("remove")
                .build());
    }
}
