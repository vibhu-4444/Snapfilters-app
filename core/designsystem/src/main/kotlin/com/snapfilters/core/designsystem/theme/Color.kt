package com.snapfilters.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Design System Color Tokens from PRD v3.0 Chapter 7
val ColorSurfaceBase = Color(0xFF0B0B0C)
val ColorSurfaceRaised = Color(0xFF141416)
val ColorSurfaceSunken = Color(0xFF08080A)

val ColorTextPrimary = Color(0xFFF5F5F7)
val ColorTextSecondary = Color(0xFF9A9AA0)
val ColorTextTertiary = Color(0xFF5E5E66)

val ColorAccent = Color(0xFFC9A227)
val ColorAccentTint = Color(0xFF2A2416)

val ColorDanger = Color(0xFFE5484D)
val ColorSuccess = Color(0xFF3DD68C)

// Light Theme Tokens (Permitted for Library and Settings only)
val ColorSurfaceBaseLight = Color(0xFFFFFFFF)
val ColorTextPrimaryLight = Color(0xFF111113)

@Immutable
data class SnapColors(
    val surfaceBase: Color = ColorSurfaceBase,
    val surfaceRaised: Color = ColorSurfaceRaised,
    val surfaceSunken: Color = ColorSurfaceSunken,
    val textPrimary: Color = ColorTextPrimary,
    val textSecondary: Color = ColorTextSecondary,
    val textTertiary: Color = ColorTextTertiary,
    val accent: Color = ColorAccent,
    val accentTint: Color = ColorAccentTint,
    val danger: Color = ColorDanger,
    val success: Color = ColorSuccess,
    val isDark: Boolean = true
)

val DarkSnapColors = SnapColors(
    surfaceBase = ColorSurfaceBase,
    surfaceRaised = ColorSurfaceRaised,
    surfaceSunken = ColorSurfaceSunken,
    textPrimary = ColorTextPrimary,
    textSecondary = ColorTextSecondary,
    textTertiary = ColorTextTertiary,
    accent = ColorAccent,
    accentTint = ColorAccentTint,
    danger = ColorDanger,
    success = ColorSuccess,
    isDark = true
)

val LightSnapColors = SnapColors(
    surfaceBase = ColorSurfaceBaseLight,
    surfaceRaised = Color(0xFFF5F5F7),
    surfaceSunken = Color(0xFFE5E5EA),
    textPrimary = ColorTextPrimaryLight,
    textSecondary = Color(0xFF6E6E73),
    textTertiary = Color(0xFF8E8E93),
    accent = ColorAccent,
    accentTint = Color(0xFFFFF7DC),
    danger = ColorDanger,
    success = ColorSuccess,
    isDark = false
)

val LocalSnapColors = staticCompositionLocalOf { DarkSnapColors }
