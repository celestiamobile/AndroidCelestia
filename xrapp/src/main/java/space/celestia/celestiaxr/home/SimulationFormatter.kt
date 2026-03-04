package space.celestia.celestiaxr.home

import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.os.Build
import space.celestia.celestia.Observer
import space.celestia.celestia.Utils
import space.celestia.celestiaui.utils.CelestiaString
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

internal const val oneMiInKm = 1.60934
internal const val oneFtInKm = 0.0003048

internal fun usesMetricSystem(): Boolean {
    return if (Build.VERSION.SDK_INT >= 28) {
        LocaleData.getMeasurementSystem(ULocale.getDefault()) == LocaleData.MeasurementSystem.SI
    } else {
        Locale.getDefault().country !in setOf("US", "LR", "MM")
    }
}

internal fun formatNumber(number: Double, formatter: NumberFormat): String {
    val abs = kotlin.math.abs(number)
    val fractionDigits = when {
        abs >= 1000.0 -> 0
        abs >= 10.0 -> 1
        abs >= 1.0 -> 2
        else -> 4
    }
    formatter.minimumFractionDigits = fractionDigits
    formatter.maximumFractionDigits = fractionDigits
    return formatter.format(number)
}

internal fun formatTime(julianDate: Double, isPaused: Boolean, isLightTravelDelayEnabled: Boolean, formatter: DateFormat): String {
    val time = formatter.format(Utils.createDateFromJulianDay(julianDate))
    return when {
        isPaused && isLightTravelDelayEnabled -> CelestiaString("%s LT (Paused)", "Time display with light travel delay and simulation paused").format(time)
        isPaused -> CelestiaString("%s (Paused)", "Time display with simulation paused").format(time)
        isLightTravelDelayEnabled -> CelestiaString("%s LT", "Time display with light travel delay enabled").format(time)
        else -> time
    }
}

internal fun formatTimeScale(timeScale: Double, formatter: NumberFormat): String {
    return when {
        timeScale == 1.0 -> CelestiaString("Real Time", "")
        timeScale == -1.0 -> CelestiaString("-Real Time", "Reversed real time scale")
        kotlin.math.abs(timeScale) < 1e-15 -> CelestiaString("Time Stopped", "")
        else -> CelestiaString("%s× Real Time", "Time scale multiplier, %s is the numeric value").format(formatNumber(timeScale, formatter))
    }
}

internal fun formatLength(km: Double, isMetric: Boolean, formatter: NumberFormat): String {
    val ly = Utils.kilometersToLightYears(km)
    val mpcThreshold = Utils.parsecsToLightYears(1e6)
    val kpcThreshold = 0.5 * Utils.parsecsToLightYears(1e3)
    val number: Double
    val format: String
    if (kotlin.math.abs(ly) >= mpcThreshold) {
        format = CelestiaString("%s Mpc", "Unit megaparsec")
        number = Utils.lightYearsToParsecs(ly) / 1e6
    } else if (kotlin.math.abs(ly) >= kpcThreshold) {
        format = CelestiaString("%s kpc", "Unit kiloparsec")
        number = Utils.lightYearsToParsecs(ly) / 1e3
    } else {
        val au = Utils.kilometersToAU(km)
        if (kotlin.math.abs(au) >= 1000.0) {
            format = CelestiaString("%s ly", "Unit light year")
            number = ly
        } else if (km >= 10000000.0) {
            format = CelestiaString("%s au", "Unit astronomical unit")
            number = au
        } else if (!isMetric) {
            if (km >= oneMiInKm) {
                format = CelestiaString("%s mi", "Unit mile")
                number = km / oneMiInKm
            } else {
                format = CelestiaString("%s ft", "Unit foot")
                number = km / oneFtInKm
            }
        } else {
            if (km >= 1.0) {
                format = CelestiaString("%s km", "Unit kilometer")
                number = km
            } else {
                format = CelestiaString("%s m", "Unit meter")
                number = km * 1000.0
            }
        }
    }
    return format.format(formatNumber(number, formatter))
}

internal fun formatSpeed(speed: Double, isMetric: Boolean, formatter: NumberFormat): String {
    val au = Utils.kilometersToAU(speed)
    val number: Double
    val format: String
    if (kotlin.math.abs(au) >= 1000.0) {
        format = CelestiaString("%s ly/s", "Unit light year per second")
        number = Utils.kilometersToLightYears(speed)
    } else if (speed >= 10000000.0) {
        format = CelestiaString("%s au/s", "Unit astronomical unit per second")
        number = au
    } else if (!isMetric) {
        if (speed >= oneMiInKm) {
            format = CelestiaString("%s mi/s", "Unit mile per second")
            number = speed / oneMiInKm
        } else {
            format = CelestiaString("%s ft/s", "Unit foot per second")
            number = speed / oneFtInKm
        }
    } else {
        if (speed >= 1.0) {
            format = CelestiaString("%s km/s", "Unit kilometer per second")
            number = speed
        } else {
            format = CelestiaString("%s m/s", "Unit meter per second")
            number = speed * 1000.0
        }
    }
    return format.format(formatNumber(number, formatter))
}

internal fun formatCoordinateSystem(coordinateSystem: Int, referenceName: String, targetName: String): String {
    return when (coordinateSystem) {
        Observer.COORDINATE_SYSTEM_ECLIPTICAL -> String.format(CelestiaString("Follow %s", "Coordinate system, %s is the reference object name"), referenceName)
        Observer.COORDINATE_SYSTEM_BODY_FIXED -> String.format(CelestiaString("Sync Orbit %s", "Coordinate system, %s is the reference object name"), referenceName)
        Observer.COORDINATE_SYSTEM_PHASE_LOCK -> String.format(CelestiaString("Lock %s → %s", "Coordinate system, %s are the reference and target object names"), referenceName, targetName)
        Observer.COORDINATE_SYSTEM_CHASE -> String.format(CelestiaString("Chase %s", "Coordinate system, %s is the reference object name"), referenceName)
        Observer.COORDINATE_SYSTEM_UNIVERSAL -> CelestiaString("Free Flight", "Flight mode, coordinate system")
        else -> CelestiaString("Unknown", "")
    }
}
