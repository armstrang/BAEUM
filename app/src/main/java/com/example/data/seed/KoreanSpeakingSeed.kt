package com.example.data.seed

enum class SpeakingDifficulty(val label: String, val koreanBadge: String, val assistDescription: String) {
    BEGINNER("Beginner", "초급", "Hangul • Romanization • Translation"),
    INTERMEDIATE("Intermediate", "중급", "Hangul • Optional assistance"),
    ADVANCED("Advanced", "고급", "Korean-first experience")
}

data class ConversationTurnPrompt(
    val turnId: String,
    val speakerRole: String,
    val promptKo: String,
    val romanization: String,
    val translationEn: String,
    val expectedKeywords: List<String>,
    val expectedSpeechLevel: String, // "해요체" or "합니다체"
    val modelResponseKo: String,
    val modelResponseMeaning: String,
    val hintEn: String
)

data class SpeakingScenario(
    val id: String,
    val title: String,
    val titleKo: String,
    val category: String, // "Seoul Life", "Workplace", "TOPIK Speaking", "Daily"
    val difficulty: SpeakingDifficulty,
    val settingDescription: String,
    val partnerRole: String,
    val turns: List<ConversationTurnPrompt>
)

/**
 * 30 distinct, professionally structured Korean conversation scenarios:
 * 10 Beginner (초급), 10 Intermediate (중급), 10 Advanced (고급).
 * Every turn has a unique turnId to guarantee zero duplicated prompts or infinite loops.
 */
object KoreanSpeakingSeed {

    fun scenariosByDifficulty(difficulty: SpeakingDifficulty): List<SpeakingScenario> =
        allScenarios.filter { it.difficulty == difficulty }

    fun workplaceScenarios(): List<SpeakingScenario> =
        allScenarios.filter { it.category == "Workplace" }

    fun topikSpeakingScenarios(): List<SpeakingScenario> =
        allScenarios.filter { it.category == "TOPIK Speaking" }

    val allScenarios: List<SpeakingScenario> = listOf(
        // ==================== BEGINNER (10 SCENARIOS) ====================
        SpeakingScenario(
            id = "beg_cafe", title = "At a Korean Café", titleKo = "한국 카페에서 주문하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Order a drink, pick a size, and choose dine-in or takeout in Seoul.",
            partnerRole = "Barista",
            turns = listOf(
                ConversationTurnPrompt("beg_cafe_1", "Barista", "어서 오세요! 무엇을 주문하시겠어요?",
                    "eo-seo o-se-yo! mu-eo-seul ju-mun-ha-si-ge-sseo-yo?", "Welcome! What would you like to order?",
                    listOf("아메리카노", "커피", "라떼", "차", "주스", "주세요", "주문"), "해요체",
                    "아이스 아메리카노 한 잔 주세요.", "One iced Americano, please.",
                    "Name a drink + 주세요 (e.g., 아이스 아메리카노 주세요)."),
                ConversationTurnPrompt("beg_cafe_2", "Barista", "사이즈는 어떻게 하시겠어요?",
                    "sa-i-jeu-neun eo-tteo-ke ha-si-ge-sseo-yo?", "What size would you like?",
                    listOf("큰", "작은", "보통", "레귤러", "라지", "사이즈", "주세요"), "해요체",
                    "큰 사이즈로 주세요.", "Large size, please.",
                    "Say 큰 사이즈로 주세요 or 보통 사이즈로 주세요."),
                ConversationTurnPrompt("beg_cafe_3", "Barista", "매장에서 드시고 가시나요, 포장해 드릴까요?",
                    "mae-jang-e-seo deu-si-go ga-si-na-yo, po-jang-hae deu-ril-kka-yo?", "For here, or would you like it to go?",
                    listOf("매장", "포장", "먹고", "마시고", "가져", "주세요"), "해요체",
                    "포장해 주세요.", "To go, please.",
                    "Choose 매장에서 마실게요 (for here) or 포장해 주세요 (to go)."),
                ConversationTurnPrompt("beg_cafe_4", "Barista", "4,500원입니다. 결제는 어떻게 하시겠어요?",
                    "sa-cheon o-baek-wo-nim-ni-da. gyeol-je-neun eo-tteo-ke ha-si-ge-sseo-yo?", "That is 4,500 won. How would you like to pay?",
                    listOf("카드", "현금", "계산", "결제", "할게요", "감사"), "해요체",
                    "카드로 계산할게요. 감사합니다.", "I will pay by card. Thank you.",
                    "Mention 카드로 (by card) or 현금으로 (by cash) + 계산할게요.")
            )
        ),
        SpeakingScenario(
            id = "beg_intro", title = "First Self-Introduction", titleKo = "처음 만나서 인사하기",
            category = "Daily", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Introduce your name, nationality, and hobby politely to a new acquaintance.",
            partnerRole = "New Friend (민지)",
            turns = listOf(
                ConversationTurnPrompt("beg_intro_1", "민지", "안녕하세요! 저는 민지예요. 성함이 어떻게 되세요?",
                    "an-nyeong-ha-se-yo! jeo-neun min-ji-ye-yo. seong-ha-mi eo-tteo-ke doe-se-yo?", "Hello! I'm Minji. What is your name?",
                    listOf("안녕", "저는", "이름", "이에요", "예요", "입니다"), "해요체",
                    "안녕하세요! 저는 알렉스예요. 반갑습니다.", "Hello! I'm Alex. Nice to meet you.",
                    "Greet politely and state your name with 저는 [name]예요/이에요."),
                ConversationTurnPrompt("beg_intro_2", "민지", "반가워요! 어느 나라에서 오셨어요?",
                    "ban-ga-wo-yo! eo-neu na-ra-e-seo o-syeo-sseo-yo?", "Nice to meet you! Which country are you from?",
                    listOf("에서", "왔어요", "사람", "나라", "한국", "인도네시아", "미국"), "해요체",
                    "인도네시아에서 왔어요.", "I came from Indonesia.",
                    "Use [Country]에서 왔어요."),
                ConversationTurnPrompt("beg_intro_3", "민지", "한국어는 얼마나 공부하셨어요?",
                    "han-gu-geo-neun eol-ma-na gong-bu-ha-syeo-sseo-yo?", "How long have you studied Korean?",
                    listOf("개월", "년", "달", "공부", "시작", "한국어", "배웠"), "해요체",
                    "육 개월 동안 한국어를 공부했어요.", "I studied Korean for six months.",
                    "State duration + 공부했어요 or 배우고 있어요."),
                ConversationTurnPrompt("beg_intro_4", "민지", "주말에는 보통 무엇을 하세요?",
                    "ju-ma-re-neun bo-tong mu-eo-seul ha-se-yo?", "What do you usually do on weekends?",
                    listOf("영화", "운동", "공부", "친구", "카페", "쉬어요", "해요", "가요"), "해요체",
                    "주말에는 친구를 만나거나 운동을 해요.", "On weekends I meet friends or exercise.",
                    "Describe a weekend hobby ending in -아요/어요.")
            )
        ),
        SpeakingScenario(
            id = "beg_restaurant", title = "Ordering at a Korean Restaurant", titleKo = "한식당에서 식사하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Request a table, order Korean dishes, and ask for water politely.",
            partnerRole = "Server",
            turns = listOf(
                ConversationTurnPrompt("beg_rest_1", "Server", "어서 오세요! 몇 분이세요?",
                    "eo-seo o-se-yo! myeot bu-ni-se-yo?", "Welcome! How many people?",
                    listOf("명", "혼자", "둘", "두", "한", "세", "네", "이에요", "예요"), "해요체",
                    "두 명이에요.", "Two people.",
                    "Use Native Korean number + 명이에요 (한 명, 두 명, 세 명)."),
                ConversationTurnPrompt("beg_rest_2", "Server", "이쪽으로 앉으세요. 주문하시겠어요?",
                    "i-jjo-geu-ro an-jeu-se-yo. ju-mun-ha-si-ge-sseo-yo?", "Please sit here. Are you ready to order?",
                    listOf("비빔밥", "김치", "불고기", "냉면", "국밥", "주세요", "하나", "둘"), "해요체",
                    "비빔밥 하나하고 김치찌개 하나 주세요.", "One bibimbap and one kimchi stew, please.",
                    "Name a Korean dish + 주세요."),
                ConversationTurnPrompt("beg_rest_3", "Server", "음료수나 필요한 게 더 있으세요?",
                    "eum-nyo-su-na pi-ryo-han ge deo i-sseu-se-yo?", "Would you like any drinks or anything else?",
                    listOf("물", "콜라", "사이다", "반찬", "괜찮", "주세요"), "해요체",
                    "물 좀 주세요. 감사합니다.", "Some water, please. Thank you.",
                    "Ask for 물 주세요 or politely decline with 괜찮아요.")
            )
        ),
        SpeakingScenario(
            id = "beg_subway", title = "Asking Subway Directions", titleKo = "지하철역 길 묻기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Ask a passerby how to reach the nearest subway station and transfer line.",
            partnerRole = "Seoul Local",
            turns = listOf(
                ConversationTurnPrompt("beg_sub_1", "Local", "네, 무슨 일이세요? 도움이 필요하신가요?",
                    "ne, mu-seun i-ri-se-yo? do-u-mi pi-ryo-ha-sin-ga-yo?", "Yes, what is it? Do you need help?",
                    listOf("지하철", "역", "어디", "어떻게", "가요", "실례", "죄송"), "해요체",
                    "실례합니다, 가장 가까운 지하철역이 어디에 있어요?", "Excuse me, where is the nearest subway station?",
                    "Ask where the station is using 어디에 있어요?"),
                ConversationTurnPrompt("beg_sub_2", "Local", "앞으로 쭉 가셔서 오른쪽으로 돌면 2호선 역이 나와요.",
                    "a-peu-ro jjuk ga-syeo-seo o-reun-jjo-geu-ro dol-myeon...", "Go straight ahead and turn right to find the Line 2 station.",
                    listOf("얼마나", "분", "걸려요", "가까워", "감사", "알겠"), "해요체",
                    "걸어서 몇 분쯤 걸려요?", "About how many minutes does it take on foot?",
                    "Ask travel time with 몇 분 걸려요? or thank them."),
                ConversationTurnPrompt("beg_sub_3", "Local", "걸어서 오 분 정도 걸려요. 조심히 가세요!",
                    "geo-reo-seo o bun jeong-do geol-lyeo-yo. jo-sim-hi ga-se-yo!", "It takes about 5 minutes on foot. Have a safe trip!",
                    listOf("감사", "고맙", "안녕", "네"), "해요체",
                    "친절하게 알려 주셔서 정말 감사합니다!", "Thank you very much for telling me kindly!",
                    "Express polite thanks (감사합니다 / 고맙습니다).")
            )
        ),
        SpeakingScenario(
            id = "beg_shop", title = "Shopping at Myeongdong", titleKo = "명동에서 옷 사기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Ask for the price, request a different size, and complete a purchase.",
            partnerRole = "Shop Clerk",
            turns = listOf(
                ConversationTurnPrompt("beg_shop_1", "Clerk", "어서 오세요! 찾으시는 물건 있으세요?",
                    "eo-seo o-se-yo! cha-jeu-si-neun mul-geon i-sseu-se-yo?", "Welcome! Are you looking for something specific?",
                    listOf("옷", "티셔츠", "재킷", "가방", "신발", "얼마", "보고", "찾고"), "해요체",
                    "이 재킷은 얼마예요?", "How much is this jacket?",
                    "Ask about an item or its price (얼마예요?)."),
                ConversationTurnPrompt("beg_shop_2", "Clerk", "이 제품은 39,000원입니다. 입어 보시겠어요?",
                    "i je-pu-meun sam-man gu-cheon-wo-nim-ni-da. i-beo bo-si-ge-sseo-yo?", "This item is 39,000 won. Would you like to try it on?",
                    listOf("네", "입어", "사이즈", "큰", "작은", "색", "볼게요", "주세요"), "해요체",
                    "네, 한 번 입어 볼게요. 더 큰 사이즈도 있어요?", "Yes, I'll try it on. Do you have a larger size?",
                    "Respond with 네, 입어 볼게요 or ask about size/color."),
                ConversationTurnPrompt("beg_shop_3", "Clerk", "잘 어울리세요! 이걸로 하시겠어요?",
                    "jal eo-ul-li-se-yo! i-geol-lo ha-si-ge-sseo-yo?", "It suits you well! Would you like to take this one?",
                    listOf("네", "이걸로", "살게요", "주세요", "카드", "계산"), "해요체",
                    "네, 이걸로 살게요. 카드로 계산해 주세요.", "Yes, I'll buy this one. Please charge my card.",
                    "Confirm purchase with 이걸로 살게요 / 주세요.")
            )
        ),
        SpeakingScenario(
            id = "beg_convenience", title = "At a Convenience Store", titleKo = "편의점에서 계산하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Buy snacks, decline a plastic bag, and recharge a T-money transit card.",
            partnerRole = "Cashier",
            turns = listOf(
                ConversationTurnPrompt("beg_conv_1", "Cashier", "안녕하세요! 봉투 필요하세요?",
                    "an-nyeong-ha-se-yo! bong-tu pi-ryo-ha-se-yo?", "Hello! Do you need a bag?",
                    listOf("아니요", "괜찮", "네", "봉투", "주세요", "필요"), "해요체",
                    "아니요, 봉투는 괜찮아요.", "No, I don't need a bag.",
                    "Say 네, 주세요 or 아니요, 괜찮아요."),
                ConversationTurnPrompt("beg_conv_2", "Cashier", "삼각김밥은 데워 드릴까요?",
                    "sam-gak-gim-ba-beun de-wo deu-ril-kka-yo?", "Shall I heat up the triangle kimbap?",
                    listOf("네", "데워", "아니요", "괜찮", "주세요"), "해요체",
                    "네, 데워 주세요.", "Yes, please heat it up.",
                    "Reply 네, 데워 주세요 or 아니요, 괜찮아요."),
                ConversationTurnPrompt("beg_conv_3", "Cashier", "더 필요한 건 없으세요?",
                    "deo pi-ryo-han geon eop-sseu-se-yo?", "Is there anything else you need?",
                    listOf("교통카드", "충전", "영수증", "없어요", "괜찮", "감사"), "해요체",
                    "교통카드 만 원만 충전해 주세요.", "Please recharge my transit card with 10,000 won.",
                    "Ask to recharge 교통카드 or say 괜찮아요, 감사합니다.")
            )
        ),
        SpeakingScenario(
            id = "beg_taxi", title = "Taking a Seoul Taxi", titleKo = "택시 타기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Tell the taxi driver your destination and ask to stop at the entrance.",
            partnerRole = "Taxi Driver",
            turns = listOf(
                ConversationTurnPrompt("beg_taxi_1", "Driver", "어서 타세요. 어디로 모실까요?",
                    "eo-seo ta-se-yo. eo-di-ro mo-sil-kka-yo?", "Hop in. Where can I take you?",
                    listOf("서울역", "홍대", "강남", "명동", "호텔", "공항", "가 주세요", "으로"), "해요체",
                    "홍대입구역으로 가 주세요.", "Please go to Hongik Univ. Station.",
                    "Give a destination + 으로 가 주세요."),
                ConversationTurnPrompt("beg_taxi_2", "Driver", "네, 알겠습니다. 길이 조금 막히는데 괜찮으신가요?",
                    "ne, al-ge-sseum-ni-da. gi-ri jo-geum ma-ki-neun-de...", "Understood. Traffic is a bit heavy, is that okay?",
                    listOf("네", "괜찮", "얼마나", "시간", "분", "걸릴까요"), "해요체",
                    "네, 괜찮아요. 얼마나 걸릴까요?", "Yes, that's fine. How long will it take?",
                    "Respond politely and ask how long it takes."),
                ConversationTurnPrompt("beg_taxi_3", "Driver", "다 왔습니다! 여기서 내려 드릴까요?",
                    "da wa-sseum-ni-da! yeo-gi-seo nae-ryeo deu-ril-kka-yo?", "We're here! Shall I drop you off right here?",
                    listOf("네", "여기서", "세워", "카드", "감사", "수고"), "해요체",
                    "네, 여기서 세워 주세요. 카드로 계산할게요.", "Yes, please stop here. I'll pay by card.",
                    "Confirm stopping here and thank the driver.")
            )
        ),
        SpeakingScenario(
            id = "beg_weather", title = "Small Talk About Weather", titleKo = "오늘 날씨 이야기하기",
            category = "Daily", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Chat naturally with a classmate about today's weather and favorite season.",
            partnerRole = "Classmate (준호)",
            turns = listOf(
                ConversationTurnPrompt("beg_wea_1", "준호", "오늘 날씨가 정말 좋네요! 아침에 춥지 않았어요?",
                    "o-neul nal-ssi-ga jeong-mal jon-ne-yo! a-chi-me chup-ji a-na-sseo-yo?", "The weather is really nice today! Wasn't it cold this morning?",
                    listOf("네", "아니요", "조금", "추웠", "따뜻", "시원", "맑", "좋아요"), "해요체",
                    "아침에는 조금 추웠는데 지금은 따뜻해요.", "It was a bit cold this morning, but it's warm now.",
                    "Describe the temperature (추웠어요 / 따뜻해요 / 시원해요)."),
                ConversationTurnPrompt("beg_wea_2", "준호", "한국 사계절 중에서 어느 계절을 제일 좋아하세요?",
                    "han-guk sa-gye-jeol jung-e-seo eo-neu gye-jeo-reul je-il jo-a-ha-se-yo?", "Among Korea's four seasons, which season do you like best?",
                    listOf("봄", "여름", "가을", "겨울", "좋아", "예뻐", "시원"), "해요체",
                    "저는 가을을 제일 좋아해요. 날씨가 시원하거든요.", "I like autumn best because the weather is cool.",
                    "Pick 봄, 여름, 가을, or 겨울 and say 좋아해요."),
                ConversationTurnPrompt("beg_wea_3", "준호", "주말에 날씨 좋으면 한강 공원에 갈래요?",
                    "ju-ma-re nal-ssi jo-eu-myeon han-gang gong-wo-ne gal-lae-yo?", "If the weather is nice this weekend, want to go to Hangang Park?",
                    listOf("네", "좋아요", "같이", "가요", "가고 싶어요", "약속"), "해요체",
                    "네, 좋아요! 같이 가요.", "Yes, sounds great! Let's go together.",
                    "Accept enthusiastically or suggest another time politely.")
            )
        ),
        SpeakingScenario(
            id = "beg_pharmacy", title = "At a Korean Pharmacy", titleKo = "약국에서 약 사기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Explain mild cold symptoms and understand how to take medicine.",
            partnerRole = "Pharmacist",
            turns = listOf(
                ConversationTurnPrompt("beg_phar_1", "Pharmacist", "어서 오세요. 어디가 불편하신가요?",
                    "eo-seo o-se-yo. eo-di-ga bul-pyeon-ha-sin-ga-yo?", "Welcome. What seems to be troubling you?",
                    listOf("감기", "머리", "목", "배", "아파요", "열", "기침", "약"), "해요체",
                    "감기에 걸려서 목이 아프고 기침이 나요.", "I caught a cold, so my throat hurts and I have a cough.",
                    "Describe a symptom: 머리가 아파요 / 목이 아파요 / 감기약 주세요."),
                ConversationTurnPrompt("beg_phar_2", "Pharmacist", "열은 없으세요? 알레르기 있는 약이 있나요?",
                    "yeo-reun eop-sseu-se-yo? al-le-reu-gi in-neun ya-gi in-na-yo?", "No fever? Are you allergic to any medication?",
                    listOf("네", "아니요", "없어요", "조금", "괜찮"), "해요체",
                    "열은 없고 약 알레르기도 없어요.", "I have no fever and no medicine allergies.",
                    "Confirm whether you have fever/allergies (없어요)."),
                ConversationTurnPrompt("beg_phar_3", "Pharmacist", "이 약을 하루 세 번, 식후 30분에 드세요.",
                    "i ya-geul ha-ru se beon, si-ku sam-sip-bu-ne deu-se-yo.", "Take this medicine 3 times a day, 30 minutes after meals.",
                    listOf("네", "알겠", "감사", "식후", "물"), "해요체",
                    "네, 잘 알겠습니다. 감사합니다.", "Yes, I understand clearly. Thank you.",
                    "Acknowledge the instructions politely.")
            )
        ),
        SpeakingScenario(
            id = "beg_work_greet", title = "First Day at Work (EPS)", titleKo = "직장 첫 출근 인사",
            category = "Workplace", difficulty = SpeakingDifficulty.BEGINNER,
            settingDescription = "Greet your factory team leader respectfully on your first day in Korea.",
            partnerRole = "Team Leader (반장님)",
            turns = listOf(
                ConversationTurnPrompt("beg_wk_1", "반장님", "어서 와요! 오늘 처음 출근했죠? 자기소개 해 볼까요?",
                    "eo-seo wa-yo! o-neul cheo-eum chul-geun-haet-jjyo?", "Welcome! Today is your first day, right? Would you introduce yourself?",
                    listOf("안녕", "저는", "입니다", "잘 부탁", "열심히"), "합니다체",
                    "안녕하십니까! 새로 온 알렉스입니다. 잘 부탁드립니다.", "Hello! I am Alex, the new member. Please look after me.",
                    "Use formal 합니다체: 안녕하십니까, 저는 [name]입니다. 잘 부탁드립니다."),
                ConversationTurnPrompt("beg_wk_2", "반장님", "반가워요. 안전모랑 안전화는 다 받았어요?",
                    "ban-ga-wo-yo. an-jeon-mo-rang an-jeon-hwa-neun da ba-da-sseo-yo?", "Nice to meet you. Did you receive your helmet and safety shoes?",
                    listOf("네", "받았", "안전모", "안전화", "착용", "아직"), "합니다체",
                    "네, 안전모와 안전화를 모두 받았습니다.", "Yes, I received both the helmet and safety shoes.",
                    "Answer with 네, 받았습니다 or 아직 못 받았습니다."),
                ConversationTurnPrompt("beg_wk_3", "반장님", "작업할 때 모르는 게 있으면 바로 물어보세요.",
                    "ja-geop-hal ttae mo-reu-neun ge i-sseu-myeon ba-ro mu-reo-bo-se-yo.", "If there is anything you don't know during work, ask right away.",
                    listOf("네", "알겠", "열심히", "감사", "안전", "물어"), "합니다체",
                    "네, 알겠습니다! 안전하게 열심히 일하겠습니다.", "Yes, understood! I will work safely and diligently.",
                    "Confirm formally using 네, 알겠습니다.")
            )
        ),

        // ==================== INTERMEDIATE (10 SCENARIOS) ====================
        SpeakingScenario(
            id = "int_clinic", title = "Visiting a Korean Clinic", titleKo = "병원에서 증상 설명하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Describe persistent symptoms, duration, and ask about recovery time.",
            partnerRole = "Doctor",
            turns = listOf(
                ConversationTurnPrompt("int_cl_1", "Doctor", "안녕하세요, 어디가 어떻게 아프셔서 오셨나요?",
                    "an-nyeong-ha-se-yo, eo-di-ga eo-tteo-ke a-peu-syeo-seo o-syeon-na-yo?", "Hello, what brings you in today?",
                    listOf("어제", "며칠", "아파", "열", "기침", "소화", "두통", "부터"), "해요체",
                    "이틀 전부터 속이 안 좋고 두통이 계속 있어요.", "Since two days ago my stomach has felt upset and I've had a headache.",
                    "Explain when symptoms started (-부터) and what hurts."),
                ConversationTurnPrompt("int_cl_2", "Doctor", "최근에 무리하시거나 평소와 다른 음식을 드셨나요?",
                    "choe-geu-ne mu-ri-ha-si-geo-na pyeong-so-wa da-reun eum-si-geul deu-syeon-na-yo?", "Have you overworked recently or eaten anything unusual?",
                    listOf("야근", "피곤", "매운", "음식", "잠", "스트레스", "못", "먹었"), "해요체",
                    "요즘 야근이 많아서 잠을 잘 못 잤어요.", "I had a lot of overtime lately so I couldn't sleep well.",
                    "Give a reason using -아/어서 or -기 때문에."),
                ConversationTurnPrompt("int_cl_3", "Doctor", "며칠 푹 쉬시고 처방해 드린 약을 꼬박꼬박 드세요. 궁금한 점 있으세요?",
                    "myeo-chil puk swi-si-go cheo-bang-hae deu-rin ya-geul...", "Rest well for a few days and take the prescribed medicine regularly. Any questions?",
                    listOf("언제", "음식", "커피", "운동", "다시", "괜찮", "조심", "감사"), "해요체",
                    "약을 먹는 동안 피해야 할 음식이 있나요?", "Are there any foods I should avoid while taking the medicine?",
                    "Ask a practical follow-up question using -(으)ㄹ까요 or -나요?")
            )
        ),
        SpeakingScenario(
            id = "int_work_report", title = "Reporting a Machine Issue (EPS)", titleKo = "기계 이상 보고하기",
            category = "Workplace", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Calmly report abnormal noise on a factory machine and confirm safety steps.",
            partnerRole = "Supervisor (팀장님)",
            turns = listOf(
                ConversationTurnPrompt("int_wr_1", "팀장님", "무슨 일인가요? 급한 문제가 생겼나요?",
                    "mu-seun i-rin-ga-yo? geu-pan mun-je-ga saeng-gyeon-na-yo?", "What happened? Did an urgent issue arise?",
                    listOf("기계", "소리", "이상", "멈", "고장", "확인", "작동", "보고"), "합니다체",
                    "팀장님, 3번 기계에서 이상한 소리가 나서 보고드립니다.", "Team leader, I'm reporting an unusual noise coming from Machine 3.",
                    "Report the issue formally using -아/어서 보고드립니다."),
                ConversationTurnPrompt("int_wr_2", "팀장님", "지금 기계 전원은 꺼 놓았습니까?",
                    "ji-geum gi-gye jeon-wo-neun kkeo no-a-sseum-ni-kka?", "Have you turned off the machine power now?",
                    listOf("네", "전원", "껐", "차단", "안전", "먼저", "정지"), "합니다체",
                    "네, 안전을 위해 바로 전원을 끄고 작업 중지 표지를 붙였습니다.", "Yes, for safety I turned off the power immediately and placed a stop sign.",
                    "Confirm power-off and safety action in 합니다체."),
                ConversationTurnPrompt("int_wr_3", "팀장님", "잘 대처했어요. 수리팀이 올 때까지 다른 라인에서 작업해 줄 수 있나요?",
                    "jal dae-cheo-hae-sseo-yo. su-ri-ti-mi ol ttae-kka-ji...", "Good response. Can you work on another line until the repair team arrives?",
                    listOf("네", "알겠", "이동", "작업", "도와", "하겠습니다"), "합니다체",
                    "네, 알겠습니다. 바로 2번 라인으로 이동해서 작업하겠습니다.", "Yes, understood. I will move to Line 2 right away and work there.",
                    "Confirm readiness politely with -겠습니다.")
            )
        ),
        SpeakingScenario(
            id = "int_schedule", title = "Rescheduling an Appointment", titleKo = "약속 시간 변경하기",
            category = "Daily", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Apologize politely for an unexpected conflict and propose a new meeting time.",
            partnerRole = "Colleague (수진)",
            turns = listOf(
                ConversationTurnPrompt("int_sch_1", "수진", "여보세요? 내일 저녁 약속 때문에 전화하셨어요?",
                    "yeo-bo-se-yo? nae-il jeo-nyeok yak-sok ttae-mu-ne...", "Hello? Are you calling about tomorrow evening's plans?",
                    listOf("죄송", "미안", "갑자기", "약속", "시간", "변경", "바꿀", "야근", "일이"), "해요체",
                    "죄송한데 내일 갑자기 회의가 생겨서 약속 시간을 바꿀 수 있을까요?", "I'm sorry, a meeting suddenly came up tomorrow—could we change our time?",
                    "Apologize, explain why, and ask -(으)ㄹ 수 있을까요?"),
                ConversationTurnPrompt("int_sch_2", "수진", "괜찮아요! 그럼 이번 주 중에 언제 시간이 되세요?",
                    "gwaen-cha-na-yo! geu-reom i-beon ju jung-e...", "No problem! When are you free this week then?",
                    listOf("금요일", "토요일", "일요일", "목요일", "저녁", "오후", "괜찮", "어때요"), "해요체",
                    "금요일 저녁 일곱 시는 어떠세요?", "How about Friday evening at 7 o'clock?",
                    "Propose a specific day/time with -은/는 어떠세요?"),
                ConversationTurnPrompt("int_sch_3", "수진", "금요일 일곱 시 좋아요! 장소는 그대로 강남역으로 할까요?",
                    "geum-yo-il il-gop si jo-a-yo! jang-so-neun...", "Friday at 7 sounds good! Keep Gangnam Station as the place?",
                    listOf("네", "좋아요", "강남", "그대로", "감사", "뵐게요", "만나"), "해요체",
                    "네, 좋아요! 이해해 주셔서 감사합니다. 금요일에 뵐게요.", "Yes, great! Thank you for understanding. See you Friday.",
                    "Confirm location and express appreciation.")
            )
        ),
        SpeakingScenario(
            id = "int_exchange", title = "Exchanging a Defective Product", titleKo = "불량 제품 교환하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Explain a defect on an item you purchased and request an exchange with receipt.",
            partnerRole = "Store Manager",
            turns = listOf(
                ConversationTurnPrompt("int_ex_1", "Manager", "안녕하세요, 고객님. 무엇을 도와드릴까요?",
                    "an-nyeong-ha-se-yo, go-gaeng-nim. mu-eo-seul do-wa-deu-ril-kka-yo?", "Hello, customer. How may I assist you?",
                    listOf("어제", "샀는데", "불량", "고장", "교환", "환불", "문제", "바꾸고"), "해요체",
                    "어제 여기서 산 이어폰 한쪽이 안 들려서 교환하고 싶어요.", "One side of the earphones I bought here yesterday doesn't work, so I'd like to exchange them.",
                    "State what you bought, the defect, and 교환하고 싶어요."),
                ConversationTurnPrompt("int_ex_2", "Manager", "불편을 드려 죄송합니다. 영수증과 결제하신 카드를 가지고 계신가요?",
                    "bul-pyeo-neul deu-ryeo joe-song-ham-ni-da...", "We apologize for the inconvenience. Do you have the receipt and card?",
                    listOf("네", "영수증", "카드", "여기", "가지고", "있어요"), "해요체",
                    "네, 영수증하고 카드 둘 다 여기 있어요.", "Yes, I have both the receipt and the card right here.",
                    "Confirm you brought the receipt and card."),
                ConversationTurnPrompt("int_ex_3", "Manager", "새 제품으로 교환해 드렸습니다. 여기서 바로 작동 확인해 보시겠어요?",
                    "sae je-pu-meu-ro gyo-hwan-hae deu-ryeo-sseum-ni-da...", "I've exchanged it for a new one. Would you like to test it here?",
                    listOf("네", "확인", "잘", "되네요", "감사", "수고"), "해요체",
                    "네, 확인해 보니 잘 되네요. 빠르게 처리해 주셔서 감사합니다.", "Yes, checking it now it works well. Thank you for handling it quickly.",
                    "Confirm it works and thank the manager.")
            )
        ),
        SpeakingScenario(
            id = "int_bank", title = "Opening a Korean Bank Account", titleKo = "은행에서 계좌 개설하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Open a salary account and request mobile banking setup at a bank in Korea.",
            partnerRole = "Bank Teller",
            turns = listOf(
                ConversationTurnPrompt("int_bk_1", "Teller", "안녕하세요, 어떤 업무로 오셨나요?",
                    "an-nyeong-ha-se-yo, eo-tteon eom-mu-ro o-syeon-na-yo?", "Hello, what banking service are you here for?",
                    listOf("계좌", "통장", "만들", "개설", "월급", "체크카드", "싶어요"), "해요체",
                    "월급 통장을 만들고 싶어서 왔어요.", "I came because I want to open a salary bank account.",
                    "Mention 통장/계좌 만들기 and purpose (월급/생활비)."),
                ConversationTurnPrompt("int_bk_2", "Teller", "외국인등록증과 재직증명서를 가져오셨나요?",
                    "oe-gu-gin-deung-nok-jjeung-gwa jae-jik-jeung-myeong-seo...", "Did you bring your residence card and certificate of employment?",
                    listOf("네", "가져왔", "여기", "등록증", "서류", "있어요"), "해요체",
                    "네, 외국인등록증과 회사 재직증명서 모두 가져왔어요.", "Yes, I brought both my residence card and employment certificate.",
                    "Confirm the documents you brought."),
                ConversationTurnPrompt("int_bk_3", "Teller", "모바일 뱅킹과 해외 송금 기능도 함께 신청해 드릴까요?",
                    "mo-ba-il baeng-king-gwa hae-oe song-geum...", "Would you also like to apply for mobile banking and overseas remittance?",
                    listOf("네", "신청", "모바일", "송금", "부탁", "감사", "수수료"), "해요체",
                    "네, 둘 다 신청해 주세요. 해외 송금 수수료가 얼마인가요?", "Yes, please register both. How much is the overseas transfer fee?",
                    "Accept and ask a practical banking question.")
            )
        ),
        SpeakingScenario(
            id = "int_housing", title = "Renting an Officetel in Seoul", titleKo = "원룸/오피스텔 집 구하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Discuss deposit (보증금), monthly rent (월세), and proximity to transit with a realtor.",
            partnerRole = "Real Estate Agent",
            turns = listOf(
                ConversationTurnPrompt("int_hs_1", "Agent", "어서 오세요! 어떤 조건의 방을 찾고 계세요?",
                    "eo-seo o-se-yo! eo-tteon jo-geo-nui bang-eul chat-kko gye-se-yo?", "Welcome! What kind of room conditions are you looking for?",
                    listOf("월세", "원룸", "오피스텔", "지하철", "가까", "조용", "보증금", "찾고"), "해요체",
                    "지하철역에서 가깝고 채광이 좋은 원룸을 찾고 있어요.", "I'm looking for a sunny studio close to a subway station.",
                    "Describe your room preferences (역 근처, 월세, 조용한 곳)."),
                ConversationTurnPrompt("int_hs_2", "Agent", "보증금과 월세 예산은 어느 정도로 생각하세요?",
                    "bo-jeung-geum-gwa wol-sse ye-sa-neun...", "What budget do you have in mind for deposit and monthly rent?",
                    listOf("보증금", "월세", "만 원", "관리비", "포함", "정도"), "해요체",
                    "보증금 500만 원에 월세 60만 원 정도면 좋겠어요. 관리비는 별도인가요?", "Around 5M won deposit and 600k monthly rent. Is the maintenance fee separate?",
                    "State a budget and ask about 관리비 (maintenance fee)."),
                ConversationTurnPrompt("int_hs_3", "Agent", "조건에 맞는 방이 두 곳 있는데 지금 바로 보러 가실래요?",
                    "jo-geo-ne man-neun bang-i du got in-neun-de...", "We have two matching rooms—would you like to see them right now?",
                    listOf("네", "좋아요", "보러", "가요", "감사", "확인"), "해요체",
                    "네, 좋아요! 지금 바로 보러 가요.", "Yes, great! Let's go see them right now.",
                    "Agree to view the rooms.")
            )
        ),
        SpeakingScenario(
            id = "int_topik_spk_1", title = "TOPIK Speaking: Weekend Routine", titleKo = "TOPIK 말하기: 여가 활동",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Practice structured TOPIK Speaking responses describing habits and reasons.",
            partnerRole = "Examiner",
            turns = listOf(
                ConversationTurnPrompt("int_ts1_1", "Examiner", "평소에 스트레스를 받을 때 주로 어떤 방법으로 푸십니까?",
                    "pyeong-so-e seu-teu-re-seu-reul ba-deul ttae...", "How do you usually relieve stress?",
                    listOf("운동", "산책", "음악", "친구", "잠", "스트레스", "풀어요", "합니다"), "합니다체",
                    "저는 스트레스를 받으면 한강 공원을 산책하거나 좋아하는 음악을 듣습니다.", "When stressed, I walk in Hangang Park or listen to my favorite music.",
                    "Give 1–2 concrete habits using -거나 and polite/formal endings."),
                ConversationTurnPrompt("int_ts1_2", "Examiner", "그 방법이 본인에게 왜 효과적이라고 생각하십니까?",
                    "geu bang-beo-bi bo-ni-ne-ge wae hyo-gwa-jeo-gi-ra-go...", "Why do you think that method is effective for you?",
                    listOf("기분", "생각", "건강", "때문", "좋아", "편안", "집중"), "합니다체",
                    "야외에서 맑은 공기를 마시면서 걸으면 머리가 맑아지고 기분이 전환되기 때문입니다.", "Because walking in fresh air clears my mind and refreshes my mood.",
                    "Explain clearly with -기 때문입니다."),
                ConversationTurnPrompt("int_ts1_3", "Examiner", "앞으로 새롭게 도전해 보고 싶은 취미가 있다면 소개해 주십시오.",
                    "a-peu-ro sae-rop-ge do-jeon-hae bo-go si-peun...", "If there is a new hobby you'd like to try in the future, please introduce it.",
                    listOf("요리", "등산", "사진", "배워", "도전", "싶습니다", "싶어요", "취미"), "합니다체",
                    "앞으로 한국 전통 요리를 배워 보고 싶습니다. 직접 만들어서 친구들과 나누고 싶기 때문입니다.", "I want to try learning traditional Korean cooking so I can share it with friends.",
                    "Use -아/어 보고 싶습니다 + a brief reason.")
            )
        ),
        SpeakingScenario(
            id = "int_leave_req", title = "Requesting Annual Leave (EPS)", titleKo = "휴가 신청 상담하기",
            category = "Workplace", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Politely request annual leave and explain how your tasks will be handed over.",
            partnerRole = "Manager (과장님)",
            turns = listOf(
                ConversationTurnPrompt("int_lv_1", "과장님", "잠깐 얘기하자고 했죠? 무슨 일인가요?",
                    "jam-kkan yae-gi-ha-ja-go haet-jjyo? mu-seun i-rin-ga-yo?", "You asked to talk for a moment. What is it about?",
                    listOf("휴가", "연차", "신청", "다음", "가족", "개인", "말씀", "드릴"), "합니다체",
                    "과장님, 다음 달에 가족 행사가 있어서 연차를 3일 신청하고 싶습니다.", "Manager, I have a family event next month and would like to request 3 days of leave.",
                    "State the reason and duration politely in 합니다체."),
                ConversationTurnPrompt("int_lv_2", "과장님", "그 기간에 맡고 있는 작업 일정은 괜찮을까요?",
                    "geu gi-ga-ne mat-kko in-neun ja-geop il-jeong-eun...", "Will the work schedule you are responsible for be okay during that period?",
                    listOf("미리", "끝", "인수인계", "동료", "부탁", "마무리", "준비", "겠습니다"), "합니다체",
                    "출발하기 전에 제 작업을 미리 마무리하고 동료에게 인수인계하겠습니다.", "I will finish my tasks ahead of time and hand over smoothly to my coworker.",
                    "Reassure the manager about task handover (인수인계)."),
                ConversationTurnPrompt("int_lv_3", "과장님", "계획이 잘 되어 있네요. 휴가 신청서 작성해서 오늘 오후까지 제출하세요.",
                    "gye-hoe-gi jal doe-eo in-ne-yo...", "You've planned well. Fill out the leave form and submit it by this afternoon.",
                    listOf("네", "알겠", "제출", "감사", "오후"), "합니다체",
                    "감사합니다, 과장님. 오늘 오후까지 신청서를 작성해서 제출하겠습니다.", "Thank you, Manager. I will fill out and submit the form by this afternoon.",
                    "Express thanks and confirm submission.")
            )
        ),
        SpeakingScenario(
            id = "int_travel_plan", title = "Planning a Jeju Trip", titleKo = "제주도 여행 계획 세우기",
            category = "Daily", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Coordinate flights, local food, and sightseeing with a Korean friend.",
            partnerRole = "Friend (지훈)",
            turns = listOf(
                ConversationTurnPrompt("int_tr_1", "지훈", "다음 달 제주도 여행 기대돼요! 제일 먼저 어디에 가고 싶어요?",
                    "da-eum dal je-ju-do yeo-haeng gi-dae-dwae-yo!", "I'm excited for our Jeju trip next month! Where do you want to go first?",
                    listOf("바다", "한라산", "성산", "시장", "카페", "오름", "가고 싶", "구경"), "해요체",
                    "도착하자마자 예쁜 바다가 보이는 카페에 가고 싶어요.", "As soon as we arrive, I want to visit a café overlooking the ocean.",
                    "Mention a Jeju spot + 가고 싶어요."),
                ConversationTurnPrompt("int_tr_2", "지훈", "좋아요! 이동은 렌터카를 빌릴까요, 아니면 버스를 탈까요?",
                    "jo-a-yo! i-dong-eun ren-teo-ka-reul bil-lil-kka-yo...", "Nice! Should we rent a car or take buses?",
                    listOf("렌터카", "버스", "편하", "운전", "시간", "좋을", "것 같아요"), "해요체",
                    "가야 할 곳이 많으니까 렌터카를 빌리는 게 더 편할 것 같아요.", "Since we have many places to visit, renting a car seems more convenient.",
                    "Compare options using -(으)니까 and -(으)ㄹ 것 같아요."),
                ConversationTurnPrompt("int_tr_3", "지훈", "맞아요! 숙소는 제가 알아볼 테니까 식당 리스트는 같이 찾아봐요.",
                    "ma-ja-yo! suk-sso-neun je-ga a-ra-bol te-ni-kka...", "Right! I'll look into lodging, so let's search for restaurants together.",
                    listOf("네", "제가", "식당", "맛집", "찾아", "보낼게요", "고마워"), "해요체",
                    "고마워요! 제가 유명한 제주 맛집을 찾아서 내일 공유할게요.", "Thanks! I'll find famous Jeju restaurants and share the list tomorrow.",
                    "Commit to your part using -(으)ㄹ게요.")
            )
        ),
        SpeakingScenario(
            id = "int_delivery", title = "Parcel Delivery Follow-Up", titleKo = "택배 배송 문의하기",
            category = "Seoul Life", difficulty = SpeakingDifficulty.INTERMEDIATE,
            settingDescription = "Contact a delivery driver about leaving a parcel at the front door.",
            partnerRole = "Courier",
            turns = listOf(
                ConversationTurnPrompt("int_dl_1", "Courier", "안녕하세요, 택배 기사입니다. 지금 집에 계신가요?",
                    "an-nyeong-ha-se-yo, taek-bae gi-sa-im-ni-da...", "Hello, this is the courier. Are you home right now?",
                    listOf("집", "회사", "외출", "없어요", "문 앞", "놔", "놓아", "부탁"), "해요체",
                    "죄송한데 지금 회사에 있어서 문 앞에 놓아 주시면 감사하겠습니다.", "Sorry, I'm at work right now, so I'd appreciate it if you leave it at the front door.",
                    "Explain you're away and ask to leave it at 문 앞."),
                ConversationTurnPrompt("int_dl_2", "Courier", "알겠습니다. 공동현관 비밀번호가 필요한가요?",
                    "al-ge-sseum-ni-da. gong-dong-hyeon-gwan bi-mil-beon-ho-ga...", "Got it. Do I need the building lobby keypad code?",
                    listOf("네", "비밀번호", "문자", "보내", "경비실", "아니요"), "해요체",
                    "네, 지금 바로 문자로 공동현관 비밀번호를 보내 드릴게요.", "Yes, I'll text you the lobby passcode right away.",
                    "Offer to send it via text (문자로 보내 드릴게요) or direct to 경비실."),
                ConversationTurnPrompt("int_dl_3", "Courier", "확인했습니다! 배송 완료되면 사진 찍어서 보내 드리겠습니다.",
                    "hwa-gi-nae-sseum-ni-da! bae-song wal-lyo-doe-myeon...", "Confirmed! I'll send you a photo once delivered.",
                    listOf("네", "감사", "수고", "고생", "조심"), "해요체",
                    "더운 날씨에 수고 많으십니다. 정말 감사합니다!", "Thank you for your hard work! Really appreciate it!",
                    "Thank the courier politely.")
            )
        ),

        // ==================== ADVANCED (10 SCENARIOS) ====================
        SpeakingScenario(
            id = "adv_interview", title = "Korean Tech Job Interview", titleKo = "한국 기업 면접",
            category = "Workplace", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Answer behavioral and role-fit questions in polished formal Korean (합니다체).",
            partnerRole = "Interviewer (면접관)",
            turns = listOf(
                ConversationTurnPrompt("adv_iv_1", "면접관", "안녕하십니까. 먼저 간단한 자기소개와 우리 회사에 지원하신 동기를 말씀해 주십시오.",
                    "", "Hello. Please give a brief self-introduction and your motivation for applying.",
                    listOf("지원", "경험", "역량", "기여", "관심", "성장", "입니다", "습니다"), "합니다체",
                    "안녕하십니까. 사용자 중심의 문제를 해결해 온 개발자 알렉스입니다. 귀사의 혁신적인 제품 철학에 깊이 공감하여 지원하게 되었습니다.",
                    "Hello. I am Alex, a user-focused developer. I applied because I deeply resonate with your company's innovative product philosophy.",
                    "Connect past experience (경험) to company contribution (기여) in 합니다체."),
                ConversationTurnPrompt("adv_iv_2", "면접관", "협업 과정에서 의견 충돌이 발생했을 때 어떻게 해결하신 경험이 있습니까?",
                    "", "Tell us about a time you resolved a disagreement during team collaboration.",
                    listOf("소통", "데이터", "경청", "기준", "타협", "해결", "결과", "협업"), "합니다체",
                    "먼저 상대방의 관점을 끝까지 경청한 뒤, 객관적인 사용자 데이터를 기준으로 장단점을 비교하여 모두가 납득할 수 있는 절충안을 도출했습니다.",
                    "I listened to the other perspective first, then compared trade-offs using objective user data to reach a consensus.",
                    "Use STAR structure: 경청 → 객관적 근거 → 해결 결과."),
                ConversationTurnPrompt("adv_iv_3", "면접관", "입사 후 1년 안에 달성하고 싶은 구체적인 목표가 있다면 무엇입니까?",
                    "", "What concrete goal would you like to achieve within your first year?",
                    listOf("목표", "품질", "효율", "개선", "기여", "성장", "하겠습니다"), "합니다체",
                    "빠르게 팀의 업무 흐름을 익혀 서비스 안정성을 높이고, 글로벌 사용자 경험을 개선하는 프로젝트에 핵심적으로 기여하겠습니다.",
                    "I will quickly learn the team's workflow, improve service stability, and contribute directly to global UX improvements.",
                    "State a measurable 1-year contribution ending in -겠습니다.")
            )
        ),
        SpeakingScenario(
            id = "adv_remote_work", title = "Debating Remote vs. Office Work", titleKo = "재택근무와 출근 근무 토론",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Articulate balanced arguments with connectives (-는 반면에, -뿐만 아니라).",
            partnerRole = "Panel Moderator",
            turns = listOf(
                ConversationTurnPrompt("adv_rw_1", "Moderator", "최근 하이브리드 근무 제도에 대한 논의가 활발합니다. 재택근무의 가장 큰 장점은 무엇이라고 보십니까?",
                    "", "Hybrid work is widely discussed. What is the biggest advantage of remote work?",
                    listOf("출퇴근", "시간", "절약", "집중", "효율", "유연", "장점", "생각합니다"), "합니다체",
                    "가장 큰 장점은 출퇴근 시간을 절약하여 업무 집중도와 일과 삶의 균형을 동시에 높일 수 있다는 점입니다.",
                    "The biggest advantage is saving commute time, boosting both focus and work-life balance.",
                    "Present a clear thesis using -다는 점입니다 or -기 때문입니다."),
                ConversationTurnPrompt("adv_rw_2", "Moderator", "반면에 대면 출근이 반드시 필요하다는 의견도 있습니다. 이에 대해서는 어떻게 생각하십니까?",
                    "", "On the other hand, some argue in-person work is essential. What is your view?",
                    listOf("소통", "협업", "유대감", "신입", "반면", "필요", "보완"), "합니다체",
                    "긴밀한 브레인스토밍이나 신입 사원의 빠른 적응에는 대면 소통이 확실히 유리한 측면이 있다고 생각합니다.",
                    "I agree face-to-face communication has clear advantages for close brainstorming and onboarding new hires.",
                    "Acknowledge the counterpoint using -는 측면이 있다."),
                ConversationTurnPrompt("adv_rw_3", "Moderator", "그렇다면 기업 입장에서 가장 이상적인 근무 정책은 무엇일까요?",
                    "", "What would be the ideal work policy from a company's perspective?",
                    listOf("하이브리드", "혼합", "직무", "특성", "유연", "신뢰", "성과"), "합니다체",
                    "직무 특성에 따라 주 2~3회 출근하는 유연한 하이브리드 모델을 도입하고, 근무 시간이 아닌 성과 중심으로 평가하는 것이 바람직합니다.",
                    "Adopting a flexible hybrid model tailored to role needs and evaluating by outcomes rather than hours is ideal.",
                    "Conclude with a policy recommendation using -는 것이 바람직합니다.")
            )
        ),
        SpeakingScenario(
            id = "adv_ai_education", title = "AI in Modern Education", titleKo = "인공지능과 미래 교육",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Analyze opportunities and ethical risks of AI learning tools in formal Korean.",
            partnerRole = "Professor",
            turns = listOf(
                ConversationTurnPrompt("adv_ai_1", "Professor", "교육 현장에서 인공지능 도구의 활용이 빠르게 확산되고 있습니다. 학습자에게 어떤 긍정적인 변화가 있을까요?",
                    "", "AI tools are spreading rapidly in education. What positive changes do they bring learners?",
                    listOf("맞춤형", "개인", "피드백", "효율", "접근성", "수준", "도움", "학습"), "합니다체",
                    "학습자 개개인의 수준과 취약점에 맞춘 맞춤형 피드백을 즉시 받을 수 있어 학습 효율이 크게 높아집니다.",
                    "Learners receive instant personalized feedback tailored to their level and weak points, greatly raising efficiency.",
                    "Use formal vocabulary: 맞춤형 학습, 즉각적 피드백, 효율성."),
                ConversationTurnPrompt("adv_ai_2", "Professor", "한편 지나친 의존에 대한 우려도 큽니다. 어떤 부작용을 경계해야 할까요?",
                    "", "Yet concerns about over-reliance are growing. What side effects must we guard against?",
                    listOf("비판적", "사고력", "저하", "의존", "정확성", "오류", "윤리", "우려"), "합니다체",
                    "스스로 문제를 분석하는 비판적 사고력이 약해질 수 있으며, 검증되지 않은 오류 정보를 무비판적으로 수용할 위험이 있습니다.",
                    "Critical thinking can weaken, and learners risk accepting unverified erroneous information uncritically.",
                    "Highlight cognitive or accuracy risks clearly."),
                ConversationTurnPrompt("adv_ai_3", "Professor", "앞으로 교사의 역할은 어떻게 변화해야 한다고 생각하십니까?",
                    "", "How should the role of teachers evolve going forward?",
                    listOf("지식", "전달", "멘토", "사고", "윤리", "소통", "역할", "안내"), "합니다체",
                    "단순한 지식 전달자를 넘어 학생의 창의적 사고와 협업 능력, 그리고 디지털 윤리를 길러 주는 멘토로 변화해야 합니다.",
                    "Teachers should evolve beyond knowledge transmitters into mentors fostering creativity, collaboration, and digital ethics.",
                    "Contrast old vs. new role using -을/를 넘어.")
            )
        ),
        SpeakingScenario(
            id = "adv_presentation", title = "Quarterly Project Briefing", titleKo = "분기 프로젝트 성과 보고",
            category = "Workplace", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Present metrics, root-cause analysis, and Q3 corrective roadmap to leadership.",
            partnerRole = "Executive Director (이사님)",
            turns = listOf(
                ConversationTurnPrompt("adv_pr_1", "이사님", "이번 분기 프로젝트 핵심 성과부터 간략히 브리핑해 주시겠습니까?",
                    "", "Could you briefly walk us through this quarter's key project outcomes?",
                    listOf("목표", "달성", "증가", "개선", "성과", "사용자", "지표", "습니다"), "합니다체",
                    "이번 분기에는 핵심 기능 개편을 통해 사용자 유지율을 18% 개선했으며, 주요 일정 역시 기한 내에 완료했습니다.",
                    "This quarter we improved retention by 18% via core redesign and delivered key milestones on time.",
                    "Summarize measurable outcomes concisely."),
                ConversationTurnPrompt("adv_pr_2", "이사님", "예상보다 진척이 늦었던 항목이 있다면 원인과 대응 방안을 설명해 주세요.",
                    "", "If any item lagged behind plan, explain the root cause and mitigation.",
                    listOf("원인", "지연", "검증", "개선", "보완", "일정", "리스크", "대응"), "합니다체",
                    "다국어 품질 검증 단계에서 예상치 못한 호환성 이슈가 발생했으나, 자동화 테스트를 도입하여 다음 분기에는 재발을 방지할 계획입니다.",
                    "Unexpected compatibility issues arose during localization QA, so we introduced automated tests to prevent recurrence.",
                    "Address cause honestly and present a concrete preventive measure."),
                ConversationTurnPrompt("adv_pr_3", "이사님", "다음 분기 우선순위는 어디에 두고 진행할 예정입니까?",
                    "", "Where will you place top priority next quarter?",
                    listOf("우선순위", "안정성", "성능", "확장", "고객", "집중", "계획입니다"), "합니다체",
                    "시스템 안정성 확보와 고객 피드백이 가장 많았던 핵심 사용성 개선에 역량을 집중할 계획입니다.",
                    "We plan to focus on system stability and top customer-requested usability improvements.",
                    "Close with a clear priority statement ending in -계획입니다.")
            )
        ),
        SpeakingScenario(
            id = "adv_environment", title = "Sustainable Urban Mobility", titleKo = "친환경 도시 교통 정책",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Discuss carbon neutrality, public transit incentives, and citizen participation.",
            partnerRole = "Policy Interviewer",
            turns = listOf(
                ConversationTurnPrompt("adv_en_1", "Interviewer", "대도시 대기오염을 줄이기 위해 대중교통 이용을 어떻게 활성화할 수 있을까요?",
                    "", "How can we boost public transit usage to cut urban air pollution?",
                    listOf("대중교통", "할인", "노선", "편의", "환승", "정책", "확대", "혜택"), "합니다체",
                    "정기권 요금 부담을 낮추고 출퇴근 시간대 배차 간격을 단축해 자가용보다 편리한 이동 경험을 제공해야 합니다.",
                    "We should lower pass costs and shorten peak headways so transit beats driving in convenience.",
                    "Propose actionable transit policies."),
                ConversationTurnPrompt("adv_en_2", "Interviewer", "시민들이 친환경 정책에 자발적으로 참여하게 만들려면 무엇이 중요할까요?",
                    "", "What matters most to encourage voluntary citizen participation in green policies?",
                    listOf("참여", "인식", "혜택", "실천", "보상", "투명", "효과", "중요"), "합니다체",
                    "단순한 규제보다 실천에 따른 실질적 혜택을 제공하고, 시민 참여가 가져온 환경 개선 효과를 투명하게 공유하는 것이 중요합니다.",
                    "Offering tangible incentives rather than only restrictions and transparently sharing impact are key.",
                    "Contrast regulation vs. positive incentives."),
                ConversationTurnPrompt("adv_en_3", "Interviewer", "개인 차원에서 실천할 수 있는 가장 효과적인 습관은 무엇입니까?",
                    "", "What is the most effective habit individuals can practice?",
                    listOf("일회용", "텀블러", "대중교통", "분리배출", "에너지", "절약", "습관"), "합니다체",
                    "가까운 거리는 도보나 자전거를 이용하고, 다회용 컵 사용과 올바른 분리배출을 일상화하는 작은 습관이 큰 변화를 만듭니다.",
                    "Walking or cycling short distances and making reusable cups and proper recycling daily habits create big change.",
                    "Connect daily actions to macro impact.")
            )
        ),
        SpeakingScenario(
            id = "adv_culture_diff", title = "Navigating Korean Workplace Culture", titleKo = "한국 직장 문화와 소통",
            category = "Workplace", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Reflect thoughtfully on hierarchy, consensus building, and cross-cultural communication.",
            partnerRole = "HR Mentor",
            turns = listOf(
                ConversationTurnPrompt("adv_cu_1", "HR Mentor", "한국 직장에서 근무하면서 가장 인상 깊었거나 적응이 필요했던 문화는 무엇인가요?",
                    "", "What aspect of Korean workplace culture stood out or took adjustment?",
                    listOf("호칭", "존댓말", "보고", "회식", "배려", "빠른", "속도", "문화"), "합니다체",
                    "직급에 따른 호칭과 정중한 보고 문화가 처음에는 낯설었지만, 상호 존중과 빠른 협업을 돕는 방식이라는 점을 이해하게 되었습니다.",
                    "Titles and formal reporting felt unfamiliar at first, yet I came to see how they support respect and fast teamwork.",
                    "Reflect respectfully with balanced cultural insight."),
                ConversationTurnPrompt("adv_cu_2", "HR Mentor", "문화적 차이로 오해가 생길 때 외국인 직장인으로서 어떻게 대처하는 것이 좋을까요?",
                    "", "How should international professionals handle misunderstandings caused by cultural differences?",
                    listOf("질문", "확인", "솔직", "존중", "대화", "이해", "소통"), "합니다체",
                    "혼자 추측하기보다 정중하게 맥락을 여쭤보고, 서로의 업무 기대치를 명확히 확인하는 열린 소통이 가장 효과적입니다.",
                    "Rather than assuming, politely asking for context and clarifying expectations openly works best.",
                    "Emphasize proactive clarification and respect."),
                ConversationTurnPrompt("adv_cu_3", "HR Mentor", "다국적 팀이 시너지를 내기 위해 필요한 핵심 가치는 무엇이라고 보십니까?",
                    "", "What core value helps multicultural teams create synergy?",
                    listOf("다양성", "존중", "신뢰", "포용", "공유", "가치", "협력"), "합니다체",
                    "서로의 차이를 약점이 아닌 강점으로 바라보는 포용력과 명확한 공통 목표 공유가 시너지의 핵심이라고 생각합니다.",
                    "Inclusivity that views differences as strengths, paired with shared goals, is the core of synergy.",
                    "Articulate a clear leadership principle.")
            )
        ),
        SpeakingScenario(
            id = "adv_negotiation", title = "Vendor Contract Negotiation", titleKo = "협력사 계약 조건 협의",
            category = "Workplace", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Negotiate delivery timelines and quality guarantees diplomatically in formal Korean.",
            partnerRole = "Partner Representative",
            turns = listOf(
                ConversationTurnPrompt("adv_ng_1", "Partner", "제안하신 납품 단가는 현재 원자재 비용 상승을 고려할 때 다소 빠듯합니다. 조정 가능할까요?",
                    "", "Your proposed unit price is tight given rising material costs. Can we adjust?",
                    listOf("단가", "물량", "장기", "계약", "검토", "조건", "절충", "제안"), "합니다체",
                    "원자재 부담은 충분히 이해합니다. 장기 계약을 전제로 발주 물량을 확대하는 대신 단가를 합리적인 선에서 유지하는 방안을 제안드립니다.",
                    "We understand material pressures. We propose increasing order volume under a long-term contract to keep unit pricing reasonable.",
                    "Acknowledge their concern, then propose a win-win trade-off."),
                ConversationTurnPrompt("adv_ng_2", "Partner", "물량 보장이 된다면 검토해 보겠습니다. 다만 납기 일정은 최소 3주가 필요합니다.",
                    "", "If volume is guaranteed we'll review it, though we need at least 3 weeks lead time.",
                    listOf("납기", "일정", "분할", "초도", "물량", "품질", "협의"), "합니다체",
                    "전체 납기는 3주로 맞추되, 긴급 수요 대응을 위해 초도 물량 30%만 2주 안에 선발송해 주실 수 있을까요?",
                    "We can agree to 3 weeks overall if 30% of the initial batch can ship within 2 weeks for urgent demand.",
                    "Suggest a phased delivery compromise."),
                ConversationTurnPrompt("adv_ng_3", "Partner", "초도 물량 분할 배송은 가능할 것 같습니다. 최종 합의안을 문서로 정리할까요?",
                    "", "Phased initial shipment looks doable. Shall we document the final agreement?",
                    listOf("네", "합의", "계약서", "검토", "송부", "감사", "협력"), "합니다체",
                    "네, 오늘 논의된 조건을 반영한 수정 계약서 초안을 내일 오전까지 송부드리겠습니다. 원활한 협력 감사드립니다.",
                    "Yes, we will send the revised draft contract reflecting today's terms by tomorrow morning. Thank you.",
                    "Confirm next steps formally.")
            )
        ),
        SpeakingScenario(
            id = "adv_media_literacy", title = "Digital Media & Fact-Checking", titleKo = "디지털 정보와 팩트체크",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Discuss misinformation on short-form platforms and responsible media habits.",
            partnerRole = "Journalist",
            turns = listOf(
                ConversationTurnPrompt("adv_md_1", "Journalist", "짧은 영상 플랫폼을 통해 뉴스를 소비하는 경향이 늘고 있습니다. 어떤 장단점이 있다고 보십니까?",
                    "", "Consuming news via short-form video is rising. What pros and cons do you see?",
                    listOf("빠르", "접근", "요약", "맥락", "왜곡", "자극", "장점", "단점"), "합니다체",
                    "핵심 정보를 빠르게 파악할 수 있다는 장점이 있는 반면, 맥락이 생략되거나 자극적인 내용으로 왜곡될 우려가 큽니다.",
                    "It lets people grasp headlines quickly, yet carries high risk of missing context or sensational distortion.",
                    "Balance 장점 and 단점 using -는 반면."),
                ConversationTurnPrompt("adv_md_2", "Journalist", "허위 정보에 현혹되지 않기 위해 개인은 어떤 검증 습관을 가져야 할까요?",
                    "", "What verification habits should individuals build to avoid misinformation?",
                    listOf("출처", "공식", "교차", "검증", "근거", "확인", "비판"), "합니다체",
                    "정보의 출처와 작성 시점을 반드시 확인하고, 최소 두 개 이상의 공신력 있는 매체를 통해 교차 검증하는 습관이 필요합니다.",
                    "Checking the source and date and cross-verifying across at least two reputable outlets is essential.",
                    "Mention 출처 확인 and 교차 검증."),
                ConversationTurnPrompt("adv_md_3", "Journalist", "플랫폼 기업에는 어떤 사회적 책임이 요구된다고 생각하십니까?",
                    "", "What social responsibility should platform companies bear?",
                    listOf("알고리즘", "투명", "표시", "책임", "신고", "보호", "기준"), "합니다체",
                    "추천 알고리즘의 투명성을 높이고, 검증되지 않은 정보나 합성 콘텐츠에는 명확한 출처 표시를 의무화해야 합니다.",
                    "Platforms must raise algorithm transparency and mandate clear labeling on unverified or synthetic content.",
                    "Frame clear institutional responsibility.")
            )
        ),
        SpeakingScenario(
            id = "adv_aging_society", title = "Demographic Shifts & Aging Society", titleKo = "고령화 사회와 세대 공존",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Discuss Korea's aging population, lifelong learning, and intergenerational empathy.",
            partnerRole = "Sociology Host",
            turns = listOf(
                ConversationTurnPrompt("adv_ag_1", "Host", "한국은 세계에서 가장 빠르게 고령화가 진행되는 국가 중 하나입니다. 가장 시급한 과제는 무엇일까요?",
                    "", "Korea is aging faster than almost any country. What is the most urgent challenge?",
                    listOf("돌봄", "연금", "일자리", "복지", "경제", "고령", "부담", "과제"), "합니다체",
                    "노년층의 경제적 자립을 돕는 양질의 일자리 확대와 지속가능한 돌봄·연금 체계 구축이 가장 시급하다고 생각합니다.",
                    "Expanding quality senior jobs for financial independence and building sustainable care and pension systems are most urgent.",
                    "Identify concrete socio-economic priorities."),
                ConversationTurnPrompt("adv_ag_2", "Host", "디지털 전환 속에서 고령층의 소외 문제를 해결할 방안이 있을까요?",
                    "", "How can we address digital exclusion among older adults?",
                    listOf("교육", "키오스크", "접근성", "쉬운", "인터페이스", "지원", "배려"), "합니다체",
                    "지역 주민센터 중심의 실습 교육을 확대하는 동시에, 키오스크와 앱 설계 단계부터 고령자 친화적 인터페이스를 의무화해야 합니다.",
                    "Expanding hands-on community training while mandating senior-friendly kiosk and app interfaces from design stage is key.",
                    "Combine education and accessible UX design."),
                ConversationTurnPrompt("adv_ag_3", "Host", "청년 세대와 노년 세대가 서로 이해하고 공존하기 위해 필요한 노력은 무엇입니까?",
                    "", "What helps younger and older generations coexist with mutual understanding?",
                    listOf("세대", "소통", "경험", "존중", "교류", "공존", "협력"), "합니다체",
                    "서로를 부담으로 보기보다 경험을 나누는 동반자로 인식하고, 지역 사회와 직장에서 세대 간 교류 기회를 늘려야 합니다.",
                    "Viewing each other as partners sharing experience rather than burdens and increasing intergenerational exchange is vital.",
                    "Close with a constructive social vision.")
            )
        ),
        SpeakingScenario(
            id = "adv_korean_wave", title = "Global Impact of Korean Culture", titleKo = "한류와 글로벌 문화 교류",
            category = "TOPIK Speaking", difficulty = SpeakingDifficulty.ADVANCED,
            settingDescription = "Analyze why Korean cinema, music, and language resonate globally and how to sustain exchange.",
            partnerRole = "Cultural Critic",
            turns = listOf(
                ConversationTurnPrompt("adv_kw_1", "Critic", "K-팝과 한국 드라마가 전 세계적으로 큰 사랑을 받는 근본적인 이유는 무엇이라고 보십니까?",
                    "", "What is the underlying reason K-pop and Korean dramas are loved worldwide?",
                    listOf("서사", "공감", "완성도", "감정", "소통", "디지털", "품질", "매력"), "합니다체",
                    "보편적인 인간의 감정을 섬세하게 담아낸 서사와 높은 제작 완성도, 그리고 팬들과 적극적으로 소통하는 문화가 어우러진 결과라고 봅니다.",
                    "It stems from stories capturing universal human emotions, high production craft, and active fan engagement.",
                    "Analyze multiple contributing factors cleanly."),
                ConversationTurnPrompt("adv_kw_2", "Critic", "한류가 일시적인 유행을 넘어 오래 지속되려면 어떤 점이 중요할까요?",
                    "", "What is needed for the Korean Wave to last beyond a passing trend?",
                    listOf("다양성", "창작", "상호", "교류", "현지", "존중", "지속"), "합니다체",
                    "일방적인 확산보다 현지 문화와 상호 교류하고, 다양한 장르와 신진 창작자들이 자유롭게 실험할 수 있는 생태계를 지켜야 합니다.",
                    "Fostering two-way exchange with local cultures and protecting an ecosystem where diverse creators can experiment is crucial.",
                    "Emphasize mutual exchange (상호 교류) and diversity."),
                ConversationTurnPrompt("adv_kw_3", "Critic", "언어 학습이 문화 이해에 미치는 역할에 대해 한 말씀 부탁드립니다.",
                    "", "Please share your thoughts on how language learning shapes cultural understanding.",
                    listOf("언어", "문화", "뉘앙스", "사고방식", "이해", "존중", "깊이"), "합니다체",
                    "언어에는 그 사회의 사고방식과 정서가 담겨 있기 때문에, 한국어를 배우는 과정 자체가 한국 문화를 깊이 존중하고 이해하는 가장 진정한 통로가 됩니다.",
                    "Because language holds a society's mindset and emotions, learning Korean is the truest gateway to understanding Korean culture deeply.",
                    "Tie language learning back to cultural empathy.")
            )
        )
    )
}
