// LoadingFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.loading

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.AppStatusReporter
import javax.inject.Inject

@AndroidEntryPoint
class LoadingFragment : Fragment(), AppStatusReporter.Listener {

    @Inject
    lateinit var appStatusReporter: AppStatusReporter

    private var loadingLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appStatusReporter.register(this)
    }

    override fun onDestroy() {
        appStatusReporter.unregister(this)

        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_loading, container, false)
        loadingLabel = view.findViewById(R.id.loading_label)
        loadingLabel?.text = appStatusReporter.status
        return view
    }

    override fun celestiaLoadingProgress(status: String) {
        lifecycleScope.launch {
            Log.d(TAG, status)
            loadingLabel?.text = status
        }
    }

    override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {}

    companion object {
        private const val TAG = "LoadingFragment"

        fun newInstance() = LoadingFragment()
    }
}
