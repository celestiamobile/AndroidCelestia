package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

public class CelestiaUtils {
    public static native double getJulianDay(int era, int year, int month, int day, int hour, int minute, int second, int millisecond);
    public static native int[] getJulianDayComponents(double julianDay);
}
