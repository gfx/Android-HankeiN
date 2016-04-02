package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.DialogEditLocationMemoBinding;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.event.LocationMemoRemovedEvent;
import com.github.gfx.hankei_n.model.AddressAutocompleAdapter;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.PlaceEngine;
import com.github.gfx.hankei_n.toolbox.MarkerHueAllocator;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

@ParametersAreNonnullByDefault
public class EditLocationMemoFragment extends DialogFragment {

    static final String kLocationMemo = "location_memo";

    @Inject
    MarkerHueAllocator markerHueAllocator;

    @Inject
    PlaceEngine placeEngine;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @Inject
    BehaviorSubject<LocationMemoRemovedEvent> locationMemoRemovedSubject;

    DialogEditLocationMemoBinding binding;

    LocationMemo memo;

    AlertDialog dialog;

    AddressAutocompleAdapter adapter;

    String oldAddress;

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

        LocationMemo argMemo = (LocationMemo) getArguments().getSerializable(kLocationMemo);
        if (argMemo != null) {
            memo = argMemo.copy();
            binding.editAddress.setText(argMemo.address);
            binding.editNote.setText(argMemo.note);
            if (argMemo.radius > 0) {
                binding.checkboxCircle.setChecked(true);
                binding.editRadius.setText(String.valueOf(argMemo.radius));
                binding.editRadius.setEnabled(true);
            }
            oldAddress = memo.address;
        } else {
            memo = new LocationMemo("", "", new LatLng(0, 0), 0, markerHueAllocator.allocate());
        }

        adapter = new AddressAutocompleAdapter(getContext(), placeEngine);
        binding.editAddress.setAdapter(adapter);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.add_location_memo)
                .setView(binding.getRoot())
                .setNeutralButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        if (argMemo == null || argMemo.id == 0) {
            dialogBuilder.setPositiveButton(R.string.dialog_button_add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendLocationMemoAddedEventAndDismiss();
                }
            });
        } else {
            dialogBuilder.setPositiveButton(R.string.dialog_button_update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendLocationMemoAddedEventAndDismiss();
                }
            });
            dialogBuilder.setNegativeButton(R.string.dialog_button_remove, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    askToRemove();
                }
            });
        }

        dialog = dialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                setupEventListeners();
            }
        });
        return dialog;
    }

    void setupEventListeners() {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(binding.editAddress.length() > 0);

        RxTextView.afterTextChangeEvents(binding.editAddress).subscribe(new Action1<TextViewAfterTextChangeEvent>() {
            @Override
            public void call(TextViewAfterTextChangeEvent event) {
                Editable s = event.editable();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
                memo.address = s.toString();
            }
        });

        RxTextView.afterTextChangeEvents(binding.editNote).subscribe(new Action1<TextViewAfterTextChangeEvent>() {
            @Override
            public void call(TextViewAfterTextChangeEvent event) {
                Editable s = event.editable();
                memo.note = s.toString();
            }
        });

        RxTextView.afterTextChangeEvents(binding.editRadius).subscribe(new Action1<TextViewAfterTextChangeEvent>() {
            @Override
            public void call(TextViewAfterTextChangeEvent event) {
                Editable s = event.editable();
                memo.radius = Double.parseDouble(s.toString());
            }
        });

        binding.checkboxCircle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.editRadius.setEnabled(isChecked);
                if (isChecked) {
                    memo.radius = Double.parseDouble(binding.editRadius.getText().toString());
                } else {
                    memo.radius = 0;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidthDp = (int) (metrics.widthPixels / metrics.density);

        int widthPx;
        if (screenWidthDp < 500) {
            widthPx = (int) (metrics.widthPixels * 0.95);
        } else {
            widthPx = (int) (metrics.widthPixels * 0.75);
        }
        lp.width = widthPx;
        dialog.getWindow().setAttributes(lp);
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

    void sendLocationMemoAddedEventAndDismiss() {
        if (memo.address.equals(oldAddress)) {
            castLocationMemo();
            return;
        }

        placeEngine.getLocationFromAddress(memo.address)
                .retry(2)
                .subscribe(new Subscriber<LatLng>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Network error or something?
                        onNext(memo.getLatLng());
                    }

                    @Override
                    public void onNext(LatLng latLng) {
                        memo.latitude = latLng.latitude;
                        memo.longitude = latLng.longitude;
                        castLocationMemo();
                    }
                });
    }

    void castLocationMemo() {
        locationMemoAddedSubject.onNext(new LocationMemoAddedEvent(memo));
        dialog.dismiss();
    }

    void askToRemove() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.ask_to_remove_memo)
                .setPositiveButton(R.string.affirmative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeMemo();
                    }
                })
                .setNegativeButton(R.string.negative, null)
                .show();
    }

    void removeMemo() {
        locationMemoRemovedSubject.onNext(new LocationMemoRemovedEvent(memo));
    }

    public void initAddress(String address) {
        if (binding.editAddress.getText().length() == 0) {
            binding.editAddress.setAdapter(null);
            binding.editAddress.setText(address);
            binding.editAddress.setAdapter(adapter);
        }
    }
}
