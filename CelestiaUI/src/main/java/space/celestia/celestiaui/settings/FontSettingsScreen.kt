package space.celestia.celestiaui.settings

import android.graphics.Typeface
import android.graphics.fonts.SystemFonts
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.celestia.celestia.Font
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.Footer
import space.celestia.celestiaui.compose.Header
import space.celestia.celestiaui.compose.RadioButtonRow
import space.celestia.celestiaui.settings.viewmodel.CustomFont
import space.celestia.celestiaui.settings.viewmodel.boldFont
import space.celestia.celestiaui.settings.viewmodel.normalFont
import space.celestia.celestiaui.settings.viewmodel.SettingsViewModel
import space.celestia.celestiaui.utils.CelestiaString

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun FontSettingsScreen(paddingValues: PaddingValues) {
    data class Font(val path: String, val name: String, val ttcIndex: Int)

    val viewModel: SettingsViewModel = hiltViewModel()
    val cachedTypefaces = remember { mutableStateMapOf<Font, Typeface>() }
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
        mutableStateOf(viewModel.appSettingsNoBackup.normalFont)
    }
    var boldFont by remember {
        mutableStateOf(viewModel.appSettingsNoBackup.boldFont)
    }
    val currentFont = if (selectedTabIndex == 0) normalFont else boldFont
    if (fontsLoaded) {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = paddingValues) {
            item {
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    val tabTitleModifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.tab_title_text_padding_vertical))
                    Tab(selected = selectedTabIndex == 0, onClick = {
                        selectedTabIndex = 0
                    }) {
                        Text(text = CelestiaString("Normal", "Normal font style"), modifier = tabTitleModifier)
                    }
                    Tab(selected = selectedTabIndex == 1, onClick = {
                        selectedTabIndex = 1
                    }) {
                        Text(text = CelestiaString("Bold", "Bold font style"), modifier = tabTitleModifier)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                RadioButtonRow(primaryText = CelestiaString("Default", ""), selected = currentFont == null, onClick = {
                    if (selectedTabIndex == 0) {
                        viewModel.appSettingsNoBackup.normalFont = null
                        normalFont = null
                    } else {
                        viewModel.appSettingsNoBackup.boldFont = null
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
                    val typeface = cachedTypefaces[it]
                    if (typeface == null) {
                        LaunchedEffect(Unit) {
                            val font = withContext(Dispatchers.IO) {
                                Typeface.Builder(it.path).setTtcIndex(it.ttcIndex).build()
                            }
                            cachedTypefaces[it] = font
                        }
                    }
                    RadioButtonRow(
                        primaryText = it.name,
                        secondaryText =  if (typeface == null) null else "The quick brown fox jumps over the lazy dog",
                        secondaryTextTypeFace = typeface,
                        selected = it.path == currentFont?.path && it.ttcIndex == currentFont.ttcIndex,
                        onClick = {
                            val font = CustomFont(it.path, it.ttcIndex)
                            if (selectedTabIndex == 0) {
                                viewModel.appSettingsNoBackup.normalFont = font
                                normalFont = font
                            } else {
                                viewModel.appSettingsNoBackup.boldFont = font
                                boldFont = font
                            }
                        })
                }
            }
            item {
                Footer(text = CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
            }
        }
    } else {
        LaunchedEffect(Unit) {
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}