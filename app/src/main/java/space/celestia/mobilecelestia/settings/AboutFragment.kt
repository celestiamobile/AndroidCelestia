/*
 * AboutFragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.TitledFragment
import space.celestia.mobilecelestia.utils.AssetUtils
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.versionCode
import space.celestia.mobilecelestia.utils.versionName

enum class AboutAction {
    VisitOfficialWebsite, VisitOfficialForum;
}

class AboutFragment : TitledFragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = AboutRecyclerViewAdapter(createAboutItems(), listener)
            }
        }
        return view
    }

    private fun createAboutItems(): List<List<AboutItem>> {
        val array = ArrayList<List<AboutItem>>()

        // Version
        val ctx = context
        var versionName = "Unknown"
        if (ctx != null)
            versionName = "${ctx.versionName}(${ctx.versionCode})"

        array.add(listOf(
            VersionItem(versionName)
        ))

        // Authors
        getInfo("CelestiaResources/AUTHORS", CelestiaString("Authors", ""))?.let {
            array.add(it)
        }

        // Translators
        getInfo("CelestiaResources/TRANSLATORS", CelestiaString("Translators", ""))?.let {
            array.add(it)
        }

        // Links
        array.add(
            listOf(
                ActionItem(AboutAction.VisitOfficialWebsite),
                ActionItem(AboutAction.VisitOfficialForum)
            )
        )

        return array
    }

    private fun getInfo(assetPath: String, title: String): List<AboutItem>? {
        val ctx = context ?: return null
        try {
            val info = AssetUtils.readFileToText(ctx, assetPath)
            return listOf(
                TitleItem(title),
                DetailItem(info)
            )
        } catch (_: Exception) {}
        return null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AboutFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onAboutActionSelected(action: AboutAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutFragment()
    }
}
