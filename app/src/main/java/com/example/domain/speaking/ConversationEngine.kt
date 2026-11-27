package com.example.domain.speaking

import com.example.data.seed.ConversationTurnPrompt
import com.example.data.seed.KoreanSpeakingSeed
import com.example.data.seed.SpeakingDifficulty
import com.example.data.seed.SpeakingScenario
import com.example.data.seed.localizedTitle
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.KoreanLinguisticsEngine
import kotlin.math.roundToInt

/**
 * Explicit Conversation State Machine (BAEUM Blueprint Page 30):
 * Separates speech recognition, user preview/editing, response evaluation, and turn advancement.
 *
 * Guarantees:
 * - Every turn has a unique turnId (no repeated questions or loops).
 * - Technical speech recognition failure never produces score 0.
 * - Even a score of 0 advances to the next unique turn cleanly.
 * - Pronunciation is explicitly marked "Not assessed" because STT confidence is never used as pronunciation score.
 */
enum class ConversationPhase {
    AWAITING_INPUT,
    RECORDING,
    PREVIEW_TRANSCRIPT,
    EVALUATING,
    COMPLETED
}

data class EvaluatedTurn(
    val turnId: String,
    val promptKo: String,
    val speakerRole: String,
    val userTranscript: String,
    val wasManualOrEdited: Boolean,
    val scoreOutOf10: Int,
    val relevanceScore: Int,      // 0..10
    val vocabularyScore: Int,     // 0..10
    val grammarScore: Int,        // 0..10
    val speechLevelScore: Int,    // 0..10
    val feedbackNote: String,
    val suggestedImprovementKo: String,
    val modelResponseKo: String
)

data class LiveConversationState(
    val scenario: SpeakingScenario,
    val currentTurnIndex: Int = 0,
    val phase: ConversationPhase = ConversationPhase.AWAITING_INPUT,
    val evaluatedTurns: List<EvaluatedTurn> = emptyList(),
    val presentedTurnIds: Set<String> = emptySet(),
    val draftTranscript: String = "",
    val livePartialTranscript: String = "",
    val draftEditedManually: Boolean = false,
    val lastRecordingPath: String? = null,
    val lastRecordingSeconds: Int = 0,
    val lastWaveform: List<Float> = emptyList(),
    val recognitionErrorMessage: String? = null,
    val showOptionalHint: Boolean = false
) {
    val currentPrompt: ConversationTurnPrompt?
        get() = scenario.turns.getOrNull(currentTurnIndex)

    val totalTurns: Int get() = scenario.turns.size
    val isComplete: Boolean get() = phase == ConversationPhase.COMPLETED || currentTurnIndex >= scenario.turns.size
}

data class FinalSpeakingAssessment(
    val scenarioTitle: String,
    val difficulty: SpeakingDifficulty,
    val totalScoreOutOf100: Int,
    val bandLabel: String,
    val completedTurns: Int,
    val relevanceAvg: Float,
    val vocabularyAvg: Float,
    val grammarAvg: Float,
    val communicationAvg: Float,
    val pronunciationStatus: String = "Not assessed (no phonetic audio analyzer active)",
    val strengths: List<String>,
    val weaknesses: List<String>,
    val improvements: List<Triple<String, String, String>>, // userText, suggestedText, note
    val recommendations: List<String>,
    val turns: List<EvaluatedTurn>
)

object ConversationEngine {

    /**
     * Automatically selects a randomized scenario for the chosen difficulty,
     * avoiding recently played scenario IDs whenever alternatives exist.
     */
    fun pickAutoScenario(
        difficulty: SpeakingDifficulty,
        recentScenarioIds: List<String>,
        categoryFilter: String? = null
    ): SpeakingScenario {
        val base = if (categoryFilter != null) {
            KoreanSpeakingSeed.allScenarios.filter { it.category == categoryFilter }
        } else {
            KoreanSpeakingSeed.scenariosByDifficulty(difficulty)
        }
        val fresh = base.filter { it.id !in recentScenarioIds }
        val candidates = if (fresh.isNotEmpty()) fresh else base
        return candidates.random()
    }

    private val CONVERSATION_WHITESPACE_REGEX = Regex("\\s+")
    private val PREDICATE_COMPLETION_MARKERS = listOf("주세요", "싶어요", "할게요", "겠습니다", "세요")

    fun startScenario(scenario: SpeakingScenario): LiveConversationState {
        val firstId = scenario.turns.firstOrNull()?.turnId
        return LiveConversationState(
            scenario = scenario,
            currentTurnIndex = 0,
            phase = if (firstId != null) ConversationPhase.AWAITING_INPUT else ConversationPhase.COMPLETED,
            presentedTurnIds = setOfNotNull(firstId)
        )
    }

    /**
     * Evaluates a confirmed Korean response transparently using evidence-based linguistic criteria:
     * 1. Contextual relevance (keyword/intent coverage)
     * 2. Vocabulary & Hangul richness (Korean Unicode tokens)
     * 3. Grammar & particle structure (token-boundary particles/connectors & sentence completeness)
     * 4. Speech level appropriateness (해요체 / 합니다체 / 반말 alignment)
     *
     * Never fabricates a pronunciation score.
     */
    fun evaluateResponse(
        prompt: ConversationTurnPrompt,
        userText: String,
        wasManualOrEdited: Boolean,
        isIndonesian: Boolean = false
    ): EvaluatedTurn {
        val cleaned = userText.trim()
        if (cleaned.isBlank() || !HangulUnicodeEngine.containsHangul(cleaned)) {
            return EvaluatedTurn(
                turnId = prompt.turnId,
                promptKo = prompt.promptKo,
                speakerRole = prompt.speakerRole,
                userTranscript = cleaned.ifBlank {
                    if (isIndonesian) "(Belum ada respons bahasa Korea)" else "(No Korean response entered)"
                },
                wasManualOrEdited = wasManualOrEdited,
                scoreOutOf10 = 0,
                relevanceScore = 0,
                vocabularyScore = 0,
                grammarScore = 0,
                speechLevelScore = 0,
                feedbackNote = if (isIndonesian) {
                    "Teks Hangul Korea belum terdeteksi. Percakapan lanjut ke giliran berikutnya — pelajari contoh kalimat di bawah."
                } else {
                    "No Korean Hangul text detected. Conversation continues to the next turn — review the model phrase below."
                },
                suggestedImprovementKo = prompt.modelResponseKo,
                modelResponseKo = prompt.modelResponseKo
            )
        }

        val hangulChars = cleaned.filter { it.code in 0xAC00..0xD7A3 }
        val hangulCharCount = hangulChars.length
        val uniqueHangulRatio = if (hangulCharCount > 0) {
            hangulChars.toSet().size.toDouble() / hangulCharCount.coerceAtMost(30).toDouble()
        } else 0.0
        val spamPenalty = if (hangulCharCount >= 6 && uniqueHangulRatio < 0.30) 0.35 else 1.0

        // 1. Contextual relevance (0..10)
        val matchedKeywords = prompt.expectedKeywords.count { kw -> kw.isNotBlank() && cleaned.contains(kw) }
        val relevanceRaw = when {
            matchedKeywords >= 3 -> 10
            matchedKeywords == 2 -> 9
            matchedKeywords == 1 -> 7
            hangulCharCount >= 8 -> 5
            else -> 3
        }
        val relevance = (relevanceRaw * spamPenalty).roundToInt().coerceIn(0, 10)

        // 2. Vocabulary richness (0..10)
        val wordTokens = cleaned.split(CONVERSATION_WHITESPACE_REGEX).filter { it.isNotBlank() }
        val vocabularyRaw = when {
            wordTokens.size >= 5 && hangulCharCount >= 12 -> 10
            wordTokens.size >= 3 && hangulCharCount >= 7 -> 8
            wordTokens.size >= 2 && hangulCharCount >= 4 -> 7
            hangulCharCount >= 2 -> 5
            else -> 3
        }
        val vocabulary = (vocabularyRaw * spamPenalty).roundToInt().coerceIn(0, 10)

        // 3. Grammar & particles / connectors (0..10) — token-boundary verified
        val particleCount = KoreanLinguisticsEngine.countWordFinalParticles(cleaned)
        val connectorCount = KoreanLinguisticsEngine.countDiscourseConnectors(cleaned)
        val endsFormal = KoreanLinguisticsEngine.hasFormalPoliteEnding(cleaned)
        val endsPolite = KoreanLinguisticsEngine.hasPoliteHaeyoEnding(cleaned)
        val hasGrammaticalStructure = particleCount > 0 || connectorCount > 0
        val hasPredicateCompletion = endsFormal || endsPolite ||
            PREDICATE_COMPLETION_MARKERS.any { cleaned.contains(it) }

        val grammarRaw = when {
            (particleCount + connectorCount) >= 2 && hasPredicateCompletion && hangulCharCount >= 8 -> 10
            hasGrammaticalStructure && hasPredicateCompletion && hangulCharCount >= 6 -> 9
            hasGrammaticalStructure || (hasPredicateCompletion && hangulCharCount >= 5) -> 7
            hangulCharCount >= 5 -> 5
            else -> 3
        }
        val grammar = (grammarRaw * spamPenalty).roundToInt().coerceIn(0, 10)

        // 4. Speech level appropriateness (0..10) — Unicode Jamo ending recognition
        val speechLevelRaw = when (prompt.expectedSpeechLevel) {
            "합니다체" -> when {
                endsFormal -> 10
                endsPolite -> 8
                else -> 4
            }
            else -> when {
                endsPolite || endsFormal -> 10
                else -> 5
            }
        }
        val speechLevel = (speechLevelRaw * spamPenalty).roundToInt().coerceIn(0, 10)

        val overall10 = ((relevance * 0.35) + (vocabulary * 0.20) + (grammar * 0.25) + (speechLevel * 0.20))
            .roundToInt()
            .coerceIn(0, 10)

        val feedbackParts = mutableListOf<String>()
        if (spamPenalty < 1.0) {
            feedbackParts += if (isIndonesian) {
                "Terdeteksi pengulangan suku kata tidak natural. Gunakan kalimat bahasa Korea yang utuh."
            } else {
                "Repeated-syllable spam detected. Respond with a meaningful Korean sentence."
            }
        } else if (isIndonesian) {
            if (matchedKeywords >= 1) {
                feedbackParts += "Menjawab konteks percakapan dengan tepat."
            } else {
                feedbackParts += "Coba sertakan kata kunci konteks (${prompt.expectedKeywords.take(3).joinToString(", ")})."
            }
            if (speechLevel < 8) {
                feedbackParts += "Gunakan akhiran sopan ${prompt.expectedSpeechLevel} (-아요/어요 atau -ㅂ/습니다) untuk situasi ini."
            } else {
                feedbackParts += "Tingkat kesopanan bahasa Korea sudah sesuai (${prompt.expectedSpeechLevel})."
            }
            if (!hasGrammaticalStructure) {
                feedbackParts += "Tambahkan partikel (은/는, 이/가, 을/를, 에) agar struktur kalimat lebih lengkap."
            }
        } else {
            if (matchedKeywords >= 1) {
                feedbackParts += "Addressed the prompt context cleanly."
            } else {
                feedbackParts += "Try including key context words (${prompt.expectedKeywords.take(3).joinToString(", ")})."
            }
            if (speechLevel < 8) {
                feedbackParts += "Use polite ${prompt.expectedSpeechLevel} endings (-아요/어요 or -ㅂ/습니다) for this setting."
            } else {
                feedbackParts += "Appropriate Korean politeness level (${prompt.expectedSpeechLevel})."
            }
            if (!hasGrammaticalStructure) {
                feedbackParts += "Add particles (은/는, 이/가, 을/를, 에) for fuller sentence structure."
            }
        }

        return EvaluatedTurn(
            turnId = prompt.turnId,
            promptKo = prompt.promptKo,
            speakerRole = prompt.speakerRole,
            userTranscript = cleaned,
            wasManualOrEdited = wasManualOrEdited,
            scoreOutOf10 = overall10,
            relevanceScore = relevance,
            vocabularyScore = vocabulary,
            grammarScore = grammar,
            speechLevelScore = speechLevel,
            feedbackNote = feedbackParts.joinToString(" "),
            suggestedImprovementKo = prompt.modelResponseKo,
            modelResponseKo = prompt.modelResponseKo
        )
    }

    /**
     * Atomically appends the evaluated turn and advances to the next unique turn (or completes).
     * Even if scoreOutOf10 == 0, the conversation ALWAYS progresses forward without looping.
     */
    fun commitTurnAndAdvance(
        state: LiveConversationState,
        evaluated: EvaluatedTurn
    ): LiveConversationState {
        // Prevent double submission for the same turnId
        if (state.evaluatedTurns.any { it.turnId == evaluated.turnId }) {
            return state
        }
        val updatedTurns = state.evaluatedTurns + evaluated
        val nextIndex = state.currentTurnIndex + 1
        val nextPrompt = state.scenario.turns.getOrNull(nextIndex)

        val finished = nextPrompt == null || nextPrompt.turnId in state.presentedTurnIds
        return state.copy(
            currentTurnIndex = nextIndex,
            phase = if (finished) ConversationPhase.COMPLETED else ConversationPhase.AWAITING_INPUT,
            evaluatedTurns = updatedTurns,
            presentedTurnIds = if (nextPrompt != null) state.presentedTurnIds + nextPrompt.turnId else state.presentedTurnIds,
            draftTranscript = "",
            livePartialTranscript = "",
            draftEditedManually = false,
            lastRecordingPath = null,
            lastRecordingSeconds = 0,
            lastWaveform = emptyList(),
            recognitionErrorMessage = null,
            showOptionalHint = false
        )
    }

    fun buildFinalAssessment(
        state: LiveConversationState,
        isIndonesian: Boolean = false
    ): FinalSpeakingAssessment {
        val turns = state.evaluatedTurns
        val localizedTitle = state.scenario.localizedTitle(isIndonesian)
        val pronStatus = if (isIndonesian) {
            "Tidak dinilai (tidak ada penganalisis fonetik akustik aktif)"
        } else {
            "Not assessed (no phonetic audio analyzer active)"
        }
        if (turns.isEmpty()) {
            return FinalSpeakingAssessment(
                scenarioTitle = localizedTitle,
                difficulty = state.scenario.difficulty,
                totalScoreOutOf100 = 0,
                bandLabel = if (isIndonesian) "Sesi belum selesai" else "Incomplete session",
                completedTurns = 0,
                relevanceAvg = 0f,
                vocabularyAvg = 0f,
                grammarAvg = 0f,
                communicationAvg = 0f,
                pronunciationStatus = pronStatus,
                strengths = emptyList(),
                weaknesses = listOf(if (isIndonesian) "Belum ada giliran yang dinilai." else "No evaluated turns recorded."),
                improvements = emptyList(),
                recommendations = listOf(
                    if (isIndonesian) "Selesaikan minimal satu giliran dalam bahasa Korea untuk mendapatkan evaluasi berbasis bukti."
                    else "Complete at least one turn in Korean to generate evidence-based feedback."
                ),
                turns = emptyList()
            )
        }

        val relAvg = turns.map { it.relevanceScore }.average().toFloat()
        val vocAvg = turns.map { it.vocabularyScore }.average().toFloat()
        val graAvg = turns.map { it.grammarScore }.average().toFloat()
        val spcAvg = turns.map { it.speechLevelScore }.average().toFloat()
        val commAvg = ((relAvg + spcAvg) / 2f)
        val total100 = (turns.map { it.scoreOutOf10 }.average() * 10.0).roundToInt().coerceIn(0, 100)

        val band = if (isIndonesian) {
            when {
                total100 >= 85 -> "Komunikasi sangat natural"
                total100 >= 70 -> "Komunikasi baik"
                total100 >= 50 -> "Kelancaran sedang berkembang"
                else -> "Latihan dasar"
            }
        } else {
            when {
                total100 >= 85 -> "Strong natural communication"
                total100 >= 70 -> "Good communication"
                total100 >= 50 -> "Developing fluency"
                else -> "Foundational practice"
            }
        }

        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        if (isIndonesian) {
            if (relAvg >= 7.5f) strengths += "Jawaban selalu relevan dengan konteks skenario."
            else weaknesses += "Beberapa jawaban melewatkan kata kunci konteks utama."

            if (spcAvg >= 8.0f) strengths += "Penggunaan tingkat kesopanan bahasa Korea konsisten."
            else {
                weaknesses += "Akhiran kesopanan sesekali belum sesuai situasi."
                recommendations += "Tinjau pola kesopanan (해요체 vs. 합니다체) sebelum mencoba lagi."
            }

            if (graAvg >= 7.5f) strengths += "Struktur kalimat dan partikel bahasa Korea jelas."
            else {
                weaknesses += "Frasa singkat masih kekurangan partikel penghubung atau akhiran kata kerja lengkap."
                recommendations += "Latih pola Studio Tata Bahasa (-아/어서, -고 싶다, -(으)ㄹ 수 있다)."
            }

            if (vocAvg >= 7.5f) strengths += "Rentang kosakata Korea baik untuk tingkat ${state.scenario.difficulty.koreanBadge}."
            else recommendations += "Ulas kosakata topik ${state.scenario.category} di Kartu Pintar FSRS."

            if (recommendations.isEmpty()) {
                recommendations += "Coba skenario berikutnya atau naik ke tingkat kesulitan yang lebih tinggi."
            }
        } else {
            if (relAvg >= 7.5f) strengths += "Stayed directly on topic across scenario turns."
            else weaknesses += "Responses missed key scenario vocabulary or context cues."

            if (spcAvg >= 8.0f) strengths += "Consistent polite Korean speech level usage."
            else {
                weaknesses += "Speech level endings occasionally dropped below expected formality."
                recommendations += "Review Speech Level Lab (해요체 vs. 합니다체) before retrying."
            }

            if (graAvg >= 7.5f) strengths += "Clear Korean particle and sentence structure."
            else {
                weaknesses += "Short phrases lacked connecting particles or complete verb endings."
                recommendations += "Practice Grammar Studio patterns (-아/어서, -고 싶다, -(으)ㄹ 수 있다)."
            }

            if (vocAvg >= 7.5f) strengths += "Good Korean lexical range for ${state.scenario.difficulty.label} level."
            else recommendations += "Review ${state.scenario.category} vocabulary flashcards in Smart Flashcards."

            if (recommendations.isEmpty()) {
                recommendations += "Try the next scenario or step up to a higher difficulty level."
            }
        }

        val improvements = turns
            .filter { it.scoreOutOf10 < 9 }
            .map { Triple(it.userTranscript, it.suggestedImprovementKo, it.feedbackNote) }
            .ifEmpty {
                turns.take(1).map {
                    Triple(
                        it.userTranscript,
                        it.modelResponseKo,
                        if (isIndonesian) "Referensi ungkapan penutur asli yang natural." else "Polished native phrasing reference."
                    )
                }
            }

        return FinalSpeakingAssessment(
            scenarioTitle = localizedTitle,
            difficulty = state.scenario.difficulty,
            totalScoreOutOf100 = total100,
            bandLabel = band,
            completedTurns = turns.size,
            relevanceAvg = relAvg,
            vocabularyAvg = vocAvg,
            grammarAvg = graAvg,
            communicationAvg = commAvg,
            pronunciationStatus = pronStatus,
            strengths = strengths.ifEmpty {
                listOf(
                    if (isIndonesian) "Menyelesaikan seluruh ${turns.size} giliran percakapan."
                    else "Completed all ${turns.size} conversation turns."
                )
            },
            weaknesses = weaknesses,
            improvements = improvements,
            recommendations = recommendations,
            turns = turns
        )
    }
}
