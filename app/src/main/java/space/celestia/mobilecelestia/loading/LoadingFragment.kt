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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.loading.viewmodel.LoadingViewModel
import space.celestia.mobilecelestia.utils.AppStatusReporter


@Composable
private fun LoadingScreen() {
    val viewModel: LoadingViewModel = hiltViewModel()
    val lifeCycleOwner = LocalLifecycleOwner.current

    var statusText by rememberSaveable { mutableStateOf(viewModel.appStatusReporter.status) }
    val scope = rememberCoroutineScope()

    DisposableEffect(lifeCycleOwner) {
        val observer = object: AppStatusReporter.Listener {
            override fun celestiaLoadingProgress(status: String) {
                scope.launch {
                    statusText = status
                }
            }

            override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {}
        }
        viewModel.appStatusReporter.register(observer)
        onDispose {
            viewModel.appStatusReporter.unregister(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.common_page_medium_margin_horizontal)).background(color = MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.loading_gap_vertical))) {
            Image(painter = painterResource(R.drawable.loading_icon), contentDescription = null, modifier = Modifier.size(dimensionResource(R.dimen.app_icon_dimension)))
            Text(text = statusText, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@AndroidEntryPoint
class LoadingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    LoadingScreen()
                }
            }
        }
    }

    companion object {
        fun newInstance() = LoadingFragment()
    }
}
