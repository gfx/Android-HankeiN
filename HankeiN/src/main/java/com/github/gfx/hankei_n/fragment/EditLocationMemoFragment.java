package com.github.gfx.hankei_n.fragment;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;

import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Geocoder;
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

public class EditLocationMemoFragment extends DialogFragment {

    public static EditLocationMemoFragment newInstance() {
        return new EditLocationMemoFragment();
    }

    @Inject
    Geocoder geocoder;

    @InjectView(R.id.edit_address)
    AutoCompleteTextView editAddress;

    @InjectView(R.id.edit_note)
    EditText editNote;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HankeiNApplication.getAppComponent(getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_edit_location_memo, null);

        ButterKnife.inject(this, view);

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
}
