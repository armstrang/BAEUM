package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SpeakingSessionEntity
import com.example.data.seed.KoreanSpeakingSeed
import com.example.data.seed.SpeakingDifficulty
import com.example.ui.components.BaeumBrandHeader
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.localizedDescription
import com.example.ui.theme.localizedLabel
import kotlin.math.PI
import kotlin.math.sin

// SCREEN 16 / 24 — Speaking Hub (Conversation, Pronunciation Lab, Workplace, TOPIK Speaking, History)
@Composable
fun SpeakingHubScreen(
    historyCount: Int,
    onOpenSettings: () -> Unit,
    onStartConversationFlow: () -> Unit,
    onOpenPronunciationLab: () -> Unit,
    onStartWorkplaceSpeaking: () -> Unit,
    onStartTopikSpeaking: () -> Unit,
    onOpenSessionHistory: () -> Unit
) {
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
                    title = bText("Speak Korean", "Bicara Korea"),
                    subtitle = bText("Find your voice in Korean.", "Temukan kefasihan Anda dalam bahasa Korea."),
                    onSettingsClick = onOpenSettings
                )
            }

            // Hero Korean Conversation Card (Blueprint Screen 16)
            StaggeredEntrance(index = 1) {
                BaeumGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = BaeumGlassStyle.HERO,
                    accentGlowColor = primary,
                    contentPadding = PaddingValues(24.dp)
                ) {
                    // Gently breathing silk acoustic crest isolated in its own composable to avoid 60fps parent recomposition
                    SpeakingWaveCrest(
                        primary = primary,
                        mint = extended.mint,
                        reducedMotion = extended.reducedMotion
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = bText("Korean Conversation", "Percakapan Bahasa Korea"),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bText(
                        "Practice 30 realistic Korean scenarios across Beginner, Intermediate & Advanced levels.",
                        "Latih 30 skenario realistis bahasa Korea untuk tingkat Pemula, Menengah & Mahir."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))
                BaeumPrimaryButton(
                    text = bText("Start conversation", "Mulai percakapan"),
                    onClick = onStartConversationFlow,
                    testTag = "speak_start_conversation"
                )
            }
        }

        StaggeredEntrance(index = 2) {
            SpeakingModuleRow(
                title = bText("Pronunciation & Sound Lab", "Lab Pelafalan & Bunyi"),
                subtitle = bText("Tense consonants, aspiration, Batchim & liaison drills", "Latihan konsonan tegang, aspirasi, Batchim & penghubungan bunyi"),
                icon = Icons.Filled.GraphicEq,
                accent = extended.mint,
                onClick = onOpenPronunciationLab,
                testTag = "speak_pronunciation_lab"
            )
        }
        StaggeredEntrance(index = 3) {
            SpeakingModuleRow(
                title = bText("Workplace Speaking", "Bicara di Tempat Kerja"),
                subtitle = bText("EPS-TOPIK & Korean office professional situations", "Situasi profesional EPS-TOPIK & kantor Korea"),
                icon = Icons.Filled.BusinessCenter,
                accent = extended.gold,
                onClick = onStartWorkplaceSpeaking,
                testTag = "speak_workplace"
            )
        }
        StaggeredEntrance(index = 4) {
            SpeakingModuleRow(
                title = bText("TOPIK Speaking", "Berbicara TOPIK"),
                subtitle = bText("Structured speaking assessment practice", "Latihan penilaian berbicara terstruktur"),
                icon = Icons.Filled.RecordVoiceOver,
                accent = primary,
                onClick = onStartTopikSpeaking,
                testTag = "speak_topik"
            )
        }
        StaggeredEntrance(index = 5) {
            SpeakingModuleRow(
                title = bText("Session History", "Riwayat Sesi"),
                subtitle = if (historyCount == 0) {
                    bText("No completed sessions yet", "Belum ada sesi yang selesai")
                } else {
                    bText("Review $historyCount previous assessment(s)", "Tinjau $historyCount penilaian sebelumnya")
                },
                icon = Icons.Filled.History,
                accent = extended.cherryPink,
                onClick = onOpenSessionHistory,
                testTag = "speak_history"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SpeakingWaveCrest(
    primary: Color,
    mint: Color,
    reducedMotion: Boolean
) {
    val wavePhaseState = if (reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "speak_crest")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "crest_phase"
        )
    }
    val crestHeights = androidx.compose.runtime.remember {
        floatArrayOf(0.28f, 0.45f, 0.72f, 0.40f, 0.88f, 0.56f, 0.96f, 0.62f, 0.82f, 0.44f, 0.68f, 0.35f, 0.52f, 0.25f)
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val phase = wavePhaseState.value
        val count = crestHeights.size
        val slot = size.width / (count + 4)
        val startX = slot * 2f
        val barW = slot * 0.44f
        crestHeights.forEachIndexed { i, base ->
            val pulse = if (reducedMotion) 1f else (0.86f + 0.14f * sin(phase + i * 0.55f))
            val h = (size.height * base * pulse).coerceAtLeast(8f)
            val x = startX + i * slot
            val y = (size.height - h) / 2f
            val barBrush = Brush.verticalGradient(
                colors = if (i % 3 == 0) {
                    listOf(mint, primary.copy(alpha = 0.75f))
                } else {
                    listOf(primary, mint.copy(alpha = 0.65f))
                },
                startY = y,
                endY = y + h
            )
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

@Composable
private fun SpeakingModuleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String
) {
    BaeumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        style = BaeumGlassStyle.STANDARD,
        accentGlowColor = accent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f))
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = accent.copy(alpha = 0.85f)
            )
        }
    }
}

// SCREEN 17 / 24 — Difficulty Selection (Automatically selects randomized scenario; no extra Random button!)
@Composable
fun DifficultySelectionScreen(
    onBack: () -> Unit,
    onSelectDifficulty: (SpeakingDifficulty) -> Unit
) {
    val extended = LocalBaeumColors.current
    val cards = listOf(
        Triple(SpeakingDifficulty.BEGINNER, extended.mint, KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.BEGINNER).size),
        Triple(SpeakingDifficulty.INTERMEDIATE, MaterialTheme.colorScheme.primary, KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.INTERMEDIATE).size),
        Triple(SpeakingDifficulty.ADVANCED, extended.gold, KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.ADVANCED).size)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Select difficulty", "Pilih tingkat kesulitan"),
            subtitle = bText("Scenario selected automatically", "Skenario dipilih otomatis"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            cards.forEach { (diff, accent, count) ->
                SeoulCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("difficulty_${diff.name}"),
                    onClick = { onSelectDifficulty(diff) },
                    contentPadding = PaddingValues(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = diff.localizedLabel(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                        Text(
                            text = diff.koreanBadge,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = diff.localizedDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bText("$count randomized scenarios ready", "$count skenario acak siap dilatih"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bText("Start session ›", "Mulai sesi ›"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = accent
                        )
                    }
                }
            }
        }
    }
}

// Speaking Session History
@Composable
fun SpeakingHistoryScreen(
    sessions: List<SpeakingSessionEntity>,
    onBack: () -> Unit,
    onOpenSession: (SpeakingSessionEntity) -> Unit
) {
    val extended = LocalBaeumColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Session History", "Riwayat Sesi"),
            subtitle = bText("Previous speaking assessments", "Penilaian berbicara sebelumnya"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (sessions.isEmpty()) {
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        bText("No speaking sessions recorded yet", "Belum ada sesi berbicara yang tersimpan"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        bText(
                            "Complete a Korean conversation to view evidence-based feedback and track your progress here.",
                            "Selesaikan satu percakapan Korea untuk melihat umpan balik dan melacak progres Anda di sini."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                sessions.forEach { s ->
                    SeoulCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenSession(s) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.scenarioTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    bText("${s.difficulty} • ${s.completedTurns} turns • ${s.dayKey}", "${s.difficulty} • ${s.completedTurns} giliran • ${s.dayKey}"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${s.overallScore}/100",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (s.overallScore >= 70) extended.mint else extended.gold
                            )
                        }
                    }
                }
            }
        }
    }
}
