package com.github.gfx.hankei_n.event;

import com.github.gfx.hankei_n.model.LocationMemo;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationMemoRemovedEvent {

    public final LocationMemo memo;

    public LocationMemoRemovedEvent(LocationMemo memo) {
        this.memo = memo;
    }

}
