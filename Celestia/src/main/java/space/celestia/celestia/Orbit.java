// Orbit.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

public class Orbit {
    protected long pointer;

    Orbit(long ptr) {
        pointer = ptr;
    }

    public Boolean isPeriodic() { return c_isPeriodic(pointer); }
    public double getPeriod() { return c_getPeriod(pointer); }
    public double getBoundingRadius() { return c_getBoundingRadius(pointer); }
    public double getValidBeginTime() { return c_getValidBeginTime(pointer); }
    public double getValidEndTime() { return c_getValidEndTime(pointer); }

    public Vector getVelocityAtTime(double julianDay) { return c_getVelocityAtTime(pointer, julianDay); }
    public Vector getPositionAtTime(double julianDay) { return c_getPositionAtTime(pointer, julianDay); }

    private static native boolean c_isPeriodic(long pointer);
    private static native double c_getPeriod(long pointer);
    private static native double c_getBoundingRadius(long pointer);
    private static native double c_getValidBeginTime(long pointer);
    private static native double c_getValidEndTime(long pointer);

    private static native Vector c_getVelocityAtTime(long pointer, double julianDay);
    private static native Vector c_getPositionAtTime(long pointer, double julianDay);
}
