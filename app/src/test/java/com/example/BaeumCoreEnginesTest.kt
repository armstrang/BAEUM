package com.example

import com.example.data.seed.ExamType
import com.example.data.seed.KoreanSpeakingSeed
import com.example.data.seed.SpeakingDifficulty
import com.example.domain.exam.ExamEngine
import com.example.domain.exam.ExamSessionMode
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsCardSnapshot
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.speaking.ConversationEngine
import com.example.domain.speaking.ConversationPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaeumCoreEnginesTest {

    @Test
    fun hangulUnicodeCompositionAndDecomposition() {
        assertEquals("가", HangulUnicodeEngine.composeSyllable('ㄱ', 'ㅏ', null))
        assertEquals("한", HangulUnicodeEngine.composeSyllable('ㅎ', 'ㅏ', 'ㄴ'))
        assertEquals("배", HangulUnicodeEngine.composeSyllable('ㅂ', 'ㅐ', null))
        assertEquals("움", HangulUnicodeEngine.composeSyllable('ㅇ', 'ㅜ', 'ㅁ'))

        val decomposed = HangulUnicodeEngine.decomposeSyllable('한')
        assertEquals(Triple('ㅎ', 'ㅏ', 'ㄴ'), decomposed)
        assertTrue(HangulUnicodeEngine.hasBatchim('학'))
        assertEquals('ㄱ', HangulUnicodeEngine.getBatchim('학'))
    }

    @Test
    fun fsrsSchedulerProducesMonotonicIntervalsAndPromotesStages() {
        val now = 1_700_000_000_000L
        val newCard = FsrsCardSnapshot()
        val previews = FsrsScheduler.previewAll(newCard, nowEpochMs = now)

        assertTrue(previews[FsrsRating.AGAIN]!!.intervalDays < previews[FsrsRating.HARD]!!.intervalDays)
        assertTrue(previews[FsrsRating.HARD]!!.intervalDays < previews[FsrsRating.GOOD]!!.intervalDays)
        assertTrue(previews[FsrsRating.GOOD]!!.intervalDays < previews[FsrsRating.EASY]!!.intervalDays)

        // Repeated Easy reviews should graduate stability >= 21 days -> MASTERED
        var card = newCard
        var t = now
        repeat(4) {
            val res = FsrsScheduler.schedule(card, FsrsRating.EASY, nowEpochMs = t)
            card = res.nextState
            t = card.dueEpochMs
        }
        assertEquals(CardMemoryStage.MASTERED, card.stage)
        assertTrue(card.stability >= 21.0)
    }

    @Test
    fun speakingScenariosHaveTenPerDifficultyAndScoreZeroStillAdvances() {
        assertEquals(10, KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.BEGINNER).size)
        assertEquals(10, KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.INTERMEDIATE).size)
        assertEquals(10, KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.ADVANCED).size)

        val scenario = KoreanSpeakingSeed.scenariosByDifficulty(SpeakingDifficulty.BEGINNER).first()
        var state = ConversationEngine.startScenario(scenario)
        val firstTurnId = state.currentPrompt!!.turnId

        // Submit non-Korean text -> score 0, yet MUST advance to next unique turn without looping!
        val evalZero = ConversationEngine.evaluateResponse(state.currentPrompt!!, "hello", true)
        assertEquals(0, evalZero.scoreOutOf10)
        state = ConversationEngine.commitTurnAndAdvance(state, evalZero)

        assertEquals(1, state.currentTurnIndex)
        assertNotEquals(firstTurnId, state.currentPrompt?.turnId)

        // Complete all remaining turns cleanly -> COMPLETED
        while (!state.isComplete) {
            val prompt = state.currentPrompt!!
            val eval = ConversationEngine.evaluateResponse(prompt, prompt.modelResponseKo, false)
            assertTrue(eval.scoreOutOf10 >= 7)
            state = ConversationEngine.commitTurnAndAdvance(state, eval)
        }
        assertEquals(ConversationPhase.COMPLETED, state.phase)

        val report = ConversationEngine.buildFinalAssessment(state)
        assertTrue(report.pronunciationStatus.contains("Not assessed"))
        assertEquals(scenario.turns.size, report.completedTurns)
    }

    @Test
    fun examEngineGradesCorrectlyAndProducesNonOfficialBand() {
        val session = ExamEngine.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK)
        val allCorrect = session.questions.associate { it.id to it.correctOptionIndex }
        val graded = ExamEngine.gradeSession(session.copy(selectedOptions = allCorrect))

        assertEquals(200, graded.totalScore)
        assertTrue(graded.estimatedLevel.contains("Practice"))
        assertEquals(0, graded.questionReviews.count { !it.isCorrect })
    }
}
