package com.example.domain.curriculum

/**
 * Stage grouping inside the Zero-to-Seoul Complete Beginner Syllabus (4 Stages × 3 Lessons = 12 Lessons).
 */
data class BeginnerStage(
    val number: Int,
    val titleKo: String,
    val titleEn: String,
    val titleId: String,
    val subtitleEn: String,
    val subtitleId: String,
    val accentTag: String // "primary" | "mint" | "gold" | "cherry"
)

/**
 * Bite-sized interactive Hangul/phrase study card inside a Beginner Lesson.
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
 * Built-in 3-question guided mini-check at the end of every Beginner Lesson.
 */
data class BeginnerMiniCheck(
    val promptEn: String,
    val promptId: String,
    val stimulusKo: String,
    val options: List<String>,
    val correctOption: String,
    val explanationEn: String,
    val explanationId: String,
    val speakOnTapKo: String = stimulusKo
)

/**
 * Suggested hands-on next studio action after finishing a Beginner Lesson.
 */
enum class BeginnerPracticeAction {
    HANGUL_WRITING,
    HANGUL_STUDIO,
    BEGINNER_FLASHCARDS,
    GRAMMAR_STUDIO,
    EVERYDAY_SPEAKING
}

/**
 * Complete guided lesson for zero-background Korean learners.
 */
data class BeginnerLesson(
    val id: String,
    val number: Int,
    val stageNumber: Int,
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
    val miniChecks: List<BeginnerMiniCheck>,
    val practiceAction: BeginnerPracticeAction,
    val practiceLabelEn: String,
    val practiceLabelId: String,
    val xpReward: Int = 60
)

/**
 * Structured, jargon-free 12-lesson Complete Beginner Korean Syllabus ("Zero-to-Seoul Path")
 * modeled on Yonsei KLI / King Sejong Institute beginner progression.
 */
object BeginnerSyllabusCatalog {

    const val LESSON_XP_REWARD = 60
    const val GRADUATION_BONUS_XP = 150

    val stages: List<BeginnerStage> = listOf(
        BeginnerStage(
            number = 1,
            titleKo = "한글 첫걸음",
            titleEn = "Stage 1 · Reading Your First Hangul",
            titleId = "Tahap 1 · Membaca Hangul Pertama",
            subtitleEn = "Master basic vowels, core consonants & syllable blocks in minutes.",
            subtitleId = "Kuasai vokal dasar, konsonan inti & cara menyusun blok suku kata dalam hitungan menit.",
            accentTag = "primary"
        ),
        BeginnerStage(
            number = 2,
            titleKo = "받침과 발음",
            titleEn = "Stage 2 · Natural Reading & Batchim",
            titleId = "Tahap 2 · Bacaan Alami & Batchim",
            subtitleEn = "Unlock Y-vowels, final consonants (batchim), tense letters & smooth linking.",
            subtitleId = "Pahami vokal-Y, konsonan akhir (batchim), huruf ganda & rahasia sambung bunyi.",
            accentTag = "mint"
        ),
        BeginnerStage(
            number = 3,
            titleKo = "첫 한국어 회화",
            titleEn = "Stage 3 · Greetings, Self-Intro & Core Particles",
            titleId = "Tahap 3 · Sapaan, Perkenalan & Partikel Inti",
            subtitleEn = "Speak politely, introduce yourself & understand Korean sentence order.",
            subtitleId = "Menyapa sopan, memperkenalkan diri & memahami urutan kalimat bahasa Korea.",
            accentTag = "gold"
        ),
        BeginnerStage(
            number = 4,
            titleKo = "서울 실전",
            titleEn = "Stage 4 · Real-Life Seoul Survival",
            titleId = "Tahap 4 · Praktik Nyata di Seoul",
            subtitleEn = "Numbers, ordering at cafes & conjugating everyday Korean verbs effortlessly.",
            subtitleId = "Angka Korea, memesan di kafe & mengubah kata kerja harian dengan rumus mudah.",
            accentTag = "cherry"
        )
    )

    val lessons: List<BeginnerLesson> = listOf(
        // ==================== STAGE 1: FIRST HANGUL ====================
        BeginnerLesson(
            id = "beg_01_basic_vowels",
            number = 1,
            stageNumber = 1,
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

        // ==================== STAGE 3: GREETINGS, SELF-INTRO & PARTICLES ====================
        BeginnerLesson(
            id = "beg_07_essential_greetings",
            number = 7,
            stageNumber = 3,
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
            estimatedMinutes = 4,
            emblemKo = "저",
            titleEn = "Korean Sentence Order & Self-Introduction",
            titleId = "Urutan Kalimat Korea & Perkenalan Diri",
            goalEn = "Introduce your name & nationality and grasp the Verb-At-The-End rule.",
            goalId = "Memperkenalkan nama & asal negara serta memahami rumus Kata Kerja di Akhir.",
            overviewEn = "Unlike English/Indonesian (Subject → Verb → Object), Korean ALWAYS puts the verb at the VERY END: Subject → Object → Verb! ('I + coffee + drink' = 저는 커피를 마셔요). For 'I am [Noun]', attach 이에요 (after batchim) or 예요 (after a vowel).",
            overviewId = "Berbeda dengan bahasa Indonesia (Subjek → Predikat → Objek), bahasa Korea SELALU menaruh kata kerja di PALING AKHIR: Subjek → Objek → Kata Kerja! ('Saya + kopi + minum' = 저는 커피를 마셔요). Untuk 'Saya adalah [Nama]', tempel 이에요 (setelah batchim) atau 예요 (setelah vokal).",
            keyRuleEn = "Self-Intro Formula: 저는 [Name]이에요/예요. (I am [Name].) + 만나서 반가워요! (Nice to meet you!)",
            keyRuleId = "Rumus Perkenalan: 저는 [Nama]이에요/예요. (Saya [Nama].) + 만나서 반가워요! (Senang bertemu!)",
            studyCards = listOf(
                BeginnerStudyCard("저는", "jeo-neun", "I / As for me (polite)", "Saya (sopan, topik kalimat)", "저 = humble polite 'I'; 는 marks the topic", "저 = 'saya' sopan; 는 menandai topik kalimat"),
                BeginnerStudyCard("학생이에요", "hak-saeng-i-e-yo", "I am a student", "Saya adalah pelajar", "학생 ends in batchim ㅇ → attach 이에요", "학생 berakhiran batchim ㅇ → pakai 이에요"),
                BeginnerStudyCard("인도네시아 사람이에요", "in-do-ne-si-a sa-ram-i-e-yo", "I am Indonesian", "Saya orang Indonesia", "사람 (person) ends in ㅁ → attach 이에요", "사람 (orang) berakhiran ㅁ → pakai 이에요"),
                BeginnerStudyCard("저는 커피를 마셔요", "jeo-neun keo-pi-reul ma-syeo-yo", "I drink coffee (S-O-V!)", "Saya minum kopi (S-O-V!)", "Literally: I + coffee + drink!", "Secara harfiah: Saya + kopi + minum!"),
                BeginnerStudyCard("만나서 반가워요", "man-na-seo ban-ga-wo-yo", "Nice to meet you!", "Senang bertemu denganmu!", "Warm closing line after giving your name", "Kalimat penutup ramah setelah menyebutkan nama")
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
            estimatedMinutes = 4,
            emblemKo = "은",
            titleEn = "4 Must-Know Beginner Particles (은/는, 이/가, 을/를, 에)",
            titleId = "4 Partikel Wajib Pemula (은/는, 이/가, 을/를, 에)",
            goalEn = "Spot the role of every noun in a sentence using 4 simple tags.",
            goalId = "Mengenali peran setiap kata benda dalam kalimat lewat 4 penanda sederhana.",
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
            estimatedMinutes = 4,
            emblemKo = "주",
            titleEn = "Ordering at a Seoul Cafe & Asking Prices",
            titleId = "Memesan di Kafe Seoul & Bertanya Harga",
            goalEn = "Order anything politely with '[Item] 주세요' and ask '얼마예요?'.",
            goalId = "Memesan apa pun dengan sopan memakai '[Barang] 주세요' dan bertanya '얼마예요?'.",
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
            estimatedMinutes = 4,
            emblemKo = "해",
            titleEn = "Everyday Verbs & The Easy ~아요/~어요 Formula",
            titleId = "Kata Kerja Harian & Rumus Mudah ~아요/~어요",
            goalEn = "Turn dictionary verbs (-다) into natural polite sentences effortlessly.",
            goalId = "Mengubah kata kerja kamus (-다) menjadi kalimat sopan harian dengan mudah.",
            overviewEn = "Every Korean verb in the dictionary ends in -다 (가다 = to go, 먹다 = to eat). To speak politely in the present tense, drop -다 and follow 3 tiny rules: ① Last vowel ㅏ/ㅗ → add 아요. ② Any other vowel → add 어요. ③ Ends in 하다 → becomes 해요! You are now ready for TOPIK 1급!",
            overviewId = "Semua kata kerja Korea di kamus berakhiran -다 (가다 = pergi, 먹다 = makan). Untuk berbicara sopan sehari-hari, buang -다 lalu ikuti 3 rumus singkat: ① Vokal akhir ㅏ/ㅗ → tambah 아요. ② Vokal lain → tambah 어요. ③ Berakhiran 하다 → berubah jadi 해요! Selesai pelajaran ini, kamu resmi siap naik ke TOPIK 1급!",
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
                    promptEn = "You graduated the Zero-to-Seoul Syllabus! What does 한국어를 공부해요 mean?",
                    promptId = "Kamu lulus Silabus Pemula dari Nol! Apa arti 한국어를 공부해요?",
                    stimulusKo = "한국어를 공부해요",
                    options = listOf("I study Korean / Saya belajar bahasa Korea", "I drink coffee", "I go to school", "Nice to meet you"),
                    correctOption = "I study Korean / Saya belajar bahasa Korea",
                    explanationEn = "한국어를 (Korean) + 공부해요 (study) = I study Korean! You're ready for TOPIK 1급!",
                    explanationId = "한국어를 (bahasa Korea) + 공부해요 (belajar) = Saya belajar bahasa Korea! Kamu siap naik ke TOPIK 1급!"
                )
            ),
            practiceAction = BeginnerPracticeAction.BEGINNER_FLASHCARDS,
            practiceLabelEn = "Graduate to TOPIK 1급 Flashcards",
            practiceLabelId = "Lanjut ke Flashcard TOPIK 1급"
        )
    )

    fun lessonById(id: String): BeginnerLesson? = lessons.firstOrNull { it.id == id }

    fun lessonsForStage(stageNumber: Int): List<BeginnerLesson> =
        lessons.filter { it.stageNumber == stageNumber }

    fun nextRecommendedLesson(completedIds: Set<String>): BeginnerLesson =
        lessons.firstOrNull { it.id !in completedIds } ?: lessons.last()

    fun completionCount(completedIds: Set<String>): Int =
        lessons.count { it.id in completedIds }

    fun completionFraction(completedIds: Set<String>): Float =
        (completionCount(completedIds).toFloat() / lessons.size.coerceAtLeast(1)).coerceIn(0f, 1f)

    fun isGraduated(completedIds: Set<String>): Boolean =
        completionCount(completedIds) >= lessons.size
}
