package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.VocabEntity
import com.example.domain.quiz.ModeProgressStats
import com.example.domain.quiz.QuizMode
import com.example.domain.quiz.QuizSessionState
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.gentleShake
import com.example.ui.theme.localizedBadge
import com.example.ui.theme.localizedSubtitle
import com.example.ui.theme.localizedTitle
import com.example.ui.theme.rememberAnimatedInt
import com.example.ui.theme.tactileClickable

private val QuizModePairs = QuizMode.entries.chunked(2)

// SCREEN 13 / 24 — Quiz Center (Adaptive learning modes including Vocabulary Recall & Sentence Arrangement)
@Composable
fun QuizCenterScreen(
    allVocab: List<VocabEntity>,
    modeProgress: Map<QuizMode, ModeProgressStats> = emptyMap(),
    onBack: () -> Unit,
    onSelectQuizMode: (QuizMode, Boolean) -> Unit
) {
    val weakCount = remember(allVocab) {
        allVocab.count { it.quizWrongCount > it.quizCorrectCount || it.lapses > 0 }
    }
    val modePairs = QuizModePairs
    val extended = LocalBaeumColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Smart Quiz", "Kuis Cerdas"),
            subtitle = bText("Train the skills that matter", "Latih keterampilan yang paling penting"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            modePairs.forEachIndexed { rowIdx, pair ->
                StaggeredEntrance(index = rowIdx) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { mode ->
                            val stats = modeProgress[mode]
                            SeoulCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quiz_mode_${mode.name}"),
                                onClick = { onSelectQuizMode(mode, false) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mode.localizedBadge(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = extended.mint
                                    )
                                    if (stats != null && stats.sessionsCompleted > 0) {
                                        Text(
                                            text = "${stats.accuracyPercent}%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (stats.accuracyPercent >= 80) extended.mint else extended.gold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = mode.localizedTitle(),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mode.localizedSubtitle(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (stats != null && stats.sessionsCompleted > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bText(
                                            "${stats.sessionsCompleted} session${if (stats.sessionsCompleted == 1) "" else "s"} • Best ${stats.bestWeightedScore}%",
                                            "${stats.sessionsCompleted} sesi • Terbaik ${stats.bestWeightedScore}%"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Real data-driven Recommended card
            SeoulCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_recommended_card"),
                onClick = { onSelectQuizMode(QuizMode.MIXED, true) },
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bText("Recommended for you", "Rekomendasi untuk Anda"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (weakCount > 0) {
                                bText(
                                    "Practice $weakCount weak or lapsed Korean item(s)",
                                    "Latih $weakCount materi Korea yang masih perlu diperkuat"
                                )
                            } else {
                                bText(
                                    "Adaptive mixed session across recall, arrangement & grammar",
                                    "Sesi campuran adaptif: kosakata, susun kalimat & tata bahasa"
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// SCREEN 14 / 24 — Active Quiz (Multiple Choice, Hangul Typing, and Sentence Arrangement)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveQuizScreen(
    state: QuizSessionState?,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onSelectOption: (String) -> Unit,
    onTypedChange: (String) -> Unit,
    onAppendToken: (Int) -> Unit = {},
    onRemoveToken: (Int) -> Unit = {},
    onClearTokens: () -> Unit = {},
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestart: () -> Unit
) {
    if (state == null || state.questions.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Quiz", "Kuis"), onBack = onBack)
            Column(modifier = Modifier.padding(20.dp)) {
                SeoulCard {
                    Text(
                        bText("Not enough items for this quiz mode yet.", "Belum cukup materi untuk mode kuis ini."),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BaeumSecondaryButton(text = bText("Back to Quiz Center", "Kembali ke Pusat Kuis"), onClick = onBack)
                }
            }
        }
        return
    }

    val extended = LocalBaeumColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = state.mode.localizedTitle(),
            subtitle = if (state.isCompleted) {
                bText("Session complete", "Sesi selesai")
            } else {
                bText("Question ${state.currentIndex + 1} / ${state.questions.size}", "Soal ${state.currentIndex + 1} / ${state.questions.size}")
            },
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.isCompleted) {
                val eval = state.evaluation
                val displayScore = eval?.weightedMasteryScore
                    ?: ((state.correctCount.toFloat() / state.questions.size.coerceAtLeast(1)) * 100f).toInt()
                val animScore by rememberAnimatedInt(displayScore, label = "quiz_final_score")
                val scoreAccent = if (displayScore >= 70) extended.mint else extended.gold

                StaggeredEntrance(index = 0) {
                    BaeumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        style = BaeumGlassStyle.HERO,
                        accentGlowColor = scoreAccent,
                        contentPadding = PaddingValues(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularGoalRing(
                                progress = displayScore / 100f,
                                centerLabel = "$animScore",
                                centerSubLabel = if (eval != null) bText("Grade ${eval.grade.label}", "Nilai ${eval.grade.label}") else "%",
                                size = 112.dp,
                                strokeWidth = 9.dp,
                                accentColor = scoreAccent
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (eval != null) bText("Grade ${eval.grade.label} • $animScore/100", "Nilai ${eval.grade.label} • $animScore/100") else "$animScore%",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = scoreAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (eval != null) {
                                    bText(
                                        "${eval.correctCount}/${eval.totalQuestions} correct (${eval.rawAccuracyPercent}%) • ${eval.earnedPoints}/${eval.maxPoints} pts • Streak ${eval.maxStreak}",
                                        "${eval.correctCount}/${eval.totalQuestions} benar (${eval.rawAccuracyPercent}%) • ${eval.earnedPoints}/${eval.maxPoints} poin • Runtun ${eval.maxStreak}"
                                    )
                                } else {
                                    bText("${state.correctCount} of ${state.questions.size} correct", "${state.correctCount} dari ${state.questions.size} benar")
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (eval != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = eval.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (eval.skillBreakdowns.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    eval.skillBreakdowns.forEach { sk ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(sk.skillTag, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${sk.correct}/${sk.total} (${sk.accuracyPercent}%)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ProgressBarSlim(progress = sk.accuracyPercent / 100f)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.missedQuestions.isNotEmpty()) {
                    Text(
                        text = bText("Review mistakes (${state.missedQuestions.size})", "Tinjau kesalahan (${state.missedQuestions.size})"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    state.missedQuestions.forEach { missed ->
                        SeoulCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = extended.cherryPink.copy(alpha = 0.5f)
                        ) {
                            Text(missed.stimulusText, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(bText("Correct: ${missed.correctAnswer}", "Benar: ${missed.correctAnswer}"), style = MaterialTheme.typography.labelLarge, color = extended.mint)
                            Text(missed.explanationTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                BaeumPrimaryButton(text = bText("Practice again", "Latihan lagi"), onClick = onRestart, testTag = "quiz_restart_button")
                BaeumSecondaryButton(text = bText("Back to Quiz Center", "Kembali ke Pusat Kuis"), onClick = onBack)
            } else {
                val q = state.currentQuestion!!
                val answeredCount = state.telemetry.size
                val runningAcc = if (answeredCount == 0) 0 else ((state.correctCount.toFloat() / answeredCount) * 100f).toInt()

                ProgressBarSlim(progress = (state.currentIndex + 1).toFloat() / state.questions.size)

                // Live progress, streak & points strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bText("Score: ${state.earnedPoints} pts", "Skor: ${state.earnedPoints} poin"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (state.currentStreak > 0) {
                            bText("Streak: ${state.currentStreak}x", "Runtun: ${state.currentStreak}x")
                        } else {
                            bText("Accuracy: ${if (answeredCount > 0) "$runningAcc%" else "—"}", "Akurasi: ${if (answeredCount > 0) "$runningAcc%" else "—"}")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.currentStreak >= 2) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = q.skillTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Trigger crisp gentle shake on incorrect answer check
                var wrongShakeCount by remember { mutableIntStateOf(0) }
                LaunchedEffect(state.currentIndex, state.isChecked, state.isCorrect) {
                    if (state.isChecked && !state.isCorrect) {
                        wrongShakeCount += 1
                    }
                }

                // Stimulus Card with smooth question-to-question glide & Hero Glass glow
                AnimatedContent(
                    targetState = state.currentIndex to q,
                    transitionSpec = {
                        val ms = if (extended.reducedMotion) 0 else 320
                        (slideInHorizontally(tween(ms, easing = BaeumMotion.SilkEasing)) { (it * 0.16f).toInt() } +
                            fadeIn(tween(ms, easing = BaeumMotion.SilkEasing)) +
                            scaleIn(initialScale = 0.96f, animationSpec = tween(ms, easing = BaeumMotion.SilkEasing)))
                            .togetherWith(
                                slideOutHorizontally(tween((ms * 0.7f).toInt(), easing = BaeumMotion.SilkEasing)) { -(it * 0.14f).toInt() } +
                                    fadeOut(tween((ms * 0.65f).toInt()))
                            )
                    },
                    label = "quiz_stimulus_transition"
                ) { (_, currentQ) ->
                    BaeumGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gentleShake(wrongShakeCount),
                        style = BaeumGlassStyle.HERO,
                        accentGlowColor = when {
                            state.isChecked && state.isCorrect -> extended.mint
                            state.isChecked && !state.isCorrect -> extended.cherryPink
                            else -> MaterialTheme.colorScheme.primary
                        },
                        onClick = currentQ.audioTextKo?.let { txt -> { onSpeakKorean(txt) } },
                        contentPadding = PaddingValues(vertical = 26.dp, horizontal = 22.dp)
                    ) {
                        Text(
                            text = currentQ.promptLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentQ.stimulusText,
                            style = if (currentQ.isArrangementMode) {
                                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (currentQ.audioTextKo != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = bText("Listen", "Dengar"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = bText("Listen in Korean", "Dengar dalam bahasa Korea"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Answer Input: Typing, Sentence Arrangement, or Multiple Choice
                when {
                    q.isTypingMode -> {
                        OutlinedTextField(
                            value = state.typedInput,
                            onValueChange = onTypedChange,
                            enabled = !state.isChecked,
                            placeholder = { Text(bText("Type Korean Hangul (e.g., 학교)...", "Ketik Hangul Korea (mis. 학교)...")) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_typing_input")
                        )
                    }

                    q.isArrangementMode -> {
                        val trayBorder = when {
                            state.isChecked && state.isCorrect -> extended.mint
                            state.isChecked && !state.isCorrect -> extended.cherryPink
                            state.arrangedTokenIndices.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else -> extended.cardBorder
                        }

                        // Sentence construction tray
                        SeoulCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_arrangement_tray"),
                            borderColor = trayBorder,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bText(
                                        "Korean sentence (${state.arrangedTokenIndices.size}/${q.options.size})",
                                        "Kalimat Korea (${state.arrangedTokenIndices.size}/${q.options.size})"
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!state.isChecked && state.arrangedTokenIndices.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onClearTokens() }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("quiz_arrangement_clear"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = bText("Clear arranged words", "Hapus susunan kata"),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = bText("Clear", "Hapus"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (state.arrangedTokenIndices.isEmpty()) {
                                Text(
                                    text = bText(
                                        "Tap the Korean phrases below in natural SOV order",
                                        "Ketuk frasa Korea di bawah sesuai urutan kalimat alami (SOV)"
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    state.arrangedTokenIndices.forEachIndexed { placedPos, optIdx ->
                                        val tokenText = q.options.getOrNull(optIdx).orEmpty()
                                        Surface(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .tactileClickable(enabled = !state.isChecked, pressedScale = 0.93f) { onRemoveToken(placedPos) }
                                                .testTag("quiz_placed_token_$placedPos"),
                                            shape = RoundedCornerShape(18.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = tokenText,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (!state.isChecked) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Filled.Close,
                                                        contentDescription = "Remove $tokenText",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Scrambled phrase bank
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_arrangement_bank")
                        ) {
                            q.options.forEachIndexed { idx, token ->
                                val alreadyUsed = idx in state.arrangedTokenIndices
                                Surface(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .alpha(if (alreadyUsed) 0.32f else 1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .tactileClickable(enabled = !state.isChecked && !alreadyUsed, pressedScale = 0.93f) { onAppendToken(idx) }
                                        .testTag("quiz_arrange_token_$idx"),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, extended.glassHighlightBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = token,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        val letters = listOf("A", "B", "C", "D")
                        q.options.forEachIndexed { idx, opt ->
                            val isSelected = state.selectedOption == opt
                            val isCorrectOpt = opt == q.correctAnswer
                            val targetBorder = when {
                                state.isChecked && isCorrectOpt -> extended.mint
                                state.isChecked && isSelected && !isCorrectOpt -> extended.cherryPink
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> extended.cardBorder.copy(alpha = 0.65f)
                            }
                            val targetBg = when {
                                state.isChecked && isCorrectOpt -> extended.mint.copy(alpha = 0.16f)
                                state.isChecked && isSelected && !isCorrectOpt -> extended.cherryPink.copy(alpha = 0.16f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
                                else -> extended.glassFillTop
                            }
                            val optAnimMs = if (extended.reducedMotion) 0 else 220
                            val borderColor by animateColorAsState(
                                targetValue = targetBorder,
                                animationSpec = tween(optAnimMs),
                                label = "quiz_opt_border_$idx"
                            )
                            val bgColor by animateColorAsState(
                                targetValue = targetBg,
                                animationSpec = tween(optAnimMs),
                                label = "quiz_opt_bg_$idx"
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .tactileClickable(enabled = !state.isChecked, pressedScale = 0.975f) { onSelectOption(opt) }
                                    .testTag("quiz_option_$idx"),
                                shape = RoundedCornerShape(22.dp),
                                color = bgColor,
                                border = BorderStroke(if (isSelected || (state.isChecked && isCorrectOpt)) 1.5.dp else 1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = letters.getOrElse(idx) { "${idx + 1}" },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isSelected || (state.isChecked && isCorrectOpt)) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = borderColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Animated Instant Feedback Card
                AnimatedVisibility(
                    visible = state.isChecked,
                    enter = if (extended.reducedMotion) {
                        fadeIn(tween(0))
                    } else {
                        fadeIn(tween(240, easing = BaeumMotion.SilkEasing)) +
                            expandVertically(animationSpec = BaeumMotion.silkSpring()) +
                            scaleIn(initialScale = 0.96f, animationSpec = BaeumMotion.silkSpring())
                    },
                    exit = fadeOut(tween(if (extended.reducedMotion) 0 else 150))
                ) {
                    val accent = if (state.isCorrect) extended.mint else extended.cherryPink
                    SeoulCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = accent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (state.isCorrect) {
                                    bText("Correct! ${q.correctAnswer}", "Benar! ${q.correctAnswer}")
                                } else {
                                    bText("Answer: ${q.correctAnswer}", "Jawaban: ${q.correctAnswer}")
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = accent,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isCorrect) bText("+${q.points} pts", "+${q.points} poin") else bText("Review", "Tinjau"),
                                style = MaterialTheme.typography.labelMedium,
                                color = accent
                            )
                        }
                        if ((q.isTypingMode || q.isArrangementMode) && state.lastEvaluationHint.isNotBlank() && !state.isCorrect) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.lastEvaluationHint, style = MaterialTheme.typography.labelMedium, color = extended.gold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(q.explanationKo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(q.explanationTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                BaeumPrimaryButton(
                    text = if (!state.isChecked) {
                        bText("Check answer", "Periksa jawaban")
                    } else if (state.currentIndex == state.questions.lastIndex) {
                        bText("View summary", "Lihat ringkasan")
                    } else {
                        bText("Next question", "Soal berikutnya")
                    },
                    enabled = state.isChecked || state.canSubmitCurrent,
                    onClick = { if (!state.isChecked) onCheckAnswer() else onNextQuestion() },
                    testTag = "quiz_action_button"
                )
            }
        }
    }
}
