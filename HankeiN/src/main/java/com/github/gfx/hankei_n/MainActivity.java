package com.github.gfx.hankei_n;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity implements GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMapLongClickListener {
    private static final String TAG = "MainActivity";

    private static final String projectUrl = "http://github.com/gfx/Android-HankeiN";

    static final float MAP_ZOOM = 14f;

    static final float DEFAULT_RADIUS = 1.5f;

    private Geocoder geocoder;
    private Prefs prefs;

    private boolean myLocationInitialized = false;

    private GoogleMap map;

    private Marker mapMarker;
    private Circle mapCircle;

    @InjectView(R.id.status)
    TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        geocoder = new Geocoder(this, Locale.JAPAN);
        prefs = new Prefs(this);

        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);

        map.setOnMyLocationChangeListener(this);
        map.setOnMapLongClickListener(this);

        setTitle(getRadius());
    }

    @Override
    protected void onResume() {
        super.onResume();

        final float prevLatitude = prefs.get("prevLatitude", 0.0f);
        final float prevLongitude = prefs.get("prevLongitude", 0.0f);

        if (prevLatitude != 0.0f || prevLongitude != 0.0) {
            setMyLocation(prevLatitude, prevLongitude, false);
        }

        final float pointedLatitude = prefs.get("pointedLatitude", 0.0f);
        final float pointedLongitude = prefs.get("pointedLongitude", 0.0f);

        if (pointedLatitude != 0.0f || pointedLongitude != 0.0) {
            updatePoint(new LatLng(pointedLatitude, pointedLongitude));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return openSettingView();
            case R.id.action_about:
                return openAboutThisApp();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean openSettingView() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("半径の設定 (km)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(getRadius()));
        dialog.setView(input);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    float value = Float.parseFloat(input.getText().toString());
                    setRadius(value);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "エラー: 数値を入力してください", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
        return true;
    }

    private boolean openAboutThisApp() {
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(projectUrl));
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(100);

        prefs.put("pointedLatitude", (float) latLng.latitude);
        prefs.put("pointedLongitude", (float) latLng.longitude);

        updatePoint(latLng);
    }

    private String getAddrFromLatLng(LatLng latLng) {
        try {
            final List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            final Address address = addresses.get(0);

            final List<String> names = new ArrayList<>();
            for (int i = Math.min(1, address.getMaxAddressLineIndex()); i <= address.getMaxAddressLineIndex(); i++) {
                names.add(address.getAddressLine(i));
            }

            return TextUtils.join(" ", names);
        } catch (Exception e) {
            Log.wtf(TAG, e);
            return String.format("(%.02f, %.02f)", latLng.latitude, latLng.longitude);
        }
    }

    private void updatePoint(final LatLng latLng) {
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (mapMarker != null) {
            mapMarker.remove();
        }
        mapMarker = map.addMarker(markerOptions);

        final CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(getRadius() * 1000);
        circleOptions.strokeWidth(1);
        circleOptions.strokeColor(0x990099ee);
        circleOptions.fillColor(0x110099ee);
        if (mapCircle != null) {
            mapCircle.remove();
        }
        mapCircle = map.addCircle(circleOptions);

        final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
        uiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final String addrName = getAddrFromLatLng(latLng);
                mapMarker.setTitle(addrName);
                statusView.setText(addrName);
            }
        }, 10);
    }

    @Override
    public void onMyLocationChange(Location location) {
        prefs.put("prevLatitude", (float) location.getLatitude());
        prefs.put("prevLongitude", (float) location.getLongitude());

        if (myLocationInitialized) return;
        myLocationInitialized = true;

        setMyLocation(location.getLatitude(), location.getLongitude(), true);
    }

    private void setMyLocation(double lat, double lng, boolean animation) {
        final LatLng latlng = new LatLng(lat, lng);
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, MAP_ZOOM);

        if (animation) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
    }

    private void setTitle(float radius) {
        setTitle(getResources().getString(R.string.app_title_template, radius));
    }

    private float getRadius() {
        return prefs.get("radius", DEFAULT_RADIUS);
    }

    private void setRadius(float radius) {
        setTitle(radius);
        prefs.put("radius", radius);

        final LatLng latLng = new LatLng(prefs.get("pointedLatitude", 0f), prefs.get("pointedLongitude", 0f));
        updatePoint(latLng);
    }
}
