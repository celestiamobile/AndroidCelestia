// NavigationFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import space.celestia.mobilecelestia.R
import java.lang.ref.WeakReference

abstract class NavigationFragment: Fragment(), Toolbar.OnMenuItemClickListener {
    interface NavigationBarItem: Parcelable

    class BarButtonItem(val id: Int, val title: String?, val icon: Int? = null, val enabled: Boolean = true, val checked: Boolean? = null): NavigationBarItem {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readByte() != 0.toByte(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean)

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
            parcel.createTypedArrayList(BarButtonItem) ?: listOf())

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
            parcel.createTypedArrayList(BarButtonItem) ?: listOf())

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
            parcel.createTypedArrayList(BarButtonItem) ?: listOf())

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

    abstract class SubFragment: Fragment() {
        var title: String
            get() = innerTitle
            set(value) {
                if (innerTitle != value) {
                    innerTitle = value
                    updateParentFragment()
                }
            }
        var rightNavigationBarItems: List<NavigationBarItem>
            get() = innerRightNavigationBarItems
            set(value) {
                if (innerRightNavigationBarItems != value) {
                    innerRightNavigationBarItems = value
                    updateParentFragment()
                }
            }
        var leftNavigationBarItem: BarButtonItem?
            get() = innerLeftNavigationBarItem
            set(value) {
                if (innerLeftNavigationBarItem != value) {
                    innerLeftNavigationBarItem = value
                    updateParentFragment()
                }
            }
        var showNavigationBar: Boolean
            get() = innerShowNavigationBar
            set(value) {
                if (innerShowNavigationBar != value) {
                    innerShowNavigationBar = value
                    updateParentFragment()
                }
            }

        private var innerTitle: String = ""
        private var innerRightNavigationBarItems: List<NavigationBarItem> = listOf()
        private var innerLeftNavigationBarItem: BarButtonItem? = null
        private var innerShowNavigationBar: Boolean = true

        private fun updateParentFragment() {
            val parent = parentFragment
            if (parent !is NavigationFragment) return
            if (parent.top != this) return
            parent.configureToolbar(title, rightNavigationBarItems, leftNavigationBarItem, parent.lastGoBack, showNavigationBar)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            if (savedInstanceState != null) {
                innerTitle = savedInstanceState.getString(ARG_TITLE, "")
                innerLeftNavigationBarItem = BundleCompat.getParcelable(savedInstanceState, ARG_LEFT_ITEM, BarButtonItem::class.java)
                innerRightNavigationBarItems = BundleCompat.getParcelableArrayList(savedInstanceState, ARG_RIGHT_ITEMS, BarButtonItem::class.java) ?: listOf()
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

    private lateinit var toolbar: Toolbar
    private lateinit var appBar: AppBarLayout

    private var lastTitle: String = ""
    private var lastLeftItem: BarButtonItem? = null
    private var lastRightItems: List<NavigationBarItem> = listOf()
    private var lastGoBack = false
    private var lastShowNavigationBar: Boolean = true
    private var commitIds = arrayListOf<Int>()
    private var fragmentCreated = false
    private var isBackStackChangeInProgress = false
    private var isPredictiveBack = false

    open val fragmentResource: Int
        get() = R.layout.fragment_general_container_with_toolbar

    val top: SubFragment?
        get() = childFragmentManager.findFragmentById(R.id.fragment_container) as? SubFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weakSelf = WeakReference(this)
        childFragmentManager.addOnBackStackChangedListener(object: FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
                val self = weakSelf.get() ?: return
                if (self.isBackStackChangeInProgress) {
                    self.isPredictiveBack = true
                } else {
                    val backEntryCount = self.childFragmentManager.backStackEntryCount
                    if (backEntryCount < self.commitIds.size - 1)
                        self.poppedToIndex(backEntryCount)
                }
            }

            override fun onBackStackChangeStarted(fragment: Fragment, pop: Boolean) {
                super.onBackStackChangeStarted(fragment, pop)
                val self = weakSelf.get() ?: return
                self.isBackStackChangeInProgress = true
                self.isPredictiveBack = false
            }

            override fun onBackStackChangeCommitted(fragment: Fragment, pop: Boolean) {
                super.onBackStackChangeCommitted(fragment, pop)
                val self = weakSelf.get() ?: return
                if (isPredictiveBack) {
                    val backEntryCount = self.childFragmentManager.backStackEntryCount - 1
                    if (backEntryCount < self.commitIds.size - 1)
                        self.poppedToIndex(backEntryCount)
                }
                self.isBackStackChangeInProgress = false
            }

            override fun onBackStackChangeCancelled() {
                super.onBackStackChangeCancelled()
                val self = weakSelf.get() ?: return
                self.isBackStackChangeInProgress = false
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(fragmentResource, container, false)
        toolbar = view.findViewById(R.id.toolbar)
        appBar = view.findViewById(R.id.appbar)
        return view
    }

    abstract fun createInitialFragment(savedInstanceState: Bundle?): SubFragment

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_TITLE, lastTitle)
        outState.putParcelable(ARG_LEFT_ITEM, lastLeftItem)
        outState.putParcelableArrayList(ARG_RIGHT_ITEMS, ArrayList(lastRightItems))
        outState.putBoolean(ARG_BACK, lastGoBack)
        outState.putBoolean(ARG_SHOW_BAR, lastShowNavigationBar)
        outState.putIntegerArrayList(ARG_COMMIT_IDS, commitIds)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setOnMenuItemClickListener(this)
        if (fragmentCreated) {
            configureToolbarByCurrentFragment()
        } else if (savedInstanceState == null) {
            replaceFragment(createInitialFragment(savedInstanceState))
            fragmentCreated = true
        } else {
            lastTitle = savedInstanceState.getString(ARG_TITLE, "")
            lastRightItems = BundleCompat.getParcelableArrayList(savedInstanceState, ARG_RIGHT_ITEMS, BarButtonItem::class.java) ?: listOf()
            lastLeftItem = BundleCompat.getParcelable(savedInstanceState, ARG_LEFT_ITEM, BarButtonItem::class.java)
            lastGoBack = savedInstanceState.getBoolean(ARG_BACK)
            lastShowNavigationBar = savedInstanceState.getBoolean(ARG_SHOW_BAR)
            commitIds = savedInstanceState.getIntegerArrayList(ARG_COMMIT_IDS) ?: arrayListOf()
            configureToolbar(
                lastTitle,
                lastRightItems,
                lastLeftItem,
                lastGoBack,
                lastShowNavigationBar
            )
            appBar.setExpanded(true)
            fragmentCreated = true
        }
    }

    private fun replaceFragment(fragment: SubFragment) {
        val commitId = replace(fragment, R.id.fragment_container, true) ?: return
        commitIds = arrayListOf(commitId)
        configureToolbar(fragment.title, fragment.rightNavigationBarItems, fragment.leftNavigationBarItem,false, fragment.showNavigationBar)
        appBar.setExpanded(true)
    }

    fun pushFragment(fragment: SubFragment) {
        val commitId = push(fragment, R.id.fragment_container) ?: return
        commitIds.add(commitId)
        configureToolbar(fragment.title, fragment.rightNavigationBarItems, fragment.leftNavigationBarItem, true, fragment.showNavigationBar)
        appBar.setExpanded(true)
    }

    private fun configureToolbar(name: String, rightNavigationBarItems: List<NavigationBarItem>, leftNavigationBarItem: BarButtonItem?, canGoBack: Boolean, showNavigationBar: Boolean) {
        toolbar.title = name
        toolbar.visibility = if (showNavigationBar) View.VISIBLE else  View.GONE

        toolbar.menu.clear()
        for (barItem in rightNavigationBarItems) {
            if (barItem is BarButtonItem) {
                val item = toolbar.menu.add(Menu.NONE, barItem.id, Menu.NONE, barItem.title)
                item.isEnabled = barItem.enabled
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
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
                toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back)
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

    private fun popFragment() {
        childFragmentManager.popBackStack()
    }

    private fun poppedToIndex(index: Int) {
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
            lastGoBack = false
        }
        commitIds = ArrayList(commitIds.subList(0, index + 1))
        configureToolbarByCurrentFragment()
    }

    private fun configureToolbarByCurrentFragment() {
        val frag = top ?: return
        configureToolbar(
            frag.title,
            frag.rightNavigationBarItems,
            frag.leftNavigationBarItem,
            lastGoBack,
            frag.showNavigationBar
        )
        appBar.setExpanded(true)
    }

    private companion object {
        const val ARG_LEFT_ITEM = "left"
        const val ARG_RIGHT_ITEMS = "right"
        const val ARG_TITLE = "title"
        const val ARG_BACK = "back"
        const val ARG_SHOW_BAR = "show-bar"
        const val ARG_COMMIT_IDS = "commit-ids"
    }
}