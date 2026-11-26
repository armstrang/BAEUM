package com.example.data.seed

enum class ExamType(
    val title: String,
    val badgeKo: String,
    val subtitle: String,
    val sectionsLabel: String,
    val maxScore: Int,
    val mockDurationMinutes: Int,
    val officialReferenceNote: String
) {
    TOPIK_I(
        title = "TOPIK I",
        badgeKo = "1–2급",
        subtitle = "Listening • Reading",
        sectionsLabel = "Listening (100) + Reading (100)",
        maxScore = 200,
        mockDurationMinutes = 25,
        officialReferenceNote = "Practice simulation modeled on TOPIK I (NIIED) structure. Practice estimates are not an official TOPIK certificate."
    ),
    TOPIK_II(
        title = "TOPIK II",
        badgeKo = "3–6급",
        subtitle = "Listening • Reading • Writing",
        sectionsLabel = "Listening (100) + Reading (100) + Writing (100)",
        maxScore = 300,
        mockDurationMinutes = 35,
        officialReferenceNote = "Practice simulation modeled on TOPIK II (NIIED) structure. Writing prompt uses rubric self-check; not an official certificate."
    ),
    EPS_TOPIK(
        title = "EPS-TOPIK",
        badgeKo = "고용허가제",
        subtitle = "Workplace listening • Reading",
        sectionsLabel = "Workplace Listening (100) + Reading (100)",
        maxScore = 200,
        mockDurationMinutes = 25,
        officialReferenceNote = "Practice simulation modeled on HRD Korea EPS-TOPIK workplace format. Independent study material."
    ),
    TOPIK_SPEAKING(
        title = "TOPIK Speaking",
        badgeKo = "말하기",
        subtitle = "Speaking assessment practice",
        sectionsLabel = "Task Response • Structured Speech • Opinion",
        maxScore = 200,
        mockDurationMinutes = 15,
        officialReferenceNote = "Practice simulation for TOPIK Speaking tasks. Scores are local practice estimates, not official NIIED results."
    )
}

data class ExamQuestion(
    val id: String,
    val examType: ExamType,
    val section: String, // "Listening", "Reading", "Writing", "Speaking"
    val instructionKo: String,
    val stimulusKo: String,
    val audioTranscriptKo: String? = null, // Played via ko-KR TTS for Listening questions
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0, // -1 for open writing/speaking
    val isOpenEnded: Boolean = false,
    val expectedKeywords: List<String> = emptyList(),
    val explanationEn: String,
    val pointWeight: Int = 3, // Authentic TOPIK/EPS weight: 2점, 3점, or 4점
    val difficultyTier: Int = 1 // 1 = Foundation, 2 = Intermediate, 3 = Advanced
)

object KoreanExamSeed {

    fun questionsFor(examType: ExamType): List<ExamQuestion> = when (examType) {
        ExamType.TOPIK_I -> topik1Questions
        ExamType.TOPIK_II -> topik2Questions
        ExamType.EPS_TOPIK -> epsTopikQuestions
        ExamType.TOPIK_SPEAKING -> topikSpeakingQuestions
    }

    private val topik1Questions = listOf(
        ExamQuestion(
            id = "t1_l1", examType = ExamType.TOPIK_I, section = "Listening",
            instructionKo = "다음을 듣고 알맞은 대답을 고르십시오.",
            stimulusKo = "질문을 듣고 이어질 가장 알맞은 대답을 고르세요.",
            audioTranscriptKo = "민수 씨, 오늘 점심에 무엇을 먹을까요?",
            options = listOf("비빔밥을 먹읍시다.", "어제 친구를 만났어요.", "지하철로 삼십 분 걸려요.", "도서관에서 공부해요."),
            correctOptionIndex = 0,
            explanationEn = "The speaker asks 'What shall we eat for lunch today?' — '비빔밥을 먹읍시다' ('Let's eat bibimbap') is the logical response."
        ),
        ExamQuestion(
            id = "t1_l2", examType = ExamType.TOPIK_I, section = "Listening",
            instructionKo = "다음을 듣고 장소로 알맞은 것을 고르십시오.",
            stimulusKo = "대화가 이루어지는 곳은 어디입니까?",
            audioTranscriptKo = "어서 오세요. 따뜻한 아메리카노 한 잔과 치즈케이크 하나 주세요.",
            options = listOf("병원", "카페", "우체국", "은행"),
            correctOptionIndex = 1,
            explanationEn = "Ordering an Americano and cheesecake clearly takes place at a café (카페)."
        ),
        ExamQuestion(
            id = "t1_l3", examType = ExamType.TOPIK_I, section = "Listening",
            instructionKo = "다음을 듣고 남자가 할 행동으로 알맞은 것을 고르십시오.",
            stimulusKo = "남자는 지금 무엇을 하려고 합니까?",
            audioTranscriptKo = "밖에 비가 많이 오네요. 우산을 사러 편의점에 다녀올게요.",
            options = listOf("집에서 쉴 것입니다.", "편의점에 갈 것입니다.", "영화를 볼 것입니다.", "책을 읽을 것입니다."),
            correctOptionIndex = 1,
            explanationEn = "'편의점에 다녀올게요' means he is going to the convenience store to buy an umbrella."
        ),
        ExamQuestion(
            id = "t1_l4", examType = ExamType.TOPIK_I, section = "Listening",
            instructionKo = "다음을 듣고 맞는 내용을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioTranscriptKo = "저는 주말마다 공원에서 자전거를 탑니다. 이번 토요일에도 친구와 함께 탈 겁니다.",
            options = listOf("평일마다 자전거를 탑니다.", "혼자 자전거를 탈 것입니다.", "주말마다 공원에서 자전거를 탑니다.", "자전거가 없습니다."),
            correctOptionIndex = 2,
            explanationEn = "'주말마다 공원에서 자전거를 탑니다' matches the spoken passage directly."
        ),
        ExamQuestion(
            id = "t1_r1", examType = ExamType.TOPIK_I, section = "Reading",
            instructionKo = "빈칸에 들어갈 알맞은 말을 고르십시오.",
            stimulusKo = "저는 매일 아침 여덟 시에 학교에 ( ____ ).",
            options = listOf("잡니다", "갑니다", "먹습니다", "읽습니다"),
            correctOptionIndex = 1,
            explanationEn = "Location particle -에 pairs naturally with movement verb 갑니다 ('go to school')."
        ),
        ExamQuestion(
            id = "t1_r2", examType = ExamType.TOPIK_I, section = "Reading",
            instructionKo = "빈칸에 들어갈 알맞은 말을 고르십시오.",
            stimulusKo = "날씨가 너무 더워서 ( ____ ) 물을 마셨어요.",
            options = listOf("차가운", "무거운", "어려운", "가까운"),
            correctOptionIndex = 0,
            explanationEn = "Because the weather is hot (더워서), 'cold water' (차가운 물) is the logical adjective."
        ),
        ExamQuestion(
            id = "t1_r3", examType = ExamType.TOPIK_I, section = "Reading",
            instructionKo = "무엇에 대한 이야기인지 알맞은 것을 고르십시오.",
            stimulusKo = "이곳에서는 여러 나라의 책을 빌릴 수 있습니다. 조용히 공부하는 사람도 많습니다.",
            options = listOf("시장", "식당", "도서관", "공항"),
            correctOptionIndex = 2,
            explanationEn = "A place where you borrow books and study quietly is a library (도서관)."
        ),
        ExamQuestion(
            id = "t1_r4", examType = ExamType.TOPIK_I, section = "Reading",
            instructionKo = "밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
            stimulusKo = "한국어 시험에 합격해서 기분이 정말 좋습니다.",
            options = listOf("행복합니다", "피곤합니다", "바쁩니다", "심심합니다"),
            correctOptionIndex = 0,
            explanationEn = "기분이 좋습니다 ('feel good') is closest in meaning to 행복합니다 ('am happy')."
        ),
        ExamQuestion(
            id = "t1_r5", examType = ExamType.TOPIK_I, section = "Reading",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            stimulusKo = "수진 씨는 요즘 한국 요리를 배웁니다. 일주일에 두 번 요리 학원에 갑니다. 지난주에는 불고기를 만들었습니다.",
            options = listOf("수진 씨는 요리 선생님입니다.", "수진 씨는 매일 학원에 갑니다.", "수진 씨는 지난주에 불고기를 만들었습니다.", "수진 씨는 한국 음식을 싫어합니다."),
            correctOptionIndex = 2,
            explanationEn = "The text explicitly states '지난주에는 불고기를 만들었습니다' (made bulgogi last week)."
        ),
        ExamQuestion(
            id = "t1_r6", examType = ExamType.TOPIK_I, section = "Reading",
            instructionKo = "빈칸에 들어갈 알맞은 문법을 고르십시오.",
            stimulusKo = "감기에 걸려서 병원에 ( ____ ) 약을 받았습니다.",
            options = listOf("가고", "가지만", "가려고", "가거나"),
            correctOptionIndex = 0,
            explanationEn = "-고 connects sequential actions cleanly: went to the hospital and received medicine."
        )
    )

    private val topik2Questions = listOf(
        ExamQuestion(
            id = "t2_l1", examType = ExamType.TOPIK_II, section = "Listening",
            instructionKo = "대화를 듣고 남자의 중심 생각을 고르십시오.",
            stimulusKo = "남자가 강조하는 내용은 무엇입니까?",
            audioTranscriptKo = "요즘 스마트폰으로 뉴스를 쉽게 볼 수 있지만, 출처를 확인하지 않고 공유하면 잘못된 정보가 퍼질 수 있습니다.",
            options = listOf("스마트폰 사용을 금지해야 한다.", "정보 공유 전에 출처를 확인해야 한다.", "종이 신문만 읽어야 한다.", "뉴스 속도가 가장 중요하다."),
            correctOptionIndex = 1,
            explanationEn = "The speaker warns against sharing news without verifying its source (출처를 확인하지 않고 공유하면...)."
        ),
        ExamQuestion(
            id = "t2_l2", examType = ExamType.TOPIK_II, section = "Listening",
            instructionKo = "다음을 듣고 여자의 태도로 알맞은 것을 고르십시오.",
            stimulusKo = "여자는 새 근무 제도에 대해 어떻게 생각합니까?",
            audioTranscriptKo = "유연근무제를 도입한 뒤 출퇴근 스트레스가 줄었을 뿐만 아니라 업무 몰입도도 확실히 높아졌습니다.",
            options = listOf("긍정적으로 평가하고 있다.", "도입을 반대하고 있다.", "전혀 관심이 없다.", "비용 증가를 걱정하고 있다."),
            correctOptionIndex = 0,
            explanationEn = "Using -을 뿐만 아니라 to list reduced stress and higher focus reflects a clearly positive evaluation."
        ),
        ExamQuestion(
            id = "t2_r1", examType = ExamType.TOPIK_II, section = "Reading",
            instructionKo = "( )에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "독서는 지식을 넓혀 ( ____ ) 다른 사람의 감정을 이해하는 공감 능력도 키워 준다.",
            options = listOf("줄 뿐만 아니라", "주는 반면에", "주기 무섭게", "주기는커녕"),
            correctOptionIndex = 0,
            explanationEn = "-(으)ㄹ 뿐만 아니라 ('not only ... but also') links two positive effects of reading."
        ),
        ExamQuestion(
            id = "t2_r2", examType = ExamType.TOPIK_II, section = "Reading",
            instructionKo = "밑줄 친 부분과 바꾸어 쓸 수 있는 것을 고르십시오.",
            stimulusKo = "신입 사원이라도 의견이 있으면 주저하지 말고 말해야 한다.",
            options = listOf("망설이지 말고", "서두르지 말고", "포기하지 말고", "기대하지 말고"),
            correctOptionIndex = 0,
            explanationEn = "주저하다 ('to hesitate') is synonymous with 망설이다."
        ),
        ExamQuestion(
            id = "t2_r3", examType = ExamType.TOPIK_II, section = "Reading",
            instructionKo = "다음 글의 주제로 가장 알맞은 것을 고르십시오.",
            stimulusKo = "도시 내 작은 공원을 늘리면 여름철 기온 상승을 완화할 수 있고, 주민들의 정신 건강에도 긍정적인 영향을 미친다. 따라서 도시 개발 시 녹지 공간 확보는 필수적이다.",
            options = listOf("도심 녹지 공간 확보의 필요성", "대중교통 요금 인상의 원인", "고층 건물 설계의 최신 유행", "여름철 에너지 소비량 비교"),
            correctOptionIndex = 0,
            explanationEn = "The concluding sentence ('녹지 공간 확보는 필수적이다') directly states the necessity of urban green space."
        ),
        ExamQuestion(
            id = "t2_r4", examType = ExamType.TOPIK_II, section = "Reading",
            instructionKo = "( )에 들어갈 가장 알맞은 연결 표현을 고르십시오.",
            stimulusKo = "기후 변화 문제가 심각해지고 있다. ( ____ ) 각국 정부는 재생에너지 전환을 서두르고 있다.",
            options = listOf("이에 따라", "그럼에도 불구하고", "오히려", "반면에"),
            correctOptionIndex = 0,
            explanationEn = "'이에 따라' ('Accordingly / Consequently') introduces the policy response to worsening climate change."
        ),
        ExamQuestion(
            id = "t2_w1", examType = ExamType.TOPIK_II, section = "Writing",
            instructionKo = "[쓰기 연습] 다음 주제에 대해 2~3문장으로 한국어 의견을 작성하십시오.",
            stimulusKo = "주제: 외국어를 꾸준히 배우는 것이 왜 중요합니까? (예: 문화 이해, 기회 확대, 소통 등)",
            isOpenEnded = true,
            expectedKeywords = listOf("외국어", "문화", "소통", "기회", "이해", "중요", "때문", "니다", "다"),
            explanationEn = "Strong TOPIK II writing uses clear topic sentences, formal written style (-다 / -습니다), and connectors like -기 때문에 or -뿐만 아니라."
        )
    )

    private val epsTopikQuestions = listOf(
        ExamQuestion(
            id = "eps_l1", examType = ExamType.EPS_TOPIK, section = "Listening",
            instructionKo = "다음을 듣고 작업 전에 꼭 해야 할 일을 고르십시오.",
            stimulusKo = "반장님의 지시 내용과 같은 것을 고르십시오.",
            audioTranscriptKo = "오늘 용접 작업이 있으니까 시작하기 전에 반드시 보안면과 안전장갑을 착용하십시오.",
            options = listOf("보안면과 안전장갑을 착용한다.", "기계 전원을 켜고 자리를 비운다.", "작업장 안에서 식사를 한다.", "안전모를 벗고 작업한다."),
            correctOptionIndex = 0,
            explanationEn = "The supervisor explicitly instructs wearing a welding shield (보안면) and safety gloves (안전장갑) before starting."
        ),
        ExamQuestion(
            id = "eps_l2", examType = ExamType.EPS_TOPIK, section = "Listening",
            instructionKo = "다음을 듣고 알맞은 대답을 고르십시오.",
            stimulusKo = "질문에 대한 가장 적절한 직장 내 대답을 고르십시오.",
            audioTranscriptKo = "이 상자들 창고로 다 옮겼어요?",
            options = listOf("네, 방금 다 정리해 놓았습니다.", "어제 기숙사에 도착했습니다.", "월급날은 매달 25일입니다.", "지하철 2호선을 타십시오."),
            correctOptionIndex = 0,
            explanationEn = "'네, 방금 다 정리해 놓았습니다' politely confirms the boxes were moved and organized."
        ),
        ExamQuestion(
            id = "eps_l3", examType = ExamType.EPS_TOPIK, section = "Listening",
            instructionKo = "다음을 듣고 비상시 행동으로 맞는 것을 고르십시오.",
            stimulusKo = "화재가 발생했을 때 가장 먼저 해야 할 일은 무엇입니까?",
            audioTranscriptKo = "작업장에서 불이 나면 당황하지 말고 비상벨을 누른 뒤 소화기나 비상구를 확인하고 즉시 대피하십시오.",
            options = listOf("비상벨을 누르고 즉시 대피한다.", "기계를 계속 작동시킨다.", "창문을 모두 닫고 기다린다.", "엘리베이터를 타고 내려간다."),
            correctOptionIndex = 0,
            explanationEn = "Pressing the emergency alarm (비상벨) and evacuating promptly (즉시 대피) follows the workplace safety instruction."
        ),
        ExamQuestion(
            id = "eps_r1", examType = ExamType.EPS_TOPIK, section = "Reading",
            instructionKo = "다음 안전 표지 문구의 의미로 알맞은 것을 고르십시오.",
            stimulusKo = "[ 주의: 고압 전류 위험 — 관계자 외 출입 금지 ]",
            options = listOf("허가된 사람만 들어갈 수 있습니다.", "누구나 자유롭게 휴식할 수 있습니다.", "여기서 음식을 먹을 수 있습니다.", "안전화 없이 들어가도 됩니다."),
            correctOptionIndex = 0,
            explanationEn = "'관계자 외 출입 금지' means authorized personnel only; others are prohibited from entering."
        ),
        ExamQuestion(
            id = "eps_r2", examType = ExamType.EPS_TOPIK, section = "Reading",
            instructionKo = "빈칸에 들어갈 가장 알맞은 말을 고르십시오.",
            stimulusKo = "소음이 심한 작업장에서는 청력을 보호하기 위해 꼭 ( ____ )을/를 착용해야 합니다.",
            options = listOf("귀마개", "앞치마", "우산", "지갑"),
            correctOptionIndex = 0,
            explanationEn = "Earplugs (귀마개) protect hearing (청력) in high-noise workplaces (소음이 심한 작업장)."
        ),
        ExamQuestion(
            id = "eps_r3", examType = ExamType.EPS_TOPIK, section = "Reading",
            instructionKo = "다음 안내문을 읽고 내용과 같은 것을 고르십시오.",
            stimulusKo = "기숙사 공지: 밤 11시 이후에는 세탁기 사용을 자제해 주시고, 외출할 때는 난방기와 전등을 반드시 꺼 주시기 바랍니다.",
            options = listOf("외출할 때 난방기와 전등을 꺼야 합니다.", "자정에 세탁기를 사용하는 것이 좋습니다.", "기숙사에서는 외출이 금지되어 있습니다.", "전등을 항상 켜 두어야 합니다."),
            correctOptionIndex = 0,
            explanationEn = "The notice requires turning off heaters (난방기) and lights (전등) when going out."
        ),
        ExamQuestion(
            id = "eps_r4", examType = ExamType.EPS_TOPIK, section = "Reading",
            instructionKo = "빈칸에 들어갈 알맞은 표현을 고르십시오.",
            stimulusKo = "기계에 이상이 생기면 임의로 고치지 말고 즉시 반장님께 ( ____ ).",
            options = listOf("보고해야 합니다", "퇴근해야 합니다", "구입해야 합니다", "포장해야 합니다"),
            correctOptionIndex = 0,
            explanationEn = "When a machine malfunctions, standard safety protocol is to report immediately to the team leader (보고해야 합니다)."
        ),
        ExamQuestion(
            id = "eps_r5", examType = ExamType.EPS_TOPIK, section = "Reading",
            instructionKo = "어업/농업 현장 안전 수칙으로 올바른 것을 고르십시오.",
            stimulusKo = "어선에 승선하여 작업할 때는 파도에 대비해 반드시 ( ____ )를 입고 작업해야 합니다.",
            options = listOf("구명조끼", "넥타이", "운동복", "가방"),
            correctOptionIndex = 0,
            explanationEn = "A life jacket (구명조끼) is mandatory when working aboard a fishing vessel (어선)."
        )
    )

    private val topikSpeakingQuestions = listOf(
        ExamQuestion(
            id = "tspk_1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking",
            instructionKo = "[과제 1: 질문에 대답하기] 다음 질문에 한국어로 정중하게 대답을 작성하십시오.",
            stimulusKo = "한국어를 배우기 시작한 계기는 무엇입니까? 그리고 한국어로 가장 하고 싶은 일은 무엇입니까?",
            audioTranscriptKo = "한국어를 배우기 시작한 계기는 무엇입니까? 그리고 한국어로 가장 하고 싶은 일은 무엇입니까?",
            isOpenEnded = true,
            expectedKeywords = listOf("한국어", "배우", "때문", "싶", "문화", "일", "여행", "친구", "요", "습니다"),
            explanationEn = "Address both parts of the prompt clearly (why you started + what you want to do) using polite 해요체 or formal 합니다체."
        ),
        ExamQuestion(
            id = "tspk_2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking",
            instructionKo = "[과제 2: 상황 대응하기] 주어진 상황에서 자연스럽게 요청하는 말을 작성하십시오.",
            stimulusKo = "상황: 한국 식당에서 음식을 주문했는데, 매운 음식을 잘 못 먹습니다. 직원에게 덜 맵게 해 달라고 정중히 부탁해 보세요.",
            isOpenEnded = true,
            expectedKeywords = listOf("매운", "맵", "잘 못", "덜", "안", "주세요", "부탁", "혹시", "요"),
            explanationEn = "Polite requests use '혹시...', '-아/어 주실 수 있어요?', or '덜 맵게 해 주세요'."
        ),
        ExamQuestion(
            id = "tspk_3", examType = ExamType.TOPIK_SPEAKING, section = "Speaking",
            instructionKo = "[과제 3: 의견 제시하기] 자신의 입장과 근거를 한국어로 논리적으로 작성하십시오.",
            stimulusKo = "질문: 혼자 공부하는 것과 스터디 그룹에서 함께 공부하는 것 중 어느 쪽이 더 효과적이라고 생각하십니까? 이유를 들어 설명하세요.",
            isOpenEnded = true,
            expectedKeywords = listOf("혼자", "함께", "같이", "집중", "동기", "생각", "때문", "효과", "습니다", "요"),
            explanationEn = "State your clear preference first, then support it with 1–2 concrete reasons using -기 때문입니다."
        )
    )
}
