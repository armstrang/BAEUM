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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import com.example.domain.curriculum.BossQuestionKind
import com.example.domain.curriculum.StageBossEngine
import com.example.domain.curriculum.SyllabusMistakeItem
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.LuxuryConfettiOverlay
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.gentleShake
import com.example.ui.theme.tactileClickable
import kotlinx.coroutines.delay
import kotlin.random.Random

private val BossPillShape = RoundedCornerShape(12.dp)
private val BossOptionShape = RoundedCornerShape(16.dp)
private val BossTileShape = RoundedCornerShape(14.dp)

/**
 * Timed 60–90s Stage Boss Gauntlet at the end of every syllabus stage.
 * Combines Listening Discrimination, Tap-to-Assemble Sentence Order, and Rapid Mastery Checks
 * to unlock that stage's exclusive Dojang Boss Seal + 120 XP Bounty!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StageBossScreen(
    stageNumber: Int,
    isAlreadyDefeated: Boolean,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onCheckSound: (Boolean, Int) -> Unit,
    onBossFinished: (stageNumber: Int, passed: Boolean, correctCount: Int, totalCount: Int) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val challenge = remember(stageNumber) { StageBossEngine.buildChallenge(stageNumber) }

    if (challenge == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Stage Boss", "Bos Tahap"), onBack = onBack)
            Column(modifier = Modifier.padding(20.dp)) {
                SeoulCard {
                    Text(bText("Stage Boss not found.", "Bos Tahap tidak ditemukan."))
                    Spacer(modifier = Modifier.height(10.dp))
                    BaeumSecondaryButton(text = bText("Back", "Kembali"), onClick = onBack)
                }
            }
        }
        return
    }

    var attemptSeed by remember(stageNumber) { mutableIntStateOf(0) }
    var currentIndex by remember(stageNumber, attemptSeed) { mutableIntStateOf(0) }
    var remainingSeconds by remember(stageNumber, attemptSeed) { mutableIntStateOf(challenge.timeLimitSeconds) }
    var correctCount by remember(stageNumber, attemptSeed) { mutableIntStateOf(0) }
    var streak by remember(stageNumber, attemptSeed) { mutableIntStateOf(0) }
    var finished by remember(stageNumber, attemptSeed) { mutableStateOf(false) }
    var submittedCallback by remember(stageNumber, attemptSeed) { mutableStateOf(false) }
    var shakeSeq by remember(stageNumber, attemptSeed) { mutableIntStateOf(0) }

    // Per-round state
    var pickedOption by remember(stageNumber, attemptSeed, currentIndex) { mutableStateOf<String?>(null) }
    val placedIndices = remember(stageNumber, attemptSeed, currentIndex) { mutableStateListOf<Int>() }
    var buildChecked by remember(stageNumber, attemptSeed, currentIndex) { mutableStateOf(false) }
    var buildWasCorrect by remember(stageNumber, attemptSeed, currentIndex) { mutableStateOf(false) }

    val activeQuestion = challenge.questions.getOrNull(currentIndex)

    // Countdown clock
    LaunchedEffect(stageNumber, attemptSeed, finished) {
        while (!finished && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        }
        if (!finished && remainingSeconds <= 0) {
            finished = true
        }
    }

    // Auto-speak audio discrimination prompts on entry
    LaunchedEffect(stageNumber, attemptSeed, currentIndex, finished) {
        if (!finished && activeQuestion?.kind == BossQuestionKind.AUDIO_DISCRIMINATION) {
            onSpeakKorean(activeQuestion.speakKo)
        }
    }

    val victory = StageBossEngine.isVictory(correctCount, challenge)
    LaunchedEffect(finished) {
        if (finished && !submittedCallback) {
            submittedCallback = true
            onBossFinished(stageNumber, victory, correctCount, challenge.questions.size)
        }
    }

    fun advanceOrFinish() {
        if (currentIndex + 1 < challenge.questions.size) {
            currentIndex += 1
        } else {
            finished = true
        }
    }

    val timerAccent = when {
        remainingSeconds <= 15 -> extended.cherryPink
        remainingSeconds <= 30 -> extended.gold
        else -> extended.mint
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
                title = bText("Stage Boss · ${challenge.stage.titleKo}", "Bos Tahap · ${challenge.stage.titleKo}"),
                subtitle = bText(
                    "Need ${challenge.passThreshold}/${challenge.questions.size} to unlock Seal '${challenge.stage.bossSealKo}'",
                    "Capai ${challenge.passThreshold}/${challenge.questions.size} untuk buka Stempel '${challenge.stage.bossSealKo}'"
                ),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!finished && activeQuestion != null) {
                    // Top HUD: Timer + Round Counter + Combo Flame
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(BossPillShape)
                                .background(timerAccent.copy(alpha = 0.16f))
                                .border(1.dp, timerAccent.copy(alpha = 0.45f), BossPillShape)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = timerAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${remainingSeconds}s",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = timerAccent
                            )
                        }

                        Text(
                            text = "${currentIndex + 1} / ${challenge.questions.size}  ·  ✓ $correctCount",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier
                                .clip(BossPillShape)
                                .background(extended.gold.copy(alpha = 0.14f))
                                .border(1.dp, extended.gold.copy(alpha = 0.4f), BossPillShape)
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = extended.gold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${streak}x",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = extended.gold
                            )
                        }
                    }

                    ProgressBarSlim(
                        progress = (currentIndex.toFloat() / challenge.questions.size.coerceAtLeast(1)).coerceIn(0f, 1f),
                        color = extended.gold
                    )

                    AnimatedContent(
                        targetState = currentIndex to activeQuestion,
                        transitionSpec = {
                            BaeumMotion.directionalContentTransform(
                                forward = true,
                                reducedMotion = extended.reducedMotion,
                                durationMs = 220
                            )
                        },
                        label = "stage_boss_round"
                    ) { (_, q) ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (q.kind) {
                                BossQuestionKind.AUDIO_DISCRIMINATION -> {
                                    val answered = pickedOption != null
                                    BaeumGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .gentleShake(shakeSeq),
                                        style = BaeumGlassStyle.HERO,
                                        accentGlowColor = primary,
                                        contentPadding = PaddingValues(22.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = bText("🎧 LISTENING ROUND", "🎧 RONDE DENGAR"),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    letterSpacing = 1.4.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = primary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = bText(q.promptEn, q.promptId),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(CircleShape)
                                                    .background(primary.copy(alpha = 0.20f))
                                                    .border(1.5.dp, primary, CircleShape)
                                                    .tactileClickable(pressedScale = 0.90f) { onSpeakKorean(q.speakKo) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = bText("Play Korean audio", "Putar audio Korea"),
                                                    tint = primary,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = bText("Tap to replay audio", "Ketuk untuk putar ulang"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        q.options.forEach { opt ->
                                            val isPicked = pickedOption == opt
                                            val isRight = opt == q.correctOption
                                            val bg = when {
                                                answered && isRight -> extended.mint.copy(alpha = 0.20f)
                                                answered && isPicked && !isRight -> extended.cherryPink.copy(alpha = 0.18f)
                                                else -> extended.elevatedSurface.copy(alpha = 0.7f)
                                            }
                                            val border = when {
                                                answered && isRight -> extended.mint
                                                answered && isPicked && !isRight -> extended.cherryPink
                                                else -> extended.glassHighlightBorder
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 54.dp)
                                                    .clip(BossOptionShape)
                                                    .background(bg)
                                                    .border(1.2.dp, border, BossOptionShape)
                                                    .tactileClickable(enabled = !answered, pressedScale = 0.97f) {
                                                        pickedOption = opt
                                                        if (opt == q.correctOption) {
                                                            correctCount += 1
                                                            streak += 1
                                                            onCheckSound(true, streak)
                                                        } else {
                                                            streak = 0
                                                            shakeSeq += 1
                                                            onCheckSound(false, 0)
                                                        }
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = opt,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    if (answered) {
                                        BossFeedbackStrip(
                                            isCorrect = pickedOption == q.correctOption,
                                            explanation = bText(q.explanationEn, q.explanationId),
                                            onNext = { advanceOrFinish() },
                                            isLast = currentIndex + 1 == challenge.questions.size
                                        )
                                    }
                                }

                                BossQuestionKind.SENTENCE_ORDER -> {
                                    val assembled = placedIndices.mapNotNull { q.shuffledBank.getOrNull(it) }
                                    BaeumGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .gentleShake(shakeSeq),
                                        style = BaeumGlassStyle.HERO,
                                        accentGlowColor = extended.gold,
                                        contentPadding = PaddingValues(20.dp)
                                    ) {
                                        Text(
                                            text = bText("🧩 BOSS SENTENCE BUILD", "🧩 SUSUN KALIMAT BOS"),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.4.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = extended.gold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = bText(q.promptEn, q.promptId),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 68.dp)
                                                .clip(BossOptionShape)
                                                .background(extended.elevatedSurface.copy(alpha = 0.72f))
                                                .border(
                                                    1.2.dp,
                                                    when {
                                                        buildChecked && buildWasCorrect -> extended.mint
                                                        buildChecked && !buildWasCorrect -> extended.cherryPink
                                                        else -> extended.glassHighlightBorder
                                                    },
                                                    BossOptionShape
                                                )
                                                .padding(10.dp),
                                            contentAlignment = if (assembled.isEmpty()) Alignment.Center else Alignment.CenterStart
                                        ) {
                                            if (assembled.isEmpty()) {
                                                Text(
                                                    text = bText("Tap tiles below in order...", "Ketuk balok di bawah berurutan..."),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    placedIndices.forEach { bankIdx ->
                                                        val tok = q.shuffledBank.getOrNull(bankIdx).orEmpty()
                                                        Box(
                                                            modifier = Modifier
                                                                .defaultMinSize(minHeight = 44.dp)
                                                                .clip(BossTileShape)
                                                                .background(extended.gold.copy(alpha = 0.22f))
                                                                .border(1.2.dp, extended.gold, BossTileShape)
                                                                .tactileClickable(enabled = !buildChecked, pressedScale = 0.94f) {
                                                                    placedIndices.remove(bankIdx)
                                                                }
                                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = tok,
                                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (!buildChecked) {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            q.shuffledBank.forEachIndexed { idx, tok ->
                                                val used = idx in placedIndices
                                                Box(
                                                    modifier = Modifier
                                                        .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
                                                        .clip(BossTileShape)
                                                        .background(
                                                            if (used) extended.elevatedSurface.copy(alpha = 0.25f)
                                                            else extended.elevatedSurface.copy(alpha = 0.85f)
                                                        )
                                                        .border(
                                                            1.2.dp,
                                                            if (used) extended.glassHighlightBorder.copy(alpha = 0.3f)
                                                            else extended.gold.copy(alpha = 0.55f),
                                                            BossTileShape
                                                        )
                                                        .tactileClickable(enabled = !used, pressedScale = 0.94f) {
                                                            placedIndices.add(idx)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = tok,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (used) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                        else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        BaeumPrimaryButton(
                                            text = bText("Lock Answer", "Kunci Jawaban"),
                                            enabled = assembled.isNotEmpty(),
                                            onClick = {
                                                buildChecked = true
                                                val ok = assembled == q.targetTokens
                                                buildWasCorrect = ok
                                                if (ok) {
                                                    correctCount += 1
                                                    streak += 1
                                                    onCheckSound(true, streak)
                                                    onSpeakKorean(q.speakKo)
                                                } else {
                                                    streak = 0
                                                    shakeSeq += 1
                                                    onCheckSound(false, 0)
                                                }
                                            }
                                        )
                                    } else {
                                        BossFeedbackStrip(
                                            isCorrect = buildWasCorrect,
                                            explanation = bText(q.explanationEn, q.explanationId),
                                            onNext = { advanceOrFinish() },
                                            isLast = currentIndex + 1 == challenge.questions.size
                                        )
                                    }
                                }

                                BossQuestionKind.RAPID_CHECK -> {
                                    val answered = pickedOption != null
                                    BaeumGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .gentleShake(shakeSeq),
                                        style = BaeumGlassStyle.HERO,
                                        accentGlowColor = extended.mint,
                                        contentPadding = PaddingValues(22.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = bText("⚡ RAPID BOSS CHECK", "⚡ CEK KILAT BOS"),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    letterSpacing = 1.4.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = extended.mint
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = bText(q.promptEn, q.promptId),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = q.stimulusKo,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        q.options.forEach { opt ->
                                            val isPicked = pickedOption == opt
                                            val isRight = opt == q.correctOption
                                            val bg = when {
                                                answered && isRight -> extended.mint.copy(alpha = 0.20f)
                                                answered && isPicked && !isRight -> extended.cherryPink.copy(alpha = 0.18f)
                                                else -> extended.elevatedSurface.copy(alpha = 0.7f)
                                            }
                                            val border = when {
                                                answered && isRight -> extended.mint
                                                answered && isPicked && !isRight -> extended.cherryPink
                                                else -> extended.glassHighlightBorder
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 52.dp)
                                                    .clip(BossOptionShape)
                                                    .background(bg)
                                                    .border(1.2.dp, border, BossOptionShape)
                                                    .tactileClickable(enabled = !answered, pressedScale = 0.97f) {
                                                        pickedOption = opt
                                                        if (opt == q.correctOption) {
                                                            correctCount += 1
                                                            streak += 1
                                                            onCheckSound(true, streak)
                                                        } else {
                                                            streak = 0
                                                            shakeSeq += 1
                                                            onCheckSound(false, 0)
                                                        }
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = opt,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    if (answered) {
                                        BossFeedbackStrip(
                                            isCorrect = pickedOption == q.correctOption,
                                            explanation = bText(q.explanationEn, q.explanationId),
                                            onNext = { advanceOrFinish() },
                                            isLast = currentIndex + 1 == challenge.questions.size
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Boss Result Card
                    BaeumGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stage_boss_result_card"),
                        style = BaeumGlassStyle.HERO,
                        accentGlowColor = if (victory) extended.gold else extended.cherryPink,
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(74.dp)
                                    .clip(CircleShape)
                                    .background((if (victory) extended.gold else extended.cherryPink).copy(alpha = 0.20f))
                                    .border(2.dp, if (victory) extended.gold else extended.cherryPink, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = challenge.stage.bossSealKo,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (victory) extended.gold else extended.cherryPink
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (victory) {
                                    bText("Stage Boss Defeated! ⚔️", "Bos Tahap Tumbang! ⚔️")
                                } else {
                                    bText("So Close — Try Again!", "Sedikit Lagi — Coba Lagi!")
                                },
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = bText(
                                    "Score: $correctCount / ${challenge.questions.size} (Pass: ${challenge.passThreshold}/${challenge.questions.size})",
                                    "Skor: $correctCount / ${challenge.questions.size} (Lulus: ${challenge.passThreshold}/${challenge.questions.size})"
                                ),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (victory) extended.mint else extended.gold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    victory && !isAlreadyDefeated -> bText(
                                        "Exclusive Dojang Boss Seal '${challenge.stage.bossSealKo}' unlocked · +${challenge.xpReward} XP!",
                                        "Stempel Dojang Bos '${challenge.stage.bossSealKo}' terbuka · +${challenge.xpReward} XP!"
                                    )
                                    victory -> bText(
                                        "Boss Seal '${challenge.stage.bossSealKo}' defended with mastery!",
                                        "Stempel Bos '${challenge.stage.bossSealKo}' berhasil dipertahankan!"
                                    )
                                    else -> bText(
                                        "Review the stage lessons and strike back anytime — zero penalties!",
                                        "Ulas singkat pelajaran tahap ini lalu tantang lagi kapan saja — tanpa penalti!"
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (!victory) {
                        BaeumPrimaryButton(
                            text = bText("Rematch Stage Boss ⚔️", "Tantang Ulang Bos ⚔️"),
                            onClick = { attemptSeed += 1 }
                        )
                    }
                    BaeumSecondaryButton(
                        text = bText("Back to Syllabus Roadmap", "Kembali ke Peta Silabus"),
                        onClick = onBack
                    )
                }
            }
        }

        LuxuryConfettiOverlay(triggerKey = if (finished && victory) 1 else 0)
    }
}

@Composable
private fun BossFeedbackStrip(
    isCorrect: Boolean,
    explanation: String,
    onNext: () -> Unit,
    isLast: Boolean
) {
    val extended = LocalBaeumColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SeoulCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = (if (isCorrect) extended.mint else extended.gold).copy(alpha = 0.55f)
        ) {
            Text(
                text = if (isCorrect) bText("Direct Hit! ⚡", "Tepat Sasaran! ⚡") else bText("Counter-Tip:", "Catatan Cepat:"),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isCorrect) extended.mint else extended.gold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        BaeumPrimaryButton(
            text = if (isLast) bText("Finish Boss Battle ⚔️", "Akhiri Pertarungan Bos ⚔️") else bText("Next Round →", "Ronde Berikutnya →"),
            onClick = onNext
        )
    }
}

/**
 * Mistake Redemption Vault ("Balas Dendam Kesalahan"):
 * Lets learners clear stored mistakes from Sentence Builders & Quick Checks for +20 XP each!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MistakeRedemptionScreen(
    mistakeKeys: Set<String>,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onCheckSound: (Boolean, Int) -> Unit,
    onRedeemMistake: (String) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val items = remember(mistakeKeys) {
        mistakeKeys.mapNotNull { BeginnerSyllabusCatalog.resolveMistakeItem(it) }
    }
    var redeemedSessionCount by remember { mutableIntStateOf(0) }
    var activeKey by remember(items) { mutableStateOf(items.firstOrNull()?.key) }
    val currentItem = items.firstOrNull { it.key == activeKey } ?: items.firstOrNull()
    var shakeSeq by remember(currentItem?.key) { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BaeumTopBar(
                title = bText("Mistake Redemption Drill", "Balas Dendam Kesalahan"),
                subtitle = bText(
                    "${items.size} pending · +${BeginnerSyllabusCatalog.REDEMPTION_BONUS_XP} XP each",
                    "${items.size} tersisa · +${BeginnerSyllabusCatalog.REDEMPTION_BONUS_XP} XP per soal"
                ),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (currentItem == null) {
                    BaeumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        style = BaeumGlassStyle.HERO,
                        accentGlowColor = extended.mint,
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = extended.mint,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = bText("Mistake Vault Cleared! ✨", "Brankas Kesalahan Bersih! ✨"),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (redeemedSessionCount > 0) {
                                    bText(
                                        "You redeemed $redeemedSessionCount mistake(s) for +${redeemedSessionCount * BeginnerSyllabusCatalog.REDEMPTION_BONUS_XP} XP!",
                                        "Kamu menaklukkan $redeemedSessionCount kesalahan dengan +${redeemedSessionCount * BeginnerSyllabusCatalog.REDEMPTION_BONUS_XP} XP!"
                                    )
                                } else {
                                    bText(
                                        "Zero pending mistakes! Any slip during lessons will appear here for bonus XP redemption.",
                                        "Tidak ada kesalahan tersisa! Setiap soal yang sempat salah di pelajaran akan muncul di sini untuk ditaklukkan kembali."
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    BaeumPrimaryButton(
                        text = bText("Back to Syllabus", "Kembali ke Silabus"),
                        onClick = onBack
                    )
                } else {
                    when (currentItem) {
                        is SyllabusMistakeItem.SentenceBuildMistake -> {
                            val ex = currentItem.exercise
                            val bank = remember(currentItem.key) {
                                val combined = (ex.targetTokens + ex.distractorTokens).mapIndexed { i, t -> i to t }
                                combined.shuffled(Random(currentItem.key.hashCode()))
                            }
                            val placed = remember(currentItem.key) { mutableStateListOf<Int>() }
                            var wrongFlag by remember(currentItem.key) { mutableStateOf(false) }

                            BaeumGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .gentleShake(shakeSeq),
                                style = BaeumGlassStyle.HERO,
                                accentGlowColor = extended.cherryPink,
                                contentPadding = PaddingValues(20.dp)
                            ) {
                                Text(
                                    text = bText(
                                        "🔥 REDEMPTION · ${currentItem.lesson.titleEn}",
                                        "🔥 BALAS DENDAM · ${currentItem.lesson.titleId}"
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = extended.cherryPink
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = bText(ex.promptEn, ex.promptId),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 68.dp)
                                        .clip(BossOptionShape)
                                        .background(extended.elevatedSurface.copy(alpha = 0.72f))
                                        .border(
                                            1.2.dp,
                                            if (wrongFlag) extended.cherryPink else extended.glassHighlightBorder,
                                            BossOptionShape
                                        )
                                        .padding(10.dp),
                                    contentAlignment = if (placed.isEmpty()) Alignment.Center else Alignment.CenterStart
                                ) {
                                    if (placed.isEmpty()) {
                                        Text(
                                            text = bText("Rebuild the Korean sentence cleanly...", "Susun ulang kalimat Korea dengan tepat..."),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            placed.forEach { tid ->
                                                val tok = bank.firstOrNull { it.first == tid }?.second.orEmpty()
                                                Box(
                                                    modifier = Modifier
                                                        .defaultMinSize(minHeight = 44.dp)
                                                        .clip(BossTileShape)
                                                        .background(primary.copy(alpha = 0.2f))
                                                        .border(1.2.dp, primary, BossTileShape)
                                                        .tactileClickable(pressedScale = 0.94f) {
                                                            placed.remove(tid)
                                                            wrongFlag = false
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(tok, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                                                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (wrongFlag) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💡 " + bText(ex.hintEn, ex.hintId),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extended.gold
                                    )
                                }
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                bank.forEach { (tid, tok) ->
                                    val used = tid in placed
                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
                                            .clip(BossTileShape)
                                            .background(
                                                if (used) extended.elevatedSurface.copy(alpha = 0.25f)
                                                else extended.elevatedSurface.copy(alpha = 0.85f)
                                            )
                                            .border(
                                                1.2.dp,
                                                if (used) extended.glassHighlightBorder.copy(alpha = 0.3f)
                                                else primary.copy(alpha = 0.55f),
                                                BossTileShape
                                            )
                                            .tactileClickable(enabled = !used, pressedScale = 0.94f) {
                                                placed.add(tid)
                                                wrongFlag = false
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tok,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (used) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            BaeumPrimaryButton(
                                text = bText("Redeem Mistake (+20 XP)", "Taklukkan Kesalahan (+20 XP)"),
                                enabled = placed.isNotEmpty(),
                                onClick = {
                                    val assembled = placed.mapNotNull { id -> bank.firstOrNull { it.first == id }?.second }
                                    if (assembled == ex.targetTokens) {
                                        redeemedSessionCount += 1
                                        onSpeakKorean(ex.fullSentenceKo)
                                        onRedeemMistake(currentItem.key)
                                        activeKey = items.firstOrNull { it.key != currentItem.key }?.key
                                    } else {
                                        wrongFlag = true
                                        shakeSeq += 1
                                        onCheckSound(false, 0)
                                    }
                                }
                            )
                        }

                        is SyllabusMistakeItem.MiniCheckMistake -> {
                            val chk = currentItem.check
                            var wrongOpt by remember(currentItem.key) { mutableStateOf<String?>(null) }

                            BaeumGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .gentleShake(shakeSeq),
                                style = BaeumGlassStyle.HERO,
                                accentGlowColor = extended.cherryPink,
                                contentPadding = PaddingValues(20.dp)
                            ) {
                                Text(
                                    text = bText(
                                        "🔥 REDEMPTION · ${currentItem.lesson.titleEn}",
                                        "🔥 BALAS DENDAM · ${currentItem.lesson.titleId}"
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = extended.cherryPink
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = bText(chk.promptEn, chk.promptId),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = chk.stimulusKo,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (wrongOpt != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💡 " + bText(chk.explanationEn, chk.explanationId),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extended.gold
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                chk.options.forEach { opt ->
                                    val isBad = wrongOpt == opt
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 52.dp)
                                            .clip(BossOptionShape)
                                            .background(
                                                if (isBad) extended.cherryPink.copy(alpha = 0.16f)
                                                else extended.elevatedSurface.copy(alpha = 0.7f)
                                            )
                                            .border(
                                                1.2.dp,
                                                if (isBad) extended.cherryPink else extended.glassHighlightBorder,
                                                BossOptionShape
                                            )
                                            .tactileClickable(pressedScale = 0.97f) {
                                                if (opt == chk.correctOption) {
                                                    redeemedSessionCount += 1
                                                    onSpeakKorean(chk.speakOnTapKo)
                                                    onRedeemMistake(currentItem.key)
                                                    activeKey = items.firstOrNull { it.key != currentItem.key }?.key
                                                } else {
                                                    wrongOpt = opt
                                                    shakeSeq += 1
                                                    onCheckSound(false, 0)
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LuxuryConfettiOverlay(triggerKey = if (currentItem == null && redeemedSessionCount > 0) 1 else 0)
    }
}
