// SheetHeader.kt
//
// Copyright (C) 2026, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color

class SheetHeaderState internal constructor(private val defaultColor: State<Color>) {
    // Incoming pages take precedence while outgoing pages finish their transition.
    private val sources = mutableStateListOf<State<Color>>()

    val color: Color
        get() = sources.lastOrNull()?.value ?: defaultColor.value

    internal fun register(color: State<Color>): () -> Unit {
        sources.add(color)
        return { sources.remove(color) }
    }
}

val LocalSheetHeaderState = staticCompositionLocalOf<SheetHeaderState?> { null }

@Composable
fun rememberSheetHeaderState(): SheetHeaderState {
    val defaultColor = rememberUpdatedState(MaterialTheme.colorScheme.surfaceContainerLow)
    return remember { SheetHeaderState(defaultColor) }
}

@Composable
fun SheetHeaderColor(color: Color) {
    RegisterSheetHeaderColor(rememberUpdatedState(color))
}

@Composable
private fun RegisterSheetHeaderColor(color: State<Color>) {
    val headerState = LocalSheetHeaderState.current ?: return
    DisposableEffect(headerState, color) {
        val unregister = headerState.register(color)
        onDispose { unregister() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    if (LocalSheetHeaderState.current == null) {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
        return
    }

    val targetColor by remember(colors, scrollBehavior) {
        derivedStateOf {
            if ((scrollBehavior?.state?.overlappedFraction ?: 0f) > 0.01f) {
                colors.scrolledContainerColor
            } else {
                colors.containerColor
            }
        }
    }
    val backgroundColor = animateColorAsState(
        targetValue = targetColor,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "SheetHeaderColor"
    )
    RegisterSheetHeaderColor(backgroundColor)

    TopAppBar(
        title = title,
        modifier = modifier.drawBehind { drawRect(backgroundColor.value) },
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors.copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        scrollBehavior = scrollBehavior
    )
}
