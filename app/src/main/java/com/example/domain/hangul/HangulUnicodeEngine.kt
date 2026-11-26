package com.example.domain.hangul

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot

data class HangulJamoInfo(
    val glyph: String,
    val nameKo: String,
    val romanization: String,
    val pronunciationNote: String,
    val category: JamoCategory,
    val sampleWordKo: String,
    val sampleWordMeaning: String,
    val guideStrokes: List<List<Offset>> = emptyList() // Normalized 0f..1f coordinates
)

enum class JamoCategory(val title: String) {
    BASIC_CONSONANT("Consonants"),
    DOUBLE_CONSONANT("Tense"),
    BASIC_VOWEL("Vowels"),
    COMPOUND_VOWEL("Compound"),
    BATCHIM("Batchim")
}

data class SoundChangeRule(
    val id: String,
    val title: String,
    val titleKo: String,
    val explanation: String,
    val exampleWritten: String,
    val examplePronounced: String,
    val exampleRomanization: String,
    val exampleMeaning: String
)

data class KoreanNumberEntry(
    val value: Int,
    val nativeKo: String,
    val nativeRom: String,
    val sinoKo: String,
    val sinoRom: String,
    val usageNote: String
)

data class StrokeEvaluationResult(
    val passed: Boolean,
    val coveragePercent: Int,
    val strokeCountMatched: Boolean,
    val feedback: String
)

/**
 * Reliable Korean Unicode Hangul processor (U+AC00..U+D7A3) and guided stroke-order engine.
 */
object HangulUnicodeEngine {

    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_END = 0xD7A3

    val CHOSEONG = listOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    val JUNGSEONG = listOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ',
        'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    val JONGSEONG = listOf(
        null, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
        'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ',
        'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    /**
     * Composes a valid Unicode Hangul syllable block from initial consonant (choseong),
     * medial vowel (jungseong), and optional final consonant / batchim (jongseong).
     */
    fun composeSyllable(initial: Char, vowel: Char, batchim: Char? = null): String? {
        val lIndex = CHOSEONG.indexOf(initial)
        val vIndex = JUNGSEONG.indexOf(vowel)
        val tIndex = if (batchim == null) 0 else JONGSEONG.indexOf(batchim)
        if (lIndex < 0 || vIndex < 0 || tIndex < 0) return null
        val codePoint = HANGUL_BASE + (lIndex * 21 + vIndex) * 28 + tIndex
        return codePoint.toChar().toString()
    }

    fun decomposeSyllable(syllable: Char): Triple<Char, Char, Char?>? {
        val code = syllable.code
        if (code !in HANGUL_BASE..HANGUL_END) return null
        val offset = code - HANGUL_BASE
        val lIndex = offset / (21 * 28)
        val vIndex = (offset % (21 * 28)) / 28
        val tIndex = offset % 28
        return Triple(CHOSEONG[lIndex], JUNGSEONG[vIndex], JONGSEONG[tIndex])
    }

    fun containsHangul(text: String): Boolean =
        text.any { it.code in HANGUL_BASE..HANGUL_END || it in CHOSEONG || it in JUNGSEONG }

    fun hasBatchim(syllable: Char): Boolean {
        val parts = decomposeSyllable(syllable) ?: return false
        return parts.third != null
    }

    fun getBatchim(syllable: Char): Char? = decomposeSyllable(syllable)?.third

    fun romanizeSyllable(initial: Char, vowel: Char, batchim: Char?): String {
        val initMap = mapOf(
            'ㄱ' to "g", 'ㄲ' to "kk", 'ㄴ' to "n", 'ㄷ' to "d", 'ㄸ' to "tt",
            'ㄹ' to "r", 'ㅁ' to "m", 'ㅂ' to "b", 'ㅃ' to "pp", 'ㅅ' to "s",
            'ㅆ' to "ss", 'ㅇ' to "", 'ㅈ' to "j", 'ㅉ' to "jj", 'ㅊ' to "ch",
            'ㅋ' to "k", 'ㅌ' to "t", 'ㅍ' to "p", 'ㅎ' to "h"
        )
        val vowelMap = mapOf(
            'ㅏ' to "a", 'ㅐ' to "ae", 'ㅑ' to "ya", 'ㅒ' to "yae", 'ㅓ' to "eo",
            'ㅔ' to "e", 'ㅕ' to "yeo", 'ㅖ' to "ye", 'ㅗ' to "o", 'ㅘ' to "wa",
            'ㅙ' to "wae", 'ㅚ' to "oe", 'ㅛ' to "yo", 'ㅜ' to "u", 'ㅝ' to "wo",
            'ㅞ' to "we", 'ㅟ' to "wi", 'ㅠ' to "yu", 'ㅡ' to "eu", 'ㅢ' to "ui", 'ㅣ' to "i"
        )
        val finalMap = mapOf(
            'ㄱ' to "k", 'ㄲ' to "k", 'ㄴ' to "n", 'ㄷ' to "t", 'ㄹ' to "l",
            'ㅁ' to "m", 'ㅂ' to "p", 'ㅅ' to "t", 'ㅆ' to "t", 'ㅇ' to "ng",
            'ㅈ' to "t", 'ㅊ' to "t", 'ㅋ' to "k", 'ㅌ' to "t", 'ㅍ' to "p", 'ㅎ' to "t"
        )
        return (initMap[initial] ?: "") + (vowelMap[vowel] ?: "") + (batchim?.let { finalMap[it] } ?: "")
    }

    /**
     * Validates drawn user strokes against canonical Hangul stroke-order guide paths.
     * Checks stroke count, start-to-end directional alignment, and normalized path proximity.
     */
    fun evaluateStrokes(
        userStrokes: List<List<Offset>>,
        guideStrokes: List<List<Offset>>,
        canvasSizePx: Float
    ): StrokeEvaluationResult {
        if (guideStrokes.isEmpty() || canvasSizePx <= 0f) {
            return StrokeEvaluationResult(
                passed = false,
                coveragePercent = 0,
                strokeCountMatched = false,
                feedback = "Stroke guide is unavailable for this character."
            )
        }
        if (userStrokes.isEmpty()) {
            return StrokeEvaluationResult(
                passed = false,
                coveragePercent = 0,
                strokeCountMatched = false,
                feedback = "Draw the character on the grid first, following the numbered stroke guides."
            )
        }

        val normalizedUser = userStrokes.map { stroke ->
            stroke.map { pt -> Offset((pt.x / canvasSizePx).coerceIn(0f, 1f), (pt.y / canvasSizePx).coerceIn(0f, 1f)) }
        }

        val strokeCountMatched = normalizedUser.size == guideStrokes.size
        val guideSamplePoints = guideStrokes.flatMap { samplePolyline(it, 14) }
        val userSamplePoints = normalizedUser.flatMap { samplePolyline(it, 16) }

        var covered = 0
        for (gp in guideSamplePoints) {
            val minDist = userSamplePoints.minOfOrNull { up -> hypot(gp.x - up.x, gp.y - up.y) } ?: 1f
            if (minDist <= 0.14f) covered++
        }
        val coverage = ((covered.toFloat() / guideSamplePoints.size.coerceAtLeast(1)) * 100f).toInt()

        // Verify direction of paired strokes (start near guide start, end near guide end)
        var directionMatches = 0
        val pairedCount = minOf(normalizedUser.size, guideStrokes.size)
        for (i in 0 until pairedCount) {
            val uStart = normalizedUser[i].first()
            val uEnd = normalizedUser[i].last()
            val gStart = guideStrokes[i].first()
            val gEnd = guideStrokes[i].last()
            val startDist = hypot(uStart.x - gStart.x, uStart.y - gStart.y)
            val endDist = hypot(uEnd.x - gEnd.x, uEnd.y - gEnd.y)
            if (startDist <= 0.24f && endDist <= 0.24f) {
                directionMatches++
            }
        }

        val passed = coverage >= 65 && strokeCountMatched && directionMatches >= (guideStrokes.size + 1) / 2
        val feedback = when {
            passed -> "Accurate form and stroke direction ($coverage% guide alignment)."
            !strokeCountMatched && normalizedUser.size < guideStrokes.size ->
                "Expected ${guideStrokes.size} stroke(s), drew ${normalizedUser.size}. Tap Demonstrate to review stroke order."
            !strokeCountMatched ->
                "Too many strokes (${normalizedUser.size} vs ${guideStrokes.size}). Keep each stroke continuous."
            directionMatches < (guideStrokes.size + 1) / 2 ->
                "Check stroke direction: Korean strokes generally move left-to-right and top-to-bottom."
            else ->
                "Guide alignment is $coverage%. Try tracing closer to the lavender guide lines."
        }
        return StrokeEvaluationResult(
            passed = passed,
            coveragePercent = coverage,
            strokeCountMatched = strokeCountMatched,
            feedback = feedback
        )
    }

    private fun samplePolyline(points: List<Offset>, samplesPerSegment: Int): List<Offset> {
        if (points.size <= 1) return points
        val out = mutableListOf<Offset>()
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            for (s in 0..samplesPerSegment) {
                val t = s.toFloat() / samplesPerSegment
                out.add(Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t))
            }
        }
        return out
    }

    val basicConsonants: List<HangulJamoInfo> = listOf(
        HangulJamoInfo("ㄱ", "기역", "g / k", "Soft g between vowels; crisp k initially or as batchim.", JamoCategory.BASIC_CONSONANT, "가방", "Bag",
            listOf(listOf(Offset(0.24f, 0.25f), Offset(0.76f, 0.25f), Offset(0.76f, 0.78f)))),
        HangulJamoInfo("ㄴ", "니은", "n", "Tongue touches upper alveolar ridge, like 'n' in 'noon'.", JamoCategory.BASIC_CONSONANT, "나무", "Tree",
            listOf(listOf(Offset(0.26f, 0.24f), Offset(0.26f, 0.76f), Offset(0.78f, 0.76f)))),
        HangulJamoInfo("ㄷ", "디귿", "d / t", "Voiceless t at start; voiced d between vowels; unreleased t as batchim.", JamoCategory.BASIC_CONSONANT, "다리", "Bridge / Leg",
            listOf(listOf(Offset(0.25f, 0.25f), Offset(0.76f, 0.25f)), listOf(Offset(0.25f, 0.25f), Offset(0.25f, 0.76f), Offset(0.76f, 0.76f)))),
        HangulJamoInfo("ㄹ", "리을", "r / l", "Quick flap r between vowels; clear l at end of a syllable.", JamoCategory.BASIC_CONSONANT, "라면", "Ramen",
            listOf(
                listOf(Offset(0.25f, 0.22f), Offset(0.75f, 0.22f), Offset(0.75f, 0.48f)),
                listOf(Offset(0.25f, 0.48f), Offset(0.75f, 0.48f)),
                listOf(Offset(0.25f, 0.48f), Offset(0.25f, 0.78f), Offset(0.76f, 0.78f))
            )),
        HangulJamoInfo("ㅁ", "미음", "m", "Closed-lip nasal m, identical to English 'm'.", JamoCategory.BASIC_CONSONANT, "마음", "Heart / Mind",
            listOf(
                listOf(Offset(0.25f, 0.24f), Offset(0.25f, 0.76f)),
                listOf(Offset(0.25f, 0.24f), Offset(0.75f, 0.24f), Offset(0.75f, 0.76f)),
                listOf(Offset(0.25f, 0.76f), Offset(0.75f, 0.76f))
            )),
        HangulJamoInfo("ㅂ", "비읍", "b / p", "Soft p initially, voiced b between vowels, unreleased p in batchim.", JamoCategory.BASIC_CONSONANT, "바다", "Sea",
            listOf(
                listOf(Offset(0.26f, 0.22f), Offset(0.26f, 0.78f)),
                listOf(Offset(0.74f, 0.22f), Offset(0.74f, 0.78f)),
                listOf(Offset(0.26f, 0.50f), Offset(0.74f, 0.50f)),
                listOf(Offset(0.26f, 0.78f), Offset(0.74f, 0.78f))
            )),
        HangulJamoInfo("ㅅ", "시옷", "s / sh", "Soft s; becomes 'sh' before ㅣ, ㅑ, ㅕ, ㅛ, ㅠ.", JamoCategory.BASIC_CONSONANT, "사람", "Person",
            listOf(
                listOf(Offset(0.50f, 0.22f), Offset(0.24f, 0.78f)),
                listOf(Offset(0.46f, 0.46f), Offset(0.76f, 0.78f))
            )),
        HangulJamoInfo("ㅇ", "이응", "silent / ng", "Silent placeholder as initial consonant; 'ng' as batchim.", JamoCategory.BASIC_CONSONANT, "아이", "Child",
            listOf(listOf(
                Offset(0.50f, 0.24f), Offset(0.30f, 0.34f), Offset(0.24f, 0.50f),
                Offset(0.30f, 0.68f), Offset(0.50f, 0.76f), Offset(0.70f, 0.68f),
                Offset(0.76f, 0.50f), Offset(0.70f, 0.34f), Offset(0.50f, 0.24f)
            ))),
        HangulJamoInfo("ㅈ", "지읒", "j / ch", "Unaspirated j/ch sound; unreleased t in batchim.", JamoCategory.BASIC_CONSONANT, "자동차", "Car",
            listOf(
                listOf(Offset(0.24f, 0.26f), Offset(0.76f, 0.26f), Offset(0.24f, 0.78f)),
                listOf(Offset(0.48f, 0.48f), Offset(0.76f, 0.78f))
            )),
        HangulJamoInfo("ㅊ", "치읓", "ch", "Strongly aspirated 'ch' with a clear puff of air.", JamoCategory.BASIC_CONSONANT, "친구", "Friend",
            listOf(
                listOf(Offset(0.42f, 0.18f), Offset(0.58f, 0.18f)),
                listOf(Offset(0.24f, 0.32f), Offset(0.76f, 0.32f), Offset(0.24f, 0.80f)),
                listOf(Offset(0.48f, 0.52f), Offset(0.76f, 0.80f))
            )),
        HangulJamoInfo("ㅋ", "키읔", "k", "Strongly aspirated 'k' like 'k' in 'key'.", JamoCategory.BASIC_CONSONANT, "커피", "Coffee",
            listOf(
                listOf(Offset(0.24f, 0.24f), Offset(0.76f, 0.24f), Offset(0.76f, 0.78f)),
                listOf(Offset(0.24f, 0.50f), Offset(0.74f, 0.50f))
            )),
        HangulJamoInfo("ㅌ", "티읕", "t", "Strongly aspirated 't' like 't' in 'tea'.", JamoCategory.BASIC_CONSONANT, "토마토", "Tomato",
            listOf(
                listOf(Offset(0.25f, 0.24f), Offset(0.75f, 0.24f)),
                listOf(Offset(0.25f, 0.50f), Offset(0.72f, 0.50f)),
                listOf(Offset(0.25f, 0.24f), Offset(0.25f, 0.76f), Offset(0.76f, 0.76f))
            )),
        HangulJamoInfo("ㅍ", "피읖", "p", "Strongly aspirated 'p' like 'p' in 'pie'.", JamoCategory.BASIC_CONSONANT, "포도", "Grape",
            listOf(
                listOf(Offset(0.22f, 0.26f), Offset(0.78f, 0.26f)),
                listOf(Offset(0.36f, 0.26f), Offset(0.36f, 0.74f)),
                listOf(Offset(0.64f, 0.26f), Offset(0.64f, 0.74f)),
                listOf(Offset(0.22f, 0.74f), Offset(0.78f, 0.74f))
            )),
        HangulJamoInfo("ㅎ", "히읗", "h", "Breathy h sound; often softens or aspirates adjacent stops.", JamoCategory.BASIC_CONSONANT, "하늘", "Sky",
            listOf(
                listOf(Offset(0.42f, 0.16f), Offset(0.58f, 0.16f)),
                listOf(Offset(0.22f, 0.30f), Offset(0.78f, 0.30f)),
                listOf(
                    Offset(0.50f, 0.42f), Offset(0.32f, 0.52f), Offset(0.32f, 0.70f),
                    Offset(0.50f, 0.80f), Offset(0.68f, 0.70f), Offset(0.68f, 0.52f), Offset(0.50f, 0.42f)
                )
            ))
    )

    val doubleConsonants: List<HangulJamoInfo> = listOf(
        HangulJamoInfo("ㄲ", "쌍기역", "kk", "Tense unaspirated k, higher pitch onset, no puff of air.", JamoCategory.DOUBLE_CONSONANT, "꽃", "Flower"),
        HangulJamoInfo("ㄸ", "쌍디귿", "tt", "Tense unaspirated t, crisp release with stiff vocal folds.", JamoCategory.DOUBLE_CONSONANT, "딸기", "Strawberry"),
        HangulJamoInfo("ㅃ", "쌍비읍", "pp", "Tense unaspirated p, like 'p' in 'spy'.", JamoCategory.DOUBLE_CONSONANT, "빵", "Bread"),
        HangulJamoInfo("ㅆ", "쌍시옷", "ss", "Sharp, tense s sound with strong airflow.", JamoCategory.DOUBLE_CONSONANT, "쓰다", "To write / use"),
        HangulJamoInfo("ㅉ", "쌍지읒", "jj", "Tense unaspirated j/ch with tight constriction.", JamoCategory.DOUBLE_CONSONANT, "짜다", "Salty")
    )

    val basicVowels: List<HangulJamoInfo> = listOf(
        HangulJamoInfo("ㅏ", "아", "a", "Open 'a' as in 'father'.", JamoCategory.BASIC_VOWEL, "아침", "Morning",
            listOf(listOf(Offset(0.45f, 0.18f), Offset(0.45f, 0.82f)), listOf(Offset(0.45f, 0.50f), Offset(0.72f, 0.50f)))),
        HangulJamoInfo("ㅑ", "야", "ya", "'ya' as in 'yard'.", JamoCategory.BASIC_VOWEL, "야구", "Baseball",
            listOf(listOf(Offset(0.44f, 0.18f), Offset(0.44f, 0.82f)), listOf(Offset(0.44f, 0.40f), Offset(0.72f, 0.40f)), listOf(Offset(0.44f, 0.60f), Offset(0.72f, 0.60f)))),
        HangulJamoInfo("ㅓ", "어", "eo", "Open-mid back unrounded vowel, similar to 'u' in 'sun' with open jaw.", JamoCategory.BASIC_VOWEL, "어머니", "Mother",
            listOf(listOf(Offset(0.28f, 0.50f), Offset(0.56f, 0.50f)), listOf(Offset(0.56f, 0.18f), Offset(0.56f, 0.82f)))),
        HangulJamoInfo("ㅕ", "여", "yeo", "y + eo glide.", JamoCategory.BASIC_VOWEL, "여름", "Summer"),
        HangulJamoInfo("ㅗ", "오", "o", "Rounded 'o' with tight lips.", JamoCategory.BASIC_VOWEL, "오늘", "Today",
            listOf(listOf(Offset(0.50f, 0.28f), Offset(0.50f, 0.62f)), listOf(Offset(0.20f, 0.62f), Offset(0.80f, 0.62f)))),
        HangulJamoInfo("ㅛ", "요", "yo", "y + rounded o.", JamoCategory.BASIC_VOWEL, "요리", "Cooking"),
        HangulJamoInfo("ㅜ", "우", "u", "Rounded 'oo' as in 'moon'.", JamoCategory.BASIC_VOWEL, "우유", "Milk",
            listOf(listOf(Offset(0.20f, 0.38f), Offset(0.80f, 0.38f)), listOf(Offset(0.50f, 0.38f), Offset(0.50f, 0.74f)))),
        HangulJamoInfo("ㅠ", "유", "yu", "'you' sound.", JamoCategory.BASIC_VOWEL, "유리", "Glass"),
        HangulJamoInfo("ㅡ", "으", "eu", "Unrounded close back vowel — pull lips flat while saying 'oo'.", JamoCategory.BASIC_VOWEL, "음악", "Music",
            listOf(listOf(Offset(0.20f, 0.50f), Offset(0.80f, 0.50f)))),
        HangulJamoInfo("ㅣ", "이", "i", "High front 'ee' as in 'see'.", JamoCategory.BASIC_VOWEL, "이름", "Name",
            listOf(listOf(Offset(0.50f, 0.18f), Offset(0.50f, 0.82f))))
    )

    val compoundVowels: List<HangulJamoInfo> = listOf(
        HangulJamoInfo("ㅐ", "애", "ae", "Modern Korean merges ㅐ and ㅔ closely to 'e' as in 'bed'.", JamoCategory.COMPOUND_VOWEL, "개", "Dog"),
        HangulJamoInfo("ㅔ", "에", "e", "'e' as in 'met'.", JamoCategory.COMPOUND_VOWEL, "세계", "World"),
        HangulJamoInfo("ㅒ", "얘", "yae", "'ye' as in 'yes'.", JamoCategory.COMPOUND_VOWEL, "얘기", "Story / talk"),
        HangulJamoInfo("ㅖ", "예", "ye", "'ye' as in 'yesterday'.", JamoCategory.COMPOUND_VOWEL, "예약", "Reservation"),
        HangulJamoInfo("ㅘ", "와", "wa", "ㅗ + ㅏ glide ('wa').", JamoCategory.COMPOUND_VOWEL, "과일", "Fruit"),
        HangulJamoInfo("ㅙ", "왜", "wae", "ㅗ + ㅐ ('we' as in 'wet').", JamoCategory.COMPOUND_VOWEL, "왜", "Why"),
        HangulJamoInfo("ㅚ", "외", "oe / we", "Pronounced like 'we' in modern Seoul Korean.", JamoCategory.COMPOUND_VOWEL, "외국", "Foreign country"),
        HangulJamoInfo("ㅝ", "워", "wo", "ㅜ + ㅓ ('wo' as in 'wonder').", JamoCategory.COMPOUND_VOWEL, "병원", "Hospital"),
        HangulJamoInfo("ㅞ", "웨", "we", "ㅜ + ㅔ ('we' as in 'wedding').", JamoCategory.COMPOUND_VOWEL, "웨딩", "Wedding"),
        HangulJamoInfo("ㅟ", "위", "wi", "ㅜ + ㅣ ('wee').", JamoCategory.COMPOUND_VOWEL, "위험", "Danger"),
        HangulJamoInfo("ㅢ", "의", "ui", "ㅡ + ㅣ glide; often pronounced 'i' or 'e' in non-initial/possessive positions.", JamoCategory.COMPOUND_VOWEL, "의사", "Doctor")
    )

    val batchimGroups: List<HangulJamoInfo> = listOf(
        HangulJamoInfo("ㄱ / ㅋ / ㄲ", "대표음 [ㄱ]", "-k", "Unreleased final [k] stop (as in 책, 부엌, 밖).", JamoCategory.BATCHIM, "책", "Book"),
        HangulJamoInfo("ㄴ", "대표음 [ㄴ]", "-n", "Alveolar nasal [n] (as in 산, 눈).", JamoCategory.BATCHIM, "산", "Mountain"),
        HangulJamoInfo("ㄷ / ㅅ / ㅆ / ㅈ / ㅊ / ㅌ / ㅎ", "대표음 [ㄷ]", "-t", "All neutralize to unreleased [t] at syllable end (as in 옷, 낮, 꽃, 끝).", JamoCategory.BATCHIM, "꽃", "Flower"),
        HangulJamoInfo("ㄹ", "대표음 [ㄹ]", "-l", "Clear final [l] sound (as in 물, 달).", JamoCategory.BATCHIM, "물", "Water"),
        HangulJamoInfo("ㅁ", "대표음 [ㅁ]", "-m", "Closed-lip nasal [m] (as in 밤, 마음).", JamoCategory.BATCHIM, "밤", "Night / Chestnut"),
        HangulJamoInfo("ㅂ / ㅍ", "대표음 [ㅂ]", "-p", "Unreleased final [p] stop (as in 밥, 앞).", JamoCategory.BATCHIM, "밥", "Cooked rice / Meal"),
        HangulJamoInfo("ㅇ", "대표음 [ㅇ]", "-ng", "Velar nasal [ng] (as in 방, 강).", JamoCategory.BATCHIM, "방", "Room")
    )

    val soundChangeRules: List<SoundChangeRule> = listOf(
        SoundChangeRule(
            id = "liaison",
            title = "Resyllabification (Liaison)",
            titleKo = "연음 법칙",
            explanation = "When a batchim consonant (other than ㅇ) is followed by a syllable starting with silent ㅇ, the final consonant smoothly links over as the next syllable's initial sound.",
            exampleWritten = "한국어",
            examplePronounced = "[한구거]",
            exampleRomanization = "han-gu-geo",
            exampleMeaning = "Korean language"
        ),
        SoundChangeRule(
            id = "nasalization",
            title = "Nasal Assimilation",
            titleKo = "비음화",
            explanation = "Final stops ㄱ, ㄷ, ㅂ assimilate into nasals ㅇ, ㄴ, ㅁ respectively when followed by ㄴ or ㅁ.",
            exampleWritten = "감사합니다",
            examplePronounced = "[감사함니다]",
            exampleRomanization = "gam-sa-ham-ni-da",
            exampleMeaning = "Thank you (formal)"
        ),
        SoundChangeRule(
            id = "tensification",
            title = "Post-Obstruent Tensification",
            titleKo = "경음화",
            explanation = "Plain consonants ㄱ, ㄷ, ㅂ, ㅅ, ㅈ become tense (ㄲ, ㄸ, ㅃ, ㅆ, ㅉ) after final stops ㄱ, ㄷ, ㅂ.",
            exampleWritten = "학교",
            examplePronounced = "[학꾜]",
            exampleRomanization = "hak-kkyo",
            exampleMeaning = "School"
        ),
        SoundChangeRule(
            id = "aspiration",
            title = "Aspiration (ㅎ Fusion)",
            titleKo = "격음화",
            explanation = "When ㅎ meets ㄱ, ㄷ, ㅂ, or ㅈ in either order, they merge into aspirated ㅋ, ㅌ, ㅍ, or ㅊ.",
            exampleWritten = "좋다",
            examplePronounced = "[조타]",
            exampleRomanization = "jo-ta",
            exampleMeaning = "To be good"
        ),
        SoundChangeRule(
            id = "liquidization",
            title = "Liquidization",
            titleKo = "유음화",
            explanation = "When ㄴ and ㄹ meet (ㄴ+ㄹ or ㄹ+ㄴ), both assimilate into a smooth double [ㄹ] sound.",
            exampleWritten = "신라 / 연락",
            examplePronounced = "[실라 / 열락]",
            exampleRomanization = "sil-la / yeol-lak",
            exampleMeaning = "Silla / Contact"
        ),
        SoundChangeRule(
            id = "palatalization",
            title = "Palatalization",
            titleKo = "구개음화",
            explanation = "Final ㄷ or ㅌ followed by grammatical suffix 이 shifts to ㅈ or ㅊ.",
            exampleWritten = "같이",
            examplePronounced = "[가치]",
            exampleRomanization = "ga-chi",
            exampleMeaning = "Together"
        )
    )

    val koreanNumbers: List<KoreanNumberEntry> = listOf(
        KoreanNumberEntry(1, "하나 (한)", "ha-na", "일", "il", "Native: counting items, hours, age. Sino: dates, minutes, money, floors."),
        KoreanNumberEntry(2, "둘 (두)", "dul", "이", "i", "두 시 (2 o'clock) vs 이 분 (2 minutes)."),
        KoreanNumberEntry(3, "셋 (세)", "set", "삼", "sam", "세 개 (3 items) vs 삼 월 (March)."),
        KoreanNumberEntry(4, "넷 (네)", "net", "사", "sa", "네 명 (4 people) vs 사 층 (4th floor)."),
        KoreanNumberEntry(5, "다섯", "da-seot", "오", "o", "다섯 시간 (5 hours) vs 오천 원 (5,000 won)."),
        KoreanNumberEntry(6, "여섯", "yeo-seot", "육", "yuk", "여섯 살 (6 years old) vs 육 일 (6th day)."),
        KoreanNumberEntry(7, "일곱", "il-gop", "칠", "chil", "일곱 시 (7 o'clock) vs 칠 월 (July)."),
        KoreanNumberEntry(8, "여덟", "yeo-deol", "팔", "pal", "여덟 권 (8 books) vs 팔십 (80)."),
        KoreanNumberEntry(9, "아홉", "a-hop", "구", "gu", "아홉 잔 (9 cups) vs 구 호선 (Line 9)."),
        KoreanNumberEntry(10, "열", "yeol", "십", "sip", "열 명 (10 people) vs 십 분 (10 minutes)."),
        KoreanNumberEntry(20, "스물 (스무)", "seu-mul", "이십", "i-sip", "스무 살 (20 years old) vs 이십 일 (20th)."),
        KoreanNumberEntry(100, "— (uses Sino)", "—", "백", "baek", "Native stops at 99; 100+ always uses Sino-Korean (백, 천, 만).")
    )

    val writableCharacters: List<HangulJamoInfo>
        get() = basicConsonants + basicVowels.filter { it.guideStrokes.isNotEmpty() }
}
