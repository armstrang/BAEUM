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
        val r = FsrsScheduler.currentRetrievability(
            reps = card.reps,
            stability = card.stability,
            lastReviewEpochMs = card.lastReviewEpochMs,
            nowEpochMs = nowEpochMs
        )
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
     * Uses Schwartzian urgency caching and progression-bucket frontier selection so
     * 6,600+ card banks resolve in O(N) without full sorting of untouched NEW cards.
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
        val learningStageName = CardMemoryStage.LEARNING.name

        // Single-pass partition into due reviews, quiz-weak reviews, and progression-bucketed new cards
        val dueScored = ArrayList<Pair<VocabEntity, Double>>()
        val weakScored = ArrayList<Pair<VocabEntity, Double>>()
        val newBuckets = if (dueOnly) null else HashMap<Int, ArrayList<VocabEntity>>(12)

        for (card in pool) {
            val isDueOrLearning = card.reps > 0 && (card.dueEpochMs in 1..endOfToday || card.stage == learningStageName)
            if (isDueOrLearning) {
                dueScored.add(card to computeReviewUrgency(card, nowEpochMs))
            } else if (!dueOnly) {
                val isQuizWeak = card.quizWrongCount > card.quizCorrectCount || card.lapses >= 2
                if (isQuizWeak) {
                    weakScored.add(card to computeReviewUrgency(card, nowEpochMs))
                } else if (card.reps == 0) {
                    val orderKey = LEVEL_PROGRESSION_ORDER[card.subCategory] ?: 100
                    newBuckets!!.getOrPut(orderKey) { ArrayList() }.add(card)
                }
            }
        }

        // Precomputed urgency sort (O(k log k) on due cards only, zero comparator recomputation)
        dueScored.sortByDescending { it.second }

        if (dueOnly) {
            val limit = minOf(targetSize, dueScored.size)
            return List(limit) { dueScored[it].first }
        }

        weakScored.sortByDescending { it.second }

        val reviewBucketSize = dueScored.size + weakScored.size
        val reviewQuota = if (reviewBucketSize > 0) {
            minOf(reviewBucketSize, (targetSize * 0.70).toInt().coerceAtLeast(1))
        } else 0
        val newQuota = (targetSize - reviewQuota).coerceAtLeast(0)

        val chosenReviews = ArrayList<VocabEntity>(reviewQuota)
        val seenIds = HashSet<String>(reviewQuota * 2 + 1)
        for (pair in dueScored) {
            if (chosenReviews.size >= reviewQuota) break
            if (seenIds.add(pair.first.id)) chosenReviews.add(pair.first)
        }
        for (pair in weakScored) {
            if (chosenReviews.size >= reviewQuota) break
            if (seenIds.add(pair.first.id)) chosenReviews.add(pair.first)
        }

        // Pull only the lowest curriculum frontier buckets needed to populate the shuffle window
        val frontierTarget = (newQuota * 4).coerceAtLeast(24)
        val frontierWindow = ArrayList<VocabEntity>(frontierTarget)
        if (newQuota > 0 && !newBuckets.isNullOrEmpty()) {
            val sortedOrders = newBuckets.keys.sorted()
            val comparator = compareByDescending<VocabEntity> { if (it.isBookmarked) 1 else 0 }
                .thenBy { it.id }
            for (order in sortedOrders) {
                if (frontierWindow.size >= frontierTarget) break
                val bucket = newBuckets[order] ?: continue
                bucket.sortWith(comparator)
                val remaining = frontierTarget - frontierWindow.size
                val takeCount = minOf(remaining, bucket.size)
                for (i in 0 until takeCount) {
                    frontierWindow.add(bucket[i])
                }
            }
        }
        frontierWindow.shuffle(rng)
        val chosenNew = if (newQuota > 0) frontierWindow.take(newQuota) else emptyList()

        // Interleave new cards smoothly among review cards (ratio ~2 review : 1 new)
        val interleaved = interleave(chosenReviews, chosenNew)

        // Fallback if pool has only future-scheduled cards
        if (interleaved.isEmpty()) {
            val scoredFallback = pool.map { it to computeReviewUrgency(it, nowEpochMs) }
                .sortedByDescending { it.second }
            val limit = minOf(targetSize, scoredFallback.size)
            return List(limit) { scoredFallback[it].first }
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
