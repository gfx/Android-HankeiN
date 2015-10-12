package com.github.gfx.hankei_n.model;

import com.google.android.gms.location.places.AutocompletePrediction;

import com.github.gfx.hankei_n.R;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import rx.Subscriber;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class AddressAutocompleAdapter extends ArrayAdapter<Spanned> {

    final PlaceEngine placeEngine;

    public AddressAutocompleAdapter(Context context, PlaceEngine placeEngine) {
        super(context, R.layout.support_simple_spinner_dropdown_item);
        this.placeEngine = placeEngine;
    }

    List<Spanned> convertToSpannedList(Iterable<AutocompletePrediction> predictions) {
        List<Spanned> list = new ArrayList<>();

        for (AutocompletePrediction prediction : predictions) {
            list.add(convertToSpanned(prediction));
        }
        return list;
    }

    Spanned convertToSpanned(AutocompletePrediction prediction) {
        SpannableString ss = new SpannableString(prediction.getDescription());

        StyleSpan bold = new StyleSpan(Typeface.BOLD);

        for (AutocompletePrediction.Substring substring : prediction.getMatchedSubstrings()) {
            ss.setSpan(bold, substring.getOffset(),
                    substring.getOffset() + substring.getLength(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
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
