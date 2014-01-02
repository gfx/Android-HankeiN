package com.github.gfx.hankei_n;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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

    static final int MARKER_COLOR = 0x0099ff;

    private Geocoder geocoder;
    private Prefs prefs;

    private boolean cameraInitialized = false;

    private GoogleMap map;

    private SingleMarker marker;

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

        marker = new SingleMarker(map, getRadius(), MARKER_COLOR);

        setAppTitle(getRadius());
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

    /**
     * Initialize the camera position.
     * (1) initial start -> Does nothing. Changes camera position on onMyLocationChange().
     * (2) resume        -> Starts with the saved position and then moves to my location on onMyLocationChange().
     */
    @Override
    protected void onResume() {
        super.onResume();

        /* marker location */
        final float pointedLatitude = prefs.get("pointedLatitude", 0.0f);
        final float pointedLongitude = prefs.get("pointedLongitude", 0.0f);

        if (pointedLatitude != 0.0f || pointedLongitude != 0.0) {
            updatePoint(new LatLng(pointedLatitude, pointedLongitude));
        }

        /* camera location */
        final float prevLatitude = prefs.get("prevLatitude", 0.0f);
        final float prevLongitude = prefs.get("prevLongitude", 0.0f);

        if (prevLatitude != 0.0f || prevLongitude != 0.0) {
            final float zoom = prefs.get("prevCameraZoom", MAP_ZOOM);
            setMyLocation(prevLatitude, prevLongitude, false, zoom);

            final String addressName = prefs.get("addressName", (String)null);
            if (addressName != null) {
                setStatusText(addressName);
            }
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
            case R.id.action_reset:
                 return openResetView();
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

        // restart the app
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private boolean openAboutThisApp() {
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(projectUrl));
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        return true;
    }

    @Override
    public void onMyLocationChange(Location location) {
        prefs.put("prevLatitude", (float) location.getLatitude());
        prefs.put("prevLongitude", (float) location.getLongitude());

        if (cameraInitialized) return;
        cameraInitialized = true;

        setMyLocation(location.getLatitude(), location.getLongitude(), true, prefs.get("prevCameraZoom", MAP_ZOOM));
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
            return String.format(Locale.getDefault(), "(%.02f, %.02f)", latLng.latitude, latLng.longitude);
        }
    }

    private void updatePoint(final LatLng latLng) {
        marker.move(latLng);

        // update title and status text
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return getAddrFromLatLng(latLng);
            }

            @Override
            protected void onPostExecute(String addrName) {
                setStatusText(addrName);
                prefs.put("addressName", addrName);
            }
        }.execute((Void) null);
    }

    private void setStatusText(String addressName) {
        marker.setTitle(addressName);
        statusView.setText(addressName);
    }


    private void setMyLocation(double lat, double lng, boolean animation, float zoom) {
        final LatLng latlng = new LatLng(lat, lng);
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, zoom);

        if (animation) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
    }

    private void setAppTitle(float radius) {
        setTitle(getResources().getString(R.string.app_title_template, radius));
    }

    private float getRadius() {
        return prefs.get("radius", DEFAULT_RADIUS);
    }

    private void setRadius(float radius) {
        setRadius(radius);
        setAppTitle(radius);
        prefs.put("radius", radius);

        final LatLng latLng = new LatLng(prefs.get("pointedLatitude", 0f), prefs.get("pointedLongitude", 0f));
        updatePoint(latLng);
    }
}
