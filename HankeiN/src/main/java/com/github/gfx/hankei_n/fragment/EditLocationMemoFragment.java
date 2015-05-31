package com.github.gfx.hankei_n.fragment;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.LocationChanged;
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
import timber.log.Timber;

public class EditLocationMemoFragment extends DialogFragment {

    public static EditLocationMemoFragment newInstance() {
        EditLocationMemoFragment fragment = new EditLocationMemoFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    AddressAutocompleteEngine autocompleteEngine;

    @Inject
    BehaviorSubject<LocationChanged> locationChangedSubject;

    @InjectView(R.id.edit_address)
    AutoCompleteTextView editAddress;

    @InjectView(R.id.edit_note)
    EditText editNote;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.v("onCreate");
        super.onCreate(savedInstanceState);

        HankeiNApplication.getAppComponent(getActivity()).inject(this);

        autocompleteEngine = new AddressAutocompleteEngine(getActivity(), locationChangedSubject);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.v("onCreateDialog");

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
        Timber.v("onResume");
        super.onResume();

        autocompleteEngine.start();
    }

    @Override
    public void onPause() {
        autocompleteEngine.stop();

        super.onPause();
    }
}
