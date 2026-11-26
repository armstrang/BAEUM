package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.audio.TtsStatus
import com.example.domain.hangul.HangulJamoInfo
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.localizedExampleMeaning
import com.example.domain.hangul.localizedExplanation
import com.example.domain.hangul.localizedPronunciationNote
import com.example.domain.hangul.localizedSampleMeaning
import com.example.domain.hangul.localizedTitle
import com.example.domain.hangul.localizedUsageNote
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian

// SCREEN 05 / 24 — Hangul Studio (Alphabet, Syllable Builder, Sound Changes, Numbers)
@Composable
fun HangulStudioScreen(
    showRomanization: Boolean,
    ttsStatus: TtsStatus,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onStartPractice: () -> Unit,
    onOpenWritingStudio: () -> Unit
) {
    val isId = isIndonesian()
    val tabs = if (isId) {
        listOf("Konsonan", "Tegang", "Vokal", "Gabungan", "Batchim", "Aturan Bunyi", "Angka")
    } else {
        listOf("Consonants", "Tense", "Vowels", "Compound", "Batchim", "Sound Rules", "Numbers")
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedJamo by remember { mutableStateOf<HangulJamoInfo>(HangulUnicodeEngine.basicConsonants.first()) }

    // Real Unicode Syllable Builder state
    val builderInitials = listOf('ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ', 'ㅇ', 'ㅈ', 'ㅎ')
    val builderVowels = listOf('ㅏ', 'ㅓ', 'ㅗ', 'ㅜ', 'ㅡ', 'ㅣ', 'ㅐ', 'ㅔ')
    val builderBatchims: List<Char?> = listOf(null, 'ㄱ', 'ㄴ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅇ')
    var selInitIdx by remember { mutableIntStateOf(0) }
    var selVowelIdx by remember { mutableIntStateOf(0) }
    var selBatchimIdx by remember { mutableIntStateOf(0) }

    val initChar = builderInitials[selInitIdx]
    val vowelChar = builderVowels[selVowelIdx]
    val batchimChar = builderBatchims[selBatchimIdx]
    val composedSyllable = HangulUnicodeEngine.composeSyllable(initChar, vowelChar, batchimChar) ?: "가"
    val composedRom = HangulUnicodeEngine.romanizeSyllable(initChar, vowelChar, batchimChar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Hangul Studio", "Studio Hangul"),
            subtitle = bText("Consonants • Vowels • Batchim", "Konsonan • Vokal • Batchim"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Horizontal category tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("hangul_tab_$idx")
                    )
                }
            }

            if (ttsStatus is TtsStatus.Unavailable) {
                Spacer(modifier = Modifier.height(8.dp))
                SeoulCard(
                    borderColor = LocalBaeumColors.current.gold.copy(alpha = 0.6f),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        text = ttsStatus.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalBaeumColors.current.gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> JamoGridSection(
                    items = HangulUnicodeEngine.basicConsonants,
                    selected = selectedJamo,
                    showRomanization = showRomanization,
                    onSelect = {
                        selectedJamo = it
                        onSpeakKorean(it.nameKo)
                    }
                )
                1 -> JamoGridSection(
                    items = HangulUnicodeEngine.doubleConsonants,
                    selected = selectedJamo,
                    showRomanization = showRomanization,
                    onSelect = {
                        selectedJamo = it
                        onSpeakKorean(it.nameKo)
                    }
                )
                2 -> JamoGridSection(
                    items = HangulUnicodeEngine.basicVowels,
                    selected = selectedJamo,
                    showRomanization = showRomanization,
                    onSelect = {
                        selectedJamo = it
                        onSpeakKorean(it.nameKo)
                    }
                )
                3 -> JamoGridSection(
                    items = HangulUnicodeEngine.compoundVowels,
                    selected = selectedJamo,
                    showRomanization = showRomanization,
                    onSelect = {
                        selectedJamo = it
                        onSpeakKorean(it.nameKo)
                    }
                )
                4 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HangulUnicodeEngine.batchimGroups.forEach { b ->
                            SeoulCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onSpeakKorean(b.sampleWordKo) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.glyph, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                        Text("${b.nameKo} (${b.romanization})", style = MaterialTheme.typography.labelMedium, color = LocalBaeumColors.current.mint)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(b.localizedPronunciationNote(isId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(b.sampleWordKo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                                        Text(b.localizedSampleMeaning(isId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HangulUnicodeEngine.soundChangeRules.forEach { rule ->
                            SeoulCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onSpeakKorean(rule.exampleWritten) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rule.localizedTitle(isId), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(rule.titleKo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(rule.localizedExplanation(isId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${rule.exampleWritten}  →  ${rule.examplePronounced}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LocalBaeumColors.current.mint
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "(${rule.localizedExampleMeaning(isId)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HangulUnicodeEngine.koreanNumbers.forEach { num ->
                            SeoulCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onSpeakKorean("${num.nativeKo}, ${num.sinoKo}") }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${num.value}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(bText("Native: ${num.nativeKo}", "Asli: ${num.nativeKo}"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Sino: ${num.sinoKo}", style = MaterialTheme.typography.titleSmall, color = LocalBaeumColors.current.mint)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(num.localizedUsageNote(isId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (selectedTab in 0..3) {
                Spacer(modifier = Modifier.height(12.dp))
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSpeakKorean(selectedJamo.sampleWordKo) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedJamo.glyph}  (${selectedJamo.nameKo})",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(selectedJamo.localizedPronunciationNote(isId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = bText(
                                    "Example: ${selectedJamo.sampleWordKo} — ${selectedJamo.localizedSampleMeaning(false)}",
                                    "Contoh: ${selectedJamo.sampleWordKo} — ${selectedJamo.localizedSampleMeaning(true)}"
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = bText("Play audio", "Putar audio"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Unicode Syllable Builder (Blueprint Screen 05)
            SeoulCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSpeakKorean(composedSyllable) }
            ) {
                Text(
                    text = bText("SYLLABLE BUILDER", "PENYUSUN SUKU KATA"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                val equation = if (batchimChar == null) {
                    "$initChar + $vowelChar = $composedSyllable"
                } else {
                    "$initChar + $vowelChar + $batchimChar = $composedSyllable"
                }
                Text(
                    text = equation,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (showRomanization) {
                        bText("[$composedRom]  •  Tap to listen", "[$composedRom]  •  Ketuk untuk mendengar")
                    } else {
                        bText("Tap to listen to pronunciation", "Ketuk untuk mendengar pelafalan")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BaeumSecondaryButton(
                        text = bText("Cons: $initChar", "Kons: $initChar"),
                        onClick = {
                            selInitIdx = (selInitIdx + 1) % builderInitials.size
                            HangulUnicodeEngine.composeSyllable(builderInitials[selInitIdx], vowelChar, batchimChar)?.let(onSpeakKorean)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "builder_cycle_initial"
                    )
                    BaeumSecondaryButton(
                        text = bText("Vowel: $vowelChar", "Vokal: $vowelChar"),
                        onClick = {
                            selVowelIdx = (selVowelIdx + 1) % builderVowels.size
                            HangulUnicodeEngine.composeSyllable(initChar, builderVowels[selVowelIdx], batchimChar)?.let(onSpeakKorean)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "builder_cycle_vowel"
                    )
                    BaeumSecondaryButton(
                        text = bText("End: ${batchimChar ?: "—"}", "Akhir: ${batchimChar ?: "—"}"),
                        onClick = {
                            selBatchimIdx = (selBatchimIdx + 1) % builderBatchims.size
                            HangulUnicodeEngine.composeSyllable(initChar, vowelChar, builderBatchims[selBatchimIdx])?.let(onSpeakKorean)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "builder_cycle_batchim"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            BaeumPrimaryButton(text = bText("Start practice", "Mulai latihan"), onClick = onStartPractice, testTag = "hangul_start_practice")
            Spacer(modifier = Modifier.height(10.dp))
            BaeumSecondaryButton(text = bText("Writing studio", "Studio menulis"), onClick = onOpenWritingStudio, testTag = "hangul_open_writing")
        }
    }
}

@Composable
private fun JamoGridSection(
    items: List<HangulJamoInfo>,
    selected: HangulJamoInfo,
    showRomanization: Boolean,
    onSelect: (HangulJamoInfo) -> Unit
) {
    val rows = items.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    val isSelected = item.glyph == selected.glyph
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelect(item) }
                            .testTag("jamo_${item.glyph}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else LocalBaeumColors.current.cardBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.glyph,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (showRomanization) {
                                Text(
                                    text = item.romanization,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
