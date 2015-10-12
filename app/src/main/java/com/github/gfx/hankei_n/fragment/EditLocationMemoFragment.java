package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.AddressAutocompleAdapter;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.PlaceEngine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class EditLocationMemoFragment extends DialogFragment {

    @Inject
    PlaceEngine placeEngine;

    @Inject
    BehaviorSubject<LocationChangedEvent> locationChangedSubject;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @InjectView(R.id.edit_address)
    AutoCompleteTextView editAddress;

    @InjectView(R.id.edit_note)
    EditText editNote;

    AlertDialog dialog;

    public static EditLocationMemoFragment newInstance() {
        EditLocationMemoFragment fragment = new EditLocationMemoFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.v("onCreate");
        super.onCreate(savedInstanceState);

        HankeiNApplication.getAppComponent(getActivity()).inject(this);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Timber.v("onCreateDialog");

        View view = View.inflate(getActivity(), R.layout.dialog_edit_location_memo, null);
        ButterKnife.inject(this, view);

        DialogHandler dialogHandler = new DialogHandler();

        editAddress.setAdapter(new AddressAutocompleAdapter(getActivity(), placeEngine));
        editAddress.addTextChangedListener(dialogHandler);

        dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_location_memo)
                .setView(view)
                .setPositiveButton(R.string.dialog_button_add, null /* will be overridden in dialogHandler */)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .create();

        dialog.setOnShowListener(dialogHandler);

        return dialog;
    }

    @Override
    public void onResume() {
        Timber.v("onResume");
        super.onResume();

        placeEngine.start();
    }

    @Override
    public void onPause() {
        placeEngine.stop();

        super.onPause();
    }

    void sendLocationMemoAddedEventAndDismiss() {
        final String address = editAddress.getText().toString();
        final String note = editNote.getText().toString();

        placeEngine.getLocationFromAddress(address)
                .subscribe(new Subscriber<LatLng>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        LocationMemo memo = new LocationMemo(
                                address,
                                note,
                                new LatLng(0, 0));
                        locationMemoAddedSubject.onNext(new LocationMemoAddedEvent(memo));
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(LatLng latLng) {
                        LocationMemo memo = new LocationMemo(
                                address,
                                note,
                                latLng);
                        locationMemoAddedSubject.onNext(new LocationMemoAddedEvent(memo));
                        dialog.dismiss();
                    }
                });
    }

    class DialogHandler implements TextWatcher, AlertDialog.OnShowListener {

        @Override
        public void onShow(DialogInterface dialogInterface) {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendLocationMemoAddedEventAndDismiss();
                }
            });
        }

        @Override
        public void afterTextChanged(Editable s) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }
}
