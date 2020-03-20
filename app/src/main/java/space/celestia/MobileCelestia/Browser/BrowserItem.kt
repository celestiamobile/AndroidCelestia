package space.celestia.MobileCelestia.Browser

import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Core.*

fun CelestiaUniverse.solBrowserRoot(): CelestiaBrowserItem {
    val sol = findObject("Sol").star!!
    return CelestiaBrowserItem(starCatalog.getStarName(sol), sol, this)
}

fun CelestiaSimulation.starBrowserRoot(): CelestiaBrowserItem {
    fun List<CelestiaStar>.createBrowserMap(): Map<String, CelestiaBrowserItem> {
        val map = HashMap<String, CelestiaBrowserItem>()
        for (item in this) {
            val name = universe.starCatalog.getStarName(item)
            map[name] = CelestiaBrowserItem(name, item, universe)
        }
        return map
    }
    val nearest = getStarBrowser(CelestiaStarBrowser.KIND_NEAREST).stars.createBrowserMap()
    val brightest = getStarBrowser(CelestiaStarBrowser.KIND_BRIGHTEST).stars.createBrowserMap()
    val hasPlanets = getStarBrowser(CelestiaStarBrowser.KIND_WITH_PLANETS).stars.createBrowserMap()
    val nearestItem = CelestiaBrowserItem("Nearest Stars", nearest)
    val brightestItem = CelestiaBrowserItem("Brightest Stars", brightest)
    val hasPlanetItem = CelestiaBrowserItem("Stars with Planets", hasPlanets)

    return CelestiaBrowserItem("Stars", mapOf(
        nearestItem.name to nearestItem,
        brightestItem.name to brightestItem,
        hasPlanetItem.name to hasPlanetItem
    ))
}

fun CelestiaUniverse.dsoBrowserRoot(): CelestiaBrowserItem {
    val typeMap = mapOf<String, String>(
        "SB" to "Galaxies (Barred Spiral)",
        "S" to "Galaxies (Spiral)",
        "E" to "Galaxies (Elliptical)",
        "Irr" to "Galaxies (Irregular)",
        "Neb" to "Nebulae",
        "Glob" to "Globulars",
        "Open cluster" to "Open Clusters",
        "Unknown" to "Unknown"
    )
    val prefixes = listOf<String>("SB", "S", "E", "Irr", "Neb", "Glob", "Open cluster")

    val tempMap = HashMap<String, HashMap<String, CelestiaBrowserItem>>()

    for (i in 0 until dsoCatalog.count) {
        val dso = dsoCatalog.getDSO(i)
        var matchType = prefixes.find { dso.type.startsWith(it) }
        if (matchType == null)
            matchType = "Unknown"

        val name = dsoCatalog.getDSOName(dso)
        val item = CelestiaBrowserItem(name, dso, this)

        if (tempMap[matchType] != null)
            tempMap[matchType]!![name] = item
        else
            tempMap[matchType] = hashMapOf(name to item)
    }

    val results = HashMap<String, CelestiaBrowserItem>()
    for (map in tempMap) {
        val fullName = typeMap[map.key]!!
        results[fullName] = CelestiaBrowserItem(fullName, map.value)
    }

    return CelestiaBrowserItem("Deep Sky Objects", results)
}

class BrowserItem(val item: CelestiaBrowserItem, val final: Boolean) : RecyclerViewItem {}

class BrowserItemMenu(val item: CelestiaBrowserItem, val icon: Int) {}