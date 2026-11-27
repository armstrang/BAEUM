package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BaeumTheme
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.tactileBindClick
import com.example.ui.theme.tactilePressScale

/**
 * Visual depth presets for [BaeumGlassCard] tailored to dashboard widgets,
 * elevated hero panels, and interactive Korean flashcards following Blueprint v1.0.
 */
enum class BaeumGlassStyle(
    val defaultCornerRadius: Dp,
    val defaultBlurRadius: Dp,
    val defaultShadowElevation: Dp,
    val specularAlpha: Float,
    val orbIntensity: Float
) {
    SUBTLE(
        defaultCornerRadius = 18.dp,
        defaultBlurRadius = 0.dp,
        defaultShadowElevation = 4.dp,
        specularAlpha = 0.05f,
        orbIntensity = 0.08f
    ),
    STANDARD(
        defaultCornerRadius = 24.dp,
        defaultBlurRadius = 0.dp,
        defaultShadowElevation = 8.dp,
        specularAlpha = 0.08f,
        orbIntensity = 0.12f
    ),
    HERO(
        defaultCornerRadius = 26.dp,
        defaultBlurRadius = 0.dp,
        defaultShadowElevation = 14.dp,
        specularAlpha = 0.12f,
        orbIntensity = 0.18f
    ),
    FLASHCARD(
        defaultCornerRadius = 28.dp,
        defaultBlurRadius = 0.dp,
        defaultShadowElevation = 14.dp,
        specularAlpha = 0.13f,
        orbIntensity = 0.18f
    )
}

/**
 * Crisp Seoul Atelier layered card Composable aligned with Baeum UI/UX Blueprint v1.0.
 *
 * Avoids heavy real-time `Modifier.blur` passes so LazyColumn scrolling stays locked at 60/120fps:
 * 1. Soft tinted elevation shadow (`shadow`) tuned for Seoul Night obsidian or Seoul Morning porcelain.
 * 2. Cached `drawWithCache` vertical silk surface gradient + whisper-soft internal ambient sheen.
 * 3. Memoized hairline specular beveled border (`1.dp`) keeping foreground Hangul typography razor-sharp.
 * 4. Optional tactile spring-press compression and haptic feedback when [onClick] is supplied.
 */
@Composable
fun BaeumGlassCard(
    modifier: Modifier = Modifier,
    style: BaeumGlassStyle = BaeumGlassStyle.STANDARD,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = style.defaultCornerRadius,
    @Suppress("UNUSED_PARAMETER") blurRadius: Dp = style.defaultBlurRadius,
    shadowElevation: Dp = style.defaultShadowElevation,
    containerColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    accentGlowColor: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val extended = LocalBaeumColors.current
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val useCustomFill = containerColor != Color.Unspecified
    val useCustomBorder = borderColor != Color.Unspecified

    val isDark = extended.isDark
    val shadowTint = remember(isDark, useCustomBorder, borderColor) {
        if (isDark) {
            if (useCustomBorder) borderColor.copy(alpha = 0.28f) else Color(0x66000000)
        } else {
            if (useCustomBorder) borderColor.copy(alpha = 0.22f) else Color(0x22192138)
        }
    }

    val topFill = if (useCustomFill) containerColor else extended.glassFillTop
    val bottomFill = if (useCustomFill) {
        containerColor.copy(alpha = (containerColor.alpha * 0.94f).coerceIn(0f, 1f))
    } else {
        extended.glassFillBottom
    }
    val orbSecondary = extended.mint
    val borderWidth = 1.dp

    val borderBrush = remember(useCustomBorder, borderColor, isDark, extended.glassHighlightBorder, extended.cardBorder) {
        if (useCustomBorder) {
            Brush.linearGradient(
                colors = listOf(
                    borderColor,
                    borderColor.copy(alpha = if (isDark) 0.50f else 0.76f),
                    extended.cardBorder.copy(alpha = if (isDark) 0.40f else 0.70f)
                )
            )
        } else if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    extended.glassHighlightBorder,
                    extended.cardBorder.copy(alpha = 0.65f),
                    extended.cardBorder.copy(alpha = 0.30f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    extended.cardBorder,
                    extended.glassHighlightBorder,
                    extended.cardBorder.copy(alpha = 0.85f)
                )
            )
        }
    }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.tactilePressScale(
                        interactionSource = interactionSource,
                        enabled = true,
                        pressedScale = 0.972f
                    )
                } else {
                    Modifier
                }
            )
            .shadow(
                elevation = if (isDark) shadowElevation else (shadowElevation * 0.72f),
                shape = shape,
                ambientColor = shadowTint,
                spotColor = shadowTint
            )
            .clip(shape)
            .drawWithCache {
                val baseFillBrush = Brush.verticalGradient(colors = listOf(topFill, bottomFill))
                val topLeftOrb = Offset(size.width * 0.14f, size.height * 0.12f)
                val bottomRightOrb = Offset(size.width * 0.88f, size.height * 0.90f)
                val maxDim = size.maxDimension.coerceAtLeast(1f)
                val glowAlpha = if (isDark) style.orbIntensity else style.orbIntensity * 0.28f

                val orb1Brush = Brush.radialGradient(
                    colors = listOf(accentGlowColor.copy(alpha = glowAlpha), Color.Transparent),
                    center = topLeftOrb,
                    radius = maxDim * 0.64f
                )
                val orb2Brush = Brush.radialGradient(
                    colors = listOf(orbSecondary.copy(alpha = glowAlpha * 0.55f), Color.Transparent),
                    center = bottomRightOrb,
                    radius = maxDim * 0.52f
                )
                val sheenBrush = if (isDark) {
                    Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = style.specularAlpha),
                        0.32f to Color.White.copy(alpha = style.specularAlpha * 0.22f),
                        1.0f to Color.Transparent
                    )
                } else {
                    Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.45f),
                        0.26f to Color.Transparent,
                        1.0f to extended.cardBorder.copy(alpha = 0.06f)
                    )
                }
                val topRimBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (isDark) style.specularAlpha * 1.4f else 0.65f),
                        Color.Transparent
                    )
                )

                onDrawBehind {
                    drawRect(brush = baseFillBrush)
                    drawCircle(brush = orb1Brush, radius = maxDim * 0.64f, center = topLeftOrb)
                    drawCircle(brush = orb2Brush, radius = maxDim * 0.52f, center = bottomRightOrb)
                    drawRect(brush = sheenBrush)
                    drawLine(
                        brush = topRimBrush,
                        start = Offset(size.width * 0.12f, 1f),
                        end = Offset(size.width * 0.88f, 1f),
                        strokeWidth = 1.5f
                    )
                }
            }
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.tactileBindClick(
                        interactionSource = interactionSource,
                        enabled = true,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060914)
@Composable
private fun BaeumGlassCardDarkPreview() {
    BaeumTheme(themeMode = ThemeMode.DARK) {
        BaeumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            style = BaeumGlassStyle.FLASHCARD
        ) {
            Text(
                text = "배움",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Learning • Frosted Seoul Night Glass",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F4FA)
@Composable
private fun BaeumGlassCardLightPreview() {
    BaeumTheme(themeMode = ThemeMode.LIGHT) {
        BaeumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            style = BaeumGlassStyle.STANDARD
        ) {
            Text(
                text = "오늘의 목표",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "15 / 20 words reviewed today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
