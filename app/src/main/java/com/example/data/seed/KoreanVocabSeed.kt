package com.example.data.seed

import com.example.data.local.CurriculumPathway
import com.example.data.local.VocabEntity

/**
 * Curated, linguistically vetted Korean vocabulary dataset for Baeum.
 * All entries are original educational study entries (not copied from official exam papers)
 * covering Hangul Fundamentals, TOPIK I (1–2급), TOPIK II (3–6급), EPS-TOPIK (6 domains),
 * and Everyday Korean / Seoul Life.
 */
object KoreanVocabSeed {

    fun allEntries(): List<VocabEntity> =
        hangulVocab + topik1Vocab + topik2Vocab + epsTopikVocab + everydayVocab

    private val hangulVocab = listOf(
        VocabEntity(
            id = "hg_01", hangul = "가방", romanization = "ga-bang",
            meaningEn = "Bag", meaningId = "Tas", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Basic Syllables",
            exampleKo = "이 가방이 아주 가벼워요.", exampleEn = "This bag is very light.",
            exampleId = "Tas ini sangat ringan.",
            pronunciationNote = "Initial ㄱ sounds lightly voiceless; final ㅇ is [ng].",
            relatedWords = "책 • 지갑 • 우산"
        ),
        VocabEntity(
            id = "hg_02", hangul = "나무", romanization = "na-mu",
            meaningEn = "Tree / Wood", meaningId = "Pohon / Kayu", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Basic Syllables",
            exampleKo = "공원에 큰 나무가 많아요.", exampleEn = "There are many tall trees in the park.",
            exampleId = "Di taman ada banyak pohon besar.",
            pronunciationNote = "Open syllables without batchim; clear ㄴ + ㅏ and ㅁ + ㅜ.",
            relatedWords = "꽃 • 숲 • 잎"
        ),
        VocabEntity(
            id = "hg_03", hangul = "바다", romanization = "ba-da",
            meaningEn = "Sea / Ocean", meaningId = "Laut", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Basic Syllables",
            exampleKo = "여름에 부산 바다에 가요.", exampleEn = "I go to the Busan sea in summer.",
            exampleId = "Saya pergi ke laut Busan saat musim panas.",
            pronunciationNote = "ㅂ is slightly softer initially; medial ㄷ voices to [d].",
            relatedWords = "산 • 강 • 해변"
        ),
        VocabEntity(
            id = "hg_04", hangul = "꽃", romanization = "kkot",
            meaningEn = "Flower", meaningId = "Bunga", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Batchim & Tense",
            exampleKo = "봄에 벚꽃이 정말 예뻐요.", exampleEn = "Cherry blossoms are really pretty in spring.",
            exampleId = "Bunga sakura sangat indah di musim semi.",
            pronunciationNote = "[꼳] — Final ㅊ neutralizes to unreleased [t] when standing alone; links as [꼬치] before 이.",
            relatedWords = "벚꽃 • 나무 • 화분"
        ),
        VocabEntity(
            id = "hg_05", hangul = "한국어", romanization = "han-gu-geo",
            meaningEn = "Korean language", meaningId = "Bahasa Korea", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Sound Changes",
            exampleKo = "매일 한국어를 공부해요.", exampleEn = "I study Korean every day.",
            exampleId = "Saya belajar bahasa Korea setiap hari.",
            pronunciationNote = "[한구거] — Liaison (연음): final ㄱ in 국 moves to silent ㅇ in 어.",
            hanja = "韓國語", relatedWords = "한글 • 공부 • 단어"
        ),
        VocabEntity(
            id = "hg_06", hangul = "같이", romanization = "ga-chi",
            meaningEn = "Together", meaningId = "Bersama", partOfSpeech = "Adverb",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Sound Changes",
            exampleKo = "점심 같이 먹을까요?", exampleEn = "Shall we eat lunch together?",
            exampleId = "Mau makan siang bersama?",
            pronunciationNote = "[가치] — Palatalization (구개음화): ㅌ + 이 shifts to [치].",
            relatedWords = "함께 • 혼자 • 친구"
        ),
        VocabEntity(
            id = "hg_07", hangul = "물", romanization = "mul",
            meaningEn = "Water", meaningId = "Air", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Batchim & Tense",
            exampleKo = "물 한 잔 주세요.", exampleEn = "Please give me a glass of water.",
            exampleId = "Tolong beri saya segelas air.",
            pronunciationNote = "Batchim ㄹ is pronounced as a clear [l] with tongue tip on ridge.",
            relatedWords = "차 • 커피 • 우유"
        ),
        VocabEntity(
            id = "hg_08", hangul = "빵", romanization = "ppang",
            meaningEn = "Bread", meaningId = "Roti", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.HANGUL, subCategory = "Batchim & Tense",
            exampleKo = "아침에 따뜻한 빵을 먹었어요.", exampleEn = "I ate warm bread in the morning.",
            exampleId = "Saya makan roti hangat di pagi hari.",
            pronunciationNote = "Tense consonant ㅃ has no puff of air; sharp onset.",
            relatedWords = "우유 • 커피 • 케이크"
        )
    )

    private val topik1Vocab = listOf(
        VocabEntity(
            id = "tp1_01", hangul = "안녕하세요", romanization = "an-nyeong-ha-se-yo",
            meaningEn = "Hello (polite)", meaningId = "Halo (sopan)", partOfSpeech = "Expression",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "안녕하세요, 만나서 반갑습니다.", exampleEn = "Hello, nice to meet you.",
            exampleId = "Halo, senang bertemu dengan Anda.",
            pronunciationNote = "Natural smooth phrasing; polite 해요체 greeting.",
            formalityNote = "Polite (해요체). Use 안녕하십니까 for formal settings; 안녕 with close friends.",
            hanja = "安寧", relatedWords = "감사합니다 • 반갑습니다 • 안녕히 가세요"
        ),
        VocabEntity(
            id = "tp1_02", hangul = "학교", romanization = "hak-gyo",
            meaningEn = "School", meaningId = "Sekolah", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "저는 매일 학교에 갑니다.", exampleEn = "I go to school every day.",
            exampleId = "Saya pergi ke sekolah setiap hari.",
            pronunciationNote = "[학꾜] — Tensification (경음화): ㄱ after ㄱ batchim tenses to ㄲ.",
            hanja = "學校", relatedWords = "학생 • 선생님 • 교실"
        ),
        VocabEntity(
            id = "tp1_03", hangul = "먹다", romanization = "meok-da",
            meaningEn = "To eat", meaningId = "Makan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "저는 밥을 먹어요.", exampleEn = "I eat a meal.",
            exampleId = "Saya makan nasi.",
            pronunciationNote = "[먹따] — Tensification after ㄱ batchim.",
            grammarNote = "Regular consonant-stem verb; takes -어요 in polite present.",
            conjugationsSerialized = "먹어요::Polite present||먹습니다::Formal present||먹었어요::Polite past||먹을 거예요::Polite future||드시다::Honorific equivalent",
            formalityNote = "Use 드시다 or 잡수시다 when speaking respectfully about elders.",
            honorificForm = "드시다 (드세요)", relatedWords = "음식 • 밥 • 식사 • 마시다"
        ),
        VocabEntity(
            id = "tp1_04", hangul = "가다", romanization = "ga-da",
            meaningEn = "To go", meaningId = "Pergi", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "주말에 서울역에 가요.", exampleEn = "I am going to Seoul Station on the weekend.",
            exampleId = "Saya pergi ke Stasiun Seoul akhir pekan ini.",
            pronunciationNote = "Clear open vowel stem; contracts 가 + 아요 → 가요.",
            grammarNote = "Pair with location particle -에 (destination).",
            conjugationsSerialized = "가요::Polite present||갑니다::Formal present||갔어요::Polite past||갈 거예요::Polite future||가시다::Honorific form",
            honorificForm = "가시다 (가세요)", relatedWords = "오다 • 다니다 • 출발하다"
        ),
        VocabEntity(
            id = "tp1_05", hangul = "사랑", romanization = "sa-rang",
            meaningEn = "Love", meaningId = "Cinta", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "가족의 사랑은 소중합니다.", exampleEn = "A family's love is precious.",
            exampleId = "Cinta keluarga sangat berharga.",
            pronunciationNote = "Intervocalic ㄹ is a light alveolar flap.",
            relatedWords = "마음 • 가족 • 행복"
        ),
        VocabEntity(
            id = "tp1_06", hangul = "감사합니다", romanization = "gam-sa-ham-ni-da",
            meaningEn = "Thank you (formal)", meaningId = "Terima kasih (formal)", partOfSpeech = "Expression",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "도와주셔서 정말 감사합니다.", exampleEn = "Thank you very much for helping me.",
            exampleId = "Terima kasih banyak telah membantu saya.",
            pronunciationNote = "[감사함니다] — Nasalization (비음화): ㅂ before ㄴ becomes [ㅁ].",
            formalityNote = "Formal polite (합니다체). Polite everyday equivalent: 고마워요.",
            hanja = "感謝", relatedWords = "고맙습니다 • 죄송합니다 • 괜찮아요"
        ),
        VocabEntity(
            id = "tp1_07", hangul = "공부하다", romanization = "gong-bu-ha-da",
            meaningEn = "To study", meaningId = "Belajar", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "도서관에서 한국어를 공부해요.", exampleEn = "I study Korean at the library.",
            exampleId = "Saya belajar bahasa Korea di perpustakaan.",
            pronunciationNote = "하다 verbs conjugate to 해요 in polite present.",
            conjugationsSerialized = "공부해요::Polite present||공부합니다::Formal present||공부했어요::Polite past||공부할 거예요::Polite future||공부하시다::Honorific form",
            hanja = "工夫", relatedWords = "배우다 • 복습하다 • 시험"
        ),
        VocabEntity(
            id = "tp1_08", hangul = "마시다", romanization = "ma-si-da",
            meaningEn = "To drink", meaningId = "Minum", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "아침마다 따뜻한 차를 마셔요.", exampleEn = "I drink warm tea every morning.",
            exampleId = "Saya minum teh hangat setiap pagi.",
            pronunciationNote = "시 sounds like [shi]; 시 + 어요 contracts to 셔요.",
            conjugationsSerialized = "마셔요::Polite present||마십니다::Formal present||마셨어요::Polite past||마실 거예요::Polite future||드시다::Honorific equivalent",
            honorificForm = "드시다", relatedWords = "물 • 커피 • 차"
        ),
        VocabEntity(
            id = "tp1_09", hangul = "친구", romanization = "chin-gu",
            meaningEn = "Friend", meaningId = "Teman", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "오랜만에 친구를 만났어요.", exampleEn = "I met a friend for the first time in a long while.",
            exampleId = "Saya bertemu teman setelah sekian lama.",
            pronunciationNote = "Aspirated ㅊ at start; voiced [g] in 구 after ㄴ.",
            hanja = "親舊", relatedWords = "동료 • 가족 • 만나다"
        ),
        VocabEntity(
            id = "tp1_10", hangul = "시간", romanization = "si-gan",
            meaningEn = "Time / Hour", meaningId = "Waktu / Jam", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "오늘 저녁에 시간 있어요?", exampleEn = "Do you have time this evening?",
            exampleId = "Apakah kamu punya waktu malam ini?",
            pronunciationNote = "Pair with Native Korean numbers for duration (두 시간 = 2 hours).",
            hanja = "時間", relatedWords = "분 • 약속 • 오늘"
        ),
        VocabEntity(
            id = "tp1_11", hangul = "병원", romanization = "byeong-won",
            meaningEn = "Hospital / Clinic", meaningId = "Rumah sakit / Klinik", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "감기에 걸려서 병원에 갔어요.", exampleEn = "I caught a cold so I went to the hospital.",
            exampleId = "Karena flu, saya pergi ke rumah sakit.",
            hanja = "病院", relatedWords = "약국 • 의사 • 아프다"
        ),
        VocabEntity(
            id = "tp1_12", hangul = "읽다", romanization = "ik-da",
            meaningEn = "To read", meaningId = "Membaca", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "자기 전에 책을 읽어요.", exampleEn = "I read a book before going to sleep.",
            exampleId = "Saya membaca buku sebelum tidur.",
            pronunciationNote = "[익따] in dictionary form; [일거요] when followed by vowel -어요!",
            grammarNote = "Double batchim ㄺ pronounces ㄱ before consonants, but both ㄹ and ㄱ across vowel suffix.",
            conjugationsSerialized = "읽어요::Polite present [일거요]||읽습니다::Formal present [익씀니다]||읽었어요::Polite past||읽을 거예요::Polite future",
            relatedWords = "책 • 쓰다 • 신문"
        ),
        VocabEntity(
            id = "tp1_13", hangul = "약속", romanization = "yak-sok",
            meaningEn = "Promise / Appointment", meaningId = "Janji / Pertemuan", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "2급",
            exampleKo = "내일 오후에 중요한 약속이 있어요.", exampleEn = "I have an important appointment tomorrow afternoon.",
            exampleId = "Besok sore saya ada janji penting.",
            pronunciationNote = "[약쏙] — Tensification of ㅅ after ㄱ batchim.",
            hanja = "約束", relatedWords = "시간 • 만나다 • 지키다"
        ),
        VocabEntity(
            id = "tp1_14", hangul = "준비하다", romanization = "jun-bi-ha-da",
            meaningEn = "To prepare", meaningId = "Mempersiapkan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "2급",
            exampleKo = "한국어 능력 시험을 준비하고 있어요.", exampleEn = "I am preparing for the Korean proficiency exam.",
            exampleId = "Saya sedang mempersiapkan ujian kemampuan bahasa Korea.",
            conjugationsSerialized = "준비해요::Polite present||준비합니다::Formal present||준비했어요::Polite past||준비할 거예요::Polite future",
            hanja = "準備", relatedWords = "시험 • 연습 • 계획"
        ),
        VocabEntity(
            id = "tp1_15", hangul = "도착하다", romanization = "do-cha-ka-da",
            meaningEn = "To arrive", meaningId = "Tiba / Sampai", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "2급",
            exampleKo = "인천공항에 방금 도착했어요.", exampleEn = "I just arrived at Incheon Airport.",
            exampleId = "Saya baru saja tiba di Bandara Incheon.",
            pronunciationNote = "[도차카다] — Aspiration (격음화): ㄱ + ㅎ merges to [ㅋ].",
            conjugationsSerialized = "도착해요::Polite present||도착합니다::Formal present||도착했어요::Polite past||도착할 거예요::Polite future",
            hanja = "到着", relatedWords = "출발하다 • 공항 • 역"
        ),
        VocabEntity(
            id = "tp1_16", hangul = "돕다", romanization = "dop-da",
            meaningEn = "To help", meaningId = "Membantu", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "2급",
            exampleKo = "어려울 때 서로 도와요.", exampleEn = "We help each other in difficult times.",
            exampleId = "Kita saling membantu saat kesulitan.",
            pronunciationNote = "[돕따] — ㅂ-irregular verb: ㅂ changes to 오 before -아요 → 도와요!",
            grammarNote = "ㅂ irregular (special exception taking -와요 instead of -워요).",
            conjugationsSerialized = "도와요::Polite present (ㅂ-irregular)||돕습니다::Formal present||도왔어요::Polite past||도울 거예요::Polite future",
            relatedWords = "도움 • 부탁하다 • 고맙다"
        ),
        VocabEntity(
            id = "tp1_17", hangul = "듣다", romanization = "deut-da",
            meaningEn = "To listen / hear", meaningId = "Mendengarkan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "2급",
            exampleKo = "출근할 때 한국 음악을 들어요.", exampleEn = "I listen to Korean music on my way to work.",
            exampleId = "Saya mendengarkan musik Korea saat berangkat kerja.",
            pronunciationNote = "[듣따] — ㄷ-irregular verb: ㄷ changes to ㄹ before vowel endings → 들어요.",
            grammarNote = "ㄷ irregular verb (듣다 → 들어요, 들었어요).",
            conjugationsSerialized = "들어요::Polite present (ㄷ-irregular)||듣습니다::Formal present||들었어요::Polite past||들을 거예요::Polite future",
            relatedWords = "음악 • 말하다 • 질문"
        ),
        VocabEntity(
            id = "tp1_18", hangul = "날씨", romanization = "nal-ssi",
            meaningEn = "Weather", meaningId = "Cuaca", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "2급",
            exampleKo = "오늘 날씨가 맑고 따뜻해요.", exampleEn = "Today's weather is clear and warm.",
            exampleId = "Cuaca hari ini cerah dan hangat.",
            pronunciationNote = "Batchim ㄹ followed by tense ㅆ.",
            relatedWords = "비 • 눈 • 맑다 • 흐리다"
        )
    )

    private val topik2Vocab = listOf(
        VocabEntity(
            id = "tp2_01", hangul = "경험", romanization = "gyeong-heom",
            meaningEn = "Experience", meaningId = "Pengalaman", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "3급",
            exampleKo = "해외에서 일한 경험이 큰 도움이 되었어요.", exampleEn = "The experience of working abroad was a great help.",
            exampleId = "Pengalaman bekerja di luar negeri sangat membantu.",
            hanja = "經驗", relatedWords = "기회 • 경력 • 배우다"
        ),
        VocabEntity(
            id = "tp2_02", hangul = "해결하다", romanization = "hae-gyeol-ha-da",
            meaningEn = "To solve / resolve", meaningId = "Menyelesaikan / Memecahkan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "3급",
            exampleKo = "대화를 통해 오해를 해결했습니다.", exampleEn = "We resolved the misunderstanding through conversation.",
            exampleId = "Kami menyelesaikan kesalahpahaman melalui percakapan.",
            conjugationsSerialized = "해결해요::Polite present||해결합니다::Formal present||해결했어요::Polite past||해결할 거예요::Polite future",
            hanja = "解決", relatedWords = "문제 • 방법 • 원인"
        ),
        VocabEntity(
            id = "tp2_03", hangul = "문화", romanization = "mun-hwa",
            meaningEn = "Culture", meaningId = "Budaya", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "3급",
            exampleKo = "한국의 전통 문화에 관심이 많아요.", exampleEn = "I have a strong interest in traditional Korean culture.",
            exampleId = "Saya sangat tertarik dengan budaya tradisional Korea.",
            hanja = "文化", relatedWords = "전통 • 사회 • 역사"
        ),
        VocabEntity(
            id = "tp2_04", hangul = "영향", romanization = "yeong-hyang",
            meaningEn = "Influence / Impact", meaningId = "Pengaruh / Dampak", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "4급",
            exampleKo = "수면 부족은 집중력에 나쁜 영향을 미칩니다.", exampleEn = "Lack of sleep has a negative impact on concentration.",
            exampleId = "Kurang tidur berdampak buruk pada konsentrasi.",
            grammarNote = "Common collocation: 영향을 미치다 (exert influence), 영향을 받다 (be influenced).",
            hanja = "影響", relatedWords = "결과 • 원인 • 변화"
        ),
        VocabEntity(
            id = "tp2_05", hangul = "비교하다", romanization = "bi-gyo-ha-da",
            meaningEn = "To compare", meaningId = "Membandingkan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "4급",
            exampleKo = "두 제품의 가격과 품질을 비교해 보세요.", exampleEn = "Try comparing the price and quality of the two products.",
            exampleId = "Coba bandingkan harga dan kualitas kedua produk.",
            conjugationsSerialized = "비교해요::Polite present||비교합니다::Formal present||비교했어요::Polite past||비교할 거예요::Polite future",
            hanja = "比較", relatedWords = "차이 • 비슷하다 • 선택"
        ),
        VocabEntity(
            id = "tp2_06", hangul = "유지하다", romanization = "yu-ji-ha-da",
            meaningEn = "To maintain / preserve", meaningId = "Mempertahankan / Menjaga", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "4급",
            exampleKo = "건강을 유지하기 위해 꾸준히 운동합니다.", exampleEn = "I exercise steadily in order to maintain my health.",
            exampleId = "Saya berolahraga secara rutin untuk menjaga kesehatan.",
            conjugationsSerialized = "유지해요::Polite present||유지합니다::Formal present||유지했어요::Polite past||유지할 거예요::Polite future",
            hanja = "維持", relatedWords = "건강 • 습관 • 관리"
        ),
        VocabEntity(
            id = "tp2_07", hangul = "필수적이다", romanization = "pil-su-jeo-gi-da",
            meaningEn = "To be essential / indispensable", meaningId = "Sangat penting / Wajib", partOfSpeech = "Adjective",
            curriculum = CurriculumPathway.TOPIK, subCategory = "5급",
            exampleKo = "디지털 시대에는 정보 판별 능력이 필수적이다.", exampleEn = "In the digital era, information literacy is essential.",
            exampleId = "Di era digital, kemampuan memilah informasi sangat penting.",
            pronunciationNote = "[필쑤저기다] — Written academic style (한다체) common in TOPIK II essays.",
            hanja = "必須的", relatedWords = "중요하다 • 필요하다 • 핵심"
        ),
        VocabEntity(
            id = "tp2_08", hangul = "개선하다", romanization = "gae-seon-ha-da",
            meaningEn = "To improve / reform", meaningId = "Memperbaiki / Meningkatkan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.TOPIK, subCategory = "5급",
            exampleKo = "근무 환경을 개선하기 위한 정책이 시행되었다.", exampleEn = "A policy to improve the working environment was implemented.",
            exampleId = "Kebijakan untuk memperbaiki lingkungan kerja telah diterapkan.",
            conjugationsSerialized = "개선해요::Polite present||개선합니다::Formal present||개선했다::Plain academic past||개선될 것이다::Passive future",
            hanja = "改善", relatedWords = "발전 • 변화 • 정책"
        ),
        VocabEntity(
            id = "tp2_09", hangul = "지속가능하다", romanization = "ji-sok-ga-neung-ha-da",
            meaningEn = "To be sustainable", meaningId = "Berkelanjutan", partOfSpeech = "Adjective",
            curriculum = CurriculumPathway.TOPIK, subCategory = "6급",
            exampleKo = "지속가능한 발전을 위해 친환경 에너지를 확대해야 한다.", exampleEn = "To achieve sustainable development, we must expand eco-friendly energy.",
            exampleId = "Untuk pembangunan berkelanjutan, energi ramah lingkungan harus diperluas.",
            pronunciationNote = "[지속까능하다] — High-frequency TOPIK II Level 6 essay vocabulary.",
            hanja = "持續可能", relatedWords = "환경 • 발전 • 에너지"
        ),
        VocabEntity(
            id = "tp2_10", hangul = "상호작용", romanization = "sang-ho-ja-gyong",
            meaningEn = "Interaction", meaningId = "Interaksi", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.TOPIK, subCategory = "6급",
            exampleKo = "언어 습득에서 학습자 간의 상호작용은 핵심 역할을 한다.", exampleEn = "Interaction among learners plays a key role in language acquisition.",
            exampleId = "Interaksi antar pembelajar berperan penting dalam pemerolehan bahasa.",
            hanja = "相互作用", relatedWords = "소통 • 관계 • 협력"
        )
    )

    private val epsTopikVocab = listOf(
        // Daily communication (일상생활)
        VocabEntity(
            id = "eps_01", hangul = "출근하다", romanization = "chul-geun-ha-da",
            meaningEn = "To go to work / clock in", meaningId = "Berangkat kerja", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Daily communication",
            exampleKo = "매일 아침 여덟 시까지 공장에 출근합니다.", exampleEn = "I commute to the factory by 8 a.m. every morning.",
            exampleId = "Saya berangkat kerja ke pabrik sebelum jam 8 setiap pagi.",
            conjugationsSerialized = "출근해요::Polite present||출근합니다::Formal present||출근했어요::Polite past||출근할 거예요::Polite future",
            hanja = "出勤", relatedWords = "퇴근하다 • 근무 • 지각"
        ),
        VocabEntity(
            id = "eps_02", hangul = "월급", romanization = "wol-geup",
            meaningEn = "Monthly salary / paycheck", meaningId = "Gaji bulanan", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Daily communication",
            exampleKo = "매월 25일에 통장으로 월급이 들어옵니다.", exampleEn = "Monthly salary is deposited into my bank account on the 25th.",
            exampleId = "Gaji bulanan masuk ke rekening setiap tanggal 25.",
            hanja = "月給", relatedWords = "급여명세서 • 통장 • 야근수당"
        ),
        VocabEntity(
            id = "eps_03", hangul = "기숙사", romanization = "gi-suk-sa",
            meaningEn = "Dormitory", meaningId = "Asrama", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Daily communication",
            exampleKo = "회사 근처 기숙사에서 동료들과 지냅니다.", exampleEn = "I stay with coworkers in a dormitory near the company.",
            exampleId = "Saya tinggal bersama rekan kerja di asrama dekat perusahaan.",
            pronunciationNote = "[기숙싸] — Tensification of ㅅ after ㄱ batchim.",
            hanja = "寄宿舍", relatedWords = "방 • 규칙 • 청소"
        ),
        // Manufacturing (제조업)
        VocabEntity(
            id = "eps_04", hangul = "공장", romanization = "gong-jang",
            meaningEn = "Factory / Plant", meaningId = "Pabrik", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Manufacturing",
            exampleKo = "작업이 끝나면 공장 바닥을 깨끗이 정리하세요.", exampleEn = "Please clean and organize the factory floor after work finishes.",
            exampleId = "Rapikan lantai pabrik setelah pekerjaan selesai.",
            hanja = "工場", relatedWords = "작업장 • 기계 • 생산"
        ),
        VocabEntity(
            id = "eps_05", hangul = "용접하다", romanization = "yong-jeo-pa-da",
            meaningEn = "To weld", meaningId = "Mengelas", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Manufacturing",
            exampleKo = "용접할 때는 반드시 보안면을 써야 합니다.", exampleEn = "You must wear a welding face shield when welding.",
            exampleId = "Saat mengelas, wajib memakai pelindung wajah.",
            pronunciationNote = "[용저파다] — Aspiration: ㅂ + ㅎ merges into [ㅍ].",
            conjugationsSerialized = "용접해요::Polite present||용접합니다::Formal present||용접했어요::Polite past",
            hanja = "鎔接", relatedWords = "절단하다 • 조립하다 • 보안면"
        ),
        VocabEntity(
            id = "eps_06", hangul = "조립하다", romanization = "jo-ri-pa-da",
            meaningEn = "To assemble", meaningId = "Merakit", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Manufacturing",
            exampleKo = "설명서를 보고 부품을 정확하게 조립하십시오.", exampleEn = "Please assemble the parts accurately following the manual.",
            exampleId = "Rakit komponen secara akurat sesuai buku petunjuk.",
            pronunciationNote = "[조리파다] — Aspiration: ㅂ + ㅎ → [ㅍ].",
            conjugationsSerialized = "조립해요::Polite present||조립합니다::Formal present||조립했어요::Polite past",
            hanja = "組立", relatedWords = "부품 • 검사하다 • 포장하다"
        ),
        // Workplace safety (산업안전)
        VocabEntity(
            id = "eps_07", hangul = "안전모", romanization = "an-jeon-mo",
            meaningEn = "Safety helmet / Hard hat", meaningId = "Helm keselamatan", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Workplace safety",
            exampleKo = "현장에 들어가기 전에 안전모를 꼭 착용하세요.", exampleEn = "Always wear a safety helmet before entering the worksite.",
            exampleId = "Selalu kenakan helm keselamatan sebelum memasuki area kerja.",
            hanja = "安全帽", relatedWords = "안전화 • 보안경 • 보호구"
        ),
        VocabEntity(
            id = "eps_08", hangul = "위험", romanization = "wi-heom",
            meaningEn = "Danger / Hazard", meaningId = "Bahaya", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Workplace safety",
            exampleKo = "기계가 작동 중일 때는 손을 넣으면 매우 위험합니다.", exampleEn = "Putting your hand in while the machine is running is extremely dangerous.",
            exampleId = "Memasukkan tangan saat mesin beroperasi sangat berbahaya.",
            hanja = "危險", relatedWords = "주의 • 금지 • 비상구"
        ),
        VocabEntity(
            id = "eps_09", hangul = "소화기", romanization = "so-hwa-gi",
            meaningEn = "Fire extinguisher", meaningId = "Alat pemadam api (APAR)", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Workplace safety",
            exampleKo = "비상시를 대비해 소화기 위치를 미리 확인하세요.", exampleEn = "Check the location of the fire extinguisher in advance for emergencies.",
            exampleId = "Periksa lokasi pemadam api sebelumnya untuk keadaan darurat.",
            hanja = "消火器", relatedWords = "화재 • 비상구 • 대피"
        ),
        // Agriculture (농업)
        VocabEntity(
            id = "eps_10", hangul = "비닐하우스", romanization = "bi-nil-ha-u-seu",
            meaningEn = "Greenhouse / Vinyl house", meaningId = "Rumah kaca plastik", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Agriculture",
            exampleKo = "비닐하우스 안의 온도와 습도를 확인해야 합니다.", exampleEn = "We need to check the temperature and humidity inside the greenhouse.",
            exampleId = "Kita perlu memeriksa suhu dan kelembapan di dalam greenhouse.",
            relatedWords = "농작물 • 수확하다 • 비료"
        ),
        VocabEntity(
            id = "eps_11", hangul = "수확하다", romanization = "su-hwa-ka-da",
            meaningEn = "To harvest", meaningId = "Memanen", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Agriculture",
            exampleKo = "가을에는 잘 익은 사과를 수확합니다.", exampleEn = "In autumn, we harvest ripe apples.",
            exampleId = "Di musim gugur, kami memanen apel yang sudah matang.",
            pronunciationNote = "[수화카다] — Aspiration: ㄱ + ㅎ → [ㅋ].",
            hanja = "收穫", relatedWords = "심다 • 농장 • 작물"
        ),
        // Fisheries (어업)
        VocabEntity(
            id = "eps_12", hangul = "양식장", romanization = "yang-sik-jang",
            meaningEn = "Fish farm / Aquaculture site", meaningId = "Tambak / Budidaya ikan", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Fisheries",
            exampleKo = "양식장에서 물고기에게 사료를 줍니다.", exampleEn = "We feed the fish at the aquaculture farm.",
            exampleId = "Kami memberi pakan ikan di tambak budidaya.",
            pronunciationNote = "[양식짱] — Tensification of ㅈ after ㄱ batchim.",
            hanja = "養殖場", relatedWords = "그물 • 어선 • 구명조끼"
        ),
        VocabEntity(
            id = "eps_13", hangul = "구명조끼", romanization = "gu-myeong-jo-kki",
            meaningEn = "Life jacket", meaningId = "Jaket pelampung", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Fisheries",
            exampleKo = "배에 탈 때는 반드시 구명조끼를 입으십시오.", exampleEn = "You must wear a life jacket when boarding the boat.",
            exampleId = "Wajib mengenakan jaket pelampung saat naik kapal.",
            relatedWords = "어선 • 안전 • 바다"
        ),
        // Workplace culture (직장문화)
        VocabEntity(
            id = "eps_14", hangul = "보고하다", romanization = "bo-go-ha-da",
            meaningEn = "To report (to a supervisor)", meaningId = "Melaporkan (ke atasan)", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Workplace culture",
            exampleKo = "문제가 생기면 즉시 반장님께 보고해야 합니다.", exampleEn = "If a problem arises, you must report it immediately to the team leader.",
            exampleId = "Jika terjadi masalah, segera laporkan kepada mandor/ketua tim.",
            formalityNote = "Use honorific particle -께 when reporting to supervisors (팀장님께 / 반장님께).",
            hanja = "報告", relatedWords = "반장님 • 지시 • 회의"
        ),
        VocabEntity(
            id = "eps_15", hangul = "수고하셨습니다", romanization = "su-go-ha-syeot-sseum-ni-da",
            meaningEn = "Thank you for your hard work", meaningId = "Terima kasih atas kerja kerasnya", partOfSpeech = "Expression",
            curriculum = CurriculumPathway.EPS_TOPIK, subCategory = "Workplace culture",
            exampleKo = "오늘도 모두 수고하셨습니다. 조심해서 들어가세요.", exampleEn = "Thank you all for your hard work today. Get home safely.",
            exampleId = "Terima kasih atas kerja keras hari ini. Hati-hati di jalan pulang.",
            pronunciationNote = "[수고하셛씀니다] — Standard Korean workplace farewell at shift end.",
            formalityNote = "Appropriate among coworkers or to juniors; to high-ranking seniors prefer '고생 많으셨습니다' or '먼저 들어가 보겠습니다'.",
            relatedWords = "퇴근 • 동료 • 회식"
        )
    )

    private val everydayVocab = listOf(
        VocabEntity(
            id = "ev_01", hangul = "주문하다", romanization = "ju-mun-ha-da",
            meaningEn = "To order (food/items)", meaningId = "Memesan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Cafe & Dining",
            exampleKo = "아이스 아메리카노 한 잔 주문할게요.", exampleEn = "I'll order one iced Americano.",
            exampleId = "Saya pesan satu gelas iced Americano.",
            conjugationsSerialized = "주문해요::Polite present||주문할게요::Polite intention||주문했어요::Polite past",
            hanja = "注文", relatedWords = "메뉴 • 계산하다 • 포장"
        ),
        VocabEntity(
            id = "ev_02", hangul = "얼마예요", romanization = "eol-ma-ye-yo",
            meaningEn = "How much is it?", meaningId = "Berapa harganya?", partOfSpeech = "Expression",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Shopping",
            exampleKo = "이 티셔츠는 얼마예요?", exampleEn = "How much is this T-shirt?",
            exampleId = "Berapa harga kaus ini?",
            formalityNote = "Polite 해요체 — natural in stores, cafes, and markets.",
            relatedWords = "가격 • 카드 • 현금 • 영수증"
        ),
        VocabEntity(
            id = "ev_03", hangul = "환승하다", romanization = "hwan-seung-ha-da",
            meaningEn = "To transfer (subway/bus)", meaningId = "Transit / Pindah jalur", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Transportation",
            exampleKo = "신도림역에서 2호선으로 환승하세요.", exampleEn = "Transfer to Line 2 at Sindorim Station.",
            exampleId = "Transit ke Jalur 2 di Stasiun Sindorim.",
            hanja = "換乘", relatedWords = "지하철 • 출구 • 교통카드"
        ),
        VocabEntity(
            id = "ev_04", hangul = "추천하다", romanization = "chu-cheon-ha-da",
            meaningEn = "To recommend", meaningId = "Merekomendasikan", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Cafe & Dining",
            exampleKo = "이 식당에서 제일 맛있는 메뉴를 추천해 주세요.", exampleEn = "Please recommend the most delicious dish at this restaurant.",
            exampleId = "Tolong rekomendasikan menu paling enak di restoran ini.",
            hanja = "推薦", relatedWords = "메뉴 • 맛있다 • 인기"
        ),
        VocabEntity(
            id = "ev_05", hangul = "예약하다", romanization = "ye-ya-ka-da",
            meaningEn = "To reserve / book", meaningId = "Memesan / Reservasi", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Travel",
            exampleKo = "금요일 저녁 일곱 시에 두 명 예약했어요.", exampleEn = "I reserved a table for two at 7 p.m. on Friday.",
            exampleId = "Saya sudah reservasi untuk dua orang hari Jumat jam 7 malam.",
            pronunciationNote = "[예야카다] — Aspiration: ㄱ + ㅎ → [ㅋ].",
            hanja = "豫約", relatedWords = "취소하다 • 확인하다 • 호텔"
        ),
        VocabEntity(
            id = "ev_06", hangul = "죄송합니다", romanization = "joe-song-ham-ni-da",
            meaningEn = "I am sorry / Excuse me (formal)", meaningId = "Maaf / Permisi (formal)", partOfSpeech = "Expression",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Social & Honorifics",
            exampleKo = "죄송합니다, 잠시만 지나가겠습니다.", exampleEn = "Excuse me, may I pass by for a moment?",
            exampleId = "Permisi, saya numpang lewat sebentar.",
            pronunciationNote = "[죄송함니다] — Nasalization: ㅂ before ㄴ → [ㅁ].",
            formalityNote = "Formal apology; use 미안해요 politely with peers and 미안해 in 반말 with close friends.",
            relatedWords = "미안해요 • 실례합니다 • 괜찮아요"
        ),
        VocabEntity(
            id = "ev_07", hangul = "면접", romanization = "myeon-jeop",
            meaningEn = "Job interview", meaningId = "Wawancara kerja", partOfSpeech = "Noun",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Interviews",
            exampleKo = "다음 주 화요일에 한국 회사 면접이 있습니다.", exampleEn = "I have an interview at a Korean company next Tuesday.",
            exampleId = "Selasa depan saya ada wawancara di perusahaan Korea.",
            hanja = "面接", relatedWords = "이력서 • 자기소개 • 지원하다"
        ),
        VocabEntity(
            id = "ev_08", hangul = "계산하다", romanization = "gye-san-ha-da",
            meaningEn = "To pay the bill / calculate", meaningId = "Membayar / Menghitung", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Shopping",
            exampleKo = "카드로 계산할게요. 영수증은 괜찮아요.", exampleEn = "I'll pay by card. No receipt needed, thank you.",
            exampleId = "Saya bayar pakai kartu. Tidak perlu struk.",
            hanja = "計算", relatedWords = "카드 • 영수증 • 할인"
        ),
        VocabEntity(
            id = "ev_09", hangul = "드시다", romanization = "deu-si-da",
            meaningEn = "To eat / drink (honorific)", meaningId = "Makan / Minum (hormat)", partOfSpeech = "Verb",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Social & Honorifics",
            exampleKo = "할머니, 진지 맛있게 드세요.", exampleEn = "Grandmother, please enjoy your meal.",
            exampleId = "Nenek, selamat menikmati hidangannya.",
            grammarNote = "Honorific suppletive verb replacing 먹다 and 마시다.",
            conjugationsSerialized = "드세요::Honorific polite present||드십니다::Honorific formal present||드셨어요::Honorific past||드실 거예요::Honorific future",
            formalityNote = "Always used for elders, teachers, managers, and honored guests.",
            honorificForm = "드시다", relatedWords = "먹다 • 진지 • 주무시다"
        ),
        VocabEntity(
            id = "ev_10", hangul = "괜찮아요", romanization = "gwaen-cha-na-yo",
            meaningEn = "It's okay / No thank you", meaningId = "Tidak apa-apa / Tidak usah", partOfSpeech = "Expression",
            curriculum = CurriculumPathway.EVERYDAY, subCategory = "Social & Honorifics",
            exampleKo = "봉투는 괜찮아요, 가방에 넣을게요.", exampleEn = "No bag needed, I'll put it in my bag.",
            exampleId = "Tidak perlu kantong, saya masukkan ke tas saja.",
            pronunciationNote = "[괜차나요] — ㄶ before vowel drops ㅎ smoothly.",
            relatedWords = "감사합니다 • 네 • 아니요"
        )
    )
}
