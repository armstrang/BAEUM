package com.example.data.seed

import com.example.data.local.CurriculumPathway

data class GrammarExample(
    val korean: String,
    val romanization: String,
    val translationEn: String,
    val translationId: String
)

data class GrammarPattern(
    val id: String,
    val title: String,
    val meaningEn: String,
    val meaningId: String,
    val summaryEn: String,
    val summaryId: String,
    val curriculum: String,
    val levelBadge: String,
    val speechLevelNote: String,
    val structureFormula: String,
    val structureDetail: String,
    val commonMistake: String,
    val examples: List<GrammarExample>,
    val quizStem: String,
    val quizCorrectOption: String,
    val quizDistractors: List<String>,
    val quizExplanation: String
)

object KoreanGrammarSeed {
    val allPatterns: List<GrammarPattern> = listOf(
        GrammarPattern(
            id = "gr_go_sipda",
            title = "-고 싶다",
            meaningEn = "Want to do something",
            meaningId = "Ingin melakukan sesuatu",
            summaryEn = "Expresses the speaker's personal wish or desire to perform an action.",
            summaryId = "Menyatakan keinginan pribadi pembicara untuk melakukan sesuatu.",
            curriculum = CurriculumPathway.TOPIK,
            levelBadge = "TOPIK I • Beginner",
            speechLevelNote = "Conjugates across speech levels: -고 싶어 (반말), -고 싶어요 (해요체), -고 싶습니다 (합니다체).",
            structureFormula = "Verb stem + -고 싶다",
            structureDetail = "Attach directly to a verb stem regardless of final batchim. For third-person subjects, use -고 싶어하다.",
            commonMistake = "Do not use -고 싶다 directly with adjectives (e.g., use 예뻐지고 싶어요 instead of 예쁘고 싶어요) or third-person statements without -어하다.",
            examples = listOf(
                GrammarExample("한국에 가고 싶어요.", "han-gu-ge ga-go si-peo-yo.", "I want to go to Korea.", "Saya ingin pergi ke Korea."),
                GrammarExample("따뜻한 국밥을 먹고 싶어요.", "tta-tteu-tan guk-ba-beul meok-go si-peo-yo.", "I want to eat warm soup with rice.", "Saya ingin makan sup nasi hangat."),
                GrammarExample("한국어를 자연스럽게 말하고 싶어요.", "han-gu-geo-reul ja-yeon-seu-reop-ge mal-ha-go si-peo-yo.", "I want to speak Korean naturally.", "Saya ingin berbicara bahasa Korea secara alami.")
            ),
            quizStem = "주말에 부산에 ( ____ ). (I want to go to Busan on the weekend.)",
            quizCorrectOption = "가고 싶어요",
            quizDistractors = listOf("가면 안 돼요", "갈 수 없어요", "갔기 때문에"),
            quizExplanation = "Attach -고 싶어요 to the verb stem 가- (가다) to express 'want to go'."
        ),
        GrammarPattern(
            id = "gr_aseo_eoseo",
            title = "-아/어서",
            meaningEn = "Because / So; Sequential action",
            meaningId = "Karena / Lalu (urutan tindakan)",
            summaryEn = "Links cause and natural effect, or two closely connected sequential actions.",
            summaryId = "Menghubungkan sebab-akibat atau dua tindakan berurutan yang erat.",
            curriculum = CurriculumPathway.TOPIK,
            levelBadge = "TOPIK I • 1급",
            speechLevelNote = "The tense is marked only on the final verb, never before -아/어서.",
            structureFormula = "Verb/Adj stem + -아서 (ㅏ/ㅗ) / -어서 (other) / 해서 (하다)",
            structureDetail = "Used for everyday reasons (감기에 걸려서 병원에 갔어요) and gratitude/apologies (도와주셔서 감사합니다).",
            commonMistake = "Never attach past tense (-았/었-) before -어서, and do not pair a reason clause using -아/어서 with imperatives (-세요) or propositions (-(으)ㄹ까요).",
            examples = listOf(
                GrammarExample("비가 와서 우산을 샀어요.", "bi-ga wa-seo u-sa-neul sa-sseo-yo.", "It rained, so I bought an umbrella.", "Karena hujan, saya membeli payung."),
                GrammarExample("늦어서 죄송합니다.", "neu-jeo-seo joe-song-ham-ni-da.", "I am sorry for being late.", "Maaf karena saya terlambat."),
                GrammarExample("친구를 만나서 영화를 봤어요.", "chin-gu-reul man-na-seo yeong-hwa-reul bwa-sseo-yo.", "I met a friend and watched a movie.", "Saya bertemu teman lalu menonton film.")
            ),
            quizStem = "배가 ( ____ ) 식당에 갔어요. (Because I was hungry, I went to a restaurant.)",
            quizCorrectOption = "고파서",
            quizDistractors = listOf("고팠어서", "고프지만", "고프려고"),
            quizExplanation = "으-irregular stem 고프- + -아서 becomes 고파서. Never add past tense before -아서."
        ),
        GrammarPattern(
            id = "gr_eul_su_itda",
            title = "-(으)ㄹ 수 있다 / 없다",
            meaningEn = "Can / Cannot (Ability or Possibility)",
            meaningId = "Bisa / Tidak bisa (Kemampuan atau Kemungkinan)",
            summaryEn = "Expresses whether someone has the ability or whether a situation allows an action.",
            summaryId = "Menyatakan kemampuan atau kemungkinan melakukan suatu tindakan.",
            curriculum = CurriculumPathway.TOPIK,
            levelBadge = "TOPIK I • 2급",
            speechLevelNote = "Use -(으)ㄹ 수 있어요 in conversation, -(으)ㄹ 수 있습니다 in formal workplace/interview speech.",
            structureFormula = "Verb stem + -ㄹ 수 있다 (vowel/ㄹ) / -을 수 있다 (consonant)",
            structureDetail = "If stem ends in ㄹ (e.g., 만들다), drop extra 을 → 만들 수 있어요.",
            commonMistake = "Avoid writing 만들을 수 있어요; stems ending in ㄹ take 수 있다 directly (만들 수 있어요).",
            examples = listOf(
                GrammarExample("한글을 빠르게 읽을 수 있어요.", "han-geu-reul ppa-reu-ge il-geul su i-sseo-yo.", "I can read Hangul quickly.", "Saya bisa membaca Hangul dengan cepat."),
                GrammarExample("여기에서는 사진을 찍을 수 없어요.", "yeo-gi-e-seo-neun sa-ji-neul jji-geul su eop-sseo-yo.", "You cannot take photos here.", "Anda tidak bisa mengambil foto di sini."),
                GrammarExample("김치찌개를 만들 수 있어요.", "gim-chi-jji-gae-reul man-deul su i-sseo-yo.", "I can make kimchi stew.", "Saya bisa membuat sup kimchi.")
            ),
            quizStem = "한국어 신문을 ( ____ ). (I can read Korean newspapers.)",
            quizCorrectOption = "읽을 수 있어요",
            quizDistractors = listOf("읽고 싶어요", "읽으면 안 돼요", "읽어야 해요"),
            quizExplanation = "Consonant stem 읽- takes -을 수 있어요 to express ability."
        ),
        GrammarPattern(
            id = "gr_myeon_an_doeda",
            title = "-(으)면 안 되다",
            meaningEn = "Must not / Is not allowed",
            meaningId = "Tidak boleh / Dilarang",
            summaryEn = "States workplace rules, safety prohibitions, and social boundaries.",
            summaryId = "Menyatakan larangan keselamatan kerja dan aturan sosial.",
            curriculum = CurriculumPathway.EPS_TOPIK,
            levelBadge = "EPS-TOPIK • Safety",
            speechLevelNote = "Very frequent in factory safety briefings and EPS-TOPIK reading/listening questions.",
            structureFormula = "Verb stem + -면 안 되다 (vowel/ㄹ) / -으면 안 되다 (consonant)",
            structureDetail = "Combine with 꼭/절대 for strict industrial safety instructions.",
            commonMistake = "Do not confuse with -지 않아도 되다 ('don't have to'). -(으)면 안 되다 means strict prohibition.",
            examples = listOf(
                GrammarExample("작업장 안에서는 담배를 피우면 안 됩니다.", "ja-geop-jang a-ne-seo-neun dam-bae-reul pi-u-myeon an doem-ni-da.", "You must not smoke inside the workplace.", "Dilarang merokok di dalam area kerja."),
                GrammarExample("안전모를 쓰지 않고 들어가면 안 돼요.", "an-jeon-mo-reul sseu-ji an-ko deu-reo-ga-myeon an dwae-yo.", "You must not enter without wearing a safety helmet.", "Tidak boleh masuk tanpa memakai helm keselamatan."),
                GrammarExample("기계가 돌 때 만지면 안 됩니다.", "gi-gye-ga dol ttae man-ji-myeon an doem-ni-da.", "You must not touch the machine while it is running.", "Tidak boleh menyentuh mesin saat sedang berputar.")
            ),
            quizStem = "위험 구역에 허가 없이 ( ____ ). (Must not enter without permission.)",
            quizCorrectOption = "들어가면 안 됩니다",
            quizDistractors = listOf("들어가도 됩니다", "들어가려고 합니다", "들어갈 수 있습니다"),
            quizExplanation = "-(으)면 안 됩니다 expresses formal prohibition in safety rules."
        ),
        GrammarPattern(
            id = "gr_aya_eoya_hada",
            title = "-아/어야 하다 (되다)",
            meaningEn = "Must / Have to / Should",
            meaningId = "Harus / Wajib",
            summaryEn = "Expresses obligation, duty, or required procedure in daily life and the workplace.",
            summaryId = "Menyatakan kewajiban atau prosedur yang harus dilakukan.",
            curriculum = CurriculumPathway.EPS_TOPIK,
            levelBadge = "EPS-TOPIK • Core",
            speechLevelNote = "Both -아/어야 해요 and -아/어야 돼요 are natural in speech; -아/어야 합니다 is preferred in formal instructions.",
            structureFormula = "Verb stem + -아야/어야/해야 하다",
            structureDetail = "Follows the same vowel harmony rules as -아/어요.",
            commonMistake = "Remember irregular verbs: 돕다 → 도와야 해요, 듣다 → 들어야 해요.",
            examples = listOf(
                GrammarExample("출근 전에 보호구를 점검해야 합니다.", "chul-geun jeo-ne bo-ho-gu-reul jeom-geom-hae-ya ham-ni-da.", "You must inspect protective gear before work.", "Anda harus memeriksa alat pelindung sebelum kerja."),
                GrammarExample("내일까지 서류를 제출해야 해요.", "nae-il-kka-ji seo-ryu-reul je-chul-hae-ya hae-yo.", "I have to submit the documents by tomorrow.", "Saya harus menyerahkan dokumen paling lambat besok."),
                GrammarExample("문제가 생기면 바로 보고해야 합니다.", "mun-je-ga saeng-gi-myeon ba-ro bo-go-hae-ya ham-ni-da.", "If a problem occurs, you must report it right away.", "Jika timbul masalah, harus segera melapor.")
            ),
            quizStem = "용접할 때는 보안면을 꼭 ( ____ ). (Must wear a face shield.)",
            quizCorrectOption = "써야 합니다",
            quizDistractors = listOf("쓰면 안 됩니다", "쓰지 않아도 됩니다", "쓸까요"),
            quizExplanation = "쓰다 (으-irregular) + -어야 합니다 becomes 써야 합니다 ('must wear')."
        ),
        GrammarPattern(
            id = "gr_speech_levels",
            title = "Speech Levels: 반말 • 해요체 • 합니다체",
            meaningEn = "Korean Politeness & Honorific System",
            meaningId = "Tingkatan Kesopanan Bahasa Korea",
            summaryEn = "Choosing the right sentence ending based on social distance, age, and setting.",
            summaryId = "Memilih akhiran kalimat sesuai jarak sosial, usia, dan situasi.",
            curriculum = CurriculumPathway.EVERYDAY,
            levelBadge = "Speech Level Lab",
            speechLevelNote = "핵심 원칙: Start with 해요체 or 합니다체 with strangers, coworkers, and elders; only switch to 반말 when mutually agreed.",
            structureFormula = "해요체 (-아/어요) • 합니다체 (-ㅂ/습니다) • 반말 (-아/어) • Honorific (-(으)시-)",
            structureDetail = "Honorific infix -(으)시- elevates the subject (선생님께서 오세요), while sentence ending elevates the listener.",
            commonMistake = "Never attach -(으)시- to yourself! Say '저는 먹어요', never '저는 드세요'.",
            examples = listOf(
                GrammarExample("반말: 밥 먹었어?", "bap meo-geo-sseo?", "Casual (close friend): Did you eat?", "Santai (teman dekat): Sudah makan?"),
                GrammarExample("해요체: 식사하셨어요?", "sik-sa-ha-syeo-sseo-yo?", "Polite + Honorific: Have you eaten?", "Sopan + Hormat: Apakah Anda sudah makan?"),
                GrammarExample("합니다체: 반장님, 작업이 끝났습니다.", "ban-jang-nim, ja-geo-bi kkeun-nat-sseum-ni-da.", "Formal workplace: Team leader, the task is complete.", "Formal kerja: Pak Mandor, pekerjaan telah selesai.")
            ),
            quizStem = "Speaking to your Korean manager at work: '팀장님, 회의 자료를 ( ____ ).'",
            quizCorrectOption = "준비했습니다",
            quizDistractors = listOf("준비했어", "준비하신다", "준비해"),
            quizExplanation = "합니다체 (-ㅂ/습니다) is formal and respectful toward a workplace manager without wrongly putting honorific -시- on yourself."
        ),
        GrammarPattern(
            id = "gr_gi_ttamune",
            title = "-기 때문에",
            meaningEn = "Because of / Owing to",
            meaningId = "Dikarenakan / Oleh sebab",
            summaryEn = "States a clear, objective reason; common in explanations, presentations, and TOPIK II essays.",
            summaryId = "Menyatakan alasan objektif yang jelas; umum di presentasi dan esai TOPIK II.",
            curriculum = CurriculumPathway.TOPIK,
            levelBadge = "TOPIK II • 3급",
            speechLevelNote = "More explicit and formal than -아/어서; allows past tense (-았/었기 때문에).",
            structureFormula = "Verb/Adj stem + -기 때문에 | Noun + 때문에",
            structureDetail = "Unlike -아/어서, past tense marker -았/었- is grammatically valid before -기 때문에.",
            commonMistake = "Use Noun + 때문에 (비 때문에), NOT Noun + 기 때문에.",
            examples = listOf(
                GrammarExample("교통이 편리하기 때문에 이 동네가 인기가 많습니다.", "gyo-tong-i pyeol-li-ha-gi ttae-mu-ne...", "Because transportation is convenient, this neighborhood is popular.", "Karena transportasi nyaman, lingkungan ini populer."),
                GrammarExample("미리 연습했기 때문에 발표를 잘 마쳤어요.", "mi-ri yeon-seu-paet-kki ttae-mu-ne...", "Because I practiced in advance, I finished the presentation well.", "Karena sudah berlatih sebelumnya, presentasi berjalan lancar."),
                GrammarExample("태풍 때문에 비행기가 지연되었습니다.", "tae-pung ttae-mu-ne bi-haeng-gi-ga ji-yeon-doe-eo-sseum-ni-da.", "The flight was delayed because of the typhoon.", "Penerbangan ditunda karena topan.")
            ),
            quizStem = "꾸준히 복습( ____ ) 실력이 크게 늘었습니다. (Because I reviewed steadily...)",
            quizCorrectOption = "했기 때문에",
            quizDistractors = listOf("했아서", "하더라도", "할 뿐만 아니라"),
            quizExplanation = "Past tense -았/었- combines cleanly with -기 때문에 (했기 때문에)."
        ),
        GrammarPattern(
            id = "gr_neun_ppunman_anira",
            title = "-(으)ㄹ 뿐만 아니라",
            meaningEn = "Not only ... but also ...",
            meaningId = "Tidak hanya ... tetapi juga ...",
            summaryEn = "Adds a second parallel point in formal arguments, reports, and TOPIK II writing.",
            summaryId = "Menambahkan poin kedua secara paralel dalam argumen formal dan tulisan TOPIK II.",
            curriculum = CurriculumPathway.TOPIK,
            levelBadge = "TOPIK II • 4급",
            speechLevelNote = "Frequently paired with -도 ('also') in the second clause.",
            structureFormula = "Verb/Adj stem + -(으)ㄹ 뿐만 아니라 | Noun + 뿐만 아니라",
            structureDetail = "Strong high-scoring structure for TOPIK II Question 54 argumentative essays.",
            commonMistake = "Ensure both clauses share the same positive or negative evaluative direction.",
            examples = listOf(
                GrammarExample("이 앱은 어휘뿐만 아니라 말하기도 연습할 수 있습니다.", "i ae-beun eo-hwi-ppun-man a-ni-ra...", "This app lets you practice not only vocabulary, but also speaking.", "Aplikasi ini tidak hanya melatih kosakata, tetapi juga berbicara."),
                GrammarExample("운동은 체력을 길러 줄 뿐만 아니라 스트레스도 줄여 준다.", "un-dong-eun che-ryeo-geul gil-leo jul ppun-man a-ni-ra...", "Exercise not only builds stamina, but also reduces stress.", "Olahraga tidak hanya meningkatkan stamina, tapi juga mengurangi stres.")
            ),
            quizStem = "독서는 지식을 넓혀 ( ____ ) 사고력도 키워 줍니다.",
            quizCorrectOption = "줄 뿐만 아니라",
            quizDistractors = listOf("주기 위해서", "주기는커녕", "주는 반면에"),
            quizExplanation = "-(으)ㄹ 뿐만 아니라 connects two positive effects ('not only broadens knowledge, but also builds thinking skills')."
        )
    )
}
