/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("Mdc3Theme")

package space.celestia.celestiaui.compose

import android.content.Context
import android.content.res.Resources
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import space.celestia.celestiaui.R
import java.lang.reflect.Method

/**
 * Applies the host XML theme's colors, typography, and shapes to Compose.
 */
@Composable
fun Mdc3Theme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // We try and use the theme key value if available, which should be a perfect key for caching
    // and avoid the expensive theme lookups in re-compositions.
    //
    // If the key is not available, we use the Theme itself as a rough approximation. Using the
    // Theme instance as the key is not perfect, but it should work for 90% of cases.
    // It falls down when the theme is manually mutated after a composition has happened
    // (via `applyStyle()`, `rebase()`, `setTo()`), but the majority of apps do not use those.
    val key = context.theme.key ?: context.theme

    val layoutDirection = LocalLayoutDirection.current

    val themeParams = remember(key) {
        createMdc3Theme(
            context = context,
            layoutDirection = layoutDirection
        )
    }

    MaterialTheme(
        colorScheme = themeParams.colorScheme,
        typography = themeParams.typography,
        shapes = themeParams.shapes
    ) {
        // We update the LocalContentColor to match our onBackground. This allows the default
        // content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content
        )
    }
}

private data class Theme3Parameters(
    val colorScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes
)

/**
 * Reads text appearances without fixed text colors so Compose can supply content colors.
 */
private fun createMdc3Theme(
    context: Context,
    layoutDirection: LayoutDirection,
): Theme3Parameters {
    val density = Density(context)
    return context.obtainStyledAttributes(R.styleable.ThemeAdapterMaterial3Theme).use { ta ->
        require(ta.hasValue(R.styleable.ThemeAdapterMaterial3Theme_isMaterial3Theme)) {
            "createMdc3Theme requires the host context's theme to extend Theme.Material3"
        }

        val colorScheme = run {
            /* First we'll read the Material 3 color palette */
            val primary = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorPrimary)
            val onPrimary = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnPrimary)
            val primaryInverse = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorPrimaryInverse)
            val primaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorPrimaryContainer)
            val onPrimaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnPrimaryContainer)
            val primaryFixed = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorPrimaryFixed)
            val primaryFixedDim = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorPrimaryFixedDim)
            val onPrimaryFixed = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnPrimaryFixed)
            val onPrimaryFixedVariant = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnPrimaryFixedVariant)
            val secondary = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSecondary)
            val onSecondary = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSecondary)
            val secondaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSecondaryContainer)
            val onSecondaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSecondaryContainer)
            val secondaryFixed = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSecondaryFixed)
            val secondaryFixedDim = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSecondaryFixedDim)
            val onSecondaryFixed = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSecondaryFixed)
            val onSecondaryFixedVariant = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSecondaryFixedVariant)
            val tertiary = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorTertiary)
            val onTertiary = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnTertiary)
            val tertiaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorTertiaryContainer)
            val onTertiaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnTertiaryContainer)
            val tertiaryFixed = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorTertiaryFixed)
            val tertiaryFixedDim = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorTertiaryFixedDim)
            val onTertiaryFixed = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnTertiaryFixed)
            val onTertiaryFixedVariant = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnTertiaryFixedVariant)
            val background = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_android_colorBackground)
            val onBackground = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnBackground)
            val surface = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurface)
            val surfaceBright = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceBright)
            val surfaceDim = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceDim)
            val surfaceContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceContainer)
            val surfaceContainerHigh = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceContainerHigh)
            val surfaceContainerHighest = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceContainerHighest)
            val surfaceContainerLow = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceContainerLow)
            val surfaceContainerLowest = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceContainerLowest)
            val onSurface = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSurface)
            val surfaceVariant = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceVariant)
            val onSurfaceVariant = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSurfaceVariant)
            val elevationOverlay = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_elevationOverlayColor)
            val surfaceInverse = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorSurfaceInverse)
            val onSurfaceInverse = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnSurfaceInverse)
            val outline = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOutline)
            val outlineVariant = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOutlineVariant)
            val error = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorError)
            val onError = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnError)
            val errorContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorErrorContainer)
            val onErrorContainer = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_colorOnErrorContainer)
            val scrimBackground = ta.parseColor(R.styleable.ThemeAdapterMaterial3Theme_scrimBackground)

            val isLightTheme = ta.getBoolean(R.styleable.ThemeAdapterMaterial3Theme_isLightTheme, true)

            if (isLightTheme) {
                lightColorScheme(
                    primary = primary,
                    onPrimary = onPrimary,
                    inversePrimary = primaryInverse,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    primaryFixed = primaryFixed,
                    primaryFixedDim = primaryFixedDim,
                    onPrimaryFixed = onPrimaryFixed,
                    onPrimaryFixedVariant = onPrimaryFixedVariant,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    secondaryContainer = secondaryContainer,
                    onSecondaryContainer = onSecondaryContainer,
                    secondaryFixed = secondaryFixed,
                    secondaryFixedDim = secondaryFixedDim,
                    onSecondaryFixed = onSecondaryFixed,
                    onSecondaryFixedVariant = onSecondaryFixedVariant,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    tertiaryContainer = tertiaryContainer,
                    onTertiaryContainer = onTertiaryContainer,
                    tertiaryFixed = tertiaryFixed,
                    tertiaryFixedDim = tertiaryFixedDim,
                    onTertiaryFixed = onTertiaryFixed,
                    onTertiaryFixedVariant = onTertiaryFixedVariant,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    surfaceBright = surfaceBright,
                    surfaceDim = surfaceDim,
                    surfaceContainer = surfaceContainer,
                    surfaceContainerHigh = surfaceContainerHigh,
                    surfaceContainerHighest = surfaceContainerHighest,
                    surfaceContainerLow = surfaceContainerLow,
                    surfaceContainerLowest = surfaceContainerLowest,
                    onSurface = onSurface,
                    surfaceVariant = surfaceVariant,
                    onSurfaceVariant = onSurfaceVariant,
                    surfaceTint = elevationOverlay,
                    inverseSurface = surfaceInverse,
                    inverseOnSurface = onSurfaceInverse,
                    outline = outline,
                    outlineVariant = outlineVariant,
                    error = error,
                    onError = onError,
                    errorContainer = errorContainer,
                    onErrorContainer = onErrorContainer,
                    scrim = scrimBackground
                )
            } else {
                darkColorScheme(
                    primary = primary,
                    onPrimary = onPrimary,
                    inversePrimary = primaryInverse,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    primaryFixed = primaryFixed,
                    primaryFixedDim = primaryFixedDim,
                    onPrimaryFixed = onPrimaryFixed,
                    onPrimaryFixedVariant = onPrimaryFixedVariant,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    secondaryContainer = secondaryContainer,
                    onSecondaryContainer = onSecondaryContainer,
                    secondaryFixed = secondaryFixed,
                    secondaryFixedDim = secondaryFixedDim,
                    onSecondaryFixed = onSecondaryFixed,
                    onSecondaryFixedVariant = onSecondaryFixedVariant,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    tertiaryContainer = tertiaryContainer,
                    onTertiaryContainer = onTertiaryContainer,
                    tertiaryFixed = tertiaryFixed,
                    tertiaryFixedDim = tertiaryFixedDim,
                    onTertiaryFixed = onTertiaryFixed,
                    onTertiaryFixedVariant = onTertiaryFixedVariant,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    surfaceBright = surfaceBright,
                    surfaceDim = surfaceDim,
                    surfaceContainer = surfaceContainer,
                    surfaceContainerHigh = surfaceContainerHigh,
                    surfaceContainerHighest = surfaceContainerHighest,
                    surfaceContainerLow = surfaceContainerLow,
                    surfaceContainerLowest = surfaceContainerLowest,
                    onSurface = onSurface,
                    surfaceVariant = surfaceVariant,
                    onSurfaceVariant = onSurfaceVariant,
                    surfaceTint = elevationOverlay,
                    inverseSurface = surfaceInverse,
                    inverseOnSurface = onSurfaceInverse,
                    outline = outline,
                    outlineVariant = outlineVariant,
                    error = error,
                    onError = onError,
                    errorContainer = errorContainer,
                    onErrorContainer = onErrorContainer,
                    scrim = scrimBackground
                )
            }
        }

        fun readTextAppearance(index: Int) = parseTextAppearance(
            context = context,
            id = ta.getResourceIdOrThrow(index),
            density = density,
            setTextColors = false,
            defaultFontFamily = null
        )

        val typography = Typography(
            displayLarge = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceDisplayLarge),
            displayMedium = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceDisplayMedium),
            displaySmall = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceDisplaySmall),
            headlineLarge = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceHeadlineLarge),
            headlineMedium = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceHeadlineMedium),
            headlineSmall = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceHeadlineSmall),
            titleLarge = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceTitleLarge),
            titleMedium = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceTitleMedium),
            titleSmall = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceTitleSmall),
            bodyLarge = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceBodyLarge),
            bodyMedium = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceBodyMedium),
            bodySmall = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceBodySmall),
            labelLarge = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceLabelLarge),
            labelMedium = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceLabelMedium),
            labelSmall = readTextAppearance(R.styleable.ThemeAdapterMaterial3Theme_textAppearanceLabelSmall),
        )

        val shapes = Shapes(
            extraSmall = parseShapeAppearance(
                context = context,
                id = ta.getResourceIdOrThrow(R.styleable.ThemeAdapterMaterial3Theme_shapeAppearanceCornerExtraSmall),
                layoutDirection = layoutDirection,
                fallbackShape = emptyShapes.extraSmall
            ),
            small = parseShapeAppearance(
                context = context,
                id = ta.getResourceIdOrThrow(R.styleable.ThemeAdapterMaterial3Theme_shapeAppearanceCornerSmall),
                layoutDirection = layoutDirection,
                fallbackShape = emptyShapes.small
            ),
            medium = parseShapeAppearance(
                context = context,
                id = ta.getResourceIdOrThrow(R.styleable.ThemeAdapterMaterial3Theme_shapeAppearanceCornerMedium),
                layoutDirection = layoutDirection,
                fallbackShape = emptyShapes.medium
            ),
            large = parseShapeAppearance(
                context = context,
                id = ta.getResourceIdOrThrow(R.styleable.ThemeAdapterMaterial3Theme_shapeAppearanceCornerLarge),
                layoutDirection = layoutDirection,
                fallbackShape = emptyShapes.large
            ),
            extraLarge = parseShapeAppearance(
                context = context,
                id = ta.getResourceIdOrThrow(R.styleable.ThemeAdapterMaterial3Theme_shapeAppearanceCornerExtraLarge),
                layoutDirection = layoutDirection,
                fallbackShape = emptyShapes.extraLarge
            )
        )

        Theme3Parameters(colorScheme, typography, shapes)
    }
}

private val emptyShapes = Shapes()

/**
 * This is gross, but we need a way to check for theme equality. Theme does not implement
 * `equals()` or `hashCode()`, but it does have a hidden method called `getKey()`.
 *
 * The cost of this reflective invoke is a lot cheaper than the full theme read which can
 * happen on each re-composition.
 */
private inline val Resources.Theme.key: Any?
    get() {
        if (!sThemeGetKeyMethodFetched) {
            try {
                @Suppress("SoonBlockedPrivateApi")
                sThemeGetKeyMethod = Resources.Theme::class.java.getDeclaredMethod("getKey")
                    .apply { isAccessible = true }
            } catch (e: ReflectiveOperationException) {
                // Failed to retrieve Theme.getKey method
            }
            sThemeGetKeyMethodFetched = true
        }
        if (sThemeGetKeyMethod != null) {
            return try {
                sThemeGetKeyMethod?.invoke(this)
            } catch (e: ReflectiveOperationException) {
                // Failed to invoke Theme.getKey()
            }
        }
        return null
    }

private var sThemeGetKeyMethodFetched = false
private var sThemeGetKeyMethod: Method? = null
