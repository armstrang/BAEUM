package com.example.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Curated 120fps-grade motion language & spring physics primitives for Baeum's "Seoul Atelier" experience.
 * Every modifier defers animated values into GPU `graphicsLayer` or `drawWithCache` phases to keep
 * scrolling and transitions silky-smooth with zero per-frame recompositions.
 */
object BaeumMotion {
    /** Quintic ease-out curve for cinematic screen glides and hero transitions. */
    val SilkEasing = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f)

    /** Graceful decelerate curve for subtle optical fades and odometer counters. */
    val VelvetEasing = CubicBezierEasing(0.22f, 0.94f, 0.34f, 1.0f)

    /** Crisp tactile spring for button presses, tile selections, and chip toggles. */
    fun <T> silkSpring() = spring<T>(
        dampingRatio = 0.76f,
        stiffness = 440f
    )

    /** Expressive smooth spring for entrances, rings, and stately reveals. */
    fun <T> luxurySpring() = spring<T>(
        dampingRatio = 0.78f,
        stiffness = 260f
    )

    /** Fluid gliding spring for floating dock capsule, track slider, and swipe recovery. */
    fun <T> liquidSpring() = spring<T>(
        dampingRatio = 0.74f,
        stiffness = 310f
    )

    /** Playful elastic spring for reward stars, combo badges, and dock icon pops. */
    fun <T> playfulSpring() = spring<T>(
        dampingRatio = 0.58f,
        stiffness = 380f
    )

    /** Smooth spring for `animateContentSize` on expanding trays, hints, and card reveals. */
    fun sizeSpring() = spring<IntSize>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    /**
     * Parallax depth push transition when drilling into a detail/study screen.
     */
    fun routePushEnter(reducedMotion: Boolean): EnterTransition {
        if (reducedMotion) return fadeIn(tween(0))
        return slideInHorizontally(
            initialOffsetX = { (it * 0.26f).toInt() },
            animationSpec = tween(360, easing = SilkEasing)
        ) + fadeIn(tween(300, easing = SilkEasing)) +
            scaleIn(initialScale = 0.968f, animationSpec = tween(360, easing = SilkEasing))
    }

    fun routePushExit(reducedMotion: Boolean): ExitTransition {
        if (reducedMotion) return fadeOut(tween(0))
        return slideOutHorizontally(
            targetOffsetX = { -(it * 0.10f).toInt() },
            animationSpec = tween(340, easing = SilkEasing)
        ) + fadeOut(tween(260, easing = VelvetEasing)) +
            scaleOut(targetScale = 0.955f, animationSpec = tween(340, easing = SilkEasing))
    }

    fun routePopEnter(reducedMotion: Boolean): EnterTransition {
        if (reducedMotion) return fadeIn(tween(0))
        return slideInHorizontally(
            initialOffsetX = { -(it * 0.12f).toInt() },
            animationSpec = tween(340, easing = SilkEasing)
        ) + fadeIn(tween(280, easing = SilkEasing)) +
            scaleIn(initialScale = 0.958f, animationSpec = tween(340, easing = SilkEasing))
    }

    fun routePopExit(reducedMotion: Boolean): ExitTransition {
        if (reducedMotion) return fadeOut(tween(0))
        return slideOutHorizontally(
            targetOffsetX = { (it * 0.24f).toInt() },
            animationSpec = tween(340, easing = SilkEasing)
        ) + fadeOut(tween(240, easing = VelvetEasing)) +
            scaleOut(targetScale = 0.968f, animationSpec = tween(340, easing = SilkEasing))
    }

    /**
     * Directional shared-axis transition when switching top-level dock tabs.
     */
    fun tabEnter(forward: Boolean, reducedMotion: Boolean): EnterTransition {
        if (reducedMotion) return fadeIn(tween(0))
        val dir = if (forward) 1 else -1
        return slideInHorizontally(
            initialOffsetX = { (it * 0.11f * dir).toInt() },
            animationSpec = tween(290, easing = SilkEasing)
        ) + fadeIn(tween(250, easing = VelvetEasing)) +
            scaleIn(initialScale = 0.982f, animationSpec = tween(290, easing = SilkEasing))
    }

    fun tabExit(forward: Boolean, reducedMotion: Boolean): ExitTransition {
        if (reducedMotion) return fadeOut(tween(0))
        val dir = if (forward) -1 else 1
        return slideOutHorizontally(
            targetOffsetX = { (it * 0.08f * dir).toInt() },
            animationSpec = tween(240, easing = SilkEasing)
        ) + fadeOut(tween(190, easing = VelvetEasing)) +
            scaleOut(targetScale = 0.985f, animationSpec = tween(240, easing = SilkEasing))
    }

    /**
     * Silky vertical unfurl enter/exit for hints, explanations, solved bars, and callouts.
     */
    fun silkExpandEnter(reducedMotion: Boolean): EnterTransition {
        if (reducedMotion) return fadeIn(tween(0))
        return expandVertically(
            animationSpec = sizeSpring(),
            expandFrom = Alignment.Top
        ) + fadeIn(tween(240, easing = VelvetEasing)) +
            scaleIn(initialScale = 0.96f, animationSpec = silkSpring())
    }

    fun silkCollapseExit(reducedMotion: Boolean): ExitTransition {
        if (reducedMotion) return fadeOut(tween(0))
        return shrinkVertically(
            animationSpec = tween(200, easing = SilkEasing),
            shrinkTowards = Alignment.Top
        ) + fadeOut(tween(160)) +
            scaleOut(targetScale = 0.97f, animationSpec = tween(180))
    }

    /**
     * Unified directional slide + silk fade transition for stepped content (questions, steps, cards).
     * Automatically collapses to an instant cut when [reducedMotion] is enabled.
     */
    fun directionalContentTransform(
        forward: Boolean,
        reducedMotion: Boolean,
        durationMs: Int = 300
    ): ContentTransform {
        if (reducedMotion) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        val slideSpec = tween<IntOffset>(durationMs, easing = SilkEasing)
        val exitMs = (durationMs * 0.68f).toInt()
        val direction = if (forward) 1 else -1
        return (slideInHorizontally(slideSpec) { direction * (it * 0.16f).toInt() } +
            fadeIn(tween(durationMs, easing = SilkEasing)) +
            scaleIn(initialScale = 0.968f, animationSpec = tween(durationMs, easing = SilkEasing)))
            .togetherWith(
                slideOutHorizontally(tween(exitMs, easing = SilkEasing)) { -direction * (it * 0.11f).toInt() } +
                    fadeOut(tween(exitMs)) +
                    scaleOut(targetScale = 0.978f, animationSpec = tween(exitMs))
            )
    }

    /**
     * Subtle editorial crossfade for switching tabs, grammar cards, or speech levels.
     */
    fun subtleCrossfadeTransform(
        reducedMotion: Boolean,
        durationMs: Int = 260
    ): ContentTransform {
        if (reducedMotion) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        return (fadeIn(tween(durationMs, easing = VelvetEasing)) +
            scaleIn(initialScale = 0.976f, animationSpec = tween(durationMs, easing = SilkEasing)))
            .togetherWith(
                fadeOut(tween((durationMs * 0.68f).toInt())) +
                    scaleOut(targetScale = 1.015f, animationSpec = tween((durationMs * 0.68f).toInt()))
            )
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
 * Outer spring compression transform for interactive surfaces.
 * Place this BEFORE `.shadow().clip().background().border()` so the ENTIRE physical surface
 * (shadow, fill, specular bevel, and contents) compresses organically together at 120fps.
 * Pair with [tactileBindClick] AFTER `.clip(...)` so the ripple stays bounded inside rounded corners.
 */
fun Modifier.tactilePressScale(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.968f
): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (reducedMotion || !enabled) return@composed this

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgressState = animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = BaeumMotion.silkSpring(),
        label = "tactile_surface_press"
    )

    this.graphicsLayer {
        val p = pressProgressState.value
        val s = 1f - (1f - pressedScale) * p
        scaleX = s
        scaleY = s
        alpha = 1f - (0.045f * p)
    }
}

/**
 * Inner bounded ripple + haptic click binder paired with [tactilePressScale].
 */
fun Modifier.tactileBindClick(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    hapticEnabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val haptics = LocalHapticFeedback.current
    this.clickable(
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
 * Adds luxury tactile spring compression + gentle ripple + subtle haptic feedback to any clickable surface.
 * Uses a single `pressProgress` animation (0f..1f) read strictly in `graphicsLayer` draw phase
 * and bypasses animations altogether when [reducedMotion] is enabled or [enabled] is false.
 */
fun Modifier.tactileClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.968f,
    hapticEnabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .tactilePressScale(
            interactionSource = interactionSource,
            enabled = enabled,
            pressedScale = pressedScale
        )
        .tactileBindClick(
            interactionSource = interactionSource,
            enabled = enabled,
            hapticEnabled = hapticEnabled,
            onClick = onClick
        )
}

/**
 * Ultra-lightweight GPU-only gentle breathing pulse for active calls-to-action, streak badges,
 * or urgent Stage Boss countdown pills (zero recompositions, honors reducedMotion).
 */
fun Modifier.breathingPulse(
    enabled: Boolean = true,
    minScale: Float = 0.97f,
    maxScale: Float = 1.035f,
    durationMillis: Int = 1400
): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (!enabled || reducedMotion) return@composed this

    val infinite = rememberInfiniteTransition(label = "breathing_pulse")
    val scaleState = infinite.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    this.graphicsLayer {
        val s = scaleState.value
        scaleX = s
        scaleY = s
    }
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
                        size = Size(bandW * 2f, h)
                    )
                }
            }
        }
    }
}

/**
 * Triggers a crisp, realistic damped spring shake + subtle micro-tilt when [triggerKey] increments.
 */
fun Modifier.gentleShake(triggerKey: Int): Modifier = composed {
    val reducedMotion = LocalBaeumColors.current.reducedMotion
    if (reducedMotion) return@composed this
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
            val hops = listOf(-13f, 11f, -8f, 5f, -2.5f, 0f)
            hops.forEachIndexed { idx, target ->
                offsetX.animateTo(
                    targetValue = target,
                    animationSpec = if (idx == hops.lastIndex) {
                        BaeumMotion.silkSpring()
                    } else {
                        tween(durationMillis = 44, easing = FastOutSlowInEasing)
                    }
                )
            }
        }
    }

    this.graphicsLayer {
        val x = offsetX.value
        translationX = x
        rotationZ = (x * 0.055f).coerceIn(-0.9f, 0.9f)
    }
}

/**
 * Smooth spring-driven staggered choreography for cards and sections entering the viewport.
 * Defers progress reads into `graphicsLayer` so children never recompose during entrance,
 * and bypasses animation allocation completely when [reducedMotion] is enabled.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    delayStepMs: Long = 34L,
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
            val delayMs = (index.coerceIn(0, 6)) * delayStepMs
            if (delayMs > 0L) delay(delayMs)
            visible = true
        }
    }

    val progressState = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.80f,
            stiffness = 285f
        ),
        label = "stagger_entrance_$index"
    )

    Box(
        modifier = modifier.graphicsLayer {
            val progress = progressState.value.coerceIn(0f, 1.05f)
            alpha = progress.coerceIn(0f, 1f)
            translationY = (1f - progress) * 20.dp.toPx()
            val sc = 0.968f + (0.032f * progress)
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
 * Living, zero-allocation radial silk-aurora canvas behind screens using `drawWithCache` + GPU
 * `withTransform` drift. Caches gradients once per viewport size & theme so the entire navigation
 * tree glides softly at 120fps without recomposition overhead.
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
    val reducedMotion = extended.reducedMotion

    val driftPhaseState = if (reducedMotion) {
        remember { mutableFloatStateOf(0f) }
    } else {
        val infinite = rememberInfiniteTransition(label = "seoul_aurora_drift")
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "aurora_angle"
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawWithCache {
                val w = size.width.coerceAtLeast(1f)
                val h = size.height.coerceAtLeast(1f)
                val driftPx = 18.dp.toPx()

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

                val starColor = Color(0xFFE7E2FF).copy(alpha = 0.24f)
                val cachedStars = if (isDark) {
                    SEOUL_CONSTELLATION_POINTS.map { (fx, fy, r) ->
                        Offset(w * fx, h * fy) to r.dp.toPx()
                    }
                } else {
                    emptyList()
                }

                onDrawBehind {
                    val phase = driftPhaseState.value
                    val dx1 = if (reducedMotion) 0f else cos(phase) * driftPx
                    val dy1 = if (reducedMotion) 0f else sin(phase) * driftPx * 0.75f
                    val dx2 = if (reducedMotion) 0f else -sin(phase) * driftPx * 0.85f
                    val dy2 = if (reducedMotion) 0f else cos(phase) * driftPx * 0.65f

                    withTransform({ translate(left = dx1, top = dy1) }) {
                        drawCircle(brush = orb1Brush, radius = orb1Radius, center = orb1Center)
                    }
                    withTransform({ translate(left = dx2, top = dy2) }) {
                        drawCircle(brush = orb2Brush, radius = orb2Radius, center = orb2Center)
                    }
                    withTransform({ translate(left = -dx1 * 0.6f, top = -dy2 * 0.6f) }) {
                        drawCircle(brush = orb3Brush, radius = orb3Radius, center = orb3Center)
                    }
                    cachedStars.forEach { (center, radiusPx) ->
                        drawCircle(color = starColor, radius = radiusPx, center = center)
                    }
                }
            },
        content = content
    )
}
