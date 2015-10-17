package com.github.gfx.hankei_n.fragment;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoList;

import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

@ParametersAreNonnullByDefault
public class SidemenuFragment extends Fragment {

    final Adapter adapter = new Adapter();

    @Inject
    Application context;

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @InjectView(R.id.list_location_memos)
    ListView memosListView;

    @Inject
    LocationMemoList memos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sidemenu, container, false);
        ButterKnife.inject(this, view);
        HankeiNApplication.getAppComponent(getActivity()).inject(this);

        memosListView.setAdapter(adapter);

        locationMemoAddedSubject.subscribe(new Action1<LocationMemoAddedEvent>() {
            @Override
            public void call(LocationMemoAddedEvent locationMemoAddedEvent) {
                adapter.addItem(locationMemoAddedEvent.memo);
            }
        });
        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.button_add_location_memo)
    void onAddLocationMemoButton() {
        EditLocationMemoFragment.newInstance()
                .show(getFragmentManager(), "edit_location_memo");
    }

    static class ViewHolder {

        @InjectView(R.id.text_address)
        TextView address;

        @InjectView(R.id.text_note)
        TextView note;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

    }

    private class Adapter extends BaseAdapter {

        public void addItem(LocationMemo memo) {
            memos.upsert(memo);
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
                convertView = inflater.inflate(R.layout.widget_location_memo, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            final LocationMemo memo = getItem(position);

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.address.setText(memo.address);
            viewHolder.note.setText(memo.note);

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

        void showEditDialog(LocationMemo memo) {
            EditLocationMemoFragment.newInstance(memo)
                    .show(getFragmentManager(), "edit_location_memo");
        }

        void askToRemove(final LocationMemo memo) {
            new AlertDialog.Builder(getContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("メモを削除しますか？")
                    .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            memos.removeItem(memo);
                            memos.save();

                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("いいえ", null)
                    .show();
        }
    }
}
