/*
 * ResourceFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.TitledFragment
import space.celestia.mobilecelestia.common.pop
import space.celestia.mobilecelestia.common.push
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.favorite.FavoriteFragment
import space.celestia.mobilecelestia.favorite.MutableFavoriteBaseItem
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.CelestiaString

class ResourceFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val toolbar by lazy { requireView().findViewById<Toolbar>(R.id.toolbar) }
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_container_with_toolbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener {
            popItem()
        }
        val fragment = ResourceCategoryListFragment.newInstance()
        replace(fragment, R.id.fragment_container)
        toolbar.title = fragment.title
        showInitialToolbar()
    }

    private fun replaceItem(item: ResourceCategory) {
        toolbar.title = item.name
        replace(ResourceItemListFragment.newInstance(item), R.id.fragment_container)
        showInitialToolbar()
    }

    fun pushItem(item: ResourceCategory) {
        val frag = ResourceItemListFragment.newInstance(item)
        push(frag, R.id.fragment_container)
        toolbar.title = item.name
        showDetailToolbar()
    }

    fun pushItem(item: ResourceItem) {
        val frag = ResourceItemFragment.newInstance(item)
        push(frag, R.id.fragment_container)
        toolbar.title = ""
        showDetailToolbar()
    }

    private fun showInitialToolbar() {
        toolbar.menu.clear()
        toolbar.navigationIcon = null
        toolbar.menu.add(Menu.NONE, MENU_ITEM_MANAGE_INSTALLED, Menu.NONE, "Installed")
    }

    private fun showDetailToolbar() {
        toolbar.menu.clear()
        toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_arrow_back, null)
    }

    private fun popItem() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            showInitialToolbar()
        }
        val frag = childFragmentManager.fragments[index] as TitledFragment
        toolbar.title = frag.title
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item == null) return true
        when (item.itemId) {
            MENU_ITEM_MANAGE_INSTALLED -> {
                val fragment = InstalledResourceListFragment.newInstance()
                push(fragment, R.id.fragment_container)
                toolbar.title = fragment.title
                showDetailToolbar()
            } else -> {}
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ResourceFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
    }

    companion object {
        fun newInstance() = ResourceFragment()

        private const val MENU_ITEM_MANAGE_INSTALLED = 0
    }
}
