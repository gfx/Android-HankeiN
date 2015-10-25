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
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class EditLocationMemoFragment extends DialogFragment {

    static final String kLocationMemo = "location_memo";

    @Inject
    PlaceEngine placeEngine;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    DialogEditLocationMemoBinding binding;

    long argMemoId = 0;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.v("onCreate");
        super.onCreate(savedInstanceState);

        HankeiNApplication.getAppComponent(getActivity()).inject(this);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Timber.v("onCreateDialog");

        binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(), R.layout.dialog_edit_location_memo, null, false);

        LocationMemo memo = (LocationMemo) getArguments().getSerializable(kLocationMemo);
        if (memo != null) {
            argMemoId = memo.id;
            binding.editAddress.setText(memo.address);
            binding.editNote.setText(memo.note);
        }

        binding.editAddress.setAdapter(new AddressAutocompleAdapter(getActivity(), placeEngine));
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

        dialog = new AlertDialog.Builder(getActivity())
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
        final String address = binding.editAddress.getText().toString();
        final String note = binding.editNote.getText().toString();

        placeEngine.getLocationFromAddress(address)
                .subscribe(new Subscriber<LatLng>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onNext(new LatLng(0, 0));
                    }

                    @Override
                    public void onNext(LatLng latLng) {
                        LocationMemo memo = new LocationMemo(
                                argMemoId,
                                address,
                                note,
                                latLng);
                        locationMemoAddedSubject.onNext(new LocationMemoAddedEvent(memo));
                        dialog.dismiss();
                    }
                });
    }

}
