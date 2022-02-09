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
private var brightestStars: BrowserItem? = null
private var starsWithPlanets: BrowserItem? = null

fun Simulation.createStaticBrowserItems() {
    if (solRoot == null)
        solRoot = universe.createSolBrowserRoot()
    if (dsoRoot == null)
        dsoRoot = universe.createDSOBrowserRoot()
    if (brightestStars == null)
        brightestStars = createStarBrowserRootItem(StarBrowser.KIND_BRIGHTEST, CelestiaString("Brightest Stars (Absolute Magnitude)",""), true)
    if (starsWithPlanets == null)
        starsWithPlanets = createStarBrowserRootItem(StarBrowser.KIND_WITH_PLANETS, CelestiaString("Stars with Planets",""), false)
}

fun Simulation.createDynamicBrowserItems() {
    starRoot = createStarBrowserRoot()
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

private fun Simulation.createStarBrowserRootItem(kind: Int, title: String, ordered: Boolean): BrowserItem {
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
    fun List<Star>.createOrderedBrowserMap(): List<BrowserItem.KeyValuePair> {
        val list = arrayListOf<BrowserItem.KeyValuePair>()
        for (item in this) {
            val name = universe.starCatalog.getStarName(item)
            list.add(BrowserItem.KeyValuePair(name, BrowserItem(name, null, item, universe)))
        }
        return list
    }

    return if (ordered) {
        val items = getStarBrowser(kind).use {
            it.stars
        }.createOrderedBrowserMap()
        BrowserItem(title, null, items)
    } else {
        val items = getStarBrowser(kind).use {
            it.stars
        }.createBrowserMap()
        BrowserItem(title, null, items)
    }
}

private fun Simulation.createStarBrowserRoot(): BrowserItem {
    val nearest = createStarBrowserRootItem(StarBrowser.KIND_NEAREST, CelestiaString("Nearest Stars", ""), true)
    val brighter = createStarBrowserRootItem(StarBrowser.KIND_BRIGHTER, CelestiaString("Brightest Stars", ""), true)
    val hashMap = hashMapOf(
        nearest.name to nearest,
        brighter.name to brighter
    )
    val brightest = brightestStars
    if (brightest != null)
        hashMap[brightest.name] = brightest
    val hasPlanets = starsWithPlanets
    if (hasPlanets != null)
        hashMap[hasPlanets.name] = hasPlanets
    return BrowserItem(CelestiaString("Stars", ""), null, hashMap)
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