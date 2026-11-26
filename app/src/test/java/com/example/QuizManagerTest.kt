package com.example

import com.example.data.seed.VocabAssetLoader
import com.example.domain.quiz.QuizManager
import com.example.domain.quiz.QuizMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuizManagerTest {

    private fun loadWordBank() = VocabAssetLoader.loadMergedVocabulary(
        topik1Stream = resolveAsset("vocab/topik_1.tsv").inputStream(),
        topik2Stream = resolveAsset("vocab/topik_2.tsv").inputStream()
    )

    private fun resolveAsset(rel: String): File =
        listOf(File("src/main/assets/$rel"), File("app/src/main/assets/$rel"))
            .first { it.exists() }

    @Test
    fun adaptiveSelectionPrioritizesWeakItemsAndAvoidsRecentRepeatsAcrossSessions() {
        var clock = 1_750_000_000_000L
        val manager = QuizManager(timeProvider = { clock })
        val rawBank = loadWordBank().take(80)

        val weakItems = rawBank.take(3).map {
            it.copy(quizWrongCount = 6, quizCorrectCount = 0, lapses = 3)
        }
        val pool = weakItems + rawBank.drop(3)

        val session1 = manager.startSession(
            mode = QuizMode.RECALL,
            allVocab = pool,
            maxCount = 6,
            weakOnly = false
        )
        assertEquals(6, session1.questions.size)
        val firstSessionIds = session1.questions.mapNotNull { it.vocabId }.toSet()
        // At least one high-priority weak word must be selected
        assertTrue(weakItems.any { it.id in firstSessionIds })

        // Second consecutive session should draw fresh items from the remaining pool
        clock += 60_000L
        val session2 = manager.startSession(
            mode = QuizMode.RECALL,
            allVocab = pool,
            maxCount = 6,
            weakOnly = false
        )
        assertEquals(6, session2.questions.size)
        val secondSessionIds = session2.questions.mapNotNull { it.vocabId }.toSet()
        assertTrue(
            "Consecutive session should avoid repeating recent vocabulary IDs",
            firstSessionIds.intersect(secondSessionIds).isEmpty()
        )
    }

    @Test
    fun sentenceArrangementBuildsScrambledTokensEvaluatesExactAndPartialOrder() {
        var clock = 1_750_000_000_000L
        val manager = QuizManager(timeProvider = { clock })
        val bank = loadWordBank().take(120)

        val session = manager.startSession(
            mode = QuizMode.SENTENCE_ARRANGEMENT,
            allVocab = bank,
            maxCount = 4
        )
        assertEquals(4, session.questions.size)
        assertTrue(session.questions.all { it.isArrangementMode && it.arrangedTokens.size >= 3 })

        // Verify scrambled bank order differs from canonical SOV target
        val q0 = session.currentQuestion!!
        assertNotEquals(q0.arrangedTokens, q0.options)
        assertFalse(manager.sessionState.value!!.canSubmitCurrent)

        // Place tokens in exact canonical order by finding matching unused indices in options
        val usedIndices = mutableSetOf<Int>()
        q0.arrangedTokens.forEach { targetToken ->
            val optIdx = q0.options.indices.first { it !in usedIndices && q0.options[it] == targetToken }
            usedIndices += optIdx
            manager.appendArrangementToken(optIdx)
        }

        // Test remove + re-add + clear
        val placedBeforeRemove = manager.sessionState.value!!.arrangedTokenIndices
        manager.removeArrangementToken(placedBeforeRemove.lastIndex)
        assertEquals(q0.options.size - 1, manager.sessionState.value!!.arrangedTokenIndices.size)
        assertFalse(manager.sessionState.value!!.canSubmitCurrent)

        manager.appendArrangementToken(placedBeforeRemove.last())
        assertTrue(manager.sessionState.value!!.canSubmitCurrent)
        assertEquals(q0.correctAnswer, manager.sessionState.value!!.arrangedSentencePreview)

        val outcome0 = manager.submitCurrentAnswer()
        assertNotNull(outcome0)
        assertTrue(outcome0!!.evaluation.isCorrect)
        assertEquals(q0.points, outcome0.pointsAwarded)
        assertEquals(1, outcome0.updatedState.currentStreak)

        // Advance to Q1 and submit the scrambled (wrong) order intentionally
        assertNull(manager.advanceToNext())
        val q1 = manager.sessionState.value!!.currentQuestion!!
        q1.options.indices.forEach { manager.appendArrangementToken(it) }
        val outcome1 = manager.submitCurrentAnswer()
        assertNotNull(outcome1)
        assertFalse(outcome1!!.evaluation.isCorrect)
        assertEquals(0, outcome1.updatedState.currentStreak)
        assertTrue(outcome1.evaluation.feedbackHint.contains("Korean"))
        assertEquals(1, outcome1.updatedState.missedQuestions.size)

        // Clear should do nothing once checked
        manager.clearArrangement()
        assertEquals(q1.options.size, manager.sessionState.value!!.arrangedTokenIndices.size)
    }

    @Test
    fun scoreCalculationProgressSnapshotAndModeProgressTrackingWorkEndToEnd() {
        var clock = 1_750_000_000_000L
        val manager = QuizManager(timeProvider = { clock })
        val bank = loadWordBank().take(80)

        val session = manager.startSession(
            mode = QuizMode.RECALL,
            allVocab = bank,
            maxCount = 4
        )
        assertEquals(4, session.questions.size)

        // Answer Q0, Q1, Q2 correctly and Q3 incorrectly
        for (i in 0 until 4) {
            clock += 5_000L
            val q = manager.sessionState.value!!.currentQuestion!!
            val answer = if (i < 3) q.correctAnswer else q.options.first { it != q.correctAnswer }
            manager.selectOption(answer)
            val outcome = manager.submitCurrentAnswer()
            assertNotNull(outcome)

            val snapshot = manager.getCurrentProgress()
            assertNotNull(snapshot)
            assertEquals(i + 1, snapshot!!.answeredCount)
            assertEquals(4, snapshot.totalQuestions)
            assertEquals((i + 1) / 4f, snapshot.completionFraction, 0.001f)

            val completion = manager.advanceToNext()
            if (i < 3) {
                assertNull(completion)
            } else {
                assertNotNull(completion)
                assertEquals(QuizMode.RECALL, completion!!.mode)
                assertEquals(4, completion.totalQuestions)
                assertEquals(3, completion.correctAnswers)
                assertEquals(3, completion.evaluation.maxStreak)
                assertTrue(completion.evaluation.weightedMasteryScore in 60..95)
            }
        }

        // Verify cumulative per-mode stats updated
        val recallStats = manager.getProgressForMode(QuizMode.RECALL)
        assertEquals(1, recallStats.sessionsCompleted)
        assertEquals(4, recallStats.totalQuestionsAnswered)
        assertEquals(3, recallStats.totalCorrect)
        assertEquals(75, recallStats.accuracyPercent)
        assertEquals(3, recallStats.bestStreak)
    }

    @Test
    fun mixedQuizIncludesBothVocabularyRecallAndSentenceArrangement() {
        val manager = QuizManager()
        val bank = loadWordBank().take(120)

        val session = manager.startSession(
            mode = QuizMode.MIXED,
            allVocab = bank,
            maxCount = 8
        )
        assertEquals(8, session.questions.size)
        assertTrue(
            "Mixed mode should contain at least one Sentence Arrangement question",
            session.questions.any { it.isArrangementMode }
        )
        assertTrue(
            "Mixed mode should contain vocabulary recall / multiple-choice questions",
            session.questions.any { !it.isArrangementMode && !it.isTypingMode }
        )
    }

    @Test
    fun hydrateHistoricalProgressPopulatesModeStatsWithoutOverwritingLiveProgress() {
        val manager = QuizManager()
        manager.hydrateHistoricalProgress(
            listOf(
                Triple(QuizMode.RECALL.title, 8, 7),
                Triple(QuizMode.RECALL.title, 8, 8),
                Triple(QuizMode.SENTENCE_ARRANGEMENT.title, 6, 5)
            )
        )

        val recallStats = manager.getProgressForMode(QuizMode.RECALL)
        assertEquals(2, recallStats.sessionsCompleted)
        assertEquals(16, recallStats.totalQuestionsAnswered)
        assertEquals(15, recallStats.totalCorrect)
        assertEquals(94, recallStats.accuracyPercent)
        assertEquals(100, recallStats.bestWeightedScore)

        val arrangeStats = manager.getProgressForMode(QuizMode.SENTENCE_ARRANGEMENT)
        assertEquals(1, arrangeStats.sessionsCompleted)
        assertEquals(6, arrangeStats.totalQuestionsAnswered)
        assertEquals(5, arrangeStats.totalCorrect)
        assertEquals(83, arrangeStats.accuracyPercent)
    }
}
