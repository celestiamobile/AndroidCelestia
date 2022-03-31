/*
 * SettingsModel.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.io.Serializable

const val settingUnmarkAllID = "UnmarkAll"

enum class SettingsKey(private val rawDisplayName: String) : PreferenceManager.Key, Serializable {
    // Boolean values
    ShowStars("Stars"),
    ShowPlanets("Planets"),
    ShowDwarfPlanets("Dwarf Planets"),
    ShowMoons("Moons"),
    ShowMinorMoons("Minor Moons"),
    ShowAsteroids("Asteroids"),
    ShowComets("Comets"),
    ShowSpacecrafts("Spacecraft"),
    ShowGalaxies("Galaxies"),
    ShowNebulae("Nebulae"),
    ShowGlobulars("Globulars"),
    ShowOpenClusters("Open Clusters"),
    ShowAtmospheres("Atmospheres"),
    ShowCloudMaps("Clouds"),
    ShowCloudShadows("Cloud Shadows"),
    ShowNightMaps("Night Lights"),
    ShowPlanetRings("Planet Rings"),
    ShowRingShadows("Ring Shadows"),
    ShowCometTails("Comet Tails"),
    ShowEclipseShadows("Eclipse Shadows"),
    ShowOrbits("Show Orbits"),
    ShowFadingOrbits("Fading Orbits"),
    ShowPartialTrajectories("Partial Trajectories"),
    ShowStellarOrbits("Stars"),
    ShowPlanetOrbits("Planets"),
    ShowDwarfPlanetOrbits("Dwarf Planets"),
    ShowMoonOrbits("Moons"),
    ShowMinorMoonOrbits("Minor Moons"),
    ShowAsteroidOrbits("Asteroids"),
    ShowCometOrbits("Comets"),
    ShowSpacecraftOrbits("Spacecraft"),
    ShowCelestialSphere("Equatorial"),
    ShowEclipticGrid("Ecliptic"),
    ShowHorizonGrid("Horizontal"),
    ShowGalacticGrid("Galactic"),
    ShowDiagrams("Show Diagrams"),
    ShowConstellationLabels("Show Labels"),
    ShowLatinConstellationLabels("Show Labels in Latin"),
    ShowBoundaries("Show Boundaries"),
    ShowStarLabels("Stars"),
    ShowPlanetLabels("Planets"),
    ShowDwarfPlanetLabels("Dwarf Planets"),
    ShowMoonLabels("Moons"),
    ShowMinorMoonLabels("Minor Moons"),
    ShowAsteroidLabels("Asteroids"),
    ShowCometLabels("Comets"),
    ShowSpacecraftLabels("Spacecraft"),
    ShowGalaxyLabels("Galaxies"),
    ShowNebulaLabels("Nebulae"),
    ShowGlobularLabels("Globulars"),
    ShowOpenClusterLabels("Open Clusters"),
    ShowLocationLabels("Show Locations"),
    ShowCityLabels("Cities"),
    ShowObservatoryLabels("Observatories"),
    ShowLandingSiteLabels("Landing Sites"),
    ShowMonsLabels("Montes (Mountains)"),
    ShowMareLabels("Maria (Seas)"),
    ShowCraterLabels("Craters"),
    ShowVallisLabels("Valles (Valleys)"),
    ShowTerraLabels("Terrae (Land masses)"),
    ShowEruptiveCenterLabels("Volcanoes"),
    ShowOtherLabels("Other"),
    ShowMarkers("Show Markers"),
    ShowEcliptic("Ecliptic Line"),
    ShowTintedIllumination("Tinted Illumination"),
    ShowAutoMag("Auto Mag"),
    ShowSmoothLines("Smooth Lines"),
    // Int values
    TimeZone("Time Zone"),
    DateFormat("Date Format"),
    Resolution("Texture Resolution"),
    StarStyle("Star Style"),
    HudDetail("Info Display"),
    MeasurementSystem("Measure Units"),
    TemperatureScale("Temperature Scale"),
    // Double values
    FaintestVisible("Faintest Stars"),
    AmbientLightLevel("Ambient Light"),
    GalaxyBrightness("Galaxy Brightness"),
    MinimumFeatureSize("Minimum Labeled Feature Size");

    val displayName: String
        get() = CelestiaString(rawDisplayName, "")

    companion object {
        val allBooleanCases: List<SettingsKey>
            get() = listOf(
                // Boolean values
                ShowStars,
                ShowPlanets,
                ShowDwarfPlanets,
                ShowMoons,
                ShowMinorMoons,
                ShowAsteroids,
                ShowComets,
                ShowSpacecrafts,
                ShowGalaxies,
                ShowNebulae,
                ShowGlobulars,
                ShowOpenClusters,
                ShowAtmospheres,
                ShowCloudMaps,
                ShowCloudShadows,
                ShowNightMaps,
                ShowPlanetRings,
                ShowRingShadows,
                ShowCometTails,
                ShowEclipseShadows,
                ShowOrbits,
                ShowFadingOrbits,
                ShowPartialTrajectories,
                ShowStellarOrbits,
                ShowPlanetOrbits,
                ShowDwarfPlanetOrbits,
                ShowMoonOrbits,
                ShowMinorMoonOrbits,
                ShowAsteroidOrbits,
                ShowCometOrbits,
                ShowSpacecraftOrbits,
                ShowCelestialSphere,
                ShowEclipticGrid,
                ShowHorizonGrid,
                ShowGalacticGrid,
                ShowEcliptic,
                ShowTintedIllumination,
                ShowAutoMag,
                ShowSmoothLines,
                ShowDiagrams,
                ShowConstellationLabels,
                ShowLatinConstellationLabels,
                ShowBoundaries,
                ShowStarLabels,
                ShowPlanetLabels,
                ShowDwarfPlanetLabels,
                ShowMoonLabels,
                ShowMinorMoonLabels,
                ShowAsteroidLabels,
                ShowCometLabels,
                ShowSpacecraftLabels,
                ShowGalaxyLabels,
                ShowNebulaLabels,
                ShowGlobularLabels,
                ShowOpenClusterLabels,
                ShowLocationLabels,
                ShowCityLabels,
                ShowObservatoryLabels,
                ShowLandingSiteLabels,
                ShowMonsLabels,
                ShowMareLabels,
                ShowCraterLabels,
                ShowVallisLabels,
                ShowTerraLabels,
                ShowEruptiveCenterLabels,
                ShowOtherLabels,
                ShowMarkers,
            )

        val allIntCases: List<SettingsKey>
            get() = listOf(
                TimeZone,
                DateFormat,
                Resolution,
                StarStyle,
                HudDetail,
                MeasurementSystem,
                TemperatureScale,
            )

        val allDoubleCases: List<SettingsKey>
            get() = listOf(
                FaintestVisible,
                AmbientLightLevel,
                GalaxyBrightness,
                MinimumFeatureSize
            )
    }

    override val valueString: String
        get() = toString()
}

interface SettingsItem : RecyclerViewItem {
    val name: String
}

class SettingsSliderItem(
    private val internalKey: SettingsKey,
    val minValue: Double = 0.0,
    val maxValue: Double = 1.0
) : SettingsItem, Serializable {
    val key: String = internalKey.valueString

    override val name: String
        get() = CelestiaString(internalKey.displayName, "")

    override val clickable: Boolean
        get() = false
}

class SettingsPreferenceSwitchItem(
    val key: PreferenceManager.PredefinedKey,
    private val rawDisplayName: String,
    val defaultOn: Boolean = false
) : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString(rawDisplayName, "")

    override val clickable: Boolean
        get() = false
}

interface SettingsDynamicListItem: SettingsItem {
    fun createItems(): List<SettingsItem>
}

class SettingsLanguageItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Language", "")
}

class SettingsSwitchItem(
    val key: String,
    private val displayName: String,
    val volatile: Boolean,
    val representation: Representation = Representation.Checkmark
) : SettingsItem, Serializable {
    enum class Representation {
        Checkmark, Switch;
    }

    override val name: String
        get() = displayName

    constructor(key: SettingsKey, representation: Representation = Representation.Checkmark) : this(key.valueString, key.displayName, false, representation)

    override val clickable: Boolean
        get() = representation == Representation.Checkmark
}

class SettingsKeyedSelectionItem(
    val key: String,
    private val displayName: String,
    val index: Int
) : SettingsItem, Serializable {
    override val name: String
        get() = displayName

    constructor(key: SettingsKey, displayName: String, index: Int) : this(key.valueString, displayName, index)

    override val clickable: Boolean
        get() = true
}

private val staticDisplayItems: List<SettingsItem> = listOf(
    SettingsCommonItem.create(CelestiaString("Objects", ""), listOf(
        SettingsSwitchItem(SettingsKey.ShowStars),
        SettingsSwitchItem(SettingsKey.ShowPlanets),
        SettingsSwitchItem(SettingsKey.ShowDwarfPlanets),
        SettingsSwitchItem(SettingsKey.ShowMoons),
        SettingsSwitchItem(SettingsKey.ShowMinorMoons),
        SettingsSwitchItem(SettingsKey.ShowAsteroids),
        SettingsSwitchItem(SettingsKey.ShowComets),
        SettingsSwitchItem(SettingsKey.ShowSpacecrafts),
        SettingsSwitchItem(SettingsKey.ShowGalaxies),
        SettingsSwitchItem(SettingsKey.ShowNebulae),
        SettingsSwitchItem(SettingsKey.ShowGlobulars),
        SettingsSwitchItem(SettingsKey.ShowOpenClusters)
    )),
    SettingsCommonItem.create(CelestiaString("Features", ""), listOf(
        SettingsSwitchItem(SettingsKey.ShowAtmospheres),
        SettingsSwitchItem(SettingsKey.ShowCloudMaps),
        SettingsSwitchItem(SettingsKey.ShowCloudShadows),
        SettingsSwitchItem(SettingsKey.ShowNightMaps),
        SettingsSwitchItem(SettingsKey.ShowPlanetRings),
        SettingsSwitchItem(SettingsKey.ShowRingShadows),
        SettingsSwitchItem(SettingsKey.ShowCometTails),
        SettingsSwitchItem(SettingsKey.ShowEclipseShadows)
    )),
    SettingsCommonItem(CelestiaString("Orbits", ""), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowOrbits, SettingsSwitchItem.Representation.Switch),
            SettingsSwitchItem(SettingsKey.ShowFadingOrbits, SettingsSwitchItem.Representation.Switch),
            SettingsSwitchItem(SettingsKey.ShowPartialTrajectories, SettingsSwitchItem.Representation.Switch),
        )),
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowStellarOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowPlanetOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowDwarfPlanetOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowMoonOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowMinorMoonOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowAsteroidOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowCometOrbits, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowSpacecraftOrbits, SettingsSwitchItem.Representation.Checkmark)
        )),
    )),
    SettingsCommonItem(CelestiaString("Grids", ""), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowCelestialSphere, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowEclipticGrid, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowHorizonGrid, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowGalacticGrid, SettingsSwitchItem.Representation.Checkmark),
        )),
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowEcliptic, SettingsSwitchItem.Representation.Checkmark),
        )),
    )),
    SettingsCommonItem.create(CelestiaString("Constellations", ""), listOf(
        SettingsSwitchItem(SettingsKey.ShowDiagrams, SettingsSwitchItem.Representation.Checkmark),
        SettingsSwitchItem(SettingsKey.ShowConstellationLabels, SettingsSwitchItem.Representation.Checkmark),
        SettingsSwitchItem(SettingsKey.ShowLatinConstellationLabels, SettingsSwitchItem.Representation.Checkmark),
        SettingsSwitchItem(SettingsKey.ShowBoundaries, SettingsSwitchItem.Representation.Checkmark),
    )),
    SettingsCommonItem.create(CelestiaString("Object Labels", ""), listOf(
        SettingsSwitchItem(SettingsKey.ShowStarLabels),
        SettingsSwitchItem(SettingsKey.ShowPlanetLabels),
        SettingsSwitchItem(SettingsKey.ShowDwarfPlanetLabels),
        SettingsSwitchItem(SettingsKey.ShowMoonLabels),
        SettingsSwitchItem(SettingsKey.ShowMinorMoonLabels),
        SettingsSwitchItem(SettingsKey.ShowAsteroidLabels),
        SettingsSwitchItem(SettingsKey.ShowCometLabels),
        SettingsSwitchItem(SettingsKey.ShowSpacecraftLabels),
        SettingsSwitchItem(SettingsKey.ShowGalaxyLabels),
        SettingsSwitchItem(SettingsKey.ShowNebulaLabels),
        SettingsSwitchItem(SettingsKey.ShowGlobularLabels),
        SettingsSwitchItem(SettingsKey.ShowOpenClusterLabels)
    )),
    SettingsCommonItem(CelestiaString("Locations", ""), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowLocationLabels, SettingsSwitchItem.Representation.Switch),
            SettingsSliderItem(SettingsKey.MinimumFeatureSize, 0.0, 99.0),
        )),
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowCityLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowObservatoryLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowLandingSiteLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowMonsLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowMareLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowCraterLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowVallisLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowTerraLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowEruptiveCenterLabels, SettingsSwitchItem.Representation.Checkmark),
            SettingsSwitchItem(SettingsKey.ShowOtherLabels, SettingsSwitchItem.Representation.Checkmark),
        )),
    )),
    SettingsCommonItem.create(CelestiaString("Markers", ""), listOf(
        SettingsSwitchItem(SettingsKey.ShowMarkers, SettingsSwitchItem.Representation.Switch),
        SettingsUnknownTextItem(CelestiaString("Unmark All", ""), settingUnmarkAllID)
    )),
    SettingsCommonItem(
        CelestiaString("Reference Vectors", ""),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSwitchItem("ShowBodyAxes", CelestiaString("Show Body Axes", ""), true),
                    SettingsSwitchItem("ShowFrameAxes", CelestiaString("Show Frame Axes", ""), true),
                    SettingsSwitchItem("ShowSunDirection", CelestiaString("Show Sun Direction", ""), true),
                    SettingsSwitchItem("ShowVelocityVector", CelestiaString("Show Velocity Vector", ""), true),
                    SettingsSwitchItem("ShowPlanetographicGrid", CelestiaString("Show Planetographic Grid", ""), true),
                    SettingsSwitchItem("ShowTerminator", CelestiaString("Show Terminator", ""), true)
                ),
                footer = CelestiaString("Reference vectors are only visible for the current selected solar system object.", "")
            )
        )
    )
)

class SettingsCurrentTimeItem : SettingsItem {
    override val name: String
        get() = CelestiaString("Current Time", "")
}

private val staticTimeAndRegionItems: List<SettingsItem> = listOf(
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.TimeZone.displayName, ""),
        listOf(
            SettingsKeyedSelectionItem(SettingsKey.TimeZone, CelestiaString("Local Time", ""), 0),
            SettingsKeyedSelectionItem(SettingsKey.TimeZone, CelestiaString("UTC", ""), 1)
        )
    ),
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.DateFormat.displayName, ""),
        listOf(
            SettingsKeyedSelectionItem(SettingsKey.DateFormat, CelestiaString("Default", ""), 0),
            SettingsKeyedSelectionItem(SettingsKey.DateFormat, CelestiaString("YYYY MMM DD HH:MM:SS TZ", ""), 1),
            SettingsKeyedSelectionItem(SettingsKey.DateFormat, CelestiaString("UTC Offset", ""), 2)
        )
    ),
    SettingsCurrentTimeItem(),
    SettingsCommonItem(
        CelestiaString(SettingsKey.MeasurementSystem.displayName, ""),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsKeyedSelectionItem(SettingsKey.MeasurementSystem, CelestiaString("Metric", ""), 0),
                    SettingsKeyedSelectionItem(SettingsKey.MeasurementSystem, CelestiaString("Imperial", ""), 1)
                )
            ),
            SettingsCommonItem.Section(
                listOf(
                    SettingsKeyedSelectionItem(SettingsKey.TemperatureScale, CelestiaString("Kelvin", ""), 0),
                    SettingsKeyedSelectionItem(SettingsKey.TemperatureScale, CelestiaString("Celsius", ""), 1),
                    SettingsKeyedSelectionItem(SettingsKey.TemperatureScale, CelestiaString("Fahrenheit", ""), 2)
                ),
                header = CelestiaString(SettingsKey.TemperatureScale.displayName, ""),
            )
        )
    ),
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.HudDetail.displayName, ""),
        listOf(
            SettingsKeyedSelectionItem(SettingsKey.HudDetail, CelestiaString("None", ""), 0),
            SettingsKeyedSelectionItem(SettingsKey.HudDetail, CelestiaString("Terse", ""), 1),
            SettingsKeyedSelectionItem(SettingsKey.HudDetail, CelestiaString("Verbose", ""), 2)
        )
    ),
    SettingsLanguageItem(),
)

class SettingsDataLocationItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Data Location", "")
}

class SettingsRefreshRateItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Frame Rate", "")
}

private val staticRendererItems: List<SettingsItem> = listOf(
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.Resolution.displayName, ""),
        listOf(
            SettingsKeyedSelectionItem(SettingsKey.Resolution, CelestiaString("Low", ""), 0),
            SettingsKeyedSelectionItem(SettingsKey.Resolution, CelestiaString("Medium", ""), 1),
            SettingsKeyedSelectionItem(SettingsKey.Resolution, CelestiaString("High", ""), 2)
        )
    ),
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.StarStyle.displayName, ""),
        listOf(
            SettingsKeyedSelectionItem(SettingsKey.StarStyle, CelestiaString("Fuzzy Points", ""), 0),
            SettingsKeyedSelectionItem(SettingsKey.StarStyle, CelestiaString("Points", ""), 1),
            SettingsKeyedSelectionItem(SettingsKey.StarStyle, CelestiaString("Scaled Discs", ""), 2)
        )
    ),
    SettingsCommonItem(CelestiaString("Render Parameters", ""), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowSmoothLines, SettingsSwitchItem.Representation.Switch),
            SettingsSwitchItem(SettingsKey.ShowTintedIllumination, SettingsSwitchItem.Representation.Switch),
        )),
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowAutoMag, SettingsSwitchItem.Representation.Switch),
            SettingsSliderItem(SettingsKey.AmbientLightLevel, 0.0, 1.0),
            SettingsSliderItem(SettingsKey.FaintestVisible, 3.0, 12.0),
            SettingsSliderItem(SettingsKey.GalaxyBrightness, 0.0, 1.0)
        )),
    )),
    SettingsRefreshRateItem(),
    SettingsCommonItem(CelestiaString("Advanced", ""), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.FullDPI, "HiDPI", true),
            SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.MSAA, "Anti-aliasing")
        ),  footer =  CelestiaString("Configuration will take effect after a restart.", ""))
    )),
    SettingsRenderInfoItem()
)

private val staticAdvancedItems: List<SettingsItem> = listOf(
    SettingsDataLocationItem()
)

class SettingsRenderInfoItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Render Info", "")
}

class SettingsAboutItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("About", "")
}

class SettingsUnknownTextItem(override val name: String, val id: String) : SettingsItem, Serializable

class SettingsActionItem(override val name: String, val action: Int): SettingsItem, Serializable

private val staticOtherItems: List<SettingsItem> = listOf(
    SettingsCommonItem.create(
        CelestiaString("Debug", ""),
        listOf(
            SettingsActionItem(CelestiaString("Toggle FPS Display", ""), 0x60),
            SettingsActionItem(CelestiaString("Toggle Console Display", ""), 0x7E)
        )
    ),
    SettingsAboutItem()
)

val mainSettingSections: List<CommonSectionV2> = listOf(
    CommonSectionV2(staticDisplayItems, CelestiaString("Display", "")),
    CommonSectionV2(staticTimeAndRegionItems, CelestiaString("Time & Region", "")),
    CommonSectionV2(staticRendererItems, CelestiaString("Renderer", "")),
    CommonSectionV2(staticAdvancedItems, CelestiaString("Advanced", "")),
    CommonSectionV2(staticOtherItems, "")
)

class SettingsCommonItem(override val name: String, val sections: List<Section>) : SettingsItem, Serializable {
    class Section(val rows: List<SettingsItem>, val header: String? = "", val footer: String? = null) : Serializable

    companion object {
        fun create(name: String, items: List<SettingsItem>): SettingsCommonItem {
            return SettingsCommonItem(name, listOf(Section(items)))
        }
    }
}

open class SettingsBaseFragment: NavigationFragment.SubFragment() {
    open fun reload() {}
}
