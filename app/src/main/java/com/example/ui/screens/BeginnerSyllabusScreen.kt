package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

private val LessonEmblemShape = RoundedCornerShape(16.dp)
private val LessonPillShape = RoundedCornerShape(12.dp)
private val LessonOptionShape = RoundedCornerShape(16.dp)

/**
 * Guided 4-Stage × 12-Lesson Complete Beginner Syllabus Roadmap ("Zero-to-Seoul Path").
 * Designed specifically for absolute zero-background learners who cannot read Hangul yet.
 */
@Composable
fun BeginnerSyllabusScreen(
    completedLessonIds: Set<String>,
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val completedCount = remember(completedLessonIds) { BeginnerSyllabusCatalog.completionCount(completedLessonIds) }
    val totalLessons = BeginnerSyllabusCatalog.lessons.size
    val progress = remember(completedLessonIds) { BeginnerSyllabusCatalog.completionFraction(completedLessonIds) }
    val nextLesson = remember(completedLessonIds) { BeginnerSyllabusCatalog.nextRecommendedLesson(completedLessonIds) }
    val isGraduated = completedCount >= totalLessons
    val animPct by rememberAnimatedInt((progress * 100f).toInt(), label = "beginner_syllabus_pct")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Beginner Syllabus", "Silabus Pemula dari Nol"),
            subtitle = bText("Zero-to-Seoul · 12 guided lessons", "Dari Nol ke Seoul · 12 pelajaran terpandu"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Roadmap Summary + 1-Tap Next Lesson CTA
            StaggeredEntrance(index = 0) {
                BaeumGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("beginner_syllabus_hero"),
                    style = BaeumGlassStyle.HERO,
                    accentGlowColor = if (isGraduated) extended.gold else extended.mint,
                    contentPadding = PaddingValues(22.dp)
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
                                    tint = if (isGraduated) extended.gold else extended.mint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = bText("COMPLETE BEGINNER ROADMAP", "SILABUS KHUSUS PEMULA NOL"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isGraduated) extended.gold else extended.mint
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isGraduated) {
                                    bText("Syllabus Graduated! 🎉", "Silabus Pemula Lulus! 🎉")
                                } else {
                                    bText(
                                        "$completedCount / $totalLessons lessons completed",
                                        "$completedCount / $totalLessons pelajaran selesai"
                                    )
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    isGraduated -> bText(
                                        "You can read Hangul, greet politely, order in Seoul & build sentences. Review any lesson anytime!",
                                        "Kamu sudah bisa baca Hangul, menyapa sopan, memesan di Seoul & membuat kalimat. Ulas pelajaran kapan saja!"
                                    )
                                    completedCount == 0 -> bText(
                                        "Never read Hangul before? Follow Step 01 → 12 at just 3–4 minutes per lesson.",
                                        "Belum bisa baca Hangul sama sekali? Ikuti Pelajaran 01 → 12 cukup 3–4 menit per sesi."
                                    )
                                    else -> bText(
                                        "Up next: Lesson ${nextLesson.number} — ${nextLesson.titleEn}",
                                        "Berikutnya: Pelajaran ${nextLesson.number} — ${nextLesson.titleId}"
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularGoalRing(
                            progress = progress,
                            centerLabel = "$animPct%",
                            centerSubLabel = "$completedCount/$totalLessons",
                            size = 86.dp,
                            strokeWidth = 7.5.dp,
                            accentColor = if (isGraduated) extended.gold else extended.mint
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BaeumPrimaryButton(
                        text = if (completedCount == 0) {
                            bText("Start Lesson 1 · ${nextLesson.titleEn}", "Mulai Pelajaran 1 · ${nextLesson.titleId}")
                        } else if (isGraduated) {
                            bText("Review Lesson 12 · Verb Formula", "Ulas Pelajaran 12 · Rumus Kata Kerja")
                        } else {
                            bText("Continue Lesson ${nextLesson.number} · ${nextLesson.titleEn}", "Lanjut Pelajaran ${nextLesson.number} · ${nextLesson.titleId}")
                        },
                        onClick = { onOpenLesson(nextLesson.id) },
                        testTag = "beginner_syllabus_continue_cta"
                    )
                }
            }

            // 4 Structured Stages with 3 Numbered Lessons each
            BeginnerSyllabusCatalog.stages.forEachIndexed { stageIdx, stage ->
                val stageLessons = remember(stage.number) { BeginnerSyllabusCatalog.lessonsForStage(stage.number) }
                val stageDone = stageLessons.count { it.id in completedLessonIds }
                val stageAccent = when (stage.accentTag) {
                    "mint" -> extended.mint
                    "gold" -> extended.gold
                    "cherry" -> extended.cherryPink
                    else -> primary
                }

                StaggeredEntrance(index = stageIdx + 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Stage Section Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "STAGE ${stage.number}  ·  ${stage.titleKo}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.4.sp,
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

                        // 3 Lesson Cards in this Stage
                        stageLessons.forEach { lesson ->
                            val isDone = lesson.id in completedLessonIds
                            val isRecommendedNext = !isGraduated && lesson.id == nextLesson.id
                            val emblemBrush = remember(stageAccent, isDone) {
                                Brush.verticalGradient(
                                    listOf(
                                        (if (isDone) extended.mint else stageAccent).copy(alpha = 0.24f),
                                        (if (isDone) extended.mint else stageAccent).copy(alpha = 0.08f)
                                    )
                                )
                            }

                            BaeumGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("beginner_lesson_card_${lesson.number}"),
                                style = if (isRecommendedNext) BaeumGlassStyle.STANDARD else BaeumGlassStyle.SUBTLE,
                                accentGlowColor = if (isRecommendedNext) stageAccent else primary,
                                borderColor = when {
                                    isRecommendedNext -> stageAccent.copy(alpha = 0.72f)
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
                                            .border(
                                                1.dp,
                                                (if (isDone) extended.mint else stageAccent).copy(alpha = 0.5f),
                                                LessonEmblemShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lesson.emblemKo,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (isDone) extended.mint else stageAccent
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

                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = bText("Completed", "Selesai"),
                                            tint = extended.mint,
                                            modifier = Modifier.size(24.dp)
                                        )
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
    }
}

private enum class BeginnerLessonPhase {
    LEARN,
    MINI_CHECK,
    COMPLETE
}

/**
 * Interactive step-by-step Beginner Lesson player:
 * Phase 1: Friendly concept + Key Rule + Interactive Hangul audio study cards
 * Phase 2: 3-Question built-in mini-check with immediate warm explanations & TTS audio
 * Phase 3: Victory summary + Confetti + 1-tap jump to the next lesson or hands-on studio!
 */
@Composable
fun BeginnerLessonScreen(
    lesson: BeginnerLesson?,
    isAlreadyCompleted: Boolean,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onCheckSound: (Boolean, Int) -> Unit,
    onLessonCompleted: (String) -> Unit,
    onOpenNextLesson: (String) -> Unit,
    onOpenPracticeAction: (BeginnerPracticeAction) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary

    if (lesson == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Beginner Lesson", "Pelajaran Pemula"), onBack = onBack)
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

    var phase by remember(lesson.id) { mutableStateOf(BeginnerLessonPhase.LEARN) }
    var checkIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var selectedOption by remember(lesson.id, checkIndex) { mutableStateOf<String?>(null) }
    var checkCorrectCount by remember(lesson.id) { mutableIntStateOf(0) }
    var wrongShakeSeq by remember(lesson.id, checkIndex) { mutableIntStateOf(0) }
    var completionCommitted by remember(lesson.id) { mutableStateOf(false) }

    val nextLesson = remember(lesson.id) {
        BeginnerSyllabusCatalog.lessons.firstOrNull { it.number == lesson.number + 1 }
    }

    val phaseProgress = when (phase) {
        BeginnerLessonPhase.LEARN -> 0.35f
        BeginnerLessonPhase.MINI_CHECK -> 0.45f + ((checkIndex + 1).toFloat() / lesson.miniChecks.size.coerceAtLeast(1)) * 0.45f
        BeginnerLessonPhase.COMPLETE -> 1.0f
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
                title = bText("Lesson ${lesson.number} of ${BeginnerSyllabusCatalog.lessons.size}", "Pelajaran ${lesson.number} dari ${BeginnerSyllabusCatalog.lessons.size}"),
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
                ProgressBarSlim(
                    progress = phaseProgress,
                    color = if (phase == BeginnerLessonPhase.COMPLETE) extended.gold else extended.mint
                )

                // Phase Stepper Ribbon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = bText("1. Learn", "1. Belajar"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (phase == BeginnerLessonPhase.LEARN) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = if (phase == BeginnerLessonPhase.LEARN) primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bText("2. Quick Check (${lesson.miniChecks.size})", "2. Cek Cepat (${lesson.miniChecks.size})"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (phase == BeginnerLessonPhase.MINI_CHECK) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = if (phase == BeginnerLessonPhase.MINI_CHECK) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bText("3. Done", "3. Selesai"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (phase == BeginnerLessonPhase.COMPLETE) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = if (phase == BeginnerLessonPhase.COMPLETE) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant
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
                    label = "beginner_lesson_phase"
                ) { activePhase ->
                    when (activePhase) {
                        BeginnerLessonPhase.LEARN -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Overview & Warm Beginner Explanation
                                BaeumGlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    style = BaeumGlassStyle.HERO,
                                    accentGlowColor = primary
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(LessonEmblemShape)
                                                .background(primary.copy(alpha = 0.18f))
                                                .border(1.dp, primary.copy(alpha = 0.5f), LessonEmblemShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = lesson.emblemKo,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = primary
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
                                    // Golden Beginner Rule Box
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

                                // Interactive Study Cards with TTS Audio & Mnemonics
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
                                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = card.romanization,
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                        color = primary,
                                                        modifier = Modifier.padding(bottom = 3.dp)
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
                                                    .background(primary.copy(alpha = 0.14f))
                                                    .border(1.dp, primary.copy(alpha = 0.35f), CircleShape)
                                                    .tactileClickable(pressedScale = 0.90f) { onSpeakKorean(card.audioTextKo) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = bText("Listen", "Dengar"),
                                                    tint = primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                BaeumPrimaryButton(
                                    text = bText("Start 3-Question Quick Check →", "Mulai Cek Cepat 3 Soal →"),
                                    onClick = {
                                        checkIndex = 0
                                        selectedOption = null
                                        checkCorrectCount = 0
                                        phase = BeginnerLessonPhase.MINI_CHECK
                                    },
                                    testTag = "beginner_lesson_start_check"
                                )
                            }
                        }

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
                                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
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
                                                        onCheckSound(true, checkCorrectCount)
                                                    } else {
                                                        wrongShakeSeq += 1
                                                        onCheckSound(false, 0)
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 13.dp)
                                                .testTag("beginner_check_opt_$optIdx"),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = when {
                                                    isAnswered && isTrueOpt -> extended.mint
                                                    isAnswered && isPicked -> extended.cherryPink
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }

                                if (isAnswered) {
                                    SeoulCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        borderColor = if (isCorrect) extended.mint.copy(alpha = 0.6f) else extended.gold.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = if (isCorrect) {
                                                bText("Spot on! · 잘했어요!", "Tepat sekali! · 잘했어요!")
                                            } else {
                                                bText("Nice try! Correct answer: ${check.correctOption}", "Bagus sudah mencoba! Jawaban tepat: ${check.correctOption}")
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                                        text = if (checkIndex < lesson.miniChecks.lastIndex) {
                                            bText("Next Question →", "Soal Berikutnya →")
                                        } else {
                                            bText("Finish Lesson & Claim +${lesson.xpReward} XP", "Selesaikan Pelajaran & Klaim +${lesson.xpReward} XP")
                                        },
                                        onClick = {
                                            if (checkIndex < lesson.miniChecks.lastIndex) {
                                                checkIndex += 1
                                                selectedOption = null
                                            } else {
                                                if (!completionCommitted) {
                                                    completionCommitted = true
                                                    onLessonCompleted(lesson.id)
                                                }
                                                phase = BeginnerLessonPhase.COMPLETE
                                            }
                                        },
                                        testTag = "beginner_check_next"
                                    )
                                }

                                BaeumSecondaryButton(
                                    text = bText("Review lesson cards again", "Lihat lagi kartu materi"),
                                    onClick = { phase = BeginnerLessonPhase.LEARN }
                                )
                            }
                        }

                        BeginnerLessonPhase.COMPLETE -> {
                            BaeumGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                style = BaeumGlassStyle.HERO,
                                accentGlowColor = extended.gold,
                                contentPadding = PaddingValues(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(extended.gold.copy(alpha = 0.20f))
                                            .border(1.5.dp, extended.gold.copy(alpha = 0.7f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = extended.gold,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = bText("Lesson ${lesson.number} Mastered!", "Pelajaran ${lesson.number} Dikuasai!"),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = extended.gold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bText(
                                            "Quick Check: $checkCorrectCount/${lesson.miniChecks.size} • ${if (isAlreadyCompleted) "Reviewed" else "+${lesson.xpReward} XP earned"}",
                                            "Cek Cepat: $checkCorrectCount/${lesson.miniChecks.size} • ${if (isAlreadyCompleted) "Diulas kembali" else "+${lesson.xpReward} XP diperoleh"}"
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = extended.mint,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bText(lesson.goalEn, lesson.goalId),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (nextLesson != null) {
                                        BaeumPrimaryButton(
                                            text = bText(
                                                "Next: Lesson ${nextLesson.number} · ${nextLesson.titleEn}",
                                                "Lanjut: Pelajaran ${nextLesson.number} · ${nextLesson.titleId}"
                                            ),
                                            onClick = { onOpenNextLesson(nextLesson.id) },
                                            testTag = "beginner_lesson_next_cta"
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    BaeumSecondaryButton(
                                        text = bText(lesson.practiceLabelEn, lesson.practiceLabelId),
                                        onClick = { onOpenPracticeAction(lesson.practiceAction) },
                                        testTag = "beginner_lesson_practice_cta"
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    BaeumSecondaryButton(
                                        text = bText("Back to Syllabus Roadmap", "Kembali ke Peta Silabus"),
                                        onClick = onBack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (phase == BeginnerLessonPhase.COMPLETE) {
            LuxuryConfettiOverlay(triggerKey = lesson.number * 31 + checkCorrectCount + 1)
        }
    }
}
