package com.example.domain.fsrs

import com.example.data.local.VocabEntity
import kotlin.math.max
import kotlin.random.Random

/**
 * Intelligent Flashcard Queue & Interleaving Algorithm.
 *
 * Prioritizes cards by FSRS forgetting urgency (1 - Retrievability), overdue severity,
 * quiz error cross-signaling, and leech/bookmark weighting, then smoothly interleaves
 * new curriculum frontier cards among review cards to maximize long-term retention
 * while preventing cognitive fatigue.
 */
object FlashcardSessionAlgorithm {

    private const val DAY_MS = 86_400_000L
    private const val HALF_DAY_MS = 43_200_000L

    private val LEVEL_PROGRESSION_ORDER = mapOf(
        "Basic Syllables" to 0,
        "Batchim & Tense" to 1,
        "Sound Changes" to 2,
        "1급" to 10,
        "2급" to 20,
        "3급" to 30,
        "4급" to 40,
        "5급" to 50,
        "6급" to 60
    )

    /**
     * Computes a composite forgetting urgency score for a reviewed card.
     * Higher score = higher immediate priority in the study queue.
     */
    fun computeReviewUrgency(card: VocabEntity, nowEpochMs: Long = System.currentTimeMillis()): Double {
        val snap = card.toFsrsSnapshot()
        val r = FsrsScheduler.currentRetrievability(snap, nowEpochMs)
        val forgettingRisk = (1.0 - r).coerceIn(0.0, 1.0) // Highest when R approaches 0

        val elapsedDays = if (card.lastReviewEpochMs > 0L) {
            max(0.0, (nowEpochMs - card.lastReviewEpochMs).toDouble() / DAY_MS)
        } else 0.0
        val overdueRatio = (elapsedDays / max(1.0, card.scheduledDays)).coerceIn(0.0, 3.0)

        val quizWeakness = ((card.quizWrongCount * 0.35) - (card.quizCorrectCount * 0.08)).coerceIn(0.0, 2.0)
        val lapseWeight = (card.lapses * 0.22).coerceAtMost(1.5)
        val bookmarkBoost = if (card.isBookmarked) 0.18 else 0.0
        val learningBoost = if (card.stage == CardMemoryStage.LEARNING.name) 0.45 else 0.0

        return (forgettingRisk * 2.4) + (overdueRatio * 0.8) + quizWeakness + lapseWeight + bookmarkBoost + learningBoost
    }

    /**
     * Builds an adaptive, interleaved flashcard queue from the filtered vocabulary pool.
     */
    fun buildAdaptiveDeck(
        pool: List<VocabEntity>,
        dailyGoal: Int = 20,
        dueOnly: Boolean = false,
        nowEpochMs: Long = System.currentTimeMillis(),
        maxSessionSize: Int = 15,
        rng: Random = Random.Default
    ): List<VocabEntity> {
        if (pool.isEmpty()) return emptyList()

        val endOfToday = nowEpochMs + HALF_DAY_MS
        val targetSize = if (dueOnly) maxSessionSize.coerceAtLeast(25) else maxSessionSize.coerceIn(10, dailyGoal.coerceIn(15, 25))

        // 1. Due or learning cards sorted by highest forgetting urgency
        val dueReviews = pool
            .filter { it.reps > 0 && (it.dueEpochMs in 1..endOfToday || it.stage == CardMemoryStage.LEARNING.name) }
            .sortedByDescending { computeReviewUrgency(it, nowEpochMs) }

        if (dueOnly) {
            return dueReviews.take(targetSize)
        }

        // 2. Weak cards missed in quizzes even if not strictly due yet
        val dueIds = dueReviews.map { it.id }.toHashSet()
        val quizWeakCards = pool
            .filter { it.id !in dueIds && (it.quizWrongCount > it.quizCorrectCount || it.lapses >= 2) }
            .sortedByDescending { computeReviewUrgency(it, nowEpochMs) }

        val reviewBucket = (dueReviews + quizWeakCards).distinctBy { it.id }

        // 3. New cards ordered along the curriculum frontier (1급 -> 6급) with light local shuffling
        // so consecutive sessions explore diverse words rather than always repeating alphabetically.
        val newAll = pool
            .filter { it.reps == 0 && it.id !in dueIds }
            .sortedWith(
                compareBy<VocabEntity> { LEVEL_PROGRESSION_ORDER[it.subCategory] ?: 100 }
                    .thenByDescending { if (it.isBookmarked) 1 else 0 }
                    .thenBy { it.id }
            )

        // Take a frontier window of up to 4x needed new cards and shuffle within that frontier
        val reviewQuota = if (reviewBucket.isNotEmpty()) {
            minOf(reviewBucket.size, (targetSize * 0.70).toInt().coerceAtLeast(1))
        } else 0
        val newQuota = (targetSize - reviewQuota).coerceAtLeast(0)

        val chosenReviews = reviewBucket.take(reviewQuota)
        val frontierWindow = newAll.take((newQuota * 4).coerceAtLeast(24)).shuffled(rng)
        val chosenNew = frontierWindow.take(newQuota)

        // 4. Interleave new cards smoothly among review cards (ratio ~2 review : 1 new)
        val interleaved = interleave(chosenReviews, chosenNew)

        // Fallback if pool has only future-scheduled cards
        if (interleaved.isEmpty()) {
            return pool.sortedByDescending { computeReviewUrgency(it, nowEpochMs) }.take(targetSize)
        }

        return interleaved
    }

    /**
     * Intra-session re-queueing algorithm:
     * - AGAIN: re-inserts 2–3 slots ahead (capped at max 2 re-queues per card so sessions always terminate).
     * - HARD on first exposure: re-inserts once near the end of the session for active consolidation.
     */
    fun applyIntraSessionRequeue(
        currentQueue: List<VocabEntity>,
        currentIndex: Int,
        updatedCard: VocabEntity,
        rating: FsrsRating,
        wasFirstExposure: Boolean
    ): List<VocabEntity> {
        val nextQueue = currentQueue.toMutableList()
        val occurrences = nextQueue.count { it.id == updatedCard.id }

        when {
            rating == FsrsRating.AGAIN && occurrences < 3 -> {
                val insertAt = minOf(currentIndex + 3, nextQueue.size)
                nextQueue.add(insertAt, updatedCard)
            }
            rating == FsrsRating.HARD && wasFirstExposure && occurrences < 2 && currentIndex < nextQueue.lastIndex -> {
                val insertAt = minOf(currentIndex + 5, nextQueue.size)
                nextQueue.add(insertAt, updatedCard)
            }
        }
        return nextQueue
    }

    private fun interleave(
        reviews: List<VocabEntity>,
        newCards: List<VocabEntity>
    ): List<VocabEntity> {
        if (reviews.isEmpty()) return newCards
        if (newCards.isEmpty()) return reviews

        val result = ArrayList<VocabEntity>(reviews.size + newCards.size)
        var rIdx = 0
        var nIdx = 0
        while (rIdx < reviews.size || nIdx < newCards.size) {
            repeat(2) {
                if (rIdx < reviews.size) result.add(reviews[rIdx++])
            }
            if (nIdx < newCards.size) {
                result.add(newCards[nIdx++])
            }
        }
        return result
    }
}
