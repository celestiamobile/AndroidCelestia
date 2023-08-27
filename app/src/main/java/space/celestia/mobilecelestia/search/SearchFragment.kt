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

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import java.lang.ref.WeakReference
import javax.inject.Inject

@ExperimentalCoroutinesApi
fun EditText.textChanges(): Flow<String> {
    return callbackFlow {
        val listener = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                trySend(p0?.toString() ?: "")
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }
}

@AndroidEntryPoint
class SearchFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private val listAdapter by lazy { SearchRecyclerViewAdapter(listener) }

    private lateinit var searchView: SearchView
    private lateinit var searchBar: SearchBar
    private lateinit var listView: RecyclerView

    private var searchKey: String = ""
    private var searchResults = listOf<String>()

    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var executor: CelestiaExecutor

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
        searchBar = view.findViewById(R.id.search_bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        // Set the adapter
        listAdapter.updateSearchResults(searchKey, searchResults)
        listView = view.findViewById(R.id.list)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = listAdapter

        // layout_behavior and layout_anchor do not work well together, so we cannot do edge to edge here...
        // listView.clipToPadding = false
        // listView.fitsSystemWindows = true

        return view
    }

    @FlowPreview
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar.text = searchKey
        searchView.setText(searchKey)
        searchView.editText.hint = ""
        searchView.editText.imeOptions =
            EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN

        setupSearchSearchView()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setupSearchSearchView() {
        searchView.editText.textChanges()
            .distinctUntilChanged()
            .debounce(300)
            .mapLatest { key ->
                val result = if (key.isEmpty()) listOf<String>() else withContext(executor.asCoroutineDispatcher()) { appCore.simulation.completionForText(key, SEARCH_RESULT_LIMIT) }
                Pair(key, result)
            }
            .onEach {
                withContext(Dispatchers.Main) {
                    searchKey = it.first
                    searchResults = it.second
                    listAdapter.updateSearchResults(searchKey, searchResults)
                    listAdapter.notifyDataSetChanged()
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(lifecycleScope)

        val weakSelf = WeakReference(this)
        searchView.addTransitionListener { _, _, newState ->
            val self = weakSelf.get() ?: return@addTransitionListener
            if (newState == SearchView.TransitionState.HIDING || newState == SearchView.TransitionState.HIDDEN) {
                self.searchBar.text = self.searchView.text
            }
        }
        searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            val self = weakSelf.get() ?: return@setOnEditorActionListener false
            val searchKey = self.searchView.text
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = searchKey?.toString()
                if (!searchText.isNullOrEmpty() && searchResults.isEmpty()) {
                    listener?.onSearchItemSubmit(searchText)
                }
            }
            return@setOnEditorActionListener false
        }
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