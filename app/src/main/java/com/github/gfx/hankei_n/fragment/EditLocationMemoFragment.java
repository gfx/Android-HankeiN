package com.github.gfx.hankei_n.fragment;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.LocationChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.AddressAutocompleAdapter;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.PlacesEngine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class EditLocationMemoFragment extends DialogFragment {

    public static EditLocationMemoFragment newInstance() {
        EditLocationMemoFragment fragment = new EditLocationMemoFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Inject
    PlacesEngine autocompleteEngine;

    @Inject
    BehaviorSubject<LocationChangedEvent> locationChangedSubject;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @InjectView(R.id.edit_address)
    AutoCompleteTextView editAddress;

    @InjectView(R.id.edit_note)
    EditText editNote;

    AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.v("onCreate");
        super.onCreate(savedInstanceState);

        HankeiNApplication.getAppComponent(getActivity()).inject(this);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.v("onCreateDialog");

        View view = View.inflate(getActivity(), R.layout.dialog_edit_location_memo, null);
        ButterKnife.inject(this, view);

        editAddress.setAdapter(new AddressAutocompleAdapter(getActivity(), autocompleteEngine));
        editAddress.addTextChangedListener(new TextWatcher() {
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
                .setView(view)
                .setPositiveButton(R.string.dialog_button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationMemoAddedSubject.onNext(createLocationMemoAddedEvent());
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return dialog;
    }

    @Override
    public void onResume() {
        Timber.v("onResume");
        super.onResume();

        autocompleteEngine.start();
    }

    @Override
    public void onPause() {
        autocompleteEngine.stop();

        super.onPause();
    }

    LocationMemoAddedEvent createLocationMemoAddedEvent() {
        LocationMemo memo = new LocationMemo(
                editAddress.getText().toString(),
                editNote.getText().toString(),
                null);
        return new LocationMemoAddedEvent(memo);
    }
}
