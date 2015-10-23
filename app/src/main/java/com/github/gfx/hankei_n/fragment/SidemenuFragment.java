package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.FragmentSidemenuBinding;
import com.github.gfx.hankei_n.databinding.WidgetLocationMemoBinding;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoList;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

@ParametersAreNonnullByDefault
public class SidemenuFragment extends Fragment {

    static final String TAG = SidemenuFragment.class.getSimpleName();

    static final String CATEGORY_LOCATION_MEMO = "LocationMemo";

    final Adapter adapter = new Adapter();

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @Inject
    Tracker tracker;

    @Inject
    LocationMemoList memos;

    FragmentSidemenuBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(), R.layout.fragment_sidemenu, container, false);
        HankeiNApplication.getAppComponent(getActivity()).inject(this);

        binding.listLocationMemos.setAdapter(adapter);

        binding.buttonAddLocationMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditLocationMemoFragment.newInstance()
                        .show(getFragmentManager(), "edit_location_memo");
            }
        });

        locationMemoAddedSubject.subscribe(new Action1<LocationMemoAddedEvent>() {
            @Override
            public void call(LocationMemoAddedEvent locationMemoAddedEvent) {
                addMemo(locationMemoAddedEvent.memo);
            }
        });
        return binding.getRoot();
    }


    void showEditDialog(LocationMemo memo) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_LOCATION_MEMO)
                .setAction("showEditDialog")
                .build());

        EditLocationMemoFragment.newInstance(memo)
                .show(getFragmentManager(), "edit_location_memo");
    }

    void addMemo(LocationMemo memo) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_LOCATION_MEMO)
                .setAction("add")
                .build());

        adapter.addItem(memo);
    }

    void askToRemove(final LocationMemo memo) {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.ask_to_remove_memo)
                .setPositiveButton(R.string.affirmative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeMemo(memo);
                    }
                })
                .setNegativeButton(R.string.negative, null)
                .show();
    }

    void removeMemo(LocationMemo memo) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_LOCATION_MEMO)
                .setAction("remove")
                .build());

        adapter.removeItem(memo);
    }


    private class Adapter extends BaseAdapter {

        public void addItem(LocationMemo memo) {
            memos.upsert(memo);
            memos.save();
            notifyDataSetChanged();
        }

        public void removeItem(LocationMemo memo) {
            memos.removeItem(memo);
            memos.save();

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return memos.size();
        }

        @Override
        public LocationMemo getItem(int position) {
            return memos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, @Nullable View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                WidgetLocationMemoBinding binding = DataBindingUtil
                        .inflate(inflater, R.layout.widget_location_memo, parent, false);
                convertView = binding.getRoot();
            }

            final LocationMemo memo = getItem(position);

            WidgetLocationMemoBinding binding = DataBindingUtil.getBinding(convertView);
            binding.textAddress.setText(memo.address);
            binding.textNote.setText(memo.note);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDialog(memo);
                }
            });

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    askToRemove(memo);
                    return true;
                }
            });

            return convertView;
        }
    }
}
