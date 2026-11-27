package com.example.data.seed

/**
 * Authentic NIIED-modeled TOPIK I (1–2급) 70-question mock exam bank.
 * - Listening (1~30): 20 × 3점 + 10 × 4점 = 100점 (all 2× playback in Mock mode).
 * - Reading (31~70): 20 × 2점 + 20 × 3점 = 100점.
 * - 100% pure Korean stimuli and options with bilingual post-exam explanations (EN & ID).
 */
object Topik1MockBank {

    private fun listening(
        id: String,
        typeKo: String,
        instructionKo: String,
        stimulusKo: String,
        audioKo: String,
        options: List<String>,
        correctIdx: Int,
        weight: Int,
        tier: Int,
        explanationEn: String,
        explanationId: String,
        groupId: String? = null,
        groupLabelKo: String? = null
    ) = ExamQuestion(
        id = id,
        examType = ExamType.TOPIK_I,
        section = "Listening",
        period = 1,
        questionTypeKo = typeKo,
        instructionKo = instructionKo,
        stimulusKo = stimulusKo,
        audioTranscriptKo = audioKo,
        maxAudioPlaysInMock = 2,
        sharedGroupId = groupId,
        sharedGroupLabelKo = groupLabelKo,
        options = options,
        correctOptionIndex = correctIdx,
        pointWeight = weight,
        difficultyTier = tier,
        explanationEn = explanationEn,
        explanationId = explanationId
    )

    private fun reading(
        id: String,
        typeKo: String,
        instructionKo: String,
        stimulusKo: String,
        options: List<String>,
        correctIdx: Int,
        weight: Int,
        tier: Int,
        explanationEn: String,
        explanationId: String,
        passageBoxKo: String? = null,
        underlinedText: String? = null,
        groupId: String? = null,
        groupLabelKo: String? = null
    ) = ExamQuestion(
        id = id,
        examType = ExamType.TOPIK_I,
        section = "Reading",
        period = 1,
        questionTypeKo = typeKo,
        instructionKo = instructionKo,
        passageBoxKo = passageBoxKo,
        stimulusKo = stimulusKo,
        underlinedText = underlinedText,
        sharedGroupId = groupId,
        sharedGroupLabelKo = groupLabelKo,
        options = options,
        correctOptionIndex = correctIdx,
        pointWeight = weight,
        difficultyTier = tier,
        explanationEn = explanationEn,
        explanationId = explanationId
    )

    val listeningQuestions: List<ExamQuestion> = listOf(
        // [1~4] 알맞은 대답 고르기
        listening(
            id = "t1_l01", typeKo = "알맞은 대답 고르기",
            instructionKo = "다음을 듣고 <보기>와 같이 물음에 맞는 대답을 고르십시오.",
            stimulusKo = "질문을 듣고 가장 알맞은 대답을 고르십시오.",
            audioKo = "여자: 민수 씨, 오늘 점심에 무엇을 먹을까요?",
            options = listOf("비빔밥을 먹읍시다.", "어제 친구를 만났어요.", "지하철로 삼십 분 걸려요.", "도서관에서 공부해요."),
            correctIdx = 0, weight = 3, tier = 1,
            explanationEn = "The speaker asks 'What shall we eat for lunch today?' — '비빔밥을 먹읍시다' ('Let's eat bibimbap') directly answers the suggestion.",
            explanationId = "Pembicara bertanya 'Mau makan apa siang ini?' — '비빔밥을 먹읍시다' ('Ayo makan bibimbap') adalah jawaban yang paling tepat."
        ),
        listening(
            id = "t1_l02", typeKo = "알맞은 대답 고르기",
            instructionKo = "다음을 듣고 <보기>와 같이 물음에 맞는 대답을 고르십시오.",
            stimulusKo = "질문을 듣고 가장 알맞은 대답을 고르십시오.",
            audioKo = "남자: 수진 씨, 학교에 어떻게 와요?",
            options = listOf("네, 학교가 커요.", "아홉 시에 시작해요.", "버스를 타고 와요.", "친구하고 같이 가요."),
            correctIdx = 2, weight = 3, tier = 1,
            explanationEn = "'어떻게 와요?' asks for the means of transportation ('How do you come to school?') — '버스를 타고 와요' ('I come by bus').",
            explanationId = "'어떻게 와요?' menanyakan cara transportasi ke sekolah — '버스를 타고 와요' ('Datang naik bus')."
        ),
        listening(
            id = "t1_l03", typeKo = "알맞은 대답 고르기",
            instructionKo = "다음을 듣고 <보기>와 같이 물음에 맞는 대답을 고르십시오.",
            stimulusKo = "질문을 듣고 가장 알맞은 대답을 고르십시오.",
            audioKo = "여자: 이 사과 얼마예요?",
            options = listOf("네, 아주 맛있어요.", "세 개에 오천 원이에요.", "과일 가게에 있어요.", "어제 시장에서 샀어요."),
            correctIdx = 1, weight = 3, tier = 1,
            explanationEn = "'얼마예요?' asks the price — '세 개에 오천 원이에요' ('5,000 won for three') answers directly.",
            explanationId = "'얼마예요?' menanyakan harga — '세 개에 오천 원이에요' ('5.000 won untuk tiga buah')."
        ),
        listening(
            id = "t1_l04", typeKo = "알맞은 대답 고르기",
            instructionKo = "다음을 듣고 <보기>와 같이 물음에 맞는 대답을 고르십시오.",
            stimulusKo = "질문을 듣고 가장 알맞은 대답을 고르십시오.",
            audioKo = "남자: 어제 본 영화가 어땠어요?",
            options = listOf("네, 내일 볼 거예요.", "영화관이 집 근처예요.", "생각보다 정말 재미있었어요.", "친구가 표를 샀어요."),
            correctIdx = 2, weight = 4, tier = 1,
            explanationEn = "'어땠어요?' asks for an impression of the movie watched yesterday — '생각보다 정말 재미있었어요' ('It was much more fun than expected').",
            explanationId = "'어땠어요?' menanyakan kesan tentang film kemarin — '생각보다 정말 재미있었어요' ('Jauh lebih seru daripada perkiraan')."
        ),
        // [5~6] 이어질 말 고르기
        listening(
            id = "t1_l05", typeKo = "이어질 말 고르기",
            instructionKo = "다음을 듣고 <보기>와 같이 다음 말에 이어지는 것을 고르십시오.",
            stimulusKo = "이어질 말로 가장 알맞은 것을 고르십시오.",
            audioKo = "여자: 내일 시험 잘 보세요.",
            options = listOf("감사합니다. 열심히 볼게요.", "미안해요. 시간이 없었어요.", "네, 잘 먹었습니다.", "안녕히 주무세요."),
            correctIdx = 0, weight = 3, tier = 1,
            explanationEn = "When someone wishes you good luck on an exam ('시험 잘 보세요'), responding '감사합니다. 열심히 볼게요' is natural and polite.",
            explanationId = "Saat diberi ucapan semoga sukses ujian ('시험 잘 보세요'), balasan yang tepat adalah '감사합니다. 열심히 볼게요' ('Terima kasih, saya akan mengerjakannya dengan sungguh-sungguh')."
        ),
        listening(
            id = "t1_l06", typeKo = "이어질 말 고르기",
            instructionKo = "다음을 듣고 <보기>와 같이 다음 말에 이어지는 것을 고르십시오.",
            stimulusKo = "이어질 말로 가장 알맞은 것을 고르십시오.",
            audioKo = "남자: 실례지만 여기 앉아도 될까요?",
            options = listOf("네, 물론이에요. 앉으세요.", "아니요, 어제 샀어요.", "네, 여기서 내리세요.", "고맙습니다. 또 오세요."),
            correctIdx = 0, weight = 4, tier = 1,
            explanationEn = "'여기 앉아도 될까요?' asks permission to sit — '네, 물론이에요. 앉으세요' ('Yes, of course. Please sit') is the natural reply.",
            explanationId = "'여기 앉아도 될까요?' meminta izin duduk — '네, 물론이에요. 앉으세요' ('Ya, tentu. Silakan duduk')."
        ),
        // [7~10] 장소 고르기
        listening(
            id = "t1_l07", typeKo = "장소 파악",
            instructionKo = "여기는 어디입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "대화가 이루어지는 장소는 어디입니까?",
            audioKo = "여자: 어서 오세요. 무엇을 드릴까요? 남자: 따뜻한 아메리카노 한 잔과 치즈케이크 하나 주세요.",
            options = listOf("병원", "카페", "우체국", "은행"),
            correctIdx = 1, weight = 3, tier = 1,
            explanationEn = "Ordering hot Americano and cheesecake takes place at a café (카페).",
            explanationId = "Memesan kopi Americano hangat dan cheesecake jelas terjadi di kafe (카페)."
        ),
        listening(
            id = "t1_l08", typeKo = "장소 파악",
            instructionKo = "여기는 어디입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "대화가 이루어지는 장소는 어디입니까?",
            audioKo = "남자: 이 소포를 인도네시아로 보내고 싶은데요. 여자: 저울 위에 올려놓으세요. 배편으로 보내실 건가요, 항공편으로 보내실 건가요?",
            options = listOf("우체국", "약국", "서점", "미용실"),
            correctIdx = 0, weight = 3, tier = 1,
            explanationEn = "Weighing a parcel (소포) and choosing sea or air mail (배편/항공편) happens at a post office (우체국).",
            explanationId = "Menimbang paket (소포) dan memilih pengiriman laut/udara (배편/항공편) terjadi di kantor pos (우체국)."
        ),
        listening(
            id = "t1_l09", typeKo = "장소 파악",
            instructionKo = "여기는 어디입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "대화가 이루어지는 장소는 어디입니까?",
            audioKo = "여자: 감기약을 사러 왔는데요. 목이 많이 아파요. 남자: 이 약을 식사 후에 하루 세 번 드세요.",
            options = listOf("식당", "영화관", "약국", "공항"),
            correctIdx = 2, weight = 3, tier = 1,
            explanationEn = "Buying cold medicine (감기약) and receiving dosage instructions ('식사 후에 하루 세 번 드세요') happens at a pharmacy (약국).",
            explanationId = "Membeli obat flu (감기약) dan mendapat aturan minum obat terjadi di apotek (약국)."
        ),
        listening(
            id = "t1_l10", typeKo = "장소 파악",
            instructionKo = "여기는 어디입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "대화가 이루어지는 장소는 어디입니까?",
            audioKo = "남자: 이 한국어 사전은 며칠 동안 빌릴 수 있어요? 여자: 이주일 동안 빌릴 수 있고요, 학생증을 보여 주세요.",
            options = listOf("백화점", "도서관", "세탁소", "지하철역"),
            correctIdx = 1, weight = 3, tier = 1,
            explanationEn = "Borrowing a dictionary for two weeks with a student ID (빌리다, 학생증) takes place at a library (도서관).",
            explanationId = "Meminjam kamus selama dua minggu dengan kartu pelajar (학생증) terjadi di perpustakaan (도서관)."
        ),
        // [11~14] 화제(무엇에 대한 이야기) 고르기
        listening(
            id = "t1_l11", typeKo = "화제 파악",
            instructionKo = "다음은 무엇에 대해 말하고 있습니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "두 사람은 무엇에 대해 이야기하고 있습니까?",
            audioKo = "여자: 오늘 바람이 많이 불고 좀 쌀쌀하네요. 남자: 네, 저녁에는 비도 온다고 하니까 우산을 챙기세요.",
            options = listOf("날씨", "취미", "직업", "가족"),
            correctIdx = 0, weight = 3, tier = 1,
            explanationEn = "Mentioning wind (바람), chilly air (쌀쌀하다), rain (비), and an umbrella (우산) is about the weather (날씨).",
            explanationId = "Membahas angin (바람), udara dingin (쌀쌀하다), hujan (비), dan payung (우산) adalah tentang cuaca (날씨)."
        ),
        listening(
            id = "t1_l12", typeKo = "화제 파악",
            instructionKo = "다음은 무엇에 대해 말하고 있습니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "두 사람은 무엇에 대해 이야기하고 있습니까?",
            audioKo = "남자: 여동생은 지금 대학교에 다녀요? 여자: 아니요, 작년에 졸업하고 지금은 회사에 다녀요.",
            options = listOf("음식", "가족", "교통", "여행"),
            correctIdx = 1, weight = 3, tier = 1,
            explanationEn = "Talking about a younger sister (여동생) graduating from university and working is about family (가족).",
            explanationId = "Membicarakan adik perempuan (여동생) yang lulus kuliah dan bekerja adalah topik keluarga (가족)."
        ),
        listening(
            id = "t1_l13", typeKo = "화제 파악",
            instructionKo = "다음은 무엇에 대해 말하고 있습니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "두 사람은 무엇에 대해 이야기하고 있습니까?",
            audioKo = "여자: 저는 주말마다 산에 올라가서 사진 찍는 것을 좋아해요. 남자: 와, 멋지네요. 저는 집에서 기타를 치며 쉬어요.",
            options = listOf("계절", "약속", "취미", "건강"),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "Hiking, taking photos, and playing guitar on weekends are hobbies (취미).",
            explanationId = "Mendaki gunung, memotret, dan bermain gitar di akhir pekan adalah topik hobi (취미)."
        ),
        listening(
            id = "t1_l14", typeKo = "화제 파악",
            instructionKo = "다음은 무엇에 대해 말하고 있습니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "두 사람은 무엇에 대해 이야기하고 있습니까?",
            audioKo = "남자: 이번 여름 방학에 제주도에 가려고 해요. 비행기 표도 벌써 예매했어요. 여자: 정말 부러워요. 맛있는 것도 많이 드시고 오세요.",
            options = listOf("여행 계획", "집 이사", "병원 예약", "학교 수업"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "Planning to visit Jeju Island during summer vacation and booking flight tickets is about travel plans (여행 계획).",
            explanationId = "Berencana pergi ke Jeju saat liburan musim panas dan sudah memesan tiket pesawat adalah tentang rencana wisata (여행 계획)."
        ),
        // [15~16] 행동 / 상황 파악
        listening(
            id = "t1_l15", typeKo = "이어질 행동 파악",
            instructionKo = "다음 대화를 잘 듣고 여자가 이어서 할 행동으로 알맞은 것을 고르십시오.",
            stimulusKo = "여자는 지금 무엇을 하려고 합니까?",
            audioKo = "여자: 밖에 비가 갑자기 많이 오네요. 우산이 없는데 어떡하죠? 남자: 건물 일층에 편의점이 있어요. 여자: 아, 그럼 거기 가서 우산을 하나 사 올게요.",
            options = listOf("집에서 쉴 것입니다.", "편의점에 가서 우산을 살 것입니다.", "남자에게 우산을 빌려줄 것입니다.", "택시를 타고 집에 갈 것입니다."),
            correctIdx = 1, weight = 4, tier = 2,
            explanationEn = "The woman says '거기 가서 우산을 하나 사 올게요' — she will go to the convenience store on the 1st floor to buy an umbrella.",
            explanationId = "Wanita berkata '거기 가서 우산을 하나 사 올게요' — ia akan pergi ke minimarket untuk membeli payung."
        ),
        listening(
            id = "t1_l16", typeKo = "이어질 행동 파악",
            instructionKo = "다음 대화를 잘 듣고 남자가 이어서 할 행동으로 알맞은 것을 고르십시오.",
            stimulusKo = "남자는 이 대화 후에 무엇을 할 것입니까?",
            audioKo = "남자: 어, 회의 자료를 사무실 책상에 두고 왔네요. 여자: 회의 시작까지 십 분밖에 안 남았는데 빨리 다녀오세요. 남자: 네, 지금 바로 올라가서 가져올게요.",
            options = listOf("사무실에 가서 자료를 가져올 것입니다.", "회의를 내일로 미룰 것입니다.", "책상을 새로 주문할 것입니다.", "여자와 함께 점심을 먹을 것입니다."),
            correctIdx = 0, weight = 4, tier = 2,
            explanationEn = "He left the meeting materials on his office desk and says '지금 바로 올라가서 가져올게요' (will go up immediately to fetch them).",
            explanationId = "Ia meninggalkan bahan rapat di meja kantor dan berkata '지금 바로 올라가서 가져올게요' (segera naik untuk mengambilnya)."
        ),
        // [17~21] 대화 내용과 같은 것 고르기
        listening(
            id = "t1_l17", typeKo = "대화 내용 일치",
            instructionKo = "다음을 듣고 <보기>와 같이 대화 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 수미 씨, 주말에 보통 뭐 해요? 여자: 저는 토요일마다 공원에서 자전거를 타요. 이번 토요일에도 동생과 같이 탈 거예요.",
            options = listOf("여자는 평일마다 자전거를 탑니다.", "여자는 혼자 자전거를 탈 것입니다.", "여자는 토요일마다 공원에서 자전거를 탑니다.", "여자는 자전거가 없습니다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "The woman explicitly states '토요일마다 공원에서 자전거를 타요' (rides a bicycle in the park every Saturday).",
            explanationId = "Wanita menyebutkan langsung '토요일마다 공원에서 자전거를 타요' (bersepeda di taman setiap Sabtu)."
        ),
        listening(
            id = "t1_l18", typeKo = "대화 내용 일치",
            instructionKo = "다음을 듣고 <보기>와 같이 대화 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "여자: 지훈 씨, 한국어 학원은 어때요? 남자: 선생님이 친절하셔서 재미있어요. 그런데 집에서 좀 멀어서 아침에 일찍 일어나야 해요.",
            options = listOf("남자는 학원 선생님을 좋아하지 않습니다.", "남자의 집은 학원에서 아주 가깝습니다.", "남자는 아침에 일찍 일어나야 합니다.", "남자는 저녁에만 학원에 갑니다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "Because the academy is far from home, he says '아침에 일찍 일어나야 해요' (must wake up early in the morning).",
            explanationId = "Karena kursusnya jauh dari rumah, pria berkata '아침에 일찍 일어나야 해요' (harus bangun pagi-pagi)."
        ),
        listening(
            id = "t1_l19", typeKo = "대화 내용 일치",
            instructionKo = "다음을 듣고 <보기>와 같이 대화 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 이 파란색 코트 정말 예쁘네요. 한번 입어 봐도 돼요? 여자: 네, 그런데 파란색은 큰 사이즈만 남았어요. 검은색은 모든 사이즈가 있습니다.",
            options = listOf("파란색 코트는 작은 사이즈가 있습니다.", "검은색 코트는 모든 사이즈가 있습니다.", "남자는 검은색 바지를 찾고 있습니다.", "여자는 코트를 입어 볼 수 없다고 합니다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The clerk says blue only has large left, while black has all sizes ('검은색은 모든 사이즈가 있습니다').",
            explanationId = "Pelayan mengatakan warna biru hanya tersisa ukuran besar, sedangkan hitam tersedia semua ukuran ('검은색은 모든 사이즈가 있습니다')."
        ),
        listening(
            id = "t1_l20", typeKo = "대화 내용 일치",
            instructionKo = "다음을 듣고 <보기>와 같이 대화 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "여자: 민호 씨, 내일 저녁 약속 시간을 여섯 시에서 일곱 시로 바꿀 수 있어요? 내일 일이 좀 늦게 끝날 것 같아요. 남자: 괜찮아요. 저도 일곱 시가 더 편해요.",
            options = listOf("두 사람은 오늘 저녁에 만납니다.", "여자는 내일 일이 일찍 끝납니다.", "남자는 일곱 시에 만나는 것이 괜찮습니다.", "두 사람은 약속을 취소했습니다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "The man agrees to move tomorrow's meeting to 7 PM and says '저도 일곱 시가 더 편해요'.",
            explanationId = "Pria menyetujui perubahan janji besok menjadi pukul 7 malam ('저도 일곱 시가 더 편해요')."
        ),
        listening(
            id = "t1_l21", typeKo = "대화 내용 일치",
            instructionKo = "다음을 듣고 <보기>와 같이 대화 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 안녕하십니까? 시청역 근처에 새로 문을 연 한식당을 소개합니다. 개업 기념으로 이번 주 금요일까지 모든 메뉴를 이십 퍼센트 할인해 드립니다.",
            options = listOf("이 식당은 일식당입니다.", "할인 행사는 이번 주 금요일까지입니다.", "시청역에서 아주 멀리 떨어져 있습니다.", "한 가지 메뉴만 할인합니다."),
            correctIdx = 1, weight = 4, tier = 2,
            explanationEn = "The announcement clearly states a 20% discount on all menus until this Friday ('이번 주 금요일까지').",
            explanationId = "Pengumuman menyatakan diskon 20% untuk semua menu berlaku sampai Jumat minggu ini ('이번 주 금요일까지')."
        ),
        // [22~24] 중심 생각 고르기
        listening(
            id = "t1_l22", typeKo = "중심 생각 파악",
            instructionKo = "다음을 듣고 여자의 중심 생각을 고르십시오.",
            stimulusKo = "여자의 중심 생각으로 알맞은 것을 고르십시오.",
            audioKo = "여자: 저는 물건을 사기 전에 항상 인터넷에서 가격과 후기를 비교해 봅니다. 시간이 조금 걸려도 그렇게 하면 후회하지 않고 좋은 물건을 살 수 있습니다.",
            options = listOf("물건은 무조건 빨리 사는 것이 좋다.", "인터넷 쇼핑은 위험해서 하면 안 된다.", "물건을 사기 전에 미리 비교해 보는 것이 좋다.", "비싼 물건이 항상 품질이 좋다."),
            correctIdx = 2, weight = 3, tier = 3,
            explanationEn = "She emphasizes comparing prices and reviews before purchasing so you don't regret it.",
            explanationId = "Wanita menekankan pentingnya membandingkan harga dan ulasan sebelum membeli barang agar tidak menyesal."
        ),
        listening(
            id = "t1_l23", typeKo = "중심 생각 파악",
            instructionKo = "다음을 듣고 남자의 중심 생각을 고르십시오.",
            stimulusKo = "남자의 중심 생각으로 알맞은 것을 고르십시오.",
            audioKo = "남자: 바쁘다고 아침 식사를 거르는 사람들이 많습니다. 하지만 아침을 조금이라도 먹으면 오전에 집중이 잘되고 건강에도 훨씬 좋습니다.",
            options = listOf("바쁠 때는 아침을 안 먹는 것이 편하다.", "아침 식사를 챙겨 먹는 것이 건강과 집중에 좋다.", "저녁을 많이 먹으면 건강에 좋다.", "아침에는 커피만 마시는 것이 좋다."),
            correctIdx = 1, weight = 4, tier = 3,
            explanationEn = "The man's main idea is that eating even a small breakfast improves morning focus and health.",
            explanationId = "Gagasan utama pria adalah sarapan meski sedikit tetap baik untuk fokus di pagi hari dan kesehatan."
        ),
        listening(
            id = "t1_l24", typeKo = "중심 생각 파악",
            instructionKo = "다음을 듣고 여자의 중심 생각을 고르십시오.",
            stimulusKo = "여자의 중심 생각으로 알맞은 것을 고르십시오.",
            audioKo = "여자: 외국어를 잘하려면 단어만 외우는 것보다 매일 짧게라도 직접 말해 보는 것이 중요합니다. 틀리는 것을 두려워하지 말고 자꾸 사용해 봐야 실력이 늡니다.",
            options = listOf("외국어는 단어장만 보면 충분하다.", "틀릴까 봐 말하지 않는 것이 안전하다.", "외국어를 늘리려면 매일 직접 말해 보는 것이 중요하다.", "외국어는 혼자 공부할 수 없다."),
            correctIdx = 2, weight = 4, tier = 3,
            explanationEn = "The woman argues that speaking daily without fear of mistakes is key to improving a foreign language.",
            explanationId = "Wanita menekankan bahwa berbicara langsung setiap hari tanpa takut salah sangat penting agar kemampuan bahasa asing meningkat."
        ),
        // [25~26] 묶음 듣기 세트 1
        listening(
            id = "t1_l25", typeKo = "묶음 듣기 · 이유 파악",
            instructionKo = "다음을 듣고 물음에 답하십시오.",
            stimulusKo = "남자는 왜 여자에게 전화를 걸었습니까?",
            audioKo = "남자: 여보세요, 지연 씨. 저 민수예요. 오늘 오후 세 시에 같이 박물관에 가기로 했잖아요. 여자: 네, 맞아요. 지금 준비하고 있었어요. 남자: 정말 미안한데 갑자기 회사에 급한 일이 생겨서 오늘 못 갈 것 같아요. 이번 일요일로 옮겨도 될까요? 여자: 괜찮아요, 일 바쁘시군요. 그럼 일요일 두 시에 박물관 앞에서 만나요.",
            options = listOf("박물관 위치를 물어보려고", "오늘 약속을 다른 날로 바꾸려고", "회사에 지각했다고 알리려고", "표 가격을 확인하려고"),
            correctIdx = 1, weight = 3, tier = 3,
            explanationEn = "Minsu calls because urgent work came up and asks to reschedule today's museum visit to Sunday ('이번 일요일로 옮겨도 될까요?').",
            explanationId = "Minsu menelepon karena ada urusan mendadak di kantor dan ingin memindahkan janji ke hari Minggu ('이번 일요일로 옮겨도 될까요?').",
            groupId = "t1_lg_1", groupLabelKo = "※ [묶음 듣기] 다음 대화를 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t1_l26", typeKo = "묶음 듣기 · 내용 일치",
            instructionKo = "다음을 듣고 물음에 답하십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 여보세요, 지연 씨. 저 민수예요. 오늘 오후 세 시에 같이 박물관에 가기로 했잖아요. 여자: 네, 맞아요. 지금 준비하고 있었어요. 남자: 정말 미안한데 갑자기 회사에 급한 일이 생겨서 오늘 못 갈 것 같아요. 이번 일요일로 옮겨도 될까요? 여자: 괜찮아요, 일 바쁘시군요. 그럼 일요일 두 시에 박물관 앞에서 만나요.",
            options = listOf("두 사람은 오늘 세 시에 박물관에 갑니다.", "여자는 일요일에 시간이 없습니다.", "두 사람은 일요일 두 시에 박물관 앞에서 만납니다.", "남자는 지금 박물관 앞에 도착했습니다."),
            correctIdx = 2, weight = 4, tier = 3,
            explanationEn = "They agreed to meet at 2 PM on Sunday in front of the museum ('일요일 두 시에 박물관 앞에서 만나요').",
            explanationId = "Mereka sepakat bertemu hari Minggu pukul 2 siang di depan museum ('일요일 두 시에 박물관 앞에서 만나요').",
            groupId = "t1_lg_1", groupLabelKo = "※ [묶음 듣기] 다음 대화를 잘 듣고 두 물음에 답하십시오."
        ),
        // [27~28] 묶음 듣기 세트 2
        listening(
            id = "t1_l27", typeKo = "묶음 듣기 · 화제 파악",
            instructionKo = "다음을 듣고 물음에 답하십시오.",
            stimulusKo = "안내 방송은 무엇에 대해 설명하고 있습니까?",
            audioKo = "여자: 승객 여러분, 잠시 안내 말씀 드리겠습니다. 우리 열차는 잠시 후 부산역에 도착합니다. 내리실 때는 선반 위에 두고 내리는 물건이 없는지 다시 한번 확인해 주시기 바랍니다. 또한 승강장과 열차 사이가 넓으니 발밑을 조심해서 내려 주십시오.",
            options = listOf("기차표 환불 방법", "열차 도착 및 하차 시 주의 사항", "부산 관광지 소개", "열차 내 식당 이용 시간"),
            correctIdx = 1, weight = 3, tier = 3,
            explanationEn = "The announcement notifies passengers of arrival at Busan Station and safety reminders when getting off.",
            explanationId = "Pengumuman memberi tahu kedatangan kereta di Stasiun Busan dan hal yang perlu diperhatikan saat turun.",
            groupId = "t1_lg_2", groupLabelKo = "※ [묶음 듣기] 다음 안내 방송을 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t1_l28", typeKo = "묶음 듣기 · 내용 일치",
            instructionKo = "다음을 듣고 물음에 답하십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "여자: 승객 여러분, 잠시 안내 말씀 드리겠습니다. 우리 열차는 잠시 후 부산역에 도착합니다. 내리실 때는 선반 위에 두고 내리는 물건이 없는지 다시 한번 확인해 주시기 바랍니다. 또한 승강장과 열차 사이가 넓으니 발밑을 조심해서 내려 주십시오.",
            options = listOf("열차는 곧 서울역에 도착합니다.", "선반 위 물건을 확인하고 내려야 합니다.", "승강장과 열차 사이가 아주 좁습니다.", "열차 출발이 삼십 분 늦어집니다."),
            correctIdx = 1, weight = 4, tier = 3,
            explanationEn = "Passengers are reminded to check the overhead rack for forgotten belongings ('선반 위에 두고 내리는 물건이 없는지 확인').",
            explanationId = "Penumpang diminta memeriksa rak atas agar tidak ada barang tertinggal ('선반 위에 두고 내리는 물건이 없는지 확인').",
            groupId = "t1_lg_2", groupLabelKo = "※ [묶음 듣기] 다음 안내 방송을 잘 듣고 두 물음에 답하십시오."
        ),
        // [29~30] 묶음 듣기 세트 3
        listening(
            id = "t1_l29", typeKo = "묶음 듣기 · 이유 파악",
            instructionKo = "다음을 듣고 물음에 답하십시오.",
            stimulusKo = "남자는 왜 요리 교실에 등록했습니까?",
            audioKo = "여자: 준호 씨, 요즘 퇴근 후에 요리 교실에 다닌다면서요? 남자: 네, 혼자 살다 보니까 맨날 배달 음식만 시켜 먹게 되더라고요. 건강에도 안 좋고 돈도 많이 들어서 직접 요리를 배워 보기로 했어요. 여자: 좋은 생각이네요. 저도 다음 달부터 같이 다녀 보고 싶어요.",
            options = listOf("요리사가 되려고", "건강을 챙기고 식비를 줄이려고", "여자의 부탁을 받아서", "식당을 새로 열려고"),
            correctIdx = 1, weight = 3, tier = 3,
            explanationEn = "He enrolled because ordering delivery daily was bad for his health and expensive ('건강에도 안 좋고 돈도 많이 들어서').",
            explanationId = "Ia mendaftar kursus memasak karena terus memesan makanan antar buruk bagi kesehatan dan mahal ('건강에도 안 좋고 돈도 많이 들어서').",
            groupId = "t1_lg_3", groupLabelKo = "※ [묶음 듣기] 다음 대화를 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t1_l30", typeKo = "묶음 듣기 · 내용 일치",
            instructionKo = "다음을 듣고 물음에 답하십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "여자: 준호 씨, 요즘 퇴근 후에 요리 교실에 다닌다면서요? 남자: 네, 혼자 살다 보니까 맨날 배달 음식만 시켜 먹게 되더라고요. 건강에도 안 좋고 돈도 많이 들어서 직접 요리를 배워 보기로 했어요. 여자: 좋은 생각이네요. 저도 다음 달부터 같이 다녀 보고 싶어요.",
            options = listOf("남자는 가족과 함께 살고 있습니다.", "남자는 출근하기 전에 요리를 배웁니다.", "여자는 다음 달에 요리 교실에 다니고 싶어 합니다.", "여자는 이미 요리 교실에 다니고 있습니다."),
            correctIdx = 2, weight = 4, tier = 3,
            explanationEn = "The woman says '저도 다음 달부터 같이 다녀 보고 싶어요' (wants to join starting next month).",
            explanationId = "Wanita berkata '저도 다음 달부터 같이 다녀 보고 싶어요' (ingin ikut mulai bulan depan).",
            groupId = "t1_lg_3", groupLabelKo = "※ [묶음 듣기] 다음 대화를 잘 듣고 두 물음에 답하십시오."
        )
    )

    val readingQuestions: List<ExamQuestion> = listOf(
        // [31~33] 무엇에 대한 이야기입니까?
        reading(
            id = "t1_r01", typeKo = "주제 파악",
            instructionKo = "무엇에 대한 이야기입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "이곳에서는 여러 나라의 책을 빌릴 수 있습니다. 조용히 공부하는 사람도 많습니다.",
            options = listOf("시장", "식당", "도서관", "공항"),
            correctIdx = 2, weight = 2, tier = 1,
            explanationEn = "A place where you can borrow books from many countries and study quietly is a library (도서관).",
            explanationId = "Tempat untuk meminjam buku dari berbagai negara dan belajar dengan tenang adalah perpustakaan (도서관)."
        ),
        reading(
            id = "t1_r02", typeKo = "주제 파악",
            instructionKo = "무엇에 대한 이야기입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "저는 매일 아침 일곱 시에 일어납니다. 그리고 세수를 하고 아침밥을 먹습니다.",
            options = listOf("날씨", "하루 일과", "주말 여행", "쇼핑"),
            correctIdx = 1, weight = 2, tier = 1,
            explanationEn = "Waking up at 7 AM, washing one's face, and eating breakfast describes a daily routine (하루 일과).",
            explanationId = "Bangun jam 7 pagi, mencuci muka, dan sarapan menjelaskan rutinitas harian (하루 일과)."
        ),
        reading(
            id = "t1_r03", typeKo = "주제 파악",
            instructionKo = "무엇에 대한 이야기입니까? <보기>와 같이 알맞은 것을 고르십시오.",
            stimulusKo = "한국의 봄은 따뜻하고 꽃이 많이 핍니다. 가을에는 하늘이 높고 단풍이 아름답습니다.",
            options = listOf("계절", "교통", "음식", "직업"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "Describing spring (봄) and autumn (가을) in Korea is about seasons (계절).",
            explanationId = "Menjelaskan musim semi (봄) dan musim gugur (가을) di Korea adalah tentang musim (계절)."
        ),
        // [34~39] 빈칸 추론 (어휘·문법 — 100% 한국어, 번역 힌트 없음)
        reading(
            id = "t1_r04", typeKo = "어휘 빈칸",
            instructionKo = "<보기>와 같이 ( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "저는 매일 아침 여덟 시에 학교에 ( ____ ).",
            options = listOf("잡니다", "갑니다", "먹습니다", "읽습니다"),
            correctIdx = 1, weight = 2, tier = 1,
            explanationEn = "Destination marker 학교에 pairs naturally with the movement verb 갑니다 ('go to school').",
            explanationId = "Partikel tujuan 학교에 berpasangan tepat dengan kata kerja gerak 갑니다 ('pergi ke sekolah')."
        ),
        reading(
            id = "t1_r05", typeKo = "어휘 빈칸",
            instructionKo = "<보기>와 같이 ( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "날씨가 너무 더워서 ( ____ ) 물을 마셨어요.",
            options = listOf("차가운", "무거운", "어려운", "가까운"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "Because the weather is very hot (더워서), drinking cold water (차가운 물) is the only logical choice.",
            explanationId = "Karena cuaca sangat panas (더워서), minum air dingin (차가운 물) adalah pilihan yang paling logis."
        ),
        reading(
            id = "t1_r06", typeKo = "문법 빈칸",
            instructionKo = "<보기>와 같이 ( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "감기에 걸려서 병원에 ( ____ ) 약을 받았습니다.",
            options = listOf("가고", "가지만", "가려고", "가거나"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "-고 connects sequential actions cleanly: went to the hospital and got medicine.",
            explanationId = "-고 menghubungkan dua tindakan berurutan: pergi ke rumah sakit lalu menerima obat."
        ),
        reading(
            id = "t1_r07", typeKo = "어휘 빈칸",
            instructionKo = "<보기>와 같이 ( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "방 안이 너무 어두워요. 불을 좀 ( ____ ) 주세요.",
            options = listOf("켜", "꺼", "닫아", "벗어"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "Since the room is too dark (어두워요), asking to turn on the light (불을 켜 주세요) fits accurately.",
            explanationId = "Karena ruangan terlalu gelap (어두워요), meminta menyalakan lampu (불을 켜 주세요) adalah jawaban tepat."
        ),
        reading(
            id = "t1_r08", typeKo = "문법 빈칸",
            instructionKo = "<보기>와 같이 ( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "도서관 안에서는 큰 소리로 ( ____ ). 조용히 해야 합니다.",
            options = listOf("말하면 안 됩니다", "말해도 됩니다", "말할 수 있습니다", "말하고 싶습니다"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "The second sentence ('조용히 해야 합니다' — must be quiet) requires prohibition -(으)면 안 됩니다.",
            explanationId = "Kalimat kedua ('조용히 해야 합니다' — harus tenang) menuntut bentuk larangan -(으)면 안 됩니다."
        ),
        reading(
            id = "t1_r09", typeKo = "어휘 빈칸",
            instructionKo = "<보기>와 같이 ( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "친구 생일이라서 백화점에서 예쁜 ( ____ )을 샀습니다.",
            options = listOf("선물", "질문", "약속", "계단"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "Buying a pretty gift (선물) at a department store for a friend's birthday matches the context and batchim particle -을.",
            explanationId = "Membeli hadiah (선물) cantik di mal untuk ulang tahun teman cocok dengan konteks dan partikel -을."
        ),
        // [40~42] 안내문 / 표지판 / 문자 내용 일치
        reading(
            id = "t1_r10", typeKo = "안내문 이해",
            instructionKo = "다음 안내문을 읽고 맞지 않는 것을 고르십시오.",
            passageBoxKo = "[ 서울 시립 박물관 이용 안내 ]\n• 관람 시간: 09:00 ~ 18:00\n• 휴관일: 매주 월요일\n• 입장료: 무료\n• 주의: 전시실 안에서는 사진 촬영 금지",
            stimulusKo = "안내문의 내용과 다른 것을 고르십시오.",
            options = listOf("입장할 때 돈을 내지 않아도 됩니다.", "매주 월요일에는 문을 열지 않습니다.", "오후 다섯 시에도 관람할 수 있습니다.", "전시실 안에서 사진을 찍을 수 있습니다."),
            correctIdx = 3, weight = 3, tier = 1,
            explanationEn = "The notice states '사진 촬영 금지' (photography prohibited), so option 4 ('can take photos') contradicts the notice.",
            explanationId = "Pengumuman menyatakan '사진 촬영 금지' (dilarang memotret), sehingga pilihan 4 bertentangan dengan isi pengumuman."
        ),
        reading(
            id = "t1_r11", typeKo = "문자 메시지 이해",
            instructionKo = "다음 문자 메시지를 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[문자 메시지 — 유진]\n민재 씨, 저 지금 지하철 2호선 탔어요.\n생각보다 차가 아니라 사람이 많네요.\n강남역 출구 3번 앞에서 10분 후에 만나요!",
            stimulusKo = "문자 메시지의 내용과 같은 것을 고르십시오.",
            options = listOf("유진 씨는 지금 버스를 타고 있습니다.", "두 사람은 강남역 3번 출구 앞에서 만납니다.", "유진 씨는 이미 강남역에 도착했습니다.", "유진 씨는 약속을 한 시간 미뤘습니다."),
            correctIdx = 1, weight = 2, tier = 1,
            explanationEn = "Yujin asks to meet in 10 minutes in front of Gangnam Station Exit 3 ('강남역 출구 3번 앞에서').",
            explanationId = "Yujin mengajak bertemu 10 menit lagi di depan Pintu Keluar 3 Stasiun Gangnam."
        ),
        reading(
            id = "t1_r12", typeKo = "안내문 이해",
            instructionKo = "다음 할인 쿠폰을 보고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[ 한빛 서점 봄맞이 할인 쿠폰 ]\n• 할인 금액: 3,000원 할인 (20,000원 이상 구매 시)\n• 사용 기간: 4월 1일 ~ 4월 30일\n• 사용 장소: 강남점, 홍대점 (온라인 사용 불가)",
            stimulusKo = "쿠폰 내용과 같은 것을 고르십시오.",
            options = listOf("인터넷 서점에서도 사용할 수 있습니다.", "만 원을 사면 삼천 원을 할인받습니다.", "사월 한 달 동안 사용할 수 있습니다.", "모든 지점에서 사용할 수 있습니다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "The validity period is April 1 to April 30 ('사월 한 달 동안'). Minimum spend is 20,000 won and online is excluded.",
            explanationId = "Masa berlaku kupon adalah 1–30 April ('사월 한 달 동안'). Minimal belanja 20.000 won dan tidak berlaku online."
        ),
        // [43~45] 밑줄 친 부분 유의어 & 짧은 글 내용 일치
        reading(
            id = "t1_r13", typeKo = "밑줄 친 표현 유의어",
            instructionKo = "밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
            stimulusKo = "한국어 시험에 합격해서 기분이 정말 좋습니다.",
            underlinedText = "기분이 정말 좋습니다",
            options = listOf("행복합니다", "피곤합니다", "바쁩니다", "심심합니다"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "'기분이 정말 좋습니다' ('feel really great') is closest in meaning to '행복합니다' ('am happy').",
            explanationId = "'기분이 정말 좋습니다' ('perasaan sangat senang') paling dekat maknanya dengan '행복합니다' ('bahagia')."
        ),
        reading(
            id = "t1_r14", typeKo = "짧은 글 내용 일치",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "수진 씨는 요즘 한국 요리를 배웁니다. 일주일에 두 번 요리 학원에 갑니다. 지난주에는 불고기를 만들었는데 가족들이 모두 맛있게 먹었습니다.",
            stimulusKo = "글의 내용과 같은 것을 고르십시오.",
            options = listOf("수진 씨는 요리 선생님입니다.", "수진 씨는 매일 요리 학원에 갑니다.", "수진 씨는 지난주에 불고기를 만들었습니다.", "수진 씨의 가족은 불고기를 싫어합니다."),
            correctIdx = 2, weight = 3, tier = 1,
            explanationEn = "The passage explicitly states '지난주에는 불고기를 만들었는데' (made bulgogi last week).",
            explanationId = "Bacaan menyatakan secara eksplisit '지난주에는 불고기를 만들었는데' (membuat bulgogi minggu lalu)."
        ),
        reading(
            id = "t1_r15", typeKo = "짧은 글 내용 일치",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "저는 어릴 때부터 강아지를 키우고 싶었습니다. 하지만 아파트에 살아서 키우지 못했습니다. 지난달에 마당이 있는 집으로 이사해서 드디어 작은 강아지를 키우기 시작했습니다.",
            stimulusKo = "글의 내용과 같은 것을 고르십시오.",
            options = listOf("저는 지금도 아파트에 살고 있습니다.", "저는 지난달에 이사했습니다.", "저는 어릴 때 강아지를 키웠습니다.", "새 집에는 마당이 없습니다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The writer moved last month ('지난달에 마당이 있는 집으로 이사해서') to a house with a yard.",
            explanationId = "Penulis pindah bulan lalu ('지난달에 ... 이사해서') ke rumah yang memiliki halaman."
        ),
        // [46~48] 중심 생각 고르기
        reading(
            id = "t1_r16", typeKo = "중심 생각 파악",
            instructionKo = "다음 글의 중심 생각을 고르십시오.",
            passageBoxKo = "저는 매일 밤 잠자리에 들기 전에 일기를 씁니다. 하루 동안 있었던 일을 적으면 마음이 편안해지고, 나중에 다시 읽을 때 좋은 추억이 됩니다.",
            stimulusKo = "이 글의 중심 생각으로 알맞은 것을 고르십시오.",
            options = listOf("일기를 쓰면 여러 가지 좋은 점이 있다.", "밤에는 일찍 자는 것이 가장 중요하다.", "다른 사람의 일기를 읽으면 재미있다.", "일기는 아침에 쓰는 것이 더 편하다."),
            correctIdx = 0, weight = 2, tier = 2,
            explanationEn = "The writer explains two benefits of keeping a diary: peace of mind and lasting good memories.",
            explanationId = "Penulis menjelaskan manfaat menulis buku harian: hati menjadi tenang dan menjadi kenangan indah."
        ),
        reading(
            id = "t1_r17", typeKo = "중심 생각 파악",
            instructionKo = "다음 글의 중심 생각을 고르십시오.",
            passageBoxKo = "요즘 스마트폰을 오래 보는 사람들이 많습니다. 화면을 너무 오래 보면 눈이 쉽게 피곤해집니다. 그래서 한 시간에 한 번씩 먼 곳을 보며 눈을 쉬게 해 주어야 합니다.",
            stimulusKo = "이 글의 중심 생각으로 알맞은 것을 고르십시오.",
            options = listOf("스마트폰 화면은 클수록 좋다.", "눈 건강을 위해 스마트폰을 보다가 자주 쉬어야 한다.", "스마트폰을 아예 사지 말아야 한다.", "어두운 곳에서 스마트폰을 보면 잘 보인다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The concluding advice ('그래서 ... 눈을 쉬게 해 주어야 합니다') highlights resting one's eyes periodically.",
            explanationId = "Kalimat penutup ('그래서 ... 눈을 쉬게 해 주어야 합니다') menekankan pentingnya mengistirahatkan mata secara berkala."
        ),
        reading(
            id = "t1_r18", typeKo = "중심 생각 파악",
            instructionKo = "다음 글의 중심 생각을 고르십시오.",
            passageBoxKo = "가까운 거리는 자동차를 타지 않고 걸어가는 것이 좋습니다. 걸으면 운동도 되고 공기 오염도 줄일 수 있기 때문입니다.",
            stimulusKo = "이 글의 중심 생각으로 알맞은 것을 고르십시오.",
            options = listOf("자동차는 항상 빠르고 편리하다.", "가까운 거리는 걸어 다니는 것이 좋다.", "공기 오염은 해결하기 어렵다.", "먼 거리도 걸어서 가야 한다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The opening thesis ('가까운 거리는 ... 걸어가는 것이 좋습니다') is supported by health and environmental reasons.",
            explanationId = "Kalimat utama ('가까운 거리는 ... 걸어가는 것이 좋습니다') didukung alasan olahraga dan mengurangi polusi udara."
        ),
        // [49~56] 묶음 문항 (Shared Reading Passages — 4 sets × 2 questions)
        reading(
            id = "t1_r19", typeKo = "묶음 독해 · 빈칸 추론",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "한국에는 추석이라는 큰 명절이 있습니다. 추석에는 멀리 떨어져 살던 가족들이 모두 고향에 모여 송편을 만들고 보름달을 봅니다. ( ㄱ ) 길에 차가 아주 많아서 고향에 가는 데 평소보다 시간이 오래 걸립니다.",
            stimulusKo = "( ㄱ )에 들어갈 알맞은 말을 고르십시오.",
            options = listOf("그래서", "하지만", "왜냐하면", "그리고"),
            correctIdx = 1, weight = 2, tier = 2,
            explanationEn = "The contrast between the joyful reunion and heavy traffic taking a long time requires '하지만' ('However').",
            explanationId = "Kontras antara berkumpulnya keluarga dengan macet parah di jalan membutuhkan konjungsi '하지만' ('Tetapi').",
            groupId = "t1_rg_1", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r20", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "한국에는 추석이라는 큰 명절이 있습니다. 추석에는 멀리 떨어져 살던 가족들이 모두 고향에 모여 송편을 만들고 보름달을 봅니다. ( ㄱ ) 길에 차가 아주 많아서 고향에 가는 데 평소보다 시간이 오래 걸립니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("추석에는 길에 차가 거의 없습니다.", "추석에는 가족들이 모여 송편을 만듭니다.", "추석에는 고향에 가지 않습니다.", "추석에는 평소보다 빨리 도착합니다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The text states families gather in their hometowns and make songpyeon ('송편을 만들고 보름달을 봅니다').",
            explanationId = "Teks menyebutkan keluarga berkumpul di kampung halaman dan membuat songpyeon.",
            groupId = "t1_rg_1", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r21", typeKo = "묶음 독해 · 빈칸 추론",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "우리 동네에는 매주 토요일마다 중고 장터가 열립니다. 주민들은 집에서 안 쓰는 옷이나 책, 장난감을 가지고 나와서 싸게 팝니다. 저도 지난주에 작아진 코트를 팔고 필요한 요리책을 ( ㄱ ).",
            stimulusKo = "( ㄱ )에 들어갈 알맞은 말을 고르십시오.",
            options = listOf("샀습니다", "버렸습니다", "잃어버렸습니다", "보냈습니다"),
            correctIdx = 0, weight = 2, tier = 2,
            explanationEn = "At a secondhand market (중고 장터), the writer sold an outgrown coat and bought ('샀습니다') a needed cookbook.",
            explanationId = "Di pasar barang bekas (중고 장터), penulis menjual mantel kekecilan dan membeli ('샀습니다') buku resep.",
            groupId = "t1_rg_2", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r22", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "우리 동네에는 매주 토요일마다 중고 장터가 열립니다. 주민들은 집에서 안 쓰는 옷이나 책, 장난감을 가지고 나와서 싸게 팝니다. 저도 지난주에 작아진 코트를 팔고 필요한 요리책을 ( ㄱ ).",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("중고 장터는 한 달에 한 번 열립니다.", "장터에서는 새 물건만 비싸게 팝니다.", "글쓴이는 지난주에 코트를 팔았습니다.", "글쓴이는 장난감을 많이 샀습니다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "The writer mentions selling a coat that became too small last week ('지난주에 작아진 코트를 팔고').",
            explanationId = "Penulis menyebutkan menjual mantel yang sudah kekecilan minggu lalu.",
            groupId = "t1_rg_2", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r23", typeKo = "묶음 독해 · 빈칸 추론",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "서울의 공공 자전거 '따릉이'는 시민들에게 아주 인기가 많습니다. 대여소가 지하철역과 공원 근처에 많아서 누구나 쉽게 ( ㄱ ). 이용 요금도 저렴해서 출퇴근할 때 타는 사람이 점점 늘고 있습니다.",
            stimulusKo = "( ㄱ )에 들어갈 알맞은 말을 고르십시오.",
            options = listOf("빌릴 수 있습니다", "수리해야 합니다", "만들어야 합니다", "팔 수 있습니다"),
            correctIdx = 0, weight = 2, tier = 2,
            explanationEn = "With many rental stations (대여소), anyone can easily rent ('빌릴 수 있습니다') a public bike.",
            explanationId = "Karena banyak stasiun penyewaan (대여소), siapa saja bisa menyewanya dengan mudah ('빌릴 수 있습니다').",
            groupId = "t1_rg_3", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r24", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "서울의 공공 자전거 '따릉이'는 시민들에게 아주 인기가 많습니다. 대여소가 지하철역과 공원 근처에 많아서 누구나 쉽게 ( ㄱ ). 이용 요금도 저렴해서 출퇴근할 때 타는 사람이 점점 늘고 있습니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("따릉이는 이용 요금이 매우 비쌉니다.", "출퇴근할 때 따릉이를 타는 사람이 늘고 있습니다.", "따릉이 대여소는 공원 안에만 있습니다.", "따릉이는 외국인만 이용할 수 있습니다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The last sentence states commuters riding Ttareungi are steadily increasing ('출퇴근할 때 타는 사람이 점점 늘고 있습니다').",
            explanationId = "Kalimat terakhir menyatakan pengguna Ttareungi untuk berangkat/pulang kerja semakin bertambah.",
            groupId = "t1_rg_3", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r25", typeKo = "묶음 독해 · 빈칸 추론",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "한국 식당에서는 보통 물과 반찬을 무료로 줍니다. 요즘은 손님이 직접 정수기에서 물을 가져다 마시는 '셀프 서비스' 식당도 많습니다. 그래서 식당에 들어가면 먼저 물이 셀프인지 ( ㄱ ).",
            stimulusKo = "( ㄱ )에 들어갈 알맞은 말을 고르십시오.",
            options = listOf("확인하는 것이 좋습니다", "주문하지 말아야 합니다", "계산해야 합니다", "요리하는 것이 좋습니다"),
            correctIdx = 0, weight = 2, tier = 2,
            explanationEn = "Because many restaurants use self-service water, it is good to check first ('확인하는 것이 좋습니다').",
            explanationId = "Karena banyak restoran memakai sistem ambil air sendiri, sebaiknya mengecek lebih dulu ('확인하는 것이 좋습니다').",
            groupId = "t1_rg_4", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r26", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "한국 식당에서는 보통 물과 반찬을 무료로 줍니다. 요즘은 손님이 직접 정수기에서 물을 가져다 마시는 '셀프 서비스' 식당도 많습니다. 그래서 식당에 들어가면 먼저 물이 셀프인지 ( ㄱ ).",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("한국 식당은 모든 반찬이 유료입니다.", "손님이 직접 물을 가져다 마시는 식당도 많습니다.", "정수기 물은 따로 돈을 내야 합니다.", "모든 한국 식당에는 정수기가 없습니다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The text notes there are many self-service restaurants where guests get water themselves.",
            explanationId = "Teks menjelaskan banyak restoran swalayan di mana pelanggan mengambil air sendiri dari dispenser.",
            groupId = "t1_rg_4", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        // [57~58] 문장 순서 배열하기 (가)(나)(다)(라)
        reading(
            id = "t1_r27", typeKo = "문장 순서 배열",
            instructionKo = "다음을 순서대로 맞게 나열한 것을 고르십시오.",
            passageBoxKo = "(가) 그래서 우산을 쓰지 않고 버스 정류장까지 뛰어갔습니다.\n(나) 오늘 아침에 집을 나올 때는 하늘이 맑았습니다.\n(다) 결국 옷이 다 젖어서 감기에 걸렸습니다.\n(라) 그런데 오후에 퇴근할 때 갑자기 소나기가 내렸습니다.",
            stimulusKo = "문장을 자연스러운 순서로 배열한 것을 고르십시오.",
            options = listOf("(나)-(라)-(가)-(다)", "(나)-(가)-(라)-(다)", "(라)-(나)-(가)-(다)", "(가)-(나)-(라)-(다)"),
            correctIdx = 0, weight = 3, tier = 3,
            explanationEn = "Chronological flow: clear sky in the morning (나) → sudden shower in the afternoon (라) → ran without an umbrella (가) → got soaked and caught a cold (다).",
            explanationId = "Urutan kronologis: pagi cerah (나) → sore tiba-tiba hujan deras (라) → berlari tanpa payung (가) → akhirnya basah kuyup dan masuk angin (다)."
        ),
        reading(
            id = "t1_r28", typeKo = "문장 순서 배열",
            instructionKo = "다음을 순서대로 맞게 나열한 것을 고르십시오.",
            passageBoxKo = "(가) 그곳에서 한국 친구들과 함께 김치를 직접 담가 보았습니다.\n(나) 지난 주말에 김치 체험 박물관에 다녀왔습니다.\n(다) 집에 돌아와서 저녁에 가족들과 맛있게 먹었습니다.\n(라) 처음이라 조금 서툴렀지만 정말 재미있었습니다.",
            stimulusKo = "문장을 자연스러운 순서로 배열한 것을 고르십시오.",
            options = listOf("(나)-(가)-(라)-(다)", "(나)-(라)-(가)-(다)", "(가)-(나)-(다)-(라)", "(다)-(나)-(가)-(라)"),
            correctIdx = 0, weight = 3, tier = 3,
            explanationEn = "Visited kimchi museum (나) → made kimchi there (그곳에서, 가) → clumsy at first but fun (라) → returned home and ate with family (다).",
            explanationId = "Pergi ke museum kimchi (나) → membuat kimchi di sana (그곳에서, 가) → masih canggung tapi seru (라) → pulang dan makan bersama keluarga (다)."
        ),
        // [59~62] 문장 삽입 (ㄱ)(ㄴ)(ㄷ)(ㄹ) & 문맥 어휘
        reading(
            id = "t1_r29", typeKo = "문장 삽입",
            instructionKo = "다음 글을 읽고 <보기>의 문장이 들어갈 곳으로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "<보기> 하지만 요즘은 일회용 컵 대신 개인 텀블러를 쓰는 사람이 늘고 있습니다.\n\n( ㄱ ) 카페에서 커피를 마실 때 일회용 컵을 사용하면 아주 편리합니다. ( ㄴ ) 그래서 많은 사람들이 습관처럼 일회용 컵을 사용해 왔습니다. ( ㄷ ) 환경 보호에 대한 관심이 높아졌기 때문입니다. ( ㄹ ) 일부 카페에서는 텀블러를 가져오면 음료값을 할인해 주기도 합니다.",
            stimulusKo = "<보기>의 문장이 들어갈 위치로 가장 알맞은 곳은 어디입니까?",
            options = listOf("( ㄱ )", "( ㄴ )", "( ㄷ )", "( ㄹ )"),
            correctIdx = 2, weight = 2, tier = 3,
            explanationEn = "<보기> starts with '하지만' (contrast against using disposable cups in ㄴ) and is immediately explained by '환경 보호에 대한 관심이 높아졌기 때문입니다' (because of environmental awareness) right after (ㄷ).",
            explanationId = "<보기> dimulai dengan '하지만' (kontras dengan kebiasaan gelas sekali pakai) dan langsung dijelaskan oleh alasan kesadaran lingkungan tepat sesudah (ㄷ)."
        ),
        reading(
            id = "t1_r30", typeKo = "문장 삽입",
            instructionKo = "다음 글을 읽고 <보기>의 문장이 들어갈 곳으로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "<보기> 그런데 어제는 너무 피곤해서 알람 소리를 듣지 못했습니다.\n\n( ㄱ ) 저는 매일 아침 여섯 시에 알람을 맞춰 놓습니다. ( ㄴ ) 평소에는 알람이 울리면 바로 일어나서 출근 준비를 합니다. ( ㄷ ) 눈을 떠 보니 벌써 여덟 시가 넘어 있었습니다. ( ㄹ ) 다행히 택시를 타고 가서 지각은 면했습니다.",
            stimulusKo = "<보기>의 문장이 들어갈 위치로 가장 알맞은 곳은 어디입니까?",
            options = listOf("( ㄱ )", "( ㄴ )", "( ㄷ )", "( ㄹ )"),
            correctIdx = 2, weight = 3, tier = 3,
            explanationEn = "After stating the usual habit (평소에는, ㄴ), <보기> introduces yesterday's exception ('그런데 어제는...') at (ㄷ), leading directly into waking up past 8 AM.",
            explanationId = "Sesudah kebiasaan normal (평소에는), <보기> memperkenalkan pengecualian kemarin ('그런데 어제는...') di (ㄷ), lalu tersambung dengan bangun lewat jam 8."
        ),
        // [61~62] 추가 어휘·문법·밑줄
        reading(
            id = "t1_r31", typeKo = "밑줄 친 표현 유의어",
            instructionKo = "밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
            stimulusKo = "이 가방은 디자인이 예쁘고 가격도 싸서 인기가 많습니다.",
            underlinedText = "가격도 싸서",
            options = listOf("저렴해서", "무거워서", "복잡해서", "좁아서"),
            correctIdx = 0, weight = 2, tier = 2,
            explanationEn = "'싸다' ('cheap / inexpensive') is synonymous with '저렴하다' ('affordable / inexpensive').",
            explanationId = "'싸다' ('murah') bersinonim dengan '저렴하다' ('terjangkau / murah')."
        ),
        reading(
            id = "t1_r32", typeKo = "문법 빈칸",
            instructionKo = "( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "한국 친구와 자주 이야기하다 보니 한국어 실력이 많이 ( ____ ).",
            options = listOf("늘었습니다", "줄었습니다", "잃었습니다", "막혔습니다"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "Speaking frequently with a Korean friend causes one's Korean skills to improve/grow ('실력이 늘었습니다').",
            explanationId = "Sering berbicara dengan teman Korea membuat kemampuan bahasa Korea meningkat ('실력이 늘었습니다')."
        ),
        // [63~70] 긴 글 독해 (4 sets × 2 questions)
        reading(
            id = "t1_r33", typeKo = "긴 글 독해 · 목적 파악",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "안녕하세요, 한강 문화센터입니다. 이번 주 토요일 오후 두 시에 예정되어 있던 '한국 전통차 만들기' 수업이 강사님의 건강 사정으로 다음 주 토요일로 연기되었습니다. 이미 신청하신 분들은 별도로 다시 신청하지 않으셔도 됩니다. 일정 변경으로 불편을 드려 죄송합니다.",
            stimulusKo = "이 글을 쓴 목적은 무엇입니까?",
            options = listOf("새 강사를 모집하려고", "수업 날짜가 바뀐 것을 알리려고", "전통차 가격을 안내하려고", "수강 신청을 마감하려고"),
            correctIdx = 1, weight = 2, tier = 2,
            explanationEn = "The cultural center writes to notify students that Saturday's class has been postponed to next Saturday ('다음 주 토요일로 연기되었습니다').",
            explanationId = "Pusat budaya menulis pengumuman untuk memberi tahu bahwa jadwal kelas diundur ke Sabtu depan.",
            groupId = "t1_rg_5", groupLabelKo = "※ [긴 글 독해] 다음 안내문을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r34", typeKo = "긴 글 독해 · 내용 일치",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "안녕하세요, 한강 문화센터입니다. 이번 주 토요일 오후 두 시에 예정되어 있던 '한국 전통차 만들기' 수업이 강사님의 건강 사정으로 다음 주 토요일로 연기되었습니다. 이미 신청하신 분들은 별도로 다시 신청하지 않으셔도 됩니다. 일정 변경으로 불편을 드려 죄송합니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("수업은 이번 주 일요일에 열립니다.", "이미 신청한 사람은 다시 신청해야 합니다.", "강사님의 건강 문제로 수업이 연기되었습니다.", "전통차 수업이 완전히 취소되었습니다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "The class was postponed due to the instructor's health ('강사님의 건강 사정으로'). Existing registrants do NOT need to re-apply.",
            explanationId = "Kelas diundur karena kondisi kesehatan pengajar ('강사님의 건강 사정으로'), dan peserta lama tidak perlu mendaftar ulang.",
            groupId = "t1_rg_5", groupLabelKo = "※ [긴 글 독해] 다음 안내문을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r35", typeKo = "긴 글 독해 · 빈칸 추론",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "식물을 키우면 집 안의 공기가 깨끗해질 뿐만 아니라 마음도 안정됩니다. 하지만 처음 키우는 사람은 물을 너무 자주 주어서 식물을 죽이는 경우가 많습니다. 흙을 손으로 만져 보고 ( ㄱ ) 물을 주는 것이 올바른 방법입니다.",
            stimulusKo = "( ㄱ )에 들어갈 가장 알맞은 것을 고르십시오.",
            options = listOf("말랐을 때", "젖었을 때", "차가울 때", "어두울 때"),
            correctIdx = 0, weight = 3, tier = 3,
            explanationEn = "To avoid overwatering, touch the soil and water only when it is dry ('말랐을 때').",
            explanationId = "Agar tidak terlalu sering menyiram, sentuh tanah dan siram saat tanah sudah kering ('말랐을 때').",
            groupId = "t1_rg_6", groupLabelKo = "※ [긴 글 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r36", typeKo = "긴 글 독해 · 내용 일치",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "식물을 키우면 집 안의 공기가 깨끗해질 뿐만 아니라 마음도 안정됩니다. 하지만 처음 키우는 사람은 물을 너무 자주 주어서 식물을 죽이는 경우가 많습니다. 흙을 손으로 만져 보고 ( ㄱ ) 물을 주는 것이 올바른 방법입니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("식물은 매일 물을 많이 주어야 잘 자랍니다.", "식물을 키우면 집 안 공기가 나빠집니다.", "초보자는 물을 너무 자주 주어 실패하는 경우가 많습니다.", "흙은 손으로 만지면 안 됩니다."),
            correctIdx = 2, weight = 3, tier = 3,
            explanationEn = "The text warns beginners often kill plants by watering too frequently ('물을 너무 자주 주어서').",
            explanationId = "Teks menjelaskan pemula sering membuat tanaman mati karena terlalu sering menyiram air.",
            groupId = "t1_rg_6", groupLabelKo = "※ [긴 글 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r37", typeKo = "긴 글 독해 · 이유 파악",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "한국의 전통 집인 '한옥'은 나무와 흙, 돌 같은 자연 재료로 짓습니다. 한옥에는 여름에 시원하게 쉴 수 있는 '마루'와 겨울에 바닥을 따뜻하게 데워 주는 '온돌'이 함께 있습니다. 그래서 한옥은 사계절이 뚜렷한 한국의 기후에 아주 잘 어울리는 집입니다.",
            stimulusKo = "한옥이 한국의 기후에 잘 어울리는 이유는 무엇입니까?",
            options = listOf("높은 건물로 지을 수 있기 때문에", "여름용 마루와 겨울용 온돌이 모두 있기 때문에", "유리와 철로만 만들었기 때문에", "짓는 시간이 하루밖에 안 걸리기 때문에"),
            correctIdx = 1, weight = 2, tier = 3,
            explanationEn = "Hanok combines cool wooden floors (마루) for summer and heated floors (온돌) for winter, fitting Korea's four seasons.",
            explanationId = "Hanok memadukan lantai kayu sejuk (마루) untuk musim panas dan pemanas lantai (온돌) untuk musim dingin.",
            groupId = "t1_rg_7", groupLabelKo = "※ [긴 글 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r38", typeKo = "긴 글 독해 · 내용 일치",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "한국의 전통 집인 '한옥'은 나무와 흙, 돌 같은 자연 재료로 짓습니다. 한옥에는 여름에 시원하게 쉴 수 있는 '마루'와 겨울에 바닥을 따뜻하게 데워 주는 '온돌'이 함께 있습니다. 그래서 한옥은 사계절이 뚜렷한 한국의 기후에 아주 잘 어울리는 집입니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("한옥은 나무, 흙, 돌 등 자연 재료로 짓습니다.", "온돌은 여름에 집을 시원하게 해 줍니다.", "마루는 바닥을 따뜻하게 데우는 시설입니다.", "한국은 일 년 내내 날씨가 똑같습니다."),
            correctIdx = 0, weight = 3, tier = 3,
            explanationEn = "The first sentence directly states Hanok is built with natural materials like wood, soil, and stone.",
            explanationId = "Kalimat pertama menyebutkan secara langsung bahwa Hanok dibangun dari bahan alami seperti kayu, tanah, dan batu.",
            groupId = "t1_rg_7", groupLabelKo = "※ [긴 글 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r39", typeKo = "긴 글 독해 · 빈칸 추론",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "지하철이나 버스에서 물건을 잃어버렸을 때는 당황하지 말고 유실물 센터에 문의하면 됩니다. 먼저 자신이 탔던 노선과 시간, 내린 역을 정확히 기억해 두어야 합니다. 그러면 직원이 ( ㄱ ) 물건을 훨씬 빨리 찾을 수 있습니다.",
            stimulusKo = "( ㄱ )에 들어갈 가장 알맞은 말을 고르십시오.",
            options = listOf("기록을 확인해서", "지하철을 멈춰서", "새 물건을 사서", "요금을 돌려줘서"),
            correctIdx = 0, weight = 3, tier = 3,
            explanationEn = "Providing the line, time, and station lets staff check records ('기록을 확인해서') to locate lost items faster.",
            explanationId = "Memberi tahu jalur, waktu, dan stasiun membantu petugas mengecek catatan ('기록을 확인해서') agar barang cepat ditemukan.",
            groupId = "t1_rg_8", groupLabelKo = "※ [긴 글 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t1_r40", typeKo = "긴 글 독해 · 내용 일치",
            instructionKo = "다음 글을 읽고 물음에 답하십시오.",
            passageBoxKo = "지하철이나 버스에서 물건을 잃어버렸을 때는 당황하지 말고 유실물 센터에 문의하면 됩니다. 먼저 자신이 탔던 노선과 시간, 내린 역을 정확히 기억해 두어야 합니다. 그러면 직원이 ( ㄱ ) 물건을 훨씬 빨리 찾을 수 있습니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("지하철에서 잃어버린 물건은 찾을 수 없습니다.", "물건을 잃어버리면 유실물 센터에 문의할 수 있습니다.", "내린 역은 기억하지 않아도 됩니다.", "버스에서는 유실물 센터를 이용할 수 없습니다."),
            correctIdx = 1, weight = 3, tier = 3,
            explanationEn = "The passage advises contacting the Lost & Found Center ('유실물 센터에 문의하면 됩니다').",
            explanationId = "Teks menyarankan untuk menghubungi pusat barang hilang ('유실물 센터에 문의하면 됩니다').",
            groupId = "t1_rg_8", groupLabelKo = "※ [긴 글 독해] 다음 글을 읽고 두 물음에 답하십시오."
        )
    )

    val allQuestions: List<ExamQuestion> = listeningQuestions + readingQuestions
}
