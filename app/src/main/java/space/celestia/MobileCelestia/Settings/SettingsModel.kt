package space.celestia.MobileCelestia.Settings

import android.preference.MultiSelectListPreference
import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.RecyclerViewItem

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
        SettingsMultiSelectionItem.Selection("Stars", "ShowStars"),
        SettingsMultiSelectionItem.Selection("Planets", "ShowPlanets"),
        SettingsMultiSelectionItem.Selection("Dwarf Planets", "ShowDwarfPlanets")
    )),
    SettingsMultiSelectionItem("Orbits", "ShowOrbits", listOf(
        SettingsMultiSelectionItem.Selection("Stars", "ShowStarOrbits"),
        SettingsMultiSelectionItem.Selection("Planets", "ShowPlanetOrbits"),
        SettingsMultiSelectionItem.Selection("Dwarf Planets", "ShowDwarfPlanetOrbits")
    ))
)

class SettingsSingleSelectionItem(
    override val name: String,
    val key: String,
    val selections: List<Selection>) : SettingsItem {
    class Selection(val name: String, val value: Int) {}
}

private val staticTimeItems: List<SettingsSingleSelectionItem> = listOf(
    SettingsSingleSelectionItem("Time Zone", "TimeZone", listOf(
        SettingsSingleSelectionItem.Selection("Local Time", 0),
        SettingsSingleSelectionItem.Selection("UTC", 1)
    ))
)

val mainSettingSections: List<CommonSectionV2> = listOf(
    CommonSectionV2(staticDisplayItems, "Display"),
    CommonSectionV2(staticTimeItems, "Time")
)
