package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.DialogEditLocationMemoBinding;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.AddressAutocompleAdapter;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.PlaceEngine;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

@ParametersAreNonnullByDefault
public class EditLocationMemoFragment extends DialogFragment {

    static final String kLocationMemo = "location_memo";

    @Inject
    PlaceEngine placeEngine;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    DialogEditLocationMemoBinding binding;

    LocationMemo argMemo;

    AlertDialog dialog;

    public static EditLocationMemoFragment newInstance() {
        EditLocationMemoFragment fragment = new EditLocationMemoFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public static EditLocationMemoFragment newInstance(LocationMemo memo) {
        EditLocationMemoFragment fragment = new EditLocationMemoFragment();

        Bundle args = new Bundle();
        args.putSerializable(kLocationMemo, memo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HankeiNApplication.getAppComponent(context).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        binding = DialogEditLocationMemoBinding.inflate(inflater, null, false);

        LocationMemo memo = (LocationMemo) getArguments().getSerializable(kLocationMemo);
        if (memo != null) {
            argMemo = memo;
            binding.editAddress.setText(memo.address);
            binding.editNote.setText(memo.note);
        }

        binding.editAddress.setAdapter(new AddressAutocompleAdapter(getContext(), placeEngine));
        binding.editAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
            }
        });

        binding.checkboxCircle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.editRadius.setEnabled(isChecked);
            }
        });

        dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.add_location_memo)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.dialog_button_add, null /* will be overridden in dialogHandler */)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(binding.editAddress.length() > 0);

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendLocationMemoAddedEventAndDismiss();
                    }
                });

            }
        });

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        placeEngine.start();
    }

    @Override
    public void onPause() {
        placeEngine.stop();

        super.onPause();
    }

    String getAddress() {
        return binding.editAddress.getText().toString();
    }

    String getNote() {
        return binding.editNote.getText().toString();
    }

    double getRadius() {
        if (binding.checkboxCircle.isChecked()) {
            return Double.parseDouble(binding.editRadius.getText().toString());
        } else {
            return 0;
        }
    }

    void sendLocationMemoAddedEventAndDismiss() {
        final String address = getAddress();

        if (argMemo != null && address.equals(argMemo.address)) {
            castLocationMemo(new LocationMemo(
                    argMemo.id,
                    address,
                    getNote(),
                    argMemo.buildLocation(),
                    getRadius()));

        } else {
            placeEngine.getLocationFromAddress(address)
                    .onErrorResumeNext(new Func1<Throwable, Observable<? extends LatLng>>() {
                        @Override
                        public Observable<? extends LatLng> call(Throwable throwable) {
                            return placeEngine.getLocationFromAddress(address); // retry once

                        }
                    })
                    .subscribe(new Subscriber<LatLng>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            // FIXME: any idea?
                            onNext(new LatLng(0, 0));
                        }

                        @Override
                        public void onNext(LatLng latLng) {
                            castLocationMemo(new LocationMemo(
                                    argMemo != null ? argMemo.id : 0,
                                    address,
                                    getNote(),
                                    latLng,
                                    getRadius()
                            ));
                        }
                    });
        }
    }

    void castLocationMemo(LocationMemo memo) {
        locationMemoAddedSubject.onNext(new LocationMemoAddedEvent(memo));
        dialog.dismiss();
    }
}
