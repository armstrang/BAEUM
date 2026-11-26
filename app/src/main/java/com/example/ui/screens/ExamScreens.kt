package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamAttemptEntity
import com.example.data.seed.ExamType
import com.example.domain.exam.ActiveExamSession
import com.example.domain.exam.ExamReport
import com.example.domain.exam.ExamSessionMode
import com.example.ui.components.BaeumBrandHeader
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian
import com.example.ui.theme.localizedLabel
import com.example.ui.theme.localizedSubtitle
import com.example.ui.theme.tactileClickable
import kotlinx.coroutines.delay

// SCREEN 20 / 24 — Examination Center (Independent exam types + History)
@Composable
fun ExamCenterScreen(
    attempts: List<ExamAttemptEntity>,
    onOpenSettings: () -> Unit,
    onStartExam: (ExamType, ExamSessionMode) -> Unit
) {
    var showHistory by remember { mutableStateOf(false) }
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary

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
                    title = bText("Exam Center", "Pusat Ujian"),
                    subtitle = bText("Realistic timed examinations & readiness reports", "Simulasi ujian berbatas waktu & laporan kesiapan"),
                    onSettingsClick = onOpenSettings
                )
            }

            ExamType.entries.forEachIndexed { idx, examType ->
                val accent = when (idx % 3) {
                    0 -> primary
                    1 -> extended.mint
                    else -> extended.gold
                }
                StaggeredEntrance(index = idx + 1) {
                    BaeumGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exam_card_${examType.name}"),
                        style = BaeumGlassStyle.STANDARD,
                        accentGlowColor = accent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = examType.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(accent.copy(alpha = 0.16f))
                                    .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = examType.badgeKo,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = examType.localizedSubtitle(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(extended.elevatedSurface.copy(alpha = 0.78f))
                                    .border(1.dp, extended.glassHighlightBorder, RoundedCornerShape(18.dp))
                                    .tactileClickable(pressedScale = 0.95f) { onStartExam(examType, ExamSessionMode.PRACTICE) }
                                    .testTag("exam_practice_${examType.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bText("Practice", "Latihan"),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.12f))
                                        )
                                    )
                                    .border(1.dp, accent.copy(alpha = 0.52f), RoundedCornerShape(18.dp))
                                    .tactileClickable(pressedScale = 0.95f) { onStartExam(examType, ExamSessionMode.MOCK) }
                                    .testTag("exam_mock_${examType.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = bText("Mock exam", "Ujian simulasi"),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = accent
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            StaggeredEntrance(index = ExamType.entries.size + 1) {
                BaeumSecondaryButton(
                    text = if (showHistory) {
                        bText("Hide examination history (${attempts.size})", "Sembunyikan riwayat ujian (${attempts.size})")
                    } else {
                        bText("Examination history (${attempts.size})", "Riwayat ujian (${attempts.size})")
                    },
                    onClick = { showHistory = !showHistory },
                    testTag = "exam_history_toggle"
                )
            }

            AnimatedVisibility(
                visible = showHistory,
                enter = fadeIn(tween(240, easing = BaeumMotion.SilkEasing)) + expandVertically(BaeumMotion.silkSpring()),
                exit = fadeOut(tween(180)) + shrinkVertically(BaeumMotion.silkSpring())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (attempts.isEmpty()) {
                        SeoulCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                bText(
                                    "No completed examinations yet. Start a Practice or Mock session above to track your score history.",
                                    "Belum ada ujian yang diselesaikan. Mulai sesi Latihan atau Simulasi di atas untuk melacak riwayat skor."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        attempts.forEach { a ->
                            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${a.examType.replace('_', ' ')} • ${a.mode}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${a.estimatedLevel} • ${a.dayKey}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "${a.totalScore}/${a.maxScore}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = extended.mint
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// SCREEN 21 / 24 — Active Examination (Absolute-clock timer, flag, question navigator, autosave)
@Composable
fun ActiveExamScreen(
    session: ActiveExamSession?,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onSelectOption: (String, Int) -> Unit,
    onOpenResponseChange: (String, String) -> Unit,
    onToggleFlag: (String) -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onSubmitExam: () -> Unit
) {
    if (session == null || session.currentQuestion == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Examination", "Ujian"), onBack = onBack)
            Text(bText("No active examination.", "Tidak ada ujian yang sedang berlangsung."), modifier = Modifier.padding(24.dp))
        }
        return
    }

    var remainingSec by remember(session.deadlineEpochMs) {
        mutableLongStateOf(((session.deadlineEpochMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L))
    }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    // Absolute-clock countdown survives backgrounding accurately!
    LaunchedEffect(session.deadlineEpochMs, session.isSubmitted) {
        while (!session.isSubmitted) {
            val left = ((session.deadlineEpochMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            remainingSec = left
            if (left == 0L) {
                onSubmitExam()
                break
            }
            delay(1000L)
        }
    }

    BackHandler(enabled = !session.isSubmitted) {
        showLeaveDialog = true
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(bText("Leave active exam?", "Keluar dari ujian aktif?")) },
            text = {
                Text(
                    bText(
                        "You have answered ${session.answeredCount} of ${session.questions.size} questions. You can submit now or leave without grading.",
                        "Anda telah menjawab ${session.answeredCount} dari ${session.questions.size} soal. Anda dapat mengumpulkan sekarang atau keluar tanpa penilaian."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    onSubmitExam()
                }) { Text(bText("Submit & grade", "Kumpulkan & nilai")) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    onBack()
                }) { Text(bText("Exit without saving", "Keluar tanpa menyimpan")) }
            }
        )
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(bText("Submit ${session.examType.title}?", "Kumpulkan ${session.examType.title}?")) },
            text = {
                Text(
                    bText(
                        "Answered ${session.answeredCount} / ${session.questions.size} questions. Submit to calculate your practice score and error analysis.",
                        "Terjawab ${session.answeredCount} / ${session.questions.size} soal. Kumpulkan untuk menghitung skor latihan dan analisis kesalahan."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitDialog = false
                    onSubmitExam()
                }) { Text(bText("Submit exam", "Kumpulkan ujian")) }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text(bText("Continue reviewing", "Lanjut meninjau")) }
            }
        )
    }

    val q = session.currentQuestion!!
    val extended = LocalBaeumColors.current
    val isId = isIndonesian()
    val timerText = String.format(java.util.Locale.US, "%02d:%02d", remainingSec / 60, remainingSec % 60)
    val isFlagged = q.id in session.flaggedQuestionIds
    val localizedSection = localizeExamSection(q.section, isId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = session.examType.title,
            subtitle = bText(
                "$localizedSection section • ${session.mode.localizedLabel()}",
                "Bagian $localizedSection • ${session.mode.localizedLabel()}"
            ),
            onBack = { showLeaveDialog = true },
            trailing = {
                IconButton(onClick = { onToggleFlag(q.id) }) {
                    Icon(
                        imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
                        contentDescription = bText("Flag question", "Tandai soal"),
                        tint = if (isFlagged) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bText(
                        "Question ${session.currentIndex + 1} of ${session.questions.size}",
                        "Soal ${session.currentIndex + 1} dari ${session.questions.size}"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timerText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (remainingSec < 120) extended.cherryPink else extended.gold
                )
            }

            // Stimulus Card
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(q.instructionKo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = q.stimulusKo,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (q.audioTranscriptKo != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    BaeumSecondaryButton(
                        text = bText("Play Korean Listening Audio", "Putar Audio Menyimak Korea"),
                        onClick = { onSpeakKorean(q.audioTranscriptKo) }
                    )
                }
            }

            if (q.isOpenEnded) {
                val draft = session.openResponses[q.id].orEmpty()
                OutlinedTextField(
                    value = draft,
                    onValueChange = { onOpenResponseChange(q.id, it) },
                    placeholder = { Text(bText("Write your Korean response here...", "Tulis jawaban bahasa Korea Anda di sini...")) },
                    minLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_open_input")
                )
            } else {
                val chosen = session.selectedOptions[q.id]
                q.options.forEachIndexed { idx, opt ->
                    val isSelected = chosen == idx
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectOption(q.id, idx) }
                            .testTag("exam_opt_$idx"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else extended.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Question Navigator Grid (Blueprint Screen 21) — 5 columns at 48dp min touch height
            SeoulCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                Text(
                    text = bText("QUESTION NAVIGATOR", "NAVIGATOR SOAL"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                val rows = session.questions.indices.chunked(5)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { rowIndices ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowIndices.forEach { idx ->
                                val item = session.questions[idx]
                                val isCur = idx == session.currentIndex
                                val isAns = if (item.isOpenEnded) !session.openResponses[item.id].isNullOrBlank()
                                else session.selectedOptions.containsKey(item.id)
                                val flagged = item.id in session.flaggedQuestionIds
                                val bg = when {
                                    isCur -> MaterialTheme.colorScheme.primary
                                    isAns -> extended.mint.copy(alpha = 0.22f)
                                    else -> extended.elevatedSurface
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onJumpToQuestion(idx) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = bg,
                                    border = if (flagged) BorderStroke(1.5.dp, extended.gold) else null
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCur) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            repeat(5 - rowIndices.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // Previous / Next / Submit row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BaeumSecondaryButton(
                    text = bText("Previous", "Sebelumnya"),
                    enabled = session.currentIndex > 0,
                    onClick = { onJumpToQuestion(session.currentIndex - 1) },
                    modifier = Modifier.weight(1f)
                )
                if (session.currentIndex < session.questions.lastIndex) {
                    BaeumPrimaryButton(
                        text = bText("Next", "Berikutnya"),
                        onClick = { onJumpToQuestion(session.currentIndex + 1) },
                        modifier = Modifier.weight(1f),
                        testTag = "exam_next_button"
                    )
                } else {
                    BaeumPrimaryButton(
                        text = bText("Submit exam", "Kumpulkan"),
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "exam_submit_button"
                    )
                }
            }
        }
    }
}

// SCREEN 22 / 24 — Examination Report (Scores, section breakdown, error review, non-official disclaimer)
@Composable
fun ExamReportScreen(
    report: ExamReport?,
    onBack: () -> Unit
) {
    if (report == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Exam result", "Hasil ujian"), onBack = onBack)
            Text(bText("No report available.", "Belum ada laporan tersedia."), modifier = Modifier.padding(24.dp))
        }
        return
    }

    var filterIncorrectOnly by remember { mutableStateOf(false) }
    var showAnswerReport by remember { mutableStateOf(false) }
    val extended = LocalBaeumColors.current
    val isId = isIndonesian()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("${report.mode.localizedLabel()} result", "Hasil ${report.mode.localizedLabel()}"),
            subtitle = report.examType.title,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "${report.totalScore} / ${report.maxScore}",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = bText("Grade ${report.grade.label} • ${report.estimatedLevel}", "Nilai ${report.grade.label} • ${report.estimatedLevel}"),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = extended.mint,
                textAlign = TextAlign.Center
            )
            Text(
                text = bText(
                    "Accuracy ${report.accuracyPercent}% • ${report.avgSecondsPerQuestion}s/q • Practice result (Not official)",
                    "Akurasi ${report.accuracyPercent}% • ${report.avgSecondsPerQuestion}d/soal • Hasil latihan (Bukan resmi)"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Section Breakdown Card (Weighted)
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                report.sectionScores.forEachIndexed { idx, sec ->
                    val secName = localizeExamSection(sec.sectionName, isId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (sec.isEstimated) {
                                bText("$secName (Rubric est.)", "$secName (Est. rubrik)")
                            } else {
                                "$secName (${sec.correctCount}/${sec.totalCount})"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${sec.earned}/${sec.max}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ProgressBarSlim(progress = sec.earned.toFloat() / sec.max.coerceAtLeast(1))
                    if (idx < report.sectionScores.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (report.tierBreakdowns.size >= 2) {
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        bText("Difficulty tier performance", "Performa tingkat kesulitan"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    report.tierBreakdowns.forEachIndexed { idx, tier ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tier.tierLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${tier.correct}/${tier.total} (${tier.percent}%)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        ProgressBarSlim(progress = tier.percent / 100f, color = extended.mint)
                        if (idx < report.tierBreakdowns.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    bText("Performance analysis", "Analisis performa"),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(report.analysisSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (report.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    report.recommendations.forEach { rec ->
                        Text("• $rec", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            BaeumPrimaryButton(
                text = bText(
                    "Review incorrect answers (${report.questionReviews.count { !it.isCorrect }})",
                    "Tinjau jawaban salah (${report.questionReviews.count { !it.isCorrect }})"
                ),
                onClick = {
                    filterIncorrectOnly = true
                    showAnswerReport = true
                },
                testTag = "exam_review_incorrect"
            )
            BaeumSecondaryButton(
                text = bText("View full answer report", "Lihat laporan jawaban lengkap"),
                onClick = {
                    filterIncorrectOnly = false
                    showAnswerReport = true
                },
                testTag = "exam_view_full_report"
            )

            if (showAnswerReport) {
                val items = if (filterIncorrectOnly) report.questionReviews.filter { !it.isCorrect } else report.questionReviews
                if (items.isEmpty()) {
                    SeoulCard(modifier = Modifier.fillMaxWidth(), borderColor = extended.mint) {
                        Text(
                            bText(
                                "Zero mistakes! Every question met the target standard.",
                                "Tanpa kesalahan! Semua soal memenuhi standar target."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = extended.mint
                        )
                    }
                } else {
                    items.forEachIndexed { _, rev ->
                        SeoulCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (rev.isCorrect) extended.mint.copy(alpha = 0.6f) else extended.cherryPink.copy(alpha = 0.6f)
                        ) {
                            val secLabel = localizeExamSection(rev.question.section, isId)
                            Text(
                                text = "$secLabel (${rev.question.pointWeight}점) • ${
                                    if (rev.isCorrect) bText("Correct", "Benar") else bText("Needs review", "Perlu ditinjau")
                                }",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (rev.isCorrect) extended.mint else extended.cherryPink
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(rev.question.stimulusKo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (rev.question.isOpenEnded) {
                                Text(
                                    bText(
                                        "Your response: ${rev.openResponseText.orEmpty().ifBlank { "(blank)" }}",
                                        "Jawaban Anda: ${rev.openResponseText.orEmpty().ifBlank { "(kosong)" }}"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                rev.rubricScore?.let { rub ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bText(
                                            "Rubric • Relevance ${rub.relevanceScore}% • Length ${rub.completenessScore}% • Grammar ${rub.grammarScore}% • Register ${rub.registerScore}%",
                                            "Rubrik • Relevansi ${rub.relevanceScore}% • Kelengkapan ${rub.completenessScore}% • Tata bahasa ${rub.grammarScore}% • Kesopanan ${rub.registerScore}%"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(rub.feedback, style = MaterialTheme.typography.bodySmall, color = extended.gold)
                                }
                            } else {
                                val pickedStr = rev.selectedOptionIndex?.let { rev.question.options.getOrNull(it) } ?: bText("(unanswered)", "(tidak dijawab)")
                                val correctStr = rev.question.options.getOrNull(rev.question.correctOptionIndex).orEmpty()
                                Text(bText("Your answer: $pickedStr", "Jawaban Anda: $pickedStr"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(bText("Correct answer: $correctStr", "Jawaban benar: $correctStr"), style = MaterialTheme.typography.labelLarge, color = extended.mint)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(rev.question.explanationEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun localizeExamSection(section: String, isIndonesian: Boolean): String {
    if (!isIndonesian) return section
    return when (section.lowercase()) {
        "listening" -> "Menyimak"
        "reading" -> "Membaca"
        "writing" -> "Menulis"
        "speaking" -> "Berbicara"
        else -> section
    }
}
