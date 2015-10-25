package com.github.gfx.hankei_n.test;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.BuildConfig;
import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class LocationMemoListTest {

    Context context;

    LocationMemoList memos;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        reset();
        memos.clear();
    }

    void reset() {
        memos = new LocationMemoList(context, "test.db");
    }

    @Test
    public void testAdd() throws Exception {
        memos.upsert(new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0)));
        memos.upsert(new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0)));

        List<LocationMemo> list = memos.all();

        assertThat(list.size(), is(2));

        assertThat(list.get(0).address, is("foo"));
        assertThat(list.get(1).address, is("bar"));
    }

    @Test
    public void testRemove() throws Exception {
        LocationMemo a = new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0));
        LocationMemo b = new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0));


        memos.upsert(a);
        memos.upsert(b);

        memos.remove(a);

        List<LocationMemo> list = memos.all();

        assertThat(list.size(), is(1));

        assertThat(list.get(0), is(b));
    }


    @Test
    public void testSaveAndLoad() throws Exception {

        memos.upsert(new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0)));
        memos.upsert(new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0)));

        reset();

        List<LocationMemo> list = memos.all();
        assertThat(list.size(), is(2));

        assertThat(list.get(0).address, is("foo"));
        assertThat(list.get(0).note, is("note 1"));
        assertThat(list.get(0).buildLocation(), is(new LatLng(1.0, 2.0)));

        assertThat(list.get(1).address, is("bar"));
        assertThat(list.get(1).note, is("note 2"));
        assertThat(list.get(1).buildLocation(), is(new LatLng(3.0, 4.0)));
    }
}
