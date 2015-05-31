package com.github.gfx.hankei_n.fragment;

import com.github.gfx.hankei_n.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SidemenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sidemenu, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.add_location_memo)
    void onAddLocationMemo() {
        FragmentManager fm = getFragmentManager();

        EditLocationMemoFragment.newInstance()
                .show(fm, "edit_location_memo");
    }

}
