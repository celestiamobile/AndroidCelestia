// SettingsPushNotificationsScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.Footer
import space.celestia.celestiaui.compose.SwitchRow
import space.celestia.celestiaui.settings.viewmodel.SettingsViewModel
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager

private enum class PushPermissionState { Loading, Granted, Denied }

@Composable
fun SettingsPushNotificationsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    var permissionState by remember { mutableStateOf(PushPermissionState.Loading) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.appSettings[PreferenceManager.PredefinedKey.PushNotificationsAsked] = "true"
        if (granted) {
            // Permission granted via this screen → enable all types by default.
            viewModel.appSettings.startEditing()
            viewModel.appSettings[PreferenceManager.PredefinedKey.PushWeeklyAddon] = "true"
            viewModel.appSettings[PreferenceManager.PredefinedKey.PushLatestNews] = "true"
            viewModel.appSettings[PreferenceManager.PredefinedKey.PushFeaturedAddon] = "true"
            viewModel.appSettings.stopEditing()
            permissionState = PushPermissionState.Granted
            scope.launch { viewModel.pushNotificationRegistrar.register() }
        } else {
            permissionState = PushPermissionState.Denied
        }
    }

    fun checkPermission(): PushPermissionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return PushPermissionState.Granted
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return if (granted) PushPermissionState.Granted else PushPermissionState.Denied
    }

    LaunchedEffect(Unit) {
        val initial = checkPermission()
        if (initial == PushPermissionState.Granted) {
            permissionState = PushPermissionState.Granted
        } else {
            // Permission not yet granted. Request it; the system silently
            // delivers a "denied" callback in the permanently-denied case,
            // which falls through to the Denied UI.
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Re-check on resume so coming back from system settings reflects the new state.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionState = checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (permissionState) {
        PushPermissionState.Loading -> Unit
        PushPermissionState.Granted -> GrantedContent(paddingValues, viewModel, scope)
        PushPermissionState.Denied -> DeniedContent(paddingValues, context)
    }
}

@Composable
private fun GrantedContent(paddingValues: PaddingValues, viewModel: SettingsViewModel, scope: kotlinx.coroutines.CoroutineScope) {
    var weeklyAddon by remember {
        mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.PushWeeklyAddon] != "false")
    }
    var latestNews by remember {
        mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.PushLatestNews] != "false")
    }
    var featuredAddon by remember {
        mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.PushFeaturedAddon] != "false")
    }

    LazyColumn(
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = paddingValues
    ) {
        item { Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short))) }
        item {
            SwitchRow(
                primaryText = CelestiaString("Weekly Add-on", "Weekly add-on push notification setting"),
                checked = weeklyAddon,
                onCheckedChange = { weeklyAddon = it }
            )
        }
        item {
            SwitchRow(
                primaryText = CelestiaString("Latest News", "Latest news push notification setting"),
                checked = latestNews,
                onCheckedChange = { latestNews = it }
            )
        }
        item {
            SwitchRow(
                primaryText = CelestiaString("Featured Add-on", "Featured add-on push notification setting"),
                checked = featuredAddon,
                onCheckedChange = { featuredAddon = it }
            )
        }
        item { Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall))) }
        item {
            FilledTonalButton(
                onClick = {
                    viewModel.appSettings.startEditing()
                    viewModel.appSettings[PreferenceManager.PredefinedKey.PushWeeklyAddon] = if (weeklyAddon) "true" else "false"
                    viewModel.appSettings[PreferenceManager.PredefinedKey.PushLatestNews] = if (latestNews) "true" else "false"
                    viewModel.appSettings[PreferenceManager.PredefinedKey.PushFeaturedAddon] = if (featuredAddon) "true" else "false"
                    viewModel.appSettings.stopEditing()
                    scope.launch { viewModel.pushNotificationRegistrar.register() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal))
            ) {
                Text(CelestiaString("Save", "Save button on push notifications screen"))
            }
        }
    }
}

@Composable
private fun DeniedContent(paddingValues: PaddingValues, context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        Footer(
            text = CelestiaString(
                "Notifications are disabled. Enable them in System Settings to receive push notifications from Celestia.",
                "Push notifications denied explanation"
            )
        )
        FilledTonalButton(onClick = {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
                    Settings.EXTRA_APP_PACKAGE, context.packageName
                )
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            context.startActivity(intent)
        }) {
            Text(CelestiaString("Open System Settings", "Button to open system notification settings"))
        }
    }
}
