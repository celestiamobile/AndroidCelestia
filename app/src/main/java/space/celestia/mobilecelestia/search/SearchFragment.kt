/*
 * SearchFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestia.AppCore
import javax.inject.Inject

@ExperimentalCoroutinesApi
fun SearchView.textChanges(): Flow<Pair<String, Boolean>> {
    return callbackFlow {
        val listener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                trySend(Pair(newText ?: "", false))
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                trySend(Pair(query ?: "", true))
                return true
            }
        }
        setOnQueryTextListener(listener)
        awaitClose { setOnQueryTextListener(null) }
    }
}

@AndroidEntryPoint
class SearchFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private val listAdapter by lazy { SearchRecyclerViewAdapter(listener) }

    private lateinit var searchView: SearchView
    private lateinit var listView: RecyclerView

    private var searchKey: String = ""
    private var searchResults = listOf<String>()

    @Inject
    lateinit var appCore: AppCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            searchKey = savedInstanceState.getString(SEARCH_KEY_ARG, "")
            searchResults = savedInstanceState.getStringArrayList(SEARCH_RESULTS_ARG) ?: listOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_item_list, container, false)

        searchView = view.findViewById(R.id.search_view)
        searchView.setQuery(searchKey, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        // Set the adapter
        listAdapter.updateSearchResults(searchResults)
        listView = view.findViewById(R.id.list)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = listAdapter
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setBackground(
            searchView.findViewById(R.id.search_plate),
            ColorDrawable(Color.TRANSPARENT)
        )
        searchView.findViewById<EditText>(R.id.search_src_text).hint = ""
        searchView.imeOptions =
            EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
        searchView.setIconifiedByDefault(false)

        setupSearchSearchView()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setupSearchSearchView() {
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as? SearchView)?.isIconified = false
            }
        }
        searchView.textChanges()
            .distinctUntilChanged()
            .debounce(300)
            .mapLatest {
                val key = it.first
                val result = if (key.isEmpty()) listOf<String>() else appCore.simulation.completionForText(key, SEARCH_RESULT_LIMIT)
                Triple(key, result, it.second)
            }
            .onEach {
                withContext(Dispatchers.Main) {
                    searchKey = it.first
                    searchResults = it.second
                    listAdapter.updateSearchResults(searchResults)
                    listAdapter.notifyDataSetChanged()

                    if (!it.third) return@withContext

                    // Submit, clear focus
                    activity?.hideKeyboard()
                    searchView.clearFocus()
                    setupSearchSearchView()

                    if (searchKey.isNotEmpty() && searchResults.isEmpty()) {
                        listener?.onSearchItemSubmit(searchKey)
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_KEY_ARG, searchKey)
        outState.putStringArrayList(SEARCH_RESULTS_ARG, ArrayList(searchResults))
        super.onSaveInstanceState(outState)
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

    interface Listener {
        fun onSearchItemSelected(text: String)
        fun onSearchItemSubmit(text: String)
    }

    companion object {
        private const val SEARCH_RESULT_LIMIT = 20

        private const val SEARCH_KEY_ARG = "search-key"
        private const val SEARCH_RESULTS_ARG = "search-results"

        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}

fun Activity.hideKeyboard() {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}