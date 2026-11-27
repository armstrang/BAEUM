package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.DayForecastPoint
import com.example.ui.components.BaeumBrandHeader
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SevenDayForecastChart
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.rememberAnimatedInt
import com.example.ui.theme.tactileClickable
import com.example.viewmodel.DashboardSummary
import java.util.Calendar

private enum class SeoulSkyAtmosphere(
    val labelKo: String,
    val labelEn: String,
    val labelId: String,
    val icon: ImageVector,
    val bannerRes: Int
) {
    MORNING("아침", "Morning", "Pagi", Icons.Filled.WbTwilight, R.drawable.img_seoul_morning_banner),
    DAY("낮", "Day", "Siang", Icons.Filled.WbSunny, R.drawable.img_seoul_day_banner),
    NIGHT("밤", "Night", "Malam", Icons.Filled.NightsStay, R.drawable.img_seoul_hero_banner);

    fun next(): SeoulSkyAtmosphere = when (this) {
        MORNING -> DAY
        DAY -> NIGHT
        NIGHT -> MORNING
    }
}

private data class HeroSkyPalette(
    val scrimColors: List<Color>,
    val borderColors: List<Color>,
    val eyebrowColor: Color,
    val countColor: Color,
    val subCountColor: Color,
    val streakBg: Color,
    val streakBorder: Color,
    val streakText: Color,
    val dueIconTint: Color,
    val dueText: Color,
    val ringAccent: Color,
    val ringTrack: Color,
    val skyPillBg: Color,
    val skyPillBorder: Color,
    val skyPillContent: Color
)

// SCREEN 03 / 24 — Home Dashboard (Living Seoul Sky Atelier Command Center)
@Composable
fun HomeDashboardScreen(
    summary: DashboardSummary,
    onOpenSettings: () -> Unit,
    onResumeLearning: () -> Unit,
    onReviewDue: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenVocabulary: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenQuizCenter: () -> Unit,
    onOpenGrammarStudio: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val scrollState = rememberScrollState()
    val goalProgress = (summary.todayWords.toFloat() / summary.dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val goalPercent = (goalProgress * 100).toInt()
    val animatedTodayWords by rememberAnimatedInt(summary.todayWords, label = "hero_today_words")
    val animatedGoalPercent by rememberAnimatedInt(goalPercent, label = "hero_goal_pct")
    val pathwayProgress = if (summary.activePathwayTotal > 0) {
        summary.activePathwayLearned.toFloat() / summary.activePathwayTotal
    } else 0f

    var selectedForecastDay by remember(summary.sevenDayForecast) {
        mutableStateOf<DayForecastPoint?>(summary.sevenDayForecast.firstOrNull())
    }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val defaultSky = remember(currentHour, extended.isDark) {
        when {
            currentHour in 5..10 -> SeoulSkyAtmosphere.MORNING
            currentHour in 11..16 -> SeoulSkyAtmosphere.DAY
            !extended.isDark -> SeoulSkyAtmosphere.MORNING
            else -> SeoulSkyAtmosphere.NIGHT
        }
    }
    var skyOverride by remember { mutableStateOf<SeoulSkyAtmosphere?>(null) }
    val activeSky = skyOverride ?: defaultSky

    val (greetingKo, greetingSub) = when (activeSky) {
        SeoulSkyAtmosphere.MORNING -> "좋은 아침이에요!" to bText(
            "Fresh Seoul morning momentum for Korean.",
            "Semangat pagi Seoul untuk belajar bahasa Korea."
        )
        SeoulSkyAtmosphere.DAY -> "안녕하세요!" to bText(
            "Bright afternoon focus for today's Korean.",
            "Fokus siang yang cerah untuk bahasa Korea hari ini."
        )
        SeoulSkyAtmosphere.NIGHT -> "좋은 저녁이에요!" to bText(
            "Unwind with a calm Seoul night session.",
            "Tenangkan diri dengan sesi belajar malam ala Seoul."
        )
    }

    val targetPalette = when (activeSky) {
        SeoulSkyAtmosphere.MORNING -> HeroSkyPalette(
            scrimColors = listOf(Color(0xEBFFF7F4), Color(0xC8F7EEFB), Color(0x58F6DDD5)),
            borderColors = listOf(Color(0xD8FFFFFF), Color(0x88E5B8D8), Color(0x66F5CBA7)),
            eyebrowColor = Color(0xFF5B46C8),
            countColor = Color(0xFF192138),
            subCountColor = Color(0xFF4E5A74),
            streakBg = Color(0x30D97B29),
            streakBorder = Color(0x66D97B29),
            streakText = Color(0xFF8C4600),
            dueIconTint = Color(0xFF0B7469),
            dueText = Color(0xFF165650),
            ringAccent = Color(0xFF5B46C8),
            ringTrack = Color(0x28192138),
            skyPillBg = Color(0xB8FFFFFF),
            skyPillBorder = Color(0xE0FFFFFF),
            skyPillContent = Color(0xFF4B37B0)
        )
        SeoulSkyAtmosphere.DAY -> HeroSkyPalette(
            scrimColors = listOf(Color(0xEBF3FAFF), Color(0xC6E8F4FD), Color(0x50D4ECFA)),
            borderColors = listOf(Color(0xE6FFFFFF), Color(0x887AC4F0), Color(0x667CE0D3)),
            eyebrowColor = Color(0xFF1A548E),
            countColor = Color(0xFF102238),
            subCountColor = Color(0xFF3E5670),
            streakBg = Color(0x30DF941A),
            streakBorder = Color(0x66D98B14),
            streakText = Color(0xFF824B00),
            dueIconTint = Color(0xFF0B7469),
            dueText = Color(0xFF12544F),
            ringAccent = Color(0xFF1D74B8),
            ringTrack = Color(0x26102238),
            skyPillBg = Color(0xB8FFFFFF),
            skyPillBorder = Color(0xE0FFFFFF),
            skyPillContent = Color(0xFF184F84)
        )
        SeoulSkyAtmosphere.NIGHT -> HeroSkyPalette(
            scrimColors = listOf(Color(0xEE0B1020), Color(0xCC111930), Color(0x8818223E)),
            borderColors = listOf(Color(0x50D8D0FF), Color(0x5CA99BFF), Color(0x3867D8CB)),
            eyebrowColor = Color(0xFFC4BAFF),
            countColor = Color(0xFFF4F5FC),
            subCountColor = Color(0xFFBAC4DF),
            streakBg = Color(0x2CF3C77B),
            streakBorder = Color(0x55F3C77B),
            streakText = Color(0xFFF9DC9E),
            dueIconTint = Color(0xFF67D8CB),
            dueText = Color(0xFFD5F5F0),
            ringAccent = Color(0xFFA99BFF),
            ringTrack = Color(0x5A2E3C5C),
            skyPillBg = Color(0x78151D32),
            skyPillBorder = Color(0x4AD8D0FF),
            skyPillContent = Color(0xFFE5DFFF)
        )
    }

    val colorMs = if (extended.reducedMotion) 0 else 400
    val eyebrowColor by animateColorAsState(targetPalette.eyebrowColor, tween(colorMs), label = "hero_eyebrow")
    val countColor by animateColorAsState(targetPalette.countColor, tween(colorMs), label = "hero_count")
    val subCountColor by animateColorAsState(targetPalette.subCountColor, tween(colorMs), label = "hero_subcount")
    val streakBg by animateColorAsState(targetPalette.streakBg, tween(colorMs), label = "hero_streak_bg")
    val streakBorder by animateColorAsState(targetPalette.streakBorder, tween(colorMs), label = "hero_streak_border")
    val streakText by animateColorAsState(targetPalette.streakText, tween(colorMs), label = "hero_streak_text")
    val dueIconTint by animateColorAsState(targetPalette.dueIconTint, tween(colorMs), label = "hero_due_icon")
    val dueText by animateColorAsState(targetPalette.dueText, tween(colorMs), label = "hero_due_text")
    val ringAccent by animateColorAsState(targetPalette.ringAccent, tween(colorMs), label = "hero_ring_accent")
    val ringTrack by animateColorAsState(targetPalette.ringTrack, tween(colorMs), label = "hero_ring_track")
    val skyPillBg by animateColorAsState(targetPalette.skyPillBg, tween(colorMs), label = "hero_sky_pill_bg")
    val skyPillBorder by animateColorAsState(targetPalette.skyPillBorder, tween(colorMs), label = "hero_sky_pill_border")
    val skyPillContent by animateColorAsState(targetPalette.skyPillContent, tween(colorMs), label = "hero_sky_pill_content")

    val heroShape = remember { RoundedCornerShape(26.dp) }
    val heroBorderBrush = remember(activeSky) { Brush.linearGradient(targetPalette.borderColors) }
    val heroScrimBrush = remember(activeSky) { Brush.horizontalGradient(colors = targetPalette.scrimColors) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
    Column(
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        StaggeredEntrance(index = 0) {
            BaeumBrandHeader(
                title = greetingKo,
                subtitle = greetingSub,
                onSettingsClick = onOpenSettings
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bespoke Living Seoul Sky Hero Card (Morning / Day / Night) with smooth Crossfade & Parallax
        StaggeredEntrance(index = 1) {
            val heroShadow = when {
                activeSky == SeoulSkyAtmosphere.NIGHT -> Color(0x80000000)
                extended.isDark -> Color(0x55000000)
                else -> Color(0x22192138)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 14.dp, shape = heroShape, ambientColor = heroShadow, spotColor = heroShadow)
                    .clip(heroShape)
                    .tactileClickable(pressedScale = 0.985f, onClick = onReviewDue)
                    .border(
                        width = 1.dp,
                        brush = heroBorderBrush,
                        shape = heroShape
                    )
            ) {
                Crossfade(
                    targetState = activeSky,
                    animationSpec = tween(if (extended.reducedMotion) 0 else 500, easing = BaeumMotion.SilkEasing),
                    modifier = Modifier.matchParentSize(),
                    label = "seoul_sky_banner_crossfade"
                ) { sky ->
                    Image(
                        painter = painterResource(id = sky.bannerRes),
                        contentDescription = "Seoul ${sky.labelEn.lowercase()} atmospheric backdrop",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (!extended.reducedMotion) {
                                    val parallax = (scrollState.value * 0.22f).coerceIn(0f, 68f)
                                    translationY = parallax
                                    scaleX = 1.08f
                                    scaleY = 1.08f
                                }
                            }
                    )
                }

                // Harmonized translucent sky scrim
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(heroScrimBrush)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp)
                ) {
                    // Top header row: DAILY GOAL eyebrow + Interactive Sky Mood Pill (>= 48dp touch target)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bText("DAILY GOAL", "TARGET HARIAN"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = eyebrowColor
                        )
                        val activeSkyLabel = bText(activeSky.labelEn, activeSky.labelId)
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .tactileClickable(pressedScale = 0.94f) {
                                    skyOverride = activeSky.next()
                                }
                                .testTag("home_sky_atmosphere_toggle"),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(skyPillBg)
                                    .border(1.dp, skyPillBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = activeSky.icon,
                                    contentDescription = bText("Switch sky atmosphere ($activeSkyLabel)", "Ganti suasana langit ($activeSkyLabel)"),
                                    tint = skyPillContent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${activeSky.labelKo} · $activeSkyLabel",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = skyPillContent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$animatedTodayWords",
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                    color = countColor
                                )
                                Text(
                                    text = bText(" / ${summary.dailyGoal} words", " / ${summary.dailyGoal} kata"),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = subCountColor,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Streak Pill Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(streakBg)
                                    .border(1.dp, streakBorder, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = bText("Study streak", "Runtutan belajar"),
                                    tint = streakText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = when {
                                        summary.streakDays > 0 && summary.longestStreakDays > summary.streakDays ->
                                            bText(
                                                "${summary.streakDays}-day streak · Best ${summary.longestStreakDays}d",
                                                "Runtutan ${summary.streakDays} hari · Rekor ${summary.longestStreakDays}h"
                                            )
                                        summary.streakDays > 0 ->
                                            bText("${summary.streakDays}-day streak", "Runtutan ${summary.streakDays} hari")
                                        summary.longestStreakDays > 0 ->
                                            bText(
                                                "Best ${summary.longestStreakDays}d · Start today",
                                                "Rekor ${summary.longestStreakDays}h · Mulai hari ini"
                                            )
                                        else -> bText("Start your streak today", "Mulai runtutan hari ini")
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = streakText
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Effortless 1-Tap Live Due Status Whisper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = dueIconTint,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (summary.dueTodayCount > 0) {
                                        bText(
                                            "${summary.dueTodayCount} review${if (summary.dueTodayCount == 1) "" else "s"} ready · Tap to begin",
                                            "${summary.dueTodayCount} ulasan siap · Ketuk untuk mulai"
                                        )
                                    } else {
                                        bText(
                                            "All due reviews complete · Tap to study",
                                            "Semua ulasan selesai · Ketuk untuk belajar"
                                        )
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = dueText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        CircularGoalRing(
                            progress = goalProgress,
                            centerLabel = "$animatedGoalPercent%",
                            centerSubLabel = bText("today", "hari ini"),
                            size = 94.dp,
                            strokeWidth = 8.5.dp,
                            accentColor = ringAccent,
                            centerTextColor = countColor,
                            centerSubTextColor = subCountColor,
                            trackColor = ringTrack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Quick Studio Shortcut Pebbles (Words • Cards • Quiz • Grammar)
        StaggeredEntrance(index = 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStudioPebble(
                    label = bText("Words", "Kosakata"),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accent = MaterialTheme.colorScheme.primary,
                    testTag = "shortcut_words",
                    onClick = onOpenVocabulary,
                    modifier = Modifier.weight(1f)
                )
                QuickStudioPebble(
                    label = bText("Cards", "Kartu"),
                    icon = Icons.Filled.Style,
                    accent = extended.mint,
                    testTag = "shortcut_cards",
                    onClick = onOpenFlashcards,
                    modifier = Modifier.weight(1f)
                )
                QuickStudioPebble(
                    label = bText("Quiz", "Kuis"),
                    icon = Icons.Filled.Psychology,
                    accent = extended.gold,
                    testTag = "shortcut_quiz",
                    onClick = onOpenQuizCenter,
                    modifier = Modifier.weight(1f)
                )
                QuickStudioPebble(
                    label = bText("Grammar", "Tata Bhs"),
                    icon = Icons.Filled.AutoStories,
                    accent = extended.cherryPink,
                    testTag = "shortcut_grammar",
                    onClick = onOpenGrammarStudio,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Active Curriculum Journey Card + Primary CTA
        StaggeredEntrance(index = 3) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bText("Continue learning", "Lanjutkan belajar"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "오늘도 한 걸음",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                BaeumGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = BaeumGlassStyle.STANDARD,
                    onClick = onResumeLearning
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bText("ACTIVE CURRICULUM", "KURIKULUM AKTIF"),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = bText("Daily vocabulary", "Kosakata harian"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = summary.activePathwayLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (summary.activePathwayLearned == 0) {
                            bText(
                                "0 / ${summary.activePathwayTotal} words studied — begin your first set",
                                "0 / ${summary.activePathwayTotal} kata dipelajari — mulai set pertamamu"
                            )
                        } else {
                            bText(
                                "${summary.activePathwayLearned} / ${summary.activePathwayTotal} words studied",
                                "${summary.activePathwayLearned} / ${summary.activePathwayTotal} kata dipelajari"
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProgressBarSlim(progress = pathwayProgress)
                }

                Spacer(modifier = Modifier.height(14.dp))

                BaeumPrimaryButton(
                    text = if (summary.activePathwayLearned == 0) bText("Start learning", "Mulai belajar") else bText("Resume learning", "Lanjutkan belajar"),
                    onClick = onResumeLearning,
                    testTag = "home_resume_learning_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Interactive 7-Day FSRS Review Forecast Card
        StaggeredEntrance(index = 4) {
            Column {
                BaeumGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = BaeumGlassStyle.STANDARD,
                    accentGlowColor = extended.mint
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bText("FSRS FORECAST", "PREDIKSI FSRS"),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = bText("Next 7 days", "7 hari ke depan"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .tactileClickable(pressedScale = 0.94f, onClick = onOpenReviewPlanner),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bText("Planner", "Jadwal"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = bText("Open review planner", "Buka perencana ulasan"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    SevenDayForecastChart(
                        points = summary.sevenDayForecast,
                        selectedOffset = selectedForecastDay?.dayOffset,
                        onBarClick = { selectedForecastDay = it }
                    )
                    selectedForecastDay?.let { pt ->
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedContent(
                            targetState = pt,
                            transitionSpec = {
                                if (extended.reducedMotion) {
                                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                                } else {
                                    (fadeIn(tween(220, easing = BaeumMotion.SilkEasing)) +
                                        slideInVertically(tween(220, easing = BaeumMotion.SilkEasing)) { it / 4 })
                                        .togetherWith(
                                            fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 4 }
                                        )
                                }
                            },
                            label = "home_forecast_day_detail"
                        ) { activeDay ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(extended.elevatedSurface.copy(alpha = 0.72f))
                                    .border(1.dp, extended.glassHighlightBorder, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${activeDay.shortLabel} • ${activeDay.dateKey}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = bText("${activeDay.dueCount} scheduled", "${activeDay.dueCount} terjadwal"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (activeDay.dueCount > 0) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BaeumSecondaryButton(
                    text = if (summary.dueTodayCount > 0) {
                        bText(
                            "Review ${summary.dueTodayCount} scheduled word${if (summary.dueTodayCount == 1) "" else "s"}",
                            "Ulas ${summary.dueTodayCount} kata terjadwal"
                        )
                    } else {
                        bText("All caught up • Study new cards", "Semua selesai • Pelajari kartu baru")
                    },
                    onClick = onReviewDue,
                    testTag = "home_review_due_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
    }
}

@Composable
private fun QuickStudioPebble(
    label: String,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconBgBrush = remember(accent) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.10f)))
    }
    BaeumGlassCard(
        modifier = modifier.testTag(testTag),
        style = BaeumGlassStyle.SUBTLE,
        accentGlowColor = accent,
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 6.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = accent.copy(alpha = 0.35f),
                        spotColor = accent.copy(alpha = 0.35f)
                    )
                    .clip(CircleShape)
                    .background(iconBgBrush)
                    .border(1.dp, accent.copy(alpha = 0.42f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
