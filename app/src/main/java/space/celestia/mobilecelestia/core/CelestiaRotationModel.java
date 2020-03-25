package space.celestia.mobilecelestia.core;

public class CelestiaRotationModel {
    protected long pointer;

    CelestiaRotationModel(long ptr) { pointer = ptr; }

    public Boolean isPeriodic() { return c_isPeriodic(); }
    public double getPeriod() { return c_getPeriod(); }
    public double getValidBeginTime() { return c_getValidBeginTime(); }
    public double getValidEndTime() { return c_getValidEndTime(); }

    public CelestiaVector getAngularVelocityAtTime(double julianDay) { return c_getAngularVelocityAtTime(julianDay); }
    public CelestiaVector getEquatorOrientationAtTime(double julianDay) { return c_getEquatorOrientationAtTime(julianDay); }
    public CelestiaVector getSpinAtTime(double julianDay) { return c_getSpinAtTime(julianDay); }

    private native boolean c_isPeriodic();
    private native double c_getPeriod();
    private native double c_getValidBeginTime();
    private native double c_getValidEndTime();

    private native CelestiaVector c_getAngularVelocityAtTime(double julianDay);
    private native CelestiaVector c_getEquatorOrientationAtTime(double julianDay);
    private native CelestiaVector c_getSpinAtTime(double julianDay);

}
