package com.snapfilters.core.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

enum class ShutterState {
    Idle,
    Pressed,
    Disabled,
    TimerCounting
}

/**
 * ShutterButton per PRD v3.0 Chapter 7 Component Inventory:
 * - 72 dp outer ring, 60 dp inner disc
 * - Idle, pressed, disabled, timer states
 * - Haptics on shutter, no sound effects
 */
@Composable
fun ShutterButton(
    modifier: Modifier = Modifier,
    state: ShutterState = ShutterState.Idle,
    timerSecondsRemaining: Int? = null,
    onClick: () -> Unit
) {
    val view = LocalView.current
    var isPressedInternal by remember { mutableStateOf(false) }

    val isEffectivelyPressed = state == ShutterState.Pressed || isPressedInternal
    val innerScale by animateFloatAsState(
        targetValue = if (isEffectivelyPressed) 0.88f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "shutterInnerScale"
    )

    val outerRingColor = when (state) {
        ShutterState.Disabled -> SnapTheme.colors.textTertiary
        ShutterState.TimerCounting -> SnapTheme.colors.accent
        else -> SnapTheme.colors.textPrimary
    }

    val innerDiscColor = when (state) {
        ShutterState.Disabled -> SnapTheme.colors.textTertiary.copy(alpha = 0.5f)
        ShutterState.TimerCounting -> SnapTheme.colors.accent
        else -> SnapTheme.colors.textPrimary
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .semantics { contentDescription = "Camera Shutter" }
            .border(width = 3.dp, color = outerRingColor, shape = CircleShape)
            .pointerInput(state) {
                if (state != ShutterState.Disabled) {
                    detectTapGestures(
                        onPress = {
                            isPressedInternal = true
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            tryAwaitRelease()
                            isPressedInternal = false
                        },
                        onTap = {
                            onClick()
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(innerScale)
                .clip(CircleShape)
                .background(innerDiscColor),
            contentAlignment = Alignment.Center
        ) {
            if (state == ShutterState.TimerCounting && timerSecondsRemaining != null) {
                Text(
                    text = timerSecondsRemaining.toString(),
                    style = SnapTheme.typography.title,
                    color = Color.Black
                )
            }
        }
    }
}

