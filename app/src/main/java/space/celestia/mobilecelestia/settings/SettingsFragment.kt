/*
 * SettingsFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import RenderInfoScreen
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.purchase.FontSettingsScreen
import space.celestia.mobilecelestia.purchase.SubscriptionBackingScreen
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import kotlin.reflect.typeOf

inline fun <reified T : Any> serializableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String) =
        bundle.getString(key)?.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, json.encodeToString(value))
    }
}

@Serializable
sealed class Settings {
    abstract val name: String

    @Serializable
    data object Home : Settings() {
        override val name: String
            get() = CelestiaString("Settings", "")
    }

    @Serializable
    data object About : Settings() {
        override val name: String
            get() = CelestiaString("About", "About Celstia...")
    }

    @Serializable
    data object RefreshRate : Settings() {
        override val name: String
            get() = CelestiaString("Frame Rate", "Frame rate of simulation")
    }

    @Serializable
    data object CurrentTime : Settings() {
        override val name: String
            get() = CelestiaString("Current Time", "")
    }

    @Serializable
    data object DataLocation : Settings() {
        override val name: String
            get() = CelestiaString("Data Location", "Title for celestia.cfg, data location setting")
    }

    @Serializable
    data object Language : Settings() {
        override val name: String
            get() = CelestiaString("Language", "Display language setting")
    }

    @Serializable
    data object Font : Settings() {
        override val name: String
            get() = CelestiaString("Font", "")
    }

    @Serializable
    data object RenderInfo : Settings() {
        override val name: String
            get() = CelestiaString("Render Info", "Information about renderer")
    }

    @Serializable
    data object Toolbar : Settings() {
        override val name: String
            get() = CelestiaString("Toolbar", "Toolbar customization entry in Settings")
    }

    @Serializable
    data class Common(val data: Data) : Settings() {
        override val name: String
            get() = data.name.displayName

        @Serializable
        class Section(val rows: List<SettingsEntry>, val header: StringResource? = StringResource.Empty, val footer: StringResource? = null)

        @Serializable
        class Data(val name: StringResource, val sections: List<Section>)

        constructor(name: StringResource, sections: List<Section>) : this(Data(name, sections))

        companion object {
            fun create(name: StringResource, items: List<SettingsEntry>) : Common {
                return Common(Data(name, listOf(Section(items))))
            }

            val typeMap = mapOf(typeOf<Data>() to serializableType<Data>())
        }
    }
}

@Serializable
data class SettingsSection(val items: List<Settings>, val title: StringResource)

class SettingsFragment : Fragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    private fun getCurrentTopAppBarTitle(controller: NavController): String {
        val backStackEntry = controller.currentBackStackEntry ?: return ""
        if (backStackEntry.destination.hasRoute<Settings.Common>()) {
            return backStackEntry.toRoute<Settings.Common>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.Home>()) {
            return backStackEntry.toRoute<Settings.Home>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.About>()) {
            return backStackEntry.toRoute<Settings.About>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.RefreshRate>()) {
            return backStackEntry.toRoute<Settings.RefreshRate>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.CurrentTime>()) {
            return backStackEntry.toRoute<Settings.CurrentTime>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.DataLocation>()) {
            return backStackEntry.toRoute<Settings.DataLocation>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.Language>()) {
            return backStackEntry.toRoute<Settings.Language>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.Font>()) {
            return backStackEntry.toRoute<Settings.Font>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.RenderInfo>()) {
            return backStackEntry.toRoute<Settings.RenderInfo>().name
        }
        if (backStackEntry.destination.hasRoute<Settings.Toolbar>()) {
            return backStackEntry.toRoute<Settings.Toolbar>().name
        }
        return ""
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val viewModel: SettingsViewModel = hiltViewModel()
        val sections = if (viewModel.purchaseManager.canUseInAppPurchase()) mainSettingSectionsBeforePlus + celestiaPlusSettingSection + mainSettingSectionsAfterPlus else mainSettingSectionsBeforePlus + mainSettingSectionsAfterPlus
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val navController = rememberNavController()
        var title by remember {
            mutableStateOf("")
        }
        var canPop by remember { mutableStateOf(false) }
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
                canPop = controller.previousBackStackEntry != null
                title = getCurrentTopAppBarTitle(controller)
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }

        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = title)
            }, navigationIcon = {
                if (canPop) {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }, scrollBehavior = scrollBehavior)
        }) { paddingValues ->
            NavHost(navController = navController, startDestination = Settings.Home) {
                composable<Settings.Home> {
                    SettingsListScreen(paddingValues = paddingValues, itemHandler = {
                        navController.navigate(it)
                    }, sections = sections,  modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.Language> {
                    LanguageSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.RefreshRate> {
                    RefreshRateSettingsScreen(paddingValues = paddingValues, changeHandler = {
                        listener?.onRefreshRateChanged(it)
                    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.About> {
                    AboutScreen(paddingValues = paddingValues, urlHandler = { url, localizable ->
                        listener?.onAboutURLSelected(url, localizable)
                    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.CurrentTime> {
                    TimeSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.DataLocation> {
                    DataLocationSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.Common>(typeMap = Settings.Common.typeMap) { backStackEntry ->
                    CommonSettingsScreen(paddingValues = paddingValues, item = backStackEntry.toRoute<Settings.Common>().data, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<Settings.RenderInfo> {
                    RenderInfoScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    composable<Settings.Font> {
                        SubscriptionBackingScreen(paddingValues = paddingValues, content = {
                            FontSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                        }) {
                            listener?.requestOpenSubscriptionManagement()
                        }
                    }
                }
                composable<Settings.Toolbar> {
                    SubscriptionBackingScreen(paddingValues = paddingValues, content = {
                        ToolbarSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                    }) {
                        listener?.requestOpenSubscriptionManagement()
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onRefreshRateChanged(frameRateOption: Int)
        fun onAboutURLSelected(url: String, localizable: Boolean)
        fun requestOpenSubscriptionManagement()
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
