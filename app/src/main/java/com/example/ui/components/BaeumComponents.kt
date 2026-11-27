package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.DayForecastPoint
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.tactileClickable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val ButtonPillShape = RoundedCornerShape(18.dp)
private val BrandMonogramShape = RoundedCornerShape(7.dp)
private val ForecastColumnShape = RoundedCornerShape(14.dp)

/**
 * Sculpted Seoul Atelier surface card with cached silk gradient, crisp hairline rim,
 * balanced rounded corners, and tactile spring press physics when interactive.
 */
@Composable
fun SeoulCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    BaeumGlassCard(
        modifier = modifier,
        style = BaeumGlassStyle.STANDARD,
        onClick = onClick,
        cornerRadius = cornerRadius,
        containerColor = containerColor,
        borderColor = borderColor,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Refined Seoul Atelier primary CTA button with smooth amethyst gradient, hairline specular top edge,
 * calm colored elevation, and tactile spring compression.
 */
@Composable
fun BaeumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val disabledSurface = MaterialTheme.colorScheme.surfaceVariant

    val bgBrush = remember(enabled, extended.buttonGradientStart, primary, extended.buttonGradientEnd, disabledSurface) {
        if (enabled) {
            Brush.horizontalGradient(
                colors = listOf(
                    extended.buttonGradientStart,
                    primary,
                    extended.buttonGradientEnd
                )
            )
        } else {
            val disabledBg = disabledSurface.copy(alpha = 0.65f)
            Brush.horizontalGradient(listOf(disabledBg, disabledBg))
        }
    }

    val rimBrush = remember(enabled) {
        if (enabled) {
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.34f), Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        }
    }

    val glowColor = if (enabled) primary.copy(alpha = 0.36f) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = ButtonPillShape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(ButtonPillShape)
            .background(bgBrush)
            .border(width = 1.dp, brush = rimBrush, shape = ButtonPillShape)
            .tactileClickable(enabled = enabled, pressedScale = 0.972f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.2.sp
            ),
            color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

/**
 * Calm layered secondary button with crisp hairline border and tactile spring press.
 */
@Composable
fun BaeumSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val extended = LocalBaeumColors.current

    val fillBrush = remember(extended.isDark, enabled, extended.elevatedSurface, extended.glassFillBottom) {
        if (extended.isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    extended.elevatedSurface.copy(alpha = if (enabled) 0.92f else 0.45f),
                    extended.glassFillBottom.copy(alpha = if (enabled) 0.96f else 0.45f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (enabled) 1f else 0.55f),
                    extended.glassFillBottom.copy(alpha = if (enabled) 1f else 0.55f)
                )
            )
        }
    }
    val borderBrush = remember(extended.isDark, enabled, extended.glassHighlightBorder, extended.cardBorder) {
        Brush.linearGradient(
            colors = listOf(
                if (extended.isDark) extended.glassHighlightBorder.copy(alpha = if (enabled) 0.85f else 0.25f)
                else extended.cardBorder.copy(alpha = if (enabled) 1f else 0.4f),
                extended.cardBorder.copy(alpha = if (extended.isDark) 0.55f else 0.85f)
            )
        )
    }
    val lightShadow = if (extended.isDark) Color.Transparent else Color(0x18192138)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .shadow(
                elevation = if (!extended.isDark && enabled) 4.dp else 0.dp,
                shape = ButtonPillShape,
                ambientColor = lightShadow,
                spotColor = lightShadow
            )
            .clip(ButtonPillShape)
            .background(fillBrush)
            .border(width = 1.dp, brush = borderBrush, shape = ButtonPillShape)
            .tactileClickable(enabled = enabled, pressedScale = 0.974f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp
            ),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

/**
 * Editorial Seoul Atelier brand header with crisp Hangul monogram seal (`배`) and circular settings pill.
 */
@Composable
fun BaeumBrandHeader(
    title: String,
    subtitle: String,
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val sealBrush = remember(primary, extended.mint) {
        Brush.linearGradient(listOf(primary, extended.mint))
    }
    val iconFillBrush = remember(extended.glassFillTop, extended.glassFillBottom) {
        Brush.verticalGradient(listOf(extended.glassFillTop, extended.glassFillBottom))
    }
    val iconBorderBrush = remember(extended.isDark, extended.glassHighlightBorder, extended.cardBorder) {
        Brush.linearGradient(
            if (extended.isDark) {
                listOf(extended.glassHighlightBorder, extended.cardBorder.copy(alpha = 0.5f))
            } else {
                listOf(extended.cardBorder, extended.cardBorder.copy(alpha = 0.8f))
            }
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(BrandMonogramShape)
                        .background(sealBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "배",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BAEUM  ·  배움",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onSettingsClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = if (extended.isDark) 0.dp else 5.dp,
                        shape = CircleShape,
                        ambientColor = Color(0x1C192138),
                        spotColor = Color(0x1C192138)
                    )
                    .clip(CircleShape)
                    .background(iconFillBrush)
                    .border(width = 1.dp, brush = iconBorderBrush, shape = CircleShape)
                    .tactileClickable(pressedScale = 0.92f, onClick = onSettingsClick)
                    .testTag("settings_icon_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

/**
 * Refined floating top navigation bar with 48dp circular back button and unclipped flexible trailing slot.
 */
@Composable
fun BaeumTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val extended = LocalBaeumColors.current
    val fillBrush = remember(extended.glassFillTop, extended.glassFillBottom) {
        Brush.verticalGradient(listOf(extended.glassFillTop, extended.glassFillBottom))
    }
    val borderBrush = remember(extended.isDark, extended.glassHighlightBorder, extended.cardBorder) {
        Brush.linearGradient(
            if (extended.isDark) {
                listOf(extended.glassHighlightBorder, extended.cardBorder.copy(alpha = 0.45f))
            } else {
                listOf(extended.cardBorder, extended.cardBorder.copy(alpha = 0.8f))
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = if (extended.isDark) 0.dp else 5.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x1C192138),
                    spotColor = Color(0x1C192138)
                )
                .clip(CircleShape)
                .background(fillBrush)
                .border(width = 1.dp, brush = borderBrush, shape = CircleShape)
                .tactileClickable(pressedScale = 0.90f, onClick = onBack)
                .testTag("topbar_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            trailing?.invoke()
        }
    }
}

/**
 * Luminous multi-stop gradient circular goal ring with soft ambient halo & specular tip glow.
 * Pre-caches sweep gradient & stroke styles via `drawWithCache` for zero per-frame allocations.
 */
@Composable
fun CircularGoalRing(
    progress: Float,
    centerLabel: String,
    centerSubLabel: String? = null,
    size: Dp = 90.dp,
    strokeWidth: Dp = 8.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    centerTextColor: Color = MaterialTheme.colorScheme.onSurface,
    centerSubTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trackColor: Color = LocalBaeumColors.current.elevatedSurface.copy(alpha = 0.85f)
) {
    val extended = LocalBaeumColors.current
    val animatedProgressState = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.luxurySpring(),
        label = "goal_ring"
    )
    val mintColor = extended.mint
    val goldColor = extended.gold

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size)
                .drawWithCache {
                    val strokePx = strokeWidth.toPx()
                    val arcSize = Size(this.size.width - strokePx * 1.4f, this.size.height - strokePx * 1.4f)
                    val topLeft = Offset(strokePx * 0.7f, strokePx * 0.7f)
                    val radius = arcSize.width / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val trackStroke = Stroke(width = strokePx, cap = StrokeCap.Round)
                    val haloStroke = Stroke(width = strokePx * 1.9f, cap = StrokeCap.Round)
                    val haloColor = accentColor.copy(alpha = 0.20f)
                    val tipColor = Color.White.copy(alpha = 0.88f)
                    val tipRadius = strokePx * 0.34f
                    val arcBrush = Brush.sweepGradient(
                        0.0f to accentColor,
                        0.5f to mintColor,
                        0.85f to goldColor,
                        1.0f to accentColor,
                        center = center
                    )

                    onDrawBehind {
                        val sweep = 360f * animatedProgressState.value
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = trackStroke
                        )

                        if (sweep > 0.5f) {
                            drawArc(
                                color = haloColor,
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = haloStroke
                            )
                            drawArc(
                                brush = arcBrush,
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = trackStroke
                            )
                            val endRad = (-90.0 + sweep) * (PI / 180.0)
                            val tipX = center.x + (radius * cos(endRad)).toFloat()
                            val tipY = center.y + (radius * sin(endRad)).toFloat()
                            drawCircle(
                                color = tipColor,
                                radius = tipRadius,
                                center = Offset(tipX, tipY)
                            )
                        }
                    }
                }
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = centerTextColor
            )
            if (centerSubLabel != null) {
                Text(
                    text = centerSubLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = centerSubTextColor
                )
            }
        }
    }
}

/**
 * Silky gradient pill progress bar with spring physics and zero per-frame allocations (`drawWithCache`).
 */
@Composable
fun ProgressBarSlim(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = LocalBaeumColors.current.elevatedSurface.copy(alpha = 0.85f),
    height: Dp = 7.dp
) {
    val extended = LocalBaeumColors.current
    val animState = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.silkSpring(),
        label = "slim_progress"
    )
    val endColor = if (color == MaterialTheme.colorScheme.primary) extended.mint else color.copy(alpha = 0.72f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawWithCache {
                val radius = size.height / 2f
                val corner = CornerRadius(radius, radius)
                val fillBrush = Brush.horizontalGradient(
                    colors = listOf(color, endColor),
                    startX = 0f,
                    endX = size.width.coerceAtLeast(1f)
                )
                val glintColor = Color.White.copy(alpha = 0.45f)

                onDrawBehind {
                    val anim = animState.value
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = corner,
                        size = size
                    )
                    val fillW = (size.width * anim).coerceAtLeast(if (anim > 0f) size.height else 0f)
                    if (fillW > 0f) {
                        clipRect(right = fillW) {
                            drawRoundRect(
                                brush = fillBrush,
                                cornerRadius = corner,
                                size = Size(fillW, size.height)
                            )
                        }
                        if (fillW > size.height * 1.4f) {
                            drawCircle(
                                color = glintColor,
                                radius = radius * 0.42f,
                                center = Offset(fillW - radius, radius)
                            )
                        }
                    }
                }
            }
    )
}

/**
 * Interactive 7-day FSRS forecast chart with zero-relayout GPU-drawn spring capsule bars.
 */
@Composable
fun SevenDayForecastChart(
    points: List<DayForecastPoint>,
    selectedOffset: Int? = null,
    onBarClick: (DayForecastPoint) -> Unit = {}
) {
    val maxDue = remember(points) { (points.maxOfOrNull { it.dueCount } ?: 0).coerceAtLeast(1) }
    val primary = MaterialTheme.colorScheme.primary
    val extended = LocalBaeumColors.current
    val mutedBar = extended.elevatedSurface
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val colorAnimMs = if (extended.reducedMotion) 0 else 200

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { pt ->
            val isHighlighted = selectedOffset == pt.dayOffset || (selectedOffset == null && pt.dayOffset == 0)
            val targetFraction = if (pt.dueCount == 0) 0.14f else (pt.dueCount.toFloat() / maxDue).coerceIn(0.18f, 1f)
            val barFractionState = animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.luxurySpring(),
                label = "forecast_bar_${pt.dayOffset}"
            )
            val labelColor by animateColorAsState(
                targetValue = if (isHighlighted) primary else mutedText,
                animationSpec = tween(colorAnimMs),
                label = "forecast_label_${pt.dayOffset}"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(ForecastColumnShape)
                    .background(
                        if (isHighlighted) {
                            primary.copy(alpha = if (extended.isDark) 0.12f else 0.08f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .then(
                        if (isHighlighted) {
                            Modifier.border(
                                width = 1.dp,
                                color = primary.copy(alpha = 0.28f),
                                shape = ForecastColumnShape
                            )
                        } else Modifier
                    )
                    .tactileClickable(pressedScale = 0.94f) { onBarClick(pt) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pt.dueCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(68.dp)
                        .width(26.dp)
                        .drawWithCache {
                            val barW = 20.dp.toPx().coerceAtMost(size.width)
                            val barX = (size.width - barW) / 2f
                            val radius = CornerRadius(barW / 2f, barW / 2f)
                            val rimStroke = Stroke(width = 1.dp.toPx())
                            val activeBrush = Brush.verticalGradient(
                                colors = listOf(primary, extended.mint.copy(alpha = 0.88f)),
                                startY = 0f,
                                endY = size.height
                            )
                            val inactiveBrush = Brush.verticalGradient(
                                colors = listOf(mutedBar.copy(alpha = 0.95f), mutedBar.copy(alpha = 0.55f)),
                                startY = 0f,
                                endY = size.height
                            )
                            val glowBrush = Brush.radialGradient(
                                colors = listOf(primary.copy(alpha = 0.30f), Color.Transparent),
                                center = Offset(size.width / 2f, size.height * 0.65f),
                                radius = size.width * 0.95f
                            )
                            val highlightRimColor = Color.White.copy(alpha = 0.35f)

                            onDrawBehind {
                                val frac = barFractionState.value.coerceIn(0.12f, 1f)
                                val barH = (size.height * frac).coerceAtLeast(8.dp.toPx())
                                val barTop = size.height - barH
                                if (isHighlighted) {
                                    drawCircle(
                                        brush = glowBrush,
                                        radius = size.width * 0.95f,
                                        center = Offset(size.width / 2f, barTop + barH * 0.5f)
                                    )
                                }
                                drawRoundRect(
                                    brush = if (isHighlighted) activeBrush else inactiveBrush,
                                    topLeft = Offset(barX, barTop),
                                    size = Size(barW, barH),
                                    cornerRadius = radius
                                )
                                if (isHighlighted) {
                                    drawRoundRect(
                                        color = highlightRimColor,
                                        topLeft = Offset(barX, barTop),
                                        size = Size(barW, barH),
                                        cornerRadius = radius,
                                        style = rimStroke
                                    )
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = pt.shortLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isHighlighted) MaterialTheme.colorScheme.onSurface else mutedText,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Studio-grade luminous acoustic waveform with organic exponential frame smoothing
 * and cached vertical gradient brushes (zero per-frame allocations).
 */
@Composable
fun RealWaveformBars(
    amplitudes: List<Float>,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val mint = extended.mint
    val gold = extended.gold
    val idleColor = extended.elevatedSurface
    val barsToShow = 24

    val targetLevels = remember(barsToShow) { FloatArray(barsToShow) { 0.14f } }
    val displayLevels = remember(barsToShow) { FloatArray(barsToShow) { 0.14f } }
    var frameTick by remember { mutableIntStateOf(0) }

    // Update target buffer without allocating intermediate lists
    val ampSize = amplitudes.size
    if (ampSize == 0) {
        for (i in 0 until barsToShow) targetLevels[i] = 0.14f
    } else if (ampSize >= barsToShow) {
        val offset = ampSize - barsToShow
        for (i in 0 until barsToShow) targetLevels[i] = amplitudes[offset + i].coerceIn(0.12f, 1.0f)
    } else {
        val pad = barsToShow - ampSize
        for (i in 0 until pad) targetLevels[i] = 0.12f
        for (i in 0 until ampSize) targetLevels[pad + i] = amplitudes[i].coerceIn(0.12f, 1.0f)
    }

    if (extended.reducedMotion) {
        for (i in 0 until barsToShow) displayLevels[i] = targetLevels[i]
    } else {
        LaunchedEffect(isActive, ampSize, amplitudes.lastOrNull()) {
            var settled = false
            while (!settled) {
                withFrameNanos {
                    var maxDiff = 0f
                    for (i in 0 until barsToShow) {
                        val diff = targetLevels[i] - displayLevels[i]
                        val step = diff * 0.28f
                        displayLevels[i] = if (abs(diff) < 0.008f) targetLevels[i] else displayLevels[i] + step
                        if (abs(diff) > maxDiff) maxDiff = abs(diff)
                    }
                    settled = maxDiff < 0.008f
                    frameTick++
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawWithCache {
                val primaryBrush = Brush.verticalGradient(listOf(primary, mint.copy(alpha = 0.72f)))
                val mintBrush = Brush.verticalGradient(listOf(mint, primary.copy(alpha = 0.72f)))
                val goldBrush = Brush.verticalGradient(listOf(gold, primary.copy(alpha = 0.75f)))
                val gap = size.width / (barsToShow * 2f)
                val barWidth = ((size.width / barsToShow) - gap).coerceAtLeast(3.5f)
                val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

                onDrawBehind {
                    @Suppress("UNUSED_VARIABLE")
                    val tick = frameTick
                    val showIdle = !isActive && ampSize == 0
                    for (idx in 0 until barsToShow) {
                        val amp = displayLevels[idx].coerceIn(0.12f, 1.0f)
                        val h = (size.height * amp).coerceAtLeast(6f)
                        val x = idx * (barWidth + gap) + gap / 2f
                        val y = (size.height - h) / 2f
                        if (showIdle) {
                            drawRoundRect(
                                color = idleColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, h),
                                cornerRadius = corner
                            )
                        } else {
                            val brush = when {
                                idx % 5 == 0 -> goldBrush
                                idx % 2 == 0 -> mintBrush
                                else -> primaryBrush
                            }
                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, h),
                                cornerRadius = corner
                            )
                        }
                    }
                }
            }
    )
}
