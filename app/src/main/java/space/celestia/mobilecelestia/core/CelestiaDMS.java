package space.celestia.mobilecelestia.core;

public class CelestiaDMS {
    private int degrees;
    private int minutes;
    private double seconds;

    public CelestiaDMS(int degrees, int minutes, double seconds) {
        this.degrees = degrees;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public CelestiaDMS(double decimal) {
        this.degrees = c_getDegrees(decimal);
        this.minutes = c_getMinutes(decimal);
        this.seconds = c_getSeconds(decimal);
    }

    public int getDegrees() {
        return degrees;
    }

    public int getHours() {
        return getDegrees();
    }

    public int getMinutes() {
        return minutes;
    }

    public double getSeconds() {
        return seconds;
    }

    public double getDecimal() {
        return c_getDecimal(degrees, minutes, seconds);
    }

    private native int c_getDegrees(double decimal);
    private native int c_getMinutes(double decimal);
    private native double c_getSeconds(double decimal);
    private native double c_getDecimal(int degrees, int minutes, double seconds);
}
