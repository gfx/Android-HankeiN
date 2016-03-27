package com.github.gfx.hankei_n.toolbox;

import com.github.gfx.hankei_n.model.Prefs;

public class MarkerHueAllocator {

    static final String kMarkerHue = "marker_hue";

    final Prefs prefs;

    public MarkerHueAllocator(Prefs prefs) {
        this.prefs = prefs;
    }

    public float allocate() {
        float hue = prefs.get(kMarkerHue, 0.0f);
        prefs.put(kMarkerHue, hue + 20.0f);
        return hue;
    }
}
