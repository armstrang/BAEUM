package com.example

import com.example.data.local.CurriculumPathway
import com.example.data.local.UserSettings
import com.example.data.local.VocabEntity
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsRating
import com.example.viewmodel.FlashcardFsrsStore
import com.example.viewmodel.FlashcardFsrsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardFsrsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeFlashcardFsrsStore(
        initialVocab: List<VocabEntity>,
        initialSettings: UserSettings = UserSettings()
    ) : FlashcardFsrsStore {
        private val vocabFlow = MutableStateFlow(initialVocab)
        private val settingsFlow = MutableStateFlow(initialSettings)
        val persistedReviews = mutableListOf<Triple<String, FsrsRating, Double>>()
        val restoredIds = mutableListOf<String>()

        override val allVocabFlow: Flow<List<VocabEntity>> = vocabFlow
        override val userSettingsFlow: Flow<UserSettings> = settingsFlow

        fun currentVocab(id: String): VocabEntity =
            vocabFlow.value.first { it.id == id }

        override suspend fun persistFsrsReview(
            vocabBefore: VocabEntity,
            updatedVocab: VocabEntity,
            rating: FsrsRating,
            intervalDays: Double,
            studyDurationSec: Int,
            nowEpochMs: Long
        ): VocabEntity {
            persistedReviews.add(Triple(updatedVocab.id, rating, intervalDays))
            vocabFlow.value = vocabFlow.value.map { if (it.id == updatedVocab.id) updatedVocab else it }
            return updatedVocab
        }

        override suspend fun restoreVocabState(previousVocab: VocabEntity) {
            restoredIds.add(previousVocab.id)
            vocabFlow.value = vocabFlow.value.map { if (it.id == previousVocab.id) previousVocab else it }
        }
    }

    private fun sampleVocab(
        id: String,
        hangul: String,
        reps: Int = 0,
        stability: Double = 0.0,
        difficulty: Double = 0.0,
        lapses: Int = 0,
        stage: CardMemoryStage = CardMemoryStage.NEW,
        lastReviewEpochMs: Long = 0L,
        dueEpochMs: Long = 0L
    ): VocabEntity = VocabEntity(
        id = id,
        hangul = hangul,
        romanization = "rom-$id",
        meaningEn = "Meaning $id",
        meaningId = "Arti $id",
        partOfSpeech = "noun",
        curriculum = CurriculumPathway.TOPIK,
        subCategory = "1급",
        exampleKo = "$hangul 예시",
        exampleEn = "Example $id",
        exampleId = "Contoh $id",
        pronunciationNote = "",
        stability = stability,
        difficulty = difficulty,
        reps = reps,
        lapses = lapses,
        stage = stage.name,
        lastReviewEpochMs = lastReviewEpochMs,
        dueEpochMs = dueEpochMs
    )

    @Test
    fun startSession_computesFsrsPreviewsAndTelemetryInsideViewModel() = runTest(testDispatcher) {
        val now = 1_700_000_000_000L
        val card1 = sampleVocab("v1", "한국어")
        val store = FakeFlashcardFsrsStore(listOf(card1))
        val vm = FlashcardFsrsViewModel(store = store, clock = { now }, rng = Random(42))
        advanceUntilIdle()

        vm.startSession(curriculumFilter = CurriculumPathway.TOPIK)
        advanceUntilIdle()

        val state = vm.sessionState.value
        assertEquals("v1", state.currentCard?.id)
        assertEquals(CardMemoryStage.NEW, state.currentCardTelemetry?.stage)
        assertEquals(4, state.ratingPreviews.size)

        val againInterval = state.ratingPreviews[FsrsRating.AGAIN]!!.intervalDays
        val hardInterval = state.ratingPreviews[FsrsRating.HARD]!!.intervalDays
        val goodInterval = state.ratingPreviews[FsrsRating.GOOD]!!.intervalDays
        val easyInterval = state.ratingPreviews[FsrsRating.EASY]!!.intervalDays
        assertTrue(againInterval < hardInterval)
        assertTrue(hardInterval < goodInterval)
        assertTrue(goodInterval < easyInterval)
    }

    @Test
    fun rateCurrentCard_ignoresUnrevealedRatingAndSchedulesWhenRevealed() = runTest(testDispatcher) {
        var now = 1_700_000_000_000L
        val card1 = sampleVocab("v1", "감사합니다")
        val card2 = sampleVocab("v2", "안녕하세요")
        val store = FakeFlashcardFsrsStore(listOf(card1, card2))
        val vm = FlashcardFsrsViewModel(store = store, clock = { now }, rng = Random(7))
        advanceUntilIdle()

        vm.startSession(curriculumFilter = CurriculumPathway.TOPIK)
        advanceUntilIdle()

        // Rating before reveal must be ignored
        vm.rateCurrentCard(FsrsRating.GOOD)
        advanceUntilIdle()
        assertEquals(0, vm.sessionState.value.reviewedCount)
        assertTrue(store.persistedReviews.isEmpty())

        // Reveal and rate EASY
        vm.revealCard()
        now += 5_000L
        val firstId = vm.sessionState.value.currentCard!!.id
        vm.rateCurrentCard(FsrsRating.EASY)
        advanceUntilIdle()

        val persisted = store.currentVocab(firstId)
        assertEquals(1, persisted.reps)
        assertTrue(persisted.stability > 1.0)
        assertTrue(persisted.dueEpochMs > now)
        assertEquals(CardMemoryStage.REVIEW.name, persisted.stage)
        assertEquals(1, vm.sessionState.value.reviewedCount)
        assertTrue(vm.sessionState.value.canUndo)
    }

    @Test
    fun ratingAgain_incrementsLapsesAndRequeuesCardInSession() = runTest(testDispatcher) {
        var now = 1_700_000_000_000L
        val cards = listOf(
            sampleVocab("v1", "사과"),
            sampleVocab("v2", "물"),
            sampleVocab("v3", "책")
        )
        val store = FakeFlashcardFsrsStore(cards)
        val vm = FlashcardFsrsViewModel(store = store, clock = { now }, rng = Random(1))
        advanceUntilIdle()

        vm.startSession(curriculumFilter = CurriculumPathway.TOPIK)
        advanceUntilIdle()

        val initialSize = vm.sessionState.value.cards.size
        val targetId = vm.sessionState.value.currentCard!!.id
        vm.revealCard()
        now += 4_000L
        vm.rateCurrentCard(FsrsRating.AGAIN)
        advanceUntilIdle()

        val updated = store.currentVocab(targetId)
        assertEquals(1, updated.lapses)
        assertEquals(CardMemoryStage.LEARNING.name, updated.stage)
        assertEquals(initialSize + 1, vm.sessionState.value.cards.size)
        assertTrue(vm.sessionState.value.cards.drop(1).any { it.id == targetId })
    }

    @Test
    fun undoLastRating_restoresExactPreviousFsrsState() = runTest(testDispatcher) {
        var now = 1_700_000_000_000L
        val dayMs = 86_400_000L
        val reviewedCard = sampleVocab(
            id = "v_due",
            hangul = "학교",
            reps = 2,
            stability = 6.5,
            difficulty = 5.2,
            lapses = 0,
            stage = CardMemoryStage.REVIEW,
            lastReviewEpochMs = now - 6 * dayMs,
            dueEpochMs = now - dayMs
        )
        val store = FakeFlashcardFsrsStore(listOf(reviewedCard))
        val vm = FlashcardFsrsViewModel(store = store, clock = { now }, rng = Random(3))
        advanceUntilIdle()

        vm.startSession(dueOnly = true)
        advanceUntilIdle()
        vm.revealCard()
        now += 3_000L
        vm.rateCurrentCard(FsrsRating.AGAIN)
        advanceUntilIdle()

        // Verify state mutated
        assertEquals(1, store.currentVocab("v_due").lapses)
        assertEquals(3, store.currentVocab("v_due").reps)

        // Undo must restore exact original FSRS snapshot
        vm.undoLastRating()
        advanceUntilIdle()

        val restored = store.currentVocab("v_due")
        assertEquals(0, restored.lapses)
        assertEquals(2, restored.reps)
        assertEquals(6.5, restored.stability, 0.0001)
        assertEquals(5.2, restored.difficulty, 0.0001)
        assertEquals(CardMemoryStage.REVIEW.name, restored.stage)
        assertEquals(0, vm.sessionState.value.currentIndex)
        assertEquals(0, vm.sessionState.value.reviewedCount)
        assertTrue(vm.sessionState.value.isRevealed)
        assertFalse(vm.sessionState.value.canUndo)
        assertFalse(vm.sessionState.value.isCompleted)
    }

    @Test
    fun multiDaySpacedReviews_growStabilityAndCompleteSessionEvaluation() = runTest(testDispatcher) {
        val dayMs = 86_400_000L
        var now = 1_700_000_000_000L
        val card = sampleVocab("v_grow", "공부")
        val store = FakeFlashcardFsrsStore(listOf(card))
        val vm = FlashcardFsrsViewModel(store = store, clock = { now }, rng = Random(9))
        advanceUntilIdle()

        // Session 1: Easy (graduates immediately)
        vm.startSession(curriculumFilter = CurriculumPathway.TOPIK)
        advanceUntilIdle()
        vm.revealCard()
        now += 4_000L
        vm.rateCurrentCard(FsrsRating.EASY)
        advanceUntilIdle()
        val s1 = store.currentVocab("v_grow").stability
        assertTrue(vm.sessionState.value.isCompleted)
        assertNotNull(vm.sessionState.value.evaluation)

        // Advance clock to next due date and review GOOD
        now = store.currentVocab("v_grow").dueEpochMs + 1_000L
        vm.startSession(dueOnly = true)
        advanceUntilIdle()
        vm.revealCard()
        now += 4_000L
        vm.rateCurrentCard(FsrsRating.GOOD)
        advanceUntilIdle()
        val s2 = store.currentVocab("v_grow").stability
        assertTrue("Expected stability to grow on due GOOD review ($s2 > $s1)", s2 > s1)

        // Advance clock again and review GOOD -> should reach MASTERED
        now = store.currentVocab("v_grow").dueEpochMs + 1_000L
        vm.startSession(dueOnly = true)
        advanceUntilIdle()
        vm.revealCard()
        now += 4_000L
        vm.rateCurrentCard(FsrsRating.GOOD)
        advanceUntilIdle()
        val finalCard = store.currentVocab("v_grow")
        assertTrue("Expected stability to keep growing (${finalCard.stability} > $s2)", finalCard.stability > s2)
        assertEquals(3, finalCard.reps)
        assertEquals(CardMemoryStage.MASTERED.name, finalCard.stage)
    }
}
