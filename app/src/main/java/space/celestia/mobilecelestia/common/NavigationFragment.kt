/*
 * NavigationFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R

abstract class NavigationFragment: Fragment(), Poppable, Toolbar.OnMenuItemClickListener {
    class MenuItem(val id: Int, val title: String, val icon: Int? = null)

    abstract class SubFragment: Fragment() {
        var title: String = ""
            set(value: String) {
                field = value
                updateParentFragment()
            }
        var menuItems: List<MenuItem> = listOf()
            set(value: List<MenuItem>) {
                field = value
                updateParentFragment()
            }

        private fun updateParentFragment() {
            val parent = parentFragment
            if (parent is NavigationFragment)
                parent.configureToolbar(title, menuItems)
        }
    }

    private val toolbar by lazy { requireView().findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_container_with_toolbar, container, false)
    }

    abstract fun createInitialFragment(savedInstanceState: Bundle?): SubFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener {
            popFragment()
        }
        if (savedInstanceState == null) {
            replaceFragment(createInitialFragment(savedInstanceState))
        } else {
            configureToolbar(null, null, canPop())
        }
    }

    fun replaceFragment(fragment: SubFragment) {
        replace(fragment, R.id.fragment_container)
        configureToolbar(fragment.title, fragment.menuItems, false)
    }

    fun pushFragment(fragment: SubFragment) {
        push(fragment, R.id.fragment_container)
        configureToolbar(fragment.title, fragment.menuItems, true)
    }

    private fun configureToolbar(name: String?, menuItems: List<MenuItem>?, canGoBack: Boolean? = null) {
        if (name != null)
            toolbar.title = name

        if (canGoBack != null)
            toolbar.navigationIcon = if (canGoBack) ResourcesCompat.getDrawable(resources, R.drawable.ic_action_arrow_back, null) else null

        if (menuItems == null) return
        toolbar.menu.clear()
        for (menuItem in menuItems) {
            val item = toolbar.menu.add(Menu.NONE, menuItem.id, Menu.NONE, menuItem.title)
            val icon = menuItem.icon
            if (icon != null)
                item.setIcon(icon)
        }
    }

    override fun onMenuItemClick(item: android.view.MenuItem?): Boolean {
        if (item == null) return true
        return menuItemClicked(item.itemId)
    }

    open fun menuItemClicked(id: Int): Boolean {
        return true
    }

    override fun canPop(): Boolean {
        return childFragmentManager.backStackEntryCount > 0
    }

    override fun popLast() {
        popFragment()
    }

    private fun popFragment() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
        val frag = childFragmentManager.fragments[index]
        if (frag is SubFragment)
            configureToolbar(frag.title, frag.menuItems)
    }
}