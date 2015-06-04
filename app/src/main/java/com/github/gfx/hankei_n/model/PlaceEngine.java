package com.github.gfx.hankei_n.model;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.github.gfx.hankei_n.event.LocationChangedEvent;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class PlaceEngine {

    final GoogleApiClient googleApiClient;

    final Observable<LocationChangedEvent> locationChangedObservable;

    final Geocoder geocoder;

    Subscription subscription;

    LatLng location;

    public PlaceEngine(Context context, Geocoder geocoder, Observable<LocationChangedEvent> locationChangedEventObservable) {
        this.geocoder = geocoder;
        this.locationChangedObservable = locationChangedEventObservable;

        ConnectionHandler handler = new ConnectionHandler();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(handler)
                .addOnConnectionFailedListener(handler)
                .build();
    }

    public void start() {
        googleApiClient.connect();

        subscription = locationChangedObservable.subscribe(new Action1<LocationChangedEvent>() {
            @Override
            public void call(LocationChangedEvent myLocationChanged) {
                setLocation(myLocationChanged.location);
            }
        });
    }

    public void stop() {
        googleApiClient.disconnect();

        subscription.unsubscribe();
        subscription = null;
    }

    public void setLocation(LatLng latLng) {
        this.location = latLng;
    }

    public Observable<Iterable<AutocompletePrediction>> queryAutocompletion(final String s) {
        if (location == null) {
            Timber.w("no location set");
            return Observable.empty();
        }

        if (!googleApiClient.isConnected()) {
            Timber.w("not connected");
            return Observable.empty();
        }

        final LatLngBounds bounds = LatLngBounds.builder()
                .include(location)
                .build();

        //List<Integer> types = Arrays.asList();
        //final AutocompleteFilter filter = AutocompleteFilter.create(types);
        final AutocompleteFilter filter = null;

        return Observable.create(new Observable.OnSubscribe<Iterable<AutocompletePrediction>>() {
            @Override
            public void call(final Subscriber<? super Iterable<AutocompletePrediction>> subscriber) {
                final PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi
                        .getAutocompletePredictions(googleApiClient, s, bounds, filter);

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

    class ConnectionHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnected(Bundle bundle) {
            Timber.d("onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Timber.d("onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Timber.d("onConnectionFailed");
        }
    }

    // Geocoder

    public Observable<String> getAddrFromLatLng(final LatLng latLng) {
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
                    subscriber.onError(new GeocodingException("No addresses"));
                    return;
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

    public static class GeocodingException extends Exception {

        public GeocodingException(String detailMessage) {
            super(detailMessage);
        }

        public GeocodingException(Throwable throwable) {
            super(throwable);
        }
    }
}
