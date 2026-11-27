package com.example.data.seed

/**
 * Authentic HRD Korea EPS-TOPIK (40 Qs: 20 Reading + 20 Listening × 5점 = 200점)
 * and NIIED TOPIK Speaking IBT (6 Official Tasks = 200점, 2 variants each).
 */
object EpsAndSpeakingMockBank {

    private fun epsReading(
        id: String,
        typeKo: String,
        instructionKo: String,
        stimulusKo: String,
        options: List<String>,
        correctIdx: Int,
        tier: Int,
        explanationEn: String,
        explanationId: String,
        passageBoxKo: String? = null,
        underlinedText: String? = null
    ) = ExamQuestion(
        id = id,
        examType = ExamType.EPS_TOPIK,
        section = "Reading",
        period = 1,
        questionTypeKo = typeKo,
        instructionKo = instructionKo,
        passageBoxKo = passageBoxKo,
        stimulusKo = stimulusKo,
        underlinedText = underlinedText,
        options = options,
        correctOptionIndex = correctIdx,
        pointWeight = 5,
        difficultyTier = tier,
        explanationEn = explanationEn,
        explanationId = explanationId
    )

    private fun epsListening(
        id: String,
        typeKo: String,
        instructionKo: String,
        stimulusKo: String,
        audioKo: String,
        options: List<String>,
        correctIdx: Int,
        tier: Int,
        explanationEn: String,
        explanationId: String
    ) = ExamQuestion(
        id = id,
        examType = ExamType.EPS_TOPIK,
        section = "Listening",
        period = 1,
        questionTypeKo = typeKo,
        instructionKo = instructionKo,
        stimulusKo = stimulusKo,
        audioTranscriptKo = audioKo,
        maxAudioPlaysInMock = 2,
        options = options,
        correctOptionIndex = correctIdx,
        pointWeight = 5,
        difficultyTier = tier,
        explanationEn = explanationEn,
        explanationId = explanationId
    )

    val epsReadingQuestions: List<ExamQuestion> = listOf(
        // [1~5] 안전 표지판 & 보호구
        epsReading(
            id = "eps_r01", typeKo = "안전 표지판",
            instructionKo = "다음 안전 표지 문구의 의미로 알맞은 것을 고르십시오.",
            passageBoxKo = "[ 주의: 고압 전류 위험 — 관계자 외 출입 금지 ]",
            stimulusKo = "이 표지판이 뜻하는 내용으로 알맞은 것은 무엇입니까?",
            options = listOf("허가된 사람만 들어갈 수 있습니다.", "누구나 자유롭게 휴식할 수 있습니다.", "여기서 음식을 먹을 수 있습니다.", "안전화 없이 들어가도 됩니다."),
            correctIdx = 0, tier = 1,
            explanationEn = "'관계자 외 출입 금지' means entry is prohibited except for authorized personnel.",
            explanationId = "'관계자 외 출입 금지' berarti dilarang masuk selain petugas yang berwenang."
        ),
        epsReading(
            id = "eps_r02", typeKo = "보호구 어휘",
            instructionKo = "빈칸에 들어갈 가장 알맞은 말을 고르십시오.",
            stimulusKo = "소음이 심한 작업장에서는 청력을 보호하기 위해 꼭 ( ____ )을/를 착용해야 합니다.",
            options = listOf("귀마개", "앞치마", "우산", "지갑"),
            correctIdx = 0, tier = 1,
            explanationEn = "Earplugs (귀마개) protect hearing (청력) in loud workplaces.",
            explanationId = "Penyumbat telinga (귀마개) wajib dipakai untuk melindungi pendengaran di tempat kerja bising."
        ),
        epsReading(
            id = "eps_r03", typeKo = "안전 표지판",
            instructionKo = "다음 안전 표지 문구의 의미로 알맞은 것을 고르십시오.",
            passageBoxKo = "[ 화기 엄금 — 인화성 물질 보관 장소 ]",
            stimulusKo = "이 장소에서 지켜야 할 안전 수칙은 무엇입니까?",
            options = listOf("담배를 피우거나 불을 사용하면 안 됩니다.", "기계에 기름을 많이 발라야 합니다.", "물을 계속 뿌려 놓아야 합니다.", "큰 소리로 대화하면 안 됩니다."),
            correctIdx = 0, tier = 1,
            explanationEn = "'화기 엄금' strictly forbids fire or smoking near flammable materials (인화성 물질).",
            explanationId = "'화기 엄금' melarang keras menyalakan api atau merokok di dekat bahan mudah terbakar."
        ),
        epsReading(
            id = "eps_r04", typeKo = "보호구 어휘",
            instructionKo = "빈칸에 들어갈 가장 알맞은 말을 고르십시오.",
            stimulusKo = "어선에 승선하여 작업할 때는 파도에 대비해 반드시 ( ____ )를 입고 작업해야 합니다.",
            options = listOf("구명조끼", "넥타이", "운동복", "가방"),
            correctIdx = 0, tier = 1,
            explanationEn = "A life jacket (구명조끼) is mandatory when working aboard a fishing vessel (어선).",
            explanationId = "Jaket pelampung (구명조끼) wajib dipakai saat bekerja di atas kapal nelayan."
        ),
        epsReading(
            id = "eps_r05", typeKo = "건설 현장 안전",
            instructionKo = "빈칸에 들어갈 가장 알맞은 말을 고르십시오.",
            stimulusKo = "높은 곳에서 작업할 때는 떨어지는 사고를 막기 위해 꼭 ( ____ )를 매야 합니다.",
            options = listOf("안전대", "손수건", "허리띠 장식", "장화"),
            correctIdx = 0, tier = 1,
            explanationEn = "A safety harness/belt (안전대) prevents fall accidents when working at height.",
            explanationId = "Sabuk pengaman kerja ketinggian (안전대) wajib dipasang untuk mencegah kecelakaan jatuh."
        ),
        // [6~10] 직장 어휘 & 문법 빈칸
        epsReading(
            id = "eps_r06", typeKo = "작업 보고 절차",
            instructionKo = "빈칸에 들어갈 가장 알맞은 표현을 고르십시오.",
            stimulusKo = "기계에 이상이 생기면 임의로 고치지 말고 즉시 반장님께 ( ____ ).",
            options = listOf("보고해야 합니다", "퇴근해야 합니다", "구입해야 합니다", "포장해야 합니다"),
            correctIdx = 0, tier = 1,
            explanationEn = "If a machine malfunctions, do not repair it on your own — report immediately ('보고해야 합니다') to the team leader.",
            explanationId = "Jika mesin bermasalah, jangan memperbaiki sendiri — segera laporkan ('보고해야 합니다') kepada mandor."
        ),
        epsReading(
            id = "eps_r07", typeKo = "공구·설비 어휘",
            instructionKo = "빈칸에 들어갈 가장 알맞은 말을 고르십시오.",
            stimulusKo = "무거운 상자를 창고로 옮길 때는 허리를 다치지 않도록 ( ____ )을/를 사용하십시오.",
            options = listOf("손수레", "지우개", "칫솔", "달력"),
            correctIdx = 0, tier = 1,
            explanationEn = "Use a handcart (손수레) when moving heavy boxes to avoid back injury.",
            explanationId = "Gunakan troli dorong (손수레) saat memindahkan kardus berat agar punggung tidak cedera."
        ),
        epsReading(
            id = "eps_r08", typeKo = "근무 규정 문법",
            instructionKo = "빈칸에 들어갈 가장 알맞은 것을 고르십시오.",
            stimulusKo = "기계가 작동하는 중에는 위험하므로 절대로 손을 ( ____ ).",
            options = listOf("넣으면 안 됩니다", "넣어도 됩니다", "넣고 싶습니다", "넣을 수 있습니다"),
            correctIdx = 0, tier = 2,
            explanationEn = "Never put your hand in while a machine is running: prohibition -(으)면 안 됩니다.",
            explanationId = "Saat mesin berjalan, dilarang keras memasukkan tangan: bentuk larangan -(으)면 안 됩니다."
        ),
        epsReading(
            id = "eps_r09", typeKo = "작업 마무리",
            instructionKo = "빈칸에 들어갈 가장 알맞은 말을 고르십시오.",
            stimulusKo = "퇴근하기 전에는 사용한 공구를 제자리에 ( ____ ) 작업장 바닥을 깨끗이 청소해야 합니다.",
            options = listOf("정리하고", "버리고", "숨기고", "부수고"),
            correctIdx = 0, tier = 2,
            explanationEn = "Before leaving work, organize tools back in their place ('정리하고') and clean the floor.",
            explanationId = "Sebelum pulang kerja, rapikan alat ke tempat semula ('정리하고') dan bersihkan lantai."
        ),
        epsReading(
            id = "eps_r10", typeKo = "밑줄 친 어휘 유의어",
            instructionKo = "밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
            stimulusKo = "오늘 주문 물량이 많아서 평소보다 작업 속도를 조금 서둘러야 합니다.",
            underlinedText = "서둘러야 합니다",
            options = listOf("빨리 해야 합니다", "천천히 해야 합니다", "중단해야 합니다", "미루어야 합니다"),
            correctIdx = 0, tier = 2,
            explanationEn = "'서두르다' ('to hurry') matches '빨리 하다' ('to do quickly').",
            explanationId = "'서두르다' ('terburu-buru / mempercepat') bermakna sama dengan '빨리 하다'."
        ),
        // [11~15] 공지문 / 기숙사 / 급여 / 근로계약서
        epsReading(
            id = "eps_r11", typeKo = "기숙사 공지 이해",
            instructionKo = "다음 안내문을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[ 기숙사 생활 수칙 안내 ]\n• 밤 11시 이후에는 세탁기 사용 및 큰 소리 대화 자제\n• 외출 시 난방기, 선풍기, 전등 전원 반드시 차단\n• 외부인은 관리인 허가 없이 기숙사 출입 불가",
            stimulusKo = "안내문의 내용과 같은 것을 고르십시오.",
            options = listOf("외출할 때 난방기와 전등을 꺼야 합니다.", "자정에 세탁기를 사용하는 것이 좋습니다.", "기숙사에서는 외출이 금지되어 있습니다.", "외부인은 허가 없이 자유롭게 들어올 수 있습니다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Residents must turn off heaters, fans, and lights when leaving ('전원 반드시 차단').",
            explanationId = "Penghuni wajib mematikan pemanas, kipas, dan lampu saat keluar asrama."
        ),
        epsReading(
            id = "eps_r12", typeKo = "급여명세서 이해",
            instructionKo = "다음 급여명세서를 보고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[ 5월 급여명세서 — 성명: 아디 ]\n• 기본급: 2,100,000원\n• 연장근로 수당: 350,000원\n• 공제 합계(세금·보험료): 220,000원\n• 실수령액: 2,230,000원\n• 지급일: 매월 25일 (계좌 이체)",
            stimulusKo = "급여명세서 내용과 일치하는 것을 고르십시오.",
            options = listOf("세금과 보험료를 빼고 실제로 받는 돈은 2,230,000원입니다.", "월급은 매월 1일에 현금으로 받습니다.", "연장근로 수당은 지급되지 않았습니다.", "기본급은 350,000원입니다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Net take-home pay ('실수령액') after deductions ('공제 합계') is 2,230,000 won.",
            explanationId = "Gaji bersih yang diterima ('실수령액') setelah potongan pajak & asuransi adalah 2.230.000 won."
        ),
        epsReading(
            id = "eps_r13", typeKo = "공장 휴무 공지",
            instructionKo = "다음 공지문을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "[ 여름 정기 휴가 안내 ]\n• 휴가 기간: 8월 3일(월) ~ 8월 7일(금)\n• 정상 출근일: 8월 10일(월) 오전 8시 30분\n• 비상 연락처: 생산관리팀 김 과장 (010-2345-6789)\n※ 휴가 출발 전 작업장 창문과 가스 밸브를 꼭 잠그십시오.",
            stimulusKo = "공지문의 내용과 일치하는 것을 고르십시오.",
            options = listOf("직원들은 8월 10일 월요일에 출근합니다.", "휴가는 8월 7일에 시작됩니다.", "휴가 동안 공장 창문을 열어 두어야 합니다.", "정상 출근 시간은 오전 10시입니다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Normal return to work ('정상 출근일') is Monday, August 10 at 8:30 AM.",
            explanationId = "Hari masuk kerja normal adalah Senin, 10 Agustus pukul 08.30."
        ),
        epsReading(
            id = "eps_r14", typeKo = "출입국·체류 안내",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "외국인 근로자가 한국에서 체류 기간을 연장하려면 기간이 만료되기 4개월 전부터 만료일 전까지 출입국·외국인사무소에 신청해야 합니다. 만약 정해진 기한을 넘겨 신청하면 범칙금을 내야 합니다.",
            stimulusKo = "이 글의 내용과 같은 것을 고르십시오.",
            options = listOf("체류 기간 만료 전에 미리 연장 신청을 해야 합니다.", "체류 기간이 지난 뒤에 신청해도 벌금이 없습니다.", "체류 기간 연장은 우체국에서 신청합니다.", "만료 1년 전부터 연장 신청을 해야 합니다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Extension must be applied before the expiration date ('만료일 전까지'); applying late incurs a fine ('범칙금').",
            explanationId = "Perpanjangan izin tinggal harus diajukan sebelum tanggal habis berlaku agar tidak terkena denda ('범칙금')."
        ),
        epsReading(
            id = "eps_r15", typeKo = "농업·축산업 안전",
            instructionKo = "다음 글을 읽고 빈칸에 들어갈 가장 알맞은 것을 고르십시오.",
            passageBoxKo = "여름철 비닐하우스 안은 온도와 습도가 매우 높아 열사병에 걸리기 쉽습니다. 따라서 한낮에는 무리하게 작업하지 말고 그늘에서 자주 휴식을 취하며 ( ____ )을/를 충분히 마셔야 합니다.",
            stimulusKo = "빈칸에 들어갈 가장 알맞은 것을 고르십시오.",
            options = listOf("물과 이온 음료", "뜨거운 국물", "탄산과 술", "기계 윤활유"),
            correctIdx = 0, tier = 2,
            explanationEn = "To prevent heatstroke in greenhouses, rest in the shade and drink plenty of water and electrolyte drinks ('물과 이온 음료').",
            explanationId = "Untuk mencegah heatstroke di rumah kaca, istirahat di tempat teduh dan minum cukup air serta minuman elektrolit."
        ),
        // [16~20] 직장 예절·안전·재해 예방 독해
        epsReading(
            id = "eps_r16", typeKo = "산업재해 예방",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "용접 작업을 할 때는 불꽃이 튀어 화상을 입거나 화재가 날 수 있습니다. 작업 주변에 종이나 기름 같은 인화성 물질을 치워 두고, 소화기를 가까운 곳에 준비한 뒤 보안면과 가죽 장갑을 착용해야 합니다.",
            stimulusKo = "용접 작업 전 안전 수칙으로 올바른 것을 고르십시오.",
            options = listOf("주변의 인화성 물질을 치우고 소화기를 가까이에 둡니다.", "불꽃이 잘 보이도록 보안면을 벗고 작업합니다.", "면장갑만 끼고 작업해도 안전합니다.", "작업장 옆에 종이 상자를 쌓아 둡니다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Clear flammable items around welding areas, keep a fire extinguisher nearby, and wear a shield and leather gloves.",
            explanationId = "Singkirkan bahan mudah terbakar, siapkan APAR di dekat area las, dan pakai topeng las serta sarung tangan kulit."
        ),
        epsReading(
            id = "eps_r17", typeKo = "응급 처치",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "작업 중에 뜨거운 물이나 기계에 데어 화상을 입었을 때는 먼저 깨끗하고 시원한 흐르는 물에 15분 이상 화상 부위를 식혀야 합니다. 물집이 생겼을 때 손으로 터뜨리면 세균에 감염될 수 있으므로 그대로 두고 병원에 가야 합니다.",
            stimulusKo = "화상을 입었을 때의 올바른 대처 방법은 무엇입니까?",
            options = listOf("시원한 흐르는 물에 식히고 물집은 터뜨리지 않습니다.", "물집을 바늘로 바로 터뜨려 물을 뺍니다.", "얼음을 피부에 직접 오래 대고 있습니다.", "아무 조치 없이 작업을 계속합니다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Cool the burn under clean running cool water for 15+ minutes and never pop blisters due to infection risk.",
            explanationId = "Dinginkan luka bakar di bawah air mengalir bersih ≥15 menit dan jangan memecahkan lepuhan karena berisiko infeksi."
        ),
        epsReading(
            id = "eps_r18", typeKo = "근로 기준",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "한국의 표준근로계약서에는 근무 장소, 업무 내용, 근로 시간, 휴게 시간, 임금 및 지급 날짜가 적혀 있습니다. 근로자는 서명하기 전에 계약서의 내용을 꼼꼼히 확인하고, 사본 한 부를 꼭 보관해야 합니다.",
            stimulusKo = "표준근로계약서에 대한 설명으로 맞는 것을 고르십시오.",
            options = listOf("서명 전에 내용을 확인하고 사본을 보관해야 합니다.", "임금과 근로 시간은 계약서에 적지 않습니다.", "근로자는 계약서를 읽지 않고 서명해야 합니다.", "계약서는 회사만 보관하고 근로자는 볼 수 없습니다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Workers should review every clause carefully before signing and keep one copy ('사본 한 부를 꼭 보관').",
            explanationId = "Pekerja harus memeriksa isi kontrak kerja secara teliti sebelum menandatangani dan menyimpan satu salinan."
        ),
        epsReading(
            id = "eps_r19", typeKo = "화학물질 취급",
            instructionKo = "다음 글을 읽고 내용과 같은 것을 고르십시오.",
            passageBoxKo = "세척제나 페인트 같은 유해 화학물질을 사용할 때는 반드시 환풍기를 켜서 실내 공기를 환기해야 합니다. 또한 피부나 눈에 화학물질이 닿았을 때는 즉시 깨끗한 물로 씻어 내고 관리자에게 알려야 합니다.",
            stimulusKo = "화학물질 취급 수칙으로 알맞은 것을 고르십시오.",
            options = listOf("환풍기를 켜서 환기하고 피부에 닿으면 즉시 물로 씻습니다.", "냄새가 나지 않도록 창문과 환풍기를 모두 닫습니다.", "눈에 들어가면 손으로 세게 비빈 뒤 참습니다.", "방독 마스크는 사용하지 않아도 됩니다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Run exhaust fans for ventilation and immediately rinse with clean water if chemicals contact skin or eyes.",
            explanationId = "Nyalakan kipas ventilasi saat memakai bahan kimia dan segera bilas dengan air bersih jika terkena kulit/mata."
        ),
        epsReading(
            id = "eps_r20", typeKo = "직장 내 소통",
            instructionKo = "다음 글을 읽고 중심 생각으로 알맞은 것을 고르십시오.",
            passageBoxKo = "공장이나 건설 현장에서 일하다 보면 작업 지시 내용을 정확히 이해하지 못할 때가 있습니다. 이때 아는 척하고 그대로 작업하면 불량품이 생기거나 큰 안전사고로 이어질 수 있습니다. 이해가 안 되는 부분은 즉시 다시 물어보고 확인하는 것이 가장 안전한 방법입니다.",
            stimulusKo = "이 글의 중심 생각은 무엇입니까?",
            options = listOf("작업 지시가 불명확하면 바로 다시 질문해 확인해야 한다.", "모르는 내용이 있어도 질문하지 말고 혼자 해결해야 한다.", "작업 속도만 빠르면 안전 확인은 생략해도 된다.", "반장님에게 질문하는 것은 예의에 어긋난다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Pretending to understand leads to defects or serious accidents; asking again to confirm is safest.",
            explanationId = "Jika instruksi kerja kurang jelas, segera bertanya kembali untuk memastikan demi mencegah produk cacat dan kecelakaan kerja."
        )
    )

    val epsListeningQuestions: List<ExamQuestion> = listOf(
        // [21~26] 작업 지시 & 안전 방송
        epsListening(
            id = "eps_l01", typeKo = "작업 전 지시",
            instructionKo = "다음을 듣고 작업 전에 꼭 해야 할 일을 고르십시오.",
            stimulusKo = "반장님의 지시 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 오늘 용접 작업이 있으니까 시작하기 전에 반드시 보안면과 안전장갑을 착용하십시오.",
            options = listOf("보안면과 안전장갑을 착용한다.", "기계 전원을 켜고 자리를 비운다.", "작업장 안에서 식사를 한다.", "안전모를 벗고 작업한다."),
            correctIdx = 0, tier = 1,
            explanationEn = "The supervisor instructs wearing a welding shield (보안면) and safety gloves (안전장갑) before starting.",
            explanationId = "Mandor memerintahkan memakai topeng las (보안면) dan sarung tangan pengaman (안전장갑) sebelum mulai."
        ),
        epsListening(
            id = "eps_l02", typeKo = "직장 질문 대답",
            instructionKo = "다음을 듣고 질문에 가장 알맞은 대답을 고르십시오.",
            stimulusKo = "질문에 대한 가장 적절한 직장 내 대답을 고르십시오.",
            audioKo = "여자: 이 상자들 창고로 다 옮겼어요?",
            options = listOf("네, 방금 다 정리해 놓았습니다.", "어제 기숙사에 도착했습니다.", "월급날은 매달 이십오 일입니다.", "지하철 이호선을 타십시오."),
            correctIdx = 0, tier = 1,
            explanationEn = "'네, 방금 다 정리해 놓았습니다' politely confirms the boxes were moved and arranged.",
            explanationId = "'네, 방금 다 정리해 놓았습니다' menjawab dengan sopan bahwa kotak-kotak sudah dipindahkan dan dirapikan."
        ),
        epsListening(
            id = "eps_l03", typeKo = "비상 대처",
            instructionKo = "다음을 듣고 비상시 행동으로 맞는 것을 고르십시오.",
            stimulusKo = "화재가 발생했을 때 해야 할 행동은 무엇입니까?",
            audioKo = "남자: 작업장에서 불이 나면 당황하지 말고 비상벨을 누른 뒤 소화기나 비상구를 확인하고 즉시 대피하십시오.",
            options = listOf("비상벨을 누르고 즉시 대피한다.", "기계를 계속 작동시킨다.", "창문을 모두 닫고 기다린다.", "엘리베이터를 타고 내려간다."),
            correctIdx = 0, tier = 1,
            explanationEn = "Press the emergency alarm (비상벨), check the extinguisher/exit, and evacuate immediately.",
            explanationId = "Tekan bel darurat (비상벨) lalu segera evakuasi melalui pintu darurat."
        ),
        epsListening(
            id = "eps_l04", typeKo = "근무 시간 확인",
            instructionKo = "다음을 듣고 들은 내용과 같은 것을 고르십시오.",
            stimulusKo = "오늘 점심시간은 몇 시부터입니까?",
            audioKo = "여자: 여러분, 오늘은 물건 납품 일정 때문에 점심시간이 평소보다 삼십 분 늦어집니다. 열두 시 반부터 한 시 반까지 식사하시면 됩니다.",
            options = listOf("열두 시부터 한 시까지", "열두 시 반부터 한 시 반까지", "열한 시 반부터 열두 시 반까지", "한 시부터 두 시까지"),
            correctIdx = 1, tier = 1,
            explanationEn = "Lunch is shifted 30 minutes later today: 12:30 to 1:30 ('열두 시 반부터 한 시 반까지').",
            explanationId = "Jam makan siang hari ini bergeser 30 menit: pukul 12.30 sampai 13.30."
        ),
        epsListening(
            id = "eps_l05", typeKo = "직장 질문 대답",
            instructionKo = "다음을 듣고 질문에 가장 알맞은 대답을 고르십시오.",
            stimulusKo = "질문에 대한 가장 적절한 대답을 고르십시오.",
            audioKo = "남자: 반장님, 이 기계 전원은 어떻게 끕니까?",
            options = listOf("빨간색 정지 버튼을 길게 누르면 됩니다.", "어제 공구 상자를 잃어버렸습니다.", "내일은 회사 창립기념일입니다.", "식당은 건물 이층에 있습니다."),
            correctIdx = 0, tier = 1,
            explanationEn = "To the question 'How do I turn off this machine?', explaining 'Press and hold the red stop button' answers directly.",
            explanationId = "Jawaban 'Tekan lama tombol merah berhenti' menjawab langsung cara mematikan mesin."
        ),
        epsListening(
            id = "eps_l06", typeKo = "작업 순서 파악",
            instructionKo = "다음 대화를 듣고 남자가 먼저 해야 할 일을 고르십시오.",
            stimulusKo = "남자가 가장 먼저 해야 할 일은 무엇입니까?",
            audioKo = "여자: 리한 씨, 포장 작업 시작하기 전에 불량품이 섞여 있는지 먼저 검사해 주세요. 검사가 끝나면 상자에 열 개씩 담아서 테이프로 붙이면 됩니다. 남자: 네, 알겠습니다. 먼저 제품 검사부터 시작하겠습니다.",
            options = listOf("불량품이 있는지 검사한다.", "상자에 테이프를 먼저 붙인다.", "물건을 트럭에 싣는다.", "포장 상자를 접어서 버린다."),
            correctIdx = 0, tier = 1,
            explanationEn = "He confirms starting with product inspection first ('먼저 제품 검사부터 시작하겠습니다').",
            explanationId = "Ia mengonfirmasi akan mulai dari pemeriksaan barang cacat terlebih dahulu."
        ),
        // [27~34] 직장 대화 이해 (연장근무, 휴가, 은행, 병원, 기숙사)
        epsListening(
            id = "eps_l07", typeKo = "연장 근무 대화",
            instructionKo = "다음 대화를 듣고 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 수피안 씨, 오늘 주문이 밀려서 두 시간 정도 연장 근무를 할 수 있을까요? 여자: 네, 반장님. 오늘 저녁에 특별한 약속이 없으니까 할 수 있습니다. 남자: 고마워요. 저녁 식사는 회사 식당에서 제공하고 연장 수당도 정확히 계산해 드릴게요.",
            options = listOf("여자는 오늘 연장 근무를 할 수 있다.", "여자는 저녁 약속 때문에 연장 근무를 거절했다.", "저녁 식사는 본인이 직접 사 먹어야 한다.", "남자는 오늘 휴가를 신청했다."),
            correctIdx = 0, tier = 2,
            explanationEn = "The worker agrees to work 2 hours of overtime ('할 수 있습니다') since she has no evening plans.",
            explanationId = "Pekerja menyetujui lembur 2 jam karena tidak memiliki janji malam ini."
        ),
        epsListening(
            id = "eps_l08", typeKo = "병원 진료 대화",
            instructionKo = "다음 대화를 듣고 여자가 아픈 곳을 고르십시오.",
            stimulusKo = "여자는 어디가 아파서 병원에 왔습니까?",
            audioKo = "남자: 어서 오세요. 어디가 불편해서 오셨습니까? 여자: 어제 작업장에서 무거운 자재를 들다가 허리를 삐끗한 것 같아요. 움직일 때마다 허리가 너무 아파요.",
            options = listOf("허리", "치아", "눈", "귀"),
            correctIdx = 0, tier = 2,
            explanationEn = "She sprained her lower back ('허리를 삐끗한 것 같아요') while lifting heavy materials.",
            explanationId = "Ia terkilir pada bagian pinggang ('허리') saat mengangkat bahan berat di tempat kerja."
        ),
        epsListening(
            id = "eps_l09", typeKo = "은행 업무 대화",
            instructionKo = "다음 대화를 듣고 남자가 은행에 온 목적을 고르십시오.",
            stimulusKo = "남자는 무엇을 하려고 은행에 왔습니까?",
            audioKo = "남자: 안녕하세요. 고향에 있는 가족에게 돈을 보내고 싶은데요. 여자: 해외 송금 신청서를 작성해 주시고, 외국인등록증과 통장을 보여 주시겠어요?",
            options = listOf("고향에 해외 송금을 하려고", "새 신용카드를 해지하려고", "자동차 보험에 가입하려고", "외국인등록증을 재발급받으려고"),
            correctIdx = 0, tier = 2,
            explanationEn = "He came to remit money to his family back home ('고향에 있는 가족에게 돈을 보내고 싶은데요' = 해외 송금).",
            explanationId = "Ia datang ke bank untuk mengirim uang ke keluarga di kampung halaman (해외 송금)."
        ),
        epsListening(
            id = "eps_l10", typeKo = "작업장 안전 대화",
            instructionKo = "다음 대화를 듣고 남자가 주의해야 할 사항을 고르십시오.",
            stimulusKo = "여자가 남자에게 당부한 안전 수칙은 무엇입니까?",
            audioKo = "여자: 부디 씨, 지게차가 창고 안에서 자주 이동하니까 걸어 다닐 때는 꼭 노란색 보행자 통로로만 다니세요. 이어폰을 끼고 걸으면 경적 소리를 못 들어서 아주 위험합니다. 남자: 네, 알겠습니다. 조심하겠습니다.",
            options = listOf("노란색 보행자 통로로 다니고 이어폰을 끼지 않는다.", "지게차 바로 뒤를 바짝 따라 걸어간다.", "창고 안에서 뛰어서 빠르게 이동한다.", "음악을 크게 들으며 상자를 나른다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Walk only along the yellow pedestrian lane and avoid wearing earphones so forklift horns can be heard.",
            explanationId = "Berjalan hanya di jalur kuning pejalan kaki dan tidak memakai earphone agar mendengar klakson forklift."
        ),
        epsListening(
            id = "eps_l11", typeKo = "휴가 신청 대화",
            instructionKo = "다음 대화를 듣고 들은 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 과장님, 다음 달에 고향에서 여동생 결혼식이 있어서 일주일 정도 휴가를 쓰고 싶은데요. 여자: 축하해요. 휴가 신청서를 작성해서 이번 주 수요일까지 사무실에 제출해 주세요.",
            options = listOf("남자는 여동생 결혼식 때문에 휴가를 신청하려 한다.", "남자는 내일부터 바로 휴가를 간다.", "여자는 휴가 신청을 거절했다.", "휴가 신청서는 다음 달에 내면 된다."),
            correctIdx = 0, tier = 2,
            explanationEn = "He wants a one-week leave next month for his younger sister's wedding and must submit the form by Wednesday.",
            explanationId = "Ia mengajukan cuti seminggu bulan depan untuk pernikahan adiknya dan harus menyerahkan formulir sebelum Rabu."
        ),
        epsListening(
            id = "eps_l12", typeKo = "기계 점검 대화",
            instructionKo = "다음 대화를 듣고 이어질 행동으로 알맞은 것을 고르십시오.",
            stimulusKo = "두 사람은 지금 무엇을 할 것입니까?",
            audioKo = "남자: 어, 절단기에서 평소와 다른 이상한 소리가 나네요. 여자: 위험하니까 바로 전원을 끄고 '수리 중' 표지판을 걸어 두세요. 제가 지금 수리 기사님께 연락할게요.",
            options = listOf("전원을 끄고 표지판을 건 뒤 수리를 요청한다.", "소리가 멈출 때까지 기계를 더 세게 돌린다.", "부품을 임의로 분해해서 교체한다.", "아무에게도 말하지 않고 퇴근한다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Shut off power immediately, hang an 'Under Repair' sign ('수리 중' 표지판), and call the repair technician.",
            explanationId = "Matikan daya segera, pasang tanda 'Sedang Diperbaiki', dan hubungi teknisi."
        ),
        epsListening(
            id = "eps_l13", typeKo = "농장 작업 지시",
            instructionKo = "다음을 듣고 농장 관리자의 지시와 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 오늘은 오전에 밭에 농약을 뿌릴 예정입니다. 농약을 뿌릴 때는 바람을 등지고 작업해야 약이 얼굴에 묻지 않습니다. 반드시 방독 마스크와 보호복, 고무장갑을 착용하십시오.",
            options = listOf("바람을 등지고 방독 마스크와 보호복을 입고 작업한다.", "바람을 정면으로 마주 보고 농약을 뿌린다.", "마스크 없이 일반 반팔 옷을 입고 작업한다.", "농약 작업은 밤에만 진행한다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Spray pesticides with your back to the wind ('바람을 등지고') while wearing a gas mask, protective suit, and rubber gloves.",
            explanationId = "Semprot pestisida membelakangi arah angin sambil memakai masker gas, baju pelindung, dan sarung tangan karet."
        ),
        epsListening(
            id = "eps_l14", typeKo = "기숙사 생활 안내",
            instructionKo = "다음을 듣고 기숙사 쓰레기 배출 방법으로 맞는 것을 고르십시오.",
            stimulusKo = "기숙사 쓰레기 배출 규칙으로 맞는 것을 고르십시오.",
            audioKo = "여자: 기숙사 여러분, 쓰레기를 버릴 때는 일반 쓰레기와 음식물 쓰레기, 재활용품을 꼭 분리해서 버려 주십시오. 재활용품인 페트병은 라벨을 떼고 찌그러뜨려서 투명 봉투에 담아 내놓아야 합니다.",
            options = listOf("페트병은 라벨을 제거해 재활용품으로 분리 배출한다.", "모든 쓰레기를 한 봉투에 섞어서 버린다.", "음식물 쓰레기를 재활용 봉투에 넣는다.", "라벨을 붙인 채로 일반 쓰레기에 버린다."),
            correctIdx = 0, tier = 2,
            explanationEn = "Separate general, food, and recyclable waste; remove labels from PET bottles and crush them before recycling.",
            explanationId = "Pilah sampah umum, makanan, dan daur ulang; lepas label botol PET lalu pipihkan untuk daur ulang."
        ),
        // [35~40] 고급 현장 브리핑 & 근로 복지
        epsListening(
            id = "eps_l15", typeKo = "건설 현장 브리핑",
            instructionKo = "다음을 듣고 현장 안전 교육 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 내일부터 비가 많이 예보되어 있습니다. 비가 올 때는 발판이 미끄러워 추락 사고 위험이 매우 높으므로 높은 곳에서의 외부 작업을 전면 중단합니다. 실내 정리와 자재 점검만 진행하겠습니다.",
            options = listOf("비가 오면 높은 곳의 외부 작업을 중단한다.", "비가 와도 옥상 작업을 평소대로 강행한다.", "내일은 공장이 완전히 휴무한다.", "실내 정리 작업도 모두 취소한다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Because wet scaffolding increases fall risk, outdoor work at height is suspended during rain.",
            explanationId = "Saat hujan, pekerjaan luar ruangan di ketinggian dihentikan penuh karena pijakan licin berisiko jatuh."
        ),
        epsListening(
            id = "eps_l16", typeKo = "건강검진 안내",
            instructionKo = "다음을 듣고 건강검진 주의 사항으로 맞는 것을 고르십시오.",
            stimulusKo = "건강검진 전에 지켜야 할 사항은 무엇입니까?",
            audioKo = "여자: 다음 주 화요일 오전에 전 직원 정기 건강검진이 있습니다. 정확한 혈액 검사를 위해 전날 밤 아홉 시 이후부터는 물 이외의 음식은 아무것도 드시지 말고 금식해 주시기 바랍니다.",
            options = listOf("검진 전날 밤 아홉 시 이후에는 음식을 먹지 않는다.", "검진 당일 아침에 밥을 든든하게 먹고 온다.", "정기 건강검진은 신입 사원만 받는다.", "검진 전날 밤에는 물도 절대 마시면 안 된다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Fast from all food (only water allowed) after 9 PM the night before the blood test.",
            explanationId = "Puasa makan (kecuali air putih) mulai pukul 9 malam sehari sebelum pemeriksaan darah."
        ),
        epsListening(
            id = "eps_l17", typeKo = "어업 현장 지시",
            instructionKo = "다음을 듣고 선장님의 지시 내용과 같은 것을 고르십시오.",
            stimulusKo = "선장님이 강조하는 안전 수칙은 무엇입니까?",
            audioKo = "남자: 그물을 끌어올리는 기계가 작동할 때는 장갑이나 옷소매가 기계에 말려 들어가지 않도록 각별히 주의하십시오. 갑판 바닥에 생선 기름이나 물기가 남아 있으면 바로 닦아 내서 미끄러지지 않게 해야 합니다.",
            options = listOf("기계에 옷이 말리지 않게 조심하고 갑판 물기를 바로 닦는다.", "기계가 돌아갈 때 옷소매를 길게 풀어 둔다.", "갑판 청소는 조업이 끝난 다음 날에 한다.", "그물 기계 옆에서 휴식을 취한다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Keep gloves/sleeves clear of hauling machinery and wipe oil/water off the deck immediately to prevent slips.",
            explanationId = "Waspadai sarung tangan/lengan baju agar tidak tersangkut mesin jaring dan segera bersihkan air/minyak di dek."
        ),
        epsListening(
            id = "eps_l18", typeKo = "월급 문의 대화",
            instructionKo = "다음 대화를 듣고 내용과 같은 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "남자: 대리님, 이번 달 급여명세서를 보니까 지난주 토요일에 특근한 수당이 빠져 있는 것 같아요. 여자: 어머, 정말요? 출근 기록부를 다시 확인해 볼게요. 누락된 게 맞으면 이번 주 금요일 안에 추가로 입금해 드리겠습니다.",
            options = listOf("남자는 토요일 특근 수당이 빠진 것 같아 문의했다.", "여자는 수당을 지급할 수 없다고 거절했다.", "남자는 지난주 토요일에 결근했다.", "누락된 수당은 내년으로 이월된다."),
            correctIdx = 0, tier = 3,
            explanationEn = "The worker politely inquired about missing Saturday special-shift pay, and HR promised to verify records and deposit it by Friday.",
            explanationId = "Pekerja menanyakan tunjangan lembur Sabtu yang terlewat, dan staf berjanji memeriksa absensi lalu mentransfernya paling lambat Jumat."
        ),
        epsListening(
            id = "eps_l19", typeKo = "소방 훈련 안내",
            instructionKo = "다음을 듣고 소화기 사용 순서로 맞는 것을 고르십시오.",
            stimulusKo = "소화기를 사용할 때의 올바른 방법은 무엇입니까?",
            audioKo = "남자: 소화기를 사용할 때는 먼저 손잡이의 안전핀을 뽑습니다. 그다음 노즐을 불이 난 곳으로 향하게 잡고, 바람을 등진 상태에서 손잡이를 힘껏 눌러 빗자루로 쓸듯이 뿌려 주십시오.",
            options = listOf("안전핀을 뽑고 바람을 등진 채 불쪽을 향해 뿌린다.", "안전핀을 뽑지 않은 상태로 손잡이를 누른다.", "바람을 정면으로 맞으며 천장을 향해 뿌린다.", "소화기를 불 속에 직접 던진다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Pull the safety pin (안전핀), aim the nozzle at the fire with your back to the wind, and squeeze the handle in a sweeping motion.",
            explanationId = "Tarik pin pengaman, arahkan nosel ke sumber api sambil membelakangi angin, lalu tekan tuas menyapu."
        ),
        epsListening(
            id = "eps_l20", typeKo = "산재 보험 안내",
            instructionKo = "다음을 듣고 산업재해보상보험에 대한 설명으로 맞는 것을 고르십시오.",
            stimulusKo = "들은 내용과 같은 것을 고르십시오.",
            audioKo = "여자: 외국인 근로자도 일하다가 다치거나 직업병에 걸리면 한국인과 똑같이 산재보험 혜택을 받을 수 있습니다. 치료비뿐만 아니라 치료 때문에 일을 못 하는 기간의 휴업 급여도 지원되므로, 다쳤을 때는 숨기지 말고 즉시 병원 진료를 받으시기 바랍니다.",
            options = listOf("외국인 근로자도 업무 중 다치면 치료비와 휴업 급여를 지원받을 수 있다.", "외국인 근로자는 산재보험 혜택을 받을 수 없다.", "산재보험은 치료비만 지원하고 휴업 급여는 없다.", "다쳤을 때는 회사에 알리지 않고 참아야 한다."),
            correctIdx = 0, tier = 3,
            explanationEn = "Foreign workers receive equal Industrial Accident Compensation Insurance covering medical costs and temporary disability benefits.",
            explanationId = "Pekerja asing berhak mendapat asuransi kecelakaan kerja (산재보험) yang menanggung biaya pengobatan dan tunjangan selama tidak dapat bekerja."
        )
    )

    // Official HRD Korea EPS-TOPIK order: Reading (1~20) then Listening (21~40)
    val epsTopikQuestions: List<ExamQuestion> = epsReadingQuestions + epsListeningQuestions

    // ==================== NIIED TOPIK SPEAKING IBT (6 Official Tasks = 200점) ====================

    val speakingTask1Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "tspk_1_v1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 1 · 질문에 대답하기 (20점)",
            instructionKo = "[과제 1] 질문을 듣고 한국어로 정중하게 대답하십시오. (준비 20초 / 답변 30초)",
            stimulusKo = "한국어를 배우기 시작한 계기는 무엇입니까? 그리고 한국어로 가장 하고 싶은 일은 무엇입니까?",
            audioTranscriptKo = "질문 1번입니다. 한국어를 배우기 시작한 계기는 무엇입니까? 그리고 한국어로 가장 하고 싶은 일은 무엇입니까?",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 1,
            targetMinChars = 30, targetMaxChars = 120, prepSeconds = 20, speakSeconds = 30,
            pointWeight = 20, difficultyTier = 1,
            expectedKeywords = listOf("한국어", "배우", "때문", "싶", "문화", "일", "여행", "친구", "요", "습니다"),
            modelAnswerKo = "저는 한국 드라마와 음악에 관심이 생겨서 한국어를 배우기 시작했습니다. 앞으로 한국어를 유창하게 배워서 한국 친구들과 자유롭게 이야기하고 혼자 한국 여행을 가고 싶습니다.",
            explanationEn = "Task 1 (20 pts): Answer BOTH questions directly (why you started + what you want to do) in polite 해요체 or formal 합니다체.",
            explanationId = "Tugas 1 (20 poin): Jawab KEDUA pertanyaan (alasan mulai belajar + hal yang paling ingin dilakukan) dengan ragam sopan 해요체/합니다체."
        ),
        ExamQuestion(
            id = "tspk_1_v2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 1 · 질문에 대답하기 (20점)",
            instructionKo = "[과제 1] 질문을 듣고 한국어로 정중하게 대답하십시오. (준비 20초 / 답변 30초)",
            stimulusKo = "주말에 시간이 있을 때 주로 무엇을 합니까? 그 활동을 좋아하는 이유는 무엇입니까?",
            audioTranscriptKo = "질문 1번입니다. 주말에 시간이 있을 때 주로 무엇을 합니까? 그 활동을 좋아하는 이유는 무엇입니까?",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 1,
            targetMinChars = 30, targetMaxChars = 120, prepSeconds = 20, speakSeconds = 30,
            pointWeight = 20, difficultyTier = 1,
            expectedKeywords = listOf("주말", "보통", "주로", "좋아", "때문", "스트레스", "재미", "운동", "요", "습니다"),
            modelAnswerKo = "저는 주말에 시간이 있을 때 주로 공원에서 산책하거나 친구들과 카페에 갑니다. 일주일 동안 쌓인 스트레스가 풀리고 기분이 상쾌해지기 때문에 이 활동을 좋아합니다.",
            explanationEn = "State your usual weekend activity and give a clear reason using -기 때문입니다 or -아/어서.",
            explanationId = "Sebutkan aktivitas akhir pekan Anda beserta alasan jelas menggunakan -기 때문입니다 atau -아/어서."
        )
    )

    val speakingTask2Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "tspk_2_v1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 2 · 상황 보고 역할 수행하기 (30점)",
            instructionKo = "[과제 2] 주어진 상황을 보고 상대방에게 자연스럽게 요청하십시오. (준비 30초 / 답변 40초)",
            passageBoxKo = "[상황 카드]\n• 장소: 서울의 한 한식당\n• 상황: 김치찌개를 주문하려고 하지만 매운 음식을 잘 못 먹습니다.\n• 과제: 직원에게 안 맵거나 덜 맵게 해 달라고 부탁하고, 덜 매운 추천 메뉴도 물어보세요.",
            stimulusKo = "식당 직원에게 정중하게 부탁하고 질문하는 말을 하십시오.",
            audioTranscriptKo = "과제 2번입니다. 식당에서 매운 음식을 잘 못 먹는 상황입니다. 직원에게 덜 맵게 해 달라고 정중하게 부탁하고 추천 메뉴를 물어보십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 2,
            targetMinChars = 40, targetMaxChars = 150, prepSeconds = 30, speakSeconds = 40,
            pointWeight = 30, difficultyTier = 1,
            expectedKeywords = listOf("매운", "맵", "덜", "주세요", "혹시", "추천", "메뉴", "요", "습니다"),
            modelAnswerKo = "저기요, 혹시 김치찌개를 덜 맵게 해 주실 수 있으세요? 제가 매운 음식을 잘 못 먹어서요. 만약 어렵다면 덜 매운 다른 메뉴를 하나 추천해 주시면 감사하겠습니다.",
            explanationEn = "Task 2 (30 pts): Cover both functional goals — polite request ('덜 맵게 해 주실 수 있으세요?') + asking for a recommendation ('추천해 주세요').",
            explanationId = "Tugas 2 (30 poin): Penuhi kedua tujuan — permintaan sopan agar tidak terlalu pedas dan menanyakan rekomendasi menu lain."
        ),
        ExamQuestion(
            id = "tspk_2_v2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 2 · 상황 보고 역할 수행하기 (30점)",
            instructionKo = "[과제 2] 주어진 상황을 보고 상대방에게 자연스럽게 요청하십시오. (준비 30초 / 답변 40초)",
            passageBoxKo = "[상황 카드]\n• 장소: 옷 가게\n• 상황: 어제 산 셔츠가 집에서 입어 보니 조금 작습니다.\n• 과제: 직원에게 영수증을 보여 주며 한 치수 큰 사이즈로 교환하고 싶다고 말하고, 다른 색상도 있는지 물어보세요.",
            stimulusKo = "옷 가게 직원에게 정중하게 교환을 요청하고 질문하십시오.",
            audioTranscriptKo = "과제 2번입니다. 어제 산 셔츠가 작아서 옷 가게에 다시 갔습니다. 직원에게 큰 사이즈로 교환을 부탁하고 다른 색상도 물어보십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 2,
            targetMinChars = 40, targetMaxChars = 150, prepSeconds = 30, speakSeconds = 40,
            pointWeight = 30, difficultyTier = 1,
            expectedKeywords = listOf("셔츠", "작", "사이즈", "교환", "바꾸", "색", "영수증", "주세요", "요"),
            modelAnswerKo = "안녕하세요. 어제 여기서 산 셔츠가 조금 작아서 교환하러 왔습니다. 영수증 여기 있는데요, 한 치수 큰 사이즈로 바꿀 수 있을까요? 그리고 혹시 파란색 말고 다른 색상도 있는지 궁금합니다.",
            explanationEn = "Politely explain the issue (조금 작아서), request exchange (교환/바꿀 수 있을까요), and inquire about other colors.",
            explanationId = "Jelaskan masalah ukuran, minta penukaran dengan sopan (교환/바꿀 수 있을까요), dan tanyakan pilihan warna lain."
        )
    )

    val speakingTask3Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "tspk_3_v1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 3 · 장면 순서대로 이야기하기 (30점)",
            instructionKo = "[과제 3] 네 장의 장면 설명을 순서대로 연결하여 하나의 이야기로 말하십시오. (준비 40초 / 답변 60초)",
            passageBoxKo = "[4컷 이야기 장면]\n① 민수 씨가 아침에 지각할 것 같아서 급하게 뛰어가다 지갑을 떨어뜨림\n② 뒤에 오던 학생이 지갑을 발견하고 주워서 민수 씨를 부름\n③ 학생이 지갑을 돌려주자 민수 씨가 진심으로 고마워함\n④ 다음 날 민수 씨가 감사의 표시로 학생에게 커피를 선물하며 웃음",
            stimulusKo = "네 장면을 과거 시제와 시간 연결 표현(먼저, 그런데, 그 뒤, 결국)을 사용해 자연스럽게 이야기하십시오.",
            audioTranscriptKo = "과제 3번입니다. 네 장의 장면을 순서대로 연결하여 자연스러운 이야기로 말씀해 주십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 3,
            targetMinChars = 60, targetMaxChars = 220, prepSeconds = 40, speakSeconds = 60,
            pointWeight = 30, difficultyTier = 2,
            expectedKeywords = listOf("지갑", "떨어", "학생", "주워", "돌려", "고마", "감사", "커피", "는데", "그래서"),
            modelAnswerKo = "어느 날 아침 민수 씨가 회사에 늦을 것 같아서 급하게 뛰어가다가 길에 지갑을 떨어뜨렸습니다. 그런데 마침 뒤에서 걸어오던 한 학생이 그 지갑을 발견하고 주워서 민수 씨를 불러 세웠습니다. 학생이 지갑을 돌려주자 민수 씨는 정말 다행이라며 진심으로 고마워했습니다. 그래서 다음 날 민수 씨는 감사의 표시로 그 학생에게 따뜻한 커피를 선물하며 함께 환하게 웃었습니다.",
            explanationEn = "Task 3 (30 pts): Narrate all 4 scenes in chronological sequence using past tense (-았/었습니다) and discourse links (그런데, 그래서).",
            explanationId = "Tugas 3 (30 poin): Ceritakan keempat adegan secara kronologis dengan kala lampau (-았/었습니다) dan kata hubung (그런데, 그래서)."
        ),
        ExamQuestion(
            id = "tspk_3_v2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 3 · 장면 순서대로 이야기하기 (30점)",
            instructionKo = "[과제 3] 네 장의 장면 설명을 순서대로 연결하여 하나의 이야기로 말하십시오. (준비 40초 / 답변 60초)",
            passageBoxKo = "[4컷 이야기 장면]\n① 지수 씨가 어머니 생신 선물로 직접 케이크를 만들기로 결심함\n② 요리 영상을 보며 반죽을 만들었지만 처음이라 부엌이 엉망이 됨\n③ 실수 끝에 정성을 다해 케이크를 완성하고 예쁘게 장식함\n④ 생신 파티에서 어머니가 감동하며 가족 모두 즐겁게 축하함",
            stimulusKo = "네 장면을 순서대로 연결하여 자연스러운 이야기로 말하십시오.",
            audioTranscriptKo = "과제 3번입니다. 어머니 생신 케이크를 만드는 네 장면을 순서대로 연결하여 이야기해 주십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 3,
            targetMinChars = 60, targetMaxChars = 220, prepSeconds = 40, speakSeconds = 60,
            pointWeight = 30, difficultyTier = 2,
            expectedKeywords = listOf("생신", "케이크", "만들", "처음", "완성", "어머니", "감동", "가족", "지만", "그래서"),
            modelAnswerKo = "지수 씨는 어머니 생신을 축하드리기 위해 직접 케이크를 만들기로 결심했습니다. 요리 영상을 보며 열심히 시작했지만 처음 해 보는 요리라 부엌이 금세 엉망이 되었습니다. 그래도 포기하지 않고 정성을 다해 예쁜 케이크를 완성했습니다. 생신 파티에서 케이크를 보신 어머니는 크게 감동하셨고 가족 모두가 즐겁게 생신을 축하했습니다.",
            explanationEn = "Connect all 4 story beats smoothly with contrast (-지만 / 그래도) and warm conclusion.",
            explanationId = "Hubungkan 4 adegan dengan lancar memakai konjungsi kontras (-지만 / 그래도) dan penutup yang hangat."
        )
    )

    val speakingTask4Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "tspk_4_v1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 4 · 대화 완성하기 (40점)",
            instructionKo = "[과제 4] 대화의 맥락을 파악하여 빈 곳에 들어갈 적절한 설명과 제안을 말하십시오. (준비 40초 / 답변 60초)",
            passageBoxKo = "[대화 상황]\n친구: 이번에 한국 회사 면접을 보게 됐는데 너무 떨리고 어떻게 준비해야 할지 모르겠어. 조언 좀 해 줄 수 있어?\n\n[말해야 할 내용]\n1. 친구를 격려하고 공감하기\n2. 면접 준비 방법 두 가지 제안하기 (예: 예상 질문 연습, 회사 정보 조사, 복장·예절)",
            stimulusKo = "친구에게 공감하고 구체적인 면접 준비 방법 두 가지를 자연스럽게 제안하십시오.",
            audioTranscriptKo = "과제 4번입니다. 친구가 한국 회사 면접을 앞두고 긴장하고 있습니다. 공감해 주고 준비 방법 두 가지를 제안해 보십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 4,
            targetMinChars = 70, targetMaxChars = 240, prepSeconds = 40, speakSeconds = 60,
            pointWeight = 40, difficultyTier = 2,
            expectedKeywords = listOf("면접", "긴장", "준비", "질문", "연습", "회사", "조사", "먼저", "그리고", "요"),
            modelAnswerKo = "면접을 앞두고 많이 긴장되겠네요. 저도 처음 면접 볼 때 정말 떨렸어요. 먼저 지원한 회사의 주요 사업과 인재상을 미리 조사해 두면 자신 있게 대답할 수 있어요. 그리고 자기소개나 지원 동기 같은 예상 질문을 소리 내어 연습해 보는 것이 큰 도움이 됩니다. 차분하게 준비하면 분명 좋은 결과가 있을 거예요.",
            explanationEn = "Task 4 (40 pts): Express empathy first, propose two clear actionable tips using '먼저 / 그리고', and close with encouragement.",
            explanationId = "Tugas 4 (40 poin): Tunjukkan empati terlebih dahulu, berikan dua saran konkret dengan '먼저 / 그리고', lalu tutup dengan semangat."
        ),
        ExamQuestion(
            id = "tspk_4_v2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 4 · 대화 완성하기 (40점)",
            instructionKo = "[과제 4] 대화의 맥락을 파악하여 빈 곳에 들어갈 적절한 설명과 제안을 말하십시오. (준비 40초 / 답변 60초)",
            passageBoxKo = "[대화 상황]\n동료: 요즘 밤에 잠이 잘 안 와서 출근할 때마다 너무 피곤해. 잠을 잘 자는 좋은 방법이 있을까?\n\n[말해야 할 내용]\n1. 동료의 피로에 공감하기\n2. 수면에 도움이 되는 습관 두 가지 제안하기 (예: 스마트폰 줄이기, 카페인 피하기, 가벼운 스트레칭)",
            stimulusKo = "동료에게 공감하고 건강한 수면 습관 두 가지를 제안하십시오.",
            audioTranscriptKo = "과제 4번입니다. 동료가 불면증 때문에 피곤해하고 있습니다. 공감해 주고 수면에 도움이 되는 방법 두 가지를 제안하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 4,
            targetMinChars = 70, targetMaxChars = 240, prepSeconds = 40, speakSeconds = 60,
            pointWeight = 40, difficultyTier = 2,
            expectedKeywords = listOf("잠", "피곤", "스마트폰", "카페인", "스트레칭", "습관", "도움", "먼저", "그리고", "요"),
            modelAnswerKo = "잠을 제대로 못 자면 낮에 정말 힘드시죠. 먼저 잠들기 한 시간 전부터는 스마트폰을 내려놓고 조명을 어둡게 하는 것이 좋습니다. 그리고 오후 늦게는 커피 같은 카페인 음료를 피하고 따뜻한 물로 샤워하거나 가벼운 스트레칭을 해 보세요. 꾸준히 실천하시면 훨씬 푹 주무실 수 있을 거예요.",
            explanationEn = "Combine empathy, two concrete sleep habits, and polite advisory expressions (-(으)ㄴ/는 것이 좋습니다, -아/어 보세요).",
            explanationId = "Gabungkan empati, dua saran kebiasaan tidur sehat, dan ungkapan saran sopan (-(으)ㄴ/는 것이 좋습니다, -아/어 보세요)."
        )
    )

    val speakingTask5Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "tspk_5_v1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 5 · 자료 해석하고 제안하기 (40점)",
            instructionKo = "[과제 5] 다음 자료를 요약하여 설명하고 문제 해결 방안을 제시하십시오. (준비 70초 / 답변 80초)",
            passageBoxKo = "<조사: 직장인의 일회용 컵 사용 실태 (성인 500명)>\n• 1주일 평균 일회용 컵 사용량: 5.4개\n• 개인 텀블러를 사용하지 않는 이유:\n  1위: 가지고 다니기 무겁고 번거로움 (56%)\n  2위: 씻기 귀찮음 (29%)\n  3위: 할인 혜택이 적음 (15%)",
            stimulusKo = "자료의 핵심 수치를 설명하고, 텀블러 사용을 늘리기 위한 구체적인 방안을 발표하십시오.",
            audioTranscriptKo = "과제 5번입니다. 직장인의 일회용 컵 사용 실태 자료를 요약하고, 텀블러 사용을 늘릴 수 있는 방안을 제안해 주십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 5,
            targetMinChars = 90, targetMaxChars = 300, prepSeconds = 70, speakSeconds = 80,
            pointWeight = 40, difficultyTier = 3,
            expectedKeywords = listOf("일회용", "텀블러", "이유", "번거", "할인", "세척", "제안", "따라서", "습니다"),
            modelAnswerKo = "조사 자료에 따르면 직장인은 일주일에 평균 5.4개의 일회용 컵을 사용하고 있습니다. 개인 텀블러를 쓰지 않는 가장 큰 이유는 휴대가 번거롭다는 응답이 56%로 가장 많았고, 세척이 귀찮다는 응답이 29%, 할인 혜택 부족이 15%였습니다. 따라서 텀블러 사용을 늘리기 위해서는 회사나 카페에 간편한 텀블러 세척기를 설치하고, 텀블러 이용 시 할인 혜택을 더욱 확대할 필요가 있습니다.",
            explanationEn = "Task 5 (40 pts): Report key stats accurately ('조사 자료에 따르면...'), highlight top reasons, and propose realistic solutions tied directly to the data.",
            explanationId = "Tugas 5 (40 poin): Laporkan angka utama secara akurat, soroti penyebab terbesar, dan ajukan solusi konkret yang sesuai data."
        ),
        ExamQuestion(
            id = "tspk_5_v2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 5 · 자료 해석하고 제안하기 (40점)",
            instructionKo = "[과제 5] 다음 자료를 요약하여 설명하고 문제 해결 방안을 제시하십시오. (준비 70초 / 답변 80초)",
            passageBoxKo = "<조사: 대학생의 독서 습관 변화>\n• 월평균 독서량: 2018년 2.4권 → 2025년 1.1권\n• 독서를 하지 않는 주요 이유:\n  1위: 스마트폰·짧은 영상 시청 시간 증가 (48%)\n  2위: 취업·학업 준비로 시간 부족 (37%)\n  3위: 어떤 책을 읽어야 할지 모름 (15%)",
            stimulusKo = "자료의 변화 추세와 원인을 설명하고, 대학생 독서 활성화를 위한 방안을 제안하십시오.",
            audioTranscriptKo = "과제 5번입니다. 대학생의 독서 습관 변화 자료를 설명하고 독서를 활성화할 방안을 제안해 주십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 5,
            targetMinChars = 90, targetMaxChars = 300, prepSeconds = 70, speakSeconds = 80,
            pointWeight = 40, difficultyTier = 3,
            expectedKeywords = listOf("독서", "감소", "줄", "스마트폰", "시간", "추천", "도서관", "따라서", "습니다"),
            modelAnswerKo = "자료를 보면 대학생의 월평균 독서량은 2018년 2.4권에서 2025년 1.1권으로 절반 이하로 줄어들었습니다. 주요 원인으로는 스마트폰과 짧은 영상 시청 증가가 48%로 가장 높았고, 학업과 취업 준비로 인한 시간 부족이 37%였습니다. 따라서 학교 도서관에서 관심 분야별 추천 도서를 안내하고, 이동 중에도 읽을 수 있는 전자책과 오디오북 지원을 확대하는 방안이 효과적이라고 생각합니다.",
            explanationEn = "Summarize the downward trend, cite the top two reasons, and propose practical solutions using formal polite speech (-습니다).",
            explanationId = "Ringkas tren penurunan, sebutkan dua penyebab terbesar, lalu usulkan solusi praktis dalam ragam formal sopan (-습니다)."
        )
    )

    val speakingTask6Variants: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "tspk_6_v1", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 6 · 의견 제시하기 (40점)",
            instructionKo = "[과제 6] 질문에 대해 자신의 입장을 분명히 밝히고 근거를 들어 논리적으로 말하십시오. (준비 70초 / 답변 80초)",
            passageBoxKo = "[토론 주제]\n혼자 공부하는 것과 스터디 그룹에서 함께 공부하는 것 중 어느 쪽이 더 효과적이라고 생각하십니까?\n\n• 자신의 입장을 분명히 제시하세요.\n• 그렇게 생각하는 이유를 두 가지 이상 구체적으로 설명하세요.",
            stimulusKo = "자신의 입장과 두 가지 이상의 근거를 논리적으로 말하십시오.",
            audioTranscriptKo = "과제 6번입니다. 혼자 공부하는 것과 함께 공부하는 것 중 어느 쪽이 더 효과적이라고 생각하십니까? 입장과 이유를 설명하십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 6,
            targetMinChars = 100, targetMaxChars = 320, prepSeconds = 70, speakSeconds = 80,
            pointWeight = 40, difficultyTier = 3,
            expectedKeywords = listOf("혼자", "함께", "생각", "첫째", "둘째", "때문", "효과", "동기", "집중", "습니다"),
            modelAnswerKo = "저는 스터디 그룹에서 함께 공부하는 것이 더 효과적이라고 생각합니다. 첫째, 정해진 시간에 동료들과 함께 공부하면 미루지 않고 꾸준히 학습하는 동기 부여가 되기 때문입니다. 둘째, 혼자서는 알기 어려운 실수나 부족한 부분을 서로 질문하고 설명해 주면서 이해도가 훨씬 깊어집니다. 따라서 목표를 끝까지 달성하기 위해서는 함께 공부하는 방식이 더 유리하다고 봅니다.",
            explanationEn = "Task 6 (40 pts): State your stance clearly up front, organize 2+ arguments with signposts ('첫째 / 둘째 / -기 때문입니다'), and wrap up with '따라서'.",
            explanationId = "Tugas 6 (40 poin): Nyatakan posisi dengan jelas di awal, susun 2+ argumen terstruktur ('첫째 / 둘째 / -기 때문입니다'), lalu simpulkan dengan '따라서'."
        ),
        ExamQuestion(
            id = "tspk_6_v2", examType = ExamType.TOPIK_SPEAKING, section = "Speaking", period = 1,
            questionTypeKo = "과제 6 · 의견 제시하기 (40점)",
            instructionKo = "[과제 6] 질문에 대해 자신의 입장을 분명히 밝히고 근거를 들어 논리적으로 말하십시오. (준비 70초 / 답변 80초)",
            passageBoxKo = "[토론 주제]\n대중교통 요금을 무료로 전환하는 정책에 찬성하십니까, 반대하십니까?\n\n• 찬성 또는 반대 입장을 분명히 밝히세요.\n• 근거를 두 가지 이상 들어 논리적으로 설명하세요.",
            stimulusKo = "자신의 찬반 입장과 두 가지 이상의 근거를 논리적으로 말하십시오.",
            audioTranscriptKo = "과제 6번입니다. 대중교통 무료화 정책에 찬성하십니까, 반대하십니까? 자신의 입장과 근거를 말씀해 주십시오.",
            isOpenEnded = true, correctOptionIndex = -1, writingTaskNumber = 6,
            targetMinChars = 100, targetMaxChars = 320, prepSeconds = 70, speakSeconds = 80,
            pointWeight = 40, difficultyTier = 3,
            expectedKeywords = listOf("대중교통", "찬성", "반대", "환경", "예산", "첫째", "둘째", "때문", "따라서", "습니다"),
            modelAnswerKo = "저는 대중교통 무료화 정책에 신중하게 반대하는 입장입니다. 첫째, 요금을 전면 무료로 하면 막대한 운영 예산을 세금으로 충당해야 하므로 재정 부담이 지나치게 커질 수 있기 때문입니다. 둘째, 예산이 부족해지면 오히려 차량 노후화나 배차 간격 증가로 서비스 품질이 떨어질 우려가 있습니다. 따라서 전면 무료화보다는 학생이나 저소득층을 위한 맞춤형 교통비 지원 제도를 확대하는 것이 더 바람직하다고 생각합니다.",
            explanationEn = "Present a clear stance (찬성/반대), give two structured economic/social reasons ('첫째/둘째'), and offer a balanced alternative.",
            explanationId = "Sampaikan sikap tegas (setuju/menolak), sertakan dua alasan terstruktur ('첫째/둘째'), dan akhiri dengan alternatif yang seimbang."
        )
    )

    val topikSpeakingQuestions: List<ExamQuestion> = listOf(
        speakingTask1Variants.first(),
        speakingTask2Variants.first(),
        speakingTask3Variants.first(),
        speakingTask4Variants.first(),
        speakingTask5Variants.first(),
        speakingTask6Variants.first()
    )
}
