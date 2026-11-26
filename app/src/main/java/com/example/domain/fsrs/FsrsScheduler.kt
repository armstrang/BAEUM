package com.example.domain.fsrs

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Genuine FSRS-4.5 (Free Spaced Repetition Scheduler) implementation.
 * Computes Stability (S), Difficulty (D), Retrievability (R), and next review intervals
 * for Again (1), Hard (2), Good (3), and Easy (4) ratings.
 *
 * Memory Stage Definitions (documented for Review Planner & Analytics):
 * - NEW: Never reviewed (reps == 0).
 * - LEARNING: Initial acquisition or relearning phase (intervalDays < 1.0 or state == LEARNING/RELEARNING).
 * - REVIEW: Graduated card with 1.0 <= stability < 21.0 days.
 * - MASTERED: Graduated card with stability >= 21.0 days and reps >= 3 (high long-term retention).
 */
enum class FsrsRating(val value: Int, val label: String) {
    AGAIN(1, "Again"),
    HARD(2, "Hard"),
    GOOD(3, "Good"),
    EASY(4, "Easy")
}

enum class CardMemoryStage {
    NEW,
    LEARNING,
    REVIEW,
    MASTERED
}

data class FsrsCardSnapshot(
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val elapsedDays: Double = 0.0,
    val scheduledDays: Double = 0.0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val stage: CardMemoryStage = CardMemoryStage.NEW,
    val lastReviewEpochMs: Long = 0L,
    val dueEpochMs: Long = 0L
)

data class FsrsScheduleResult(
    val nextState: FsrsCardSnapshot,
    val intervalDays: Double,
    val formattedInterval: String
)

object FsrsScheduler {
    // FSRS-4.5 default 17 parameters
    private val w = doubleArrayOf(
        0.4072, 1.1829, 3.1262, 15.4722,
        7.2102, 0.5316, 1.0651, 0.0234,
        1.616, 0.1544, 1.0824, 1.9813,
        0.0953, 0.2975, 2.2042, 0.2407,
        2.9466
    )

    private const val DECAY = -0.5
    private const val FACTOR = 19.0 / 81.0
    private const val DAY_MS = 86_400_000L
    private const val MIN_MS = 60_000L
    private const val MASTERED_STABILITY_DAYS = 21.0
    private const val LEECH_LAPSE_THRESHOLD = 4
    // FSRS-5 short-term same-day review stability parameters
    private const val W17_SHORT_TERM = 0.51655
    private const val W18_SHORT_TERM = 0.6621

    fun retrievability(elapsedDays: Double, stability: Double): Double {
        if (stability <= 0.0) return 0.0
        return (1.0 + FACTOR * (max(0.0, elapsedDays) / stability)).pow(DECAY)
    }

    fun currentRetrievability(
        card: FsrsCardSnapshot,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Double {
        if (card.reps == 0 || card.stability <= 0.0) return 0.0
        val elapsed = if (card.lastReviewEpochMs > 0L) {
            max(0.0, (nowEpochMs - card.lastReviewEpochMs).toDouble() / DAY_MS)
        } else 0.0
        return retrievability(elapsed, card.stability)
    }

    fun isLeech(card: FsrsCardSnapshot): Boolean = card.lapses >= LEECH_LAPSE_THRESHOLD

    fun previewAll(
        card: FsrsCardSnapshot,
        nowEpochMs: Long = System.currentTimeMillis(),
        desiredRetention: Double = 0.90
    ): Map<FsrsRating, FsrsScheduleResult> = FsrsRating.entries.associateWith { rating ->
        schedule(card, rating, nowEpochMs, desiredRetention)
    }

    fun schedule(
        card: FsrsCardSnapshot,
        rating: FsrsRating,
        nowEpochMs: Long = System.currentTimeMillis(),
        desiredRetention: Double = 0.90
    ): FsrsScheduleResult {
        val retention = desiredRetention.coerceIn(0.75, 0.97)
        val elapsedDays = if (card.lastReviewEpochMs > 0L) {
            max(0.0, (nowEpochMs - card.lastReviewEpochMs).toDouble() / DAY_MS)
        } else {
            0.0
        }

        val isFirstReview = card.reps == 0 || card.stage == CardMemoryStage.NEW
        val newDifficulty: Double
        val newStability: Double
        val newLapses: Int
        val intervalDays: Double

        if (isFirstReview) {
            newDifficulty = initDifficulty(rating).coerceIn(1.0, 10.0)
            newStability = initStability(rating)
            newLapses = if (rating == FsrsRating.AGAIN) card.lapses + 1 else card.lapses
            intervalDays = when (rating) {
                FsrsRating.AGAIN -> 1.0 / (24.0 * 60.0) // 1 min
                FsrsRating.HARD -> 6.0 / (24.0 * 60.0)  // 6 min
                FsrsRating.GOOD -> 10.0 / (24.0 * 60.0) // 10 min
                FsrsRating.EASY -> max(1.0, nextInterval(newStability, retention))
            }
        } else {
            val r = retrievability(elapsedDays, card.stability)
            newDifficulty = nextDifficulty(card.difficulty, rating)
            val isSameDayShortTerm = elapsedDays < 1.0
            if (rating == FsrsRating.AGAIN) {
                newStability = if (isSameDayShortTerm) {
                    shortTermStability(card.stability, rating)
                } else {
                    nextForgetStability(newDifficulty, card.stability, r)
                }
                newLapses = card.lapses + 1
                intervalDays = 5.0 / (24.0 * 60.0) // 5 min relearn step
            } else {
                newStability = if (isSameDayShortTerm) {
                    shortTermStability(card.stability, rating)
                } else {
                    nextRecallStability(newDifficulty, card.stability, r, rating)
                }
                newLapses = card.lapses
                val rawInterval = nextInterval(newStability, retention)
                intervalDays = when (rating) {
                    FsrsRating.HARD -> max(1.0, rawInterval * 0.8)
                    FsrsRating.GOOD -> max(1.0, rawInterval)
                    FsrsRating.EASY -> max(2.0, rawInterval * 1.3)
                    FsrsRating.AGAIN -> 5.0 / (24.0 * 60.0)
                }
            }
        }

        val newReps = card.reps + 1
        val nextStage = computeStage(
            reps = newReps,
            stability = newStability,
            intervalDays = intervalDays,
            rating = rating
        )

        val dueDeltaMs = if (intervalDays < 1.0) {
            max(MIN_MS, (intervalDays * DAY_MS).toLong())
        } else {
            (intervalDays.roundToInt().coerceAtLeast(1) * DAY_MS)
        }

        val updated = FsrsCardSnapshot(
            stability = newStability,
            difficulty = newDifficulty,
            elapsedDays = elapsedDays,
            scheduledDays = intervalDays,
            reps = newReps,
            lapses = newLapses,
            stage = nextStage,
            lastReviewEpochMs = nowEpochMs,
            dueEpochMs = nowEpochMs + dueDeltaMs
        )

        return FsrsScheduleResult(
            nextState = updated,
            intervalDays = intervalDays,
            formattedInterval = formatInterval(intervalDays)
        )
    }

    private fun initStability(rating: FsrsRating): Double =
        max(0.1, w[rating.value - 1])

    private fun initDifficulty(rating: FsrsRating): Double =
        w[4] - exp(w[5] * (rating.value - 1)) + 1.0

    private fun nextDifficulty(currentD: Double, rating: FsrsRating): Double {
        val nextD = currentD - w[6] * (rating.value - 3)
        // Mean reversion toward initial Easy difficulty
        val d0 = initDifficulty(FsrsRating.GOOD)
        return (w[7] * d0 + (1.0 - w[7]) * nextD).coerceIn(1.0, 10.0)
    }

    private fun nextRecallStability(
        d: Double,
        s: Double,
        r: Double,
        rating: FsrsRating
    ): Double {
        val hardPenalty = if (rating == FsrsRating.HARD) w[15] else 1.0
        val easyBonus = if (rating == FsrsRating.EASY) w[16] else 1.0
        val growth = exp(w[8]) *
            (11.0 - d) *
            s.pow(-w[9]) *
            (exp((1.0 - r) * w[10]) - 1.0) *
            hardPenalty *
            easyBonus
        return max(0.1, s * (1.0 + growth))
    }

    private fun nextForgetStability(d: Double, s: Double, r: Double): Double {
        val forget = w[11] *
            d.pow(-w[12]) *
            ((s + 1.0).pow(w[13]) - 1.0) *
            exp((1.0 - r) * w[14])
        return min(s, max(0.1, forget))
    }

    /**
     * FSRS-5 short-term stability update for same-day reviews (elapsedDays < 1.0).
     */
    private fun shortTermStability(s: Double, rating: FsrsRating): Double {
        val updated = s * exp(W17_SHORT_TERM * (rating.value - 3 + W18_SHORT_TERM))
        return max(0.1, updated)
    }

    private fun nextInterval(stability: Double, desiredRetention: Double): Double {
        val interval = (stability / FACTOR) * (desiredRetention.pow(1.0 / DECAY) - 1.0)
        return interval.coerceIn(1.0, 3650.0)
    }

    fun computeStage(
        reps: Int,
        stability: Double,
        intervalDays: Double,
        rating: FsrsRating? = null
    ): CardMemoryStage {
        if (reps == 0) return CardMemoryStage.NEW
        if (rating == FsrsRating.AGAIN || intervalDays < 1.0) return CardMemoryStage.LEARNING
        if (stability >= MASTERED_STABILITY_DAYS && reps >= 3) return CardMemoryStage.MASTERED
        return CardMemoryStage.REVIEW
    }

    fun formatInterval(intervalDays: Double): String {
        val minutes = (intervalDays * 24.0 * 60.0).roundToInt()
        return when {
            minutes <= 1 -> "1m"
            minutes < 60 -> "${minutes}m"
            intervalDays < 1.0 -> "${(intervalDays * 24.0).roundToInt()}h"
            intervalDays < 30.0 -> "${intervalDays.roundToInt()}d"
            intervalDays < 365.0 -> "${(intervalDays / 30.0).roundToInt()}mo"
            else -> {
                val years = intervalDays / 365.0
                if (years < 10.0) String.format("%.1fy", years) else "${years.roundToInt()}y"
            }
        }
    }
}
