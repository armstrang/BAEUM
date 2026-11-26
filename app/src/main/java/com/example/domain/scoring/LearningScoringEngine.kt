package com.example.domain.scoring

import com.example.data.local.ExamAttemptEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SpeakingSessionEntity
import com.example.data.local.VocabEntity
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.hangul.HangulUnicodeEngine
import kotlin.math.min
import kotlin.math.roundToInt

enum class LetterGrade(val label: String, val summary: String) {
    S("S", "Mastery level"),
    A("A", "Strong proficiency"),
    B("B", "Solid progress"),
    C("C", "Building foundations"),
    D("D", "Needs focused review");

    companion object {
        fun fromScore(score0To100: Int): LetterGrade = when {
            score0To100 >= 92 -> S
            score0To100 >= 82 -> A
            score0To100 >= 70 -> B
            score0To100 >= 55 -> C
            else -> D
        }
    }
}

enum class KoreanRegisterTarget {
    FORMAL_WRITTEN, // TOPIK II essay: -(으)ㄴ/는다, -이다, -된다 (also accepts 합니다체)
    FORMAL_POLITE,  // 합니다체 (-ㅂ/습니다, -ㅂ/습니까)
    POLITE_EVERYDAY // 해요체 (-아요/어요, -세요) or 합니다체
}

data class OpenResponseRubricScore(
    val totalRatio: Double,       // 0.0..1.0
    val relevanceScore: Int,      // 0..100
    val completenessScore: Int,   // 0..100
    val grammarScore: Int,        // 0..100
    val registerScore: Int,       // 0..100
    val hangulCharCount: Int,
    val keywordMatches: Int,
    val feedback: String
)

data class JamoAnswerEvaluation(
    val isCorrect: Boolean,
    val similarity: Double,       // 0.0..1.0 Jamo-level similarity
    val isNearMiss: Boolean,
    val feedbackHint: String
)

data class FlashcardRatingOutcome(
    val vocabId: String,
    val hangul: String,
    val rating: FsrsRating,
    val stabilityBefore: Double,
    val stabilityAfter: Double,
    val lapsesAfter: Int,
    val stageBefore: String,
    val stageAfter: String
)

data class FlashcardSessionEvaluation(
    val totalReviews: Int,
    val againCount: Int,
    val hardCount: Int,
    val goodCount: Int,
    val easyCount: Int,
    val recallRatePercent: Int,
    val avgStabilityGainDays: Double,
    val newlyMasteredCount: Int,
    val leechWarnings: List<String>,
    val masteryScore: Int,
    val grade: LetterGrade,
    val avgSecondsPerCard: Int,
    val recommendation: String
)

data class QuizAnswerTelemetry(
    val questionId: String,
    val skillTag: String,
    val difficultyLevel: Int,
    val points: Int,
    val isCorrect: Boolean,
    val jamoSimilarity: Double = if (isCorrect) 1.0 else 0.0
)

data class SkillAccuracyBreakdown(
    val skillTag: String,
    val correct: Int,
    val total: Int,
    val accuracyPercent: Int
)

data class QuizSessionEvaluation(
    val totalQuestions: Int,
    val correctCount: Int,
    val rawAccuracyPercent: Int,
    val earnedPoints: Int,
    val maxPoints: Int,
    val weightedMasteryScore: Int, // 0..100 including difficulty weight & streak bonus
    val maxStreak: Int,
    val avgSecondsPerQuestion: Int,
    val grade: LetterGrade,
    val skillBreakdowns: List<SkillAccuracyBreakdown>,
    val weakestSkill: String?,
    val recommendation: String
)

data class LearnerReadinessProfile(
    val overallReadiness: Int,       // 0..100 composite index
    val vocabRetentionScore: Int,    // 0..100 from FSRS retrievability & mastery ratio
    val quizMasteryScore: Int,       // 0..100 from recent quiz accuracy
    val examReadinessScore: Int,     // 0..100 from mock exam scores
    val speakingFluencyScore: Int,   // 0..100 from speaking evaluations
    val estimatedPracticeTier: String,
    val leechCount: Int,
    val avgRetrievabilityPercent: Int,
    val nextRecommendedFocus: String
)

/**
 * Unified, transparent scoring & evaluation engine for Baeum across:
 * 1. Open-ended Korean Writing & Speaking (4-axis linguistic rubric)
 * 2. Korean Typing / Hangul Jamo similarity evaluation
 * 3. Flashcards FSRS session quality & retention scoring
 * 4. Adaptive Quiz difficulty-weighted & streak-aware scoring
 * 5. Holistic Learner Readiness Index
 */
object LearningScoringEngine {

    private val KOREAN_PARTICLES = listOf(
        "은", "는", "이", "가", "을", "를", "에", "에서", "으로", "로", "도", "의", "와", "과", "하고", "부터", "까지", "보다"
    )

    private val KOREAN_CONNECTORS = listOf(
        "어서", "아서", "해서", "지만", "는데", "으면", "면", "기 때문", "때문에", "므로", "면서",
        "고", "거나", "도록", "려고", "위해", "통해", "따라", "반면", "그러나", "따라서", "또한", "그래서"
    )

    /**
     * 4-Axis Korean Open-Response Rubric used by TOPIK II Writing and TOPIK Speaking exams:
     * - Relevance & Keyword Coverage (35%)
     * - Completeness & Substantive Hangul Length (25%)
     * - Grammar, Particles & Discourse Cohesion (25%)
     * - Speech Register & Style Consistency (15%)
     *
     * Guards against non-Korean input and repeated-character spam.
     */
    fun evaluateKoreanOpenResponse(
        text: String,
        expectedKeywords: List<String>,
        targetMinHangul: Int = 35,
        expectedRegister: KoreanRegisterTarget = KoreanRegisterTarget.FORMAL_WRITTEN
    ): OpenResponseRubricScore {
        val cleaned = text.trim()
        if (cleaned.isBlank() || !HangulUnicodeEngine.containsHangul(cleaned)) {
            return OpenResponseRubricScore(
                totalRatio = 0.0,
                relevanceScore = 0,
                completenessScore = 0,
                grammarScore = 0,
                registerScore = 0,
                hangulCharCount = 0,
                keywordMatches = 0,
                feedback = "No Korean Hangul response detected. Write a complete Korean answer using target keywords."
            )
        }

        val hangulChars = cleaned.filter { it.code in 0xAC00..0xD7A3 }
        val hangulCount = hangulChars.length
        val uniqueHangulRatio = if (hangulCount > 0) {
            hangulChars.toSet().size.toDouble() / hangulCount.coerceAtMost(40).toDouble()
        } else 0.0

        // Anti-spam check: detect repeated single-syllable gibberish (e.g., "가가가가가가...")
        val spamPenalty = if (hangulCount >= 8 && uniqueHangulRatio < 0.28) 0.35 else 1.0

        // 1. Relevance & Keyword Coverage (0..100)
        val kwHits = expectedKeywords.count { kw -> kw.isNotBlank() && cleaned.contains(kw) }
        val kwTarget = expectedKeywords.size.coerceIn(1, 4)
        val relevanceRaw = if (expectedKeywords.isEmpty()) {
            ((hangulCount.toDouble() / targetMinHangul.coerceAtLeast(10)) * 85.0).coerceIn(20.0, 95.0)
        } else {
            val hitRatio = (kwHits.toDouble() / kwTarget.toDouble()).coerceIn(0.0, 1.0)
            val baseContext = if (hangulCount >= 12) 20.0 else 10.0
            (baseContext + hitRatio * 80.0).coerceIn(0.0, 100.0)
        }
        val relevanceScore = (relevanceRaw * spamPenalty).roundToInt().coerceIn(0, 100)

        // 2. Completeness & Length (0..100)
        val wordTokens = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        val lengthRatio = (hangulCount.toDouble() / targetMinHangul.coerceAtLeast(15)).coerceIn(0.0, 1.15)
        val sentenceCount = cleaned.split(Regex("[.?!。]+")).count { it.trim().length >= 3 }
        val completenessRaw = (min(1.0, lengthRatio) * 75.0 +
            min(1.0, wordTokens.size / 6.0) * 15.0 +
            min(1.0, sentenceCount / 2.0) * 10.0)
        val completenessScore = (completenessRaw * spamPenalty).roundToInt().coerceIn(0, 100)

        // 3. Grammar, Particles & Cohesion (0..100)
        val particleHits = KOREAN_PARTICLES.count { cleaned.contains(it) }
        val connectorHits = KOREAN_CONNECTORS.count { cleaned.contains(it) }
        val grammarRaw = (
            min(1.0, particleHits / 3.0) * 55.0 +
            min(1.0, connectorHits / 2.0) * 45.0
        )
        val grammarScore = (grammarRaw * spamPenalty).roundToInt().coerceIn(0, 100)

        // 4. Register & Style Appropriateness (0..100)
        val hasWrittenPlain = listOf("는다", "ㄴ다", "이다", "된다", "있다", "없다", "한다", "겠다", "필요하다", "중요하다")
            .any { cleaned.contains(it) }
        val hasFormalPolite = listOf("습니다", "습니까", "입니다", "드립니다", "겠습니다")
            .any { cleaned.contains(it) }
        val hasPolite = listOf("아요", "어요", "해요", "예요", "이에요", "세요", "주세요")
            .any { cleaned.contains(it) }

        val registerRaw = when (expectedRegister) {
            KoreanRegisterTarget.FORMAL_WRITTEN -> when {
                hasWrittenPlain && !hasPolite -> 100.0
                hasWrittenPlain || hasFormalPolite -> 88.0
                hasPolite -> 72.0
                else -> 40.0
            }
            KoreanRegisterTarget.FORMAL_POLITE -> when {
                hasFormalPolite -> 100.0
                hasPolite -> 80.0
                else -> 45.0
            }
            KoreanRegisterTarget.POLITE_EVERYDAY -> when {
                hasPolite || hasFormalPolite -> 100.0
                hasWrittenPlain -> 70.0
                else -> 45.0
            }
        }
        val registerScore = (registerRaw * spamPenalty).roundToInt().coerceIn(0, 100)

        val weighted = (
            relevanceScore * 0.35 +
            completenessScore * 0.25 +
            grammarScore * 0.25 +
            registerScore * 0.15
        ) / 100.0

        val feedbackParts = mutableListOf<String>()
        if (kwHits == 0 && expectedKeywords.isNotEmpty()) {
            feedbackParts += "Include topic keywords (${expectedKeywords.take(3).joinToString(", ")})."
        } else if (kwHits >= kwTarget) {
            feedbackParts += "Strong topic keyword coverage ($kwHits/$kwTarget)."
        }
        if (hangulCount < targetMinHangul) {
            feedbackParts += "Expand response length ($hangulCount/$targetMinHangul Hangul syllables target)."
        }
        if (connectorHits == 0) {
            feedbackParts += "Add Korean connectors (-지만, -기 때문에, -아/어서, 따라서) for cohesion."
        }
        if (registerScore < 75) {
            feedbackParts += when (expectedRegister) {
                KoreanRegisterTarget.FORMAL_WRITTEN -> "Use formal written endings (-는다/ㄴ다 or -ㅂ/습니다) for TOPIK II writing."
                KoreanRegisterTarget.FORMAL_POLITE -> "Use formal polite endings (-ㅂ/습니다)."
                KoreanRegisterTarget.POLITE_EVERYDAY -> "Use polite endings (-아요/어요 or -ㅂ/습니다)."
            }
        }
        if (feedbackParts.isEmpty()) {
            feedbackParts += "Well-structured Korean response with balanced vocabulary, grammar, and register."
        }

        return OpenResponseRubricScore(
            totalRatio = weighted.coerceIn(0.0, 1.0),
            relevanceScore = relevanceScore,
            completenessScore = completenessScore,
            grammarScore = grammarScore,
            registerScore = registerScore,
            hangulCharCount = hangulCount,
            keywordMatches = kwHits,
            feedback = feedbackParts.joinToString(" ")
        )
    }

    /**
     * Evaluates Korean typed answers using Unicode Hangul Jamo decomposition + Levenshtein similarity.
     * Exact match returns isCorrect = true. Near misses (>= 0.80 Jamo match) get specific diagnostic hints.
     */
    fun evaluateKoreanTypingAnswer(userInput: String, targetHangul: String): JamoAnswerEvaluation {
        val normUser = normalizeKoreanAnswer(userInput)
        val normTarget = normalizeKoreanAnswer(targetHangul)

        if (normUser.isEmpty()) {
            return JamoAnswerEvaluation(
                isCorrect = false,
                similarity = 0.0,
                isNearMiss = false,
                feedbackHint = "Enter your Korean answer in Hangul."
            )
        }
        if (normUser.equals(normTarget, ignoreCase = true)) {
            return JamoAnswerEvaluation(
                isCorrect = true,
                similarity = 1.0,
                isNearMiss = false,
                feedbackHint = "Exact Hangul spelling match!"
            )
        }

        val userJamo = decomposeToJamoString(normUser)
        val targetJamo = decomposeToJamoString(normTarget)
        val dist = levenshteinDistance(userJamo, targetJamo)
        val maxLen = maxOf(userJamo.length, targetJamo.length, 1)
        val similarity = (1.0 - dist.toDouble() / maxLen.toDouble()).coerceIn(0.0, 1.0)
        val isNearMiss = similarity >= 0.78

        val pct = (similarity * 100.0).roundToInt()
        val hint = when {
            isNearMiss -> "Very close ($pct% Jamo match)! Check your vowel or batchim spelling carefully."
            similarity >= 0.50 -> "Partial match ($pct% Jamo similarity). Compare each syllable block with '$targetHangul'."
            else -> "Review the syllable structure and spelling of '$targetHangul'."
        }

        return JamoAnswerEvaluation(
            isCorrect = false,
            similarity = similarity,
            isNearMiss = isNearMiss,
            feedbackHint = hint
        )
    }

    fun evaluateFlashcardSession(
        outcomes: List<FlashcardRatingOutcome>,
        elapsedSec: Int
    ): FlashcardSessionEvaluation {
        if (outcomes.isEmpty()) {
            return FlashcardSessionEvaluation(
                totalReviews = 0, againCount = 0, hardCount = 0, goodCount = 0, easyCount = 0,
                recallRatePercent = 0, avgStabilityGainDays = 0.0, newlyMasteredCount = 0,
                leechWarnings = emptyList(), masteryScore = 0, grade = LetterGrade.C,
                avgSecondsPerCard = 0, recommendation = "Start a Smart Flashcard set to build FSRS memory stability."
            )
        }

        val again = outcomes.count { it.rating == FsrsRating.AGAIN }
        val hard = outcomes.count { it.rating == FsrsRating.HARD }
        val good = outcomes.count { it.rating == FsrsRating.GOOD }
        val easy = outcomes.count { it.rating == FsrsRating.EASY }
        val total = outcomes.size

        val weightedRecall = (easy * 1.0 + good * 0.92 + hard * 0.55) / total.toDouble()
        val recallPct = (weightedRecall * 100.0).roundToInt().coerceIn(0, 100)
        val avgStabilityDelta = outcomes.map { it.stabilityAfter - it.stabilityBefore }.average()
        val newlyMastered = outcomes.count {
            it.stageBefore != CardMemoryStage.MASTERED.name && it.stageAfter == CardMemoryStage.MASTERED.name
        }
        val leeches = outcomes
            .filter { it.lapsesAfter >= 4 }
            .map { it.hangul }
            .distinct()

        val masteryScore = (
            recallPct * 0.85 +
            (if (again == 0) 15.0 else (1.0 - again.toDouble() / total) * 10.0)
        ).roundToInt().coerceIn(0, 100)

        val grade = LetterGrade.fromScore(masteryScore)
        val avgSec = (elapsedSec.coerceAtLeast(1) / total).coerceAtLeast(1)

        val recommendation = when {
            leeches.isNotEmpty() -> "Leech alert (${leeches.take(3).joinToString(", ")}): study their example sentences & Hanja roots."
            again >= (total + 2) / 3 -> "High lapse rate — reinforce missed words with a Weak-Items Smart Quiz."
            recallPct >= 88 -> "Excellent retention! Ready to introduce new vocabulary from the next TOPIK tier."
            else -> "Steady FSRS progress. Return tomorrow for your scheduled due reviews."
        }

        return FlashcardSessionEvaluation(
            totalReviews = total,
            againCount = again,
            hardCount = hard,
            goodCount = good,
            easyCount = easy,
            recallRatePercent = recallPct,
            avgStabilityGainDays = avgStabilityDelta,
            newlyMasteredCount = newlyMastered,
            leechWarnings = leeches,
            masteryScore = masteryScore,
            grade = grade,
            avgSecondsPerCard = avgSec,
            recommendation = recommendation
        )
    }

    fun evaluateQuizSession(
        answers: List<QuizAnswerTelemetry>,
        elapsedSec: Int
    ): QuizSessionEvaluation {
        if (answers.isEmpty()) {
            return QuizSessionEvaluation(
                totalQuestions = 0, correctCount = 0, rawAccuracyPercent = 0,
                earnedPoints = 0, maxPoints = 0, weightedMasteryScore = 0,
                maxStreak = 0, avgSecondsPerQuestion = 0, grade = LetterGrade.C,
                skillBreakdowns = emptyList(), weakestSkill = null,
                recommendation = "Complete a quiz session to generate skill diagnostics."
            )
        }

        val total = answers.size
        val correct = answers.count { it.isCorrect }
        val rawPct = ((correct.toDouble() / total) * 100.0).roundToInt().coerceIn(0, 100)

        val maxPts = answers.sumOf { it.points }.coerceAtLeast(1)
        val earnedPts = answers.sumOf { ans ->
            when {
                ans.isCorrect -> ans.points
                ans.jamoSimilarity >= 0.80 -> (ans.points * 0.25).roundToInt() // Small diagnostic credit on near-miss typing
                else -> 0
            }
        }

        var streak = 0
        var maxStreak = 0
        for (a in answers) {
            if (a.isCorrect) {
                streak++
                if (streak > maxStreak) maxStreak = streak
            } else {
                streak = 0
            }
        }

        val pointRatio = (earnedPts.toDouble() / maxPts.toDouble()).coerceIn(0.0, 1.0)
        val streakBonus = ((maxStreak.toDouble() / total.toDouble()) * 8.0).coerceAtMost(8.0)
        val weightedScore = (pointRatio * 92.0 + streakBonus).roundToInt().coerceIn(0, 100)
        val grade = LetterGrade.fromScore(weightedScore)
        val avgSec = (elapsedSec.coerceAtLeast(1) / total).coerceAtLeast(1)

        val skillBreakdowns = answers.groupBy { it.skillTag }.map { (tag, items) ->
            val ok = items.count { it.isCorrect }
            SkillAccuracyBreakdown(
                skillTag = tag,
                correct = ok,
                total = items.size,
                accuracyPercent = ((ok.toDouble() / items.size) * 100.0).roundToInt()
            )
        }.sortedBy { it.accuracyPercent }

        val weakest = skillBreakdowns.firstOrNull { it.accuracyPercent < 80 }?.skillTag

        val recommendation = when {
            weakest != null -> "Focus next on $weakest — missed cards are automatically prioritized in your Smart Flashcards."
            rawPct == 100 -> "Flawless run! Try a Mock Exam or step up to higher TOPIK levels."
            rawPct >= 80 -> "Strong recall accuracy ($rawPct%). Challenge yourself with Writing or Conjugation mode."
            else -> "Review missed items in Flashcards, then re-run Weak Items Mixed Quiz."
        }

        return QuizSessionEvaluation(
            totalQuestions = total,
            correctCount = correct,
            rawAccuracyPercent = rawPct,
            earnedPoints = earnedPts,
            maxPoints = maxPts,
            weightedMasteryScore = weightedScore,
            maxStreak = maxStreak,
            avgSecondsPerQuestion = avgSec,
            grade = grade,
            skillBreakdowns = skillBreakdowns,
            weakestSkill = weakest,
            recommendation = recommendation
        )
    }

    /**
     * Computes a holistic, strictly data-grounded Learner Readiness Profile from real Room records.
     */
    fun computeReadinessProfile(
        vocab: List<VocabEntity>,
        quizRecords: List<QuizRecordEntity>,
        examAttempts: List<ExamAttemptEntity>,
        speakingSessions: List<SpeakingSessionEntity>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): LearnerReadinessProfile {
        val reviewed = vocab.filter { it.reps > 0 }
        val leechCount = vocab.count { FsrsScheduler.isLeech(it.toFsrsSnapshot()) || it.quizWrongCount >= 4 }

        val avgRetPct = if (reviewed.isNotEmpty()) {
            (reviewed.map { FsrsScheduler.currentRetrievability(it.toFsrsSnapshot(), nowEpochMs) }.average() * 100.0)
                .roundToInt()
                .coerceIn(0, 100)
        } else 0

        val mastered = vocab.count { it.stage == CardMemoryStage.MASTERED.name }
        val vocabCoverageBonus = ((reviewed.size.toDouble() / 120.0).coerceAtMost(1.0) * 35.0) +
            ((mastered.toDouble() / 60.0).coerceAtMost(1.0) * 20.0)
        val vocabRetentionScore = if (reviewed.isEmpty()) 0 else {
            (avgRetPct * 0.45 + vocabCoverageBonus).roundToInt().coerceIn(0, 100)
        }

        val recentQuizzes = quizRecords.take(10)
        val quizScore = if (recentQuizzes.isNotEmpty()) {
            val totQ = recentQuizzes.sumOf { it.totalQuestions }.coerceAtLeast(1)
            val totC = recentQuizzes.sumOf { it.correctAnswers }
            ((totC.toDouble() / totQ) * 100.0).roundToInt().coerceIn(0, 100)
        } else 0

        val recentExams = examAttempts.take(5)
        val examScore = if (recentExams.isNotEmpty()) {
            recentExams.map { (it.totalScore.toDouble() / it.maxScore.coerceAtLeast(1)) * 100.0 }
                .average().roundToInt().coerceIn(0, 100)
        } else 0

        val recentSpeaking = speakingSessions.take(5)
        val speakingScore = if (recentSpeaking.isNotEmpty()) {
            recentSpeaking.map { it.overallScore }.average().roundToInt().coerceIn(0, 100)
        } else 0

        // Dynamically weight active pillars so beginners with only flashcards/quizzes still get an honest score
        var weightSum = 0.0
        var weightedTotal = 0.0
        if (reviewed.isNotEmpty()) { weightedTotal += vocabRetentionScore * 0.35; weightSum += 0.35 }
        if (recentQuizzes.isNotEmpty()) { weightedTotal += quizScore * 0.25; weightSum += 0.25 }
        if (recentExams.isNotEmpty()) { weightedTotal += examScore * 0.25; weightSum += 0.25 }
        if (recentSpeaking.isNotEmpty()) { weightedTotal += speakingScore * 0.15; weightSum += 0.15 }

        val overall = if (weightSum > 0.0) {
            // Scale slightly by pillar breadth (0.70..1.0) so all 4 pillars unlock 100
            val breadthFactor = 0.70 + (weightSum * 0.30)
            ((weightedTotal / weightSum) * breadthFactor).roundToInt().coerceIn(0, 100)
        } else 0

        val tier = when {
            overall >= 85 && mastered >= 80 -> "TOPIK II Advanced Track (5–6급)"
            overall >= 70 && mastered >= 35 -> "TOPIK II Intermediate Track (3–4급)"
            overall >= 50 && reviewed.size >= 20 -> "TOPIK I Upper Beginner (2급)"
            overall >= 20 -> "TOPIK I Foundation (1급)"
            else -> "Getting Started (Hangul & 1급)"
        }

        val nextAction = when {
            reviewed.isEmpty() -> "Complete your first Smart Flashcard session to initialize FSRS tracking."
            leechCount >= 3 -> "Clear $leechCount difficult leech word(s) using Weak Items Quiz."
            recentQuizzes.isEmpty() -> "Take a Smart Quiz to benchmark active vocabulary recall."
            recentExams.isEmpty() -> "Try a timed TOPIK I or EPS-TOPIK Mock Exam for baseline scoring."
            recentSpeaking.isEmpty() -> "Practice a Speaking scenario to activate communication scoring."
            examScore < quizScore - 15 -> "Focus on timed Mock Exam pacing and Reading/Listening comprehension."
            else -> "Maintain your daily FSRS reviews and step up to the next TOPIK level."
        }

        return LearnerReadinessProfile(
            overallReadiness = overall,
            vocabRetentionScore = vocabRetentionScore,
            quizMasteryScore = quizScore,
            examReadinessScore = examScore,
            speakingFluencyScore = speakingScore,
            estimatedPracticeTier = tier,
            leechCount = leechCount,
            avgRetrievabilityPercent = avgRetPct,
            nextRecommendedFocus = nextAction
        )
    }

    private fun normalizeKoreanAnswer(input: String): String =
        input.trim().replace(Regex("[.?!,~\"'\\s]+"), "")

    private fun decomposeToJamoString(text: String): String {
        val sb = StringBuilder(text.length * 3)
        for (ch in text) {
            val parts = HangulUnicodeEngine.decomposeSyllable(ch)
            if (parts != null) {
                sb.append(parts.first)
                sb.append(parts.second)
                if (parts.third != null) sb.append(parts.third)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,
                    prev[j] + 1,
                    prev[j - 1] + cost
                )
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[b.length]
    }
}
