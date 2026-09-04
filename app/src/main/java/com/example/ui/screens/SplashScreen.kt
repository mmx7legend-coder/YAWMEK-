package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.audio.SoundHapticManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    val soundManager = remember { SoundHapticManager.getInstance(context) }

    // Multi-stage choreographed animation states
    val markScale = remember { Animatable(0.6f) }
    val markAlpha = remember { Animatable(0f) }
    val ringExpansion = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(16f) }
    val textAlpha = remember { Animatable(0f) }
    val arabicAlpha = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        soundManager.playSplashTone()

        // Phase 1: YAWMEK mark appears with punchy spring
        launch {
            markAlpha.animateTo(1f, animationSpec = tween(YawmekMotion.DurationStandard, easing = FastOutSlowInEasing))
        }
        launch {
            markScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Phase 2: Brand Accent Warm Amber glow ring expands (300ms in)
        delay(250)
        launch {
            ringAlpha.animateTo(0.7f, animationSpec = tween(180))
            ringExpansion.animateTo(1.4f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            ringAlpha.animateTo(0f, animationSpec = tween(220))
        }

        // Phase 3: "YAWMEK" and "يومك" gracefully reveal
        delay(200)
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(YawmekMotion.DurationStandard))
            textOffsetY.animateTo(0f, animationSpec = tween(YawmekMotion.DurationStandard, easing = FastOutSlowInEasing))
        }

        delay(120)
        launch {
            arabicAlpha.animateTo(1f, animationSpec = tween(YawmekMotion.DurationStandard))
        }

        // Phase 4: Hold briefly for premium brand impression (~350ms)
        delay(400)

        // Phase 5: Smooth exit fade
        exitAlpha.animateTo(0f, animationSpec = tween(YawmekMotion.DurationStandard, easing = LinearEasing))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF132038),
                        DarkBackground,
                        Color(0xFF04060B)
                    ),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Emblem + Animated Accent Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Expanding Warm Amber accent ring
                if (ringAlpha.value > 0f) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(ringExpansion.value)
                            .alpha(ringAlpha.value)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(WarmAmberGold.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.minDimension / 2
                            ),
                            radius = size.minDimension / 2.2f,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                // Core radiant emblem with Midnight Sapphire + Warm Amber gradient
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(markScale.value)
                        .alpha(markAlpha.value)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    WarmAmberGold,
                                    BrandAmberLight,
                                    SapphirePrimary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "YAWMEK Logo",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Brand Titles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = "YAWMEK",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 5.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "يومك",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WarmAmberGold,
                    modifier = Modifier.alpha(arabicAlpha.value)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "كل لحظة محسوبة",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.alpha(arabicAlpha.value * 0.9f)
                )
            }
        }
    }
}
