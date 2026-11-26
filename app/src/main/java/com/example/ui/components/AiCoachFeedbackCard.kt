package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.speaking.KoreanAiCoachFeedback
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian

/**
 * Displays structured Korean speaking/pronunciation coaching along with real microphone
 * acoustic cadence analysis and an explicit notice whenever running in local/sample mode.
 */
@Composable
fun AiCoachFeedbackCard(
    feedback: KoreanAiCoachFeedback,
    onSpeakKorean: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val isId = isIndonesian()

    SeoulCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_coach_feedback_card"),
        containerColor = extended.elevatedSurface,
        borderColor = primary.copy(alpha = 0.38f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = bText("AI Coach", "Pelatih AI"),
                    tint = primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = bText("Korean Coach Insight", "Saran Pelatih Korea"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (feedback.isSampleMode) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = extended.gold.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, extended.gold.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = bText("Sample Preview", "Pratinjau Lokal"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.gold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = feedback.localizedTip(isId),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, extended.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = bText("Voice cadence", "Ritme suara"),
                    tint = extended.mint,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bText("VOICE CADENCE & ENERGY", "ENERGI & RITME SUARA"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = extended.mint
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = feedback.localizedCadence(isId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (feedback.naturalAlternativeKo.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                onClick = { onSpeakKorean(feedback.naturalAlternativeKo.substringBefore("→").trim()) },
                shape = RoundedCornerShape(12.dp),
                color = primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.32f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_coach_listen_model")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bText("NATURAL MODEL PHRASE", "CONTOH UNGKAPAN NATURAL"),
                            style = MaterialTheme.typography.labelSmall,
                            color = primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = feedback.naturalAlternativeKo,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = bText("Listen to natural phrase", "Dengarkan contoh ungkapan"),
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (feedback.isSampleMode) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag("ai_coach_firebase_notice")
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedback.localizedNotice(isId),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
