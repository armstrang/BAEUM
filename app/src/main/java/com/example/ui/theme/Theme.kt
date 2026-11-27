package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class BaeumExtendedColors(
    val mint: Color,
    val cherryPink: Color,
    val gold: Color,
    val cardBorder: Color,
    val elevatedSurface: Color,
    val glassFillTop: Color,
    val glassFillBottom: Color,
    val glassHighlightBorder: Color,
    val buttonGradientStart: Color,
    val buttonGradientEnd: Color,
    val ambientGlowPrimary: Color,
    val ambientGlowSecondary: Color,
    val ambientGlowGold: Color,
    val dockSurface: Color,
    val isDark: Boolean,
    val reducedMotion: Boolean = false
)

val LocalBaeumColors = staticCompositionLocalOf {
    BaeumExtendedColors(
        mint = SeoulMint,
        cherryPink = SeoulCherryPink,
        gold = SeoulGold,
        cardBorder = SeoulBorderDark,
        elevatedSurface = SeoulElevated,
        glassFillTop = Color(0xF519223B),
        glassFillBottom = Color(0xF2121A2E),
        glassHighlightBorder = SeoulSpecularRimDark,
        buttonGradientStart = SeoulLavenderBright,
        buttonGradientEnd = SeoulLavenderDim,
        ambientGlowPrimary = Color(0x267B6BE6),
        ambientGlowSecondary = Color(0x1867D8CB),
        ambientGlowGold = Color(0x14F3C77B),
        dockSurface = Color(0xF512192D),
        isDark = true,
        reducedMotion = false
    )
}

private val SeoulNightColorScheme = darkColorScheme(
    primary = SeoulLavender,
    onPrimary = SeoulMidnight,
    primaryContainer = Color(0xFF252452),
    onPrimaryContainer = SeoulLavenderBright,
    secondary = SeoulMint,
    onSecondary = SeoulMidnight,
    secondaryContainer = Color(0xFF143A3F),
    onSecondaryContainer = SeoulMint,
    tertiary = SeoulGold,
    onTertiary = SeoulMidnight,
    tertiaryContainer = Color(0xFF3C301C),
    onTertiaryContainer = SeoulGold,
    background = SeoulMidnight,
    onBackground = SeoulMoonWhite,
    surface = SeoulNavy,
    onSurface = SeoulMoonWhite,
    surfaceVariant = SeoulElevated,
    onSurfaceVariant = SeoulMuted,
    outline = SeoulBorderDark,
    outlineVariant = Color(0xFF222D46),
    error = SeoulCherryPink,
    onError = SeoulMidnight,
    errorContainer = Color(0xFF421E2E),
    onErrorContainer = SeoulCherryPink
)

private val SeoulMorningColorScheme = lightColorScheme(
    primary = MorningPrimary,
    onPrimary = Color.White,
    primaryContainer = MorningPrimaryContainer,
    onPrimaryContainer = MorningPrimaryDeep,
    secondary = MorningMint,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F4EF),
    onSecondaryContainer = Color(0xFF06564E),
    tertiary = MorningGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFAEBD0),
    onTertiaryContainer = Color(0xFF6B3F00),
    background = MorningBackground,
    onBackground = MorningText,
    surface = MorningSurface,
    onSurface = MorningText,
    surfaceVariant = MorningSubtle,
    onSurfaceVariant = MorningSecondaryText,
    outline = MorningBorderLight,
    outlineVariant = Color(0xFFE3DFF2),
    error = MorningCherryPink,
    onError = Color.White,
    errorContainer = Color(0xFFFFDEE8),
    onErrorContainer = Color(0xFF881336)
)

private val BaeumShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

enum class ThemeMode { SYSTEM, DARK, LIGHT }

@Composable
fun BaeumTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    fontScale: Float = 1.0f,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) SeoulNightColorScheme else SeoulMorningColorScheme
    val extendedColors = remember(useDark, reducedMotion) {
        if (useDark) {
            BaeumExtendedColors(
                mint = SeoulMint,
                cherryPink = SeoulCherryPink,
                gold = SeoulGold,
                cardBorder = SeoulBorderDark,
                elevatedSurface = SeoulElevated,
                glassFillTop = Color(0xF519223B),
                glassFillBottom = Color(0xF2121A2E),
                glassHighlightBorder = SeoulSpecularRimDark,
                buttonGradientStart = SeoulLavenderBright,
                buttonGradientEnd = SeoulLavenderDim,
                ambientGlowPrimary = Color(0x267B6BE6),
                ambientGlowSecondary = Color(0x1867D8CB),
                ambientGlowGold = Color(0x14F3C77B),
                dockSurface = Color(0xF512192D),
                isDark = true,
                reducedMotion = reducedMotion
            )
        } else {
            BaeumExtendedColors(
                mint = MorningMint,
                cherryPink = MorningCherryPink,
                gold = MorningGold,
                cardBorder = MorningBorderLight,
                elevatedSurface = MorningSubtle,
                glassFillTop = Color(0xFFFFFFFF),
                glassFillBottom = Color(0xFFFBFBFF),
                glassHighlightBorder = MorningSpecularRimLight,
                buttonGradientStart = MorningPrimary,
                buttonGradientEnd = MorningPrimaryDeep,
                ambientGlowPrimary = Color(0x145B46C8),
                ambientGlowSecondary = Color(0x0E0B7469),
                ambientGlowGold = Color(0x0C8C5300),
                dockSurface = Color(0xFCFFFFFF),
                isDark = false,
                reducedMotion = reducedMotion
            )
        }
    }
    val typography = remember(fontScale) { buildBaeumTypography(fontScale) }

    CompositionLocalProvider(LocalBaeumColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = BaeumShapes,
            content = content
        )
    }
}
