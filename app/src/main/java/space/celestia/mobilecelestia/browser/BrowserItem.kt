/*
 * BrowserItem.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.core.*
import space.celestia.mobilecelestia.utils.CelestiaString

private var solRoot: CelestiaBrowserItem? = null
private var starRoot: CelestiaBrowserItem? = null
private var dsoRoot: CelestiaBrowserItem? = null

fun CelestiaSimulation.createAllBrowserItems() {
    solRoot = universe.createSolBrowserRoot()
    starRoot = createStarBrowserRoot()
    dsoRoot = universe.createDSOBrowserRoot()
}

private fun CelestiaUniverse.createSolBrowserRoot(): CelestiaBrowserItem {
    val sol = findObject("Sol").star!!
    return CelestiaBrowserItem(starCatalog.getStarName(sol), CelestiaString("Solar System", ""), sol, this)
}

fun CelestiaUniverse.solBrowserRoot(): CelestiaBrowserItem {
    if (solRoot == null)
        solRoot = createSolBrowserRoot()
    return solRoot!!
}

private fun CelestiaSimulation.createStarBrowserRoot(): CelestiaBrowserItem {
    fun List<CelestiaStar>.createBrowserMap(): Map<String, CelestiaBrowserItem> {
        val map = HashMap<String, CelestiaBrowserItem>()
        for (item in this) {
            val name = universe.starCatalog.getStarName(item)
            map[name] = CelestiaBrowserItem(name, null, item, universe)
        }
        return map
    }
    val nearest = getStarBrowser(CelestiaStarBrowser.KIND_NEAREST).stars.createBrowserMap()
    val brightest = getStarBrowser(CelestiaStarBrowser.KIND_BRIGHTEST).stars.createBrowserMap()
    val hasPlanets = getStarBrowser(CelestiaStarBrowser.KIND_WITH_PLANETS).stars.createBrowserMap()
    val nearestItem = CelestiaBrowserItem(CelestiaString("Nearest Stars", ""), null, nearest)
    val brightestItem = CelestiaBrowserItem(CelestiaString("Brightest Stars",""), null,  brightest)
    val hasPlanetItem = CelestiaBrowserItem(CelestiaString("Stars with Planets",""), null, hasPlanets)

    return CelestiaBrowserItem(CelestiaString("Stars", ""), null, mapOf(
        nearestItem.name to nearestItem,
        brightestItem.name to brightestItem,
        hasPlanetItem.name to hasPlanetItem
    ))
}

fun CelestiaSimulation.starBrowserRoot(): CelestiaBrowserItem {
    if (starRoot == null)
        starRoot = createStarBrowserRoot()
    return starRoot!!
}

private fun CelestiaUniverse.createDSOBrowserRoot(): CelestiaBrowserItem {
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

    val tempMap = HashMap<String, HashMap<String, CelestiaBrowserItem>>()

    for (i in 0 until dsoCatalog.count) {
        val dso = dsoCatalog.getDSO(i)
        var matchType = prefixes.find { dso.type.startsWith(it) }
        if (matchType == null)
            matchType = "Unknown"

        val name = dsoCatalog.getDSOName(dso)
        val item = CelestiaBrowserItem(name, null, dso, this)

        if (tempMap[matchType] != null)
            tempMap[matchType]!![name] = item
        else
            tempMap[matchType] = hashMapOf(name to item)
    }

    val results = HashMap<String, CelestiaBrowserItem>()
    for (map in tempMap) {
        val fullName = typeMap[map.key] ?: error("${map.key} not found")
        results[fullName] = CelestiaBrowserItem(fullName, null, map.value)
    }

    return CelestiaBrowserItem(CelestiaString("Deep Sky Objects", ""), CelestiaString("DSOs", ""), results)
}

fun CelestiaUniverse.dsoBrowserRoot(): CelestiaBrowserItem {
    if (dsoRoot == null)
        dsoRoot = createDSOBrowserRoot()
    return dsoRoot!!
}

class BrowserItem(val item: CelestiaBrowserItem, val isLeaf: Boolean) : RecyclerViewItem

class BrowserItemMenu(val item: CelestiaBrowserItem, val icon: Int)