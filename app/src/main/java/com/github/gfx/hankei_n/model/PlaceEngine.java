package com.github.gfx.hankei_n.model;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.toolbox.Locations;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class PlaceEngine {

    final Context context;

    final GoogleApiClient googleApiClient;

    final BehaviorSubject<LocationChangedEvent> locationChangedSubject = BehaviorSubject.create();

    final Geocoder geocoder;

    LatLng location = Locations.NOWHERE;

    @Inject
    public PlaceEngine(Context context, Geocoder geocoder, GoogleApiClient googleApiClient) {
        this.context = context;
        this.geocoder = geocoder;
        this.googleApiClient = googleApiClient;

        googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                handleLastLocation();
            }

            @Override
            public void onConnectionSuspended(int i) {
                Timber.i("GoogleApiClient connection suspended");
            }
        });
        googleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Timber.w("GoogleApiClient connection failed: %s", connectionResult.getErrorMessage());
            }
        });
    }

    public Observable<LocationChangedEvent> getMyLocationChangedObservable() {
        return locationChangedSubject;
    }

    private void handleLastLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            guessCurrentLocation();
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
            castMyLocation(Locations.createLatLng(currentLocation), true);
        } else {
            guessCurrentLocation();
        }
    }

    private void guessCurrentLocation() {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        Observable.just(
                new Locale("", telephony.getNetworkCountryIso()).getDisplayCountry(),
                new Locale("", telephony.getSimCountryIso()).getDisplayCountry(),
                TimeZone.getDefault().getID(),
                Locale.getDefault().getDisplayCountry()
        )
                .onBackpressureBuffer(1)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return !s.isEmpty();
                    }
                })
                .flatMap(new Func1<String, Observable<LatLng>>() {
                    @Override
                    public Observable<LatLng> call(String hint) {
                        return getLocationFromAddress(hint);
                    }
                })
                .filter(new Func1<LatLng, Boolean>() {
                    @Override
                    public Boolean call(LatLng latLng) {
                        return Locations.isSomewhere(latLng);
                    }
                })
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LatLng>() {
                    @Override
                    public void call(LatLng latLng) {
                        castMyLocation(latLng, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.w(throwable, "no location found in guessCurrentLocation");
                    }
                });
    }

    private void castMyLocation(LatLng latLng, boolean accurate) {
        locationChangedSubject.onNext(new LocationChangedEvent(latLng, accurate));
    }

    public void start() {
        googleApiClient.connect();
    }

    public void stop() {
        googleApiClient.disconnect();
    }

    public void setLocation(@NonNull LatLng latLng) {
        this.location = latLng;
    }

    public Observable<Iterable<AutocompletePrediction>> queryAutocompletion(final String s) {
        if (!googleApiClient.isConnected()) {
            Timber.w("GoogleApiClient is not connected");
            return Observable.empty();
        }

        return Observable.create(new Observable.OnSubscribe<Iterable<AutocompletePrediction>>() {
            @Override
            public void call(final Subscriber<? super Iterable<AutocompletePrediction>> subscriber) {
                LatLngBounds bounds = LatLngBounds.builder()
                        .include(location)
                        .build();

                final PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi
                        .getAutocompletePredictions(googleApiClient, s, bounds, null);

                result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
                        subscriber.onNext(autocompletePredictions);
                        autocompletePredictions.release();
                        subscriber.onCompleted();
                    }
                });
            }
        });
    }

    // Geocoding

    @DebugLog
    public Observable<String> getAddressFromLocation(final LatLng latLng) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                final List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    subscriber.onError(new GeocodingException(e));
                    return;
                }

                if (addresses.isEmpty()) {
                    subscriber.onError(new GeocodingException("No addresses found for: " + latLng));
                } else {
                    final Address address = addresses.get(0);

                    final List<String> names = new ArrayList<>();
                    for (int i = Math.min(1, address.getMaxAddressLineIndex()); i <= address.getMaxAddressLineIndex(); i++) {
                        names.add(address.getAddressLine(i));
                    }

                    subscriber.onNext(TextUtils.join(" ", names));
                    subscriber.onCompleted();
                }
            }
        });
    }

    @DebugLog
    public Observable<LatLng> getLocationFromAddress(final String address) {
        return Observable.create(new Observable.OnSubscribe<LatLng>() {
            @Override
            public void call(Subscriber<? super LatLng> subscriber) {
                final List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocationName(address, 1);
                } catch (IOException e) {
                    subscriber.onError(new GeocodingException(e));
                    return;
                }

                if (addresses.isEmpty()) {
                    subscriber.onError(new GeocodingException("No location found for: " + address));
                } else {
                    Address address = addresses.get(0);
                    subscriber.onNext(Locations.createLatLng(address));
                    subscriber.onCompleted();
                }
            }
        });
    }

    public static class GeocodingException extends Exception {

        public GeocodingException(String detailMessage) {
            super(detailMessage);
        }

        public GeocodingException(Throwable throwable) {
            super(throwable);
        }
    }
}
