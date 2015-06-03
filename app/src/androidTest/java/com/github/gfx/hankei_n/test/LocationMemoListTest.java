package com.github.gfx.hankei_n.test;

import com.google.android.gms.maps.model.LatLng;

import com.github.gfx.hankei_n.model.LocationMemo;
import com.github.gfx.hankei_n.model.LocationMemoList;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LocationMemoListTest {

    Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        LocationMemoList memos = new LocationMemoList();

        memos.add(new LocationMemo("foo", "note 1", new LatLng(1.0, 2.0)));
        memos.add(new LocationMemo("bar", "note 2", new LatLng(3.0, 4.0)));

        memos.save(context);

        memos = LocationMemoList.load(context);

        assertThat(memos.size(), is(2));

        assertThat(memos.get(0).address, is("foo"));
        assertThat(memos.get(0).note, is("note 1"));
        assertThat(memos.get(0).location.latitude, is(1.0));
        assertThat(memos.get(0).location.longitude, is(2.0));

        assertThat(memos.get(1).address, is("bar"));
        assertThat(memos.get(1).note, is("note 2"));
        assertThat(memos.get(1).location.latitude, is(3.0));
        assertThat(memos.get(1).location.longitude, is(4.0));
    }
}
