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
import space.celestia.celestiaui.common.CommonSectionV2
import space.celestia.celestiaui.control.viewmodel.JoystickAction
import space.celestia.celestiaui.settings.viewmodel.Footer
import space.celestia.celestiaui.settings.viewmodel.SettingsAboutItem
import space.celestia.celestiaui.settings.viewmodel.SettingsActionItem
import space.celestia.celestiaui.settings.viewmodel.SettingsCommonItem
import space.celestia.celestiaui.settings.viewmodel.SettingsCurrentTimeItem
import space.celestia.celestiaui.settings.viewmodel.SettingsDataLocationItem
import space.celestia.celestiaui.settings.viewmodel.SettingsFontItem
import space.celestia.celestiaui.settings.viewmodel.SettingsItem
import space.celestia.celestiaui.settings.viewmodel.SettingsKey
import space.celestia.celestiaui.settings.viewmodel.SettingsLanguageItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceSelectionItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceSliderItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceSwitchItem
import space.celestia.celestiaui.settings.viewmodel.SettingsRefreshRateItem
import space.celestia.celestiaui.settings.viewmodel.SettingsRenderInfoItem
import space.celestia.celestiaui.settings.viewmodel.SettingsSelectionSingleItem
import space.celestia.celestiaui.settings.viewmodel.SettingsSliderItem
import space.celestia.celestiaui.settings.viewmodel.SettingsSwitchItem
import space.celestia.celestiaui.settings.viewmodel.SettingsToolbarItem
import space.celestia.celestiaui.settings.viewmodel.SettingsUnknownTextItem
import space.celestia.celestiaui.settings.viewmodel.settingUnmarkAllID
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager

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
                footer = Footer.Text(CelestiaString("Reference vectors are only visible for the current selected solar system object.", ""))
            )
        )
    )
)

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
                ), footer = Footer.Text(CelestiaString("Tinted illumination saturation setting is only effective with Blackbody star colors.", ""))
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
        ),  footer = Footer.Text(CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))),
        SettingsCommonItem.Section(
            header = CelestiaString("External Display", "Section header text for settings"),
            rows = listOf(SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.DetectVirtualDisplay, CelestiaString("Detect Virtual Display (Casting)", "Settings to support virtual display in external display"), false)),
            footer = Footer.TextWithLink(text = CelestiaString("Wireless displays can sometimes stay 'active' after disconnecting. If casting fails, please check the FAQ.", "Warning text for using virtual display in Celestia"), linkText = CelestiaString("FAQ", "The text FAQ found in other text where FAQ indicates the link"), link = "https://celestia.mobi/help/faq", localizable = true)
        ),
    )),
    SettingsRenderInfoItem()
)

val gameControllerRemapOptions = listOf(
    Pair(JoystickAction.Key.None.key, CelestiaString("None", "Empty HUD display")),
    Pair(JoystickAction.Key.Celestia.MoveFaster.key, CelestiaString("Travel Faster", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.MoveSlower.key, CelestiaString("Travel Slower", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.StopSpeed.key, CelestiaString("Stop", "Interupt the process of finding eclipse/Set traveling speed to 0")),
    Pair(JoystickAction.Key.Celestia.ReverseSpeed.key, CelestiaString("Reverse Travel Direction", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.ReverseOrientation.key, CelestiaString("Reverse Observer Orientation", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.TapCenter.key, CelestiaString("Tap Center", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.GoTo.key, CelestiaString("Go to Object", "")),
    Pair(JoystickAction.Key.Celestia.Esc.key, CelestiaString("Esc", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.PitchUp.key, CelestiaString("Pitch Up", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.PitchDown.key, CelestiaString("Pitch Down", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.YawLeft.key, CelestiaString("Yaw Left", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.YawRight.key, CelestiaString("Yaw Right", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.RollLeft.key, CelestiaString("Roll Left", "Game controller action")),
    Pair(JoystickAction.Key.Celestia.RollRight.key, CelestiaString("Roll Right", "Game controller action")),
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
                footer = Footer.Text(CelestiaString("Some configurations will take effect after a restart.", ""))
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
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapA, displayName = CelestiaString("A / X", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.MoveSlower.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapB, displayName = CelestiaString("B / Circle", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.None.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapX, displayName = CelestiaString("X / Square", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.MoveFaster.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapY, displayName = CelestiaString("Y / Triangle", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.None.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadUp, displayName = CelestiaString("D-pad Up", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.PitchUp.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadDown, displayName = CelestiaString("D-pad Down", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.PitchDown.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadLeft, displayName = CelestiaString("D-pad Left", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.RollLeft.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapDpadRight, displayName = CelestiaString("D-pad Right", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.RollRight.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLB, displayName = CelestiaString("LB / L1", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.None.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapLT, displayName = CelestiaString("LT / L2", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.RollLeft.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRB, displayName = CelestiaString("RB / R1", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.None.key),
                    SettingsPreferenceSelectionItem(PreferenceManager.PredefinedKey.ControllerRemapRT, displayName = CelestiaString("RT / R2", "Game controller button"), options = gameControllerRemapOptions, defaultSelection = JoystickAction.Key.Celestia.RollRight.key),
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