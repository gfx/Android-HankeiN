package com.github.gfx.hankei_n.toolbox;

import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.model.LocationMemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;

public class Intents {

    private static String createGoogleMapUri(double latitude, double longitude) {
        return String.format(Locale.getDefault(), "https://www.google.co.jp/maps/?q=%g,%g", latitude, longitude);
    }

    public static Intent createStreetViewIntent(double latitude, double longitude) {
        // https://developers.google.com/maps/documentation/android-api/intents
        Uri uri = Uri.parse(String.format(Locale.getDefault(), "google.streetview:cbll=%g,%g",latitude, longitude));
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public static Intent createShareTextIntent(CharSequence title, CharSequence text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return Intent.createChooser(intent, title);
    }

    public static Intent createShareLocationMemoIntent(Context context, LocationMemo memo) {
        StringBuilder textToShare = new StringBuilder();
        textToShare.append(memo.address);
        textToShare.append('\n');
        if (memo.note.length() > 0) {
            textToShare.append(memo.note);
            textToShare.append('\n');
        }

        textToShare.append(createGoogleMapUri(memo.latitude, memo.longitude));
        return createShareTextIntent(context.getString(R.string.share_location_memo_title, memo.address), textToShare);
    }

    public static Intent createOpenWithMapIntent(LocationMemo memo) {
        Uri gmmIntentUri = Uri.parse(String.format(Locale.getDefault(),
                "geo:%g,%g?q=%s",memo.latitude, memo.longitude, Uri.encode(memo.address)));
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        //intent.setPackage("com.google.android.apps.maps");
        return intent;
    }
}
