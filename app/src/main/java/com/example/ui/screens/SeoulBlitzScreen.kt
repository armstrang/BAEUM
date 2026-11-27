package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engagement.SeoulBlitzState
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.ComboFlamePill
import com.example.ui.components.FloatingXpBadge
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

private val BlitzOptionShape = RoundedCornerShape(18.dp)
private val BlitzBadgeShape = RoundedCornerShape(14.dp)

/**
 * 60-Second Seoul Blitz (`60초 서울 블리츠`) — high-flow rapid Korean recall arcade loop
 * with ascending pentatonic combo multipliers, Fever Mode (+2s bonus per 5-streak), personal high scores,
 * and instant 1-tap replay.
 */
@Composable
fun SeoulBlitzScreen(
    state: SeoulBlitzState?,
    onBack: () -> Unit,
    onStartOrReplay: () -> Unit,
    onSelectOption: (String) -> Unit,
    onSpeakKorean: (String) -> Unit,
    onAbandon: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val haptics = LocalHapticFeedback.current

    BackHandler {
        onAbandon()
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            onAbandon()
        }
    }

    var wrongShakeSeq by remember { mutableIntStateOf(0) }
    LaunchedEffect(state?.lastFeedbackSeq) {
        if (state != null && state.lastFeedbackSeq > 0) {
            if (state.lastAnswerWasCorrect == false) {
                wrongShakeSeq += 1
            } else if (state.lastAnswerWasCorrect == true) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
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
                title = bText("60s Seoul Blitz", "60d Seoul Blitz"),
                subtitle = bText("Rapid flow recall · Beat your record", "Uji refleks Korea · Pecahkan rekormu"),
                onBack = {
                    onAbandon()
                    onBack()
                }
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state == null || (state.currentPrompt == null && !state.isFinished)) {
                    BaeumGlassCard(modifier = Modifier.fillMaxWidth(), style = BaeumGlassStyle.HERO) {
                        Text(
                            text = bText("Preparing your Korean Blitz deck...", "Menyiapkan dek Seoul Blitz..."),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        BaeumPrimaryButton(text = bText("Start 60s Blitz", "Mulai 60d Blitz"), onClick = onStartOrReplay)
                    }
                    return@Column
                }

                if (state.isFinished) {
                    val animScore by rememberAnimatedInt(state.score, label = "blitz_final_score")
                    val accent = if (state.isNewRecord) extended.gold else extended.mint
                    val bestToBeat = maxOf(state.score, state.previousBestScore)

                    StaggeredEntrance(index = 0) {
                        BaeumGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            style = BaeumGlassStyle.HERO,
                            accentGlowColor = accent,
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (state.isNewRecord) {
                                    Row(
                                        modifier = Modifier
                                            .clip(BlitzBadgeShape)
                                            .background(extended.gold.copy(alpha = 0.22f))
                                            .border(1.dp, extended.gold.copy(alpha = 0.65f), BlitzBadgeShape)
                                            .padding(horizontal = 12.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = extended.gold, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = bText("NEW PERSONAL RECORD · 신기록!", "REKOR PRIBADI BARU · 신기록!"),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = extended.gold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                CircularGoalRing(
                                    progress = state.accuracyPercent / 100f,
                                    centerLabel = "$animScore",
                                    centerSubLabel = bText("points", "poin"),
                                    size = 120.dp,
                                    strokeWidth = 9.dp,
                                    accentColor = accent
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = if (state.xpEarned > 0) "+${state.xpEarned} XP" else bText("Time's up!", "Waktu habis!"),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = accent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bText(
                                        "${state.correctCount}/${state.answeredCount} correct (${state.accuracyPercent}%) • Peak ${state.maxCombo}x combo",
                                        "${state.correctCount}/${state.answeredCount} benar (${state.accuracyPercent}%) • Kombo puncak ${state.maxCombo}x"
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (state.timeBonusEarnedSec > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bText(
                                            "+${state.timeBonusEarnedSec}s Fever time bonus earned!",
                                            "+${state.timeBonusEarnedSec}d bonus waktu Fever diperoleh!"
                                        ),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = extended.mint
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                BaeumPrimaryButton(
                                    text = bText("Play Again · Beat $bestToBeat", "Main Lagi · Kalahkan $bestToBeat"),
                                    onClick = onStartOrReplay,
                                    testTag = "blitz_replay_button"
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                BaeumSecondaryButton(
                                    text = bText("Back to Home", "Kembali ke Beranda"),
                                    onClick = {
                                        onAbandon()
                                        onBack()
                                    }
                                )
                            }
                        }
                    }

                    if (state.missedWords.isNotEmpty()) {
                        Text(
                            text = bText("Lock in missed words (${state.missedWords.size})", "Kunci kata yang terlewat (${state.missedWords.size})"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        state.missedWords.forEach { missed ->
                            SeoulCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = extended.cherryPink.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${missed.hangul}  ·  ${missed.romanization}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = missed.meaning,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = extended.mint
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(primary.copy(alpha = 0.14f))
                                            .tactileClickable(pressedScale = 0.90f) { onSpeakKorean(missed.hangul) },
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
                    }
                } else {
                    val prompt = state.currentPrompt ?: return@Column
                    val animScore by rememberAnimatedInt(state.score, label = "blitz_live_score")
                    val timeFraction = (state.remainingSec.toFloat() / state.totalDurationSec.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val isLowTime = state.remainingSec <= 10
                    val timerColor by animateColorAsState(
                        targetValue = when {
                            isLowTime -> extended.cherryPink
                            state.isFeverMode -> extended.gold
                            else -> extended.mint
                        },
                        animationSpec = tween(if (extended.reducedMotion) 0 else 240),
                        label = "blitz_timer_color"
                    )

                    // Live HUD: Timer | Score | Combo Flame
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(BlitzBadgeShape)
                                .background(timerColor.copy(alpha = 0.16f))
                                .border(1.dp, timerColor.copy(alpha = 0.5f), BlitzBadgeShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${state.remainingSec}s",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = timerColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$animScore",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = bText(
                                    "Best ${maxOf(state.previousBestScore, state.score)}",
                                    "Rekor ${maxOf(state.previousBestScore, state.score)}"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier.defaultMinSize(minWidth = 84.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (state.combo >= 2) {
                                ComboFlamePill(combo = state.combo)
                            } else {
                                Text(
                                    text = "x${state.multiplier}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    ProgressBarSlim(
                        progress = timeFraction,
                        color = timerColor
                    )

                    // Fever Ribbon + Floating Score / Time Bonus Popup
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isFeverMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = null, tint = extended.gold, modifier = Modifier.size(15.dp))
                                Text(
                                    text = bText(
                                        "SEOUL FEVER · x${state.multiplier} POINTS · +2s EVERY 5 HITS",
                                        "SEOUL FEVER · x${state.multiplier} POIN · +2d TIAP 5 BENAR"
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = extended.gold
                                )
                            }
                        }
                        if (state.lastFeedbackSeq > 0 && state.lastAnswerWasCorrect == true) {
                            val popupLabel = buildString {
                                append("+${state.lastPointsDelta}")
                                if (state.lastTimeBonusSec > 0) append("  •  +${state.lastTimeBonusSec}s!")
                            }
                            FloatingXpBadge(
                                label = popupLabel,
                                eventSeq = state.lastFeedbackSeq,
                                accentColor = if (state.lastTimeBonusSec > 0) extended.gold else extended.mint,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }

                    // Rapid-fire Stimulus Card
                    AnimatedContent(
                        targetState = prompt,
                        transitionSpec = {
                            BaeumMotion.directionalContentTransform(
                                forward = true,
                                reducedMotion = extended.reducedMotion,
                                durationMs = 220
                            )
                        },
                        label = "blitz_prompt_transition"
                    ) { activePrompt ->
                        val cardGlow = when {
                            state.isFeverMode -> extended.gold
                            else -> primary
                        }
                        BaeumGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .gentleShake(wrongShakeSeq),
                            style = BaeumGlassStyle.HERO,
                            accentGlowColor = cardGlow,
                            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 26.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (activePrompt.isHangulPrompt) {
                                        bText("TAP THE MEANING", "KETUK ARTINYA")
                                    } else {
                                        bText("TAP THE HANGUL", "KETUK HANGUL-NYA")
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.6.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = cardGlow
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = activePrompt.stimulusText,
                                    style = if (activePrompt.isHangulPrompt) {
                                        MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                                    } else {
                                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                    },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                if (activePrompt.isHangulPrompt && activePrompt.romanization.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activePrompt.romanization,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(cardGlow.copy(alpha = 0.16f))
                                        .border(1.dp, cardGlow.copy(alpha = 0.42f), CircleShape)
                                        .tactileClickable(pressedScale = 0.90f) {
                                            onSpeakKorean(activePrompt.hangul)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = bText("Hear Korean pronunciation", "Dengar pelafalan Korea"),
                                        tint = cardGlow,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4 High-Tactile Rapid Options (>= 58dp touch height)
                    val optFillBrush = remember(extended.glassFillTop, extended.glassFillBottom) {
                        Brush.verticalGradient(listOf(extended.glassFillTop, extended.glassFillBottom))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        prompt.options.forEachIndexed { idx, opt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 58.dp)
                                    .clip(BlitzOptionShape)
                                    .background(optFillBrush)
                                    .border(1.dp, extended.glassHighlightBorder, BlitzOptionShape)
                                    .tactileClickable(pressedScale = 0.96f) {
                                        onSelectOption(opt)
                                    }
                                    .padding(horizontal = 18.dp, vertical = 14.dp)
                                    .testTag("blitz_option_$idx"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primary.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = primary
                                        )
                                    }
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state?.isFinished == true && (state.isNewRecord || state.score >= 150)) {
            LuxuryConfettiOverlay(triggerKey = state.score.coerceAtLeast(1))
        }
    }
}
