/*
 * AsyncListFragment.kt
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.EndSubFragment
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.createLoadingDrawable
import space.celestia.mobilecelestia.utils.showAlert

abstract class AsyncListFragment<T: AsyncListItem>: EndSubFragment() {
    private var selectListener: Listener<T>? = null

    private var imageView: ImageView? = null
    private var refreshTextView: TextView? = null
    private var circularProgressDrawable: CircularProgressDrawable? = null

    private var items: List<T> = listOf()
    private val listAdapter by lazy { AsyncListAdapter(selectListener) }

    interface Listener<T> {
        fun onAsyncListItemSelected(item: T)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            val savedItems = savedInstanceState.getSerializable(ARG_LIST)

            if (savedItems != null)
                @Suppress("UNCHECKED_CAST")
                items = savedItems as List<T>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loading_grouped_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val imageView = view.findViewById<ImageView>(R.id.loading_image)
        val refresh = view.findViewById<TextView>(R.id.refresh)
        refresh.text = CelestiaString("Refresh", "")

        refresh.setOnClickListener {
            callRefresh()
        }

        val drawable = createLoadingDrawable(inflater.context)
        imageView.setImageDrawable(drawable)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = listAdapter
        this.imageView = imageView
        this.circularProgressDrawable = drawable
        this.refreshTextView = refresh

        if (items.size == 0) {
            callRefresh()
        } else {
            stopRefreshing(true)
            listAdapter.updateItems(items)
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener<*>) {
            @Suppress("UNCHECKED_CAST")
            selectListener = context as Listener<T>
        } else {
            throw RuntimeException("$context must implement AsyncListFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        selectListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(ARG_LIST, ArrayList(items))
    }

    private fun callRefresh() {
        startRefreshing()

        lifecycleScope.launch {
            try {
                val items = refresh()
                listAdapter.updateItems(items)
                listAdapter.notifyDataSetChanged()
                stopRefreshing(true)
            } catch (error: Throwable) {
                stopRefreshing(false)
                val message = defaultErrorMessage ?: error.message ?: return@launch
                activity?.showAlert(message)
            }
            val items = refresh()
        }
    }

    abstract val defaultErrorMessage: String?
    abstract suspend fun refresh(): List<T>

    fun startRefreshing() {
        imageView?.visibility = View.VISIBLE
        refreshTextView?.visibility = View.INVISIBLE
        circularProgressDrawable?.start()
    }

    fun stopRefreshing(success: Boolean) {
        imageView?.visibility = View.INVISIBLE
        refreshTextView?.visibility = if (success) View.INVISIBLE else View.VISIBLE
        circularProgressDrawable?.stop()
    }

    companion object {
        private const val ARG_LIST = "list"
    }
}