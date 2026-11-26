package com.example.data.seed

import android.content.res.AssetManager
import com.example.data.local.CurriculumPathway
import com.example.data.local.VocabEntity
import com.example.domain.hangul.KoreanLinguisticsEngine
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Loads the enriched TOPIK I (1–2급) and TOPIK II (3–6급) vocabulary banks from
 * pipe-delimited TSV asset files, classifies entries accurately across TOPIK levels 1급–6급,
 * enriches them with linguistic metadata (Revised Romanization, phonological notes,
 * conjugations, and particle-accurate example sentences), and merges/deduplicates
 * them with the curated bespoke entries in [KoreanVocabSeed].
 */
object VocabAssetLoader {

    const val EXPECTED_MIN_VOCAB_COUNT = 5500

    private const val TARGET_LEVEL_1_COUNT = 850
    private const val TARGET_LEVEL_3_COUNT = 1200
    private const val TARGET_LEVEL_4_COUNT = 1200
    private const val TARGET_LEVEL_5_COUNT = 1200

    data class RawTsvRow(
        val hangul: String,
        val meaningEn: String,
        val meaningId: String,
        val posCode: String,
        val hanja: String,
        val irregularTag: String
    )

    fun loadMergedVocabulary(assets: AssetManager): List<VocabEntity> {
        val topik1Stream = runCatching { assets.open("vocab/topik_1.tsv") }.getOrNull()
        val topik2Stream = runCatching { assets.open("vocab/topik_2.tsv") }.getOrNull()
        return loadMergedVocabulary(topik1Stream = topik1Stream, topik2Stream = topik2Stream)
    }

    /**
     * Stream-based entry point enabling fast, deterministic JVM unit testing without Android AssetManager.
     */
    fun loadMergedVocabulary(
        topik1Stream: InputStream?,
        topik2Stream: InputStream?
    ): List<VocabEntity> {
        val curated = KoreanVocabSeed.allEntries()
        val result = ArrayList<VocabEntity>(curated.size + 6800)
        val seenHangul = HashSet<String>(8192)

        // Preserve all bespoke curated entries first (keeps handcrafted notes & examples).
        for (entry in curated) {
            result.add(entry)
            seenHangul.add(entry.hangul.trim())
        }

        val existingLevel1Count = curated.count {
            it.curriculum == CurriculumPathway.TOPIK && it.subCategory == "1급"
        }

        // 1) Parse TOPIK I pool (1급 + 2급)
        val topik1Rows = topik1Stream?.use { parseTsvStream(it) }.orEmpty()
            .filter { seenHangul.add(it.hangul) }

        val rankedTopik1 = topik1Rows.sortedWith(
            compareBy<RawTsvRow> { scoreTopik1Difficulty(it) }
                .thenBy { it.hangul }
        )

        val level1Quota = (TARGET_LEVEL_1_COUNT - existingLevel1Count).coerceAtLeast(0)
        rankedTopik1.forEachIndexed { idx, row ->
            val levelNum = if (idx < level1Quota) 1 else 2
            result.add(buildEntity(row, levelNum))
        }

        // 2) Parse TOPIK II pool (3급 + 4급 + 5급 + 6급)
        val topik2Rows = topik2Stream?.use { parseTsvStream(it) }.orEmpty()
            .filter { seenHangul.add(it.hangul) }

        val rankedTopik2 = topik2Rows.sortedWith(
            compareBy<RawTsvRow> { scoreTopik2Difficulty(it) }
                .thenBy { it.hangul }
        )

        val cutoff3 = TARGET_LEVEL_3_COUNT
        val cutoff4 = cutoff3 + TARGET_LEVEL_4_COUNT
        val cutoff5 = cutoff4 + TARGET_LEVEL_5_COUNT

        rankedTopik2.forEachIndexed { idx, row ->
            val levelNum = when {
                idx < cutoff3 -> 3
                idx < cutoff4 -> 4
                idx < cutoff5 -> 5
                else -> 6
            }
            result.add(buildEntity(row, levelNum))
        }

        return result
    }

    fun parseTsvStream(stream: InputStream): List<RawTsvRow> {
        val rows = ArrayList<RawTsvRow>(2048)
        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.lineSequence().forEach { rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#")) return@forEach
                val parts = line.split('|')
                if (parts.size < 4) return@forEach
                val hangul = parts[0].trim()
                val meaningEn = parts[1].trim()
                val meaningId = parts[2].trim()
                val posCode = parts[3].trim()
                val hanja = parts.getOrNull(4)?.trim().orEmpty()
                val irregularTag = parts.getOrNull(5)?.trim().orEmpty()
                if (hangul.isNotEmpty() && meaningEn.isNotEmpty()) {
                    rows.add(
                        RawTsvRow(
                            hangul = hangul,
                            meaningEn = meaningEn,
                            meaningId = meaningId.ifEmpty { meaningEn },
                            posCode = posCode,
                            hanja = hanja,
                            irregularTag = irregularTag
                        )
                    )
                }
            }
        }
        return rows
    }

    private fun buildEntity(row: RawTsvRow, levelNum: Int): VocabEntity {
        val pos = expandPartOfSpeech(row.posCode)
        val subCategory = "${levelNum}급"
        val romanization = KoreanLinguisticsEngine.romanize(row.hangul)
        val pronNote = KoreanLinguisticsEngine.detectPronunciationNote(row.hangul)
        val conjugations = KoreanLinguisticsEngine.buildConjugationsSerialized(
            hangul = row.hangul,
            pos = pos,
            irregularTag = row.irregularTag
        )
        val (exKo, exEn, exId) = KoreanLinguisticsEngine.buildContextualExample(
            hangul = row.hangul,
            meaningEn = row.meaningEn,
            meaningId = row.meaningId,
            pos = pos,
            subCategory = subCategory,
            irregularTag = row.irregularTag
        )

        val grammarNote = buildGrammarNote(pos, row.irregularTag, levelNum)
        val formalityNote = when (levelNum) {
            1, 2 -> "TOPIK I ($subCategory) core vocabulary — commonly used in polite 해요체 everyday speech."
            3, 4 -> "TOPIK II ($subCategory) intermediate vocabulary — used in both polite speech and news/essays."
            else -> "TOPIK II ($subCategory) advanced vocabulary — frequent in formal discourse, reports, and academic texts."
        }

        return VocabEntity(
            id = "tp${levelNum}_${fnv1a64Hex(row.hangul)}",
            hangul = row.hangul,
            romanization = romanization,
            meaningEn = row.meaningEn,
            meaningId = row.meaningId,
            partOfSpeech = pos,
            curriculum = CurriculumPathway.TOPIK,
            subCategory = subCategory,
            exampleKo = exKo,
            exampleEn = exEn,
            exampleId = exId,
            pronunciationNote = pronNote,
            grammarNote = grammarNote,
            conjugationsSerialized = conjugations,
            formalityNote = formalityNote,
            hanja = row.hanja
        )
    }

    private fun expandPartOfSpeech(code: String): String = when (code) {
        "N" -> "Noun"
        "V" -> "Verb"
        "A" -> "Adjective"
        "Adv" -> "Adverb"
        "Pron" -> "Pronoun"
        "Det" -> "Determiner"
        "Expr" -> "Expression"
        "Num" -> "Number"
        "Part" -> "Particle"
        "Counter" -> "Counter"
        else -> code.ifBlank { "Noun" }
    }

    private fun buildGrammarNote(pos: String, irregularTag: String, levelNum: Int): String {
        val irregText = when (irregularTag) {
            "ㅂ" -> "ㅂ irregular (ㅂ 불규칙): final ㅂ changes to 우 (or 오 for 돕다/곱다) before vowel endings."
            "ㄷ" -> "ㄷ irregular (ㄷ 불규칙): final ㄷ changes to ㄹ before vowel endings."
            "ㅅ" -> "ㅅ irregular (ㅅ 불규칙): final ㅅ drops before vowel endings without contracting."
            "르" -> "르 irregular (르 불규칙): ㅡ drops and an extra ㄹ attaches to the preceding syllable before -아/어."
            "ㅎ" -> "ㅎ irregular (ㅎ 불규칙): final ㅎ drops and the vowel merges into ㅐ/ㅒ before -아/어."
            else -> ""
        }
        if (irregText.isNotEmpty()) return irregText
        return when (pos) {
            "Verb" -> "Standard Korean verb (${levelNum}급). Combines with tense, aspect, and connective endings."
            "Adjective" -> "Descriptive verb / adjective (${levelNum}급). Conjugates directly as a predicate or with -(으)ㄴ before nouns."
            "Adverb" -> "Modifies verbs, adjectives, or clauses (${levelNum}급)."
            "Number" -> "Korean numeral (${levelNum}급). Pair carefully with native or Sino-Korean counters."
            else -> ""
        }
    }

    /**
     * Scores TOPIK I words so essential Level 1 (1급) survival vocabulary ranks first
     * and upper-beginner Level 2 (2급) vocabulary follows naturally.
     */
    private fun scoreTopik1Difficulty(row: RawTsvRow): Int {
        if (row.hangul in CORE_TOPIK_1_ANCHORS) return 0
        var score = 20

        // Foundational closed classes belong overwhelmingly to 1급
        when (row.posCode) {
            "Pron", "Num", "Det", "Part", "Expr" -> score -= 14
            "Adv" -> score -= 5
            "A" -> score -= 3
            "V" -> score -= 2
        }

        // Shorter words are generally learned earlier
        val len = row.hangul.length
        score += when {
            len == 1 -> -6
            len == 2 -> -2
            len == 3 -> 4
            else -> 10
        }

        // Native Korean (no Hanja) beginner words skew strongly toward 1급
        if (row.hanja.isEmpty()) {
            score -= 5
        } else {
            // Very common beginner Hanja concepts stay in 1급
            if (row.hanja.any { it in BEGINNER_HANJA_CHARS }) {
                score -= 3
            } else {
                score += 6
            }
        }

        // Loanwords of everyday life (버스, 택시, 커피, 라면, etc.) are beginner staples
        if (row.hanja.isEmpty() && row.posCode == "N" && len <= 3) {
            score -= 2
        }

        // Words ending in 요일 (days of week) or 월 (months) belong in 1급
        if (row.hangul.endsWith("요일") || (row.hangul.endsWith("월") && len <= 3)) {
            return 1
        }

        return score
    }

    /**
     * Scores TOPIK II words across 3급 (lower-intermediate) -> 4급 (upper-intermediate)
     * -> 5급 (advanced) -> 6급 (mastery / literary / formal academic).
     */
    private fun scoreTopik2Difficulty(row: RawTsvRow): Int {
        var score = 50
        val len = row.hangul.length
        val hasHanja = row.hanja.isNotEmpty()

        // Native everyday verbs/adjectives/nouns appear earlier in TOPIK II (3급–4급)
        if (!hasHanja) {
            score -= 12
            if (len <= 3) score -= 5
        } else {
            score += 8
            if (row.hanja.length >= 3) score += 12
            if (row.hanja.length >= 4) score += 12
        }

        // Compound predicates with ~하다/~되다/~지다
        when {
            row.hangul.endsWith("지다") || row.hangul.endsWith("가다") || row.hangul.endsWith("오다") -> score -= 8
            row.hangul.endsWith("하다") && hasHanja -> score += 4
            row.hangul.endsWith("되다") -> score += 6
            row.hangul.endsWith("스럽다") || row.hangul.endsWith("롭다") -> score += 9
        }

        // Abstract / institutional / academic suffixes skew toward 5급–6급
        if (hasHanja && row.hangul.length >= 2) {
            val lastChar = row.hangul.last()
            if (lastChar in ADVANCED_SINO_SUFFIXES) {
                score += 11
            }
            if (lastChar in INTERMEDIATE_SINO_SUFFIXES) {
                score -= 4
            }
        }

        // Formal adverbs (e.g. 흔쾌히, 극히, 점차) often test at 5급–6급
        if (row.posCode == "Adv" && hasHanja) {
            score += 10
        }

        // Longer compounds & specialized English gloss cues
        if (len >= 4) score += 7
        val enLower = row.meaningEn.lowercase()
        if (enLower.contains("policy") || enLower.contains("ideology") || enLower.contains("treaty") ||
            enLower.contains("sovereignty") || enLower.contains("legislation") || enLower.contains("discourse") ||
            enLower.contains("diploma") || enLower.contains("armistice") || enLower.contains("paradigm") ||
            enLower.contains("doctrine") || enLower.contains("literary") || enLower.contains("ecstasy")
        ) {
            score += 18
        }

        return score
    }

    private fun fnv1a64Hex(input: String): String {
        var hash = -3750763034362895579L // 0xcbf29ce484222325
        val prime = 1099511628211L
        for (ch in input) {
            hash = hash xor ch.code.toLong()
            hash *= prime
        }
        return java.lang.Long.toUnsignedString(hash, 16)
    }

    private val BEGINNER_HANJA_CHARS = setOf(
        '學', '生', '校', '時', '間', '家', '族', '父', '母', '兄', '弟', '妹',
        '食', '堂', '茶', '水', '火', '木', '金', '土', '日', '月', '年', '週',
        '病', '院', '藥', '銀', '行', '電', '話', '車', '驛', '門', '室', '館',
        '韓', '國', '語', '英', '文', '字', '冊', '書', '音', '樂', '映', '畫',
        '運', '動', '旅', '山', '江', '海', '天', '氣', '色', '數', '名', '人'
    )

    private val INTERMEDIATE_SINO_SUFFIXES = setOf(
        '기', '장', '실', '점', '관', '소', '표', '비', '료', '품', '감', '법', '력'
    )

    private val ADVANCED_SINO_SUFFIXES = setOf(
        '론', '권', '율', '성', '화', '의', '체', '계', '학', '증', '제', '조', '책', '령', '헌', '극', '열'
    )

    private val CORE_TOPIK_1_ANCHORS = setOf(
        // Greetings & essentials
        "네", "예", "아니요", "안녕하세요", "감사합니다", "감사하다", "고맙다", "미안하다", "죄송하다",
        "괜찮다", "여보세요", "어서", "제발", "부탁하다",
        // Pronouns, demonstratives & question words
        "나", "저", "너", "우리", "저희", "당신", "그", "그녀", "이것", "그것", "저것",
        "여기", "거기", "저기", "어디", "누구", "무엇", "뭐", "언제", "왜", "어떻게", "얼마", "얼마나", "몇", "어느", "무슨",
        // Numbers & time
        "하나", "둘", "셋", "넷", "다섯", "여섯", "일곱", "여덟", "아홉", "열", "스물", "서른", "마흔", "쉰",
        "일", "이", "삼", "사", "오", "육", "칠", "팔", "구", "십", "백", "천", "만",
        "오늘", "어제", "내일", "모레", "지금", "아침", "점심", "저녁", "밤", "낮", "오전", "오후",
        "시간", "분", "초", "시", "날", "달", "해", "년", "주", "주말", "평일", "방학", "휴일", "생일",
        "봄", "여름", "가을", "겨울", "계절", "날씨",
        // Family & people
        "사람", "남자", "여자", "아이", "아기", "어른", "가족", "부모", "부모님", "아버지", "어머니",
        "아빠", "엄마", "형", "오빠", "누나", "언니", "동생", "남동생", "여동생", "할아버지", "할머니",
        "남편", "아내", "아들", "딸", "친구", "선생님", "학생", "대학생", "의사", "간호사", "경찰", "요리사", "가수", "회사원", "손님",
        // Places & transport
        "집", "방", "부엌", "거실", "화장실", "학교", "교실", "도서관", "병원", "약국", "은행", "우체국",
        "가게", "시장", "마트", "슈퍼마켓", "백화점", "식당", "카페", "빵집", "서점", "영화관", "극장", "공원",
        "공항", "역", "지하철", "버스", "택시", "기차", "비행기", "자전거", "자동차", "차", "배", "길", "거리", "회사", "사무실", "호텔",
        // Food & drink
        "음식", "밥", "국", "반찬", "김치", "불고기", "비빔밥", "라면", "국수", "빵", "고기", "소고기", "돼지고기", "닭고기",
        "생선", "계란", "달걀", "채소", "야채", "과일", "사과", "바나나", "포도", "딸기", "수박", "귤", "오렌지",
        "물", "차", "커피", "우유", "주스", "맥주", "소주", "술", "소금", "설탕", "간장", "아이스크림", "케이크", "과자",
        // Everyday objects & body & clothes
        "책", "공책", "연필", "볼펜", "지우개", "가방", "지갑", "돈", "카드", "열쇠", "시계", "안경", "우산",
        "전화", "휴대폰", "컴퓨터", "텔레비전", "카메라", "사진", "편지", "선물", "꽃", "나무", "산", "바다", "강", "하늘", "비", "눈", "바람", "구름",
        "옷", "바지", "치마", "셔츠", "티셔츠", "코트", "신발", "운동화", "구두", "양말", "모자", "장갑",
        "몸", "머리", "얼굴", "눈", "코", "입", "귀", "목", "어깨", "팔", "손", "손가락", "배", "다리", "발", "이", "마음",
        // Colors
        "색", "색깔", "빨간색", "파란색", "노란색", "초록색", "검은색", "까만색", "하얀색", "흰색", "갈색", "회색", "보라색", "분홍색",
        // Core verbs
        "가다", "오다", "다니다", "들어가다", "나오다", "올라가다", "내려가다", "앉다", "서다", "걷다", "뛰다", "타다", "내리다",
        "먹다", "마시다", "자다", "일어나다", "씻다", "입다", "신다", "쓰다", "벗다", "보다", "듣다", "말하다", "이야기하다",
        "읽다", "배우다", "가르치다", "공부하다", "일하다", "쉬다", "놀다", "운동하다", "수영하다", "요리하다", "청소하다", "빨래하다",
        "사다", "팔다", "주다", "받다", "빌리다", "보내다", "만나다", "기다리다", "찾다", "알다", "모르다", "생각하다",
        "좋아하다", "싫어하다", "사랑하다", "살다", "죽다", "열다", "닫다", "켜다", "끄다", "만들다", "찍다", "그리다", "부르다", "춤추다",
        "웃다", "울다", "돕다", "전화하다", "질문하다", "대답하다", "시작하다", "끝나다", "준비하다", "약속하다", "인사하다", "소개하다", "주문하다",
        // Core adjectives
        "있다", "없다", "크다", "작다", "많다", "적다", "길다", "짧다", "높다", "낮다", "넓다", "좁다",
        "좋다", "나쁘다", "예쁘다", "아름답다", "귀엽다", "멋있다", "맛있다", "맛없다", "재미있다", "재미없다",
        "쉽다", "어렵다", "싸다", "비싸다", "가깝다", "멀다", "빠르다", "느리다", "무겁다", "가볍다",
        "덥다", "춥다", "따뜻하다", "시원하다", "뜨겁다", "차갑다", "맑다", "흐리다", "밝다", "어둡다",
        "새롭다", "오래되다", "깨끗하다", "더럽다", "조용하다", "시끄럽다", "바쁘다", "한가하다", "피곤하다", "아프다", "건강하다",
        "기쁘다", "슬프다", "즐겁다", "행복하다", "화나다", "무섭다", "같다", "다르다", "친절하다", "착하다", "유명하다", "편하다", "불편하다", "필요하다", "중요하다",
        // Core adverbs
        "아주", "매우", "너무", "정말", "진짜", "참", "가장", "제일", "더", "덜", "많이", "조금", "잘", "못", "안",
        "항상", "언제나", "자주", "보통", "가끔", "별로", "전혀", "다", "모두", "같이", "함께", "혼자",
        "빨리", "천천히", "일찍", "늦게", "먼저", "나중에", "벌써", "이미", "아직", "또", "다시", "바로", "곧", "갑자기", "꼭", "아마", "그리고", "그러나", "하지만", "그래서", "그런데"
    )
}
