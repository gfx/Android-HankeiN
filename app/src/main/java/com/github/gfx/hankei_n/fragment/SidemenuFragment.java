package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.databinding.CardLocationMemoBinding;
import com.github.gfx.hankei_n.databinding.FragmentSidemenuBinding;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoManager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class SidemenuFragment extends Fragment {

    static final String CATEGORY_LOCATION_MEMO = "LocationMemo";

    Adapter adapter;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @Inject
    Tracker tracker;

    @Inject
    LocationMemoManager memos;

    FragmentSidemenuBinding binding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        HankeiNApplication.getAppComponent(context).inject(this);

        Timber.d("onAttach");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSidemenuBinding.inflate(inflater, container, false);

        adapter = new Adapter(); // it depends on memos
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


    private static class VH extends RecyclerView.ViewHolder {

        final CardLocationMemoBinding binding;

        public VH(CardLocationMemoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private class Adapter extends RecyclerView.Adapter<VH> {

        List<LocationMemo> list = memos.all();

        public void addItem(LocationMemo memo) {
            memos.upsert(memo);
            list = memos.all();
            notifyDataSetChanged();
        }

        public void removeItem(LocationMemo memo) {
            memos.remove(memo);
            list = memos.all();

            notifyDataSetChanged();
        }


        public LocationMemo getItem(int position) {
            return list.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            CardLocationMemoBinding binding = CardLocationMemoBinding.inflate(inflater, parent, false);
            return new VH(binding);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            final LocationMemo memo = getItem(position);

            CardLocationMemoBinding binding = holder.binding;
            binding.textAddress.setText(memo.address);
            binding.textNote.setText(memo.note);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDialog(memo);
                }
            });

            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    askToRemove(memo);
                    return true;
                }
            });
        }
    }
}
