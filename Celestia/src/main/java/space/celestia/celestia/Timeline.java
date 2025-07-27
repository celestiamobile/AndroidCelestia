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
        return c_getPhase(index, pointer);
    }

    public static class Phase {
        private final double phaseStartTime;
        private final double phaseEndTime;

        Phase(double phaseStartTime, double phaseEndTime) {
            this.phaseStartTime = phaseStartTime;
            this.phaseEndTime = phaseEndTime;
        }

        public double getStartTime() {
            return phaseStartTime;
        }

        public double getEndTime() {
            return phaseEndTime;
        }
    }

    private static native int c_getPhaseCount(long pointer);
    private static native Phase c_getPhase(int index, long pointer);
}
