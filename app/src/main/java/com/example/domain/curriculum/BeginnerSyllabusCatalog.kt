package com.example.domain.curriculum

/**
 * Distinct progressive guided syllabus tracks inside BAEUM:
 * 1. BEGINNER ("Pemula dari Nol → Menyusun Kalimat", 12 lessons)
 * 2. TOPIK_I ("TOPIK I · 1–2급", 8 lessons)
 * 3. TOPIK_II ("TOPIK II · 3–6급", 8 lessons)
 */
enum class SyllabusTrack(
    val badgeKo: String,
    val shortLabelEn: String,
    val shortLabelId: String,
    val titleEn: String,
    val titleId: String,
    val subtitleEn: String,
    val subtitleId: String,
    val accentTag: String,
    val graduationBonusXp: Int
) {
    BEGINNER(
        badgeKo = "입문",
        shortLabelEn = "Zero → Sentence",
        shortLabelId = "Pemula Nol",
        titleEn = "Beginner · Zero to Sentence",
        titleId = "Pemula · Nol → Susun Kalimat",
        subtitleEn = "Start from zero: Hangul → Batchim → Sentence Building → Everyday Seoul",
        subtitleId = "Mulai dari nol: Hangul → Batchim → Menyusun Kalimat → Percakapan Seoul",
        accentTag = "mint",
        graduationBonusXp = 150
    ),
    TOPIK_I(
        badgeKo = "초급",
        shortLabelEn = "TOPIK I (1–2급)",
        shortLabelId = "TOPIK I (1–2급)",
        titleEn = "TOPIK I · 1–2급 Mastery",
        titleId = "Silabus TOPIK I · 1–2급",
        subtitleEn = "Separate TOPIK I syllabus: Tenses, negation, connectors, requests & exam strategy",
        subtitleId = "Silabus khusus TOPIK I: Lampau/depan, negasi, penghubung, arah & strategi ujian",
        accentTag = "primary",
        graduationBonusXp = 200
    ),
    TOPIK_II(
        badgeKo = "중고급",
        shortLabelEn = "TOPIK II (3–6급)",
        shortLabelId = "TOPIK II (3–6급)",
        titleEn = "TOPIK II · 3–6급 Fluency",
        titleId = "Silabus TOPIK II · 3–6급",
        subtitleEn = "Separate TOPIK II syllabus: Nuanced logic, indirect quotes, Haerache & Writing #51–54",
        subtitleId = "Silabus khusus TOPIK II: Logika formal, kutipan tak langsung, Haerache & Esai #51–54",
        accentTag = "gold",
        graduationBonusXp = 300
    );

    companion object {
        fun fromKey(raw: String?): SyllabusTrack? =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
    }
}

/**
 * Progressive romanization policy ("Auto-Fade Romanization") to prevent Latin-script dependency:
 * - FULL: Stage 1–2 (Hangul decoding, Lessons 1–6)
 * - TAP_TO_PEEK: Stage 3–4 (Sentence Building) & TOPIK I (1–2급)
 * - EXAM_HIDDEN: TOPIK II (3–6급) — mirrors authentic exam conditions with optional tap-to-peek
 */
enum class RomanizationVisibility {
    FULL,
    TAP_TO_PEEK,
    EXAM_HIDDEN
}

/**
 * Stage grouping inside a Guided Syllabus Track, crowned by a timed Stage Boss Challenge.
 */
data class BeginnerStage(
    val number: Int,
    val track: SyllabusTrack = SyllabusTrack.BEGINNER,
    val titleKo: String,
    val titleEn: String,
    val titleId: String,
    val subtitleEn: String,
    val subtitleId: String,
    val accentTag: String, // "primary" | "mint" | "gold" | "cherry"
    val bossSealKo: String = "승",
    val bossTimeSeconds: Int = 75
)

/**
 * Bite-sized interactive Hangul/phrase/grammar study card inside a Guided Lesson.
 */
data class BeginnerStudyCard(
    val hangul: String,
    val romanization: String,
    val meaningEn: String,
    val meaningId: String,
    val tipEn: String,
    val tipId: String,
    val audioTextKo: String = hangul
)

/**
 * Interactive Tap-to-Assemble Word / Sentence Builder challenge included in every guided lesson.
 * Supports both translation-to-Korean and Listening-First (Audio-to-Sentence) modes.
 */
data class SentenceBuildExercise(
    val promptEn: String,
    val promptId: String,
    val targetTokens: List<String>,
    val distractorTokens: List<String> = emptyList(),
    val fullSentenceKo: String,
    val romanization: String,
    val hintEn: String,
    val hintId: String,
    val isAudioFirst: Boolean = false
)

/**
 * Built-in 3-question guided mini-check at the end of every Guided Lesson.
 */
data class BeginnerMiniCheck(
    val promptEn: String,
    val promptId: String,
    val stimulusKo: String,
    val options: List<String>,
    val correctOption: String,
    val explanationEn: String,
    val explanationId: String,
    val speakOnTapKo: String = stimulusKo,
    val isAudioPrompt: Boolean = false
)

/**
 * Suggested hands-on next studio action after finishing a Guided Lesson.
 */
enum class BeginnerPracticeAction {
    HANGUL_WRITING,
    HANGUL_STUDIO,
    BEGINNER_FLASHCARDS,
    GRAMMAR_STUDIO,
    EVERYDAY_SPEAKING,
    TOPIK_I_EXAM,
    TOPIK_II_FLASHCARDS,
    TOPIK_II_EXAM
}

/**
 * Complete guided lesson with study cards, interactive sentence-building exercises, and mini-checks.
 */
data class BeginnerLesson(
    val id: String,
    val number: Int, // 1-based lesson number within its track
    val stageNumber: Int,
    val track: SyllabusTrack = SyllabusTrack.BEGINNER,
    val estimatedMinutes: Int,
    val emblemKo: String,
    val titleEn: String,
    val titleId: String,
    val goalEn: String,
    val goalId: String,
    val overviewEn: String,
    val overviewId: String,
    val keyRuleEn: String,
    val keyRuleId: String,
    val studyCards: List<BeginnerStudyCard>,
    val sentenceExercises: List<SentenceBuildExercise>,
    val miniChecks: List<BeginnerMiniCheck>,
    val practiceAction: BeginnerPracticeAction,
    val practiceLabelEn: String,
    val practiceLabelId: String,
    val xpReward: Int = 60
)

/**
 * Parsed entry stored inside the Mistake Redemption Vault ("Balas Dendam Kesalahan").
 */
sealed class SyllabusMistakeItem {
    abstract val key: String
    abstract val lesson: BeginnerLesson

    data class SentenceBuildMistake(
        override val key: String,
        override val lesson: BeginnerLesson,
        val exerciseIndex: Int,
        val exercise: SentenceBuildExercise
    ) : SyllabusMistakeItem()

    data class MiniCheckMistake(
        override val key: String,
        override val lesson: BeginnerLesson,
        val checkIndex: Int,
        val check: BeginnerMiniCheck
    ) : SyllabusMistakeItem()
}

/**
 * Structured 3-Track Progressive Korean Curriculum (30 Lessons · 60 Sentence Builders · 8 Stage Bosses):
 * - Track 1 (BEGINNER, 12 lessons): Zero-background Hangul → Interactive Sentence Building → Everyday Seoul
 * - Track 2 (TOPIK_I, 10 lessons): Dedicated TOPIK I (1–2급) grammar, irregulars, modifiers & exam skills
 * - Track 3 (TOPIK_II, 8 lessons): Dedicated TOPIK II (3–6급) discourse, Haerache & Writing #51–54 mastery
 */
object BeginnerSyllabusCatalog {

    const val LESSON_XP_REWARD = 60
    const val TOPIK_I_LESSON_XP = 75
    const val TOPIK_II_LESSON_XP = 90
    const val PERFECT_STAR_BONUS_XP = 25
    const val GRADUATION_BONUS_XP = 150
    const val STAGE_BOSS_XP = 120
    const val REDEMPTION_BONUS_XP = 20

    val allStages: List<BeginnerStage> = listOf(
        // ==================== TRACK 1: BEGINNER (ZERO -> SENTENCE) ====================
        BeginnerStage(
            number = 1,
            track = SyllabusTrack.BEGINNER,
            titleKo = "한글 첫걸음",
            titleEn = "Stage 1 · Reading Your First Hangul",
            titleId = "Tahap 1 · Membaca Hangul Pertama",
            subtitleEn = "Master basic vowels, core consonants & syllable blocks in minutes.",
            subtitleId = "Kuasai vokal dasar, konsonan inti & cara menyusun blok suku kata dalam hitungan menit.",
            accentTag = "primary",
            bossSealKo = "훈",
            bossTimeSeconds = 60
        ),
        BeginnerStage(
            number = 2,
            track = SyllabusTrack.BEGINNER,
            titleKo = "받침과 발음",
            titleEn = "Stage 2 · Natural Reading & Batchim",
            titleId = "Tahap 2 · Bacaan Alami & Batchim",
            subtitleEn = "Unlock Y-vowels, final consonants (batchim), tense letters & smooth linking.",
            subtitleId = "Pahami vokal-Y, konsonan akhir (batchim), huruf ganda & rahasia sambung bunyi.",
            accentTag = "mint",
            bossSealKo = "울",
            bossTimeSeconds = 65
        ),
        BeginnerStage(
            number = 3,
            track = SyllabusTrack.BEGINNER,
            titleKo = "문장 만들기",
            titleEn = "Stage 3 · Greetings & Building First Sentences",
            titleId = "Tahap 3 · Sapaan & Menyusun Kalimat Pertama",
            subtitleEn = "Speak politely, introduce yourself & assemble real Korean S-O-V sentences.",
            subtitleId = "Menyapa sopan, memperkenalkan diri & merangkai kalimat Korea (S-O-P) dengan partikel inti.",
            accentTag = "gold",
            bossSealKo = "문",
            bossTimeSeconds = 75
        ),
        BeginnerStage(
            number = 4,
            track = SyllabusTrack.BEGINNER,
            titleKo = "서울 실전",
            titleEn = "Stage 4 · Everyday Seoul Sentence Survival",
            titleId = "Tahap 4 · Menyusun Kalimat Harian di Seoul",
            subtitleEn = "Numbers, cafe orders & conjugating everyday Korean verbs into complete sentences.",
            subtitleId = "Angka Korea, memesan di kafe & mengubah kata kerja harian menjadi kalimat lengkap.",
            accentTag = "cherry",
            bossSealKo = "별",
            bossTimeSeconds = 75
        ),

        // ==================== TRACK 2: TOPIK I (1-2급) ====================
        BeginnerStage(
            number = 5,
            track = SyllabusTrack.TOPIK_I,
            titleKo = "토픽 1 핵심 문법",
            titleEn = "Stage 1 · TOPIK I Tenses, Negation & Connectors",
            titleId = "Tahap 1 · Kala, Negasi & Penghubung TOPIK I",
            subtitleEn = "Talk about past & future, express ability/desire & connect two clauses smoothly.",
            subtitleId = "Bercerita masa lalu & rencana, menyatakan bisa/ingin serta menggabungkan 2 kalimat.",
            accentTag = "primary",
            bossSealKo = "결",
            bossTimeSeconds = 80
        ),
        BeginnerStage(
            number = 6,
            track = SyllabusTrack.TOPIK_I,
            titleKo = "토픽 1 실전·도약",
            titleEn = "Stage 2 · Daily Scenarios, Irregulars, Modifiers & Strategy",
            titleId = "Tahap 2 · Situasi Harian, Irregular, Modifier & Strategi TOPIK I",
            subtitleEn = "Schedules, directions, irregular verbs (ㅂ/ㄷ/르), noun modifiers & TOPIK I strategy.",
            subtitleId = "Jadwal, arah, kata kerja tak beraturan (ㅂ/ㄷ/르), modifier benda & strategi ujian TOPIK I.",
            accentTag = "mint",
            bossSealKo = "길",
            bossTimeSeconds = 85
        ),

        // ==================== TRACK 3: TOPIK II (3-6급) ====================
        BeginnerStage(
            number = 7,
            track = SyllabusTrack.TOPIK_II,
            titleKo = "중급 논리와 화법",
            titleEn = "Stage 1 · Intermediate Nuance & Indirect Speech",
            titleId = "Tahap 1 · Nuansa Menengah & Kalimat Tidak Langsung",
            subtitleEn = "Formal reasons, life experiences, quoting others & contrast/concession structures.",
            subtitleId = "Sebab-akibat formal, pengalaman, kutipan tidak langsung & struktur kontras/konsesi.",
            accentTag = "gold",
            bossSealKo = "논",
            bossTimeSeconds = 90
        ),
        BeginnerStage(
            number = 8,
            track = SyllabusTrack.TOPIK_II,
            titleKo = "토픽 2 고급·쓰기",
            titleEn = "Stage 2 · Academic Haerache & Writing #51–54",
            titleId = "Tahap 2 · Ragam Tulis Haerache & Menulis #51–54",
            subtitleEn = "Master formal written ~는다/ㄴ다 style, notices (#51–52), graph trends (#53) & essays (#54).",
            subtitleId = "Kuasai gaya tulis formal ~는다/ㄴ다, soal #51–52, analisis grafik #53 & esai #54.",
            accentTag = "cherry",
            bossSealKo = "필",
            bossTimeSeconds = 90
        )
    )

    val allLessons: List<BeginnerLesson> = listOf(
        // =========================================================================
        // TRACK 1: BEGINNER (ZERO -> SENTENCE) — 12 LESSONS
        // =========================================================================

        // ==================== STAGE 1: FIRST HANGUL ====================
        BeginnerLesson(
            id = "beg_01_basic_vowels",
            number = 1,
            stageNumber = 1,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 3,
            emblemKo = "아",
            titleEn = "6 Basic Vowels & Silent ㅇ",
            titleId = "6 Vokal Dasar & Huruf Kosong ㅇ",
            goalEn = "Read and pronounce 아, 어, 오, 우, 으, 이 cleanly.",
            goalId = "Membaca dan melafalkan 아, 어, 오, 우, 으, 이 dengan lancar.",
            overviewEn = "Korean vowels are drawn from 3 simple shapes: a standing person (ㅣ), flat earth (ㅡ), and the sun dot (•). Because a Korean syllable never starts with a bare vowel, we place the silent circle ㅇ in front.",
            overviewId = "Vokal Hangul dibentuk dari 3 bentuk sederhana: manusia berdiri (ㅣ), bumi datar (ㅡ), dan titik matahari (•). Karena suku kata Korea tidak boleh berdiri sebagai vokal saja, kita pasang lingkaran senyap ㅇ di depan/atasnya.",
            keyRuleEn = "Rule: ㅇ before a vowel is SILENT! Just read the vowel sound: 아 = a, 이 = i, 오 = o.",
            keyRuleId = "Aturan Emas: ㅇ di awal suku kata TIDAK BERBUNYI! Cukup baca vokalnya: 아 = a, 이 = i, 오 = o.",
            studyCards = listOf(
                BeginnerStudyCard("아", "a", "Sound 'a' (as in father)", "Bunyi 'a' (seperti apa)", "Line points RIGHT → mouth opens wide: 'a'", "Garis menunjuk KANAN → mulut terbuka lebar: 'a'"),
                BeginnerStudyCard("어", "eo", "Sound 'eo' (open 'o' / 'uh')", "Bunyi 'eo' (antara o dan e, mulut rileks)", "Line points LEFT (inward) → relaxed 'eo'", "Garis menunjuk KIRI (ke dalam) → 'eo' lembut"),
                BeginnerStudyCard("오", "o", "Sound 'o' (round lips)", "Bunyi 'o' (bibir membulat)", "Line rises UP like the sun above earth: 'o'", "Garis naik KE ATAS seperti matahari terbit: 'o'"),
                BeginnerStudyCard("우", "u", "Sound 'u' (as in moon)", "Bunyi 'u' (seperti buku)", "Line points DOWN like falling rain: 'u'", "Garis turun KE BAWAH seperti air turun: 'u'"),
                BeginnerStudyCard("으", "eu", "Sound 'eu' (smile flat)", "Bunyi 'eu' (seperti 'e' di 'keras', bibir ditarik datar)", "Flat horizontal line → pull lips wide flat: 'eu'", "Garis datar → tarik bibir mendatar sambil ucap 'eu'"),
                BeginnerStudyCard("이", "i", "Sound 'i' (as in tree)", "Bunyi 'i' (seperti ibu)", "Straight vertical line like a standing 'I': 'i'", "Garis tegak lurus seperti huruf 'I': 'i'"),
                BeginnerStudyCard("아이", "a-i", "Child (your 1st Korean word!)", "Anak kecil (kata Korea pertamamu!)", "Combine 아 (a) + 이 (i) = 아이!", "Gabungkan 아 (a) + 이 (i) = 아이!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Assemble your first Korean word: 'Child' (a-i)",
                    promptId = "Susun kata Korea pertamamu: 'Anak kecil' (a-i)",
                    targetTokens = listOf("아", "이"),
                    distractorTokens = listOf("우"),
                    fullSentenceKo = "아이",
                    romanization = "a-i",
                    hintEn = "Start with 아 (a), then add 이 (i)!",
                    hintId = "Mulai dengan 아 (a), lalu tambahkan 이 (i)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Assemble the Korean word for 'Cucumber' (o-i)",
                    promptId = "Susun kata Korea untuk 'Timun' (o-i)",
                    targetTokens = listOf("오", "이"),
                    distractorTokens = listOf("어"),
                    fullSentenceKo = "오이",
                    romanization = "o-i",
                    hintEn = "ㅗ points UP (o) → 오, then 이 (i) → 오이!",
                    hintId = "ㅗ menunjuk ke atas (o) → 오, lalu 이 (i) → 오이!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you read this vowel syllable?",
                    promptId = "Bagaimana cara membaca suku kata ini?",
                    stimulusKo = "오",
                    options = listOf("o", "u", "a", "i"),
                    correctOption = "o",
                    explanationEn = "ㅗ points UP like the rising sun, so 오 is read 'o'.",
                    explanationId = "ㅗ menunjuk ke atas seperti matahari terbit, jadi 오 dibaca 'o'."
                ),
                BeginnerMiniCheck(
                    promptEn = "Which Hangul makes the 'u' sound (line pointing down)?",
                    promptId = "Hangul mana yang berbunyi 'u' (garis menunjuk ke bawah)?",
                    stimulusKo = "우",
                    options = listOf("아", "우", "으", "이"),
                    correctOption = "우",
                    explanationEn = "ㅜ points downward like water, paired with silent ㅇ → 우 (u).",
                    explanationId = "ㅜ mengarah ke bawah, dipasangkan dengan ㅇ senyap → 우 (u)."
                ),
                BeginnerMiniCheck(
                    promptEn = "You can already read a real word! What does 아이 sound like?",
                    promptId = "Kamu sudah bisa baca kata asli! Bagaimana bunyi 아이 (anak kecil)?",
                    stimulusKo = "아이",
                    options = listOf("a-i", "o-i", "eo-u", "u-i"),
                    correctOption = "a-i",
                    explanationEn = "아 (a) + 이 (i) = a-i (child)! You just read your first Korean word.",
                    explanationId = "아 (a) + 이 (i) = a-i (anak kecil)! Kamu baru saja membaca kata Korea pertamamu."
                )
            ),
            practiceAction = BeginnerPracticeAction.HANGUL_WRITING,
            practiceLabelEn = "Trace vowels on Writing Canvas",
            practiceLabelId = "Latih goresan di Kanvas Hangul"
        ),

        BeginnerLesson(
            id = "beg_02_core_consonants",
            number = 2,
            stageNumber = 1,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "가",
            titleEn = "7 Golden Consonants (ㄱ ㄴ ㄷ ㄹ ㅁ ㅂ ㅅ)",
            titleId = "7 Konsonan Emas Pertama (ㄱ ㄴ ㄷ ㄹ ㅁ ㅂ ㅅ)",
            goalEn = "Recognize the 7 most common consonants using visual shape tricks.",
            goalId = "Mengenal 7 konsonan paling sering dipakai dengan trik bentuk visual.",
            overviewEn = "Hangul consonants were designed to mirror the shape of your mouth and tongue! Pair any consonant with ㅏ (a) and you can immediately read real Korean words like 바다 (sea) and 나무 (tree).",
            overviewId = "Konsonan Hangul dirancang menyerupai bentuk mulut dan lidah! Pasangkan dengan vokal ㅏ (a) dan kamu langsung bisa membaca kata Korea nyata seperti 바다 (laut) dan 나무 (pohon).",
            keyRuleEn = "Memory Shortcut: ㄱ(g/k gun), ㄴ(n nose), ㄷ(d door), ㄹ(r/l rattlesnake), ㅁ(m mouth), ㅂ(b bucket), ㅅ(s summit).",
            keyRuleId = "Trik Ingat Cepat: ㄱ(g/k sudut), ㄴ(n hidung/nose), ㄷ(d pintu/door), ㄹ(r/l ular), ㅁ(m mulut kotak), ㅂ(b ember/bucket), ㅅ(s gunung/summit).",
            studyCards = listOf(
                BeginnerStudyCard("가", "ga / ka", "ㄱ (g/k) + ㅏ (a)", "ㄱ (g/k) + ㅏ (a)", "ㄱ looks like a bent knee/angle → 'g/k'", "ㄱ seperti sudut siku → bunyi 'g/k' lembut"),
                BeginnerStudyCard("나", "na", "ㄴ (n) + ㅏ (a) — means 'I'", "ㄴ (n) + ㅏ (a) — berarti 'Aku'", "ㄴ curves like a Nose → 'n'", "ㄴ melengkung seperti hidung (Nose) → 'n'"),
                BeginnerStudyCard("다", "da / ta", "ㄷ (d/t) + ㅏ (a)", "ㄷ (d/t) + ㅏ (a)", "ㄷ looks like a Door frame → 'd'", "ㄷ seperti kusen pintu (Door) → 'd/t' lembut"),
                BeginnerStudyCard("라", "ra / la", "ㄹ (r/l) + ㅏ (a)", "ㄹ (r/l) + ㅏ (a)", "ㄹ zig-zags like a Rattlesnake → 'r/l'", "ㄹ berkelok seperti jalan/ular → 'r/l'"),
                BeginnerStudyCard("마", "ma", "ㅁ (m) + ㅏ (a)", "ㅁ (m) + ㅏ (a)", "ㅁ is a square Mouth → 'm'", "ㅁ berbentuk kotak seperti Mulut → 'm'"),
                BeginnerStudyCard("바다", "ba-da", "Sea (ㅂ+ㅏ + ㄷ+ㅏ)", "Laut (ㅂ+ㅏ + ㄷ+ㅏ)", "ㅂ looks like a Bucket → ba + da = sea!", "ㅂ seperti ember (Bucket) → ba-da = laut!"),
                BeginnerStudyCard("나무", "na-mu", "Tree (ㄴ+ㅏ + ㅁ+ㅜ)", "Pohon (ㄴ+ㅏ + ㅁ+ㅜ)", "나 (na) + 무 (mu) = 나무 (tree)!", "나 (na) + 무 (mu) = 나무 (pohon)!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the Korean word for 'Sea' (ba-da)",
                    promptId = "Susun kata Korea untuk 'Laut' (ba-da)",
                    targetTokens = listOf("바", "다"),
                    distractorTokens = listOf("마"),
                    fullSentenceKo = "바다",
                    romanization = "ba-da",
                    hintEn = "Bucket ㅂ+ㅏ = 바, Door ㄷ+ㅏ = 다!",
                    hintId = "Ember ㅂ+ㅏ = 바, Pintu ㄷ+ㅏ = 다!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the Korean word for 'Tree' (na-mu)",
                    promptId = "Susun kata Korea untuk 'Pohon' (na-mu)",
                    targetTokens = listOf("나", "무"),
                    distractorTokens = listOf("가"),
                    fullSentenceKo = "나무",
                    romanization = "na-mu",
                    hintEn = "Nose ㄴ+ㅏ = 나, Mouth ㅁ+ㅜ = 무!",
                    hintId = "Hidung ㄴ+ㅏ = 나, Mulut ㅁ+ㅜ = 무!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you read the Korean word for 'tree'?",
                    promptId = "Bagaimana cara membaca kata Korea untuk 'pohon' ini?",
                    stimulusKo = "나무",
                    options = listOf("na-mu", "ma-nu", "ba-da", "ga-su"),
                    correctOption = "na-mu",
                    explanationEn = "ㄴ+ㅏ = na, ㅁ+ㅜ = mu → na-mu (tree).",
                    explanationId = "ㄴ+ㅏ = na, ㅁ+ㅜ = mu → na-mu (pohon)."
                ),
                BeginnerMiniCheck(
                    promptEn = "Which consonant looks like a square mouth and sounds like 'm'?",
                    promptId = "Konsonan mana yang berbentuk kotak mulut dan berbunyi 'm'?",
                    stimulusKo = "ㅁ",
                    options = listOf("ㅁ", "ㅂ", "ㄴ", "ㄷ"),
                    correctOption = "ㅁ",
                    explanationEn = "ㅁ is shaped like a mouth → 'm' sound (as in 마 ma, 무 mu).",
                    explanationId = "ㅁ berbentuk seperti mulut → bunyi 'm' (seperti 마 ma, 무 mu)."
                ),
                BeginnerMiniCheck(
                    promptEn = "Read this real Korean word meaning 'sea'!",
                    promptId = "Baca kata Korea asli yang artinya 'laut' ini!",
                    stimulusKo = "바다",
                    options = listOf("ba-da", "sa-da", "da-ba", "ra-da"),
                    correctOption = "ba-da",
                    explanationEn = "ㅂ (b) + ㅏ (a) = 바, ㄷ (d) + ㅏ (a) = 다 → ba-da!",
                    explanationId = "ㅂ (b) + ㅏ (a) = 바, ㄷ (d) + ㅏ (a) = 다 → ba-da!"
                )
            ),
            practiceAction = BeginnerPracticeAction.HANGUL_STUDIO,
            practiceLabelEn = "Experiment in Hangul Builder",
            practiceLabelId = "Coba Rakit Suku Kata di Studio Hangul"
        ),

        BeginnerLesson(
            id = "beg_03_blocks_and_aspirated",
            number = 3,
            stageNumber = 1,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "커",
            titleEn = "Syllable Blocks & Breath Consonants (ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ)",
            titleId = "Blok Suku Kata & Konsonan Hembus (ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ)",
            goalEn = "Finish all 14 basic consonants and read everyday loanwords like 커피 & 택시.",
            goalId = "Tuntaskan seluruh 14 konsonan dasar dan baca kata populer seperti 커피 & 택시.",
            overviewEn = "Every Hangul syllable fits inside one neat square! Tall vowels (ㅏ ㅓ ㅣ) sit on the RIGHT of the consonant (가, 나, 하). Wide vowels (ㅗ ㅜ ㅡ) sit BELOW the consonant (고, 노, 후). Adding a stroke adds a puff of air: ㄱ→ㅋ, ㄷ→ㅌ, ㅂ→ㅍ, ㅈ→ㅊ!",
            overviewId = "Setiap suku kata Hangul membentuk satu kotak rapi! Vokal tegak (ㅏ ㅓ ㅣ) ditaruh di KANAN konsonan (가, 나, 하). Vokal datar (ㅗ ㅜ ㅡ) ditaruh di BAWAH konsonan (고, 노, 후). Menambah 1 garis memberi hembusan napas kuat: ㄱ→ㅋ, ㄷ→ㅌ, ㅂ→ㅍ, ㅈ→ㅊ!",
            keyRuleEn = "Add 1 line = strong puff of air: ㄱ(g)→ㅋ(k), ㄷ(d)→ㅌ(t), ㅂ(b)→ㅍ(p), ㅈ(j)→ㅊ(ch), ㅎ(h = hat on a circle!).",
            keyRuleId = "Tambah 1 garis = bunyi berhembus kuat: ㄱ(g)→ㅋ(k), ㄷ(d)→ㅌ(t), ㅂ(b)→ㅍ(p), ㅈ(j)→ㅊ(ch), ㅎ(h = lingkaran bertopi!).",
            studyCards = listOf(
                BeginnerStudyCard("자", "ja", "ㅈ (j) + ㅏ (a)", "ㅈ (j) + ㅏ (a)", "ㅈ is ㅅ with a top roof → 'j'", "ㅈ adalah ㅅ bertutup atas → bunyi 'j'"),
                BeginnerStudyCard("차", "cha", "Tea / Car (ㅊ + ㅏ)", "Teh / Mobil (ㅊ + ㅏ)", "ㅊ adds a puff tick on ㅈ → strong 'ch'!", "ㅊ menambah garis di atas ㅈ → 'ch' berhembus!"),
                BeginnerStudyCard("커피", "keo-pi", "Coffee (ㅋ+ㅓ + ㅍ+ㅣ)", "Kopi (ㅋ+ㅓ + ㅍ+ㅣ)", "ㅋ = strong 'k', ㅍ = strong 'p' → keo-pi!", "ㅋ = 'k' kuat, ㅍ = 'p' kuat → keo-pi (kopi)!"),
                BeginnerStudyCard("피자", "pi-ja", "Pizza (ㅍ+ㅣ + ㅈ+ㅏ)", "Pizza (ㅍ+ㅣ + ㅈ+ㅏ)", "You can read Korean menus already!", "Kamu sudah bisa membaca menu Korea!"),
                BeginnerStudyCard("타다", "ta-da", "To ride (ㅌ+ㅏ + ㄷ+ㅏ)", "Naik kendaraan (ㅌ+ㅏ + ㄷ+ㅏ)", "ㅌ adds a line inside ㄷ → crisp 't'", "ㅌ menambah garis di tengah ㄷ → 't' tegas"),
                BeginnerStudyCard("하나", "ha-na", "One (ㅎ+ㅏ + ㄴ+ㅏ)", "Satu (ㅎ+ㅏ + ㄴ+ㅏ)", "ㅎ looks like a person wearing a Hat → 'h'", "ㅎ seperti orang memakai topi (Hat) → 'h'")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Assemble the cafe word 'Coffee' (keo-pi)",
                    promptId = "Susun kata kafe 'Kopi' (keo-pi)",
                    targetTokens = listOf("커", "피"),
                    distractorTokens = listOf("차"),
                    fullSentenceKo = "커피",
                    romanization = "keo-pi",
                    hintEn = "ㅋ+ㅓ = 커 (keo), ㅍ+ㅣ = 피 (pi)!",
                    hintId = "ㅋ+ㅓ = 커 (keo), ㅍ+ㅣ = 피 (pi)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Assemble your first mini phrase: 'One pizza' (피자 하나)",
                    promptId = "Susun frasa pertamamu: 'Satu pizza' (피자 하나)",
                    targetTokens = listOf("피자", "하나"),
                    distractorTokens = listOf("타다"),
                    fullSentenceKo = "피자 하나",
                    romanization = "pi-ja ha-na",
                    hintEn = "In Korean, the item (피자) comes first, then the number (하나)!",
                    hintId = "Dalam bahasa Korea, nama makanan (피자) disebut dulu, lalu jumlahnya (하나)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "What familiar drink is written here?",
                    promptId = "Minuman familiar apa yang tertulis di sini?",
                    stimulusKo = "커피",
                    options = listOf("Coffee (keo-pi)", "Tea (cha)", "Water (mul)", "Milk (u-yu)"),
                    correctOption = "Coffee (keo-pi)",
                    explanationEn = "ㅋ+ㅓ = keo, ㅍ+ㅣ = pi → 커피 (coffee)!",
                    explanationId = "ㅋ+ㅓ = keo, ㅍ+ㅣ = pi → 커피 (kopi)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Where does a horizontal vowel like ㅗ or ㅜ sit inside a block?",
                    promptId = "Di mana posisi vokal datar seperti ㅗ atau ㅜ dalam blok suku kata?",
                    stimulusKo = "코",
                    options = listOf("Below the consonant (코)", "To the right (카)", "Above the consonant", "Separate from consonant"),
                    correctOption = "Below the consonant (코)",
                    explanationEn = "Horizontal vowels (ㅗ, ㅜ, ㅡ) always sit cleanly UNDER the initial consonant.",
                    explanationId = "Vokal datar (ㅗ, ㅜ, ㅡ) selalu diletakkan di BAWAH konsonan awal."
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you read this Korean food word?",
                    promptId = "Bagaimana cara membaca kata makanan ini?",
                    stimulusKo = "피자",
                    options = listOf("pi-ja (Pizza)", "bi-sa", "ki-cha", "ti-ha"),
                    correctOption = "pi-ja (Pizza)",
                    explanationEn = "ㅍ+ㅣ = pi, ㅈ+ㅏ = ja → 피자 (pizza)!",
                    explanationId = "ㅍ+ㅣ = pi, ㅈ+ㅏ = ja → 피자 (pizza)!"
                )
            ),
            practiceAction = BeginnerPracticeAction.HANGUL_WRITING,
            practiceLabelEn = "Practice writing consonants",
            practiceLabelId = "Latih menulis konsonan"
        ),

        // ==================== STAGE 2: NATURAL READING & BATCHIM ====================
        BeginnerLesson(
            id = "beg_04_y_and_compound_vowels",
            number = 4,
            stageNumber = 2,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 3,
            emblemKo = "요",
            titleEn = "Y-Vowels & Everyday Combos (ㅑ ㅕ ㅛ ㅠ ㅐ ㅔ ㅘ)",
            titleId = "Vokal-Y & Vokal Gabungan (ㅑ ㅕ ㅛ ㅠ ㅐ ㅔ ㅘ)",
            goalEn = "Decode the 'extra line = Y' pattern and essential combined vowels.",
            goalId = "Kuasai trik 'tambah 1 garis = bunyi Y' dan vokal gabungan paling sering muncul.",
            overviewEn = "Good news: you don't need to memorize Y-vowels from scratch! Just add ONE extra short stroke to ㅏ ㅓ ㅗ ㅜ and you get ㅑ(ya) ㅕ(yeo) ㅛ(yo) ㅠ(yu). And ㅐ/ㅔ both sound like 'e' in everyday Seoul speech.",
            overviewId = "Kabar baik: kamu tidak perlu menghafal dari nol! Cukup tambah SATU garis kecil pada ㅏ ㅓ ㅗ ㅜ maka jadi ㅑ(ya) ㅕ(yeo) ㅛ(yo) ㅠ(yu). Lalu ㅐ dan ㅔ keduanya berbunyi 'e' seperti 'enak'.",
            keyRuleEn = "1 extra stroke = add 'y': ㅏ→ㅑ (ya), ㅓ→ㅕ (yeo), ㅗ→ㅛ (yo), ㅜ→ㅠ (yu). Plus ㅗ+ㅏ = ㅘ (wa)!",
            keyRuleId = "Tambah 1 garis = tambah 'y': ㅏ→ㅑ (ya), ㅓ→ㅕ (yeo), ㅗ→ㅛ (yo), ㅜ→ㅠ (yu). Dan ㅗ+ㅏ = ㅘ (wa)!",
            studyCards = listOf(
                BeginnerStudyCard("야구", "ya-gu", "Baseball (야 + 구)", "Bisbol (야 + 구)", "ㅏ (a) + 1 stroke = ㅑ (ya)", "ㅏ (a) + 1 garis = ㅑ (ya)"),
                BeginnerStudyCard("요리", "yo-ri", "Cooking (요 + 리)", "Memasak (요 + 리)", "ㅗ (o) + 1 stroke = ㅛ (yo)", "ㅗ (o) + 1 garis = ㅛ (yo)"),
                BeginnerStudyCard("우유", "u-yu", "Milk (우 + 유)", "Susu (우 + 유)", "ㅜ (u) + 1 stroke = ㅠ (yu) — super cute word!", "ㅜ (u) + 1 garis = ㅠ (yu) — kata yang mudah diingat!"),
                BeginnerStudyCard("카페", "ka-pe", "Cafe (카 + 페)", "Kafe (카 + 페)", "ㅔ and ㅐ sound like 'e' in modern Korean", "ㅔ dan ㅐ berbunyi 'e' dalam bahasa Korea modern"),
                BeginnerStudyCard("과자", "gwa-ja", "Snack (과 + 자)", "Camilan (과 + 자)", "ㅗ(o) + ㅏ(a) blended fast → ㅘ (wa)!", "ㅗ(o) + ㅏ(a) diucapkan cepat → ㅘ (wa)!"),
                BeginnerStudyCard("뭐", "mwo", "What? (casual)", "Apa? (santai)", "ㅜ(u) + ㅓ(eo) blended fast → ㅝ (wo)!", "ㅜ(u) + ㅓ(eo) diucapkan cepat → ㅝ (wo)!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Assemble 'Milk' (u-yu) using the Y-vowel trick",
                    promptId = "Susun kata 'Susu' (u-yu) dengan trik vokal-Y",
                    targetTokens = listOf("우", "유"),
                    distractorTokens = listOf("요"),
                    fullSentenceKo = "우유",
                    romanization = "u-yu",
                    hintEn = "우 (u) first, then 유 (yu — two ticks down)!",
                    hintId = "우 (u) dulu, lalu 유 (yu — dua garis ke bawah)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the cafe phrase: 'Cafe snack' (카페 과자)",
                    promptId = "Susun frasa kafe: 'Camilan kafe' (카페 과자)",
                    targetTokens = listOf("카페", "과자"),
                    distractorTokens = listOf("야구"),
                    fullSentenceKo = "카페 과자",
                    romanization = "ka-pe gwa-ja",
                    hintEn = "카페 (cafe) + 과자 (snack)!",
                    hintId = "카페 (kafe) + 과자 (camilan)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Read this everyday Korean drink!",
                    promptId = "Baca nama minuman sehari-hari ini!",
                    stimulusKo = "우유",
                    options = listOf("u-yu (Milk)", "o-yo", "a-ya", "eo-yeo"),
                    correctOption = "u-yu (Milk)",
                    explanationEn = "우 (u) + 유 (yu) = u-yu (milk)!",
                    explanationId = "우 (u) + 유 (yu) = u-yu (susu)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What sound does adding a second tick to ㅗ (o) create?",
                    promptId = "Bunyi apa yang muncul jika ㅗ (o) ditambah satu garis lagi menjadi ㅛ?",
                    stimulusKo = "요",
                    options = listOf("yo", "ya", "yu", "wa"),
                    correctOption = "yo",
                    explanationEn = "ㅗ (o) + 1 short tick = ㅛ (yo), as in 요리 (cooking) and 안녕하세요!",
                    explanationId = "ㅗ (o) + 1 garis = ㅛ (yo), seperti di 요리 (masak) dan 안녕하세요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you read the combined vowel ㅘ in 과자 (snack)?",
                    promptId = "Bagaimana membaca vokal gabungan ㅘ pada kata 과자 (camilan)?",
                    stimulusKo = "과자",
                    options = listOf("gwa-ja", "go-ja", "gu-ja", "ga-ja"),
                    correctOption = "gwa-ja",
                    explanationEn = "ㅗ + ㅏ = ㅘ (wa), so ㄱ+ㅘ = gwa → gwa-ja.",
                    explanationId = "ㅗ + ㅏ = ㅘ (wa), jadi ㄱ+ㅘ = gwa → gwa-ja."
                )
            ),
            practiceAction = BeginnerPracticeAction.HANGUL_STUDIO,
            practiceLabelEn = "Explore all vowels in Hangul Studio",
            practiceLabelId = "Lihat semua vokal di Studio Hangul"
        ),

        BeginnerLesson(
            id = "beg_05_batchim_made_simple",
            number = 5,
            stageNumber = 2,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "한",
            titleEn = "Final Consonants (Batchim 받침) Made Simple",
            titleId = "Konsonan Akhir (Batchim 받침) Tanpa Pusing",
            goalEn = "Read 3-letter syllable blocks like 한국, 사랑, and 김밥 confidently.",
            goalId = "Membaca blok 3 huruf seperti 한국, 사랑, dan 김밥 dengan percaya diri.",
            overviewEn = "When a consonant sits at the BOTTOM floor of a syllable block, it is called Batchim (받침 = support). Good news: no matter which consonant sits on the bottom, it always reduces to one of just 7 clean ending sounds (k, n, t, l, m, p, ng).",
            overviewId = "Ketika konsonan diletakkan di LANTAI BAWAH blok suku kata, itu disebut Batchim (받침 = penyangga). Hebatnya, konsonan apa pun di bawah hanya menghasilkan 7 bunyi akhir yang jelas (k, n, t, l, m, p, ng).",
            keyRuleEn = "Big Secret: ㅇ at the TOP is silent, but ㅇ at the BOTTOM sounds like '-ng'! So 사 + 랑 = sa-rang (love)!",
            keyRuleId = "Rahasia Besar: ㅇ di ATAS/AWAL senyap, tapi ㅇ di BAWAH berbunyi '-ng'! Jadi 사 + 랑 = sa-rang (cinta)!",
            studyCards = listOf(
                BeginnerStudyCard("한", "han", "ㅎ(h) + ㅏ(a) + ㄴ(n)", "ㅎ(h) + ㅏ(a) + ㄴ(n)", "Read top-to-bottom: h + a + n = han!", "Baca dari atas ke bawah: h + a + n = han!"),
                BeginnerStudyCard("한국", "han-guk", "South Korea", "Korea Selatan", "ㄱ at the bottom closes softly as '-k' → han-guk", "ㄱ di bawah menutup lembut sebagai '-k' → han-guk"),
                BeginnerStudyCard("사랑", "sa-rang", "Love", "Cinta", "ㅇ at the bottom = '-ng'! 라 + ㅇ = rang", "ㅇ di bawah = '-ng'! 라 + ㅇ = rang"),
                BeginnerStudyCard("김밥", "gim-bap", "Kimbap (seaweed rice roll)", "Kimbap (nasi gulung rumput laut)", "ㅁ = '-m' (gim), ㅂ = '-p' (bap) → gim-bap!", "ㅁ = '-m' (gim), ㅂ = '-p' (bap) → gim-bap!"),
                BeginnerStudyCard("물", "mul", "Water", "Air", "ㄹ at the bottom sounds like '-l' → mul", "ㄹ di bawah berbunyi '-l' → mul"),
                BeginnerStudyCard("안녕", "an-nyeong", "Hi / Peace", "Halo / Damai", "안 (an) + 녕 (nyeong) — the root of 안녕하세요!", "안 (an) + 녕 (nyeong) — akar kata 안녕하세요!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Assemble the Korean word for 'Korea' (han-guk)",
                    promptId = "Susun kata Korea untuk 'Korea' (han-guk)",
                    targetTokens = listOf("한", "국"),
                    distractorTokens = listOf("물"),
                    fullSentenceKo = "한국",
                    romanization = "han-guk",
                    hintEn = "한 (han) + 국 (guk) = 한국!",
                    hintId = "한 (han) + 국 (guk) = 한국!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the phrase: 'Korean kimbap' (한국 김밥)",
                    promptId = "Susun frasa: 'Kimbap Korea' (한국 김밥)",
                    targetTokens = listOf("한국", "김밥"),
                    distractorTokens = listOf("사랑"),
                    fullSentenceKo = "한국 김밥",
                    romanization = "han-guk gim-bap",
                    hintEn = "Place 한국 (Korea) before 김밥 (kimbap)!",
                    hintId = "Taruh 한국 (Korea) di depan 김밥 (kimbap)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "What sound does ㅇ make when it sits at the BOTTOM (batchim) of 랑?",
                    promptId = "Bunyi apa yang dihasilkan ㅇ saat berada di BAWAH (batchim) pada 랑?",
                    stimulusKo = "사랑",
                    options = listOf("-ng (sa-rang)", "Silent (sa-ra)", "-n (sa-ran)", "-m (sa-ram)"),
                    correctOption = "-ng (sa-rang)",
                    explanationEn = "ㅇ is silent at the start, and '-ng' at the bottom → 사랑 = sa-rang!",
                    explanationId = "ㅇ senyap di awal, tetapi berbunyi '-ng' di bawah → 사랑 = sa-rang!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Read this iconic Korean dish!",
                    promptId = "Baca nama makanan khas Korea ini!",
                    stimulusKo = "김밥",
                    options = listOf("gim-bap", "gi-ba", "gam-ban", "kim-bang"),
                    correctOption = "gim-bap",
                    explanationEn = "김 (g+i+m) + 밥 (b+a+p) = gim-bap!",
                    explanationId = "김 (g+i+m) + 밥 (b+a+p) = gim-bap!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you read 한국 (Korea)?",
                    promptId = "Bagaimana membaca kata 한국 (Korea)?",
                    stimulusKo = "한국",
                    options = listOf("han-guk", "ha-gu", "hang-un", "han-mul"),
                    correctOption = "han-guk",
                    explanationEn = "한 (han) + 국 (guk) = han-guk (Korea).",
                    explanationId = "한 (han) + 국 (guk) = han-guk (Korea)."
                )
            ),
            practiceAction = BeginnerPracticeAction.HANGUL_STUDIO,
            practiceLabelEn = "Explore Batchim 7-Sound Guide",
            practiceLabelId = "Lihat Panduan 7 Bunyi Batchim"
        ),

        BeginnerLesson(
            id = "beg_06_tense_and_liaison",
            number = 6,
            stageNumber = 2,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "빵",
            titleEn = "Double Consonants & Smooth Linking (연음)",
            titleId = "Konsonan Ganda & Sambung Bunyi (Liaison)",
            goalEn = "Pronounce ㄲ ㄸ ㅃ ㅆ ㅉ crisply and master the #1 natural reading rule.",
            goalId = "Melafalkan ㄲ ㄸ ㅃ ㅆ ㅉ dengan tegas dan menguasai aturan baca alami #1.",
            overviewEn = "Double consonants (ㄲ ㄸ ㅃ ㅆ ㅉ) are pronounced firmly with ZERO puff of air — like 'p' in 'spa' or 't' in 'stop'. And here is the #1 Korean reading secret: when a batchim is followed by silent ㅇ, the bottom consonant JUMPS over to start the next syllable!",
            overviewId = "Konsonan ganda (ㄲ ㄸ ㅃ ㅆ ㅉ) diucapkan tegas TANPA hembusan napas (mirip 'p' di 'apa' atau 'k' di 'kakak'). Dan inilah rahasia baca #1 Korea: jika batchim bertemu ㅇ senyap, bunyi bawahnya MELOMPAT ke suku kata berikutnya!",
            keyRuleEn = "Liaison Rule (연음): 한국어 is written han-guk-eo, but pronounced 한구거 (han-gu-geo) because ㄱ jumps into ㅇ!",
            keyRuleId = "Aturan Sambung Bunyi (연음): 한국어 ditulis han-guk-eo, tapi dibaca 한구거 (han-gu-geo) karena ㄱ melompat menggantikan ㅇ!",
            studyCards = listOf(
                BeginnerStudyCard("빵", "ppang", "Bread (ㅃ = crisp tense p)", "Roti (ㅃ = 'p' tegas tanpa angin)", "Tense ㅃ + ㅏ + ㅇ(-ng) = ppang!", "ㅃ tegas + ㅏ + ㅇ(-ng) = ppang (roti)!"),
                BeginnerStudyCard("오빠", "o-ppa", "Older brother (to female)", "Kakak laki-laki (panggilan perempuan)", "오 + 빠 (tense p) = o-ppa", "오 + 빠 ('p' tegas) = o-ppa"),
                BeginnerStudyCard("딸기", "ttal-gi", "Strawberry (ㄸ = tense t)", "Stroberi (ㄸ = 't' tegas)", "ㄸ + ㅏ + ㄹ(l) + 기 = ttal-gi", "ㄸ + ㅏ + ㄹ(l) + 기 = ttal-gi"),
                BeginnerStudyCard("한국어", "han-gu-geo", "Korean language (spoken: 한구거)", "Bahasa Korea (dibaca: 한구거)", "ㄱ jumps into 어 → han-gu-geo, super smooth!", "ㄱ melompat ke 어 → han-gu-geo, mulus banget!"),
                BeginnerStudyCard("먹어요", "meo-geo-yo", "Eat / Eating (spoken: 머거요)", "Makan (dibaca: 머거요)", "ㄱ in 먹 jumps into 어 → meo-geo-yo!", "ㄱ pada 먹 melompat ke 어 → meo-geo-yo!"),
                BeginnerStudyCard("이름이", "i-reu-mi", "Name (subject) (spoken: 이르미)", "Nama (subjek) (dibaca: 이르미)", "ㅁ in 름 jumps into 이 → i-reu-mi", "ㅁ pada 름 melompat ke 이 → i-reu-mi")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build your first food sentence: 'I eat bread' (빵 먹어요)",
                    promptId = "Susun kalimat makanan pertamamu: 'Makan roti' (빵 먹어요)",
                    targetTokens = listOf("빵", "먹어요"),
                    distractorTokens = listOf("한국어"),
                    fullSentenceKo = "빵 먹어요",
                    romanization = "ppang meo-geo-yo",
                    hintEn = "Food (빵) first, action verb (먹어요) at the end!",
                    hintId = "Makanan (빵) di depan, kata kerja (먹어요) di akhir!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the phrase: 'Strawberry bread' (딸기 빵)",
                    promptId = "Susun frasa: 'Roti stroberi' (딸기 빵)",
                    targetTokens = listOf("딸기", "빵"),
                    distractorTokens = listOf("오빠"),
                    fullSentenceKo = "딸기 빵",
                    romanization = "ttal-gi ppang",
                    hintEn = "Flavor (딸기) comes first, then bread (빵)!",
                    hintId = "Rasa (딸기) ditaruh dulu, baru roti (빵)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How is 한국어 (Korean language) naturally pronounced via linking?",
                    promptId = "Bagaimana pelafalan alami kata 한국어 (bahasa Korea) dengan aturan sambung bunyi?",
                    stimulusKo = "한국어",
                    options = listOf("han-gu-geo (한구거)", "han-guk-eo (paused)", "han-gu-keo", "ha-gu-eo"),
                    correctOption = "han-gu-geo (한구거)",
                    explanationEn = "Bottom ㄱ smoothly slides into the silent ㅇ of 어 → han-gu-geo!",
                    explanationId = "Batchim ㄱ melompat halus menggantikan ㅇ pada 어 → han-gu-geo!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 빵 (ppang) mean?",
                    promptId = "Apa arti kata 빵 (ppang)?",
                    stimulusKo = "빵",
                    options = listOf("Bread / Roti", "Water / Air", "Coffee / Kopi", "Room / Kamar"),
                    correctOption = "Bread / Roti",
                    explanationEn = "빵 (ppang) means bread — a super useful word at any Korean bakery!",
                    explanationId = "빵 (ppang) artinya roti — sangat berguna di toko roti Korea!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How does 먹어요 sound naturally when spoken?",
                    promptId = "Bagaimana bunyi alami 먹어요 (makan) saat diucapkan?",
                    stimulusKo = "먹어요",
                    options = listOf("meo-geo-yo (머거요)", "meok-eo-yo", "mo-ko-yo", "meo-keo-yo"),
                    correctOption = "meo-geo-yo (머거요)",
                    explanationEn = "ㄱ links forward into 어 → 머거요 (meo-geo-yo).",
                    explanationId = "ㄱ tersambung ke depan menuju 어 → 머거요 (meo-geo-yo)."
                )
            ),
            practiceAction = BeginnerPracticeAction.BEGINNER_FLASHCARDS,
            practiceLabelEn = "Review Hangul words in Flashcards",
            practiceLabelId = "Ulas kosakata Hangul di Flashcard"
        ),

        // ==================== STAGE 3: GREETINGS & BUILDING FIRST SENTENCES ====================
        BeginnerLesson(
            id = "beg_07_essential_greetings",
            number = 7,
            stageNumber = 3,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 3,
            emblemKo = "안",
            titleEn = "8 Essential Polite Greetings",
            titleId = "8 Sapaan & Ungkapan Sopan Wajib",
            goalEn = "Greet, thank, apologize, and reply politely to anyone in Korea.",
            goalId = "Menyapa, berterima kasih, meminta maaf, dan menjawab dengan sopan di Korea.",
            overviewEn = "Congratulations — you can now read Hangul! Now let's speak right away. In Korea, ending a sentence with ~요 (yo) or ~습니다 (seumnida) immediately sounds warm, polite, and respectful.",
            overviewId = "Selamat — kamu sudah bisa membaca Hangul! Sekarang saatnya langsung berbicara. Di Korea, mengakhiri kalimat dengan ~요 (yo) atau ~습니다 (seumnida) langsung terdengar sopan dan hangat.",
            keyRuleEn = "Politeness Rule: When in doubt, use ~요 (polite everyday) or ~합니다 (formal polite). Never drop ~요 with strangers!",
            keyRuleId = "Aturan Kesopanan: Saat ragu, selalu gunakan akhiran ~요 (sopan harian) atau ~합니다 (formal sopan) kepada orang baru.",
            studyCards = listOf(
                BeginnerStudyCard("안녕하세요", "an-nyeong-ha-se-yo", "Hello (polite, anytime)", "Halo / Selamat pagi-siang-malam (sopan)", "Works morning, afternoon & night!", "Bisa dipakai pagi, siang, sore, maupun malam!"),
                BeginnerStudyCard("감사합니다", "gam-sa-ham-ni-da", "Thank you (formal polite)", "Terima kasih (sopan formal)", "Note: ㅂ before ㄴ sounds like 'm' → ham-ni-da!", "Catatan: ㅂ bertemu ㄴ dibaca 'm' → ham-ni-da!"),
                BeginnerStudyCard("죄송합니다", "joe-song-ham-ni-da", "I'm sorry (polite)", "Maaf / Mohon maaf (sopan)", "Polite apology for mistakes or bumping someone", "Permintaan maaf sopan di segala situasi"),
                BeginnerStudyCard("네 / 아니요", "ne / a-ni-yo", "Yes / No (polite)", "Ya / Tidak (sopan)", "Use 네 (ne) to agree or say 'I'm listening'", "Gunakan 네 (ne) untuk mengiyakan dengan sopan"),
                BeginnerStudyCard("괜찮아요", "gwaen-cha-na-yo", "It's okay / No problem", "Tidak apa-apa / Baik-baik saja", "Super versatile: 'I'm fine' or 'No thanks'", "Sangat serbaguna: 'tidak apa-apa' atau 'baik'"),
                BeginnerStudyCard("잠시만요", "jam-si-man-yo", "Excuse me / Just a moment", "Permisi / Sebentar ya", "Use when passing through a crowd or pausing", "Dipakai saat permisi lewat atau minta tunggu sebentar")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Assemble the polite reply: 'Yes, thank you!'",
                    promptId = "Susun balasan sopan: 'Ya, terima kasih!'",
                    targetTokens = listOf("네,", "감사합니다"),
                    distractorTokens = listOf("아니요,"),
                    fullSentenceKo = "네, 감사합니다",
                    romanization = "ne, gam-sa-ham-ni-da",
                    hintEn = "Start with 네, (Yes), then 감사합니다 (thank you)!",
                    hintId = "Mulai dengan 네, (Ya), lalu 감사합니다 (terima kasih)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Assemble the polite reply: 'No, it's okay!'",
                    promptId = "Susun balasan sopan: 'Tidak, tidak apa-apa!'",
                    targetTokens = listOf("아니요,", "괜찮아요"),
                    distractorTokens = listOf("죄송합니다"),
                    fullSentenceKo = "아니요, 괜찮아요",
                    romanization = "a-ni-yo, gwaen-cha-na-yo",
                    hintEn = "아니요, (No) + 괜찮아요 (it's okay)!",
                    hintId = "아니요, (Tidak) + 괜찮아요 (tidak apa-apa)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you politely say 'Hello' at any time of day?",
                    promptId = "Bagaimana menyapa 'Halo' secara sopan di segala waktu?",
                    stimulusKo = "안녕하세요",
                    options = listOf("안녕하세요", "감사합니다", "죄송합니다", "아니요"),
                    correctOption = "안녕하세요",
                    explanationEn = "안녕하세요 (an-nyeong-ha-se-yo) is the universal polite Korean greeting.",
                    explanationId = "안녕하세요 (an-nyeong-ha-se-yo) adalah sapaan sopan universal di Korea."
                ),
                BeginnerMiniCheck(
                    promptEn = "Someone accidentally bumps into you and says 죄송합니다. How do you kindly reply 'It's okay'?",
                    promptId = "Seseorang tak sengaja menyenggolmu dan berkata 죄송합니다. Bagaimana membalas 'Tidak apa-apa'?",
                    stimulusKo = "괜찮아요",
                    options = listOf("괜찮아요", "잠시만요", "아니요", "감사합니다"),
                    correctOption = "괜찮아요",
                    explanationEn = "괜찮아요 (gwaen-cha-na-yo) means 'It's okay / No worries'.",
                    explanationId = "괜찮아요 (gwaen-cha-na-yo) berarti 'Tidak apa-apa'."
                ),
                BeginnerMiniCheck(
                    promptEn = "How is 감사합니다 pronounced naturally?",
                    promptId = "Bagaimana pelafalan alami kata 감사합니다 (terima kasih)?",
                    stimulusKo = "감사합니다",
                    options = listOf("gam-sa-ham-ni-da", "gam-sa-hap-ni-da", "kam-sa-ha-da", "gam-sa-yo"),
                    correctOption = "gam-sa-ham-ni-da",
                    explanationEn = "ㅂ softens to 'm' before ㄴ → gam-sa-ham-ni-da.",
                    explanationId = "ㅂ melunak menjadi 'm' sebelum ㄴ → gam-sa-ham-ni-da."
                )
            ),
            practiceAction = BeginnerPracticeAction.EVERYDAY_SPEAKING,
            practiceLabelEn = "Practice greetings in Speaking Studio",
            practiceLabelId = "Latih sapaan di Studio Bicara"
        ),

        BeginnerLesson(
            id = "beg_08_sov_and_self_intro",
            number = 8,
            stageNumber = 3,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "저",
            titleEn = "Building Korean Sentences (S-O-V) & Self-Intro",
            titleId = "Menyusun Kalimat Korea (S-O-P) & Perkenalan",
            goalEn = "Build real S-O-V Korean sentences and introduce yourself smoothly.",
            goalId = "Menyusun kalimat Korea berpola S-O-P dan memperkenalkan diri dengan lancar.",
            overviewEn = "Unlike English/Indonesian (Subject → Verb → Object), Korean ALWAYS puts the verb at the VERY END: Subject → Object → Verb! ('I + coffee + drink' = 저는 커피를 마셔요). For 'I am [Noun]', attach 이에요 (after batchim) or 예요 (after a vowel).",
            overviewId = "Berbeda dengan bahasa Indonesia (Subjek → Predikat → Objek), bahasa Korea SELALU menaruh kata kerja di PALING AKHIR: Subjek → Objek → Kata Kerja! ('Saya + kopi + minum' = 저는 커피를 마셔요). Untuk 'Saya adalah [Nama]', tempel 이에요 (setelah batchim) atau 예요 (setelah vokal).",
            keyRuleEn = "Sentence Formula: [Subject 은/는] + [Object 을/를] + [Verb]! Self-Intro: 저는 [Noun]이에요/예요.",
            keyRuleId = "Rumus Menyusun Kalimat: [Subjek 은/는] + [Objek 을/를] + [Kata Kerja]! Perkenalan: 저는 [Kata Benda]이에요/예요.",
            studyCards = listOf(
                BeginnerStudyCard("저는", "jeo-neun", "I / As for me (polite)", "Saya (sopan, topik kalimat)", "저 = humble polite 'I'; 는 marks the topic", "저 = 'saya' sopan; 는 menandai topik kalimat"),
                BeginnerStudyCard("학생이에요", "hak-saeng-i-e-yo", "I am a student", "Saya adalah pelajar", "학생 ends in batchim ㅇ → attach 이에요", "학생 berakhiran batchim ㅇ → pakai 이에요"),
                BeginnerStudyCard("인도네시아 사람이에요", "in-do-ne-si-a sa-ram-i-e-yo", "I am Indonesian", "Saya orang Indonesia", "사람 (person) ends in ㅁ → attach 이에요", "사람 (orang) berakhiran ㅁ → pakai 이에요"),
                BeginnerStudyCard("저는 커피를 마셔요", "jeo-neun keo-pi-reul ma-syeo-yo", "I drink coffee (S-O-V!)", "Saya minum kopi (S-O-P!)", "Literally: I + coffee + drink!", "Secara harfiah: Saya + kopi + minum!"),
                BeginnerStudyCard("만나서 반가워요", "man-na-seo ban-ga-wo-yo", "Nice to meet you!", "Senang bertemu denganmu!", "Warm closing line after giving your name", "Kalimat penutup ramah setelah menyebutkan nama")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the S-O-V sentence: 'I drink coffee'",
                    promptId = "Susun kalimat S-O-P: 'Saya minum kopi'",
                    targetTokens = listOf("저는", "커피를", "마셔요"),
                    distractorTokens = listOf("학생이에요"),
                    fullSentenceKo = "저는 커피를 마셔요",
                    romanization = "jeo-neun keo-pi-reul ma-syeo-yo",
                    hintEn = "Subject (저는) → Object (커피를) → Verb at the END (마셔요)!",
                    hintId = "Subjek (저는) → Objek (커피를) → Kata kerja di AKHIR (마셔요)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build your self-introduction: 'I am a student'",
                    promptId = "Susun perkenalan dirimu: 'Saya adalah pelajar'",
                    targetTokens = listOf("저는", "학생이에요"),
                    distractorTokens = listOf("마셔요"),
                    fullSentenceKo = "저는 학생이에요",
                    romanization = "jeo-neun hak-saeng-i-e-yo",
                    hintEn = "Start with 저는 (I), then finish with 학생이에요 (am a student)!",
                    hintId = "Mulai dengan 저는 (Saya), lalu tutup dengan 학생이에요 (pelajar)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Where does the verb ALWAYS go in a Korean sentence?",
                    promptId = "Di mana posisi kata kerja SELALU berada dalam kalimat bahasa Korea?",
                    stimulusKo = "저는 커피를 마셔요",
                    options = listOf("At the very end (S-O-V)", "Right after subject (S-V-O)", "At the very beginning", "Anywhere freely"),
                    correctOption = "At the very end (S-O-V)",
                    explanationEn = "Korean follows Subject → Object → Verb: 저는 (I) 커피를 (coffee) 마셔요 (drink).",
                    explanationId = "Bahasa Korea berpola Subjek → Objek → Kata Kerja: 저는 (saya) 커피를 (kopi) 마셔요 (minum)."
                ),
                BeginnerMiniCheck(
                    promptEn = "The noun 학생 (student) ends with a batchim (ㅇ). Which ending means 'am/is'?",
                    promptId = "Kata 학생 (pelajar) berakhiran batchim (ㅇ). Akhiran mana yang tepat untuk 'adalah'?",
                    stimulusKo = "학생___",
                    options = listOf("이에요 (학생이에요)", "예요 (학생예요)", "마셔요", "가요"),
                    correctOption = "이에요 (학생이에요)",
                    explanationEn = "After a batchim (final consonant), use 이에요 → 학생이에요.",
                    explanationId = "Setelah huruf ber-batchim (konsonan akhir), gunakan 이에요 → 학생이에요."
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 만나서 반가워요 mean?",
                    promptId = "Apa arti ungkapan 만나서 반가워요?",
                    stimulusKo = "만나서 반가워요",
                    options = listOf("Nice to meet you / Senang bertemu", "Goodbye / Selamat tinggal", "How much is this?", "I am hungry"),
                    correctOption = "Nice to meet you / Senang bertemu",
                    explanationEn = "만나서 반가워요 is the friendly polite way to say 'Nice to meet you!'.",
                    explanationId = "만나서 반가워요 artinya 'Senang bertemu denganmu!'."
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "See more patterns in Grammar Studio",
            practiceLabelId = "Lihat pola lain di Studio Tata Bahasa"
        ),

        BeginnerLesson(
            id = "beg_09_core_particles",
            number = 9,
            stageNumber = 3,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "은",
            titleEn = "4 Core Particles to Connect Sentences (은/는, 이/가, 을/를, 에)",
            titleId = "4 Partikel Kunci Perangkai Kalimat (은/는, 이/가, 을/를, 에)",
            goalEn = "Attach the right particle to build clear Subject, Object & Place sentences.",
            goalId = "Memasang partikel yang tepat untuk merangkai kalimat Subjek, Objek & Tempat.",
            overviewEn = "Korean attaches tiny 1-syllable tags (particles) right after nouns so you instantly know WHO is acting, WHAT is acted on, and WHERE! They always come in pairs based on whether the noun ends in a batchim.",
            overviewId = "Bahasa Korea menempelkan penanda 1 suku kata (partikel) tepat di belakang kata benda agar langsung jelas SIAPA pelakunya, APA objeknya, dan DI MANA! Pasangannya selalu mengikuti: ada batchim vs tanpa batchim.",
            keyRuleEn = "Batchim Pair Trick: With batchim → 은 / 이 / 을. Without batchim → 는 / 가 / 를. Location/Direction → 에 (to/at) & 에서 (action at).",
            keyRuleId = "Trik Pasangan Batchim: Ada batchim → 은 / 이 / 을. Tanpa batchim → 는 / 가 / 를. Tempat/Arah → 에 (ke/di) & 에서 (melakukan kegiatan di).",
            studyCards = listOf(
                BeginnerStudyCard("은 / 는", "eun / neun", "Topic marker ('As for...')", "Penanda topik ('Adapun / Mengenai...')", "이름은 (batchim ㅁ) vs 저는 (no batchim)", "이름은 (ada batchim ㅁ) vs 저는 (tanpa batchim)"),
                BeginnerStudyCard("이 / 가", "i / ga", "Subject marker (who/what)", "Penanda subjek (siapa/apa)", "날씨가 좋아요 (The weather is nice)", "날씨가 좋아요 (Cuacanya bagus)"),
                BeginnerStudyCard("을 / 를", "eul / reul", "Object marker (receives verb)", "Penanda objek (yang dikenai tindakan)", "밥을 먹어요 (batchim) vs 커피를 마셔요 (vowel)", "밥을 먹어요 (ada batchim) vs 커피를 마셔요 (vokal)"),
                BeginnerStudyCard("서울에 가요", "seo-u-re ga-yo", "I go TO Seoul (에 = destination)", "Saya pergi KE Seoul (에 = tujuan)", "Use 에 with 가다 (go), 오다 (come), 있다 (exist)", "Pakai 에 dengan 가다 (pergi), 오다 (datang), 있다 (ada)"),
                BeginnerStudyCard("카페에서 공부해요", "ka-pe-e-seo gong-bu-hae-yo", "Study AT a cafe (에서 = action place)", "Belajar DI kafe (에서 = tempat beraktivitas)", "Use 에서 whenever you DO an action at a place", "Pakai 에서 saat MELAKUKAN aktivitas di suatu tempat")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the sentence: 'I go to Seoul'",
                    promptId = "Susun kalimat: 'Saya pergi ke Seoul'",
                    targetTokens = listOf("저는", "서울에", "가요"),
                    distractorTokens = listOf("서울에서"),
                    fullSentenceKo = "저는 서울에 가요",
                    romanization = "jeo-neun seo-u-re ga-yo",
                    hintEn = "Use destination particle 에 (서울에) with 가요 (go)!",
                    hintId = "Gunakan partikel tujuan 에 (서울에) untuk kata kerja 가요 (pergi)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the sentence: 'I study at a cafe'",
                    promptId = "Susun kalimat: 'Saya belajar di kafe'",
                    targetTokens = listOf("저는", "카페에서", "공부해요"),
                    distractorTokens = listOf("카페에"),
                    fullSentenceKo = "저는 카페에서 공부해요",
                    romanization = "jeo-neun ka-pe-e-seo gong-bu-hae-yo",
                    hintEn = "Doing an action (공부해요) at a place takes 에서 (카페에서)!",
                    hintId = "Melakukan aktivitas (공부해요) di suatu tempat memakai 에서 (카페에서)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Choose the right object particle for 물 (water — ends in batchim ㄹ):",
                    promptId = "Pilih partikel objek yang tepat untuk 물 (air — punya batchim ㄹ):",
                    stimulusKo = "물___ 마셔요",
                    options = listOf("을 (물을)", "를 (물를)", "에 (물에)", "에서 (물에서)"),
                    correctOption = "을 (물을)",
                    explanationEn = "물 ends in batchim ㄹ, so attach 을 → 물을 마셔요 (drink water).",
                    explanationId = "물 berakhiran batchim ㄹ, jadi tempelkan 을 → 물을 마셔요 (minum air)."
                ),
                BeginnerMiniCheck(
                    promptEn = "Which particle marks the destination in 'I go to school'?",
                    promptId = "Partikel mana yang menandai tujuan pada kalimat 'Saya pergi ke sekolah'?",
                    stimulusKo = "학교___ 가요",
                    options = listOf("에 (학교에 가요)", "을 (학교을 가요)", "은 (학교은 가요)", "이 (학교이 가요)"),
                    correctOption = "에 (학교에 가요)",
                    explanationEn = "에 attaches to destinations with 가다 (to go) → 학교에 가요.",
                    explanationId = "에 dipakai untuk tujuan bersama 가다 (pergi) → 학교에 가요."
                ),
                BeginnerMiniCheck(
                    promptEn = "You are EATING at a restaurant (식당). Which location particle fits an active verb?",
                    promptId = "Kamu sedang MAKAN di restoran (식당). Partikel tempat mana yang cocok untuk kata kerja aksi?",
                    stimulusKo = "식당___ 먹어요",
                    options = listOf("에서 (식당에서)", "를 (식당를)", "가 (식당가)", "는 (식당는)"),
                    correctOption = "에서 (식당에서)",
                    explanationEn = "Use 에서 when an action happens at a location → 식당에서 먹어요.",
                    explanationId = "Gunakan 에서 ketika suatu aktivitas terjadi di lokasi tersebut → 식당에서 먹어요."
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Practice particles in Grammar Studio",
            practiceLabelId = "Latih partikel di Studio Tata Bahasa"
        ),

        // ==================== STAGE 4: REAL-LIFE SEOUL SURVIVAL ====================
        BeginnerLesson(
            id = "beg_10_korean_numbers",
            number = 10,
            stageNumber = 4,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "일",
            titleEn = "Korean Numbers Without Confusion",
            titleId = "Angka Korea (Sino vs Asli) Tanpa Bingung",
            goalEn = "Know when to use 일·이·삼 (money/dates) vs 하나·둘·셋 (counting cups/hours).",
            goalId = "Tahu kapan memakai 일·이·삼 (harga/tanggal) vs 하나·둘·셋 (menghitung barang/jam).",
            overviewEn = "Korean uses two number systems — and once you know the golden rule, it is super easy! Use Sino-Korean (일 이 삼 사 오) for prices, phone numbers, dates, and minutes. Use Native Korean (하나 둘 셋 넷 다섯) for counting items, cups, people, and clock hours.",
            overviewId = "Bahasa Korea punya 2 sistem angka — sangat mudah begitu tahu aturannya! Pakai Sino-Korea (일 이 삼 사 오) untuk harga uang, nomor HP, tanggal, dan menit. Pakai Asli Korea (하나 둘 셋 넷 다섯) untuk menghitung barang, gelas, orang, dan jam.",
            keyRuleEn = "Order Tip: Before counters like 잔 (cup) or 개 (piece), 하나→한, 둘→두, 셋→세, 넷→네! Example: 한 잔 (1 cup), 두 개 (2 pieces).",
            keyRuleId = "Tips Pesan: Di depan satuan seperti 잔 (gelas) atau 개 (buah), 하나→한, 둘→두, 셋→세, 넷→네! Contoh: 한 잔 (1 gelas), 두 개 (2 buah).",
            studyCards = listOf(
                BeginnerStudyCard("일, 이, 삼, 사, 오", "il, i, sam, sa, o", "Sino-Korean 1–5 (prices, dates)", "Sino-Korea 1–5 (harga, tanggal, menit)", "Think: 3,000 won = 삼천 원 (sam-cheon won)", "Contoh: 3.000 won = 삼천 원 (sam-cheon won)"),
                BeginnerStudyCard("하나, 둘, 셋, 넷, 다섯", "ha-na, dul, set, net, da-seot", "Native Korean 1–5 (items, hours)", "Asli Korea 1–5 (jumlah barang, jam)", "Used for counting objects & telling the hour", "Dipakai untuk menghitung jumlah benda & jam"),
                BeginnerStudyCard("한 잔", "han jan", "One cup (coffee/tea/water)", "Satu gelas / cangkir (kopi/teh/air)", "하나 shortens to 한 before a counter!", "하나 disingkat menjadi 한 sebelum satuan!"),
                BeginnerStudyCard("두 개", "du gae", "Two pieces / items", "Dua buah / biji", "둘 shortens to 두 before 개 (general item counter)", "둘 disingkat menjadi 두 sebelum 개 (satuan umum)"),
                BeginnerStudyCard("삼천 원", "sam-cheon won", "3,000 Korean Won", "3.000 Won Korea", "천 = 1,000; 원 = Korean currency (Won)", "천 = 1.000; 원 = mata uang Won Korea")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Assemble: 'One cup of coffee' (커피 한 잔)",
                    promptId = "Susun: 'Satu cangkir kopi' (커피 한 잔)",
                    targetTokens = listOf("커피", "한", "잔"),
                    distractorTokens = listOf("일"),
                    fullSentenceKo = "커피 한 잔",
                    romanization = "keo-pi han jan",
                    hintEn = "Item (커피) + Native 한 (one) + counter 잔 (cup)!",
                    hintId = "Benda (커피) + angka Asli 한 (satu) + satuan 잔 (gelas)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Assemble the price sentence: 'It is 3,000 won'",
                    promptId = "Susun kalimat harga: 'Harganya 3.000 won'",
                    targetTokens = listOf("삼천", "원이에요"),
                    distractorTokens = listOf("세"),
                    fullSentenceKo = "삼천 원이에요",
                    romanization = "sam-cheon wo-ni-e-yo",
                    hintEn = "Prices use Sino-Korean 삼천 (3,000) + 원이에요 (won)!",
                    hintId = "Harga pakai Sino-Korea 삼천 (3.000) + 원이에요!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you ask for 'one cup' (e.g., 1 coffee) in Korean?",
                    promptId = "Bagaimana menyebut 'satu gelas/cangkir' (misal 1 kopi) dalam bahasa Korea?",
                    stimulusKo = "한 잔",
                    options = listOf("한 잔 (han jan)", "일 잔", "하나 원", "삼 개"),
                    correctOption = "한 잔 (han jan)",
                    explanationEn = "Counting cups uses Native Korean, and 하나 shortens to 한 → 한 잔!",
                    explanationId = "Menghitung gelas pakai Asli Korea, dan 하나 disingkat jadi 한 → 한 잔!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which number system is used for prices in Won (원)?",
                    promptId = "Sistem angka mana yang dipakai untuk harga dalam Won (원)?",
                    stimulusKo = "삼천 원",
                    options = listOf("Sino-Korean (일, 이, 삼...)", "Native Korean (하나, 둘, 셋...)", "English numbers only", "Neither"),
                    correctOption = "Sino-Korean (일, 이, 삼...)",
                    explanationEn = "Money/prices always use Sino-Korean: 삼천 원 = 3,000 won.",
                    explanationId = "Harga uang selalu menggunakan Sino-Korea: 삼천 원 = 3.000 won."
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 두 개 mean?",
                    promptId = "Apa arti ungkapan 두 개?",
                    stimulusKo = "두 개",
                    options = listOf("Two items / Dua buah", "One cup / Satu gelas", "Three people / Tiga orang", "Two won"),
                    correctOption = "Two items / Dua buah",
                    explanationEn = "두 (two, shortened from 둘) + 개 (piece/item) = two items!",
                    explanationId = "두 (dua, singkatan dari 둘) + 개 (buah/biji) = dua buah!"
                )
            ),
            practiceAction = BeginnerPracticeAction.HANGUL_STUDIO,
            practiceLabelEn = "Full Korean Numbers chart",
            practiceLabelId = "Lihat tabel lengkap Angka Korea"
        ),

        BeginnerLesson(
            id = "beg_11_cafe_and_shopping",
            number = 11,
            stageNumber = 4,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "주",
            titleEn = "Ordering at a Seoul Cafe & Asking Prices",
            titleId = "Memesan di Kafe Seoul & Bertanya Harga",
            goalEn = "Build complete cafe order sentences with '[Item] 주세요' and '얼마예요?'.",
            goalId = "Merangkai kalimat pesan kafe lengkap dengan '[Barang] 주세요' dan '얼마예요?'.",
            overviewEn = "With just TWO magic phrases — 주세요 ('Please give me...') and 얼마예요? ('How much is it?') — you can already order food, buy souvenirs, and survive your first day in Seoul with a smile!",
            overviewId = "Cukup dengan DUA kalimat ajaib — 주세요 ('Tolong beri saya...') dan 얼마예요? ('Berapa harganya?') — kamu sudah bisa memesan makanan, belanja oleh-oleh, dan bertahan di Seoul dengan percaya diri!",
            keyRuleEn = "Magic Survival Pattern: [Thing] + [Count] + 주세요! Example: 아메리카노 한 잔 주세요. (One Americano, please.)",
            keyRuleId = "Pola Ajaib Bertahan Hidup: [Benda] + [Jumlah] + 주세요! Contoh: 아메리카노 한 잔 주세요. (Minta satu Americano.)",
            studyCards = listOf(
                BeginnerStudyCard("주세요", "ju-se-yo", "Please give me...", "Tolong beri saya... / Minta...", "The single most useful polite phrase in Korea!", "Ungkapan sopan paling berguna di seluruh Korea!"),
                BeginnerStudyCard("물 좀 주세요", "mul jom ju-se-yo", "Please give me some water", "Tolong minta air minum", "좀 softens requests politely ('a little / please')", "좀 membuat permintaan terdengar lebih halus"),
                BeginnerStudyCard("아이스 아메리카노 한 잔 주세요", "a-i-seu a-me-ri-ka-no han jan ju-se-yo", "One iced Americano, please", "Minta satu Es Americano", "Korea's #1 favorite cafe order!", "Pesanan kafe paling populer di Korea!"),
                BeginnerStudyCard("이거 얼마예요?", "i-geo eol-ma-ye-yo?", "How much is this?", "Ini berapa harganya?", "이거 = this thing; 얼마예요 = how much is it?", "이거 = benda ini; 얼마예요 = berapa harganya?"),
                BeginnerStudyCard("맛있어요!", "ma-si-sseo-yo!", "It's delicious!", "Enak sekali!", "Say this to the chef — they will beam with joy!", "Ucapkan ke penjual makanan — mereka pasti senang!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Order at a cafe: 'Please give me one cup of coffee'",
                    promptId = "Pesan di kafe: 'Tolong beri saya satu cangkir kopi'",
                    targetTokens = listOf("커피", "한 잔", "주세요"),
                    distractorTokens = listOf("얼마예요"),
                    fullSentenceKo = "커피 한 잔 주세요",
                    romanization = "keo-pi han jan ju-se-yo",
                    hintEn = "Pattern: [Item] + [Count] + 주세요!",
                    hintId = "Pola: [Barang] + [Jumlah] + 주세요!"
                ),
                SentenceBuildExercise(
                    promptEn = "Ask a shopkeeper: 'How much is this bread?'",
                    promptId = "Tanya ke penjual: 'Roti ini berapa harganya?'",
                    targetTokens = listOf("이", "빵", "얼마예요?"),
                    distractorTokens = listOf("주세요"),
                    fullSentenceKo = "이 빵 얼마예요?",
                    romanization = "i ppang eol-ma-ye-yo?",
                    hintEn = "이 (this) + 빵 (bread) + 얼마예요? (how much is it?)",
                    hintId = "이 (ini) + 빵 (roti) + 얼마예요? (berapa harganya?)"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you politely ask for bread (빵) at a bakery?",
                    promptId = "Bagaimana cara sopan memesan roti (빵) di toko roti?",
                    stimulusKo = "빵 주세요",
                    options = listOf("빵 주세요", "빵 안녕하세요", "빵 얼마예요", "빵 죄송합니다"),
                    correctOption = "빵 주세요",
                    explanationEn = "[Item] + 주세요 means 'Please give me [item]' → 빵 주세요!",
                    explanationId = "[Barang] + 주세요 artinya 'Tolong beri [barang]' → 빵 주세요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "You want to know the price of an item in your hand. What do you ask?",
                    promptId = "Kamu ingin tahu harga barang yang kamu pegang. Apa yang kamu tanyakan?",
                    stimulusKo = "이거 얼마예요?",
                    options = listOf("이거 얼마예요?", "이거 맛있어요?", "이거 괜찮아요?", "이거 물이에요?"),
                    correctOption = "이거 얼마예요?",
                    explanationEn = "이거 (this) + 얼마예요? (how much is it?) = How much is this?",
                    explanationId = "이거 (ini) + 얼마예요? (berapa harganya?) = Ini berapa harganya?"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 맛있어요! mean after tasting Korean food?",
                    promptId = "Apa arti 맛있어요! saat mencicipi makanan Korea?",
                    stimulusKo = "맛있어요!",
                    options = listOf("It's delicious! / Enak!", "It's expensive / Mahal", "Excuse me / Permisi", "Goodbye"),
                    correctOption = "It's delicious! / Enak!",
                    explanationEn = "맛있어요 (ma-si-sseo-yo) means 'It is delicious!'.",
                    explanationId = "맛있어요 (ma-si-sseo-yo) berarti 'Enak sekali!'."
                )
            ),
            practiceAction = BeginnerPracticeAction.EVERYDAY_SPEAKING,
            practiceLabelEn = "Roleplay ordering at a Seoul Cafe",
            practiceLabelId = "Simulasi memesan di Kafe Seoul"
        ),

        BeginnerLesson(
            id = "beg_12_everyday_verbs_yo",
            number = 12,
            stageNumber = 4,
            track = SyllabusTrack.BEGINNER,
            estimatedMinutes = 4,
            emblemKo = "해",
            titleEn = "Everyday Verbs & Complete Sentences (~아요/~어요)",
            titleId = "Kata Kerja Harian & Kalimat Lengkap (~아요/~어요)",
            goalEn = "Turn dictionary verbs (-다) into full polite Korean sentences effortlessly.",
            goalId = "Mengubah kata kerja kamus (-다) menjadi kalimat Korea sopan lengkap dengan mudah.",
            overviewEn = "Every Korean verb in the dictionary ends in -다 (가다 = to go, 먹다 = to eat). To speak politely in the present tense, drop -다 and follow 3 tiny rules: ① Last vowel ㅏ/ㅗ → add 아요. ② Any other vowel → add 어요. ③ Ends in 하다 → becomes 해요! Finish this lesson to graduate into the TOPIK I Syllabus!",
            overviewId = "Semua kata kerja Korea di kamus berakhiran -다 (가다 = pergi, 먹다 = makan). Untuk berbicara sopan sehari-hari, buang -다 lalu ikuti 3 rumus singkat: ① Vokal akhir ㅏ/ㅗ → tambah 아요. ② Vokal lain → tambah 어요. ③ Berakhiran 하다 → berubah jadi 해요! Selesai pelajaran ini, kamu resmi siap masuk ke Silabus TOPIK I!",
            keyRuleEn = "3-Line Shortcut: Drop -다 → ㅏ/ㅗ + 아요 (가다→가요), other + 어요 (먹다→먹어요), 하다 → 해요 (공부하다→공부해요)!",
            keyRuleId = "Rumus 3 Baris: Buang -다 → ㅏ/ㅗ + 아요 (가다→가요), lainnya + 어요 (먹다→먹어요), 하다 → 해요 (공부하다→공부해요)!",
            studyCards = listOf(
                BeginnerStudyCard("가다 → 가요", "ga-da → ga-yo", "To go → I go", "Pergi → Saya pergi", "가 already ends in ㅏ, so 가+아요 merges cleanly into 가요!", "가 sudah berakhiran ㅏ, jadi 가+아요 menyatu jadi 가요!"),
                BeginnerStudyCard("보다 → 봐요", "bo-da → bwa-yo", "To see/watch → I watch", "Melihat/menonton → Saya menonton", "보(ㅗ) + 아요 blends smoothly into 봐요!", "보(ㅗ) + 아요 menyatu halus menjadi 봐요!"),
                BeginnerStudyCard("먹다 → 먹어요", "meok-da → meo-geo-yo", "To eat → I eat", "Makan → Saya makan", "Stem vowel is ㅓ (not ㅏ/ㅗ) → attach 어요!", "Vokal dasar adalah ㅓ (bukan ㅏ/ㅗ) → tambah 어요!"),
                BeginnerStudyCard("마시다 → 마셔요", "ma-si-da → ma-syeo-yo", "To drink → I drink", "Minum → Saya minum", "시(ㅣ) + 어요 blends into 셔요 → 마셔요!", "시(ㅣ) + 어요 menyatu jadi 셔요 → 마셔요!"),
                BeginnerStudyCard("공부하다 → 공부해요", "gong-bu-ha-da → gong-bu-hae-yo", "To study → I study", "Belajar → Saya belajar", "ALL ~하다 verbs simply turn into ~해요!", "SEMUA kata kerja ~하다 langsung berubah jadi ~해요!"),
                BeginnerStudyCard("좋아해요", "jo-a-hae-yo", "I like (it)!", "Saya suka!", "한국어를 좋아해요! = I like Korean!", "한국어를 좋아해요! = Saya suka bahasa Korea!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build a complete sentence: 'I study Korean'",
                    promptId = "Susun kalimat lengkap: 'Saya belajar bahasa Korea'",
                    targetTokens = listOf("저는", "한국어를", "공부해요"),
                    distractorTokens = listOf("공부하요"),
                    fullSentenceKo = "저는 한국어를 공부해요",
                    romanization = "jeo-neun han-gu-geo-reul gong-bu-hae-yo",
                    hintEn = "Subject (저는) + Object (한국어를) + Verb (공부해요)!",
                    hintId = "Subjek (저는) + Objek (한국어를) + Kata kerja (공부해요)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build a complete sentence: 'I eat kimbap at home'",
                    promptId = "Susun kalimat lengkap: 'Saya makan kimbap di rumah'",
                    targetTokens = listOf("집에서", "김밥을", "먹어요"),
                    distractorTokens = listOf("먹아요"),
                    fullSentenceKo = "집에서 김밥을 먹어요",
                    romanization = "ji-be-seo gim-ba-beul meo-geo-yo",
                    hintEn = "Place (집에서) → Object (김밥을) → Verb (먹어요)!",
                    hintId = "Tempat (집에서) → Objek (김밥을) → Kata kerja (먹어요)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Turn 공부하다 (to study) into polite everyday Korean:",
                    promptId = "Ubah 공부하다 (belajar) menjadi bentuk sopan sehari-hari:",
                    stimulusKo = "공부하다 → ?",
                    options = listOf("공부해요", "공부하요", "공부허요", "공부다요"),
                    correctOption = "공부해요",
                    explanationEn = "Every ~하다 verb becomes ~해요 → 공부해요 (I study)!",
                    explanationId = "Semua kata kerja ~하다 selalu berubah menjadi ~해요 → 공부해요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you conjugate 먹다 (to eat) into polite present tense?",
                    promptId = "Bagaimana bentuk sopan sehari-hari dari 먹다 (makan)?",
                    stimulusKo = "먹다 → ?",
                    options = listOf("먹어요", "먹아요", "먹해요", "먹예요"),
                    correctOption = "먹어요",
                    explanationEn = "The stem vowel in 먹 is ㅓ (not ㅏ/ㅗ), so attach 어요 → 먹어요.",
                    explanationId = "Vokal dasar pada 먹 adalah ㅓ (bukan ㅏ/ㅗ), jadi tambahkan 어요 → 먹어요."
                ),
                BeginnerMiniCheck(
                    promptEn = "You graduated the Zero-to-Sentence Syllabus! What does 한국어를 공부해요 mean?",
                    promptId = "Kamu lulus Silabus Pemula dari Nol! Apa arti 한국어를 공부해요?",
                    stimulusKo = "한국어를 공부해요",
                    options = listOf("I study Korean / Saya belajar bahasa Korea", "I drink coffee", "I go to school", "Nice to meet you"),
                    correctOption = "I study Korean / Saya belajar bahasa Korea",
                    explanationEn = "한국어를 (Korean) + 공부해요 (study) = I study Korean! You're ready for the TOPIK I Syllabus!",
                    explanationId = "한국어를 (bahasa Korea) + 공부해요 (belajar) = Saya belajar bahasa Korea! Kamu siap masuk Silabus TOPIK I!"
                )
            ),
            practiceAction = BeginnerPracticeAction.BEGINNER_FLASHCARDS,
            practiceLabelEn = "Practice TOPIK 1급 Flashcards",
            practiceLabelId = "Latih Flashcard TOPIK 1급"
        ),

        // =========================================================================
        // TRACK 2: TOPIK I (1-2급) DEDICATED SYLLABUS — 8 LESSONS
        // =========================================================================

        // ==================== STAGE 5: TOPIK I CORE GRAMMAR ====================
        BeginnerLesson(
            id = "t1_01_past_future_tenses",
            number = 1,
            stageNumber = 5,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "았",
            titleEn = "Past (~았/었어요) & Future Plans (~(으)ㄹ 거예요)",
            titleId = "Masa Lalu (~았/었어요) & Rencana (~(으)ㄹ 거예요)",
            goalEn = "Talk naturally about what you did yesterday and what you plan to do tomorrow.",
            goalId = "Bercerita tentang kegiatan kemarin dan rencana besok dengan pola inti TOPIK I.",
            overviewEn = "Welcome to the TOPIK I Syllabus! Every TOPIK I listening and reading dialogue shifts between yesterday (어제) and tomorrow (내일). Past tense simply slips double ㅆ under 아/어/해 → 갔어요, 먹었어요, 공부했어요! Future plans attach -(으)ㄹ 거예요.",
            overviewId = "Selamat datang di Silabus khusus TOPIK I! Hampir semua soal TOPIK I membedakan kemarin (어제) dan besok (내일). Untuk masa lalu, cukup tambah batchim ㅆ pada 아/어/해 → 갔어요, 먹었어요, 공부했어요! Untuk rencana masa depan, pasang -(으)ㄹ 거예요.",
            keyRuleEn = "Time Anchor Rule: 어제 (yesterday) → ~았/었어요. 내일 (tomorrow) → ~ㄹ 거예요 (vowel) / ~을 거예요 (batchim)!",
            keyRuleId = "Rumus Penanda Waktu: 어제 (kemarin) → ~았/었어요. 내일 (besok) → ~ㄹ 거예요 (vokal) / ~을 거예요 (batchim)!",
            studyCards = listOf(
                BeginnerStudyCard("갔어요", "ga-sseo-yo", "Went (가다 → 갔어요)", "Sudah pergi (가다 → 갔어요)", "가 + 았어요 merges into 갔어요", "가 + 았어요 menyatu jadi 갔어요"),
                BeginnerStudyCard("만났어요", "man-na-sseo-yo", "Met (만나다 → 만났어요)", "Sudah bertemu (만나다 → 만났어요)", "어제 친구를 만났어요 (Met a friend yesterday)", "어제 친구를 만났어요 (Kemarin bertemu teman)"),
                BeginnerStudyCard("먹었어요", "meo-geo-sseo-yo", "Ate (먹다 → 먹었어요)", "Sudah makan (먹다 → 먹었어요)", "먹 + 었어요 → 먹었어요", "먹 + 었어요 → 먹었어요"),
                BeginnerStudyCard("갈 거예요", "gal geo-ye-yo", "Will go (가다 → 갈 거예요)", "Akan pergi (가다 → 갈 거예요)", "No batchim on 가 → attach ㄹ 거예요!", "가 tanpa batchim → tempel ㄹ 거예요!"),
                BeginnerStudyCard("먹을 거예요", "meo-geul geo-ye-yo", "Will eat (먹다 → 먹을 거예요)", "Akan makan (먹다 → 먹을 거예요)", "Batchim ㄱ on 먹 → attach 을 거예요!", "먹 ada batchim ㄱ → tempel 을 거예요!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the past-tense sentence: 'Yesterday I met a friend'",
                    promptId = "Susun kalimat masa lalu: 'Kemarin saya bertemu teman'",
                    targetTokens = listOf("어제", "친구를", "만났어요"),
                    distractorTokens = listOf("만날 거예요"),
                    fullSentenceKo = "어제 친구를 만났어요",
                    romanization = "eo-je chin-gu-reul man-na-sseo-yo",
                    hintEn = "Time (어제) → Object (친구를) → Past Verb (만났어요)!",
                    hintId = "Waktu (어제) → Objek (친구를) → Kata kerja lampau (만났어요)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the future plan: 'Tomorrow I will go to Seoul'",
                    promptId = "Susun kalimat rencana: 'Besok saya akan pergi ke Seoul'",
                    targetTokens = listOf("내일", "서울에", "갈 거예요"),
                    distractorTokens = listOf("갔어요"),
                    fullSentenceKo = "내일 서울에 갈 거예요",
                    romanization = "nae-il seo-u-re gal geo-ye-yo",
                    hintEn = "내일 (tomorrow) matches future ~(으)ㄹ 거예요 → 갈 거예요!",
                    hintId = "내일 (besok) berpasangan dengan bentuk depan ~(으)ㄹ 거예요 → 갈 거예요!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Complete the sentence with the matching tense for 어제 (yesterday):",
                    promptId = "Lengkapi kalimat dengan kala yang sesuai untuk 어제 (kemarin):",
                    stimulusKo = "어제 한국 음식을 ___",
                    options = listOf("먹었어요", "먹을 거예요", "먹고 싶어요", "먹으세요"),
                    correctOption = "먹었어요",
                    explanationEn = "어제 (yesterday) signals past tense → 먹었어요 (ate).",
                    explanationId = "어제 (kemarin) menandakan masa lampau → 먹었어요 (sudah makan)."
                ),
                BeginnerMiniCheck(
                    promptEn = "Choose the right future plan ending for 주말에 (this weekend) + 보다 (watch):",
                    promptId = "Pilih bentuk masa depan yang tepat untuk 주말에 (akhir pekan) + 보다 (menonton):",
                    stimulusKo = "주말에 영화를 ___",
                    options = listOf("볼 거예요", "봤어요", "보을 거예요", "보고 있어요"),
                    correctOption = "볼 거예요",
                    explanationEn = "보 has no batchim, so attach ㄹ 거예요 → 볼 거예요 (will watch).",
                    explanationId = "보 tidak ber-batchim, jadi pasang ㄹ 거예요 → 볼 거예요 (akan menonton)."
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you say 'I studied' (past tense of 공부하다)?",
                    promptId = "Bagaimana mengatakan 'Saya sudah belajar' (bentuk lampau dari 공부하다)?",
                    stimulusKo = "공부하다 → ?",
                    options = listOf("공부했어요", "공부하었어요", "공부할 거예요", "공부해요"),
                    correctOption = "공부했어요",
                    explanationEn = "All ~하다 verbs change to ~했어요 in the past tense!",
                    explanationId = "Semua kata kerja ~하다 berubah menjadi ~했어요 pada bentuk lampau!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Drill tenses in Grammar Studio",
            practiceLabelId = "Latih kala di Studio Tata Bahasa",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_02_negation_and_ability",
            number = 2,
            stageNumber = 5,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "못",
            titleEn = "Negation (안 / 못) & Ability (~(으)ㄹ 수 있어요)",
            titleId = "Negasi (안 / 못) & Kemampuan (~(으)ㄹ 수 있어요)",
            goalEn = "Distinguish 'don't' (안) vs 'can't' (못) and express what you can or cannot do.",
            goalId = "Membedakan 'tidak mau/tidak' (안) vs 'tidak bisa' (못) serta menyatakan kemampuan.",
            overviewEn = "A classic TOPIK I favorite! 안 means you CHOOSE not to do something ('I don't drink coffee' = 커피를 안 마셔요). 못 means you CANNOT due to lack of ability or circumstances ('I can't eat spicy food' = 매운 음식을 못 먹어요). For formal 'can / can't', use ~(으)ㄹ 수 있어요 / 없어요.",
            overviewId = "Langganan soal TOPIK I! 안 berarti TIDAK (pilihan sendiri: 커피를 안 마셔요). 못 berarti TIDAK BISA karena keterbatasan atau situasi (매운 음식을 못 먹어요). Untuk menyatakan 'bisa / tidak bisa' secara penuh, pakai ~(으)ㄹ 수 있어요 / 없어요.",
            keyRuleEn = "Golden Contrast: 안 = don't (choice), 못 = can't (ability/situation). Note: for ~하다 nouns, put 안/못 right before 하다 (운동 안 해요)!",
            keyRuleId = "Pembeda Emas: 안 = tidak (pilihan), 못 = tidak bisa (kemampuan/situasi). Pada kata kerja ~하다, taruh 안/못 sebelum 하다 (운동 안 해요)!",
            studyCards = listOf(
                BeginnerStudyCard("안 마셔요", "an ma-syeo-yo", "Don't drink (by choice)", "Tidak minum (pilihan sendiri)", "Place 안 right before the verb!", "Taruh 안 tepat di depan kata kerja!"),
                BeginnerStudyCard("못 먹어요", "mot meo-geo-yo", "Can't eat (unable)", "Tidak bisa makan (tidak sanggup)", "매운 음식을 못 먹어요 = Can't eat spicy food", "매운 음식을 못 먹어요 = Tidak bisa makan pedas"),
                BeginnerStudyCard("읽을 수 있어요", "il-geul su i-sseo-yo", "Can read", "Bisa membaca", "한국어를 읽을 수 있어요 = I can read Korean!", "한국어를 읽을 수 있어요 = Saya bisa membaca bahasa Korea!"),
                BeginnerStudyCard("갈 수 없어요", "gal su eop-sseo-yo", "Cannot go", "Tidak bisa pergi", "오늘은 바빠서 갈 수 없어요 (Busy today so can't go)", "오늘은 바빠서 갈 수 없어요 (Hari ini sibuk jadi tak bisa pergi)"),
                BeginnerStudyCard("가지 않아요", "ga-ji a-na-yo", "Do not go (polite long form)", "Tidak pergi (bentuk panjang sopan)", "Verb stem + 지 않아요 = same meaning as 안 + verb", "Akar kata + 지 않아요 = sama artinya dengan 안 + kata kerja")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the sentence: 'I cannot eat spicy food'",
                    promptId = "Susun kalimat: 'Saya tidak bisa makan makanan pedas'",
                    targetTokens = listOf("매운", "음식을", "못", "먹어요"),
                    distractorTokens = listOf("안"),
                    fullSentenceKo = "매운 음식을 못 먹어요",
                    romanization = "mae-un eum-si-geul mot meo-geo-yo",
                    hintEn = "'Cannot' (inability) uses 못 right before 먹어요!",
                    hintId = "'Tidak bisa' memakai 못 tepat di depan 먹어요!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the proud sentence: 'I can read Korean!'",
                    promptId = "Susun kalimat bangga: 'Saya bisa membaca bahasa Korea!'",
                    targetTokens = listOf("저는", "한국어를", "읽을 수", "있어요"),
                    distractorTokens = listOf("없어요"),
                    fullSentenceKo = "저는 한국어를 읽을 수 있어요",
                    romanization = "jeo-neun han-gu-geo-reul il-geul su i-sseo-yo",
                    hintEn = "Subject (저는) + Object (한국어를) + 읽을 수 + 있어요!",
                    hintId = "Subjek (저는) + Objek (한국어를) + 읽을 수 + 있어요!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "You want to go to the party, but you are sick and UNABLE to go. Which negation fits?",
                    promptId = "Kamu ingin datang ke pesta, tapi sedang sakit sehingga TIDAK BISA pergi. Mana yang tepat?",
                    stimulusKo = "아파서 파티에 ___ 가요",
                    options = listOf("못", "안", "더", "잘"),
                    correctOption = "못",
                    explanationEn = "못 expresses inability due to illness or external obstacles → 못 가요.",
                    explanationId = "못 menyatakan ketidakmampuan karena sakit/halangan → 못 가요."
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 수영할 수 있어요 mean?",
                    promptId = "Apa arti kalimat 수영할 수 있어요?",
                    stimulusKo = "수영할 수 있어요",
                    options = listOf("I can swim / Saya bisa berenang", "I cannot swim", "I want to swim", "I swam yesterday"),
                    correctOption = "I can swim / Saya bisa berenang",
                    explanationEn = "~(으)ㄹ 수 있어요 means 'can / am able to' → I can swim!",
                    explanationId = "~(으)ㄹ 수 있어요 berarti 'bisa / mampu' → Saya bisa berenang!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Where does 안 go with the 하다 verb 운동하다 (to exercise)?",
                    promptId = "Di mana posisi 안 pada kata kerja 하다 seperti 운동하다 (berolahraga)?",
                    stimulusKo = "오늘은 ___",
                    options = listOf("운동 안 해요", "안 운동해요", "운동 해 안요", "운동 못 않아요"),
                    correctOption = "운동 안 해요",
                    explanationEn = "For Noun + 하다 verbs, split before 하다 → 운동 안 해요!",
                    explanationId = "Untuk kata kerja Benda + 하다, selipkan sebelum 하다 → 운동 안 해요!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Practice negation in Grammar Studio",
            practiceLabelId = "Latih negasi di Studio Tata Bahasa",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_03_desire_and_try",
            number = 3,
            stageNumber = 5,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "싶",
            titleEn = "Desires (~고 싶어요), Trying (~아/어 보세요) & Must (~아/어야 해요)",
            titleId = "Keinginan (~고 싶어요), Mencoba (~아/어 보세요) & Harus (~아/어야 해요)",
            goalEn = "Express dreams, recommend trying something, and state obligations clearly.",
            goalId = "Menyatakan keinginan, menyarankan mencoba sesuatu, dan keharusan sehari-hari.",
            overviewEn = "These 3 high-frequency TOPIK I patterns unlock 70% of daily Seoul conversations! Attach ~고 싶어요 to any verb stem to say 'I want to...'. Use ~아/어 보세요 to kindly suggest 'Please try...'. Use ~아/어야 해요 to say 'must / have to'.",
            overviewId = "3 pola super populer di TOPIK I ini membuka 70% percakapan harian di Seoul! Tempel ~고 싶어요 pada akar kata kerja untuk 'ingin...'. Pakai ~아/어 보세요 untuk menyarankan 'cobalah...'. Pakai ~아/어야 해요 untuk 'harus...'.",
            keyRuleEn = "Zero-Exception Stem Pattern: Drop -다 + 고 싶어요! (가다 → 가고 싶어요, 먹다 → 먹고 싶어요 — no batchim check needed!)",
            keyRuleId = "Pola Tanpa Pengecualian: Buang -다 + 고 싶어요! (가다 → 가고 싶어요, 먹다 → 먹고 싶어요 — tidak perlu cek batchim!)",
            studyCards = listOf(
                BeginnerStudyCard("가고 싶어요", "ga-go si-peo-yo", "I want to go", "Saya ingin pergi", "한국에 가고 싶어요 = I want to go to Korea", "한국에 가고 싶어요 = Saya ingin pergi ke Korea"),
                BeginnerStudyCard("먹어 보세요", "meo-geo bo-se-yo", "Please try eating (it)", "Cobalah makan ini", "Polite recommendation in restaurants & shops!", "Saran sopan yang sering terdengar di restoran & toko!"),
                BeginnerStudyCard("입어 보세요", "i-beo bo-se-yo", "Please try wearing (it)", "Silakan coba pakai", "Super frequent in TOPIK I clothing-store dialogues!", "Sangat sering muncul di dialog toko baju TOPIK I!"),
                BeginnerStudyCard("공부해야 해요", "gong-bu-hae-ya hae-yo", "Must / have to study", "Harus belajar", "시험이 있어서 공부해야 해요 (Have an exam so must study)", "시험이 있어서 공부해야 해요 (Ada ujian jadi harus belajar)"),
                BeginnerStudyCard("일찍 자야 해요", "il-jjik ja-ya hae-yo", "Must sleep early", "Harus tidur lebih awal", "자(ㅏ) + 야 해요 → 자야 해요!", "자(ㅏ) + 야 해요 → 자야 해요!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the dream sentence: 'I want to go to Korea'",
                    promptId = "Susun kalimat impian: 'Saya ingin pergi ke Korea'",
                    targetTokens = listOf("저는", "한국에", "가고 싶어요"),
                    distractorTokens = listOf("가야 해요"),
                    fullSentenceKo = "저는 한국에 가고 싶어요",
                    romanization = "jeo-neun han-gu-ge ga-go si-peo-yo",
                    hintEn = "저는 + 한국에 (to Korea) + 가고 싶어요 (want to go)!",
                    hintId = "저는 + 한국에 (ke Korea) + 가고 싶어요 (ingin pergi)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the polite recommendation: 'Please try eating kimchi'",
                    promptId = "Susun saran sopan: 'Cobalah makan kimchi'",
                    targetTokens = listOf("이", "김치를", "먹어 보세요"),
                    distractorTokens = listOf("먹고 싶어요"),
                    fullSentenceKo = "이 김치를 먹어 보세요",
                    romanization = "i gim-chi-reul meo-geo bo-se-yo",
                    hintEn = "이 김치를 (this kimchi) + 먹어 보세요 (please try eating)!",
                    hintId = "이 김치를 (kimchi ini) + 먹어 보세요 (cobalah makan)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "A shop clerk hands you a jacket and says 이 옷을 입어 보세요. What do they mean?",
                    promptId = "Penjaga toko memberikan jaket dan berkata 이 옷을 입어 보세요. Apa artinya?",
                    stimulusKo = "이 옷을 입어 보세요",
                    options = listOf("Please try this outfit on", "Please buy this immediately", "Do not touch this outfit", "I already wore this"),
                    correctOption = "Please try this outfit on",
                    explanationEn = "~아/어 보세요 means 'please try [doing]' → Please try wearing this outfit!",
                    explanationId = "~아/어 보세요 berarti 'silakan coba' → Silakan coba pakai baju ini!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you say 'I have to go to the hospital'?",
                    promptId = "Bagaimana mengatakan 'Saya harus pergi ke rumah sakit'?",
                    stimulusKo = "병원에 ___",
                    options = listOf("가야 해요", "가고 싶어요", "가 보세요", "갈 거예요"),
                    correctOption = "가야 해요",
                    explanationEn = "~아/어야 해요 expresses necessity/obligation ('must / have to') → 가야 해요.",
                    explanationId = "~아/어야 해요 menyatakan keharusan ('harus') → 가야 해요."
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 친구를 만나고 싶어요 mean?",
                    promptId = "Apa arti kalimat 친구를 만나고 싶어요?",
                    stimulusKo = "친구를 만나고 싶어요",
                    options = listOf("I want to meet a friend", "I met a friend", "I must meet a friend", "I cannot meet a friend"),
                    correctOption = "I want to meet a friend",
                    explanationEn = "만나다 + 고 싶어요 = want to meet!",
                    explanationId = "만나다 + 고 싶어요 = ingin bertemu!"
                )
            ),
            practiceAction = BeginnerPracticeAction.EVERYDAY_SPEAKING,
            practiceLabelEn = "Practice speaking desires & requests",
            practiceLabelId = "Latih percakapan keinginan & saran",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_04_connectors_and_reason",
            number = 4,
            stageNumber = 5,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "서",
            titleEn = "Connecting Sentences (~고, ~아/어서, ~지만, ~(으)니까)",
            titleId = "Menghubungkan Kalimat (~고, ~아/어서, ~지만, ~(으)니까)",
            goalEn = "Join short beginner thoughts into fluent two-clause TOPIK I sentences.",
            goalId = "Menggabungkan dua kalimat pendek menjadi satu kalimat TOPIK I yang mengalir alami.",
            overviewEn = "To leap from beginner fragments to TOPIK I fluency, connect two ideas with 4 core bridges: ~고 (and / then), ~아/어서 (because / so), ~지만 (but / however), and ~(으)니까 (since — especially before commands or suggestions like ~세요 / ~읍시다!).",
            overviewId = "Untuk naik kelas ke level TOPIK I, sambungkan dua ide dengan 4 jembatan utama: ~고 (dan / lalu), ~아/어서 (karena / sehingga), ~지만 (tetapi), dan ~(으)니까 (karena — wajib dipakai sebelum ajakan/perintah seperti ~세요 / ~읍시다!).",
            keyRuleEn = "TOPIK I Exam Secret: Never put past tense 았/었 before ~아/어서! Say 비가 와서 (NOT 비가 왔어서). And before ~세요 (command), use ~(으)니까!",
            keyRuleId = "Rahasia Soal TOPIK I: JANGAN pakai 았/었 sebelum ~아/어서! Ucapkan 비가 와서 (BUKAN 비가 왔어서). Sebelum perintah ~세요, gunakan ~(으)니까!",
            studyCards = listOf(
                BeginnerStudyCard("~고", "-go", "And / And then", "Dan / Lalu", "밥을 먹고 학교에 가요 (Eat and then go to school)", "밥을 먹고 학교에 가요 (Makan lalu pergi ke sekolah)"),
                BeginnerStudyCard("~아/어서", "-a/eo-seo", "Because / So (natural cause)", "Karena / Sehingga", "비가 와서 집에 있어요 (Because it's raining, I stay home)", "비가 와서 집에 있어요 (Karena hujan, saya di rumah)"),
                BeginnerStudyCard("~지만", "-ji-man", "But / Although", "Tetapi / Meskipun", "비싸지만 맛있어요 (Expensive, but delicious!)", "비싸지만 맛있어요 (Mahal, tetapi enak!)"),
                BeginnerStudyCard("~(으)니까", "-(eu)ni-kka", "Since / Because (with requests)", "Karena (cocok untuk ajakan/perintah)", "추우니까 옷을 따뜻하게 입으세요 (It's cold, so dress warmly)", "추우니까 옷을 따뜻하게 입으세요 (Karena dingin, pakailah baju hangat)"),
                BeginnerStudyCard("피곤해서 쉬어요", "pi-go-nae-seo swi-eo-yo", "I'm tired so I'm resting", "Karena lelah jadi beristirahat", "피곤하다 → 피곤해서 (always drop 았/었 before 서!)", "피곤하다 → 피곤해서 (selalu tanpa 았/었 sebelum 서!)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the cause sentence: 'Because it rains, I stay at home'",
                    promptId = "Susun kalimat sebab-akibat: 'Karena turun hujan, saya di rumah'",
                    targetTokens = listOf("비가", "와서", "집에", "있어요"),
                    distractorTokens = listOf("왔어서"),
                    fullSentenceKo = "비가 와서 집에 있어요",
                    romanization = "bi-ga wa-seo ji-be i-sseo-yo",
                    hintEn = "비가 와서 (because it rains) + 집에 있어요 (stay at home)!",
                    hintId = "비가 와서 (karena hujan, tanpa 았!) + 집에 있어요 (ada di rumah)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the contrast sentence: 'It is expensive, but delicious'",
                    promptId = "Susun kalimat kontras: 'Mahal, tetapi enak'",
                    targetTokens = listOf("이", "음식은", "비싸지만", "맛있어요"),
                    distractorTokens = listOf("비싸서"),
                    fullSentenceKo = "이 음식은 비싸지만 맛있어요",
                    romanization = "i eum-si-geun bi-ssa-ji-man ma-si-sseo-yo",
                    hintEn = "Use ~지만 (비싸지만) to express 'but'!",
                    hintId = "Gunakan ~지만 (비싸지만) untuk arti 'tetapi'!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Which connector correctly precedes the polite command 우산을 쓰세요 (Please use an umbrella)?",
                    promptId = "Penghubung mana yang tepat sebelum kalimat perintah sopan 우산을 쓰세요 (Pakailah payung)?",
                    stimulusKo = "비가 ___ 우산을 쓰세요",
                    options = listOf("오니까", "와서", "오지만", "오고"),
                    correctOption = "오니까",
                    explanationEn = "Before commands (~세요) or suggestions (~읍시다), TOPIK I requires ~(으)니까 → 오니까!",
                    explanationId = "Sebelum perintah (~세요) atau ajakan, TOPIK I mewajibkan ~(으)니까 → 오니까!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Choose the grammatically correct reason clause:",
                    promptId = "Pilih klausa alasan yang benar secara tata bahasa:",
                    stimulusKo = "어제 너무 ___ 일찍 잤어요",
                    options = listOf("피곤해서", "피곤했어서", "피곤하지만", "피곤하니까요"),
                    correctOption = "피곤해서",
                    explanationEn = "Never attach past tense 었 inside ~아/어서 — the tense sits on the final verb 잤어요!",
                    explanationId = "Jangan pasang bentuk lampau 었 di dalam ~아/어서 — penanda lampau cukup di akhir (잤어요)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 한국어는 어렵지만 재미있어요 mean?",
                    promptId = "Apa arti kalimat 한국어는 어렵지만 재미있어요?",
                    stimulusKo = "한국어는 어렵지만 재미있어요",
                    options = listOf("Korean is hard, but fun", "Korean is hard, so I quit", "Korean is easy and fun", "Korean is fun because it's hard"),
                    correctOption = "Korean is hard, but fun",
                    explanationEn = "~지만 links contrasting ideas: 어렵다 (hard) + 지만 (but) + 재미있다 (fun)!",
                    explanationId = "~지만 menyambung dua hal berlawanan: 어렵다 (sulit) + 지만 (tetapi) + 재미있다 (seru)!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Review connectors in Grammar Studio",
            practiceLabelId = "Ulas penghubung di Studio Tata Bahasa",
            xpReward = TOPIK_I_LESSON_XP
        ),

        // ==================== STAGE 6: TOPIK I PRACTICAL & STRATEGY ====================
        BeginnerLesson(
            id = "t1_05_time_and_schedule",
            number = 5,
            stageNumber = 6,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "시",
            titleEn = "Clock Time, Schedules & Ranges (부터 ~ 까지)",
            titleId = "Jam, Jadwal Harian & Rentang Waktu (부터 ~ 까지)",
            goalEn = "Read Korean clock times and express schedules using 에 and 부터~까지.",
            goalId = "Membaca jam Korea serta menyusun kalimat jadwal dengan 에 dan 부터~까지.",
            overviewEn = "TOPIK I listening loves appointment & store-hour questions! Remember the golden split: HOURS (시) use Native Korean (한 시, 두 시, 세 시), while MINUTES (분) use Sino-Korean (십 분, 삼십 분). For 'from X to Y', use [Start]부터 [End]까지!",
            overviewId = "Listening TOPIK I sangat sering menanyakan jam janji temu & jam buka toko! Ingat pembagian emasnya: JAM (시) pakai Asli Korea (한 시, 두 시, 세 시), sedangkan MENIT (분) pakai Sino-Korea (십 분, 삼십 분). Untuk 'dari X sampai Y', pakai [Mulai]부터 [Akhir]까지!",
            keyRuleEn = "Clock Combo: Native Hour + 시 + Sino Minute + 분! Example: 2:30 = 두 시 삼십 분 (or 두 시 반 = half past 2).",
            keyRuleId = "Kombinasi Jam: Angka Asli + 시 + Angka Sino + 분! Contoh: 2:30 = 두 시 삼십 분 (atau 두 시 반 = setengah tiga).",
            studyCards = listOf(
                BeginnerStudyCard("여덟 시에", "yeo-deol si-e", "At 8 o'clock", "Pada pukul 8", "Attach time particle 에 to specific times!", "Tempel partikel waktu 에 pada jam/hari tertentu!"),
                BeginnerStudyCard("두 시 반", "du si ban", "2:30 (half past two)", "Jam 2:30 (setengah tiga)", "반 means 'half' (30 minutes) — super common in TOPIK I!", "반 berarti 'setengah' (30 menit) — sering keluar di TOPIK I!"),
                BeginnerStudyCard("아홉 시부터 다섯 시까지", "a-hop si-bu-teo da-seot si-kka-ji", "From 9:00 to 5:00", "Dari jam 9 sampai jam 5", "부터 = from (time), 까지 = until/to", "부터 = mulai dari (waktu), 까지 = sampai"),
                BeginnerStudyCard("월요일마다", "wo-ryo-il-ma-da", "Every Monday", "Setiap hari Senin", "Noun + 마다 = every (주말마다 = every weekend)", "Kata benda + 마다 = setiap (주말마다 = setiap akhir pekan)"),
                BeginnerStudyCard("오늘 / 내일 / 어제", "o-neul / nae-il / eo-je", "Today / Tomorrow / Yesterday", "Hari ini / Besok / Kemarin", "NEVER attach 에 after 오늘, 내일, 어제!", "JANGAN tempel 에 setelah 오늘, 내일, 어제!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the routine sentence: 'I wake up at 8 in the morning'",
                    promptId = "Susun kalimat rutinitas: 'Saya bangun jam 8 pagi'",
                    targetTokens = listOf("아침", "여덟 시에", "일어나요"),
                    distractorTokens = listOf("팔 시에"),
                    fullSentenceKo = "아침 여덟 시에 일어나요",
                    romanization = "a-chim yeo-deol si-e i-reo-na-yo",
                    hintEn = "Hours use Native Korean 여덟 시 + 에 (at)!",
                    hintId = "Jam memakai angka Asli Korea 여덟 시 + 에!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the schedule: 'I work from 9 to 5'",
                    promptId = "Susun jadwal: 'Saya bekerja dari jam 9 sampai jam 5'",
                    targetTokens = listOf("아홉 시부터", "다섯 시까지", "일해요"),
                    distractorTokens = listOf("일했어요"),
                    fullSentenceKo = "아홉 시부터 다섯 시까지 일해요",
                    romanization = "a-hop si-bu-teo da-seot si-kka-ji i-rae-yo",
                    hintEn = "[Start]부터 + [End]까지 + 일해요 (work)!",
                    hintId = "[Mulai]부터 + [Selesai]까지 + 일해요 (bekerja)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you read 3:30 in Korean?",
                    promptId = "Bagaimana menyebut pukul 3:30 dalam bahasa Korea?",
                    stimulusKo = "3:30",
                    options = listOf("세 시 삼십 분 (세 시 반)", "삼 시 삼십 분", "세 시 셋 분", "셋 시 삼 분"),
                    correctOption = "세 시 삼십 분 (세 시 반)",
                    explanationEn = "Hour = Native 세 시 (3), Minute = Sino 삼십 분 (30) or 반 (half)!",
                    explanationId = "Jam = Asli 세 시 (3), Menit = Sino 삼십 분 (30) atau 반!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which word NEVER takes the time particle 에?",
                    promptId = "Kata waktu mana yang TIDAK BOLEH memakai partikel 에?",
                    stimulusKo = "___ 친구를 만나요",
                    options = listOf("내일 (Tomorrow)", "토요일 (Saturday)", "세 시 (3 o'clock)", "오후 (Afternoon)"),
                    correctOption = "내일 (Tomorrow)",
                    explanationEn = "어제, 오늘, 내일, 지금 never take 에! Say 내일 친구를 만나요.",
                    explanationId = "어제, 오늘, 내일, 지금 tidak pernah memakai 에! Cukup 내일 친구를 만나요."
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 오전 10시부터 오후 6시까지 mean on a museum sign?",
                    promptId = "Apa arti tanda museum 오전 10시부터 오후 6시까지?",
                    stimulusKo = "오전 10시부터 오후 6시까지",
                    options = listOf("From 10 AM to 6 PM", "Closed on the 10th", "10 minutes to 6", "Opens at 6 PM"),
                    correctOption = "From 10 AM to 6 PM",
                    explanationEn = "오전 = AM, 오후 = PM, 부터 = from, 까지 = until!",
                    explanationId = "오전 = pagi (AM), 오후 = sore (PM), 부터 = dari, 까지 = sampai!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_I_EXAM,
            practiceLabelEn = "Try TOPIK I Practice Questions",
            practiceLabelId = "Coba Latihan Soal TOPIK I",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_06_location_and_directions",
            number = 6,
            stageNumber = 6,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "옆",
            titleEn = "Positions (앞/뒤/옆/안) & Giving Directions (~(으)로)",
            titleId = "Posisi Tempat (앞/뒤/옆/안) & Petunjuk Arah (~(으)로)",
            goalEn = "Describe where places are and follow Seoul subway & street directions.",
            goalId = "Menjelaskan letak tempat dan memahami petunjuk arah jalan/kereta di Seoul.",
            overviewEn = "Every TOPIK I exam asks where a building or object is located! Place the landmark first, then the position word + 에: 은행 옆에 (next to the bank), 책상 위에 (on top of the desk). For directions ('toward right/left'), attach ~(으)로!",
            overviewId = "Setiap ujian TOPIK I selalu menanyakan letak gedung atau benda! Sebutkan patokannya dulu, lalu kata posisi + 에: 은행 옆에 (di samping bank), 책상 위에 (di atas meja). Untuk arah tujuan ('ke kanan/kiri'), pasang ~(으)로!",
            keyRuleEn = "Position Formula: [Landmark] + [앞 front / 뒤 back / 옆 beside / 위 on / 아래 under / 안 inside] + 에 있어요!",
            keyRuleId = "Rumus Lokasi: [Patokan] + [앞 depan / 뒤 belakang / 옆 samping / 위 atas / 아래 bawah / 안 dalam] + 에 있어요!",
            studyCards = listOf(
                BeginnerStudyCard("은행 옆에", "eu-naeng yeo-pe", "Next to the bank", "Di samping bank", "옆 = beside / next to", "옆 = di samping / sebelah"),
                BeginnerStudyCard("학교 앞에", "hak-gyo a-pe", "In front of the school", "Di depan sekolah", "앞 = front; 뒤 = back", "앞 = depan; 뒤 = belakang"),
                BeginnerStudyCard("가방 안에", "ga-bang a-ne", "Inside the bag", "Di dalam tas", "안 = inside; 밖 = outside", "안 = dalam; 밖 = luar"),
                BeginnerStudyCard("오른쪽으로 가세요", "o-reun-jjo-geu-ro ga-se-yo", "Please go to the right", "Silakan belok/pergi ke kanan", "오른쪽 = right; 왼쪽 = left; 똑바로 = straight", "오른쪽 = kanan; 왼쪽 = kiri; 똑바로 = lurus"),
                BeginnerStudyCard("지하철로 가요", "ji-ha-cheol-lo ga-yo", "Go by subway", "Pergi naik kereta bawah tanah", "~(으)로 also marks transport! (Batchim ㄹ takes 로)", "~(으)로 juga menandai transportasi! (Batchim ㄹ cukup pakai 로)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the location sentence: 'There is a cafe next to the bank'",
                    promptId = "Susun kalimat lokasi: 'Di samping bank ada kafe'",
                    targetTokens = listOf("은행", "옆에", "카페가", "있어요"),
                    distractorTokens = listOf("에서"),
                    fullSentenceKo = "은행 옆에 카페가 있어요",
                    romanization = "eu-naeng yeo-pe ka-pe-ga i-sseo-yo",
                    hintEn = "Landmark (은행) + Position (옆에) + Subject (카페가) + 있어요!",
                    hintId = "Patokan (은행) + Posisi (옆에) + Subjek (카페가) + 있어요!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the direction: 'Please go to the right'",
                    promptId = "Susun petunjuk arah: 'Silakan pergi ke sebelah kanan'",
                    targetTokens = listOf("오른쪽으로", "가세요"),
                    distractorTokens = listOf("오른쪽에서"),
                    fullSentenceKo = "오른쪽으로 가세요",
                    romanization = "o-reun-jjo-geu-ro ga-se-yo",
                    hintEn = "Direction uses ~(으)로 → 오른쪽으로 + 가세요!",
                    hintId = "Arah memakai ~(으)로 → 오른쪽으로 + 가세요!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Where is the cat? 고양이가 의자 아래에 있어요.",
                    promptId = "Di manakah kucingnya? 고양이가 의자 아래에 있어요.",
                    stimulusKo = "의자 아래에 있어요",
                    options = listOf("Under the chair / Di bawah kursi", "On top of the chair", "Behind the chair", "Next to the chair"),
                    correctOption = "Under the chair / Di bawah kursi",
                    explanationEn = "의자 = chair, 아래 = under/below → Under the chair!",
                    explanationId = "의자 = kursi, 아래 = bawah → Di bawah kursi!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which particle expresses 'by bus' (버스)?",
                    promptId = "Partikel mana yang berarti 'naik / dengan bus' (버스)?",
                    stimulusKo = "회사에 버스___ 가요",
                    options = listOf("로 (버스로)", "으로 (버스으로)", "을 (버스을)", "이 (버스이)"),
                    correctOption = "로 (버스로)",
                    explanationEn = "버스 ends in a vowel (no batchim), so attach 로 → 버스로 가요!",
                    explanationId = "버스 berakhiran vokal (tanpa batchim), jadi tempel 로 → 버스로 가요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 똑바로 가세요 mean when asking for directions?",
                    promptId = "Apa arti 똑바로 가세요 saat bertanya jalan?",
                    stimulusKo = "똑바로 가세요",
                    options = listOf("Go straight ahead / Jalan lurus", "Turn left", "Turn right", "Go back"),
                    correctOption = "Go straight ahead / Jalan lurus",
                    explanationEn = "똑바로 (straight) + 가세요 (please go) = Go straight ahead!",
                    explanationId = "똑바로 (lurus) + 가세요 (silakan pergi) = Silakan jalan lurus!"
                )
            ),
            practiceAction = BeginnerPracticeAction.EVERYDAY_SPEAKING,
            practiceLabelEn = "Practice directions in Speaking Studio",
            practiceLabelId = "Latih tanya arah di Studio Bicara",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_07_comparison_and_requests",
            number = 7,
            stageNumber = 6,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 4,
            emblemKo = "더",
            titleEn = "Comparisons (~보다 더) & Polite Favors (~아/어 주세요)",
            titleId = "Perbandingan (~보다 더) & Minta Tolong (~아/어 주세요)",
            goalEn = "Compare two things cleanly and ask for help politely in Korean.",
            goalId = "Membandingkan dua hal dengan tepat dan meminta bantuan secara sopan.",
            overviewEn = "Want to say 'Subway is faster than bus' or 'Please speak slowly'? Attach ~보다 ('than') to the thing you compare against, and add 더 ('more') before the adjective! For polite favors ('please do X for me'), combine Verb + 아/어 주세요.",
            overviewId = "Ingin bilang 'Kereta lebih cepat daripada bus' atau 'Tolong bicara pelan'? Tempel ~보다 ('daripada') di belakang pembandingnya, lalu tambahkan 더 ('lebih')! Untuk meminta bantuan sopan ('tolong lakukan...'), gabungkan Kata Kerja + 아/어 주세요.",
            keyRuleEn = "Comparison Formula: [A]이/가 [B]보다 더 [Adjective]! Polite Favor: [Verb ~아/어] + 주세요 (도와주세요 = Please help me!).",
            keyRuleId = "Rumus Perbandingan: [A]이/가 [B]보다 더 [Kata Sifat]! Minta Tolong: [Kata Kerja ~아/어] + 주세요 (도와주세요 = Tolong bantu saya!).",
            studyCards = listOf(
                BeginnerStudyCard("~보다 더", "-bo-da deo", "More ... than ~", "Lebih ... daripada ~", "Attach 보다 directly to the noun being compared against!", "Tempel 보다 langsung di belakang benda pembanding!"),
                BeginnerStudyCard("제일 / 가장", "je-il / ga-jang", "The most / #1", "Paling ...", "한국 음식 중에서 비빔밥을 제일 좋아해요 (Like bibimbap most)", "한국 음식 중에서 비빔밥을 제일 좋아해요 (Paling suka bibimbap)"),
                BeginnerStudyCard("천천히 말해 주세요", "cheon-cheo-ni ma-rae ju-se-yo", "Please speak slowly", "Tolong berbicara pelan-pelan", "Lifesaver phrase for every Korean learner!", "Kalimat penyelamat wajib bagi semua pembelajar Korea!"),
                BeginnerStudyCard("도와주세요", "do-wa-ju-se-yo", "Please help me", "Tolong bantu saya", "돕다 (help) irregular → 도와 + 주세요!", "돕다 (membantu) tidak beraturan → 도와 + 주세요!"),
                BeginnerStudyCard("사진을 찍어 주세요", "sa-ji-neul jji-geo ju-se-yo", "Please take a photo for me", "Tolong fotokan saya", "찍다 → 찍어 + 주세요 (polite favor)", "찍다 → 찍어 + 주세요 (minta tolong sopan)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the comparison: 'The subway is faster than the bus'",
                    promptId = "Susun perbandingan: 'Kereta bawah tanah lebih cepat daripada bus'",
                    targetTokens = listOf("지하철이", "버스보다", "더", "빨라요"),
                    distractorTokens = listOf("버스를"),
                    fullSentenceKo = "지하철이 버스보다 더 빨라요",
                    romanization = "ji-ha-cheo-ri beo-seu-bo-da deo ppal-la-yo",
                    hintEn = "A (지하철이) + B보다 (버스보다) + 더 + 빨라요!",
                    hintId = "A (지하철이) + B보다 (버스보다) + 더 + 빨라요!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the polite favor: 'Please speak slowly'",
                    promptId = "Susun permintaan sopan: 'Tolong berbicara pelan-pelan'",
                    targetTokens = listOf("천천히", "말해", "주세요"),
                    distractorTokens = listOf("말해요"),
                    fullSentenceKo = "천천히 말해 주세요",
                    romanization = "cheon-cheo-ni ma-rae ju-se-yo",
                    hintEn = "천천히 (slowly) + 말해 주세요 (please speak)!",
                    hintId = "천천히 (pelan-pelan) + 말해 주세요 (tolong katakan)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "In '사과가 바나나보다 더 맛있어요', which fruit is MORE delicious?",
                    promptId = "Pada kalimat '사과가 바나나보다 더 맛있어요', buah mana yang LEBIH enak?",
                    stimulusKo = "사과가 바나나보다 더 맛있어요",
                    options = listOf("Apple (사과)", "Banana (바나나)", "Both are equal", "Neither"),
                    correctOption = "Apple (사과)",
                    explanationEn = "바나나보다 means 'than banana', so 사과 (apple) is more delicious!",
                    explanationId = "바나나보다 artinya 'daripada pisang', jadi 사과 (apel) yang lebih enak!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you ask someone to open the window (창문을 열다) for you politely?",
                    promptId = "Bagaimana meminta seseorang membukakan jendela (창문을 열다) dengan sopan?",
                    stimulusKo = "창문을 ___",
                    options = listOf("열어 주세요", "열고 싶어요", "열었어요", "열지 마세요"),
                    correctOption = "열어 주세요",
                    explanationEn = "열다 → 열어 + 주세요 = Please open (it for me)!",
                    explanationId = "열다 → 열어 + 주세요 = Tolong bukakan!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 한국어 중에서 듣기가 제일 쉬워요 mean?",
                    promptId = "Apa arti 한국어 중에서 듣기가 제일 쉬워요?",
                    stimulusKo = "듣기가 제일 쉬워요",
                    options = listOf("Listening is the easiest", "Listening is the hardest", "Reading is the easiest", "Listening is slower"),
                    correctOption = "Listening is the easiest",
                    explanationEn = "듣기 (listening) + 제일 (most) + 쉬워요 (easy)!",
                    explanationId = "듣기 (mendengar) + 제일 (paling) + 쉬워요 (mudah)!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Explore all TOPIK I patterns",
            practiceLabelId = "Jelajahi semua pola TOPIK I",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_08_irregular_verbs",
            number = 8,
            stageNumber = 6,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 5,
            emblemKo = "불",
            titleEn = "Irregular Verbs Made Easy (ㅂ · ㄷ · 르 · 으 · ㄹ)",
            titleId = "Kata Kerja Tidak Beraturan Tanpa Pusing (ㅂ · ㄷ · 르 · 으 · ㄹ)",
            goalEn = "Conjugate everyday irregulars like 춥다→추워요, 듣다→들어요 & 모르다→몰라요 cleanly.",
            goalId = "Mengubah kata kerja tak beraturan wajib seperti 춥다→추워요, 듣다→들어요 & 모르다→몰라요 dengan tepat.",
            overviewEn = "This lesson is the #1 bridge between beginner and intermediate Korean! Most verbs follow standard ~아요/~어요, but 5 tiny sound-smoothing families shift slightly before vowels: ㅂ→우 (춥다→추워요), ㄷ→ㄹ (듣다→들어요), 르→ㄹ+라/러 (모르다→몰라요), ㅡ drops (쓰다→써요), and ㄹ drops before ㄴ/ㅂ/ㅅ (살다→사세요).",
            overviewId = "Pelajaran ini adalah jembatan #1 menuju bahasa Korea tingkat menengah! Sebagian besar kata kerja mengikuti rumus standar ~아요/~어요, tetapi 5 kelompok kecil berubah halus sebelum vokal: ㅂ→우 (춥다→추워요), ㄷ→ㄹ (듣다→들어요), 르→ㄹ+라/러 (모르다→몰라요), ㅡ gugur (쓰다→써요), dan ㄹ gugur sebelum ㄴ/ㅂ/ㅅ (살다→사세요).",
            keyRuleEn = "5 Mini-Shortcuts: ㅂ→워 (추워요/더워요), ㄷ→ㄹ (들어요/걸어요), 르→ㄹ라/ㄹ러 (몰라요/빨라요), ㅡ drops (써요/바빠요), ㄹ drops before ㅅ (사세요)!",
            keyRuleId = "5 Rumus Kilat: ㅂ→워 (추워요/더워요), ㄷ→ㄹ (들어요/걸어요), 르→ㄹ라/ㄹ러 (몰라요/빨라요), ㅡ hilang (써요/바빠요), ㄹ hilang sebelum ㅅ (사세요)!",
            studyCards = listOf(
                BeginnerStudyCard("춥다 → 추워요", "chup-da → chu-wo-yo", "Cold → It is cold (ㅂ irregular)", "Dingin → Dingin sekali (ㅂ tidak beraturan)", "ㅂ turns into 우 + 어요 → 워요! Same for 덥다→더워요, 어렵다→어려워요", "ㅂ berubah jadi 우 + 어요 → 워요! Sama dengan 덥다→더워요, 어렵다→어려워요"),
                BeginnerStudyCard("듣다 → 들어요", "deut-da → deu-reo-yo", "To listen → I listen (ㄷ irregular)", "Mendengarkan → Saya mendengar (ㄷ tidak beraturan)", "ㄷ softens to ㄹ before a vowel → 들어요! Same for 걷다→걸어요", "ㄷ melunak jadi ㄹ sebelum vokal → 들어요! Sama dengan 걷다→걸어요"),
                BeginnerStudyCard("모르다 → 몰라요", "mo-reu-da → mol-la-yo", "Not know → I don't know (르 irregular)", "Tidak tahu → Saya tidak tahu (르 tidak beraturan)", "ㅡ drops & ㄹ doubles: 모르 → 몰라요! Same for 빠르다→빨라요", "ㅡ hilang & ㄹ menjadi ganda: 모르 → 몰라요! Sama dengan 빠르다→빨라요"),
                BeginnerStudyCard("바쁘다 → 바빠요", "ba-ppeu-da → ba-ppa-yo", "Busy → I'm busy (ㅡ drop)", "Sibuk → Saya sibuk (ㅡ gugur)", "Drop ㅡ and look at the previous vowel ㅏ → 바빠요!", "Buang ㅡ lalu ikuti vokal sebelumnya (ㅏ) → 바빠요!"),
                BeginnerStudyCard("쓰다 → 써요", "sseu-da → sseo-yo", "Write / Use → I write (ㅡ drop)", "Menulis / Memakai → Saya menulis", "Single-syllable ㅡ stems always take 어요 → 써요, 커요!", "Akar 1 suku kata berakhiran ㅡ selalu memakai 어요 → 써요, 커요!"),
                BeginnerStudyCard("살다 → 사세요", "sal-da → sa-se-yo", "To live → Live politely (ㄹ drop)", "Tinggal → Tinggal (sopan)", "ㄹ politely steps aside before ㅅ/ㄴ/ㅂ → 사세요, 압니다!", "ㄹ gugur sebelum huruf ㅅ/ㄴ/ㅂ → 사세요, 압니다!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build using ㄷ irregular: 'I listen to Korean music'",
                    promptId = "Susun dengan irregular ㄷ: 'Saya mendengarkan musik Korea'",
                    targetTokens = listOf("저는", "한국 음악을", "들어요"),
                    distractorTokens = listOf("듣어요"),
                    fullSentenceKo = "저는 한국 음악을 들어요",
                    romanization = "jeo-neun han-guk eu-ma-geul deu-reo-yo",
                    hintEn = "듣다 is ㄷ-irregular → 들어요 (NOT 듣어요)!",
                    hintId = "듣다 adalah irregular ㄷ → 들어요 (BUKAN 듣어요)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Listen & assemble (ㅂ irregular): 'Today the weather is very cold'",
                    promptId = "Dengar & susun (irregular ㅂ): 'Hari ini cuacanya sangat dingin'",
                    targetTokens = listOf("오늘", "날씨가", "너무", "추워요"),
                    distractorTokens = listOf("춥어요"),
                    fullSentenceKo = "오늘 날씨가 너무 추워요",
                    romanization = "o-neul nal-ssi-ga neo-mu chu-wo-yo",
                    hintEn = "춥다 is ㅂ-irregular → 추워요!",
                    hintId = "춥다 adalah irregular ㅂ → 추워요!",
                    isAudioFirst = true
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you conjugate 어렵다 (difficult — ㅂ irregular) into polite ~요?",
                    promptId = "Bagaimana bentuk sopan ~요 dari 어렵다 (sulit — irregular ㅂ)?",
                    stimulusKo = "어렵다 → ?",
                    options = listOf("어려워요", "어렵어요", "어려아요", "어렵워요"),
                    correctOption = "어려워요",
                    explanationEn = "Drop ㅂ and add 워요 → 어려워요 (It is difficult)!",
                    explanationId = "Ganti ㅂ menjadi 워요 → 어려워요 (Sulit)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Conjugate 모르다 (not know — 르 irregular) into polite present tense:",
                    promptId = "Ubah 모르다 (tidak tahu — irregular 르) ke bentuk sopan sehari-hari:",
                    stimulusKo = "모르다 → ?",
                    options = listOf("몰라요", "모르어요", "모라요", "몰러요"),
                    correctOption = "몰라요",
                    explanationEn = "르 doubles ㄹ and pairs with ㅏ (from 오 in 모) → 몰라요!",
                    explanationId = "르 menggandakan ㄹ dan ikut vokal ㅗ pada 모 → 몰라요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Complete with ㄷ irregular 걷다 (to walk): '매일 공원에서 ___.'",
                    promptId = "Lengkapi dengan irregular ㄷ 걷다 (berjalan): '매일 공원에서 ___.'",
                    stimulusKo = "매일 공원에서 ___",
                    options = listOf("걸어요", "걷어요", "거어요", "걸아요"),
                    correctOption = "걸어요",
                    explanationEn = "걷다 changes ㄷ → ㄹ before 어요 → 걸어요 (walk)!",
                    explanationId = "걷다 mengubah ㄷ → ㄹ sebelum 어요 → 걸어요 (berjalan)!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Review irregular verbs in Grammar Studio",
            practiceLabelId = "Ulas kata kerja tidak beraturan di Studio Tata Bahasa",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_09_noun_modifiers",
            number = 9,
            stageNumber = 6,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 5,
            emblemKo = "는",
            titleEn = "Noun Modifiers — The Bridge to TOPIK II (~(으)ㄴ / ~는 / ~(으)ㄹ)",
            titleId = "Modifier Kata Benda — Jembatan TOPIK II (~(으)ㄴ / ~는 / ~(으)ㄹ)",
            goalEn = "Describe nouns with verbs & adjectives: 'the food I ate', 'music I like', 'friend I'll meet'.",
            goalId = "Menerangkan kata benda dengan kata kerja: 'makanan yang dimakan', 'lagu yang disukai', 'teman yang akan ditemui'.",
            overviewEn = "In English & Indonesian, descriptions go AFTER the noun ('food THAT I ate yesterday'). In Korean, the entire description goes BEFORE the noun using 3 magic time-tags! Action verbs: Past = ~(으)ㄴ, Present = ~는, Future = ~(으)ㄹ! Adjectives always use ~(으)ㄴ (좋은 날씨, 예쁜 꽃).",
            overviewId = "Dalam bahasa Indonesia, keterangan ditaruh SETELAH kata benda ('makanan YANG saya makan kemarin'). Dalam bahasa Korea, seluruh keterangannya ditaruh SEBELUM kata benda memakai 3 penanda waktu ajaib! Kata kerja: Lampau = ~(으)ㄴ, Sekarang = ~는, Masa Depan = ~(으)ㄹ! Kata sifat selalu pakai ~(으)ㄴ (좋은 날씨).",
            keyRuleEn = "3-Tense Noun Map: Past Verb ~(으)ㄴ (어제 먹은 음식) | Present Verb ~는 (좋아하는 노래) | Future Verb ~(으)ㄹ (내일 만날 친구)!",
            keyRuleId = "Peta 3 Kala Modifier: Lampau ~(으)ㄴ (어제 먹은 음식) | Sekarang ~는 (좋아하는 노래) | Akan Datang ~(으)ㄹ (내일 만날 친구)!",
            studyCards = listOf(
                BeginnerStudyCard("좋아하는 노래", "jo-a-ha-neun no-rae", "A song that I like (Present ~는)", "Lagu yang saya sukai (Sekarang ~는)", "Action verb + 는 + Noun — zero batchim change!", "Kata kerja + 는 + Kata Benda — langsung tempel tanpa ubah batchim!"),
                BeginnerStudyCard("어제 먹은 음식", "eo-je meo-geun eum-sik", "Food I ate yesterday (Past ~은)", "Makanan yang dimakan kemarin (Lampau ~은)", "Past verb with batchim (먹) → attach 은!", "Kata kerja lampau ber-batchim (먹) → pasang 은!"),
                BeginnerStudyCard("어제 본 영화", "eo-je bon yeong-hwa", "Movie I watched yesterday (Past ~ㄴ)", "Film yang ditonton kemarin (Lampau ~ㄴ)", "Past verb without batchim (보) → attach ㄴ (본)!", "Kata kerja lampau tanpa batchim (보) → pasang ㄴ (본)!"),
                BeginnerStudyCard("내일 만날 친구", "nae-il man-nal chin-gu", "Friend I will meet tomorrow (Future ~ㄹ)", "Teman yang akan ditemui besok (Depan ~ㄹ)", "Future modifier ~(으)ㄹ + Noun = 'to [verb] / that will [verb]'", "Modifier masa depan ~(으)ㄹ + Benda = 'yang akan...'"),
                BeginnerStudyCard("좋은 날씨 / 맛있는 빵", "jo-eun nal-ssi / ma-sin-neun ppang", "Nice weather / Delicious bread", "Cuaca bagus / Roti yang enak", "Adjectives use ~(으)ㄴ, EXCEPT 있다/없다 which take ~는 (맛있는)!", "Kata sifat pakai ~(으)ㄴ, KECUALI 있다/없다 yang selalu pakai ~는 (맛있는)!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build using Present Modifier ~는: 'This is the song I like'",
                    promptId = "Susun dengan Modifier ~는: 'Ini adalah lagu yang saya sukai'",
                    targetTokens = listOf("이것은", "제가", "좋아하는", "노래예요"),
                    distractorTokens = listOf("좋아한"),
                    fullSentenceKo = "이것은 제가 좋아하는 노래예요",
                    romanization = "i-geo-seun je-ga jo-a-ha-neun no-rae-ye-yo",
                    hintEn = "Place 제가 좋아하는 (that I like) RIGHT BEFORE 노래예요 (song)!",
                    hintId = "Taruh 제가 좋아하는 (yang saya sukai) TEPAT SEBELUM 노래예요 (lagu)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Listen & assemble (Past Modifier ~(으)ㄴ): 'The food I ate yesterday was delicious'",
                    promptId = "Dengar & susun (Modifier Lampau ~(으)ㄴ): 'Makanan yang saya makan kemarin enak'",
                    targetTokens = listOf("어제", "먹은", "음식이", "맛있었어요"),
                    distractorTokens = listOf("먹는"),
                    fullSentenceKo = "어제 먹은 음식이 맛있었어요",
                    romanization = "eo-je meo-geun eum-si-gi ma-si-sseo-sseo-yo",
                    hintEn = "어제 (yesterday) matches past modifier 먹은 + 음식이!",
                    hintId = "어제 (kemarin) berpasangan dengan modifier lampau 먹은 + 음식이!",
                    isAudioFirst = true
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Choose the right modifier for 'the book I will read tomorrow (내일 읽다)':",
                    promptId = "Pilih modifier yang tepat untuk 'buku yang akan dibaca besok (내일 읽다)':",
                    stimulusKo = "내일 ___ 책",
                    options = listOf("읽을 (내일 읽을 책)", "읽은 (past)", "읽는 (present)", "읽고"),
                    correctOption = "읽을 (내일 읽을 책)",
                    explanationEn = "내일 (tomorrow) signals future modifier ~(으)ㄹ → 읽을 책!",
                    explanationId = "내일 (besok) menandakan modifier masa depan ~(으)ㄹ → 읽을 책!"
                ),
                BeginnerMiniCheck(
                    promptEn = "How do you say 'delicious Korean food' using 맛있다?",
                    promptId = "Bagaimana menyebut 'makanan Korea yang enak' menggunakan 맛있다?",
                    stimulusKo = "___ 한국 음식",
                    options = listOf("맛있는", "맛있은", "맛있ㄴ", "맛있을"),
                    correctOption = "맛있는",
                    explanationEn = "All ~있다 / ~없다 adjectives ALWAYS take ~는 → 맛있는, 재미있는!",
                    explanationId = "Semua kata sifat ~있다 / ~없다 SELALU memakai ~는 → 맛있는, 재미있는!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 저기서 커피를 마시는 사람이 제 친구예요 mean?",
                    promptId = "Apa arti 저기서 커피를 마시는 사람이 제 친구예요?",
                    stimulusKo = "커피를 마시는 사람",
                    options = listOf("The person drinking coffee over there is my friend", "The person who will buy coffee", "My friend hates coffee", "I drank coffee with a stranger"),
                    correctOption = "The person drinking coffee over there is my friend",
                    explanationEn = "마시는 사람 = the person who is drinking (right now)!",
                    explanationId = "마시는 사람 = orang yang sedang minum!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Master modifiers in Grammar Studio",
            practiceLabelId = "Dalami modifier di Studio Tata Bahasa",
            xpReward = TOPIK_I_LESSON_XP
        ),

        BeginnerLesson(
            id = "t1_10_topik1_exam_strategy",
            number = 10,
            stageNumber = 6,
            track = SyllabusTrack.TOPIK_I,
            estimatedMinutes = 5,
            emblemKo = "합",
            titleEn = "TOPIK I Reading & Listening Strategy (1–2급)",
            titleId = "Strategi Jitu Soal Membaca & Mendengar TOPIK I",
            goalEn = "Spot TOPIK I distractors, synonyms, main topics & passage clues fast.",
            goalId = "Mengenali jebakan soal, sinonim, ide pokok & petunjuk cepat ujian TOPIK I.",
            overviewEn = "You've mastered the core TOPIK I grammar — now learn how the official TOPIK I test thinks! Always read the question & 4 options BEFORE listening, watch for negation traps (안/못/없어요), and match paraphrased keywords (e.g., 값이 싸다 ↔ 저렴하다, 자주 ↔ 매일). Finish this lesson to graduate TOPIK I!",
            overviewId = "Kamu sudah menguasai tata bahasa inti TOPIK I — kini pelajari cara kerja soal resminya! Selalu baca pertanyaan & 4 opsi SEBELUM audio diputar, waspadai jebakan negasi (안/못/없어요), dan cocokkan sinonim kata kunci. Tuntaskan pelajaran ini untuk lulus Silabus TOPIK I!",
            keyRuleEn = "TOPIK I Golden Strategy: ① Read choices first → ② Spot Who/Where/When → ③ Check final verb tense & negation (있어요 vs 없어요)!",
            keyRuleId = "Strategi Emas TOPIK I: ① Baca opsi dulu → ② Tangkap Siapa/Di mana/Kapan → ③ Cek kala & negasi di akhir kalimat (있어요 vs 없어요)!",
            studyCards = listOf(
                BeginnerStudyCard("무엇에 대한 이야기입니까?", "mu-eo-se dae-han i-ya-gi-im-ni-kka?", "What is this passage about?", "Tentang apakah teks ini?", "Standard TOPIK I #31–33 prompt — scan repeated topic nouns!", "Soal langganan TOPIK I #31–33 — cari kata benda yang sering diulang!"),
                BeginnerStudyCard("같은 것을 고르십시오", "ga-teun geo-seul go-reu-sip-si-o", "Choose the statement that matches", "Pilih pernyataan yang sesuai isi teks", "Check EVERY detail: time, place, who did what!", "Periksa setiap detail: waktu, tempat, dan siapa pelakunya!"),
                BeginnerStudyCard("알맞은 것을 고르십시오", "al-ma-jeun geo-seul go-reu-sip-si-o", "Choose the most suitable word/phrase", "Pilih kata/frasa yang paling tepat", "Check the sentence BEFORE and AFTER the blank ( ).", "Lihat kalimat SEBELUM dan SESUDAH bagian kosong ( )."),
                BeginnerStudyCard("그래서 / 그러나 / 그리고", "geu-rae-seo / geu-reo-na / geu-ri-go", "So / However / And", "Jadi / Namun / Dan", "Connector words flip or confirm the answer instantly!", "Kata sambung langsung menentukan arah jawaban!"),
                BeginnerStudyCard("무료 / 할인 / 휴무", "mu-ryo / ha-rin / hyu-mu", "Free / Discount / Closed day", "Gratis / Diskon / Hari libur tutup", "Top 3 words on TOPIK I notice & poster questions!", "3 kata paling sering muncul di soal poster & pengumuman TOPIK I!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build a TOPIK I passage sentence: 'Every weekend I exercise at the park'",
                    promptId = "Susun kalimat bacaan TOPIK I: 'Setiap akhir pekan saya olahraga di taman'",
                    targetTokens = listOf("주말마다", "공원에서", "운동해요"),
                    distractorTokens = listOf("공원에"),
                    fullSentenceKo = "주말마다 공원에서 운동해요",
                    romanization = "ju-mal-ma-da gong-wo-ne-seo un-dong-hae-yo",
                    hintEn = "주말마다 (every weekend) + 공원에서 (action place) + 운동해요!",
                    hintId = "주말마다 (setiap akhir pekan) + 공원에서 (tempat aktivitas) + 운동해요!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build a notice sentence: 'You can borrow books at the library'",
                    promptId = "Susun kalimat pengumuman: 'Bisa meminjam buku di perpustakaan'",
                    targetTokens = listOf("도서관에서", "책을", "빌릴 수", "있어요"),
                    distractorTokens = listOf("없어요"),
                    fullSentenceKo = "도서관에서 책을 빌릴 수 있어요",
                    romanization = "do-seo-gwa-ne-seo chae-geul bil-lil su i-sseo-yo",
                    hintEn = "도서관에서 (at the library) + 책을 (books) + 빌릴 수 있어요 (can borrow)!",
                    hintId = "도서관에서 (di perpustakaan) + 책을 (buku) + 빌릴 수 있어요 (bisa meminjam)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "A poster says: '매주 월요일 휴무 · 입장료 무료'. Which statement matches?",
                    promptId = "Sebuah poster bertuliskan: '매주 월요일 휴무 · 입장료 무료'. Pernyataan mana yang benar?",
                    stimulusKo = "월요일 휴무 · 입장료 무료",
                    options = listOf("Closed Mondays & free entry", "Open Mondays & 50% off", "Free on Mondays only", "Paid tickets required"),
                    correctOption = "Closed Mondays & free entry",
                    explanationEn = "휴무 = closed day, 입장료 무료 = free admission!",
                    explanationId = "휴무 = tutup/libur, 입장료 무료 = tiket masuk gratis!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Fill in the blank: 저는 운동을 좋아해요. (   ) 매일 공원에서 달려요.",
                    promptId = "Isi titik-titik: 저는 운동을 좋아해요. (   ) 매일 공원에서 달려요.",
                    stimulusKo = "운동을 좋아해요. (   ) 매일 달려요.",
                    options = listOf("그래서 (Therefore/So)", "그러나 (However)", "하지만 (But)", "아니면 (Or)"),
                    correctOption = "그래서 (Therefore/So)",
                    explanationEn = "Liking exercise is the CAUSE of running daily → 그래서 (so/therefore)!",
                    explanationId = "Suka olahraga adalah SEBAB dari berlari setiap hari → 그래서 (oleh karena itu)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What is the topic of: '사과와 포도가 달아요. 여름에는 수박이 맛있어요.'?",
                    promptId = "Apa topik utama teks: '사과와 포도가 달아요. 여름에는 수박이 맛있어요.'?",
                    stimulusKo = "사과 · 포도 · 수박",
                    options = listOf("과일 (Fruit / Buah-buahan)", "날씨 (Weather)", "교통 (Transport)", "직업 (Jobs)"),
                    correctOption = "과일 (Fruit / Buah-buahan)",
                    explanationEn = "사과 (apple), 포도 (grape), 수박 (watermelon) all point to 과일 (fruit)!",
                    explanationId = "사과 (apel), 포도 (anggur), 수박 (semangka) semuanya merujuk pada 과일 (buah-buahan)!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_I_EXAM,
            practiceLabelEn = "Take Authentic TOPIK I Mock Exam",
            practiceLabelId = "Mulai Simulasi Ujian TOPIK I",
            xpReward = TOPIK_I_LESSON_XP
        ),

        // =========================================================================
        // TRACK 3: TOPIK II (3-6급) DEDICATED SYLLABUS — 8 LESSONS
        // =========================================================================

        // ==================== STAGE 7: INTERMEDIATE NUANCE & INDIRECT SPEECH ====================
        BeginnerLesson(
            id = "t2_01_nuanced_cause_and_background",
            number = 1,
            stageNumber = 7,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "때",
            titleEn = "Formal Cause & Background (~기 때문에, ~는 바람에, ~(으)ㄴ/는데)",
            titleId = "Sebab Formal & Latar Belakang (~기 때문에, ~는 바람에, ~(으)ㄴ/는데)",
            goalEn = "Distinguish neutral cause, unexpected negative cause, and contextual background.",
            goalId = "Membedakan sebab formal netral, sebab tak terduga negatif, dan latar belakang konteks.",
            overviewEn = "Welcome to the TOPIK II Syllabus (3–6급)! Unlike TOPIK I's simple ~아/어서, TOPIK II tests exact nuance: ~기 때문에 gives a clear/formal reason, ~는 바람에 explains an UNEXPECTED negative result (e.g., sudden rain or oversleeping), and ~(으)ㄴ/는데 smoothly sets background context.",
            overviewId = "Selamat datang di Silabus khusus TOPIK II (3–6급)! Di sini ketepatan nuansa diuji: ~기 때문에 untuk alasan jelas/formal, ~는 바람에 khusus akibat NEGATIF tak terduga (misal ketiduran/hujan deras), dan ~(으)ㄴ/는데 untuk membuka latar konteks.",
            keyRuleEn = "Nuance Check: ~는 바람에 ONLY takes unexpected/negative outcomes in past tense! Use ~덕분에 for positive credit ('thanks to').",
            keyRuleId = "Cek Nuansa: ~는 바람에 HANYA untuk akibat negatif/tak terduga (lampau)! Untuk hasil positif ('berkat'), pakai ~덕분에!",
            studyCards = listOf(
                BeginnerStudyCard("~기 때문에", "-gi ttae-mu-ne", "Because / Due to (formal)", "Karena / Disebabkan oleh (formal)", "Attaches to verbs/adjectives; Noun + 때문에", "Dipasang di kata kerja/sifat; Kata benda + 때문에"),
                BeginnerStudyCard("~는 바람에", "-neun ba-ra-me", "Because (unexpected mishap)", "Gara-gara / Akibat tak terduga", "늦잠을 자는 바람에 지각했어요 (Overslept so was late)", "늦잠을 자는 바람에 지각했어요 (Gara-gara kesiangan jadi telat)"),
                BeginnerStudyCard("~덕분에", "-deok-bu-ne", "Thanks to (positive result)", "Berkat (hasil positif)", "친구 덕분에 합격했어요 (Passed thanks to my friend)", "친구 덕분에 합격했어요 (Lulus berkat teman)"),
                BeginnerStudyCard("~(으)ㄴ/는데", "-(eu)n/neun-de", "Background / Soft contrast", "Latar belakang / 'Padahal / Nah...'", "날씨가 추운데 따뜻하게 입으세요", "날씨가 추운데 따뜻하게 입으세요"),
                BeginnerStudyCard("길이 막히다", "gi-ri ma-ki-da", "Traffic is jammed", "Jalan macet", "눈이 오는 바람에 길이 막혔어요", "눈이 오는 바람에 길이 막혔어요")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the TOPIK II mishap sentence: 'Because it snowed heavily, the road was jammed'",
                    promptId = "Susun kalimat TOPIK II: 'Gara-gara turun salju lebat, jalanan macet'",
                    targetTokens = listOf("눈이", "많이 오는", "바람에", "길이 막혔어요"),
                    distractorTokens = listOf("덕분에"),
                    fullSentenceKo = "눈이 많이 오는 바람에 길이 막혔어요",
                    romanization = "nu-ni ma-ni o-neun ba-ra-me gi-ri ma-kyeo-sseo-yo",
                    hintEn = "Traffic jam is an unexpected negative result → ~는 바람에!",
                    hintId = "Macet adalah akibat negatif tak terduga → pakai ~는 바람에!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the formal reason: 'Because time was short, I hurried'",
                    promptId = "Susun alasan formal: 'Karena waktu kurang, saya bergegas'",
                    targetTokens = listOf("시간이", "부족하기", "때문에", "서둘렀어요"),
                    distractorTokens = listOf("바람에"),
                    fullSentenceKo = "시간이 부족하기 때문에 서둘렀어요",
                    romanization = "si-ga-ni bu-jo-ka-gi ttae-mu-ne seo-dul-leo-sseo-yo",
                    hintEn = "부족하다 + 기 때문에 = formal reason!",
                    hintId = "부족하다 + 기 때문에 = sebab formal!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Choose the exact connector for a POSITIVE credit: '선생님 ___ 한국어가 많이 늘었어요.'",
                    promptId = "Pilih penghubung tepat untuk hasil POSITIF: '선생님 ___ 한국어가 많이 늘었어요.'",
                    stimulusKo = "선생님 ___ 한국어가 늘었어요",
                    options = listOf("덕분에 (Thanks to)", "바람에 (Due to mishap)", "탓에 (Fault of)", "대신에 (Instead of)"),
                    correctOption = "덕분에 (Thanks to)",
                    explanationEn = "덕분에 expresses gratitude for a positive outcome!",
                    explanationId = "덕분에 dipakai khusus untuk hasil positif ('berkat')!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which ending matches an unexpected mishap: '버스를 놓치는 ___ 시험에 늦었어요'?",
                    promptId = "Akhiran mana cocok untuk insiden tak terduga: '버스를 놓치는 ___ 시험에 늦었어요'?",
                    stimulusKo = "버스를 놓치는 ___ 늦었어요",
                    options = listOf("바람에", "덕분에", "반면에", "위해서"),
                    correctOption = "바람에",
                    explanationEn = "Missing the bus caused being late → ~는 바람에!",
                    explanationId = "Ketinggalan bus menyebabkan terlambat → ~는 바람에!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which sentence is ungrammatical with ~는 바람에?",
                    promptId = "Kalimat mana yang SALAH jika memakai ~는 바람에?",
                    stimulusKo = "~는 바람에 제약",
                    options = listOf("열심히 공부하는 바람에 장학금을 받았어요", "비가 오는 바람에 소풍이 취소됐어요", "감기에 걸리는 바람에 못 갔어요", "지갑을 잃어버리는 바람에 당황했어요"),
                    correctOption = "열심히 공부하는 바람에 장학금을 받았어요",
                    explanationEn = "Winning a scholarship is positive — use 덕분에 or 기 때문에 instead of 바람에!",
                    explanationId = "Mendapat beasiswa adalah hal positif — tidak boleh pakai ~는 바람에!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_II_FLASHCARDS,
            practiceLabelEn = "Study TOPIK II (3–4급) Flashcards",
            practiceLabelId = "Latih Flashcard TOPIK II (3–4급)",
            xpReward = TOPIK_II_LESSON_XP
        ),

        BeginnerLesson(
            id = "t2_02_experience_condition_change",
            number = 2,
            stageNumber = 7,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "적",
            titleEn = "Experience (~(으)ㄴ 적이 있다) & Change (~게 되다)",
            titleId = "Pengalaman (~(으)ㄴ 적이 있다) & Perubahan (~게 되다)",
            goalEn = "Share life experiences and describe how situations naturally evolved.",
            goalId = "Menceritakan pengalaman hidup dan perubahan keadaan yang terjadi secara alami.",
            overviewEn = "In TOPIK II interviews and reading passages, ~(으)ㄴ 적이 있다/없다 expresses whether you have EVER experienced something. Meanwhile, ~게 되다 expresses a change of state ('ended up doing / came to be') caused by external circumstances or steady growth!",
            overviewId = "Dalam bacaan dan wawancara TOPIK II, ~(으)ㄴ 적이 있다/없다 menyatakan 'pernah / belum pernah'. Sementara ~게 되다 menyatakan perubahan keadaan ('akhirnya menjadi / jadi bisa') karena situasi atau proses belajar!",
            keyRuleEn = "Natural Change: 잘하다 → 잘하게 되었어요 (came to be good at it!). Ever tried: 가 보다 → 가 본 적이 있어요 (have been there!).",
            keyRuleId = "Perubahan Alami: 잘하다 → 잘하게 되었어요 (jadi mahir!). Pernah mencoba: 가 보다 → 가 본 적이 있어요 (pernah pergi ke sana!).",
            studyCards = listOf(
                BeginnerStudyCard("~(으)ㄴ 적이 있어요", "-(eu)n jeo-gi i-sseo-yo", "Have experienced ~ before", "Pernah melakukan ~", "제주도에 가 본 적이 있어요 (Have been to Jeju)", "제주도에 가 본 적이 있어요 (Pernah pergi ke Jeju)"),
                BeginnerStudyCard("~(으)ㄴ 적이 없어요", "-(eu)n jeo-gi eop-sseo-yo", "Have never ~", "Belum pernah ~", "눈을 본 적이 없어요 (Have never seen snow)", "눈을 본 적이 없어요 (Belum pernah melihat salju)"),
                BeginnerStudyCard("~게 되다", "-ge doe-da", "Come to / End up ~ing", "Akhirnya menjadi / Jadi ~", "한국에서 일하게 되었어요 (Ended up working in Korea)", "한국에서 일하게 되었어요 (Akhirnya bekerja di Korea)"),
                BeginnerStudyCard("~(으)면", "-(eu)myeon", "If / When (condition)", "Jika / Apabila", "꾸준히 연습하면 실력이 늘어요", "꾸준히 연습하면 실력이 늘어요"),
                BeginnerStudyCard("~기 전에 / ~(으)ㄴ 후에", "-gi jeo-ne / -(eu)n hu-e", "Before ~ing / After ~ing", "Sebelum ~ / Setelah ~", "자기 전에 복습해요 (Review before sleeping)", "자기 전에 복습해요 (Mengulas sebelum tidur)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build: 'I have been to Jeju Island before'",
                    promptId = "Susun: 'Saya pernah pergi ke Pulau Jeju'",
                    targetTokens = listOf("제주도에", "가 본", "적이", "있어요"),
                    distractorTokens = listOf("갈"),
                    fullSentenceKo = "제주도에 가 본 적이 있어요",
                    romanization = "je-ju-do-e ga bon jeo-gi i-sseo-yo",
                    hintEn = "Past modifier 가 본 + 적이 있어요 (have experienced)!",
                    hintId = "Modifier lampau 가 본 + 적이 있어요 (pernah)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build: 'Thanks to practice, I came to speak Korean well'",
                    promptId = "Susun: 'Berkat latihan, saya jadi pandai berbahasa Korea'",
                    targetTokens = listOf("연습 덕분에", "한국어를", "잘하게", "되었어요"),
                    distractorTokens = listOf("바람에"),
                    fullSentenceKo = "연습 덕분에 한국어를 잘하게 되었어요",
                    romanization = "yeon-seup deok-bu-ne han-gu-geo-reul ja-ra-ge doe-eo-sseo-yo",
                    hintEn = "연습 덕분에 (thanks to practice) + 한국어를 + 잘하게 되었어요!",
                    hintId = "연습 덕분에 (berkat latihan) + 한국어를 + 잘하게 되었어요!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "How do you say 'I have never eaten kimchi'?",
                    promptId = "Bagaimana mengatakan 'Saya belum pernah makan kimchi'?",
                    stimulusKo = "김치를 ___ 없어요",
                    options = listOf("먹은 적이", "먹을 적이", "먹는 적이", "먹기 적이"),
                    correctOption = "먹은 적이",
                    explanationEn = "Experience ALWAYS uses the past modifier ~(으)ㄴ + 적이 있다/없다 → 먹은 적이 없어요!",
                    explanationId = "Pengalaman SELALU pakai modifier lampau ~(으)ㄴ + 적이 있다/없다 → 먹은 적이 없어요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What nuance does 서울로 이사하게 되었어요 carry?",
                    promptId = "Nuansa apa yang terkandung dalam 서울로 이사하게 되었어요?",
                    stimulusKo = "서울로 이사하게 되었어요",
                    options = listOf("Circumstances led me to move to Seoul", "I refused to move to Seoul", "I moved to Seoul by mistake", "I want to leave Seoul"),
                    correctOption = "Circumstances led me to move to Seoul",
                    explanationEn = "~게 되다 softly shows a change brought about by life/job circumstances.",
                    explanationId = "~게 되다 menggambarkan perubahan situasi hidup/pekerjaan secara halus."
                ),
                BeginnerMiniCheck(
                    promptEn = "Choose the right 'after eating' structure:",
                    promptId = "Pilih struktur yang benar untuk 'setelah makan':",
                    stimulusKo = "밥을 ___ 약을 드세요",
                    options = listOf("먹은 후에", "먹기 후에", "먹을 후에", "먹는 전에"),
                    correctOption = "먹은 후에",
                    explanationEn = "Before = ~기 전에; After = ~(으)ㄴ 후에 → 먹은 후에!",
                    explanationId = "Sebelum = ~기 전에; Setelah = ~(으)ㄴ 후에 → 먹은 후에!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Practice TOPIK II grammar patterns",
            practiceLabelId = "Latih tata bahasa TOPIK II",
            xpReward = TOPIK_II_LESSON_XP
        ),

        BeginnerLesson(
            id = "t2_03_indirect_quotation",
            number = 3,
            stageNumber = 7,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "다",
            titleEn = "Indirect Quotations (~다고 / ~냐고 / ~(으)라고 / ~자고 하다)",
            titleId = "Kalimat Tidak Langsung (~다고 / ~냐고 / ~(으)라고 / ~자고 하다)",
            goalEn = "Report statements, questions, commands & suggestions like a native speaker.",
            goalId = "Menyampaikan kembali pernyataan, pertanyaan, perintah & ajakan orang lain secara alami.",
            overviewEn = "Indirect quotation is the #1 bridge into true TOPIK II fluency — and it follows a crystal-clear 4-part map! Statement → ~(ㄴ/는)다고 하다. Question → ~냐고 하다. Command → ~(으)라고 하다. Suggestion ('let's') → ~자고 하다!",
            overviewId = "Kalimat tidak langsung adalah kunci utama kelancaran TOPIK II — polanya sangat rapi dibagi 4! Pernyataan → ~(ㄴ/는)다고 하다. Pertanyaan → ~냐고 하다. Perintah → ~(으)라고 하다. Ajakan ('ayo') → ~자고 하다!",
            keyRuleEn = "4-Way Quote Map: Tell = ~다고 | Ask = ~냐고 | Order = ~(으)라고 | Suggest = ~자고 (+ 하다/말하다)!",
            keyRuleId = "Peta 4 Kutipan: Berita = ~다고 | Tanya = ~냐고 | Perintah = ~(으)라고 | Ajak = ~자고 (+ 하다/말하다)!",
            studyCards = listOf(
                BeginnerStudyCard("~다고 하다", "-da-go ha-da", "Say that ... (statement)", "Mengatakan bahwa ... (pernyataan)", "내일 시험이 있다고 했어요 (Said there is an exam tomorrow)", "내일 시험이 있다고 했어요 (Bilang besok ada ujian)"),
                BeginnerStudyCard("~냐고 하다", "-nya-go ha-da", "Ask whether / if ...", "Bertanya apakah ...", "어디에 가냐고 물었어요 (Asked where I'm going)", "어디에 가냐고 물었어요 (Bertanya mau pergi ke mana)"),
                BeginnerStudyCard("~(으)라고 하다", "-(eu)ra-go ha-da", "Tell someone to ... (command)", "Menyuruh agar ... (perintah)", "의사가 푹 쉬라고 했어요 (Doctor told me to rest well)", "의사가 푹 쉬라고 했어요 (Dokter menyuruh istirahat penuh)"),
                BeginnerStudyCard("~자고 하다", "-ja-go ha-da", "Suggest 'let's ...'", "Mengajak 'ayo ...'", "친구가 밥을 먹자고 했어요 (Friend suggested eating together)", "친구가 밥을 먹자고 했어요 (Teman mengajak makan bersama)"),
                BeginnerStudyCard("~대요 / ~재요", "-dae-yo / -jae-yo", "Short spoken quotes!", "Bentuk singkat lisan!", "비가 온대요 = 'I heard it's raining!' (TOPIK II Listening star!)", "비가 온대요 = 'Katanya hujan!' (Sering keluar di Listening TOPIK II!)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Report the announcement: 'The teacher said there is an exam tomorrow'",
                    promptId = "Laporkan pengumuman: 'Guru berkata besok ada ujian'",
                    targetTokens = listOf("선생님께서", "내일 시험이", "있다고", "하셨어요"),
                    distractorTokens = listOf("있자고"),
                    fullSentenceKo = "선생님께서 내일 시험이 있다고 하셨어요",
                    romanization = "seon-saeng-nim-kke-seo nae-il si-heo-mi it-da-go ha-syeo-sseo-yo",
                    hintEn = "Statement quote uses ~다고 → 있다고 하셨어요!",
                    hintId = "Kutipan pernyataan memakai ~다고 → 있다고 하셨어요!"
                ),
                SentenceBuildExercise(
                    promptEn = "Report the invitation: 'My friend suggested eating lunch together'",
                    promptId = "Laporkan ajakan: 'Teman mengajak makan siang bersama'",
                    targetTokens = listOf("친구가", "같이 점심을", "먹자고", "했어요"),
                    distractorTokens = listOf("먹냐고"),
                    fullSentenceKo = "친구가 같이 점심을 먹자고 했어요",
                    romanization = "chin-gu-ga ga-chi jeom-si-meul meok-ja-go hae-sseo-yo",
                    hintEn = "Suggestion ('let's eat') uses ~자고 → 먹자고 했어요!",
                    hintId = "Ajakan ('ayo makan') memakai ~자고 → 먹자고 했어요!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Report the doctor's command: '약을 드세요' (Please take medicine) →",
                    promptId = "Ubah perintah dokter '약을 드세요' (Minumlah obat) ke kalimat tidak langsung →",
                    stimulusKo = "의사 선생님께서 약을 ___ 하셨어요",
                    options = listOf("먹으라고 (command)", "먹자고 (let's)", "먹냐고 (question)", "먹는다고 (statement)"),
                    correctOption = "먹으라고 (command)",
                    explanationEn = "Indirect commands use ~(으)라고 하다 → 먹으라고 하셨어요!",
                    explanationId = "Kutipan perintah memakai ~(으)라고 하다 → 먹으라고 하셨어요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 친구가 내일 눈이 온대요 mean in everyday conversation?",
                    promptId = "Apa arti 친구가 내일 눈이 온대요 dalam percakapan sehari-hari?",
                    stimulusKo = "내일 눈이 온대요",
                    options = listOf("My friend says it will snow tomorrow", "My friend asked if it will snow", "Let's watch snow tomorrow", "Tell my friend it snowed"),
                    correctOption = "My friend says it will snow tomorrow",
                    explanationEn = "~ㄴ/는대요 is the natural spoken contraction of ~ㄴ/는다고 해요!",
                    explanationId = "~ㄴ/는대요 adalah singkatan lisan alami dari ~ㄴ/는다고 해요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which quotation ending reports a QUESTION?",
                    promptId = "Akhiran kutipan mana yang dipakai untuk melaporkan PERTANYAAN?",
                    stimulusKo = "몇 시에 도착하___ 물었어요",
                    options = listOf("냐고 (도착하냐고)", "자고 (도착하자고)", "라고 (도착하라고)", "다고 (도착하다고)"),
                    correctOption = "냐고 (도착하냐고)",
                    explanationEn = "Reported questions always use ~냐고 (묻다/하다)!",
                    explanationId = "Kalimat tanya tidak langsung selalu memakai ~냐고 (묻다/하다)!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_II_FLASHCARDS,
            practiceLabelEn = "Review TOPIK II vocabulary",
            practiceLabelId = "Ulas kosakata TOPIK II",
            xpReward = TOPIK_II_LESSON_XP
        ),

        BeginnerLesson(
            id = "t2_04_contrast_concession",
            number = 4,
            stageNumber = 7,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "반",
            titleEn = "Contrast (~는 반면에), Concession (~아/어도) & Passive",
            titleId = "Kontras (~는 반면에), Konsesi (~아/어도) & Bentuk Pasif",
            goalEn = "Build balanced analytical sentences that shine in TOPIK II reading & essays.",
            goalId = "Menyusun kalimat analitis seimbang yang sering muncul di bacaan & esai TOPIK II.",
            overviewEn = "TOPIK II passages love comparing two sides of an issue! Use ~는 반면에 ('on the other hand / whereas') to contrast two facts clearly. Use ~아/어도 ('even if / no matter how') for strong determination, and spot common Korean passives (열리다, 보이다, 들리다)!",
            overviewId = "Bacaan TOPIK II sangat suka membandingkan dua sisi isu! Pakai ~는 반면에 ('sebaliknya / sedangkan') untuk kontras objektif. Pakai ~아/어도 ('meskipun / seberapa pun') untuk keteguhan, serta kenali kata kerja pasif penting (열리다, 보이다, 들리다)!",
            keyRuleEn = "Balanced Writing Trick: [Side A] ~는 반면에, [Side B] ~다! Concession: 아무리 [Adj/Verb] + 아/어도 (No matter how..., still...).",
            keyRuleId = "Trik Esai Seimbang: [Sisi A] ~는 반면에, [Sisi B] ~다! Konsesi: 아무리 [Kata Sifat/Kerja] + 아/어도 (Sebagaimana pun..., tetap...).",
            studyCards = listOf(
                BeginnerStudyCard("~는 반면에", "-neun ban-myeo-ne", "Whereas / On the other hand", "Sedangkan / Sebaliknya", "여름은 더운 반면에 겨울은 추워요", "여름은 더운 반면에 겨울은 추워요"),
                BeginnerStudyCard("아무리 ~아/어도", "a-mu-ri -a/eo-do", "No matter how ... / Even if", "Seberapa pun ... / Meskipun", "아무리 바빠도 아침을 먹어요", "아무리 바빠도 아침을 먹어요"),
                BeginnerStudyCard("~(으)ㄹ 뿐만 아니라", "-(eu)l ppun-man a-ni-ra", "Not only A, but also B", "Tidak hanya A, tetapi juga B", "Power connector for TOPIK II essays!", "Penghubung ampuh untuk menaikkan skor esai TOPIK II!"),
                BeginnerStudyCard("열리다 / 닫히다", "yeol-li-da / da-chi-da", "To be opened / To be closed", "Terbuka / Tertutup (pasif)", "Passive verbs take subject particle 이/가 (문이 열려요)!", "Kata kerja pasif memakai subjek 이/가 (문이 열려요)!"),
                BeginnerStudyCard("보이다 / 들리다", "bo-i-da / deul-li-da", "To be visible / To be heard", "Terlihat / Terdengar (pasif)", "산이 잘 보여요 (The mountain is clearly visible)", "산이 잘 보여요 (Gunungnya terlihat jelas)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build the contrast: 'Whereas summer is hot, winter is cold'",
                    promptId = "Susun kalimat kontras: 'Sedangkan musim panas panas, musim dingin dingin'",
                    targetTokens = listOf("여름은", "더운 반면에", "겨울은", "추워요"),
                    distractorTokens = listOf("더워서"),
                    fullSentenceKo = "여름은 더운 반면에 겨울은 추워요",
                    romanization = "yeo-reu-meun deo-un ban-myeo-ne gyeo-u-reun chu-wo-yo",
                    hintEn = "더운 반면에 (whereas it is hot) neatly contrasts summer & winter!",
                    hintId = "더운 반면에 (sedangkan panas) mengontraskan musim panas & dingin!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build the concession: 'No matter how busy I am, I always eat breakfast'",
                    promptId = "Susun konsesi: 'Sesibuk apa pun, saya pasti sarapan'",
                    targetTokens = listOf("아무리", "바빠도", "아침을", "꼭 먹어요"),
                    distractorTokens = listOf("바빠서"),
                    fullSentenceKo = "아무리 바빠도 아침을 꼭 먹어요",
                    romanization = "a-mu-ri ba-ppa-do a-chi-meul kkok meo-geo-yo",
                    hintEn = "아무리 always pairs with ~아/어도 → 바빠도!",
                    hintId = "아무리 selalu berpasangan dengan ~아/어도 → 바빠도!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Fill in the analytical contrast: '도시는 편리한 (   ) 공기가 맑지 않다.'",
                    promptId = "Lengkapi kontras analitis: '도시는 편리한 (   ) 공기가 맑지 않다.'",
                    stimulusKo = "편리한 (   ) 공기가 맑지 않다",
                    options = listOf("반면에 (whereas)", "덕분에 (thanks to)", "바람에 (because of)", "후에 (after)"),
                    correctOption = "반면에 (whereas)",
                    explanationEn = "Convenient vs not clean air = direct contrast → 반면에!",
                    explanationId = "Praktis vs udara kurang bersih = kontras langsung → 반면에!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which particle pairs with the passive verb 열렸어요 (opened by itself)?",
                    promptId = "Partikel mana yang tepat untuk kata kerja pasif 열렸어요 (terbuka)?",
                    stimulusKo = "바람에 창문___ 열렸어요",
                    options = listOf("이 (창문이)", "을 (창문을)", "에게 (창문에게)", "로 (창문로)"),
                    correctOption = "이 (창문이)",
                    explanationEn = "Passive verbs take a subject (이/가), not an object (을/를) → 창문이 열렸어요!",
                    explanationId = "Kata kerja pasif memakai partikel subjek (이/가) → 창문이 열렸어요!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What does 건강에 좋을 뿐만 아니라 맛도 좋다 mean?",
                    promptId = "Apa arti 건강에 좋을 뿐만 아니라 맛도 좋다?",
                    stimulusKo = "건강에 좋을 뿐만 아니라 맛도 좋다",
                    options = listOf("Not only healthy, but also delicious", "Healthy, so it tastes bland", "Delicious, even if unhealthy", "Neither healthy nor tasty"),
                    correctOption = "Not only healthy, but also delicious",
                    explanationEn = "~(으)ㄹ 뿐만 아니라 + 도 = 'Not only A, but also B'!",
                    explanationId = "~(으)ㄹ 뿐만 아니라 + 도 = 'Tidak hanya A, tetapi juga B'!"
                )
            ),
            practiceAction = BeginnerPracticeAction.GRAMMAR_STUDIO,
            practiceLabelEn = "Explore intermediate grammar studio",
            practiceLabelId = "Jelajahi studio tata bahasa menengah",
            xpReward = TOPIK_II_LESSON_XP
        ),

        // ==================== STAGE 8: ACADEMIC HAERACHE & WRITING #51-54 ====================
        BeginnerLesson(
            id = "t2_05_formal_written_haerache",
            number = 5,
            stageNumber = 8,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "다",
            titleEn = "Formal Written Style — Haerache (~는다 / ~ㄴ다 / ~다)",
            titleId = "Ragam Tulis Akademik — Haerache (~는다 / ~ㄴ다 / ~다)",
            goalEn = "Transform polite ~요 endings into crisp TOPIK II academic written style.",
            goalId = "Mengubah akhiran sopan ~요 menjadi gaya tulis ilmiah TOPIK II (~는다/ㄴ다/다).",
            overviewEn = "Here is the #1 rule of TOPIK II Writing (#52, #53, #54) & Korean newspapers: NEVER end academic essays with ~요 or ~습니다! Instead use neutral written Haerache (해라체): Action verbs take ~는다 (batchim) / ~ㄴ다 (vowel). Adjectives stay plain ~다 (중요하다, 많다). Past tense uses ~았/었다!",
            overviewId = "Inilah aturan #1 Menulis TOPIK II (#52, #53, #54) & artikel koran Korea: JANGAN akhiri esai ilmiah dengan ~요 atau ~습니다! Gunakan gaya tulis netral Haerache: Kata kerja aksi pakai ~는다 (batchim) / ~ㄴ다 (vokal). Kata sifat tetap bentuk dasar ~다 (중요하다, 많다). Masa lampau pakai ~았/었다!",
            keyRuleEn = "Haerache Blueprint: Verb (먹다→먹는다, 가다→간다, 사용하다→사용한다) | Adjective (중요하다→중요하다) | Noun (학생이다) | Past (증가했다).",
            keyRuleId = "Pola Haerache: Kata Kerja (먹다→먹는다, 가다→간다, 사용하다→사용한다) | Kata Sifat (중요하다→중요하다) | Kata Benda (학생이다) | Lampau (증가했다).",
            studyCards = listOf(
                BeginnerStudyCard("사용한다", "sa-yong-han-da", "Uses / Use (written verb)", "Menggunakan (gaya tulis)", "사용하다 (vowel) → attach ㄴ다 → 사용한다!", "사용하다 (vokal) → pasang ㄴ다 → 사용한다!"),
                BeginnerStudyCard("먹는다 / 읽는다", "meong-neun-da / ing-neun-da", "Eats / Reads (written verb)", "Makan / Membaca (gaya tulis)", "Verb stem with batchim → attach 는다!", "Akar kata kerja ber-batchim → pasang 는다!"),
                BeginnerStudyCard("중요하다 / 필요하다", "jung-yo-ha-da / pi-ryo-ha-da", "Is important / Is necessary", "Sangat penting / Diperlukan", "Adjectives NEVER take ㄴ/는다 — stay plain ~다!", "Kata sifat TIDAK pakai ㄴ/는다 — tetap ~다!"),
                BeginnerStudyCard("증가하였다 / 증가했다", "jeung-ga-ha-yeot-da", "Increased (formal past)", "Telah meningkat (lampau formal)", "Essential for TOPIK II #53 data reports!", "Wajib dikuasai untuk laporan data TOPIK II #53!"),
                BeginnerStudyCard("~할 것이다", "-hal geo-si-da", "Will ~ (written future)", "Akan ~ (masa depan tulis)", "Written counterpart of spoken ~할 거예요!", "Padanan tulis formal dari bentuk lisan ~할 거예요!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Write in formal Haerache: 'Many people use smartphones'",
                    promptId = "Susun gaya tulis Haerache: 'Banyak orang menggunakan ponsel pintar'",
                    targetTokens = listOf("많은 사람들이", "스마트폰을", "사용한다"),
                    distractorTokens = listOf("사용해요"),
                    fullSentenceKo = "많은 사람들이 스마트폰을 사용한다",
                    romanization = "ma-neun sa-ram-deu-ri seu-ma-teu-po-neul sa-yong-han-da",
                    hintEn = "Action verb 사용하다 in Haerache → 사용한다 (NOT 사용해요)!",
                    hintId = "Kata kerja aksi 사용하다 dalam Haerache → 사용한다 (BUKAN 사용해요)!"
                ),
                SentenceBuildExercise(
                    promptEn = "Write in formal Haerache: 'Environmental protection is very important'",
                    promptId = "Susun gaya tulis Haerache: 'Perlindungan lingkungan sangat penting'",
                    targetTokens = listOf("환경 보호는", "매우", "중요하다"),
                    distractorTokens = listOf("중요한다"),
                    fullSentenceKo = "환경 보호는 매우 중요하다",
                    romanization = "hwan-gyeong bo-ho-neun mae-u jung-yo-ha-da",
                    hintEn = "Adjective 중요하다 stays plain ~다 (NEVER 중요한다)!",
                    hintId = "Kata sifat 중요하다 tetap bentuk dasar ~다 (JANGAN 중요한다)!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Convert '사람들이 책을 읽어요' into formal TOPIK II Haerache:",
                    promptId = "Ubah '사람들이 책을 읽어요' ke ragam tulis formal TOPIK II (Haerache):",
                    stimulusKo = "사람들이 책을 ___",
                    options = listOf("읽는다", "읽ㄴ다", "읽다", "읽습니다"),
                    correctOption = "읽는다",
                    explanationEn = "읽다 is an action verb with a batchim → attach 는다 → 읽는다!",
                    explanationId = "읽다 adalah kata kerja aksi ber-batchim → pasang 는다 → 읽는다!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which adjective ending is CORRECT in an academic TOPIK II essay?",
                    promptId = "Akhiran kata sifat mana yang BENAR dalam esai akademik TOPIK II?",
                    stimulusKo = "이 문제는 매우 ___",
                    options = listOf("심각하다", "심각한다", "심각해요", "심각하는다"),
                    correctOption = "심각하다",
                    explanationEn = "Adjectives stay plain ~다 in Haerache → 심각하다 (is serious)!",
                    explanationId = "Kata sifat tetap ~다 dalam gaya Haerache → 심각하다 (serius)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What is the formal written equivalent of '도움이 될 거예요' (It will be helpful)?",
                    promptId = "Apa padanan tulis formal dari '도움이 될 거예요' (Akan bermanfaat)?",
                    stimulusKo = "도움이 될 거예요 → ?",
                    options = listOf("도움이 될 것이다", "도움이 될 거다요", "도움이 된다요", "도움이 됐어요"),
                    correctOption = "도움이 될 것이다",
                    explanationEn = "Spoken ~거예요 becomes formal written ~것이다 → 도움이 될 것이다!",
                    explanationId = "Bentuk lisan ~거예요 berubah menjadi tulis formal ~것이다 → 도움이 될 것이다!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_II_EXAM,
            practiceLabelEn = "Practice TOPIK II Reading & Writing",
            practiceLabelId = "Latihan Soal Membaca & Menulis TOPIK II",
            xpReward = TOPIK_II_LESSON_XP
        ),

        BeginnerLesson(
            id = "t2_06_topik2_writing_51_52",
            number = 6,
            stageNumber = 8,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "51",
            titleEn = "TOPIK II Writing #51 (Polite Notice) & #52 (Academic Logic)",
            titleId = "Menulis TOPIK II #51 (Pengumuman) & #52 (Logika Ilmiah)",
            goalEn = "Score easy full marks on the two fill-in-the-blank TOPIK II writing tasks.",
            goalId = "Meraih poin maksimal pada dua soal isian singkat Menulis TOPIK II (#51 & #52).",
            overviewEn = "Questions #51 and #52 are worth 20 easy points once you know their speech-level contrast! #51 is a practical email, message, or notice — ALWAYS match its polite style (~습니다 / ~시기 바랍니다 / ~아/어 주십시오). #52 is an informative paragraph — ALWAYS write in Haerache (~는다/ㄴ다/다) and mirror the logic of the neighboring sentence!",
            overviewId = "Soal #51 dan #52 bernilai 20 poin empuk bila tahu rahasia ragam bahasanya! #51 berupa email/pengumuman — WAJIB cocokkan gaya sopan formal (~습니다 / ~시기 바랍니다). #52 berupa paragraf ilmiah — WAJIB pakai Haerache (~는다/ㄴ다/다) dan ikuti cermin logika kalimat sekitarnya!",
            keyRuleEn = "#51 vs #52 Rule: #51 = Polite (~습니다 / ~주시기 바랍니다 / ~는지 궁금합니다). #52 = Plain Academic Haerache (~기 때문이다 / ~(으)ㄹ 수 있다).",
            keyRuleId = "Rumus #51 vs #52: #51 = Sopan Formal (~습니다 / ~주시기 바랍니다). #52 = Ilmiah Haerache (~기 때문이다 / ~(으)ㄹ 수 있다).",
            studyCards = listOf(
                BeginnerStudyCard("~해 주시기 바랍니다", "-hae ju-si-gi ba-ram-ni-da", "Please kindly ... (official notice)", "Dimohon untuk ... (pengumuman resmi)", "Top-scoring closing phrase for #51 notices!", "Kalimat penutup peraih nilai penuh untuk soal #51!"),
                BeginnerStudyCard("~는지 알고 싶습니다", "-neun-ji al-go sip-seum-ni-da", "I would like to know whether...", "Saya ingin mengetahui apakah...", "Golden inquiry template for #51 emails!", "Template pertanyaan emas untuk soal email #51!"),
                BeginnerStudyCard("~기 때문이다", "-gi ttae-mu-ni-da", "It is because ... (#52)", "Hal ini karena ... (#52)", "When the sentence starts with 왜냐하면, end with ~기 때문이다!", "Jika kalimat diawali 왜냐하면, akhiri dengan ~기 때문이다!"),
                BeginnerStudyCard("~(으)ㄹ 수 있다", "-(eu)l su it-da", "Can / May possibly ... (#52)", "Dapat / Bisa jadi ... (#52)", "Ideal objective hedging for #52 scientific facts!", "Ungkapan objektif ideal untuk fakta ilmiah #52!"),
                BeginnerStudyCard("반대로 / 오히려", "ban-dae-ro / o-hi-ryeo", "Conversely / Rather", "Sebaliknya / Malah", "Signals an opposite verb/adjective inside #52 blank!", "Menandakan isi blank #52 harus berlawanan arah!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build a #51 formal notice: 'Those attending the event, please apply'",
                    promptId = "Susun pengumuman #51: 'Bagi yang hadir di acara, dimohon mendaftar'",
                    targetTokens = listOf("행사에 참석하실 분은", "신청해", "주시기 바랍니다"),
                    distractorTokens = listOf("신청한다"),
                    fullSentenceKo = "행사에 참석하실 분은 신청해 주시기 바랍니다",
                    romanization = "haeng-sa-e cham-seo-ka-sil bu-neun sin-cheong-hae ju-si-gi ba-ram-ni-da",
                    hintEn = "#51 official notice finishes with ~해 주시기 바랍니다!",
                    hintId = "Pengumuman resmi #51 ditutup dengan ~해 주시기 바랍니다!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build a #52 academic line: 'If exercise is lacking, health can worsen'",
                    promptId = "Susun kalimat ilmiah #52: 'Jika kurang olahraga, kesehatan bisa memburuk'",
                    targetTokens = listOf("운동이 부족하면", "건강이", "나빠질 수", "있다"),
                    distractorTokens = listOf("있어요"),
                    fullSentenceKo = "운동이 부족하면 건강이 나빠질 수 있다",
                    romanization = "un-dong-i bu-jo-ka-myeon geon-gang-i na-ppa-jil su it-da",
                    hintEn = "#52 uses plain academic Haerache: ~(으)ㄹ 수 있다!",
                    hintId = "#52 memakai Haerache ilmiah: ~(으)ㄹ 수 있다!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "In Writing #51 (a polite email to a professor), which ending fits?",
                    promptId = "Pada Menulis #51 (email sopan ke dosen), akhiran mana yang wajib dipakai?",
                    stimulusKo = "언제 상담이 가능한지 (   ).",
                    options = listOf("알려 주시기 바랍니다", "알려 준다", "알려 줘", "알려 주었다"),
                    correctOption = "알려 주시기 바랍니다",
                    explanationEn = "#51 requires formal polite style (~습니다 / ~시기 바랍니다)!",
                    explanationId = "#51 selalu memakai ragam sopan formal (~습니다 / ~시기 바랍니다)!"
                ),
                BeginnerMiniCheck(
                    promptEn = "In Writing #52, a sentence begins with '왜냐하면 ...'. How MUST it end?",
                    promptId = "Pada Menulis #52, kalimat diawali '왜냐하면 ...'. Bagaimana WAJIB mengakhirinya?",
                    stimulusKo = "왜냐하면 물이 부족하(   ).",
                    options = listOf("기 때문이다", "기 때문이에요", "기 때문입니다", "지만 좋다"),
                    correctOption = "기 때문이다",
                    explanationEn = "왜냐하면 + ~기 때문이다 in Haerache is a guaranteed #52 scoring pair!",
                    explanationId = "왜냐하면 + ~기 때문이다 (Haerache) adalah pasangan wajib skor penuh #52!"
                ),
                BeginnerMiniCheck(
                    promptEn = "In #52: '낮에는 기온이 올라가지만, 밤에는 (   ).' What verb fits logically?",
                    promptId = "Pada #52: '낮에는 기온이 올라가지만, 밤에는 (   ).' Kata kerja mana yang logis?",
                    stimulusKo = "낮에는 올라가지만, 밤에는 (   ).",
                    options = listOf("내려간다 (drops)", "올라간다 (rises)", "내려가요 (polite)", "올라갔습니다"),
                    correctOption = "내려간다 (drops)",
                    explanationEn = "~지만 flips 올라가다 (rise) to 내려가다 (drop), in Haerache → 내려간다!",
                    explanationId = "~지만 membalik 올라가다 (naik) menjadi 내려가다 (turun) dalam Haerache → 내려간다!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_II_EXAM,
            practiceLabelEn = "Try TOPIK II Writing #51–52 in Exam",
            practiceLabelId = "Coba Soal Menulis #51–52 di Ujian",
            xpReward = TOPIK_II_LESSON_XP
        ),

        BeginnerLesson(
            id = "t2_07_topik2_writing_53_graph",
            number = 7,
            stageNumber = 8,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 5,
            emblemKo = "53",
            titleEn = "TOPIK II Writing #53 — Graph & Trend Report Blueprint",
            titleId = "Menulis TOPIK II #53 — Rumus Laporan Grafik & Data",
            goalEn = "Write a clean 200–300 character data report using ready-to-use TOPIK II templates.",
            goalId = "Menulis laporan grafik 200–300 huruf secara rapi memakai template siap pakai TOPIK II.",
            overviewEn = "Question #53 (30 points!) is actually the EASIEST way to boost your TOPIK II score because every chart follows the exact same 3-sentence blueprint: ① Introduce survey ([Org]에서 [Topic]에 대해 조사하였다) → ② Describe trend (크게 증가하였다 / 감소하였다) → ③ State causes (이러한 변화의 원인으로 ~이/가 꼽혔다)!",
            overviewId = "Soal #53 (bernilai 30 poin!) sebenarnya cara TERCEPAT mendongkrak skor TOPIK II karena setiap grafik mengikuti 3 langkah pasti: ① Pembuka survei ([Lembaga]에서 ... 조사하였다) → ② Tren angka (크게 증가하였다 / 감소하였다) → ③ Penyebab (원인으로 ~이/가 꼽혔다)!",
            keyRuleEn = "#53 3-Step Formula: ① 조사 결과 (~에 따르면) → ② [A]에서 [B]으로 증가/감소하였다 → ③ 가장 큰 원인으로 [Cause]이/가 꼽혔다!",
            keyRuleId = "Rumus 3 Langkah #53: ① 조사 결과 → ② [A]에서 [B]으로 증가/감소하였다 → ③ 가장 큰 원인으로 [Penyebab]이/가 꼽혔다!",
            studyCards = listOf(
                BeginnerStudyCard("조사 결과", "jo-sa gyeol-gwa", "As a result of the survey", "Berdasarkan hasil survei", "Clean opener linking graph title to data!", "Pembuka rapi penghubung judul grafik ke data!"),
                BeginnerStudyCard("크게 증가하였다", "keu-ge jeung-ga-ha-yeot-da", "Increased sharply", "Meningkat secara tajam", "Opposite: 크게 감소하였다 (decreased sharply)", "Lawan katanya: 크게 감소하였다 (menurun tajam)"),
                BeginnerStudyCard("약 2배 늘었다", "yak du-bae neu-reot-da", "Roughly doubled (2×)", "Meningkat sekitar 2 kali lipat", "Comparing start & end ratio scores extra points!", "Membandingkan kelipatan awal-akhir memberi nilai tambah!"),
                BeginnerStudyCard("원인으로 꼽혔다", "wo-ni-neu-ro kko-pyeot-da", "Was cited as a cause", "Disebut sebagai penyebab", "Gold-standard academic phrase for the Causes box!", "Ungkapan akademik bintang 5 untuk kotak Penyebab!"),
                BeginnerStudyCard("전망이다", "jeon-mang-i-da", "It is projected / expected that...", "Diperkirakan bahwa...", "앞으로도 계속 증가할 전망이다 (Expected to keep rising)", "앞으로도 계속 증가할 전망이다 (Diperkirakan terus naik)")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build a #53 trend sentence: 'As a result of the survey, online purchases increased sharply'",
                    promptId = "Susun kalimat tren #53: 'Hasil survei menunjukkan pembelian daring meningkat tajam'",
                    targetTokens = listOf("조사 결과", "온라인 구매가", "크게", "증가하였다"),
                    distractorTokens = listOf("증가해요"),
                    fullSentenceKo = "조사 결과 온라인 구매가 크게 증가하였다",
                    romanization = "jo-sa gyeol-gwa on-la-in gu-mae-ga keu-ge jeung-ga-ha-yeot-da",
                    hintEn = "조사 결과 + Subject (온라인 구매가) + 크게 증가하였다!",
                    hintId = "조사 결과 + Subjek (온라인 구매가) + 크게 증가하였다!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build a #53 cause sentence: 'Convenience was cited as the biggest reason'",
                    promptId = "Susun kalimat penyebab #53: 'Kepraktisan disebut sebagai alasan terbesar'",
                    targetTokens = listOf("가장 큰", "원인으로", "편리함이", "꼽혔다"),
                    distractorTokens = listOf("꼽아요"),
                    fullSentenceKo = "가장 큰 원인으로 편리함이 꼽혔다",
                    romanization = "ga-jang keun wo-ni-neu-ro pyeol-li-ha-mi kko-pyeot-da",
                    hintEn = "가장 큰 원인으로 (as the biggest cause) + 편리함이 꼽혔다!",
                    hintId = "가장 큰 원인으로 (sebagai penyebab terbesar) + 편리함이 꼽혔다!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "A graph falls from 80% in 2018 to 35% in 2024. Which verb describes this?",
                    promptId = "Grafik turun dari 80% di 2018 menjadi 35% di 2024. Kata kerja mana yang tepat?",
                    stimulusKo = "80% → 35%",
                    options = listOf("크게 감소하였다 (decreased sharply)", "크게 증가하였다 (increased sharply)", "변화가 없었다 (no change)", "2배 늘었다 (doubled)"),
                    correctOption = "크게 감소하였다 (decreased sharply)",
                    explanationEn = "감소하다 = decrease; 증가하다 = increase!",
                    explanationId = "감소하다 = menurun; 증가하다 = meningkat!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which ending cleanly expresses a future outlook ('is projected to rise') in #53?",
                    promptId = "Akhiran mana yang tepat untuk proyeksi masa depan ('diperkirakan naik') di soal #53?",
                    stimulusKo = "앞으로도 계속 증가할 ___",
                    options = listOf("전망이다", "꼽혔다", "조사했다", "때문이에요"),
                    correctOption = "전망이다",
                    explanationEn = "~(으)ㄹ 전망이다 = 'It is projected/expected that...' — a classic #53 closer!",
                    explanationId = "~(으)ㄹ 전망이다 = 'Diperkirakan akan...' — penutup klasik #53!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What is the recommended length for TOPIK II Writing #53?",
                    promptId = "Berapa target panjang huruf untuk Menulis TOPIK II #53?",
                    stimulusKo = "쓰기 53번 분량",
                    options = listOf("200–300 characters (자)", "50–80 characters", "600–700 characters", "1,000+ characters"),
                    correctOption = "200–300 characters (자)",
                    explanationEn = "#53 targets 200–300 Korean characters (including spaces), while #54 targets 600–700!",
                    explanationId = "#53 bertarget 200–300 huruf (termasuk spasi), sedangkan esai #54 bertarget 600–700 huruf!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_II_EXAM,
            practiceLabelEn = "Practice TOPIK II #53 in Exam Studio",
            practiceLabelId = "Latihan Soal #53 di Studio Ujian",
            xpReward = TOPIK_II_LESSON_XP
        ),

        BeginnerLesson(
            id = "t2_08_topik2_writing_54_essay",
            number = 8,
            stageNumber = 8,
            track = SyllabusTrack.TOPIK_II,
            estimatedMinutes = 6,
            emblemKo = "54",
            titleEn = "TOPIK II Writing #54 — High-Score Argumentative Essay",
            titleId = "Menulis TOPIK II #54 — Struktur Esai Argumentatif Skor Tinggi",
            goalEn = "Structure a persuasive 600–700 character Korean essay across Intro, Body & Conclusion.",
            goalId = "Menyusun esai argumentatif Korea 600–700 huruf yang terstruktur: Pembuka, Isi & Penutup.",
            overviewEn = "The pinnacle of Korean mastery — TOPIK II Writing #54 (50 points)! Never write freely without a map: always answer EVERY bullet point in the prompt across 4 clean paragraphs (Intro → Point 1 → Point 2/Counterpoint → Solution & Conclusion) in consistent Haerache! Finish this lesson to graduate ALL 3 BAEUM Syllabi!",
            overviewId = "Puncak kemahiran bahasa Korea — Menulis TOPIK II #54 (50 poin)! Selalu jawab SEMUA poin pertanyaan dalam 4 paragraf rapi (Pembuka → Argumen 1 → Argumen 2/Solusi → Kesimpulan) dengan Haerache konsisten! Tuntaskan pelajaran ini untuk menamatkan seluruh 3 Silabus BAEUM!",
            keyRuleEn = "#54 Essay Skeleton: ① Intro (현대 사회에서 ~에 대해 논의하고자 한다) → ② 첫째 / 둘째 → ③ 그러나 (nuance) → ④ 따라서 ~하는 것이 바람직하다!",
            keyRuleId = "Kerangka Esai #54: ① Pembuka (논의하고자 한다) → ② 첫째 / 둘째 → ③ 그러나 (nuansa) → ④ 따라서 ~하는 것이 바람직하다!",
            studyCards = listOf(
                BeginnerStudyCard("~에 대해 논의하고자 한다", "-e dae-hae no-nui-ha-go-ja han-da", "This essay aims to discuss...", "Tulisan ini bertujuan membahas...", "Polished academic thesis opener for #54!", "Kalimat pembuka tesis akademik kelas atas untuk #54!"),
                BeginnerStudyCard("첫째, ... 둘째, ...", "cheot-jjae ... dul-jjae", "First, ... Second, ...", "Pertama, ... Kedua, ...", "Keeps graders effortlessly following your logic!", "Membuat penguji mudah mengikuti alur logikamu!"),
                BeginnerStudyCard("이러한 문제를 해결하기 위해서는", "i-reo-han mun-je-reul hae-gyeol-ha-gi wi-hae-seo-neun", "In order to solve this issue...", "Untuk menyelesaikan masalah ini...", "Ideal transition into your Solution paragraph!", "Transisi ideal menuju paragraf Solusi!"),
                BeginnerStudyCard("따라서", "tta-ra-seo", "Therefore / Consequently", "Oleh karena itu / Maka", "Academic upgrade to casual 그래서!", "Versi akademik tingkat lanjut dari 그래서!"),
                BeginnerStudyCard("~하는 것이 바람직하다", "-ha-neun geo-si ba-ram-ji-ka-da", "It is desirable / advisable to...", "Sebaiknya / Idealnya kita...", "High-scoring conclusion ending for TOPIK 5–6급!", "Akhiran kesimpulan peraih skor tinggi TOPIK 5–6급!")
            ),
            sentenceExercises = listOf(
                SentenceBuildExercise(
                    promptEn = "Build a #54 thesis opener: 'This essay will discuss the pros and cons of AI'",
                    promptId = "Susun pembuka tesis #54: 'Esai ini akan membahas kelebihan dan kekurangan AI'",
                    targetTokens = listOf("인공지능의", "장단점에 대해", "논의하고자", "한다"),
                    distractorTokens = listOf("해요"),
                    fullSentenceKo = "인공지능의 장단점에 대해 논의하고자 한다",
                    romanization = "in-gong-ji-neung-ui jang-dan-jeo-me dae-hae no-nui-ha-go-ja han-da",
                    hintEn = "Topic (인공지능의 장단점에 대해) + 논의하고자 한다!",
                    hintId = "Topik (인공지능의 장단점에 대해) + 논의하고자 한다!"
                ),
                SentenceBuildExercise(
                    promptEn = "Build a #54 conclusion: 'Therefore, a balanced attitude is desirable'",
                    promptId = "Susun kesimpulan #54: 'Oleh karena itu, sikap yang seimbang sangatlah ideal'",
                    targetTokens = listOf("따라서", "균형 잡힌", "태도가", "바람직하다"),
                    distractorTokens = listOf("그래서요"),
                    fullSentenceKo = "따라서 균형 잡힌 태도가 바람직하다",
                    romanization = "tta-ra-seo gyun-hyeong ja-pin tae-do-ga ba-ram-ji-ka-da",
                    hintEn = "따라서 (Therefore) + 균형 잡힌 태도가 (a balanced attitude) + 바람직하다!",
                    hintId = "따라서 (Oleh karena itu) + 균형 잡힌 태도가 (sikap seimbang) + 바람직하다!"
                )
            ),
            miniChecks = listOf(
                BeginnerMiniCheck(
                    promptEn = "Which word is the formal academic upgrade to 그래서 for a #54 conclusion?",
                    promptId = "Kata sambung mana yang merupakan versi akademik formal dari 그래서 untuk kesimpulan #54?",
                    stimulusKo = "(   ) 사회적 노력이 필요하다.",
                    options = listOf("따라서 (Therefore)", "그래가지고 (So, slang)", "근데 (By the way)", "왜냐하면 (Because)"),
                    correctOption = "따라서 (Therefore)",
                    explanationEn = "따라서 signals a clear, logical academic conclusion!",
                    explanationId = "따라서 menandakan kesimpulan akademik yang tegas dan logis!"
                ),
                BeginnerMiniCheck(
                    promptEn = "Which closing expression earns top marks for recommending a solution in #54?",
                    promptId = "Penutup mana yang meraih skor tinggi saat menyarankan solusi di esai #54?",
                    stimulusKo = "서로 배려하는 자세가 ___",
                    options = listOf("바람직하다 (is desirable)", "좋아요 (too casual)", "좋은 것 같아 (spoken)", "바람직한다 (wrong form)"),
                    correctOption = "바람직하다 (is desirable)",
                    explanationEn = "바람직하다 is an adjective in clean Haerache — ideal for TOPIK 5–6급 essays!",
                    explanationId = "바람직하다 adalah kata sifat dalam Haerache rapi — ideal untuk esai TOPIK 5–6급!"
                ),
                BeginnerMiniCheck(
                    promptEn = "What is the #1 way to lose points on TOPIK II Writing #54?",
                    promptId = "Apa penyebab #1 kehilangan poin pada Menulis TOPIK II #54?",
                    stimulusKo = "쓰기 54번 감점 요인",
                    options = listOf("Skipping one of the prompt's 3 sub-questions or mixing ~요 with ~다", "Using 첫째 / 둘째 structure", "Writing 650 characters in Haerache", "Giving a concrete example"),
                    correctOption = "Skipping one of the prompt's 3 sub-questions or mixing ~요 with ~다",
                    explanationEn = "Always answer every bullet point in the prompt and keep 100% consistent Haerache!",
                    explanationId = "Selalu jawab seluruh poin soal dan jaga konsistensi Haerache 100% dari awal sampai akhir!"
                )
            ),
            practiceAction = BeginnerPracticeAction.TOPIK_II_EXAM,
            practiceLabelEn = "Take Authentic TOPIK II Mock Exam",
            practiceLabelId = "Mulai Simulasi Ujian TOPIK II",
            xpReward = TOPIK_II_LESSON_XP
        )
    )

    // Backwards-compatible Beginner Track views (12 lessons across 4 stages)
    val stages: List<BeginnerStage> = allStages.filter { it.track == SyllabusTrack.BEGINNER }
    val lessons: List<BeginnerLesson> = allLessons.filter { it.track == SyllabusTrack.BEGINNER }

    fun stagesForTrack(track: SyllabusTrack): List<BeginnerStage> =
        allStages.filter { it.track == track }

    fun stageByNumber(stageNumber: Int): BeginnerStage? =
        allStages.firstOrNull { it.number == stageNumber }

    fun lessonsForTrack(track: SyllabusTrack): List<BeginnerLesson> =
        allLessons.filter { it.track == track }

    fun lessonById(id: String): BeginnerLesson? =
        allLessons.firstOrNull { it.id == id }

    fun lessonsForStage(stageNumber: Int): List<BeginnerLesson> =
        allLessons.filter { it.stageNumber == stageNumber }

    /**
     * Progressive Auto-Fade Romanization Policy:
     * - Beginner Stages 1–2 (Hangul decoding): FULL
     * - Beginner Stages 3–4 (Sentence Building) & TOPIK I: TAP_TO_PEEK
     * - TOPIK II (3–6급): EXAM_HIDDEN (authentic exam feel, tap to peek if needed)
     */
    fun romanizationPolicyFor(lesson: BeginnerLesson): RomanizationVisibility = when {
        lesson.track == SyllabusTrack.BEGINNER && lesson.stageNumber <= 2 -> RomanizationVisibility.FULL
        lesson.track == SyllabusTrack.TOPIK_II -> RomanizationVisibility.EXAM_HIDDEN
        else -> RomanizationVisibility.TAP_TO_PEEK
    }

    /**
     * Activates Listening-First (Audio-to-Sentence) mode automatically on the 2nd sentence builder
     * from Stage 2 onward (or whenever explicitly flagged on the exercise).
     */
    fun isAudioFirstExercise(lesson: BeginnerLesson, exerciseIndex: Int): Boolean {
        val ex = lesson.sentenceExercises.getOrNull(exerciseIndex) ?: return false
        return ex.isAudioFirst || (lesson.stageNumber >= 2 && exerciseIndex == 1)
    }

    /**
     * Activates Listening Minimal-Pair ear discrimination on selected Hangul decoding checks.
     */
    fun isAudioPromptCheck(lesson: BeginnerLesson, checkIndex: Int): Boolean {
        val chk = lesson.miniChecks.getOrNull(checkIndex) ?: return false
        return chk.isAudioPrompt || (lesson.stageNumber <= 2 && checkIndex == 0)
    }

    /**
     * Extracts clean Korean Hangul vocabulary tokens from a lesson so completing it
     * automatically feeds matching words into Room's FSRS daily review queue.
     */
    fun coreKeywordsFor(lesson: BeginnerLesson): Set<String> {
        val regex = Regex("[가-힣]{1,8}")
        val out = LinkedHashSet<String>()
        lesson.studyCards.forEach { card ->
            regex.findAll(card.hangul).forEach { m ->
                val token = m.value
                if (token.length >= 2 || lesson.stageNumber == 1) out.add(token)
            }
        }
        lesson.sentenceExercises.forEach { ex ->
            ex.targetTokens.forEach { raw ->
                regex.findAll(raw).forEach { m ->
                    if (m.value.length >= 2) out.add(m.value)
                }
            }
        }
        return out
    }

    fun mistakeKeyForBuild(lessonId: String, exerciseIndex: Int): String =
        "$lessonId::build::$exerciseIndex"

    fun mistakeKeyForCheck(lessonId: String, checkIndex: Int): String =
        "$lessonId::check::$checkIndex"

    fun resolveMistakeItem(mistakeKey: String): SyllabusMistakeItem? {
        val parts = mistakeKey.split("::")
        if (parts.size != 3) return null
        val lesson = lessonById(parts[0]) ?: return null
        val idx = parts[2].toIntOrNull() ?: return null
        return when (parts[1]) {
            "build" -> lesson.sentenceExercises.getOrNull(idx)?.let {
                SyllabusMistakeItem.SentenceBuildMistake(mistakeKey, lesson, idx, it)
            }
            "check" -> lesson.miniChecks.getOrNull(idx)?.let {
                SyllabusMistakeItem.MiniCheckMistake(mistakeKey, lesson, idx, it)
            }
            else -> null
        }
    }

    fun nextLessonInTrack(currentLesson: BeginnerLesson): BeginnerLesson? =
        lessonsForTrack(currentLesson.track).firstOrNull { it.number == currentLesson.number + 1 }

    fun nextTrackAfter(track: SyllabusTrack): SyllabusTrack? = when (track) {
        SyllabusTrack.BEGINNER -> SyllabusTrack.TOPIK_I
        SyllabusTrack.TOPIK_I -> SyllabusTrack.TOPIK_II
        SyllabusTrack.TOPIK_II -> null
    }

    /**
     * Automatically determines the learner's current guided track:
     * BEGINNER -> once 12/12 graduated -> TOPIK_I -> once 10/10 graduated -> TOPIK_II.
     */
    fun activeTrackFor(completedIds: Set<String>): SyllabusTrack = when {
        !isTrackGraduated(completedIds, SyllabusTrack.BEGINNER) -> SyllabusTrack.BEGINNER
        !isTrackGraduated(completedIds, SyllabusTrack.TOPIK_I) -> SyllabusTrack.TOPIK_I
        else -> SyllabusTrack.TOPIK_II
    }

    fun nextRecommendedLesson(completedIds: Set<String>, track: SyllabusTrack): BeginnerLesson {
        val trackLessons = lessonsForTrack(track)
        return trackLessons.firstOrNull { it.id !in completedIds } ?: trackLessons.last()
    }

    fun nextRecommendedLesson(completedIds: Set<String>): BeginnerLesson =
        nextRecommendedLesson(completedIds, SyllabusTrack.BEGINNER)

    fun completionCount(completedIds: Set<String>, track: SyllabusTrack): Int =
        lessonsForTrack(track).count { it.id in completedIds }

    fun completionCount(completedIds: Set<String>): Int =
        completionCount(completedIds, SyllabusTrack.BEGINNER)

    fun completionFraction(completedIds: Set<String>, track: SyllabusTrack): Float {
        val total = lessonsForTrack(track).size.coerceAtLeast(1)
        return (completionCount(completedIds, track).toFloat() / total).coerceIn(0f, 1f)
    }

    fun completionFraction(completedIds: Set<String>): Float =
        completionFraction(completedIds, SyllabusTrack.BEGINNER)

    fun isTrackGraduated(completedIds: Set<String>, track: SyllabusTrack): Boolean {
        val trackLessons = lessonsForTrack(track)
        return trackLessons.isNotEmpty() && completionCount(completedIds, track) >= trackLessons.size
    }

    fun isGraduated(completedIds: Set<String>): Boolean =
        isTrackGraduated(completedIds, SyllabusTrack.BEGINNER)

    fun isAllTracksGraduated(completedIds: Set<String>): Boolean =
        SyllabusTrack.entries.all { isTrackGraduated(completedIds, it) }

    fun totalCompletionCount(completedIds: Set<String>): Int =
        allLessons.count { it.id in completedIds }

    fun totalCompletionFraction(completedIds: Set<String>): Float =
        (totalCompletionCount(completedIds).toFloat() / allLessons.size.coerceAtLeast(1)).coerceIn(0f, 1f)

    fun perfectCount(perfectIds: Set<String>, track: SyllabusTrack): Int =
        lessonsForTrack(track).count { it.id in perfectIds }
}
