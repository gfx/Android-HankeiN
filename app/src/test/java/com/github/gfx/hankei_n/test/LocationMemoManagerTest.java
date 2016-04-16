package com.github.gfx.hankei_n.test;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoManager;
import com.github.gfx.hankei_n.model.MarkerManager;
import com.github.gfx.hankei_n.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class LocationMemoManagerTest {

    Context context;

    LocationMemoManager memos;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        reset();
        memos.clear();
    }

    void reset() {
        memos = new LocationMemoManager(
                OrmaDatabase.builder(context).name("test.db").build(), new MarkerManager());
    }

    @Test
    public void testAdd() throws Exception {
        memos.upsert(new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0), 1.5, 0));
        memos.upsert(new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0), 1.5, 0));

        assertThat(memos.count(), is(2));

        assertThat(memos.get(0).address, is("foo"));
        assertThat(memos.get(1).address, is("bar"));
    }

    @Test
    public void testUpsert() throws Exception {
        LocationMemo a = new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0), 1.5, 0);
        memos.upsert(a);
        memos.upsert(a);

        assertThat(memos.count(), is(1));

        assertThat(memos.get(0).address, is("foo"));
        assertThat(memos.get(0).id, is(greaterThan(0L)));
    }

    @Test
    public void testRemove() throws Exception {
        LocationMemo a = new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0), 1.5, 0);
        LocationMemo b = new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0), 1.5, 0);

        memos.upsert(a);
        memos.upsert(b);

        memos.remove(a);

        assertThat(memos.count(), is(1));
        assertThat(memos.get(0), is(b));
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        memos.upsert(new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0), 1.5, 0));
        memos.upsert(new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0), 1.5, 0));

        reset();

        assertThat(memos.count(), is(2));

        assertThat(memos.get(0).address, is("foo"));
        assertThat(memos.get(0).note, is("note 1"));
        assertThat(memos.get(0).getLatLng(), is(new LatLng(1.0, 2.0)));

        assertThat(memos.get(1).address, is("bar"));
        assertThat(memos.get(1).note, is("note 2"));
        assertThat(memos.get(1).getLatLng(), is(new LatLng(3.0, 4.0)));
    }
}
