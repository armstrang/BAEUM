package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.RomanizationMode
import com.example.data.local.VocabEntity
import com.example.data.repository.BaeumRepository
import com.example.data.repository.DayForecastPoint
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduler
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
import com.example.ui.components.SevenDayForecastChart
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.rememberAnimatedInt
import com.example.ui.theme.tactileClickable
import com.example.viewmodel.FlashcardFsrsUiState
import kotlinx.coroutines.launch
import kotlin.math.abs

// SCREEN 11 / 24 — Smart Flashcard (Recall + FSRS-4.5/5 ViewModel Scheduler)
@Composable
fun SmartFlashcardScreen(
    session: FlashcardFsrsUiState,
    romanizationMode: RomanizationMode,
    translationLang: String,
    onBack: () -> Unit,
    onReveal: () -> Unit,
    onRate: (FsrsRating) -> Unit,
    onUndo: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onToggleRomanization: () -> Unit,
    onStartNewSession: () -> Unit
) {
    var showExitConfirm by remember { mutableStateOf(false) }
    val hasActiveProgress = !session.isCompleted && session.currentCard != null && session.reviewedCount > 0

    BackHandler(enabled = hasActiveProgress) {
        showExitConfirm = true
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(bText("Leave review session?", "Keluar dari sesi ulasan?")) },
            text = {
                Text(
                    bText(
                        "Your ${session.reviewedCount} completed FSRS rating(s) are already saved to your local database.",
                        "${session.reviewedCount} penilaian FSRS Anda yang selesai sudah tersimpan di perangkat."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    onBack()
                }) { Text(bText("Leave", "Keluar")) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text(bText("Continue studying", "Lanjut belajar")) }
            }
        )
    }

    val extended = LocalBaeumColors.current
    val currentCard = session.currentCard
    val showRomNow = when (romanizationMode) {
        RomanizationMode.ALWAYS -> true
        RomanizationMode.ON_BACK_ONLY -> session.isRevealed
        RomanizationMode.HIDDEN -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Smart Flashcard", "Flashcard Cerdas"),
            subtitle = session.title,
            onBack = { if (hasActiveProgress) showExitConfirm = true else onBack() },
            trailing = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (session.canUndo && !session.isSubmitting) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(extended.gold.copy(alpha = 0.16f))
                                .border(1.dp, extended.gold.copy(alpha = 0.45f), CircleShape)
                                .tactileClickable(pressedScale = 0.90f, onClick = onUndo)
                                .testTag("flashcard_undo_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = bText("Undo last rating", "Batalkan penilaian terakhir"),
                                tint = extended.gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    val romOn = romanizationMode == RomanizationMode.ALWAYS
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (romOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                else extended.glassFillTop
                            )
                            .border(
                                1.dp,
                                if (romOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                else extended.glassHighlightBorder,
                                CircleShape
                            )
                            .tactileClickable(pressedScale = 0.90f, onClick = onToggleRomanization)
                            .testTag("flashcard_quick_rom_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (romOn) "Aa" else "가",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (romOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            if (session.isCompleted || currentCard == null) {
                val eval = session.evaluation
                val recallPct = eval?.recallRatePercent ?: 100
                val animatedRecall by rememberAnimatedInt(recallPct, label = "flashcard_recall_pct")
                Spacer(modifier = Modifier.height(16.dp))
                StaggeredEntrance(index = 0) {
                    BaeumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        style = BaeumGlassStyle.HERO,
                        accentGlowColor = if (recallPct >= 75) extended.mint else extended.gold,
                        contentPadding = PaddingValues(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (eval != null && eval.totalReviews > 0) {
                                CircularGoalRing(
                                    progress = recallPct / 100f,
                                    centerLabel = "$animatedRecall%",
                                    centerSubLabel = bText("Grade ${eval.grade.label}", "Nilai ${eval.grade.label}"),
                                    size = 110.dp,
                                    strokeWidth = 9.dp,
                                    accentColor = if (recallPct >= 75) extended.mint else extended.gold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Text(
                                text = if (eval != null) bText("Session Complete", "Sesi Selesai")
                                else if (session.reviewedCount > 0) "잘했어요!" else bText("All caught up!", "Semua sudah beres!"),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (recallPct >= 75) extended.mint else extended.gold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (session.sessionXpEarned > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(extended.gold.copy(alpha = 0.18f))
                                            .border(1.dp, extended.gold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+${session.sessionXpEarned} XP",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = extended.gold
                                        )
                                    }
                                    if (session.maxSessionCombo >= 2) {
                                        ComboFlamePill(combo = session.maxSessionCombo)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = eval?.grade?.summary ?: if (session.reviewedCount > 0) {
                                    bText(
                                        "Reviewed ${session.reviewedCount} card(s) with FSRS scheduling.",
                                        "Telah mengulas ${session.reviewedCount} kartu dengan penjadwalan FSRS."
                                    )
                                } else {
                                    bText(
                                        "No scheduled cards are due right now. Start a fresh study deck anytime.",
                                        "Tidak ada kartu yang jatuh tempo saat ini. Mulai dek belajar baru kapan saja."
                                    )
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (eval != null && eval.totalReviews > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(extended.elevatedSurface.copy(alpha = 0.65f))
                                        .border(1.dp, extended.glassHighlightBorder, RoundedCornerShape(18.dp))
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(bText("Again ${eval.againCount}", "Lagi ${eval.againCount}"), style = MaterialTheme.typography.labelMedium, color = extended.cherryPink)
                                    Text(bText("Hard ${eval.hardCount}", "Sulit ${eval.hardCount}"), style = MaterialTheme.typography.labelMedium, color = extended.gold)
                                    Text(bText("Good ${eval.goodCount}", "Bagus ${eval.goodCount}"), style = MaterialTheme.typography.labelMedium, color = extended.mint)
                                    Text(bText("Easy ${eval.easyCount}", "Mudah ${eval.easyCount}"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val gainStr = String.format(java.util.Locale.US, "%+.1fd", eval.avgStabilityGainDays)
                                Text(
                                    text = bText(
                                        "Stability delta: $gainStr • ${eval.avgSecondsPerCard}s/card • Newly mastered: ${eval.newlyMasteredCount}",
                                        "Perubahan stabilitas: $gainStr • ${eval.avgSecondsPerCard}d/kartu • Baru dikuasai: ${eval.newlyMasteredCount}"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = eval.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (eval.leechWarnings.isNotEmpty()) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(22.dp))
                            BaeumPrimaryButton(text = bText("Keep Flow Going (Next Set)", "Lanjutkan Alur (Set Berikutnya)"), onClick = onStartNewSession, testTag = "flashcard_study_more")
                            Spacer(modifier = Modifier.height(10.dp))
                            BaeumSecondaryButton(text = bText("Back to Home", "Kembali ke Beranda"), onClick = onBack)
                        }
                    }
                }
            } else {
                val totalCards = session.cards.size.coerceAtLeast(1)
                val remainingAhead = (totalCards - 1 - session.currentIndex).coerceAtLeast(0)
                val haptics = LocalHapticFeedback.current
                val density = LocalDensity.current
                val swipeThresholdPx = with(density) { 118.dp.toPx() }
                val dragOffsetX = remember { Animatable(0f) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(session.currentIndex, currentCard.id) {
                    dragOffsetX.snapTo(0f)
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(session.currentIndex + 1).coerceAtMost(totalCards)} / $totalCards",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (session.sessionCombo >= 2) {
                            ComboFlamePill(combo = session.sessionCombo)
                        }
                        Text(
                            text = if (session.sessionXpEarned > 0) {
                                "+${session.sessionXpEarned} XP"
                            } else {
                                bText("${session.reviewedCount} reviewed", "${session.reviewedCount} diulas")
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (session.sessionXpEarned > 0) extended.gold else MaterialTheme.colorScheme.primary
                        )
                    }
                    if (session.lastXpAwarded > 0 && session.xpEventSeq > 0) {
                        FloatingXpBadge(
                            label = "+${session.lastXpAwarded} XP",
                            eventSeq = session.xpEventSeq,
                            accentColor = extended.gold,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProgressBarSlim(progress = (session.currentIndex + 1).toFloat() / totalCards)

                // Live FSRS Card Telemetry Ribbon computed by FlashcardFsrsViewModel
                val telemetry = session.currentCardTelemetry
                if (telemetry != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val stageColor = when (telemetry.stage) {
                        CardMemoryStage.NEW -> MaterialTheme.colorScheme.primary
                        CardMemoryStage.LEARNING -> extended.gold
                        CardMemoryStage.REVIEW -> extended.mint
                        CardMemoryStage.MASTERED -> extended.mint
                    }
                    val stageLabel = when (telemetry.stage) {
                        CardMemoryStage.NEW -> bText("NEW", "BARU")
                        CardMemoryStage.LEARNING -> bText("LEARNING", "BELAJAR")
                        CardMemoryStage.REVIEW -> bText("REVIEW", "ULASAN")
                        CardMemoryStage.MASTERED -> bText("MASTERED", "DIKUASAI")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flashcard_fsrs_telemetry"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(stageColor.copy(alpha = 0.16f))
                                    .border(1.dp, stageColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = stageLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = stageColor
                                )
                            }
                            if (telemetry.isLeech) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(extended.cherryPink.copy(alpha = 0.16f))
                                        .border(1.dp, extended.cherryPink.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = bText("HARD CARD", "KARTU SULIT"),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = extended.cherryPink
                                    )
                                }
                            }
                        }
                        val metricText = if (telemetry.reps > 0) {
                            val sText = String.format(java.util.Locale.US, "%.1fd", telemetry.stabilityDays)
                            val dText = String.format(java.util.Locale.US, "%.1f", telemetry.difficulty)
                            val rText = telemetry.retrievabilityPercent?.let { " • R $it%" } ?: ""
                            "S $sText • D $dText$rText"
                        } else {
                            bText(
                                "Target retention ${(session.desiredRetention * 100).toInt()}%",
                                "Target retensi ${(session.desiredRetention * 100).toInt()}%"
                            )
                        }
                        Text(
                            text = metricText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Layered Physical Card Deck + Physics Swipe-to-Rate + 3D Perspective Pulse
                val cardFlipTilt by animateFloatAsState(
                    targetValue = if (session.isRevealed && !extended.reducedMotion) 0f else if (!extended.reducedMotion) 2.2f else 0f,
                    animationSpec = BaeumMotion.luxurySpring(),
                    label = "card_tilt"
                )
                val dragFraction = (dragOffsetX.value / swipeThresholdPx).coerceIn(-1f, 1f)
                val absDrag = abs(dragFraction)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (remainingAhead > 0) 14.dp else 0.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Depth Card #2 peeking behind
                    if (remainingAhead >= 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f + 0.04f * absDrag)
                                .height(220.dp)
                                .align(Alignment.BottomCenter)
                                .graphicsLayer {
                                    translationY = (20.dp.toPx() * (1f - 0.35f * absDrag))
                                    alpha = 0.28f + 0.14f * absDrag
                                }
                                .clip(RoundedCornerShape(30.dp))
                                .background(extended.glassFillBottom)
                                .border(1.dp, extended.cardBorder.copy(alpha = 0.45f), RoundedCornerShape(30.dp))
                        )
                    }

                    // Depth Card #1 peeking directly beneath the active card
                    if (remainingAhead >= 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.94f + 0.05f * absDrag)
                                .height(232.dp)
                                .align(Alignment.BottomCenter)
                                .graphicsLayer {
                                    translationY = (11.dp.toPx() * (1f - 0.55f * absDrag))
                                    alpha = 0.52f + 0.25f * absDrag
                                }
                                .clip(RoundedCornerShape(32.dp))
                                .background(extended.glassFillTop)
                                .border(1.dp, extended.glassHighlightBorder.copy(alpha = 0.55f), RoundedCornerShape(32.dp))
                        )
                    }

                    AnimatedContent(
                        targetState = Pair(session.currentIndex, currentCard),
                        transitionSpec = {
                            val ms = if (extended.reducedMotion) 0 else 360
                            (slideInHorizontally(tween(ms, easing = BaeumMotion.SilkEasing)) { (it * 0.22f).toInt() } +
                                fadeIn(tween(ms, easing = BaeumMotion.SilkEasing)) +
                                scaleIn(initialScale = 0.93f, animationSpec = tween(ms, easing = BaeumMotion.SilkEasing)))
                                .togetherWith(
                                    slideOutHorizontally(tween((ms * 0.8f).toInt(), easing = BaeumMotion.SilkEasing)) { -(it * 0.24f).toInt() } +
                                        fadeOut(tween((ms * 0.65f).toInt())) +
                                        scaleOut(targetScale = 0.93f, animationSpec = tween((ms * 0.8f).toInt()))
                                )
                        },
                        label = "flashcard_slide"
                    ) { (_, card) ->
                        val swipeBorderColor = when {
                            dragFraction > 0.15f -> extended.mint.copy(alpha = (0.45f + 0.55f * absDrag).coerceAtMost(1f))
                            dragFraction < -0.15f -> extended.cherryPink.copy(alpha = (0.45f + 0.55f * absDrag).coerceAtMost(1f))
                            session.isRevealed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                            else -> Color.Unspecified
                        }
                        val swipeGlowColor = when {
                            dragFraction > 0.15f -> extended.mint
                            dragFraction < -0.15f -> extended.cherryPink
                            session.isRevealed -> extended.mint
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(session.isRevealed, session.isSubmitting, card.id) {
                                    if (!session.isRevealed || session.isSubmitting) return@pointerInput
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            val currentX = dragOffsetX.value
                                            when {
                                                currentX >= swipeThresholdPx -> {
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    onRate(FsrsRating.GOOD)
                                                }
                                                currentX <= -swipeThresholdPx -> {
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    onRate(FsrsRating.AGAIN)
                                                }
                                                else -> {
                                                    coroutineScope.launch {
                                                        dragOffsetX.animateTo(0f, BaeumMotion.liquidSpring())
                                                    }
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                dragOffsetX.animateTo(0f, BaeumMotion.liquidSpring())
                                            }
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            coroutineScope.launch {
                                                dragOffsetX.snapTo((dragOffsetX.value + dragAmount).coerceIn(-swipeThresholdPx * 1.5f, swipeThresholdPx * 1.5f))
                                            }
                                        }
                                    )
                                }
                                .graphicsLayer {
                                    translationX = dragOffsetX.value
                                    rotationZ = if (extended.reducedMotion) 0f else (dragOffsetX.value / 28f).coerceIn(-10f, 10f)
                                    rotationX = cardFlipTilt
                                    cameraDistance = 14f * this.density
                                }
                        ) {
                            BaeumGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(
                                        animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.sizeSpring()
                                    )
                                    .testTag("flashcard_main_surface"),
                                style = BaeumGlassStyle.FLASHCARD,
                                onClick = if (!session.isRevealed) onReveal else null,
                                borderColor = swipeBorderColor,
                                accentGlowColor = swipeGlowColor,
                                contentPadding = PaddingValues(26.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.26f), RoundedCornerShape(14.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "${card.partOfSpeech.uppercase()} • ${card.subCategory}",
                                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f), RoundedCornerShape(16.dp))
                                            .tactileClickable(pressedScale = 0.88f) { onSpeakKorean(card.hangul) }
                                            .testTag("flashcard_audio_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = stringResource(R.string.cd_audio_play),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = card.hangul,
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (showRomNow) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = card.romanization,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                if (!session.isRevealed) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.TouchApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = bText("Tap card to reveal meaning", "Ketuk kartu untuk melihat arti"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = session.isRevealed,
                                    enter = BaeumMotion.silkExpandEnter(extended.reducedMotion),
                                    exit = BaeumMotion.silkCollapseExit(extended.reducedMotion)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.45f)
                                                .height(1.dp)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), Color.Transparent)
                                                    )
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = card.meaningFor(translationLang),
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        if (card.pronunciationNote.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = card.pronunciationNote,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(extended.elevatedSurface.copy(alpha = 0.78f))
                                                .border(1.dp, extended.glassHighlightBorder, RoundedCornerShape(20.dp))
                                                .tactileClickable(pressedScale = 0.98f) { onSpeakKorean(card.exampleKo) }
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = card.exampleKo,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = card.exampleFor(translationLang),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Swipe,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = bText("Swipe left for Again • right for Good", "Geser kiri untuk Lagi • kanan untuk Bagus"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Floating Swipe Stamp Overlay (GOOD / AGAIN)
                            if (absDrag > 0.16f) {
                                val isRight = dragFraction > 0f
                                val stampColor = if (isRight) extended.mint else extended.cherryPink
                                Box(
                                    modifier = Modifier
                                        .align(if (isRight) Alignment.TopStart else Alignment.TopEnd)
                                        .padding(20.dp)
                                        .graphicsLayer {
                                            alpha = ((absDrag - 0.14f) / 0.5f).coerceIn(0f, 1f)
                                            val s = 0.85f + 0.2f * absDrag
                                            scaleX = s
                                            scaleY = s
                                            rotationZ = if (isRight) -8f else 8f
                                        }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(stampColor.copy(alpha = 0.20f))
                                        .border(1.5.dp, stampColor, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isRight) bText("GOOD", "BAGUS") else bText("AGAIN", "LAGI"),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.4.sp
                                        ),
                                        color = stampColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Smooth Morph Between "Show answer" CTA and 4 Precision FSRS Rating Buttons
                AnimatedContent(
                    targetState = session.isRevealed,
                    transitionSpec = {
                        val ms = if (extended.reducedMotion) 0 else 280
                        (fadeIn(tween(ms, easing = BaeumMotion.SilkEasing)) +
                            slideInVertically(tween(ms, easing = BaeumMotion.SilkEasing)) { it / 4 })
                            .togetherWith(
                                fadeOut(tween((ms * 0.65f).toInt())) +
                                    slideOutVertically(tween((ms * 0.65f).toInt())) { -it / 5 }
                            )
                    },
                    label = "flashcard_action_morph"
                ) { revealed ->
                    if (!revealed) {
                        BaeumPrimaryButton(
                            text = bText("Show answer", "Lihat jawaban"),
                            onClick = onReveal,
                            testTag = "flashcard_reveal_button"
                        )
                    } else {
                        // FSRS-4.5/5 live intervals computed inside FlashcardFsrsViewModel
                        val previews = session.ratingPreviews
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FsrsRatingButton(
                                label = bText("Again", "Lagi"),
                                interval = previews[FsrsRating.AGAIN]?.formattedInterval ?: "1m",
                                accent = extended.cherryPink,
                                enabled = !session.isSubmitting,
                                modifier = Modifier.weight(1f),
                                testTag = "fsrs_rate_again",
                                onClick = { onRate(FsrsRating.AGAIN) }
                            )
                            FsrsRatingButton(
                                label = bText("Hard", "Sulit"),
                                interval = previews[FsrsRating.HARD]?.formattedInterval ?: "6m",
                                accent = extended.gold,
                                enabled = !session.isSubmitting,
                                modifier = Modifier.weight(1f),
                                testTag = "fsrs_rate_hard",
                                onClick = { onRate(FsrsRating.HARD) }
                            )
                            FsrsRatingButton(
                                label = bText("Good", "Bagus"),
                                interval = previews[FsrsRating.GOOD]?.formattedInterval ?: "10m",
                                accent = extended.mint,
                                enabled = !session.isSubmitting,
                                modifier = Modifier.weight(1f),
                                testTag = "fsrs_rate_good",
                                onClick = { onRate(FsrsRating.GOOD) }
                            )
                            FsrsRatingButton(
                                label = bText("Easy", "Mudah"),
                                interval = previews[FsrsRating.EASY]?.formattedInterval ?: "4d",
                                accent = MaterialTheme.colorScheme.primary,
                                enabled = !session.isSubmitting,
                                modifier = Modifier.weight(1f),
                                testTag = "fsrs_rate_easy",
                                onClick = { onRate(FsrsRating.EASY) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bText("Show romanization", "Tampilkan romanisasi"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = romanizationMode == RomanizationMode.ALWAYS,
                        onCheckedChange = { onToggleRomanization() }
                    )
                }
            }
        }
    }
    if (session.isCompleted && session.reviewedCount > 0 && (session.evaluation?.recallRatePercent ?: 100) >= 75) {
        LuxuryConfettiOverlay(triggerKey = session.reviewedCount * 13 + session.sessionXpEarned)
    }
    }
}

private val FsrsRatingShape = RoundedCornerShape(22.dp)

@Composable
private fun FsrsRatingButton(
    label: String,
    interval: String,
    accent: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    val bgBrush = remember(accent) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0.06f)))
    }
    Box(
        modifier = modifier
            .height(68.dp)
            .clip(FsrsRatingShape)
            .background(bgBrush)
            .border(1.2.dp, accent.copy(alpha = 0.55f), FsrsRatingShape)
            .tactileClickable(enabled = enabled, pressedScale = 0.93f, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = interval,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// SCREEN 12 / 24 — Review Planner (Spaced repetition forecast & Memory Stages)
@Composable
fun ReviewPlannerScreen(
    allVocab: List<VocabEntity>,
    onBack: () -> Unit,
    onStartDueReview: () -> Unit,
    onStartNewStudy: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val plannerSnapshot = remember(allVocab) { BaeumRepository.computeVocabAggregate(allVocab) }
    val dueToday = plannerSnapshot.dueTodayCount
    val overdue = plannerSnapshot.overdueCount
    val forecast = plannerSnapshot.sevenDayForecast
    var selectedDay by remember(forecast) { mutableStateOf<DayForecastPoint?>(forecast.firstOrNull()) }

    val total = plannerSnapshot.totalCount.coerceAtLeast(1)
    val newCount = plannerSnapshot.newCount
    val learningCount = plannerSnapshot.learningCount
    val reviewCount = plannerSnapshot.reviewCount
    val masteredCount = plannerSnapshot.masteredCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Review Planner", "Perencana Ulasan"),
            subtitle = bText("FSRS-4.5 spaced repetition", "Pengulangan berjarak FSRS-4.5"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SeoulCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$dueToday",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(bText("Due today", "Jatuh tempo hari ini"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SeoulCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$overdue",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (overdue > 0) extended.cherryPink else extended.mint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(bText("Overdue", "Terlambat"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bText("NEXT 7 DAYS", "7 HARI KE DEPAN"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                SevenDayForecastChart(
                    points = forecast,
                    selectedOffset = selectedDay?.dayOffset,
                    onBarClick = { selectedDay = it }
                )
                selectedDay?.let { pt ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = bText(
                            "${pt.shortLabel} (${pt.dateKey}): ${pt.dueCount} card(s) scheduled",
                            "${pt.shortLabel} (${pt.dateKey}): ${pt.dueCount} kartu terjadwal"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = bText("Memory stages", "Tahap memori"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                MemoryStageRow(bText("New", "Baru"), newCount, total, MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                MemoryStageRow(bText("Learning", "Sedang dipelajari"), learningCount, total, extended.gold)
                Spacer(modifier = Modifier.height(12.dp))
                MemoryStageRow(bText("Review", "Ulasan"), reviewCount, total, extended.mint)
                Spacer(modifier = Modifier.height(12.dp))
                MemoryStageRow(bText("Mastered (stability ≥ 21d)", "Dikuasai (stabilitas ≥ 21 hari)"), masteredCount, total, MaterialTheme.colorScheme.primary)
            }

            if (dueToday > 0) {
                BaeumPrimaryButton(
                    text = bText("Start due review ($dueToday)", "Mulai ulasan jatuh tempo ($dueToday)"),
                    onClick = onStartDueReview,
                    testTag = "planner_start_due_button"
                )
            } else {
                SeoulCard(borderColor = extended.mint.copy(alpha = 0.6f)) {
                    Text(bText("All caught up for today", "Semua ulasan hari ini selesai"), style = MaterialTheme.typography.titleMedium, color = extended.mint)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        bText(
                            "No FSRS reviews are overdue. Introduce new Korean words to keep momentum.",
                            "Tidak ada ulasan FSRS yang tertunda. Pelajari kosakata Korea baru untuk menjaga ritme."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BaeumPrimaryButton(text = bText("Study new cards", "Pelajari kartu baru"), onClick = onStartNewStudy)
            }
        }
    }
}

@Composable
private fun MemoryStageRow(label: String, count: Int, total: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("$count", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(6.dp))
        ProgressBarSlim(progress = count.toFloat() / total.coerceAtLeast(1), color = color)
    }
}
