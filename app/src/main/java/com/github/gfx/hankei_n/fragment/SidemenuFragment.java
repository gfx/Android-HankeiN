package com.github.gfx.hankei_n.fragment;

import com.github.gfx.hankei_n.HankeiNApplication;
import com.github.gfx.hankei_n.R;
import com.github.gfx.hankei_n.event.LocationMemoAddedEvent;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class SidemenuFragment extends Fragment {

    @Inject
    BehaviorSubject<LocationMemoAddedEvent> locationMemoAddedSubject;

    @InjectView(R.id.list_location_memos)
    ListView memosListView;

    @Inject
    LocationMemoList memos;

    final Adapter adapter = new Adapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        FragmentManager fm = getFragmentManager();

        EditLocationMemoFragment.newInstance()
                .show(fm, "edit_location_memo");
    }

    class Adapter extends BaseAdapter {

        public void addItem(LocationMemo memo) {
            memos.add(memo);
            memos.save(getActivity());
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.widget_location_memo, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.address.setText(getItem(position).address);
            viewHolder.note.setText(getItem(position).note);

            return convertView;
        }
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
}
