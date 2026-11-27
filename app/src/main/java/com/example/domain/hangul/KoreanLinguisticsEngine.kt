package com.example.domain.hangul

/**
 * Algorithmic Korean linguistics engine built on Unicode Hangul Jamo decomposition.
 * Provides Revised Romanization of Korean, phonological sound-change detection,
 * regular & irregular predicate conjugation, and grammatically accurate particle selection.
 */
object KoreanLinguisticsEngine {

    private val INITIAL_ROM = arrayOf(
        "g", "kk", "n", "d", "tt", "r", "m", "b", "pp",
        "s", "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
    )

    private val MEDIAL_ROM = arrayOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye",
        "o", "wa", "wae", "oe", "yo", "u", "wo", "we",
        "wi", "yu", "eu", "ui", "i"
    )

    private val FINAL_ROM = arrayOf(
        "", "k", "k", "k", "n", "n", "n", "t", "l",
        "k", "m", "l", "l", "l", "p", "l", "m", "p",
        "p", "t", "t", "ng", "t", "t", "k", "t", "p", "t"
    )

    private const val CHO_COUNT = 19
    private const val JUNG_COUNT = 21
    private const val JONG_COUNT = 28

    // Initial jamo indices:
    // 0:ㄱ 1:ㄲ 2:ㄴ 3:ㄷ 4:ㄸ 5:ㄹ 6:ㅁ 7:ㅂ 8:ㅃ 9:ㅅ 10:ㅆ 11:ㅇ 12:ㅈ 13:ㅉ 14:ㅊ 15:ㅋ 16:ㅌ 17:ㅍ 18:ㅎ
    // Medial jamo indices:
    // 0:ㅏ 1:ㅐ 2:ㅑ 3:ㅒ 4:ㅓ 5:ㅔ 6:ㅕ 7:ㅖ 8:ㅗ 9:ㅘ 10:ㅙ 11:ㅚ 12:ㅛ 13:ㅜ 14:ㅝ 15:ㅞ 16:ㅟ 17:ㅠ 18:ㅡ 19:ㅢ 20:ㅣ
    // Final jamo indices:
    // 0:none 1:ㄱ 2:ㄲ 3:ㄳ 4:ㄴ 5:ㄵ 6:ㄶ 7:ㄷ 8:ㄹ 9:ㄺ 10:ㄻ 11:ㄼ 12:ㄽ 13:ㄾ 14:ㄿ 15:ㅀ 16:ㅁ 17:ㅂ 18:ㅄ 19:ㅅ 20:ㅆ 21:ㅇ 22:ㅈ 23:ㅊ 24:ㅋ 25:ㅌ 26:ㅍ 27:ㅎ

    private fun isHangulSyllable(c: Char): Boolean = c.code in 0xAC00..0xD7A3

    private fun decomposeIndices(c: Char): Triple<Int, Int, Int>? {
        if (!isHangulSyllable(c)) return null
        val offset = c.code - 0xAC00
        val cho = offset / (JUNG_COUNT * JONG_COUNT)
        val jung = (offset % (JUNG_COUNT * JONG_COUNT)) / JONG_COUNT
        val jong = offset % JONG_COUNT
        return Triple(cho, jung, jong)
    }

    private fun composeIndices(cho: Int, jung: Int, jong: Int): Char =
        (0xAC00 + (cho * JUNG_COUNT + jung) * JONG_COUNT + jong).toChar()

    /**
     * Checks whether the last Hangul syllable of a Korean word has a final consonant (받침).
     */
    fun endsWithBatchim(word: String): Boolean {
        val last = word.lastOrNull { isHangulSyllable(it) } ?: return false
        return (decomposeIndices(last)?.third ?: 0) != 0
    }

    fun endsWithRieulBatchim(word: String): Boolean {
        val last = word.lastOrNull { isHangulSyllable(it) } ?: return false
        return decomposeIndices(last)?.third == 8
    }

    /**
     * Attaches a Korean particle pair with accurate batchim and ㄹ-batchim (으로/로) harmony.
     */
    fun attachParticle(word: String, withBatchim: String, withoutBatchim: String): String {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return trimmed
        if (withBatchim == "으로" && withoutBatchim == "로") {
            return if (endsWithBatchim(trimmed) && !endsWithRieulBatchim(trimmed)) {
                "${trimmed}으로"
            } else {
                "${trimmed}로"
            }
        }
        return if (endsWithBatchim(trimmed)) "$trimmed$withBatchim" else "$trimmed$withoutBatchim"
    }

    private val WHITESPACE_REGEX = Regex("\\s+")

    /**
     * Revised Romanization of Korean with common phonological assimilation rules
     * (palatalization, double-batchim & single-batchim liaison, ㅎ-drop, nasalization,
     * ㄹ-nasalization, liquidization, and 2-way aspiration).
     */
    fun romanize(text: String): String {
        val words = text.trim().split(WHITESPACE_REGEX).filter { it.isNotEmpty() }
        return words.joinToString(" ") { word -> romanizeWord(word) }
    }

    private fun romanizeWord(word: String): String {
        val chars = word.toCharArray()
        val syllables = chars.map { decomposeIndices(it) }.toMutableList()
        val out = mutableListOf<String>()

        var i = 0
        while (i < chars.size) {
            val cur = syllables[i]
            if (cur == null) {
                if (chars[i].isLetterOrDigit()) out.add(chars[i].toString())
                i++
                continue
            }
            var (cho, jung, jong) = cur
            val next = if (i + 1 < syllables.size) syllables[i + 1] else null

            // Handle phonological interaction with next syllable
            var nextOverrideCho: Int? = null
            if (next != null && jong != 0) {
                val (nCho, nJung, nJong) = next
                when {
                    // Palatalization: ㄷ/ㅌ + 이 -> 지/치
                    (jong == 7 || jong == 25) && nCho == 11 && nJung == 20 -> {
                        nextOverrideCho = if (jong == 7) 12 else 14
                        jong = 0
                    }
                    // ㅎ / ㄶ / ㅀ silent drop before vowel (좋아요 -> jo-a-yo, 많아요 -> ma-na-yo, 싫어요 -> si-reo-yo)
                    nCho == 11 && jong == 27 -> {
                        jong = 0
                    }
                    nCho == 11 && jong == 6 -> {
                        jong = 0
                        nextOverrideCho = 2 // ㄴ liaises
                    }
                    nCho == 11 && jong == 15 -> {
                        jong = 0
                        nextOverrideCho = 5 // ㄹ liaises
                    }
                    // Double-batchim liaison before silent ㅇ (앉아요, 읽어요, 삶이, 넓어요, 없어요)
                    nCho == 11 && jong in setOf(3, 5, 9, 10, 11, 12, 13, 14, 18) -> {
                        when (jong) {
                            3 -> { jong = 1; nextOverrideCho = 10 }  // ㄳ -> ㄱ + ㅆ
                            5 -> { jong = 4; nextOverrideCho = 12 }  // ㄵ -> ㄴ + ㅈ
                            9 -> { jong = 8; nextOverrideCho = 0 }   // ㄺ -> ㄹ + ㄱ
                            10 -> { jong = 8; nextOverrideCho = 6 }  // ㄻ -> ㄹ + ㅁ
                            11 -> { jong = 8; nextOverrideCho = 7 }  // ㄼ -> ㄹ + ㅂ
                            12 -> { jong = 8; nextOverrideCho = 10 } // ㄽ -> ㄹ + ㅆ
                            13 -> { jong = 8; nextOverrideCho = if (nJung == 20) 14 else 16 } // ㄾ -> ㄹ + ㅌ/ㅊ
                            14 -> { jong = 8; nextOverrideCho = 17 } // ㄿ -> ㄹ + ㅍ
                            18 -> { jong = 17; nextOverrideCho = 10 } // ㅄ -> ㅂ + ㅆ
                        }
                    }
                    // Liaison: single batchim (except ㅇ, ㅎ) before silent ㅇ
                    nCho == 11 && jong in setOf(1, 2, 4, 7, 8, 16, 17, 19, 20, 22, 23, 24, 25, 26) -> {
                        nextOverrideCho = when (jong) {
                            1 -> 0; 2 -> 1; 4 -> 2; 7 -> 3; 8 -> 5; 16 -> 6; 17 -> 7
                            19 -> 9; 20 -> 10; 22 -> 12; 23 -> 14; 24 -> 15; 25 -> 16; 26 -> 17
                            else -> nCho
                        }
                        jong = 0
                    }
                    // Two-way Aspiration: ㅎ / ㄶ / ㅀ + ㄱ/ㄷ/ㅈ -> ㅋ/ㅌ/ㅊ (좋다 -> jo-ta, 많다 -> man-ta, 싫다 -> sil-ta)
                    jong in setOf(27, 6, 15) && nCho in setOf(0, 3, 12) -> {
                        nextOverrideCho = when (nCho) {
                            0 -> 15  // ㅋ
                            3 -> 16  // ㅌ
                            12 -> 14 // ㅊ
                            else -> nCho
                        }
                        jong = when (jong) {
                            6 -> 4  // ㄶ -> ㄴ
                            15 -> 8 // ㅀ -> ㄹ
                            else -> 0
                        }
                    }
                    // ㅎ + ㅅ -> ㅆ
                    jong == 27 && nCho == 9 -> {
                        jong = 0
                        nextOverrideCho = 10
                    }
                    // Aspiration: ㄱ/ㄷ/ㅂ/ㅈ (and ㄺ/ㄼ/ㄵ) + ㅎ -> ㅋ/ㅌ/ㅍ/ㅊ
                    nCho == 18 && jong in setOf(1, 5, 7, 9, 11, 17, 22) -> {
                        nextOverrideCho = when (jong) {
                            1, 9 -> 15   // ㅋ
                            7 -> 16      // ㅌ
                            17, 11 -> 17 // ㅍ
                            22, 5 -> 14  // ㅊ
                            else -> 18
                        }
                        jong = when (jong) {
                            5 -> 4
                            9, 11 -> 8
                            else -> 0
                        }
                    }
                    // ㄹ-nasalization after ㅁ/ㅇ (음료수 -> eum-nyo-su, 종로 -> jong-no)
                    (jong == 16 || jong == 21) && nCho == 5 -> {
                        nextOverrideCho = 2
                    }
                    // Mutual nasalization: ㄱ/ㅂ + ㄹ -> ㅇ/ㅁ + ㄴ (독립 -> dong-nip, 협력 -> hyeom-nyeok)
                    jong in setOf(1, 2, 24, 17, 26) && nCho == 5 -> {
                        jong = if (jong in setOf(1, 2, 24)) 21 else 16
                        nextOverrideCho = 2
                    }
                    // Nasalization: ㄱ/ㄷ/ㅂ coda before ㄴ/ㅁ -> ㅇ/ㄴ/ㅁ
                    (nCho == 2 || nCho == 6) && jong in setOf(1, 2, 24, 7, 19, 20, 22, 23, 25, 17, 26) -> {
                        jong = when (jong) {
                            1, 2, 24 -> 21 // ㅇ
                            7, 19, 20, 22, 23, 25 -> 4 // ㄴ
                            17, 26 -> 16 // ㅁ
                            else -> jong
                        }
                    }
                    // Liquidization: ㄴ+ㄹ or ㄹ+ㄴ -> ll (신라 -> sil-la, 설날 -> seol-lal)
                    (jong == 4 && nCho == 5) -> {
                        jong = 8
                        nextOverrideCho = 5
                    }
                    (jong == 8 && nCho == 2) -> {
                        nextOverrideCho = 5
                    }
                }
                syllables[i] = Triple(cho, jung, jong)
                if (nextOverrideCho != null) {
                    syllables[i + 1] = Triple(nextOverrideCho, nJung, nJong)
                }
            }

            val choStr = if (cho == 5 && i > 0 && syllables[i - 1]?.third == 8) "l" else INITIAL_ROM[cho]
            val jungStr = MEDIAL_ROM[jung]
            val jongStr = FINAL_ROM[jong]
            out.add(choStr + jungStr + jongStr)
            i++
        }
        return out.joinToString("-")
    }

    /**
     * Detects real Korean phonological rules across syllable boundaries so learners
     * receive authentic pronunciation guidance on every vocabulary card.
     */
    fun detectPronunciationNote(hangul: String): String {
        val notes = mutableListOf<String>()
        for (i in 0 until hangul.length - 1) {
            val cur = decomposeIndices(hangul[i]) ?: continue
            val next = decomposeIndices(hangul[i + 1]) ?: continue
            val jong = cur.third
            val nCho = next.first
            val nJung = next.second
            if (jong == 0) continue

            when {
                (jong == 7 || jong == 25) && nCho == 11 && nJung == 20 ->
                    notes.add("Palatalization (구개음화): final ㄷ/ㅌ before 이 shifts to [지/치].")
                nCho == 11 && jong in setOf(6, 15, 27) ->
                    notes.add("ㅎ-weakening (ㅎ 탈락): final ㅎ drops silently before a vowel.")
                nCho == 11 && jong != 21 ->
                    notes.add("Liaison (연음): final consonant carries smoothly into the following vowel.")
                (jong in setOf(27, 6, 15) && nCho in setOf(0, 3, 12)) ||
                    (nCho == 18 && jong in setOf(1, 5, 7, 9, 11, 17, 22)) ->
                    notes.add("Aspiration (격음화): consonant merges with ㅎ into an aspirated stop [ㅋ/ㅌ/ㅍ/ㅊ].")
                (jong in setOf(1, 2, 16, 17, 21, 24, 26) && nCho == 5) ||
                    ((nCho == 2 || nCho == 6) && jong in setOf(1, 2, 7, 17, 19, 20, 22, 23, 24, 25, 26)) ->
                    notes.add("Nasalization (비음화): consonant boundary assimilates nasally (ㅁ/ㄴ/ㅇ).")
                (jong == 4 && nCho == 5) || (jong == 8 && nCho == 2) ->
                    notes.add("Liquidization (유음화): ㄴ + ㄹ / ㄹ + ㄴ assimilates to flowing [ll].")
                jong in setOf(1, 7, 17) && nCho in setOf(0, 3, 7, 9, 12) ->
                    notes.add("Tensification (경음화): following consonant tenses after stop batchim.")
            }
            if (notes.isNotEmpty()) break
        }
        return notes.firstOrNull().orEmpty()
    }

    /**
     * Checks whether a Hangul syllable at [index] inside [word] undergoes phonological assimilation
     * with the following syllable (used to pick clean representative-coda batchim quiz items).
     */
    fun undergoesCodaAssimilationAt(word: String, index: Int): Boolean {
        if (index !in word.indices || index == word.lastIndex) return false
        val cur = decomposeIndices(word[index]) ?: return false
        val next = decomposeIndices(word[index + 1]) ?: return false
        val jong = cur.third
        val nCho = next.first
        if (jong == 0) return false
        return when {
            nCho == 11 -> true // Liaison or ㅎ-drop
            (jong in setOf(27, 6, 15) && nCho in setOf(0, 3, 9, 12)) -> true // Aspiration
            (nCho == 18 && jong in setOf(1, 5, 7, 9, 11, 17, 22)) -> true // Aspiration
            (nCho == 2 || nCho == 6) && jong in setOf(1, 2, 7, 17, 19, 20, 22, 23, 24, 25, 26) -> true // Nasalization
            nCho == 5 && jong in setOf(1, 2, 4, 16, 17, 21, 24, 26) -> true // ㄹ-nasalization / liquidization
            jong == 8 && nCho == 2 -> true // Liquidization
            else -> false
        }
    }

    private val BIEUP_IRREGULARS = setOf(
        "돕다", "곱다", "춥다", "덥다", "쉽다", "어렵다", "가깝다", "무겁다", "가볍다",
        "맵다", "아름답다", "고맙다", "반갑다", "부럽다", "무섭다", "귀엽다", "뜨겁다",
        "차갑다", "부드럽다", "시끄럽다", "어지럽다", "외롭다", "즐겁다", "새롭다",
        "어둡다", "눕다", "굽다", "줍다", "더럽다", "싱겁다", "그립다", "부끄럽다"
    )
    private val DIGEUT_IRREGULARS = setOf("듣다", "걷다", "묻다", "싣다", "깨닫다", "일컫다")
    private val SIOT_IRREGULARS = setOf("짓다", "낫다", "붓다", "젓다", "긋다", "잇다")
    private val REU_IRREGULARS = setOf(
        "모르다", "다르다", "빠르다", "부르다", "고르다", "오르다", "자르다", "기르다",
        "흐르다", "누르다", "마르다", "나르다", "서두르다", "서투르다", "게으르다",
        "바르다", "가르다", "어우르다", "이르다", "구르다"
    )
    private val HIEUT_IRREGULARS = setOf(
        "그렇다", "이렇다", "저렇다", "어떻다", "빨갛다", "파랗다",
        "노랗다", "하얗다", "까맣다", "동그랗다", "커다랗다"
    )
    private val SUPPLETIVE_HONORIFICS = mapOf(
        "먹다" to "드세요",
        "마시다" to "드세요",
        "자다" to "주무세요",
        "있다" to "계세요",
        "말하다" to "말씀하세요",
        "죽다" to "돌아가세요"
    )

    private fun resolveIrregularTag(hangul: String, explicitTag: String): String {
        if (explicitTag.isNotBlank()) return explicitTag
        return when {
            hangul in BIEUP_IRREGULARS || hangul.endsWith("스럽다") -> "ㅂ"
            hangul in DIGEUT_IRREGULARS -> "ㄷ"
            hangul in SIOT_IRREGULARS -> "ㅅ"
            hangul in REU_IRREGULARS -> "르"
            hangul in HIEUT_IRREGULARS -> "ㅎ"
            else -> ""
        }
    }

    /**
     * Conjugates Korean verbs and adjectives ending in 다 into standard polite/formal/past/future forms.
     * Supports regular stems, 하다 predicates, vowel contraction (including ㅚ -> ㅙ), ㅡ-drop, ㄹ-drop,
     * suppletive honorifics, and both marked and auto-detected irregulars (ㅂ, ㄷ, ㅅ, 르, ㅎ).
     */
    fun buildConjugationsSerialized(
        hangul: String,
        pos: String,
        irregularTag: String = ""
    ): String {
        if ((pos != "Verb" && pos != "Adjective") || !hangul.endsWith("다") || hangul.length < 2) {
            return ""
        }
        val effectiveIrregular = resolveIrregularTag(hangul, irregularTag)
        val stem = hangul.dropLast(1)
        val lastChar = stem.last()
        val lastDecomp = decomposeIndices(lastChar) ?: return ""
        val (cho, jung, jong) = lastDecomp
        val prefix = stem.dropLast(1)

        val politePresent = conjugatePolitePresent(stem, prefix, cho, jung, jong, effectiveIrregular)
        val politePast = conjugatePolitePast(politePresent)
        val formalPresent = conjugateFormalPresent(stem, prefix, cho, jung, jong)
        val politeFuture = conjugatePoliteFuture(stem, prefix, cho, jung, jong, effectiveIrregular)

        val entries = mutableListOf(
            "$politePresent::Polite present (해요체)",
            "$formalPresent::Formal present (합니다체)",
            "$politePast::Polite past",
            "$politeFuture::Polite future"
        )
        if (pos == "Verb") {
            val honorific = SUPPLETIVE_HONORIFICS[hangul]
                ?: conjugateHonorific(stem, prefix, cho, jung, jong, effectiveIrregular)
            entries.add("$honorific::Honorific polite (-(으)세요)")
        }
        return entries.joinToString("||")
    }

    // Standard Korean vowel harmony bright vowels (양성모음): ㅏ(0), ㅗ(8)
    private fun isBrightVowel(jung: Int): Boolean = jung == 0 || jung == 8

    private fun conjugatePolitePresent(
        stem: String,
        prefix: String,
        cho: Int,
        jung: Int,
        jong: Int,
        irregularTag: String
    ): String {
        if (stem.endsWith("하")) return stem.dropLast(1) + "해요"

        // Marked or auto-detected irregulars
        when (irregularTag) {
            "ㅂ" -> if (jong == 17) {
                val base = prefix + composeIndices(cho, jung, 0)
                return if (stem == "돕" || stem == "곱") base + "와요" else base + "워요"
            }
            "ㄷ" -> if (jong == 7) {
                val rChar = composeIndices(cho, jung, 8)
                return prefix + rChar + if (isBrightVowel(jung)) "아요" else "어요"
            }
            "ㅅ" -> if (jong == 19) {
                val bare = composeIndices(cho, jung, 0)
                return prefix + bare + if (isBrightVowel(jung)) "아요" else "어요"
            }
            "르" -> if (lastCharIsReu(cho, jung, jong) && prefix.isNotEmpty()) {
                val prev = decomposeIndices(prefix.last())
                if (prev != null && prev.third == 0) {
                    val newPrev = composeIndices(prev.first, prev.second, 8)
                    val end = if (isBrightVowel(prev.second)) "라요" else "러요"
                    return prefix.dropLast(1) + newPrev + end
                }
            }
            "ㅎ" -> if (jong == 27) {
                val newJung = if (jung == 2 || jung == 6) 3 else 1 // ㅐ / ㅒ
                return prefix + composeIndices(cho, newJung, 0) + "요"
            }
        }

        // With batchim (regular; includes standard 얇아요/얕아요 bright harmony)
        if (jong != 0) {
            val bright = isBrightVowel(jung) || stem == "얇" || stem == "얕"
            return stem + if (bright) "아요" else "어요"
        }

        // Open vowel stems
        return when (jung) {
            0, 1, 2, 3, 4, 5, 6, 7 -> stem + "요" // 가요, 보내요, 서요, 세요, 켜요
            8 -> prefix + composeIndices(cho, 9, 0) + "요" // 오 -> 와요
            11 -> prefix + composeIndices(cho, 10, 0) + "요" // 되 -> 돼요, 뵈 -> 봬요
            13 -> prefix + composeIndices(cho, 14, 0) + "요" // 배우 -> 배워요
            20 -> prefix + composeIndices(cho, 6, 0) + "요" // 마시 -> 마셔요
            18 -> { // ㅡ drop
                val prevBright = prefix.lastOrNull()?.let { decomposeIndices(it)?.second }?.let { isBrightVowel(it) } == true
                prefix + composeIndices(cho, if (prevBright) 0 else 4, 0) + "요"
            }
            else -> stem + "어요"
        }
    }

    private fun lastCharIsReu(cho: Int, jung: Int, jong: Int): Boolean = cho == 5 && jung == 18 && jong == 0

    private fun conjugatePolitePast(politePresent: String): String {
        if (!politePresent.endsWith("요") || politePresent.length < 2) return politePresent
        val beforeYo = politePresent.dropLast(1)
        val last = decomposeIndices(beforeYo.last()) ?: return politePresent
        if (last.third == 0) {
            val withSs = composeIndices(last.first, last.second, 20) // ㅆ batchim
            return beforeYo.dropLast(1) + withSs + "어요"
        }
        return beforeYo + "었어요"
    }

    private fun conjugateFormalPresent(
        stem: String,
        prefix: String,
        cho: Int,
        jung: Int,
        jong: Int
    ): String {
        return when (jong) {
            0 -> prefix + composeIndices(cho, jung, 17) + "니다" // ㅂ batchim + 니다
            8 -> prefix + composeIndices(cho, jung, 17) + "니다" // ㄹ drops before ㅂ니다
            else -> stem + "습니다"
        }
    }

    private fun conjugatePoliteFuture(
        stem: String,
        prefix: String,
        cho: Int,
        jung: Int,
        jong: Int,
        irregularTag: String
    ): String {
        return when {
            irregularTag == "ㅂ" && jong == 17 -> prefix + composeIndices(cho, jung, 0) + "울 거예요"
            irregularTag == "ㄷ" && jong == 7 -> prefix + composeIndices(cho, jung, 8) + "을 거예요"
            irregularTag == "ㅅ" && jong == 19 -> prefix + composeIndices(cho, jung, 0) + "을 거예요"
            irregularTag == "ㅎ" && jong == 27 -> prefix + composeIndices(cho, jung, 8) + " 거예요"
            jong == 0 -> prefix + composeIndices(cho, jung, 8) + " 거예요"
            jong == 8 -> stem + " 거예요"
            else -> stem + "을 거예요"
        }
    }

    private fun conjugateHonorific(
        stem: String,
        prefix: String,
        cho: Int,
        jung: Int,
        jong: Int,
        irregularTag: String
    ): String {
        return when {
            irregularTag == "ㅂ" && jong == 17 -> prefix + composeIndices(cho, jung, 0) + "우세요"
            irregularTag == "ㄷ" && jong == 7 -> prefix + composeIndices(cho, jung, 8) + "으세요"
            irregularTag == "ㅅ" && jong == 19 -> prefix + composeIndices(cho, jung, 0) + "으세요"
            jong == 0 -> stem + "세요"
            jong == 8 -> prefix + composeIndices(cho, jung, 0) + "세요" // ㄹ drops before ㅅ
            else -> stem + "으세요"
        }
    }

    // ==================== TOKEN-BOUNDARY & JAMO DISCOURSE / REGISTER ANALYZERS ====================

    private val NON_PARTICLE_NOUNS = setOf(
        "휴가", "화가", "작가", "국가", "가격", "사과", "결과", "효과", "통과", "교과", "학과", "치과", "내과", "외과",
        "포도", "지도", "속도", "온도", "정도", "태도", "제도", "기도", "수도", "복도", "각도", "밀도", "한도",
        "의미", "주의", "회의", "동의", "강의", "편의", "정의", "예의", "문의", "선의", "호의",
        "도로", "서로", "따로", "바로", "새로", "수로", "경로", "진로", "통로", "항로", "선로", "가로", "세로", "피로", "위로",
        "어머니", "아버지", "할머니", "할아버지", "고양이", "강아지", "어린이", "나비", "커피", "요리", "소리", "자리", "머리", "다리", "허리",
        "가을", "마을", "거울", "겨울", "서울", "하늘", "구름", "이름", "기분", "질문", "주문", "소문", "방문", "신분"
    )

    private val NON_CONNECTOR_NOUNS = setOf(
        "라면", "화면", "표면", "측면", "장면", "정면", "이면", "단면", "수면", "방면", "전면", "후면", "지면에", "냉면",
        "사고", "창고", "참고", "경고", "광고", "보고", "원고", "재고", "중고", "최고", "초고", "공고", "선고", "망고", "빙고"
    )

    private val NON_POLITE_YO_NOUNS = setOf(
        "필요", "중요", "개요", "주요", "소요", "수요", "동요", "민요", "가요", "도요"
    )

    private val MULTI_CHAR_PARTICLES = listOf(
        "에서", "으로", "하고", "부터", "까지", "보다", "에게", "한테", "처럼", "만큼", "밖에", "이랑", "이나", "라도"
    )

    private val STANDALONE_CONNECTORS = setOf(
        "그러나", "따라서", "또한", "그래서", "하지만", "그런데", "그러므로", "반면에",
        "그리고", "왜냐하면", "게다가", "결국", "우선", "반면"
    )

    private val MULTI_CHAR_CONNECTOR_ENDINGS = listOf(
        "때문에", "으면서", "더라도", "으려고", "으니까", "으므로", "이지만",
        "았는데", "었는데", "했는데", "위해서", "통해서", "따라서",
        "아서", "어서", "해서", "지만", "는데", "으면", "면서", "거나",
        "도록", "려고", "니까", "므로", "위해", "통해", "따라", "아도", "어도", "해도"
    )

    private val WRITTEN_PLAIN_ENDINGS = listOf(
        "는다", "이다", "된다", "있다", "없다", "한다", "겠다",
        "필요하다", "중요하다", "가능하다", "어렵다", "쉽다", "크다", "작다",
        "많다", "적다", "좋다", "높다", "낮다", "같다", "다르다"
    )

    private fun cleanKoreanTokens(text: String): List<String> =
        text.trim()
            .split(WHITESPACE_REGEX)
            .map { token -> token.trim { !isHangulSyllable(it) && !it.isLetterOrDigit() } }
            .filter { it.isNotEmpty() }

    /**
     * Counts distinct Korean word-final particles using token boundaries and phonological batchim rules
     * so bare noun lists (e.g., "사과 고기 라면 가방") do not produce false positives.
     */
    fun countWordFinalParticles(text: String): Int {
        val tokens = cleanKoreanTokens(text)
        val matched = HashSet<String>()
        for (token in tokens) {
            if (token.length < 2) continue
            if (token in NON_PARTICLE_NOUNS) continue
            val last = token.last()
            if (!isHangulSyllable(last)) continue
            // Skip clear sentence-final predicate endings
            if (last == '요' || last == '다' || last == '까') continue

            val multi = MULTI_CHAR_PARTICLES.firstOrNull { p ->
                token.length >= p.length + 1 && token.endsWith(p)
            }
            if (multi != null) {
                val stem = token.dropLast(multi.length)
                if (multi == "으로" && (!endsWithBatchim(stem) || endsWithRieulBatchim(stem))) {
                    continue
                }
                matched.add(multi)
                continue
            }

            val stem = token.dropLast(1)
            val prev = stem.lastOrNull() ?: continue
            if (!isHangulSyllable(prev)) continue
            val stemHasBatchim = endsWithBatchim(stem)

            val particle: String? = when (last) {
                '은' -> if (stemHasBatchim) "은" else null
                '는' -> if (!stemHasBatchim) "는" else null
                '이' -> if (stemHasBatchim) "이" else null
                '가' -> if (!stemHasBatchim) "가" else null
                '을' -> if (stemHasBatchim) "을" else null
                '를' -> if (!stemHasBatchim) "를" else null
                '과' -> if (stemHasBatchim) "과" else null
                '와' -> if (!stemHasBatchim) "와" else null
                '로' -> if (!stemHasBatchim || endsWithRieulBatchim(stem)) "로" else null
                '에', '도', '의' -> last.toString()
                else -> null
            }
            if (particle != null) {
                matched.add(particle)
            }
        }
        return matched.size
    }

    /**
     * Counts distinct Korean discourse connectors and clause-linking endings using token boundaries.
     */
    fun countDiscourseConnectors(text: String): Int {
        val tokens = cleanKoreanTokens(text)
        val matched = HashSet<String>()
        for (token in tokens) {
            if (token in STANDALONE_CONNECTORS) {
                matched.add(token)
                continue
            }
            if (token.length < 2 || token in NON_CONNECTOR_NOUNS) continue

            val multi = MULTI_CHAR_CONNECTOR_ENDINGS.firstOrNull { ending ->
                token.endsWith(ending) && (token == ending || token.length >= ending.length + 1)
            }
            if (multi != null) {
                matched.add(multi)
                continue
            }

            val last = token.last()
            if ((last == '고' || last == '면') && token.length >= 2 && token !in NON_CONNECTOR_NOUNS) {
                // Require preceding Hangul syllable to avoid non-Korean artifacts
                val prev = token[token.length - 2]
                if (isHangulSyllable(prev)) {
                    matched.add(last.toString())
                }
            }
        }
        return matched.size
    }

    /**
     * Detects formal polite Korean endings (합니다체), including both consonant-stem (-습니다/-습니까)
     * and open/ㄹ-stem composed ㅂ-batchim endings (갑니다, 합니다, 됩니다, 감사합니다, 가십니까).
     */
    fun hasFormalPoliteEnding(text: String): Boolean {
        val tokens = cleanKoreanTokens(text)
        return tokens.any { token ->
            if (token.length < 3) return@any false
            if (token.endsWith("습니다") || token.endsWith("습니까") ||
                token.endsWith("읍니다") || token.endsWith("읍니까")
            ) {
                return@any true
            }
            if (token.endsWith("니다") || token.endsWith("니까")) {
                val beforeEnding = token[token.length - 3]
                decomposeIndices(beforeEnding)?.third == 17 // ㅂ batchim (e.g., 갑니다, 합니다, 됩니다)
            } else {
                false
            }
        }
    }

    /**
     * Detects polite everyday Korean endings (해요체: -아요/어요/해요/예요/이에요/세요/주세요/죠/네요).
     */
    fun hasPoliteHaeyoEnding(text: String): Boolean {
        val tokens = cleanKoreanTokens(text)
        return tokens.any { token ->
            if (token.length < 2) return@any false
            if (token in NON_POLITE_YO_NOUNS) return@any false
            token.endsWith("요") || token.endsWith("죠")
        }
    }

    /**
     * Detects formal written plain-style endings (해라체 / 문어체: -는다, composed -ㄴ다 like 간다/한다/줄어든다,
     * past -았다/-었다/-했다 via ㅆ-batchim + 다, and descriptive -이다/-있다/-없다).
     */
    fun hasWrittenPlainEnding(text: String): Boolean {
        val tokens = cleanKoreanTokens(text)
        return tokens.any { token ->
            if (token.length < 2 || !token.endsWith("다")) return@any false
            if (WRITTEN_PLAIN_ENDINGS.any { token.endsWith(it) }) return@any true
            val beforeDa = decomposeIndices(token[token.length - 2]) ?: return@any false
            // Composed -ㄴ다 (jong == 4: 간다, 본다, 한다, 줄어든다) or past -았다/었다/했다 (jong == 20: 갔다, 늘었다)
            beforeDa.third == 4 || beforeDa.third == 20
        }
    }

    /**
     * Generates a grammatically natural Korean example sentence using correct Korean
     * particles (이/가, 을/를, 은/는) and proper conjugated forms matched to TOPIK level.
     */
    fun buildContextualExample(
        hangul: String,
        meaningEn: String,
        meaningId: String,
        pos: String,
        subCategory: String,
        irregularTag: String = ""
    ): Triple<String, String, String> {
        val hasBatchim = endsWithBatchim(hangul)
        val objParticle = if (hasBatchim) "을" else "를"
        val subjParticle = if (hasBatchim) "이" else "가"
        val topicParticle = if (hasBatchim) "은" else "는"
        val shortEn = meaningEn.substringBefore("/").substringBefore("(").trim()
        val shortId = meaningId.substringBefore("/").substringBefore("(").trim()
        val isAdvanced = subCategory in setOf("4급", "5급", "6급")

        return when (pos) {
            "Verb" -> {
                val conj = buildConjugationsSerialized(hangul, pos, irregularTag)
                val polite = conj.substringBefore("::").ifBlank { hangul }
                if (isAdvanced) {
                    Triple(
                        "이 문제를 해결하기 위해 함께 ${polite.removeSuffix("요")}야 합니다.",
                        "We need to ${shortEn.removePrefix("To ").removePrefix("to ").trim()} together to resolve this issue.",
                        "Kita perlu ${shortId.lowercase()} bersama untuk menyelesaikan masalah ini."
                    )
                } else {
                    Triple(
                        "오늘 시간이 있을 때 $polite.",
                        "When I have time today, I ${shortEn.removePrefix("To ").removePrefix("to ").trim()}.",
                        "Hari ini saat ada waktu, saya ${shortId.lowercase()}."
                    )
                }
            }
            "Adjective" -> {
                val conj = buildConjugationsSerialized(hangul, pos, irregularTag)
                val polite = conj.substringBefore("::").ifBlank { hangul }
                if (isAdvanced) {
                    Triple(
                        "이번 결과는 생각보다 매우 $polite.",
                        "This result is very ${shortEn.removePrefix("To be ").removePrefix("to be ").trim()} compared to expectations.",
                        "Hasil kali ini sangat ${shortId.lowercase()} dibandingkan perkiraan."
                    )
                } else {
                    Triple(
                        "이것은 정말 $polite.",
                        "This is really ${shortEn.removePrefix("To be ").removePrefix("to be ").trim()}.",
                        "Ini benar-benar ${shortId.lowercase()}."
                    )
                }
            }
            "Adverb" -> Triple(
                "한국어를 $hangul 연습하고 있어요.",
                "I am practicing Korean ($shortEn).",
                "Saya sedang berlatih bahasa Korea ($shortId)."
            )
            "Expression" -> Triple(
                "상황에 맞게 \"$hangul\"라고 말해요.",
                "Say \"$shortEn\" appropriately for the situation.",
                "Ucapkan \"$shortId\" sesuai dengan situasinya."
            )
            "Number", "Counter" -> Triple(
                "이 표현은 '$hangul'${objParticle} 세거나 나타낼 때 써요.",
                "Use '$hangul' ($shortEn) when counting or expressing quantity.",
                "Gunakan '$hangul' ($shortId) saat menghitung atau menyatakan jumlah."
            )
            else -> { // Noun, Pronoun, Determiner
                if (isAdvanced) {
                    Triple(
                        "현대 사회에서 ${hangul}${topicParticle} 매우 중요한 주제입니다.",
                        "In modern society, $shortEn is a very important topic.",
                        "Dalam masyarakat modern, $shortId merupakan topik yang sangat penting."
                    )
                } else if (subCategory == "3급") {
                    Triple(
                        "이번 수업에서 ${hangul}${objParticle} 자세히 배웠어요.",
                        "I learned about $shortEn in detail in this class.",
                        "Saya mempelajari $shortId secara mendalam di kelas ini."
                    )
                } else {
                    Triple(
                        "여기에 ${hangul}${subjParticle} 있어요.",
                        "There is $shortEn here.",
                        "Di sini ada $shortId."
                    )
                }
            }
        }
    }
}
