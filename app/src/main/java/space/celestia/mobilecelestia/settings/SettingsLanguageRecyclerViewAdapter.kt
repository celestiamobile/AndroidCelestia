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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.utils.CelestiaString
import java.lang.ref.WeakReference
import java.util.*

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

class LanguageRadioItem(val languages: List<String>, val selected: String?) : RecyclerViewItem {
    override val clickable: Boolean
        get() = false
}

class CurrentLanguageItem(private val current: String?) : RecyclerViewItem {
    val title: String
        get() = CelestiaString("Current Language", "")
    val subtitle: String?
        get() = current

    override val clickable: Boolean
        get() = false
}

class ResetLanguageItem : RecyclerViewItem {
    val title: String
        get() = CelestiaString("Reset to Default", "")
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
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is LanguageRadioItem)
            return LANG_SELECTION_ITEM
        if (item is CurrentLanguageItem)
            return CURRENT_ITEM
        if (item is ResetLanguageItem)
            return ACTION_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ACTION_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.title.setTextColor(parent.context.getPrimaryColor())
            return holder
        }
        if (viewType == CURRENT_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.detail.visibility = View.VISIBLE
            return holder
        }
        if (viewType == LANG_SELECTION_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_radio_button_item, parent,false)
            return RadioButtonViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is ResetLanguageItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            return
        }
        if (item is CurrentLanguageItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.text = item.subtitle
            return
        }
        if (item is LanguageRadioItem && holder is RadioButtonViewHolder) {
            val weakSelf = WeakReference(this)
            holder.configure(text = "", showTitle = false, options = item.languages.map { getLocalizedLanguageName(it) }, checkedIndex = item.languages.indexOfFirst { it == item.selected }) { newIndex ->
                val self = weakSelf.get() ?: return@configure
                self.listener?.onSetOverrideLanguage(item.languages[newIndex])
            }
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        val sections = ArrayList<CommonSectionV2>()
        val currentLang = dataSource?.currentLanguage() ?: "en"
        sections.add(CommonSectionV2((listOf(CurrentLanguageItem(getLocalizedLanguageName(currentLang))))))
        val languages = dataSource?.availableLanguages() ?: listOf()
        sections.add(CommonSectionV2(listOf(LanguageRadioItem(languages, dataSource?.currentOverrideLanguage()))))
        sections.add(CommonSectionV2(listOf(ResetLanguageItem()), footer =  CelestiaString("Configuration will take effect after a restart.", "")))
        updateSectionsWithHeader(sections)
    }

    private companion object {
        const val ACTION_ITEM = 0
        const val CURRENT_ITEM    = 1
        const val LANG_SELECTION_ITEM   = 2
    }
}
