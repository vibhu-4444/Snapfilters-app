package com.snapfilters.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val MaterialDarkColors = darkColorScheme(
    primary = ColorAccent,
    background = ColorSurfaceBase,
    surface = ColorSurfaceRaised,
    onPrimary = ColorSurfaceBase,
    onBackground = ColorTextPrimary,
    onSurface = ColorTextPrimary,
    error = ColorDanger
)

private val MaterialLightColors = lightColorScheme(
    primary = ColorAccent,
    background = ColorSurfaceBaseLight,
    surface = Color(0xFFF5F5F7),
    onPrimary = ColorSurfaceBaseLight,
    onBackground = ColorTextPrimaryLight,
    onSurface = ColorTextPrimaryLight,
    error = ColorDanger
)

@Composable
fun SnapTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkSnapColors else LightSnapColors
    val materialColors = if (darkTheme) MaterialDarkColors else MaterialLightColors

    CompositionLocalProvider(
        LocalSnapColors provides colors,
        LocalSnapTypography provides SnapTypography(),
        LocalSnapSpacing provides SnapSpacing(),
        LocalSnapShapes provides SnapShapes()
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}

object SnapTheme {
    val colors: SnapColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSnapColors.current

    val typography: SnapTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalSnapTypography.current

    val spacing: SnapSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSnapSpacing.current

    val shapes: SnapShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalSnapShapes.current
}
