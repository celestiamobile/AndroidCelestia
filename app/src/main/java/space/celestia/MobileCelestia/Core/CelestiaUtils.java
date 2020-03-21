package space.celestia.MobileCelestia.Core;

public class CelestiaUtils {
    public native static double getJulianDay(int era, int year, int month, int day, int hour, int minute, int second, int millisecond);
    public static native int[] getJulianDayComponents(double julianDay);
}
