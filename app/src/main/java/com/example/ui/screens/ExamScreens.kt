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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.audio.SpeechCaptureState
import com.example.data.audio.TtsStatus
import com.example.data.local.ExamAttemptEntity
import com.example.data.seed.ExamPaperLength
import com.example.data.seed.ExamQuestion
import com.example.data.seed.ExamType
import com.example.domain.exam.ActiveExamSession
import com.example.domain.exam.ExamEngine
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
import com.example.ui.theme.LocalBaeumLang
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian
import com.example.ui.theme.localizedLabel
import com.example.ui.theme.localizedSubtitle
import com.example.ui.theme.tactileClickable
import kotlinx.coroutines.delay
import java.util.Locale

private val CIRCLED_NUMBERS = listOf("①", "②", "③", "④", "⑤")

// ==================== SCREEN 20 — EXAMINATION CENTER ====================
@Composable
fun ExamCenterScreen(
    attempts: List<ExamAttemptEntity>,
    onOpenSettings: () -> Unit,
    onStartExam: (ExamType, ExamSessionMode, ExamPaperLength) -> Unit
) {
    var showHistory by remember { mutableStateOf(false) }
    var expandedAttemptId by remember { mutableStateOf<Long?>(null) }
    var briefingExamType by remember { mutableStateOf<ExamType?>(null) }
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val isId = isIndonesian()

    briefingExamType?.let { targetType ->
        ExamBriefingDialog(
            examType = targetType,
            onDismiss = { briefingExamType = null },
            onStartMock = { chosenLength ->
                briefingExamType = null
                onStartExam(targetType, ExamSessionMode.MOCK, chosenLength)
            }
        )
    }

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
                    subtitle = bText(
                        "Official-format TOPIK simulations with OMR, period timers & audio limits",
                        "Simulasi TOPIK format asli lengkap dengan OMR, timer sesi & batas audio"
                    ),
                    onSettingsClick = onOpenSettings
                )
            }

            ExamType.entries.forEachIndexed { idx, examType ->
                val accent = when (idx % 3) {
                    0 -> primary
                    1 -> extended.mint
                    else -> extended.gold
                }
                val bestAttempt = remember(attempts, examType) {
                    attempts.filter { it.examType == examType.name }.maxByOrNull { it.totalScore }
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = examType.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = examType.localizedSubtitle(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(accent.copy(alpha = 0.16f))
                                    .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = examType.badgeKo,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Official format spec strip
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = extended.elevatedSurface.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, extended.cardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (isId) examType.fullQuestionSummaryId else examType.fullQuestionSummaryEn,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isId) examType.passingBandsId else examType.passingBandsEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (bestAttempt != null) {
                                        Text(
                                            text = bText(
                                                "Best ${bestAttempt.totalScore}/${bestAttempt.maxScore}",
                                                "Terbaik ${bestAttempt.totalScore}/${bestAttempt.maxScore}"
                                            ),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = extended.mint
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
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
                                    .tactileClickable(pressedScale = 0.95f) {
                                        onStartExam(examType, ExamSessionMode.PRACTICE, ExamPaperLength.STANDARD)
                                    }
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
                                    .tactileClickable(pressedScale = 0.95f) {
                                        briefingExamType = examType
                                    }
                                    .testTag("exam_mock_${examType.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = bText("Mock exam", "Simulasi TOPIK"),
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
                                    "No completed examinations yet. Start a Practice or TOPIK Mock session above to track your score history.",
                                    "Belum ada ujian yang diselesaikan. Mulai sesi Latihan atau Simulasi TOPIK di atas untuk melacak riwayat skor."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        attempts.forEach { a ->
                            val isExpanded = expandedAttemptId == a.id
                            val parsedSections = remember(a.sectionScoresSerialized) {
                                a.sectionScoresSerialized.split("||").mapNotNull { row ->
                                    val p = row.split("::")
                                    if (p.size == 3) Triple(p[0], p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 100) else null
                                }
                            }
                            SeoulCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { expandedAttemptId = if (isExpanded) null else a.id }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${a.examType.replace('_', ' ')} • ${a.mode}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "${a.estimatedLevel} • ${a.dayKey}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${a.totalScore}/${a.maxScore}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = extended.mint
                                    )
                                }
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val correctCount = (a.totalQuestions - a.wrongCount).coerceAtLeast(0)
                                    val mins = (a.durationSec / 60).coerceAtLeast(1)
                                    Text(
                                        text = bText(
                                            "Correct: $correctCount/${a.totalQuestions} • Duration: ${mins} min",
                                            "Benar: $correctCount/${a.totalQuestions} • Durasi: ${mins} menit"
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (parsedSections.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        parsedSections.forEach { (secName, earned, max) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    localizeExamSection(secName, isId),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    "$earned/$max",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            ProgressBarSlim(progress = earned.toFloat() / max.coerceAtLeast(1))
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                    if (a.analysisSummary.isNotBlank()) {
                                        Text(
                                            text = a.analysisSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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

// ==================== PRE-EXAM OFFICIAL TOPIK BRIEFING DIALOG ====================
@Composable
private fun ExamBriefingDialog(
    examType: ExamType,
    onDismiss: () -> Unit,
    onStartMock: (ExamPaperLength) -> Unit
) {
    var selectedLength by remember { mutableStateOf(ExamPaperLength.STANDARD) }
    val extended = LocalBaeumColors.current
    val isId = isIndonesian()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, extended.glassHighlightBorder)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bText("EXAMINATION BRIEFING", "PANDUAN SIMULASI RESMI"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = extended.gold
                        )
                        Text(
                            text = "${examType.title} (${examType.badgeKo})",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = bText("Close", "Tutup"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Select Paper Length: Compact Mock vs Full Official Simulation
                Text(
                    text = bText("Select simulation length", "Pilih durasi & paket soal"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                listOf(ExamPaperLength.STANDARD, ExamPaperLength.FULL_SIMULATION).forEach { length ->
                    val isSelected = selectedLength == length
                    val summary = when (length) {
                        ExamPaperLength.STANDARD -> if (isId) examType.standardQuestionSummaryId else examType.standardQuestionSummaryEn
                        ExamPaperLength.FULL_SIMULATION -> if (isId) examType.fullQuestionSummaryId else examType.fullQuestionSummaryEn
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedLength = length }
                            .testTag("exam_length_${length.name}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else extended.elevatedSurface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else extended.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isId) length.titleId else length.titleEn,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = length.badgeKo,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else extended.gold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Official Rules Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = extended.elevatedSurface.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, extended.cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = bText("OFFICIAL EXAM RULES (수험 유의사항)", "ATURAN SIMULASI TOPIK (수험 유의사항)"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        val audioRule = when (examType) {
                            ExamType.TOPIK_II -> bText(
                                "• Listening playback cap: 1× for Part 1 (short dialogues), 2× for Part 2 (interviews & lectures).",
                                "• Batas putar audio Menyimak: 1× untuk bagian awal, 2× untuk bagian wawancara & ceramah."
                            )
                            ExamType.TOPIK_SPEAKING -> bText(
                                "• Timed preparation & response phases with optional Korean voice dictation (ko-KR).",
                                "• Fase waktu persiapan & menjawab disertai dikte suara Korea (ko-KR) atau ketik manual."
                            )
                            else -> bText(
                                "• Listening playback cap: maximum 2× per question (matches official TOPIK/EPS audio).",
                                "• Batas putar audio Menyimak: maksimal 2× per soal (sesuai ketentuan resmi TOPIK/EPS)."
                            )
                        }
                        Text(audioRule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        if (examType.hasTwoPeriods) {
                            Text(
                                bText(
                                    "• Two separate timed periods: Period 1 (Listening + Writing 51–54) locks before Period 2 (Reading).",
                                    "• Dua sesi terpisah: Sesi 1 (Menyimak + Menulis 51–54) dikunci sebelum masuk Sesi 2 (Membaca)."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            bText(
                                "• 100% pure Korean test paper — transcripts, translations, and model answers unlock after submission.",
                                "• Naskah soal 100% bahasa Korea — transkrip, pembahasan, dan contoh jawaban terbuka setelah dikumpulkan."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            bText(
                                "• Grade cutoffs: ${examType.passingBandsEn}",
                                "• Ambang kelulusan: ${examType.passingBandsId}"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mint
                        )
                    }
                }

                Text(
                    text = examType.officialReferenceNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                BaeumPrimaryButton(
                    text = bText("Start Mock Exam (시험 시작)", "Mulai Simulasi TOPIK (시험 시작)"),
                    onClick = { onStartMock(selectedLength) },
                    testTag = "exam_briefing_start"
                )
            }
        }
    }
}

// ==================== SCREEN 21 — ACTIVE EXAMINATION (OFFICIAL TOPIK PAPER + OMR) ====================
@Composable
fun ActiveExamScreen(
    session: ActiveExamSession?,
    ttsStatus: TtsStatus,
    speechCaptureState: SpeechCaptureState,
    onAbandonExam: () -> Unit,
    onPlayExamAudio: (String) -> Unit,
    onStopExamAudio: () -> Unit,
    onStartVoiceAnswer: () -> Unit,
    onStopVoiceAnswer: () -> Unit,
    onSelectOption: (String, Int) -> Unit,
    onToggleEliminateOption: (String, Int) -> Unit,
    onOpenResponseChange: (String, String) -> Unit,
    onDualBlankResponseChange: (String, String, String) -> Unit,
    onTogglePracticeCheck: (String) -> Unit,
    onToggleFlag: (String) -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onEnterPeriodTransition: () -> Unit,
    onStartNextPeriod: () -> Unit,
    onSubmitExam: () -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            onStopExamAudio()
        }
    }

    if (session == null || session.currentQuestion == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Examination", "Ujian"), onBack = onAbandonExam)
            Text(
                bText("No active examination.", "Tidak ada ujian yang sedang berlangsung."),
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        return
    }

    var remainingSec by remember(session.deadlineEpochMs, session.activePeriod) {
        mutableLongStateOf(((session.deadlineEpochMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L))
    }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showOmrSheet by remember { mutableStateOf(false) }
    var showPracticeScript by remember(session.currentQuestion?.id) { mutableStateOf(false) }
    var hasTriggeredAutoSubmit by remember(session.startedAtEpochMs) { mutableStateOf(false) }

    // Speaking per-task phase timer state (Prep -> Speak)
    val q = session.currentQuestion!!
    var speakingPhase by remember(q.id) { mutableStateOf("IDLE") } // IDLE, PREP, SPEAK
    var speakingPhaseSec by remember(q.id) { mutableIntStateOf(q.prepSeconds) }

    LaunchedEffect(speakingPhase, q.id) {
        while (speakingPhase == "PREP" || speakingPhase == "SPEAK") {
            if (speakingPhaseSec <= 0) {
                if (speakingPhase == "PREP") {
                    speakingPhase = "SPEAK"
                    speakingPhaseSec = q.speakSeconds.coerceAtLeast(30)
                } else {
                    speakingPhase = "IDLE"
                }
                break
            }
            delay(1000L)
            speakingPhaseSec = (speakingPhaseSec - 1).coerceAtLeast(0)
        }
    }

    // Absolute-clock period countdown survives backgrounding accurately
    LaunchedEffect(session.deadlineEpochMs, session.isSubmitted, session.awaitingPeriodTransition, session.activePeriod) {
        while (!session.isSubmitted && !session.awaitingPeriodTransition) {
            val left = ((session.deadlineEpochMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            remainingSec = left
            if (left == 0L) {
                if (session.hasNextPeriod) {
                    onEnterPeriodTransition()
                } else if (!hasTriggeredAutoSubmit) {
                    hasTriggeredAutoSubmit = true
                    onSubmitExam()
                }
                break
            }
            delay(1000L)
        }
    }

    BackHandler(enabled = !session.isSubmitted) {
        showLeaveDialog = true
    }

    val extended = LocalBaeumColors.current
    val isId = isIndonesian()
    val lang = LocalBaeumLang.current
    val isMock = session.mode == ExamSessionMode.MOCK

    // TOPIK II Period 1 -> Period 2 Intermission Overlay
    if (session.awaitingPeriodTransition && session.hasNextPeriod) {
        PeriodTransitionScreen(
            session = session,
            onStartNextPeriod = onStartNextPeriod,
            onSubmitEarly = {
                if (!hasTriggeredAutoSubmit) {
                    hasTriggeredAutoSubmit = true
                    onSubmitExam()
                }
            }
        )
        return
    }

    if (showOmrSheet) {
        OmrAnswerSheetDialog(
            session = session,
            onDismiss = { showOmrSheet = false },
            onJump = { idx ->
                showOmrSheet = false
                onJumpToQuestion(idx)
            },
            onTriggerFinish = {
                showOmrSheet = false
                if (session.hasNextPeriod) onEnterPeriodTransition() else showSubmitDialog = true
            }
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(bText("Leave active exam?", "Keluar dari ujian aktif?")) },
            text = {
                Text(
                    bText(
                        "You have answered ${session.answeredCount} of ${session.questions.size} questions. You can submit for grading now or exit without saving.",
                        "Anda telah menjawab ${session.answeredCount} dari ${session.questions.size} soal. Anda dapat mengumpulkan untuk dinilai sekarang atau keluar tanpa menyimpan."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    if (!hasTriggeredAutoSubmit) {
                        hasTriggeredAutoSubmit = true
                        onSubmitExam()
                    }
                }) { Text(bText("Submit & grade", "Kumpulkan & nilai")) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    onAbandonExam()
                }) { Text(bText("Exit without saving", "Keluar tanpa menyimpan")) }
            }
        )
    }

    if (showSubmitDialog) {
        val unansweredCount = session.questions.size - session.answeredCount
        val flaggedCount = session.flaggedQuestionIds.size
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(bText("Submit ${session.examType.title} OMR?", "Kumpulkan OMR ${session.examType.title}?")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        bText(
                            "Answered: ${session.answeredCount} / ${session.questions.size} questions",
                            "Terjawab: ${session.answeredCount} / ${session.questions.size} soal"
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (unansweredCount > 0) {
                        Text(
                            bText(
                                "⚠ $unansweredCount unanswered question(s) remain blank on your OMR sheet.",
                                "⚠ Masih ada $unansweredCount soal kosong pada lembar OMR Anda."
                            ),
                            color = extended.cherryPink,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (flaggedCount > 0) {
                        Text(
                            bText(
                                "⚑ $flaggedCount flagged question(s) marked for review.",
                                "⚑ Ada $flaggedCount soal bertanda bendera untuk ditinjau."
                            ),
                            color = extended.gold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitDialog = false
                    if (!hasTriggeredAutoSubmit) {
                        hasTriggeredAutoSubmit = true
                        onSubmitExam()
                    }
                }) { Text(bText("Submit exam", "Kumpulkan ujian")) }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text(bText("Review OMR", "Tinjau lagi")) }
            }
        )
    }

    val timerText = formatCountdown(remainingSec)
    val isFlagged = q.id in session.flaggedQuestionIds
    val localizedSection = localizeExamSection(q.section, isId)
    val periodBadge = if (session.totalPeriods > 1 && isMock) {
        if (session.activePeriod == 1) bText("Period 1 (1교시)", "Sesi 1 (1교시)")
        else bText("Period 2 (2교시)", "Sesi 2 (2교시)")
    } else {
        session.mode.localizedLabel()
    }

    val isPracticeChecked = !isMock && q.id in session.practiceCheckedIds
    val isTtsSpeakingThis = ttsStatus is TtsStatus.Speaking && ttsStatus.text == q.audioTranscriptKo
    val remainingAudio = session.remainingAudioPlays(q)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = session.examType.title,
            subtitle = "$periodBadge • $localizedSection",
            onBack = { showLeaveDialog = true },
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showOmrSheet = true }
                            .testTag("exam_omr_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = "OMR",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "OMR ${session.activePeriodAnsweredCount}/${session.activePeriodIndices.size}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    IconButton(
                        onClick = { onToggleFlag(q.id) },
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
                            contentDescription = bText("Flag question", "Tandai soal"),
                            tint = if (isFlagged) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exam Header Ribbon: Question Number + Weight + Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f))
                    ) {
                        Text(
                            text = if (q.writingTaskNumber != null && q.section == "Writing") {
                                "${q.writingTaskNumber}번"
                            } else {
                                "${session.currentIndex + 1} / ${session.questions.size}"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = extended.gold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, extended.gold.copy(alpha = 0.36f))
                    ) {
                        Text(
                            text = "${q.pointWeight}점",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = extended.gold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (q.questionTypeKo.isNotBlank()) {
                        Text(
                            text = q.questionTypeKo,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (remainingSec < 300) extended.cherryPink.copy(alpha = 0.18f) else extended.elevatedSurface,
                    border = BorderStroke(1.dp, if (remainingSec < 300) extended.cherryPink else extended.cardBorder)
                ) {
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (remainingSec < 300) extended.cherryPink else extended.gold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            ProgressBarSlim(
                progress = session.activePeriodAnsweredCount.toFloat() / session.activePeriodIndices.size.coerceAtLeast(1),
                color = extended.mint
            )

            // Shared Passage / Group Banner (e.g., [25~26] 묶음 듣기 / 묶음 독해)
            if (!q.sharedGroupLabelKo.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = extended.mint.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, extended.mint.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = q.sharedGroupLabelKo,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.mint,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Official Listening Audio Controller Card (when audioTranscriptKo != null)
            if (q.audioTranscriptKo != null) {
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isTtsSpeakingThis) MaterialTheme.colorScheme.primary else extended.cardBorder,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isTtsSpeakingThis) MaterialTheme.colorScheme.primary else extended.mint,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isTtsSpeakingThis) {
                                    bText("Playing Korean audio...", "Memutar audio Korea...")
                                } else {
                                    bText("Korean Listening Audio", "Audio Menyimak Korea")
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val quotaText = when {
                            remainingAudio == null -> bText("Unlimited", "Tak terbatas")
                            remainingAudio > 0 -> bText(
                                "$remainingAudio/${q.maxAudioPlaysInMock} plays left",
                                "Sisa $remainingAudio/${q.maxAudioPlaysInMock}× putar"
                            )
                            else -> bText("Limit reached", "Batas habis")
                        }
                        val quotaColor = when {
                            remainingAudio == null -> extended.mint
                            remainingAudio > 0 -> extended.gold
                            else -> extended.cherryPink
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = quotaColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, quotaColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = quotaText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = quotaColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (ttsStatus is TtsStatus.Unavailable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bText(
                                "Korean TTS voice pack unavailable: ${ttsStatus.reason}",
                                "Suara TTS Korea tidak tersedia: ${ttsStatus.reason}"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.cherryPink
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val canPlay = session.canPlayAudio(q)
                        BaeumSecondaryButton(
                            text = when {
                                isTtsSpeakingThis -> bText("Stop Audio", "Hentikan Audio")
                                !canPlay -> bText("Playback Completed (Mock Rule)", "Pemutaran Selesai (Aturan Ujian)")
                                else -> bText(
                                    "Play Audio (${if (isMock) "${q.maxAudioPlaysInMock}× max" else "ko-KR"})",
                                    "Putar Audio (${if (isMock) "maks ${q.maxAudioPlaysInMock}×" else "ko-KR"})"
                                )
                            },
                            enabled = isTtsSpeakingThis || canPlay,
                            onClick = {
                                if (isTtsSpeakingThis) onStopExamAudio() else onPlayExamAudio(q.id)
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "exam_play_audio"
                        )
                        if (!isMock) {
                            Surface(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 52.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { showPracticeScript = !showPracticeScript },
                                shape = RoundedCornerShape(18.dp),
                                color = extended.elevatedSurface,
                                border = BorderStroke(1.dp, extended.cardBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (showPracticeScript) bText("Hide Script", "Tutup Teks") else bText("Script", "Transkrip"),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (!isMock && showPracticeScript) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = extended.elevatedSurface.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, extended.cardBorder)
                        ) {
                            Text(
                                text = q.audioTranscriptKo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Official TOPIK Paper Stimulus Card (<보기>, [안내문], <자료>, underlined text)
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = q.instructionKo,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (!q.passageBoxKo.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (extended.isDark) {
                            extended.elevatedSurface.copy(alpha = 0.85f)
                        } else {
                            Color(0xFFFDFBF6) // subtle Hanji paper tint in Light Mode
                        },
                        border = BorderStroke(1.dp, extended.glassHighlightBorder)
                    ) {
                        Text(
                            text = buildUnderlinedKoreanText(
                                fullText = q.passageBoxKo,
                                underlinedTarget = q.underlinedText,
                                highlightColor = MaterialTheme.colorScheme.primary
                            ),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = buildUnderlinedKoreanText(
                        fullText = q.stimulusKo,
                        underlinedTarget = q.underlinedText,
                        highlightColor = MaterialTheme.colorScheme.primary
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Answer Input Area: Open-ended (Writing 51-54 / Speaking 1-6) OR MC (① ② ③ ④ + Elimination)
            if (q.isOpenEnded) {
                val draft = session.openResponses[q.id].orEmpty()

                if (q.section == "Speaking") {
                    // Authentic TOPIK Speaking Prep/Speak Timer + Korean STT Voice Capture
                    SeoulCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = bText(
                                        "Prep ${q.prepSeconds}s • Speak ${q.speakSeconds}s",
                                        "Persiapan ${q.prepSeconds}dtk • Jawab ${q.speakSeconds}dtk"
                                    ),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = extended.gold
                                )
                                Text(
                                    text = when (speakingPhase) {
                                        "PREP" -> bText("Preparing... ${speakingPhaseSec}s left", "Persiapan... sisa ${speakingPhaseSec}dtk")
                                        "SPEAK" -> bText("Speaking phase... ${speakingPhaseSec}s left", "Waktu menjawab... sisa ${speakingPhaseSec}dtk")
                                        else -> bText("Tap timer to simulate official IBT timing", "Ketuk timer untuk simulasi waktu resmi IBT")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (speakingPhase == "IDLE") {
                                            speakingPhase = "PREP"
                                            speakingPhaseSec = q.prepSeconds.coerceAtLeast(15)
                                        } else {
                                            speakingPhase = "IDLE"
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = extended.gold.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, extended.gold.copy(alpha = 0.45f))
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (speakingPhase == "IDLE") bText("Start Timer", "Mulai Timer") else bText("Reset", "Ulang"),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = extended.gold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val isListeningVoice = speechCaptureState is SpeechCaptureState.Listening
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isListeningVoice) onStopVoiceAnswer() else onStartVoiceAnswer()
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isListeningVoice) extended.cherryPink.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, if (isListeningVoice) extended.cherryPink else MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isListeningVoice) Icons.Filled.Stop else Icons.Filled.Mic,
                                    contentDescription = null,
                                    tint = if (isListeningVoice) extended.cherryPink else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isListeningVoice) {
                                        bText("Stop Korean Voice Capture", "Hentikan Rekaman Suara Korea")
                                    } else {
                                        bText("Speak Korean Answer (ko-KR Voice)", "Jawab dengan Suara Korea (ko-KR)")
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isListeningVoice) extended.cherryPink else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        if (speechCaptureState is SpeechCaptureState.Listening && speechCaptureState.partialTranscript.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🎙 ${speechCaptureState.partialTranscript}",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.mint
                            )
                        }
                    }
                }

                if (q.dualBlanks) {
                    // Authentic TOPIK II 51번 / 52번 Dual Blank Inputs: (ㄱ) and (ㄴ)
                    val (blankG, blankN) = remember(draft) { ExamEngine.parseDualBlankResponse(draft) }
                    SeoulCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                        Text(
                            text = bText("Write your completion for each blank:", "Tulis pelengkap kalimat untuk masing-masing bagian:"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = blankG,
                            onValueChange = { onDualBlankResponseChange(q.id, it, blankN) },
                            label = { Text("( ㄱ )") },
                            placeholder = { Text(bText("Korean phrase for (ㄱ)...", "Frasa Korea untuk (ㄱ)...")) },
                            minLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exam_open_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = blankN,
                            onValueChange = { onDualBlankResponseChange(q.id, blankG, it) },
                            label = { Text("( ㄴ )") },
                            placeholder = { Text(bText("Korean phrase for (ㄴ)...", "Frasa Korea untuk (ㄴ)...")) },
                            minLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exam_open_input_n")
                        )
                    }
                } else {
                    // Single Open Editor + Official 원고지 Character Counter (for 53번 200~300자, 54번 600~700자, Speaking)
                    val charsWithSpaces = draft.trim().replace(Regex("\\s+"), " ").length.takeIf { draft.isNotBlank() } ?: 0
                    val hangulOnly = draft.count { it.code in 0xAC00..0xD7A3 }
                    val minC = q.targetMinChars
                    val maxC = q.targetMaxChars

                    if (minC > 0) {
                        val inRange = charsWithSpaces >= minC && (maxC <= 0 || charsWithSpaces <= maxC)
                        val overMax = maxC > 0 && charsWithSpaces > maxC
                        val counterColor = when {
                            overMax -> extended.cherryPink
                            inRange -> extended.mint
                            charsWithSpaces >= (minC * 0.6f) -> extended.gold
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        SeoulCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bText(
                                        "Char count (incl. spaces): ${charsWithSpaces}자",
                                        "Jumlah huruf (termasuk spasi): ${charsWithSpaces}자"
                                    ),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = counterColor
                                )
                                Text(
                                    text = if (maxC > 0) "${minC}~${maxC}자" else "≥${minC}자",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            ProgressBarSlim(
                                progress = (charsWithSpaces.toFloat() / minC.coerceAtLeast(1)).coerceIn(0f, 1f),
                                color = counterColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bText(
                                    "Hangul syllables: $hangulOnly • Official TOPIK counts spaces & punctuation (원고지 기준)",
                                    "Suku kata Hangul: $hangulOnly • TOPIK resmi menghitung spasi & tanda baca (standar 원고지)"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draft,
                        onValueChange = { onOpenResponseChange(q.id, it) },
                        placeholder = {
                            Text(
                                if (q.section == "Writing") {
                                    bText("Write your Korean composition in formal written style (-는다/ㄴ다)...", "Tulis karangan Korea dengan gaya tulis formal (-는다/ㄴ다)...")
                                } else {
                                    bText("Speak above or type your Korean response here...", "Bicara dengan mikrofon di atas atau ketik jawaban Korea di sini...")
                                }
                            )
                        },
                        minLines = if (q.writingTaskNumber == 54) 9 else 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exam_open_input")
                    )
                }
            } else {
                // Authentic TOPIK Multiple-Choice Options: Circled ① ② ③ ④ + Strike-through Elimination
                val chosen = session.selectedOptions[q.id]
                val eliminated = session.eliminatedOptions[q.id].orEmpty()

                q.options.forEachIndexed { idx, opt ->
                    val isSelected = chosen == idx
                    val isEliminated = idx in eliminated
                    val isCorrectOption = idx == q.correctOptionIndex
                    val showRightHighlight = isPracticeChecked && isCorrectOption
                    val showWrongHighlight = isPracticeChecked && isSelected && !isCorrectOption

                    val containerBg = when {
                        showRightHighlight -> extended.mint.copy(alpha = 0.20f)
                        showWrongHighlight -> extended.cherryPink.copy(alpha = 0.20f)
                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        showRightHighlight -> extended.mint
                        showWrongHighlight -> extended.cherryPink
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> extended.cardBorder
                    }
                    val circleBg = when {
                        showRightHighlight -> extended.mint
                        showWrongHighlight -> extended.cherryPink
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }
                    val circleTextColor = when {
                        showRightHighlight || showWrongHighlight || isSelected -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectOption(q.id, idx) }
                            .testTag("exam_opt_$idx"),
                        shape = RoundedCornerShape(16.dp),
                        color = containerBg,
                        border = BorderStroke(if (isSelected || showRightHighlight || showWrongHighlight) 1.5.dp else 1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(circleBg)
                                    .border(
                                        1.2.dp,
                                        if (circleBg == Color.Transparent) MaterialTheme.colorScheme.outline else circleBg,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = CIRCLED_NUMBERS.getOrElse(idx) { "${idx + 1}" },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = circleTextColor
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    textDecoration = if (isEliminated && !isSelected) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (isEliminated && !isSelected) 0.42f else 1f)
                            )
                            IconButton(
                                onClick = { onToggleEliminateOption(q.id, idx) },
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = bText("Strike out option", "Coret pilihan"),
                                    tint = if (isEliminated) extended.cherryPink else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Practice Mode Immediate Check & Explanation (STRICTLY hidden in Mock Mode!)
            if (!isMock) {
                BaeumSecondaryButton(
                    text = if (isPracticeChecked) {
                        bText("Hide Explanation", "Sembunyikan Pembahasan")
                    } else {
                        bText("Check Answer & Explanation (Practice)", "Periksa Jawaban & Pembahasan (Latihan)")
                    },
                    onClick = { onTogglePracticeCheck(q.id) },
                    testTag = "exam_practice_check"
                )

                if (isPracticeChecked) {
                    val isRight = !q.isOpenEnded && session.selectedOptions[q.id] == q.correctOptionIndex
                    SeoulCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (q.isOpenEnded) extended.gold else if (isRight) extended.mint else extended.cherryPink
                    ) {
                        if (!q.isOpenEnded) {
                            val correctBadge = CIRCLED_NUMBERS.getOrElse(q.correctOptionIndex) { "${q.correctOptionIndex + 1}" }
                            val correctText = q.options.getOrNull(q.correctOptionIndex).orEmpty()
                            Text(
                                text = if (isRight) bText("Correct!", "Benar!") else bText("Answer: $correctBadge $correctText", "Jawaban benar: $correctBadge $correctText"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isRight) extended.mint else extended.cherryPink
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Text(
                            text = q.explanationFor(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!q.modelAnswerKo.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = bText("Model Answer (모범 답안):", "Contoh Jawaban (모범 답안):"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = extended.mint
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = q.modelAnswerKo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sticky Bottom Exam Navigation Dock (Previous / OMR / Next or Period Transition or Submit)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = extended.dockSurface,
            border = BorderStroke(1.dp, extended.cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val prevIdx = session.previousAccessibleIndex()
                val nextIdx = session.nextAccessibleIndex()
                BaeumSecondaryButton(
                    text = bText("Previous", "Sebelumnya"),
                    enabled = prevIdx != null,
                    onClick = { if (prevIdx != null) onJumpToQuestion(prevIdx) },
                    modifier = Modifier.weight(1f)
                )
                when {
                    nextIdx != null -> {
                        BaeumPrimaryButton(
                            text = bText("Next", "Berikutnya"),
                            onClick = { onJumpToQuestion(nextIdx) },
                            modifier = Modifier.weight(1f),
                            testTag = "exam_next_button"
                        )
                    }
                    session.hasNextPeriod -> {
                        BaeumPrimaryButton(
                            text = bText("Finish Period 1", "Selesai Sesi 1"),
                            onClick = onEnterPeriodTransition,
                            modifier = Modifier.weight(1f),
                            testTag = "exam_finish_period1_button"
                        )
                    }
                    else -> {
                        BaeumPrimaryButton(
                            text = bText("Submit OMR", "Kumpulkan"),
                            onClick = { showSubmitDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "exam_submit_button"
                        )
                    }
                }
            }
        }
    }
}

// ==================== TOPIK II PERIOD 1 -> PERIOD 2 INTERMISSION SCREEN ====================
@Composable
private fun PeriodTransitionScreen(
    session: ActiveExamSession,
    onStartNextPeriod: () -> Unit,
    onSubmitEarly: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val p1Count = session.questions.count { it.period == 1 }
    val p1Answered = session.questions.count { it.period == 1 && session.isQuestionAnswered(it) }
    val p2Count = session.questions.count { it.period == 2 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        BaeumGlassCard(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            style = BaeumGlassStyle.STANDARD,
            accentGlowColor = extended.gold
        ) {
            Text(
                text = bText("TOPIK II • PERIOD INTERMISSION", "TOPIK II • JEDA ANTAR SESI"),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = extended.gold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = bText("Period 1 Complete (1교시 종료)", "Sesi 1 Selesai (1교시 종료)"),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = bText(
                    "Listening & Writing (51–54) answered: $p1Answered / $p1Count.\nOnce you start Period 2 (Reading), Period 1 answers are sealed just like the official NIIED exam.",
                    "Menyimak & Menulis (51–54) terjawab: $p1Answered / $p1Count.\nSetelah memulai Sesi 2 (Membaca), lembar Sesi 1 akan dikunci persis seperti ujian TOPIK II resmi."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = extended.elevatedSurface,
                border = BorderStroke(1.dp, extended.cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = bText("Next: Period 2 — Reading (2교시 읽기)", "Berikutnya: Sesi 2 — Membaca (2교시 읽기)"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = bText(
                            "$p2Count questions • ${session.period2DurationMinutes} minutes",
                            "$p2Count soal • ${session.period2DurationMinutes} menit"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            BaeumPrimaryButton(
                text = bText("Start Period 2: Reading (2교시 시작)", "Mulai Sesi 2: Membaca (2교시 시작)"),
                onClick = onStartNextPeriod,
                testTag = "exam_start_period2_button"
            )
            Spacer(modifier = Modifier.height(8.dp))
            BaeumSecondaryButton(
                text = bText("Submit Exam Now", "Kumpulkan Ujian Sekarang"),
                onClick = onSubmitEarly
            )
        }
    }
}

// ==================== OFFICIAL OMR ANSWER SHEET MODAL ====================
@Composable
private fun OmrAnswerSheetDialog(
    session: ActiveExamSession,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit,
    onTriggerFinish: () -> Unit
) {
    var filter by remember { mutableStateOf("ALL") } // ALL, UNANSWERED, FLAGGED
    val extended = LocalBaeumColors.current
    val isId = isIndonesian()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, extended.glassHighlightBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = bText("OMR ANSWER SHEET (답안지)", "LEMBAR JAWABAN OMR (답안지)"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = bText(
                                "Answered ${session.answeredCount}/${session.questions.size} • Flagged ${session.flaggedQuestionIds.size}",
                                "Terjawab ${session.answeredCount}/${session.questions.size} • Ditandai ${session.flaggedQuestionIds.size}"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = bText("Close", "Tutup"))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "ALL" to bText("All", "Semua"),
                        "UNANSWERED" to bText("Blank", "Kosong"),
                        "FLAGGED" to bText("Flagged", "Ditandai")
                    ).forEach { (key, label) ->
                        val active = filter == key
                        Surface(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { filter = key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else extended.elevatedSurface,
                            border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else extended.cardBorder)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val groupedIndices = session.questions.indices.groupBy { session.questions[it].section }
                    groupedIndices.forEach { (sectionName, indices) ->
                        val filteredIndices = indices.filter { idx ->
                            val q = session.questions[idx]
                            when (filter) {
                                "UNANSWERED" -> !session.isQuestionAnswered(q)
                                "FLAGGED" -> q.id in session.flaggedQuestionIds
                                else -> true
                            }
                        }
                        if (filteredIndices.isEmpty()) return@forEach

                        Text(
                            text = "${localizeExamSection(sectionName, isId)} (${indices.count { session.isQuestionAnswered(session.questions[it]) }}/${indices.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            filteredIndices.forEach { idx ->
                                val item = session.questions[idx]
                                val isCur = idx == session.currentIndex
                                val isLocked = session.isQuestionLocked(idx)
                                val isAccessible = session.isQuestionAccessible(idx)
                                val isFlag = item.id in session.flaggedQuestionIds
                                val chosen = session.selectedOptions[item.id]
                                val isAns = session.isQuestionAnswered(item)

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = isAccessible) { onJump(idx) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isCur -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                        isLocked -> extended.elevatedSurface.copy(alpha = 0.45f)
                                        else -> extended.elevatedSurface
                                    },
                                    border = BorderStroke(
                                        if (isCur || isFlag) 1.5.dp else 1.dp,
                                        when {
                                            isCur -> MaterialTheme.colorScheme.primary
                                            isFlag -> extended.gold
                                            else -> extended.cardBorder
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = if (item.writingTaskNumber != null && item.section == "Writing") {
                                                    "${item.writingTaskNumber}번"
                                                } else {
                                                    String.format(Locale.US, "%02d", idx + 1)
                                                },
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = if (isCur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isLocked) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (isFlag) {
                                                Icon(
                                                    imageVector = Icons.Filled.Flag,
                                                    contentDescription = null,
                                                    tint = extended.gold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        if (item.isOpenEnded) {
                                            Text(
                                                text = if (isAns) bText("Written ✓", "Terisi ✓") else bText("Blank", "Kosong"),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isAns) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                for (b in 0 until item.options.size.coerceAtMost(4)) {
                                                    val filled = chosen == b
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(if (filled) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                            .border(
                                                                1.dp,
                                                                if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "${b + 1}",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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

                Spacer(modifier = Modifier.height(12.dp))
                BaeumPrimaryButton(
                    text = if (session.hasNextPeriod) {
                        bText("Finish Period 1 → Period 2", "Selesai Sesi 1 → Sesi 2")
                    } else {
                        bText("Submit OMR Sheet", "Kumpulkan Lembar OMR")
                    },
                    onClick = onTriggerFinish
                )
            }
        }
    }
}

// ==================== SCREEN 22 — OFFICIAL-STYLE EXAM REPORT & REVIEW ====================
@Composable
fun ExamReportScreen(
    report: ExamReport?,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit = {},
    onRetakeExam: (ExamType, ExamSessionMode, ExamPaperLength) -> Unit = { _, _, _ -> }
) {
    if (report == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Exam result", "Hasil ujian"), onBack = onBack)
            Text(bText("No report available.", "Belum ada laporan tersedia."), modifier = Modifier.padding(24.dp))
        }
        return
    }

    var filterMode by remember { mutableStateOf("INCORRECT") } // ALL, INCORRECT, or section name
    var showAnswerReport by remember { mutableStateOf(false) }
    val extended = LocalBaeumColors.current
    val isId = isIndonesian()
    val lang = LocalBaeumLang.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("${report.mode.localizedLabel()} Certificate", "Laporan ${report.mode.localizedLabel()}"),
            subtitle = "${report.examType.title} (${report.examType.badgeKo})",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Official-Style Score Certificate Hero Card (모의 성적표)
            BaeumGlassCard(
                modifier = Modifier.fillMaxWidth(),
                style = BaeumGlassStyle.STANDARD,
                accentGlowColor = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bText("SIMULATED SCORE REPORT • 모의 성적표", "LAPORAN SKOR SIMULASI • 모의 성적표"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = extended.gold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${report.totalScore} / ${report.maxScore}",
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = bText("Grade ${report.grade.label} • ${report.estimatedLevel}", "Nilai ${report.grade.label} • ${report.estimatedLevel}"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = extended.mint,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bText(
                            "Accuracy ${report.accuracyPercent}% • ${report.avgSecondsPerQuestion}s/q • Practice simulation (Not official)",
                            "Akurasi ${report.accuracyPercent}% • ${report.avgSecondsPerQuestion}d/soal • Simulasi latihan (Bukan resmi)"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (report.cutoffLadder.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            report.cutoffLadder.forEach { band ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (band.achieved) extended.mint.copy(alpha = 0.2f) else extended.elevatedSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (band.achieved) extended.mint else extended.cardBorder
                                    )
                                ) {
                                    Text(
                                        text = if (band.achieved) "✓ ${band.label}" else band.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (band.achieved) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Breakdown Card
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
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${sec.earned}/${sec.max}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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

            val wrongCount = report.questionReviews.count { !it.isCorrect }
            BaeumPrimaryButton(
                text = bText(
                    "Review incorrect answers ($wrongCount)",
                    "Tinjau jawaban salah ($wrongCount)"
                ),
                onClick = {
                    filterMode = "INCORRECT"
                    showAnswerReport = true
                },
                testTag = "exam_review_incorrect"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BaeumSecondaryButton(
                    text = bText("Full report", "Laporan lengkap"),
                    onClick = {
                        filterMode = "ALL"
                        showAnswerReport = true
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "exam_view_full_report"
                )
                BaeumSecondaryButton(
                    text = bText("Retake exam", "Ulangi ujian"),
                    onClick = { onRetakeExam(report.examType, report.mode, report.paperLength) },
                    modifier = Modifier.weight(1f),
                    testTag = "exam_retake_button"
                )
            }

            if (showAnswerReport) {
                val sectionKeys = report.sectionScores.map { it.sectionName }.distinct()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf(
                        "INCORRECT" to bText("Wrong ($wrongCount)", "Salah ($wrongCount)"),
                        "ALL" to bText("All (${report.questionReviews.size})", "Semua (${report.questionReviews.size})")
                    ) + sectionKeys.map { sec -> sec to localizeExamSection(sec, isId) }

                    chips.forEach { (key, label) ->
                        val active = filterMode == key
                        Surface(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { filterMode = key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else extended.elevatedSurface,
                            border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else extended.cardBorder)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                val indexedItems = report.questionReviews.mapIndexed { idx, rev -> idx to rev }.filter { (_, rev) ->
                    when (filterMode) {
                        "INCORRECT" -> !rev.isCorrect
                        "ALL" -> true
                        else -> rev.question.section == filterMode
                    }
                }

                if (indexedItems.isEmpty()) {
                    SeoulCard(modifier = Modifier.fillMaxWidth(), borderColor = extended.mint) {
                        Text(
                            bText(
                                "Zero mistakes in this filter! Every question met the target standard.",
                                "Tanpa kesalahan pada filter ini! Semua soal memenuhi standar target."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = extended.mint
                        )
                    }
                } else {
                    indexedItems.forEach { (origIdx, rev) ->
                        val q = rev.question
                        SeoulCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (rev.isCorrect) extended.mint.copy(alpha = 0.6f) else extended.cherryPink.copy(alpha = 0.6f)
                        ) {
                            val secLabel = localizeExamSection(q.section, isId)
                            val numLabel = if (q.writingTaskNumber != null && q.section == "Writing") "${q.writingTaskNumber}번" else "Q${origIdx + 1}"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$numLabel • $secLabel (${q.pointWeight}점) • ${
                                        if (rev.isCorrect) bText("Correct", "Benar") else bText("Needs review", "Perlu ditinjau")
                                    }",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (rev.isCorrect) extended.mint else extended.cherryPink
                                )
                                if (q.audioTranscriptKo != null) {
                                    Surface(
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onSpeakKorean(q.audioTranscriptKo) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = bText("Replay", "Putar"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            if (!q.audioTranscriptKo.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = extended.elevatedSurface
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = bText("LISTENING SCRIPT (듣기 대본)", "TRANSKRIP AUDIO (듣기 대본)"),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = q.audioTranscriptKo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            if (!q.passageBoxKo.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = buildUnderlinedKoreanText(q.passageBoxKo, q.underlinedText, MaterialTheme.colorScheme.primary),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = buildUnderlinedKoreanText(q.stimulusKo, q.underlinedText, MaterialTheme.colorScheme.primary),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            if (q.isOpenEnded) {
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
                                            "Rubric (${rub.totalCharCountWithSpaces}자) • Relevance ${rub.relevanceScore}% • Length ${rub.completenessScore}% • Grammar ${rub.grammarScore}% • Register ${rub.registerScore}%",
                                            "Rubrik (${rub.totalCharCountWithSpaces}자) • Relevansi ${rub.relevanceScore}% • Panjang ${rub.completenessScore}% • Tata bahasa ${rub.grammarScore}% • Ragam ${rub.registerScore}%"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(rub.feedback, style = MaterialTheme.typography.bodySmall, color = extended.gold)
                                }
                                if (!q.modelAnswerKo.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = extended.mint.copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, extended.mint.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = bText("MODEL ANSWER (모범 답안)", "CONTOH JAWABAN (모범 답안)"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = extended.mint
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = q.modelAnswerKo,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            } else {
                                val pickedBadge = rev.selectedOptionIndex?.let { CIRCLED_NUMBERS.getOrElse(it) { "${it + 1}" } } ?: ""
                                val pickedStr = rev.selectedOptionIndex?.let { "$pickedBadge ${q.options.getOrNull(it).orEmpty()}" }
                                    ?: bText("(unanswered)", "(tidak dijawab)")
                                val correctBadge = CIRCLED_NUMBERS.getOrElse(q.correctOptionIndex) { "${q.correctOptionIndex + 1}" }
                                val correctStr = "$correctBadge ${q.options.getOrNull(q.correctOptionIndex).orEmpty()}"
                                Text(
                                    bText("Your answer: $pickedStr", "Jawaban Anda: $pickedStr"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    bText("Correct answer: $correctStr", "Jawaban benar: $correctStr"),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = extended.mint
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = q.explanationFor(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildUnderlinedKoreanText(
    fullText: String,
    underlinedTarget: String?,
    highlightColor: Color
): AnnotatedString {
    if (underlinedTarget.isNullOrBlank()) return AnnotatedString(fullText)
    val startIdx = fullText.indexOf(underlinedTarget)
    if (startIdx < 0) return AnnotatedString(fullText)
    return buildAnnotatedString {
        append(fullText)
        addStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            ),
            start = startIdx,
            end = startIdx + underlinedTarget.length
        )
    }
}

private fun formatCountdown(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val mins = (safe % 3600) / 60
    val secs = safe % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
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
