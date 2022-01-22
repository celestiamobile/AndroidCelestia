/*
 * SettingsLanguageRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.utils.CelestiaString
import java.util.*
import kotlin.collections.ArrayList

interface LanguageItem : RecyclerViewItem {
    val title: String
    val subtitle: String?
}

private fun getLocale(locale: String): Locale {
    val components = locale.split("_")
    if (components.size == 1)
        return Locale(components[0])
    if (components.size == 2)
        return Locale(components[0], components[1])
    return Locale(components[0], components[1], components[2])
}

// Here we avoid using country in locales related to Chinese
private var substitutionList = mapOf(
    "zh_CN" to "zh-Hans",
    "zh_TW" to "zh-Hant"
)

private fun getLocalizedLanguageName(locale: String): String {
    val lang = substitutionList[locale] ?: locale
    val loc1 = Locale.forLanguageTag(lang)
    val name1 = loc1.getDisplayName(loc1)
    if (name1.isNotEmpty())
        return name1
    val loc2 = getLocale(lang)
    return loc2.getDisplayName(loc2)
}

class SpecificLanguageItem(val language: String) : LanguageItem {
    override val title: String
        get() = getLocalizedLanguageName(language)

    override val subtitle: String?
        get() = null
}

class CurrentLanguageItem(private val current: String?) : LanguageItem {
    override val title: String
        get() = CelestiaString("Current Language", "")
    override val subtitle: String?
        get() = current

    override val clickable: Boolean
        get() = false
}

class ResetLanguageItem : LanguageItem {
    override val title: String
        get() = CelestiaString("Reset to Default", "")
    override val subtitle: String?
        get() = null
}

class SettingsLanguageRecyclerViewAdapter(
    private val listener: SettingsLanguageFragment.Listener?,
    private val dataSource: SettingsLanguageFragment.DataSource?
) : SeparatorHeaderRecyclerViewAdapter(listOf()) {

    init {
        reload()
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is ResetLanguageItem) {
            listener?.onSetOverrideLanguage(null)
        } else if (item is SpecificLanguageItem) {
            listener?.onSetOverrideLanguage(item.language)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SpecificLanguageItem)
            return LANG_ITEM
        if (item is LanguageItem)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM)
            return CommonTextViewHolder(parent)
        if (viewType == LANG_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            val value = TypedValue()
            parent.context.theme.resolveAttribute(android.R.attr.colorPrimary, value, true)
            ImageViewCompat.setImageTintList(holder.accessory, ColorStateList.valueOf(value.data))
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SpecificLanguageItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.accessory.visibility = if (dataSource?.currentOverrideLanguage() == item.language) View.VISIBLE else View.GONE
            return
        }
        if (item is LanguageItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = item.subtitle
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        val sections = ArrayList<CommonSectionV2>()
        val currentLang = dataSource?.currentLanguage() ?: "en"
        sections.add(CommonSectionV2((listOf(CurrentLanguageItem(getLocalizedLanguageName(currentLang))))))
        val languages = dataSource?.availableLanguages() ?: listOf()
        sections.add(CommonSectionV2(languages.map { SpecificLanguageItem(it) }))
        sections.add(CommonSectionV2(listOf(ResetLanguageItem()), footer =  CelestiaString("Configuration will take effect after a restart.", "")))
        updateSectionsWithHeader(sections)
    }

    private companion object {
        const val SETTING_ITEM = 0
        const val LANG_ITEM    = 1
    }
}
