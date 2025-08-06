// SettingsModel.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Build
import androidx.annotation.RequiresApi
import space.celestia.mobilecelestia.celestia.CelestiaInteraction
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.io.Serializable

const val settingUnmarkAllID = "UnmarkAll"

enum class SettingsKey : PreferenceManager.Key, Serializable {
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
    ShowEcliptic,
    ShowAutoMag,
    ShowSmoothLines,
    EnableReverseWheel,
    EnableRayBasedDragging,
    EnableFocusZooming,
    EnableAlignCameraToSurfaceOnLand,
    // Int values
    TimeZone,
    DateFormat,
    Resolution,
    StarStyle,
    HudDetail,
    MeasurementSystem,
    TemperatureScale,
    ScriptSystemAccessPolicy,
    StarColors,
    // Double values
    FaintestVisible,
    AmbientLightLevel,
    GalaxyBrightness,
    MinimumFeatureSize,
    TintSaturation;

    val displayName: String
        get() = when(this) {
            ShowStars -> CelestiaString("Stars", "Tab for stars in Star Browser")
            ShowPlanets -> CelestiaString("Planets", "")
            ShowDwarfPlanets -> CelestiaString("Dwarf Planets", "")
            ShowMoons -> CelestiaString("Moons", "")
            ShowMinorMoons -> CelestiaString("Minor Moons", "")
            ShowAsteroids -> CelestiaString("Asteroids", "")
            ShowComets -> CelestiaString("Comets", "")
            ShowSpacecrafts -> CelestiaString("Spacecraft", "Plural")
            ShowGalaxies -> CelestiaString("Galaxies", "")
            ShowNebulae -> CelestiaString("Nebulae", "")
            ShowGlobulars -> CelestiaString("Globulars", "")
            ShowOpenClusters -> CelestiaString("Open Clusters", "")
            ShowAtmospheres -> CelestiaString("Atmospheres", "")
            ShowCloudMaps -> CelestiaString("Clouds", "")
            ShowCloudShadows -> CelestiaString("Cloud Shadows", "")
            ShowNightMaps -> CelestiaString("Night Lights", "")
            ShowPlanetRings -> CelestiaString("Planet Rings", "")
            ShowRingShadows -> CelestiaString("Ring Shadows", "")
            ShowCometTails -> CelestiaString("Comet Tails", "")
            ShowEclipseShadows -> CelestiaString("Eclipse Shadows", "")
            ShowOrbits -> CelestiaString("Show Orbits", "")
            ShowFadingOrbits -> CelestiaString("Fading Orbits", "")
            ShowPartialTrajectories -> CelestiaString("Partial Trajectories", "")
            ShowStellarOrbits -> CelestiaString("Stars", "Tab for stars in Star Browser")
            ShowPlanetOrbits -> CelestiaString("Planets", "")
            ShowDwarfPlanetOrbits -> CelestiaString("Dwarf Planets", "")
            ShowMoonOrbits -> CelestiaString("Moons", "")
            ShowMinorMoonOrbits -> CelestiaString("Minor Moons", "")
            ShowAsteroidOrbits -> CelestiaString("Asteroids", "")
            ShowCometOrbits -> CelestiaString("Comets", "")
            ShowSpacecraftOrbits -> CelestiaString("Spacecraft", "Plural")
            ShowCelestialSphere -> CelestiaString("Equatorial", "Grids")
            ShowEclipticGrid -> CelestiaString("Ecliptic", "Grids")
            ShowHorizonGrid -> CelestiaString("Horizontal", "Grids")
            ShowGalacticGrid -> CelestiaString("Galactic", "Grids")
            ShowDiagrams -> CelestiaString("Show Diagrams", "Show constellation diagrams")
            ShowConstellationLabels -> CelestiaString("Show Labels", "Constellation labels")
            ShowLatinConstellationLabels -> CelestiaString("Show Labels in Latin", "Constellation labels in Latin")
            ShowBoundaries -> CelestiaString("Show Boundaries", "Show constellation boundaries")
            ShowStarLabels -> CelestiaString("Stars", "Tab for stars in Star Browser")
            ShowPlanetLabels -> CelestiaString("Planets", "")
            ShowDwarfPlanetLabels -> CelestiaString("Dwarf Planets", "")
            ShowMoonLabels -> CelestiaString("Moons", "")
            ShowMinorMoonLabels -> CelestiaString("Minor Moons", "")
            ShowAsteroidLabels -> CelestiaString("Asteroids", "")
            ShowCometLabels -> CelestiaString("Comets", "")
            ShowSpacecraftLabels -> CelestiaString("Spacecraft", "Plural")
            ShowGalaxyLabels -> CelestiaString("Galaxies", "")
            ShowNebulaLabels -> CelestiaString("Nebulae", "")
            ShowGlobularLabels -> CelestiaString("Globulars", "")
            ShowOpenClusterLabels -> CelestiaString("Open Clusters", "")
            ShowLocationLabels -> CelestiaString("Show Locations", "")
            ShowCityLabels -> CelestiaString("Cities", "")
            ShowObservatoryLabels -> CelestiaString("Observatories", "Location labels")
            ShowLandingSiteLabels -> CelestiaString("Landing Sites", "Location labels")
            ShowMonsLabels -> CelestiaString("Montes (Mountains)", "Location labels")
            ShowMareLabels -> CelestiaString("Maria (Seas)", "Location labels")
            ShowCraterLabels -> CelestiaString("Craters", "Location labels")
            ShowVallisLabels -> CelestiaString("Valles (Valleys)", "Location labels")
            ShowTerraLabels -> CelestiaString("Terrae (Land masses)", "Location labels")
            ShowEruptiveCenterLabels -> CelestiaString("Volcanoes", "Location labels")
            ShowOtherLabels -> CelestiaString("Other", "Other location labels; Android/iOS, Other objects to choose from in Eclipse Finder")
            ShowMarkers -> CelestiaString("Show Markers", "")
            ShowEcliptic -> CelestiaString("Ecliptic Line", "")
            ShowAutoMag -> CelestiaString("Auto Mag", "Auto mag for star display")
            ShowSmoothLines -> CelestiaString("Smooth Lines", "Smooth lines for rendering")
            EnableReverseWheel -> CelestiaString("Reverse Mouse Wheel", "")
            EnableRayBasedDragging -> CelestiaString("Ray-Based Dragging", "")
            EnableFocusZooming -> CelestiaString("Focus Zooming", "")
            EnableAlignCameraToSurfaceOnLand -> CelestiaString("Align to Surface on Landing", "Option to align camera to surface when landing")
            TimeZone -> CelestiaString("Time Zone", "")
            DateFormat -> CelestiaString("Date Format", "")
            Resolution -> CelestiaString("Texture Resolution", "")
            StarStyle -> CelestiaString("Star Style", "")
            HudDetail -> CelestiaString("Info Display", "HUD display")
            MeasurementSystem -> CelestiaString("Measure Units", "Measurement system")
            TemperatureScale -> CelestiaString("Temperature Scale", "")
            ScriptSystemAccessPolicy -> CelestiaString("Script System Access Policy", "Policy for managing lua script's access to the system")
            StarColors -> CelestiaString("Star Colors", "")
            FaintestVisible -> CelestiaString("Faintest Stars", "Control the faintest star that Celestia should display")
            AmbientLightLevel -> CelestiaString("Ambient Light", "In setting")
            GalaxyBrightness -> CelestiaString("Galaxy Brightness", "Render parameter")
            MinimumFeatureSize -> CelestiaString("Minimum Labeled Feature Size", "Minimum feature size that we should display a label for")
            TintSaturation -> CelestiaString("Tinted Illumination Saturation", "")
        }

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
                EnableReverseWheel,
                EnableRayBasedDragging,
                EnableFocusZooming,
                EnableAlignCameraToSurfaceOnLand,
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
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE, CelestiaString("None", "Empty HUD display")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER, CelestiaString("Travel Faster", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER, CelestiaString("Travel Slower", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_STOP_SPEED, CelestiaString("Stop", "Interupt the process of finding eclipse/Set traveling speed to 0")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_REVERSE_SPEED, CelestiaString("Reverse Travel Direction", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_REVERSE_ORIENTATION, CelestiaString("Reverse Observer Orientation", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_TAP_CENTER, CelestiaString("Tap Center", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_GO_TO, CelestiaString("Go to Object", "")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ESC, CelestiaString("Esc", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP, CelestiaString("Pitch Up", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN, CelestiaString("Pitch Down", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_YAW_LEFT, CelestiaString("Yaw Left", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_YAW_RIGHT, CelestiaString("Yaw Right", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT, CelestiaString("Roll Left", "Game controller action")),
    Pair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT, CelestiaString("Roll Right", "Game controller action")),
)

interface SettingsItem {
    val name: String
}

class SettingsSliderItem(
    private val internalKey: SettingsKey,
    val minValue: Double = 0.0,
    val maxValue: Double = 1.0
) : SettingsItem, Serializable {
    val key: String = internalKey.valueString

    override val name: String
        get() = internalKey.displayName
}

class SettingsPreferenceSwitchItem(
    val key: PreferenceManager.PredefinedKey,
    private val displayName: String,
    val defaultOn: Boolean = false,
    val subtitle: String? = null
) : SettingsItem, Serializable {
    override val name: String
        get() = displayName
}

class SettingsPreferenceSliderItem(
    val key: PreferenceManager.PredefinedKey,
    private val displayName: String,
    val subtitle: String? = null,
    val minValue: Double = 0.0,
    val maxValue: Double = 1.0,
    val defaultValue: Double = 0.0
) : SettingsItem, Serializable {
    override val name: String
        get() = displayName
}

class SettingsLanguageItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Language", "Display language setting")
}

class SettingsSwitchItem(
    val key: String,
    private val displayName: String,
    val volatile: Boolean,
    val representation: Representation = Representation.Checkmark,
    val subtitle: String? = null
) : SettingsItem, Serializable {
    enum class Representation {
        Checkmark, Switch;
    }

    override val name: String
        get() = displayName

    constructor(key: SettingsKey, representation: Representation = Representation.Checkmark, subtitle: String? = null) : this(key.valueString, key.displayName, false, representation, subtitle)
}

class SettingsPreferenceSelectionItem(
    val key: PreferenceManager.PredefinedKey,
    private val displayName: String,
    val options: List<Pair<Int, String>>,
    val defaultSelection: Int
) : SettingsItem, Serializable {
    override val name: String
        get() = displayName
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
    SettingsCommonItem.create(CelestiaString("Object Labels", "Labels"), listOf(
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
    SettingsCommonItem(CelestiaString("Locations", "Location labels to display"), listOf(
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
        SettingsUnknownTextItem(CelestiaString("Unmark All", "Unmark all objects"), settingUnmarkAllID)
    )),
    SettingsCommonItem(
        CelestiaString("Reference Vectors", "Reference vectors for an object"),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSwitchItem("ShowBodyAxes", CelestiaString("Show Body Axes", "Reference vector"), true),
                    SettingsSwitchItem("ShowFrameAxes", CelestiaString("Show Frame Axes", "Reference vector"), true),
                    SettingsSwitchItem("ShowSunDirection", CelestiaString("Show Sun Direction", "Reference vector"), true),
                    SettingsSwitchItem("ShowVelocityVector", CelestiaString("Show Velocity Vector", "Reference vector"), true),
                    SettingsSwitchItem("ShowPlanetographicGrid", CelestiaString("Show Planetographic Grid", "Reference vector"), true),
                    SettingsSwitchItem("ShowTerminator", CelestiaString("Show Terminator", "Reference vector"), true)
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
        SettingsKey.TimeZone.displayName,
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.TimeZone, options = listOf(
                Pair(0, CelestiaString("Local Time", "")),
                Pair(1, CelestiaString("UTC", "")),
            ), displayName = SettingsKey.TimeZone.displayName, defaultSelection = 0, showTitle = false)
        )
    ),
    SettingsCommonItem.create(
        SettingsKey.DateFormat.displayName,
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
        SettingsKey.MeasurementSystem.displayName,
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.MeasurementSystem, options = listOf(
                Pair(0, CelestiaString("Metric", "Metric measurement system")),
                Pair(1, CelestiaString("Imperial", "Imperial measurement system")),
            ), displayName = SettingsKey.MeasurementSystem.displayName, defaultSelection = 0, showTitle = false),
            SettingsSelectionSingleItem(key = SettingsKey.TemperatureScale, options = listOf(
                Pair(0, CelestiaString("Kelvin", "Temperature scale")),
                Pair(1, CelestiaString("Celsius", "Temperature scale")),
                Pair(2, CelestiaString("Fahrenheit", "Temperature scale")),
            ), displayName = SettingsKey.TemperatureScale.displayName, defaultSelection = 0, showTitle = true)
        )
    ),
    SettingsCommonItem.create(
        SettingsKey.HudDetail.displayName,
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.HudDetail, options = listOf(
                Pair(0, CelestiaString("None", "Empty HUD display")),
                Pair(1, CelestiaString("Terse", "Terse HUD display")),
                Pair(2, CelestiaString("Verbose", "Verbose HUD display")),
            ), displayName = SettingsKey.HudDetail.displayName, defaultSelection = 1, showTitle = false),
        )
    ),
    SettingsLanguageItem(),
)

class SettingsDataLocationItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Data Location", "Title for celestia.cfg, data location setting")
}

@RequiresApi(Build.VERSION_CODES.Q)
class SettingsFontItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Font", "")
}

class SettingsToolbarItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Toolbar", "Toolbar customization entry in Settings")
}


class SettingsRefreshRateItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Frame Rate", "Frame rate of simulation")
}

private val staticRendererItems: List<SettingsItem> = listOf(
    SettingsCommonItem.create(
        SettingsKey.Resolution.displayName,
        listOf(
            SettingsSelectionSingleItem(key = SettingsKey.Resolution, options = listOf(
                Pair(0, CelestiaString("Low", "Low resolution")),
                Pair(1, CelestiaString("Medium", "Medium resolution")),
                Pair(2, CelestiaString("High", "High resolution")),
            ), displayName = SettingsKey.Resolution.displayName, defaultSelection = 1, showTitle = false)
        )
    ),
    SettingsCommonItem(
        SettingsKey.StarStyle.displayName,
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.StarStyle, options = listOf(
                        Pair(0, CelestiaString("Fuzzy Points", "Star style")),
                        Pair(1, CelestiaString("Points", "Star style")),
                        Pair(2, CelestiaString("Scaled Discs", "Star style")),
                    ), displayName = SettingsKey.StarStyle.displayName, defaultSelection = 0),
                    SettingsSelectionSingleItem(key = SettingsKey.StarColors, options = listOf(
                        Pair(0, CelestiaString("Classic Colors", "Star colors option")),
                        Pair(1, CelestiaString("Blackbody D65", "Star colors option")),
                        Pair(2, CelestiaString("Blackbody (Solar Whitepoint)", "Star colors option")),
                        Pair(3, CelestiaString("Blackbody (Vega Whitepoint)", "Star colors option")),
                    ), displayName = SettingsKey.StarColors.displayName, defaultSelection = 1),
                    SettingsSliderItem(SettingsKey.TintSaturation, 0.0, 1.0),
                ), footer = CelestiaString("Tinted illumination saturation setting is only effective with Blackbody star colors.", "")
            )
        )
    ),
    SettingsCommonItem(CelestiaString("Render Parameters", "Render parameters in setting"), listOf(
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
    SettingsCommonItem(CelestiaString("Advanced", "Advanced setting items"), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.FullDPI, CelestiaString("HiDPI", "HiDPI support in display"), true),
            SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.MSAA, CelestiaString("Anti-aliasing", ""))
        ),  footer =  CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
    )),
    SettingsRenderInfoItem()
)

private val staticAdvancedItems: List<SettingsItem> = listOf(
    SettingsCommonItem(
        CelestiaString("Interaction", "Settings for interaction"),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSwitchItem(SettingsKey.EnableReverseWheel, representation = SettingsSwitchItem.Representation.Switch),
                    SettingsSwitchItem(SettingsKey.EnableRayBasedDragging, subtitle = CelestiaString("Dragging behavior based on change of pick rays instead of screen coordinates", ""), representation = SettingsSwitchItem.Representation.Switch),
                    SettingsSwitchItem(SettingsKey.EnableFocusZooming, subtitle = CelestiaString("Zooming behavior keeping the original focus location on screen", ""), representation = SettingsSwitchItem.Representation.Switch),
                    SettingsPreferenceSliderItem(PreferenceManager.PredefinedKey.PickSensitivity, displayName = CelestiaString("Sensitivity", "Setting for sensitivity for selecting an object"), subtitle = CelestiaString("Sensitivity for object selection", "Notes for the sensitivity setting"), minValue = 1.0, maxValue = 20.0, defaultValue = 10.0),
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ContextMenu, displayName = CelestiaString("Context Menu", "Settings for whether context menu is enabled"), true, subtitle = CelestiaString("Context menu by long press or context click", "Description for how a context menu is triggered")),
                ),
                footer = CelestiaString("Some configurations will take effect after a restart.", "")
            )
        )
    ),
    SettingsCommonItem(
        CelestiaString("Camera", "Settings for camera control"),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSwitchItem(SettingsKey.EnableAlignCameraToSurfaceOnLand, representation = SettingsSwitchItem.Representation.Switch),
                ),
            )
        )
    ),
    SettingsCommonItem(
        CelestiaString("Game Controller", "Settings for game controller"),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapA, displayName = CelestiaString("A / X", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapB, displayName = CelestiaString("B / Circle", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapX, displayName = CelestiaString("X / Square", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapY, displayName = CelestiaString("Y / Triangle", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadUp, displayName = CelestiaString("D-pad Up", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadDown, displayName = CelestiaString("D-pad Down", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadLeft, displayName = CelestiaString("D-pad Left", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadRight, displayName = CelestiaString("D-pad Right", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLB, displayName = CelestiaString("LB / L1", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLT, displayName = CelestiaString("LT / L2", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRB, displayName = CelestiaString("RB / R1", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRT, displayName = CelestiaString("RT / R2", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT),
                ),
                header = CelestiaString("Buttons", "Settings for game controller buttons"),
            ),
            SettingsCommonItem.Section(
                listOf(
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerEnableLeftThumbstick, displayName = CelestiaString("Enable Left Thumbstick", "Setting item to control whether left thumbstick should be enabled"), true),
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerEnableRightThumbstick, displayName = CelestiaString("Enable Right Thumbstick", "Setting item to control whether right thumbstick should be enabled"), true),
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerInvertX, displayName = CelestiaString("Invert Horizontally", "Invert game controller thumbstick axis horizontally"), false),
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerInvertY, displayName = CelestiaString("Invert Vertically", "Invert game controller thumbstick axis vertically"), false),
                ),
                header = CelestiaString("Thumbsticks", "Settings for game controller thumbsticks"),
            )
        )
    ),
    SettingsDataLocationItem(),
    SettingsCommonItem(
        CelestiaString("Security", "Security settings title"),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.ScriptSystemAccessPolicy, options = listOf(
                        Pair(0, CelestiaString("Ask", "Script system access policy option")),
                        Pair(1, CelestiaString("Allow", "Script system access policy option")),
                        Pair(2, CelestiaString("Deny", "Script system access policy option")),
                    ), displayName = SettingsKey.ScriptSystemAccessPolicy.displayName, defaultSelection = 0, showTitle = true, subtitle = CelestiaString("Lua scripts' access to the file system", "Note for Script System Access Policy"))
                ),
            )
        )
    ),
)

class SettingsRenderInfoItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Render Info", "Information about renderer")
}

class SettingsAboutItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("About", "About Celstia...")
}

class SettingsUnknownTextItem(override val name: String, val id: String) : SettingsItem, Serializable

class SettingsActionItem(override val name: String, val action: Int): SettingsItem, Serializable

private val staticOtherItems: List<SettingsItem> = listOf(
    SettingsCommonItem.create(
        CelestiaString("Debug", "Debug menu"),
        listOf(
            SettingsActionItem(CelestiaString("Toggle FPS Display", "Toggle FPS display on overlay"), 0x60),
            SettingsActionItem(CelestiaString("Toggle Console Display", "Toggle console log display on overlay"), 0x7E)
        )
    ),
    SettingsAboutItem()
)

val celestiaPlusSettingSection: List<CommonSectionV2<SettingsItem>> by lazy {
    val items = arrayListOf<SettingsItem>()
    items.add(SettingsToolbarItem())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        items.add(SettingsFontItem())
    }
    if (items.isEmpty()) {
        listOf()
    } else {
        listOf(CommonSectionV2(items, CelestiaString("Celestia PLUS", "Name for the subscription service")))
    }
}

val mainSettingSectionsBeforePlus: List<CommonSectionV2<SettingsItem>> = listOf(
    CommonSectionV2(staticDisplayItems, CelestiaString("Display", "Display settings")),
    CommonSectionV2(staticTimeAndRegionItems, CelestiaString("Time & Region", "time and region related settings")),
    CommonSectionV2(staticRendererItems, CelestiaString("Renderer", "In settings")),
    CommonSectionV2(staticAdvancedItems, CelestiaString("Advanced", "Advanced setting items")),
)

val mainSettingSectionsAfterPlus: List<CommonSectionV2<SettingsItem>> = listOf(
    CommonSectionV2(staticOtherItems, ""),
)

class SettingsCommonItem(override val name: String, val sections: List<Section>) : SettingsItem, Serializable {
    class Section(val rows: List<SettingsItem>, val header: String? = "", val footer: String? = null) : Serializable

    companion object {
        fun create(name: String, items: List<SettingsItem>): SettingsCommonItem {
            return SettingsCommonItem(name, listOf(Section(items)))
        }
    }
}
