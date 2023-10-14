package space.celestia.mobilecelestia.purchase

import android.graphics.fonts.SystemFonts
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.celestia.celestia.Font
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.settings.CustomFont
import space.celestia.mobilecelestia.settings.boldFont
import space.celestia.mobilecelestia.settings.normalFont
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.Q)
class FontSettingFragment : SubscriptionBackingFragment() {
    private class Font(val path: String, val name: String, val ttcIndex: Int)

    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    private var bottomPadding = mutableIntStateOf(0)

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            bottomPadding.intValue = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            WindowInsetsCompat.CONSUMED
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Font", "")
    }

    @Composable
    private fun MainScreen() {
        var systemFonts by remember {
            mutableStateOf<List<Font>>(listOf())
        }
        var fontsLoaded by remember {
            mutableStateOf(false)
        }
        var selectedTabIndex by remember {
            mutableIntStateOf(0)
        }
        var normalFont by remember {
            mutableStateOf(appSettings.normalFont)
        }
        var boldFont by remember {
            mutableStateOf(appSettings.boldFont)
        }
        val currentFont = if (selectedTabIndex == 0) normalFont else boldFont
        if (fontsLoaded) {
            LazyColumn {
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
                            appSettings.normalFont = null
                            normalFont = null
                        } else {
                            appSettings.boldFont = null
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
                                    appSettings.normalFont = font
                                    normalFont = font
                                } else {
                                    appSettings.boldFont = font
                                    boldFont = font
                                }
                            })
                    }
                }
                item {
                    Footer(text = CelestiaString("Configuration will take effect after a restart.", ""))
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    with(LocalDensity.current) {
                        Spacer(modifier = Modifier.height(bottomPadding.intValue.toDp()))
                    }
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
                fontsLoaded = true
                systemFonts = fonts
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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