package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.GrammarBookmarkEntity
import com.example.data.seed.KoreanGrammarSeed
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText

// SCREEN 15 / 24 — Grammar Studio (Patterns, sentence structure, speech levels, examples & linked quiz)
@Composable
fun GrammarStudioScreen(
    bookmarks: List<GrammarBookmarkEntity>,
    translationLang: String,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onPracticeGrammar: (String) -> Unit
) {
    val patterns = remember { KoreanGrammarSeed.allPatterns }
    var selectedId by rememberSaveable { mutableStateOf(patterns.first().id) }
    val selected = remember(patterns, selectedId) {
        patterns.firstOrNull { it.id == selectedId } ?: patterns.first()
    }
    val isBookmarked = remember(bookmarks, selected.id) {
        bookmarks.any { it.grammarId == selected.id }
    }
    val extended = LocalBaeumColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Korean Grammar", "Tata Bahasa Korea"),
            subtitle = selected.levelBadge,
            onBack = onBack,
            trailing = {
                IconButton(onClick = { onToggleBookmark(selected.id) }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(R.string.cd_bookmark),
                        tint = if (isBookmarked) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pattern Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                patterns.forEach { p ->
                    FilterChip(
                        selected = p.id == selected.id,
                        onClick = { selectedId = p.id },
                        label = { Text(p.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("grammar_chip_${p.id}")
                    )
                }
            }

            AnimatedContent(
                targetState = selected,
                transitionSpec = { BaeumMotion.subtleCrossfadeTransform(extended.reducedMotion) },
                label = "grammar_pattern_detail"
            ) { pattern ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Hero Pattern Card (Blueprint Screen 15)
                    SeoulCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(22.dp)
                    ) {
                        Text(
                            text = pattern.title,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (translationLang == "ID") pattern.meaningId else pattern.meaningEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (translationLang == "ID") pattern.summaryId else pattern.summaryEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Sentence Structure Card
                    Text(bText("Sentence structure", "Struktur kalimat"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    SeoulCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = pattern.structureFormula,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pattern.structureDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bText("Speech level: ${pattern.speechLevelNote}", "Tingkat kesopanan: ${pattern.speechLevelNote}"),
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mint
                        )
                    }

                    // Examples
                    Text(bText("Examples", "Contoh kalimat"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    pattern.examples.forEach { ex ->
                        SeoulCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSpeakKorean(ex.korean) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.korean, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (translationLang == "ID") ex.translationId else ex.translationEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.cd_audio_play),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Common Mistake Card
                    SeoulCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = extended.gold.copy(alpha = 0.55f)
                    ) {
                        Text(bText("Common mistake to avoid", "Kesalahan umum yang harus dihindari"), style = MaterialTheme.typography.labelLarge, color = extended.gold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pattern.commonMistake, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    BaeumPrimaryButton(
                        text = bText("Practice this grammar", "Latih tata bahasa ini"),
                        onClick = { onPracticeGrammar(pattern.id) },
                        testTag = "grammar_practice_button"
                    )
                }
            }
        }
    }
}
