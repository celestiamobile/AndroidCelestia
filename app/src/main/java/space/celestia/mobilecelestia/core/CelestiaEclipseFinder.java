package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CelestiaEclipseFinder {
    public static final int ECLIPSE_KIND_SOLAR = 0x01;
    public static final int ECLIPSE_KIND_LUNAR = 0x02;

    private final long pointer;

    public static class Eclipse {
        public final CelestiaBody occulter;
        public final CelestiaBody receiver;
        public final double startTimeJulian;
        public final double endTimeJulian;

        private Eclipse(long occulter, long receiver, double startTime, double endTime) {
            startTimeJulian = startTime;
            endTimeJulian = endTime;
            this.occulter = new CelestiaBody(occulter);
            this.receiver = new CelestiaBody(receiver);
        }
    }

    public CelestiaEclipseFinder(CelestiaBody body) {
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
    protected void finalize() throws Throwable {
        c_finalized(pointer);

        super.finalize();
    }

    public void abort() {
        c_abort(pointer);
    }

    private static native long c_createWithBody(long ptr);
    private static native void c_finalized(long ptr);
    private static native void c_abort(long ptr);
    private static native String c_search(long ptr, int kind, double startTimeJulian, double endTimeJulian);
}
