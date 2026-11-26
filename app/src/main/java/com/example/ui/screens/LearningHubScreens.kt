package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CurriculumPathway
import com.example.data.local.VocabEntity
import com.example.ui.components.BaeumBrandHeader
import com.example.ui.components.BaeumGlassCard
import com.example.ui.components.BaeumGlassStyle
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.bText
import com.example.ui.theme.isIndonesian

// SCREEN 04 / 24 — Learning Hub (Four independent pathways)
@Composable
fun LearningHubScreen(
    allVocab: List<VocabEntity>,
    onOpenSettings: () -> Unit,
    onSelectHangul: () -> Unit,
    onSelectTopik: () -> Unit,
    onSelectEpsTopik: () -> Unit,
    onSelectEveryday: () -> Unit,
    onOpenGrammarStudio: () -> Unit,
    onOpenQuizCenter: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val progressByPathway = remember(allVocab) {
        allVocab.groupBy { it.curriculum }.mapValues { (_, list) ->
            if (list.isEmpty()) 0f else list.count { it.reps > 0 }.toFloat() / list.size
        }
    }

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
                    title = bText("Learn Korean", "Belajar Korea"),
                    subtitle = bText("Curated pathways crafted for mastery", "Jalur belajar terstruktur menuju kefasihan"),
                    onSettingsClick = onOpenSettings
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            StaggeredEntrance(index = 1) {
                PathwayCard(
                    emblem = "한",
                    title = bText("Hangul Fundamentals", "Dasar Hangul"),
                    subtitle = bText(
                        "Alphabet, syllable blocks, batchim & stroke order",
                        "Alfabet, blok suku kata, batchim & urutan goresan"
                    ),
                    progress = progressByPathway[CurriculumPathway.HANGUL] ?: 0f,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = onSelectHangul,
                    testTag = "pathway_hangul"
                )
            }
            StaggeredEntrance(index = 2) {
                PathwayCard(
                    emblem = "급",
                    title = "TOPIK 1–6급",
                    subtitle = bText(
                        "6,600+ academic vocabulary, grammar & exam mastery",
                        "6.600+ kosakata akademik, tata bahasa & persiapan ujian"
                    ),
                    progress = progressByPathway[CurriculumPathway.TOPIK] ?: 0f,
                    accent = extended.mint,
                    onClick = onSelectTopik,
                    testTag = "pathway_topik"
                )
            }
            StaggeredEntrance(index = 3) {
                PathwayCard(
                    emblem = "일",
                    title = "EPS-TOPIK",
                    subtitle = bText(
                        "Industrial safety, manufacturing & workplace Korean",
                        "Keselamatan kerja, manufaktur & bahasa Korea di tempat kerja"
                    ),
                    progress = progressByPathway[CurriculumPathway.EPS_TOPIK] ?: 0f,
                    accent = extended.gold,
                    onClick = onSelectEpsTopik,
                    testTag = "pathway_eps"
                )
            }
            StaggeredEntrance(index = 4) {
                PathwayCard(
                    emblem = "말",
                    title = bText("Everyday Korean", "Korea Sehari-hari"),
                    subtitle = bText(
                        "Seoul life conversations & polite speech levels",
                        "Percakapan hidup di Seoul & tingkat kesopanan bicara"
                    ),
                    progress = progressByPathway[CurriculumPathway.EVERYDAY] ?: 0f,
                    accent = extended.cherryPink,
                    onClick = onSelectEveryday,
                    testTag = "pathway_everyday"
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            StaggeredEntrance(index = 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BaeumSecondaryButton(
                        text = bText("Grammar Studio", "Studio Tata Bahasa"),
                        onClick = onOpenGrammarStudio,
                        modifier = Modifier.weight(1f),
                        testTag = "learn_grammar_button"
                    )
                    BaeumSecondaryButton(
                        text = bText("Quiz Center", "Pusat Kuis"),
                        onClick = onOpenQuizCenter,
                        modifier = Modifier.weight(1f),
                        testTag = "learn_quiz_button"
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PathwayCard(
    emblem: String,
    title: String,
    subtitle: String,
    progress: Float,
    accent: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val pct = (progress * 100).toInt()
    val emblemShape = RoundedCornerShape(18.dp)
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
                    .size(50.dp)
                    .clip(emblemShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.08f))
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.45f), emblemShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emblem,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        ProgressBarSlim(progress = progress, color = accent)
    }
}

// SCREEN 07 / 24 — TOPIK Center (Levels 1–6)
@Composable
fun TopikCenterScreen(
    allVocab: List<VocabEntity>,
    onBack: () -> Unit,
    onOpenLevelVocab: (String) -> Unit,
    onStartLevelFlashcards: (String?) -> Unit,
    onOpenGrammar: () -> Unit,
    onOpenExam: () -> Unit
) {
    val topikVocab = remember(allVocab) { allVocab.filter { it.curriculum == CurriculumPathway.TOPIK } }
    val levelOrder = listOf("1급", "2급", "3급", "4급", "5급", "6급")
    val byLevel = remember(topikVocab) { topikVocab.groupBy { it.subCategory } }
    val cumulativeCounts = remember(byLevel) {
        var running = 0
        levelOrder.associateWith { lvl ->
            running += byLevel[lvl]?.size ?: 0
            running
        }
    }

    val isId = isIndonesian()
    fun levelStatus(level: String): String {
        val list = byLevel[level].orEmpty()
        if (list.isEmpty()) return if (isId) "Kurikulum siap" else "Curriculum ready"
        val learned = list.count { it.reps > 0 }
        val cumul = cumulativeCounts[level] ?: list.size
        return if (learned == 0) {
            if (level == "1급") {
                if (isId) "${list.size} kata" else "${list.size} words"
            } else {
                "+${list.size} • $cumul total"
            }
        } else {
            "${(learned * 100) / list.size}% • $learned/${list.size}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("TOPIK Center", "Pusat TOPIK"),
            subtitle = bText("Korean proficiency • ${topikVocab.size} words", "Kemahiran Korea • ${topikVocab.size} kata"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "한국어능력시험",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "TOPIK • ${topikVocab.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bText(
                        "Structured vocabulary across TOPIK I (1–2급) and TOPIK II (3–6급). Study tracks are practice curricula, not official certification.",
                        "Kosakata terstruktur untuk TOPIK I (1–2급) dan TOPIK II (3–6급). Jalur ini adalah kurikulum latihan, bukan sertifikasi resmi."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = bText("TOPIK I / Beginner", "TOPIK I / Pemula"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TopikLevelTile("1급", levelStatus("1급"), Modifier.weight(1f)) { onOpenLevelVocab("1급") }
                TopikLevelTile("2급", levelStatus("2급"), Modifier.weight(1f)) { onOpenLevelVocab("2급") }
            }

            Text(
                text = bText("TOPIK II / Intermediate & advanced", "TOPIK II / Menengah & mahir"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TopikLevelTile("3급", levelStatus("3급"), Modifier.weight(1f)) { onOpenLevelVocab("3급") }
                TopikLevelTile("4급", levelStatus("4급"), Modifier.weight(1f)) { onOpenLevelVocab("4급") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TopikLevelTile("5급", levelStatus("5급"), Modifier.weight(1f)) { onOpenLevelVocab("5급") }
                TopikLevelTile("6급", levelStatus("6급"), Modifier.weight(1f)) { onOpenLevelVocab("6급") }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BaeumSecondaryButton(
                    text = bText("TOPIK Flashcards", "Flashcard TOPIK"),
                    onClick = { onStartLevelFlashcards(null) },
                    modifier = Modifier.weight(1f)
                )
                BaeumSecondaryButton(
                    text = bText("Grammar & Exams", "Tata Bahasa"),
                    onClick = onOpenGrammar,
                    modifier = Modifier.weight(1f)
                )
            }
            BaeumSecondaryButton(
                text = bText("Open TOPIK Exam Simulator", "Buka Simulator Ujian TOPIK"),
                onClick = onOpenExam
            )
        }
    }
}

@Composable
private fun TopikLevelTile(
    levelKo: String,
    status: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SeoulCard(
        modifier = modifier.testTag("topik_level_$levelKo"),
        onClick = onClick
    ) {
        Text(
            text = levelKo,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// SCREEN 08 / 24 — EPS-TOPIK Center (Employment-oriented Korean)
@Composable
fun EpsTopikCenterScreen(
    allVocab: List<VocabEntity>,
    onBack: () -> Unit,
    onOpenCategoryVocab: (String) -> Unit,
    onStartEpsFlashcards: () -> Unit,
    onOpenEpsExam: () -> Unit
) {
    val isId = isIndonesian()
    val epsCategories = listOf(
        Triple("Daily communication", "Komunikasi harian", "일상생활"),
        Triple("Manufacturing", "Manufaktur", "제조업"),
        Triple("Workplace safety", "Keselamatan kerja", "산업안전"),
        Triple("Agriculture", "Pertanian", "농업"),
        Triple("Fisheries", "Perikanan", "어업"),
        Triple("Workplace culture", "Budaya kerja", "직장문화")
    )
    val epsVocab = remember(allVocab) { allVocab.filter { it.curriculum == CurriculumPathway.EPS_TOPIK } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = "EPS-TOPIK",
            subtitle = bText("Employment-oriented Korean", "Bahasa Korea dunia kerja"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "한국에서 일하기",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "EPS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LocalBaeumColors.current.gold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bText(
                        "Your career in Korea — workplace safety terminology, industrial communication, and independent EPS-TOPIK exam prep.",
                        "Karier Anda di Korea — istilah keselamatan kerja, komunikasi industri, dan persiapan ujian EPS-TOPIK mandiri."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            epsCategories.forEach { (eng, idLabel, kor) ->
                val count = epsVocab.count { it.subCategory == eng }
                SeoulCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("eps_cat_$eng"),
                    onClick = { onOpenCategoryVocab(eng) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isId) idLabel else eng,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isId) "$kor • $count istilah kunci" else "$kor • $count key terms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BaeumSecondaryButton(
                    text = bText("EPS Flashcards", "Flashcard EPS"),
                    onClick = onStartEpsFlashcards,
                    modifier = Modifier.weight(1f)
                )
                BaeumSecondaryButton(
                    text = bText("EPS Mock Exam", "Simulasi EPS"),
                    onClick = onOpenEpsExam,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Practical Korean & Speech Level Lab (Blueprint Page 31 differentiation feature)
@Composable
fun EverydayKoreanScreen(
    allVocab: List<VocabEntity>,
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onOpenCategoryVocab: (String) -> Unit,
    onStartConversation: () -> Unit
) {
    val isId = isIndonesian()
    var selectedSituation by remember { mutableIntStateOf(0) }
    val speechComparisons = listOf(
        Triple(if (isId) "Sapaan" else "Greeting", "안녕? 잘 지냈어?", "안녕하세요? 잘 지내셨어요?  |  안녕하십니까?"),
        Triple(if (isId) "Makan" else "Eating", "밥 먹었어?", if (isId) "밥 먹었어요?  |  진지 드셨습니까? (Honorifik)" else "밥 먹었어요?  |  진지 드셨습니까? (Honorific)"),
        Triple(if (isId) "Terima kasih" else "Thanking", "고마워!", "고마워요.  |  정말 감사합니다."),
        Triple(if (isId) "Maaf" else "Apologizing", "미안해.", "죄송해요.  |  대단히 죄송합니다.")
    )
    val categories = listOf(
        Triple("Cafe & Dining", "Kafe & Makan", "카페 • 식당"),
        Triple("Shopping", "Belanja", "쇼핑 • 계산"),
        Triple("Transportation", "Transportasi", "지하철 • 택시"),
        Triple("Travel", "Perjalanan", "여행 • 예약"),
        Triple("Interviews", "Wawancara Kerja", "면접 • 직장"),
        Triple("Social & Honorifics", "Sosial & Honorifik", "높임말 • 예절")
    )
    val everydayVocab = remember(allVocab) { allVocab.filter { it.curriculum == CurriculumPathway.EVERYDAY } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Everyday Korean", "Korea Sehari-hari"),
            subtitle = bText("Seoul Life & Speech Level Lab", "Kehidupan Seoul & Lab Kesopanan"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Speech Level Lab Interactive Card
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bText("SPEECH LEVEL LAB", "LAB TINGKAT KESOPANAN"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bText(
                        "Compare 반말 (Casual) vs. 해요체 / 합니다체 (Polite & Formal)",
                        "Bandingkan 반말 (Santai) vs. 해요체 / 합니다체 (Sopan & Formal)"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    speechComparisons.forEachIndexed { idx, (label, _, _) ->
                        val active = idx == selectedSituation
                        SeoulCard(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            cornerRadius = 14.dp,
                            containerColor = if (active) MaterialTheme.colorScheme.primary else LocalBaeumColors.current.elevatedSurface,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp, horizontal = 6.dp),
                            onClick = { selectedSituation = idx }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                val (_, casual, formal) = speechComparisons[selectedSituation]
                SeoulCard(
                    containerColor = LocalBaeumColors.current.elevatedSurface,
                    cornerRadius = 14.dp,
                    onClick = { onSpeakKorean(casual) }
                ) {
                    Text(
                        bText("반말 (Close friends / younger)", "반말 (Teman akrab / lebih muda)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalBaeumColors.current.gold
                    )
                    Text(casual, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(8.dp))
                SeoulCard(
                    containerColor = LocalBaeumColors.current.elevatedSurface,
                    cornerRadius = 14.dp,
                    onClick = { onSpeakKorean(formal.substringBefore("|").trim()) }
                ) {
                    Text(
                        bText("해요체 / 합니다체 (Polite & Honorific)", "해요체 / 합니다체 (Sopan & Honorifik)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalBaeumColors.current.mint
                    )
                    Text(formal, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Text(
                text = bText("Seoul Life Categories", "Kategori Kehidupan Seoul"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            categories.forEach { (cat, idLabel, ko) ->
                val count = everydayVocab.count { it.subCategory == cat }
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenCategoryVocab(cat) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isId) idLabel else cat, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                if (isId) "$ko • $count frasa" else "$ko • $count phrases",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            BaeumSecondaryButton(
                text = bText("Practice Seoul Life Conversations", "Latihan Percakapan Kehidupan Seoul"),
                onClick = onStartConversation
            )
        }
    }
}
