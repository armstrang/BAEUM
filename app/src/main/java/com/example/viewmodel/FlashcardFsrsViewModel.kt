package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CurriculumPathway
import com.example.data.local.UserSettings
import com.example.data.local.VocabEntity
import com.example.data.repository.BaeumRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FlashcardSessionAlgorithm
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduleResult
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.scoring.FlashcardRatingOutcome
import com.example.domain.scoring.FlashcardSessionEvaluation
import com.example.domain.scoring.LearningScoringEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Snapshot of FSRS memory metrics for the active Korean flashcard, precomputed by [FlashcardFsrsViewModel].
 */
data class FsrsCardTelemetry(
    val stage: CardMemoryStage = CardMemoryStage.NEW,
    val stabilityDays: Double = 0.0,
    val difficulty: Double = 0.0,
    val retrievabilityPercent: Int? = null,
    val reps: Int = 0,
    val lapses: Int = 0,
    val isLeech: Boolean = false
)

/**
 * Aggregate FSRS spaced-repetition health metrics across the learner's Korean vocabulary deck.
 */
data class FsrsDeckOverview(
    val totalCards: Int = 0,
    val dueNowCount: Int = 0,
    val overdueCount: Int = 0,
    val newCount: Int = 0,
    val learningCount: Int = 0,
    val reviewCount: Int = 0,
    val masteredCount: Int = 0,
    val leechCount: Int = 0,
    val averageRetrievabilityPercent: Int? = null
)

/**
 * Full UI state for a Korean Flashcards FSRS study session, owned by [FlashcardFsrsViewModel].
 */
data class FlashcardFsrsUiState(
    val title: String = "Review session",
    val cards: List<VocabEntity> = emptyList(),
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val reviewedCount: Int = 0,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val sessionStartedMs: Long = 0L,
    val cardShownEpochMs: Long = 0L,
    val desiredRetention: Double = 0.90,
    val currentCardTelemetry: FsrsCardTelemetry? = null,
    val ratingPreviews: Map<FsrsRating, FsrsScheduleResult> = emptyMap(),
    val canUndo: Boolean = false,
    val lastScheduledLabel: String? = null,
    val outcomes: List<FlashcardRatingOutcome> = emptyList(),
    val evaluation: FlashcardSessionEvaluation? = null
) {
    val currentCard: VocabEntity? get() = cards.getOrNull(currentIndex)
}

/**
 * Persistence and data-source abstraction for [FlashcardFsrsViewModel] to keep the ViewModel
 * cleanly decoupled and deterministic in JVM unit tests.
 */
interface FlashcardFsrsStore {
    val allVocabFlow: Flow<List<VocabEntity>>
    val userSettingsFlow: Flow<UserSettings>

    suspend fun persistFsrsReview(
        vocabBefore: VocabEntity,
        updatedVocab: VocabEntity,
        rating: FsrsRating,
        intervalDays: Double,
        studyDurationSec: Int,
        nowEpochMs: Long
    ): VocabEntity

    suspend fun restoreVocabState(previousVocab: VocabEntity)
}

class BaeumFlashcardFsrsStore(
    private val repository: BaeumRepository,
    private val userPreferencesRepository: UserPreferencesRepository = repository.userPreferences
) : FlashcardFsrsStore {
    override val allVocabFlow: Flow<List<VocabEntity>> = repository.allVocabFlow
    override val userSettingsFlow: Flow<UserSettings> = userPreferencesRepository.userSettingsFlow

    override suspend fun persistFsrsReview(
        vocabBefore: VocabEntity,
        updatedVocab: VocabEntity,
        rating: FsrsRating,
        intervalDays: Double,
        studyDurationSec: Int,
        nowEpochMs: Long
    ): VocabEntity = repository.persistFsrsReview(
        vocabBefore = vocabBefore,
        updatedVocab = updatedVocab,
        rating = rating,
        intervalDays = intervalDays,
        studyDurationSec = studyDurationSec,
        nowEpochMs = nowEpochMs
    )

    override suspend fun restoreVocabState(previousVocab: VocabEntity) {
        repository.restoreVocabState(previousVocab)
    }
}

private data class FsrsUndoSnapshot(
    val previousVocab: VocabEntity,
    val previousQueue: List<VocabEntity>,
    val previousIndex: Int,
    val previousReviewedCount: Int,
    val previousOutcomes: List<FlashcardRatingOutcome>
)

/**
 * Dedicated [ViewModel] implementing the FSRS (Free Spaced Repetition Scheduler) algorithm
 * for Korean flashcards.
 *
 * Responsibilities:
 * - Builds adaptive, forgetting-risk-prioritized flashcard decks using [FlashcardSessionAlgorithm].
 * - Computes live FSRS-4.5/5 Stability (S), Difficulty (D), Retrievability (R), and next-interval
 *   previews for `AGAIN`, `HARD`, `GOOD`, and `EASY` ratings inside the ViewModel.
 * - Executes FSRS state transitions via [FsrsScheduler.schedule], tracks actual per-card recall
 *   latency, applies intra-session re-queueing for lapsed/difficult cards, and persists results.
 * - Supports one-step Undo rollback to restore the card's exact prior FSRS memory state.
 */
class FlashcardFsrsViewModel(
    private val store: FlashcardFsrsStore,
    private val clock: () -> Long = System::currentTimeMillis,
    private val rng: Random = Random.Default
) : ViewModel() {

    val allVocab: StateFlow<List<VocabEntity>> = store.allVocabFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val userSettings: StateFlow<UserSettings> = store.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val deckOverview: StateFlow<FsrsDeckOverview> = allVocab
        .map { vocab -> computeDeckOverview(vocab, clock()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FsrsDeckOverview())

    private val _sessionState = MutableStateFlow(FlashcardFsrsUiState())
    val sessionState: StateFlow<FlashcardFsrsUiState> = _sessionState.asStateFlow()

    private var lastUndoSnapshot: FsrsUndoSnapshot? = null

    init {
        viewModelScope.launch {
            userSettings.collect { prefs ->
                val cur = _sessionState.value
                if (cur.desiredRetention != prefs.fsrsDesiredRetention) {
                    val card = cur.currentCard
                    val now = clock()
                    _sessionState.update {
                        it.copy(
                            desiredRetention = prefs.fsrsDesiredRetention,
                            ratingPreviews = card?.let { c ->
                                computeRatingPreviews(c, now, prefs.fsrsDesiredRetention)
                            }.orEmpty()
                        )
                    }
                }
            }
        }
    }

    /**
     * Starts an FSRS-scheduled Korean flashcard session filtered by optional curriculum/subCategory
     * or strictly due-for-review cards.
     */
    fun startSession(
        curriculumFilter: String? = null,
        subCategoryFilter: String? = null,
        dueOnly: Boolean = false,
        vocabOverride: List<VocabEntity>? = null
    ) {
        val now = clock()
        val prefs = userSettings.value
        val sourceVocab = vocabOverride ?: allVocab.value
        val pool = sourceVocab.filter { item ->
            (curriculumFilter == null || item.curriculum == curriculumFilter) &&
                (subCategoryFilter == null || item.subCategory == subCategoryFilter)
        }

        val deck = FlashcardSessionAlgorithm.buildAdaptiveDeck(
            pool = pool,
            dailyGoal = prefs.dailyGoalWords,
            dueOnly = dueOnly,
            nowEpochMs = now,
            maxSessionSize = if (dueOnly) 25 else 15,
            rng = rng
        )

        val isId = prefs.translationLanguage.equals("ID", ignoreCase = true)
        val activePathwayLabel = when (prefs.activePathway) {
            CurriculumPathway.HANGUL -> "Hangul"
            CurriculumPathway.TOPIK -> "TOPIK I"
            CurriculumPathway.EPS_TOPIK -> "EPS-TOPIK"
            CurriculumPathway.EVERYDAY -> if (isId) "Sehari-hari" else "Everyday Korean"
            else -> "TOPIK I"
        }
        val title = when {
            dueOnly -> if (isId) "Ulasan Terjadwal FSRS" else "Due FSRS Review"
            subCategoryFilter != null -> if (isId) "$subCategoryFilter • Ulasan" else "$subCategoryFilter • Review"
            curriculumFilter != null -> if (isId) "$curriculumFilter • Belajar" else "$curriculumFilter • Study"
            else -> if (isId) "$activePathwayLabel • Sesi ulasan" else "$activePathwayLabel • Review session"
        }

        lastUndoSnapshot = null
        val firstCard = deck.firstOrNull()
        _sessionState.value = FlashcardFsrsUiState(
            title = title,
            cards = deck,
            currentIndex = 0,
            isRevealed = false,
            reviewedCount = 0,
            isSubmitting = false,
            isCompleted = deck.isEmpty(),
            sessionStartedMs = now,
            cardShownEpochMs = now,
            desiredRetention = prefs.fsrsDesiredRetention,
            currentCardTelemetry = firstCard?.let { computeTelemetry(it, now) },
            ratingPreviews = firstCard?.let { computeRatingPreviews(it, now, prefs.fsrsDesiredRetention) }.orEmpty(),
            canUndo = false,
            lastScheduledLabel = null,
            outcomes = emptyList(),
            evaluation = null
        )
    }

    /**
     * Reveals the active card's meaning/example and refreshes FSRS interval previews at the exact reveal moment.
     */
    fun revealCard() {
        val cur = _sessionState.value
        val card = cur.currentCard ?: return
        if (cur.isRevealed || cur.isCompleted) return
        val now = clock()
        _sessionState.update {
            it.copy(
                isRevealed = true,
                currentCardTelemetry = computeTelemetry(card, now),
                ratingPreviews = computeRatingPreviews(card, now, it.desiredRetention)
            )
        }
    }

    /**
     * Applies the FSRS algorithm for the selected [rating] (`AGAIN`, `HARD`, `GOOD`, `EASY`),
     * updates card Stability (S), Difficulty (D), Retrievability (R), memory stage, and next due timestamp,
     * persists atomically, and advances or re-queues the card in the active session.
     */
    fun rateCurrentCard(rating: FsrsRating) {
        val session = _sessionState.value
        val card = session.currentCard ?: return
        if (!session.isRevealed || session.isSubmitting || session.isCompleted) return

        val now = clock()
        val cardDurationSec = ((now - session.cardShownEpochMs) / 1000L)
            .toInt()
            .coerceIn(2, 120)

        val scheduleResult = FsrsScheduler.schedule(
            card = card.toFsrsSnapshot(),
            rating = rating,
            nowEpochMs = now,
            desiredRetention = session.desiredRetention
        )
        val next = scheduleResult.nextState
        val updatedCard = card.copy(
            stability = next.stability,
            difficulty = next.difficulty,
            elapsedDays = next.elapsedDays,
            scheduledDays = next.scheduledDays,
            reps = next.reps,
            lapses = next.lapses,
            stage = next.stage.name,
            lastReviewEpochMs = next.lastReviewEpochMs,
            dueEpochMs = next.dueEpochMs
        )

        val undoCandidate = FsrsUndoSnapshot(
            previousVocab = card,
            previousQueue = session.cards,
            previousIndex = session.currentIndex,
            previousReviewedCount = session.reviewedCount,
            previousOutcomes = session.outcomes
        )

        _sessionState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            val persisted = store.persistFsrsReview(
                vocabBefore = card,
                updatedVocab = updatedCard,
                rating = rating,
                intervalDays = scheduleResult.intervalDays,
                studyDurationSec = cardDurationSec,
                nowEpochMs = now
            )

            val wasFirstExposure = card.reps == 0
            val outcome = FlashcardRatingOutcome(
                vocabId = persisted.id,
                hangul = persisted.hangul,
                rating = rating,
                stabilityBefore = card.stability,
                stabilityAfter = persisted.stability,
                lapsesAfter = persisted.lapses,
                stageBefore = card.stage,
                stageAfter = persisted.stage
            )
            val updatedOutcomes = session.outcomes + outcome
            val nextQueue = FlashcardSessionAlgorithm.applyIntraSessionRequeue(
                currentQueue = session.cards,
                currentIndex = session.currentIndex,
                updatedCard = persisted,
                rating = rating,
                wasFirstExposure = wasFirstExposure
            )
            val nextIdx = session.currentIndex + 1
            val done = nextIdx >= nextQueue.size
            val elapsedSec = ((now - session.sessionStartedMs) / 1000L).toInt().coerceAtLeast(1)
            val evaluation = if (done) {
                LearningScoringEngine.evaluateFlashcardSession(updatedOutcomes, elapsedSec)
            } else {
                null
            }

            lastUndoSnapshot = undoCandidate
            val nextCard = nextQueue.getOrNull(nextIdx)
            val nextTime = clock()

            _sessionState.value = session.copy(
                cards = nextQueue,
                currentIndex = nextIdx,
                isRevealed = false,
                reviewedCount = session.reviewedCount + 1,
                isSubmitting = false,
                isCompleted = done,
                cardShownEpochMs = nextTime,
                currentCardTelemetry = nextCard?.let { computeTelemetry(it, nextTime) },
                ratingPreviews = nextCard?.let { computeRatingPreviews(it, nextTime, session.desiredRetention) }.orEmpty(),
                canUndo = true,
                lastScheduledLabel = "${card.hangul} • ${scheduleResult.formattedInterval}",
                outcomes = updatedOutcomes,
                evaluation = evaluation
            )
        }
    }

    /**
     * Undoes the most recent FSRS rating in this session, restoring the card's exact previous
     * Stability, Difficulty, reps, lapses, and due timestamp.
     */
    fun undoLastRating() {
        val snapshot = lastUndoSnapshot ?: return
        val session = _sessionState.value
        if (session.isSubmitting) return

        _sessionState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            store.restoreVocabState(snapshot.previousVocab)
            lastUndoSnapshot = null

            val now = clock()
            val restoredCard = snapshot.previousVocab
            val restoredQueue = snapshot.previousQueue.toMutableList().apply {
                if (snapshot.previousIndex in indices) {
                    this[snapshot.previousIndex] = restoredCard
                }
            }

            _sessionState.value = session.copy(
                cards = restoredQueue,
                currentIndex = snapshot.previousIndex,
                isRevealed = true,
                reviewedCount = snapshot.previousReviewedCount,
                isSubmitting = false,
                isCompleted = false,
                cardShownEpochMs = now,
                currentCardTelemetry = computeTelemetry(restoredCard, now),
                ratingPreviews = computeRatingPreviews(restoredCard, now, session.desiredRetention),
                canUndo = false,
                lastScheduledLabel = null,
                outcomes = snapshot.previousOutcomes,
                evaluation = null
            )
        }
    }

    /**
     * Direct helper to compute FSRS rating previews for any Korean vocabulary card.
     */
    fun previewCardSchedule(
        vocab: VocabEntity,
        nowEpochMs: Long = clock(),
        desiredRetention: Double = userSettings.value.fsrsDesiredRetention
    ): Map<FsrsRating, FsrsScheduleResult> =
        computeRatingPreviews(vocab, nowEpochMs, desiredRetention)

    private fun computeRatingPreviews(
        vocab: VocabEntity,
        nowEpochMs: Long,
        desiredRetention: Double
    ): Map<FsrsRating, FsrsScheduleResult> =
        FsrsScheduler.previewAll(
            card = vocab.toFsrsSnapshot(),
            nowEpochMs = nowEpochMs,
            desiredRetention = desiredRetention
        )

    private fun computeTelemetry(vocab: VocabEntity, nowEpochMs: Long): FsrsCardTelemetry {
        val snapshot = vocab.toFsrsSnapshot()
        val retrievability = if (snapshot.reps > 0 && snapshot.stability > 0.0) {
            (FsrsScheduler.currentRetrievability(snapshot, nowEpochMs) * 100.0)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            null
        }
        return FsrsCardTelemetry(
            stage = snapshot.stage,
            stabilityDays = snapshot.stability,
            difficulty = snapshot.difficulty,
            retrievabilityPercent = retrievability,
            reps = snapshot.reps,
            lapses = snapshot.lapses,
            isLeech = FsrsScheduler.isLeech(snapshot)
        )
    }

    private fun computeDeckOverview(vocab: List<VocabEntity>, nowEpochMs: Long): FsrsDeckOverview {
        if (vocab.isEmpty()) return FsrsDeckOverview()
        val reviewed = vocab.filter { it.reps > 0 && it.stability > 0.0 }
        val avgR = if (reviewed.isNotEmpty()) {
            (reviewed.map { FsrsScheduler.currentRetrievability(it.toFsrsSnapshot(), nowEpochMs) }.average() * 100.0)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            null
        }
        return FsrsDeckOverview(
            totalCards = vocab.size,
            dueNowCount = BaeumRepository.countDueTodayOnly(vocab, nowEpochMs),
            overdueCount = BaeumRepository.countOverdue(vocab, nowEpochMs),
            newCount = vocab.count { it.stage == CardMemoryStage.NEW.name },
            learningCount = vocab.count { it.stage == CardMemoryStage.LEARNING.name },
            reviewCount = vocab.count { it.stage == CardMemoryStage.REVIEW.name },
            masteredCount = vocab.count { it.stage == CardMemoryStage.MASTERED.name },
            leechCount = vocab.count { FsrsScheduler.isLeech(it.toFsrsSnapshot()) },
            averageRetrievabilityPercent = avgR
        )
    }

    class Factory(
        private val repository: BaeumRepository,
        private val userPreferencesRepository: UserPreferencesRepository = repository.userPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardFsrsViewModel(
                store = BaeumFlashcardFsrsStore(repository, userPreferencesRepository)
            ) as T
        }
    }
}
