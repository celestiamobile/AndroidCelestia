/*
 * Celestia.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
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

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

private val Float.radiusString: String
    get() {
        if (this < 1) {
            return "${(this * 1000).toInt()} m"
        }
        return "${this.toInt()} km"
    }

public fun CelestiaAppCore.getOverviewForSelection(selection: CelestiaSelection): String {
    val obj = selection.`object`
    return when (obj) {
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

    str += if (body.isEllipsoid) {
        "Equatorial radius: ${body.radius.radiusString}"
    } else {
        "Size: ${body.radius.radiusString}"
    }

    val time = simulation.time
    val orbit = body.getOrbitAtTime(time)
    val rotation = body.getRotationModelAtTime(time)

    val orbitalPeriod: Double = if (orbit.isPeriodic) orbit.period else 0.0
    if (rotation.isPeriodic && body.type != CelestiaBody.BODY_TYPE_SPACECRAFT) {
        var rotPeriod = rotation.period
        var dayLength: Double = 0.0

        if (orbit.isPeriodic) {
            val siderealDaysPerYear = orbitalPeriod / rotPeriod
            val solarDaysPerYear = siderealDaysPerYear - 1.0
            if (solarDaysPerYear > 0.0001) {
                dayLength = orbitalPeriod / (siderealDaysPerYear - 1.0)
            }
        }

        val unit: String
        if (rotPeriod < 2.0) {
            rotPeriod *= 24
            dayLength *= 24

            unit = "hours"
        } else {
            unit = "days"
        }
        str += "\n"
        str += "Sidereal rotation period: ${rotPeriod.format(2)} $unit"

        if (dayLength != 0.0) {
            str += "\n"
            str += "Length of day: ${dayLength.format(2)} $unit"
        }

        if (body.hasRings()) {
            str += "\n"
            str += "Has rings"
        }

        if (body.hasAtmosphere()) {
            str += "\n"
            str += "Has atmosphere"
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
    str += "RA: ${hms.hours}h ${hms.minutes}m ${hms.seconds.format(2)}s"

    str += "\n"
    val dms = CelestiaDMS(sph.y)
    str += "DEC: ${dms.hours}° ${dms.minutes}′ ${dms.seconds.format(2)}″"

    return str
}

private fun CelestiaAppCore.getOverviewForDSO(dso: CelestiaDSO): String {
    var str = ""

    val celPos = dso.position
    val eqPos = CelestiaUtils.eclipticToEquatorial(CelestiaUtils.celToJ2000Ecliptic(celPos))
    var sph = CelestiaUtils.rectToSpherical(eqPos)

    val hms = CelestiaDMS(sph.x)
    str += "RA: ${hms.hours}h ${hms.minutes}m ${hms.seconds.format(2)}s"

    str += "\n"
    var dms = CelestiaDMS(sph.y)
    str += "DEC: ${dms.hours}° ${dms.minutes}′ ${dms.seconds.format(2)}″"

    val galPos = CelestiaUtils.equatorialToGalactic(eqPos)
    sph = CelestiaUtils.rectToSpherical(galPos)

    str += "\n"
    dms = CelestiaDMS(sph.x)
    str += "L: ${dms.hours}° ${dms.minutes}′ ${dms.seconds.format(2)}″"

    str += "\n"
    dms = CelestiaDMS(sph.y)
    str += "B: ${dms.hours}° ${dms.minutes}′ ${dms.seconds.format(2)}″"

    return str
}