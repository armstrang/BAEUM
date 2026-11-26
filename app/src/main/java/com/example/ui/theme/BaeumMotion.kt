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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Curated motion language & physics primitives for Baeum's "Seoul Atelier" experience.
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
 * Defers scale and alpha reads strictly to the draw phase (`graphicsLayer`) to avoid recomposition.
 */
fun Modifier.tactileClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.972f,
    hapticEnabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed && enabled && !reducedMotion) pressedScale else 1f,
        animationSpec = BaeumMotion.silkSpring(),
        label = "tactile_scale"
    )
    val pressAlphaState = animateFloatAsState(
        targetValue = if (isPressed && enabled && !reducedMotion) 0.94f else 1f,
        animationSpec = tween(110),
        label = "tactile_alpha"
    )

    this
        .graphicsLayer {
            val s = scaleState.value
            scaleX = s
            scaleY = s
            alpha = pressAlphaState.value
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
 * Adds a delicate diagonal glass light-sweep glint across opt-in surfaces.
 * Reads animation state strictly during draw phase (`drawWithContent`) and honors reduced motion.
 */
fun Modifier.luxuryShimmer(
    enabled: Boolean = true,
    durationMillis: Int = 4600,
    shimmerAlpha: Float = 0.14f
): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (!enabled || reducedMotion) return@composed this

    val transition = rememberInfiniteTransition(label = "luxury_shimmer")
    val progressState = transition.animateFloat(
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
        val progress = progressState.value
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
 * Defers progress reads into `graphicsLayer` so children never recompose during entrance.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    delayStepMs: Long = 44L,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    var visible by remember(index, reducedMotion) { mutableStateOf(reducedMotion) }

    LaunchedEffect(index, reducedMotion) {
        if (!reducedMotion && !visible) {
            delay((index.coerceIn(0, 8)) * delayStepMs)
            visible = true
        }
    }

    val progressState = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (reducedMotion) tween(0) else tween(
            durationMillis = 420,
            easing = BaeumMotion.SilkEasing
        ),
        label = "stagger_entrance_$index"
    )

    Box(
        modifier = modifier.graphicsLayer {
            val progress = progressState.value
            alpha = progress
            translationY = (1f - progress) * 18.dp.toPx()
            val sc = 0.978f + (0.022f * progress)
            scaleX = sc
            scaleY = sc
        }
    ) {
        content()
    }
}

private val SEOUL_CONSTELLATION_POINTS = listOf(
    Triple(0.14f, 0.08f, 1.2f),
    Triple(0.32f, 0.16f, 0.9f),
    Triple(0.76f, 0.06f, 1.4f),
    Triple(0.88f, 0.21f, 1.0f),
    Triple(0.22f, 0.34f, 0.9f),
    Triple(0.64f, 0.28f, 1.1f),
    Triple(0.91f, 0.48f, 0.95f),
    Triple(0.09f, 0.62f, 1.15f),
    Triple(0.52f, 0.72f, 0.85f),
    Triple(0.82f, 0.86f, 1.25f)
)

/**
 * Calm, zero-recomposition radial silk-aurora canvas behind screens using `drawWithCache`.
 * Caches gradients once per viewport size & theme so the entire navigation tree renders at 0% idle CPU.
 */
@Composable
fun SeoulAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val extended = LocalBaeumColors.current
    val bgColor = MaterialTheme.colorScheme.background
    val glowPrimary = extended.ambientGlowPrimary
    val glowSecondary = extended.ambientGlowSecondary
    val glowGold = extended.ambientGlowGold
    val isDark = extended.isDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawWithCache {
                val w = size.width.coerceAtLeast(1f)
                val h = size.height.coerceAtLeast(1f)

                val orb1Center = Offset(w * 0.86f, h * 0.10f)
                val orb1Radius = w * 0.96f
                val orb1Brush = Brush.radialGradient(
                    colors = listOf(glowPrimary, Color.Transparent),
                    center = orb1Center,
                    radius = orb1Radius
                )

                val orb2Center = Offset(w * 0.10f, h * 0.46f)
                val orb2Radius = w * 0.86f
                val orb2Brush = Brush.radialGradient(
                    colors = listOf(glowSecondary, Color.Transparent),
                    center = orb2Center,
                    radius = orb2Radius
                )

                val orb3Center = Offset(w * 0.88f, h * 0.86f)
                val orb3Radius = w * 0.74f
                val orb3Brush = Brush.radialGradient(
                    colors = listOf(glowGold, Color.Transparent),
                    center = orb3Center,
                    radius = orb3Radius
                )

                val starColor = Color(0xFFE7E2FF).copy(alpha = 0.22f)
                val cachedStars = if (isDark) {
                    SEOUL_CONSTELLATION_POINTS.map { (fx, fy, r) ->
                        Offset(w * fx, h * fy) to r.dp.toPx()
                    }
                } else {
                    emptyList()
                }

                onDrawBehind {
                    drawCircle(brush = orb1Brush, radius = orb1Radius, center = orb1Center)
                    drawCircle(brush = orb2Brush, radius = orb2Radius, center = orb2Center)
                    drawCircle(brush = orb3Brush, radius = orb3Radius, center = orb3Center)
                    cachedStars.forEach { (center, radiusPx) ->
                        drawCircle(color = starColor, radius = radiusPx, center = center)
                    }
                }
            },
        content = content
    )
}
