package com.example.domain.engagement

import com.example.data.local.VocabEntity
import kotlin.random.Random

/**
 * Single rapid-fire prompt inside 60-Second Seoul Blitz.
 */
data class BlitzPrompt(
    val roundNumber: Int,
    val vocabId: String,
    val hangul: String,
    val romanization: String,
    val stimulusText: String,
    val isHangulPrompt: Boolean,
    val options: List<String>,
    val correctOption: String,
    val meaningPair: String
)

/**
 * Missed word review item surfaced at the end of a Blitz run.
 */
data class BlitzMissedWord(
    val vocabId: String,
    val hangul: String,
    val romanization: String,
    val meaning: String
)

/**
 * Complete immutable state for a 60-Second Seoul Blitz run.
 */
data class SeoulBlitzState(
    val totalDurationSec: Int = DEFAULT_DURATION_SEC,
    val remainingSec: Int = DEFAULT_DURATION_SEC,
    val score: Int = 0,
    val previousBestScore: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
    val timeBonusEarnedSec: Int = 0,
    val lastPointsDelta: Int = 0,
    val lastTimeBonusSec: Int = 0,
    val lastAnswerWasCorrect: Boolean? = null,
    val lastFeedbackSeq: Int = 0,
    val currentPrompt: BlitzPrompt? = null,
    val missedWords: List<BlitzMissedWord> = emptyList(),
    val isFinished: Boolean = false,
    val isNewRecord: Boolean = false,
    val xpEarned: Int = 0,
    val activePathway: String = "",
    val translationLang: String = "EN"
) {
    val multiplier: Int
        get() = when {
            combo >= 15 -> 5
            combo >= 10 -> 4
            combo >= 5 -> 3
            combo >= 2 -> 2
            else -> 1
        }

    val isFeverMode: Boolean get() = combo >= 5 && !isFinished

    val accuracyPercent: Int
        get() = if (answeredCount <= 0) 0 else ((correctCount * 100f) / answeredCount).toInt().coerceIn(0, 100)

    companion object {
        const val DEFAULT_DURATION_SEC = 60
        const val MAX_TIME_CAP_SEC = 75
    }
}

/**
 * Pure deterministic engine for **60-Second Seoul Blitz** ("just one more round!" high-flow Korean recall).
 *
 * Prioritizes active-pathway & due/weak vocabulary while mixing in fresh words so every run
 * genuinely reinforces memory while feeling electric and addictive.
 */
object SeoulBlitzEngine {

    private const val BASE_POINTS_PER_HIT = 25
    private const val FEVER_BONUS_INTERVAL = 5
    private const val FEVER_TIME_BONUS_SEC = 2

    fun startSession(
        allVocab: List<VocabEntity>,
        activePathway: String,
        translationLang: String,
        previousBestScore: Int,
        durationSec: Int = SeoulBlitzState.DEFAULT_DURATION_SEC,
        rng: Random = Random.Default
    ): SeoulBlitzState {
        val pool = selectBlitzPool(allVocab, activePathway, translationLang)
        val firstPrompt = buildPrompt(
            pool = pool,
            roundNumber = 1,
            translationLang = translationLang,
            excludeVocabId = null,
            rng = rng
        )
        return SeoulBlitzState(
            totalDurationSec = durationSec,
            remainingSec = durationSec,
            score = 0,
            previousBestScore = previousBestScore.coerceAtLeast(0),
            currentPrompt = firstPrompt,
            isFinished = firstPrompt == null,
            activePathway = activePathway,
            translationLang = translationLang
        )
    }

    fun submitAnswer(
        state: SeoulBlitzState,
        selectedOption: String,
        allVocab: List<VocabEntity>,
        rng: Random = Random.Default
    ): SeoulBlitzState {
        if (state.isFinished) return state
        val prompt = state.currentPrompt ?: return finishSession(state)

        val isCorrect = selectedOption == prompt.correctOption
        val nextCombo = if (isCorrect) state.combo + 1 else 0
        val nextMaxCombo = maxOf(state.maxCombo, nextCombo)

        val effectiveMultiplier = when {
            nextCombo >= 15 -> 5
            nextCombo >= 10 -> 4
            nextCombo >= 5 -> 3
            nextCombo >= 2 -> 2
            else -> 1
        }
        val pointsDelta = if (isCorrect) BASE_POINTS_PER_HIT * effectiveMultiplier else 0
        val timeBonus = if (isCorrect && nextCombo > 0 && nextCombo % FEVER_BONUS_INTERVAL == 0) {
            FEVER_TIME_BONUS_SEC
        } else {
            0
        }

        val nextScore = state.score + pointsDelta
        val nextRemaining = (state.remainingSec + timeBonus).coerceAtMost(SeoulBlitzState.MAX_TIME_CAP_SEC)
        val nextMissed = if (!isCorrect && state.missedWords.none { it.vocabId == prompt.vocabId }) {
            state.missedWords + BlitzMissedWord(
                vocabId = prompt.vocabId,
                hangul = prompt.hangul,
                romanization = prompt.romanization,
                meaning = prompt.meaningPair
            )
        } else {
            state.missedWords
        }

        val pool = selectBlitzPool(allVocab, state.activePathway, state.translationLang)
        val nextPrompt = buildPrompt(
            pool = pool,
            roundNumber = prompt.roundNumber + 1,
            translationLang = state.translationLang,
            excludeVocabId = prompt.vocabId,
            rng = rng
        )

        return state.copy(
            remainingSec = nextRemaining,
            score = nextScore,
            combo = nextCombo,
            maxCombo = nextMaxCombo,
            answeredCount = state.answeredCount + 1,
            correctCount = state.correctCount + if (isCorrect) 1 else 0,
            timeBonusEarnedSec = state.timeBonusEarnedSec + timeBonus,
            lastPointsDelta = pointsDelta,
            lastTimeBonusSec = timeBonus,
            lastAnswerWasCorrect = isCorrect,
            lastFeedbackSeq = state.lastFeedbackSeq + 1,
            currentPrompt = nextPrompt,
            missedWords = nextMissed
        )
    }

    fun tickSecond(state: SeoulBlitzState): SeoulBlitzState {
        if (state.isFinished) return state
        val nextSec = (state.remainingSec - 1).coerceAtLeast(0)
        val updated = state.copy(remainingSec = nextSec)
        return if (nextSec <= 0) finishSession(updated) else updated
    }

    fun finishSession(state: SeoulBlitzState): SeoulBlitzState {
        if (state.isFinished) return state
        val isNewRecord = state.score > 0 && state.score > state.previousBestScore
        val xp = if (state.answeredCount > 0) {
            BaeumHabitEngine.computeBlitzXp(state.score, isNewRecord)
        } else {
            0
        }
        return state.copy(
            remainingSec = 0,
            isFinished = true,
            isNewRecord = isNewRecord,
            xpEarned = xp
        )
    }

    private fun selectBlitzPool(
        allVocab: List<VocabEntity>,
        activePathway: String,
        translationLang: String
    ): List<VocabEntity> {
        val pathwayMatches = allVocab.filter {
            it.curriculum == activePathway && it.hangul.isNotBlank() && it.meaningFor(translationLang).isNotBlank()
        }
        return if (pathwayMatches.size >= 8) {
            pathwayMatches
        } else {
            allVocab.filter { it.hangul.isNotBlank() && it.meaningFor(translationLang).isNotBlank() }
        }
    }

    private fun buildPrompt(
        pool: List<VocabEntity>,
        roundNumber: Int,
        translationLang: String,
        excludeVocabId: String?,
        rng: Random
    ): BlitzPrompt? {
        if (pool.size < 4) return null

        // Bias 35% of rounds toward weak/reviewed cards when available so Blitz actively heals weak spots
        var target: VocabEntity? = null
        if (rng.nextFloat() < 0.35f) {
            var weakSeen = 0
            for (item in pool) {
                if (item.id == excludeVocabId) continue
                if (item.lapses > 0 || item.quizWrongCount > item.quizCorrectCount || item.reps > 0) {
                    weakSeen++
                    if (rng.nextInt(weakSeen) == 0) target = item
                }
            }
        }
        if (target == null) {
            var idx = rng.nextInt(pool.size)
            if (pool[idx].id == excludeVocabId && pool.size > 1) {
                idx = (idx + 1) % pool.size
            }
            target = pool[idx]
        }

        val hangulPrompt = roundNumber % 3 != 0 // 2/3 Hangul->Meaning, 1/3 Meaning->Hangul for variety
        val targetMeaning = target.meaningFor(translationLang).trim()
        val correctText = if (hangulPrompt) targetMeaning else target.hangul

        val options = ArrayList<String>(4).apply { add(correctText) }
        val used = hashSetOf(correctText.lowercase())

        // Pick 3 clean distinct distractors, preferring same part-of-speech
        var attempts = 0
        while (options.size < 4 && attempts < 64) {
            attempts++
            val cand = pool[rng.nextInt(pool.size)]
            if (cand.id == target.id) continue
            if (attempts < 24 && cand.partOfSpeech != target.partOfSpeech) continue
            val text = if (hangulPrompt) cand.meaningFor(translationLang).trim() else cand.hangul
            if (text.isNotBlank() && used.add(text.lowercase())) {
                options.add(text)
            }
        }
        // Fallback linear scan if random attempts didn't fill all 4
        if (options.size < 4) {
            for (cand in pool) {
                if (options.size >= 4) break
                if (cand.id == target.id) continue
                val text = if (hangulPrompt) cand.meaningFor(translationLang).trim() else cand.hangul
                if (text.isNotBlank() && used.add(text.lowercase())) {
                    options.add(text)
                }
            }
        }
        if (options.size < 4) return null
        options.shuffle(rng)

        return BlitzPrompt(
            roundNumber = roundNumber,
            vocabId = target.id,
            hangul = target.hangul,
            romanization = target.romanization,
            stimulusText = if (hangulPrompt) target.hangul else targetMeaning,
            isHangulPrompt = hangulPrompt,
            options = options,
            correctOption = correctText,
            meaningPair = targetMeaning
        )
    }
}
