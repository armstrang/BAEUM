package com.example.data.seed

enum class ExamPaperLength(
    val titleEn: String,
    val titleId: String,
    val badgeKo: String
) {
    STANDARD(
        titleEn = "Compact Mock",
        titleId = "Simulasi Ringkas",
        badgeKo = "실전 압축"
    ),
    FULL_SIMULATION(
        titleEn = "Full Official Mock",
        titleId = "Simulasi TOPIK Penuh",
        badgeKo = "정규 실전"
    )
}

enum class ExamType(
    val title: String,
    val badgeKo: String,
    val subtitle: String,
    val sectionsLabel: String,
    val maxScore: Int,
    val mockDurationMinutes: Int,
    val fullDurationMinutes: Int,
    val standardQuestionSummaryEn: String,
    val standardQuestionSummaryId: String,
    val fullQuestionSummaryEn: String,
    val fullQuestionSummaryId: String,
    val passingBandsEn: String,
    val passingBandsId: String,
    val hasTwoPeriods: Boolean,
    val officialReferenceNote: String
) {
    TOPIK_I(
        title = "TOPIK I",
        badgeKo = "1–2급",
        subtitle = "Listening • Reading",
        sectionsLabel = "Listening (100) + Reading (100)",
        maxScore = 200,
        mockDurationMinutes = 30,
        fullDurationMinutes = 100,
        standardQuestionSummaryEn = "20 Qs (10 Listening + 10 Reading) • 30 min",
        standardQuestionSummaryId = "20 Soal (10 Menyimak + 10 Membaca) • 30 mnt",
        fullQuestionSummaryEn = "70 Qs (30 Listening + 40 Reading) • 100 min",
        fullQuestionSummaryId = "70 Soal (30 Menyimak + 40 Membaca) • 100 mnt",
        passingBandsEn = "Level 1 ≥ 80 • Level 2 ≥ 140",
        passingBandsId = "Level 1 ≥ 80 • Level 2 ≥ 140",
        hasTwoPeriods = false,
        officialReferenceNote = "Authentic simulation modeled on NIIED TOPIK I (70 Qs, 100 min, 2× listening playback). Practice estimates are not an official certificate."
    ),
    TOPIK_II(
        title = "TOPIK II",
        badgeKo = "3–6급",
        subtitle = "1교시 Listening · Writing • 2교시 Reading",
        sectionsLabel = "Listening (100) + Writing 51–54 (100) + Reading (100)",
        maxScore = 300,
        mockDurationMinutes = 65,
        fullDurationMinutes = 150,
        standardQuestionSummaryEn = "24 Qs (P1: 10 Listening + 4 Writing • P2: 10 Reading) • 65 min",
        standardQuestionSummaryId = "24 Soal (Sesi 1: 10 Menyimak + 4 Menulis • Sesi 2: 10 Membaca) • 65 mnt",
        fullQuestionSummaryEn = "64 Qs (P1: 30 Listening + 4 Writing 51–54 • P2: 30 Reading) • 150 min",
        fullQuestionSummaryId = "64 Soal (Sesi 1: 30 Menyimak + 4 Menulis 51–54 • Sesi 2: 30 Membaca) • 150 mnt",
        passingBandsEn = "3급 ≥ 120 • 4급 ≥ 150 • 5급 ≥ 190 • 6급 ≥ 230",
        passingBandsId = "Level 3 ≥ 120 • Level 4 ≥ 150 • Level 5 ≥ 190 • Level 6 ≥ 230",
        hasTwoPeriods = true,
        officialReferenceNote = "Authentic 2-period simulation modeled on NIIED TOPIK II (Period 1 Listening+Writing 51–54, Period 2 Reading). Writing uses local rubric scoring."
    ),
    EPS_TOPIK(
        title = "EPS-TOPIK",
        badgeKo = "고용허가제",
        subtitle = "Workplace Reading • Listening",
        sectionsLabel = "Workplace Reading (100) + Listening (100)",
        maxScore = 200,
        mockDurationMinutes = 25,
        fullDurationMinutes = 50,
        standardQuestionSummaryEn = "20 Qs (10 Reading + 10 Listening) • 25 min",
        standardQuestionSummaryId = "20 Soal (10 Membaca + 10 Menyimak) • 25 mnt",
        fullQuestionSummaryEn = "40 Qs (20 Reading + 20 Listening • 5 pts each) • 50 min",
        fullQuestionSummaryId = "40 Soal (20 Membaca + 20 Menyimak • 5 poin/soal) • 50 mnt",
        passingBandsEn = "Cutoff ≥ 110 • Competitive ≥ 140",
        passingBandsId = "Batas Lulus ≥ 110 • Kompetitif ≥ 140",
        hasTwoPeriods = false,
        officialReferenceNote = "Authentic workplace UBT simulation modeled on HRD Korea EPS-TOPIK (40 Qs, 50 min, Reading then Listening). Independent study material."
    ),
    TOPIK_SPEAKING(
        title = "TOPIK Speaking",
        badgeKo = "말하기 IBT",
        subtitle = "6 Structured Korean Speaking Tasks",
        sectionsLabel = "Tasks 1–6 (Answer • Roleplay • Story • Dialogue • Data • Opinion)",
        maxScore = 200,
        mockDurationMinutes = 20,
        fullDurationMinutes = 30,
        standardQuestionSummaryEn = "6 Official Tasks (200 pts) • Timed Prep & Response • 20 min",
        standardQuestionSummaryId = "6 Tugas Resmi (200 poin) • Waktu Persiapan & Jawab • 20 mnt",
        fullQuestionSummaryEn = "6 Official Tasks (200 pts) • Full IBT Timing • 30 min",
        fullQuestionSummaryId = "6 Tugas Resmi (200 poin) • Waktu IBT Penuh • 30 mnt",
        passingBandsEn = "1급 30 • 2급 60 • 3급 90 • 4급 120 • 5급 150 • 6급 170",
        passingBandsId = "Level 1: 30 • 2: 60 • 3: 90 • 4: 120 • 5: 150 • 6: 170",
        hasTwoPeriods = false,
        officialReferenceNote = "Authentic 6-task simulation modeled on NIIED TOPIK Speaking IBT with voice capture & rubric evaluation. Local practice estimate."
    )
}

data class ExamQuestion(
    val id: String,
    val examType: ExamType,
    val section: String, // "Listening", "Writing", "Reading", "Speaking"
    val instructionKo: String,
    val stimulusKo: String,
    val audioTranscriptKo: String? = null, // Played via ko-KR TTS for Listening & Speaking prompts
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0, // -1 for open writing/speaking
    val isOpenEnded: Boolean = false,
    val expectedKeywords: List<String> = emptyList(),
    val explanationEn: String,
    val pointWeight: Int = 3, // Authentic TOPIK/EPS weight: 2/3/4/5/10/20/30/40/50점
    val difficultyTier: Int = 1, // 1 = Foundation, 2 = Intermediate, 3 = Advanced
    val period: Int = 1, // 1 = 1교시, 2 = 2교시 (TOPIK II Reading)
    val questionTypeKo: String = "",
    val passageBoxKo: String? = null, // Structured <보기>, [안내문], <자료>, (가)(나)(다)(라), or shared passage
    val underlinedText: String? = null, // Exact substring underlined in stimulusKo or passageBoxKo
    val maxAudioPlaysInMock: Int = 2, // 1 for TOPIK II Q1~20 style, 2 for TOPIK I / EPS / TOPIK II Q21+
    val sharedGroupId: String? = null, // Links 2-question shared listening/reading sets
    val sharedGroupLabelKo: String? = null,
    val writingTaskNumber: Int? = null, // 51..54 for TOPIK II Writing; 1..6 for TOPIK Speaking
    val dualBlanks: Boolean = false, // True for TOPIK II 51번 & 52번 (ㄱ)/(ㄴ)
    val targetMinChars: Int = 0, // Official char target floor (including spaces for 53/54)
    val targetMaxChars: Int = 0, // Official char target ceiling
    val prepSeconds: Int = 0, // TOPIK Speaking preparation time
    val speakSeconds: Int = 0, // TOPIK Speaking response time
    val modelAnswerKo: String? = null, // Authentic model answer shown in Review/Practice Check
    val explanationId: String = "" // Indonesian explanation for bilingual post-exam review
) {
    fun explanationFor(language: String): String =
        if (language.equals("ID", ignoreCase = true) && explanationId.isNotBlank()) explanationId else explanationEn
}

object KoreanExamSeed {

    fun questionsFor(examType: ExamType): List<ExamQuestion> = when (examType) {
        ExamType.TOPIK_I -> Topik1MockBank.allQuestions
        ExamType.TOPIK_II -> Topik2MockBank.allQuestions
        ExamType.EPS_TOPIK -> EpsAndSpeakingMockBank.epsTopikQuestions
        ExamType.TOPIK_SPEAKING -> EpsAndSpeakingMockBank.topikSpeakingQuestions
    }
}
