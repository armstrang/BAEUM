package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.curriculum.BeginnerLesson
import com.example.domain.curriculum.BeginnerPracticeAction
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import com.example.domain.curriculum.SentenceBuildExercise
import com.example.domain.curriculum.SyllabusTrack
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.LuxuryConfettiOverlay
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.gentleShake
import com.example.ui.theme.rememberAnimatedInt
import com.example.ui.theme.tactileClickable
import kotlin.random.Random

private val LessonEmblemShape = RoundedCornerShape(16.dp)
private val LessonPillShape = RoundedCornerShape(12.dp)
private val LessonOptionShape = RoundedCornerShape(16.dp)
private val TileChipShape = RoundedCornerShape(14.dp)

@Composable
private fun trackAccentColor(track: SyllabusTrack): Color {
    val extended = LocalBaeumColors.current
    return when (track) {
        SyllabusTrack.BEGINNER -> extended.mint
        SyllabusTrack.TOPIK_I -> MaterialTheme.colorScheme.primary
        SyllabusTrack.TOPIK_II -> extended.gold
    }
}

/**
 * 3-Track Progressive Guided Korean Syllabus Roadmap:
 * - Track 1: Pemula Nol → Menyusun Kalimat (12 lessons)
 * - Track 2: TOPIK I · 1–2급 (8 lessons)
 * - Track 3: TOPIK II · 3–6급 (8 lessons)
 * Displays 3-Star Gold Mastery (★★★) and seamless graduation bridges between tracks.
 */
@Composable
fun BeginnerSyllabusScreen(
    completedLessonIds: Set<String>,
    perfectLessonIds: Set<String> = emptySet(),
    initialTrack: SyllabusTrack? = null,
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary

    val defaultTrack = remember(initialTrack, completedLessonIds) {
        initialTrack ?: BeginnerSyllabusCatalog.activeTrackFor(completedLessonIds)
    }
    var selectedTrackName by rememberSaveable(initialTrack?.name) { mutableStateOf(defaultTrack.name) }
    val selectedTrack = SyllabusTrack.fromKey(selectedTrackName) ?: SyllabusTrack.BEGINNER

    val totalDoneAll = remember(completedLessonIds) {
        BeginnerSyllabusCatalog.totalCompletionCount(completedLessonIds)
    }
    val totalLessonsAll = BeginnerSyllabusCatalog.allLessons.size

    val trackLessons = remember(selectedTrack) { BeginnerSyllabusCatalog.lessonsForTrack(selectedTrack) }
    val trackStages = remember(selectedTrack) { BeginnerSyllabusCatalog.stagesForTrack(selectedTrack) }
    val completedCount = remember(completedLessonIds, selectedTrack) {
        BeginnerSyllabusCatalog.completionCount(completedLessonIds, selectedTrack)
    }
    val perfectCount = remember(perfectLessonIds, selectedTrack) {
        BeginnerSyllabusCatalog.perfectCount(perfectLessonIds, selectedTrack)
    }
    val totalLessons = trackLessons.size
    val progress = remember(completedLessonIds, selectedTrack) {
        BeginnerSyllabusCatalog.completionFraction(completedLessonIds, selectedTrack)
    }
    val nextLesson = remember(completedLessonIds, selectedTrack) {
        BeginnerSyllabusCatalog.nextRecommendedLesson(completedLessonIds, selectedTrack)
    }
    val isTrackGraduated = completedCount >= totalLessons
    val nextTrack = remember(selectedTrack) { BeginnerSyllabusCatalog.nextTrackAfter(selectedTrack) }
    val accent = trackAccentColor(selectedTrack)
    val animPct by rememberAnimatedInt((progress * 100f).toInt(), label = "syllabus_track_pct")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Guided Korean Syllabus", "Silabus Belajar Terpandu"),
            subtitle = bText(
                "3 Tracks · $totalDoneAll/$totalLessonsAll lessons mastered",
                "3 Jalur · $totalDoneAll/$totalLessonsAll pelajaran selesai"
            ),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 3-Track Selector Ribbon (Pemula Nol | TOPIK I | TOPIK II)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("syllabus_track_switcher"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SyllabusTrack.entries.forEach { track ->
                    val isSelected = track == selectedTrack
                    val tAccent = trackAccentColor(track)
                    val tDone = BeginnerSyllabusCatalog.completionCount(completedLessonIds, track)
                    val tTotal = BeginnerSyllabusCatalog.lessonsForTrack(track).size
                    val tGrad = tDone >= tTotal

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 58.dp)
                            .clip(LessonOptionShape)
                            .background(
                                if (isSelected) tAccent.copy(alpha = 0.20f)
                                else extended.elevatedSurface.copy(alpha = 0.58f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) tAccent else extended.glassHighlightBorder,
                                shape = LessonOptionShape
                            )
                            .tactileClickable(pressedScale = 0.96f) {
                                selectedTrackName = track.name
                            }
                            .testTag("syllabus_track_tab_${track.name.lowercase()}")
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = track.badgeKo,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (isSelected) tAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = bText(track.shortLabelEn, track.shortLabelId),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (tGrad) "$tDone/$tTotal ✓" else "$tDone/$tTotal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (tGrad) extended.mint else tAccent
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTrack,
                transitionSpec = {
                    BaeumMotion.directionalContentTransform(
                        forward = targetState.ordinal >= initialState.ordinal,
                        reducedMotion = extended.reducedMotion,
                        durationMs = 240
                    )
                },
                label = "syllabus_track_content"
            ) { activeTrack ->
                val activeAccent = trackAccentColor(activeTrack)
                val activeStages = BeginnerSyllabusCatalog.stagesForTrack(activeTrack)
                val activeDone = BeginnerSyllabusCatalog.completionCount(completedLessonIds, activeTrack)
                val activeTotal = BeginnerSyllabusCatalog.lessonsForTrack(activeTrack).size
                val activePerfect = BeginnerSyllabusCatalog.perfectCount(perfectLessonIds, activeTrack)
                val activeGrad = activeDone >= activeTotal
                val activeNext = BeginnerSyllabusCatalog.nextRecommendedLesson(completedLessonIds, activeTrack)
                val activeProgress = BeginnerSyllabusCatalog.completionFraction(completedLessonIds, activeTrack)
                val bridgeNextTrack = BeginnerSyllabusCatalog.nextTrackAfter(activeTrack)
                val beginnerReady = BeginnerSyllabusCatalog.isTrackGraduated(completedLessonIds, SyllabusTrack.BEGINNER)

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Hero Roadmap Card for Active Track
                    BaeumGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("beginner_syllabus_hero"),
                        style = BaeumGlassStyle.HERO,
                        accentGlowColor = if (activeGrad) extended.gold else activeAccent,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.School,
                                        contentDescription = null,
                                        tint = if (activeGrad) extended.gold else activeAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${activeTrack.badgeKo}  ·  " + bText(activeTrack.titleEn, activeTrack.titleId),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 0.8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = if (activeGrad) extended.gold else activeAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (activeGrad) {
                                        bText("Track Graduated! 🎉", "Silabus Lulus! 🎉")
                                    } else {
                                        bText(
                                            "$activeDone / $activeTotal lessons done",
                                            "$activeDone / $activeTotal pelajaran selesai"
                                        )
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bText(activeTrack.subtitleEn, activeTrack.subtitleId),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // 3-Star Gold Mastery Counter Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(LessonPillShape)
                                            .background(extended.gold.copy(alpha = 0.14f))
                                            .border(1.dp, extended.gold.copy(alpha = 0.38f), LessonPillShape)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = extended.gold,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = bText(
                                                    "$activePerfect/$activeTotal Perfect ★★★",
                                                    "$activePerfect/$activeTotal Sempurna ★★★"
                                                ),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = extended.gold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            CircularGoalRing(
                                progress = activeProgress,
                                centerLabel = if (activeTrack == selectedTrack) "$animPct%" else "${(activeProgress * 100).toInt()}%",
                                centerSubLabel = "$activeDone/$activeTotal",
                                size = 84.dp,
                                strokeWidth = 7.dp,
                                accentColor = if (activeGrad) extended.gold else activeAccent
                            )
                        }

                        // Warm non-blocking suggestion if peeking at TOPIK I/II before finishing Beginner
                        if (activeTrack != SyllabusTrack.BEGINNER && !beginnerReady && activeDone == 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(LessonPillShape)
                                    .background(extended.mint.copy(alpha = 0.10f))
                                    .border(1.dp, extended.mint.copy(alpha = 0.30f), LessonPillShape)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = extended.mint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = bText(
                                        "New to Hangul? Start with 'Zero → Sentence' first — or jump right in anytime!",
                                        "Baru mulai Hangul? Mulai dari 'Pemula Nol' dulu — atau langsung pelajari jalur ini bebas!"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        BaeumPrimaryButton(
                            text = when {
                                activeDone == 0 -> bText(
                                    "Start Lesson 1 · ${activeNext.titleEn}",
                                    "Mulai Pelajaran 1 · ${activeNext.titleId}"
                                )
                                activeGrad && bridgeNextTrack != null -> bText(
                                    "Advance to ${bridgeNextTrack.shortLabelEn} Syllabus →",
                                    "Naik ke Silabus ${bridgeNextTrack.shortLabelId} →"
                                )
                                activeGrad -> bText(
                                    "Review Final Lesson · ${activeNext.titleEn}",
                                    "Ulas Pelajaran Akhir · ${activeNext.titleId}"
                                )
                                else -> bText(
                                    "Continue Lesson ${activeNext.number} · ${activeNext.titleEn}",
                                    "Lanjut Pelajaran ${activeNext.number} · ${activeNext.titleId}"
                                )
                            },
                            onClick = {
                                if (activeGrad && bridgeNextTrack != null) {
                                    selectedTrackName = bridgeNextTrack.name
                                } else {
                                    onOpenLesson(activeNext.id)
                                }
                            },
                            testTag = "beginner_syllabus_continue_cta"
                        )
                    }

                    // Stages & Numbered Lesson Cards for Active Track
                    activeStages.forEachIndexed { stageIdx, stage ->
                        val stageLessons = BeginnerSyllabusCatalog.lessonsForStage(stage.number)
                        val stageDone = stageLessons.count { it.id in completedLessonIds }
                        val stageAccent = when (stage.accentTag) {
                            "mint" -> extended.mint
                            "gold" -> extended.gold
                            "cherry" -> extended.cherryPink
                            else -> primary
                        }

                        StaggeredEntrance(index = stageIdx) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "STAGE ${stageIdx + 1}  ·  ${stage.titleKo}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.3.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = stageAccent
                                        )
                                        Text(
                                            text = bText(stage.titleEn, stage.titleId),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = bText(stage.subtitleEn, stage.subtitleId),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(LessonPillShape)
                                            .background(stageAccent.copy(alpha = 0.16f))
                                            .border(1.dp, stageAccent.copy(alpha = 0.42f), LessonPillShape)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (stageDone == stageLessons.size) "$stageDone/${stageLessons.size} ✓" else "$stageDone/${stageLessons.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = stageAccent
                                        )
                                    }
                                }

                                stageLessons.forEach { lesson ->
                                    val isDone = lesson.id in completedLessonIds
                                    val isPerfect = lesson.id in perfectLessonIds
                                    val isRecommendedNext = !activeGrad && lesson.id == activeNext.id
                                    val emblemColor = when {
                                        isPerfect -> extended.gold
                                        isDone -> extended.mint
                                        else -> stageAccent
                                    }
                                    val emblemBrush = remember(emblemColor) {
                                        Brush.verticalGradient(
                                            listOf(emblemColor.copy(alpha = 0.24f), emblemColor.copy(alpha = 0.08f))
                                        )
                                    }

                                    BaeumGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("beginner_lesson_card_${lesson.id}"),
                                        style = if (isRecommendedNext) BaeumGlassStyle.STANDARD else BaeumGlassStyle.SUBTLE,
                                        accentGlowColor = if (isRecommendedNext) stageAccent else emblemColor,
                                        borderColor = when {
                                            isRecommendedNext -> stageAccent.copy(alpha = 0.72f)
                                            isPerfect -> extended.gold.copy(alpha = 0.48f)
                                            isDone -> extended.mint.copy(alpha = 0.38f)
                                            else -> Color.Unspecified
                                        },
                                        onClick = { onOpenLesson(lesson.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(LessonEmblemShape)
                                                    .background(emblemBrush)
                                                    .border(1.dp, emblemColor.copy(alpha = 0.5f), LessonEmblemShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = lesson.emblemKo,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = emblemColor
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = String.format(java.util.Locale.US, "%02d", lesson.number),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = stageAccent
                                                    )
                                                    Text(
                                                        text = bText("${lesson.estimatedMinutes} min", "${lesson.estimatedMinutes} mnt"),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(primary.copy(alpha = 0.12f))
                                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = bText("BUILD", "SUSUN"),
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.ExtraBold
                                                            ),
                                                            color = primary
                                                        )
                                                    }
                                                    if (isRecommendedNext) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(stageAccent.copy(alpha = 0.18f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = bText("NEXT", "MULAI SINI"),
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                ),
                                                                color = stageAccent
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = bText(lesson.titleEn, lesson.titleId),
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = bText(lesson.goalEn, lesson.goalId),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                if (isPerfect) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                        repeat(3) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Star,
                                                                contentDescription = null,
                                                                tint = extended.gold,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = bText("Perfect", "Sempurna"),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = extended.gold
                                                    )
                                                } else if (isDone) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = bText("Completed", "Selesai"),
                                                        tint = extended.mint,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                        repeat(2) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Star,
                                                                contentDescription = null,
                                                                tint = extended.gold,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = Icons.Outlined.StarBorder,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                        contentDescription = bText("Open lesson", "Buka pelajaran"),
                                                        tint = if (isRecommendedNext) stageAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Next-Track Progression Bridge Card
                    if (bridgeNextTrack != null) {
                        val nextAccent = trackAccentColor(bridgeNextTrack)
                        BaeumGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            style = BaeumGlassStyle.SUBTLE,
                            accentGlowColor = nextAccent,
                            borderColor = nextAccent.copy(alpha = 0.42f),
                            onClick = { selectedTrackName = bridgeNextTrack.name }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bText("NEXT GUIDED TRACK", "SILABUS TAHAP BERIKUTNYA"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.1.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = nextAccent
                                    )
                                    Text(
                                        text = bText(bridgeNextTrack.titleEn, bridgeNextTrack.titleId),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = bText(bridgeNextTrack.subtitleEn, bridgeNextTrack.subtitleId),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = nextAccent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class BeginnerLessonPhase {
    LEARN,
    SENTENCE_BUILD,
    MINI_CHECK,
    COMPLETE
}

/**
 * 4-Phase Interactive Guided Lesson Player (Easy to Learn, Addictive & Challenging):
 * Phase 1 (Belajar): Warm concept + Golden Rule + Audio study cards with mnemonics
 * Phase 2 (Susun Kalimat): Interactive Tap-to-Assemble Word/Syllable Builder + Hint + Auto Korean TTS
 * Phase 3 (Cek Cepat): 3 fast mastery checks with immediate explanations
 * Phase 4 (Selesai): 3-Star Gold Mastery Seal (★★★), Confetti, +XP & seamless Next Lesson / Next Track CTA!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeginnerLessonScreen(
    lesson: BeginnerLesson?,
    isAlreadyCompleted: Boolean,
    isAlreadyPerfect: Boolean = false,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onCheckSound: (Boolean, Int) -> Unit,
    onLessonCompleted: (String, Boolean) -> Unit,
    onOpenNextLesson: (String) -> Unit,
    onOpenNextTrack: (SyllabusTrack) -> Unit = {},
    onOpenPracticeAction: (BeginnerPracticeAction) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary

    if (lesson == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Guided Lesson", "Pelajaran Terpandu"), onBack = onBack)
            Column(modifier = Modifier.padding(20.dp)) {
                SeoulCard {
                    Text(bText("Lesson not found.", "Pelajaran tidak ditemukan."))
                    Spacer(modifier = Modifier.height(10.dp))
                    BaeumSecondaryButton(text = bText("Back to Syllabus", "Kembali ke Silabus"), onClick = onBack)
                }
            }
        }
        return
    }

    val trackAccent = trackAccentColor(lesson.track)
    val trackTotalLessons = remember(lesson.track) {
        BeginnerSyllabusCatalog.lessonsForTrack(lesson.track).size
    }
    val nextLesson = remember(lesson.id) { BeginnerSyllabusCatalog.nextLessonInTrack(lesson) }
    val nextTrack = remember(lesson.track) { BeginnerSyllabusCatalog.nextTrackAfter(lesson.track) }

    var phase by remember(lesson.id) { mutableStateOf(BeginnerLessonPhase.LEARN) }
    var buildIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var checkIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var selectedOption by remember(lesson.id, checkIndex) { mutableStateOf<String?>(null) }
    var streakCount by remember(lesson.id) { mutableIntStateOf(0) }
    var mistakesCount by remember(lesson.id) { mutableIntStateOf(0) }
    var checkCorrectCount by remember(lesson.id) { mutableIntStateOf(0) }
    var wrongShakeSeq by remember(lesson.id) { mutableIntStateOf(0) }
    var completionCommitted by remember(lesson.id) { mutableStateOf(false) }

    // Sentence-builder interactive state per exercise
    val activeExercise: SentenceBuildExercise? = lesson.sentenceExercises.getOrNull(buildIndex)
    val shuffledBank = remember(lesson.id, buildIndex) {
        if (activeExercise == null) emptyList()
        else {
            val combined = (activeExercise.targetTokens + activeExercise.distractorTokens)
                .mapIndexed { idx, token -> idx to token }
            val rng = Random(lesson.id.hashCode() * 31 + buildIndex * 17 + 7)
            val shuffled = combined.shuffled(rng)
            // Guarantee it doesn't accidentally render already solved in exact order
            if (shuffled.map { it.second }.take(activeExercise.targetTokens.size) == activeExercise.targetTokens && shuffled.size > 1) {
                shuffled.reversed()
            } else {
                shuffled
            }
        }
    }
    val selectedTileIds = remember(lesson.id, buildIndex) { mutableStateListOf<Int>() }
    var buildSolved by remember(lesson.id, buildIndex) { mutableStateOf(false) }
    var buildWrongFeedback by remember(lesson.id, buildIndex) { mutableStateOf(false) }
    var buildHintVisible by remember(lesson.id, buildIndex) { mutableStateOf(false) }

    val phaseProgress = when (phase) {
        BeginnerLessonPhase.LEARN -> 0.22f
        BeginnerLessonPhase.SENTENCE_BUILD -> 0.28f + ((buildIndex + if (buildSolved) 1 else 0).toFloat() / lesson.sentenceExercises.size.coerceAtLeast(1)) * 0.34f
        BeginnerLessonPhase.MINI_CHECK -> 0.65f + ((checkIndex + if (selectedOption != null) 1 else 0).toFloat() / lesson.miniChecks.size.coerceAtLeast(1)) * 0.30f
        BeginnerLessonPhase.COMPLETE -> 1.0f
    }

    fun resetLessonForPerfectRetry() {
        phase = BeginnerLessonPhase.SENTENCE_BUILD
        buildIndex = 0
        checkIndex = 0
        selectedOption = null
        streakCount = 0
        mistakesCount = 0
        checkCorrectCount = 0
        completionCommitted = false
        selectedTileIds.clear()
        buildSolved = false
        buildWrongFeedback = false
        buildHintVisible = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BaeumTopBar(
                title = "${bText(lesson.track.shortLabelEn, lesson.track.shortLabelId)} · #${lesson.number}/$trackTotalLessons",
                subtitle = bText(lesson.titleEn, lesson.titleId),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProgressBarSlim(
                        progress = phaseProgress,
                        color = if (phase == BeginnerLessonPhase.COMPLETE) extended.gold else trackAccent,
                        modifier = Modifier.weight(1f)
                    )
                    if (streakCount >= 2 && phase != BeginnerLessonPhase.COMPLETE) {
                        Row(
                            modifier = Modifier
                                .clip(LessonPillShape)
                                .background(extended.gold.copy(alpha = 0.16f))
                                .border(1.dp, extended.gold.copy(alpha = 0.45f), LessonPillShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = extended.gold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${streakCount}x",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = extended.gold
                            )
                        }
                    }
                }

                // 4-Phase Stepper Ribbon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PhaseStepLabel(
                        text = bText("1. Learn", "1. Belajar"),
                        active = phase == BeginnerLessonPhase.LEARN,
                        accent = primary
                    )
                    PhaseStepLabel(
                        text = bText("2. Build (${lesson.sentenceExercises.size})", "2. Susun (${lesson.sentenceExercises.size})"),
                        active = phase == BeginnerLessonPhase.SENTENCE_BUILD,
                        accent = trackAccent
                    )
                    PhaseStepLabel(
                        text = bText("3. Check (${lesson.miniChecks.size})", "3. Cek (${lesson.miniChecks.size})"),
                        active = phase == BeginnerLessonPhase.MINI_CHECK,
                        accent = extended.mint
                    )
                    PhaseStepLabel(
                        text = bText("4. ★★★", "4. ★★★"),
                        active = phase == BeginnerLessonPhase.COMPLETE,
                        accent = extended.gold
                    )
                }

                AnimatedContent(
                    targetState = phase,
                    transitionSpec = {
                        BaeumMotion.directionalContentTransform(
                            forward = true,
                            reducedMotion = extended.reducedMotion,
                            durationMs = 260
                        )
                    },
                    label = "guided_lesson_phase"
                ) { activePhase ->
                    when (activePhase) {
                        // ==================== PHASE 1: LEARN ====================
                        BeginnerLessonPhase.LEARN -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                BaeumGlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    style = BaeumGlassStyle.HERO,
                                    accentGlowColor = trackAccent
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(LessonEmblemShape)
                                                .background(trackAccent.copy(alpha = 0.18f))
                                                .border(1.dp, trackAccent.copy(alpha = 0.5f), LessonEmblemShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = lesson.emblemKo,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = trackAccent
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = bText(lesson.titleEn, lesson.titleId),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = bText(lesson.goalEn, lesson.goalId),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = bText(lesson.overviewEn, lesson.overviewId),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(LessonOptionShape)
                                            .background(extended.gold.copy(alpha = 0.12f))
                                            .border(1.dp, extended.gold.copy(alpha = 0.42f), LessonOptionShape)
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lightbulb,
                                            contentDescription = null,
                                            tint = extended.gold,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(18.dp)
                                        )
                                        Text(
                                            text = bText(lesson.keyRuleEn, lesson.keyRuleId),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Text(
                                    text = bText("Tap any card to hear native pronunciation", "Ketuk kartu mana saja untuk mendengar pelafalan Korea"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                lesson.studyCards.forEachIndexed { idx, card ->
                                    SeoulCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("beginner_study_card_$idx"),
                                        onClick = { onSpeakKorean(card.audioTextKo) }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.Bottom,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = card.hangul,
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = card.romanization,
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                        color = trackAccent,
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = bText(card.meaningEn, card.meaningId),
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = extended.mint
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = bText(card.tipEn, card.tipId),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(trackAccent.copy(alpha = 0.14f))
                                                    .border(1.dp, trackAccent.copy(alpha = 0.35f), CircleShape)
                                                    .tactileClickable(pressedScale = 0.90f) { onSpeakKorean(card.audioTextKo) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = bText("Listen", "Dengar"),
                                                    tint = trackAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                BaeumPrimaryButton(
                                    text = if (lesson.sentenceExercises.isNotEmpty()) {
                                        bText(
                                            "Practice Building Sentences (${lesson.sentenceExercises.size}) →",
                                            "Latihan Menyusun Kalimat (${lesson.sentenceExercises.size}) →"
                                        )
                                    } else {
                                        bText("Start Quick Check →", "Mulai Cek Cepat →")
                                    },
                                    onClick = {
                                        buildIndex = 0
                                        checkIndex = 0
                                        selectedOption = null
                                        streakCount = 0
                                        mistakesCount = 0
                                        checkCorrectCount = 0
                                        selectedTileIds.clear()
                                        buildSolved = false
                                        buildWrongFeedback = false
                                        buildHintVisible = false
                                        phase = if (lesson.sentenceExercises.isNotEmpty()) {
                                            BeginnerLessonPhase.SENTENCE_BUILD
                                        } else {
                                            BeginnerLessonPhase.MINI_CHECK
                                        }
                                    },
                                    testTag = "beginner_lesson_start_build"
                                )
                            }
                        }

                        // ==================== PHASE 2: INTERACTIVE SENTENCE BUILDER ====================
                        BeginnerLessonPhase.SENTENCE_BUILD -> {
                            val exercise = activeExercise ?: lesson.sentenceExercises.first()
                            val assembledTokens = selectedTileIds.mapNotNull { id ->
                                shuffledBank.firstOrNull { it.first == id }?.second
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                BaeumGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .gentleShake(wrongShakeSeq)
                                        .testTag("sentence_builder_card"),
                                    style = BaeumGlassStyle.HERO,
                                    accentGlowColor = if (buildSolved) extended.mint else trackAccent,
                                    borderColor = when {
                                        buildSolved -> extended.mint.copy(alpha = 0.7f)
                                        buildWrongFeedback -> extended.cherryPink.copy(alpha = 0.6f)
                                        else -> Color.Unspecified
                                    },
                                    contentPadding = PaddingValues(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = bText(
                                                "SENTENCE BUILDER ${buildIndex + 1} / ${lesson.sentenceExercises.size}",
                                                "SUSUN KALIMAT ${buildIndex + 1} / ${lesson.sentenceExercises.size}"
                                            ),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.3.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = if (buildSolved) extended.mint else trackAccent
                                        )
                                        // Gentle Hint Toggle Button
                                        Box(
                                            modifier = Modifier
                                                .clip(LessonPillShape)
                                                .background(extended.gold.copy(alpha = 0.14f))
                                                .border(1.dp, extended.gold.copy(alpha = 0.36f), LessonPillShape)
                                                .tactileClickable(pressedScale = 0.94f) {
                                                    buildHintVisible = !buildHintVisible
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lightbulb,
                                                    contentDescription = null,
                                                    tint = extended.gold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = bText("Hint", "Petunjuk"),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = extended.gold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = bText(exercise.promptEn, exercise.promptId),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Answer Assembly Tray (tap placed tile to remove it)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 76.dp)
                                            .clip(LessonOptionShape)
                                            .background(
                                                if (buildSolved) extended.mint.copy(alpha = 0.14f)
                                                else extended.elevatedSurface.copy(alpha = 0.72f)
                                            )
                                            .border(
                                                width = 1.2.dp,
                                                color = when {
                                                    buildSolved -> extended.mint
                                                    buildWrongFeedback -> extended.cherryPink
                                                    assembledTokens.isNotEmpty() -> trackAccent.copy(alpha = 0.55f)
                                                    else -> extended.glassHighlightBorder
                                                },
                                                shape = LessonOptionShape
                                            )
                                            .padding(12.dp),
                                        contentAlignment = if (assembledTokens.isEmpty()) Alignment.Center else Alignment.CenterStart
                                    ) {
                                        if (assembledTokens.isEmpty()) {
                                            Text(
                                                text = bText(
                                                    "Tap tiles below in Korean order...",
                                                    "Ketuk balok kata di bawah sesuai urutan Korea..."
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                selectedTileIds.forEach { tileId ->
                                                    val token = shuffledBank.firstOrNull { it.first == tileId }?.second.orEmpty()
                                                    Box(
                                                        modifier = Modifier
                                                            .defaultMinSize(minHeight = 46.dp)
                                                            .clip(TileChipShape)
                                                            .background(
                                                                if (buildSolved) extended.mint.copy(alpha = 0.24f)
                                                                else trackAccent.copy(alpha = 0.20f)
                                                            )
                                                            .border(
                                                                1.2.dp,
                                                                if (buildSolved) extended.mint else trackAccent,
                                                                TileChipShape
                                                            )
                                                            .tactileClickable(enabled = !buildSolved, pressedScale = 0.94f) {
                                                                selectedTileIds.remove(tileId)
                                                                buildWrongFeedback = false
                                                            }
                                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = token,
                                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (!buildSolved) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Close,
                                                                    contentDescription = bText("Remove", "Hapus"),
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Optional Hint Callout (always shown when toggled or after a wrong check)
                                    if (buildHintVisible || buildWrongFeedback) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(LessonPillShape)
                                                .background(
                                                    if (buildWrongFeedback) extended.cherryPink.copy(alpha = 0.12f)
                                                    else extended.gold.copy(alpha = 0.12f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (buildWrongFeedback) extended.cherryPink.copy(alpha = 0.42f)
                                                    else extended.gold.copy(alpha = 0.36f),
                                                    LessonPillShape
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Lightbulb,
                                                contentDescription = null,
                                                tint = if (buildWrongFeedback) extended.cherryPink else extended.gold,
                                                modifier = Modifier
                                                    .padding(top = 1.dp)
                                                    .size(16.dp)
                                            )
                                            Column {
                                                if (buildWrongFeedback) {
                                                    Text(
                                                        text = bText(
                                                            "Almost there! Tap any tile above to adjust the order.",
                                                            "Hampir benar! Ketuk balok di atas untuk ubah urutannya."
                                                        ),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = extended.cherryPink
                                                    )
                                                }
                                                Text(
                                                    text = bText(exercise.hintEn, exercise.hintId),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    // Solved Celebration Audio Bar
                                    if (buildSolved) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(LessonPillShape)
                                                .background(extended.mint.copy(alpha = 0.14f))
                                                .border(1.dp, extended.mint.copy(alpha = 0.45f), LessonPillShape)
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bText("Perfect Korean! 🎉", "Kalimat Korea Tepat! 🎉"),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = extended.mint
                                                )
                                                Text(
                                                    text = exercise.fullSentenceKo,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = exercise.romanization,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(extended.mint.copy(alpha = 0.20f))
                                                    .border(1.dp, extended.mint.copy(alpha = 0.5f), CircleShape)
                                                    .tactileClickable(pressedScale = 0.90f) {
                                                        onSpeakKorean(exercise.fullSentenceKo)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = bText("Hear sentence", "Dengar kalimat"),
                                                    tint = extended.mint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Shuffled Word / Syllable Tile Bank
                                if (!buildSolved) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = bText("Word & Syllable Bank", "Pilihan Balok Kata"),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (selectedTileIds.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier
                                                        .clip(LessonPillShape)
                                                        .tactileClickable(pressedScale = 0.94f) {
                                                            selectedTileIds.clear()
                                                            buildWrongFeedback = false
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Refresh,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = bText("Reset", "Ulang"),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            shuffledBank.forEach { (tileId, token) ->
                                                val isUsed = tileId in selectedTileIds
                                                Box(
                                                    modifier = Modifier
                                                        .defaultMinSize(minWidth = 60.dp, minHeight = 50.dp)
                                                        .clip(TileChipShape)
                                                        .background(
                                                            if (isUsed) extended.elevatedSurface.copy(alpha = 0.25f)
                                                            else extended.elevatedSurface.copy(alpha = 0.85f)
                                                        )
                                                        .border(
                                                            width = 1.2.dp,
                                                            color = if (isUsed) extended.glassHighlightBorder.copy(alpha = 0.35f)
                                                            else trackAccent.copy(alpha = 0.55f),
                                                            shape = TileChipShape
                                                        )
                                                        .tactileClickable(enabled = !isUsed, pressedScale = 0.94f) {
                                                            selectedTileIds.add(tileId)
                                                            buildWrongFeedback = false
                                                        }
                                                        .testTag("sentence_tile_$tileId")
                                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = token,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
                                                        else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    BaeumPrimaryButton(
                                        text = bText("Check Sentence", "Periksa Susunan"),
                                        enabled = assembledTokens.isNotEmpty(),
                                        onClick = {
                                            if (assembledTokens == exercise.targetTokens) {
                                                buildSolved = true
                                                buildWrongFeedback = false
                                                streakCount += 1
                                                onCheckSound(true, streakCount)
                                                onSpeakKorean(exercise.fullSentenceKo)
                                            } else {
                                                buildWrongFeedback = true
                                                buildHintVisible = true
                                                mistakesCount += 1
                                                streakCount = 0
                                                wrongShakeSeq += 1
                                                onCheckSound(false, 0)
                                            }
                                        },
                                        testTag = "sentence_builder_check_btn"
                                    )
                                } else {
                                    BaeumPrimaryButton(
                                        text = if (buildIndex + 1 < lesson.sentenceExercises.size) {
                                            bText("Next Sentence Challenge →", "Susun Kalimat Berikutnya →")
                                        } else {
                                            bText("Continue to 3-Question Check →", "Lanjut ke Cek Cepat 3 Soal →")
                                        },
                                        onClick = {
                                            if (buildIndex + 1 < lesson.sentenceExercises.size) {
                                                buildIndex += 1
                                            } else {
                                                checkIndex = 0
                                                selectedOption = null
                                                phase = BeginnerLessonPhase.MINI_CHECK
                                            }
                                        },
                                        testTag = "sentence_builder_next_btn"
                                    )
                                }
                            }
                        }

                        // ==================== PHASE 3: 3-QUESTION QUICK CHECK ====================
                        BeginnerLessonPhase.MINI_CHECK -> {
                            val check = lesson.miniChecks[checkIndex.coerceIn(0, lesson.miniChecks.lastIndex)]
                            val isAnswered = selectedOption != null
                            val isCorrect = selectedOption == check.correctOption

                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                AnimatedContent(
                                    targetState = checkIndex to check,
                                    transitionSpec = {
                                        BaeumMotion.directionalContentTransform(
                                            forward = true,
                                            reducedMotion = extended.reducedMotion,
                                            durationMs = 240
                                        )
                                    },
                                    label = "beginner_check_q"
                                ) { (qIdx, q) ->
                                    BaeumGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .gentleShake(wrongShakeSeq),
                                        style = BaeumGlassStyle.HERO,
                                        accentGlowColor = extended.mint,
                                        contentPadding = PaddingValues(22.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = bText(
                                                    "QUICK CHECK ${qIdx + 1} / ${lesson.miniChecks.size}",
                                                    "CEK CEPAT ${qIdx + 1} / ${lesson.miniChecks.size}"
                                                ),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    letterSpacing = 1.4.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = extended.mint
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = bText(q.promptEn, q.promptId),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = q.stimulusKo,
                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(extended.mint.copy(alpha = 0.16f))
                                                    .border(1.dp, extended.mint.copy(alpha = 0.45f), CircleShape)
                                                    .tactileClickable(pressedScale = 0.90f) { onSpeakKorean(q.speakOnTapKo) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = bText("Hear audio", "Dengar audio"),
                                                    tint = extended.mint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    check.options.forEachIndexed { optIdx, option ->
                                        val isPicked = selectedOption == option
                                        val isTrueOpt = option == check.correctOption
                                        val bgColor = when {
                                            isAnswered && isTrueOpt -> extended.mint.copy(alpha = 0.18f)
                                            isAnswered && isPicked && !isTrueOpt -> extended.cherryPink.copy(alpha = 0.16f)
                                            else -> extended.elevatedSurface.copy(alpha = 0.65f)
                                        }
                                        val borderColor = when {
                                            isAnswered && isTrueOpt -> extended.mint
                                            isAnswered && isPicked && !isTrueOpt -> extended.cherryPink
                                            else -> extended.glassHighlightBorder
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 54.dp)
                                                .clip(LessonOptionShape)
                                                .background(bgColor)
                                                .border(1.2.dp, borderColor, LessonOptionShape)
                                                .tactileClickable(enabled = !isAnswered, pressedScale = 0.97f) {
                                                    selectedOption = option
                                                    val right = option == check.correctOption
                                                    if (right) {
                                                        checkCorrectCount += 1
                                                        streakCount += 1
                                                        onCheckSound(true, streakCount)
                                                    } else {
                                                        mistakesCount += 1
                                                        streakCount = 0
                                                        wrongShakeSeq += 1
                                                        onCheckSound(false, 0)
                                                    }
                                                }
                                                .testTag("beginner_check_opt_$optIdx")
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                if (isAnswered) {
                                    SeoulCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        borderColor = (if (isCorrect) extended.mint else extended.gold).copy(alpha = 0.55f)
                                    ) {
                                        Text(
                                            text = if (isCorrect) {
                                                bText("Spot on! ✨", "Tepat sekali! ✨")
                                            } else {
                                                bText("Good try — here's the trick:", "Bagus sudah mencoba — ini rahasianya:")
                                            },
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (isCorrect) extended.mint else extended.gold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = bText(check.explanationEn, check.explanationId),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    BaeumPrimaryButton(
                                        text = if (checkIndex + 1 < lesson.miniChecks.size) {
                                            bText("Next Question →", "Soal Berikutnya →")
                                        } else {
                                            bText("Complete Lesson ${lesson.number} 🎉", "Selesaikan Pelajaran ${lesson.number} 🎉")
                                        },
                                        onClick = {
                                            if (checkIndex + 1 < lesson.miniChecks.size) {
                                                checkIndex += 1
                                                selectedOption = null
                                            } else {
                                                phase = BeginnerLessonPhase.COMPLETE
                                            }
                                        },
                                        testTag = "beginner_check_next_btn"
                                    )
                                }
                            }
                        }

                        // ==================== PHASE 4: COMPLETE + ★★★ MASTERY ====================
                        BeginnerLessonPhase.COMPLETE -> {
                            val earnedPerfect = mistakesCount == 0
                            LaunchedEffect(lesson.id, earnedPerfect) {
                                if (!completionCommitted) {
                                    completionCommitted = true
                                    onLessonCompleted(lesson.id, earnedPerfect)
                                }
                            }

                            val earnedXpDisplay = (if (!isAlreadyCompleted) lesson.xpReward else 0) +
                                (if (earnedPerfect && !isAlreadyPerfect) BeginnerSyllabusCatalog.PERFECT_STAR_BONUS_XP else 0)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BaeumGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("beginner_lesson_complete_card"),
                                    style = BaeumGlassStyle.HERO,
                                    accentGlowColor = if (earnedPerfect) extended.gold else extended.mint,
                                    contentPadding = PaddingValues(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = extended.gold,
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // 3-Star Gold Mastery Seal Display
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            repeat(3) { starIdx ->
                                                val filled = earnedPerfect || starIdx < 2
                                                Icon(
                                                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                    contentDescription = null,
                                                    tint = if (filled) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (earnedPerfect) {
                                                bText("3-Star Perfect Mastery! ★★★", "Sempurna 3 Bintang! ★★★")
                                            } else {
                                                bText("Lesson ${lesson.number} Complete!", "Pelajaran ${lesson.number} Selesai!")
                                            },
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = bText(
                                                "Quick Check: $checkCorrectCount/${lesson.miniChecks.size} · Sentence Builder: ${lesson.sentenceExercises.size}/${lesson.sentenceExercises.size}",
                                                "Cek Cepat: $checkCorrectCount/${lesson.miniChecks.size} · Susun Kalimat: ${lesson.sentenceExercises.size}/${lesson.sentenceExercises.size}"
                                            ),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = extended.mint,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = when {
                                                earnedXpDisplay > 0 -> bText(
                                                    "+$earnedXpDisplay Atelier XP earned!",
                                                    "+$earnedXpDisplay XP Atelier diraih!"
                                                )
                                                earnedPerfect -> bText(
                                                    "Gold ★★★ mastery verified — flawless Korean!",
                                                    "Stempel emas ★★★ terjaga — bahasa Koreamu mulus!"
                                                )
                                                else -> bText(
                                                    "Replay with 0 mistakes anytime to unlock Gold ★★★ (+25 XP)!",
                                                    "Ulangi tanpa salah kapan saja untuk membuka Emas ★★★ (+25 XP)!"
                                                )
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                if (nextLesson != null) {
                                    BaeumPrimaryButton(
                                        text = bText(
                                            "Next: Lesson ${nextLesson.number} · ${nextLesson.titleEn}",
                                            "Lanjut: Pelajaran ${nextLesson.number} · ${nextLesson.titleId}"
                                        ),
                                        onClick = { onOpenNextLesson(nextLesson.id) },
                                        testTag = "beginner_lesson_next_lesson_btn"
                                    )
                                } else if (nextTrack != null) {
                                    BaeumPrimaryButton(
                                        text = bText(
                                            "Track Complete! Start ${nextTrack.shortLabelEn} Syllabus →",
                                            "Silabus Lulus! Masuk ke ${nextTrack.shortLabelId} →"
                                        ),
                                        onClick = { onOpenNextTrack(nextTrack) },
                                        testTag = "beginner_lesson_next_track_btn"
                                    )
                                }

                                if (!earnedPerfect && !isAlreadyPerfect) {
                                    BaeumSecondaryButton(
                                        text = bText("Retry for Gold ★★★ (+25 XP)", "Coba Ulang untuk Emas ★★★ (+25 XP)"),
                                        onClick = { resetLessonForPerfectRetry() },
                                        testTag = "beginner_lesson_retry_perfect_btn"
                                    )
                                }

                                BaeumSecondaryButton(
                                    text = bText(lesson.practiceLabelEn, lesson.practiceLabelId),
                                    onClick = { onOpenPracticeAction(lesson.practiceAction) },
                                    testTag = "beginner_lesson_practice_action_btn"
                                )

                                BaeumSecondaryButton(
                                    text = bText("Back to Syllabus Roadmap", "Kembali ke Peta Silabus"),
                                    onClick = onBack,
                                    testTag = "beginner_lesson_back_roadmap_btn"
                                )
                            }
                        }
                    }
                }
            }
        }

        LuxuryConfettiOverlay(visible = phase == BeginnerLessonPhase.COMPLETE)
    }
}

@Composable
private fun PhaseStepLabel(
    text: String,
    active: Boolean,
    accent: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
        ),
        color = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant
    )
}
