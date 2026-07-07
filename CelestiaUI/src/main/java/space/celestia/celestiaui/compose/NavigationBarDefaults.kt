package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalLayoutApi::class)
val NavigationBarDefaults.windowInsetsIgnoringVisibility: WindowInsets
    @Composable get() = WindowInsets.systemBarsIgnoringVisibility.union(WindowInsets.displayCutout).only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    )