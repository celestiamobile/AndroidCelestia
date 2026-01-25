package space.celestia.mobilecelestia.browser.viewmodel

import space.celestia.celestia.AstroObject
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.DSO
import space.celestia.celestia.Observer
import space.celestia.celestia.Simulation
import space.celestia.celestia.Star
import space.celestia.celestia.StarBrowser
import space.celestia.celestia.Universe
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
        brightestStars = universe.createStarBrowserRootItem(
            StarBrowser.KIND_BRIGHTEST, observer,
            CelestiaString("Brightest Stars (Absolute Magnitude)", ""), true, null)
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
        ),
        CelestiaString("Solar System", "Tab for solar system in Star Browser"), sol, universe, BrowserPredefinedItem.CategoryInfo("B2E44BE0-9DF7-FAB9-92D4-F8D323D31250", false)
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
            list.add(
                BrowserItem.KeyValuePair(name,
                    BrowserItem(name, null, item, this@createStarBrowserRootItem)
                ))
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
    val nearest = createStarBrowserRootItem(
        StarBrowser.KIND_NEAREST, observer,
        CelestiaString("Nearest Stars", ""), true, null)
    val brighter = createStarBrowserRootItem(
        StarBrowser.KIND_BRIGHTER, observer,
        CelestiaString("Brightest Stars", ""), true, null)
    val hasPlanets = createStarBrowserRootItem(
        StarBrowser.KIND_WITH_PLANETS, observer,
        CelestiaString("Stars with Planets", ""), true, BrowserPredefinedItem.CategoryInfo("1B0E1953-C21C-D628-7FA6-33A3ABBD1B40", false))
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

    val barredSpiralItems = Pair(
        CelestiaString(
            "Barred Spiral Galaxies",
            ""
        ), hashMapOf<String, BrowserItem>())
    val spiralItems = Pair(CelestiaString("Spiral Galaxies", ""), hashMapOf<String, BrowserItem>())
    val ellipticalItems = Pair(
        CelestiaString(
            "Elliptical Galaxies",
            ""
        ), hashMapOf<String, BrowserItem>())
    val lenticularItems = Pair(
        CelestiaString(
            "Lenticular Galaxies",
            ""
        ), hashMapOf<String, BrowserItem>())
    val irregularItems = Pair(
        CelestiaString(
            "Irregular Galaxies",
            ""
        ), hashMapOf<String, BrowserItem>())
    val galaxyItems = hashMapOf(
        "SBa" to barredSpiralItems,
        "SBb" to barredSpiralItems,
        "SBc" to barredSpiralItems,
        "Sa" to spiralItems,
        "Sb" to spiralItems,
        "Sc" to spiralItems,
        "S0" to lenticularItems,
        "E0" to ellipticalItems,
        "E1" to ellipticalItems,
        "E2" to ellipticalItems,
        "E3" to ellipticalItems,
        "E4" to ellipticalItems,
        "E5" to ellipticalItems,
        "E6" to ellipticalItems,
        "E7" to ellipticalItems,
        "Irr" to irregularItems,
    )
    val emissionItems = Pair(CelestiaString("Emission Nebulae", ""), hashMapOf<String, BrowserItem>())
    val reflectionItems = Pair(
        CelestiaString(
            "Reflection Nebulae",
            ""
        ), hashMapOf<String, BrowserItem>())
    val darkItems = Pair(CelestiaString("Dark Nebulae", ""), hashMapOf<String, BrowserItem>())
    val planetaryItems = Pair(
        CelestiaString(
            "Planetary Nebulae",
            ""
        ), hashMapOf<String, BrowserItem>())
    val supernovaRemnantItems = Pair(
        CelestiaString(
            "Supernova Remnants",
            ""
        ), hashMapOf<String, BrowserItem>())
    val hiiRegionItems = Pair(CelestiaString("H II Regions", ""), hashMapOf<String, BrowserItem>())
    val protoplanetaryItems = Pair(
        CelestiaString(
            "Protoplanetary Nebulae",
            ""
        ), hashMapOf<String, BrowserItem>())
    val unknownItems = Pair(CelestiaString("Unknown Nebulae", ""), hashMapOf<String, BrowserItem>())
    val nebulaItems = hashMapOf(
        "Emission" to emissionItems,
        "Reflection" to reflectionItems,
        "Dark" to darkItems,
        "Planetary" to planetaryItems,
        "Supernova Remnants" to supernovaRemnantItems,
        "HII_Region" to hiiRegionItems,
        "Protoplanetary" to protoplanetaryItems,
        " " to unknownItems,
    )
    val globularItems = hashMapOf<String, BrowserItem>()
    val openClusterItems = hashMapOf<String, BrowserItem>()

    for (i in 0 until dsoCatalog.count) {
        val dso = dsoCatalog.getDSO(i)
        val arrayListToAdd = when (dso.objectType) {
            DSO.OBJECT_TYPE_GALAXY -> galaxyItems[dso.type]?.second
            DSO.OBJECT_TYPE_GLOBULAR -> globularItems
            DSO.OBJECT_TYPE_NEBULA -> nebulaItems[dso.type]?.second
            DSO.OBJECT_TYPE_OPEN_CLUSTER -> openClusterItems
            else -> null
        }

        val name = dsoCatalog.getDSOName(dso)
        arrayListToAdd?.put(name, BrowserItem(name, null, dso, this))
    }

    val results = arrayListOf<BrowserItem.KeyValuePair>()
    val galaxyBrowserItems = arrayListOf<BrowserItem.KeyValuePair>()
    for ((name, items) in listOf(barredSpiralItems, spiralItems, ellipticalItems, lenticularItems, irregularItems)) {
        if (items.isEmpty()) continue
        galaxyBrowserItems.add(BrowserItem.KeyValuePair(name, BrowserItem(name, null, items)))
    }
    results.add(
        BrowserItem.KeyValuePair(
            CelestiaString("Galaxies", ""), BrowserPredefinedItem(
                CelestiaString("Galaxies", ""), null, galaxyBrowserItems, galaxyCategory)))
    results.add(
        BrowserItem.KeyValuePair(
            CelestiaString("Globulars", ""),
            BrowserItem(CelestiaString("Globulars", ""), null, globularItems)
        ))
    val nebulaBrowserItems = arrayListOf<BrowserItem.KeyValuePair>()
    for ((name, items) in listOf(emissionItems, reflectionItems, darkItems, planetaryItems, supernovaRemnantItems, hiiRegionItems, protoplanetaryItems, unknownItems)) {
        if (items.isEmpty()) continue
        nebulaBrowserItems.add(BrowserItem.KeyValuePair(name, BrowserItem(name, null, items)))
    }
    results.add(
        BrowserItem.KeyValuePair(
            CelestiaString("Nebulae", ""), BrowserPredefinedItem(
                CelestiaString("Nebulae", ""), null, nebulaBrowserItems, nebulaCategory)))
    results.add(
        BrowserItem.KeyValuePair(
            CelestiaString("Open Clusters", ""),
            BrowserItem(CelestiaString("Open Clusters", ""), null, openClusterItems)
        ))

    return BrowserItem(
        CelestiaString("Deep Sky Objects", ""),
        CelestiaString("DSOs", "Tab for deep sky objects in Star Browser"),
        results
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