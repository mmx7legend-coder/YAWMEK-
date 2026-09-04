package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * YAWMEK Design System Tokens
 * Midnight Sapphire + Warm Amber identity
 */
object YawmekSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val section: Dp = 28.dp
}

object YawmekRadius {
    val xs = RoundedCornerShape(8.dp)
    val sm = RoundedCornerShape(12.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(22.dp)
    val xl = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(999.dp)
}

object YawmekElevation {
    val none: Dp = 0.dp
    val subtle: Dp = 2.dp
    val card: Dp = 4.dp
    val elevated: Dp = 8.dp
    val modal: Dp = 16.dp
}

object YawmekMotion {
    const val DurationMicro = 150
    const val DurationStandard = 250
    const val DurationEmphasis = 400
    const val DurationReveal = 600

    // Smooth physics-based spring for micro-interactions (bounces subtly, feels native & premium)
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
}
