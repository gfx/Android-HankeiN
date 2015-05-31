package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.MyLocationChangedEvent;
import com.github.gfx.hankei_n.model.AddressAutocompleAdapter;
import com.github.gfx.hankei_n.model.AddressAutocompleteEngine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subjects.BehaviorSubject;

public class EditLocationMemoFragment extends DialogFragment {

    static final String kLocation = "location";

    public static EditLocationMemoFragment newInstance(LatLng location) {
        EditLocationMemoFragment fragment = new EditLocationMemoFragment();

        Bundle args = new Bundle();
        args.putParcelable(kLocation, location);
        fragment.setArguments(args);

        return fragment;
    }

    AddressAutocompleteEngine autocompleteEngine;

    @Inject
    BehaviorSubject<MyLocationChangedEvent> myLocationChangedSubject;

    @InjectView(R.id.edit_address)
    AutoCompleteTextView editAddress;

    @InjectView(R.id.edit_note)
    EditText editNote;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HankeiNApplication.getAppComponent(getActivity()).inject(this);

        LatLng location = getArguments().getParcelable(kLocation);
        assert location != null;
        autocompleteEngine = new AddressAutocompleteEngine(getActivity(), myLocationChangedSubject);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_edit_location_memo, null);
        ButterKnife.inject(this, view);

        editAddress.setAdapter(new AddressAutocompleAdapter(getActivity(), autocompleteEngine));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_location_memo)
                .setView(view)
                .setPositiveButton(R.string.dialog_button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();

        autocompleteEngine.start();
    }

    @Override
    public void onPause() {
        autocompleteEngine.stop();

        super.onPause();
    }
}
