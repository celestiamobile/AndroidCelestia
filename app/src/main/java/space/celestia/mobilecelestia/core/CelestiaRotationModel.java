/*
 * CelestiaRotationModel.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

public class CelestiaRotationModel {
    protected long pointer;

    CelestiaRotationModel(long ptr) { pointer = ptr; }

    public Boolean isPeriodic() { return c_isPeriodic(pointer); }
    public double getPeriod() { return c_getPeriod(pointer); }
    public double getValidBeginTime() { return c_getValidBeginTime(pointer); }
    public double getValidEndTime() { return c_getValidEndTime(pointer); }

    public CelestiaVector getAngularVelocityAtTime(double julianDay) { return c_getAngularVelocityAtTime(pointer, julianDay); }
    public CelestiaVector getEquatorOrientationAtTime(double julianDay) { return c_getEquatorOrientationAtTime(pointer, julianDay); }
    public CelestiaVector getSpinAtTime(double julianDay) { return c_getSpinAtTime(pointer, julianDay); }

    private static native boolean c_isPeriodic(long pointer);
    private static native double c_getPeriod(long pointer);
    private static native double c_getValidBeginTime(long pointer);
    private static native double c_getValidEndTime(long pointer);

    private static native CelestiaVector c_getAngularVelocityAtTime(long pointer, double julianDay);
    private static native CelestiaVector c_getEquatorOrientationAtTime(long pointer, double julianDay);
    private static native CelestiaVector c_getSpinAtTime(long pointer, double julianDay);

}
