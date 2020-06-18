/*
 * SettingsFragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.celestia.CelestiaView
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.core.CelestiaAppCore
import java.lang.RuntimeException

class SettingsFragment : PoppableFragment() {

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            popLast()
        }
        val main = SettingsItemFragment.newInstance()
        replace(main, main.title)
    }

    fun replace(fragment: Fragment, title: String) {
        replace(fragment, R.id.settings_container)
        toolbar.title = title
        toolbar.navigationIcon = null
    }

    fun push(fragment: Fragment, title: String) {
        push(fragment, R.id.settings_container)
        toolbar.title = title
        toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_arrow_back, null)
    }

    fun pushMainSettingItem(item: SettingsItem) {
        when (item) {
            is SettingsMultiSelectionItem -> {
                push(SettingsMultiSelectionFragment.newInstance(item), item.name)
            }
            is SettingsSingleSelectionItem -> {
                push(SettingsSingleSelectionFragment.newInstance(item), item.name)
            }
            is SettingsCommonItem -> {
                push(SettingsCommonFragment.newInstance(item), item.name)
            }
            is SettingsCurrentTimeItem -> {
                push(SettingsCurrentTimeFragment.newInstance(), item.name)
            }
            is SettingsRenderInfoItem -> {
                CelestiaView.callOnRenderThread {
                    val renderInfo = CelestiaAppCore.shared().renderInfo
                    activity?.runOnUiThread {
                        push(SimpleTextFragment.newInstance(item.name, renderInfo), item.name)
                    }
                }
            }
            is SettingsAboutItem -> {
                push(AboutFragment.newInstance(), item.name)
            }
            is SettingsDataLocationItem -> {
                push(SettingsDataLocationFragment.newInstance(), item.name)
            }
            else -> {
                throw RuntimeException("SettingsFragment cannot handle item $item")
            }
        }
    }

    fun reload() {
        val frag = childFragmentManager.findFragmentById(R.id.settings_container)
        if (frag is SettingsBaseFragment)
            frag.reload()
    }

    override fun canPop(): Boolean {
        return childFragmentManager.backStackEntryCount > 0
    }

    override fun popLast() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
        toolbar.title = (childFragmentManager.fragments[index] as TitledFragment).title
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }

}
