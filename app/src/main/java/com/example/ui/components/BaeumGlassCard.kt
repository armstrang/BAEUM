package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.tactileClickable

/**
 * Visual depth presets for [BaeumGlassCard] tailored to dashboard widgets,
 * elevated hero panels, and interactive Korean flashcards.
 */
enum class BaeumGlassStyle(
    val defaultCornerRadius: Dp,
    val defaultBlurRadius: Dp,
    val defaultShadowElevation: Dp,
    val specularAlpha: Float,
    val orbIntensity: Float
) {
    SUBTLE(
        defaultCornerRadius = 22.dp,
        defaultBlurRadius = 12.dp,
        defaultShadowElevation = 6.dp,
        specularAlpha = 0.08f,
        orbIntensity = 0.14f
    ),
    STANDARD(
        defaultCornerRadius = 28.dp,
        defaultBlurRadius = 18.dp,
        defaultShadowElevation = 11.dp,
        specularAlpha = 0.12f,
        orbIntensity = 0.20f
    ),
    HERO(
        defaultCornerRadius = 30.dp,
        defaultBlurRadius = 24.dp,
        defaultShadowElevation = 18.dp,
        specularAlpha = 0.16f,
        orbIntensity = 0.26f
    ),
    FLASHCARD(
        defaultCornerRadius = 32.dp,
        defaultBlurRadius = 26.dp,
        defaultShadowElevation = 16.dp,
        specularAlpha = 0.18f,
        orbIntensity = 0.28f
    )
}

/**
 * Reusable frosted-glass card Composable for Baeum's "Seoul Night Atelier" design system.
 *
 * Uses a multi-layered glass architecture so foreground typography remains razor-sharp:
 * 1. Soft tinted elevation shadow (`shadow`) matching dark obsidian or light porcelain themes.
 * 2. Dedicated translucent frosted backdrop layer with radial luminous silk refraction and
 *    hardware-accelerated `Modifier.blur` on Android 12+ (graceful translucent gradient on API 24-30).
 * 3. Diagonal beveled specular glass border + top inner sheen band.
 * 4. Optional tactile spring-press compression and haptic feedback when [onClick] is supplied.
 */
@Composable
fun BaeumGlassCard(
    modifier: Modifier = Modifier,
    style: BaeumGlassStyle = BaeumGlassStyle.STANDARD,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = style.defaultCornerRadius,
    blurRadius: Dp = style.defaultBlurRadius,
    shadowElevation: Dp = style.defaultShadowElevation,
    containerColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    accentGlowColor: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val extended = LocalBaeumColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val useCustomFill = containerColor != Color.Unspecified
    val useCustomBorder = borderColor != Color.Unspecified

    val shadowTint = if (extended.isDark) {
        if (useCustomBorder) borderColor.copy(alpha = 0.34f) else Color(0x7A000000)
    } else {
        if (useCustomBorder) borderColor.copy(alpha = 0.28f) else Color(0x30161E48)
    }

    val baseFillBrush = if (useCustomFill) {
        Brush.verticalGradient(
            colors = listOf(containerColor, containerColor.copy(alpha = (containerColor.alpha * 0.94f).coerceIn(0f, 1f)))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(extended.glassFillTop, extended.glassFillBottom)
        )
    }

    val specularSheenBrush = if (extended.isDark) {
        Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = style.specularAlpha),
            0.38f to Color.White.copy(alpha = style.specularAlpha * 0.25f),
            1.0f to Color.Transparent
        )
    } else {
        Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = 0.55f),
            0.28f to Color.Transparent,
            1.0f to extended.cardBorder.copy(alpha = 0.08f)
        )
    }

    val borderBrush = if (useCustomBorder) {
        Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = if (extended.isDark) 0.45f else 0.72f),
                extended.cardBorder.copy(alpha = if (extended.isDark) 0.35f else 0.65f)
            )
        )
    } else if (extended.isDark) {
        Brush.linearGradient(
            colors = listOf(
                extended.glassHighlightBorder,
                extended.cardBorder.copy(alpha = 0.58f),
                extended.cardBorder.copy(alpha = 0.24f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                extended.cardBorder.copy(alpha = 0.95f),
                extended.glassHighlightBorder.copy(alpha = 0.80f),
                extended.cardBorder.copy(alpha = 0.85f)
            )
        )
    }

    val effectiveBlur = if (extended.reducedMotion) 0.dp else blurRadius
    val supportsRenderEffectBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && effectiveBlur > 0.dp
    val orbSecondary = extended.mint
    val borderWidth = if (extended.isDark) 1.dp else 1.15.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = if (extended.isDark) shadowElevation else (shadowElevation * 0.85f),
                shape = shape,
                ambientColor = shadowTint,
                spotColor = shadowTint
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.tactileClickable(pressedScale = 0.972f, onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        // Layer 1 & 2: Frosted glass backdrop (hardware blur isolated strictly to background layer)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(baseFillBrush)
                .then(
                    if (supportsRenderEffectBlur) {
                        Modifier.blur(
                            radius = effectiveBlur,
                            edgeTreatment = BlurredEdgeTreatment.Rectangle
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            // Subtle internal light refraction orbs inside the frosted glass
            Canvas(modifier = Modifier.matchParentSize()) {
                val topLeftOrb = Offset(size.width * 0.14f, size.height * 0.12f)
                val bottomRightOrb = Offset(size.width * 0.88f, size.height * 0.90f)
                val glowAlpha = if (extended.isDark) style.orbIntensity else style.orbIntensity * 0.34f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentGlowColor.copy(alpha = glowAlpha), Color.Transparent),
                        center = topLeftOrb,
                        radius = size.maxDimension * 0.62f
                    ),
                    radius = size.maxDimension * 0.62f,
                    center = topLeftOrb
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbSecondary.copy(alpha = glowAlpha * 0.58f), Color.Transparent),
                        center = bottomRightOrb,
                        radius = size.maxDimension * 0.54f
                    ),
                    radius = size.maxDimension * 0.54f,
                    center = bottomRightOrb
                )
            }
        }

        // Layer 3: Crisp specular top sheen & diagonal beveled glass border
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(specularSheenBrush)
                .border(width = borderWidth, brush = borderBrush, shape = shape)
        )

        // Layer 4: Unblurred foreground content
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
