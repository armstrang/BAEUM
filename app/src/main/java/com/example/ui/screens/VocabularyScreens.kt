package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CurriculumPathway
import com.example.data.local.RomanizationMode
import com.example.data.local.VocabEntity
import com.example.domain.fsrs.CardMemoryStage
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian

// SCREEN 09 / 24 — Vocabulary Library (Searchable word database with status filters & bookmarks)
@Composable
fun VocabularyLibraryScreen(
    allVocab: List<VocabEntity>,
    initialCurriculum: String?,
    initialSubCategory: String?,
    romanizationMode: RomanizationMode,
    translationLang: String,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onToggleBookmark: (VocabEntity) -> Unit,
    onOpenWordDetail: (String) -> Unit,
    onStudyFilteredCards: (String?, String?) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf("All") }
    var activeCurriculum by rememberSaveable { mutableStateOf(initialCurriculum) }
    var activeSubCat by rememberSaveable { mutableStateOf(initialSubCategory) }

    val isId = isIndonesian()
    val statusTabs = listOf(
        "All" to if (isId) "Semua" else "All",
        "New" to if (isId) "Baru" else "New",
        "Learning" to if (isId) "Belajar" else "Learning",
        "Mastered" to if (isId) "Dikuasai" else "Mastered",
        "Saved" to if (isId) "Tersimpan" else "Saved"
    )
    val showRom = romanizationMode == RomanizationMode.ALWAYS

    val filtered = remember(allVocab, searchQuery, selectedStatus, activeCurriculum, activeSubCat, translationLang) {
        allVocab.filter { word ->
            val matchCur = activeCurriculum == null || word.curriculum == activeCurriculum
            val matchSub = activeSubCat == null || word.subCategory == activeSubCat
            val matchStatus = when (selectedStatus) {
                "New" -> word.stage == CardMemoryStage.NEW.name
                "Learning" -> word.stage == CardMemoryStage.LEARNING.name || word.stage == CardMemoryStage.REVIEW.name
                "Mastered" -> word.stage == CardMemoryStage.MASTERED.name
                "Saved" -> word.isBookmarked
                else -> true
            }
            val q = searchQuery.trim()
            val matchQuery = q.isEmpty() ||
                word.hangul.contains(q, ignoreCase = true) ||
                word.romanization.contains(q, ignoreCase = true) ||
                word.meaningEn.contains(q, ignoreCase = true) ||
                word.meaningId.contains(q, ignoreCase = true) ||
                word.subCategory.contains(q, ignoreCase = true)
            matchCur && matchSub && matchStatus && matchQuery
        }
    }

    val subtitleText = buildString {
        append(activeCurriculum ?: if (isId) "Semua jalur" else "All pathways")
        if (!activeSubCat.isNullOrBlank()) append(" • $activeSubCat")
        append(" (${filtered.size})")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BaeumTopBar(title = bText("Vocabulary", "Kosakata"), subtitle = subtitleText, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(bText("Search Hangul, romanization, or meaning...", "Cari Hangul, romanisasi, atau arti...")) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_search))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = LocalBaeumColors.current.cardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusTabs.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedStatus == key,
                        onClick = { selectedStatus = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("vocab_filter_$key")
                    )
                }
                if (activeCurriculum != null || activeSubCat != null) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            activeCurriculum = null
                            activeSubCat = null
                        },
                        label = { Text(bText("Show all", "Tampilkan semua")) }
                    )
                }
            }

            if (activeCurriculum == null || activeCurriculum == CurriculumPathway.TOPIK) {
                val topikLevels = listOf("1급", "2급", "3급", "4급", "5급", "6급")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeCurriculum == CurriculumPathway.TOPIK && activeSubCat == null,
                        onClick = {
                            activeCurriculum = CurriculumPathway.TOPIK
                            activeSubCat = null
                        },
                        label = { Text(bText("All TOPIK", "Semua TOPIK")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LocalBaeumColors.current.mint,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("vocab_level_all_topik")
                    )
                    topikLevels.forEach { lvl ->
                        FilterChip(
                            selected = activeSubCat == lvl,
                            onClick = {
                                activeCurriculum = CurriculumPathway.TOPIK
                                activeSubCat = if (activeSubCat == lvl) null else lvl
                            },
                            label = { Text(lvl) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LocalBaeumColors.current.mint,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("vocab_level_$lvl")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = bText("No matching words found", "Tidak ada kata yang cocok"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bText(
                            "Try a different Korean or English keyword, or reset active filters.",
                            "Coba kata kunci Korea atau Indonesia lain, atau atur ulang filter."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    BaeumSecondaryButton(
                        text = bText("Reset filters", "Atur ulang filter"),
                        onClick = {
                            searchQuery = ""
                            selectedStatus = "All"
                            activeCurriculum = null
                            activeSubCat = null
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    BaeumSecondaryButton(
                        text = bText("Study these ${filtered.size} words as Flashcards", "Pelajari ${filtered.size} kata ini di Flashcard"),
                        onClick = { onStudyFilteredCards(activeCurriculum, activeSubCat) },
                        testTag = "vocab_study_flashcards_button"
                    )
                }
                items(filtered, key = { it.id }) { word ->
                    SeoulCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocab_card_${word.id}"),
                        onClick = { onOpenWordDetail(word.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word.hangul,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (showRom) {
                                    Text(
                                        text = word.romanization,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = word.meaningFor(translationLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onSpeakKorean(word.hangul) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = stringResource(R.string.cd_audio_play),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onToggleBookmark(word) }) {
                                    Icon(
                                        imageVector = if (word.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = stringResource(R.string.cd_bookmark),
                                        tint = if (word.isBookmarked) LocalBaeumColors.current.gold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = word.exampleKo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// SCREEN 10 / 24 — Vocabulary Detail (Context-rich word reference)
@Composable
fun WordDetailScreen(
    word: VocabEntity?,
    romanizationMode: RomanizationMode,
    translationLang: String,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onToggleBookmark: (VocabEntity) -> Unit
) {
    if (word == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BaeumTopBar(title = bText("Word details", "Detail kata"), onBack = onBack)
            Text(
                text = bText("Vocabulary item not found.", "Kosakata tidak ditemukan."),
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val showRom = romanizationMode != RomanizationMode.HIDDEN
    val conjugations = remember(word.conjugationsSerialized) { word.parsedConjugations() }
    val extended = LocalBaeumColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
    ) {
        BaeumTopBar(
            title = bText("Word details", "Detail kata"),
            subtitle = "${word.subCategory} • ${word.partOfSpeech}",
            onBack = onBack,
            trailing = {
                IconButton(onClick = { onToggleBookmark(word) }) {
                    Icon(
                        imageVector = if (word.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(R.string.cd_bookmark),
                        tint = if (word.isBookmarked) extended.gold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 660.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Word Card (Blueprint Screen 10)
            SeoulCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSpeakKorean(word.hangul) },
                contentPadding = PaddingValues(vertical = 26.dp, horizontal = 20.dp)
            ) {
                Text(
                    text = word.hangul,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showRom) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = word.romanization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = word.meaningFor(translationLang),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = word.pronunciationNote.ifBlank { bText("Tap to listen to Korean pronunciation", "Ketuk untuk mendengar pelafalan Korea") },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                if (word.hanja.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hanja: ${word.hanja}",
                        style = MaterialTheme.typography.labelMedium,
                        color = extended.gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Dynamic Conjugation Table — ONLY shown when data exists (Blueprint Screen 10 Edge Case!)
            if (conjugations.isNotEmpty()) {
                Text(
                    text = bText("Conjugation", "Konjugasi"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    conjugations.forEachIndexed { idx, (form, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSpeakKorean(form) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = form,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (idx < conjugations.lastIndex) {
                            HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Example Sentence Card
            SeoulCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSpeakKorean(word.exampleKo) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bText("Example sentence", "Contoh kalimat"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.cd_audio_play),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = word.exampleKo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = word.exampleFor(translationLang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Grammar / Formality / Honorific notes when available
            if (word.grammarNote.isNotBlank() || word.formalityNote.isNotBlank() || word.honorificForm.isNotBlank()) {
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = bText("Usage & Speech Level", "Penggunaan & Tingkat Kesopanan"),
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.mint
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (word.grammarNote.isNotBlank()) {
                        Text(word.grammarNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (word.formalityNote.isNotBlank()) {
                        Text(word.formalityNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (word.honorificForm.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bText("Honorific form: ${word.honorificForm}", "Bentuk honorifik: ${word.honorificForm}"),
                            style = MaterialTheme.typography.labelLarge,
                            color = extended.gold
                        )
                    }
                }
            }

            if (word.relatedWords.isNotBlank()) {
                SeoulCard(modifier = Modifier.fillMaxWidth()) {
                    Text(bText("Related vocabulary", "Kosakata terkait"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(word.relatedWords, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            BaeumPrimaryButton(text = bText("Listen again", "Dengarkan lagi"), onClick = { onSpeakKorean(word.hangul) })
        }
    }
}
