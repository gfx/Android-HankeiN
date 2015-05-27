package com.github.gfx.hankei_n.model;

import java.util.ArrayList;
import java.util.Iterator;

public class LocationMemoList implements Iterable<LocationMemo> {

    final ArrayList<LocationMemo> memos = new ArrayList<>();

    public void add(LocationMemo memo) {
        memos.add(memo);
    }

    @Override
    public Iterator<LocationMemo> iterator() {
        return memos.iterator();
    }
}
