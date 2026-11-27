package com.example.domain.curriculum

import kotlin.random.Random

/**
 * Interactive round types inside a 60–90s Stage Boss Gauntlet.
 */
enum class BossQuestionKind {
    AUDIO_DISCRIMINATION,
    SENTENCE_ORDER,
    RAPID_CHECK
}

/**
 * Single rapid-fire question inside a Stage Boss Challenge.
 */
data class StageBossQuestion(
    val id: String,
    val kind: BossQuestionKind,
    val promptEn: String,
    val promptId: String,
    val stimulusKo: String,
    val speakKo: String,
    val options: List<String> = emptyList(),
    val correctOption: String = "",
    val targetTokens: List<String> = emptyList(),
    val shuffledBank: List<String> = emptyList(),
    val explanationEn: String,
    val explanationId: String
)

/**
 * Timed 6-round Boss Gauntlet placed at the end of every syllabus stage (Stages 1..8).
 * Passing >= 5/6 defeats the Stage Boss, unlocks its Dojang Boss Seal, and awards +120 XP.
 */
data class StageBossChallenge(
    val stage: BeginnerStage,
    val timeLimitSeconds: Int,
    val passThreshold: Int,
    val xpReward: Int,
    val questions: List<StageBossQuestion>
)

/**
 * Pure deterministic Stage Boss generator synthesizing a balanced 6-round mix
 * (Listening + Sentence Building + Rapid Mastery Check) directly from the stage's real lessons.
 */
object StageBossEngine {

    const val DEFAULT_QUESTION_COUNT = 6
    const val DEFAULT_PASS_THRESHOLD = 5

    fun buildChallenge(stageNumber: Int): StageBossChallenge? {
        val stage = BeginnerSyllabusCatalog.stageByNumber(stageNumber) ?: return null
        val lessons = BeginnerSyllabusCatalog.lessonsForStage(stageNumber)
        if (lessons.isEmpty()) return null

        val rng = Random(stageNumber * 997 + 13)
        val questions = ArrayList<StageBossQuestion>(DEFAULT_QUESTION_COUNT)

        // 1. Two Listening / Audio Discrimination rounds from study cards across stage lessons
        val allCards = lessons.flatMap { l -> l.studyCards.map { l to it } }
        val audioTargets = allCards.shuffled(rng).distinctBy { it.second.hangul }.take(2)
        audioTargets.forEachIndexed { idx, (lesson, card) ->
            val distractors = allCards
                .map { it.second.hangul }
                .distinct()
                .filter { it != card.hangul }
                .shuffled(rng)
                .take(3)
            val opts = (distractors + card.hangul).shuffled(rng)
            questions.add(
                StageBossQuestion(
                    id = "boss_${stageNumber}_audio_$idx",
                    kind = BossQuestionKind.AUDIO_DISCRIMINATION,
                    promptEn = "Listen carefully and pick the matching Korean!",
                    promptId = "Dengarkan audio lalu pilih Hangul yang tepat!",
                    stimulusKo = card.meaningEn, // secondary cue kept hidden in audio-first UI until answered
                    speakKo = card.audioTextKo,
                    options = opts,
                    correctOption = card.hangul,
                    explanationEn = "${card.hangul} (${card.romanization}) = ${card.meaningEn}",
                    explanationId = "${card.hangul} (${card.romanization}) = ${card.meaningId}"
                )
            )
        }

        // 2. Two Tap-to-Assemble Sentence Order rounds from the stage's lessons
        val allBuilds = lessons.flatMap { l -> l.sentenceExercises.map { l to it } }
        val chosenBuilds = allBuilds.shuffled(rng).take(2)
        chosenBuilds.forEachIndexed { idx, (_, ex) ->
            val bankRaw = (ex.targetTokens + ex.distractorTokens).shuffled(rng)
            val bank = if (bankRaw.take(ex.targetTokens.size) == ex.targetTokens && bankRaw.size > 1) {
                bankRaw.reversed()
            } else {
                bankRaw
            }
            questions.add(
                StageBossQuestion(
                    id = "boss_${stageNumber}_build_$idx",
                    kind = BossQuestionKind.SENTENCE_ORDER,
                    promptEn = ex.promptEn,
                    promptId = ex.promptId,
                    stimulusKo = ex.fullSentenceKo,
                    speakKo = ex.fullSentenceKo,
                    targetTokens = ex.targetTokens,
                    shuffledBank = bank,
                    explanationEn = "${ex.fullSentenceKo} — ${ex.hintEn}",
                    explanationId = "${ex.fullSentenceKo} — ${ex.hintId}"
                )
            )
        }

        // 3. Two Rapid Mastery Check rounds from the stage's mini-checks
        val allChecks = lessons.flatMap { it.miniChecks }
        val chosenChecks = allChecks.shuffled(rng).take(DEFAULT_QUESTION_COUNT - questions.size)
        chosenChecks.forEachIndexed { idx, chk ->
            questions.add(
                StageBossQuestion(
                    id = "boss_${stageNumber}_check_$idx",
                    kind = BossQuestionKind.RAPID_CHECK,
                    promptEn = chk.promptEn,
                    promptId = chk.promptId,
                    stimulusKo = chk.stimulusKo,
                    speakKo = chk.speakOnTapKo,
                    options = chk.options.shuffled(rng),
                    correctOption = chk.correctOption,
                    explanationEn = chk.explanationEn,
                    explanationId = chk.explanationId
                )
            )
        }

        val passNeed = minOf(DEFAULT_PASS_THRESHOLD, questions.size.coerceAtLeast(1))
        return StageBossChallenge(
            stage = stage,
            timeLimitSeconds = stage.bossTimeSeconds,
            passThreshold = passNeed,
            xpReward = BeginnerSyllabusCatalog.STAGE_BOSS_XP,
            questions = questions
        )
    }

    fun isVictory(correctCount: Int, challenge: StageBossChallenge): Boolean =
        correctCount >= challenge.passThreshold
}
