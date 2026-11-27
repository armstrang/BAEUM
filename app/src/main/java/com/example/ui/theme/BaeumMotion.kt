package com.example.ui.theme

import androidx.compose.animation.ContentTransform
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
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

    /**
     * Unified directional slide + silk fade transition for stepped content (questions, steps, cards).
     * Automatically collapses to an instant cut when [reducedMotion] is enabled.
     */
    fun directionalContentTransform(
        forward: Boolean,
        reducedMotion: Boolean,
        durationMs: Int = 280
    ): ContentTransform {
        if (reducedMotion) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        val slideSpec = tween<IntOffset>(durationMs, easing = SilkEasing)
        val exitMs = (durationMs * 0.68f).toInt()
        val direction = if (forward) 1 else -1
        return (slideInHorizontally(slideSpec) { direction * (it * 0.14f).toInt() } +
            fadeIn(tween(durationMs, easing = SilkEasing)) +
            scaleIn(initialScale = 0.975f, animationSpec = tween(durationMs, easing = SilkEasing)))
            .togetherWith(
                slideOutHorizontally(tween(exitMs, easing = SilkEasing)) { -direction * (it * 0.10f).toInt() } +
                    fadeOut(tween(exitMs))
            )
    }

    /**
     * Subtle editorial crossfade for switching tabs, grammar cards, or speech levels.
     */
    fun subtleCrossfadeTransform(
        reducedMotion: Boolean,
        durationMs: Int = 220
    ): ContentTransform {
        if (reducedMotion) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        return (fadeIn(tween(durationMs, easing = VelvetEasing)) +
            scaleIn(initialScale = 0.985f, animationSpec = tween(durationMs, easing = VelvetEasing)))
            .togetherWith(fadeOut(tween((durationMs * 0.65f).toInt())))
    }
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
 * Uses a single `pressProgress` animation (0f..1f) read strictly in `graphicsLayer` draw phase
 * and bypasses animations altogether when [reducedMotion] is enabled or [enabled] is false.
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

    val clickModifier = Modifier.clickable(
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

    if (reducedMotion || !enabled) {
        return@composed this.then(clickModifier)
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgressState = animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = BaeumMotion.silkSpring(),
        label = "tactile_press"
    )

    this
        .graphicsLayer {
            val p = pressProgressState.value
            val s = 1f - (1f - pressedScale) * p
            scaleX = s
            scaleY = s
            alpha = 1f - (0.06f * p)
        }
        .then(clickModifier)
}

/**
 * Adds a delicate diagonal glass light-sweep glint across opt-in surfaces.
 * Pre-caches a single [Brush] inside `drawWithCache` and translates it per frame via GPU transform
 * for zero per-frame allocations while honoring reduced motion.
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

    this.drawWithCache {
        val w = size.width.coerceAtLeast(1f)
        val h = size.height.coerceAtLeast(1f)
        val bandW = w * 0.34f
        val cachedBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = shimmerAlpha),
                Color.Transparent
            ),
            start = Offset(-bandW, 0f),
            end = Offset(bandW, h)
        )
        onDrawWithContent {
            drawContent()
            val progress = progressState.value
            if (progress in -0.35f..1.35f) {
                val centerX = w * progress
                withTransform({ translate(left = centerX) }) {
                    drawRect(
                        brush = cachedBrush,
                        topLeft = Offset(-bandW, 0f),
                        size = androidx.compose.ui.geometry.Size(bandW * 2f, h)
                    )
                }
            }
        }
    }
}

/**
 * Triggers a gentle, crisp horizontal spring shake when [triggerKey] increments (e.g. incorrect quiz check).
 */
fun Modifier.gentleShake(triggerKey: Int): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (reducedMotion) return@composed this
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
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
 * Defers progress reads into `graphicsLayer` so children never recompose during entrance,
 * and bypasses animation allocation completely when [reducedMotion] is enabled.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    delayStepMs: Long = 40L,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (reducedMotion) {
        Box(modifier = modifier) { content() }
        return
    }

    var visible by remember(index) { mutableStateOf(false) }

    LaunchedEffect(index) {
        if (!visible) {
            val delayMs = (index.coerceIn(0, 8)) * delayStepMs
            if (delayMs > 0L) delay(delayMs)
            visible = true
        }
    }

    val progressState = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 380,
            easing = BaeumMotion.SilkEasing
        ),
        label = "stagger_entrance_$index"
    )

    Box(
        modifier = modifier.graphicsLayer {
            val progress = progressState.value
            alpha = progress
            translationY = (1f - progress) * 16.dp.toPx()
            val sc = 0.982f + (0.018f * progress)
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
