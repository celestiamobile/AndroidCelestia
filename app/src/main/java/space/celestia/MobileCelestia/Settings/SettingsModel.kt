package space.celestia.MobileCelestia.Settings

import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.TitledFragment

interface SettingsItem : RecyclerViewItem {
    val name: String
}

class SettingsMultiSelectionItem(
    override val name: String,
    val masterKey: String?,
    val selections: List<Selection>
) : SettingsItem {
    class Selection(val name: String, val key: String) : RecyclerViewItem {
    }
}

private val staticDisplayItems: List<SettingsMultiSelectionItem> = listOf(
    SettingsMultiSelectionItem("Objects", null, listOf(
        SettingsMultiSelectionItem.Selection("Planets", "ShowPlanets"),
        SettingsMultiSelectionItem.Selection("Dwarf Planets", "ShowDwarfPlanets"),
        SettingsMultiSelectionItem.Selection("Moons", "ShowMoons"),
        SettingsMultiSelectionItem.Selection("Minor Moons", "ShowMinorMoons"),
        SettingsMultiSelectionItem.Selection("Asteroids", "ShowAsteroids"),
        SettingsMultiSelectionItem.Selection("Comets", "ShowComets"),
        SettingsMultiSelectionItem.Selection("Spacecrafts", "ShowSpacecrafts"),
        SettingsMultiSelectionItem.Selection("Galaxies", "ShowGalaxies"),
        SettingsMultiSelectionItem.Selection("Nebulae", "ShowNebulae"),
        SettingsMultiSelectionItem.Selection("Globulars", "ShowGlobulars"),
        SettingsMultiSelectionItem.Selection("Open Clusters", "ShowOpenClusters")
    )),
    SettingsMultiSelectionItem("Features", null, listOf(
        SettingsMultiSelectionItem.Selection("Atmospheres", "ShowAtmospheres"),
        SettingsMultiSelectionItem.Selection("Clouds", "ShowCloudMaps"),
        SettingsMultiSelectionItem.Selection("Cloud Shadows", "ShowCloudShadows"),
        SettingsMultiSelectionItem.Selection("Night Lights", "ShowNightMaps"),
        SettingsMultiSelectionItem.Selection("Planet Rings", "ShowPlanetRings"),
        SettingsMultiSelectionItem.Selection("Ring Shadows", "ShowRingShadows"),
        SettingsMultiSelectionItem.Selection("Comet Tails", "ShowCometTails"),
        SettingsMultiSelectionItem.Selection("Eclipse Shadows", "ShowEclipseShadows")
    )),
    SettingsMultiSelectionItem("Orbits", "ShowOrbits", listOf(
        SettingsMultiSelectionItem.Selection("Stars", "ShowStellarOrbits"),
        SettingsMultiSelectionItem.Selection("Planets", "ShowPlanetOrbits"),
        SettingsMultiSelectionItem.Selection("Dwarf Planets", "ShowDwarfPlanetOrbits"),
        SettingsMultiSelectionItem.Selection("Moons", "ShowMoonOrbits"),
        SettingsMultiSelectionItem.Selection("Minor Moons", "ShowMinorMoonOrbits"),
        SettingsMultiSelectionItem.Selection("Asteroids", "ShowAsteroidOrbits"),
        SettingsMultiSelectionItem.Selection("Comets", "ShowCometOrbits"),
        SettingsMultiSelectionItem.Selection("Spacecrafts", "ShowSpacecraftOrbits")
    )),
    SettingsMultiSelectionItem("Grids", null, listOf(
        SettingsMultiSelectionItem.Selection("Equatorial", "ShowCelestialSphere"),
        SettingsMultiSelectionItem.Selection("Ecliptic", "ShowEclipticGrid"),
        SettingsMultiSelectionItem.Selection("Horizontal", "ShowHorizonGrid"),
        SettingsMultiSelectionItem.Selection("Galactic", "ShowGalacticGrid")
    )),
    SettingsMultiSelectionItem("Constellations", "ShowDiagrams", listOf(
        SettingsMultiSelectionItem.Selection("Constellation Labels", "ShowConstellationLabels"),
        SettingsMultiSelectionItem.Selection("Localized Constellations", "ShowI18nConstellationLabels"),
        SettingsMultiSelectionItem.Selection("Show Boundaries", "ShowBoundaries")
    )),
    SettingsMultiSelectionItem("Object Labels", null, listOf(
        SettingsMultiSelectionItem.Selection("Stars", "ShowStarLabels"),
        SettingsMultiSelectionItem.Selection("Planets", "ShowPlanetLabels"),
        SettingsMultiSelectionItem.Selection("Dwarf Planets", "ShowDwarfPlanetLabels"),
        SettingsMultiSelectionItem.Selection("Moons", "ShowMoonLabels"),
        SettingsMultiSelectionItem.Selection("Minor Moons", "ShowMinorMoonLabels"),
        SettingsMultiSelectionItem.Selection("Asteroids", "ShowAsteroidLabels"),
        SettingsMultiSelectionItem.Selection("Comets", "ShowCometLabels"),
        SettingsMultiSelectionItem.Selection("Spacecrafts", "ShowSpacecraftLabels"),
        SettingsMultiSelectionItem.Selection("Galaxies", "ShowGalaxyLabels"),
        SettingsMultiSelectionItem.Selection("Nebulae", "ShowNebulaLabels"),
        SettingsMultiSelectionItem.Selection("Globulars", "ShowGlobularLabels"),
        SettingsMultiSelectionItem.Selection("Open Clusters", "ShowOpenClusterLabels")
    )),
    SettingsMultiSelectionItem("Locations", "ShowLocationLabels", listOf(
        SettingsMultiSelectionItem.Selection("Cities", "ShowCityLabels"),
        SettingsMultiSelectionItem.Selection("Observatories", "ShowObservatoryLabels"),
        SettingsMultiSelectionItem.Selection("Landing Sites", "ShowLandingSiteLabels"),
        SettingsMultiSelectionItem.Selection("Mons", "ShowMonsLabels"),
        SettingsMultiSelectionItem.Selection("Mare", "ShowMareLabels"),
        SettingsMultiSelectionItem.Selection("Crater", "ShowCraterLabels"),
        SettingsMultiSelectionItem.Selection("Vallis", "ShowVallisLabels"),
        SettingsMultiSelectionItem.Selection("Terra", "ShowTerraLabels"),
        SettingsMultiSelectionItem.Selection("Volcanoes", "ShowEruptiveCenterLabels")
    ))
)

class SettingsSingleSelectionItem(
    override val name: String,
    val key: String,
    val selections: List<Selection>) : SettingsItem {
    class Selection(val name: String, val value: Int) : RecyclerViewItem {}
}

class SettingsCurrentTimeItem(): SettingsItem {
    override val name: String
        get() = "Current Time"
}

private val staticTimeItems: List<SettingsItem> = listOf(
    SettingsSingleSelectionItem("Time Zone", "TimeZone", listOf(
        SettingsSingleSelectionItem.Selection("Local Time", 0),
        SettingsSingleSelectionItem.Selection("UTC", 1)
    )),
    SettingsSingleSelectionItem("Date Format", "DateFormat", listOf(
        SettingsSingleSelectionItem.Selection("Default", 0),
        SettingsSingleSelectionItem.Selection("YYYY MMM DD HH:MM:SS TZ", 1),
        SettingsSingleSelectionItem.Selection("UTC Offset", 2)
    )),
    SettingsCurrentTimeItem()
)

private val staticAdvancedItems: List<SettingsSingleSelectionItem> = listOf(
    SettingsSingleSelectionItem("Texture Resolution", "Resolution", listOf(
        SettingsSingleSelectionItem.Selection("Low", 0),
        SettingsSingleSelectionItem.Selection("Medium", 1),
        SettingsSingleSelectionItem.Selection("High", 2)
    )),
    SettingsSingleSelectionItem("Star Style", "StarStyle", listOf(
        SettingsSingleSelectionItem.Selection("Fuzzy Points", 0),
        SettingsSingleSelectionItem.Selection("Points", 1),
        SettingsSingleSelectionItem.Selection("Scaled Discs", 2)
    )),
    SettingsSingleSelectionItem("Info Display", "HudDetail", listOf(
        SettingsSingleSelectionItem.Selection("None", 0),
        SettingsSingleSelectionItem.Selection("Terse", 1),
        SettingsSingleSelectionItem.Selection("Verbose", 2)
    ))
)

class SettingsRenderInfoItem(): SettingsItem {
    override val name: String
        get() = "Render Info"
}

private val staticOtherItems: List<SettingsItem> = listOf(
    SettingsRenderInfoItem()
)

val mainSettingSections: List<CommonSectionV2> = listOf(
    CommonSectionV2(staticDisplayItems, "Display"),
    CommonSectionV2(staticTimeItems, "Time"),
    CommonSectionV2(staticAdvancedItems, "Advanced"),
    CommonSectionV2(staticOtherItems, "Other")
)

open class SettingsBaseFragment: TitledFragment() {
    open fun reload() {}
}