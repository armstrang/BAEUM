package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

val HangulFontFamily = PretendardFontFamily
val DisplayFontFamily = PretendardFontFamily
val BodyFontFamily = PretendardFontFamily

// Reserved strictly for Latin editorial mottos (contains Latin glyphs only)
val EditorialSerifFamily = FontFamily(
    Font(R.font.cormorant_garamond, FontWeight.Normal),
    Font(R.font.cormorant_garamond, FontWeight.Medium),
    Font(R.font.cormorant_garamond, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond, FontWeight.Bold)
)

fun buildBaeumTypography(scale: Float = 1.0f): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = (56 * scale).sp,
        lineHeight = (66 * scale).sp,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = (44 * scale).sp,
        lineHeight = (54 * scale).sp,
        letterSpacing = (-0.6).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (32 * scale).sp,
        lineHeight = (40 * scale).sp,
        letterSpacing = (-0.4).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = (28 * scale).sp,
        lineHeight = (35 * scale).sp,
        letterSpacing = (-0.45).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (22 * scale).sp,
        lineHeight = (29 * scale).sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (20 * scale).sp,
        lineHeight = (27 * scale).sp,
        letterSpacing = (-0.15).sp
    ),
    titleLarge = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (18 * scale).sp,
        lineHeight = (25 * scale).sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (16 * scale).sp,
        lineHeight = (23 * scale).sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (14 * scale).sp,
        lineHeight = (20 * scale).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (16 * scale).sp,
        lineHeight = (24 * scale).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (14.5 * scale).sp,
        lineHeight = (21.5 * scale).sp
    ),
    bodySmall = TextStyle(
        fontFamily = HangulFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (12.5 * scale).sp,
        lineHeight = (18 * scale).sp
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (14 * scale).sp,
        lineHeight = (18 * scale).sp,
        letterSpacing = 0.15.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = (12.5 * scale).sp,
        lineHeight = (16.5 * scale).sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = (11.5 * scale).sp,
        lineHeight = (15 * scale).sp,
        letterSpacing = 0.6.sp
    )
)
