/*
 * BrowserItem.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.celestia.*
import space.celestia.celestia.BrowserItem
import space.celestia.mobilecelestia.utils.CelestiaString

private var solRoot: BrowserItem? = null
private var starRoot: BrowserItem? = null
private var dsoRoot: BrowserItem? = null

fun Simulation.createAllBrowserItems() {
    solRoot = universe.createSolBrowserRoot()
    starRoot = createStarBrowserRoot()
    dsoRoot = universe.createDSOBrowserRoot()
}

private fun Universe.createSolBrowserRoot(): BrowserItem? {
    val sol = findObject("Sol").star ?: return null
    return BrowserItem(
        starCatalog.getStarName(
            sol
        ), CelestiaString("Solar System", ""), sol, this
    )
}

fun Universe.solBrowserRoot(): BrowserItem? {
    if (solRoot == null)
        solRoot = createSolBrowserRoot()
    return solRoot
}

private fun Simulation.createStarBrowserRoot(): BrowserItem {
    fun List<Star>.createBrowserMap(): Map<String, BrowserItem> {
        val map = HashMap<String, BrowserItem>()
        for (item in this) {
            val name = universe.starCatalog.getStarName(item)
            map[name] = BrowserItem(
                name,
                null,
                item,
                universe
            )
        }
        return map
    }
    val nearest = getStarBrowser(StarBrowser.KIND_NEAREST).use {
        it.stars
    }.createBrowserMap()
    val brightest = getStarBrowser(StarBrowser.KIND_BRIGHTEST).use {
        it.stars
    }.createBrowserMap()
    val hasPlanets = getStarBrowser(StarBrowser.KIND_WITH_PLANETS).use {
        it.stars
    }.createBrowserMap()
    val nearestItem = BrowserItem(
        CelestiaString(
            "Nearest Stars",
            ""
        ), null, nearest
    )
    val brightestItem = BrowserItem(
        CelestiaString(
            "Brightest Stars",
            ""
        ), null, brightest
    )
    val hasPlanetItem = BrowserItem(
        CelestiaString(
            "Stars with Planets",
            ""
        ), null, hasPlanets
    )

    return BrowserItem(
        CelestiaString("Stars", ""), null, mapOf(
            nearestItem.name to nearestItem,
            brightestItem.name to brightestItem,
            hasPlanetItem.name to hasPlanetItem
        )
    )
}

fun Simulation.starBrowserRoot(): BrowserItem {
    if (starRoot == null)
        starRoot = createStarBrowserRoot()
    return starRoot!!
}

private fun Universe.createDSOBrowserRoot(): BrowserItem {
    val typeMap = mapOf(
        "SB" to CelestiaString("Galaxies (Barred Spiral)", ""),
        "S" to CelestiaString("Galaxies (Spiral)", ""),
        "E" to CelestiaString("Galaxies (Elliptical)", ""),
        "Irr" to CelestiaString("Galaxies (Irregular)", ""),
        "Neb" to CelestiaString("Nebulae", ""),
        "Glob" to CelestiaString("Globulars", ""),
        "Open cluster" to CelestiaString("Open Clusters", ""),
        "Unknown" to CelestiaString("Unknown", "")
    )
    val prefixes = listOf("SB", "S", "E", "Irr", "Neb", "Glob", "Open cluster")

    val tempMap = HashMap<String, HashMap<String, BrowserItem>>()

    for (i in 0 until dsoCatalog.count) {
        val dso = dsoCatalog.getDSO(i)
        var matchType = prefixes.find { dso.type.startsWith(it) }
        if (matchType == null)
            matchType = "Unknown"

        val name = dsoCatalog.getDSOName(dso)
        val item =
            BrowserItem(name, null, dso, this)

        if (tempMap[matchType] != null)
            tempMap[matchType]!![name] = item
        else
            tempMap[matchType] = hashMapOf(name to item)
    }

    val results = HashMap<String, BrowserItem>()
    for (map in tempMap) {
        val fullName = typeMap[map.key] ?: error("${map.key} not found")
        results[fullName] = BrowserItem(
            fullName,
            null,
            map.value
        )
    }

    return BrowserItem(
        CelestiaString(
            "Deep Sky Objects",
            ""
        ), CelestiaString("DSOs", ""), results
    )
}

fun Universe.dsoBrowserRoot(): BrowserItem {
    if (dsoRoot == null)
        dsoRoot = createDSOBrowserRoot()
    return dsoRoot!!
}

class BrowserUIItem(val item: BrowserItem, val isLeaf: Boolean) : RecyclerViewItem

class BrowserUIItemMenu(val item: BrowserItem, val icon: Int)