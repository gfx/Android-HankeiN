package com.github.gfx.hankei_n.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.github.gfx.hankei_n.activity.MainActivity;
import com.github.gfx.hankei_n.databinding.CardLocationMemoBinding;
import com.github.gfx.hankei_n.databinding.FragmentSidemenuBinding;
import com.github.gfx.hankei_n.dependency.DependencyContainer;
import com.github.gfx.hankei_n.event.LocationMemoChangedEvent;
import com.github.gfx.hankei_n.event.LocationMemoFocusedEvent;
import com.github.gfx.hankei_n.event.LocationMemoRemovedEvent;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.toolbox.Assets;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import rx.functions.Action1;
import rx.subjects.PublishSubject;
import timber.log.Timber;

@ParametersAreNonnullByDefault
public class SidemenuFragment extends Fragment {

    static final String TAG = SidemenuFragment.class.getSimpleName();

    Adapter adapter;

    @Inject
    PublishSubject<LocationMemoChangedEvent> locationMemoChangedSubject;

    @Inject
    PublishSubject<LocationMemoRemovedEvent> locationMemoRemovedSubject;

    @Inject
    PublishSubject<LocationMemoFocusedEvent> locationMemoFocusedSubject;

    @Inject
    Tracker tracker;

    @Inject
    Vibrator vibrator;

    @Inject
    Assets assets;

    @Inject
    LocationMemoManager memos;

    FragmentSidemenuBinding binding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DependencyContainer.getComponent(this).inject(this);
        Timber.d("onAttach");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSidemenuBinding.inflate(inflater, container, false);

        adapter = new Adapter(inflater); // it depends on memos

        binding.listLocationMemos.setAdapter(adapter);

        locationMemoChangedSubject.subscribe(new Action1<LocationMemoChangedEvent>() {
            @Override
            public void call(LocationMemoChangedEvent changedEvent) {
                adapter.notifyDataSetChanged();
            }
        });
        return binding.getRoot();
    }

    private void showEditDialog(LocationMemo memo) {
        vibrator.vibrate(100);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(MainActivity.CATEGORY_LOCATION_MEMO)
                .setAction("showEditDialog")
                .setLabel(TAG)
                .build());

        EditLocationMemoFragment.newInstance(memo)
                .show(getFragmentManager(), "edit_location_memo");
    }

    private void focusOnMemo(LocationMemo memo) {
        locationMemoFocusedSubject.onNext(new LocationMemoFocusedEvent(memo));
    }

    private static class VH extends RecyclerView.ViewHolder {

        final CardLocationMemoBinding binding;

        public VH(CardLocationMemoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private class Adapter extends RecyclerView.Adapter<VH> {

        final LayoutInflater inflater;

        public Adapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        public LocationMemo getItem(int position) {
            return memos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return memos.count();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            CardLocationMemoBinding binding = CardLocationMemoBinding.inflate(inflater, parent, false);
            return new VH(binding);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            final LocationMemo memo = getItem(position);

            CardLocationMemoBinding binding = holder.binding;
            binding.circle.setImageDrawable(assets.createMarkerDrawable(memo.markerHue));
            binding.textAddress.setText(memo.address);
            binding.textNote.setText(memo.note);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    focusOnMemo(memo);
                }
            });
            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showEditDialog(memo);
                    return true;
                }
            });
        }
    }
}
