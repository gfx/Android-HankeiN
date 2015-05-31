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

import com.github.gfx.hankei_n.event.MyLocationChangedEvent;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import javax.annotation.ParametersAreNonnullByDefault;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

@ParametersAreNonnullByDefault
public class AddressAutocompleteEngine {

    static final String TAG = AddressAutocompleteEngine.class.getSimpleName();

    final GoogleApiClient googleApiClient;

    final Observable<MyLocationChangedEvent> myLocationChangedObservable;

    Subscription subscription;

    LatLng location;

    public AddressAutocompleteEngine(Context context, Observable<MyLocationChangedEvent> myLocationChangedObservable) {
        ConnectionHandler handler = new ConnectionHandler();

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(handler)
                .addOnConnectionFailedListener(handler)
                .build();

        this.myLocationChangedObservable = myLocationChangedObservable;
    }

    public void start() {
        googleApiClient.connect();

        subscription = myLocationChangedObservable.subscribe(new Action1<MyLocationChangedEvent>() {
            @Override
            public void call(MyLocationChangedEvent myLocationChanged) {
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

    public Observable<Iterable<AutocompletePrediction>> query(final String s) {
        assert location != null;

        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "not connected");
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

        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    }
}
