package space.celestia.mobilecelestia.purchase

import android.graphics.fonts.SystemFonts
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.celestia.celestia.Font
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.settings.CustomFont
import space.celestia.mobilecelestia.settings.boldFont
import space.celestia.mobilecelestia.settings.normalFont
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@RequiresApi(Build.VERSION_CODES.Q)
class FontSettingFragment : SubscriptionBackingFragment() {
    private class Font(val path: String, val name: String, val ttcIndex: Int)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Font", "")
    }

    @Composable
    override fun MainView() {
        val viewModel: SettingsViewModel = hiltViewModel()
        var systemFonts by remember {
            mutableStateOf(listOf<Font>())
        }
        var fontsLoaded by remember {
            mutableStateOf(false)
        }
        var selectedTabIndex by remember {
            mutableIntStateOf(0)
        }
        var normalFont by remember {
            mutableStateOf(viewModel.appSettings.normalFont)
        }
        var boldFont by remember {
            mutableStateOf(viewModel.appSettings.boldFont)
        }
        val currentFont = if (selectedTabIndex == 0) normalFont else boldFont
        if (fontsLoaded) {
            val nestedScrollInterop = rememberNestedScrollInteropConnection()
            LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = WindowInsets.systemBars.asPaddingValues()) {
                item {
                    // TODO: Replace with SegmentedButton
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        val tabTitleModifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.tab_title_text_padding_vertical))
                        Tab(selected = selectedTabIndex == 0, onClick = {
                            selectedTabIndex = 0
                        }) {
                            Text(text = CelestiaString("Normal", ""), modifier = tabTitleModifier)
                        }
                        Tab(selected = selectedTabIndex == 1, onClick = {
                            selectedTabIndex = 1
                        }) {
                            Text(text = CelestiaString("Bold", ""), modifier = tabTitleModifier)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    RadioButtonRow(primaryText = CelestiaString("Default", ""), selected = currentFont == null, onClick = {
                        if (selectedTabIndex == 0) {
                            viewModel.appSettings.normalFont = null
                            normalFont = null
                        } else {
                            viewModel.appSettings.boldFont = null
                            boldFont = null
                        }
                    })
                }
                if (systemFonts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                        Header(text = CelestiaString("System Fonts", ""))
                    }
                    items(items = systemFonts) {
                        RadioButtonRow(
                            primaryText = it.name,
                            selected = it.path == currentFont?.path && it.ttcIndex == currentFont.ttcIndex,
                            onClick = {
                                val font = CustomFont(it.path, it.ttcIndex)
                                if (selectedTabIndex == 0) {
                                    viewModel.appSettings.normalFont = font
                                    normalFont = font
                                } else {
                                    viewModel.appSettings.boldFont = font
                                    boldFont = font
                                }
                            })
                    }
                }
                item {
                    Footer(text = CelestiaString("Configuration will take effect after a restart.", ""))
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                }
            }
        } else {
            LaunchedEffect(true) {
                val fonts = withContext(Dispatchers.IO) {
                    val availableFonts = SystemFonts.getAvailableFonts()
                    val fontPaths = mutableSetOf<String>()
                    for (font in availableFonts) {
                        val file = font.file
                        if (file != null && file.isFile) {
                            fontPaths.add(file.path)
                        }
                    }
                    val fontCollections = arrayListOf<space.celestia.celestia.Font>()
                    for (fontPath in fontPaths) {
                        val fontCollection = Font(fontPath)
                        if (fontCollection.fontNames.isNotEmpty()) {
                            fontCollections.add(fontCollection)
                        }
                    }
                    fontCollections.sortWith { p0, p1 -> p0.fontNames[0].compareTo(p1.fontNames[0]) }
                    val results = arrayListOf<Font>()
                    for (fontCollection in fontCollections) {
                        for (fontIndex in fontCollection.fontNames.indices) {
                            results.add(Font(fontCollection.path, fontCollection.fontNames[fontIndex], fontIndex))
                        }
                    }
                    results
                }
                systemFonts = fonts
                fontsLoaded = true
            }
            Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): FontSettingFragment {
            return FontSettingFragment()
        }
    }
}