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

import space.celestia.celestia.*
import space.celestia.mobilecelestia.utils.CelestiaString

private var solRoot: BrowserItem? = null
private var starRoot: BrowserItem? = null
private var dsoRoot: BrowserItem? = null
private var brightestStars: BrowserItem? = null

fun Simulation.createStaticBrowserItems(observer: Observer) {
    if (solRoot == null)
        solRoot = createSolBrowserRoot()
    if (dsoRoot == null)
        dsoRoot = universe.createDSOBrowserRoot()
    if (brightestStars == null)
        brightestStars = universe.createStarBrowserRootItem(StarBrowser.KIND_BRIGHTEST, observer, CelestiaString("Brightest Stars (Absolute Magnitude)",""), true, null)
}

fun Universe.createDynamicBrowserItems(observer: Observer) {
    starRoot = createStarBrowserRoot(observer)
}

private fun Simulation.createSolBrowserRoot(): BrowserItem? {
    val sol = findObject("Sol").star ?: return null
    val universe = this.universe
    val catalog = universe.starCatalog
    return BrowserPredefinedItem(
        catalog.getStarName(
            sol
        ), CelestiaString("Solar System", "Tab for solar system in Star Browser"), sol, universe, BrowserPredefinedItem.CategoryInfo("B2E44BE0-9DF7-FAB9-92D4-F8D323D31250", false)
    )
}

fun Simulation.solBrowserRoot(): BrowserItem? {
    if (solRoot == null)
        solRoot = createSolBrowserRoot()
    return solRoot
}

private fun Universe.createStarBrowserRootItem(kind: Int, observer: Observer, title: String, ordered: Boolean, categoryInfo: BrowserPredefinedItem.CategoryInfo?): BrowserItem {
    fun List<Star>.createBrowserMap(): Map<String, BrowserItem> {
        val map = HashMap<String, BrowserItem>()
        for (item in this) {
            val name = starCatalog.getStarName(item)
            map[name] = BrowserItem(
                name,
                null,
                item,
                this@createStarBrowserRootItem
            )
        }
        return map
    }
    fun List<Star>.createOrderedBrowserMap(): List<BrowserItem.KeyValuePair> {
        val list = arrayListOf<BrowserItem.KeyValuePair>()
        for (item in this) {
            val name = starCatalog.getStarName(item)
            list.add(BrowserItem.KeyValuePair(name, BrowserItem(name, null, item, this@createStarBrowserRootItem)))
        }
        return list
    }

    return if (ordered) {
        val items = getStarBrowser(kind, observer).use {
            it.stars
        }.createOrderedBrowserMap()
        BrowserPredefinedItem(title, null, items, categoryInfo)
    } else {
        val items = getStarBrowser(kind, observer).use {
            it.stars
        }.createBrowserMap()
        BrowserPredefinedItem(title, null, items, categoryInfo)
    }
}

private fun Universe.createStarBrowserRoot(observer: Observer): BrowserItem {
    val nearest = createStarBrowserRootItem(StarBrowser.KIND_NEAREST, observer, CelestiaString("Nearest Stars", ""), true, null)
    val brighter = createStarBrowserRootItem(StarBrowser.KIND_BRIGHTER, observer, CelestiaString("Brightest Stars", ""), true, null)
    val hasPlanets = createStarBrowserRootItem(StarBrowser.KIND_WITH_PLANETS, observer, CelestiaString("Stars with Planets",""), true, BrowserPredefinedItem.CategoryInfo("1B0E1953-C21C-D628-7FA6-33A3ABBD1B40", false))
    val hashMap = hashMapOf(
        nearest.name to nearest,
        brighter.name to brighter,
        hasPlanets.name to hasPlanets
    )
    val brightest = brightestStars
    if (brightest != null)
        hashMap[brightest.name] = brightest
    return BrowserPredefinedItem(CelestiaString("Stars", "Tab for stars in Star Browser"), null, hashMap, BrowserPredefinedItem.CategoryInfo("5E023C91-86F8-EC5C-B53C-E3780163514F", true))
}

fun Universe.starBrowserRoot(observer: Observer): BrowserItem {
    if (starRoot == null)
        starRoot = createStarBrowserRoot(observer)
    return starRoot!!
}

private fun Universe.createDSOBrowserRoot(): BrowserItem {
    val galaxyCategory = BrowserPredefinedItem.CategoryInfo("56FF5D9F-44F1-CE1D-0615-5655E3C851EF", true)
    val nebulaCategory = BrowserPredefinedItem.CategoryInfo("3F7546F9-D225-5194-A228-C63281B5C6FD", true)
    val typeMap: Map<String, Pair<String, BrowserPredefinedItem.CategoryInfo?>> = mapOf(
        "SB" to Pair(CelestiaString("Galaxies (Barred Spiral)", ""), galaxyCategory),
        "S" to Pair(CelestiaString("Galaxies (Spiral)", ""), galaxyCategory),
        "E" to Pair(CelestiaString("Galaxies (Elliptical)", ""), galaxyCategory),
        "Irr" to Pair(CelestiaString("Galaxies (Irregular)", ""), galaxyCategory),
        "Neb" to Pair(CelestiaString("Nebulae", ""), nebulaCategory),
        "Glob" to Pair(CelestiaString("Globulars", ""), null),
        "Open cluster" to Pair(CelestiaString("Open Clusters", ""), null),
        "Unknown" to Pair(CelestiaString("Unknown", ""), null),
    )
    val prefixes = listOf("SB", "S", "E", "Irr", "Neb", "Glob", "Open cluster")

    val objectTypeMapping  = hashMapOf(
        DSO.OBJECT_TYPE_GLOBULAR to "Glob",
        DSO.OBJECT_TYPE_NEBULA to "Neb",
        DSO.OBJECT_TYPE_OPEN_CLUSTER to "Open cluster",
    )

    val tempMap = HashMap<String, HashMap<String, BrowserItem>>()

    for (i in 0 until dsoCatalog.count) {
        val dso = dsoCatalog.getDSO(i)
        val matchType = objectTypeMapping[dso.objectType] ?: prefixes.find { dso.type.startsWith(it) } ?: "Unknown"

        val name = dsoCatalog.getDSOName(dso)
        val item =
            BrowserItem(name, null, dso, this)

        if (tempMap[matchType] != null)
            tempMap[matchType]!![name] = item
        else
            tempMap[matchType] = hashMapOf(name to item)
    }

    val results = HashMap<String, BrowserItem>()
    for (prefix in prefixes) {
        val info = typeMap[prefix]!!
        results[info.first] = BrowserPredefinedItem(
            info.first,
            null,
            tempMap[prefix] ?: mutableMapOf(),
            info.second
        )
    }

    return BrowserItem(
        CelestiaString(
            "Deep Sky Objects",
            ""
        ), CelestiaString("DSOs", "Tab for deep sky objects in Star Browser"), results
    )
}

fun Universe.dsoBrowserRoot(): BrowserItem {
    if (dsoRoot == null)
        dsoRoot = createDSOBrowserRoot()
    return dsoRoot!!
}

class BrowserPredefinedItem: BrowserItem {
    class CategoryInfo(val id: String, val isLeaf: Boolean)

    val categoryInfo: CategoryInfo?

    constructor(name: String, alternativeName: String?, children: Map<String, BrowserItem>, categoryInfo: CategoryInfo?) : super(name, alternativeName, children) {
        this.categoryInfo = categoryInfo
    }

    constructor(name: String, alternativeName: String?, `object`: AstroObject, provider: ChildrenProvider?, categoryInfo: CategoryInfo?): super(name, alternativeName, `object`, provider) {
        this.categoryInfo = categoryInfo
    }

    constructor(name: String, alternativeName: String?, children: List<KeyValuePair>, categoryInfo: CategoryInfo?) : super(name, alternativeName, children) {
        this.categoryInfo = categoryInfo
    }
}

class BrowserUIItem(val item: BrowserItem, val isLeaf: Boolean)