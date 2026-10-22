package space.celestia.celestiaui.settings.viewmodel

import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager

private val shadowMapSizes = listOf(0, 1024, 2048, 4096, 8192)

private fun isPointSpreadFunctionStarStyle(viewModel: SettingsViewModel): Boolean {
    val key = SettingsKey.StarStyle.valueString
    val value = viewModel.coreSettings[PreferenceManager.CustomKey(key)]?.toIntOrNull()
        ?: viewModel.appCore.getIntValueForField(key)
    return value == 3
}

val commonRendererItems: List<SettingsItem> = listOf(
    SettingsCommonItem(
        CelestiaString("Stars", "Star rendering settings"),
        listOf(
            SettingsCommonItem.Section(
                listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.StarStyle, options = listOf(
                        Pair(0, CelestiaString("Fuzzy Points", "Star style")),
                        Pair(1, CelestiaString("Points", "Star style")),
                        Pair(2, CelestiaString("Scaled Discs", "Star style")),
                        Pair(3, CelestiaString("Point Spread Function", "Star style")),
                    ), displayName = SettingsKey.StarStyle.displayName, defaultSelection = 0),
                    SettingsSliderItem(SettingsKey.StarPointRadius, 1.0, 10.0, subtitle = CelestiaString("Pixel radius of a unit-irradiance star sprite.", "PSF star setting footnote")).visibleWhen { isPointSpreadFunctionStarStyle(it) },
                    SettingsSliderItem(SettingsKey.StarOptimization, 0.05, 1.0, subtitle = CelestiaString("Extent of the eye PSF glow around bright stars. Lower values widen the glow at higher GPU cost.", "PSF star setting footnote")).visibleWhen { isPointSpreadFunctionStarStyle(it) },
                    SettingsSliderItem(SettingsKey.StarMaxIrradiance, 1.0, 1000000.0, isLogarithmic = true, subtitle = CelestiaString("Soft upper limit on per-star peak irradiance to prevent bloom saturation.", "PSF star setting footnote")).visibleWhen { isPointSpreadFunctionStarStyle(it) },
                    SettingsSliderItem(SettingsKey.StarDimClipFactor, 1.0, 100.0, subtitle = CelestiaString("Soft-clips dim stars below this multiple of the perceptual visibility floor.", "PSF star setting footnote")).visibleWhen { isPointSpreadFunctionStarStyle(it) },
                    SettingsSliderItem(SettingsKey.StarExposure, 0.01, 1000000.0, isLogarithmic = true, subtitle = CelestiaString("Brightness multiplier applied to every star, extending the visible magnitude limit.", "PSF star setting footnote")).visibleWhen { isPointSpreadFunctionStarStyle(it) },
                )
            ),
            SettingsCommonItem.Section(
                listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.StarColors, options = listOf(
                        Pair(0, CelestiaString("Classic Colors", "Star colors option")),
                        Pair(1, CelestiaString("Blackbody Illuminant E", "Star colors option")),
                        Pair(2, CelestiaString("Blackbody D65", "Star colors option")),
                        Pair(3, CelestiaString("Blackbody (Solar Whitepoint)", "Star colors option")),
                        Pair(4, CelestiaString("Blackbody (Vega Whitepoint)", "Star colors option")),
                    ), displayName = SettingsKey.StarColors.displayName, defaultSelection = 2),
                    SettingsSliderItem(SettingsKey.TintSaturation, 0.0, 1.0),
                ),
                footer = Footer.Text(CelestiaString("Tinted illumination saturation setting is only effective with Blackbody star colors.", ""))
            ),
            SettingsCommonItem.Section(
                listOf(
                    SettingsSwitchItem(SettingsKey.ShowAutoMag, SettingsSwitchItem.Representation.Switch),
                    SettingsSliderItem(SettingsKey.FaintestVisible, 3.0, 12.0),
                )
            ),
        )
    ),
    SettingsCommonItem(CelestiaString("Rendering", "Rendering settings"), listOf(
        SettingsCommonItem.Section(listOf(
            SettingsSwitchItem(SettingsKey.ShowSmoothLines, SettingsSwitchItem.Representation.Switch),
        )),
        SettingsCommonItem.Section(listOf(
            SettingsSliderItem(SettingsKey.AmbientLightLevel, 0.0, 1.0),
            SettingsSliderItem(SettingsKey.GalaxyBrightness, 0.0, 1.0),
        )),
    )),
)

fun rendererQualityItem(displayItems: List<SettingsItem>): SettingsItem {
    return SettingsCommonItem(
        CelestiaString("Quality", "Rendering quality settings"),
        listOf(
            SettingsCommonItem.Section(
                header = CelestiaString("Textures", "Texture rendering quality settings"),
                rows = listOf(
                    SettingsSelectionSingleItem(key = SettingsKey.Resolution, options = listOf(
                        Pair(0, CelestiaString("Low", "Low resolution")),
                        Pair(1, CelestiaString("Medium", "Medium resolution")),
                        Pair(2, CelestiaString("High", "High resolution")),
                    ), displayName = SettingsKey.Resolution.displayName, defaultSelection = 1),
                )
            ),
            SettingsCommonItem.Section(
                header = CelestiaString("Shadows", "Shadow rendering quality settings"),
                rows = listOf(
                    SettingsPreferenceIntegerSliderItem(
                        PreferenceManager.PredefinedKey.ShadowMapSize,
                        displayName = CelestiaString("Shadow Resolution", "Resolution of shadow maps"),
                        values = shadowMapSizes,
                        defaultSelection = 0,
                        subtitle = CelestiaString("A value of 0 disables self-shadowing. Higher values produce sharper shadows at a greater performance cost.", "Shadow resolution setting footnote")
                    )
                ),
                footer = Footer.Text(CelestiaString("Shadow resolution changes take effect after a restart.", "Change requires a restart"))
            ),
            SettingsCommonItem.Section(
                header = CelestiaString("Display", "Display quality settings"),
                rows = displayItems,
                footer = Footer.Text(CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
            ),
            SettingsCommonItem.Section(
                header = CelestiaString("Atmosphere", "Atmosphere rendering quality settings"),
                rows = listOf(
                    SettingsIntegerSliderItem(SettingsKey.AtmosphereSegmentCount, 1, 16, subtitle = CelestiaString("Number of segments used to integrate atmospheric scattering. Higher values improve quality at a greater performance cost.", "Atmosphere segment count setting description")),
                    SettingsIntegerSliderItem(SettingsKey.CloudSegmentCount, 1, 16, subtitle = CelestiaString("Number of segments used to render clouds. Higher values improve quality at a greater performance cost.", "Cloud segment count setting description")),
                    SettingsSwitchItem(SettingsKey.SeparateRayleighMieScaleHeights, SettingsSwitchItem.Representation.Switch),
                )
            )
        )
    )
}

fun outputRenderingItem(extraSections: List<SettingsCommonItem.Section> = emptyList()): SettingsItem {
    return SettingsCommonItem(
        CelestiaString("Output", "Output rendering settings"),
        listOf(
            SettingsCommonItem.Section(
                rows = listOf(
                    SettingsPreferenceSwitchItem(PreferenceManager.PredefinedKey.SRGBRendering, CelestiaString("sRGB Rendering (Experimental)", "")),
                    SettingsSelectionSingleItem(key = SettingsKey.ToneMapping, options = listOf(
                        Pair(0, CelestiaString("Off", "Tone mapping mode")),
                        Pair(1, CelestiaString("Manual", "Tone mapping mode")),
                    ), displayName = SettingsKey.ToneMapping.displayName, defaultSelection = 0).visibleWhen { viewModel ->
                        viewModel.appSettings[PreferenceManager.PredefinedKey.SRGBRendering] == "true"
                    },
                    SettingsSliderItem(SettingsKey.Exposure, 0.01, 100.0, isLogarithmic = true).visibleWhen { viewModel ->
                        val srgbEnabled = viewModel.appSettings[PreferenceManager.PredefinedKey.SRGBRendering] == "true"
                        val toneMapping = viewModel.coreSettings[PreferenceManager.CustomKey(SettingsKey.ToneMapping.valueString)]?.toIntOrNull()
                            ?: viewModel.appCore.getIntValueForField(SettingsKey.ToneMapping.valueString)
                        srgbEnabled && toneMapping == 1
                    },
                ),
                footer = Footer.Text(CelestiaString("Changes to sRGB rendering take effect after a restart.", "Output rendering settings footnote"))
            ),
        ) + extraSections
    )
}
