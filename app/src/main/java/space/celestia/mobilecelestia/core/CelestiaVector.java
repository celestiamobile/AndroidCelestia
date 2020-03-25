package space.celestia.mobilecelestia.core;

public class CelestiaVector {
    private double[] array;

    public double getX() { return array[0]; }
    public double getY() { return array[1]; }
    public double getZ() { return array[2]; }
    public double getW() { return array[3]; }

    private CelestiaVector(double x, double y, double z, double w) {
        array = new double[] {x, y, z, w};
    }

    private CelestiaVector(double x, double y, double z) {
        array = new double[] {x, y, z};
    }
}
