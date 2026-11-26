package com.example.domain.fsrs

import java.util.Locale
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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
    private const val MIN_STABILITY_DAYS = 0.1
    private const val MAX_STABILITY_DAYS = 3650.0
    private const val MAX_INTERVAL_DAYS = 3650.0
    private const val DEFAULT_RETENTION = 0.90
    private const val MASTERED_STABILITY_DAYS = 21.0
    private const val LEECH_LAPSE_THRESHOLD = 4
    // FSRS-5 short-term same-day review stability parameters
    private const val W17_SHORT_TERM = 0.51655
    private const val W18_SHORT_TERM = 0.6621

    fun retrievability(elapsedDays: Double, stability: Double): Double {
        if (!elapsedDays.isFinite() || !stability.isFinite() || stability <= 0.0) return 0.0
        return (1.0 + FACTOR * (max(0.0, elapsedDays) / stability))
            .pow(DECAY)
            .coerceIn(0.0, 1.0)
    }

    fun currentRetrievability(
        card: FsrsCardSnapshot,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Double {
        if (card.reps <= 0 || !card.stability.isFinite() || card.stability <= 0.0) return 0.0
        val elapsed = if (card.lastReviewEpochMs > 0L) {
            max(0.0, (nowEpochMs - card.lastReviewEpochMs).toDouble() / DAY_MS)
        } else 0.0
        return retrievability(elapsed, card.stability)
    }

    fun isLeech(card: FsrsCardSnapshot): Boolean = card.lapses >= LEECH_LAPSE_THRESHOLD

    fun previewAll(
        card: FsrsCardSnapshot,
        nowEpochMs: Long = System.currentTimeMillis(),
        desiredRetention: Double = DEFAULT_RETENTION
    ): Map<FsrsRating, FsrsScheduleResult> = FsrsRating.entries.associateWith { rating ->
        schedule(card, rating, nowEpochMs, desiredRetention)
    }

    fun schedule(
        card: FsrsCardSnapshot,
        rating: FsrsRating,
        nowEpochMs: Long = System.currentTimeMillis(),
        desiredRetention: Double = DEFAULT_RETENTION
    ): FsrsScheduleResult {
        val retention = if (desiredRetention.isFinite()) {
            desiredRetention.coerceIn(0.75, 0.97)
        } else {
            DEFAULT_RETENTION
        }
        val elapsedDays = if (card.lastReviewEpochMs > 0L) {
            max(0.0, (nowEpochMs - card.lastReviewEpochMs).toDouble() / DAY_MS)
        } else {
            0.0
        }

        val safeReps = card.reps.coerceAtLeast(0)
        val safeLapses = card.lapses.coerceAtLeast(0)
        val isFirstReview = safeReps == 0 || card.stage == CardMemoryStage.NEW
        val newDifficulty: Double
        val newStability: Double
        val newLapses: Int
        val intervalDays: Double

        if (isFirstReview) {
            newDifficulty = initDifficulty(rating).coerceIn(1.0, 10.0)
            newStability = initStability(rating)
            newLapses = if (rating == FsrsRating.AGAIN) safeLapses + 1 else safeLapses
            intervalDays = when (rating) {
                FsrsRating.AGAIN -> 1.0 / (24.0 * 60.0) // 1 min
                FsrsRating.HARD -> 6.0 / (24.0 * 60.0)  // 6 min
                FsrsRating.GOOD -> 10.0 / (24.0 * 60.0) // 10 min
                FsrsRating.EASY -> nextInterval(newStability, retention).coerceIn(1.0, MAX_INTERVAL_DAYS)
            }
        } else {
            val safeS = if (card.stability.isFinite() && card.stability > 0.0) {
                card.stability.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
            } else {
                initStability(FsrsRating.GOOD)
            }
            val safeD = if (card.difficulty.isFinite() && card.difficulty > 0.0) {
                card.difficulty.coerceIn(1.0, 10.0)
            } else {
                initDifficulty(FsrsRating.GOOD).coerceIn(1.0, 10.0)
            }
            val r = retrievability(elapsedDays, safeS)
            newDifficulty = nextDifficulty(safeD, rating)
            val isSameDayShortTerm = elapsedDays < 1.0
            if (rating == FsrsRating.AGAIN) {
                newStability = if (isSameDayShortTerm) {
                    shortTermStability(safeS, rating)
                } else {
                    nextForgetStability(newDifficulty, safeS, r)
                }
                newLapses = safeLapses + 1
                intervalDays = 5.0 / (24.0 * 60.0) // 5 min relearn step
            } else {
                newStability = if (isSameDayShortTerm) {
                    shortTermStability(safeS, rating)
                } else {
                    nextRecallStability(newDifficulty, safeS, r, rating)
                }
                newLapses = safeLapses
                val rawInterval = nextInterval(newStability, retention)
                intervalDays = when (rating) {
                    FsrsRating.HARD -> (rawInterval * 0.8).coerceIn(1.0, MAX_INTERVAL_DAYS)
                    FsrsRating.GOOD -> rawInterval.coerceIn(1.0, MAX_INTERVAL_DAYS)
                    FsrsRating.EASY -> (rawInterval * 1.3).coerceIn(2.0, MAX_INTERVAL_DAYS)
                    FsrsRating.AGAIN -> 5.0 / (24.0 * 60.0)
                }
            }
        }

        val boundedStability = if (newStability.isFinite()) {
            newStability.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        } else {
            MIN_STABILITY_DAYS
        }
        val boundedDifficulty = if (newDifficulty.isFinite()) {
            newDifficulty.coerceIn(1.0, 10.0)
        } else {
            5.0
        }
        val boundedInterval = if (intervalDays.isFinite() && intervalDays > 0.0) {
            intervalDays.coerceAtMost(MAX_INTERVAL_DAYS)
        } else {
            1.0 / (24.0 * 60.0)
        }

        val newReps = safeReps + 1
        val nextStage = computeStage(
            reps = newReps,
            stability = boundedStability,
            intervalDays = boundedInterval,
            rating = rating
        )

        val dueDeltaMs = if (boundedInterval < 1.0) {
            max(MIN_MS, (boundedInterval * DAY_MS).roundToLong())
        } else {
            boundedInterval.roundToLong().coerceAtLeast(1L) * DAY_MS
        }

        val updated = FsrsCardSnapshot(
            stability = boundedStability,
            difficulty = boundedDifficulty,
            elapsedDays = elapsedDays,
            scheduledDays = boundedInterval,
            reps = newReps,
            lapses = newLapses,
            stage = nextStage,
            lastReviewEpochMs = nowEpochMs,
            dueEpochMs = nowEpochMs + dueDeltaMs
        )

        return FsrsScheduleResult(
            nextState = updated,
            intervalDays = boundedInterval,
            formattedInterval = formatInterval(boundedInterval)
        )
    }

    private fun initStability(rating: FsrsRating): Double =
        w[rating.value - 1].coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)

    private fun initDifficulty(rating: FsrsRating): Double =
        (w[4] - exp(w[5] * (rating.value - 1)) + 1.0).coerceIn(1.0, 10.0)

    private fun nextDifficulty(currentD: Double, rating: FsrsRating): Double {
        val nextD = currentD - w[6] * (rating.value - 3)
        // Mean reversion toward initial Good difficulty
        val d0 = initDifficulty(FsrsRating.GOOD)
        return (w[7] * d0 + (1.0 - w[7]) * nextD).coerceIn(1.0, 10.0)
    }

    private fun nextRecallStability(
        d: Double,
        s: Double,
        r: Double,
        rating: FsrsRating
    ): Double {
        val safeS = s.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        val safeD = d.coerceIn(1.0, 10.0)
        val safeR = r.coerceIn(0.0, 1.0)
        val hardPenalty = if (rating == FsrsRating.HARD) w[15] else 1.0
        val easyBonus = if (rating == FsrsRating.EASY) w[16] else 1.0
        val growth = exp(w[8]) *
            (11.0 - safeD) *
            safeS.pow(-w[9]) *
            (exp((1.0 - safeR) * w[10]) - 1.0) *
            hardPenalty *
            easyBonus
        val result = safeS * (1.0 + growth)
        return if (result.isFinite()) {
            result.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        } else {
            MAX_STABILITY_DAYS
        }
    }

    private fun nextForgetStability(d: Double, s: Double, r: Double): Double {
        val safeS = s.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        val safeD = d.coerceIn(1.0, 10.0)
        val safeR = r.coerceIn(0.0, 1.0)
        val forget = w[11] *
            safeD.pow(-w[12]) *
            ((safeS + 1.0).pow(w[13]) - 1.0) *
            exp((1.0 - safeR) * w[14])
        val bounded = if (forget.isFinite()) forget else MIN_STABILITY_DAYS
        return min(safeS, bounded.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS))
    }

    /**
     * FSRS-5 short-term stability update for same-day reviews (elapsedDays < 1.0),
     * bounded so repeated same-day reviews cannot explode stability or increase stability on AGAIN.
     */
    private fun shortTermStability(s: Double, rating: FsrsRating): Double {
        val safeS = s.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        val factor = exp(W17_SHORT_TERM * (rating.value - 3 + W18_SHORT_TERM))
        val updated = safeS * factor
        val finite = if (updated.isFinite()) updated else safeS
        return when (rating) {
            FsrsRating.AGAIN -> min(safeS, finite.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS))
            FsrsRating.HARD -> min(safeS * 1.15, finite).coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
            FsrsRating.GOOD -> min(safeS * 1.5, finite).coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
            FsrsRating.EASY -> min(safeS * 2.0, finite).coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        }
    }

    private fun nextInterval(stability: Double, desiredRetention: Double): Double {
        val safeS = stability.coerceIn(MIN_STABILITY_DAYS, MAX_STABILITY_DAYS)
        val safeR = desiredRetention.coerceIn(0.75, 0.97)
        val interval = (safeS / FACTOR) * (safeR.pow(1.0 / DECAY) - 1.0)
        return if (interval.isFinite()) {
            interval.coerceIn(1.0, MAX_INTERVAL_DAYS)
        } else {
            MAX_INTERVAL_DAYS
        }
    }

    fun computeStage(
        reps: Int,
        stability: Double,
        intervalDays: Double,
        rating: FsrsRating? = null
    ): CardMemoryStage {
        if (reps <= 0) return CardMemoryStage.NEW
        if (rating == FsrsRating.AGAIN || intervalDays < 1.0) return CardMemoryStage.LEARNING
        if (stability >= MASTERED_STABILITY_DAYS && reps >= 3) return CardMemoryStage.MASTERED
        return CardMemoryStage.REVIEW
    }

    fun formatInterval(intervalDays: Double): String {
        if (!intervalDays.isFinite() || intervalDays <= 0.0) return "1m"
        val minutes = (intervalDays * 24.0 * 60.0).roundToLong()
        return when {
            minutes <= 1L -> "1m"
            minutes < 60L -> "${minutes}m"
            intervalDays < 1.0 -> "${(intervalDays * 24.0).roundToInt()}h"
            intervalDays < 30.0 -> "${intervalDays.roundToInt()}d"
            intervalDays < 365.0 -> "${(intervalDays / 30.0).roundToInt()}mo"
            else -> {
                val years = intervalDays / 365.0
                if (years < 10.0) String.format(Locale.US, "%.1fy", years) else "${years.roundToInt()}y"
            }
        }
    }
}
