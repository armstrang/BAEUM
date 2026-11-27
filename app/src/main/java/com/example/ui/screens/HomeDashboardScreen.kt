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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.DayForecastPoint
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import com.example.domain.curriculum.SyllabusTrack
import com.example.domain.engagement.DailyAtelierQuest
import com.example.domain.engagement.DailySeoulSpark
import com.example.domain.engagement.HabitDashboardSnapshot
import com.example.domain.engagement.RhythmDayBead
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
import com.example.ui.theme.gentleShake
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
    habitSnapshot: HabitDashboardSnapshot = HabitDashboardSnapshot(),
    onOpenSettings: () -> Unit,
    onResumeLearning: () -> Unit,
    onReviewDue: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenVocabulary: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenQuizCenter: () -> Unit,
    onOpenGrammarStudio: () -> Unit,
    onStartSeoulBlitz: () -> Unit = {},
    onCompleteDailySpark: (Boolean) -> Unit = {},
    onClaimDailySeal: () -> Unit = {},
    onSpeakKorean: (String) -> Unit = {},
    onQuestAction: (String) -> Unit = {},
    onOpenBeginnerSyllabus: () -> Unit = {},
    onOpenSyllabusTrack: (SyllabusTrack?) -> Unit = { onOpenBeginnerSyllabus() },
    onOpenMistakeRedemption: () -> Unit = {},
    onStartNextBeginnerLesson: (String) -> Unit = {}
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
    val activeSky = remember(currentHour, extended.isDark) {
        if (extended.isDark) {
            SeoulSkyAtmosphere.NIGHT
        } else if (currentHour in 11..16) {
            SeoulSkyAtmosphere.DAY
        } else {
            SeoulSkyAtmosphere.MORNING
        }
    }

    val (greetingKo, greetingSub) = when {
        currentHour in 5..10 -> "좋은 아침이에요!" to bText(
            "Fresh Seoul morning momentum for Korean.",
            "Semangat pagi Seoul untuk belajar bahasa Korea."
        )
        currentHour in 11..16 -> "안녕하세요!" to bText(
            "Bright afternoon focus for today's Korean.",
            "Fokus siang yang cerah untuk bahasa Korea hari ini."
        )
        else -> "좋은 저녁이에요!" to bText(
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
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(skyPillBg)
                                .border(1.dp, skyPillBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 11.dp, vertical = 6.dp)
                                .testTag("home_sky_atmosphere_badge"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = activeSky.icon,
                                contentDescription = null,
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

                    Spacer(modifier = Modifier.height(14.dp))

                    SeoulHeroRankAndRhythmStrip(
                        habitSnapshot = habitSnapshot,
                        accentColor = ringAccent,
                        textColor = countColor,
                        subTextColor = subCountColor,
                        pillBg = skyPillBg,
                        pillBorder = skyPillBorder,
                        streakTextColor = streakText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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

        // Multi-Track Guided Learning Path Card (auto-progresses: Pemula Nol -> TOPIK I -> TOPIK II)
        Spacer(modifier = Modifier.height(18.dp))
        StaggeredEntrance(index = 3) {
            BeginnerSyllabusHomeCard(
                completedIds = habitSnapshot.completedBeginnerLessonIds,
                perfectIds = habitSnapshot.perfectSyllabusLessonIds,
                mistakeCount = habitSnapshot.mistakeVaultKeys.size,
                onOpenTrack = onOpenSyllabusTrack,
                onOpenMistakeRedemption = onOpenMistakeRedemption,
                onStartNextLesson = onStartNextBeginnerLesson
            )
        }

        // Instant 1-Tap Playable "Daily Seoul Spark" (오늘의 한마디) Hook
        habitSnapshot.dailySpark?.let { spark ->
            Spacer(modifier = Modifier.height(18.dp))
            StaggeredEntrance(index = 3) {
                DailySeoulSparkCard(
                    spark = spark,
                    isCompleted = habitSnapshot.isDailySparkCompleted,
                    onSpeakKorean = onSpeakKorean,
                    onAnswerResult = onCompleteDailySpark
                )
            }
        }

        // High-Flow 60-Second Seoul Blitz Launch Card (Beat Your Personal Record!)
        Spacer(modifier = Modifier.height(16.dp))
        StaggeredEntrance(index = 4) {
            SeoulBlitzLaunchCard(
                bestScore = habitSnapshot.blitzBestScore,
                bestCombo = habitSnapshot.bestComboEver,
                onStartBlitz = onStartSeoulBlitz
            )
        }

        // Daily Atelier Quests (3 Bite-Sized Missions + Daily Seal Stamp & Streak Shield)
        if (habitSnapshot.dailyQuests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            StaggeredEntrance(index = 5) {
                DailyAtelierQuestsCard(
                    quests = habitSnapshot.dailyQuests,
                    completedCount = habitSnapshot.completedQuestsCount,
                    isSealClaimed = habitSnapshot.isDailySealClaimed,
                    canClaimSeal = habitSnapshot.canClaimDailySeal,
                    onQuestClick = onQuestAction,
                    onClaimSeal = onClaimDailySeal
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Active Curriculum Journey Card + Primary CTA
        StaggeredEntrance(index = 6) {
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

private val HabitPillShape = RoundedCornerShape(14.dp)
private val SparkOptionShape = RoundedCornerShape(16.dp)

@Composable
private fun SeoulHeroRankAndRhythmStrip(
    habitSnapshot: HabitDashboardSnapshot,
    accentColor: Color,
    textColor: Color,
    subTextColor: Color,
    pillBg: Color,
    pillBorder: Color,
    streakTextColor: Color
) {
    val extended = LocalBaeumColors.current
    val rank = habitSnapshot.rankProgress.currentRank
    val xpProgress = habitSnapshot.rankProgress

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(pillBg.copy(alpha = 0.65f))
            .border(1.dp, pillBorder.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Rank + Total XP + Streak Shield row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.20f))
                        .border(1.dp, accentColor.copy(alpha = 0.50f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.badgeGlyph,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = accentColor
                    )
                }
                Column {
                    Text(
                        text = "Lv.${rank.level} ${rank.titleKo} · ${bText(rank.titleEn, rank.titleId)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${xpProgress.totalXp} XP · ${xpProgress.xpIntoLevel}/${xpProgress.xpForNextLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor
                    )
                }
            }

            // Streak Shield Pill
            Row(
                modifier = Modifier
                    .clip(HabitPillShape)
                    .background(extended.mint.copy(alpha = 0.16f))
                    .border(1.dp, extended.mint.copy(alpha = 0.42f), HabitPillShape)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = bText("Streak shields", "Perisai runtutan"),
                    tint = extended.mint,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "${habitSnapshot.streakShields}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = textColor
                )
            }
        }

        ProgressBarSlim(
            progress = xpProgress.progressFraction,
            color = accentColor,
            height = 5.dp
        )

        // 7-Day Habit Flame Beads
        if (habitSnapshot.weeklyRhythm.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                habitSnapshot.weeklyRhythm.forEach { bead ->
                    val beadColor = when {
                        bead.isCompleted -> extended.gold
                        bead.isShielded -> extended.mint
                        else -> subTextColor.copy(alpha = 0.25f)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (bead.isToday) 22.dp else 18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (bead.isCompleted || bead.isShielded) beadColor.copy(alpha = 0.28f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (bead.isToday) 1.5.dp else 1.dp,
                                    color = if (bead.isToday && !bead.isCompleted) accentColor else beadColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bead.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(extended.gold)
                                )
                            } else if (bead.isShielded) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = extended.mint,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Text(
                            text = bText(bead.shortLabelEn, bead.shortLabelId),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (bead.isToday) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (bead.isToday) textColor else subTextColor
                        )
                    }
                }
            }
        }

        if (habitSnapshot.shieldSavedStreakRecently) {
            Text(
                text = bText(
                    "Streak Shield protected your flame yesterday!",
                    "Perisai Runtutan menyelamatkan runtutanmu kemarin!"
                ),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = streakTextColor
            )
        }
    }
}

@Composable
private fun DailySeoulSparkCard(
    spark: DailySeoulSpark,
    isCompleted: Boolean,
    onSpeakKorean: (String) -> Unit,
    onAnswerResult: (Boolean) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    var wrongShakeSeq by remember(spark.dayKey) { mutableIntStateOf(0) }
    var wrongOption by remember(spark.dayKey) { mutableStateOf<String?>(null) }

    BaeumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .gentleShake(wrongShakeSeq)
            .testTag("home_daily_spark_card"),
        style = BaeumGlassStyle.STANDARD,
        accentGlowColor = if (isCompleted) extended.mint else extended.gold
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = if (isCompleted) extended.mint else extended.gold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = bText("DAILY SEOUL SPARK · 오늘의 한마디", "PERCIKAN SEOUL · 오늘의 한마디"),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isCompleted) extended.mint else extended.gold
                )
            }
            Box(
                modifier = Modifier
                    .clip(HabitPillShape)
                    .background((if (isCompleted) extended.mint else extended.gold).copy(alpha = 0.16f))
                    .border(
                        1.dp,
                        (if (isCompleted) extended.mint else extended.gold).copy(alpha = 0.45f),
                        HabitPillShape
                    )
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isCompleted) bText("LIT ✓", "MENYALA ✓") else "+${spark.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isCompleted) extended.mint else extended.gold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spark.hangul,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (spark.romanization.isNotBlank()) {
                    Text(
                        text = "${spark.romanization}  •  ${spark.curriculumBadge}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.14f))
                    .border(1.dp, primary.copy(alpha = 0.35f), CircleShape)
                    .tactileClickable(pressedScale = 0.90f) { onSpeakKorean(spark.hangul) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = bText("Listen to Daily Spark", "Dengar Percikan Harian"),
                    tint = primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isCompleted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SparkOptionShape)
                    .background(extended.mint.copy(alpha = 0.10f))
                    .border(1.dp, extended.mint.copy(alpha = 0.32f), SparkOptionShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = spark.correctMeaning,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = extended.mint
                )
                if (spark.exampleKo.isNotBlank()) {
                    Text(
                        text = spark.exampleKo,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (spark.exampleTranslation.isNotBlank()) {
                    Text(
                        text = spark.exampleTranslation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = bText("Tap the right meaning to ignite today's momentum:", "Ketuk arti yang benar untuk menyalakan momentum hari ini:"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                spark.options.forEachIndexed { idx, option ->
                    val isWrongPick = wrongOption == option
                    val borderCol = if (isWrongPick) extended.cherryPink.copy(alpha = 0.7f) else extended.glassHighlightBorder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(SparkOptionShape)
                            .background(
                                if (isWrongPick) extended.cherryPink.copy(alpha = 0.12f)
                                else extended.elevatedSurface.copy(alpha = 0.65f)
                            )
                            .border(1.dp, borderCol, SparkOptionShape)
                            .tactileClickable(pressedScale = 0.96f) {
                                if (option == spark.correctMeaning) {
                                    wrongOption = null
                                    onAnswerResult(true)
                                } else {
                                    wrongOption = option
                                    wrongShakeSeq += 1
                                    onAnswerResult(false)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                            .testTag("home_daily_spark_option_$idx"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isWrongPick) extended.cherryPink else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeoulBlitzLaunchCard(
    bestScore: Int,
    bestCombo: Int,
    onStartBlitz: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val gold = extended.gold
    val iconBrush = remember(gold, extended.cherryPink) {
        Brush.linearGradient(listOf(gold.copy(alpha = 0.28f), extended.cherryPink.copy(alpha = 0.18f)))
    }

    BaeumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_seoul_blitz_card"),
        style = BaeumGlassStyle.STANDARD,
        accentGlowColor = gold,
        onClick = onStartBlitz
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBrush)
                    .border(1.dp, gold.copy(alpha = 0.55f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = gold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = bText("60s Seoul Blitz", "60d Seoul Blitz"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "60초 블리츠",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = gold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (bestScore > 0) {
                        bText(
                            "Best $bestScore pts • Peak ${bestCombo.coerceAtLeast(1)}x combo · Tap to beat it!",
                            "Rekor $bestScore poin • Kombo ${bestCombo.coerceAtLeast(1)}x · Ketuk untuk pecahkan!"
                        )
                    } else {
                        bText(
                            "Rapid Hangul reflex challenge with Fever multipliers & +2s bonuses",
                            "Tantangan refleks Hangul cepat dengan mode Fever & bonus waktu +2d"
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = bText("Play 60s Seoul Blitz", "Main 60d Seoul Blitz"),
                tint = gold,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DailyAtelierQuestsCard(
    quests: List<DailyAtelierQuest>,
    completedCount: Int,
    isSealClaimed: Boolean,
    canClaimSeal: Boolean,
    onQuestClick: (String) -> Unit,
    onClaimSeal: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary

    BaeumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_daily_quests_card"),
        style = BaeumGlassStyle.STANDARD,
        accentGlowColor = if (canClaimSeal) extended.gold else primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = bText("DAILY ATELIER QUESTS · 오늘의 미션", "MISI HARIAN ATELIER · 오늘의 미션"),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = bText("$completedCount / ${quests.size} complete", "$completedCount / ${quests.size} selesai"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier
                    .clip(HabitPillShape)
                    .background((if (isSealClaimed) extended.mint else extended.gold).copy(alpha = 0.16f))
                    .border(
                        1.dp,
                        (if (isSealClaimed) extended.mint else extended.gold).copy(alpha = 0.42f),
                        HabitPillShape
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = if (isSealClaimed) extended.mint else extended.gold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isSealClaimed) bText("Sealed ✓", "Distempel ✓") else "+100 XP & +1",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSealClaimed) extended.mint else extended.gold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quests.forEach { quest ->
                val done = quest.isCompleted
                val accent = if (done) extended.mint else primary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .clip(SparkOptionShape)
                        .background(
                            if (done) extended.mint.copy(alpha = 0.08f)
                            else extended.elevatedSurface.copy(alpha = 0.55f)
                        )
                        .border(
                            1.dp,
                            if (done) extended.mint.copy(alpha = 0.35f) else extended.glassHighlightBorder,
                            SparkOptionShape
                        )
                        .then(
                            if (!done) Modifier.tactileClickable(pressedScale = 0.98f) { onQuestClick(quest.actionRoute) }
                            else Modifier
                        )
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (done) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = bText(quest.titleEn, quest.titleId),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = bText(quest.subtitleEn, quest.subtitleId),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "${quest.current}/${quest.target}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = accent
                        )
                    }
                    ProgressBarSlim(
                        progress = quest.progressFraction,
                        color = accent,
                        height = 4.dp
                    )
                }
            }
        }

        if (canClaimSeal) {
            Spacer(modifier = Modifier.height(14.dp))
            BaeumPrimaryButton(
                text = bText("Stamp Daily Seal · +100 XP & +1 Shield", "Stempel Harian · +100 XP & +1 Perisai"),
                onClick = onClaimSeal,
                testTag = "home_claim_daily_seal_button"
            )
        }
    }
}

@Composable
private fun BeginnerSyllabusHomeCard(
    completedIds: Set<String>,
    perfectIds: Set<String>,
    mistakeCount: Int = 0,
    onOpenTrack: (SyllabusTrack?) -> Unit,
    onOpenMistakeRedemption: () -> Unit = {},
    onStartNextLesson: (String) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val activeTrack = remember(completedIds) { BeginnerSyllabusCatalog.activeTrackFor(completedIds) }
    val doneCount = remember(completedIds, activeTrack) {
        BeginnerSyllabusCatalog.completionCount(completedIds, activeTrack)
    }
    val totalLessons = remember(activeTrack) {
        BeginnerSyllabusCatalog.lessonsForTrack(activeTrack).size
    }
    val perfectCount = remember(perfectIds, activeTrack) {
        BeginnerSyllabusCatalog.perfectCount(perfectIds, activeTrack)
    }
    val progress = remember(completedIds, activeTrack) {
        BeginnerSyllabusCatalog.completionFraction(completedIds, activeTrack)
    }
    val nextLesson = remember(completedIds, activeTrack) {
        BeginnerSyllabusCatalog.nextRecommendedLesson(completedIds, activeTrack)
    }
    val allGraduated = remember(completedIds) {
        BeginnerSyllabusCatalog.isAllTracksGraduated(completedIds)
    }
    val accent = when (activeTrack) {
        SyllabusTrack.BEGINNER -> extended.mint
        SyllabusTrack.TOPIK_I -> primary
        SyllabusTrack.TOPIK_II -> extended.gold
    }

    BaeumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_beginner_syllabus_card"),
        style = BaeumGlassStyle.STANDARD,
        accentGlowColor = accent,
        borderColor = accent.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${activeTrack.badgeKo} · " + bText(activeTrack.titleEn, activeTrack.titleId).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(HabitPillShape)
                    .tactileClickable(pressedScale = 0.94f) { onOpenTrack(activeTrack) },
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier
                        .clip(HabitPillShape)
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.dp, accent.copy(alpha = 0.36f), HabitPillShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$doneCount/$totalLessons",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = accent
                    )
                    if (perfectCount > 0) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = extended.gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "$perfectCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = extended.gold
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = bText("View full syllabus", "Lihat seluruh silabus"),
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (allGraduated) {
                bText("All 3 Korean Syllabi Mastered! 🎉", "Seluruh 3 Silabus Korea Lulus! 🎉")
            } else {
                bText(
                    "Lesson ${nextLesson.number}: ${nextLesson.titleEn}",
                    "Pelajaran ${nextLesson.number}: ${nextLesson.titleId}"
                )
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = bText(
                "${nextLesson.goalEn} (~${nextLesson.estimatedMinutes} min · Build + Listen)",
                "${nextLesson.goalId} (~${nextLesson.estimatedMinutes} mnt · Susun + Dengar)"
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))
        ProgressBarSlim(progress = progress, color = accent, height = 5.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // 3 Clear Guided Track Direction Pills: Pemula Nol -> TOPIK I -> TOPIK II
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SyllabusTrack.entries.forEach { track ->
                val isCurrent = track == activeTrack
                val tAccent = when (track) {
                    SyllabusTrack.BEGINNER -> extended.mint
                    SyllabusTrack.TOPIK_I -> primary
                    SyllabusTrack.TOPIK_II -> extended.gold
                }
                val tDone = BeginnerSyllabusCatalog.completionCount(completedIds, track)
                val tTotal = BeginnerSyllabusCatalog.lessonsForTrack(track).size
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(HabitPillShape)
                        .background(
                            if (isCurrent) tAccent.copy(alpha = 0.16f)
                            else extended.elevatedSurface.copy(alpha = 0.48f)
                        )
                        .border(
                            1.dp,
                            if (isCurrent) tAccent.copy(alpha = 0.55f) else extended.glassHighlightBorder,
                            HabitPillShape
                        )
                        .tactileClickable(pressedScale = 0.95f) { onOpenTrack(track) }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${bText(track.shortLabelEn, track.shortLabelId)} $tDone/$tTotal",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold
                        ),
                        color = if (isCurrent) tAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (mistakeCount > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HabitPillShape)
                    .background(extended.cherryPink.copy(alpha = 0.14f))
                    .border(1.dp, extended.cherryPink.copy(alpha = 0.45f), HabitPillShape)
                    .tactileClickable(pressedScale = 0.96f, onClick = onOpenMistakeRedemption)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bText(
                        "🔥 Redeem $mistakeCount past mistake(s) (+20 XP each)",
                        "🔥 Balas Dendam $mistakeCount soal salah (+20 XP/soal)"
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = extended.cherryPink
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = extended.cherryPink,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BaeumPrimaryButton(
                text = if (doneCount == 0) {
                    bText("Start Lesson 1", "Mulai Pelajaran 1")
                } else {
                    bText("Continue L${nextLesson.number}", "Lanjut P${nextLesson.number}")
                },
                onClick = { onStartNextLesson(nextLesson.id) },
                modifier = Modifier.weight(1.25f),
                testTag = "home_beginner_start_next_btn"
            )
            BaeumSecondaryButton(
                text = bText("3 Syllabi", "3 Jalur Silabus"),
                onClick = { onOpenTrack(activeTrack) },
                modifier = Modifier.weight(1f),
                testTag = "home_beginner_view_all_btn"
            )
        }
    }
}


