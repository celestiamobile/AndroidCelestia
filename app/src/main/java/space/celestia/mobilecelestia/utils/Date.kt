/*
 * Date.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import space.celestia.celestia.Utils
import java.util.*

val Date.julianDay: Double
    get() {
        val gc = GregorianCalendar()
        gc.timeZone = TimeZone.getTimeZone("GMT")
        gc.time = this
        val era = gc.get(GregorianCalendar.ERA)
        val year = gc.get(GregorianCalendar.YEAR)
        val month = gc.get(GregorianCalendar.MONTH) + 1
        val day = gc.get(GregorianCalendar.DAY_OF_MONTH)
        val hour = gc.get(GregorianCalendar.HOUR_OF_DAY)
        val minute = gc.get(GregorianCalendar.MINUTE)
        val second = gc.get(GregorianCalendar.SECOND)
        val millisecond = gc.get(GregorianCalendar.MILLISECOND)

        return Utils.getJulianDay(era, year, month, day, hour, minute, second, millisecond)
    }

fun createDateFromJulianDay(julianDay: Double): Date {
    val compos = Utils.getJulianDayComponents(julianDay)
    val gc = GregorianCalendar()
    gc.timeZone = TimeZone.getTimeZone("GMT")
    gc.set(GregorianCalendar.ERA, compos[0])
    gc.set(GregorianCalendar.YEAR, compos[1])
    gc.set(GregorianCalendar.MONTH, compos[2] - 1)
    gc.set(GregorianCalendar.DAY_OF_MONTH, compos[3])
    gc.set(GregorianCalendar.HOUR_OF_DAY, compos[4])
    gc.set(GregorianCalendar.MINUTE, compos[5])
    gc.set(GregorianCalendar.SECOND, compos[6])
    gc.set(GregorianCalendar.MILLISECOND, compos[7])
    return gc.time
}
