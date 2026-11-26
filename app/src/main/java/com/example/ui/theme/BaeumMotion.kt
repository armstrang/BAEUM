package com.example.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Curated motion language & physics primitives for Baeum's "Seoul Night Atelier" experience.
 */
object BaeumMotion {
    /** Quintic ease-out curve for cinematic screen and hero transitions. */
    val SilkEasing = CubicBezierEasing(0.18f, 1.0f, 0.30f, 1.0f)

    /** Graceful decelerate curve for subtle optical fades and counters. */
    val VelvetEasing = CubicBezierEasing(0.25f, 0.92f, 0.35f, 1.0f)

    /** Tactile spring for button presses, pill glides, and chip toggles. */
    fun <T> silkSpring() = spring<T>(
        dampingRatio = 0.78f,
        stiffness = 420f
    )

    /** Expressive soft spring for entrances, rings, and celebratory reveals. */
    fun <T> luxurySpring() = spring<T>(
        dampingRatio = 0.72f,
        stiffness = 240f
    )

    /** Fluid gliding spring for floating dock pill and card stack recovery. */
    fun <T> liquidSpring() = spring<T>(
        dampingRatio = 0.74f,
        stiffness = 320f
    )

    /** Gentle bouncy spring for bottom dock icon focus and micro-rewards. */
    fun <T> playfulSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 360f
    )
}

/**
 * Smoothly animates an integer value like a precision luxury odometer, respecting reduced motion.
 */
@Composable
fun rememberAnimatedInt(
    targetValue: Int,
    durationMs: Int = 520,
    label: String = "animated_int"
): State<Int> {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    return animateIntAsState(
        targetValue = targetValue,
        animationSpec = if (reducedMotion) tween(0) else tween(
            durationMillis = durationMs,
            easing = BaeumMotion.VelvetEasing
        ),
        label = label
    )
}

/**
 * Adds luxury tactile spring compression + gentle ripple + subtle haptic feedback to any clickable surface.
 */
fun Modifier.tactileClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.968f,
    hapticEnabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reducedMotion) pressedScale else 1f,
        animationSpec = BaeumMotion.silkSpring(),
        label = "tactile_scale"
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reducedMotion) 0.94f else 1f,
        animationSpec = tween(120),
        label = "tactile_alpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = pressAlpha
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true),
            enabled = enabled,
            onClick = {
                if (hapticEnabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            }
        )
}

/**
 * Adds a delicate diagonal glass light-sweep glint across hero surfaces and primary CTAs.
 * Automatically disabled when reducedMotion is enabled.
 */
fun Modifier.luxuryShimmer(
    enabled: Boolean = true,
    durationMillis: Int = 4600,
    shimmerAlpha: Float = 0.16f
): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (!enabled || reducedMotion) return@composed this

    val transition = rememberInfiniteTransition(label = "luxury_shimmer")
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    this.drawWithContent {
        drawContent()
        if (progress in -0.35f..1.35f) {
            val w = size.width
            val h = size.height
            val centerX = w * progress
            val bandW = w * 0.34f
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = shimmerAlpha),
                        Color.Transparent
                    ),
                    start = Offset(centerX - bandW, 0f),
                    end = Offset(centerX + bandW, h)
                )
            )
        }
    }
}

/**
 * Triggers a gentle, crisp horizontal spring shake when [triggerKey] increments (e.g. incorrect quiz check).
 */
fun Modifier.gentleShake(triggerKey: Int): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0 && !reducedMotion) {
            val hops = listOf(-14f, 12f, -8f, 5f, 0f)
            hops.forEach { target ->
                offsetX.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 48, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    this.graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * Smooth staggered choreography for cards and sections entering the viewport.
 * Automatically renders immediately without translation when reducedMotion is enabled.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    delayStepMs: Long = 48L,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    var visible by remember(index, reducedMotion) { mutableStateOf(reducedMotion) }

    LaunchedEffect(index, reducedMotion) {
        if (!reducedMotion && !visible) {
            delay((index.coerceIn(0, 10)) * delayStepMs)
            visible = true
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (reducedMotion) tween(0) else tween(
            durationMillis = 460,
            easing = BaeumMotion.SilkEasing
        ),
        label = "stagger_entrance_$index"
    )

    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            val dy = (1f - progress) * 22.dp.toPx()
            translationY = dy
            val sc = 0.972f + (0.028f * progress)
            scaleX = sc
            scaleY = sc
        }
    ) {
        content()
    }
}

/**
 * Living, ultra-subtle radial silk-aurora canvas behind screens so the UI breathes with depth
 * and never feels like a flat solid slab. Includes whisper-quiet starlight dust in Seoul Night mode.
 */
@Composable
fun SeoulAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val extended = LocalBaeumColors.current
    val bgColor = MaterialTheme.colorScheme.background
    val phase = if (extended.reducedMotion) {
        0.25f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "seoul_aurora")
        val animPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aurora_phase"
        )
        animPhase
    }

    val starPoints = remember {
        listOf(
            Triple(0.14f, 0.08f, 1.3f),
            Triple(0.32f, 0.16f, 1.0f),
            Triple(0.76f, 0.06f, 1.6f),
            Triple(0.88f, 0.21f, 1.1f),
            Triple(0.22f, 0.34f, 0.9f),
            Triple(0.64f, 0.28f, 1.2f),
            Triple(0.91f, 0.48f, 1.0f),
            Triple(0.09f, 0.62f, 1.3f),
            Triple(0.52f, 0.72f, 0.9f),
            Triple(0.82f, 0.86f, 1.4f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val angle = phase * PI.toFloat() * 2f

            // Primary amethyst-lavender moon orb drifting gently near top-right
            val orb1Center = Offset(
                x = w * (0.82f + 0.09f * cos(angle)),
                y = h * (0.10f + 0.05f * sin(angle))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(extended.ambientGlowPrimary, Color.Transparent),
                    center = orb1Center,
                    radius = w * 0.98f
                ),
                radius = w * 0.98f,
                center = orb1Center
            )

            // Secondary celadon-mint mist orb drifting gently near mid-left
            val orb2Center = Offset(
                x = w * (0.10f + 0.08f * sin(angle)),
                y = h * (0.44f + 0.05f * cos(angle))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(extended.ambientGlowSecondary, Color.Transparent),
                    center = orb2Center,
                    radius = w * 0.88f
                ),
                radius = w * 0.88f,
                center = orb2Center
            )

            // Warm champagne-gold shimmer whisper near bottom-right
            val orb3Center = Offset(
                x = w * (0.88f - 0.06f * sin(angle)),
                y = h * (0.84f - 0.04f * cos(angle))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(extended.ambientGlowGold, Color.Transparent),
                    center = orb3Center,
                    radius = w * 0.74f
                ),
                radius = w * 0.74f,
                center = orb3Center
            )

            // Whisper-quiet starlight constellation dust in Seoul Night dark theme
            if (extended.isDark) {
                starPoints.forEachIndexed { idx, (fx, fy, r) ->
                    val twinkle = 0.14f + 0.18f * ((sin(angle * 1.4f + idx * 1.1f) + 1f) * 0.5f)
                    drawCircle(
                        color = Color(0xFFE7E2FF).copy(alpha = twinkle),
                        radius = r.dp.toPx(),
                        center = Offset(w * fx, h * fy)
                    )
                }
            }
        }
        content()
    }
}
