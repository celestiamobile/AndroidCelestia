/*
 * SettingsModel.kt
 *
 * Copyright (C) 2024-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.os.Build
import kotlinx.serialization.Serializable
import space.celestia.mobilecelestia.celestia.CelestiaInteraction
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager

const val settingUnmarkAllID = "UnmarkAll"

val PreferenceManager.PredefinedKey.displayName: String
    get() = when(this) {
        PreferenceManager.PredefinedKey.FullDPI -> CelestiaString("HiDPI", "HiDPI support in display")
        PreferenceManager.PredefinedKey.MSAA -> CelestiaString("Anti-aliasing", "")
        PreferenceManager.PredefinedKey.ControllerRemapA -> CelestiaString("A / X", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapB -> CelestiaString("B / Circle", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapX -> CelestiaString("X / Square", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapY -> CelestiaString("Y / Triangle", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapLT -> CelestiaString("LT / L2", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapLB -> CelestiaString("LB / L1", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapRT -> CelestiaString("RT / R2", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapRB -> CelestiaString("RB / R1", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapDpadLeft -> CelestiaString("D-pad Left", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapDpadUp -> CelestiaString("D-pad Up", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapDpadRight -> CelestiaString("D-pad Right", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerRemapDpadDown -> CelestiaString("D-pad Down", "Game controller button")
        PreferenceManager.PredefinedKey.ControllerInvertX -> CelestiaString("Invert Horizontally", "Invert game controller thumbstick axis horizontally")
        PreferenceManager.PredefinedKey.ControllerInvertY -> CelestiaString("Invert Vertically", "Invert game controller thumbstick axis vertically")
        PreferenceManager.PredefinedKey.ControllerEnableLeftThumbstick -> CelestiaString("Enable Left Thumbstick", "Setting item to control whether left thumbstick should be enabled")
        PreferenceManager.PredefinedKey.ControllerEnableRightThumbstick -> CelestiaString("Enable Right Thumbstick", "Setting item to control whether right thumbstick should be enabled")
        PreferenceManager.PredefinedKey.ContextMenu -> CelestiaString("Context Menu", "Settings for whether context menu is enabled")
        PreferenceManager.PredefinedKey.PickSensitivity -> CelestiaString("Sensitivity", "Setting for sensitivity for selecting an object")
        else -> ""
    }

val PreferenceManager.PredefinedKey.subtitle: String?
    get() = when(this) {
        PreferenceManager.PredefinedKey.ContextMenu -> CelestiaString("Context menu by long press or context click", "Description for how a context menu is triggered")
        PreferenceManager.PredefinedKey.PickSensitivity -> CelestiaString("Sensitivity for object selection", "Notes for the sensitivity setting")
        else -> null
    }


@Serializable
enum class SettingsKey : PreferenceManager.Key {
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
    TintSaturation,
    // Volatile boolean values
    ShowBodyAxes,
    ShowFrameAxes,
    ShowSunDirection,
    ShowVelocityVector,
    ShowPlanetographicGrid,
    ShowTerminator;

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
            ShowBodyAxes -> CelestiaString("Show Body Axes", "Reference vector")
            ShowFrameAxes -> CelestiaString("Show Frame Axes", "Reference vector")
            ShowSunDirection -> CelestiaString("Show Sun Direction", "Reference vector")
            ShowVelocityVector -> CelestiaString("Show Velocity Vector", "Reference vector")
            ShowPlanetographicGrid -> CelestiaString("Show Planetographic Grid", "Reference vector")
            ShowTerminator -> CelestiaString("Show Terminator", "Reference vector")
        }

    val subtitle: String?
        get() = when (this) {
            ScriptSystemAccessPolicy -> CelestiaString("Lua scripts' access to the file system", "Note for Script System Access Policy")
            EnableRayBasedDragging -> CelestiaString("Dragging behavior based on change of pick rays instead of screen coordinates", "")
            EnableFocusZooming -> CelestiaString("Zooming behavior keeping the original focus location on screen", "")
            else -> null
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

@Serializable
enum class StringResource {
    Empty,
    GameControllerOperationNone,
    GameControllerOperationMoveFaster,
    GameControllerOperationMoveSlower,
    GameControllerOperationStopSpeed,
    GameControllerOperationReverseSpeed,
    GameControllerOperationReverseOrientation,
    GameControllerOperationTapCenter,
    GameControllerOperationGoTo,
    GameControllerOperationEsc,
    GameControllerOperationPitchUp,
    GameControllerOperationPitchDown,
    GameControllerOperationYawLeft,
    GameControllerOperationYawRight,
    GameControllerOperationRollLeft,
    GameControllerOperationRollRight,
    TimeZoneLocalTime,
    TimeZoneUTC,
    DateFormatDefault,
    DateFormatYYYYMMMDDHHMMSS,
    DateFormatUTCOffset,
    MeasurementSystemMetric,
    MeasurementSystemImperial,
    TemperatureScaleKelvin,
    TemperatureScaleCelsius,
    TemperatureScaleFahrenheit,
    HudDetailNone,
    HudDetailTerse,
    HudDetailVerbose,
    ResolutionLow,
    ResolutionMedium,
    ResolutionHigh,
    StarStyleFuzzyPoints,
    StarStylePoints,
    StarStyleScaledDiscs,
    StarColorsClassicColors,
    StarColorsBlackbodyD65,
    StarColorsBlackbodySolarWhitepoint,
    StarColorsBlackbodyVegaWhitepoint,
    ScriptSystemAccessPolicyAsk,
    ScriptSystemAccessPolicyAllow,
    ScriptSystemAccessPolicyDeny,
    UnmarkAll,
    ToggleFPSDisplay,
    ToggleConsoleDisplay,
    SettingPageObjects,
    SettingPageFeatures,
    SettingPageOrbits,
    SettingPageGrids,
    SettingPageConstellations,
    SettingPageObjectLabels,
    SettingPageLocations,
    SettingPageMarkers,
    SettingPageReferenceVectors,
    SettingPageReferenceVectorsFooter,
    SettingPageTimeZone,
    SettingPageDateFormat,
    SettingPageMeasurementSystem,
    SettingPageHudDetail,
    SettingPageResolution,
    SettingPageStarStyle,
    SettingPageStarStyleFooter,
    SettingPageRenderParameters,
    SettingPageAdvanced,
    SettingPageAdvancedFooter,
    SettingPageInteraction,
    SettingPageInteractionFooter,
    SettingPageGameController,
    SettingPageGameControllerButtonHeader,
    SettingPageGameControllerThumbstickHeader,
    SettingPageSecurity,
    SettingPageDebug,
    SettingPageSectionCelestiaPlus,
    SettingPageSectionDisplay,
    SettingPageSectionTimeRegion,
    SettingPageSectionRenderer,
    SettingPageSectionAdvanced;

    val displayName: String
        get() = when (this) {
            Empty -> ""
            GameControllerOperationNone -> CelestiaString("None", "Empty HUD display")
            GameControllerOperationMoveFaster -> CelestiaString("Travel Faster", "Game controller action")
            GameControllerOperationMoveSlower -> CelestiaString("Travel Slower", "Game controller action")
            GameControllerOperationStopSpeed -> CelestiaString("Stop", "Interupt the process of finding eclipse/Set traveling speed to 0")
            GameControllerOperationReverseSpeed -> CelestiaString("Reverse Travel Direction", "Game controller action")
            GameControllerOperationReverseOrientation -> CelestiaString("Reverse Observer Orientation", "Game controller action")
            GameControllerOperationTapCenter -> CelestiaString("Tap Center", "Game controller action")
            GameControllerOperationGoTo -> CelestiaString("Go to Object", "")
            GameControllerOperationEsc -> CelestiaString("Esc", "Game controller action")
            GameControllerOperationPitchUp -> CelestiaString("Pitch Up", "Game controller action")
            GameControllerOperationPitchDown -> CelestiaString("Pitch Down", "Game controller action")
            GameControllerOperationYawLeft -> CelestiaString("Yaw Left", "Game controller action")
            GameControllerOperationYawRight -> CelestiaString("Yaw Right", "Game controller action")
            GameControllerOperationRollLeft -> CelestiaString("Roll Left", "Game controller action")
            GameControllerOperationRollRight -> CelestiaString("Roll Right", "Game controller action")
            TimeZoneLocalTime -> CelestiaString("Local Time", "")
            TimeZoneUTC -> CelestiaString("UTC", "")
            DateFormatDefault -> CelestiaString("Default", "")
            DateFormatYYYYMMMDDHHMMSS -> CelestiaString("YYYY MMM DD HH:MM:SS TZ", "")
            DateFormatUTCOffset -> CelestiaString("UTC Offset", "")
            MeasurementSystemMetric -> CelestiaString("Metric", "Metric measurement system")
            MeasurementSystemImperial -> CelestiaString("Imperial", "Imperial measurement system")
            TemperatureScaleKelvin -> CelestiaString("Kelvin", "Temperature scale")
            TemperatureScaleCelsius -> CelestiaString("Celsius", "Temperature scale")
            TemperatureScaleFahrenheit -> CelestiaString("Fahrenheit", "Temperature scale")
            HudDetailNone -> CelestiaString("None", "Empty HUD display")
            HudDetailTerse -> CelestiaString("Terse", "Terse HUD display")
            HudDetailVerbose -> CelestiaString("Verbose", "Verbose HUD display")
            ResolutionLow -> CelestiaString("Low", "Low resolution")
            ResolutionMedium -> CelestiaString("Medium", "Medium resolution")
            ResolutionHigh -> CelestiaString("High", "High resolution")
            StarStyleFuzzyPoints -> CelestiaString("Fuzzy Points", "Star style")
            StarStylePoints -> CelestiaString("Points", "Star style")
            StarStyleScaledDiscs -> CelestiaString("Scaled Discs", "Star style")
            StarColorsClassicColors -> CelestiaString("Classic Colors", "Star colors option")
            StarColorsBlackbodyD65 -> CelestiaString("Blackbody D65", "Star colors option")
            StarColorsBlackbodySolarWhitepoint -> CelestiaString("Blackbody (Solar Whitepoint)", "Star colors option")
            StarColorsBlackbodyVegaWhitepoint -> CelestiaString("Blackbody (Vega Whitepoint)", "Star colors option")
            ScriptSystemAccessPolicyAsk -> CelestiaString("Ask", "Script system access policy option")
            ScriptSystemAccessPolicyAllow -> CelestiaString("Allow", "Script system access policy option")
            ScriptSystemAccessPolicyDeny -> CelestiaString("Deny", "Script system access policy option")
            UnmarkAll -> CelestiaString("Unmark All", "Unmark all objects")
            ToggleFPSDisplay -> CelestiaString("Toggle FPS Display", "Toggle FPS display on overlay")
            ToggleConsoleDisplay -> CelestiaString("Toggle Console Display", "Toggle console log display on overlay")
            SettingPageObjects -> CelestiaString("Objects", "")
            SettingPageFeatures -> CelestiaString("Features", "")
            SettingPageOrbits -> CelestiaString("Orbits", "")
            SettingPageGrids -> CelestiaString("Grids", "")
            SettingPageConstellations -> CelestiaString("Constellations", "")
            SettingPageObjectLabels -> CelestiaString("Object Labels", "Labels")
            SettingPageLocations -> CelestiaString("Locations", "Location labels to display")
            SettingPageMarkers -> CelestiaString("Markers", "")
            SettingPageReferenceVectors -> CelestiaString("Reference Vectors", "Reference vectors for an object")
            SettingPageReferenceVectorsFooter -> CelestiaString("Reference vectors are only visible for the current selected solar system object.", "")
            SettingPageTimeZone -> CelestiaString("Time Zone", "")
            SettingPageDateFormat -> CelestiaString("Date Format", "")
            SettingPageMeasurementSystem -> CelestiaString("Measure Units", "Measurement system")
            SettingPageHudDetail -> CelestiaString("Info Display", "HUD display")
            SettingPageResolution -> CelestiaString("Texture Resolution", "")
            SettingPageStarStyle -> CelestiaString("Star Style", "")
            SettingPageStarStyleFooter -> CelestiaString("Tinted illumination saturation setting is only effective with Blackbody star colors.", "")
            SettingPageRenderParameters -> CelestiaString("Render Parameters", "Render parameters in setting")
            SettingPageAdvanced -> CelestiaString("Advanced", "Advanced setting items")
            SettingPageAdvancedFooter -> CelestiaString("Configuration will take effect after a restart.", "Change requires a restart")
            SettingPageInteraction -> CelestiaString("Interaction", "Settings for interaction")
            SettingPageInteractionFooter -> CelestiaString("Some configurations will take effect after a restart.", "")
            SettingPageGameController -> CelestiaString("Game Controller", "Settings for game controller")
            SettingPageGameControllerButtonHeader -> CelestiaString("Buttons", "Settings for game controller buttons")
            SettingPageGameControllerThumbstickHeader -> CelestiaString("Thumbsticks", "Settings for game controller thumbsticks")
            SettingPageSecurity -> CelestiaString("Security", "Security settings title")
            SettingPageDebug -> CelestiaString("Debug", "Debug menu")
            SettingPageSectionCelestiaPlus -> CelestiaString("Celestia PLUS", "Name for the subscription service")
            SettingPageSectionDisplay -> CelestiaString("Display", "Display settings")
            SettingPageSectionTimeRegion -> CelestiaString("Time & Region", "time and region related settings")
            SettingPageSectionRenderer -> CelestiaString("Renderer", "In settings")
            SettingPageSectionAdvanced -> CelestiaString("Advanced", "Advanced setting items")
        }
}

private val gameControllerRemapOptions = listOf(
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE, StringResource.GameControllerOperationNone),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER, StringResource.GameControllerOperationMoveFaster),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER, StringResource.GameControllerOperationMoveSlower),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_STOP_SPEED, StringResource.GameControllerOperationStopSpeed),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_REVERSE_SPEED, StringResource.GameControllerOperationReverseSpeed),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_REVERSE_ORIENTATION, StringResource.GameControllerOperationReverseOrientation),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_TAP_CENTER, StringResource.GameControllerOperationTapCenter),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_GO_TO, StringResource.GameControllerOperationGoTo),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ESC, StringResource.GameControllerOperationEsc),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP, StringResource.GameControllerOperationPitchUp),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN, StringResource.GameControllerOperationPitchDown),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_YAW_LEFT, StringResource.GameControllerOperationYawLeft),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_YAW_RIGHT, StringResource.GameControllerOperationYawRight),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT, StringResource.GameControllerOperationRollLeft),
    OptionPair(CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT, StringResource.GameControllerOperationRollRight)
)

@Serializable
data class OptionPair(val index: Int, val name: StringResource)

@Serializable
sealed class SettingsEntry {
    abstract val name: String
    
    @Serializable
    data class SliderItem(private val internalKey: SettingsKey, val minValue: Double = 0.0, val maxValue: Double = 1.0) : SettingsEntry() {
        val key: String
            get() = internalKey.valueString

        override val name: String
            get() = internalKey.displayName
    }

    @Serializable
    data class PreferenceSwitchItem(
        val key: PreferenceManager.PredefinedKey,
        val defaultOn: Boolean = false
    ) : SettingsEntry() {
        override val name: String
            get() = key.displayName
    }

    @Serializable
    data class PreferenceSliderItem(
        val key: PreferenceManager.PredefinedKey,
        val minValue: Double = 0.0,
        val maxValue: Double = 1.0,
        val defaultValue: Double = 0.0
    ) : SettingsEntry() {
        override val name: String
            get() = key.displayName
    }

    @Serializable
    data class SwitchItem(
        val key: SettingsKey,
        val representation: Representation = Representation.Checkmark,
        val volatile: Boolean = false
        ) : SettingsEntry() {
        @Serializable
        enum class Representation  {
            Checkmark, Switch;
        }

        override val name: String
            get() = key.displayName
    }

    @Serializable
    data class PreferenceSelectionItem(
        val key: PreferenceManager.PredefinedKey,
        val options: List<OptionPair>,
        val defaultSelection: Int
    ) : SettingsEntry() {
        override val name: String
            get() = key.displayName
    }

    @Serializable
    data class SelectionSingleItem(
        val key: SettingsKey,
        val options: List<OptionPair>,
        val defaultSelection: Int,
        val showTitle: Boolean = true
    ) : SettingsEntry() {
        override val name: String
            get() = key.displayName
    }

    @Serializable
    data class UnknownTextItem(val nameResource: StringResource, val id: String) : SettingsEntry() {
        override val name: String
            get() = nameResource.displayName
    }

    @Serializable
    data class ActionItem(val nameResource: StringResource, val action: Int) : SettingsEntry() {
        override val name: String
            get() = nameResource.displayName
    }
}

private val staticDisplayItems: List<Settings> = listOf(
    Settings.Common.create(StringResource.SettingPageObjects, listOf(
        SettingsEntry.SwitchItem(SettingsKey.ShowStars),
        SettingsEntry.SwitchItem(SettingsKey.ShowPlanets),
        SettingsEntry.SwitchItem(SettingsKey.ShowDwarfPlanets),
        SettingsEntry.SwitchItem(SettingsKey.ShowMoons),
        SettingsEntry.SwitchItem(SettingsKey.ShowMinorMoons),
        SettingsEntry.SwitchItem(SettingsKey.ShowAsteroids),
        SettingsEntry.SwitchItem(SettingsKey.ShowComets),
        SettingsEntry.SwitchItem(SettingsKey.ShowSpacecrafts),
        SettingsEntry.SwitchItem(SettingsKey.ShowGalaxies),
        SettingsEntry.SwitchItem(SettingsKey.ShowNebulae),
        SettingsEntry.SwitchItem(SettingsKey.ShowGlobulars),
        SettingsEntry.SwitchItem(SettingsKey.ShowOpenClusters)
    )),
    Settings.Common.create(StringResource.SettingPageFeatures, listOf(
        SettingsEntry.SwitchItem(SettingsKey.ShowAtmospheres),
        SettingsEntry.SwitchItem(SettingsKey.ShowCloudMaps),
        SettingsEntry.SwitchItem(SettingsKey.ShowCloudShadows),
        SettingsEntry.SwitchItem(SettingsKey.ShowNightMaps),
        SettingsEntry.SwitchItem(SettingsKey.ShowPlanetRings),
        SettingsEntry.SwitchItem(SettingsKey.ShowRingShadows),
        SettingsEntry.SwitchItem(SettingsKey.ShowCometTails),
        SettingsEntry.SwitchItem(SettingsKey.ShowEclipseShadows)
    )),
    Settings.Common(StringResource.SettingPageOrbits, listOf(
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowOrbits, SettingsEntry.SwitchItem.Representation.Switch),
            SettingsEntry.SwitchItem(SettingsKey.ShowFadingOrbits, SettingsEntry.SwitchItem.Representation.Switch),
            SettingsEntry.SwitchItem(SettingsKey.ShowPartialTrajectories, SettingsEntry.SwitchItem.Representation.Switch),
        )),
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowStellarOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowPlanetOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowDwarfPlanetOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowMoonOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowMinorMoonOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowAsteroidOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowCometOrbits, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowSpacecraftOrbits, SettingsEntry.SwitchItem.Representation.Checkmark)
        )),
    )),
    Settings.Common(StringResource.SettingPageGrids, listOf(
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowCelestialSphere, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowEclipticGrid, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowHorizonGrid, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowGalacticGrid, SettingsEntry.SwitchItem.Representation.Checkmark),
        )),
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowEcliptic, SettingsEntry.SwitchItem.Representation.Checkmark),
        )),
    )),
    Settings.Common.create(StringResource.SettingPageConstellations, listOf(
        SettingsEntry.SwitchItem(SettingsKey.ShowDiagrams, SettingsEntry.SwitchItem.Representation.Checkmark),
        SettingsEntry.SwitchItem(SettingsKey.ShowConstellationLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
        SettingsEntry.SwitchItem(SettingsKey.ShowLatinConstellationLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
        SettingsEntry.SwitchItem(SettingsKey.ShowBoundaries, SettingsEntry.SwitchItem.Representation.Checkmark),
    )),
    Settings.Common.create(StringResource.SettingPageObjectLabels, listOf(
        SettingsEntry.SwitchItem(SettingsKey.ShowStarLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowPlanetLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowDwarfPlanetLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowMoonLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowMinorMoonLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowAsteroidLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowCometLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowSpacecraftLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowGalaxyLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowNebulaLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowGlobularLabels),
        SettingsEntry.SwitchItem(SettingsKey.ShowOpenClusterLabels)
    )),
    Settings.Common(StringResource.SettingPageLocations, listOf(
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowLocationLabels, SettingsEntry.SwitchItem.Representation.Switch),
            SettingsEntry.SliderItem(SettingsKey.MinimumFeatureSize, 0.0, 99.0),
        )),
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowCityLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowObservatoryLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowLandingSiteLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowMonsLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowMareLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowCraterLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowVallisLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowTerraLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowEruptiveCenterLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
            SettingsEntry.SwitchItem(SettingsKey.ShowOtherLabels, SettingsEntry.SwitchItem.Representation.Checkmark),
        )),
    )),
    Settings.Common.create(StringResource.SettingPageMarkers, listOf(
        SettingsEntry.SwitchItem(SettingsKey.ShowMarkers, SettingsEntry.SwitchItem.Representation.Switch),
        SettingsEntry.UnknownTextItem(StringResource.UnmarkAll, settingUnmarkAllID)
    )),
    Settings.Common(
        StringResource.SettingPageReferenceVectors,
        listOf(
            Settings.Common.Section(
                listOf(
                    SettingsEntry.SwitchItem(SettingsKey.ShowBodyAxes, volatile = true),
                    SettingsEntry.SwitchItem(SettingsKey.ShowFrameAxes, volatile = true),
                    SettingsEntry.SwitchItem(SettingsKey.ShowSunDirection, volatile = true),
                    SettingsEntry.SwitchItem(SettingsKey.ShowVelocityVector, volatile = true),
                    SettingsEntry.SwitchItem(SettingsKey.ShowPlanetographicGrid, volatile = true),
                    SettingsEntry.SwitchItem(SettingsKey.ShowTerminator, volatile = true)
                ),
                footer = StringResource.SettingPageReferenceVectorsFooter
            )
        )
    )
)

private val staticTimeAndRegionItems: List<Settings> = listOf(
    Settings.Common.create(
        StringResource.SettingPageTimeZone,
        listOf(
            SettingsEntry.SelectionSingleItem(key = SettingsKey.TimeZone, options = listOf(
                OptionPair(0, StringResource.TimeZoneLocalTime),
                OptionPair(1, StringResource.TimeZoneUTC),
            ), defaultSelection = 0, showTitle = false)
        )
    ),
    Settings.Common.create(
        StringResource.SettingPageDateFormat,
        listOf(
            SettingsEntry.SelectionSingleItem(key = SettingsKey.DateFormat, options = listOf(
                OptionPair(0, StringResource.DateFormatDefault),
                OptionPair(1, StringResource.DateFormatYYYYMMMDDHHMMSS),
                OptionPair(2, StringResource.DateFormatUTCOffset),
            ), defaultSelection = 1, showTitle = false)
        )
    ),
    Settings.CurrentTime,
    Settings.Common.create(
        StringResource.SettingPageMeasurementSystem,
        listOf(
            SettingsEntry.SelectionSingleItem(key = SettingsKey.MeasurementSystem, options = listOf(
                OptionPair(0, StringResource.MeasurementSystemMetric),
                OptionPair(1, StringResource.MeasurementSystemImperial),
            ), defaultSelection = 0, showTitle = false),
            SettingsEntry.SelectionSingleItem(key = SettingsKey.TemperatureScale, options = listOf(
                OptionPair(0, StringResource.TemperatureScaleKelvin),
                OptionPair(1, StringResource.TemperatureScaleCelsius),
                OptionPair(2, StringResource.TemperatureScaleFahrenheit),
            ), defaultSelection = 0, showTitle = true)
        )
    ),
    Settings.Common.create(
        StringResource.SettingPageHudDetail,
        listOf(
            SettingsEntry.SelectionSingleItem(key = SettingsKey.HudDetail, options = listOf(
                OptionPair(0, StringResource.HudDetailNone),
                OptionPair(1, StringResource.HudDetailTerse),
                OptionPair(2, StringResource.HudDetailVerbose),
            ), defaultSelection = 1, showTitle = false),
        )
    ),
    Settings.Language
)

private val staticRendererItems: List<Settings> = listOf(
    Settings.Common.create(
        StringResource.SettingPageResolution,
        listOf(
            SettingsEntry.SelectionSingleItem(key = SettingsKey.Resolution, options = listOf(
                OptionPair(0, StringResource.ResolutionLow),
                OptionPair(1, StringResource.ResolutionMedium),
                OptionPair(2, StringResource.ResolutionHigh),
            ), defaultSelection = 1, showTitle = false)
        )
    ),
    Settings.Common(
        StringResource.SettingPageStarStyle,
        listOf(
            Settings.Common.Section(
                listOf(
                    SettingsEntry.SelectionSingleItem(key = SettingsKey.StarStyle, options = listOf(
                        OptionPair(0, StringResource.StarStyleFuzzyPoints),
                        OptionPair(1, StringResource.StarStylePoints),
                        OptionPair(2, StringResource.StarStyleScaledDiscs),
                    ), defaultSelection = 0),
                    SettingsEntry.SelectionSingleItem(key = SettingsKey.StarColors, options = listOf(
                        OptionPair(0, StringResource.StarColorsClassicColors),
                        OptionPair(1, StringResource.StarColorsBlackbodyD65),
                        OptionPair(2, StringResource.StarColorsBlackbodySolarWhitepoint),
                        OptionPair(3, StringResource.StarColorsBlackbodyVegaWhitepoint),
                    ), defaultSelection = 1),
                    SettingsEntry.SliderItem(SettingsKey.TintSaturation, 0.0, 1.0),
                ), footer = StringResource.SettingPageStarStyleFooter
            )
        )
    ),
    Settings.Common(StringResource.SettingPageRenderParameters, listOf(
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowSmoothLines, SettingsEntry.SwitchItem.Representation.Switch),
        )),
        Settings.Common.Section(listOf(
            SettingsEntry.SwitchItem(SettingsKey.ShowAutoMag, SettingsEntry.SwitchItem.Representation.Switch),
            SettingsEntry.SliderItem(SettingsKey.AmbientLightLevel, 0.0, 1.0),
            SettingsEntry.SliderItem(SettingsKey.FaintestVisible, 3.0, 12.0),
            SettingsEntry.SliderItem(SettingsKey.GalaxyBrightness, 0.0, 1.0)
        )),
    )),
    Settings.RefreshRate,
    Settings.Common(StringResource.SettingPageAdvanced, listOf(
        Settings.Common.Section(listOf(
            SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.FullDPI, true),
            SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.MSAA)
        ), footer =  StringResource.SettingPageAdvancedFooter)
    )),
    Settings.RenderInfo
)

private val staticAdvancedItems: List<Settings> = listOf(
    Settings.Common(
        StringResource.SettingPageInteraction,
        listOf(
            Settings.Common.Section(
                listOf(
                    SettingsEntry.SwitchItem(SettingsKey.EnableReverseWheel, representation = SettingsEntry.SwitchItem.Representation.Switch),
                    SettingsEntry.SwitchItem(SettingsKey.EnableRayBasedDragging, representation = SettingsEntry.SwitchItem.Representation.Switch),
                    SettingsEntry.SwitchItem(SettingsKey.EnableFocusZooming, representation = SettingsEntry.SwitchItem.Representation.Switch),
                    SettingsEntry.PreferenceSliderItem(PreferenceManager.PredefinedKey.PickSensitivity, minValue = 1.0, maxValue = 20.0, defaultValue = 10.0),
                    SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.ContextMenu, true),
                ),
                footer = StringResource.SettingPageInteractionFooter
            )
        )
    ),
    Settings.Common(
        StringResource.SettingPageGameController,
        listOf(
            Settings.Common.Section(
                listOf(
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapA, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapB, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapX, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapY, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadUp, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadDown, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadLeft, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadRight, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLB, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLT, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRB, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_NONE),
                    SettingsEntry.PreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRT, options = gameControllerRemapOptions, defaultSelection = CelestiaInteraction.GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT),
                ),
                header = StringResource.SettingPageGameControllerButtonHeader,
            ),
            Settings.Common.Section(
                listOf(
                    SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerEnableLeftThumbstick, true),
                    SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerEnableRightThumbstick, true),
                    SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerInvertX, false),
                    SettingsEntry.PreferenceSwitchItem(PreferenceManager.PredefinedKey.ControllerInvertY, false),
                ),
                header = StringResource.SettingPageGameControllerThumbstickHeader,
            )
        )
    ),
    Settings.DataLocation,
    Settings.Common(
        StringResource.SettingPageSecurity,
        listOf(
            Settings.Common.Section(
                listOf(
                    SettingsEntry.SelectionSingleItem(key = SettingsKey.ScriptSystemAccessPolicy, options = listOf(
                        OptionPair(0, StringResource.ScriptSystemAccessPolicyAsk),
                        OptionPair(1, StringResource.ScriptSystemAccessPolicyAllow),
                        OptionPair(2, StringResource.ScriptSystemAccessPolicyDeny),
                    ), defaultSelection = 0, showTitle = true)
                ),
            )
        )
    ),
)

private val staticOtherItems: List<Settings> = listOf(
    Settings.Common.create(
        StringResource.SettingPageDebug,
        listOf(
            SettingsEntry.ActionItem(StringResource.ToggleFPSDisplay, 0x60),
            SettingsEntry.ActionItem(StringResource.ToggleConsoleDisplay, 0x7E)
        )
    ),
    Settings.About
)

val celestiaPlusSettingSection: List<SettingsSection> by lazy {
    val items = arrayListOf<Settings>()
    items.add(Settings.Toolbar)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        items.add(Settings.Font)
    }
    if (items.isEmpty()) {
        listOf()
    } else {
        listOf(SettingsSection(items, StringResource.SettingPageSectionCelestiaPlus))
    }
}

val mainSettingSectionsBeforePlus: List<SettingsSection> = listOf(
    SettingsSection(staticDisplayItems, StringResource.SettingPageSectionDisplay),
    SettingsSection(staticTimeAndRegionItems, StringResource.SettingPageSectionTimeRegion),
    SettingsSection(staticRendererItems, StringResource.SettingPageSectionRenderer),
    SettingsSection(staticAdvancedItems, StringResource.SettingPageSectionAdvanced),
)

val mainSettingSectionsAfterPlus: List<SettingsSection> = listOf(
    SettingsSection(staticOtherItems, StringResource.Empty),
)