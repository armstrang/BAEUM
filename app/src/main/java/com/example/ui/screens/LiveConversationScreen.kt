package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.seed.KoreanSpeakingIndonesianCatalog
import com.example.data.seed.SpeakingDifficulty
import com.example.data.seed.localizedHint
import com.example.data.seed.localizedSpeakerRole
import com.example.data.seed.localizedTitle
import com.example.data.seed.localizedTranslation
import com.example.domain.speaking.ConversationPhase
import com.example.domain.speaking.LiveConversationState
import com.example.ui.components.AiCoachFeedbackCard
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.RealWaveformBars
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian
import com.example.ui.theme.localizedLabel

// SCREEN 18 / 24 — Live Conversation (Record, preview, edit, evaluate 0–10, advance uniquely)
@Composable
fun LiveConversationScreen(
    state: LiveConversationState?,
    isPlayingRecording: Boolean,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onRedoRecording: () -> Unit,
    onEnterManualMode: () -> Unit,
    onDraftChange: (String) -> Unit,
    onPlayRecording: () -> Unit,
    onToggleHint: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onCompletedNavigate: () -> Unit
) {
    if (state == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Conversation", "Percakapan"), onBack = onBack)
            Text(bText("No active conversation scenario.", "Tidak ada skenario percakapan yang aktif."), modifier = Modifier.padding(24.dp))
        }
        return
    }

    LaunchedEffect(state.phase) {
        if (state.phase == ConversationPhase.COMPLETED) {
            onCompletedNavigate()
        }
    }

    val context = LocalContext.current
    var micPermissionDenied by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            onStartRecording()
        } else {
            micPermissionDenied = true
            onEnterManualMode()
        }
    }

    fun requestMicAndRecord() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            micPermissionDenied = false
            onStartRecording()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BackHandler(enabled = state.evaluatedTurns.isNotEmpty() && !state.isComplete) {
        showExitConfirm = true
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(bText("End conversation early?", "Akhiri percakapan lebih awal?")) },
            text = { Text(bText("Leaving now will discard this unfinished speaking session.", "Keluar sekarang akan membatalkan sesi berbicara yang belum selesai.")) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    onBack()
                }) { Text(bText("Leave", "Keluar")) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text(bText("Stay", "Lanjut")) }
            }
        )
    }

    val extended = LocalBaeumColors.current
    val isId = isIndonesian()
    val currentPrompt = state.currentPrompt
    val turnNum = (state.currentTurnIndex + 1).coerceAtMost(state.totalTurns)

    Column(modifier = Modifier.fillMaxSize()) {
        BaeumTopBar(
            title = state.scenario.localizedTitle(isId),
            subtitle = bText(
                "${state.scenario.difficulty.localizedLabel()} • Turn $turnNum of ${state.totalTurns}",
                "${state.scenario.difficulty.localizedLabel()} • Giliran $turnNum dari ${state.totalTurns}"
            ),
            onBack = {
                if (state.evaluatedTurns.isNotEmpty() && !state.isComplete) showExitConfirm = true else onBack()
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 660.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressBarSlim(progress = turnNum.toFloat() / state.totalTurns.coerceAtLeast(1))

            // Prior evaluated turns
            state.evaluatedTurns.forEach { turn ->
                // Partner bubble
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    contentPadding = PaddingValues(14.dp),
                    onClick = { onSpeakKorean(turn.promptKo) }
                ) {
                    Text(
                        text = KoreanSpeakingIndonesianCatalog.localizedSpeakerRole(turn.speakerRole, isId),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(turn.promptKo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }

                // User evaluated bubble
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    SeoulCard(
                        modifier = Modifier.fillMaxWidth(0.88f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (turn.wasManualOrEdited) bText("YOU (Edited/Typed)", "ANDA (Diedit/Diketik)") else bText("YOU", "ANDA"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = bText("Response score: ${turn.scoreOutOf10} / 10", "Skor respons: ${turn.scoreOutOf10} / 10"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (turn.scoreOutOf10 >= 7) extended.mint else extended.gold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(turn.userTranscript, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(turn.feedbackNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Latest AI Coach & Acoustic Cadence Insight Card
            state.latestAiCoachFeedback?.let { coachFeedback ->
                AiCoachFeedbackCard(
                    feedback = coachFeedback,
                    onSpeakKorean = onSpeakKorean
                )
            }

            // Current Active Partner Prompt Bubble
            if (currentPrompt != null) {
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    onClick = { onSpeakKorean(currentPrompt.promptKo) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentPrompt.localizedSpeakerRole(isId),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onSpeakKorean(currentPrompt.promptKo) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = stringResource(R.string.cd_audio_play),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = currentPrompt.promptKo,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Difficulty-aware assistance rules (Prompt Section 7)
                    val localizedTranslation = currentPrompt.localizedTranslation(isId)
                    val localizedHint = currentPrompt.localizedHint(isId)
                    when (state.scenario.difficulty) {
                        SpeakingDifficulty.BEGINNER -> {
                            if (currentPrompt.romanization.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentPrompt.romanization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(localizedTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(bText("Tip: $localizedHint", "Tips: $localizedHint"), style = MaterialTheme.typography.labelSmall, color = extended.mint)
                        }
                        SpeakingDifficulty.INTERMEDIATE -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            if (state.showOptionalHint) {
                                Text(localizedTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(bText("Hint: $localizedHint", "Petunjuk: $localizedHint"), style = MaterialTheme.typography.labelSmall, color = extended.mint)
                            }
                            TextButton(onClick = onToggleHint, contentPadding = PaddingValues(0.dp)) {
                                Text(
                                    if (state.showOptionalHint) bText("Hide assistance", "Sembunyikan bantuan")
                                    else bText("Show optional translation & hint", "Tampilkan terjemahan & petunjuk"),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        SpeakingDifficulty.ADVANCED -> {
                            // Korean-first experience without automatic translation
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bText(
                                    "고급 모드 • Target register: ${currentPrompt.expectedSpeechLevel}",
                                    "고급 모드 • Target kesopanan: ${currentPrompt.expectedSpeechLevel}"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = extended.gold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recording / Preview / Transcript Dock (Blueprint Screen 18)
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                val durText = String.format("%02d:%02d", state.lastRecordingSeconds / 60, state.lastRecordingSeconds % 60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (state.phase) {
                            ConversationPhase.RECORDING -> bText("Listening (ko-KR)...", "Mendengarkan (ko-KR)...")
                            ConversationPhase.PREVIEW_TRANSCRIPT -> bText("Recording preview", "Pratinjau rekaman")
                            else -> bText("Your Korean response", "Respons Korea Anda")
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = durText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                RealWaveformBars(
                    amplitudes = state.lastWaveform,
                    isActive = state.phase == ConversationPhase.RECORDING
                )

                if (micPermissionDenied) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bText(
                            "Microphone permission not granted. You can type your Korean response below — speech recognition errors never count against your score.",
                            "Izin mikrofon belum diberikan. Anda dapat mengetik jawaban Korea di bawah — kendala pengenalan suara tidak mengurangi skor Anda."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.gold
                    )
                }

                if (state.recognitionErrorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = extended.gold.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extended.gold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.recognitionErrorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.gold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (state.phase) {
                    ConversationPhase.AWAITING_INPUT -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { requestMicAndRecord() },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(54.dp)
                                    .testTag("conv_record_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(bText("Speak (ko-KR)", "Bicara (ko-KR)"))
                            }
                            BaeumSecondaryButton(
                                text = bText("Type Korean", "Ketik Korea"),
                                onClick = onEnterManualMode,
                                modifier = Modifier.weight(1f),
                                testTag = "conv_type_button"
                            )
                        }
                    }

                    ConversationPhase.RECORDING -> {
                        if (state.livePartialTranscript.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .testTag("conv_live_partial_transcript")
                            ) {
                                Text(
                                    text = state.livePartialTranscript,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                        Button(
                            onClick = onStopRecording,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("conv_stop_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extended.cherryPink,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(bText("Stop & preview transcript", "Berhenti & pratinjau transkrip"))
                        }
                    }

                    ConversationPhase.PREVIEW_TRANSCRIPT, ConversationPhase.EVALUATING -> {
                        Text(
                            text = bText("RECOGNIZED / DRAFT TEXT", "TEKS TERDETEKSI / DRAF"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.draftTranscript,
                            onValueChange = onDraftChange,
                            placeholder = {
                                Text(
                                    bText(
                                        "Enter or refine your Korean reply (e.g., 큰 사이즈로 주세요.)",
                                        "Ketik atau rapikan balasan Korea Anda (mis. 큰 사이즈로 주세요.)"
                                    )
                                )
                            },
                            trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.elevatedSurface,
                                unfocusedContainerColor = extended.elevatedSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("conv_transcript_input")
                        )

                        if (state.lastRecordingPath != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onPlayRecording) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isPlayingRecording) bText("Playing your voice...", "Memutar suara Anda...")
                                    else bText("Replay my recording", "Putar ulang rekaman")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BaeumSecondaryButton(
                                text = bText("Redo", "Ulangi"),
                                onClick = onRedoRecording,
                                modifier = Modifier.weight(0.8f),
                                testTag = "conv_redo_button"
                            )
                            BaeumPrimaryButton(
                                text = bText("Send answer", "Kirim jawaban"),
                                onClick = onSubmitAnswer,
                                modifier = Modifier.weight(1.2f),
                                testTag = "conv_send_button"
                            )
                        }
                    }

                    ConversationPhase.COMPLETED -> Unit
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
