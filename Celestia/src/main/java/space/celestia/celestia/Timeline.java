package space.celestia.celestia;

import androidx.annotation.NonNull;

public class Timeline {
    private final long pointer;
    Timeline(long pointer) {
        this.pointer = pointer;
    }

    public int getPhaseCount() {
        return c_getPhaseCount(pointer);
    }

    public @NonNull
    Phase getPhase(int index) {
        return new Phase(c_getPhase(index, pointer));
    }

    public static class Phase {
        private final long pointer;

        Phase(long pointer) {
            this.pointer = pointer;
        }

        public double getStartTime() {
            return c_getStartTime(pointer);
        }

        public double getEndTime() {
            return c_getEndTime(pointer);
        }

        private static native double c_getStartTime(long pointer);
        private static native double c_getEndTime(long pointer);
    }

    private static native int c_getPhaseCount(long pointer);
    private static native long c_getPhase(int index, long pointer);
}
