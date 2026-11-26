package com.example.domain.speaking

import com.example.data.seed.ConversationTurnPrompt
import kotlin.math.roundToInt

/**
 * Structured coaching output returned by [KoreanAiCoachRepository].
 *
 * When live cloud Gemini AI is not enabled (for example when Firebase setup was declined),
 * [isSampleMode] is `true` and [serviceNoticeEn]/[serviceNoticeId] explicitly inform the learner
 * that this card is running in local/sample coaching mode and requires Firebase for live Gemini AI.
 */
data class KoreanAiCoachFeedback(
    val isSampleMode: Boolean,
    val naturalAlternativeKo: String,
    val coachingTipEn: String,
    val coachingTipId: String,
    val cadenceInsightEn: String,
    val cadenceInsightId: String,
    val energyScorePercent: Int?,
    val serviceNoticeEn: String,
    val serviceNoticeId: String
) {
    fun localizedTip(isIndonesian: Boolean): String = if (isIndonesian) coachingTipId else coachingTipEn
    fun localizedCadence(isIndonesian: Boolean): String = if (isIndonesian) cadenceInsightId else cadenceInsightEn
    fun localizedNotice(isIndonesian: Boolean): String = if (isIndonesian) serviceNoticeId else serviceNoticeEn
}

/**
 * Unified AI speaking & pronunciation coach contract.
 * A live cloud provider (such as Firebase AI Logic / Gemini) can implement this interface
 * without changing any ViewModel or Compose screen code.
 */
interface KoreanAiCoachRepository {
    suspend fun coachConversationTurn(
        prompt: ConversationTurnPrompt,
        evaluatedTurn: EvaluatedTurn,
        amplitudes: List<Float>,
        durationSeconds: Int
    ): KoreanAiCoachFeedback

    suspend fun coachPronunciationAttempt(
        drill: PronunciationDrillItem,
        attempt: PronunciationAttemptResult,
        amplitudes: List<Float>
    ): KoreanAiCoachFeedback
}

/**
 * Real acoustic signal summary computed locally from captured microphone RMS amplitudes.
 * Never fabricates audio stats — if no audio samples exist (e.g., typed mode), reports null energy.
 */
object AcousticCadenceAnalyzer {

    data class CadenceSummary(
        val energyPercent: Int?,
        val summaryEn: String,
        val summaryId: String
    )

    fun analyze(
        amplitudes: List<Float>,
        spokenHangulSyllables: Int,
        durationSeconds: Int
    ): CadenceSummary {
        if (amplitudes.isEmpty()) {
            return CadenceSummary(
                energyPercent = null,
                summaryEn = "Typed input — record with the mic next time to get live voice energy and pacing feedback.",
                summaryId = "Input ketikan — rekam dengan mikrofon di giliran berikutnya untuk melihat umpan balik energi suara & tempo."
            )
        }

        val activeSamples = amplitudes.filter { it >= 0.08f }
        val avgEnergy = (amplitudes.average().toFloat().coerceIn(0f, 1f) * 100f).roundToInt()
        val peakEnergy = (amplitudes.maxOrNull()?.coerceIn(0f, 1f)?.times(100f) ?: 0f).roundToInt()
        val activeRatio = activeSamples.size.toFloat() / amplitudes.size.coerceAtLeast(1)

        val syllablesPerSec = if (durationSeconds > 0 && spokenHangulSyllables > 0) {
            spokenHangulSyllables.toFloat() / durationSeconds.toFloat()
        } else {
            null
        }

        val volumeNoteEn: String
        val volumeNoteId: String
        when {
            peakEnergy < 22 -> {
                volumeNoteEn = "Voice level was quite soft (peak $peakEnergy%) — speak slightly closer to the microphone for sharper Korean consonant detection."
                volumeNoteId = "Volume suara cukup pelan (puncak $peakEnergy%) — dekatkan sedikit ke mikrofon agar konsonan Korea terdeteksi lebih tajam."
            }
            activeRatio < 0.25f -> {
                volumeNoteEn = "Detected brief speech bursts with long pauses — try linking the full Korean phrase in one steady breath."
                volumeNoteId = "Terdeteksi jeda hening yang cukup panjang — coba ucapkan frasa Korea dalam satu tarikan napas yang stabil."
            }
            else -> {
                volumeNoteEn = "Clear vocal energy (avg $avgEnergy%, peak $peakEnergy%) with steady projection."
                volumeNoteId = "Energi suara jelas (rata-rata $avgEnergy%, puncak $peakEnergy%) dengan proyeksi yang stabil."
            }
        }

        val paceEn = when {
            syllablesPerSec == null -> ""
            syllablesPerSec < 1.3f -> " Pacing is slow and deliberate (~${String.format("%.1f", syllablesPerSec)} syl/s) — great for accuracy, try slightly smoother linking next."
            syllablesPerSec > 5.5f -> " Pacing is very fast (~${String.format("%.1f", syllablesPerSec)} syl/s) — slow down slightly on batchim endings."
            else -> " Natural conversational rhythm (~${String.format("%.1f", syllablesPerSec)} syl/s)."
        }
        val paceId = when {
            syllablesPerSec == null -> ""
            syllablesPerSec < 1.3f -> " Tempo pelan dan hati-hati (~${String.format("%.1f", syllablesPerSec)} suku kata/dtk) — bagus untuk akurasi, coba sambungkan lebih mengalir."
            syllablesPerSec > 5.5f -> " Tempo sangat cepat (~${String.format("%.1f", syllablesPerSec)} suku kata/dtk) — pelankan sedikit pada akhiran batchim."
            else -> " Ritme percakapan sudah natural (~${String.format("%.1f", syllablesPerSec)} suku kata/dtk)."
        }

        return CadenceSummary(
            energyPercent = avgEnergy,
            summaryEn = volumeNoteEn + paceEn,
            summaryId = volumeNoteId + paceId
        )
    }
}

/**
 * Clearly-labeled local/sample preview implementation of [KoreanAiCoachRepository].
 * Used when cloud Gemini AI via Firebase has not been enabled by the user.
 */
class LocalSampleKoreanAiCoachRepository : KoreanAiCoachRepository {

    override suspend fun coachConversationTurn(
        prompt: ConversationTurnPrompt,
        evaluatedTurn: EvaluatedTurn,
        amplitudes: List<Float>,
        durationSeconds: Int
    ): KoreanAiCoachFeedback {
        val syllableCount = evaluatedTurn.userTranscript.count { it.code in 0xAC00..0xD7A3 }
        val cadence = AcousticCadenceAnalyzer.analyze(
            amplitudes = amplitudes,
            spokenHangulSyllables = syllableCount,
            durationSeconds = durationSeconds
        )

        val (tipEn, tipId) = when {
            evaluatedTurn.scoreOutOf10 >= 9 -> Pair(
                "Strong, natural ${prompt.expectedSpeechLevel} reply! Compare your phrasing with the model response to pick up even smoother native collocations.",
                "Respons ${prompt.expectedSpeechLevel} sangat natural! Bandingkan kalimat Anda dengan contoh penutur asli untuk memperkaya variasi ungkapan."
            )
            evaluatedTurn.speechLevelScore < 8 -> Pair(
                "Your core idea came across, but adjust the verb ending to ${prompt.expectedSpeechLevel} so it fits the listener's social register.",
                "Maksud jawaban Anda sudah tersampaikan, namun sesuaikan akhiran kata kerja ke ragam ${prompt.expectedSpeechLevel} agar sesuai situasi."
            )
            evaluatedTurn.relevanceScore < 7 -> Pair(
                "Anchor your reply with context keywords like ${prompt.expectedKeywords.take(3).joinToString(", ")} so your partner immediately catches your intent.",
                "Sertakan kata kunci konteks seperti ${prompt.expectedKeywords.take(3).joinToString(", ")} agar lawan bicara langsung menangkap maksud Anda."
            )
            else -> Pair(
                "Good foundation! Expand with a connector (-아/어서 or -고) to make your Korean sound more complete and conversational.",
                "Dasar yang bagus! Tambahkan kata hubung (-아/어서 atau -고) agar kalimat Korea terdengar lebih utuh dan alami."
            )
        }

        return KoreanAiCoachFeedback(
            isSampleMode = true,
            naturalAlternativeKo = prompt.modelResponseKo,
            coachingTipEn = tipEn,
            coachingTipId = tipId,
            cadenceInsightEn = cadence.summaryEn,
            cadenceInsightId = cadence.summaryId,
            energyScorePercent = cadence.energyPercent,
            serviceNoticeEn = SAMPLE_NOTICE_EN,
            serviceNoticeId = SAMPLE_NOTICE_ID
        )
    }

    override suspend fun coachPronunciationAttempt(
        drill: PronunciationDrillItem,
        attempt: PronunciationAttemptResult,
        amplitudes: List<Float>
    ): KoreanAiCoachFeedback {
        val syllableCount = drill.targetPronouncedKo.count { it.code in 0xAC00..0xD7A3 }
        val estimatedSeconds = (amplitudes.size / 6).coerceAtLeast(1)
        val cadence = AcousticCadenceAnalyzer.analyze(
            amplitudes = amplitudes,
            spokenHangulSyllables = syllableCount,
            durationSeconds = estimatedSeconds
        )

        val (tipEn, tipId) = when {
            attempt.passed -> Pair(
                "Clean articulation! Repeat \"${drill.targetWrittenKo}\" twice at natural speed while keeping the same mouth shape.",
                "Artikulasi bersih! Ulangi \"${drill.targetWrittenKo}\" dua kali dengan kecepatan normal sambil menjaga posisi mulut yang sama."
            )
            attempt.syllableBreakdown.any { it.issueType == PhoneticIssueType.CONSONANT_SERIES } -> Pair(
                "Contrast focus: separate plain (soft), tense (tight throat, higher pitch, zero air), and aspirated (strong puff of air).",
                "Fokus kontras konsonan: bedakan bunyi lembut (tanpa tekanan), tegang (tenggorokan kencang tanpa angin), dan beraspirasi (embusan udara kuat)."
            )
            attempt.syllableBreakdown.any { it.issueType == PhoneticIssueType.BATCHIM_DIFFERENCE } -> Pair(
                "Hold the final batchim consonant cleanly without adding an extra vowel, or link it smoothly if the next syllable starts with ㅇ.",
                "Tahan konsonan akhir (batchim) tanpa menambahkan vokal ekstra, atau sambungkan mulus bila suku kata berikutnya diawali ㅇ."
            )
            else -> Pair(
                "Listen to the TTS model once more, tap out each syllable rhythm (${drill.targetPronouncedKo}), then record again steadily.",
                "Dengarkan audio contoh sekali lagi, ikuti ritme tiap suku kata (${drill.targetPronouncedKo}), lalu rekam ulang dengan tenang."
            )
        }

        return KoreanAiCoachFeedback(
            isSampleMode = true,
            naturalAlternativeKo = if (drill.hasDistinctPronunciation) {
                "${drill.targetWrittenKo} → [${drill.targetPronouncedKo}]"
            } else {
                drill.targetWrittenKo
            },
            coachingTipEn = tipEn,
            coachingTipId = tipId,
            cadenceInsightEn = cadence.summaryEn,
            cadenceInsightId = cadence.summaryId,
            energyScorePercent = cadence.energyPercent,
            serviceNoticeEn = SAMPLE_NOTICE_EN,
            serviceNoticeId = SAMPLE_NOTICE_ID
        )
    }

    companion object {
        const val SAMPLE_NOTICE_EN =
            "Offline sample coach preview • Live personalized Gemini AI coaching requires enabling Firebase."
        const val SAMPLE_NOTICE_ID =
            "Pratinjau pelatih lokal/sampel • Pelatih AI Gemini langsung memerlukan aktivasi Firebase."
    }
}
