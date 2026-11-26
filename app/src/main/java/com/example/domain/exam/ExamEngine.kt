package com.example.domain.exam

import com.example.data.local.CurriculumPathway
import com.example.data.local.VocabEntity
import com.example.data.seed.ExamQuestion
import com.example.data.seed.ExamType
import com.example.data.seed.KoreanExamSeed
import com.example.data.seed.KoreanGrammarSeed
import com.example.domain.scoring.KoreanRegisterTarget
import com.example.domain.scoring.LearningScoringEngine
import com.example.domain.scoring.LetterGrade
import com.example.domain.scoring.OpenResponseRubricScore
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ExamSessionMode(val label: String) {
    PRACTICE("Practice mode"),
    MOCK("Mock exam")
}

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
    val isSubmitted: Boolean = false
) {
    val currentQuestion: ExamQuestion? get() = questions.getOrNull(currentIndex)
    val answeredCount: Int
        get() = questions.count { q ->
            if (q.isOpenEnded) !openResponses[q.id].isNullOrBlank()
            else selectedOptions.containsKey(q.id)
        }
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
    val recommendations: List<String> = emptyList()
)

object ExamEngine {

    /**
     * Generates a blueprint-accurate mock or practice exam session.
     * - When [allVocab] is empty, uses curated [KoreanExamSeed] blueprint questions.
     * - When [allVocab] is populated (6,600+ words), combines curated passages (with shuffled options)
     *   plus dynamically synthesized Listening, Reading Cloze, Vocabulary, and Grammar items weighted
     *   at authentic 2점/3점/4점 tiers and ordered from easier to harder within each section.
     */
    fun startExam(
        examType: ExamType,
        mode: ExamSessionMode,
        allVocab: List<VocabEntity> = emptyList(),
        rng: Random = Random.Default,
        translationLang: String = "EN"
    ): ActiveExamSession {
        val now = System.currentTimeMillis()
        val curated = KoreanExamSeed.questionsFor(examType)

        val questions = if (allVocab.size < 20 || examType == ExamType.TOPIK_SPEAKING) {
            curated
        } else {
            assembleDynamicExamPaper(examType, mode, curated, allVocab, rng, translationLang)
        }

        val durationMin = if (mode == ExamSessionMode.MOCK) {
            examType.mockDurationMinutes
        } else {
            examType.mockDurationMinutes + 15
        }
        return ActiveExamSession(
            examType = examType,
            mode = mode,
            questions = questions,
            startedAtEpochMs = now,
            deadlineEpochMs = now + durationMin * 60_000L
        )
    }

    private fun assembleDynamicExamPaper(
        examType: ExamType,
        mode: ExamSessionMode,
        curated: List<ExamQuestion>,
        allVocab: List<VocabEntity>,
        rng: Random,
        translationLang: String
    ): List<ExamQuestion> {
        val isMock = mode == ExamSessionMode.MOCK
        val listeningTarget = when (examType) {
            ExamType.TOPIK_I, ExamType.EPS_TOPIK -> if (isMock) 10 else 6
            ExamType.TOPIK_II -> if (isMock) 8 else 5
            ExamType.TOPIK_SPEAKING -> 0
        }
        val readingTarget = when (examType) {
            ExamType.TOPIK_I, ExamType.EPS_TOPIK -> if (isMock) 10 else 6
            ExamType.TOPIK_II -> if (isMock) 10 else 6
            ExamType.TOPIK_SPEAKING -> 0
        }

        val shuffledCurated = curated.map { q -> shuffleQuestionOptions(q, rng) }
        val curatedListening = shuffledCurated.filter { it.section == "Listening" }
        val curatedReading = shuffledCurated.filter { it.section == "Reading" }
        val curatedWriting = shuffledCurated.filter { it.section == "Writing" }

        val vocabPool = selectPoolForExam(examType, allVocab)
        val usedVocabIds = HashSet<String>()

        // Synthesize additional Listening questions
        val synthListeningNeed = (listeningTarget - curatedListening.size).coerceAtLeast(0)
        val synthListening = buildDynamicListeningItems(
            examType = examType,
            pool = vocabPool,
            count = synthListeningNeed,
            usedIds = usedVocabIds,
            rng = rng,
            translationLang = translationLang
        )

        // Synthesize additional Reading questions (mix of cloze, vocabulary, and grammar)
        val synthReadingNeed = (readingTarget - curatedReading.size).coerceAtLeast(0)
        val grammarNeed = (synthReadingNeed / 3).coerceAtLeast(if (synthReadingNeed >= 2) 1 else 0)
        val vocabReadingNeed = (synthReadingNeed - grammarNeed).coerceAtLeast(0)

        val synthGrammar = buildDynamicGrammarReadingItems(examType, grammarNeed, rng, translationLang)
        val synthReading = buildDynamicVocabReadingItems(
            examType = examType,
            pool = vocabPool,
            count = vocabReadingNeed,
            usedIds = usedVocabIds,
            rng = rng,
            translationLang = translationLang
        )

        val listeningSection = (curatedListening + synthListening)
            .sortedBy { it.difficultyTier }
            .take(listeningTarget)
        val readingSection = (curatedReading + synthReading + synthGrammar)
            .sortedBy { it.difficultyTier }
            .take(readingTarget)

        val writingSection = if (examType == ExamType.TOPIK_II && isMock && curatedWriting.size < 2) {
            val task53Prompts = listOf(
                Triple(
                    "최근 5년간 한국인의 전자책(e-book) 이용률이 32%에서 68%로 크게 증가하였다. 주요 원인은 스마트폰 보급 확대와 휴대 편의성이다. 이 변화의 원인과 향후 전망을 150~250자로 설명하십시오.",
                    listOf("증가", "원인", "전자책", "스마트폰", "전망"),
                    "TOPIK II Task 53 analytical summary: state the trend (증가하였다), key causes (원인은 ~이다), and balanced outlook in formal written style (-는다/ㄴ다)."
                ),
                Triple(
                    "설문 조사 결과, 직장인의 74%가 재택근무의 장점으로 '출퇴근 시간 절약'을 꼽았으며, 단점으로는 '동료와의 소통 부족'을 지적하였다. 조사 결과를 요약하고 해결 방안을 150~250자로 쓰십시오.",
                    listOf("재택근무", "장점", "단점", "소통", "해결"),
                    "TOPIK II Task 53 summary: report survey findings accurately using formal written Korean and propose one concrete solution."
                ),
                Triple(
                    "도시 내 1인 가구가 지속적으로 늘어나면서 소포장 식품과 공유 공간 서비스의 수요가 급증하고 있다. 이러한 현상의 배경과 사회적 영향을 150~250자로 서술하십시오.",
                    listOf("1인 가구", "증가", "수요", "배경", "영향"),
                    "TOPIK II Task 53 analysis: connect the demographic shift (1인 가구 증가) to consumer demand and social impact in written plain style."
                )
            )
            val chosen = task53Prompts[rng.nextInt(task53Prompts.size)]
            val task53 = ExamQuestion(
                id = "dyn_t2_w53_${rng.nextInt(10000)}",
                examType = ExamType.TOPIK_II,
                section = "Writing",
                instructionKo = "[53번 유형] 다음 자료의 내용을 분석하여 격식 있는 문어체(-는다/ㄴ다)로 쓰십시오. (3점)",
                stimulusKo = chosen.first,
                isOpenEnded = true,
                correctOptionIndex = -1,
                expectedKeywords = chosen.second,
                explanationEn = chosen.third,
                pointWeight = 3,
                difficultyTier = 2
            )
            listOf(task53) + curatedWriting.map { it.copy(pointWeight = 4, difficultyTier = 3) }
        } else {
            curatedWriting
        }

        return listeningSection + readingSection + writingSection
    }

    private fun selectPoolForExam(examType: ExamType, allVocab: List<VocabEntity>): List<VocabEntity> {
        val filtered = when (examType) {
            ExamType.TOPIK_I -> allVocab.filter {
                it.curriculum == CurriculumPathway.TOPIK && it.subCategory in setOf("1급", "2급")
            }
            ExamType.TOPIK_II -> allVocab.filter {
                it.curriculum == CurriculumPathway.TOPIK && it.subCategory in setOf("3급", "4급", "5급", "6급")
            }
            ExamType.EPS_TOPIK -> {
                val eps = allVocab.filter { it.curriculum == CurriculumPathway.EPS_TOPIK }
                val topik1 = allVocab.filter {
                    it.curriculum == CurriculumPathway.TOPIK && it.subCategory in setOf("1급", "2급")
                }
                eps + topik1
            }
            ExamType.TOPIK_SPEAKING -> allVocab
        }
        return if (filtered.size >= 20) filtered else allVocab
    }

    private fun shuffleQuestionOptions(q: ExamQuestion, rng: Random): ExamQuestion {
        if (q.isOpenEnded || q.options.size < 2 || q.correctOptionIndex !in q.options.indices) return q
        val indexed = q.options.mapIndexed { idx, opt -> idx to opt }.shuffled(rng)
        val newCorrect = indexed.indexOfFirst { it.first == q.correctOptionIndex }
        return q.copy(
            options = indexed.map { it.second },
            correctOptionIndex = newCorrect
        )
    }

    private fun buildDynamicListeningItems(
        examType: ExamType,
        pool: List<VocabEntity>,
        count: Int,
        usedIds: MutableSet<String>,
        rng: Random,
        translationLang: String
    ): List<ExamQuestion> {
        if (count <= 0 || pool.size < 8) return emptyList()
        val isId = translationLang.equals("ID", ignoreCase = true)
        val result = ArrayList<ExamQuestion>(count)
        var attempts = 0
        while (result.size < count && attempts < count * 12) {
            attempts++
            val target = pool[rng.nextInt(pool.size)]
            if (target.id in usedIds || target.exampleKo.length < 8) continue
            val distractors = pickPlausibleVocabDistractors(pool, target, 3, rng, translationLang)
            if (distractors.size < 3) continue
            usedIds.add(target.id)

            val (tier, weight) = tierAndWeightFor(target, examType)
            val targetMeaning = target.meaningFor(translationLang)
            val optionEntries = (distractors.map { it.meaningFor(translationLang) } + targetMeaning).shuffled(rng)
            val correctIdx = optionEntries.indexOf(targetMeaning)

            result.add(
                ExamQuestion(
                    id = "dyn_${examType.name}_l_${result.size}_${target.id}",
                    examType = examType,
                    section = "Listening",
                    instructionKo = "다음을 듣고 들은 표현의 알맞은 뜻을 고르십시오. (${weight}점)",
                    stimulusKo = "음성을 듣고 핵심 어휘 '${target.hangul}'의 의미로 가장 알맞은 것을 고르세요.",
                    audioTranscriptKo = "${target.hangul}. ${target.exampleKo}",
                    options = optionEntries,
                    correctOptionIndex = correctIdx,
                    explanationEn = if (isId) {
                        "${target.hangul} (${target.romanization}) berarti '$targetMeaning'. Contoh: ${target.exampleKo} (${target.exampleFor(translationLang)})"
                    } else {
                        "${target.hangul} (${target.romanization}) means '$targetMeaning'. Example: ${target.exampleKo} (${target.exampleFor(translationLang)})"
                    },
                    pointWeight = weight,
                    difficultyTier = tier
                )
            )
        }
        return result
    }

    private fun buildDynamicVocabReadingItems(
        examType: ExamType,
        pool: List<VocabEntity>,
        count: Int,
        usedIds: MutableSet<String>,
        rng: Random,
        translationLang: String
    ): List<ExamQuestion> {
        if (count <= 0 || pool.size < 8) return emptyList()
        val isId = translationLang.equals("ID", ignoreCase = true)
        val result = ArrayList<ExamQuestion>(count)
        var attempts = 0
        while (result.size < count && attempts < count * 16) {
            attempts++
            val target = pool[rng.nextInt(pool.size)]
            if (target.id in usedIds) continue
            val stem = target.hangul.removeSuffix("다")
            val canCloze = stem.length >= 2 && target.exampleKo.contains(stem)
            val distractors = pickPlausibleVocabDistractors(pool, target, 3, rng, translationLang)
            if (distractors.size < 3) continue
            usedIds.add(target.id)

            val (tier, weight) = tierAndWeightFor(target, examType)
            val targetMeaning = target.meaningFor(translationLang)
            val targetExample = target.exampleFor(translationLang)
            if (canCloze && result.size % 2 == 0) {
                val tokenInSentence = target.exampleKo
                    .split(" ")
                    .firstOrNull { it.contains(stem) }
                    ?.trimEnd('.', '?', '!', ',')
                    ?: target.hangul
                val blanked = target.exampleKo.replaceFirst(tokenInSentence, "( ____ )")
                val opts = (distractors.map { it.hangul } + tokenInSentence).distinct().shuffled(rng)
                if (opts.size < 4) continue
                val correctIdx = opts.indexOf(tokenInSentence)
                result.add(
                    ExamQuestion(
                        id = "dyn_${examType.name}_rc_${result.size}_${target.id}",
                        examType = examType,
                        section = "Reading",
                        instructionKo = "빈칸에 들어갈 가장 알맞은 것을 고르십시오. (${weight}점)",
                        stimulusKo = "$blanked\n($targetExample)",
                        options = opts,
                        correctOptionIndex = correctIdx,
                        explanationEn = if (isId) {
                            "'${target.hangul}' ($targetMeaning) melengkapi kalimat dengan tepat: ${target.exampleKo}"
                        } else {
                            "'${target.hangul}' ($targetMeaning) completes the sentence accurately: ${target.exampleKo}"
                        },
                        pointWeight = weight,
                        difficultyTier = tier
                    )
                )
            } else {
                val opts = (distractors.map { it.hangul } + target.hangul).distinct().shuffled(rng)
                if (opts.size < 4) continue
                val correctIdx = opts.indexOf(target.hangul)
                result.add(
                    ExamQuestion(
                        id = "dyn_${examType.name}_rv_${result.size}_${target.id}",
                        examType = examType,
                        section = "Reading",
                        instructionKo = "다음 설명에 해당하는 알맞은 한국어 어휘를 고르십시오. (${weight}점)",
                        stimulusKo = "\"$targetMeaning\" (${target.partOfSpeech} • ${target.subCategory})",
                        options = opts,
                        correctOptionIndex = correctIdx,
                        explanationEn = if (isId) {
                            "${target.hangul} (${target.romanization}) berarti '$targetMeaning'. ${target.exampleKo}"
                        } else {
                            "${target.hangul} (${target.romanization}) means '$targetMeaning'. ${target.exampleKo}"
                        },
                        pointWeight = weight,
                        difficultyTier = tier
                    )
                )
            }
        }
        return result
    }

    private fun buildDynamicGrammarReadingItems(
        examType: ExamType,
        count: Int,
        rng: Random,
        translationLang: String
    ): List<ExamQuestion> {
        if (count <= 0) return emptyList()
        val isId = translationLang.equals("ID", ignoreCase = true)
        val matching = KoreanGrammarSeed.allPatterns.filter { g ->
            val badge = g.levelBadge.uppercase()
            when (examType) {
                ExamType.TOPIK_I, ExamType.EPS_TOPIK ->
                    badge.contains("TOPIK I") && !badge.contains("TOPIK II") || badge.contains("BEGINNER") || badge.contains("EPS")
                ExamType.TOPIK_II ->
                    badge.contains("TOPIK II") || badge.contains("INTERMEDIATE") || badge.contains("ADVANCED")
                ExamType.TOPIK_SPEAKING -> true
            }
        }.ifEmpty { KoreanGrammarSeed.allPatterns }

        return matching.shuffled(rng).take(count).mapIndexed { idx, g ->
            val opts = (g.quizDistractors + g.quizCorrectOption).distinct().shuffled(rng)
            val correctIdx = opts.indexOf(g.quizCorrectOption)
            val tier = if (examType == ExamType.TOPIK_II) 3 else 2
            val weight = if (tier == 3) 4 else 3
            ExamQuestion(
                id = "dyn_${examType.name}_gr_${idx}_${g.id}",
                examType = examType,
                section = "Reading",
                instructionKo = "문맥에 맞는 알맞은 문법 표현을 고르십시오. (${weight}점)",
                stimulusKo = g.quizStem,
                options = opts,
                correctOptionIndex = correctIdx,
                explanationEn = if (isId) {
                    "${g.title} (${g.structureFormula}): ${g.summaryId}"
                } else {
                    "${g.title} (${g.structureFormula}): ${g.quizExplanation}"
                },
                pointWeight = weight,
                difficultyTier = tier
            )
        }
    }

    private fun pickPlausibleVocabDistractors(
        pool: List<VocabEntity>,
        target: VocabEntity,
        count: Int,
        rng: Random,
        translationLang: String
    ): List<VocabEntity> {
        val picked = ArrayList<VocabEntity>(count)
        val seenHangul = hashSetOf(target.hangul)
        val seenMeaning = hashSetOf(target.meaningFor(translationLang))
        var attempts = 0
        while (picked.size < count && attempts < 45) {
            attempts++
            val c = pool[rng.nextInt(pool.size)]
            val cMeaning = c.meaningFor(translationLang)
            if (c.partOfSpeech == target.partOfSpeech &&
                abs(c.hangul.length - target.hangul.length) <= 1 &&
                c.hangul !in seenHangul && cMeaning !in seenMeaning
            ) {
                seenHangul.add(c.hangul)
                seenMeaning.add(cMeaning)
                picked.add(c)
            }
        }
        attempts = 0
        while (picked.size < count && attempts < 45) {
            attempts++
            val c = pool[rng.nextInt(pool.size)]
            val cMeaning = c.meaningFor(translationLang)
            if (c.hangul !in seenHangul && cMeaning !in seenMeaning) {
                seenHangul.add(c.hangul)
                seenMeaning.add(cMeaning)
                picked.add(c)
            }
        }
        return picked
    }

    private fun tierAndWeightFor(vocab: VocabEntity, examType: ExamType): Pair<Int, Int> {
        return when (examType) {
            ExamType.TOPIK_I, ExamType.EPS_TOPIK -> when (vocab.subCategory) {
                "1급", "Daily communication" -> 1 to 2
                "2급", "Manufacturing", "Workplace safety" -> 2 to 3
                else -> 2 to 3
            }
            ExamType.TOPIK_II -> when (vocab.subCategory) {
                "3급" -> 1 to 2
                "4급" -> 2 to 3
                "5급", "6급" -> 3 to 4
                else -> 2 to 3
            }
            ExamType.TOPIK_SPEAKING -> 2 to 3
        }
    }

    /**
     * Weighted, idempotent scoring calculator following each exam's verified structure:
     * - Multiple-choice items weighted by authentic pointWeight (2점/3점/4점).
     * - Open-ended Writing & Speaking items graded via the 4-axis Korean Linguistic Rubric.
     * - Produces section breakdown, difficulty tier accuracy, pacing diagnostics, letter grade,
     *   and actionable recommendations while distinguishing practice estimates from official certificates.
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
                    val register = if (session.examType == ExamType.TOPIK_II) {
                        KoreanRegisterTarget.FORMAL_WRITTEN
                    } else {
                        KoreanRegisterTarget.POLITE_EVERYDAY
                    }
                    val targetMinChars = if (session.examType == ExamType.TOPIK_II) 45 else 25
                    val rubric = LearningScoringEngine.evaluateKoreanOpenResponse(
                        text = text,
                        expectedKeywords = q.expectedKeywords,
                        targetMinHangul = targetMinChars,
                        expectedRegister = register
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
        val totalPct = ((totalScore.toDouble() / session.examType.maxScore.coerceAtLeast(1)) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
        val rawCorrect = reviews.count { it.isCorrect }
        val accuracyPct = ((rawCorrect.toDouble() / reviews.size.coerceAtLeast(1)) * 100.0).roundToInt()

        val elapsedSec = ((finishedAtEpochMs - session.startedAtEpochMs) / 1000L).toInt().coerceAtLeast(1)
        val avgSecPerQ = (elapsedSec / reviews.size.coerceAtLeast(1)).coerceAtLeast(1)
        val targetSecPerQ = (session.examType.mockDurationMinutes * 60) / reviews.size.coerceAtLeast(1)
        val pacingNote = when {
            avgSecPerQ <= (targetSecPerQ * 0.45).toInt() && accuracyPct < 70 ->
                if (isId) "Tempo terlalu cepat (${avgSecPerQ}dtk/soal) dengan kesalahan yang bisa dihindari — periksa kembali pola tata bahasa."
                else "Fast pace (${avgSecPerQ}s/q) with avoidable errors — slow down and re-check grammar stems."
            avgSecPerQ > targetSecPerQ ->
                if (isId) "Lebih lambat dari target waktu ujian (${avgSecPerQ}dtk/soal vs target ${targetSecPerQ}dtk) — latih membaca cepat."
                else "Slower than target exam pace (${avgSecPerQ}s/q vs ${targetSecPerQ}s target) — practice timed skimming."
            else ->
                if (isId) "Pengaturan tempo ujian sangat baik (${avgSecPerQ}dtk/soal)."
                else "Well-controlled exam pacing (${avgSecPerQ}s/question)."
        }

        // Difficulty tier breakdown
        val tierNames = if (isId) {
            mapOf(1 to "Dasar (2점)", 2 to "Menengah (3점)", 3 to "Lanjutan (4점)")
        } else {
            mapOf(1 to "Foundation (2점)", 2 to "Intermediate (3점)", 3 to "Advanced (4점)")
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
                "Terkuat: ${strongest.sectionName} (${strongest.earned}/${strongest.max}). Fokus prioritas: ${weakest.sectionName} (${weakest.earned}/${weakest.max}). $pacingNote"
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
                "Tingkatkan skor ${weakest.sectionName} (${weakest.earned}/${weakest.max}) dengan Kuis Pintar & Kartu FSRS."
            } else {
                "Boost ${weakest.sectionName} score (${weakest.earned}/${weakest.max}) using targeted Smart Quiz & Flashcards."
            }
        }
        val advTier = tierBreakdowns.firstOrNull { it.tierLabel.contains("Advanced") || it.tierLabel.contains("Lanjutan") }
        if (advTier != null && advTier.percent < 65) {
            recommendations += if (isId) {
                "Soal tingkat lanjut 4점 cukup menantang (${advTier.percent}%) — pelajari kosakata 4급–6급 & konektor di Studio Tata Bahasa."
            } else {
                "Advanced 4점 questions were challenging (${advTier.percent}%) — study 4급–6급 vocabulary & Grammar Studio connectors."
            }
        }
        val weakOpen = reviews.firstOrNull { it.rubricScore != null && !it.isCorrect }?.rubricScore
        if (weakOpen != null) {
            recommendations += if (isId) "Tips Menulis/Berbicara: ${weakOpen.feedback}" else "Writing/Speaking tip: ${weakOpen.feedback}"
        }
        if (recommendations.isEmpty()) {
            recommendations += if (isId) {
                "Kesiapan ujian sangat baik! Tantang dirimu ke level TOPIK berikutnya atau mode Simulasi berwaktu."
            } else {
                "Strong exam readiness! Challenge yourself with the next TOPIK level or timed Mock mode."
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
            recommendations = recommendations
        )
    }

    private fun estimatePracticeBand(examType: ExamType, totalScore: Int, isId: Boolean): String = when (examType) {
        ExamType.TOPIK_I -> when {
            totalScore >= 140 -> if (isId) "Estimasi Level 2 (Latihan)" else "Estimated Level 2 (Practice)"
            totalScore >= 80 -> if (isId) "Estimasi Level 1 (Latihan)" else "Estimated Level 1 (Practice)"
            else -> if (isId) "Di bawah target Level 1 (Latihan)" else "Below Level 1 target (Practice)"
        }
        ExamType.TOPIK_II -> when {
            totalScore >= 230 -> if (isId) "Estimasi Level 6 (Latihan)" else "Estimated Level 6 (Practice)"
            totalScore >= 190 -> if (isId) "Estimasi Level 5 (Latihan)" else "Estimated Level 5 (Practice)"
            totalScore >= 150 -> if (isId) "Estimasi Level 4 (Latihan)" else "Estimated Level 4 (Practice)"
            totalScore >= 120 -> if (isId) "Estimasi Level 3 (Latihan)" else "Estimated Level 3 (Practice)"
            else -> if (isId) "Di bawah target Level 3 (Latihan)" else "Below Level 3 target (Practice)"
        }
        ExamType.EPS_TOPIK -> when {
            totalScore >= 140 -> if (isId) "Rentang Lulus Kompetitif (Latihan)" else "Competitive Pass Range (Practice)"
            totalScore >= 110 -> if (isId) "Rentang Batas Lulus (Latihan)" else "Borderline Pass Range (Practice)"
            else -> if (isId) "Perlu Latihan Lebih Lanjut" else "Needs More Practice"
        }
        ExamType.TOPIK_SPEAKING -> when {
            totalScore >= 160 -> if (isId) "Estimasi Menengah Atas–Mahir (Latihan)" else "Estimated High Intermediate–Advanced (Practice)"
            totalScore >= 110 -> if (isId) "Estimasi Menengah (Latihan)" else "Estimated Intermediate (Practice)"
            totalScore >= 60 -> if (isId) "Estimasi Pemula (Latihan)" else "Estimated Beginner (Practice)"
            else -> if (isId) "Berbicara Dasar (Latihan)" else "Foundational Speaking (Practice)"
        }
    }
}
