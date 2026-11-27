package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CurriculumPathway
import com.example.data.local.DailyStudyEntity
import com.example.data.local.VocabEntity
import com.example.data.repository.BaeumRepository
import com.example.domain.scoring.LearnerReadinessProfile
import com.example.ui.components.BaeumBrandHeader
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian
import com.example.ui.theme.rememberAnimatedInt
import com.example.viewmodel.DashboardSummary
import java.util.Calendar

// SCREEN 23 / 24 — Learning Analytics (Progress strictly grounded in real Room DB records)
@Composable
fun AnalyticsScreen(
    summary: DashboardSummary,
    allVocab: List<VocabEntity>,
    dailyStats: List<DailyStudyEntity>,
    speakingCount: Int,
    examCount: Int,
    readinessProfile: LearnerReadinessProfile? = null,
    onOpenSettings: () -> Unit,
    onRecommendedAction: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary

    val isId = isIndonesian()
    val totalMin = summary.totalStudySeconds / 60
    val studyTimeLabel = if (totalMin >= 60) {
        if (isId) "${totalMin / 60}j ${totalMin % 60}m" else "${totalMin / 60}h ${totalMin % 60}m"
    } else {
        "${totalMin}m"
    }
    val animatedMastered by rememberAnimatedInt(summary.masteredCount, label = "kpi_mastered")
    val animatedStreak by rememberAnimatedInt(summary.streakDays, label = "kpi_streak")

    // Cumulative vocabulary growth over last 7 recorded study days
    val growthSeries = remember(dailyStats) {
        val sorted = dailyStats.sortedBy { it.dayKey }.takeLast(7)
        var running = 0
        sorted.map {
            running += it.wordsReviewed
            running
        }
    }

    // 28-day real activity heatmap cells (4 rows x 7 columns)
    val heatmapLevels = remember(dailyStats) {
        val map = dailyStats.associateBy { it.dayKey }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -27)
        FloatArray(28) {
            val key = BaeumRepository.localDayKey(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val stat = map[key]
            val activity = (stat?.wordsReviewed ?: 0) + (stat?.quizzesCompleted ?: 0) * 5 + (stat?.speakingSessions ?: 0) * 6 + (stat?.examsCompleted ?: 0) * 8
            when {
                activity >= 25 -> 1.0f
                activity >= 12 -> 0.72f
                activity >= 4 -> 0.45f
                activity > 0 -> 0.25f
                else -> 0.0f
            }
        }
    }

    // Single-pass O(N) aggregation over 6,600+ vocab items (zero intermediate lists)
    val curriculumStats = remember(allVocab) {
        val counts = HashMap<String, IntArray>(6)
        for (item in allVocab) {
            val pair = counts.getOrPut(item.curriculum) { IntArray(2) }
            pair[1]++
            if (item.reps > 0) pair[0]++
        }
        counts.mapValues { (_, arr) -> arr[0] to arr[1] }
    }

    var chartRevealed by remember { mutableStateOf(extended.reducedMotion) }
    LaunchedEffect(Unit) { chartRevealed = true }
    val chartRevealState = animateFloatAsState(
        targetValue = if (chartRevealed) 1f else 0f,
        animationSpec = if (extended.reducedMotion) tween(0) else tween(680, easing = BaeumMotion.SilkEasing),
        label = "analytics_curve_reveal"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        StaggeredEntrance(index = 0) {
            BaeumBrandHeader(
                title = bText("Your Progress", "Progres Anda"),
                subtitle = bText("Every step counts.", "Setiap langkah berarti."),
                onSettingsClick = onOpenSettings
            )
        }

        // 2x2 Luminous Glass KPI Grid (Blueprint Screen 23)
        StaggeredEntrance(index = 1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("$animatedMastered", bText("Words mastered", "Kata dikuasai"), primary, Modifier.weight(1f))
                    val streakLabel = if (summary.longestStreakDays > 0) {
                        bText("Streak • Best ${summary.longestStreakDays}d", "Runtun • Terbaik ${summary.longestStreakDays}h")
                    } else {
                        bText("Study streak", "Runtun belajar")
                    }
                    KpiCard(bText("$animatedStreak days", "$animatedStreak hari"), streakLabel, extended.gold, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(studyTimeLabel, bText("Study time", "Waktu belajar"), extended.mint, Modifier.weight(1f))
                    KpiCard(
                        value = summary.quizAccuracyPercent?.let { "$it%" } ?: "—",
                        label = bText("Quiz accuracy", "Akurasi kuis"),
                        accent = extended.cherryPink,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (readinessProfile != null) {
            val animReadiness by rememberAnimatedInt(readinessProfile.overallReadiness, label = "readiness_idx")
            StaggeredEntrance(index = 2) {
                BaeumGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = BaeumGlassStyle.HERO,
                    accentGlowColor = extended.mint,
                    contentPadding = PaddingValues(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bText("READINESS INDEX", "INDEKS KESIAPAN"),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                                color = extended.mint
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bText("Korean Mastery", "Penguasaan Bahasa Korea"),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = readinessProfile.estimatedPracticeTier,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularGoalRing(
                            progress = readinessProfile.overallReadiness / 100f,
                            centerLabel = "$animReadiness",
                            centerSubLabel = "/ 100",
                            size = 86.dp,
                            strokeWidth = 7.5.dp,
                            accentColor = extended.mint
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadinessPill(bText("Memory", "Memori"), "${readinessProfile.vocabRetentionScore}%", primary, Modifier.weight(1f))
                        ReadinessPill(bText("Quiz", "Kuis"), "${readinessProfile.quizMasteryScore}%", extended.gold, Modifier.weight(1f))
                        ReadinessPill(bText("Exam", "Ujian"), "${readinessProfile.examReadinessScore}%", extended.mint, Modifier.weight(1f))
                        ReadinessPill(bText("Speak", "Bicara"), "${readinessProfile.speakingFluencyScore}%", extended.cherryPink, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (readinessProfile.leechCount > 0) {
                            bText(
                                "FSRS retrievability: ${readinessProfile.avgRetrievabilityPercent}% • ${readinessProfile.leechCount} difficult card(s) flagged. ${readinessProfile.nextRecommendedFocus}",
                                "Retensi FSRS: ${readinessProfile.avgRetrievabilityPercent}% • ${readinessProfile.leechCount} kartu sulit ditandai. ${readinessProfile.nextRecommendedFocus}"
                            )
                        } else {
                            readinessProfile.nextRecommendedFocus
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Smooth Silk Bezier Vocabulary Growth Chart (cached Path + Brush with silk reveal)
        StaggeredEntrance(index = 3) {
            BaeumGlassCard(
                modifier = Modifier.fillMaxWidth(),
                style = BaeumGlassStyle.STANDARD,
                accentGlowColor = primary
            ) {
                Text(
                    text = bText("Vocabulary growth", "Pertumbuhan kosakata"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (growthSeries.size < 2) {
                    Text(
                        text = bText(
                            "Complete flashcard reviews across at least 2 days to chart your real vocabulary growth curve.",
                            "Selesaikan ulasan flashcard minimal di 2 hari berbeda untuk menampilkan kurva pertumbuhan kosakata Anda."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maxVal = remember(growthSeries) { growthSeries.max().coerceAtLeast(1) }
                    val mint = extended.mint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .drawWithCache {
                                val padY = 12f
                                val usableH = size.height - padY * 2f
                                val stepX = size.width / (growthSeries.size - 1).coerceAtLeast(1)
                                val pts = growthSeries.mapIndexed { idx, value ->
                                    Offset(
                                        x = idx * stepX,
                                        y = size.height - padY - ((value.toFloat() / maxVal) * usableH)
                                    )
                                }
                                val linePath = Path().apply {
                                    moveTo(pts.first().x, pts.first().y)
                                    for (i in 0 until pts.lastIndex) {
                                        val p0 = pts[i]
                                        val p1 = pts[i + 1]
                                        val midX = (p0.x + p1.x) / 2f
                                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                    }
                                }
                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(pts.last().x, size.height)
                                    lineTo(pts.first().x, size.height)
                                    close()
                                }
                                val fillBrush = Brush.verticalGradient(
                                    colors = listOf(primary.copy(alpha = 0.28f), Color.Transparent)
                                )
                                val lineBrush = Brush.horizontalGradient(listOf(primary, mint))
                                val lineStroke = Stroke(width = 5.5f, cap = StrokeCap.Round)
                                val haloColor = primary.copy(alpha = 0.28f)

                                onDrawBehind {
                                    val revealW = size.width * chartRevealState.value
                                    clipRect(right = revealW) {
                                        drawPath(path = fillPath, brush = fillBrush)
                                        drawPath(path = linePath, brush = lineBrush, style = lineStroke)
                                        for (p in pts) {
                                            drawCircle(color = haloColor, radius = 10f, center = p)
                                            drawCircle(color = mint, radius = 5f, center = p)
                                        }
                                    }
                                }
                            }
                    )
                }
            }
        }

        // 28-Day Learning Activity Heatmap (single GPU-cached Canvas replacing 28 layout Boxes)
        StaggeredEntrance(index = 4) {
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
                    Text(
                        text = bText("Learning activity (28 days)", "Aktivitas belajar (28 hari)"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = bText("${speakingCount} speak • ${examCount} exams", "${speakingCount} bicara • ${examCount} ujian"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                val mintColor = extended.mint
                val emptyBase = extended.elevatedSurface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(117.dp) // 4 rows * 24dp + 3 gaps * 7dp
                        .drawWithCache {
                            val cols = 7
                            val rows = 4
                            val gapPx = 7.dp.toPx()
                            val cellW = (size.width - gapPx * (cols - 1)) / cols
                            val cellH = (size.height - gapPx * (rows - 1)) / rows
                            val corner = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            val rimStroke = Stroke(width = 0.75.dp.toPx())
                            val emptyBrush = Brush.verticalGradient(
                                listOf(emptyBase.copy(alpha = 0.85f), emptyBase.copy(alpha = 0.50f))
                            )
                            val levelBrushes = mapOf(
                                0.25f to Brush.verticalGradient(listOf(primary.copy(alpha = 0.25f), mintColor.copy(alpha = 0.18f))),
                                0.45f to Brush.verticalGradient(listOf(primary.copy(alpha = 0.45f), mintColor.copy(alpha = 0.32f))),
                                0.72f to Brush.verticalGradient(listOf(primary.copy(alpha = 0.72f), mintColor.copy(alpha = 0.52f))),
                                1.0f to Brush.verticalGradient(listOf(primary, mintColor.copy(alpha = 0.72f)))
                            )

                            onDrawBehind {
                                for (idx in 0 until 28) {
                                    val r = idx / cols
                                    val c = idx % cols
                                    val left = c * (cellW + gapPx)
                                    val top = r * (cellH + gapPx)
                                    val intensity = heatmapLevels[idx]
                                    val brush = if (intensity <= 0f) emptyBrush else (levelBrushes[intensity] ?: emptyBrush)
                                    drawRoundRect(
                                        brush = brush,
                                        topLeft = Offset(left, top),
                                        size = Size(cellW, cellH),
                                        cornerRadius = corner
                                    )
                                    if (intensity > 0f) {
                                        drawRoundRect(
                                            color = Color.White.copy(alpha = 0.22f * intensity),
                                            topLeft = Offset(left, top),
                                            size = Size(cellW, cellH),
                                            cornerRadius = corner,
                                            style = rimStroke
                                        )
                                    }
                                }
                            }
                        }
                )
            }
        }

        // Progress by Curriculum Pathway
        StaggeredEntrance(index = 5) {
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bText("Progress by curriculum", "Progres per kurikulum"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                listOf(
                    Triple(CurriculumPathway.HANGUL, bText("Hangul Fundamentals", "Dasar Hangul"), primary),
                    Triple(CurriculumPathway.TOPIK, "TOPIK (1–6급)", extended.mint),
                    Triple(CurriculumPathway.EPS_TOPIK, bText("EPS-TOPIK Workplace", "EPS-TOPIK Dunia Kerja"), extended.gold),
                    Triple(CurriculumPathway.EVERYDAY, bText("Everyday Korean", "Korea Sehari-hari"), extended.cherryPink)
                ).forEach { (key, label, accent) ->
                    val (studied, size) = curriculumStats[key] ?: (0 to 0)
                    val total = size.coerceAtLeast(1)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("$studied / $size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ProgressBarSlim(progress = studied.toFloat() / total, color = accent)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Data-driven recommendation footer
        StaggeredEntrance(index = 6) {
            BaeumGlassCard(
                modifier = Modifier.fillMaxWidth(),
                style = BaeumGlassStyle.STANDARD,
                onClick = onRecommendedAction,
                borderColor = primary.copy(alpha = 0.55f),
                accentGlowColor = primary
            ) {
                Text(
                    text = bText("RECOMMENDED NEXT STEP", "LANGKAH BERIKUTNYA"),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    color = primary
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = summary.recommendedFocus,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReadinessPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(extended.elevatedSurface.copy(alpha = 0.72f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KpiCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    BaeumGlassCard(
        modifier = modifier,
        style = BaeumGlassStyle.SUBTLE,
        accentGlowColor = accent,
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = accent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
