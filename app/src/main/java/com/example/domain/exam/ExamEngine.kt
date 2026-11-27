package com.example.domain.exam

import com.example.data.local.VocabEntity
import com.example.data.seed.EpsAndSpeakingMockBank
import com.example.data.seed.ExamPaperLength
import com.example.data.seed.ExamQuestion
import com.example.data.seed.ExamType
import com.example.data.seed.Topik1MockBank
import com.example.data.seed.Topik2MockBank
import com.example.domain.scoring.KoreanRegisterTarget
import com.example.domain.scoring.LearningScoringEngine
import com.example.domain.scoring.LetterGrade
import com.example.domain.scoring.OpenResponseRubricScore
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ExamSessionMode(val label: String) {
    PRACTICE("Practice mode"),
    MOCK("Mock exam")
}

data class ExamCutoffBand(
    val label: String,
    val minScore: Int,
    val achieved: Boolean
)

data class ActiveExamSession(
    val examType: ExamType,
    val mode: ExamSessionMode,
    val questions: List<ExamQuestion>,
    val currentIndex: Int = 0,
    val selectedOptions: Map<String, Int> = emptyMap(),
    val openResponses: Map<String, String> = emptyMap(),
    val flaggedQuestionIds: Set<String> = emptySet(),
    val startedAtEpochMs: Long = System.currentTimeMillis(),
    val deadlineEpochMs: Long = System.currentTimeMillis() + (examType.mockDurationMinutes * 60_000L),
    val isSubmitted: Boolean = false,
    val paperLength: ExamPaperLength = ExamPaperLength.STANDARD,
    val eliminatedOptions: Map<String, Set<Int>> = emptyMap(),
    val audioPlayCounts: Map<String, Int> = emptyMap(),
    val practiceCheckedIds: Set<String> = emptySet(),
    val activePeriod: Int = 1,
    val totalPeriods: Int = 1,
    val awaitingPeriodTransition: Boolean = false,
    val period1DurationMinutes: Int = examType.mockDurationMinutes,
    val period2DurationMinutes: Int = 0
) {
    val currentQuestion: ExamQuestion? get() = questions.getOrNull(currentIndex)

    val activePeriodIndices: List<Int>
        get() = if (mode == ExamSessionMode.MOCK && totalPeriods > 1) {
            questions.indices.filter { questions[it].period == activePeriod }
        } else {
            questions.indices.toList()
        }

    fun isQuestionAnswered(q: ExamQuestion): Boolean {
        return if (q.isOpenEnded) {
            val raw = openResponses[q.id].orEmpty()
            if (q.dualBlanks) {
                val (g, n) = ExamEngine.parseDualBlankResponse(raw)
                g.isNotBlank() || n.isNotBlank()
            } else {
                raw.isNotBlank()
            }
        } else {
            selectedOptions.containsKey(q.id)
        }
    }

    val answeredCount: Int get() = questions.count { isQuestionAnswered(it) }

    val activePeriodAnsweredCount: Int
        get() = activePeriodIndices.count { isQuestionAnswered(questions[it]) }

    fun isQuestionAccessible(index: Int): Boolean {
        if (index !in questions.indices) return false
        if (mode == ExamSessionMode.MOCK && totalPeriods > 1) {
            return questions[index].period == activePeriod
        }
        return true
    }

    fun isQuestionLocked(index: Int): Boolean {
        if (index !in questions.indices) return true
        return mode == ExamSessionMode.MOCK && totalPeriods > 1 && questions[index].period < activePeriod
    }

    fun audioQuotaKey(q: ExamQuestion): String = q.sharedGroupId ?: q.id

    fun audioPlaysUsed(q: ExamQuestion): Int = audioPlayCounts[audioQuotaKey(q)] ?: 0

    /**
     * Returns null in Practice mode (unlimited replays), or remaining plays (0..maxAudioPlaysInMock) in Mock mode.
     */
    fun remainingAudioPlays(q: ExamQuestion): Int? {
        if (q.audioTranscriptKo == null) return 0
        if (mode == ExamSessionMode.PRACTICE) return null
        return (q.maxAudioPlaysInMock - audioPlaysUsed(q)).coerceAtLeast(0)
    }

    fun canPlayAudio(q: ExamQuestion): Boolean {
        if (q.audioTranscriptKo == null) return false
        val remaining = remainingAudioPlays(q)
        return remaining == null || remaining > 0
    }

    fun previousAccessibleIndex(): Int? {
        val pos = activePeriodIndices.indexOf(currentIndex)
        return if (pos > 0) activePeriodIndices[pos - 1] else null
    }

    fun nextAccessibleIndex(): Int? {
        val pos = activePeriodIndices.indexOf(currentIndex)
        return if (pos >= 0 && pos < activePeriodIndices.lastIndex) activePeriodIndices[pos + 1] else null
    }

    val isLastInActivePeriod: Boolean
        get() = nextAccessibleIndex() == null

    val hasNextPeriod: Boolean
        get() = mode == ExamSessionMode.MOCK && activePeriod < totalPeriods
}

data class ExamSectionScore(
    val sectionName: String,
    val earned: Int,
    val max: Int,
    val isEstimated: Boolean = false,
    val correctCount: Int = 0,
    val totalCount: Int = 0
)

data class ExamTierAccuracy(
    val tierLabel: String,
    val correct: Int,
    val total: Int,
    val percent: Int
)

data class ExamQuestionReview(
    val question: ExamQuestion,
    val selectedOptionIndex: Int?,
    val openResponseText: String?,
    val isCorrect: Boolean,
    val earnedWeight: Double = if (isCorrect) question.pointWeight.toDouble() else 0.0,
    val rubricScore: OpenResponseRubricScore? = null
)

data class ExamReport(
    val examType: ExamType,
    val mode: ExamSessionMode,
    val totalScore: Int,
    val maxScore: Int,
    val estimatedLevel: String,
    val sectionScores: List<ExamSectionScore>,
    val analysisSummary: String,
    val questionReviews: List<ExamQuestionReview>,
    val durationSec: Int,
    val accuracyPercent: Int = 0,
    val avgSecondsPerQuestion: Int = 0,
    val pacingNote: String = "",
    val grade: LetterGrade = LetterGrade.B,
    val tierBreakdowns: List<ExamTierAccuracy> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val cutoffLadder: List<ExamCutoffBand> = emptyList(),
    val paperLength: ExamPaperLength = ExamPaperLength.STANDARD
)

object ExamEngine {

    private const val DUAL_BLANK_G_PREFIX = "(ㄱ)"
    private const val DUAL_BLANK_N_PREFIX = "(ㄴ)"

    fun formatDualBlankResponse(blankG: String, blankN: String): String {
        val g = blankG.trim()
        val n = blankN.trim()
        if (g.isEmpty() && n.isEmpty()) return ""
        return "$DUAL_BLANK_G_PREFIX $g\n$DUAL_BLANK_N_PREFIX $n"
    }

    fun parseDualBlankResponse(raw: String?): Pair<String, String> {
        if (raw.isNullOrBlank()) return "" to ""
        val lines = raw.lines()
        val gLine = lines.firstOrNull { it.trimStart().startsWith(DUAL_BLANK_G_PREFIX) }
            ?.substringAfter(DUAL_BLANK_G_PREFIX)?.trim()
        val nLine = lines.firstOrNull { it.trimStart().startsWith(DUAL_BLANK_N_PREFIX) }
            ?.substringAfter(DUAL_BLANK_N_PREFIX)?.trim()
        if (gLine != null || nLine != null) {
            return (gLine ?: "") to (nLine ?: "")
        }
        return raw.trim() to ""
    }

    /**
     * Generates a 100% pure-Korean, blueprint-accurate TOPIK / EPS-TOPIK / TOPIK Speaking
     * exam session with zero English/Indonesian translation leaks inside stimuli or options.
     *
     * Supports:
     * - TOPIK I: 12q Practice / 20q Compact Mock (30m) / 70q Full Official Mock (100m, 100+100 pts)
     * - TOPIK II: 2-period structure (1교시 Listening+Writing 51–54, 2교시 Reading) with 1×/2× audio rules
     * - EPS-TOPIK: Authentic HRD Korea order (Reading then Listening), 20q Compact / 40q Full (50m, 5 pts each)
     * - TOPIK Speaking: All 6 official NIIED tasks (200 pts) with prep/response timing
     */
    fun startExam(
        examType: ExamType,
        mode: ExamSessionMode,
        allVocab: List<VocabEntity> = emptyList(),
        rng: Random = Random.Default,
        translationLang: String = "EN",
        paperLength: ExamPaperLength = ExamPaperLength.STANDARD
    ): ActiveExamSession {
        @Suppress("UNUSED_VARIABLE")
        val unusedVocabHint = allVocab.size // Kept in signature for backward compatibility
        @Suppress("UNUSED_VARIABLE")
        val unusedLangHint = translationLang

        val now = System.currentTimeMillis()
        val isMock = mode == ExamSessionMode.MOCK
        val isFull = isMock && paperLength == ExamPaperLength.FULL_SIMULATION

        val questions: List<ExamQuestion>
        val totalPeriods: Int
        val period1Min: Int
        val period2Min: Int

        when (examType) {
            ExamType.TOPIK_I -> {
                val listeningTarget = when {
                    !isMock -> 6
                    isFull -> 30
                    else -> 10
                }
                val readingTarget = when {
                    !isMock -> 6
                    isFull -> 40
                    else -> 10
                }
                val listening = sampleSectionPreservingGroups(
                    pool = Topik1MockBank.listeningQuestions,
                    targetCount = listeningTarget,
                    rng = rng
                ).map { shuffleQuestionOptions(it, rng) }
                val reading = sampleSectionPreservingGroups(
                    pool = Topik1MockBank.readingQuestions,
                    targetCount = readingTarget,
                    rng = rng
                ).map { shuffleQuestionOptions(it, rng) }

                questions = listening + reading
                totalPeriods = 1
                period1Min = when {
                    !isMock -> 25
                    isFull -> 100
                    else -> 30
                }
                period2Min = 0
            }

            ExamType.TOPIK_II -> {
                val listeningTarget = when {
                    !isMock -> 5
                    isFull -> 30
                    else -> 10
                }
                val readingTarget = when {
                    !isMock -> 5
                    isFull -> 30
                    else -> 10
                }

                // Preserve authentic 1회 (first part) followed by 2회 (second part) ordering in TOPIK II Listening
                val onePlayPool = Topik2MockBank.listeningQuestions.filter { it.maxAudioPlaysInMock == 1 }
                val twoPlayPool = Topik2MockBank.listeningQuestions.filter { it.maxAudioPlaysInMock >= 2 }
                val onePlayTarget = when {
                    !isMock -> 2
                    isFull -> onePlayPool.size
                    else -> 4
                }
                val twoPlayTarget = (listeningTarget - onePlayTarget).coerceAtLeast(0)

                val listening = (
                    sampleSectionPreservingGroups(onePlayPool, onePlayTarget, rng) +
                        sampleSectionPreservingGroups(twoPlayPool, twoPlayTarget, rng)
                    ).map { shuffleQuestionOptions(it, rng) }

                val writing = if (isMock) {
                    // Authentic TOPIK II Writing: ALL 4 tasks 51 (10점) + 52 (10점) + 53 (30점) + 54 (50점) = 100점
                    listOf(
                        Topik2MockBank.writingTask51Variants[rng.nextInt(Topik2MockBank.writingTask51Variants.size)],
                        Topik2MockBank.writingTask52Variants[rng.nextInt(Topik2MockBank.writingTask52Variants.size)],
                        Topik2MockBank.writingTask53Variants[rng.nextInt(Topik2MockBank.writingTask53Variants.size)],
                        Topik2MockBank.writingTask54Variants[rng.nextInt(Topik2MockBank.writingTask54Variants.size)]
                    )
                } else {
                    // Practice mode: 1 short blank task (51 or 52) + 1 essay task (53 or 54)
                    val shortPool = Topik2MockBank.writingTask51Variants + Topik2MockBank.writingTask52Variants
                    val longPool = Topik2MockBank.writingTask53Variants + Topik2MockBank.writingTask54Variants
                    listOf(
                        shortPool[rng.nextInt(shortPool.size)],
                        longPool[rng.nextInt(longPool.size)]
                    )
                }

                val reading = sampleSectionPreservingGroups(
                    pool = Topik2MockBank.readingQuestions,
                    targetCount = readingTarget,
                    rng = rng
                ).map { shuffleQuestionOptions(it, rng) }

                // Official TOPIK II Order: 1교시 (Listening -> Writing) then 2교시 (Reading)
                questions = listening + writing + reading
                totalPeriods = if (isMock) 2 else 1
                period1Min = when {
                    !isMock -> 45
                    isFull -> 90
                    else -> 40
                }
                period2Min = when {
                    !isMock -> 0
                    isFull -> 60
                    else -> 25
                }
            }

            ExamType.EPS_TOPIK -> {
                val readingTarget = when {
                    !isMock -> 6
                    isFull -> 20
                    else -> 10
                }
                val listeningTarget = when {
                    !isMock -> 6
                    isFull -> 20
                    else -> 10
                }
                // Authentic HRD Korea EPS-TOPIK order: Reading (1~20) then Listening (21~40)
                val reading = sampleSectionPreservingGroups(
                    pool = EpsAndSpeakingMockBank.epsReadingQuestions,
                    targetCount = readingTarget,
                    rng = rng
                ).map { shuffleQuestionOptions(it, rng) }
                val listening = sampleSectionPreservingGroups(
                    pool = EpsAndSpeakingMockBank.epsListeningQuestions,
                    targetCount = listeningTarget,
                    rng = rng
                ).map { shuffleQuestionOptions(it, rng) }

                questions = reading + listening
                totalPeriods = 1
                period1Min = when {
                    !isMock -> 20
                    isFull -> 50
                    else -> 25
                }
                period2Min = 0
            }

            ExamType.TOPIK_SPEAKING -> {
                // Authentic NIIED TOPIK Speaking IBT: ALL 6 official tasks (20+30+30+40+40+40 = 200점)
                questions = listOf(
                    EpsAndSpeakingMockBank.speakingTask1Variants[rng.nextInt(EpsAndSpeakingMockBank.speakingTask1Variants.size)],
                    EpsAndSpeakingMockBank.speakingTask2Variants[rng.nextInt(EpsAndSpeakingMockBank.speakingTask2Variants.size)],
                    EpsAndSpeakingMockBank.speakingTask3Variants[rng.nextInt(EpsAndSpeakingMockBank.speakingTask3Variants.size)],
                    EpsAndSpeakingMockBank.speakingTask4Variants[rng.nextInt(EpsAndSpeakingMockBank.speakingTask4Variants.size)],
                    EpsAndSpeakingMockBank.speakingTask5Variants[rng.nextInt(EpsAndSpeakingMockBank.speakingTask5Variants.size)],
                    EpsAndSpeakingMockBank.speakingTask6Variants[rng.nextInt(EpsAndSpeakingMockBank.speakingTask6Variants.size)]
                )
                totalPeriods = 1
                period1Min = if (isFull || !isMock) 30 else 20
                period2Min = 0
            }
        }

        return ActiveExamSession(
            examType = examType,
            mode = mode,
            questions = questions,
            currentIndex = 0,
            startedAtEpochMs = now,
            deadlineEpochMs = now + period1Min * 60_000L,
            paperLength = paperLength,
            activePeriod = 1,
            totalPeriods = totalPeriods,
            awaitingPeriodTransition = false,
            period1DurationMinutes = period1Min,
            period2DurationMinutes = period2Min
        )
    }

    /**
     * Moves a multi-period Mock session (e.g. TOPIK II 1교시 -> 2교시) into its intermission overlay.
     */
    fun enterPeriodTransition(session: ActiveExamSession): ActiveExamSession {
        if (!session.hasNextPeriod || session.isSubmitted) return session
        return session.copy(awaitingPeriodTransition = true)
    }

    /**
     * Begins the next official exam period (e.g. TOPIK II 2교시 Reading), locks previous period items,
     * jumps to the first question of the new period, and resets the period countdown timer.
     */
    fun startNextPeriod(
        session: ActiveExamSession,
        nowEpochMs: Long = System.currentTimeMillis()
    ): ActiveExamSession {
        if (!session.hasNextPeriod || session.isSubmitted) return session
        val nextPeriod = session.activePeriod + 1
        val firstIndexInNext = session.questions.indexOfFirst { it.period == nextPeriod }
            .takeIf { it >= 0 } ?: session.currentIndex
        val nextDurationMin = if (nextPeriod == 2 && session.period2DurationMinutes > 0) {
            session.period2DurationMinutes
        } else {
            session.period1DurationMinutes
        }
        return session.copy(
            activePeriod = nextPeriod,
            currentIndex = firstIndexInNext,
            awaitingPeriodTransition = false,
            deadlineEpochMs = nowEpochMs + nextDurationMin * 60_000L
        )
    }

    /**
     * Samples [targetCount] items from [pool] while:
     * 1. Keeping linked shared-passage groups (same [ExamQuestion.sharedGroupId]) strictly intact.
     * 2. Balancing difficulty tiers (1 -> 2 -> 3) in ascending authentic exam order.
     * 3. Producing fresh combinations across different [rng] seeds.
     */
    private fun sampleSectionPreservingGroups(
        pool: List<ExamQuestion>,
        targetCount: Int,
        rng: Random
    ): List<ExamQuestion> {
        if (targetCount <= 0 || pool.isEmpty()) return emptyList()
        if (targetCount >= pool.size) return pool

        // Build atomic blocks: either a single question or a linked sharedGroupId bundle
        val indexById = pool.mapIndexed { idx, q -> q.id to idx }.toMap()
        val consumedIds = HashSet<String>()
        val blocks = mutableListOf<List<ExamQuestion>>()
        for (q in pool) {
            if (!consumedIds.add(q.id)) continue
            val gId = q.sharedGroupId
            if (gId != null) {
                val members = pool.filter { it.sharedGroupId == gId }
                members.forEach { consumedIds.add(it.id) }
                blocks.add(members)
            } else {
                blocks.add(listOf(q))
            }
        }

        val byTier = blocks.groupBy { block -> block.minOf { it.difficultyTier } }
        val selected = mutableListOf<List<ExamQuestion>>()
        val pickedKeys = HashSet<String>()
        var currentCount = 0

        fun tryAddBlock(block: List<ExamQuestion>): Boolean {
            val key = block.first().id
            if (key in pickedKeys) return false
            if (currentCount + block.size > targetCount) return false
            pickedKeys.add(key)
            selected.add(block)
            currentCount += block.size
            return true
        }

        // Stratified pass: sample proportionally from tiers 1, 2, 3
        val tiers = byTier.keys.sorted()
        val perTierTarget = (targetCount / tiers.size.coerceAtLeast(1)).coerceAtLeast(1)
        for (tier in tiers) {
            val tierBlocks = byTier[tier].orEmpty().shuffled(rng)
            var tierCount = 0
            for (b in tierBlocks) {
                if (tierCount >= perTierTarget) break
                if (tryAddBlock(b)) {
                    tierCount += b.size
                }
            }
        }

        // Fill remaining slots with shuffled unpicked blocks (exact fit first)
        val remainingBlocks = blocks.filter { it.first().id !in pickedKeys }.shuffled(rng)
        for (b in remainingBlocks) {
            if (currentCount >= targetCount) break
            tryAddBlock(b)
        }

        // Restore authentic ascending exam order using original blueprint positions
        return selected
            .sortedBy { block -> indexById[block.first().id] ?: Int.MAX_VALUE }
            .flatten()
    }

    private fun shuffleQuestionOptions(q: ExamQuestion, rng: Random): ExamQuestion {
        if (q.isOpenEnded || q.options.size < 2 || q.correctOptionIndex !in q.options.indices) return q
        // Keep fixed position options like ( ㄱ ), ( ㄴ ), ( ㄷ ), ( ㄹ ) in canonical order!
        val isCanonicalSlotOptions = q.options.size == 4 &&
            q.options[0].contains("ㄱ") &&
            q.options[1].contains("ㄴ") &&
            q.options[2].contains("ㄷ") &&
            q.options[3].contains("ㄹ")
        if (isCanonicalSlotOptions) return q

        val indexed = q.options.mapIndexed { idx, opt -> idx to opt }.shuffled(rng)
        val newCorrect = indexed.indexOfFirst { it.first == q.correctOptionIndex }
        return q.copy(
            options = indexed.map { it.second },
            correctOptionIndex = newCorrect
        )
    }

    /**
     * Weighted, idempotent scoring calculator following each official exam's structure:
     * - Multiple-choice items weighted by authentic pointWeight (2점/3점/4점/5점).
     * - TOPIK II Writing weighted at authentic 10점 (51) + 10점 (52) + 30점 (53) + 50점 (54) = 100점.
     * - TOPIK Speaking weighted at authentic 20+30+30+40+40+40 = 200점.
     * - Produces official cutoff ladder, difficulty tier accuracy, pacing diagnostics, and bilingual feedback.
     */
    fun gradeSession(
        session: ActiveExamSession,
        finishedAtEpochMs: Long = System.currentTimeMillis(),
        translationLang: String = "EN"
    ): ExamReport {
        val isId = translationLang.equals("ID", ignoreCase = true)
        val groupedBySection = session.questions.groupBy { it.section }
        val sectionMax = when (session.examType) {
            ExamType.TOPIK_I, ExamType.EPS_TOPIK -> 100
            ExamType.TOPIK_II -> 100
            ExamType.TOPIK_SPEAKING -> 200
        }

        val reviews = mutableListOf<ExamQuestionReview>()
        val sectionScores = mutableListOf<ExamSectionScore>()

        for ((sectionName, sectionQuestions) in groupedBySection) {
            var earnedWeightSum = 0.0
            var totalWeightSum = 0.0
            var hasOpen = false
            var sectionCorrect = 0

            for (q in sectionQuestions) {
                val w = q.pointWeight.coerceAtLeast(1).toDouble()
                totalWeightSum += w

                if (q.isOpenEnded) {
                    hasOpen = true
                    val text = session.openResponses[q.id].orEmpty().trim()
                    val register = resolveRegisterTarget(q)
                    val targetMin = q.targetMinChars.takeIf { it > 0 }
                        ?: if (session.examType == ExamType.TOPIK_II) 45 else 25
                    val rubric = LearningScoringEngine.evaluateKoreanOpenResponse(
                        text = text,
                        expectedKeywords = q.expectedKeywords,
                        targetMinHangul = targetMin,
                        expectedRegister = register,
                        targetMaxChars = q.targetMaxChars,
                        isShortDualBlank = q.dualBlanks,
                        isIndonesian = isId
                    )
                    val earnedW = rubric.totalRatio * w
                    earnedWeightSum += earnedW
                    val passed = rubric.totalRatio >= 0.60
                    if (passed) sectionCorrect++
                    reviews += ExamQuestionReview(
                        question = q,
                        selectedOptionIndex = null,
                        openResponseText = text,
                        isCorrect = passed,
                        earnedWeight = earnedW,
                        rubricScore = rubric
                    )
                } else {
                    val picked = session.selectedOptions[q.id]
                    val correct = picked == q.correctOptionIndex
                    if (correct) {
                        earnedWeightSum += w
                        sectionCorrect++
                    }
                    reviews += ExamQuestionReview(
                        question = q,
                        selectedOptionIndex = picked,
                        openResponseText = null,
                        isCorrect = correct,
                        earnedWeight = if (correct) w else 0.0
                    )
                }
            }

            val score = ((earnedWeightSum / totalWeightSum.coerceAtLeast(1.0)) * sectionMax)
                .roundToInt()
                .coerceIn(0, sectionMax)

            sectionScores += ExamSectionScore(
                sectionName = sectionName,
                earned = score,
                max = sectionMax,
                isEstimated = hasOpen,
                correctCount = sectionCorrect,
                totalCount = sectionQuestions.size
            )
        }

        val totalScore = sectionScores.sumOf { it.earned }.coerceAtMost(session.examType.maxScore)
        val estimatedLevel = estimatePracticeBand(session.examType, totalScore, isId)
        val cutoffLadder = buildCutoffLadder(session.examType, totalScore, isId)
        val totalPct = ((totalScore.toDouble() / session.examType.maxScore.coerceAtLeast(1)) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
        val rawCorrect = reviews.count { it.isCorrect }
        val accuracyPct = ((rawCorrect.toDouble() / reviews.size.coerceAtLeast(1)) * 100.0).roundToInt()

        val totalAllowedMin = (session.period1DurationMinutes + session.period2DurationMinutes)
            .coerceAtLeast(session.examType.mockDurationMinutes)
        val elapsedSec = ((finishedAtEpochMs - session.startedAtEpochMs) / 1000L).toInt().coerceAtLeast(1)
        val avgSecPerQ = (elapsedSec / reviews.size.coerceAtLeast(1)).coerceAtLeast(1)
        val targetSecPerQ = (totalAllowedMin * 60) / reviews.size.coerceAtLeast(1)
        val pacingNote = when {
            avgSecPerQ <= (targetSecPerQ * 0.45).toInt() && accuracyPct < 70 ->
                if (isId) "Tempo terlalu cepat (${avgSecPerQ}dtk/soal) dengan kesalahan yang bisa dihindari — teliti kembali konteks soal."
                else "Fast pace (${avgSecPerQ}s/q) with avoidable errors — slow down and re-check question stems."
            avgSecPerQ > targetSecPerQ ->
                if (isId) "Lebih lambat dari target waktu ujian (${avgSecPerQ}dtk/soal vs target ${targetSecPerQ}dtk) — latih membaca cepat."
                else "Slower than target exam pace (${avgSecPerQ}s/q vs ${targetSecPerQ}s target) — practice timed skimming."
            else ->
                if (isId) "Pengaturan tempo ujian sangat baik (${avgSecPerQ}dtk/soal)."
                else "Well-controlled exam pacing (${avgSecPerQ}s/question)."
        }

        // Difficulty tier breakdown
        val tierNames = if (isId) {
            mapOf(1 to "Dasar (Tier 1)", 2 to "Menengah (Tier 2)", 3 to "Lanjutan (Tier 3)")
        } else {
            mapOf(1 to "Foundation (Tier 1)", 2 to "Intermediate (Tier 2)", 3 to "Advanced (Tier 3)")
        }
        val tierBreakdowns = reviews.groupBy { it.question.difficultyTier }
            .toSortedMap()
            .map { (tier, list) ->
                val ok = list.count { it.isCorrect }
                ExamTierAccuracy(
                    tierLabel = tierNames[tier] ?: "Tier $tier",
                    correct = ok,
                    total = list.size,
                    percent = ((ok.toDouble() / list.size) * 100.0).roundToInt()
                )
            }

        val strongest = sectionScores.maxByOrNull { it.earned.toFloat() / it.max }
        val weakest = sectionScores.minByOrNull { it.earned.toFloat() / it.max }
        val analysis = if (sectionScores.size >= 2 && strongest != null && weakest != null && strongest.sectionName != weakest.sectionName) {
            if (isId) {
                "Terkuat: ${localizeSectionName(strongest.sectionName, true)} (${strongest.earned}/${strongest.max}). Fokus prioritas: ${localizeSectionName(weakest.sectionName, true)} (${weakest.earned}/${weakest.max}). $pacingNote"
            } else {
                "Strongest: ${strongest.sectionName} (${strongest.earned}/${strongest.max}). Priority focus: ${weakest.sectionName} (${weakest.earned}/${weakest.max}). $pacingNote"
            }
        } else {
            if (isId) {
                "Skor latihan keseluruhan: $totalPct% ($rawCorrect/${reviews.size} soal). $pacingNote"
            } else {
                "Overall practice score: $totalPct% ($rawCorrect/${reviews.size} items). $pacingNote"
            }
        }

        val recommendations = mutableListOf<String>()
        if (weakest != null && (weakest.earned.toFloat() / weakest.max) < 0.75f) {
            recommendations += if (isId) {
                "Tingkatkan skor ${localizeSectionName(weakest.sectionName, true)} (${weakest.earned}/${weakest.max}) dengan meninjau transkrip & catatan kesalahan di bawah."
            } else {
                "Boost ${weakest.sectionName} score (${weakest.earned}/${weakest.max}) by reviewing scripts and missed explanations below."
            }
        }
        val advTier = tierBreakdowns.firstOrNull { it.tierLabel.contains("Advanced") || it.tierLabel.contains("Lanjutan") }
        if (advTier != null && advTier.percent < 65) {
            recommendations += if (isId) {
                "Soal tingkat lanjut cukup menantang (${advTier.percent}%) — latih soal urutan kalimat (가–라), sisipan kalimat, dan bacaan panjang."
            } else {
                "Advanced tier questions were challenging (${advTier.percent}%) — practice sentence ordering (가–라), sentence insertion, and multi-question passages."
            }
        }
        val weakOpen = reviews.firstOrNull { it.rubricScore != null && !it.isCorrect }?.rubricScore
        if (weakOpen != null) {
            recommendations += if (isId) "Tips Menulis/Berbicara: ${weakOpen.feedback}" else "Writing/Speaking tip: ${weakOpen.feedback}"
        }
        if (recommendations.isEmpty()) {
            recommendations += if (isId) {
                "Kesiapan ujian sangat baik! Coba Simulasi TOPIK Penuh atau naik ke level ujian berikutnya."
            } else {
                "Strong exam readiness! Try a Full Official Mock simulation or step up to the next TOPIK level."
            }
        }

        return ExamReport(
            examType = session.examType,
            mode = session.mode,
            totalScore = totalScore,
            maxScore = session.examType.maxScore,
            estimatedLevel = estimatedLevel,
            sectionScores = sectionScores,
            analysisSummary = analysis,
            questionReviews = reviews,
            durationSec = elapsedSec,
            accuracyPercent = accuracyPct,
            avgSecondsPerQuestion = avgSecPerQ,
            pacingNote = pacingNote,
            grade = LetterGrade.fromScore(totalPct),
            tierBreakdowns = tierBreakdowns,
            recommendations = recommendations,
            cutoffLadder = cutoffLadder,
            paperLength = session.paperLength
        )
    }

    private fun resolveRegisterTarget(q: ExamQuestion): KoreanRegisterTarget = when {
        q.examType == ExamType.TOPIK_II && q.writingTaskNumber == 51 -> KoreanRegisterTarget.FORMAL_POLITE
        q.examType == ExamType.TOPIK_II -> KoreanRegisterTarget.FORMAL_WRITTEN
        q.examType == ExamType.TOPIK_SPEAKING && (q.writingTaskNumber ?: 1) >= 5 -> KoreanRegisterTarget.FORMAL_POLITE
        else -> KoreanRegisterTarget.POLITE_EVERYDAY
    }

    private fun localizeSectionName(section: String, isId: Boolean): String {
        if (!isId) return section
        return when (section.lowercase()) {
            "listening" -> "Menyimak"
            "reading" -> "Membaca"
            "writing" -> "Menulis"
            "speaking" -> "Berbicara"
            else -> section
        }
    }

    private fun buildCutoffLadder(examType: ExamType, totalScore: Int, isId: Boolean): List<ExamCutoffBand> = when (examType) {
        ExamType.TOPIK_I -> listOf(
            ExamCutoffBand(if (isId) "1급 (≥80)" else "Level 1 (≥80)", 80, totalScore >= 80),
            ExamCutoffBand(if (isId) "2급 (≥140)" else "Level 2 (≥140)", 140, totalScore >= 140)
        )
        ExamType.TOPIK_II -> listOf(
            ExamCutoffBand("3급 (≥120)", 120, totalScore >= 120),
            ExamCutoffBand("4급 (≥150)", 150, totalScore >= 150),
            ExamCutoffBand("5급 (≥190)", 190, totalScore >= 190),
            ExamCutoffBand("6급 (≥230)", 230, totalScore >= 230)
        )
        ExamType.EPS_TOPIK -> listOf(
            ExamCutoffBand(if (isId) "Minimum (≥80)" else "Min (≥80)", 80, totalScore >= 80),
            ExamCutoffBand(if (isId) "Batas Lulus (≥110)" else "Cutoff (≥110)", 110, totalScore >= 110),
            ExamCutoffBand(if (isId) "Kompetitif (≥140)" else "Competitive (≥140)", 140, totalScore >= 140)
        )
        ExamType.TOPIK_SPEAKING -> listOf(
            ExamCutoffBand("1급 (30)", 30, totalScore >= 30),
            ExamCutoffBand("2급 (60)", 60, totalScore >= 60),
            ExamCutoffBand("3급 (90)", 90, totalScore >= 90),
            ExamCutoffBand("4급 (120)", 120, totalScore >= 120),
            ExamCutoffBand("5급 (150)", 150, totalScore >= 150),
            ExamCutoffBand("6급 (170)", 170, totalScore >= 170)
        )
    }

    private fun estimatePracticeBand(examType: ExamType, totalScore: Int, isId: Boolean): String = when (examType) {
        ExamType.TOPIK_I -> when {
            totalScore >= 140 -> if (isId) "Estimasi 2급 • Level 2 (Latihan)" else "Estimated Level 2 • 2급 (Practice)"
            totalScore >= 80 -> if (isId) "Estimasi 1급 • Level 1 (Latihan)" else "Estimated Level 1 • 1급 (Practice)"
            else -> if (isId) "Belum mencapai 1급 (Latihan)" else "Below Level 1 cutoff (Practice)"
        }
        ExamType.TOPIK_II -> when {
            totalScore >= 230 -> if (isId) "Estimasi 6급 • Level 6 (Latihan)" else "Estimated Level 6 • 6급 (Practice)"
            totalScore >= 190 -> if (isId) "Estimasi 5급 • Level 5 (Latihan)" else "Estimated Level 5 • 5급 (Practice)"
            totalScore >= 150 -> if (isId) "Estimasi 4급 • Level 4 (Latihan)" else "Estimated Level 4 • 4급 (Practice)"
            totalScore >= 120 -> if (isId) "Estimasi 3급 • Level 3 (Latihan)" else "Estimated Level 3 • 3급 (Practice)"
            else -> if (isId) "Belum mencapai 3급 (Latihan)" else "Below Level 3 cutoff (Practice)"
        }
        ExamType.EPS_TOPIK -> when {
            totalScore >= 140 -> if (isId) "Rentang Lulus Kompetitif (Latihan)" else "Competitive Pass Range (Practice)"
            totalScore >= 110 -> if (isId) "Rentang Batas Lulus (Latihan)" else "Borderline Pass Range (Practice)"
            else -> if (isId) "Perlu Latihan Lebih Lanjut (Latihan)" else "Needs More Practice (Practice)"
        }
        ExamType.TOPIK_SPEAKING -> when {
            totalScore >= 170 -> if (isId) "Estimasi 말하기 6급 (Latihan)" else "Estimated Speaking Level 6 (Practice)"
            totalScore >= 150 -> if (isId) "Estimasi 말하기 5급 (Latihan)" else "Estimated Speaking Level 5 (Practice)"
            totalScore >= 120 -> if (isId) "Estimasi 말하기 4급 (Latihan)" else "Estimated Speaking Level 4 (Practice)"
            totalScore >= 90 -> if (isId) "Estimasi 말하기 3급 (Latihan)" else "Estimated Speaking Level 3 (Practice)"
            totalScore >= 60 -> if (isId) "Estimasi 말하기 2급 (Latihan)" else "Estimated Speaking Level 2 (Practice)"
            totalScore >= 30 -> if (isId) "Estimasi 말하기 1급 (Latihan)" else "Estimated Speaking Level 1 (Practice)"
            else -> if (isId) "Berbicara Dasar (Latihan)" else "Foundational Speaking (Practice)"
        }
    }
}
