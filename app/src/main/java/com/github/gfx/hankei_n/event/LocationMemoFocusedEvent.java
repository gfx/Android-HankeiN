package com.github.gfx.hankei_n.event;

import com.github.gfx.hankei_n.model.LocationMemo;

public class LocationMemoFocusedEvent {

    public final LocationMemo memo;

    public LocationMemoFocusedEvent(LocationMemo memo) {
        this.memo = memo;
    }
}
