/*
 * SearchFragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.search.model.RxSearchObservable
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    private var listener: Listener? = null
    private val listAdapter by lazy { SearchRecyclerViewAdapter(listener) }

    private val compositeDisposable = CompositeDisposable()

    private var searchView: SearchView? = null

    private var lastSearchText: String = ""
    private var lastSearchResultCount: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_item_list, container, false)

        view.findViewById<View>(R.id.search_container).setOnTouchListener { _, _ -> true }
        val searchView = view.findViewById<SearchView>(R.id.search)
        searchView.setOnClickListener {
            searchView.isIconified = false
        }
        this.searchView = searchView
        setupSearchSearchView()

        // Set the adapter
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
        return view
    }

    private fun setupSearchSearchView() {
        val searchView = this.searchView ?: return
        val disposable = RxSearchObservable.fromView(searchView)
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .map {
                if (it.isEmpty()) { return@map Pair("", listOf<String>()) }
                val core = CelestiaAppCore.shared()
                return@map Pair(it, core.simulation.completionForText(it, SEARCH_RESULT_LIMIT))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                lastSearchText = it.first
                lastSearchResultCount = it.second.size
                listAdapter.updateSearchResults(it.second)
                listAdapter.notifyDataSetChanged()
            }, {}, {
                // Submit, clear focus
                searchView.clearFocus()
                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(searchView.windowToken, 0)
                setupSearchSearchView()

                if (!lastSearchText.isEmpty() && lastSearchResultCount == 0) {
                    listener?.onSearchItemSubmit(lastSearchText)
                }

                lastSearchText = ""
                lastSearchResultCount = 0
            })
        compositeDisposable.add(disposable)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SearchFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        compositeDisposable.dispose()

        super.onDestroy()
    }

    interface Listener {
        fun onSearchItemSelected(text: String)
        fun onSearchItemSubmit(text: String)
    }

    companion object {
        const val SEARCH_RESULT_LIMIT = 20

        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
