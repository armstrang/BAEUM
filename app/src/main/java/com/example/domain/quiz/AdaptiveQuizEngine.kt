package com.example.domain.quiz

import com.example.data.local.VocabEntity
import com.example.data.seed.KoreanGrammarSeed
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.KoreanLinguisticsEngine
import com.example.domain.scoring.JamoAnswerEvaluation
import com.example.domain.scoring.LearningScoringEngine
import kotlin.math.abs
import kotlin.random.Random

enum class QuizMode(
    val title: String,
    val subtitle: String,
    val shortBadge: String
) {
    RECALL("Recall", "Vocabulary recall", "Vocab"),
    SENTENCE_ARRANGEMENT("Arrange", "Sentence arrangement", "어순"),
    LISTENING("Listening", "Audio recognition", "Audio"),
    BATCHIM("Batchim", "Final consonants", "받침"),
    SENTENCE("Sentence", "Fill in the blank", "Context"),
    GRAMMAR("Grammar", "Grammar patterns", "문법"),
    MIXED("Mixed Quiz", "Adaptive practice", "Adaptive"),
    WRITING("Writing", "Type Korean words", "Typing"),
    CONJUGATION("Conjugation", "Verb transformation", "활용")
}

data class QuizQuestion(
    val id: String,
    val vocabId: String?,
    val mode: QuizMode,
    val promptLabel: String,
    val stimulusText: String,
    val audioTextKo: String? = null,
    val options: List<String> = emptyList(), // Scrambled tokens for SENTENCE_ARRANGEMENT; empty for Writing
    val correctAnswer: String,
    val isTypingMode: Boolean = false,
    val isArrangementMode: Boolean = false,
    val arrangedTokens: List<String> = emptyList(), // Expected Korean token order for SENTENCE_ARRANGEMENT
    val explanationKo: String,
    val explanationTranslation: String,
    val difficultyLevel: Int = 1, // 1 = Foundation (10 pts), 2 = Intermediate (15 pts), 3 = Challenge (20 pts)
    val points: Int = 10,
    val skillTag: String = mode.title
)

object AdaptiveQuizEngine {

    /**
     * Generates non-duplicated, difficulty-scaffolded questions prioritizing weak vocabulary
     * (high quizWrongCount, FSRS lapses, low current Retrievability, or due reviews),
     * with O(1) plausible distractor sampling (same curriculum, POS, and similar Hangul length).
     */
    fun generateQuestions(
        mode: QuizMode,
        allVocab: List<VocabEntity>,
        translationLang: String,
        maxCount: Int = 8,
        weakOnly: Boolean = false,
        focusGrammarId: String? = null,
        activeCurriculum: String? = null
    ): List<QuizQuestion> {
        if (mode == QuizMode.GRAMMAR) {
            return buildGrammarQuestions(maxCount, focusGrammarId, translationLang)
        }
        if (allVocab.size < 4) return emptyList()

        val now = System.currentTimeMillis()
        val rng = Random.Default

        // Rank by composite adaptive priority: quiz errors + lapses + FSRS forgetting risk + pathway fit
        val prioritized = allVocab
            .map { item ->
                val snap = item.toFsrsSnapshot()
                val forgettingRisk = if (item.reps > 0) {
                    ((1.0 - FsrsScheduler.currentRetrievability(snap, now)) * 4.0).toInt()
                } else 0
                val weakness = item.quizWrongCount * 4 + item.lapses * 3 + forgettingRisk
                val isDue = if (item.dueEpochMs in 1..now) 3 else 0
                val pathwayBoost = if (activeCurriculum != null && item.curriculum == activeCurriculum) 1 else 0
                val priorityScore = weakness + isDue + pathwayBoost
                Triple(item, priorityScore, rng.nextInt())
            }
            .sortedWith(
                compareByDescending<Triple<VocabEntity, Int, Int>> { it.second }
                    .thenBy { it.first.reps }
                    .thenBy { it.third }
            )
            .map { it.first }

        val pool = if (weakOnly) {
            val weak = prioritized.filter { it.quizWrongCount > 0 || it.lapses > 0 || it.dueEpochMs in 1..now }
            if (weak.size >= 4) weak else prioritized
        } else {
            prioritized
        }

        val rawQuestions = when (mode) {
            QuizMode.RECALL -> buildRecallQuestions(pool, allVocab, translationLang, maxCount, rng)
            QuizMode.SENTENCE_ARRANGEMENT -> buildSentenceArrangementQuestions(pool, translationLang, maxCount, rng)
            QuizMode.LISTENING -> buildListeningQuestions(pool, allVocab, translationLang, maxCount, rng)
            QuizMode.BATCHIM -> buildBatchimQuestions(pool, translationLang, maxCount, rng)
            QuizMode.SENTENCE -> buildSentenceQuestions(pool, allVocab, translationLang, maxCount, rng)
            QuizMode.GRAMMAR -> buildGrammarQuestions(maxCount, focusGrammarId, translationLang)
            QuizMode.WRITING -> buildWritingQuestions(pool, translationLang, maxCount)
            QuizMode.CONJUGATION -> buildConjugationQuestions(pool, allVocab, translationLang, maxCount, rng)
            QuizMode.MIXED -> {
                val combined = mutableListOf<QuizQuestion>()
                val offsetPool = pool.take(160)
                combined += buildRecallQuestions(offsetPool, allVocab, translationLang, 2, rng)
                combined += buildSentenceArrangementQuestions(offsetPool.drop(2), translationLang, 1, rng)
                combined += buildListeningQuestions(offsetPool.drop(3), allVocab, translationLang, 1, rng)
                combined += buildBatchimQuestions(offsetPool.drop(4), translationLang, 1, rng)
                combined += buildSentenceQuestions(offsetPool.drop(5), allVocab, translationLang, 1, rng)
                combined += buildGrammarQuestions(1, null, translationLang)
                combined += buildConjugationQuestions(offsetPool.drop(6), allVocab, translationLang, 1, rng)
                combined.distinctBy { it.stimulusText }.take(maxCount)
            }
        }

        // Smooth difficulty scaffolding: warm-up (tier 1) -> core (tier 2) -> challenge (tier 3)
        return rawQuestions.sortedBy { it.difficultyLevel }
    }

    /**
     * Evaluates multiple-choice, Korean typing (Jamo similarity), or Sentence Arrangement answers.
     */
    fun evaluateAnswer(question: QuizQuestion, userAnswer: String): JamoAnswerEvaluation {
        return when {
            question.isTypingMode -> LearningScoringEngine.evaluateKoreanTypingAnswer(userAnswer, question.correctAnswer)
            question.isArrangementMode -> {
                val expectedTokens = question.arrangedTokens
                val actualTokens = userAnswer.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                val normalizedCorrect = expectedTokens.joinToString(" ").trim()
                val normalizedActual = actualTokens.joinToString(" ").trim()
                val isExact = normalizedActual == normalizedCorrect
                val positionalMatches = expectedTokens.indices.count { idx ->
                    actualTokens.getOrNull(idx) == expectedTokens[idx]
                }
                val ratio = if (expectedTokens.isNotEmpty()) {
                    positionalMatches.toDouble() / expectedTokens.size.toDouble()
                } else 0.0
                val hint = when {
                    isExact -> "Natural Korean word order (SOV)!"
                    ratio >= 0.5 -> "$positionalMatches/${expectedTokens.size} phrases in the right slot — remember Korean places verbs at the end."
                    else -> "Review Korean SOV word order: Subject/Topic → Time/Place → Object → Verb."
                }
                JamoAnswerEvaluation(
                    isCorrect = isExact,
                    similarity = ratio,
                    isNearMiss = !isExact && ratio >= 0.65,
                    feedbackHint = hint
                )
            }
            else -> {
                val isCorrect = userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                JamoAnswerEvaluation(
                    isCorrect = isCorrect,
                    similarity = if (isCorrect) 1.0 else 0.0,
                    isNearMiss = false,
                    feedbackHint = if (isCorrect) "Correct!" else "Review the explanation below."
                )
            }
        }
    }

    /**
     * Aligns a distractor [VocabEntity] to the exact particle or conjugation morphology of [tokenInSentence]
     * so cloze questions test Korean meaning rather than leaking the answer via bare dictionary forms.
     */
    internal fun alignDistractorToClozeToken(
        target: VocabEntity,
        tokenInSentence: String,
        distractor: VocabEntity
    ): String? {
        if (tokenInSentence == target.hangul) return distractor.hangul

        // Case 1: Particle attached directly after target.hangul (e.g., 학교에, 친구를, 서울로)
        if (tokenInSentence.startsWith(target.hangul)) {
            val suffix = tokenInSentence.removePrefix(target.hangul)
            if (suffix.isEmpty()) return distractor.hangul
            val aligned = when (suffix) {
                "은", "는" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "은", "는")
                "이", "가" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "이", "가")
                "을", "를" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "을", "를")
                "과", "와" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "과", "와")
                "으로", "로" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "으로", "로")
                "이나", "나" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "이나", "나")
                "이랑", "랑" -> KoreanLinguisticsEngine.attachParticle(distractor.hangul, "이랑", "랑")
                "에", "에서", "도", "의", "부터", "까지", "보다", "하고", "에게", "한테", "처럼", "만" ->
                    distractor.hangul + suffix
                else -> null
            }
            if (aligned != null) return aligned
        }

        // Case 2: Conjugated verb/adjective matching one of target's parsed conjugations
        val targetForms = target.parsedConjugations().ifEmpty {
            KoreanLinguisticsEngine.buildConjugationsSerialized(target.hangul, target.partOfSpeech)
                .split("||").mapNotNull { entry ->
                    val parts = entry.split("::")
                    if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                }
        }
        val matchedForm = targetForms.firstOrNull { (form, _) -> form == tokenInSentence }
        if (matchedForm != null) {
            val distractorForms = distractor.parsedConjugations().ifEmpty {
                KoreanLinguisticsEngine.buildConjugationsSerialized(distractor.hangul, distractor.partOfSpeech)
                    .split("||").mapNotNull { entry ->
                        val parts = entry.split("::")
                        if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                    }
            }
            return distractorForms.firstOrNull { (_, label) -> label == matchedForm.second }?.first
        }

        return null
    }

    private fun difficultyForVocab(vocab: VocabEntity): Pair<Int, Int> {
        val level = when (vocab.subCategory) {
            "1급", "Basic Syllables", "Daily communication" -> 1
            "2급", "3급", "Batchim & Tense", "Sound Changes", "Manufacturing", "Workplace safety" -> 2
            "4급", "5급", "6급" -> 3
            else -> if (vocab.hangul.length >= 4 || vocab.lapses >= 2) 3 else 2
        }
        val pts = when (level) {
            1 -> 10
            2 -> 15
            else -> 20
        }
        return level to pts
    }

    /**
     * O(1)-amortized plausible distractor sampler preferring:
     * 1. Same curriculum + same POS + similar Hangul length (prevents trivial elimination)
     * 2. Same curriculum + same POS
     * 3. Any distinct word
     */
    private fun sampleDistractors(
        allVocab: List<VocabEntity>,
        target: VocabEntity,
        count: Int,
        rng: Random,
        selector: (VocabEntity) -> String
    ): List<String> {
        val targetVal = selector(target)
        val picked = LinkedHashSet<String>(count + 1)
        val size = allVocab.size
        if (size <= 1) return emptyList()

        // Pass 1: same curriculum, same POS, similar syllable length
        var attempts = 0
        while (picked.size < count && attempts < 40) {
            attempts++
            val cand = allVocab[rng.nextInt(size)]
            if (cand.id == target.id) continue
            if (cand.curriculum == target.curriculum &&
                cand.partOfSpeech == target.partOfSpeech &&
                abs(cand.hangul.length - target.hangul.length) <= 1
            ) {
                val v = selector(cand)
                if (v.isNotBlank() && v != targetVal) picked.add(v)
            }
        }
        // Pass 2: same curriculum & POS
        attempts = 0
        while (picked.size < count && attempts < 36) {
            attempts++
            val cand = allVocab[rng.nextInt(size)]
            if (cand.id == target.id) continue
            if (cand.curriculum == target.curriculum && cand.partOfSpeech == target.partOfSpeech) {
                val v = selector(cand)
                if (v.isNotBlank() && v != targetVal) picked.add(v)
            }
        }
        // Pass 3: any distinct word
        attempts = 0
        while (picked.size < count && attempts < 64) {
            attempts++
            val cand = allVocab[rng.nextInt(size)]
            if (cand.id == target.id) continue
            val v = selector(cand)
            if (v.isNotBlank() && v != targetVal) picked.add(v)
        }
        if (picked.size < count) {
            for (cand in allVocab) {
                if (cand.id == target.id) continue
                val v = selector(cand)
                if (v.isNotBlank() && v != targetVal) {
                    picked.add(v)
                    if (picked.size == count) break
                }
            }
        }
        return picked.toList()
    }

    private fun buildRecallQuestions(
        pool: List<VocabEntity>,
        allVocab: List<VocabEntity>,
        lang: String,
        maxCount: Int,
        rng: Random
    ): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        return pool.take(maxCount).mapIndexed { index, target ->
            val (diff, pts) = difficultyForVocab(target)
            val meaningToKo = index % 2 == 0
            if (meaningToKo) {
                val distractors = sampleDistractors(allVocab, target, 3, rng) { it.hangul }
                val options = (distractors + target.hangul).shuffled(rng)
                QuizQuestion(
                    id = "rec_${target.id}_$index",
                    vocabId = target.id,
                    mode = QuizMode.RECALL,
                    promptLabel = if (isId) "Pilih kata bahasa Korea yang tepat (${target.subCategory})" else "Choose the correct Korean word (${target.subCategory})",
                    stimulusText = target.meaningFor(lang),
                    audioTextKo = target.hangul,
                    options = options,
                    correctAnswer = target.hangul,
                    explanationKo = target.exampleKo,
                    explanationTranslation = target.exampleFor(lang),
                    difficultyLevel = diff,
                    points = pts,
                    skillTag = if (isId) "Kosakata" else "Vocabulary Recall"
                )
            } else {
                val targetMeaning = target.meaningFor(lang)
                val distractors = sampleDistractors(allVocab, target, 3, rng) { it.meaningFor(lang) }
                val options = (distractors + targetMeaning).shuffled(rng)
                QuizQuestion(
                    id = "rec_km_${target.id}_$index",
                    vocabId = target.id,
                    mode = QuizMode.RECALL,
                    promptLabel = if (isId) "Pilih arti yang sesuai (${target.subCategory})" else "Choose the matching meaning (${target.subCategory})",
                    stimulusText = target.hangul,
                    audioTextKo = target.hangul,
                    options = options,
                    correctAnswer = targetMeaning,
                    explanationKo = target.exampleKo,
                    explanationTranslation = target.exampleFor(lang),
                    difficultyLevel = diff,
                    points = pts,
                    skillTag = if (isId) "Kosakata" else "Vocabulary Recall"
                )
            }
        }
    }

    private fun buildSentenceArrangementQuestions(
        pool: List<VocabEntity>,
        lang: String,
        maxCount: Int,
        rng: Random
    ): List<QuizQuestion> {
        val candidates = ArrayList<Pair<VocabEntity, List<String>>>(maxCount)
        val seenSentences = HashSet<String>()

        for (vocab in pool) {
            val cleanSentence = vocab.exampleKo.trim()
            if (cleanSentence.isBlank() || cleanSentence in seenSentences) continue
            val rawTokens = cleanSentence
                .split(Regex("\\s+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (rawTokens.size !in 3..7) continue
            seenSentences.add(cleanSentence)
            candidates.add(vocab to rawTokens)
            if (candidates.size >= maxCount) break
        }

        return candidates.mapIndexed { idx, (target, orderedTokens) ->
            val (baseDiff, basePts) = difficultyForVocab(target)
            // Scramble tokens ensuring order differs from the answer
            var scrambled = orderedTokens.shuffled(rng)
            var tries = 0
            while (scrambled == orderedTokens && tries < 8) {
                scrambled = orderedTokens.shuffled(rng)
                tries++
            }
            if (scrambled == orderedTokens && orderedTokens.size >= 2) {
                scrambled = orderedTokens.reversed()
            }
            val correctJoined = orderedTokens.joinToString(" ")
            val diff = (if (orderedTokens.size >= 5) maxOf(baseDiff, 2) else baseDiff).coerceAtMost(3)
            val pts = when (diff) {
                1 -> 15
                2 -> 15
                else -> 20
            }
            val isId = lang.equals("ID", ignoreCase = true)
            QuizQuestion(
                id = "arr_${target.id}_$idx",
                vocabId = target.id,
                mode = QuizMode.SENTENCE_ARRANGEMENT,
                promptLabel = if (isId) "Susun frasa Korea sesuai urutan alami (${target.subCategory})" else "Arrange the Korean phrases in natural order (${target.subCategory})",
                stimulusText = "\"${target.exampleFor(lang)}\"",
                audioTextKo = correctJoined,
                options = scrambled,
                correctAnswer = correctJoined,
                isArrangementMode = true,
                arrangedTokens = orderedTokens,
                explanationKo = correctJoined,
                explanationTranslation = if (isId) {
                    "Kata kunci: ${target.hangul} (${target.meaningFor(lang)}) • Bahasa Korea menggunakan urutan Subjek–Objek–Predikat (SOV)."
                } else {
                    "Key word: ${target.hangul} (${target.meaningFor(lang)}) • Korean follows Subject–Object–Verb order."
                },
                difficultyLevel = diff,
                points = pts,
                skillTag = if (isId) "Susun Kalimat" else "Sentence Arrangement"
            )
        }
    }

    private fun buildListeningQuestions(
        pool: List<VocabEntity>,
        allVocab: List<VocabEntity>,
        lang: String,
        maxCount: Int,
        rng: Random
    ): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        return pool.take(maxCount).mapIndexed { idx, target ->
            val (diff, pts) = difficultyForVocab(target)
            val distractors = sampleDistractors(allVocab, target, 3, rng) { it.hangul }
            val options = (distractors + target.hangul).shuffled(rng)
            QuizQuestion(
                id = "lis_${target.id}_$idx",
                vocabId = target.id,
                mode = QuizMode.LISTENING,
                promptLabel = if (isId) "Dengarkan dan pilih kata Korea yang kamu dengar" else "Listen and choose the Korean word you hear",
                stimulusText = if (isId) "Ketuk speaker untuk memutar audio Korea" else "Tap speaker to play Korean audio",
                audioTextKo = target.hangul,
                options = options,
                correctAnswer = target.hangul,
                explanationKo = "${target.hangul} (${target.romanization}) — ${target.meaningFor(lang)}",
                explanationTranslation = target.pronunciationNote.ifBlank { target.exampleFor(lang) },
                difficultyLevel = diff,
                points = pts,
                skillTag = if (isId) "Menyimak" else "Listening"
            )
        }
    }

    private fun buildBatchimQuestions(
        pool: List<VocabEntity>,
        lang: String,
        maxCount: Int,
        rng: Random
    ): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        val batchimSounds = mapOf(
            'ㄱ' to "[ㄱ / -k]", 'ㅋ' to "[ㄱ / -k]", 'ㄲ' to "[ㄱ / -k]",
            'ㄴ' to "[ㄴ / -n]",
            'ㄷ' to "[ㄷ / -t]", 'ㅅ' to "[ㄷ / -t]", 'ㅆ' to "[ㄷ / -t]",
            'ㅈ' to "[ㄷ / -t]", 'ㅊ' to "[ㄷ / -t]", 'ㅌ' to "[ㄷ / -t]", 'ㅎ' to "[ㄷ / -t]",
            'ㄹ' to "[ㄹ / -l]",
            'ㅁ' to "[ㅁ / -m]",
            'ㅂ' to "[ㅂ / -p]", 'ㅍ' to "[ㅂ / -p]",
            'ㅇ' to "[ㅇ / -ng]"
        )
        val allSoundLabels = listOf("[ㄱ / -k]", "[ㄴ / -n]", "[ㄷ / -t]", "[ㄹ / -l]", "[ㅁ / -m]", "[ㅂ / -p]", "[ㅇ / -ng]")

        val candidates = ArrayList<Pair<VocabEntity, Char>>(maxCount)
        val usedSyllables = HashSet<Char>()

        // Pass 1: Strongly prefer word-final syllables (always undergo clean 7-coda neutralization)
        for (vocab in pool) {
            val word = vocab.hangul.trim()
            if (word.isEmpty()) continue
            val lastChar = word.last()
            val b = HangulUnicodeEngine.getBatchim(lastChar)
            if (b != null && batchimSounds.containsKey(b) && lastChar !in usedSyllables) {
                usedSyllables.add(lastChar)
                candidates.add(vocab to lastChar)
                if (candidates.size >= maxCount) break
            }
        }

        // Pass 2: Assimilation-free non-final syllables (no liaison, nasalization, liquidization, or aspiration)
        if (candidates.size < maxCount) {
            for (vocab in pool) {
                val word = vocab.hangul.trim()
                val cleanIdx = word.indices.firstOrNull { idx ->
                    val ch = word[idx]
                    val b = HangulUnicodeEngine.getBatchim(ch)
                    b != null &&
                        batchimSounds.containsKey(b) &&
                        ch !in usedSyllables &&
                        !KoreanLinguisticsEngine.undergoesCodaAssimilationAt(word, idx)
                }
                if (cleanIdx != null) {
                    val ch = word[cleanIdx]
                    usedSyllables.add(ch)
                    candidates.add(vocab to ch)
                    if (candidates.size >= maxCount) break
                }
            }
        }

        return candidates.mapIndexed { idx, (vocab, syllable) ->
            val batchimChar = HangulUnicodeEngine.getBatchim(syllable)!!
            val correctSound = batchimSounds[batchimChar]!!
            val distractors = allSoundLabels.filter { it != correctSound }.shuffled(rng).take(3)
            val options = (distractors + correctSound).shuffled(rng)
            val diff = if (batchimChar in setOf('ㅋ', 'ㄲ', 'ㅅ', 'ㅆ', 'ㅈ', 'ㅊ', 'ㅌ', 'ㅎ', 'ㅍ')) 2 else 1
            QuizQuestion(
                id = "bat_${vocab.id}_$idx",
                vocabId = vocab.id,
                mode = QuizMode.BATCHIM,
                promptLabel = if (isId) "Kenali bunyi akhir representatif konsonan akhir (받침)" else "Identify the representative final consonant (받침) sound",
                stimulusText = if (isId) "$syllable  (dalam ${vocab.hangul})" else "$syllable  (in ${vocab.hangul})",
                audioTextKo = vocab.hangul,
                options = options,
                correctAnswer = correctSound,
                explanationKo = if (isId) {
                    "Konsonan akhir '$batchimChar' pada '$syllable' dibaca sebagai bunyi akhir $correctSound."
                } else {
                    "Final consonant '$batchimChar' in '$syllable' neutralizes to representative coda $correctSound."
                },
                explanationTranslation = vocab.pronunciationNote.ifBlank { "${vocab.hangul}: ${vocab.meaningFor(lang)}" },
                difficultyLevel = diff,
                points = if (diff == 2) 15 else 10,
                skillTag = if (isId) "Fonologi Batchim" else "Batchim Phonology"
            )
        }
    }

    private fun buildSentenceQuestions(
        pool: List<VocabEntity>,
        allVocab: List<VocabEntity>,
        lang: String,
        maxCount: Int,
        rng: Random
    ): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        val questions = ArrayList<QuizQuestion>(maxCount)

        for (target in pool) {
            if (questions.size >= maxCount) break
            val stem = target.hangul.removeSuffix("다")
            if (stem.length < 2 || !target.exampleKo.contains(stem)) continue

            val tokenInSentence = target.exampleKo
                .split(Regex("\\s+"))
                .firstOrNull { it.contains(stem) }
                ?.trimEnd('.', '?', '!', ',')
                ?: continue

            val alignedDistractors = sampleDistractors(allVocab, target, 3, rng) { cand ->
                alignDistractorToClozeToken(target, tokenInSentence, cand).orEmpty()
            }.filter { it.isNotBlank() && it != tokenInSentence }

            if (alignedDistractors.size < 3) continue

            val (diff, pts) = difficultyForVocab(target)
            val blanked = target.exampleKo.replaceFirst(tokenInSentence, "( ____ )")
            val options = (alignedDistractors.take(3) + tokenInSentence).distinct().shuffled(rng)
            if (options.size < 4) continue

            val idx = questions.size
            questions.add(
                QuizQuestion(
                    id = "sen_${target.id}_$idx",
                    vocabId = target.id,
                    mode = QuizMode.SENTENCE,
                    promptLabel = if (isId) "Lengkapi kalimat bahasa Korea berikut" else "Complete the Korean sentence",
                    stimulusText = "$blanked\n\n\"${target.exampleFor(lang)}\"",
                    audioTextKo = target.exampleKo,
                    options = options,
                    correctAnswer = tokenInSentence,
                    explanationKo = target.exampleKo,
                    explanationTranslation = if (isId) {
                        "${target.hangul} (${target.meaningFor(lang)}) sesuai dengan makna dan tata bahasa kalimat."
                    } else {
                        "${target.hangul} (${target.meaningFor(lang)}) fits the sentence meaning and grammar."
                    },
                    difficultyLevel = diff.coerceAtLeast(2),
                    points = pts.coerceAtLeast(15),
                    skillTag = if (isId) "Konteks Kalimat" else "Sentence Context"
                )
            )
        }
        return questions
    }

    private fun buildGrammarQuestions(maxCount: Int, focusGrammarId: String?, lang: String = "EN"): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        val ordered = if (focusGrammarId != null) {
            val focused = KoreanGrammarSeed.allPatterns.filter { it.id == focusGrammarId }
            val others = KoreanGrammarSeed.allPatterns.filter { it.id != focusGrammarId }.shuffled()
            focused + others
        } else {
            KoreanGrammarSeed.allPatterns.shuffled()
        }
        return ordered.take(maxCount).map { g ->
            val options = (g.quizDistractors + g.quizCorrectOption).distinct().shuffled()
            val badge = g.levelBadge.uppercase()
            val diff = when {
                badge.contains("ADVANCED") -> 3
                badge.contains("TOPIK II") || badge.contains("INTERMEDIATE") -> 2
                else -> 1
            }
            QuizQuestion(
                id = "grq_${g.id}",
                vocabId = null,
                mode = QuizMode.GRAMMAR,
                promptLabel = if (isId) "Pilih bentuk tata bahasa Korea yang tepat (${g.title})" else "Select the accurate Korean grammar form (${g.title})",
                stimulusText = g.quizStem,
                audioTextKo = g.examples.firstOrNull()?.korean,
                options = options,
                correctAnswer = g.quizCorrectOption,
                explanationKo = g.structureFormula,
                explanationTranslation = if (isId) g.summaryId else g.quizExplanation,
                difficultyLevel = diff,
                points = when (diff) { 1 -> 10; 2 -> 15; else -> 20 },
                skillTag = if (isId) "Tata Bahasa" else "Grammar"
            )
        }
    }

    private fun buildWritingQuestions(
        pool: List<VocabEntity>,
        lang: String,
        maxCount: Int
    ): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        return pool.take(maxCount).mapIndexed { idx, target ->
            val (diff, pts) = difficultyForVocab(target)
            QuizQuestion(
                id = "wrt_${target.id}_$idx",
                vocabId = target.id,
                mode = QuizMode.WRITING,
                promptLabel = if (isId) "Ketik kata Korea dalam Hangul (${target.subCategory})" else "Type the Korean Hangul word (${target.subCategory})",
                stimulusText = target.meaningFor(lang),
                audioTextKo = target.hangul,
                options = emptyList(),
                correctAnswer = target.hangul,
                isTypingMode = true,
                explanationKo = "${target.hangul} (${target.romanization})",
                explanationTranslation = "${target.exampleKo} • ${target.exampleFor(lang)}",
                difficultyLevel = diff.coerceAtLeast(2),
                points = (pts + 5).coerceAtMost(20),
                skillTag = if (isId) "Mengetik Hangul" else "Hangul Typing"
            )
        }
    }

    private fun buildConjugationQuestions(
        pool: List<VocabEntity>,
        allVocab: List<VocabEntity>,
        lang: String,
        maxCount: Int,
        rng: Random
    ): List<QuizQuestion> {
        val isId = lang.equals("ID", ignoreCase = true)
        val verbsWithForms = ArrayList<VocabEntity>(maxCount)
        for (v in pool) {
            if (v.conjugationsSerialized.isNotBlank() && v.parsedConjugations().size >= 2) {
                verbsWithForms.add(v)
                if (verbsWithForms.size >= maxCount) break
            }
        }
        if (verbsWithForms.isEmpty()) return emptyList()

        val fallbackFormsPool = LinkedHashSet<String>(64)
        for (v in allVocab) {
            if (v.conjugationsSerialized.isNotBlank()) {
                v.parsedConjugations().forEach { fallbackFormsPool.add(it.first) }
                if (fallbackFormsPool.size >= 40) break
            }
        }
        val fallbackFormsList = fallbackFormsPool.toList()

        return verbsWithForms.mapIndexed { idx, verb ->
            val (diff, pts) = difficultyForVocab(verb)
            val parsedForms = verb.parsedConjugations()
            val generatedForms = KoreanLinguisticsEngine.buildConjugationsSerialized(verb.hangul, verb.partOfSpeech)
                .split("||")
                .mapNotNull { entry ->
                    val parts = entry.split("::")
                    if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                }
            val allVerbForms = (parsedForms + generatedForms).distinctBy { it.first }
            val chosen = parsedForms[idx % parsedForms.size]
            val targetForm = chosen.first
            val targetLabel = chosen.second

            // Prioritize same-verb inflections so the learner truly distinguishes tense/speech level
            val sameVerbDistractors = allVerbForms
                .map { it.first }
                .filter { it != targetForm && it != verb.hangul }
                .distinct()
                .shuffled(rng)
                .take(3)
                .toMutableList()

            if (sameVerbDistractors.size < 3) {
                val needed = 3 - sameVerbDistractors.size
                sameVerbDistractors += fallbackFormsList
                    .filter { it != targetForm && it != verb.hangul && it !in sameVerbDistractors }
                    .shuffled(rng)
                    .take(needed)
            }

            val options = (sameVerbDistractors + targetForm).distinct().shuffled(rng)
            QuizQuestion(
                id = "cnj_${verb.id}_$idx",
                vocabId = verb.id,
                mode = QuizMode.CONJUGATION,
                promptLabel = if (isId) "Pilih bentuk konjugasi $targetLabel" else "Choose the $targetLabel form",
                stimulusText = "${verb.hangul}  (${verb.meaningFor(lang)})",
                audioTextKo = targetForm,
                options = options,
                correctAnswer = targetForm,
                explanationKo = "${verb.hangul} → $targetForm ($targetLabel)",
                explanationTranslation = verb.grammarNote.ifBlank { verb.pronunciationNote.ifBlank { verb.exampleFor(lang) } },
                difficultyLevel = diff.coerceAtLeast(2),
                points = pts.coerceAtLeast(15),
                skillTag = if (isId) "Konjugasi" else "Conjugation"
            )
        }
    }
}
