package space.celestia.celestiaui.settings.viewmodel

import space.celestia.celestiaui.utils.PreferenceManager

class CustomFont(val path: String, val ttcIndex: Int)

var PreferenceManager.normalFont: CustomFont?
    get() {
        val fontPath = get(PreferenceManager.PredefinedKey.NormalFontPath) ?: return null
        val ttcIndex = get(PreferenceManager.PredefinedKey.NormalFontIndex)?.toIntOrNull() ?: 0
        return CustomFont(fontPath, ttcIndex)
    }
    set(value) {
        if (value == null) {
            set(PreferenceManager.PredefinedKey.NormalFontPath, null)
            set(PreferenceManager.PredefinedKey.NormalFontIndex, null)
        } else {
            set(PreferenceManager.PredefinedKey.NormalFontPath, value.path)
            set(PreferenceManager.PredefinedKey.NormalFontIndex, value.ttcIndex.toString())
        }
    }

var PreferenceManager.boldFont: CustomFont?
    get() {
        val fontPath = get(PreferenceManager.PredefinedKey.BoldFontPath) ?: return null
        val ttcIndex = get(PreferenceManager.PredefinedKey.BoldFontIndex)?.toIntOrNull() ?: 0
        return CustomFont(fontPath, ttcIndex)
    }
    set(value) {
        if (value == null) {
            set(PreferenceManager.PredefinedKey.BoldFontPath, null)
            set(PreferenceManager.PredefinedKey.BoldFontIndex, null)
        } else {
            set(PreferenceManager.PredefinedKey.BoldFontPath, value.path)
            set(PreferenceManager.PredefinedKey.BoldFontIndex, value.ttcIndex.toString())
        }
    }