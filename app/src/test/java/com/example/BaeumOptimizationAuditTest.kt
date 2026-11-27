package com.example

import com.example.data.local.CurriculumPathway
import com.example.data.local.VocabEntity
import com.example.data.repository.BaeumRepository
import com.example.data.seed.VocabAssetLoader
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FlashcardSessionAlgorithm
import com.example.domain.fsrs.FsrsCardSnapshot
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.quiz.AdaptiveQuizEngine
import com.example.domain.quiz.QuizMode
import com.example.domain.scoring.LearningScoringEngine
import com.example.domain.speaking.KoreanPronunciationEvaluator
import com.example.domain.speaking.SyllableMatchLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.random.Random

class BaeumOptimizationAuditTest {

    private fun resolveAsset(rel: String): File =
        listOf(File("src/main/assets/$rel"), File("app/src/main/assets/$rel"))
            .first { it.exists() }

    private fun loadFullWordBank(): List<VocabEntity> = VocabAssetLoader.loadMergedVocabulary(
        topik1Stream = resolveAsset("vocab/topik_1.tsv").inputStream(),
        topik2Stream = resolveAsset("vocab/topik_2.tsv").inputStream()
    )

    private fun sampleWord(
        id: String,
        curriculum: String = CurriculumPathway.TOPIK,
        subCategory: String = "1급",
        reps: Int = 0,
        lapses: Int = 0,
        stability: Double = 0.0,
        lastReviewEpochMs: Long = 0L,
        dueEpochMs: Long = 0L,
        stage: CardMemoryStage = CardMemoryStage.NEW,
        quizCorrect: Int = 0,
        quizWrong: Int = 0
    ): VocabEntity = VocabEntity(
        id = id,
        hangul = "학교",
        romanization = "hakgyo",
        meaningEn = "school",
        meaningId = "sekolah",
        partOfSpeech = "Noun",
        curriculum = curriculum,
        subCategory = subCategory,
        exampleKo = "저는 매일 학교에 가요.",
        exampleEn = "I go to school every day.",
        exampleId = "Saya pergi ke sekolah setiap hari.",
        reps = reps,
        lapses = lapses,
        stability = stability,
        difficulty = 5.0,
        lastReviewEpochMs = lastReviewEpochMs,
        dueEpochMs = dueEpochMs,
        stage = stage.name,
        quizCorrectCount = quizCorrect,
        quizWrongCount = quizWrong
    )

    @Test
    fun singlePassVocabAggregateMatchesIndividualHelpersExactly() {
        val dayMs = 86_400_000L
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 15, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis
        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val vocab = listOf(
            // Overdue leech in TOPIK
            sampleWord(
                id = "w_overdue",
                curriculum = CurriculumPathway.TOPIK,
                reps = 3, lapses = 4, stability = 3.5,
                lastReviewEpochMs = now - 5 * dayMs,
                dueEpochMs = startOfToday - 3_600_000L,
                stage = CardMemoryStage.LEARNING,
                quizCorrect = 1, quizWrong = 3
            ),
            // Due later today in TOPIK
            sampleWord(
                id = "w_due_today",
                curriculum = CurriculumPathway.TOPIK,
                reps = 2, lapses = 0, stability = 8.0,
                lastReviewEpochMs = now - 2 * dayMs,
                dueEpochMs = startOfToday + 14 * 3_600_000L,
                stage = CardMemoryStage.REVIEW,
                quizCorrect = 4, quizWrong = 1
            ),
            // Due in +2 days in EPS_TOPIK
            sampleWord(
                id = "w_day2",
                curriculum = CurriculumPathway.EPS_TOPIK,
                reps = 5, lapses = 0, stability = 24.0,
                lastReviewEpochMs = now - 1 * dayMs,
                dueEpochMs = startOfToday + 2 * dayMs + 3_600_000L,
                stage = CardMemoryStage.MASTERED
            ),
            // Due in +6 days in TOPIK
            sampleWord(
                id = "w_day6",
                curriculum = CurriculumPathway.TOPIK,
                reps = 4, lapses = 1, stability = 15.0,
                lastReviewEpochMs = now - 2 * dayMs,
                dueEpochMs = startOfToday + 6 * dayMs + 7_200_000L,
                stage = CardMemoryStage.REVIEW,
                quizCorrect = 0, quizWrong = 2
            ),
            // Due beyond 7-day window (+10 days)
            sampleWord(
                id = "w_day10",
                curriculum = CurriculumPathway.TOPIK,
                reps = 6, lapses = 0, stability = 40.0,
                lastReviewEpochMs = now - dayMs,
                dueEpochMs = startOfToday + 10 * dayMs,
                stage = CardMemoryStage.MASTERED
            ),
            // Untouched NEW word in TOPIK
            sampleWord(
                id = "w_new_1",
                curriculum = CurriculumPathway.TOPIK,
                reps = 0, stage = CardMemoryStage.NEW
            ),
            // Untouched NEW word in HANGUL
            sampleWord(
                id = "w_new_2",
                curriculum = CurriculumPathway.HANGUL,
                reps = 0, stage = CardMemoryStage.NEW
            )
        )

        val agg = BaeumRepository.computeVocabAggregate(
            vocab = vocab,
            activePathway = CurriculumPathway.TOPIK,
            nowEpochMs = now,
            includeRetrievability = true
        )

        assertEquals(vocab.size, agg.totalCount)
        assertEquals(BaeumRepository.countDueTodayOnly(vocab, now), agg.dueTodayCount)
        assertEquals(BaeumRepository.countOverdue(vocab, now), agg.overdueCount)
        assertEquals(BaeumRepository.buildSevenDayForecast(vocab, now), agg.sevenDayForecast)
        assertEquals(BaeumRepository.countStage(vocab, CardMemoryStage.NEW), agg.newCount)
        assertEquals(BaeumRepository.countStage(vocab, CardMemoryStage.LEARNING), agg.learningCount)
        assertEquals(BaeumRepository.countStage(vocab, CardMemoryStage.REVIEW), agg.reviewCount)
        assertEquals(BaeumRepository.countStage(vocab, CardMemoryStage.MASTERED), agg.masteredCount)
        assertEquals(2, agg.dueTodayCount)
        assertEquals(1, agg.overdueCount)
        assertEquals(listOf(2, 0, 1, 0, 0, 0, 1), agg.sevenDayForecast.map { it.dueCount })
        assertEquals(1, agg.leechCount)
        assertEquals(2, agg.weakQuizCount)
        assertEquals(5, agg.activePathwayTotal)
        assertEquals(4, agg.activePathwayLearned)

        val expectedAvgR = (vocab.filter { it.reps > 0 && it.stability > 0.0 }
            .map { FsrsScheduler.currentRetrievability(it.toFsrsSnapshot(), now) }
            .average() * 100.0).roundToInt().coerceIn(0, 100)
        assertEquals(expectedAvgR, agg.averageRetrievabilityPercent)
    }

    @Test
    fun fsrsPrimitiveOverloadsMatchSnapshotOverloadsExactly() {
        val now = 1_750_000_000_000L
        val dayMs = 86_400_000L
        val samples = listOf(
            FsrsCardSnapshot(reps = 0, stability = 0.0, lastReviewEpochMs = 0L, lapses = 0),
            FsrsCardSnapshot(reps = 3, stability = 7.5, lastReviewEpochMs = now - 3 * dayMs, lapses = 3),
            FsrsCardSnapshot(reps = 5, stability = Double.NaN, lastReviewEpochMs = now - dayMs, lapses = 4),
            FsrsCardSnapshot(reps = 2, stability = 12.0, lastReviewEpochMs = now + dayMs, lapses = 6)
        )

        for (snap in samples) {
            assertEquals(
                FsrsScheduler.currentRetrievability(snap, now),
                FsrsScheduler.currentRetrievability(snap.reps, snap.stability, snap.lastReviewEpochMs, now),
                1e-12
            )
            assertEquals(FsrsScheduler.isLeech(snap), FsrsScheduler.isLeech(snap.lapses))
        }
    }

    @Test
    fun adaptiveQuizGrammarModeIsDeterministicWhenSeeded() {
        val run1 = AdaptiveQuizEngine.generateQuestions(
            mode = QuizMode.GRAMMAR,
            allVocab = emptyList(),
            translationLang = "ID",
            maxCount = 6,
            rng = Random(2026)
        )
        val run2 = AdaptiveQuizEngine.generateQuestions(
            mode = QuizMode.GRAMMAR,
            allVocab = emptyList(),
            translationLang = "ID",
            maxCount = 6,
            rng = Random(2026)
        )
        assertEquals(6, run1.size)
        assertEquals(run1.map { it.id }, run2.map { it.id })
        assertEquals(run1.map { it.options }, run2.map { it.options })
    }

    @Test
    fun allQuizModesAndFlashcardFrontierWorkCleanlyOnFullWordBank() {
        val fullBank = loadFullWordBank()
        assertTrue("Full word bank should exceed 6,000 entries", fullBank.size >= 6000)

        for (mode in QuizMode.entries) {
            val qs = AdaptiveQuizEngine.generateQuestions(
                mode = mode,
                allVocab = fullBank,
                translationLang = "EN",
                maxCount = 8,
                rng = Random(1234)
            )
            assertTrue("Expected non-empty questions for $mode", qs.isNotEmpty())
        }

        // Verify frontier bucketing picks lowest curriculum tier (not 6급) for fresh learners
        val deck = FlashcardSessionAlgorithm.buildAdaptiveDeck(
            pool = fullBank,
            dailyGoal = 20,
            dueOnly = false,
            maxSessionSize = 15,
            rng = Random(99)
        )
        assertEquals(15, deck.size)
        assertFalse("Fresh learner frontier should not jump to 6급", deck.any { it.subCategory == "6급" })

        // Verify single-pass readiness profile handles full 6,600+ bank cleanly
        val readiness = LearningScoringEngine.computeReadinessProfile(
            vocab = fullBank,
            quizRecords = emptyList(),
            examAttempts = emptyList(),
            speakingSessions = emptyList()
        )
        assertEquals(0, readiness.overallReadiness)
        assertEquals(0, readiness.leechCount)
    }

    @Test
    fun pronunciationEvaluatorCachedDpAlignsExactCloseAndMissingSyllables() {
        val drill = KoreanPronunciationEvaluator.createAdHocDrill(
            id = "dp_test",
            writtenKo = "한국어",
            pronouncedKo = "한구거",
            romanization = "hangugeo",
            meaningEn = "Korean language",
            meaningId = "Bahasa Korea",
            tipEn = "Liaison",
            tipId = "Penghubungan"
        )

        val exactResult = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf("한국어")
        )
        assertTrue(exactResult.passed)
        assertEquals(100, exactResult.matchScorePercent)
        assertTrue(exactResult.syllableBreakdown.all { it.matchLevel == SyllableMatchLevel.EXACT })

        val partialResult = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf("한구")
        )
        assertEquals(3, partialResult.syllableBreakdown.size)
        assertNotNull(partialResult.syllableBreakdown.last().issueType)
        assertEquals(SyllableMatchLevel.MISMATCH, partialResult.syllableBreakdown.last().matchLevel)
    }
}
