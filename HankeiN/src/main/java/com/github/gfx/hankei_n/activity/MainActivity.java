package com.github.gfx.hankei_n.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.Prefs;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.MyLocationChangedEvent;
import com.github.gfx.hankei_n.fragment.EditLocationMemoFragment;
import com.github.gfx.hankei_n.model.LocationMemoList;
import com.github.gfx.hankei_n.model.SingleMarker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    static final float MAP_ZOOM = 14f;

    static final float DEFAULT_RADIUS = 1.5f;

    static final int MARKER_COLOR = 0x00ff66;

    @Inject
    Geocoder geocoder;

    @Inject
    Prefs prefs;

    @Inject
    Vibrator vibrator;

    @Inject
    Tracker tracker;

    @Inject
    GoogleApiAvailability googleApiAvailability;

    @Inject
    BehaviorSubject<MyLocationChangedEvent> myLocationChangedSubject;

    @Inject
    LocationMemoList memos;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.status)
    TextView statusView;

    @InjectView(R.id.drawer)
    DrawerLayout drawer;

    @InjectView(R.id.list_location_memos)
    ListView memosListView;

    boolean cameraInitialized = false;

    GoogleMap map;

    SingleMarker marker;

    ActionBarDrawerToggle drawerToggle;

    LatLng myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long t0 = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        HankeiNApplication.getAppComponent(this).inject(this);

        setAppTitle(getRadius());

        setupDrawer();
        setupMap();
        checkGooglePlayServices();

        tracker.send(
                new HitBuilders.TimingBuilder()
                        .setLabel("MainActivity#onCreate")
                        .setValue(System.currentTimeMillis() - t0)
                        .build());
    }

    void setupDrawer() {
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this,
                drawer,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(drawerToggle);
    }

    void setupMap() {
        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
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
        googleApiAvailability.showErrorDialogFragment(this, errorCode, 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Initialize the camera position.
     * (1) initial start -> Does nothing. Changes camera position on onMyLocationChange().
     * (2) resume        -> Starts with the saved position and then moves to my location on onMyLocationChange().
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (map != null) {
            load();
        }
    }

    void load() {
        final float pointedLatitude = prefs.get("pointedLatitude", 0.0f);
        final float pointedLongitude = prefs.get("pointedLongitude", 0.0f);

        if (pointedLatitude != 0.0f || pointedLongitude != 0.0) {
            updatePoint(new LatLng(pointedLatitude, pointedLongitude));
        }

        final float prevLatitude = prefs.get("prevLatitude", 0.0f);
        final float prevLongitude = prefs.get("prevLongitude", 0.0f);

        if (prevLatitude != 0.0f || prevLongitude != 0.0) {
            final float zoom = prefs.get("prevCameraZoom", MAP_ZOOM);
            setMyLocation(prevLatitude, prevLongitude, false, zoom);

            final String addressName = prefs.get("addressName", (String) null);
            if (addressName != null) {
                setStatusText(addressName);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        final CameraPosition pos = map.getCameraPosition();
        prefs.put("prevLatitude", (float) pos.target.latitude);
        prefs.put("prevLongitude", (float) pos.target.longitude);
        prefs.put("prevCameraZoom", pos.zoom);
        cameraInitialized = false;
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
                return openResetView();
            case R.id.action_about:
                return openAboutThisApp();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.add_location_memo)
    void onAddLocationMemo() {
        FragmentManager fm = getSupportFragmentManager();

        EditLocationMemoFragment.newInstance(myLocation)
                .show(fm, "edit_location_memo");
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
                    Toast.makeText(MainActivity.this, "エラー: 数値を入力してください", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
        return true;
    }

    private boolean openResetView() {
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
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        return true;
    }

    public void onMyLocationChange(Location location) {
        prefs.put("prevLatitude", (float) location.getLatitude());
        prefs.put("prevLongitude", (float) location.getLongitude());

        if (cameraInitialized) {
            return;
        }
        cameraInitialized = true;

        setMyLocation(location.getLatitude(), location.getLongitude(), true, prefs.get("prevCameraZoom", MAP_ZOOM));
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

    private Observable<String> getAddrFromLatLng(final LatLng latLng) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                final List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    subscriber.onError(e);
                    return;
                }

                if (addresses.isEmpty()) {
                    subscriber.onNext(defaultPlaceName(latLng));
                } else {
                    final Address address = addresses.get(0);

                    final List<String> names = new ArrayList<>();
                    for (int i = Math.min(1, address.getMaxAddressLineIndex()); i <= address.getMaxAddressLineIndex(); i++) {
                        names.add(address.getAddressLine(i));
                    }

                    subscriber.onNext(TextUtils.join(" ", names));
                }

                subscriber.onCompleted();
            }
        });
    }

    private void updatePoint(final LatLng latLng) {
        marker.move(latLng);

        getAddrFromLatLng(latLng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return getAddrFromLatLng(latLng); // retry once
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
        statusView.setText(addressName);
    }

    private void setMyLocation(double lat, double lng, boolean animation, float zoom) {
        myLocation = new LatLng(lat, lng);
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLocation, zoom);

        if (animation) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }

        myLocationChangedSubject.onNext(new MyLocationChangedEvent(myLocation));
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
}
