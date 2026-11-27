package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.audio.SpeechRecognitionState
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.localizedExampleMeaning
import com.example.domain.hangul.localizedExplanation
import com.example.domain.hangul.localizedTitle
import com.example.domain.speaking.KoreanPronunciationEvaluator
import com.example.domain.speaking.PronunciationAttemptResult
import com.example.domain.speaking.PronunciationDrillCategory
import com.example.domain.speaking.PronunciationDrillItem
import com.example.domain.speaking.SyllableMatchLevel
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.RealWaveformBars
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian
import com.example.viewmodel.PronunciationPracticeState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationLabScreen(
    state: PronunciationPracticeState,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onSelectCategory: (PronunciationDrillCategory?) -> Unit,
    onSelectDrill: (PronunciationDrillItem) -> Unit,
    onNextDrill: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onCancelListening: () -> Unit,
    onRetryDrill: () -> Unit
) {
    val context = LocalContext.current
    val extended = LocalBaeumColors.current
    val isId = isIndonesian()
    val primary = MaterialTheme.colorScheme.primary

    var micPermissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            onStartListening()
        } else {
            micPermissionDenied = true
        }
    }

    fun requestMicAndStart() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            micPermissionDenied = false
            onStartListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose { onCancelListening() }
    }

    val activeDrill = state.activeDrill
    val recState = state.recognitionState
    val isListening = recState is SpeechRecognitionState.Listening || recState is SpeechRecognitionState.Preparing
    val isProcessing = recState is SpeechRecognitionState.Processing
    val activePool = remember(state.drills, state.selectedCategory) {
        if (state.selectedCategory == null) state.drills
        else state.drills.filter { it.category == state.selectedCategory }
    }
    val passedCount = state.passedDrillIds.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Pronunciation Lab", "Lab Pelafalan"),
            subtitle = bText("Korean Speech Recognition Practice", "Latihan Pengenalan Suara Bahasa Korea"),
            onBack = {
                onCancelListening()
                onBack()
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category filter chips
            StaggeredEntrance(index = 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text(bText("All (${state.drills.size})", "Semua (${state.drills.size})")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primary.copy(alpha = 0.2f),
                            selectedLabelColor = primary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("pron_filter_all")
                    )
                    PronunciationDrillCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = state.selectedCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(if (isId) cat.labelId else cat.labelEn) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primary.copy(alpha = 0.2f),
                                selectedLabelColor = primary
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("pron_filter_${cat.name.lowercase()}")
                        )
                    }
                }
            }

            // Interactive Speaking Practice Studio Card
            StaggeredEntrance(index = 1) {
                BaeumGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = BaeumGlassStyle.HERO,
                    accentGlowColor = if (isListening) extended.cherryPink else primary,
                    contentPadding = PaddingValues(22.dp)
                ) {
                    // Header status pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = primary.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, primary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (isId) activeDrill.focusLabelId else activeDrill.focusLabelEn,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Text(
                            text = bText("$passedCount/${state.drills.size} mastered", "$passedCount/${state.drills.size} dikuasai"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = extended.mint
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Hangul + Pronunciation Display
                    AnimatedContent(
                        targetState = activeDrill,
                        transitionSpec = {
                            if (extended.reducedMotion) {
                                fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                            } else {
                                fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                            }
                        },
                        label = "active_pron_drill"
                    ) { drill ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = drill.targetWrittenKo,
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("pron_target_hangul")
                            )
                            if (drill.hasDistinctPronunciation) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bText("Pronounced: [${drill.targetPronouncedKo}]", "Dilafalkan: [${drill.targetPronouncedKo}]"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = extended.mint
                                )
                            }
                            if (drill.romanization.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = drill.romanization,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isId) drill.meaningId else drill.meaningEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = extended.elevatedSurface.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, extended.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isId) drill.coachingTipId else drill.coachingTipEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live SpeechRecognizer Waveform + Partial Transcript Display
                    val waveform = when (recState) {
                        is SpeechRecognitionState.Listening -> recState.amplitudes
                        is SpeechRecognitionState.Processing -> recState.amplitudes
                        is SpeechRecognitionState.Result -> recState.amplitudes
                        is SpeechRecognitionState.Error -> recState.amplitudes
                        else -> emptyList()
                    }
                    val partialText = when (recState) {
                        is SpeechRecognitionState.Listening -> recState.partialTranscript
                        is SpeechRecognitionState.Processing -> recState.lastPartialTranscript
                        else -> ""
                    }

                    AnimatedVisibility(visible = isListening || isProcessing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RealWaveformBars(
                                amplitudes = waveform,
                                isActive = isListening
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    isProcessing -> bText("Analyzing Korean speech...", "Menganalisis ucapan Korea...")
                                    partialText.isNotBlank() -> bText("Hearing: \"$partialText\"", "Terdengar: \"$partialText\"")
                                    recState is SpeechRecognitionState.Listening && recState.isSpeechDetected ->
                                        bText("Speech detected (ko-KR)...", "Suara terdeteksi (ko-KR)...")
                                    else -> bText("Listening (ko-KR) — say \"${activeDrill.targetWrittenKo}\"...", "Mendengarkan (ko-KR) — ucapkan \"${activeDrill.targetWrittenKo}\"...")
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (partialText.isNotBlank()) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("pron_live_transcript")
                            )
                        }
                    }

                    // Action Buttons: Listen (TTS) + Speak / Stop (SpeechRecognizer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BaeumSecondaryButton(
                            text = bText("Listen", "Dengar"),
                            onClick = { onSpeakKorean(activeDrill.targetWrittenKo) },
                            modifier = Modifier.weight(0.95f),
                            testTag = "pron_listen_button"
                        )

                        if (isListening || isProcessing) {
                            val pulseScaleState = if (extended.reducedMotion) {
                                remember { mutableStateOf(1f) }
                            } else {
                                val inf = rememberInfiniteTransition(label = "mic_pulse")
                                inf.animateFloat(
                                    initialValue = 0.98f,
                                    targetValue = 1.03f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(650, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulse_scale"
                                )
                            }
                            Button(
                                onClick = onStopListening,
                                modifier = Modifier
                                    .weight(1.25f)
                                    .height(54.dp)
                                    .graphicsLayer {
                                        val s = pulseScaleState.value
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .testTag("pron_stop_button"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = extended.cherryPink,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = bText("Stop listening", "Berhenti mendengarkan"), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(bText("Stop", "Berhenti"), fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = { requestMicAndStart() },
                                enabled = state.isSpeechServiceAvailable,
                                modifier = Modifier
                                    .weight(1.25f)
                                    .height(54.dp)
                                    .testTag("pron_speak_button"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = bText("Speak Korean", "Ucapkan bahasa Korea"), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(bText("Speak (ko-KR)", "Bicara (ko-KR)"), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Service Availability / Permission Notices
                    if (micPermissionDenied) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PronunciationNoticeBanner(
                            text = bText(
                                "Microphone permission is required for speech recognition. Allow microphone access in Android Settings or tap Speak again.",
                                "Izin mikrofon diperlukan untuk pengenalan suara. Izinkan akses mikrofon di Pengaturan Android atau ketuk Bicara lagi."
                            ),
                            accent = extended.gold
                        )
                    } else if (!state.isSpeechServiceAvailable) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PronunciationNoticeBanner(
                            text = bText(
                                "Android Speech Recognition service is not installed on this device. You can still listen to native Korean TTS models below.",
                                "Layanan Pengenalan Suara Android tidak tersedia di perangkat ini. Anda tetap dapat mendengarkan audio Korea di bawah."
                            ),
                            accent = extended.gold
                        )
                    } else if (recState is SpeechRecognitionState.Error) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PronunciationNoticeBanner(
                            text = recState.localizedMessage(isId),
                            accent = extended.gold
                        )
                    }

                    // Engine badge caption
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isOnDeviceAvailable) {
                                bText("Android SpeechRecognizer (ko-KR) • On-device ready", "Android SpeechRecognizer (ko-KR) • Siap di perangkat")
                            } else {
                                bText("Android SpeechRecognizer (ko-KR) • Jamo transcript match", "Android SpeechRecognizer (ko-KR) • Pencocokan Jamo")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Evaluation Result Breakdown Card
            val attempt = state.lastAttempt
            AnimatedVisibility(visible = attempt != null) {
                if (attempt != null) {
                    PronunciationResultCard(
                        attempt = attempt,
                        isIndonesian = isId,
                        onRetry = {
                            onRetryDrill()
                            requestMicAndStart()
                        },
                        onNext = onNextDrill
                    )
                }
            }

            // Interactive Drill Deck Section
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = bText("Tap Any Drill to Listen & Practice", "Ketuk Latihan untuk Mendengar & Berbicara"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            val contrastDrills = remember(activePool) {
                activePool.filter { it.category == PronunciationDrillCategory.MINIMAL_CONTRAST }
            }
            val otherDrills = remember(activePool) {
                activePool.filter { it.category != PronunciationDrillCategory.MINIMAL_CONTRAST }
            }

            // Contrast Triplet Quick Cards (when showing All or Minimal Contrast)
            if (state.selectedCategory == null || state.selectedCategory == PronunciationDrillCategory.MINIMAL_CONTRAST) {
                val grouped = remember(contrastDrills) {
                    contrastDrills.groupBy { it.contrastGroup.joinToString(" • ") }
                }
                grouped.forEach { (groupTitle, items) ->
                    SeoulCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = groupTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = primary
                            )
                            Text(
                                text = bText("Tap a word", "Pilih kata"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.forEach { item ->
                                val isSelected = item.id == activeDrill.id
                                val isPassed = item.id in state.passedDrillIds
                                Surface(
                                    onClick = { onSelectDrill(item) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = when {
                                        isSelected -> primary.copy(alpha = 0.22f)
                                        isPassed -> extended.mint.copy(alpha = 0.14f)
                                        else -> extended.elevatedSurface
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = when {
                                            isSelected -> primary
                                            isPassed -> extended.mint.copy(alpha = 0.55f)
                                            else -> extended.cardBorder
                                        }
                                    ),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("pron_drill_${item.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.targetWrittenKo,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isId) item.meaningId else item.meaningEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isPassed) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = bText("Passed", "Lulus"),
                                                tint = extended.mint,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sound Change & Phrase Drills
            otherDrills.forEach { drill ->
                val isSelected = drill.id == activeDrill.id
                val isPassed = drill.id in state.passedDrillIds
                SeoulCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pron_drill_${drill.id}"),
                    borderColor = when {
                        isSelected -> primary
                        isPassed -> extended.mint.copy(alpha = 0.55f)
                        else -> Color.Unspecified
                    },
                    onClick = { onSelectDrill(drill) }
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
                                Text(
                                    text = if (isId) drill.focusLabelId else drill.focusLabelEn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = extended.mint
                                )
                                if (isPassed) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = bText("Mastered", "Dikuasai"),
                                        tint = extended.mint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (drill.hasDistinctPronunciation) {
                                    "${drill.targetWrittenKo} → [${drill.targetPronouncedKo}]"
                                } else {
                                    drill.targetWrittenKo
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${drill.romanization} • ${if (isId) drill.meaningId else drill.meaningEn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = bText("Listen and select", "Dengar dan pilih"),
                            tint = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Reference: Essential Korean Sound Change Rules (tap to load directly into speaking practice)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bText("Korean Sound Change Reference", "Referensi Perubahan Bunyi Korea"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            HangulUnicodeEngine.soundChangeRules.forEach { rule ->
                val primaryWritten = rule.exampleWritten.substringBefore("/").trim()
                val primaryPronounced = rule.examplePronounced
                    .replace("[", "")
                    .replace("]", "")
                    .substringBefore("/")
                    .trim()
                val localizedTitle = rule.localizedTitle(isId)
                val localizedMeaning = rule.localizedExampleMeaning(isId)
                val localizedExplanation = rule.localizedExplanation(isId)
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val existing = state.drills.firstOrNull { it.targetWrittenKo == primaryWritten }
                        if (existing != null) {
                            onSelectDrill(existing)
                        } else {
                            onSelectDrill(
                                KoreanPronunciationEvaluator.createAdHocDrill(
                                    id = "rule_${rule.id}",
                                    writtenKo = primaryWritten,
                                    pronouncedKo = primaryPronounced,
                                    romanization = rule.exampleRomanization.substringBefore("/").trim(),
                                    meaningEn = rule.exampleMeaning.substringBefore("/").trim(),
                                    meaningId = rule.localizedExampleMeaning(true).substringBefore("/").trim(),
                                    tipEn = rule.explanation,
                                    tipId = rule.localizedExplanation(true)
                                )
                            )
                        }
                    }
                ) {
                    Text(
                        text = "$localizedTitle (${rule.titleKo})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.mint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${rule.exampleWritten} → ${rule.examplePronounced} ($localizedMeaning)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = localizedExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PronunciationResultCard(
    attempt: PronunciationAttemptResult,
    isIndonesian: Boolean,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val accent = if (attempt.passed) extended.mint else extended.gold

    BaeumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pron_result_card"),
        style = BaeumGlassStyle.STANDARD,
        accentGlowColor = accent,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (attempt.passed) {
                        bText("Great pronunciation!", "Pelafalan sangat bagus!")
                    } else {
                        bText("Keep practicing", "Terus berlatih")
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bText("Recognized: \"${attempt.bestRecognizedText}\"", "Terdeteksi: \"${attempt.bestRecognizedText}\""),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("pron_recognized_text")
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.5.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${attempt.matchScorePercent}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    modifier = Modifier.testTag("pron_score_percent")
                )
            }
        }

        if (attempt.syllableBreakdown.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = bText("SYLLABLE-BY-SYLLABLE MATCH", "KECOCOKAN PER SUKU KATA"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                attempt.syllableBreakdown.forEach { item ->
                    val chipColor = when (item.matchLevel) {
                        SyllableMatchLevel.EXACT -> extended.mint
                        SyllableMatchLevel.CLOSE -> extended.gold
                        SyllableMatchLevel.MISMATCH -> extended.cherryPink
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = chipColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.targetSyllable.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = chipColor
                            )
                            Text(
                                text = item.recognizedSyllable?.toString() ?: "—",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isIndonesian) attempt.feedbackId else attempt.feedbackEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isIndonesian) attempt.phoneticHintId else attempt.phoneticHintEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BaeumSecondaryButton(
                text = bText("Try again", "Coba lagi"),
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                testTag = "pron_retry_button"
            )
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("pron_next_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (attempt.passed) extended.mint else primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(bText("Next drill", "Berikutnya"), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PronunciationNoticeBanner(
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
        }
    }
}
