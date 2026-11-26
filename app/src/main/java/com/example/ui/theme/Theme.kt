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
        glassFillTop = Color(0xE8161F38),
        glassFillBottom = Color(0xE00E1528),
        glassHighlightBorder = SeoulSpecularRimDark,
        buttonGradientStart = SeoulLavenderBright,
        buttonGradientEnd = SeoulLavenderDim,
        ambientGlowPrimary = Color(0x2E7A68E8),
        ambientGlowSecondary = Color(0x1E6BE3D5),
        ambientGlowGold = Color(0x18F6D38A),
        dockSurface = Color(0xEB0E1426),
        isDark = true,
        reducedMotion = false
    )
}

private val SeoulNightColorScheme = darkColorScheme(
    primary = SeoulLavender,
    onPrimary = SeoulMidnight,
    primaryContainer = Color(0xFF24234E),
    onPrimaryContainer = SeoulLavenderBright,
    secondary = SeoulMint,
    onSecondary = SeoulMidnight,
    secondaryContainer = Color(0xFF12363B),
    onSecondaryContainer = SeoulMint,
    tertiary = SeoulGold,
    onTertiary = SeoulMidnight,
    tertiaryContainer = Color(0xFF392E1C),
    onTertiaryContainer = SeoulGold,
    background = SeoulMidnight,
    onBackground = SeoulMoonWhite,
    surface = SeoulNavy,
    onSurface = SeoulMoonWhite,
    surfaceVariant = SeoulElevated,
    onSurfaceVariant = SeoulMuted,
    outline = SeoulBorderDark,
    outlineVariant = Color(0xFF1D2640),
    error = SeoulCherryPink,
    onError = SeoulMidnight,
    errorContainer = Color(0xFF3F1D2C),
    onErrorContainer = SeoulCherryPink
)

private val SeoulMorningColorScheme = lightColorScheme(
    primary = MorningPrimary,
    onPrimary = Color.White,
    primaryContainer = MorningPrimaryContainer,
    onPrimaryContainer = MorningPrimaryDeep,
    secondary = MorningMint,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F3EE),
    onSecondaryContainer = Color(0xFF065951),
    tertiary = MorningGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFAE8C3),
    onTertiaryContainer = Color(0xFF704300),
    background = MorningBackground,
    onBackground = MorningText,
    surface = MorningSurface,
    onSurface = MorningText,
    surfaceVariant = MorningSubtle,
    onSurfaceVariant = MorningSecondaryText,
    outline = MorningBorderLight,
    outlineVariant = Color(0xFFD6D1EA),
    error = MorningCherryPink,
    onError = Color.White,
    errorContainer = Color(0xFFFFDCE6),
    onErrorContainer = Color(0xFF8C1437)
)

private val BaeumShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
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
    val extendedColors = if (useDark) {
        BaeumExtendedColors(
            mint = SeoulMint,
            cherryPink = SeoulCherryPink,
            gold = SeoulGold,
            cardBorder = SeoulBorderDark,
            elevatedSurface = SeoulElevated,
            glassFillTop = Color(0xEB17203A),
            glassFillBottom = Color(0xE00E1529),
            glassHighlightBorder = SeoulSpecularRimDark,
            buttonGradientStart = SeoulLavenderBright,
            buttonGradientEnd = SeoulLavenderDim,
            ambientGlowPrimary = Color(0x2E7A68E8),
            ambientGlowSecondary = Color(0x1E6BE3D5),
            ambientGlowGold = Color(0x18F6D38A),
            dockSurface = Color(0xF00D1325),
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
            glassFillBottom = Color(0xFFF8F7FD),
            glassHighlightBorder = MorningSpecularRimLight,
            buttonGradientStart = MorningPrimary,
            buttonGradientEnd = MorningPrimaryDeep,
            ambientGlowPrimary = Color(0x18523DC4),
            ambientGlowSecondary = Color(0x120A7D72),
            ambientGlowGold = Color(0x109C6000),
            dockSurface = Color(0xFAFFFFFF),
            isDark = false,
            reducedMotion = reducedMotion
        )
    }

    CompositionLocalProvider(LocalBaeumColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildBaeumTypography(fontScale),
            shapes = BaeumShapes,
            content = content
        )
    }
}
