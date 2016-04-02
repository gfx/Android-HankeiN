package com.github.gfx.hankei_n.model;

import com.google.android.gms.location.places.AutocompletePrediction;

import com.github.gfx.hankei_n.R;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import rx.Subscriber;
import timber.log.Timber;

// FIXME: Use SupportPlaceAutocompleteFragment https://developers.google.com/places/android-api/autocomplete
@ParametersAreNonnullByDefault
public class AddressAutocompleAdapter extends ArrayAdapter<CharSequence> {

    final PlaceEngine placeEngine;

    final StyleSpan bold = new StyleSpan(Typeface.BOLD);

    public AddressAutocompleAdapter(Context context, PlaceEngine placeEngine) {
        super(context, R.layout.widget_autocomplete, android.R.id.text1);
        this.placeEngine = placeEngine;
    }

    List<CharSequence> convertToSpannedList(Iterable<AutocompletePrediction> predictions) {
        List<CharSequence> list = new ArrayList<>();

        for (AutocompletePrediction prediction : predictions) {
            list.add(prediction.getFullText(bold));
        }
        return list;
    }

    boolean isCompleteInput(@Nullable CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }
        for (int i = 0, length = s.length(); i < length; i++) {
            char c = s.charAt(i);
            if (c >= 'ａ' && c <= 'ｚ') { // zenkaku alphabet
                return false;
            }
        }
        return true;
    }

    void performQuery(final String query) {
        Timber.d("performQuery: %s", query);

        placeEngine.queryAutocompletion(query)
                .subscribe(new Subscriber<Iterable<AutocompletePrediction>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "failed to queryAutocompletion queryAutocompletion for: %s", query);
                    }

                    @Override
                    public void onNext(Iterable<AutocompletePrediction> predictions) {
                        clear();
                        addAll(convertToSpannedList(predictions));
                    }
                });

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(@Nullable CharSequence constraint) {
                if (isCompleteInput(constraint)) {
                    assert constraint != null;
                    performQuery(constraint.toString());
                }
                return new FilterResults();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }
}
