package com.github.gfx.hankei_n.model.gson;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class LatLngTypeAdapter extends TypeAdapter<LatLng> {

    @Override
    public void write(JsonWriter out, LatLng value) throws IOException {
        out.beginObject();
        out.name("latitude").value(value.latitude);
        out.name("longitude").value(value.longitude);
        out.endObject();
    }

    @Override
    public LatLng read(JsonReader in) throws IOException {
        double latitude = 0;
        double longitude = 0;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "latitude":
                    latitude = in.nextDouble();
                    break;
                case "longitude":
                    longitude = in.nextDouble();
            }
        }
        in.endObject();
        return new LatLng(latitude, longitude);
    }
}
