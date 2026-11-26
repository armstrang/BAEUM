package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.speaking.FinalSpeakingAssessment
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText

// SCREEN 19 / 24 — Speaking Assessment (Evidence-based feedback, honest Pronunciation: Not assessed)
@Composable
fun SpeakingAssessmentScreen(
    assessment: FinalSpeakingAssessment?,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onNewScenario: () -> Unit
) {
    if (assessment == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Assessment", "Penilaian"), onBack = onBack)
            Text(bText("No speaking assessment available.", "Belum ada penilaian berbicara tersedia."), modifier = Modifier.padding(24.dp))
        }
        return
    }

    var showAllResponses by remember { mutableStateOf(false) }
    val extended = LocalBaeumColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = "잘했어요!",
            subtitle = bText("Conversation complete • ${assessment.scenarioTitle}", "Percakapan selesai • ${assessment.scenarioTitle}"),
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
            Spacer(modifier = Modifier.height(4.dp))
            CircularGoalRing(
                progress = assessment.totalScoreOutOf100 / 100f,
                centerLabel = "${assessment.totalScoreOutOf100}",
                centerSubLabel = "/ 100",
                size = 128.dp,
                strokeWidth = 9.dp
            )
            Text(
                text = assessment.bandLabel,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = bText(
                    "${assessment.completedTurns} responses • Practice assessment (Not official score)",
                    "${assessment.completedTurns} respons • Penilaian latihan (Bukan skor resmi)"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Skill Breakdown Card (Blueprint Screen 19)
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(bText("Skill breakdown", "Rincian keterampilan"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                RubricBar(bText("Contextual relevance", "Relevansi konteks"), assessment.relevanceAvg)
                Spacer(modifier = Modifier.height(10.dp))
                RubricBar(bText("Vocabulary", "Kosakata"), assessment.vocabularyAvg)
                Spacer(modifier = Modifier.height(10.dp))
                RubricBar(bText("Grammar & particles", "Tata bahasa & partikel"), assessment.grammarAvg)
                Spacer(modifier = Modifier.height(10.dp))
                RubricBar(bText("Communication & speech level", "Komunikasi & tingkat kesopanan"), assessment.communicationAvg)
                Spacer(modifier = Modifier.height(12.dp))
                // Explicitly never invent pronunciation score from STT!
                Text(
                    text = bText("Pronunciation: Not assessed", "Pelafalan: Tidak dinilai"),
                    style = MaterialTheme.typography.labelMedium,
                    color = extended.gold
                )
                Text(
                    text = bText(
                        "Speech recognition text accuracy is never used as a phonetic pronunciation score.",
                        "Akurasi teks pengenalan suara tidak pernah digunakan sebagai skor pelafalan fonetik."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Answer Improvements Card
            if (assessment.improvements.isNotEmpty()) {
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    Text(bText("Answer improvement", "Perbaikan jawaban"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    assessment.improvements.forEach { (userTxt, suggested, note) ->
                        SeoulCard(
                            containerColor = extended.elevatedSurface,
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(12.dp),
                            onClick = { onSpeakKorean(suggested) }
                        ) {
                            Text(
                                text = "$userTxt  →  $suggested",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = extended.mint
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Strengths & Actionable Recommendations
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(bText("Strengths & Next Steps", "Kekuatan & Langkah Berikutnya"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                assessment.strengths.forEach { s ->
                    Text("• $s", style = MaterialTheme.typography.bodySmall, color = extended.mint)
                }
                assessment.weaknesses.forEach { w ->
                    Text("• $w", style = MaterialTheme.typography.bodySmall, color = extended.cherryPink)
                }
                Spacer(modifier = Modifier.height(8.dp))
                assessment.recommendations.forEach { r ->
                    Text(bText("Next: $r", "Berikutnya: $r"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Expandable Turn-by-Turn Transcript Review
            AnimatedVisibility(visible = showAllResponses && assessment.turns.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    assessment.turns.forEachIndexed { i, t ->
                        SeoulCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(bText("Turn ${i + 1}", "Giliran ${i + 1}"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text("${t.scoreOutOf10} / 10", style = MaterialTheme.typography.labelMedium, color = extended.mint)
                            }
                            Text(bText("Prompt: ${t.promptKo}", "Pertanyaan: ${t.promptKo}"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(bText("You: ${t.userTranscript}", "Anda: ${t.userTranscript}"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(bText("Model: ${t.modelResponseKo}", "Contoh ideal: ${t.modelResponseKo}"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (assessment.turns.isNotEmpty()) {
                BaeumPrimaryButton(
                    text = if (showAllResponses) bText("Hide responses", "Sembunyikan respons") else bText("Review all responses", "Tinjau semua respons"),
                    onClick = { showAllResponses = !showAllResponses },
                    testTag = "assessment_review_all"
                )
            }
            BaeumSecondaryButton(
                text = bText("New scenario", "Skenario baru"),
                onClick = onNewScenario,
                testTag = "assessment_new_scenario"
            )
        }
    }
}

@Composable
private fun RubricBar(label: String, scoreOutOf10: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = String.format(java.util.Locale.US, "%.1f/10", scoreOutOf10),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        ProgressBarSlim(progress = (scoreOutOf10 / 10f).coerceIn(0f, 1f))
    }
}
