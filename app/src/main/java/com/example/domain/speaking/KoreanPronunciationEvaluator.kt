package com.example.domain.speaking

import com.example.domain.hangul.HangulUnicodeEngine
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class PronunciationDrillCategory(val labelEn: String, val labelId: String) {
    MINIMAL_CONTRAST("Consonant Contrasts", "Kontras Konsonan"),
    SOUND_CHANGE("Sound Changes", "Perubahan Bunyi"),
    EVERYDAY_PHRASE("Everyday Phrases", "Frasa Harian")
}

data class PronunciationDrillItem(
    val id: String,
    val category: PronunciationDrillCategory,
    val targetWrittenKo: String,
    val targetPronouncedKo: String,
    val romanization: String,
    val meaningEn: String,
    val meaningId: String,
    val focusLabelEn: String,
    val focusLabelId: String,
    val coachingTipEn: String,
    val coachingTipId: String,
    val contrastGroup: List<String> = emptyList()
) {
    val hasDistinctPronunciation: Boolean
        get() = KoreanPronunciationEvaluator.normalizeHangul(targetWrittenKo) !=
            KoreanPronunciationEvaluator.normalizeHangul(targetPronouncedKo)
}

enum class SyllableMatchLevel {
    EXACT,
    CLOSE,
    MISMATCH
}

enum class PhoneticIssueType {
    CONSONANT_SERIES,
    INITIAL_CONSONANT,
    VOWEL_DIFFERENCE,
    BATCHIM_DIFFERENCE,
    MISSING_OR_EXTRA_SYLLABLE
}

data class SyllableMatchResult(
    val targetSyllable: Char,
    val recognizedSyllable: Char?,
    val matchLevel: SyllableMatchLevel,
    val issueType: PhoneticIssueType? = null
)

data class PronunciationAttemptResult(
    val drillId: String,
    val bestRecognizedText: String,
    val allAlternatives: List<String>,
    val matchScorePercent: Int,
    val passed: Boolean,
    val isExactMatch: Boolean,
    val syllableBreakdown: List<SyllableMatchResult>,
    val feedbackEn: String,
    val feedbackId: String,
    val phoneticHintEn: String,
    val phoneticHintId: String
)

/**
 * Deterministic Korean pronunciation practice evaluator powered by Hangul Unicode Jamo decomposition
 * and phonological equivalence rules (consonant series, vowel mergers, and batchim neutralization).
 *
 * Evaluates Android [android.speech.SpeechRecognizer] n-best transcripts transparently without
 * fabricating studio acoustic formant scores.
 */
object KoreanPronunciationEvaluator {

    private val consonantSeriesGroups: List<Set<Char>> = listOf(
        setOf('ㄱ', 'ㄲ', 'ㅋ'),
        setOf('ㄷ', 'ㄸ', 'ㅌ'),
        setOf('ㅂ', 'ㅃ', 'ㅍ'),
        setOf('ㅈ', 'ㅉ', 'ㅊ'),
        setOf('ㅅ', 'ㅆ')
    )

    // In modern Seoul Korean, ㅐ/ㅔ and ㅒ/ㅖ and ㅙ/ㅚ/ㅞ are largely merged in everyday speech.
    private val equivalentVowelGroups: List<Set<Char>> = listOf(
        setOf('ㅐ', 'ㅔ'),
        setOf('ㅒ', 'ㅖ'),
        setOf('ㅙ', 'ㅚ', 'ㅞ')
    )

    // Korean 7 representative coda (batchim) neutralization classes.
    private fun batchimNeutralClass(batchim: Char?): Char? = when (batchim) {
        null -> null
        'ㄱ', 'ㄲ', 'ㅋ', 'ㄳ', 'ㄺ' -> 'ㄱ'
        'ㄴ', 'ㄵ', 'ㄶ' -> 'ㄴ'
        'ㄷ', 'ㅅ', 'ㅆ', 'ㅈ', 'ㅊ', 'ㅌ', 'ㅎ' -> 'ㄷ'
        'ㄹ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㅀ' -> 'ㄹ'
        'ㅁ', 'ㄻ' -> 'ㅁ'
        'ㅂ', 'ㅍ', 'ㅄ', 'ㄿ' -> 'ㅂ'
        'ㅇ' -> 'ㅇ'
        else -> batchim
    }

    val defaultDrills: List<PronunciationDrillItem> = listOf(
        // Minimal Consonant Contrasts (Plain vs Tense vs Aspirated)
        PronunciationDrillItem(
            id = "mc_gada",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "가다",
            targetPronouncedKo = "가다",
            romanization = "ga-da",
            meaningEn = "To go",
            meaningId = "Pergi",
            focusLabelEn = "Plain ㄱ vs. ㄲ / ㅋ",
            focusLabelId = "Biasa ㄱ vs. ㄲ / ㅋ",
            coachingTipEn = "Relax your tongue and keep a soft, low-pitched onset for plain ㄱ.",
            coachingTipId = "Rilekskan lidah dan mulai dengan nada lembut tanpa letupan kuat untuk ㄱ biasa.",
            contrastGroup = listOf("가다", "까다", "카다")
        ),
        PronunciationDrillItem(
            id = "mc_kkada",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "까다",
            targetPronouncedKo = "까다",
            romanization = "kka-da",
            meaningEn = "To peel",
            meaningId = "Mengupas",
            focusLabelEn = "Tense ㄲ (no puff of air)",
            focusLabelId = "Tegang ㄲ (tanpa hembusan napas)",
            coachingTipEn = "Tense your throat briefly and release ㄲ crisply with higher pitch and zero breathiness.",
            coachingTipId = "Kencangkan tenggorokan sejenak lalu lepaskan ㄲ dengan tajam tanpa hembusan udara.",
            contrastGroup = listOf("가다", "까다", "카다")
        ),
        PronunciationDrillItem(
            id = "mc_kada",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "카메라",
            targetPronouncedKo = "카메라",
            romanization = "ka-me-ra",
            meaningEn = "Camera (Aspirated ㅋ)",
            meaningId = "Kamera (Aspirasi ㅋ)",
            focusLabelEn = "Aspirated ㅋ (strong puff)",
            focusLabelId = "Aspirasi ㅋ (hembusan kuat)",
            coachingTipEn = "Release a strong burst of air on ㅋ, like the 'k' in English 'key'.",
            coachingTipId = "Keluarkan hembusan napas yang kuat pada ㅋ, seperti bunyi 'kh' yang tegas.",
            contrastGroup = listOf("가다", "까다", "카메라")
        ),
        PronunciationDrillItem(
            id = "mc_dal",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "달",
            targetPronouncedKo = "달",
            romanization = "dal",
            meaningEn = "Moon / Month",
            meaningId = "Bulan",
            focusLabelEn = "Plain ㄷ + Batchim ㄹ",
            focusLabelId = "Biasa ㄷ + Batchim ㄹ",
            coachingTipEn = "Start with a gentle plain ㄷ and finish cleanly with your tongue tip touching the ridge for ㄹ.",
            coachingTipId = "Awali dengan ㄷ yang ringan dan akhiri dengan ujung lidah menyentuh langit-langit untuk ㄹ.",
            contrastGroup = listOf("달", "딸", "탈")
        ),
        PronunciationDrillItem(
            id = "mc_ttal",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "딸",
            targetPronouncedKo = "딸",
            romanization = "ttal",
            meaningEn = "Daughter",
            meaningId = "Anak perempuan",
            focusLabelEn = "Tense ㄸ vs. ㄷ / ㅌ",
            focusLabelId = "Tegang ㄸ vs. ㄷ / ㅌ",
            coachingTipEn = "Hold tension on ㄸ before releasing with no breath puff — crisp and slightly higher pitch.",
            coachingTipId = "Tahan ketegangan pada ㄸ lalu lepaskan tajam tanpa hembusan angin.",
            contrastGroup = listOf("달", "딸", "탈")
        ),
        PronunciationDrillItem(
            id = "mc_tal",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "탈",
            targetPronouncedKo = "탈",
            romanization = "tal",
            meaningEn = "Mask",
            meaningId = "Topeng",
            focusLabelEn = "Aspirated ㅌ",
            focusLabelId = "Aspirasi ㅌ",
            coachingTipEn = "Push a clear puff of air as you release ㅌ.",
            coachingTipId = "Dorong hembusan napas yang jelas saat melepaskan ㅌ.",
            contrastGroup = listOf("달", "딸", "탈")
        ),
        PronunciationDrillItem(
            id = "mc_bul",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "불",
            targetPronouncedKo = "불",
            romanization = "bul",
            meaningEn = "Fire / Light",
            meaningId = "Api / Lampu",
            focusLabelEn = "Plain ㅂ vs. ㅃ / ㅍ",
            focusLabelId = "Biasa ㅂ vs. ㅃ / ㅍ",
            coachingTipEn = "Keep lips relaxed for plain ㅂ and round them cleanly into ㅜ + ㄹ.",
            coachingTipId = "Jaga bibir rileks untuk ㅂ biasa lalu bulatkan bibir untuk vokal ㅜ dan akhiran ㄹ.",
            contrastGroup = listOf("불", "뿔", "풀")
        ),
        PronunciationDrillItem(
            id = "mc_ppul",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "뿔",
            targetPronouncedKo = "뿔",
            romanization = "ppul",
            meaningEn = "Horn",
            meaningId = "Tanduk",
            focusLabelEn = "Tense ㅃ",
            focusLabelId = "Tegang ㅃ",
            coachingTipEn = "Press lips tightly together and pop ㅃ without letting air burst out.",
            coachingTipId = "Rapatkan bibir dengan kencang lalu lepaskan ㅃ secara tajam tanpa hembusan.",
            contrastGroup = listOf("불", "뿔", "풀")
        ),
        PronunciationDrillItem(
            id = "mc_pul",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "풀",
            targetPronouncedKo = "풀",
            romanization = "pul",
            meaningEn = "Grass / Glue",
            meaningId = "Rumput / Lem",
            focusLabelEn = "Aspirated ㅍ",
            focusLabelId = "Aspirasi ㅍ",
            coachingTipEn = "Release a strong burst of air from closed lips for ㅍ.",
            coachingTipId = "Lepaskan hembusan udara kuat dari kedua bibir untuk ㅍ.",
            contrastGroup = listOf("불", "뿔", "풀")
        ),
        PronunciationDrillItem(
            id = "mc_sada",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "사다",
            targetPronouncedKo = "사다",
            romanization = "sa-da",
            meaningEn = "To buy",
            meaningId = "Membeli",
            focusLabelEn = "Soft ㅅ vs. Tense ㅆ",
            focusLabelId = "Lembut ㅅ vs. Tegang ㅆ",
            coachingTipEn = "Use a gentle, slightly breathy ㅅ compared to the sharp hiss of ㅆ.",
            coachingTipId = "Gunakan bunyi ㅅ yang lembut dan sedikit berangin dibanding ㅆ yang tajam.",
            contrastGroup = listOf("사다", "싸다")
        ),
        PronunciationDrillItem(
            id = "mc_ssada",
            category = PronunciationDrillCategory.MINIMAL_CONTRAST,
            targetWrittenKo = "싸다",
            targetPronouncedKo = "싸다",
            romanization = "ssa-da",
            meaningEn = "To be cheap / To wrap",
            meaningId = "Murah / Membungkus",
            focusLabelEn = "Tense ㅆ",
            focusLabelId = "Tegang ㅆ",
            coachingTipEn = "Make a sharp, tense 'ss' sound with stronger airflow right into ㅏ.",
            coachingTipId = "Ucapkan bunyi 'ss' yang tajam dan tegas langsung menuju vokal ㅏ.",
            contrastGroup = listOf("사다", "싸다")
        ),
        // Essential Korean Sound Change Rules
        PronunciationDrillItem(
            id = "sc_hangugeo",
            category = PronunciationDrillCategory.SOUND_CHANGE,
            targetWrittenKo = "한국어",
            targetPronouncedKo = "한구거",
            romanization = "han-gu-geo",
            meaningEn = "Korean language",
            meaningId = "Bahasa Korea",
            focusLabelEn = "Liaison (연음 법칙)",
            focusLabelId = "Penghubungan Bunyi (연음 법칙)",
            coachingTipEn = "Link final ㄱ smoothly onto 어 so it flows as [한구거], not a broken stop.",
            coachingTipId = "Sambungkan batchim ㄱ ke 어 sehingga mengalir alami menjadi [한구거]."
        ),
        PronunciationDrillItem(
            id = "sc_gamsahamnida",
            category = PronunciationDrillCategory.SOUND_CHANGE,
            targetWrittenKo = "감사합니다",
            targetPronouncedKo = "감사함니다",
            romanization = "gam-sa-ham-ni-da",
            meaningEn = "Thank you (formal)",
            meaningId = "Terima kasih (formal)",
            focusLabelEn = "Nasalization (비음화)",
            focusLabelId = "Nasalisasi (비음화)",
            coachingTipEn = "Assimilate final ㅂ before ㄴ into ㅁ: pronounce it smoothly as [감사함니다].",
            coachingTipId = "Ubah batchim ㅂ sebelum ㄴ menjadi ㅁ sehingga terdengar mulus [감사함니다]."
        ),
        PronunciationDrillItem(
            id = "sc_hakgyo",
            category = PronunciationDrillCategory.SOUND_CHANGE,
            targetWrittenKo = "학교",
            targetPronouncedKo = "학꾜",
            romanization = "hak-kkyo",
            meaningEn = "School",
            meaningId = "Sekolah",
            focusLabelEn = "Tensification (경음화)",
            focusLabelId = "Penegangan Bunyi (경음화)",
            coachingTipEn = "After batchim ㄱ, tense 교 into 꾜 so it sounds like [학꾜].",
            coachingTipId = "Setelah batchim ㄱ, tegangkan 교 menjadi 꾜 sehingga terdengar [학꾜]."
        ),
        PronunciationDrillItem(
            id = "sc_jota",
            category = PronunciationDrillCategory.SOUND_CHANGE,
            targetWrittenKo = "좋다",
            targetPronouncedKo = "조타",
            romanization = "jo-ta",
            meaningEn = "To be good",
            meaningId = "Bagus / Baik",
            focusLabelEn = "Aspiration Fusion (격음화)",
            focusLabelId = "Fusi Aspirasi (격음화)",
            coachingTipEn = "Merge ㅎ + ㄷ into strongly aspirated ㅌ: pronounce [조타].",
            coachingTipId = "Gabungkan ㅎ + ㄷ menjadi konsonan aspirasi ㅌ: ucapkan [조타]."
        ),
        PronunciationDrillItem(
            id = "sc_gachi",
            category = PronunciationDrillCategory.SOUND_CHANGE,
            targetWrittenKo = "같이",
            targetPronouncedKo = "가치",
            romanization = "ga-chi",
            meaningEn = "Together",
            meaningId = "Bersama",
            focusLabelEn = "Palatalization (구개음화)",
            focusLabelId = "Palatalisasi (구개음화)",
            coachingTipEn = "Batchim ㅌ before 이 shifts to ㅊ — pronounce cleanly as [가치].",
            coachingTipId = "Batchim ㅌ di depan 이 berubah menjadi ㅊ — ucapkan jelas sebagai [가치]."
        ),
        // Everyday Practical Korean Phrases
        PronunciationDrillItem(
            id = "ep_annyeong",
            category = PronunciationDrillCategory.EVERYDAY_PHRASE,
            targetWrittenKo = "안녕하세요",
            targetPronouncedKo = "안녕하세요",
            romanization = "an-nyeong-ha-se-yo",
            meaningEn = "Hello (polite)",
            meaningId = "Halo (sopan)",
            focusLabelEn = "Polite Greeting Rhythm",
            focusLabelId = "Ritme Sapaan Sopan",
            coachingTipEn = "Keep all 5 syllables balanced and lift intonation slightly on 요.",
            coachingTipId = "Jaga keseimbangan 5 suku kata dan naikkan sedikit intonasi pada 요."
        ),
        PronunciationDrillItem(
            id = "ep_coffee",
            category = PronunciationDrillCategory.EVERYDAY_PHRASE,
            targetWrittenKo = "커피 한 잔 주세요",
            targetPronouncedKo = "커피 한 잔 주세요",
            romanization = "keo-pi han jan ju-se-yo",
            meaningEn = "One cup of coffee, please",
            meaningId = "Tolong satu cangkir kopi",
            focusLabelEn = "Aspirated ㅋ & ㅍ Flow",
            focusLabelId = "Aliran Aspirasi ㅋ & ㅍ",
            coachingTipEn = "Give crisp aspiration to 커 and 피, then glide naturally through 한 잔 주세요.",
            coachingTipId = "Berikan hembusan jelas pada 커 dan 피, lalu sambungkan dengan lancar ke 한 잔 주세요."
        ),
        PronunciationDrillItem(
            id = "ep_slowly",
            category = PronunciationDrillCategory.EVERYDAY_PHRASE,
            targetWrittenKo = "천천히 말해 주세요",
            targetPronouncedKo = "천천히 마래 주세요",
            romanization = "cheon-cheon-hi mal-hae ju-se-yo",
            meaningEn = "Please speak slowly",
            meaningId = "Tolong bicara pelan-pelan",
            focusLabelEn = "Aspirated ㅊ & Batchim ㄹ",
            focusLabelId = "Aspirasi ㅊ & Batchim ㄹ",
            coachingTipEn = "Articulate ㅊ with a clean puff and link 말 + 해 smoothly.",
            coachingTipId = "Ucapkan ㅊ dengan hembusan bersih dan sambungkan 말 + 해 secara alami."
        )
    )

    /**
     * Evaluates n-best Korean transcripts returned by Android [android.speech.SpeechRecognizer]
     * against a [PronunciationDrillItem]'s written and pronounced Hangul forms.
     */
    fun evaluate(
        drill: PronunciationDrillItem,
        alternatives: List<String>,
        confidenceScores: List<Float> = emptyList()
    ): PronunciationAttemptResult {
        val cleanedAlternatives = alternatives.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (cleanedAlternatives.isEmpty()) {
            return PronunciationAttemptResult(
                drillId = drill.id,
                bestRecognizedText = "",
                allAlternatives = emptyList(),
                matchScorePercent = 0,
                passed = false,
                isExactMatch = false,
                syllableBreakdown = normalizeHangul(drill.targetWrittenKo).map {
                    SyllableMatchResult(it, null, SyllableMatchLevel.MISMATCH, PhoneticIssueType.MISSING_OR_EXTRA_SYLLABLE)
                },
                feedbackEn = "No Korean speech was recognized. Tap Speak and pronounce clearly.",
                feedbackId = "Ucapan bahasa Korea belum terdeteksi. Ketuk Bicara dan ucapkan dengan jelas.",
                phoneticHintEn = drill.coachingTipEn,
                phoneticHintId = drill.coachingTipId
            )
        }

        val targetWrittenNorm = normalizeHangul(drill.targetWrittenKo)
        val targetPronouncedNorm = normalizeHangul(drill.targetPronouncedKo)
        val referenceTargets = listOf(targetWrittenNorm, targetPronouncedNorm).distinct().filter { it.isNotEmpty() }

        var bestCandidate = cleanedAlternatives.first()
        var bestScore = -1f
        var bestBreakdown: List<SyllableMatchResult> = emptyList()
        var bestExact = false

        for (candidate in cleanedAlternatives) {
            val candNorm = normalizeHangul(candidate)
            for (ref in referenceTargets) {
                val (score, breakdown, exact) = compareHangulStrings(ref, candNorm)
                if (score > bestScore || (score == bestScore && exact && !bestExact)) {
                    bestScore = score
                    bestCandidate = candidate
                    bestBreakdown = breakdown
                    bestExact = exact
                }
            }
        }

        // Map breakdown display targets back onto the canonical written syllables when lengths match
        val displayBreakdown = if (bestBreakdown.size == targetWrittenNorm.length) {
            bestBreakdown.mapIndexed { idx, item -> item.copy(targetSyllable = targetWrittenNorm[idx]) }
        } else {
            bestBreakdown
        }

        val scorePercent = (bestScore * 100f).roundToInt().coerceIn(0, 100)
        val hasConsonantSeriesMismatch = displayBreakdown.any { it.issueType == PhoneticIssueType.CONSONANT_SERIES }
        val passed = bestExact || (
            scorePercent >= PASS_THRESHOLD_PERCENT &&
                !(drill.category == PronunciationDrillCategory.MINIMAL_CONTRAST && hasConsonantSeriesMismatch)
            )

        val dominantIssue = displayBreakdown
            .firstOrNull { it.matchLevel != SyllableMatchLevel.EXACT }
            ?.issueType

        val (feedbackEn, feedbackId) = buildFeedback(scorePercent, bestExact, passed, dominantIssue, drill)
        val (hintEn, hintId) = buildPhoneticHint(dominantIssue, drill)

        return PronunciationAttemptResult(
            drillId = drill.id,
            bestRecognizedText = bestCandidate,
            allAlternatives = cleanedAlternatives,
            matchScorePercent = scorePercent,
            passed = passed,
            isExactMatch = bestExact,
            syllableBreakdown = displayBreakdown,
            feedbackEn = feedbackEn,
            feedbackId = feedbackId,
            phoneticHintEn = hintEn,
            phoneticHintId = hintId
        )
    }

    /**
     * Convenience helper to create a dynamic drill item for any ad-hoc word or sound-change card.
     */
    fun createAdHocDrill(
        id: String,
        writtenKo: String,
        pronouncedKo: String = writtenKo,
        romanization: String = "",
        meaningEn: String,
        meaningId: String = meaningEn,
        category: PronunciationDrillCategory = PronunciationDrillCategory.SOUND_CHANGE,
        tipEn: String = "Listen to the native TTS model once, then speak clearly into the microphone.",
        tipId: String = "Dengarkan model audio sekali, lalu ucapkan dengan jelas ke mikrofon."
    ): PronunciationDrillItem {
        val cleanPronounced = pronouncedKo.replace("[", "").replace("]", "").trim().ifBlank { writtenKo }
        return PronunciationDrillItem(
            id = id,
            category = category,
            targetWrittenKo = writtenKo.trim(),
            targetPronouncedKo = cleanPronounced,
            romanization = romanization,
            meaningEn = meaningEn,
            meaningId = meaningId,
            focusLabelEn = category.labelEn,
            focusLabelId = category.labelId,
            coachingTipEn = tipEn,
            coachingTipId = tipId
        )
    }

    fun normalizeHangul(input: String): String =
        input.filter { it.code in 0xAC00..0xD7A3 }

    private fun compareHangulStrings(
        target: String,
        recognized: String
    ): Triple<Float, List<SyllableMatchResult>, Boolean> {
        if (target.isEmpty()) return Triple(0f, emptyList(), false)
        if (recognized.isEmpty()) {
            return Triple(
                0f,
                target.map {
                    SyllableMatchResult(it, null, SyllableMatchLevel.MISMATCH, PhoneticIssueType.MISSING_OR_EXTRA_SYLLABLE)
                },
                false
            )
        }
        if (target == recognized) {
            return Triple(
                1f,
                target.map { SyllableMatchResult(it, it, SyllableMatchLevel.EXACT, null) },
                true
            )
        }

        // Align syllables via dynamic programming (Needleman-Wunsch style) using Jamo similarity
        val n = target.length
        val m = recognized.length
        val dp = Array(n + 1) { FloatArray(m + 1) }
        val gapPenalty = -0.35f

        for (i in 1..n) dp[i][0] = i * gapPenalty
        for (j in 1..m) dp[0][j] = j * gapPenalty

        for (i in 1..n) {
            for (j in 1..m) {
                val sim = syllableSimilarity(target[i - 1], recognized[j - 1]).first
                val matchScore = dp[i - 1][j - 1] + sim
                val deleteTarget = dp[i - 1][j] + gapPenalty
                val insertRec = dp[i][j - 1] + gapPenalty
                dp[i][j] = max(matchScore, max(deleteTarget, insertRec))
            }
        }

        // Backtrack to build per-target-syllable alignment
        val aligned = mutableListOf<SyllableMatchResult>()
        var i = n
        var j = m
        var totalSim = 0f
        var extraRecognizedCount = 0

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0) {
                val (sim, issue) = syllableSimilarity(target[i - 1], recognized[j - 1])
                if (dp[i][j] == dp[i - 1][j - 1] + sim) {
                    totalSim += sim
                    val level = when {
                        sim >= 0.99f -> SyllableMatchLevel.EXACT
                        sim >= 0.60f -> SyllableMatchLevel.CLOSE
                        else -> SyllableMatchLevel.MISMATCH
                    }
                    aligned.add(
                        SyllableMatchResult(
                            targetSyllable = target[i - 1],
                            recognizedSyllable = recognized[j - 1],
                            matchLevel = level,
                            issueType = if (level == SyllableMatchLevel.EXACT) null else issue
                        )
                    )
                    i--
                    j--
                    continue
                }
            }
            if (i > 0 && (j == 0 || dp[i][j] == dp[i - 1][j] + gapPenalty)) {
                aligned.add(
                    SyllableMatchResult(
                        targetSyllable = target[i - 1],
                        recognizedSyllable = null,
                        matchLevel = SyllableMatchLevel.MISMATCH,
                        issueType = PhoneticIssueType.MISSING_OR_EXTRA_SYLLABLE
                    )
                )
                i--
            } else {
                extraRecognizedCount++
                j--
            }
        }

        aligned.reverse()
        val denominator = max(n, n + extraRecognizedCount).coerceAtLeast(1)
        val overall = (totalSim / denominator.toFloat()).coerceIn(0f, 1f)
        val exact = aligned.all { it.matchLevel == SyllableMatchLevel.EXACT } && extraRecognizedCount == 0
        return Triple(overall, aligned, exact)
    }

    /**
     * Computes phonetic similarity in [0f, 1f] between two Hangul syllables by comparing
     * Initial (Choseong), Medial (Jungseong), and Final (Jongseong / Batchim) jamo components.
     */
    internal fun syllableSimilarity(target: Char, recognized: Char): Pair<Float, PhoneticIssueType?> {
        if (target == recognized) return 1f to null
        val t = HangulUnicodeEngine.decomposeSyllable(target) ?: return 0f to PhoneticIssueType.INITIAL_CONSONANT
        val r = HangulUnicodeEngine.decomposeSyllable(recognized) ?: return 0f to PhoneticIssueType.INITIAL_CONSONANT

        val (tCho, tJung, tJong) = t
        val (rCho, rJung, rJong) = r

        // 1. Initial consonant (weight 0.40)
        val sameConsonantSeries = consonantSeriesGroups.any { tCho in it && rCho in it }
        val choScore = when {
            tCho == rCho -> 1.0f
            sameConsonantSeries -> 0.45f
            else -> 0.0f
        }

        // 2. Medial vowel (weight 0.35)
        val mergedVowelMatch = equivalentVowelGroups.any { tJung in it && rJung in it }
        val jungScore = when {
            tJung == rJung -> 1.0f
            mergedVowelMatch -> 0.95f
            else -> 0.0f
        }

        // 3. Final consonant / Batchim (weight 0.25)
        val tBatchimClass = batchimNeutralClass(tJong)
        val rBatchimClass = batchimNeutralClass(rJong)
        val jongScore = when {
            tJong == rJong -> 1.0f
            tBatchimClass == rBatchimClass -> 0.95f
            else -> 0.0f
        }

        val combined = (choScore * 0.40f) + (jungScore * 0.35f) + (jongScore * 0.25f)
        val issue = when {
            tCho != rCho && sameConsonantSeries -> PhoneticIssueType.CONSONANT_SERIES
            tCho != rCho -> PhoneticIssueType.INITIAL_CONSONANT
            tJung != rJung && !mergedVowelMatch -> PhoneticIssueType.VOWEL_DIFFERENCE
            tBatchimClass != rBatchimClass -> PhoneticIssueType.BATCHIM_DIFFERENCE
            else -> null
        }

        return min(combined, if (issue == null) 1.0f else 0.96f) to issue
    }

    private fun buildFeedback(
        scorePercent: Int,
        isExact: Boolean,
        passed: Boolean,
        dominantIssue: PhoneticIssueType?,
        drill: PronunciationDrillItem
    ): Pair<String, String> = when {
        isExact -> Pair(
            "Spot-on recognition! Your Korean matched \"${drill.targetWrittenKo}\" cleanly.",
            "Pengenalan tepat! Ucapan Korea Anda cocok bersih dengan \"${drill.targetWrittenKo}\"."
        )
        passed -> Pair(
            "Clear pronunciation ($scorePercent% Hangul match). Very close to the target pattern.",
            "Pelafalan sudah jelas ($scorePercent% kecocokan Hangul). Sangat dekat dengan pola target."
        )
        dominantIssue == PhoneticIssueType.CONSONANT_SERIES -> Pair(
            "Almost there ($scorePercent%). Watch the plain / tense / aspirated consonant contrast.",
            "Hampir tepat ($scorePercent%). Perhatikan perbedaan konsonan biasa / tegang / aspirasi."
        )
        dominantIssue == PhoneticIssueType.BATCHIM_DIFFERENCE -> Pair(
            "Close ($scorePercent%). Pay attention to the final batchim consonant at the end of the syllable.",
            "Hampir cocok ($scorePercent%). Perhatikan konsonan akhir (batchim) pada penutup suku kata."
        )
        dominantIssue == PhoneticIssueType.VOWEL_DIFFERENCE -> Pair(
            "Keep practicing ($scorePercent%). Listen again to the exact vowel shape.",
            "Terus latih ($scorePercent%). Dengarkan kembali bentuk vokal yang tepat."
        )
        else -> Pair(
            "Speech recognition heard something different ($scorePercent% match). Listen once more and retry.",
            "Pengenalan suara menangkap teks yang berbeda (kecocokan $scorePercent%). Dengarkan lagi lalu coba ulang."
        )
    }

    private fun buildPhoneticHint(
        dominantIssue: PhoneticIssueType?,
        drill: PronunciationDrillItem
    ): Pair<String, String> = when (dominantIssue) {
        PhoneticIssueType.CONSONANT_SERIES -> Pair(
            "Consonant contrast tip: ${drill.coachingTipEn}",
            "Tips kontras konsonan: ${drill.coachingTipId}"
        )
        PhoneticIssueType.BATCHIM_DIFFERENCE -> Pair(
            "Batchim tip: Hold the final consonant cleanly without adding an extra vowel after it.",
            "Tips batchim: Tahan konsonan akhir dengan bersih tanpa menambahkan bunyi vokal ekstra."
        )
        PhoneticIssueType.VOWEL_DIFFERENCE -> Pair(
            "Vowel tip: Keep your lip rounding steady (ㅗ/ㅜ rounded vs. ㅓ/ㅡ unrounded).",
            "Tips vokal: Jaga bentuk bibir tetap konsisten (ㅗ/ㅜ bulat vs. ㅓ/ㅡ tidak bulat)."
        )
        else -> drill.coachingTipEn to drill.coachingTipId
    }

    const val PASS_THRESHOLD_PERCENT = 80
}
