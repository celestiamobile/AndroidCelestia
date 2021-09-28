/*
 * Celestia.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import space.celestia.mobilecelestia.core.*
import space.celestia.mobilecelestia.favorite.BookmarkNode

val CelestiaAppCore.currentBookmark: BookmarkNode?
    get() {
        val sel = simulation.selection
        if (sel.isEmpty) return null
        val name = simulation.universe.getNameForSelection(sel)
        return BookmarkNode(name, currentURL, null)
    }

fun CelestiaAppCore.getOverviewForSelection(selection: CelestiaSelection): String {
    return when (val obj = selection.`object`) {
        is CelestiaBody -> {
            getOverviewForBody(obj)
        }
        is CelestiaStar -> {
            getOverviewForStar(obj)
        }
        is CelestiaDSO -> {
            getOverviewForDSO(obj)
        }
        else -> {
            CelestiaString("No overview available.", "")
        }
    }
}

private fun CelestiaAppCore.getOverviewForBody(body: CelestiaBody): String {
    var str = ""

    val radius = body.radius
    val radiusString: String
    val oneMiInKm = 1.609344f
    val oneFtInKm = 0.0003048f
    if (measurementSystem == CelestiaAppCore.MEASUREMENT_SYSTEM_IMPERIAL) {
        if (radius >= oneMiInKm) {
           radiusString = CelestiaString("%d mi", "").format((radius / oneMiInKm).toInt())
        } else {
            radiusString = CelestiaString("%d ft", "").format((radius / oneFtInKm).toInt())
        }
    } else {
        if (radius >= 1) {
            radiusString = CelestiaString("%d km", "").format(radius.toInt())
        } else {
            radiusString = CelestiaString("%d m", "").format((radius * 1000).toInt())
        }
    }

    str += if (body.isEllipsoid) {
        CelestiaString("Equatorial radius: %s", "").format(radiusString)
    } else {
        CelestiaString("Size: %s", "").format(radiusString)
    }

    val time = simulation.time
    val orbit = body.getOrbitAtTime(time)
    val rotation = body.getRotationModelAtTime(time)

    val orbitalPeriod: Double = if (orbit.isPeriodic) orbit.period else 0.0
    if (rotation.isPeriodic && body.type != CelestiaBody.BODY_TYPE_SPACECRAFT) {
        var rotPeriod = rotation.period
        var dayLength = 0.0

        if (orbit.isPeriodic) {
            val siderealDaysPerYear = orbitalPeriod / rotPeriod
            val solarDaysPerYear = siderealDaysPerYear - 1.0
            if (solarDaysPerYear > 0.0001) {
                dayLength = orbitalPeriod / (siderealDaysPerYear - 1.0)
            }
        }

        val unitTemplate: String
        if (rotPeriod < 2.0) {
            rotPeriod *= 24
            dayLength *= 24

            unitTemplate = CelestiaString("%.2f hours", "")
        } else {
            unitTemplate = CelestiaString("%.2f days", "")
        }
        str += "\n"
        str += CelestiaString("Sidereal rotation period: %s", "").format(CelestiaString(unitTemplate, "").format(rotPeriod))

        if (dayLength != 0.0) {
            str += "\n"
            str += CelestiaString("Length of day: %s", "").format(CelestiaString(unitTemplate, "").format(dayLength))
        }

        if (body.hasRings()) {
            str += "\n"
            str += CelestiaString("Has rings", "")
        }

        if (body.hasAtmosphere()) {
            str += "\n"
            str += CelestiaString("Has atmosphere", "")
        }
    }
    return str
}

private fun CelestiaAppCore.getOverviewForStar(star: CelestiaStar): String {
    var str = ""

    val time = simulation.time
    val celPos = star.getPositionAtTime(time).offsetFrom(CelestiaUniversalCoord.getZero())
    val eqPos = CelestiaUtils.eclipticToEquatorial(CelestiaUtils.celToJ2000Ecliptic(celPos))
    val sph = CelestiaUtils.rectToSpherical(eqPos)

    val hms = CelestiaDMS(sph.x)
    str += CelestiaString("RA: %dh %dm %.2fs", "").format(hms.hours, hms.minutes, hms.seconds)

    str += "\n"
    val dms = CelestiaDMS(sph.y)
    str += CelestiaString("DEC: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds)

    return str
}

private fun getOverviewForDSO(dso: CelestiaDSO): String {
    var str = ""

    val celPos = dso.position
    val eqPos = CelestiaUtils.eclipticToEquatorial(CelestiaUtils.celToJ2000Ecliptic(celPos))
    var sph = CelestiaUtils.rectToSpherical(eqPos)

    val hms = CelestiaDMS(sph.x)
    str += CelestiaString("RA: %dh %dm %.2fs", "").format(hms.hours, hms.minutes, hms.seconds)

    str += "\n"
    var dms = CelestiaDMS(sph.y)
    str += CelestiaString("DEC: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds)

    val galPos = CelestiaUtils.equatorialToGalactic(eqPos)
    sph = CelestiaUtils.rectToSpherical(galPos)

    str += "\n"
    dms = CelestiaDMS(sph.x)
    str += CelestiaString("L: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds)

    str += "\n"
    dms = CelestiaDMS(sph.y)
    str += CelestiaString("B: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds)

    return str
}