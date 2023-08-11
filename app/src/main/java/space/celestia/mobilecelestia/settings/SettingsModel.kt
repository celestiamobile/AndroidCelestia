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

import space.celestia.mobilecelestia.celestia.CelestiaInteraction
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
    ScriptSystemAccessPolicy("Script System Access Policy"),
    StarColors("Star Colors"),
    // Double values
    FaintestVisible("Faintest Stars"),
    AmbientLightLevel("Ambient Light"),
    GalaxyBrightness("Galaxy Brightness"),
    MinimumFeatureSize("Minimum Labeled Feature Size"),
    TintSaturation("Tinted Illumination Saturation");

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
                ScriptSystemAccessPolicy,
                StarColors,
            )

        val allDoubleCases: List<SettingsKey>
            get() = listOf(
                FaintestVisible,
                AmbientLightLevel,
                GalaxyBrightness,
                MinimumFeatureSize,
                TintSaturation,
            )
    }

    override val valueString: String
        get() = toString()
}

private val gameControllerRemapOptions = listOf(
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE, CelestiaString("None", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER, CelestiaString("Travel Faster", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER, CelestiaString("Travel Slower", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_STOP_SPEED, CelestiaString("Stop", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_REVERSE_SPEED, CelestiaString("Reverse Travel Direction", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_REVERSE_ORIENTATION, CelestiaString("Reverse Observer Orientation", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_TAP_CENTER, CelestiaString("Tap Center", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_GO_TO, CelestiaString("Go to Object", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ESC, CelestiaString("Esc", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP, CelestiaString("Pitch Up", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN, CelestiaString("Pitch Down", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_YAW_LEFT, CelestiaString("Yaw Left", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_YAW_RIGHT, CelestiaString("Yaw Right", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT, CelestiaString("Roll Left", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT, CelestiaString("Roll Right", "")),
)

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
    val defaultOn: Boolean = false,
    val subtitle: String? = null
) : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString(rawDisplayName, "")

    override val clickable: Boolean
        get() = false
}

class SettingsPreferenceSliderItem(
    val key: PreferenceManager.PredefinedKey,
    private val rawDisplayName: String,
    val subtitle: String? = null,
    val minValue: Double = 0.0,
    val maxValue: Double = 1.0,
    val defaultValue: Double = 0.0
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
        get() = false
}

class SettingsPreferenceSelectionItem(
    val key: PreferenceManager.PredefinedKey,
    private val displayName: String,
    val options: List<Pair<Int, String>>,
    val defaultSelection: Int
) : SettingsItem, Serializable {
    override val name: String
        get() = displayName

    override val clickable: Boolean
        get() = true
}

class SettingsSelectionSingleItem(
    val key: String,
    private val displayName: String,
    val options: List<Pair<Int, String>>,
    val defaultSelection: Int,
    val showTitle: Boolean = true,
    val subtitle: String? = null
) : SettingsItem, Serializable {
    override val name: String
        get() = displayName

    constructor(key: SettingsKey, displayName: String, options: List<Pair<Int, String>>, defaultSelection: Int, showTitle: Boolean = true, subtitle: String? = null) : this(key.valueString, displayName, options, defaultSelection, showTitle, subtitle)

    override val clickable: Boolean
        get() = false
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
            SettingsSelectionSingleItem(key = SettingsKey.TimeZone, options = listOf(
                Pair(0, CelestiaString("Local Time", "")),
                Pair(1, CelestiaString("UTC", "")),
            ), displayName = SettingsKey.TimeZone.displayName, defaultSelection = 0, showTitle = false)
        )
    ),
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.DateFormat.displayName, ""),
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.DateFormat, options = listOf(
                Pair(0, CelestiaString("Default", "")),
                Pair(1, CelestiaString("YYYY MMM DD HH:MM:SS TZ", "")),
                Pair(2, CelestiaString("UTC Offset", "")),
            ), displayName = SettingsKey.DateFormat.displayName, defaultSelection = 1, showTitle = false)
        )
    ),
    SettingsCurrentTimeItem(),
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.MeasurementSystem.displayName, ""),
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.MeasurementSystem, options = listOf(
                Pair(0, CelestiaString("Metric", "")),
                Pair(1, CelestiaString("Imperial", "")),
            ), displayName = SettingsKey.MeasurementSystem.displayName, defaultSelection = 0, showTitle = false),
            SettingsSelectionSingleItem(key = SettingsKey.TemperatureScale, options = listOf(
                Pair(0, CelestiaString("Kelvin", "")),
                Pair(1, CelestiaString("Celsius", "")),
                Pair(2, CelestiaString("Fahrenheit", "")),
            ), displayName = SettingsKey.TemperatureScale.displayName, defaultSelection = 0, showTitle = true)
        )
    ),
    SettingsCommonItem.create(
        CelestiaString(SettingsKey.HudDetail.displayName, ""),
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.HudDetail, options = listOf(
                Pair(0, CelestiaString("None", "")),
                Pair(1, CelestiaString("Terse", "")),
                Pair(2, CelestiaString("Verbose", "")),
            ), displayName = SettingsKey.HudDetail.displayName, defaultSelection = 1, showTitle = false),
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
            SettingsSelectionSingleItem(key = SettingsKey.Resolution, options = listOf(
                Pair(0, CelestiaString("Low", "")),
                Pair(1, CelestiaString("Medium", "")),
                Pair(2, CelestiaString("High", "")),
            ), displayName = SettingsKey.Resolution.displayName, defaultSelection = 1, showTitle = false)
        )
    ),
    SettingsCommonItem(
        CelestiaString(SettingsKey.StarStyle.displayName, ""),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.StarStyle, options = listOf(
                        Pair(0, CelestiaString("Fuzzy Points", "")),
                        Pair(1, CelestiaString("Points", "")),
                        Pair(2, CelestiaString("Scaled Discs", "")),
                    ), displayName = SettingsKey.StarStyle.displayName, defaultSelection = 0),
                    SettingsSelectionSingleItem(key = SettingsKey.StarColors, options = listOf(
                        Pair(0, CelestiaString("Classic Colors", "")),
                        Pair(1, CelestiaString("Blackbody D65", "")),
                        Pair(2, CelestiaString("Blackbody (Solar Whitepoint)", "")),
                        Pair(3, CelestiaString("Blackbody (Vega Whitepoint)", "")),
                    ), displayName = SettingsKey.StarColors.displayName, defaultSelection = 1),
                    SettingsSliderItem(SettingsKey.TintSaturation, 0.0, 1.0),
                ), footer = CelestiaString("Tinted illumination saturation setting is only effective with Blackbody star colors.", "")
            )
        )
    ),
    SettingsCommonItem(CelestiaString("Render Parameters", ""), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowSmoothLines, SettingsSwitchItem.Representation.Switch),
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
    SettingsCommonItem(
        CelestiaString("Interaction", ""),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsPreferenceSliderItem(PreferenceManager.PredefinedKey.PickSensitivity, rawDisplayName = "Sensitivity", subtitle = CelestiaString("Sensitivity for object selection", ""), minValue = 1.0, maxValue = 20.0, defaultValue = 10.0),
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ContextMenu, rawDisplayName = "Context Menu", true, subtitle = CelestiaString("Context menu by long press or context click", "")),
                ),
                footer = CelestiaString("Configuration will take effect after a restart.", "")
            )
        )
    ),
    SettingsCommonItem(
        CelestiaString("Game Controller", ""),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapA, displayName = CelestiaString("A / X", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapB, displayName = CelestiaString("B / Circle", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapX, displayName = CelestiaString("X / Square", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapY, displayName = CelestiaString("Y / Triangle", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadUp, displayName = CelestiaString("D-pad Up", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadDown, displayName = CelestiaString("D-pad Down", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadLeft, displayName = CelestiaString("D-pad Left", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadRight, displayName = CelestiaString("D-pad Right", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLB, displayName = CelestiaString("LB / L1", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLT, displayName = CelestiaString("LT / L2", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRB, displayName = CelestiaString("RB / R1", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRT, displayName = CelestiaString("RT / R2", ""), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT),
                ),
                header = CelestiaString("Buttons", ""),
            ),
            SettingsCommonItem.Section(
                listOf(
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerInvertX, rawDisplayName = "Invert Horizontally", false),
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerInvertY, rawDisplayName = "Invert Vertically", false),
                ),
                header = CelestiaString("Thumbsticks", ""),
            )
        )
    ),
    SettingsDataLocationItem(),
    SettingsCommonItem(
        CelestiaString("Security", ""),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.ScriptSystemAccessPolicy, options = listOf(
                        Pair(0, CelestiaString("Ask", "")),
                        Pair(1, CelestiaString("Allow", "")),
                        Pair(2, CelestiaString("Deny", "")),
                    ), displayName = SettingsKey.ScriptSystemAccessPolicy.displayName, defaultSelection = 0, showTitle = true, subtitle = CelestiaString("Lua scripts' access to the file system", ""))
                ),
            )
        )
    ),
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

interface SettingsBaseFragment {
    fun reload() {}
}
