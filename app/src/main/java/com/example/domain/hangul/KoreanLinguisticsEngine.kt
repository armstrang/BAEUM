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
     * Revised Romanization of Korean with common phonological assimilation rules
     * (liaison, nasalization, liquidization, palatalization, aspiration).
     */
    fun romanize(text: String): String {
        val words = text.trim().split(Regex("\\s+"))
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
                    // Liaison: single batchim (except ㅇ, ㅎ) before silent ㅇ
                    nCho == 11 && jong in setOf(1, 2, 4, 7, 8, 16, 17, 19, 20, 22, 23, 24, 25, 26) -> {
                        nextOverrideCho = when (jong) {
                            1 -> 0; 2 -> 1; 4 -> 2; 7 -> 3; 8 -> 5; 16 -> 6; 17 -> 7
                            19 -> 9; 20 -> 10; 22 -> 12; 23 -> 14; 24 -> 15; 25 -> 16; 26 -> 17
                            else -> nCho
                        }
                        jong = 0
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
                    // Liquidization: ㄴ+ㄹ or ㄹ+ㄴ -> ll
                    (jong == 4 && nCho == 5) -> {
                        jong = 8
                        nextOverrideCho = 5
                    }
                    (jong == 8 && nCho == 2) -> {
                        nextOverrideCho = 5
                    }
                    // Aspiration: ㄱ/ㄷ/ㅂ/ㅈ + ㅎ -> ㅋ/ㅌ/ㅍ/ㅊ
                    nCho == 18 && jong in setOf(1, 7, 17, 22) -> {
                        nextOverrideCho = when (jong) {
                            1 -> 15; 7 -> 16; 17 -> 17; 22 -> 14 else -> 18
                        }
                        jong = 0
                    }
                }
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
                nCho == 11 && jong != 21 && jong != 27 ->
                    notes.add("Liaison (연음): final consonant carries smoothly into the following vowel.")
                (nCho == 2 || nCho == 6) && jong in setOf(1, 2, 7, 17, 19, 20, 22, 23, 24, 25, 26) ->
                    notes.add("Nasalization (비음화): obstruent batchim assimilates nasally before ㄴ/ㅁ.")
                (jong == 4 && nCho == 5) || (jong == 8 && nCho == 2) ->
                    notes.add("Liquidization (유음화): ㄴ + ㄹ / ㄹ + ㄴ assimilates to flowing [ll].")
                nCho == 18 && jong in setOf(1, 7, 17, 22) ->
                    notes.add("Aspiration (격음화): batchim merges with ㅎ into an aspirated stop.")
                jong in setOf(1, 7, 17) && nCho in setOf(0, 3, 7, 9, 12) ->
                    notes.add("Tensification (경음화): following consonant tenses after stop batchim.")
            }
            if (notes.isNotEmpty()) break
        }
        return notes.firstOrNull().orEmpty()
    }

    /**
     * Conjugates Korean verbs and adjectives ending in 다 into standard polite/formal/past/future forms.
     * Supports regular stems, 하다 predicates, vowel contraction, ㅡ-drop, ㄹ-drop, and marked irregulars (ㅂ, ㄷ, ㅅ, 르, ㅎ).
     */
    fun buildConjugationsSerialized(
        hangul: String,
        pos: String,
        irregularTag: String = ""
    ): String {
        if ((pos != "Verb" && pos != "Adjective") || !hangul.endsWith("다") || hangul.length < 2) {
            return ""
        }
        val stem = hangul.dropLast(1)
        val lastChar = stem.last()
        val lastDecomp = decomposeIndices(lastChar) ?: return ""
        val (cho, jung, jong) = lastDecomp
        val prefix = stem.dropLast(1)

        val politePresent = conjugatePolitePresent(stem, prefix, cho, jung, jong, irregularTag)
        val politePast = conjugatePolitePast(politePresent)
        val formalPresent = conjugateFormalPresent(stem, prefix, cho, jung, jong)
        val politeFuture = conjugatePoliteFuture(stem, prefix, cho, jung, jong, irregularTag)

        val entries = mutableListOf(
            "$politePresent::Polite present (해요체)",
            "$formalPresent::Formal present (합니다체)",
            "$politePast::Polite past",
            "$politeFuture::Polite future"
        )
        if (pos == "Verb") {
            val honorific = conjugateHonorific(stem, prefix, cho, jung, jong, irregularTag)
            entries.add("$honorific::Honorific polite (-(으)세요)")
        }
        return entries.joinToString("||")
    }

    private fun isBrightVowel(jung: Int): Boolean = jung == 0 || jung == 8 || jung == 2 || jung == 12 // ㅏ, ㅗ, ㅑ, ㅛ

    private fun conjugatePolitePresent(
        stem: String,
        prefix: String,
        cho: Int,
        jung: Int,
        jong: Int,
        irregularTag: String
    ): String {
        if (stem.endsWith("하")) return stem.dropLast(1) + "해요"

        // Marked irregulars
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

        // With batchim (regular)
        if (jong != 0) {
            return stem + if (isBrightVowel(jung)) "아요" else "어요"
        }

        // Open vowel stems
        return when (jung) {
            0, 1, 2, 3, 4, 5, 6, 7 -> stem + "요" // 가요, 보내요, 서요, 세요, 켜요
            8 -> prefix + composeIndices(cho, 9, 0) + "요" // 오 -> 와요
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
                "이 표현은 '$hangul'을(를) 세거나 나타낼 때 써요.",
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
