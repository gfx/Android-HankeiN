package com.github.gfx.hankei_n.model;

import android.support.annotation.NonNull;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class ReusableCompositeSubscription {

    CompositeSubscription compositeSubscription;

    public void add(@NonNull Subscription subscription) {
        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
        }
        compositeSubscription.add(subscription);
    }

    public void unsubscribe() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }
}
