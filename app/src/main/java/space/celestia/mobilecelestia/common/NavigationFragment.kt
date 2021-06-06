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
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R

abstract class NavigationFragment: InsetAwareFragment(), Poppable, Toolbar.OnMenuItemClickListener {
    interface NavigationBarItem: Parcelable

    class BarButtonItem(val id: Int, val title: String?, val icon: Int? = null, val enabled: Boolean = true, val checked: Boolean? = null): NavigationBarItem {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readByte() != 0.toByte(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(title)
            parcel.writeValue(icon)
            parcel.writeByte(if (enabled) 1 else 0)
            parcel.writeValue(checked)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<BarButtonItem> {
            override fun createFromParcel(parcel: Parcel): BarButtonItem {
                return BarButtonItem(parcel)
            }

            override fun newArray(size: Int): Array<BarButtonItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    interface NavigationBarItemGroup: NavigationBarItem {
        val id: Int
        val title: String?
        val icon: Int?
        val items: List<BarButtonItem>
    }

    class BarButtonItemGroup(override val id: Int, override val title: String?, override val icon: Int? = null, override val items: List<BarButtonItem>): NavigationBarItemGroup {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.createTypedArrayList(BarButtonItem) ?: listOf()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(title)
            parcel.writeValue(icon)
            parcel.writeTypedList(items)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<BarButtonItemGroup> {
            override fun createFromParcel(parcel: Parcel): BarButtonItemGroup {
                return BarButtonItemGroup(parcel)
            }

            override fun newArray(size: Int): Array<BarButtonItemGroup?> {
                return arrayOfNulls(size)
            }
        }
    }

    class SingleChoiceItem(override val id: Int, override val title: String?, override val icon: Int? = null, override val items: List<BarButtonItem>): NavigationBarItemGroup {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.createTypedArrayList(BarButtonItem) ?: listOf()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(title)
            parcel.writeValue(icon)
            parcel.writeTypedList(items)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SingleChoiceItem> {
            override fun createFromParcel(parcel: Parcel): SingleChoiceItem {
                return SingleChoiceItem(parcel)
            }

            override fun newArray(size: Int): Array<SingleChoiceItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    class MultiChoiceItem(override val id: Int, override val title: String?, override val icon: Int? = null, override val items: List<BarButtonItem>): NavigationBarItemGroup {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.createTypedArrayList(BarButtonItem) ?: listOf()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(title)
            parcel.writeValue(icon)
            parcel.writeTypedList(items)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<MultiChoiceItem> {
            override fun createFromParcel(parcel: Parcel): MultiChoiceItem {
                return MultiChoiceItem(parcel)
            }

            override fun newArray(size: Int): Array<MultiChoiceItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    abstract class SubFragment: InsetAwareFragment() {
        var title: String
            get() = innerTitle
            set(value) {
                innerTitle = value
                updateParentFragment()
            }
        var rightNavigationBarItems: List<NavigationBarItem>
            get() = innerRightNavigationBarItems
            set(value) {
                innerRightNavigationBarItems = value
                updateParentFragment()
            }
        var leftNavigationBarItem: BarButtonItem?
            get() = innerLeftNavigationBarItem
            set(value) {
                innerLeftNavigationBarItem = value
                updateParentFragment()
            }
        var showNavigationBar: Boolean
            get() = innerShowNavigationBar
            set(value) {
                innerShowNavigationBar = value
                updateParentFragment()
            }

        private var innerTitle: String = ""
        private var innerRightNavigationBarItems: List<NavigationBarItem> = listOf()
        private var innerLeftNavigationBarItem: BarButtonItem? = null
        private var innerShowNavigationBar: Boolean = true

        private fun updateParentFragment() {
            val parent = parentFragment
            if (parent is NavigationFragment)
                parent.configureToolbar(title, rightNavigationBarItems, leftNavigationBarItem, parent.lastGoBack, showNavigationBar)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            if (savedInstanceState != null) {
                innerTitle = savedInstanceState.getString(ARG_TITLE, "")
                innerLeftNavigationBarItem = savedInstanceState.getParcelable(ARG_LEFT_ITEM)
                innerRightNavigationBarItems = savedInstanceState.getParcelableArrayList<NavigationBarItem>(ARG_RIGHT_ITEMS) ?: listOf()
                innerShowNavigationBar = savedInstanceState.getBoolean(ARG_SHOW_BAR)
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putString(ARG_TITLE, innerTitle)
            outState.putParcelable(ARG_LEFT_ITEM, innerLeftNavigationBarItem)
            outState.putParcelableArrayList(ARG_RIGHT_ITEMS, ArrayList(innerRightNavigationBarItems))
            outState.putBoolean(ARG_SHOW_BAR, innerShowNavigationBar)
            super.onSaveInstanceState(outState)
        }

        open fun menuItemClicked(groupId: Int, id: Int): Boolean {
            return true
        }
    }

    val toolbar by lazy { requireView().findViewById<Toolbar>(R.id.toolbar) }
    private val separator by lazy { requireView().findViewById<View>(R.id.separator) }

    private var lastTitle: String = ""
    private var lastLeftItem: BarButtonItem? = null
    private var lastRightItems: List<NavigationBarItem> = listOf()
    private var lastGoBack = false
    private var lastShowNavigationBar: Boolean = true

    val top: SubFragment?
        get() = childFragmentManager.findFragmentById(R.id.fragment_container) as? SubFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_container_with_toolbar, container, false)
    }

    abstract fun createInitialFragment(savedInstanceState: Bundle?): SubFragment

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_TITLE, lastTitle)
        outState.putParcelable(ARG_LEFT_ITEM, lastLeftItem)
        outState.putParcelableArrayList(ARG_RIGHT_ITEMS, ArrayList(lastRightItems))
        outState.putBoolean(ARG_BACK, lastGoBack)
        outState.putBoolean(ARG_SHOW_BAR, lastShowNavigationBar)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setOnMenuItemClickListener(this)
        if (savedInstanceState == null) {
            replaceFragment(createInitialFragment(savedInstanceState))
        } else {
            lastTitle = savedInstanceState.getString(ARG_TITLE, "")
            lastRightItems = savedInstanceState.getParcelableArrayList<NavigationBarItem>(ARG_RIGHT_ITEMS) ?: listOf()
            lastLeftItem = savedInstanceState.getParcelable(ARG_LEFT_ITEM)
            lastGoBack = savedInstanceState.getBoolean(ARG_BACK)
            lastShowNavigationBar = savedInstanceState.getBoolean(ARG_SHOW_BAR)
            configureToolbar(
                lastTitle,
                lastRightItems,
                lastLeftItem,
                lastGoBack,
                lastShowNavigationBar
            )
        }
    }

    fun replaceFragment(fragment: SubFragment) {
        replace(fragment, R.id.fragment_container)
        configureToolbar(fragment.title, fragment.rightNavigationBarItems, fragment.leftNavigationBarItem,false, fragment.showNavigationBar)
    }

    fun pushFragment(fragment: SubFragment) {
        push(fragment, R.id.fragment_container)
        configureToolbar(fragment.title, fragment.rightNavigationBarItems, fragment.leftNavigationBarItem, true, fragment.showNavigationBar)
    }

    private fun configureToolbar(name: String, rightNavigationBarItems: List<NavigationBarItem>, leftNavigationBarItem: BarButtonItem?, canGoBack: Boolean, showNavigationBar: Boolean) {
        toolbar.title = name
        toolbar.visibility = if (showNavigationBar) View.VISIBLE else  View.GONE
        separator.visibility = if (showNavigationBar) View.VISIBLE else  View.GONE

        toolbar.menu.clear()
        for (barItem in rightNavigationBarItems) {
            if (barItem is BarButtonItem) {
                val item = toolbar.menu.add(Menu.NONE, barItem.id, Menu.NONE, barItem.title)
                item.isEnabled = barItem.enabled
                item.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
                val icon = barItem.icon
                val checked = barItem.checked
                if (icon != null)
                    item.setIcon(icon)
                if (checked != null)
                    item.isChecked = checked
            } else if (barItem is NavigationBarItemGroup) {
                val subMenu = toolbar.menu.addSubMenu(barItem.id, Menu.NONE, Menu.NONE, barItem.title)
                val icon = barItem.icon
                if (icon != null)
                    subMenu.setIcon(icon)
                for (item in barItem.items) {
                    val subItem = subMenu.add(barItem.id, item.id, Menu.NONE, item.title)
                    subItem.isEnabled = item.enabled
                    val subIcon = item.icon
                    if (subIcon != null)
                        subItem.setIcon(subIcon)
                    if (barItem is SingleChoiceItem || barItem is MultiChoiceItem) {
                        subItem.isCheckable = true
                        subItem.isChecked = item.checked ?: false
                    }
                }
                if (barItem is SingleChoiceItem) {
                    subMenu.setGroupCheckable(barItem.id, true, true)
                } else if (barItem is MultiChoiceItem) {
                    subMenu.setGroupCheckable(barItem.id, true, false)
                } else {
                    subMenu.setGroupCheckable(barItem.id, false, false)
                }
            }
        }

        if (leftNavigationBarItem?.icon != null) {
            toolbar.setNavigationIcon(leftNavigationBarItem.icon)
            toolbar.setNavigationOnClickListener {
                menuItemClicked(Menu.NONE, leftNavigationBarItem.id)
            }
        } else {
            if (canGoBack) {
                toolbar.setNavigationIcon(R.drawable.ic_action_back_tint)
            } else {
                toolbar.navigationIcon = null
            }
            toolbar.setNavigationOnClickListener {
                popFragment()
            }
        }

        lastRightItems = rightNavigationBarItems
        lastLeftItem = leftNavigationBarItem
        lastTitle = name
        lastGoBack = canGoBack
        lastShowNavigationBar = showNavigationBar
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item == null) return true
        return menuItemClicked(item.groupId,  item.itemId)
    }

    private fun menuItemClicked(groupId: Int, id: Int): Boolean {
        return top?.menuItemClicked(groupId, id) ?: true
    }

    override fun canPop(): Boolean {
        if (!isAdded()) return false
        return childFragmentManager.backStackEntryCount > 0
    }

    override fun popLast() {
        if (canPop()) {
            popFragment()
        }
    }

    private fun popFragment() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
            lastGoBack = false
        }
        val frag = childFragmentManager.fragments[index]
        if (frag is SubFragment)
            configureToolbar(frag.title, frag.rightNavigationBarItems, frag.leftNavigationBarItem, lastGoBack, frag.showNavigationBar)
    }

    private companion object {
        const val ARG_LEFT_ITEM = "left"
        const val ARG_RIGHT_ITEMS = "right"
        const val ARG_TITLE = "title"
        const val ARG_BACK = "back"
        const val ARG_SHOW_BAR = "show-bar"
    }
}