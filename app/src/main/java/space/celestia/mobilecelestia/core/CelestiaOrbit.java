/*
 * CelestiaOrbit.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

public class CelestiaOrbit {
    protected long pointer;

    CelestiaOrbit(long ptr) {
        pointer = ptr;
    }

    public Boolean isPeriodic() { return c_isPeriodic(); }
    public double getPeriod() { return c_getPeriod(); }
    public double getBoundingRadius() { return c_getBoundingRadius(); }
    public double getValidBeginTime() { return c_getValidBeginTime(); }
    public double getValidEndTime() { return c_getValidEndTime(); }

    public CelestiaVector getVelocityAtTime(double julianDay) { return c_getVelocityAtTime(julianDay); }
    public CelestiaVector getPositionAtTime(double julianDay) { return c_getPositionAtTime(julianDay); }

    private native boolean c_isPeriodic();
    private native double c_getPeriod();
    private native double c_getBoundingRadius();
    private native double c_getValidBeginTime();
    private native double c_getValidEndTime();

    private native CelestiaVector c_getVelocityAtTime(double julianDay);
    private native CelestiaVector c_getPositionAtTime(double julianDay);
}
