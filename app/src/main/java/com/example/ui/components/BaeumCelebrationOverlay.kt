package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engagement.CelebrationBannerEvent
import com.example.domain.engagement.CelebrationKind
import com.example.domain.engagement.DojangStamp
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.tactileClickable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ToastShape = RoundedCornerShape(22.dp)
private val SealOuterShape = RoundedCornerShape(18.dp)
private val SealInnerShape = RoundedCornerShape(13.dp)
private val ComboPillShape = RoundedCornerShape(16.dp)

private const val CONFETTI_COUNT = 38

/**
 * Zero-allocation 60/120fps luxury Seoul Atelier gold/jade/violet/cherry particle burst.
 * Completely skips motion when `reducedMotion` is enabled.
 */
@Composable
fun LuxuryConfettiOverlay(
    triggerKey: Int,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    if (triggerKey <= 0 || extended.reducedMotion) return

    val primary = MaterialTheme.colorScheme.primary
    val palette = remember(primary, extended.gold, extended.mint, extended.cherryPink) {
        listOf(
            extended.gold,
            extended.mint,
            primary,
            extended.cherryPink,
            Color(0xFFFFF1C1)
        )
    }

    // Pre-allocated flat arrays for zero GC pressure during 120fps burst
    val vx = remember(triggerKey) {
        val rng = Random(triggerKey * 997 + 17)
        FloatArray(CONFETTI_COUNT) {
            val angle = rng.nextDouble(-PI * 0.88, -PI * 0.12)
            val speed = rng.nextDouble(220.0, 680.0)
            (cos(angle) * speed).toFloat()
        }
    }
    val vy = remember(triggerKey) {
        val rng = Random(triggerKey * 997 + 31)
        FloatArray(CONFETTI_COUNT) {
            val angle = rng.nextDouble(-PI * 0.88, -PI * 0.12)
            val speed = rng.nextDouble(280.0, 780.0)
            (sin(angle) * speed).toFloat()
        }
    }
    val sizes = remember(triggerKey) {
        val rng = Random(triggerKey * 997 + 43)
        FloatArray(CONFETTI_COUNT) { rng.nextFloat() * 7f + 5f }
    }
    val spinSpeeds = remember(triggerKey) {
        val rng = Random(triggerKey * 997 + 59)
        FloatArray(CONFETTI_COUNT) { (rng.nextFloat() - 0.5f) * 520f }
    }
    val colorIdx = remember(triggerKey) {
        val rng = Random(triggerKey * 997 + 71)
        IntArray(CONFETTI_COUNT) { rng.nextInt(palette.size) }
    }

    var elapsedSec by remember(triggerKey) { mutableFloatStateOf(0f) }
    val durationSec = 1.65f

    LaunchedEffect(triggerKey) {
        elapsedSec = 0f
        var startNanos = 0L
        while (elapsedSec < durationSec) {
            withFrameNanos { frameNanos ->
                if (startNanos == 0L) startNanos = frameNanos
                elapsedSec = ((frameNanos - startNanos) / 1_000_000_000f).coerceAtMost(durationSec)
            }
        }
    }

    if (elapsedSec >= durationSec) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val originX = size.width * 0.5f
        val originY = size.height * 0.34f
        val t = elapsedSec
        val gravity = 980f
        val fade = (1f - (t / durationSec)).coerceIn(0f, 1f)
        val alpha = (fade * 1.35f).coerceIn(0f, 1f)

        for (i in 0 until CONFETTI_COUNT) {
            val px = originX + vx[i] * t
            val py = originY + vy[i] * t + 0.5f * gravity * t * t
            if (py > size.height + 40f || px < -40f || px > size.width + 40f) continue
            val s = sizes[i]
            val rot = spinSpeeds[i] * t
            val col = palette[colorIdx[i]].copy(alpha = alpha)
            withTransform({
                rotate(degrees = rot, pivot = Offset(px, py))
            }) {
                drawRoundRect(
                    color = col,
                    topLeft = Offset(px - s, py - s * 0.55f),
                    size = Size(s * 2f, s * 1.1f),
                    cornerRadius = CornerRadius(s * 0.4f, s * 0.4f)
                )
            }
        }
    }
}

/**
 * Draw-phase rising `+XP` / `+2s` silk feedback badge with zero relayout cost.
 */
@Composable
fun FloatingXpBadge(
    label: String,
    eventSeq: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    if (eventSeq <= 0 || label.isBlank()) return

    val progress = remember(eventSeq) { Animatable(0f) }
    LaunchedEffect(eventSeq) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(if (extended.reducedMotion) 250 else 850, easing = BaeumMotion.SilkEasing)
        )
    }

    val p = progress.value
    if (p >= 0.99f) return
    val alpha = when {
        p < 0.18f -> p / 0.18f
        p > 0.65f -> ((1f - p) / 0.35f).coerceIn(0f, 1f)
        else -> 1f
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = if (extended.reducedMotion) 0f else -42f * p
                val s = if (p < 0.2f && !extended.reducedMotion) 0.82f + p else 1f
                scaleX = s
                scaleY = s
            }
            .clip(ComboPillShape)
            .background(accentColor.copy(alpha = 0.22f))
            .border(1.dp, accentColor.copy(alpha = 0.65f), ComboPillShape)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = accentColor
        )
    }
}

/**
 * Animated Flow Combo Pill that shifts cleanly from Violet (2x-4x) -> Jade (5x-9x FLOW) -> Gold (10x+ FEVER).
 */
@Composable
fun ComboFlamePill(
    combo: Int,
    modifier: Modifier = Modifier,
    feverThreshold: Int = 5
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    if (combo < 2) return

    val isFever = combo >= feverThreshold
    val isGrand = combo >= 10
    val accent = when {
        isGrand -> extended.gold
        isFever -> extended.mint
        else -> primary
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (extended.reducedMotion) 1f else if (isFever) 1.06f else 1.0f,
        animationSpec = BaeumMotion.playfulSpring(),
        label = "combo_pill_scale"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .clip(ComboPillShape)
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.55f), ComboPillShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isFever) Icons.Filled.Bolt else Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = when {
                isGrand -> "${combo}x FEVER"
                isFever -> "${combo}x FLOW"
                else -> "${combo}x"
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp
            ),
            color = accent
        )
    }
}

/**
 * Non-blocking Seoul Atelier Obsidian-Gold celebration toast for Rank-Ups, Dojang Stamps,
 * Daily Sparks, Daily Seals, and New Blitz Records.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalCelebrationToast(
    event: CelebrationBannerEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    val topSafeInsets = WindowInsets.statusBars
        .union(WindowInsets.statusBarsIgnoringVisibility)
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top)
    val accent = when (event?.kind) {
        CelebrationKind.LEVEL_UP, CelebrationKind.DAILY_SEAL, CelebrationKind.NEW_BLITZ_RECORD -> extended.gold
        CelebrationKind.STAMP_UNLOCK -> extended.cherryPink
        CelebrationKind.DAILY_SPARK, CelebrationKind.STREAK_SHIELD_SAVED -> extended.mint
        null -> extended.gold
    }
    val confettiKey = remember(event?.id) {
        if (event != null && event.showConfetti) (event.id % 100_000L).toInt().coerceAtLeast(1) else 0
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (confettiKey > 0) {
            LuxuryConfettiOverlay(triggerKey = confettiKey)
        }

        AnimatedVisibility(
            visible = event != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(if (extended.reducedMotion) 0 else 360, easing = BaeumMotion.SilkEasing)
            ) + fadeIn(tween(if (extended.reducedMotion) 0 else 240)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(if (extended.reducedMotion) 0 else 240, easing = BaeumMotion.SilkEasing)
            ) + fadeOut(tween(if (extended.reducedMotion) 0 else 180))
        ) {
            val current = event ?: return@AnimatedVisibility
            val bgBrush = remember(extended.isDark, accent) {
                Brush.horizontalGradient(
                    if (extended.isDark) {
                        listOf(Color(0xF5151C2E), Color(0xF51E2640))
                    } else {
                        listOf(Color(0xFAFFFFFF), Color(0xFAF7F4FF))
                    }
                )
            }
            val borderBrush = remember(accent) {
                Brush.linearGradient(listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.30f)))
            }

            Box(
                modifier = Modifier
                    .windowInsetsPadding(topSafeInsets)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, ToastShape, ambientColor = accent.copy(alpha = 0.38f), spotColor = accent.copy(alpha = 0.38f))
                    .clip(ToastShape)
                    .background(bgBrush)
                    .border(1.2.dp, borderBrush, ToastShape)
                    .tactileClickable(pressedScale = 0.98f, onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Korean Atelier Seal Medallion
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accent.copy(alpha = 0.18f))
                            .border(1.2.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = current.badgeKo.take(2),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = accent,
                            maxLines = 1
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = bText(current.titleEn, current.titleId),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bText(current.subtitleEn, current.subtitleId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = bText("Dismiss", "Tutup"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Collectible Traditional Korean Seal Stamp (`도장 / Dojang`) card for the Analytics Stamp Vault.
 */
@Composable
fun DojangStampSeal(
    stamp: DojangStamp,
    unlocked: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val accent = when (stamp.accentTag) {
        "gold" -> extended.gold
        "mint" -> extended.mint
        "cherry" -> extended.cherryPink
        else -> primary
    }

    val fillBrush = remember(unlocked, accent, extended.elevatedSurface) {
        if (unlocked) {
            Brush.verticalGradient(listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f)))
        } else {
            Brush.verticalGradient(listOf(extended.elevatedSurface.copy(alpha = 0.55f), extended.elevatedSurface.copy(alpha = 0.30f)))
        }
    }

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 96.dp)
            .clip(SealOuterShape)
            .background(fillBrush)
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = when {
                    selected -> accent
                    unlocked -> accent.copy(alpha = 0.52f)
                    else -> extended.cardBorder.copy(alpha = 0.55f)
                },
                shape = SealOuterShape
            )
            .tactileClickable(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(SealInnerShape)
                .background(if (unlocked) accent.copy(alpha = 0.20f) else Color.Transparent)
                .border(
                    width = 1.2.dp,
                    color = if (unlocked) accent.copy(alpha = 0.80f) else extended.cardBorder.copy(alpha = 0.55f),
                    shape = SealInnerShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (unlocked) {
                Text(
                    text = stamp.sealKo.take(2),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = accent,
                    maxLines = 1
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = bText("Locked stamp", "Stempel terkunci"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = bText(stamp.titleEn, stamp.titleId),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
