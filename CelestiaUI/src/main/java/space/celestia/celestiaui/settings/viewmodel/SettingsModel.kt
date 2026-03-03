package space.celestia.celestiaui.settings.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import java.io.Serializable

const val settingUnmarkAllID = "UnmarkAll"

sealed class Footer: Serializable {
    data class Text(val text: String): Footer(), Serializable
    data class TextWithLink(val text: String, val linkText: String, val link: String, val localizable: Boolean): Footer(),
        Serializable
}

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

class SettingsCurrentTimeItem : SettingsItem {
    override val name: String
        get() = CelestiaString("Current Time", "")
}
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

class SettingsRenderInfoItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("Render Info", "Information about renderer")
}

class SettingsAboutItem : SettingsItem, Serializable {
    override val name: String
        get() = CelestiaString("About", "About Celestia")
}

class SettingsUnknownTextItem(override val name: String, val id: String) : SettingsItem,
    Serializable

class SettingsActionItem(override val name: String, val action: Int): SettingsItem,
    Serializable

class SettingsCommonItem(override val name: String, val sections: List<Section>) : SettingsItem,
    Serializable {
    class Section(val rows: List<SettingsItem>, val header: String? = "", val footer: Footer? = null) :
        Serializable

    companion object {
        fun create(name: String, items: List<SettingsItem>): SettingsCommonItem {
            return SettingsCommonItem(name, listOf(Section(items)))
        }
    }
}