package com.example.data.seed

/**
 * Authentic NIIED-modeled TOPIK II (3–6급) 2-Period Mock Exam Bank.
 * - Period 1 (1교시):
 *   - Listening (30 items): first 12 items = 1× playback (1회), remaining 18 items = 2× playback (2회).
 *   - Writing (4 official tasks: 51번 10점 + 52번 10점 + 53번 30점 + 54번 50점 = 100점), with 3 variants per task.
 * - Period 2 (2교시):
 *   - Reading (30 items): grammar, underlined synonyms, ad slogans, notices, sentence ordering,
 *     newspaper headlines, sentence insertion, and advanced shared-passage essays.
 */
object Topik2MockBank {

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
        maxPlays: Int,
        explanationEn: String,
        explanationId: String,
        groupId: String? = null,
        groupLabelKo: String? = null
    ) = ExamQuestion(
        id = id,
        examType = ExamType.TOPIK_II,
        section = "Listening",
        period = 1,
        questionTypeKo = typeKo,
        instructionKo = instructionKo,
        stimulusKo = stimulusKo,
        audioTranscriptKo = audioKo,
        maxAudioPlaysInMock = maxPlays,
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
        examType = ExamType.TOPIK_II,
        section = "Reading",
        period = 2,
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
        // ==================== 1회 청취 영역 (Q1~12: Official TOPIK II 1× playback) ====================
        listening(
            id = "t2_l01", typeKo = "이어질 말 고르기 (1회)",
            instructionKo = "다음을 듣고 이어질 수 있는 말을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "남자의 말에 이어질 여자의 말로 가장 알맞은 것을 고르십시오.",
            audioKo = "여자: 팀장님, 이번 달 매출 보고서 초안 작성했는데 한번 검토해 주실 수 있을까요? 남자: 수고했어요. 지금 회의가 곧 시작되니까 이따가 점심 먹고 봐 줄게요.",
            options = listOf("네, 그럼 오후에 다시 찾아뵙겠습니다.", "아니요, 아직 보고서를 시작하지 못했습니다.", "네, 지금 바로 회의를 취소하겠습니다.", "죄송하지만 점심은 이미 먹었습니다."),
            correctIdx = 0, weight = 3, tier = 1, maxPlays = 1,
            explanationEn = "The manager says he will review it after lunch ('이따가 점심 먹고 봐 줄게요'), so replying 'I will check back in the afternoon' is natural.",
            explanationId = "Manajer bilang akan meninjau setelah makan siang, jadi jawaban 'Baik, saya akan menemui Anda lagi siang nanti' paling tepat."
        ),
        listening(
            id = "t2_l02", typeKo = "이어질 말 고르기 (1회)",
            instructionKo = "다음을 듣고 이어질 수 있는 말을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "여자의 말에 이어질 남자의 말로 가장 알맞은 것을 고르십시오.",
            audioKo = "남자: 어제 산 무선 이어폰 한쪽에서 소리가 안 나요. 교환하고 싶은데요. 여자: 불편을 드려 죄송합니다. 구매 영수증과 제품을 함께 보여 주시겠어요?",
            options = listOf("네, 여기 영수증하고 이어폰 있습니다.", "아니요, 새로 하나 더 사고 싶습니다.", "소리가 아주 잘 들려서 만족합니다.", "영수증은 내일 다시 발행해 드리겠습니다."),
            correctIdx = 0, weight = 3, tier = 1, maxPlays = 1,
            explanationEn = "When the clerk asks for the receipt and product ('구매 영수증과 제품을 보여 주시겠어요?'), handing them over directly follows.",
            explanationId = "Saat petugas meminta struk dan produk, jawaban menyerahkan struk beserta earphone paling sesuai."
        ),
        listening(
            id = "t2_l03", typeKo = "행동 파악 (1회)",
            instructionKo = "다음 대화를 듣고 여자가 이어서 할 행동으로 알맞은 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "여자가 이어서 할 행동은 무엇입니까?",
            audioKo = "남자: 행사장 현수막이 아직 도착하지 않았네요. 업체에 전화해 봤어요? 여자: 아, 방금 문자가 왔는데 십 분 뒤에 정문에 도착한대요. 제가 지금 내려가서 받아 올게요. 남자: 그럼 저는 그동안 무대 마이크를 점검하고 있을게요.",
            options = listOf("업체에 다시 전화한다.", "정문으로 내려가서 현수막을 받아 온다.", "무대 마이크를 점검한다.", "행사 시작 시간을 연기한다."),
            correctIdx = 1, weight = 3, tier = 1, maxPlays = 1,
            explanationEn = "The woman says '제가 지금 내려가서 받아 올게요' — she will go down to the main gate to receive the banner.",
            explanationId = "Wanita berkata akan turun ke gerbang utama sekarang untuk menerima spanduk ('내려가서 받아 올게요')."
        ),
        listening(
            id = "t2_l04", typeKo = "행동 파악 (1회)",
            instructionKo = "다음 대화를 듣고 남자가 이어서 할 행동으로 알맞은 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "남자가 이어서 할 행동은 무엇입니까?",
            audioKo = "여자: 선배, 동아리 신입 회원 모집 포스터 디자인이 완성됐어요. 남자: 어디 보자. 글씨 색깔이 배경과 비슷해서 잘 안 보이네. 날짜 부분만 조금 더 진하게 바꾸면 좋겠어. 여자: 알겠어요. 바로 수정해서 보내 드릴게요. 남자: 고마워. 나는 그동안 게시판 사용 허가를 받아 놓을게.",
            options = listOf("포스터 글씨 색깔을 수정한다.", "게시판 사용 허가를 받는다.", "동아리 신입 회원을 면접한다.", "포스터를 인쇄소에 주문한다."),
            correctIdx = 1, weight = 3, tier = 1, maxPlays = 1,
            explanationEn = "The man says '나는 그동안 게시판 사용 허가를 받아 놓을게' (he will obtain permission to use the notice board).",
            explanationId = "Pria berkata akan mengurus izin penggunaan papan pengumuman sementara wanita merevisi poster."
        ),
        listening(
            id = "t2_l05", typeKo = "담화 목적 파악 (1회)",
            instructionKo = "다음을 듣고 남자가 말하는 목적을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "남자는 무엇을 하려고 이 말을 하고 있습니까?",
            audioKo = "남자: 아파트 입주민 여러분, 관리사무소에서 알려 드립니다. 오는 목요일 오전 열 시부터 오후 두 시까지 지하 주차장 바닥 보수 공사가 진행됩니다. 공사 시간 동안 지하 주차장 출입이 통제되오니 차량을 지상 주차장으로 미리 이동해 주시기 바랍니다.",
            options = listOf("주차장 공사와 차량 이동을 안내하려고", "주차 요금 인상을 항의하려고", "관리사무소 직원을 모집하려고", "새 아파트 분양을 홍보하려고"),
            correctIdx = 0, weight = 3, tier = 1, maxPlays = 1,
            explanationEn = "The management office is informing residents about underground parking repairs and requesting vehicle relocation.",
            explanationId = "Kantor pengelola mengumumkan perbaikan lantai parkir bawah tanah dan meminta warga memindahkan kendaraan."
        ),
        listening(
            id = "t2_l06", typeKo = "화자의 태도 파악 (1회)",
            instructionKo = "다음을 듣고 여자의 태도로 알맞은 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "여자는 새 근무 제도에 대해 어떻게 생각합니까?",
            audioKo = "여자: 유연근무제를 도입한 뒤 출퇴근 스트레스가 줄었을 뿐만 아니라 업무 몰입도도 확실히 높아졌습니다. 직원들의 만족도가 매우 커서 앞으로도 계속 유지되었으면 합니다.",
            options = listOf("긍정적으로 평가하고 있다.", "도입을 강하게 반대하고 있다.", "전혀 관심이 없다.", "비용 증가를 걱정하고 있다."),
            correctIdx = 0, weight = 3, tier = 1, maxPlays = 1,
            explanationEn = "Using -(으)ㄹ 뿐만 아니라 to list lower stress, higher focus, and high satisfaction reflects a clearly positive evaluation.",
            explanationId = "Menyebutkan berkurangnya stres, naiknya fokus kerja, dan kepuasan tinggi menunjukkan penilaian yang positif."
        ),
        listening(
            id = "t2_l07", typeKo = "화자의 중심 생각 (1회)",
            instructionKo = "다음을 듣고 남자의 중심 생각을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "남자가 강조하는 내용은 무엇입니까?",
            audioKo = "남자: 요즘 스마트폰으로 뉴스를 쉽게 볼 수 있지만, 출처를 확인하지 않고 공유하면 잘못된 정보가 빠르게 퍼질 수 있습니다. 글을 전달하기 전에 사실 여부를 먼저 점검하는 습관이 필요합니다.",
            options = listOf("스마트폰 사용을 전면 금지해야 한다.", "정보를 공유하기 전에 출처와 사실을 확인해야 한다.", "종이 신문만 구독해야 한다.", "뉴스는 속도가 정확성보다 중요하다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 1,
            explanationEn = "The speaker stresses checking sources and facts before sharing news online.",
            explanationId = "Pembicara menekankan kebiasaan memeriksa sumber dan kebenaran fakta sebelum membagikan berita."
        ),
        listening(
            id = "t2_l08", typeKo = "내용 일치 (1회)",
            instructionKo = "다음을 듣고 들은 내용과 일치하는 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "여자: 최근 도심 속 옥상 텃밭이 큰 인기를 얻고 있습니다. 옥상에 식물을 심으면 여름철 건물 내부 온도를 낮출 수 있을 뿐 아니라 주민들이 함께 채소를 가꾸며 이웃과 소통하는 계기도 됩니다.",
            options = listOf("옥상 텃밭은 건물 내부 온도를 높인다.", "옥상 텃밭은 이웃 간 소통에도 도움이 된다.", "도심에서는 옥상 텃밭을 만들 수 없다.", "옥상 텃밭에서는 채소를 키울 수 없다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 1,
            explanationEn = "Rooftop gardens cool buildings in summer AND create opportunities for neighbors to communicate ('이웃과 소통하는 계기도 됩니다').",
            explanationId = "Kebun atap menurunkan suhu gedung sekaligus menjadi sarana komunikasi antar-tetangga."
        ),
        listening(
            id = "t2_l09", typeKo = "화자의 의도 파악 (1회)",
            instructionKo = "다음 대화를 듣고 여자의 의도로 알맞은 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "여자가 이 말을 한 의도는 무엇입니까?",
            audioKo = "남자: 다음 주 발표 준비는 잘 돼 가? 여자: 자료 조사는 거의 끝났는데, 발표 시간 십 분 안에 내용을 다 담으려니 너무 빠듯하네요. 남자: 핵심 내용 세 가지만 남기고 나머지는 과감히 빼는 게 어때? 여자: 선배님, 시간이 괜찮으시면 제 슬라이드 구성 좀 한번 봐 주실 수 있을까요?",
            options = listOf("발표 날짜를 미루려고", "선배에게 슬라이드 검토를 부탁하려고", "발표 주제를 완전히 바꾸려고", "자료 조사를 대신 해 달라고 하려고"),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 1,
            explanationEn = "She directly asks her senior to review her slide organization ('슬라이드 구성 좀 한번 봐 주실 수 있을까요?').",
            explanationId = "Wanita meminta seniornya untuk membantu meninjau susunan slide presentasinya."
        ),
        listening(
            id = "t2_l10", typeKo = "내용 일치 (1회)",
            instructionKo = "다음을 듣고 들은 내용과 일치하는 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 국립중앙도서관은 다음 달부터 야간 개방 시간을 밤 열 시까지 연장한다고 밝혔습니다. 그동안 직장인들은 퇴근 후 자료실을 이용하기 어렵다는 의견이 많았는데요, 이번 조치로 평일 저녁에도 자유롭게 도서를 열람하고 대출할 수 있게 되었습니다.",
            options = listOf("도서관은 밤 열 시까지 야간 개방을 연장한다.", "직장인은 평일 저녁에 도서를 대출할 수 없다.", "야간 개방 연장은 내년부터 시행된다.", "주말에만 자료실 운영 시간을 늘린다."),
            correctIdx = 0, weight = 3, tier = 2, maxPlays = 1,
            explanationEn = "The National Library announced extending evening hours until 10 PM starting next month.",
            explanationId = "Perpustakaan Nasional memperpanjang jam buka malam sampai pukul 10 malam mulai bulan depan."
        ),
        listening(
            id = "t2_l11", typeKo = "화자의 중심 생각 (1회)",
            instructionKo = "다음을 듣고 여자의 중심 생각을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "여자의 중심 생각으로 가장 알맞은 것을 고르십시오.",
            audioKo = "여자: 실패를 경험하면 많은 사람들이 자신감을 잃고 포기합니다. 그러나 실패의 원인을 차분히 분석해 보면 다음 도전을 성공으로 이끌 귀중한 단서를 찾을 수 있습니다.",
            options = listOf("실패는 피해야 할 부끄러운 일이다.", "실패의 원인을 분석하면 다음 성공의 밑거름이 된다.", "자신감이 없으면 도전을 멈춰야 한다.", "한 번 실패한 분야는 다시 시도하지 않는 것이 좋다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 1,
            explanationEn = "Analyzing the causes of failure provides valuable clues for future success.",
            explanationId = "Menganalisis penyebab kegagalan memberi petunjuk berharga untuk meraih keberhasilan berikutnya."
        ),
        listening(
            id = "t2_l12", typeKo = "내용 일치 (1회)",
            instructionKo = "다음을 듣고 들은 내용과 일치하는 것을 고르십시오. (한 번만 들려줍니다)",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 최근 종이 영수증 대신 모바일 전자 영수증을 발급받는 소비자가 크게 늘었습니다. 종이 생산에 드는 나무와 물을 아낄 수 있을 뿐만 아니라, 영수증을 잃어버릴 염려 없이 앱에서 교환이나 환불 내역을 손쉽게 관리할 수 있기 때문입니다.",
            options = listOf("전자 영수증은 교환이나 환불에 사용할 수 없다.", "전자 영수증 이용이 늘면서 자원 절약 효과가 있다.", "종이 영수증은 앱에서 자동으로 관리된다.", "전자 영수증 발급을 이용하는 소비자가 줄고 있다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 1,
            explanationEn = "Mobile e-receipts save trees and water used in paper production and simplify tracking exchanges/refunds.",
            explanationId = "Struk digital menghemat kayu dan air untuk produksi kertas serta memudahkan pengelolaan retur di aplikasi."
        ),
        // ==================== 2회 청취 영역 (Q13~30: Official TOPIK II 2× playback) ====================
        listening(
            id = "t2_l13", typeKo = "중심 생각 파악 (2회)",
            instructionKo = "다음을 듣고 남자의 중심 생각을 고르십시오. (두 번 들려줍니다)",
            stimulusKo = "남자의 중심 생각으로 알맞은 것을 고르십시오.",
            audioKo = "남자: 지역 축제가 성공하려면 화려한 공연이나 유명 가수 초청에만 예산을 쏟기보다, 그 지역만의 고유한 역사와 생활 문화를 체험할 수 있는 프로그램을 개발해야 합니다. 그래야 관광객들이 일회성 방문에 그치지 않고 다시 찾게 됩니다.",
            options = listOf("유명 가수를 많이 초청해야 축제가 성공한다.", "지역 고유의 문화를 살린 체험형 축제를 만들어야 한다.", "지역 축제는 예산을 최대한 많이 써야 한다.", "관광객은 공연에만 관심을 가진다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 2,
            explanationEn = "He argues local festivals succeed when offering unique local history/culture experiences rather than relying solely on celebrity concerts.",
            explanationId = "Pembicara menekankan festival daerah perlu mengembangkan program budaya khas setempat, bukan hanya mengundang artis terkenal."
        ),
        listening(
            id = "t2_l14", typeKo = "중심 생각 파악 (2회)",
            instructionKo = "다음을 듣고 여자의 중심 생각을 고르십시오. (두 번 들려줍니다)",
            stimulusKo = "여자의 중심 생각으로 알맞은 것을 고르십시오.",
            audioKo = "여자: 어르신들이 키오스크나 모바일 금융 앱을 사용하는 데 어려움을 겪는 경우가 많습니다. 기술이 발전할수록 디지털 소외 계층을 위한 쉬운 안내 화면과 오프라인 지원 창구를 함께 마련하는 배려가 반드시 필요합니다.",
            options = listOf("모든 오프라인 창구를 빠르게 없애야 한다.", "디지털 취약 계층을 배려한 쉬운 인터페이스와 지원이 필요하다.", "어르신들은 금융 앱을 쓰지 않는 편이 안전하다.", "키오스크 설치를 법으로 전면 금지해야 한다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 2,
            explanationEn = "She stresses providing accessible UI and offline assistance counters for digitally underserved groups as technology advances.",
            explanationId = "Wanita menekankan perlunya tampilan ramah pengguna dan loket bantuan luring bagi kelompok rentan digital."
        ),
        // 묶음 듣기 세트 1 (인터뷰 — 2문항)
        listening(
            id = "t2_l15", typeKo = "묶음 듣기 · 인터뷰 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "여자는 왜 이 대화를 나누고 있습니까?",
            audioKo = "여자: 안녕하세요, 박사님. 최근 도시 가로수로 어떤 나무를 심느냐에 따라 미세먼지 저감 효과가 크게 달라진다는 연구 결과를 발표하셨는데요, 구체적으로 설명해 주시겠습니까? 남자: 네, 잎 표면이 거칠고 잔털이 많은 소나무나 느티나무는 일반 활엽수보다 미세먼지 흡착률이 두 배 가까이 높았습니다. 하지만 한 종류의 나무만 심으면 병충해에 취약하므로 지역 기후와 토양에 맞춰 여러 수종을 혼합해 심는 것이 바람직합니다.",
            options = listOf("가로수 연구 결과에 대해 전문가의 설명을 들으려고", "새로운 공원 개장 일정을 홍보하려고", "병충해 방제 약품 구매를 요청하려고", "소나무 벌목 반대 서명을 받으려고"),
            correctIdx = 0, weight = 3, tier = 2, maxPlays = 2,
            explanationEn = "The interviewer asks the researcher to explain the study linking street tree species to fine dust reduction.",
            explanationId = "Pewawancara meminta penjelasan ahli mengenai hasil riset jenis pohon peneduh jalan terhadap pengurangan debu halus.",
            groupId = "t2_lg_1", groupLabelKo = "※ [묶음 듣기] 다음 인터뷰를 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l16", typeKo = "묶음 듣기 · 내용 일치 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "여자: 안녕하세요, 박사님. 최근 도시 가로수로 어떤 나무를 심느냐에 따라 미세먼지 저감 효과가 크게 달라진다는 연구 결과를 발표하셨는데요, 구체적으로 설명해 주시겠습니까? 남자: 네, 잎 표면이 거칠고 잔털이 많은 소나무나 느티나무는 일반 활엽수보다 미세먼지 흡착률이 두 배 가까이 높았습니다. 하지만 한 종류의 나무만 심으면 병충해에 취약하므로 지역 기후와 토양에 맞춰 여러 수종을 혼합해 심는 것이 바람직합니다.",
            options = listOf("소나무는 미세먼지 흡착률이 가장 낮다.", "한 종류의 나무만 심으면 병충해에 강해진다.", "지역 기후와 토양에 맞게 여러 수종을 섞어 심는 것이 좋다.", "잎이 매끄러운 나무일수록 미세먼지를 잘 흡수한다."),
            correctIdx = 2, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "The researcher recommends mixing multiple tree species suited to local climate and soil to avoid pest vulnerability.",
            explanationId = "Peneliti menyarankan menanam campuran berbagai jenis pohon sesuai iklim dan tanah agar tidak rentan hama.",
            groupId = "t2_lg_1", groupLabelKo = "※ [묶음 듣기] 다음 인터뷰를 잘 듣고 두 물음에 답하십시오."
        ),
        // 묶음 듣기 세트 2 (직장 토론 — 2문항)
        listening(
            id = "t2_l17", typeKo = "묶음 듣기 · 태도 파악 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "남자는 친환경 포장재 도입에 대해 어떤 입장을 보이고 있습니까?",
            audioKo = "여자: 과장님, 다음 분기부터 배송 상자를 모두 재활용 종이 테이프와 친환경 완충재로 바꾸자는 제안이 들어왔습니다. 남자: 취지는 정말 좋지만, 기존 포장재보다 단가가 이십 퍼센트 가까이 올라가서 비용 부담이 걱정되는군요. 여자: 초기 비용은 다소 들지만, 최근 소비자들이 친환경 기업 제품을 선호하는 추세라 장기적으로는 브랜드 신뢰도와 매출 상승으로 이어질 겁니다. 남자: 그렇다면 먼저 주력 상품 두 가지에 시범 적용해 보고 고객 반응을 살펴본 뒤에 확대 여부를 결정합시다.",
            options = listOf("비용 부담을 고려해 시범 도입 후 확대를 제안하고 있다.", "단가 문제로 친환경 전환을 전면 거절하고 있다.", "모든 제품에 내일부터 즉시 적용하자고 주장하고 있다.", "소비자 반응에는 전혀 관심이 없다고 말하고 있다."),
            correctIdx = 0, weight = 3, tier = 2, maxPlays = 2,
            explanationEn = "Concerned about the ~20% unit cost hike, the manager proposes piloting on two flagship products first before expanding.",
            explanationId = "Karena mempertimbangkan kenaikan biaya 20%, manajer mengusulkan uji coba dulu pada dua produk utama sebelum diperluas.",
            groupId = "t2_lg_2", groupLabelKo = "※ [묶음 듣기] 다음 회의 대화를 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l18", typeKo = "묶음 듣기 · 내용 일치 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "여자: 과장님, 다음 분기부터 배송 상자를 모두 재활용 종이 테이프와 친환경 완충재로 바꾸자는 제안이 들어왔습니다. 남자: 취지는 정말 좋지만, 기존 포장재보다 단가가 이십 퍼센트 가까이 올라가서 비용 부담이 걱정되는군요. 여자: 초기 비용은 다소 들지만, 최근 소비자들이 친환경 기업 제품을 선호하는 추세라 장기적으로는 브랜드 신뢰도와 매출 상승으로 이어질 겁니다. 남자: 그렇다면 먼저 주력 상품 두 가지에 시범 적용해 보고 고객 반응을 살펴본 뒤에 확대 여부를 결정합시다.",
            options = listOf("친환경 포장재는 기존보다 가격이 이십 퍼센트 저렴하다.", "여자는 친환경 전환이 장기적으로 브랜드 신뢰도를 높인다고 본다.", "두 사람은 이미 모든 제품의 포장재를 교체했다.", "남자는 고객 반응을 조사하지 말자고 했다."),
            correctIdx = 1, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "The woman argues that eco-friendly packaging will boost long-term brand trust and sales ('장기적으로는 브랜드 신뢰도와 매출 상승으로 이어질 겁니다').",
            explanationId = "Wanita berpendapat kemasan ramah lingkungan akan meningkatkan kepercayaan merek dan penjualan dalam jangka panjang.",
            groupId = "t2_lg_2", groupLabelKo = "※ [묶음 듣기] 다음 회의 대화를 잘 듣고 두 물음에 답하십시오."
        ),
        // 묶음 듣기 세트 3 (강연 — 2문항)
        listening(
            id = "t2_l19", typeKo = "묶음 듣기 · 강연 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "강연자가 말하고자 하는 중심 내용은 무엇입니까?",
            audioKo = "남자: 많은 사람들이 기억력을 높이려면 한 번에 오랫동안 반복해서 외워야 한다고 생각합니다. 그러나 인지심리학 연구에 따르면, 뇌는 정보를 잊어버리기 직전에 다시 떠올리는 과정을 거칠 때 기억을 장기 저장소로 옮깁니다. 이를 '간격 반복 효과'라고 합니다. 따라서 하루에 세 시간을 몰아서 공부하기보다, 하루 삼십 분씩 며칠에 나누어 복습하는 것이 훨씬 효율적입니다.",
            options = listOf("시험 전날 밤을 새워 암기하는 것이 가장 효과적이다.", "일정한 간격을 두고 나누어 복습하는 것이 장기 기억에 유리하다.", "기억력은 타고나는 것이므로 훈련으로 바꿀 수 없다.", "정보를 한 번만 읽고 절대 복습하지 않는 것이 좋다."),
            correctIdx = 1, weight = 3, tier = 3, maxPlays = 2,
            explanationEn = "The lecturer explains the 'spaced repetition effect' (간격 반복 효과): reviewing across spaced intervals beats cramming.",
            explanationId = "Pembicara menjelaskan efek pengulangan berjarak (간격 반복 효과): mengulang berkala lebih efektif daripada belajar semalam suntuk.",
            groupId = "t2_lg_3", groupLabelKo = "※ [묶음 듣기] 다음 강연을 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l20", typeKo = "묶음 듣기 · 세부 내용 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "남자: 많은 사람들이 기억력을 높이려면 한 번에 오랫동안 반복해서 외워야 한다고 생각합니다. 그러나 인지심리학 연구에 따르면, 뇌는 정보를 잊어버리기 직전에 다시 떠올리는 과정을 거칠 때 기억을 장기 저장소로 옮깁니다. 이를 '간격 반복 효과'라고 합니다. 따라서 하루에 세 시간을 몰아서 공부하기보다, 하루 삼십 분씩 며칠에 나누어 복습하는 것이 훨씬 효율적입니다.",
            options = listOf("뇌는 정보를 잊기 직전에 다시 떠올릴 때 장기 기억으로 전환한다.", "하루에 세 시간 몰아서 공부하는 것이 가장 효율적이다.", "간격 반복 효과는 체력 운동에만 적용되는 이론이다.", "반복 복습은 장기 기억 형성을 방해한다."),
            correctIdx = 0, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "The brain transfers information to long-term storage when recalling it right before forgetting ('잊어버리기 직전에 다시 떠올리는 과정').",
            explanationId = "Otak memindahkan informasi ke memori jangka panjang saat diingat kembali tepat sebelum lupa.",
            groupId = "t2_lg_3", groupLabelKo = "※ [묶음 듣기] 다음 강연을 잘 듣고 두 물음에 답하십시오."
        ),
        // 묶음 듣기 세트 4 (다큐멘터리/보도 — 2문항)
        listening(
            id = "t2_l21", typeKo = "묶음 듣기 · 보도 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "이 보도의 주제로 가장 알맞은 것을 고르십시오.",
            audioKo = "여자: 최근 버려지는 폐플라스틱과 헌 옷을 활용해 새로운 디자인 제품으로 재탄생시키는 '새활용' 산업이 주목받고 있습니다. 단순한 재활용이 원료를 분해해 다시 쓰는 과정이라면, 새활용은 창의적인 디자인을 더해 원래 제품보다 높은 부가가치를 만들어 낸다는 점에서 차이가 있습니다. 특히 가치 소비를 중시하는 젊은 세대의 호응이 높아 시장 규모가 매년 두 자릿수 성장세를 기록하고 있습니다.",
            options = listOf("새활용 산업의 특징과 성장 배경", "의류 공장의 해외 이전 원인", "플라스틱 생산량 증가에 따른 수출 확대", "전통 수공예 기술의 소멸 위기"),
            correctIdx = 0, weight = 3, tier = 3, maxPlays = 2,
            explanationEn = "The report contrasts upcycling (새활용) with simple recycling and explains why value-driven consumers fuel its double-digit growth.",
            explanationId = "Laporan membahas ciri industri upcycling (새활용) dibanding daur ulang biasa serta penyebab pertumbuhannya.",
            groupId = "t2_lg_4", groupLabelKo = "※ [묶음 듣기] 다음 뉴스 보도를 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l22", typeKo = "묶음 듣기 · 내용 일치 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "여자: 최근 버려지는 폐플라스틱과 헌 옷을 활용해 새로운 디자인 제품으로 재탄생시키는 '새활용' 산업이 주목받고 있습니다. 단순한 재활용이 원료를 분해해 다시 쓰는 과정이라면, 새활용은 창의적인 디자인을 더해 원래 제품보다 높은 부가가치를 만들어 낸다는 점에서 차이가 있습니다. 특히 가치 소비를 중시하는 젊은 세대의 호응이 높아 시장 규모가 매년 두 자릿수 성장세를 기록하고 있습니다.",
            options = listOf("새활용은 단순 재활용과 완전히 동일한 개념이다.", "새활용은 디자인을 더해 원래보다 높은 가치를 창출한다.", "젊은 세대는 새활용 제품에 관심이 적다.", "새활용 시장 규모는 최근 급격히 축소되고 있다."),
            correctIdx = 1, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "Upcycling adds creative design to generate higher added value than the original item ('원래 제품보다 높은 부가가치를 만들어 낸다').",
            explanationId = "Upcycling menambahkan desain kreatif untuk menciptakan nilai tambah lebih tinggi dari produk aslinya.",
            groupId = "t2_lg_4", groupLabelKo = "※ [묶음 듣기] 다음 뉴스 보도를 잘 듣고 두 물음에 답하십시오."
        ),
        // 추가 고급 듣기 문항 (t2_l23..t2_l30)
        listening(
            id = "t2_l23", typeKo = "화자의 태도 (2회)",
            instructionKo = "다음을 듣고 남자의 태도로 가장 알맞은 것을 고르십시오. (두 번 들려줍니다)",
            stimulusKo = "남자는 자율주행 버스 시범 운행에 대해 어떤 태도를 보이고 있습니까?",
            audioKo = "남자: 도심 심야 구간에 자율주행 버스가 도입된 것은 교통 사각지대 해소라는 측면에서 반가운 변화입니다. 다만 악천후 상황에서의 센서 오류나 돌발 사고 시 책임 소재가 아직 명확하지 않으므로, 본격적인 확대에 앞서 안전 검증 기준을 철저히 보완해야 합니다.",
            options = listOf("긍정적 취지는 인정하되 안전 기준 보완을 촉구하고 있다.", "자율주행 기술의 발전을 전적으로 회의하고 있다.", "안전 검증 없이 즉시 전국으로 확대하자고 주장한다.", "심야 교통 문제는 해결할 필요가 없다고 본다."),
            correctIdx = 0, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "He welcomes the goal of closing late-night transit gaps ('반가운 변화') while urging stricter safety verification before full rollout ('철저히 보완해야 합니다').",
            explanationId = "Ia menyambut baik tujuannya namun mendesak penyempurnaan standar keselamatan sebelum diperluas."
        ),
        listening(
            id = "t2_l24", typeKo = "강연 세부 이해 (2회)",
            instructionKo = "다음을 듣고 들은 내용과 일치하는 것을 고르십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "여자: 한국 전통 건축에서 처마의 곡선은 단순히 아름다움을 위한 장식이 아닙니다. 여름에는 태양의 고도가 높기 때문에 길게 뻗은 처마가 뜨거운 직사광선을 막아 주고, 반대로 태양 고도가 낮은 겨울에는 햇빛이 방 안 깊숙이 들어오도록 설계된 과학적인 구조입니다.",
            options = listOf("처마의 곡선은 오직 미적 장식만을 위해 만들어졌다.", "처마는 여름 햇빛을 차단하고 겨울 햇빛을 받아들이는 역할을 한다.", "겨울에는 태양의 고도가 여름보다 훨씬 높다.", "처마 때문에 겨울철 실내가 더욱 어두워진다."),
            correctIdx = 1, weight = 3, tier = 2, maxPlays = 2,
            explanationEn = "Traditional eaves (처마) scientifically block high summer sun and invite low winter sunlight deep indoors.",
            explanationId = "Atap menjorok (처마) secara ilmiah menahan matahari terik musim panas dan memasukkan cahaya matahari musim dingin."
        ),
        listening(
            id = "t2_l25", typeKo = "대담 분석 (2회)",
            instructionKo = "다음을 듣고 남자가 제안하는 해결 방안을 고르십시오. (두 번 들려줍니다)",
            stimulusKo = "남자가 제안하는 해결 방안으로 알맞은 것을 고르십시오.",
            audioKo = "여자: 최근 지방 소도시의 인구 감소 문제가 갈수록 심각해지고 있습니다. 남자: 맞습니다. 단순히 일회성 지원금을 지급하는 정책만으로는 젊은 층의 정착을 이끌기 어렵습니다. 양질의 일자리와 의료, 교육 인프라를 함께 구축하고, 도시와 농촌을 오가며 생활하는 '생활인구'를 유치하는 다각적인 전략이 필요합니다.",
            options = listOf("일회성 현금 지원금만 대폭 늘린다.", "일자리·생활 인프라 확충과 생활인구 유치 전략을 병행한다.", "모든 공공기관을 수도권으로 다시 집중시킨다.", "지방 소도시의 교육 시설을 통폐합해 줄인다."),
            correctIdx = 1, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "Instead of one-time cash grants, he proposes building jobs/medical/education infrastructure and attracting part-time residents (생활인구).",
            explanationId = "Ia mengusulkan pembangunan lapangan kerja, infrastruktur kesehatan/pendidikan, dan menarik penduduk musiman (생활인구)."
        ),
        listening(
            id = "t2_l26", typeKo = "다큐멘터리 이해 (2회)",
            instructionKo = "다음을 듣고 들은 내용과 일치하는 것을 고르십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "여자: 흔히 꿀벌은 꿀을 만드는 곤충으로만 알려져 있지만, 사실 인류가 소비하는 주요 농작물의 약 칠십 퍼센트가 꿀벌의 수분 활동에 의존하고 있습니다. 최근 이상 기온과 농약 사용으로 꿀벌 개체 수가 급감하면서 세계 식량 안보에도 비상이 걸렸습니다.",
            options = listOf("인류 주요 농작물의 상당 부분이 꿀벌의 수분에 의존한다.", "이상 기온 덕분에 꿀벌 개체 수가 빠르게 늘고 있다.", "꿀벌은 농작물 생산과 아무런 관련이 없다.", "농약 사용은 꿀벌 보호에 큰 도움이 된다."),
            correctIdx = 0, weight = 3, tier = 2, maxPlays = 2,
            explanationEn = "About 70% of major human food crops depend on honeybee pollination ('약 칠십 퍼센트가 꿀벌의 수분 활동에 의존').",
            explanationId = "Sekitar 70% tanaman pangan utama manusia bergantung pada penyerbukan oleh lebah madu."
        ),
        listening(
            id = "t2_l27", typeKo = "묶음 듣기 · 대담 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "여자가 지적하는 '빠른 패션(패스트 패션)'의 문제점은 무엇입니까?",
            audioKo = "남자: 요즘 유행에 맞춰 저렴한 옷을 빠르게 생산하는 패스트 패션이 여전히 큰 인기를 끌고 있습니다. 여자: 소비자 입장에서는 적은 비용으로 다양한 스타일을 즐길 수 있다는 장점이 있죠. 하지만 몇 번 입지 않고 버려지는 의류 폐기물이 막대하게 늘어나 토양과 수질을 오염시키고 있습니다. 이제는 기업이 생산 단계부터 내구성이 높은 소재를 사용하고, 소비자도 오래 입을 수 있는 옷을 신중하게 고르는 문화가 자리 잡아야 합니다.",
            options = listOf("옷 가격이 지나치게 비싸 소비자가 구매하기 어렵다.", "짧게 입고 버려지는 의류 폐기물이 심각한 환경 오염을 유발한다.", "유행 변화 속도가 너무 느려 선택의 폭이 좁다.", "내구성이 너무 높아 새 제품이 팔리지 않는다."),
            correctIdx = 1, weight = 3, tier = 3, maxPlays = 2,
            explanationEn = "She highlights massive textile waste from lightly worn clothes causing soil and water pollution.",
            explanationId = "Wanita menyoroti limbah pakaian yang cepat dibuang sehingga mencemari tanah dan air.",
            groupId = "t2_lg_5", groupLabelKo = "※ [묶음 듣기] 다음 대담을 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l28", typeKo = "묶음 듣기 · 주장 파악 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "여자의 제안과 일치하는 것을 고르십시오.",
            audioKo = "남자: 요즘 유행에 맞춰 저렴한 옷을 빠르게 생산하는 패스트 패션이 여전히 큰 인기를 끌고 있습니다. 여자: 소비자 입장에서는 적은 비용으로 다양한 스타일을 즐길 수 있다는 장점이 있죠. 하지만 몇 번 입지 않고 버려지는 의류 폐기물이 막대하게 늘어나 토양과 수질을 오염시키고 있습니다. 이제는 기업이 생산 단계부터 내구성이 높은 소재를 사용하고, 소비자도 오래 입을 수 있는 옷을 신중하게 고르는 문화가 자리 잡아야 합니다.",
            options = listOf("기업은 내구성 높은 소재를 쓰고 소비자는 신중히 선택해야 한다.", "소비자만 책임을 지고 기업은 바꿀 필요가 없다.", "저렴한 옷을 더 자주 교체하는 문화를 확산해야 한다.", "의류 생산을 전면 중단해야 한다."),
            correctIdx = 0, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "She calls for both durable materials by companies and mindful, long-term purchasing by consumers.",
            explanationId = "Ia mengusulkan perusahaan memakai bahan tahan lama dan konsumen memilih pakaian awet secara bijak.",
            groupId = "t2_lg_5", groupLabelKo = "※ [묶음 듣기] 다음 대담을 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l29", typeKo = "묶음 듣기 · 강연 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "강연자가 설명하는 '수면 부채'를 해소하기 위한 올바른 방법은 무엇입니까?",
            audioKo = "남자: 평일에 잠을 줄여 일하고 주말에 몰아서 자면 피로가 풀릴 거라고 믿는 직장인이 많습니다. 하지만 부족한 잠이 누적되는 '수면 부채'는 주말 폭잠으로 완전히 갚아지지 않을 뿐더러, 오히려 생체 리듬을 깨뜨려 월요일 피로를 악화시킵니다. 주말에도 평일과 기상 시간 차이를 두 시간 이내로 유지하고, 낮에 이십 분 정도의 짧은 낮잠으로 보충하는 것이 건강에 훨씬 유익합니다.",
            options = listOf("주말에 열두 시간 이상 몰아서 잔다.", "주말 기상 시간 차이를 두 시간 이내로 유지하고 짧은 낮잠을 활용한다.", "평일에는 잠을 전혀 자지 않고 커피로 버틴다.", "오후 늦게 두 시간 이상 깊게 낮잠을 잔다."),
            correctIdx = 1, weight = 3, tier = 3, maxPlays = 2,
            explanationEn = "Keeping weekend wake-up times within 2 hours of weekdays and taking a ~20-minute nap protects circadian rhythm.",
            explanationId = "Menjaga selisih jam bangun akhir pekan maksimal 2 jam dari hari kerja dan tidur siang singkat ~20 menit lebih sehat.",
            groupId = "t2_lg_6", groupLabelKo = "※ [묶음 듣기] 다음 건강 강연을 잘 듣고 두 물음에 답하십시오."
        ),
        listening(
            id = "t2_l30", typeKo = "묶음 듣기 · 내용 일치 (2회)",
            instructionKo = "다음을 듣고 물음에 답하십시오. (두 번 들려줍니다)",
            stimulusKo = "들은 내용과 일치하는 것을 고르십시오.",
            audioKo = "남자: 평일에 잠을 줄여 일하고 주말에 몰아서 자면 피로가 풀릴 거라고 믿는 직장인이 많습니다. 하지만 부족한 잠이 누적되는 '수면 부채'는 주말 폭잠으로 완전히 갚아지지 않을 뿐더러, 오히려 생체 리듬을 깨뜨려 월요일 피로를 악화시킵니다. 주말에도 평일과 기상 시간 차이를 두 시간 이내로 유지하고, 낮에 이십 분 정도의 짧은 낮잠으로 보충하는 것이 건강에 훨씬 유익합니다.",
            options = listOf("주말에 몰아서 자면 수면 부채가 완전히 해결된다.", "주말 폭잠은 생체 리듬을 깨뜨려 월요일 피로를 키울 수 있다.", "낮잠은 건강에 항상 해로우므로 피해야 한다.", "수면 부채는 직장인에게만 나타나는 희귀 질환이다."),
            correctIdx = 1, weight = 4, tier = 3, maxPlays = 2,
            explanationEn = "Weekend oversleeping disrupts the biological clock and worsens Monday fatigue ('생체 리듬을 깨뜨려 월요일 피로를 악화시킵니다').",
            explanationId = "Tidur berlebihan di akhir pekan justru merusak ritme biologis dan memperparah kelelahan hari Senin.",
            groupId = "t2_lg_6", groupLabelKo = "※ [묶음 듣기] 다음 건강 강연을 잘 듣고 두 물음에 답하십시오."
        )
    )

    // ==================== 1교시 쓰기 (51·52·53·54번 = 정확히 100점!) ====================

    val writingTask51Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "t2_w51_v1", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "51번 · 실용문 빈칸 완성",
            instructionKo = "[51번] 다음을 읽고 ( ㄱ )과 ( ㄴ )에 들어갈 말을 각각 한 문장으로 쓰십시오. (10점)",
            passageBoxKo = "[ 한국어 말하기 대회 참가 신청 안내 ]\n\n안녕하세요? 한국어교육원입니다.\n다음 달 15일에 외국인 유학생을 위한 한국어 말하기 대회를 개최합니다.\n참가를 원하시는 분은 이번 주 금요일까지 홈페이지에서 (   ㄱ   ).\n대회에 대해 궁금한 점이 있으시면 언제든지 사무실로 (   ㄴ   ).\n많은 관심과 참여 부탁드립니다.",
            stimulusKo = "안내문의 문맥과 격식체(-ㅂ/습니다, -아/어 주십시오)에 맞게 ( ㄱ )과 ( ㄴ )을 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 51, dualBlanks = true,
            targetMinChars = 14, targetMaxChars = 70, pointWeight = 10, difficultyTier = 1,
            expectedKeywords = listOf("신청", "문의", "연락", "주시기", "주십시오", "바랍니다", "하세요"),
            modelAnswerKo = "(ㄱ) 신청해 주시기 바랍니다 / 신청하시기 바랍니다\n(ㄴ) 문의해 주시기 바랍니다 / 연락해 주십시오",
            explanationEn = "Task 51 tests practical formal announcements (-아/어 주시기 바랍니다 / -(으)시기 바랍니다): (ㄱ) apply on the website, (ㄴ) contact/inquire at the office.",
            explanationId = "Soal 51 menguji pengumuman praktis formal (-아/어 주시기 바랍니다): (ㄱ) mendaftar di situs web (신청), (ㄴ) menghubungi/bertanya ke kantor (문의/연락)."
        ),
        ExamQuestion(
            id = "t2_w51_v2", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "51번 · 실용문 빈칸 완성",
            instructionKo = "[51번] 다음을 읽고 ( ㄱ )과 ( ㄴ )에 들어갈 말을 각각 한 문장으로 쓰십시오. (10점)",
            passageBoxKo = "[ 이메일 — 김민수 교수님께 ]\n\n교수님, 안녕하십니까? 한국어학과 2학년 지수입니다.\n다름이 아니라 이번 목요일 수업에 병원 진료 때문에 (   ㄱ   ).\n죄송하지만 이번 수업 발표 자료를 이메일로 (   ㄴ   )?\n다음 수업에는 꼭 출석하겠습니다. 감사합니다.",
            stimulusKo = "교수님께 보내는 정중한 이메일 문맥에 맞게 ( ㄱ )과 ( ㄴ )을 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 51, dualBlanks = true,
            targetMinChars = 14, targetMaxChars = 70, pointWeight = 10, difficultyTier = 1,
            expectedKeywords = listOf("결석", "참석", "못", "보내", "주실", "수", "있을까요", "습니다"),
            modelAnswerKo = "(ㄱ) 참석하지 못할 것 같습니다 / 결석하게 되었습니다\n(ㄴ) 보내 주실 수 있을까요 / 보내 주실 수 있습니까",
            explanationEn = "Formal polite email to a professor: (ㄱ) state inability to attend ('참석하지 못할 것 같습니다'), (ㄴ) politely request sending materials ('보내 주실 수 있을까요').",
            explanationId = "Email sopan kepada dosen: (ㄱ) menyatakan tidak dapat hadir ('참석하지 못할 것 같습니다'), (ㄴ) memohon materi dikirim ('보내 주실 수 있을까요')."
        ),
        ExamQuestion(
            id = "t2_w51_v3", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "51번 · 실용문 빈칸 완성",
            instructionKo = "[51번] 다음을 읽고 ( ㄱ )과 ( ㄴ )에 들어갈 말을 각각 한 문장으로 쓰십시오. (10점)",
            passageBoxKo = "[ 중고 책 나눔 게시판 ]\n\n안녕하세요. 기숙사에 살고 있는 학생입니다.\n이번에 졸업을 하게 되어 그동안 보던 TOPIK 교재 다섯 권을 (   ㄱ   ).\n책 상태가 아주 깨끗하니 필요한 분께 도움이 되면 좋겠습니다.\n원하시는 분은 아래 댓글로 (   ㄴ   ).",
            stimulusKo = "게시판 안내 글의 문맥과 정중한 표현에 맞게 ( ㄱ )과 ( ㄴ )을 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 51, dualBlanks = true,
            targetMinChars = 14, targetMaxChars = 70, pointWeight = 10, difficultyTier = 1,
            expectedKeywords = listOf("나누", "드리", "줄", "연락", "남겨", "주시기", "주세요", "습니다"),
            modelAnswerKo = "(ㄱ) 무료로 나누어 드리려고 합니다 / 드리려고 합니다\n(ㄴ) 연락해 주시기 바랍니다 / 남겨 주세요",
            explanationEn = "Campus sharing notice: (ㄱ) intend to give away books ('나누어 드리려고 합니다'), (ㄴ) ask interested readers to leave a comment ('남겨 주세요 / 연락해 주세요').",
            explanationId = "Pengumuman berbagi buku: (ㄱ) bermaksud membagikan buku ('나누어 드리려고 합니다'), (ㄴ) meminta meninggalkan komentar ('남겨 주세요 / 연락해 주세요')."
        )
    )

    val writingTask52Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "t2_w52_v1", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "52번 · 설명문 빈칸 완성",
            instructionKo = "[52번] 다음을 읽고 ( ㄱ )과 ( ㄴ )에 들어갈 말을 격식 문어체(-는다/ㄴ다)로 쓰십시오. (10점)",
            passageBoxKo = "우리는 흔히 식사 직후에 곧바로 양치질을 하는 것이 치아 건강에 가장 좋다고 생각한다. 그러나 탄산음료나 과일 주스처럼 산성이 강한 음식을 먹은 직후에 바로 이를 닦으면 오히려 치아 표면이 (   ㄱ   ). 따라서 산성 음식을 섭취한 뒤에는 먼저 물로 입을 헹구고 삼십 분 정도 지난 후에 (   ㄴ   ).",
            stimulusKo = "설명문의 논리적 흐름과 문어체(-는다/ㄴ다)에 맞게 ( ㄱ )과 ( ㄴ )을 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 52, dualBlanks = true,
            targetMinChars = 14, targetMaxChars = 75, pointWeight = 10, difficultyTier = 2,
            expectedKeywords = listOf("손상", "상", "약해", "양치", "닦", "좋다", "한다", "된다"),
            modelAnswerKo = "(ㄱ) 손상될 수 있다 / 상할 수 있다\n(ㄴ) 양치질을 하는 것이 좋다 / 이를 닦아야 한다",
            explanationEn = "Task 52 uses formal plain style (-는다/ㄴ다): (ㄱ) tooth enamel can be damaged ('손상될 수 있다'), (ㄴ) it is better to brush after 30 minutes ('양치질을 하는 것이 좋다').",
            explanationId = "Soal 52 menggunakan gaya tulis formal (-는다/ㄴ다): (ㄱ) permukaan gigi bisa rusak ('손상될 수 있다'), (ㄴ) sebaiknya menyikat gigi setelah 30 menit ('양치질을 하는 것이 좋다')."
        ),
        ExamQuestion(
            id = "t2_w52_v2", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "52번 · 설명문 빈칸 완성",
            instructionKo = "[52번] 다음을 읽고 ( ㄱ )과 ( ㄴ )에 들어갈 말을 격식 문어체(-는다/ㄴ다)로 쓰십시오. (10점)",
            passageBoxKo = "운동할 때 목이 마르면 물을 한꺼번에 많이 마시는 사람이 있다. 하지만 운동 중에 찬물을 급하게 많이 마시면 위에 부담이 가서 복통이 (   ㄱ   ). 그러므로 운동 전후에는 미지근한 물을 조금씩 자주 (   ㄴ   ).",
            stimulusKo = "설명문의 인과 관계와 문어체(-는다/ㄴ다)에 맞게 ( ㄱ )과 ( ㄴ )을 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 52, dualBlanks = true,
            targetMinChars = 14, targetMaxChars = 75, pointWeight = 10, difficultyTier = 2,
            expectedKeywords = listOf("생길", "날", "일어", "마시", "마셔", "좋다", "한다", "있다"),
            modelAnswerKo = "(ㄱ) 생길 수 있다 / 날 수 있다\n(ㄴ) 마시는 것이 좋다 / 마셔야 한다",
            explanationEn = "(ㄱ) Drinking cold water rapidly can cause stomachache ('생길 수 있다'); (ㄴ) one should drink lukewarm water little by little ('마시는 것이 좋다').",
            explanationId = "(ㄱ) Minum air dingin cepat bisa menimbulkan sakit perut ('생길 수 있다'); (ㄴ) sebaiknya minum air hangat sedikit demi sedikit ('마시는 것이 좋다')."
        ),
        ExamQuestion(
            id = "t2_w52_v3", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "52번 · 설명문 빈칸 완성",
            instructionKo = "[52번] 다음을 읽고 ( ㄱ )과 ( ㄴ )에 들어갈 말을 격식 문어체(-는다/ㄴ다)로 쓰십시오. (10점)",
            passageBoxKo = "책상 위에 불필요한 물건이 많이 놓여 있으면 시선이 분산되어 일에 집중하기가 어렵다. 반대로 주변을 깔끔하게 정리하면 필요한 자료를 빨리 찾을 수 있을 뿐만 아니라 마음의 여유도 (   ㄱ   ). 이처럼 업무 효율을 높이기 위해서는 일을 시작하기 전에 먼저 책상을 (   ㄴ   ).",
            stimulusKo = "설명문의 대조와 결론에 맞게 ( ㄱ )과 ( ㄴ )을 문어체로 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 52, dualBlanks = true,
            targetMinChars = 14, targetMaxChars = 75, pointWeight = 10, difficultyTier = 2,
            expectedKeywords = listOf("생긴다", "얻", "정리", "치우", "필요", "좋다", "한다", "다"),
            modelAnswerKo = "(ㄱ) 생긴다 / 얻을 수 있다\n(ㄴ) 정리하는 것이 좋다 / 정리해야 한다",
            explanationEn = "(ㄱ) Tidying brings peace of mind ('마음의 여유도 생긴다'); (ㄴ) tidy your desk before starting work ('정리하는 것이 좋다').",
            explanationId = "(ㄱ) Merapikan meja juga menumbuhkan ketenangan pikiran ('생긴다'); (ㄴ) sebaiknya merapikan meja sebelum mulai bekerja ('정리하는 것이 좋다')."
        )
    )

    val writingTask53Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "t2_w53_v1", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "53번 · 자료 분석 작문 (200~300자)",
            instructionKo = "[53번] 다음 자료를 참고하여 '한국인의 전자책(e-book) 이용 변화'에 대한 글을 200~300자로 쓰십시오. (30점)",
            passageBoxKo = "<조사 기관: 한국독서문화연구소 / 대상: 성인 1,000명>\n\n[1] 전자책 이용률 변화\n• 2020년: 32%  →  2022년: 49%  →  2025년: 68%\n\n[2] 이용 증가 원인\n• 스마트폰·태블릿 보급 확대 (52%)\n• 시간·장소 제약 없는 휴대 편의성 (34%)\n• 구독 서비스 확산으로 비용 절감 (14%)\n\n[3] 향후 전망\n• 오디오북 결합 등 다양한 디지털 독서 형태 지속 확대 예상",
            stimulusKo = "자료의 수치 변화, 주요 원인, 향후 전망을 모두 포함하여 문어체(-는다/ㄴ다)로 200~300자 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 53, dualBlanks = false,
            targetMinChars = 200, targetMaxChars = 300, pointWeight = 30, difficultyTier = 2,
            expectedKeywords = listOf("전자책", "증가", "원인", "스마트폰", "편의", "전망", "나타났다", "다"),
            modelAnswerKo = "한국독서문화연구소가 성인 1,000명을 대상으로 조사한 결과, 한국인의 전자책 이용률은 2020년 32%에서 2022년 49%, 2025년 68%로 5년 새 두 배 이상 크게 증가한 것으로 나타났다. 이러한 증가의 가장 큰 원인은 스마트폰과 태블릿 보급 확대(52%)였으며, 시간과 장소의 제약이 없는 휴대 편의성(34%)과 구독 서비스를 통한 비용 절감(14%)이 그 뒤를 이었다. 앞으로도 오디오북과 결합된 다양한 디지털 독서 형태가 지속적으로 확대될 것으로 전망된다.",
            explanationEn = "TOPIK II Task 53 (200–300 chars): report the survey source, trend figures (32% → 49% → 68%), ranked causes, and outlook strictly in written plain style (-는다/ㄴ다).",
            explanationId = "Soal 53 TOPIK II (200–300 huruf): laporkan lembaga survei, tren angka (32% → 49% → 68%), urutan penyebab, dan proyeksi menggunakan gaya tulis formal (-는다/ㄴ다)."
        ),
        ExamQuestion(
            id = "t2_w53_v2", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "53번 · 자료 분석 작문 (200~300자)",
            instructionKo = "[53번] 다음 자료를 참고하여 '직장인의 재택근무 인식 조사'에 대한 글을 200~300자로 쓰십시오. (30점)",
            passageBoxKo = "<조사 기관: 대한고용연구원 / 대상: 직장인 1,200명>\n\n[1] 재택근무 장점\n• 출퇴근 시간 및 비용 절약 (61%)\n• 업무 자율성과 집중도 향상 (27%)\n• 가족과 보내는 시간 증가 (12%)\n\n[2] 재택근무 단점\n• 동료와의 소통 및 협업 부족 (54%)\n• 업무와 사생활 경계 모호 (31%)\n\n[3] 개선 과제\n• 정기적 대면 회의 병행 및 명확한 근무 시간 규정 마련",
            stimulusKo = "조사 결과의 장점, 단점, 개선 과제를 모두 반영하여 문어체(-는다/ㄴ다)로 200~300자 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 53, dualBlanks = false,
            targetMinChars = 200, targetMaxChars = 300, pointWeight = 30, difficultyTier = 2,
            expectedKeywords = listOf("재택근무", "장점", "단점", "출퇴근", "소통", "개선", "필요하다", "다"),
            modelAnswerKo = "대한고용연구원이 직장인 1,200명을 대상으로 재택근무 인식을 조사한 결과, 장점으로는 출퇴근 시간과 비용 절약이 61%로 가장 높았고 업무 자율성 향상(27%)과 가족 시간 증가(12%)가 뒤를 이었다. 반면 단점으로는 동료와의 소통 부족(54%)과 업무 및 사생활 경계 모호(31%)가 주로 지적되었다. 따라서 재택근무의 효과를 높이기 위해서는 정기적인 대면 회의를 병행하고 명확한 근무 시간 규정을 마련할 필요가 있다.",
            explanationEn = "Task 53 balanced summary: contrast top advantages vs disadvantages using '반면' and conclude with concrete recommendations ('따라서 ~할 필요가 있다').",
            explanationId = "Ringkasan berimbang Soal 53: bandingkan kelebihan dan kekurangan utama dengan '반면', lalu tutup dengan solusi konkret ('따라서 ~할 필요가 있다')."
        ),
        ExamQuestion(
            id = "t2_w53_v3", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "53번 · 자료 분석 작문 (200~300자)",
            instructionKo = "[53번] 다음 자료를 참고하여 '1인 가구 증가와 소비 변화'에 대한 글을 200~300자로 쓰십시오. (30점)",
            passageBoxKo = "<조사 기관: 통계정보원>\n\n[1] 전체 가구 중 1인 가구 비중\n• 2015년: 27%  →  2020년: 32%  →  2025년: 36%\n\n[2] 주요 증가 배경\n• 만혼 및 비혼 추세 확대\n• 고령화로 인한 독거노인 증가\n\n[3] 소비 시장 변화\n• 소포장 식품·간편식 및 소형 가전 수요 급증\n• 1인 식당·공유 주거 서비스 확대",
            stimulusKo = "1인 가구 비중 변화, 증가 배경, 소비 시장 영향을 문어체(-는다/ㄴ다)로 200~300자 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 53, dualBlanks = false,
            targetMinChars = 200, targetMaxChars = 300, pointWeight = 30, difficultyTier = 2,
            expectedKeywords = listOf("1인 가구", "증가", "배경", "소비", "수요", "확대", "나타났다", "다"),
            modelAnswerKo = "통계정보원의 조사에 따르면 전체 가구 중 1인 가구 비중은 2015년 27%에서 2020년 32%, 2025년 36%로 꾸준히 증가하였다. 이러한 변화는 만혼과 비혼 추세의 확대 및 고령화로 인한 독거노인 증가가 주요 배경으로 작용하였다. 이에 따라 소비 시장에서도 소포장 식품과 간편식, 소형 가전의 수요가 급증하고 있으며, 1인 식당과 공유 주거 서비스 역시 빠르게 확대되고 있다.",
            explanationEn = "Connect demographic statistics (27% → 32% → 36%) to social causes (배경) and consumer market shifts (이에 따라).",
            explanationId = "Hubungkan tren statistik (27% → 32% → 36%) dengan latar belakang sosial dan dampaknya terhadap pasar konsumsi."
        )
    )

    val writingTask54Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "t2_w54_v1", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "54번 · 논술 작문 (600~700자)",
            instructionKo = "[54번] 다음을 주제로 하여 자신의 생각을 600~700자로 글을 쓰십시오. 단, 문제를 그대로 옮겨 쓰지 마십시오. (50점)",
            passageBoxKo = "현대 사회에서는 인공지능(AI)과 자동화 기술이 빠르게 발전하면서 일상생활과 직업 세계에 큰 변화를 가져오고 있다. 기술 발전이 인간에게 많은 편리함을 주는 한편, 일자리 감소나 개인정보 침해와 같은 우려도 함께 제기되고 있다. 아래의 내용을 중심으로 '인공지능 시대에 필요한 인간의 역량과 대비'에 대한 자신의 생각을 쓰십시오.\n\n• 인공지능 기술 발전이 우리 사회에 가져온 긍정적 변화는 무엇인가?\n• 기술 확산에 따라 발생할 수 있는 문제점은 무엇인가?\n• 인공지능 시대에 개인과 사회는 어떤 준비와 노력을 해야 하는가?",
            stimulusKo = "세 가지 핵심 질문을 모두 포함하여 서론·본론·결론 구조의 격식 문어체(-는다/ㄴ다)로 600~700자 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 54, dualBlanks = false,
            targetMinChars = 550, targetMaxChars = 700, pointWeight = 50, difficultyTier = 3,
            expectedKeywords = listOf("인공지능", "기술", "변화", "문제", "일자리", "역량", "교육", "노력", "따라서", "그러나"),
            modelAnswerKo = "최근 인공지능 기술의 눈부신 발전은 산업 현장뿐만 아니라 우리의 일상생활 전반을 빠르게 변화시키고 있다. 인공지능은 방대한 자료를 단시간에 분석하고 반복적인 업무를 대신 처리해 줌으로써 생산성과 생활의 편리함을 크게 높여 준다. 특히 의료 진단이나 재난 예측 분야에서는 인간의 한계를 보완하여 생명을 보호하는 데에도 크게 기여하고 있다.\n\n그러나 이러한 기술 확산에는 적지 않은 문제점도 뒤따른다. 단순 사무직이나 제조 분야의 일자리가 급격히 줄어들어 고용 불안이 심화될 수 있으며, 대규모 데이터 수집 과정에서 개인정보가 유출되거나 편향된 정보가 확산될 위험도 크다. 또한 기술 활용 능력에 따라 세대와 계층 간 격차가 더욱 벌어질 우려도 제기된다.\n\n따라서 인공지능 시대를 현명하게 대비하기 위해서는 개인과 사회 모두의 노력이 필요하다. 개인은 기계가 대체하기 어려운 창의적 사고력과 공감 능력, 윤리적 판단력을 꾸준히 길러야 한다. 아울러 사회와 정부는 일자리 전환을 돕는 평생 교육 체계를 구축하고, 기술의 안전한 활용을 보장하는 법적·윤리적 기준을 명확히 마련해야 한다. 기술이 인간을 대체하는 도구가 아니라 함께 발전하는 동반자가 되도록 균형 잡힌 대응이 요구된다.",
            explanationEn = "TOPIK II Task 54 (600–700 chars, 50 pts): cover all 3 bullet points across Intro/Body/Conclusion using formal written Korean (-는다/ㄴ다), strong connectors (그러나, 따라서, 아울러), and balanced argumentation.",
            explanationId = "Soal 54 TOPIK II (600–700 huruf, 50 poin): jawab ketiga poin panduan dalam struktur Pendahuluan–Isi–Kesimpulan dengan gaya tulis formal (-는다/ㄴ다) dan konektor argumentatif (그러나, 따라서, 아울러)."
        ),
        ExamQuestion(
            id = "t2_w54_v2", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "54번 · 논술 작문 (600~700자)",
            instructionKo = "[54번] 다음을 주제로 하여 자신의 생각을 600~700자로 글을 쓰십시오. 단, 문제를 그대로 옮겨 쓰지 마십시오. (50점)",
            passageBoxKo = "오늘날 많은 사람들이 다양한 외국어를 배우기 위해 시간과 노력을 투자하고 있다. 번역기와 같은 기술이 발전했음에도 불구하고 여전히 외국어를 직접 익히는 가치가 강조되고 있다. 아래의 내용을 중심으로 '외국어 학습의 가치와 효과적인 학습 태도'에 대한 자신의 생각을 쓰십시오.\n\n• 번역 기술이 발전한 시대에도 외국어를 직접 배우는 것이 왜 중요한가?\n• 외국어를 배울 때 겪는 어려움은 무엇이며 그 원인은 무엇인가?\n• 외국어 실력을 꾸준히 향상시키기 위해 어떤 태도와 방법이 필요한가?",
            stimulusKo = "세 가지 핵심 질문을 모두 포함하여 서론·본론·결론 구조의 격식 문어체(-는다/ㄴ다)로 600~700자 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 54, dualBlanks = false,
            targetMinChars = 550, targetMaxChars = 700, pointWeight = 50, difficultyTier = 3,
            expectedKeywords = listOf("외국어", "번역", "문화", "소통", "어려움", "꾸준", "노력", "중요", "그러나", "따라서"),
            modelAnswerKo = "기계 번역 기술이 하루가 다르게 정교해지고 있지만, 외국어를 직접 배우는 가치는 결코 줄어들지 않는다. 언어는 단순한 정보 전달 수단이 아니라 그 사회의 역사와 문화, 사고방식이 고스란히 담긴 그릇이기 때문이다. 상대방의 언어로 직접 소통할 때 비로소 미묘한 감정과 맥락을 이해하고 진정한 신뢰 관계를 형성할 수 있으며, 새로운 학문과 직업 기회도 넓어진다.\n\n물론 외국어를 익히는 과정에는 여러 어려움이 따른다. 어휘와 문법 체계가 모국어와 달라 초기에 많은 시간이 필요하며, 단기간에 성과가 눈에 띄지 않아 쉽게 지치고 포기하는 경우가 많다. 또한 실수할까 두려워 실제 대화에서 말하기를 주저하는 태도 역시 실력 향상을 가로막는 큰 원인이 된다.\n\n따라서 외국어 실력을 꾸준히 기르기 위해서는 장기적인 안목과 능동적인 학습 태도가 필요하다. 완벽한 문장만을 고집하기보다 매일 짧은 시간이라도 듣고 말하며 언어에 자연스럽게 노출되는 습관을 들여야 한다. 아울러 그 나라의 책이나 영상, 문화를 함께 즐기며 흥미를 유지한다면 학습의 부담을 줄일 수 있다. 이처럼 꾸준함과 열린 태도로 외국어를 익힌다면 더 넓은 세상과 소통하는 소중한 기회를 얻게 될 것이다.",
            explanationEn = "Address why direct language learning remains vital despite translation tools, common obstacles, and sustainable study habits in 600–700 written-style Korean characters.",
            explanationId = "Bahas pentingnya belajar bahasa asing di era penerjemah otomatis, kendala umum, dan strategi belajar konsisten dalam 600–700 huruf gaya tulis formal."
        ),
        ExamQuestion(
            id = "t2_w54_v3", examType = ExamType.TOPIK_II, section = "Writing", period = 1,
            questionTypeKo = "54번 · 논술 작문 (600~700자)",
            instructionKo = "[54번] 다음을 주제로 하여 자신의 생각을 600~700자로 글을 쓰십시오. 단, 문제를 그대로 옮겨 쓰지 마십시오. (50점)",
            passageBoxKo = "현대인은 치열한 경쟁 속에서 빠른 성과를 추구하며 바쁘게 살아가고 있다. 그러나 충분한 휴식 없이 달려가기만 하면 몸과 마음이 쉽게 지치게 된다. 아래의 내용을 중심으로 '현대 사회에서 진정한 휴식의 의미와 필요성'에 대한 자신의 생각을 쓰십시오.\n\n• 현대인에게 충분한 휴식이 왜 중요한가?\n• 휴식을 제대로 취하지 못할 때 나타나는 문제는 무엇인가?\n• 일상에서 건강한 휴식을 실천하기 위해 어떤 노력이 필요한가?",
            stimulusKo = "세 가지 핵심 질문을 모두 포함하여 서론·본론·결론 구조의 격식 문어체(-는다/ㄴ다)로 600~700자 작성하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 54, dualBlanks = false,
            targetMinChars = 550, targetMaxChars = 700, pointWeight = 50, difficultyTier = 3,
            expectedKeywords = listOf("휴식", "현대", "건강", "스트레스", "문제", "일상", "노력", "중요", "그러나", "따라서"),
            modelAnswerKo = "빠른 속도와 높은 성과를 중시하는 현대 사회에서 휴식은 흔히 시간 낭비나 게으름으로 오해받곤 한다. 그러나 진정한 휴식은 멈춤이 아니라 다음 도약을 위해 에너지를 재충전하고 삶의 균형을 회복하는 필수적인 과정이다. 적절한 쉼을 통해 몸의 피로를 회복할 때 비로소 창의적인 생각과 높은 집중력이 발휘될 수 있다.\n\n만약 충분한 휴식을 취하지 못한 채 과도한 업무와 학업을 이어 가면 여러 심각한 문제가 발생한다. 만성 피로와 수면 장애로 신체 건강이 악화될 뿐만 아니라, 스트레스와 무기력감이 쌓여 정신 건강까지 크게 위협받게 된다. 결국 개인의 삶의 질과 인간관계가 흔들리고 사회 전체의 생산성마저 저하되는 악순환으로 이어진다.\n\n따라서 건강한 휴식을 일상 속에 정착시키기 위한 개인과 사회의 노력이 요구된다. 개인은 퇴근 후나 주말에 스마트폰과 업무에서 잠시 벗어나 산책이나 독서처럼 마음을 안정시키는 자신만의 휴식 방법을 찾아 실천해야 한다. 동시에 사회와 기업에서도 정당한 휴가 사용을 존중하고 과로를 당연시하지 않는 제도와 문화가 확산되어야 한다. 쉼의 가치를 올바르게 인정할 때 비로소 지속 가능한 성장이 가능해질 것이다.",
            explanationEn = "Structure clearly around why true rest matters, harms of chronic burnout, and individual + societal habits in 600–700 chars of formal written Korean.",
            explanationId = "Susun argumen mengenai makna istirahat, dampak kelelahan kronis, serta upaya individu dan masyarakat dalam 600–700 huruf gaya tulis formal."
        )
    )

    // ==================== 2교시 읽기 (30문항: All Authentic TOPIK II Reading Types) ====================

    val readingQuestions: List<ExamQuestion> = listOf(
        // [1~2] 문법 빈칸
        reading(
            id = "t2_r01", typeKo = "문법 빈칸",
            instructionKo = "( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "독서는 지식을 넓혀 ( ____ ) 다른 사람의 감정을 이해하는 공감 능력도 키워 준다.",
            options = listOf("줄 뿐만 아니라", "주는 반면에", "주기 무섭게", "주기는커녕"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "-(으)ㄹ 뿐만 아니라 ('not only ... but also') links two parallel positive benefits of reading.",
            explanationId = "-(으)ㄹ 뿐만 아니라 ('tidak hanya ... tetapi juga') menghubungkan dua manfaat positif membaca."
        ),
        reading(
            id = "t2_r02", typeKo = "문법 빈칸",
            instructionKo = "( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "아무리 바쁘고 힘들더라도 약속은 반드시 지켜야 ( ____ ).",
            options = listOf("할 따름이다", "하기 마련이다", "하는 법이다", "할 뿐이다"),
            correctIdx = 2, weight = 2, tier = 1,
            explanationEn = "-(으)ㄴ/는 법이다 expresses a natural principle or expected norm ('promises are meant to be kept').",
            explanationId = "-(으)ㄴ/는 법이다 menyatakan prinsip wajar atau aturan umum yang semestinya berlaku."
        ),
        // [3~4] 밑줄 친 부분과 의미가 비슷한 것 (REAL underlinedText!)
        reading(
            id = "t2_r03", typeKo = "밑줄 친 표현 유의어",
            instructionKo = "밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
            stimulusKo = "신입 사원이라도 좋은 의견이 있으면 주저하지 말고 말해야 한다.",
            underlinedText = "주저하지 말고",
            options = listOf("망설이지 말고", "서두르지 말고", "포기하지 말고", "기대하지 말고"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "'주저하다' ('to hesitate') is a direct synonym of '망설이다'.",
            explanationId = "'주저하다' ('ragu-ragu') adalah sinonim langsung dari '망설이다'."
        ),
        reading(
            id = "t2_r04", typeKo = "밑줄 친 표현 유의어",
            instructionKo = "밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
            stimulusKo = "경기가 끝날 무렵에야 비로소 승패가 결정되었다.",
            underlinedText = "끝날 무렵에야",
            options = listOf("끝날 즈음에야", "끝나자마자", "끝나기 전에", "끝난 까닭에"),
            correctIdx = 0, weight = 2, tier = 2,
            explanationEn = "-(으)ㄹ 무렵 ('around the time when') is synonymous with -(으)ㄹ 즈음.",
            explanationId = "-(으)ㄹ 무렵 ('menjelang / sekitar waktu') bersinonim dengan -(으)ㄹ 즈음."
        ),
        // [5~7] 광고 / 공익 캠페인 문구 주제
        reading(
            id = "t2_r05", typeKo = "광고·표어 주제 파악",
            instructionKo = "다음은 무엇에 대한 글인지 고르십시오.",
            passageBoxKo = "잠깐의 정지선 지키기,\n보행자의 평생을 지킵니다.\n초록불이 깜빡일 때는 다음 신호를 기다려 주세요.",
            stimulusKo = "이 공익 광고의 주제로 가장 알맞은 것을 고르십시오.",
            options = listOf("교통 안전", "에너지 절약", "식생활 개선", "층간 소음 예방"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "Respecting the stop line (정지선), pedestrians (보행자), and green lights (초록불) is a traffic safety campaign (교통 안전).",
            explanationId = "Mematuhi garis henti (정지선), pejalan kaki (보행자), dan lampu hijau adalah kampanye keselamatan lalu lintas (교통 안전)."
        ),
        reading(
            id = "t2_r06", typeKo = "광고·표어 주제 파악",
            instructionKo = "다음은 무엇에 대한 글인지 고르십시오.",
            passageBoxKo = "안 쓰는 플러그 하나 뽑는 작은 습관,\n지구의 내일을 시원하게 바꿉니다.\n오늘 밤, 대기전력부터 꺼 보세요.",
            stimulusKo = "이 광고가 권장하는 실천으로 알맞은 것을 고르십시오.",
            options = listOf("전기 절약", "분리배출", "헌혈 참여", "독서 습관"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "Unplugging unused cords and cutting standby power (대기전력) promotes saving electricity (전기 절약).",
            explanationId = "Mencabut steker tak terpakai dan mematikan daya siaga (대기전력) mengajak hemat listrik (전기 절약)."
        ),
        reading(
            id = "t2_r07", typeKo = "안내문 내용 일치",
            instructionKo = "다음 안내문을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[ 제12회 서울 청년 창업 아이디어 공모전 ]\n• 접수 기간: 9월 1일(월) ~ 9월 30일(화) 18:00 마감\n• 참가 자격: 만 19세~34세 청년 (개인 또는 4인 이하 팀)\n• 심사 절차: 1차 서류 심사 → 2차 발표 심사\n• 시상 혜택: 대상 1팀 1,000만 원 및 창업 공간 1년 무료 지원\n※ 타 대회 수상작은 응모 불가하며, 접수된 서류는 반환하지 않음.",
            stimulusKo = "안내문의 내용과 일치하는 것을 고르십시오.",
            options = listOf("다섯 명이 한 팀을 이루어 참가할 수 있다.", "다른 대회에서 상을 받은 작품도 제출할 수 있다.", "서류 심사를 통과하면 발표 심사를 거쳐야 한다.", "접수된 서류는 심사가 끝난 뒤 모두 돌려준다."),
            correctIdx = 2, weight = 3, tier = 2,
            explanationEn = "The screening process is 1st document screening → 2nd presentation screening ('1차 서류 심사 → 2차 발표 심사').",
            explanationId = "Proses seleksi terdiri atas seleksi berkas tahap 1 lalu seleksi presentasi tahap 2."
        ),
        reading(
            id = "t2_r08", typeKo = "도표·통계 이해",
            instructionKo = "다음 조사 결과를 보고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[ 2025년 직장인 여가 활동 선호도 조사 (복수 응답) ]\n1. 영상·OTT 시청: 64%\n2. 국내외 여행: 48%\n3. 운동 및 산책: 42%\n4. 독서 및 자기계발: 25%\n※ 20·30대는 '여행' 비율이 가장 높았고, 40·50대는 '운동 및 산책' 비율이 가장 높았음.",
            stimulusKo = "조사 결과와 일치하는 것을 고르십시오.",
            options = listOf("전체 응답자 중 독서 비율이 가장 높다.", "20·30대는 운동보다 여행을 더 선호한다.", "40·50대는 여행을 가장 많이 선택했다.", "영상 시청은 전체 항목 중 가장 낮다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "The footnote notes respondents in their 20s–30s had the highest share for travel ('여행 비율이 가장 높았고').",
            explanationId = "Catatan kaki menyebutkan usia 20–30-an memiliki persentase tertinggi pada wisata dibanding olahraga."
        ),
        // [9~11] 문장 순서 배열 (가)(나)(다)(라)
        reading(
            id = "t2_r09", typeKo = "문장 순서 배열",
            instructionKo = "다음을 순서대로 맞게 배열한 것을 고르십시오.",
            passageBoxKo = "(가) 그 결과 소비자들이 실제 맛을 보기 전에도 포장 색깔만으로 단맛이나 신선도를 예측하게 된다.\n(나) 식품 기업들은 제품의 특성을 효과적으로 전달하기 위해 포장 색채를 전략적으로 선택한다.\n(다) 이처럼 색채 심리는 현대 마케팅에서 구매 결정을 좌우하는 핵심 요소로 자리 잡았다.\n(라) 예를 들어 건강한 이미지를 강조하고 싶을 때는 초록색을, 식욕을 돋우고 싶을 때는 빨간색이나 주황색을 주로 활용한다.",
            stimulusKo = "글의 흐름에 맞게 순서대로 배열한 것을 고르십시오.",
            options = listOf("(나)-(라)-(가)-(다)", "(나)-(가)-(라)-(다)", "(라)-(나)-(가)-(다)", "(가)-(나)-(라)-(다)"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "General premise (나) → concrete examples ('예를 들어', 라) → consequence ('그 결과', 가) → concluding summary ('이처럼', 다).",
            explanationId = "Pernyataan umum (나) → contoh konkret ('예를 들어', 라) → akibat ('그 결과', 가) → kesimpulan ('이처럼', 다)."
        ),
        reading(
            id = "t2_r10", typeKo = "문장 순서 배열",
            instructionKo = "다음을 순서대로 맞게 배열한 것을 고르십시오.",
            passageBoxKo = "(가) 하지만 이러한 우려와 달리 실제로는 도서 대출량과 지역 문화 행사 참여율이 크게 증가하였다.\n(나) 최근 여러 지자체에서 도서관을 복합 문화 공간으로 탈바꿈시키는 사업을 추진하고 있다.\n(다) 처음에는 조용해야 할 도서관이 소란스러워질 것이라는 반대 의견도 적지 않았다.\n(라) 책만 읽는 공간에서 벗어나 전시와 강연, 휴식을 함께 즐길 수 있게 되자 주민들의 발길이 늘어난 것이다.",
            stimulusKo = "글의 흐름에 맞게 순서대로 배열한 것을 고르십시오.",
            options = listOf("(나)-(다)-(가)-(라)", "(나)-(가)-(다)-(라)", "(다)-(나)-(가)-(라)", "(나)-(라)-(다)-(가)"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "Policy introduction (나) → initial opposition/concerns (다) → contrary outcome ('이러한 우려와 달리', 가) → reason explaining the outcome (라).",
            explanationId = "Pengenalan program (나) → kekhawatiran awal (다) → hasil berlawanan ('이러한 우려와 달리', 가) → penjelasan penyebabnya (라)."
        ),
        reading(
            id = "t2_r11", typeKo = "문장 순서 배열",
            instructionKo = "다음을 순서대로 맞게 배열한 것을 고르십시오.",
            passageBoxKo = "(가) 그러나 오랜 시간 공들여 쌓아 올린 신뢰는 단 한 번의 거짓말로도 순식간에 무너질 수 있다.\n(나) 인간관계에서 신뢰는 하루아침에 형성되지 않는다.\n(다) 그러므로 사소한 약속이라도 성실히 지키려는 태도가 무엇보다 중요하다.\n(라) 크고 작은 약속을 꾸준히 지키며 진심을 보여 줄 때 비로소 믿음이 단단해진다.",
            stimulusKo = "글의 흐름에 맞게 순서대로 배열한 것을 고르십시오.",
            options = listOf("(나)-(라)-(가)-(다)", "(나)-(가)-(라)-(다)", "(라)-(나)-(가)-(다)", "(가)-(나)-(라)-(다)"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "Trust isn't built overnight (나) → it grows by keeping promises (라) → yet one lie destroys it instantly (가) → therefore keeping small promises matters most (다).",
            explanationId = "Kepercayaan tidak terbentuk semalam (나) → tumbuh lewat menepati janji (라) → namun runtuh seketika oleh satu kebohongan (가) → maka menjaga janji kecil sangat penting (다)."
        ),
        // [12~14] 빈칸 추론 (문맥 & 연결어)
        reading(
            id = "t2_r12", typeKo = "연결 표현 빈칸",
            instructionKo = "( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "기후 변화로 인한 이상 기온 현상이 해마다 심화되고 있다. ( ____ ) 각국 정부는 탄소 배출 감축과 재생에너지 전환을 서두르고 있다.",
            options = listOf("이에 따라", "그럼에도 불구하고", "오히려", "반면에"),
            correctIdx = 0, weight = 2, tier = 1,
            explanationEn = "'이에 따라' ('Accordingly / Consequently') introduces the policy response triggered by worsening climate anomalies.",
            explanationId = "'이에 따라' ('Sejalan dengan itu / Oleh karenanya') mengawali respons kebijakan atas krisis iklim."
        ),
        reading(
            id = "t2_r13", typeKo = "내용 빈칸 추론",
            instructionKo = "다음 글을 읽고 ( )에 들어갈 내용으로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "박물관 유물 전시는 단순히 귀중한 물건을 유리관 안에 진열하는 것만으로는 관람객의 깊은 공감을 얻기 어렵다. 유물이 만들어진 시대의 생활상과 그 안에 담긴 이야기를 함께 보여 줄 때 관람객은 비로소 ( ____ ).",
            stimulusKo = "( )에 들어갈 말로 가장 알맞은 것을 고르십시오.",
            options = listOf("유물의 진정한 가치를 생생하게 느낄 수 있다", "유물 보존 비용을 크게 절감할 수 있다", "전시실 이동 시간을 단축할 수 있다", "유리관의 두께를 정확히 측정할 수 있다"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "Presenting historical context and stories lets visitors vividly feel the artifacts' true value ('유물의 진정한 가치를 생생하게 느낄 수 있다').",
            explanationId = "Menyajikan latar zaman dan cerita artefak membuat pengunjung merasakan nilai sejati artefak secara hidup."
        ),
        reading(
            id = "t2_r14", typeKo = "내용 빈칸 추론",
            instructionKo = "다음 글을 읽고 ( )에 들어갈 내용으로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "운동선수들이 큰 대회를 앞두고 이미지 트레이닝을 하는 이유는 뇌가 실제 행동과 생생한 상상을 거의 비슷하게 받아들이기 때문이다. 경기 장면을 머릿속으로 구체적으로 그려 보는 훈련을 반복하면 ( ____ ) 실전에서도 흔들리지 않고 기량을 발휘하는 데 도움이 된다.",
            stimulusKo = "( )에 들어갈 말로 가장 알맞은 것을 고르십시오.",
            options = listOf("심리적 긴장을 완화하고 동작의 정확도를 높여", "체력 훈련을 완전히 생략할 수 있어", "경기 규칙을 임의로 변경할 수 있어", "관중의 응원 소리를 차단하여"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "Mental rehearsal lowers psychological tension and sharpens movement accuracy ('심리적 긴장을 완화하고 동작의 정확도를 높여').",
            explanationId = "Latihan imajinasi mental meredakan ketegangan psikologis dan meningkatkan akurasi gerakan saat bertanding."
        ),
        // [15~17] 신문 기사 제목 해석 (Iconic TOPIK II Reading Q25~27!)
        reading(
            id = "t2_r15", typeKo = "신문 기사 제목 이해",
            instructionKo = "다음 신문 기사의 제목을 가장 잘 설명한 것을 고르십시오.",
            passageBoxKo = "[ 신문 기사 제목 ]\n「물가 상승에 '알뜰 소비' 확산… 중고 거래 시장 역대 최대」",
            stimulusKo = "기사 제목의 내용과 가장 일치하는 설명을 고르십시오.",
            options = listOf("물가가 오르면서 절약하려는 소비가 늘어 중고 거래 시장이 사상 최대 규모로 커졌다.", "물가가 하락하면서 새 제품 구매가 크게 늘고 중고 시장이 위축되었다.", "중고 거래가 금지되면서 물가가 급격히 상승하였다.", "소비자들이 고가 명품 구매를 늘려 유통 시장이 호황을 맞았다."),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "'물가 상승에' (due to rising prices), '알뜰 소비 확산' (thrifty spending spreads), '역대 최대' (largest ever in history).",
            explanationId = "Kenaikan harga memicu pola belanja hemat sehingga pasar barang bekas mencapai rekor terbesar."
        ),
        reading(
            id = "t2_r16", typeKo = "신문 기사 제목 이해",
            instructionKo = "다음 신문 기사의 제목을 가장 잘 설명한 것을 고르십시오.",
            passageBoxKo = "[ 신문 기사 제목 ]\n「도심 녹지 늘렸더니… 한여름 체감 온도 3도 '뚝'」",
            stimulusKo = "기사 제목의 내용과 가장 일치하는 설명을 고르십시오.",
            options = listOf("도심에 녹지 공간을 확충한 결과 한여름 체감 온도가 3도나 크게 내려갔다.", "한여름 기온이 3도 상승하여 도심 녹지가 대부분 사라졌다.", "도심 녹지를 줄였더니 여름철 냉방 효율이 높아졌다.", "기온이 3도 아래로 떨어져 도심 공원 공사가 중단되었다."),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "The mimetic word '뚝' in Korean headlines signals a sharp drop — expanding urban greenery lowered perceived summer heat by 3°C.",
            explanationId = "Kata '뚝' pada judul berita menandakan penurunan tajam — menambah ruang hijau kota menurunkan suhu terasa musim panas 3 derajat."
        ),
        reading(
            id = "t2_r17", typeKo = "신문 기사 제목 이해",
            instructionKo = "다음 신문 기사의 제목을 가장 잘 설명한 것을 고르십시오.",
            passageBoxKo = "[ 신문 기사 제목 ]\n「외국인 관광객 발길 북적… 지방 소도시 체험 관광 '활짝'」",
            stimulusKo = "기사 제목의 내용과 가장 일치하는 설명을 고르십시오.",
            options = listOf("외국인 관광객이 많이 찾아오면서 지방 소도시의 체험 관광이 크게 활성화되고 있다.", "외국인 관광객이 줄어들어 지방 소도시 관광지가 문을 닫았다.", "관광객들이 대도시 쇼핑에만 머물러 지방 방문이 급감했다.", "체험 관광 요금이 올라 외국인 방문객의 불만이 커지고 있다."),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "'발길 북적' (bustling with visitors) + '활짝' (flourishing/booming) means regional experiential tourism is thriving.",
            explanationId = "'발길 북적' (ramai pengunjung) dan '활짝' (berkembang pesat) berarti wisata pengalaman di kota kecil daerah sedang bergairah."
        ),
        // [18~19] 중심 생각 / 주제 파악
        reading(
            id = "t2_r18", typeKo = "주제 파악",
            instructionKo = "다음 글의 주제로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "도시 내 작은 공원을 늘리면 여름철 열섬 현상을 완화할 수 있고, 주민들의 스트레스 해소와 정신 건강에도 긍정적인 영향을 미친다. 따라서 지속 가능한 도시 개발을 위해서는 녹지 공간 확보가 선택이 아니라 필수 과제이다.",
            stimulusKo = "이 글의 주제로 가장 알맞은 것을 고르십시오.",
            options = listOf("도심 녹지 공간 확보의 필요성", "대중교통 요금 인상의 원인", "고층 건물 설계의 최신 유행", "여름철 전력 소비량 비교"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "The concluding thesis ('녹지 공간 확보가 선택이 아니라 필수 과제이다') clearly points to the necessity of securing urban green space.",
            explanationId = "Kalimat penutup menegaskan pengadaan ruang terbuka hijau di kota adalah keharusan, bukan pilihan."
        ),
        reading(
            id = "t2_r19", typeKo = "필자의 태도·심정",
            instructionKo = "다음 글에 나타난 글쓴이의 태도로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "생성형 기술이 예술 창작의 문턱을 낮추어 누구나 손쉽게 그림과 음악을 만들 수 있게 된 점은 흥미롭다. 그러나 기존 창작자의 작품을 무단으로 학습해 저작권을 침해하고 예술가의 생존 기반을 흔드는 현실을 외면해서는 안 된다. 창작자의 권리를 보호하는 제도적 장치가 시급하다.",
            stimulusKo = "글쓴이의 태도로 가장 알맞은 것을 고르십시오.",
            options = listOf("창작자 저작권 보호 제도의 시급한 마련을 촉구하고 있다.", "기존 예술가의 저작권 보호는 불필요하다고 주장하고 있다.", "모든 디지털 예술 도구의 사용을 영구히 금지해야 한다고 본다.", "저작권 침해 문제는 시간이 지나면 저절로 해결된다고 낙관한다."),
            correctIdx = 0, weight = 4, tier = 3,
            explanationEn = "While acknowledging accessibility, the author urgently calls for institutional copyright protections ('제도적 장치가 시급하다').",
            explanationId = "Penulis mengakui kemudahan teknologi namun mendesak regulasi perlindungan hak cipta kreator sesegera mungkin."
        ),
        // [20~21] 문장 삽입 (ㄱ)(ㄴ)(ㄷ)(ㄹ)
        reading(
            id = "t2_r20", typeKo = "문장 삽입",
            instructionKo = "다음 글을 읽고 <보기>의 문장이 들어갈 곳으로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "<보기> 그러나 같은 물건이라도 자신이 직접 조립하거나 완성하는 과정에 참여하면 애착이 크게 높아진다.\n\n( ㄱ ) 소비자는 보통 완성된 제품을 편리하게 구매하는 것을 선호할 것이라고 생각하기 쉽다. ( ㄴ ) 실제로 조립이 필요한 가구나 요리 키트는 번거로워 보이기 때문이다. ( ㄷ ) 심리학에서는 이처럼 스스로 노력을 투입한 대상에 더 높은 가치를 부여하는 현상을 '이케아 효과'라고 부른다. ( ㄹ ) 기업들은 이러한 심리를 반영해 소비자가 직접 참여할 수 있는 체험형 상품을 늘리고 있다.",
            stimulusKo = "<보기>의 문장이 들어갈 위치로 가장 알맞은 곳은 어디입니까?",
            options = listOf("( ㄱ )", "( ㄴ )", "( ㄷ )", "( ㄹ )"),
            correctIdx = 2, weight = 4, tier = 3,
            explanationEn = "After explaining why ready-made goods seem preferred (ㄱ–ㄴ), <보기> flips with '그러나' at (ㄷ), and the following sentence defines '이처럼 스스로 노력을 투입한 대상' as the IKEA Effect.",
            explanationId = "Sesudah penjelasan mengapa produk jadi tampak lebih disukai (ㄱ–ㄴ), <보기> membalik argumen dengan '그러나' di (ㄷ), lalu langsung dirujuk sebagai '이케아 효과'."
        ),
        reading(
            id = "t2_r21", typeKo = "문장 삽입",
            instructionKo = "다음 글을 읽고 <보기>의 문장이 들어갈 곳으로 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "<보기> 문제는 이렇게 지나치게 잦은 알림이 작업의 흐름을 끊어 집중력을 크게 떨어뜨린다는 점이다.\n\n( ㄱ ) 스마트폰 알림 기능은 중요한 연락이나 일정을 놓치지 않게 해 주는 유용한 도구이다. ( ㄴ ) 덕분에 우리는 이동 중에도 실시간으로 정보를 받아 즉각 대응할 수 있다. ( ㄷ ) 한 연구에 따르면 알림으로 방해받은 뒤 원래의 집중 상태로 돌아오는 데 평균 이십 분 이상이 걸린다고 한다. ( ㄹ ) 따라서 깊이 몰입해야 할 시간에는 꼭 필요한 연락을 제외하고 알림을 꺼 두는 것이 바람직하다.",
            stimulusKo = "<보기>의 문장이 들어갈 위치로 가장 알맞은 곳은 어디입니까?",
            options = listOf("( ㄱ )", "( ㄴ )", "( ㄷ )", "( ㄹ )"),
            correctIdx = 2, weight = 4, tier = 3,
            explanationEn = "At (ㄷ), <보기> pivots from benefits of notifications (ㄱ–ㄴ) to the distraction problem, which is directly supported by the 20-minute refocus study right after (ㄷ).",
            explanationId = "Di (ㄷ), <보기> beralih dari manfaat notifikasi (ㄱ–ㄴ) ke masalah gangguan fokus yang langsung didukung hasil riset 20 menit setelahnya."
        ),
        // [22~30] 고급 묶음 독해 (4 sets + 1 final essay question = 9 items, total 30 Reading!)
        reading(
            id = "t2_r22", typeKo = "묶음 독해 · 빈칸 추론",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "도심의 오래된 공장이나 창고를 허물지 않고 미술관이나 카페, 공연장으로 재탄생시키는 '도시 재생'이 세계 여러 도시에서 활발히 이루어지고 있다. 낡은 건물을 철거하고 새 빌딩을 짓는 방식은 막대한 건축 폐기물을 발생시키고 지역의 역사적 기억마저 지워 버린다. ( ㄱ ) 기존 건축물의 골격을 살리면서 내부에 현대적 기능을 더하면 환경 부담을 줄일 수 있을 뿐만 아니라 그 지역만의 고유한 정체성과 매력을 지켜 낼 수 있다.",
            stimulusKo = "( ㄱ )에 들어갈 가장 알맞은 연결 표현을 고르십시오.",
            options = listOf("반면에", "왜냐하면", "게다가", "결국"),
            correctIdx = 0, weight = 3, tier = 2,
            explanationEn = "'반면에' ('On the other hand / By contrast') contrasts demolition/rebuilding with preserving existing structures.",
            explanationId = "'반면에' ('Sebaliknya') mengontraskan pembongkaran gedung lama dengan pelestarian kerangka bangunan.",
            groupId = "t2_rg_1", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t2_r23", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "도심의 오래된 공장이나 창고를 허물지 않고 미술관이나 카페, 공연장으로 재탄생시키는 '도시 재생'이 세계 여러 도시에서 활발히 이루어지고 있다. 낡은 건물을 철거하고 새 빌딩을 짓는 방식은 막대한 건축 폐기물을 발생시키고 지역의 역사적 기억마저 지워 버린다. ( ㄱ ) 기존 건축물의 골격을 살리면서 내부에 현대적 기능을 더하면 환경 부담을 줄일 수 있을 뿐만 아니라 그 지역만의 고유한 정체성과 매력을 지켜 낼 수 있다.",
            stimulusKo = "이 글의 내용과 일치하는 것을 고르십시오.",
            options = listOf("도시 재생은 오래된 건물을 모두 철거하는 방식이다.", "기존 건축물을 활용하면 환경 부담을 줄이고 지역 정체성을 지킬 수 있다.", "새 빌딩을 지으면 건축 폐기물이 전혀 발생하지 않는다.", "오래된 공장은 문화 공간으로 활용할 수 없다."),
            correctIdx = 1, weight = 3, tier = 2,
            explanationEn = "Adaptive reuse reduces environmental burden and preserves unique local identity.",
            explanationId = "Memanfaatkan kerangka bangunan lama mengurangi beban lingkungan sekaligus menjaga identitas khas daerah.",
            groupId = "t2_rg_1", groupLabelKo = "※ [묶음 독해] 다음 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t2_r24", typeKo = "묶음 독해 · 밑줄 의미",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "식물은 동물처럼 움직이지 못하기 때문에 해충의 공격을 받아도 도망칠 수 없다. 대신 식물은 놀라운 화학적 소통 능력을 발전시켜 왔다. 애벌레가 잎을 갉아먹기 시작하면 식물은 특유의 휘발성 물질을 공기 중으로 내보낸다. 이 냄새는 애벌레의 천적인 기생벌을 불러들일 뿐만 아니라 주변의 다른 식물에게도 위험 신호를 전달해 미리 방어 물질을 만들도록 돕는다. 겉보기에 고요한 숲속에서도 보이지 않는 정보 교환이 끊임없이 일어나고 있는 셈이다.",
            stimulusKo = "밑줄 친 '보이지 않는 정보 교환'이 가리키는 내용으로 가장 알맞은 것을 고르십시오.",
            underlinedText = "보이지 않는 정보 교환",
            options = listOf("식물이 휘발성 물질을 통해 천적을 부르고 주변에 위험을 알리는 것", "동물이 숲속에서 먹이를 찾아 이동하는 과정", "계절 변화에 따라 낙엽이 떨어지는 현상", "기생벌이 꽃가루를 옮겨 수분을 돕는 활동"),
            correctIdx = 0, weight = 4, tier = 3,
            explanationEn = "The 'invisible information exchange' refers to plants emitting volatile compounds to summon predators and warn neighboring plants.",
            explanationId = "'Pertukaran informasi tak kasatmata' merujuk pada tumbuhan yang melepas zat volatil untuk memanggil predator dan memperingatkan tumbuhan sekitar.",
            groupId = "t2_rg_2", groupLabelKo = "※ [묶음 독해] 다음 과학 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t2_r25", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "식물은 동물처럼 움직이지 못하기 때문에 해충의 공격을 받아도 도망칠 수 없다. 대신 식물은 놀라운 화학적 소통 능력을 발전시켜 왔다. 애벌레가 잎을 갉아먹기 시작하면 식물은 특유의 휘발성 물질을 공기 중으로 내보낸다. 이 냄새는 애벌레의 천적인 기생벌을 불러들일 뿐만 아니라 주변의 다른 식물에게도 위험 신호를 전달해 미리 방어 물질을 만들도록 돕는다. 겉보기에 고요한 숲속에서도 보이지 않는 정보 교환이 끊임없이 일어나고 있는 셈이다.",
            stimulusKo = "이 글의 내용과 일치하는 것을 고르십시오.",
            options = listOf("식물은 해충을 피하기 위해 뿌리를 옮겨 이동한다.", "위험 신호를 받은 주변 식물은 미리 방어 물질을 생성한다.", "휘발성 물질은 애벌레를 더 많이 끌어들이는 역할을 한다.", "식물 사이에서는 어떤 신호 전달도 일어나지 않는다."),
            correctIdx = 1, weight = 4, tier = 3,
            explanationEn = "Neighboring plants receiving the airborne signal proactively produce defense compounds ('미리 방어 물질을 만들도록 돕는다').",
            explanationId = "Tumbuhan di sekitarnya yang menerima sinyal bahaya akan menyiapkan zat pertahanan lebih dulu.",
            groupId = "t2_rg_2", groupLabelKo = "※ [묶음 독해] 다음 과학 글을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t2_r26", typeKo = "묶음 독해 · 중심 생각",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "경제학에서 '공유지의 비극'은 주인이 따로 없는 공공 자원을 구성원들이 각자의 이익만을 쫓아 무분별하게 사용하다가 결국 자원 전체가 고갈되는 현상을 뜻한다. 예를 들어 어부들이 당장의 수익을 늘리려고 물고기를 지나치게 많이 잡으면 몇 년 뒤에는 모두가 생계를 잃게 된다. 이러한 비극을 막기 위해서는 정부의 규제도 중요하지만, 무엇보다 지역 공동체 구성원들이 스스로 사용 규칙을 합의하고 상호 감시하는 자치적 협력이 효과적인 해법으로 평가받는다.",
            stimulusKo = "필자가 강조하는 '공유지의 비극' 해결 방안으로 가장 알맞은 것을 고르십시오.",
            options = listOf("공동체 구성원들의 자율적 규칙 합의와 상호 협력", "공공 자원을 전면 폐쇄하여 누구도 쓰지 못하게 하는 것", "개인의 단기 수익을 극대화하도록 경쟁을 부추기는 것", "규칙 없이 자연적으로 회복될 때까지 방치하는 것"),
            correctIdx = 0, weight = 4, tier = 3,
            explanationEn = "The author highlights self-governing cooperation where community members agree on rules and mutually monitor usage ('자치적 협력').",
            explanationId = "Penulis menekankan kerja sama otonom di mana anggota komunitas menyepakati aturan dan saling mengawasi.",
            groupId = "t2_rg_3", groupLabelKo = "※ [묶음 독해] 다음 사회 칼럼을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t2_r27", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "경제학에서 '공유지의 비극'은 주인이 따로 없는 공공 자원을 구성원들이 각자의 이익만을 쫓아 무분별하게 사용하다가 결국 자원 전체가 고갈되는 현상을 뜻한다. 예를 들어 어부들이 당장의 수익을 늘리려고 물고기를 지나치게 많이 잡으면 몇 년 뒤에는 모두가 생계를 잃게 된다. 이러한 비극을 막기 위해서는 정부의 규제도 중요하지만, 무엇보다 지역 공동체 구성원들이 스스로 사용 규칙을 합의하고 상호 감시하는 자치적 협력이 효과적인 해법으로 평가받는다.",
            stimulusKo = "이 글의 내용과 일치하는 것을 고르십시오.",
            options = listOf("공유지의 비극은 개인이 소유한 사유지에서만 발생한다.", "단기 이익을 위한 남획은 결국 공동체 전체의 피해로 이어진다.", "정부의 규제는 공유지 보호에 전혀 의미가 없다.", "어부들은 물고기를 많이 잡을수록 장기적으로 안정적인 수익을 얻는다."),
            correctIdx = 1, weight = 4, tier = 3,
            explanationEn = "Overfishing for short-term profit eventually destroys everyone's livelihood ('모두가 생계를 잃게 된다').",
            explanationId = "Penangkapan berlebihan demi keuntungan sesaat akhirnya merugikan mata pencaharian seluruh komunitas.",
            groupId = "t2_rg_3", groupLabelKo = "※ [묶음 독해] 다음 사회 칼럼을 읽고 두 물음에 답하십시오."
        ),
        reading(
            id = "t2_r28", typeKo = "묶음 독해 · 필자의 의도",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "알고리즘 기반의 맞춤형 추천 서비스는 사용자의 취향에 꼭 맞는 영상과 기사를 골라 주어 정보 탐색 시간을 획기적으로 줄여 준다. 그러나 자신이 보고 싶은 관점과 유사한 콘텐츠만 반복해서 접하다 보면 다른 의견에 대한 수용력이 떨어지고 확증 편향이 심화되기 쉽다. 이를 '필터 버블' 현상이라고 부른다. 디지털 시대의 성숙한 시민이라면 편리한 추천에만 안주하지 말고 의도적으로 다양한 시각의 자료를 찾아 읽는 균형 감각을 길러야 한다.",
            stimulusKo = "이 글을 쓴 목적으로 가장 알맞은 것을 고르십시오.",
            options = listOf("필터 버블 현상을 경계하고 다양한 시각을 접할 것을 권유하려고", "맞춤형 추천 알고리즘의 개발 기술을 상세히 교육하려고", "모든 온라인 뉴스 플랫폼의 폐쇄를 요구하려고", "정보 탐색 시간을 줄이는 최신 앱을 광고하려고"),
            correctIdx = 0, weight = 4, tier = 3,
            explanationEn = "The writer warns against the 'filter bubble' (확증 편향) and urges readers to intentionally seek diverse viewpoints.",
            explanationId = "Penulis memperingatkan bahaya 'filter bubble' dan mengajak pembaca secara sadar mencari sudut pandang beragam.",
            groupId = "t2_rg_4", groupLabelKo = "※ [묶음 독해] 다음 논설문을 읽고 세 물음에 답하십시오."
        ),
        reading(
            id = "t2_r29", typeKo = "묶음 독해 · 어휘 추론",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "알고리즘 기반의 맞춤형 추천 서비스는 사용자의 취향에 꼭 맞는 영상과 기사를 골라 주어 정보 탐색 시간을 획기적으로 줄여 준다. 그러나 자신이 보고 싶은 관점과 유사한 콘텐츠만 반복해서 접하다 보면 다른 의견에 대한 수용력이 떨어지고 확증 편향이 심화되기 쉽다. 이를 '필터 버블' 현상이라고 부른다. 디지털 시대의 성숙한 시민이라면 편리한 추천에만 안주하지 말고 의도적으로 다양한 시각의 자료를 찾아 읽는 균형 감각을 길러야 한다.",
            stimulusKo = "글의 문맥상 밑줄 친 '안주하지 말고'와 바꾸어 쓸 수 있는 가장 알맞은 표현을 고르십시오.",
            underlinedText = "안주하지 말고",
            options = listOf("만족하며 머무르지 말고", "반대하며 거부하지 말고", "의심하며 비판하지 말고", "서두르며 포기하지 말고"),
            correctIdx = 0, weight = 4, tier = 3,
            explanationEn = "'안주하다' means to settle comfortably / stay complacent without moving further ('만족하며 머무르다').",
            explanationId = "'안주하다' berarti berpuas diri dan menetap di zona nyaman tanpa melangkah lebih jauh."
            ,
            groupId = "t2_rg_4", groupLabelKo = "※ [묶음 독해] 다음 논설문을 읽고 세 물음에 답하십시오."
        ),
        reading(
            id = "t2_r30", typeKo = "묶음 독해 · 내용 일치",
            instructionKo = "다음을 읽고 물음에 답하십시오.",
            passageBoxKo = "알고리즘 기반의 맞춤형 추천 서비스는 사용자의 취향에 꼭 맞는 영상과 기사를 골라 주어 정보 탐색 시간을 획기적으로 줄여 준다. 그러나 자신이 보고 싶은 관점과 유사한 콘텐츠만 반복해서 접하다 보면 다른 의견에 대한 수용력이 떨어지고 확증 편향이 심화되기 쉽다. 이를 '필터 버블' 현상이라고 부른다. 디지털 시대의 성숙한 시민이라면 편리한 추천에만 안주하지 말고 의도적으로 다양한 시각의 자료를 찾아 읽는 균형 감각을 길러야 한다.",
            stimulusKo = "이 글의 내용과 일치하는 것을 고르십시오.",
            options = listOf("맞춤형 추천 서비스는 정보 탐색 시간을 줄이는 장점이 있다.", "필터 버블 현상은 다른 의견에 대한 수용력을 크게 높여 준다.", "추천 알고리즘을 많이 쓸수록 확증 편향이 사라진다.", "글쓴이는 맞춤형 추천 서비스에 장점이 전혀 없다고 단정한다."),
            correctIdx = 0, weight = 3, tier = 3,
            explanationEn = "The first sentence acknowledges that recommendation algorithms drastically cut information search time.",
            explanationId = "Kalimat pertama mengakui layanan rekomendasi algoritma secara drastis mempersingkat waktu pencarian informasi.",
            groupId = "t2_rg_4", groupLabelKo = "※ [묶음 독해] 다음 논설문을 읽고 세 물음에 답하십시오."
        )
    )

    val allQuestions: List<ExamQuestion> =
        listeningQuestions +
            listOf(
                writingTask51Variants.first(),
                writingTask52Variants.first(),
                writingTask53Variants.first(),
                writingTask54Variants.first()
            ) +
            readingQuestions
}
