package com.example.domain.quiz

import com.example.data.local.VocabEntity
import com.example.domain.scoring.JamoAnswerEvaluation
import com.example.domain.scoring.LearningScoringEngine
import com.example.domain.scoring.QuizAnswerTelemetry
import com.example.domain.scoring.QuizSessionEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

/**
 * Cumulative progress statistics tracked per [QuizMode] across sessions.
 */
data class ModeProgressStats(
    val mode: QuizMode,
    val sessionsCompleted: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val bestWeightedScore: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedEpochMs: Long = 0L
) {
    val accuracyPercent: Int
        get() = if (totalQuestionsAnswered == 0) 0
        else ((totalCorrect.toDouble() / totalQuestionsAnswered) * 100.0).roundToInt()
}

/**
 * Real-time progress snapshot for an active quiz session.
 */
data class QuizProgressSnapshot(
    val currentQuestionNumber: Int,
    val totalQuestions: Int,
    val answeredCount: Int,
    val correctCount: Int,
    val completionFraction: Float,
    val currentStreak: Int,
    val maxStreak: Int,
    val earnedPoints: Int,
    val maxPointsSoFar: Int,
    val runningAccuracyPercent: Int,
    val elapsedSeconds: Int
)

/**
 * Complete state managed by [QuizManager] for any quiz type
 * (Vocabulary Recall, Sentence Arrangement, Listening, Batchim, Grammar, Typing, Conjugation, Mixed).
 */
data class QuizSessionState(
    val mode: QuizMode = QuizMode.RECALL,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: String? = null,
    val typedInput: String = "",
    // Stores indices into currentQuestion.options in the exact order tapped by the user
    val arrangedTokenIndices: List<Int> = emptyList(),
    val isChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val lastEvaluationHint: String = "",
    val correctCount: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val earnedPoints: Int = 0,
    val missedQuestions: List<QuizQuestion> = emptyList(),
    val telemetry: List<QuizAnswerTelemetry> = emptyList(),
    val evaluation: QuizSessionEvaluation? = null,
    val isCompleted: Boolean = false,
    val startedAtEpochMs: Long = System.currentTimeMillis()
) {
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)

    val arrangedSentencePreview: String
        get() {
            val q = currentQuestion ?: return ""
            return arrangedTokenIndices.mapNotNull { q.options.getOrNull(it) }.joinToString(" ")
        }

    val canSubmitCurrent: Boolean
        get() {
            val q = currentQuestion ?: return false
            if (isChecked) return false
            return when {
                q.isTypingMode -> typedInput.isNotBlank()
                q.isArrangementMode -> arrangedTokenIndices.size == q.options.size && q.options.isNotEmpty()
                else -> selectedOption != null
            }
        }
}

data class QuizSubmissionOutcome(
    val question: QuizQuestion,
    val evaluation: JamoAnswerEvaluation,
    val pointsAwarded: Int,
    val updatedState: QuizSessionState
)

data class QuizCompletionRecord(
    val mode: QuizMode,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val durationSec: Int,
    val weakTopicNote: String,
    val evaluation: QuizSessionEvaluation
)

/**
 * Orchestrates adaptive question selection, real-time score calculation, and multi-session
 * progress tracking across all Korean quiz modes (including Vocabulary Recall and Sentence Arrangement).
 *
 * Key responsibilities:
 * 1. Adaptive Selection: Prioritizes weak/lapsed/due cards, avoids recently seen items across
 *    consecutive sessions, and adjusts difficulty emphasis based on rolling learner accuracy.
 * 2. Score Calculation: Evaluates Multiple Choice, Hangul Typing (Jamo similarity), and
 *    Sentence Arrangement (SOV token order) with weighted difficulty points and streak bonuses.
 * 3. Progress Tracking: Maintains real-time session progress plus per-mode historical stats.
 */
class QuizManager(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    private val _sessionState = MutableStateFlow<QuizSessionState?>(null)
    val sessionState: StateFlow<QuizSessionState?> = _sessionState.asStateFlow()

    private val _modeProgress = MutableStateFlow<Map<QuizMode, ModeProgressStats>>(emptyMap())
    val modeProgress: StateFlow<Map<QuizMode, ModeProgressStats>> = _modeProgress.asStateFlow()

    // Ring buffer of recently presented vocab IDs so consecutive quizzes feel fresh
    private val recentVocabIds = ArrayDeque<String>(64)
    private val maxRecentHistory = 48

    /**
     * Starts a new adaptive quiz session for the requested [mode].
     */
    fun startSession(
        mode: QuizMode,
        allVocab: List<VocabEntity>,
        translationLang: String = "EN",
        maxCount: Int = 8,
        weakOnly: Boolean = false,
        focusGrammarId: String? = null,
        activeCurriculum: String? = null
    ): QuizSessionState {
        val adaptedPool = buildAdaptiveCandidatePool(
            mode = mode,
            allVocab = allVocab,
            weakOnly = weakOnly
        )

        val questions = AdaptiveQuizEngine.generateQuestions(
            mode = mode,
            allVocab = adaptedPool,
            translationLang = translationLang,
            maxCount = maxCount,
            weakOnly = weakOnly,
            focusGrammarId = focusGrammarId,
            activeCurriculum = activeCurriculum
        )

        recordPresentedVocab(questions)

        val state = QuizSessionState(
            mode = mode,
            questions = questions,
            currentIndex = 0,
            isCompleted = questions.isEmpty(),
            startedAtEpochMs = timeProvider()
        )
        _sessionState.value = state
        return state
    }

    /**
     * Pre-filters/reorders vocabulary candidates using rolling mode mastery & recency avoidance:
     * - Deprioritizes words already quizzed in the most recent sessions (unless weakOnly or tiny bank).
     * - When learner accuracy in this mode is >= 85%, subtly elevates intermediate/advanced items.
     */
    private fun buildAdaptiveCandidatePool(
        mode: QuizMode,
        allVocab: List<VocabEntity>,
        weakOnly: Boolean
    ): List<VocabEntity> {
        if (allVocab.size <= 16 || weakOnly) return allVocab

        val recentSet = recentVocabIds.toHashSet()
        val freshPool = allVocab.filter { it.id !in recentSet }
        val basePool = if (freshPool.size >= 24) freshPool else allVocab

        val modeStats = _modeProgress.value[mode]
        val highPerformer = modeStats != null && modeStats.sessionsCompleted >= 1 && modeStats.accuracyPercent >= 85
        if (!highPerformer) return basePool

        val challenging = basePool.filter {
            it.subCategory in setOf("2급", "3급", "4급", "5급", "6급") || it.quizWrongCount > 0 || it.lapses > 0
        }
        return if (challenging.size >= 20) challenging else basePool
    }

    private fun recordPresentedVocab(questions: List<QuizQuestion>) {
        questions.mapNotNull { it.vocabId }.forEach { id ->
            if (recentVocabIds.size >= maxRecentHistory) {
                recentVocabIds.removeFirst()
            }
            recentVocabIds.addLast(id)
        }
    }

    fun selectOption(option: String) {
        _sessionState.update { state ->
            if (state == null || state.isChecked) state
            else state.copy(selectedOption = option)
        }
    }

    fun updateTypedInput(text: String) {
        _sessionState.update { state ->
            if (state == null || state.isChecked) state
            else state.copy(typedInput = text)
        }
    }

    /**
     * Appends a scrambled phrase chip (by its index in `currentQuestion.options`) to the user's arranged sentence.
     */
    fun appendArrangementToken(optionIndex: Int) {
        _sessionState.update { state ->
            val q = state?.currentQuestion ?: return@update state
            if (state.isChecked || !q.isArrangementMode) return@update state
            if (optionIndex !in q.options.indices || optionIndex in state.arrangedTokenIndices) return@update state
            state.copy(arrangedTokenIndices = state.arrangedTokenIndices + optionIndex)
        }
    }

    /**
     * Removes a placed phrase chip at [placedPosition] back into the available bank.
     */
    fun removeArrangementToken(placedPosition: Int) {
        _sessionState.update { state ->
            val q = state?.currentQuestion ?: return@update state
            if (state.isChecked || !q.isArrangementMode) return@update state
            if (placedPosition !in state.arrangedTokenIndices.indices) return@update state
            val updated = state.arrangedTokenIndices.toMutableList().apply { removeAt(placedPosition) }
            state.copy(arrangedTokenIndices = updated)
        }
    }

    fun clearArrangement() {
        _sessionState.update { state ->
            if (state == null || state.isChecked) state
            else state.copy(arrangedTokenIndices = emptyList())
        }
    }

    /**
     * Evaluates the user's current answer, updates streaks & weighted points, and returns the outcome.
     */
    fun submitCurrentAnswer(): QuizSubmissionOutcome? {
        val state = _sessionState.value ?: return null
        val q = state.currentQuestion ?: return null
        if (state.isChecked) return null

        val userAnswer = when {
            q.isTypingMode -> state.typedInput.trim()
            q.isArrangementMode -> state.arrangedSentencePreview.trim()
            else -> state.selectedOption.orEmpty().trim()
        }
        if (userAnswer.isBlank()) return null

        val eval = AdaptiveQuizEngine.evaluateAnswer(q, userAnswer)
        val correct = eval.isCorrect
        val awardedPts = when {
            correct -> q.points
            eval.isNearMiss -> (q.points * 0.25).roundToInt()
            else -> 0
        }
        val nextStreak = if (correct) state.currentStreak + 1 else 0
        val nextMaxStreak = maxOf(state.maxStreak, nextStreak)

        val itemTelemetry = QuizAnswerTelemetry(
            questionId = q.id,
            skillTag = q.skillTag,
            difficultyLevel = q.difficultyLevel,
            points = q.points,
            isCorrect = correct,
            jamoSimilarity = eval.similarity
        )

        val updated = state.copy(
            isChecked = true,
            isCorrect = correct,
            lastEvaluationHint = eval.feedbackHint,
            correctCount = if (correct) state.correctCount + 1 else state.correctCount,
            currentStreak = nextStreak,
            maxStreak = nextMaxStreak,
            earnedPoints = state.earnedPoints + awardedPts,
            missedQuestions = if (correct) state.missedQuestions else state.missedQuestions + q,
            telemetry = state.telemetry + itemTelemetry
        )
        _sessionState.value = updated

        return QuizSubmissionOutcome(
            question = q,
            evaluation = eval,
            pointsAwarded = awardedPts,
            updatedState = updated
        )
    }

    /**
     * Advances to the next question, or finalizes scoring & mode progress tracking if this was the last question.
     * Returns a [QuizCompletionRecord] when the session finishes, or `null` if more questions remain.
     */
    fun advanceToNext(): QuizCompletionRecord? {
        val state = _sessionState.value ?: return null
        val nextIdx = state.currentIndex + 1

        if (nextIdx >= state.questions.size) {
            val now = timeProvider()
            val durationSec = ((now - state.startedAtEpochMs) / 1000L).toInt().coerceAtLeast(1)
            val evaluation = LearningScoringEngine.evaluateQuizSession(state.telemetry, durationSec)
            val weakNote = evaluation.weakestSkill
                ?: state.missedQuestions.firstOrNull()?.promptLabel
                ?: "No errors"

            _sessionState.value = state.copy(
                isCompleted = true,
                evaluation = evaluation
            )

            // Update cumulative per-mode progress tracking
            _modeProgress.update { currentMap ->
                val prev = currentMap[state.mode] ?: ModeProgressStats(mode = state.mode)
                val updatedStats = prev.copy(
                    sessionsCompleted = prev.sessionsCompleted + 1,
                    totalQuestionsAnswered = prev.totalQuestionsAnswered + state.questions.size,
                    totalCorrect = prev.totalCorrect + state.correctCount,
                    bestWeightedScore = maxOf(prev.bestWeightedScore, evaluation.weightedMasteryScore),
                    bestStreak = maxOf(prev.bestStreak, evaluation.maxStreak),
                    lastCompletedEpochMs = now
                )
                currentMap + (state.mode to updatedStats)
            }

            return QuizCompletionRecord(
                mode = state.mode,
                totalQuestions = state.questions.size,
                correctAnswers = state.correctCount,
                durationSec = durationSec,
                weakTopicNote = weakNote,
                evaluation = evaluation
            )
        } else {
            _sessionState.value = state.copy(
                currentIndex = nextIdx,
                selectedOption = null,
                typedInput = "",
                arrangedTokenIndices = emptyList(),
                isChecked = false,
                isCorrect = false,
                lastEvaluationHint = ""
            )
            return null
        }
    }

    /**
     * Returns a live progress & scoring snapshot for UI progress bars, streak counters, and analytics.
     */
    fun getCurrentProgress(): QuizProgressSnapshot? {
        val state = _sessionState.value ?: return null
        val total = state.questions.size.coerceAtLeast(1)
        val answered = state.telemetry.size
        val maxPtsSoFar = state.telemetry.sumOf { it.points }
        val runningAcc = if (answered == 0) 0 else ((state.correctCount.toDouble() / answered) * 100.0).roundToInt()
        val elapsedSec = ((timeProvider() - state.startedAtEpochMs) / 1000L).toInt().coerceAtLeast(0)

        return QuizProgressSnapshot(
            currentQuestionNumber = (state.currentIndex + 1).coerceAtMost(total),
            totalQuestions = total,
            answeredCount = answered,
            correctCount = state.correctCount,
            completionFraction = (answered.toFloat() / total.toFloat()).coerceIn(0f, 1f),
            currentStreak = state.currentStreak,
            maxStreak = state.maxStreak,
            earnedPoints = state.earnedPoints,
            maxPointsSoFar = maxPtsSoFar,
            runningAccuracyPercent = runningAcc,
            elapsedSeconds = elapsedSec
        )
    }

    fun getProgressForMode(mode: QuizMode): ModeProgressStats =
        _modeProgress.value[mode] ?: ModeProgressStats(mode = mode)

    /**
     * Seeds historical per-mode progress stats from persisted database quiz records.
     */
    fun hydrateHistoricalProgress(
        records: List<Triple<String, Int, Int>> // (modeTitle, totalQuestions, correctAnswers)
    ) {
        if (records.isEmpty()) return
        val grouped = records.groupBy { it.first }
        val hydrated = mutableMapOf<QuizMode, ModeProgressStats>()
        QuizMode.entries.forEach { mode ->
            val matching = grouped[mode.title].orEmpty()
            if (matching.isNotEmpty()) {
                val totalQ = matching.sumOf { it.second }
                val totalC = matching.sumOf { it.third }
                val bestPct = matching.maxOf { (_, q, c) ->
                    if (q > 0) ((c.toDouble() / q) * 100.0).roundToInt() else 0
                }
                hydrated[mode] = ModeProgressStats(
                    mode = mode,
                    sessionsCompleted = matching.size,
                    totalQuestionsAnswered = totalQ,
                    totalCorrect = totalC,
                    bestWeightedScore = bestPct
                )
            }
        }
        if (hydrated.isNotEmpty()) {
            _modeProgress.update { existing ->
                val merged = existing.toMutableMap()
                hydrated.forEach { (mode, stats) ->
                    if (mode !in merged) merged[mode] = stats
                }
                merged
            }
        }
    }
}
