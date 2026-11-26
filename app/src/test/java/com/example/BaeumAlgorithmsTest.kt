package com.example

import com.example.data.local.ExamAttemptEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SpeakingSessionEntity
import com.example.data.seed.ExamType
import com.example.data.seed.VocabAssetLoader
import com.example.domain.exam.ExamEngine
import com.example.domain.exam.ExamSessionMode
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FlashcardSessionAlgorithm
import com.example.domain.fsrs.FsrsCardSnapshot
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.quiz.AdaptiveQuizEngine
import com.example.domain.quiz.QuizMode
import com.example.domain.scoring.FlashcardRatingOutcome
import com.example.domain.scoring.KoreanRegisterTarget
import com.example.domain.scoring.LearningScoringEngine
import com.example.domain.scoring.LetterGrade
import com.example.domain.scoring.QuizAnswerTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class BaeumAlgorithmsTest {

    private fun loadFullWordBank() = VocabAssetLoader.loadMergedVocabulary(
        topik1Stream = resolveAsset("vocab/topik_1.tsv").inputStream(),
        topik2Stream = resolveAsset("vocab/topik_2.tsv").inputStream()
    )

    private fun resolveAsset(rel: String): File =
        listOf(File("src/main/assets/$rel"), File("app/src/main/assets/$rel"))
            .first { it.exists() }

    @Test
    fun flashcardAlgorithmPrioritizesUrgentAndWeakCardsWithInterleavingAndCappedRequeue() {
        val now = 1_750_000_000_000L
        val dayMs = 86_400_000L
        val bank = loadFullWordBank().take(60)

        // Create 3 overdue/weak cards and keep rest as NEW
        val veryUrgent = bank[0].copy(
            reps = 3, lapses = 4, stability = 2.0, difficulty = 7.5,
            scheduledDays = 2.0, lastReviewEpochMs = now - 10 * dayMs,
            dueEpochMs = now - 8 * dayMs, stage = CardMemoryStage.REVIEW.name,
            quizWrongCount = 4
        )
        val mildDue = bank[1].copy(
            reps = 2, lapses = 0, stability = 6.0, difficulty = 5.0,
            scheduledDays = 5.0, lastReviewEpochMs = now - 5 * dayMs,
            dueEpochMs = now - 1000L, stage = CardMemoryStage.REVIEW.name
        )
        val quizWeakNotDue = bank[2].copy(
            reps = 1, lapses = 1, stability = 10.0, difficulty = 6.0,
            scheduledDays = 10.0, lastReviewEpochMs = now - dayMs,
            dueEpochMs = now + 9 * dayMs, stage = CardMemoryStage.REVIEW.name,
            quizWrongCount = 3
        )
        val pool = listOf(veryUrgent, mildDue, quizWeakNotDue) + bank.drop(3)

        // Urgency score must rank the overdue leech higher than the mild due card
        assertTrue(
            FlashcardSessionAlgorithm.computeReviewUrgency(veryUrgent, now) >
                FlashcardSessionAlgorithm.computeReviewUrgency(mildDue, now)
        )
        assertTrue(FsrsScheduler.isLeech(veryUrgent.toFsrsSnapshot()))

        // Adaptive deck must put urgent cards first AND interleave new cards
        val deck = FlashcardSessionAlgorithm.buildAdaptiveDeck(
            pool = pool,
            dailyGoal = 20,
            dueOnly = false,
            nowEpochMs = now,
            maxSessionSize = 15,
            rng = Random(42)
        )
        assertEquals(15, deck.size)
        assertEquals(veryUrgent.id, deck.first().id)
        assertTrue("Expected interleaved new cards in deck", deck.any { it.reps == 0 })

        // Intra-session AGAIN requeue is strictly capped (max 3 total occurrences -> never infinite loop)
        var queue = deck
        repeat(5) {
            queue = FlashcardSessionAlgorithm.applyIntraSessionRequeue(
                currentQueue = queue,
                currentIndex = 0,
                updatedCard = veryUrgent,
                rating = FsrsRating.AGAIN,
                wasFirstExposure = false
            )
        }
        assertTrue("Requeue cap violated", queue.count { it.id == veryUrgent.id } <= 3)

        // FSRS-5 short-term same-day review stability updates smoothly without NaN or zero
        val sameDay = FsrsScheduler.schedule(
            card = FsrsCardSnapshot(
                stability = 3.0, difficulty = 5.0, reps = 1,
                stage = CardMemoryStage.LEARNING, lastReviewEpochMs = now - 600_000L,
                dueEpochMs = now
            ),
            rating = FsrsRating.GOOD,
            nowEpochMs = now
        )
        assertTrue(sameDay.nextState.stability > 3.0)

        // Session evaluation catches leech warnings & computes recall grade
        val eval = LearningScoringEngine.evaluateFlashcardSession(
            outcomes = listOf(
                FlashcardRatingOutcome(veryUrgent.id, veryUrgent.hangul, FsrsRating.AGAIN, 2.0, 0.8, 5, "REVIEW", "LEARNING"),
                FlashcardRatingOutcome(mildDue.id, mildDue.hangul, FsrsRating.GOOD, 6.0, 14.0, 0, "REVIEW", "REVIEW"),
                FlashcardRatingOutcome("c3", "학교", FsrsRating.EASY, 12.0, 26.0, 0, "REVIEW", "MASTERED")
            ),
            elapsedSec = 45
        )
        assertEquals(3, eval.totalReviews)
        assertEquals(1, eval.newlyMasteredCount)
        assertTrue(veryUrgent.hangul in eval.leechWarnings)
    }

    @Test
    fun quizAlgorithmScaffoldsDifficultyEvaluatesJamoTypingAndComputesWeightedGrade() {
        val bank = loadFullWordBank()

        // Jamo-level typing evaluator catches exact matches and close Korean typos
        val exact = LearningScoringEngine.evaluateKoreanTypingAnswer("학교!", "학교")
        assertTrue(exact.isCorrect)
        assertEquals(1.0, exact.similarity, 0.001)

        val nearMiss = LearningScoringEngine.evaluateKoreanTypingAnswer("학고", "학교")
        assertFalse(nearMiss.isCorrect)
        assertTrue("Expected near-miss Jamo similarity >= 0.78, got ${nearMiss.similarity}", nearMiss.isNearMiss)
        assertTrue(nearMiss.feedbackHint.contains("Jamo"))

        val farOff = LearningScoringEngine.evaluateKoreanTypingAnswer("바다", "도서관")
        assertFalse(farOff.isNearMiss)

        // Quiz scaffolding sorts difficulty ascending & assigns weighted points
        val recallQs = AdaptiveQuizEngine.generateQuestions(
            mode = QuizMode.RECALL,
            allVocab = bank,
            translationLang = "EN",
            maxCount = 8
        )
        assertEquals(8, recallQs.size)
        for (i in 0 until recallQs.lastIndex) {
            assertTrue(recallQs[i].difficultyLevel <= recallQs[i + 1].difficultyLevel)
        }

        // Weighted session scoring rewards streaks & difficulty points
        val telemetry = listOf(
            QuizAnswerTelemetry("q1", "Vocabulary Recall", 1, 10, true),
            QuizAnswerTelemetry("q2", "Vocabulary Recall", 2, 15, true),
            QuizAnswerTelemetry("q3", "Grammar", 3, 20, true),
            QuizAnswerTelemetry("q4", "Grammar", 3, 20, false)
        )
        val eval = LearningScoringEngine.evaluateQuizSession(telemetry, elapsedSec = 60)
        assertEquals(75, eval.rawAccuracyPercent)
        assertEquals(3, eval.maxStreak)
        assertEquals(45, eval.earnedPoints)
        assertEquals(65, eval.maxPoints)
        assertEquals("Grammar", eval.weakestSkill)
        assertTrue(eval.weightedMasteryScore in 65..85)
    }

    @Test
    fun mockExamGeneratorSynthesizesWeightedPapersAndGradesWithFourAxisRubric() {
        val bank = loadFullWordBank()

        // Dynamic TOPIK I Mock Paper: 20 questions (10 Listening + 10 Reading)
        val topik1A = ExamEngine.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK, bank, Random(101))
        val topik1B = ExamEngine.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK, bank, Random(202))
        assertEquals(20, topik1A.questions.size)
        assertEquals(10, topik1A.questions.count { it.section == "Listening" })
        assertEquals(10, topik1A.questions.count { it.section == "Reading" })
        assertNotEquals("Different mock attempts should generate fresh questions", topik1A.questions.map { it.id }, topik1B.questions.map { it.id })

        // Dynamic TOPIK II Mock Paper: 8 Listening + 10 Reading + 2 Writing = 20 questions
        val topik2 = ExamEngine.startExam(ExamType.TOPIK_II, ExamSessionMode.MOCK, bank, Random(303))
        assertEquals(8, topik2.questions.count { it.section == "Listening" })
        assertEquals(10, topik2.questions.count { it.section == "Reading" })
        assertEquals(2, topik2.questions.count { it.section == "Writing" })

        // Dynamic EPS-TOPIK Mock Paper: 10 Listening + 10 Reading = 20 questions
        val eps = ExamEngine.startExam(ExamType.EPS_TOPIK, ExamSessionMode.MOCK, bank, Random(404))
        assertEquals(20, eps.questions.size)

        // 4-axis Korean Open-Response Rubric distinguishes real Korean essay from spam/blank
        val goodEssay = LearningScoringEngine.evaluateKoreanOpenResponse(
            text = "현대 사회에서 인공지능 기술은 빠르게 발전하고 있다. 하지만 개인 정보 보호와 일자리 변화 문제도 함께 나타나기 때문에 균형 있는 정책이 필요하다. 따라서 교육 제도를 개선하고 윤리 기준을 마련해야 한다.",
            expectedKeywords = listOf("기술", "문제", "정책", "필요"),
            targetMinHangul = 45,
            expectedRegister = KoreanRegisterTarget.FORMAL_WRITTEN
        )
        val spamEssay = LearningScoringEngine.evaluateKoreanOpenResponse(
            text = "가가가가가가가가가가가가가가가가가가가가가가가가가가가가가가가가가가가",
            expectedKeywords = listOf("기술", "문제", "정책", "필요"),
            targetMinHangul = 45,
            expectedRegister = KoreanRegisterTarget.FORMAL_WRITTEN
        )
        assertTrue("Good essay should score >= 0.80, got ${goodEssay.totalRatio}", goodEssay.totalRatio >= 0.80)
        assertTrue("Spam essay must be heavily penalized, got ${spamEssay.totalRatio}", spamEssay.totalRatio < 0.35)

        // Grade full TOPIK II Mock Exam with perfect MC + strong essay -> Level 6 (Practice)
        val perfectMc = topik2.questions.filter { !it.isOpenEnded }.associate { it.id to it.correctOptionIndex }
        val strongOpen = topik2.questions.filter { it.isOpenEnded }.associate { q ->
            q.id to ("현대 사회에서 ${q.expectedKeywords.joinToString("과 ")} 문제는 매우 중요하다. " +
                "그러나 여러 원인이 있기 때문에 사회 전체가 함께 노력해야 한다. 따라서 구체적인 해결 방안이 필요하다.")
        }
        val report = ExamEngine.gradeSession(
            session = topik2.copy(selectedOptions = perfectMc, openResponses = strongOpen),
            finishedAtEpochMs = topik2.startedAtEpochMs + 1_200_000L
        )
        assertTrue("Expected Level 6 practice band, got ${report.totalScore} (${report.estimatedLevel})", report.totalScore >= 250)
        assertEquals(LetterGrade.S, report.grade)
        assertTrue(report.tierBreakdowns.size >= 2)
        assertTrue(report.questionReviews.any { it.rubricScore != null })
    }

    @Test
    fun readinessProfileStartsAtZeroAndScalesAcrossAllFourPillars() {
        val empty = LearningScoringEngine.computeReadinessProfile(emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(0, empty.overallReadiness)

        val now = 1_750_000_000_000L
        val vocab = loadFullWordBank().take(50).map {
            it.copy(
                reps = 4, stability = 30.0, difficulty = 4.5,
                stage = CardMemoryStage.MASTERED.name,
                lastReviewEpochMs = now - 86_400_000L,
                dueEpochMs = now + 29 * 86_400_000L
            )
        }
        val quizzes = listOf(QuizRecordEntity(1, "Mixed Quiz", "TOPIK", 10, 9, 90, "None", now, "2026-11-25"))
        val exams = listOf(ExamAttemptEntity(1, "TOPIK_I", "MOCK", 176, 200, "Listening::88::100||Reading::88::100", "Estimated Level 2", "Good", 2, 20, 900, now, "2026-11-25"))
        val speaking = listOf(SpeakingSessionEntity(1, "s1", "Cafe", "Beginner", 4, 4, 86, 8.5f, 8.5f, 8.5f, 8.8f, "", "", "", "", now, "2026-11-25"))

        val profile = LearningScoringEngine.computeReadinessProfile(vocab, quizzes, exams, speaking, now)
        assertTrue("Strong multi-pillar learner should score >= 80, got ${profile.overallReadiness}", profile.overallReadiness >= 80)
        assertEquals(90, profile.quizMasteryScore)
        assertEquals(88, profile.examReadinessScore)
        assertEquals(86, profile.speakingFluencyScore)
        assertTrue(profile.avgRetrievabilityPercent >= 90)
    }
}
