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

import space.celestia.celestia.*
import space.celestia.mobilecelestia.favorite.BookmarkNode
import java.text.DateFormat
import java.util.*

val AppCore.currentBookmark: BookmarkNode?
    get() {
        val selection = simulation.selection
        if (selection.isEmpty) return null
        val name = simulation.universe.getNameForSelection(selection)
        return BookmarkNode(name, currentURL, null)
    }

fun AppCore.getOverviewForSelection(selection: Selection): String {
    return when (val obj = selection.`object`) {
        is Body -> {
            getOverviewForBody(obj)
        }
        is Star -> {
            getOverviewForStar(obj)
        }
        is DSO -> {
            getOverviewForDSO(obj)
        }
        else -> {
            CelestiaString("No overview available.", "")
        }
    }
}

private fun AppCore.getOverviewForBody(body: Body): String {
    val lines = arrayListOf<String>()

    val radius = body.radius
    val radiusString: String
    val oneMiInKm = 1.609344f
    val oneFtInKm = 0.0003048f
    if (measurementSystem == AppCore.MEASUREMENT_SYSTEM_IMPERIAL) {
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

    lines.add(if (body.isEllipsoid) {
        CelestiaString("Equatorial radius: %s", "").format(radiusString)
    } else {
        CelestiaString("Size: %s", "").format(radiusString)
    })

    val time = simulation.time
    val orbit = body.getOrbitAtTime(time)
    val rotation = body.getRotationModelAtTime(time)

    val orbitalPeriod: Double = if (orbit.isPeriodic) orbit.period else 0.0
    if (rotation.isPeriodic && body.type != Body.BODY_TYPE_SPACECRAFT) {
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
        lines.add(CelestiaString("Sidereal rotation period: %s", "").format(CelestiaString(unitTemplate, "").format(rotPeriod)))

        if (dayLength != 0.0) {
            lines.add(CelestiaString("Length of day: %s", "").format(CelestiaString(unitTemplate, "").format(dayLength)))
        }
    }

    if (body.hasRings()) {
        lines.add(CelestiaString("Has rings", ""))
    }

    if (body.hasAtmosphere()) {
        lines.add(CelestiaString("Has atmosphere", ""))
    }

    val timeline = body.timeline
    if (timeline.phaseCount > 0) {
        val startTime = timeline.getPhase(0).startTime
        val endTime = timeline.getPhase(timeline.phaseCount - 1).endTime
        val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        if (!startTime.isInfinite()) {
            lines.add(CelestiaString("Start time: %s", "").format(formatter.format(Utils.createDateFromJulianDay(startTime))))
        }
        if (!endTime.isInfinite()) {
            lines.add(CelestiaString("End time: %s", "").format(formatter.format(Utils.createDateFromJulianDay(endTime))))
        }
    }

    return lines.joinToString(separator = "\n")
}

private fun AppCore.getOverviewForStar(star: Star): String {
    val lines = arrayListOf<String>()

    lines.add(CelestiaString("Spectral type: %s", "").format(star.spectralType))

    val time = simulation.time
    val celPos = star.getPositionAtTime(time).use{ it.offsetFrom(UniversalCoord.getZero()) }
    val eqPos = Utils.eclipticToEquatorial(Utils.celToJ2000Ecliptic(celPos))
    val sph = Utils.rectToSpherical(eqPos)

    val hms = DMS(sph.x)
    lines.add(CelestiaString("RA: %dh %dm %.2fs", "").format(hms.hours, hms.minutes, hms.seconds))

    val dms = DMS(sph.y)
    lines.add(CelestiaString("DEC: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds))

    return lines.joinToString(separator = "\n")
}

private fun getOverviewForDSO(dso: DSO): String {
    val lines = arrayListOf<String>()

    lines.add(dso.description)

    val celPos = dso.position
    val eqPos = Utils.eclipticToEquatorial(Utils.celToJ2000Ecliptic(celPos))
    var sph = Utils.rectToSpherical(eqPos)

    val hms = DMS(sph.x)
    lines.add(CelestiaString("RA: %dh %dm %.2fs", "").format(hms.hours, hms.minutes, hms.seconds))

    var dms = DMS(sph.y)
    lines.add(CelestiaString("DEC: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds))

    val galPos = Utils.equatorialToGalactic(eqPos)
    sph = Utils.rectToSpherical(galPos)

    dms = DMS(sph.x)
    lines.add(CelestiaString("L: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds))

    dms = DMS(sph.y)
    lines.add(CelestiaString("B: %d° %d′ %.2f″", "").format(dms.hours, dms.minutes, dms.seconds))

    return lines.joinToString(separator = "\n")
}