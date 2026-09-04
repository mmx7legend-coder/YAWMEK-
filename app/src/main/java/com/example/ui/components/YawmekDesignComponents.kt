package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.audio.SoundHapticManager
import com.example.ui.theme.*

/**
 * Premium tactile card with smooth press compression feedback.
 */
@Composable
fun YawmekCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = YawmekRadius.lg,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundHapticManager.getInstance(context) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "card_press_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple()
                    ) {
                        soundManager.playTap()
                        onClick()
                    }
                } else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) YawmekElevation.subtle else YawmekElevation.card
        )
    ) {
        Column(
            modifier = Modifier.padding(YawmekSpacing.md),
            content = content
        )
    }
}

/**
 * Premium Button with tactile press bounce and haptic click.
 */
@Composable
fun YawmekButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    val context = LocalContext.current
    val soundManager = remember { SoundHapticManager.getInstance(context) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "btn_press_scale"
    )

    Button(
        onClick = {
            soundManager.playTap()
            onClick()
        },
        enabled = enabled,
        shape = YawmekRadius.md,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = YawmekSpacing.lg, vertical = YawmekSpacing.sm),
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Delightful animated checkbox with scale pop, color fill transition, and sound.
 */
@Composable
fun YawmekTaskCheckbox(
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = SemanticSuccess,
    uncheckedColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val context = LocalContext.current
    val soundManager = remember { SoundHapticManager.getInstance(context) }

    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "chk_scale"
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isChecked) checkedColor else uncheckedColor)
            .border(
                width = 1.5.dp,
                color = if (isChecked) checkedColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable {
                if (!isChecked) {
                    soundManager.playTaskCompleted()
                } else {
                    soundManager.playTap()
                }
                onCheckedChange()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isChecked,
            enter = fadeIn(tween(120)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(tween(90)) + scaleOut(tween(90))
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Completed",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Animated number counter for metrics, points, currency, and progress.
 */
@Composable
fun YawmekAnimatedCounter(
    value: Number,
    suffix: String = "",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut())
            },
            label = "counter_anim"
        ) { targetValue ->
            Text(
                text = "$targetValue",
                style = style,
                color = color
            )
        }
        if (suffix.isNotBlank()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = suffix,
                style = style.copy(fontSize = (style.fontSize.value * 0.85).sp, fontWeight = FontWeight.SemiBold),
                color = color.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Smoothly animated progress indicator with gradient bar and percentage.
 */
@Composable
fun YawmekProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillBrush: Brush = Brush.horizontalGradient(listOf(BrandBlue, SapphireAccent)),
    trackColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
    height: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(YawmekMotion.DurationEmphasis, easing = FastOutSlowInEasing),
        label = "prog_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(fillBrush)
        )
    }
}
