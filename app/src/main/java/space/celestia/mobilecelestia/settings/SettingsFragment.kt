// SettingsFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Build
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.purchase.FontSettingFragment
import space.celestia.mobilecelestia.purchase.ToolbarSettingFragment
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : NavigationFragment() {
    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var executor: CelestiaExecutor

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return SettingsItemFragment.newInstance()
    }

    fun pushMainSettingItem(item: SettingsItem) {
        when (item) {
            is SettingsCommonItem -> {
                pushFragment(SettingsCommonFragment.newInstance(item))
            }
            is SettingsCurrentTimeItem -> {
                pushFragment(SettingsCurrentTimeFragment.newInstance())
            }
            is SettingsRenderInfoItem -> {
                pushFragment(RenderInfoFragment.newInstance())
            }
            is SettingsRefreshRateItem -> {
                pushFragment(SettingsRefreshRateFragment.newInstance())
            }
            is SettingsAboutItem -> {
                pushFragment(AboutFragment.newInstance())
            }
            is SettingsDataLocationItem -> {
                pushFragment(SettingsDataLocationFragment.newInstance())
            }
            is SettingsLanguageItem -> {
                pushFragment(SettingsLanguageFragment.newInstance())
            }
            is SettingsFontItem -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    pushFragment(FontSettingFragment.newInstance())
            }
            is SettingsToolbarItem -> {
                pushFragment(ToolbarSettingFragment.newInstance())
            }
            else -> {
                throw RuntimeException("SettingsFragment cannot handle item $item")
            }
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
