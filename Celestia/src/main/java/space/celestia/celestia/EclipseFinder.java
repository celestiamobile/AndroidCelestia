// EclipseFinder.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EclipseFinder implements AutoCloseable {
    public static final int ECLIPSE_KIND_SOLAR = 0x01;
    public static final int ECLIPSE_KIND_LUNAR = 0x02;

    private final long pointer;
    private boolean closed = false;

    public static class Eclipse {
        public final Body occulter;
        public final Body receiver;
        public final double startTimeJulian;
        public final double endTimeJulian;

        private Eclipse(long occulter, long receiver, double startTime, double endTime) {
            startTimeJulian = startTime;
            endTimeJulian = endTime;
            this.occulter = new Body(occulter);
            this.receiver = new Body(receiver);
        }
    }

    public EclipseFinder(Body body) {
        pointer = c_createWithBody(body.pointer);
    }

    public @NonNull List<Eclipse> search(double startTime, double endTime, int kind) {
        String json = c_search(pointer, kind, startTime, endTime);
        ArrayList<Eclipse> eclipses = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject j = array.getJSONObject(i);
                Eclipse eclipse = new Eclipse(j.getLong("occulter"), j.getLong("receiver"), j.getDouble("startTime"), j.getDouble("endTime"));
                eclipses.add(eclipse);
            }
        } catch (Exception ignored) {}
        return eclipses;
    }

    @Override
    public void close() throws Exception {
        if (!closed) {
            c_destroy(pointer);
            closed = true;
        }
    }

    public void abort() {
        c_abort(pointer);
    }

    private static native long c_createWithBody(long ptr);
    private static native void c_destroy(long ptr);
    private static native void c_abort(long ptr);
    private static native String c_search(long ptr, int kind, double startTimeJulian, double endTimeJulian);
}
