package com.github.gfx.hankei_n.event;

import com.github.gfx.hankei_n.model.LocationMemo;

public class LocationMemoAddedEvent {

    final LocationMemo memo;

    public LocationMemoAddedEvent(LocationMemo memo) {
        this.memo = memo;
    }
}
