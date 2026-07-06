// FeatureFlagsManager.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.resource.model

import org.json.JSONObject
import space.celestia.celestiaui.utils.PreferenceManager
import java.util.UUID

class FeatureFlagsManager(
    private val resourceAPI: ResourceAPIService,
    private val preferenceManager: PreferenceManager,
    private val platform: String,
    private val distribution: String?,
    private val version: String
) {
    companion object {
        // Add new flag keys here
        private val flagKeys = listOf("dummy")
    }

    suspend fun update(lang: String) {
        try {
            val result = resourceAPI.features(platform = platform, distribution = distribution, lang = lang, version = version)

            val deviceIdKey = PreferenceManager.PredefinedKey.DeviceID
            val deviceId = preferenceManager[deviceIdKey] ?: run {
                val newId = UUID.randomUUID().toString()
                preferenceManager[deviceIdKey] = newId
                newId
            }

            val evaluated = mutableMapOf<String, Boolean>()
            for (key in flagKeys) {
                val progress = result[key] ?: continue
                val combined = deviceId + key
                val seed = (combined.hashCode().toLong() and 0xFFFFFFFFL).toDouble() / 0xFFFFFFFFL.toDouble()
                evaluated[key] = seed < progress
            }

            val json = JSONObject()
            for (key in flagKeys)
                json.put(key, evaluated[key] ?: false)
            preferenceManager[PreferenceManager.PredefinedKey.FeatureFlags] = json.toString()
        } catch (_: Throwable) {}
    }

    fun get(): FeatureFlags {
        val stored = preferenceManager[PreferenceManager.PredefinedKey.FeatureFlags]
            ?: return FeatureFlags()
        return try {
            val json = JSONObject(stored)
            FeatureFlags(
                dummy = json.optBoolean("dummy", false),
            )
        } catch (_: Throwable) {
            FeatureFlags()
        }
    }
}
