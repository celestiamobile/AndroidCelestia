/*
 * AboutFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
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
import space.celestia.mobilecelestia.common.EndSubFragment
import space.celestia.mobilecelestia.utils.AssetUtils
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.versionCode
import space.celestia.mobilecelestia.utils.versionName

class AboutFragment : EndSubFragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = AboutRecyclerViewAdapter(createAboutItems(), listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = CelestiaString("About", "")
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
                ActionItem(CelestiaString("Development", ""),"https://github.com/levinli303/Celestia/wiki/Development"),
                ActionItem(CelestiaString("Third Party Dependencies", ""), "https://github.com/levinli303/Celestia/wiki/Third-Party-Dependencies"),
                ActionItem(CelestiaString("Privacy Policy and Service Agreement", ""), "https://celestia.mobi/privacy.html")
            )
        )

        array.add(
            listOf(
                ActionItem(CelestiaString("Official Website", ""), "https://celestia.mobi"),
                ActionItem(CelestiaString("Support Forum", ""), "https://celestia.space/forum")
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
        fun onAboutURLSelected(url: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutFragment()
    }
}
