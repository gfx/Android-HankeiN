package com.github.gfx.hankei_n.event;

import com.github.gfx.hankei_n.model.LocationMemo;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemoAddedEvent {

    public final LocationMemo memo;

    public LocationMemoAddedEvent(LocationMemo memo) {
        this.memo = memo;
    }
}
