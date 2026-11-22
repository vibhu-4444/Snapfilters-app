package com.snapfilters.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing, Radius, and Elevation Tokens from PRD v3.0 Chapter 7:
// Spacing: 4, 8, 12, 16, 24 dp (Only these five; no arbitrary values)
// radius.control: 10 dp (Buttons, chips)
// radius.sheet: 20 dp top corners (All bottom sheets)
// radius.pill: Full (Library and Conversations entry points)
// elevation: 0 dp everywhere (Depth comes from surface tokens, not shadows)

@Immutable
data class SnapSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp
)

@Immutable
data class SnapShapes(
    val control: RoundedCornerShape = RoundedCornerShape(10.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp)
)

val LocalSnapSpacing = staticCompositionLocalOf { SnapSpacing() }
val LocalSnapShapes = staticCompositionLocalOf { SnapShapes() }
