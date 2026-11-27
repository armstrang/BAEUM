package com.example

import com.example.data.seed.EpsAndSpeakingMockBank
import com.example.data.seed.ExamPaperLength
import com.example.data.seed.ExamType
import com.example.data.seed.Topik1MockBank
import com.example.data.seed.Topik2MockBank
import com.example.domain.exam.ExamEngine
import com.example.domain.exam.ExamSessionMode
import com.example.domain.scoring.KoreanRegisterTarget
import com.example.domain.scoring.LearningScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TopikAuthenticMockTest {

    @Test
    fun allAuthenticExamBanks_haveValidIntegrityUnderlinesAndBilingualExplanations() {
        val allBankQuestions = Topik1MockBank.allQuestions +
            Topik2MockBank.listeningQuestions +
            Topik2MockBank.writingTask51Variants +
            Topik2MockBank.writingTask52Variants +
            Topik2MockBank.writingTask53Variants +
            Topik2MockBank.writingTask54Variants +
            Topik2MockBank.readingQuestions +
            EpsAndSpeakingMockBank.epsTopikQuestions +
            EpsAndSpeakingMockBank.speakingTask1Variants +
            EpsAndSpeakingMockBank.speakingTask2Variants +
            EpsAndSpeakingMockBank.speakingTask3Variants +
            EpsAndSpeakingMockBank.speakingTask4Variants +
            EpsAndSpeakingMockBank.speakingTask5Variants +
            EpsAndSpeakingMockBank.speakingTask6Variants

        // Unique IDs across the entire exam corpus
        val uniqueIds = allBankQuestions.map { it.id }.toSet()
        assertEquals("Duplicate exam question ID detected", allBankQuestions.size, uniqueIds.size)

        val englishHintLeakRegex = Regex("\\([A-Za-z ,'.?!-]{10,}\\)")

        allBankQuestions.forEach { q ->
            assertTrue("${q.id}: explanationEn must not be blank", q.explanationEn.isNotBlank())
            assertTrue("${q.id}: explanationId must not be blank", q.explanationId.isNotBlank())
            assertFalse(
                "${q.id}: stimulusKo must not contain English translation hint leak: ${q.stimulusKo}",
                englishHintLeakRegex.containsMatchIn(q.stimulusKo)
            )

            // Every underlinedText must exist verbatim in stimulusKo or passageBoxKo
            val underline = q.underlinedText
            if (underline != null) {
                val found = q.stimulusKo.contains(underline) || (q.passageBoxKo?.contains(underline) == true)
                assertTrue("${q.id}: underlinedText '$underline' not found in stimulusKo or passageBoxKo", found)
            }

            if (q.isOpenEnded) {
                assertEquals("${q.id}: open-ended correctOptionIndex must be -1", -1, q.correctOptionIndex)
                assertTrue("${q.id}: open-ended question must have expectedKeywords", q.expectedKeywords.isNotEmpty())
                assertTrue("${q.id}: open-ended question must have modelAnswerKo", !q.modelAnswerKo.isNullOrBlank())
            } else {
                assertEquals("${q.id}: MC question must have 4 options", 4, q.options.size)
                assertEquals("${q.id}: MC options must be distinct", 4, q.options.distinct().size)
                assertTrue("${q.id}: correctOptionIndex out of range", q.correctOptionIndex in 0..3)
                q.options.forEach { opt ->
                    assertTrue(
                        "${q.id}: MC option '$opt' must contain Korean Hangul (no English/ID options)",
                        opt.any { ch -> ch.code in 0xAC00..0xD7A3 || ch.code in 0x3131..0x3163 }
                    )
                }
            }
        }
    }

    @Test
    fun fullOfficialSimulations_matchExactTopikAndEpsSpecifications() {
        // 1. TOPIK I Full Official Mock: 70 questions (30 Listening = 100 pts, 40 Reading = 100 pts), 100 min
        val t1Full = ExamEngine.startExam(
            examType = ExamType.TOPIK_I,
            mode = ExamSessionMode.MOCK,
            rng = Random(11),
            paperLength = ExamPaperLength.FULL_SIMULATION
        )
        assertEquals(70, t1Full.questions.size)
        assertEquals(100, t1Full.period1DurationMinutes)
        assertEquals(1, t1Full.totalPeriods)
        val t1Listen = t1Full.questions.filter { it.section == "Listening" }
        val t1Read = t1Full.questions.filter { it.section == "Reading" }
        assertEquals(30, t1Listen.size)
        assertEquals(40, t1Read.size)
        assertEquals(100, t1Listen.sumOf { it.pointWeight })
        assertEquals(100, t1Read.sumOf { it.pointWeight })
        assertTrue(t1Listen.all { it.maxAudioPlaysInMock == 2 })

        // 2. TOPIK II Full Official Mock: 2 Periods — P1 (30 Listening [12x1회 + 18x2회] + 4 Writing [10+10+30+50=100]), P2 (30 Reading)
        val t2Full = ExamEngine.startExam(
            examType = ExamType.TOPIK_II,
            mode = ExamSessionMode.MOCK,
            rng = Random(22),
            paperLength = ExamPaperLength.FULL_SIMULATION
        )
        assertEquals(64, t2Full.questions.size)
        assertEquals(2, t2Full.totalPeriods)
        assertEquals(90, t2Full.period1DurationMinutes)
        assertEquals(60, t2Full.period2DurationMinutes)
        val t2Listen = t2Full.questions.filter { it.section == "Listening" }
        val t2Write = t2Full.questions.filter { it.section == "Writing" }
        val t2Read = t2Full.questions.filter { it.section == "Reading" }
        assertEquals(30, t2Listen.size)
        assertEquals(12, t2Listen.count { it.maxAudioPlaysInMock == 1 })
        assertEquals(18, t2Listen.count { it.maxAudioPlaysInMock == 2 })
        assertEquals(4, t2Write.size)
        assertEquals(listOf(51, 52, 53, 54), t2Write.map { it.writingTaskNumber })
        assertEquals(100, t2Write.sumOf { it.pointWeight })
        assertEquals(30, t2Read.size)
        assertTrue(t2Listen.all { it.period == 1 })
        assertTrue(t2Write.all { it.period == 1 })
        assertTrue(t2Read.all { it.period == 2 })

        // 3. EPS-TOPIK Full Official Mock: 40 questions (20 Reading first + 20 Listening second, 5 pts each = 200 pts), 50 min
        val epsFull = ExamEngine.startExam(
            examType = ExamType.EPS_TOPIK,
            mode = ExamSessionMode.MOCK,
            rng = Random(33),
            paperLength = ExamPaperLength.FULL_SIMULATION
        )
        assertEquals(40, epsFull.questions.size)
        assertEquals(50, epsFull.period1DurationMinutes)
        assertEquals("Reading", epsFull.questions.first().section)
        assertEquals("Listening", epsFull.questions.last().section)
        assertEquals(100, epsFull.questions.filter { it.section == "Reading" }.sumOf { it.pointWeight })
        assertEquals(100, epsFull.questions.filter { it.section == "Listening" }.sumOf { it.pointWeight })

        // 4. TOPIK Speaking Full: 6 Official NIIED tasks totaling 200 pts with prep & speak timing
        val spkFull = ExamEngine.startExam(
            examType = ExamType.TOPIK_SPEAKING,
            mode = ExamSessionMode.MOCK,
            rng = Random(44),
            paperLength = ExamPaperLength.FULL_SIMULATION
        )
        assertEquals(6, spkFull.questions.size)
        assertEquals(200, spkFull.questions.sumOf { it.pointWeight })
        assertTrue(spkFull.questions.all { it.prepSeconds > 0 && it.speakSeconds > 0 })
    }

    @Test
    fun topik2PeriodTransition_locksPeriod1AndResetsTimerForPeriod2Reading() {
        val startMs = 1_750_000_000_000L
        val session = ExamEngine.startExam(ExamType.TOPIK_II, ExamSessionMode.MOCK, rng = Random(55))
        assertEquals(1, session.activePeriod)
        assertTrue(session.hasNextPeriod)

        val firstPeriod2Idx = session.questions.indexOfFirst { it.period == 2 }
        assertTrue(firstPeriod2Idx > 0)
        assertFalse("Period 2 items must not be accessible during Period 1", session.isQuestionAccessible(firstPeriod2Idx))
        assertFalse("Period 1 items must not be locked during Period 1", session.isQuestionLocked(0))

        val intermission = ExamEngine.enterPeriodTransition(session)
        assertTrue(intermission.awaitingPeriodTransition)

        val period2 = ExamEngine.startNextPeriod(intermission, nowEpochMs = startMs)
        assertEquals(2, period2.activePeriod)
        assertFalse(period2.awaitingPeriodTransition)
        assertEquals(firstPeriod2Idx, period2.currentIndex)
        assertTrue("Period 1 items must be locked once Period 2 begins", period2.isQuestionLocked(0))
        assertFalse("Period 1 items must not be accessible once Period 2 begins", period2.isQuestionAccessible(0))
        assertTrue("Period 2 items must be accessible during Period 2", period2.isQuestionAccessible(firstPeriod2Idx))
        assertEquals(startMs + period2.period2DurationMinutes * 60_000L, period2.deadlineEpochMs)
    }

    @Test
    fun audioPlaybackQuotaAndSharedGroups_workAccuratelyAcrossMockAndPractice() {
        val mockT2 = ExamEngine.startExam(ExamType.TOPIK_II, ExamSessionMode.MOCK, rng = Random(88))
        val onePlayQ = mockT2.questions.first { it.section == "Listening" && it.maxAudioPlaysInMock == 1 }
        val twoPlayQ = mockT2.questions.first { it.section == "Listening" && it.maxAudioPlaysInMock == 2 }

        assertEquals(1, mockT2.remainingAudioPlays(onePlayQ))
        assertEquals(2, mockT2.remainingAudioPlays(twoPlayQ))
        assertTrue(mockT2.canPlayAudio(onePlayQ))

        val afterOnePlay = mockT2.copy(
            audioPlayCounts = mockT2.audioPlayCounts + (mockT2.audioQuotaKey(onePlayQ) to 1)
        )
        assertEquals(0, afterOnePlay.remainingAudioPlays(onePlayQ))
        assertFalse("1회 audio must lock after 1 play in Mock mode", afterOnePlay.canPlayAudio(onePlayQ))

        // Practice mode allows unlimited replays (remainingAudioPlays == null)
        val practiceT2 = ExamEngine.startExam(ExamType.TOPIK_II, ExamSessionMode.PRACTICE, rng = Random(88))
        val practiceAudioQ = practiceT2.questions.first { it.audioTranscriptKo != null }
        assertNull(practiceT2.remainingAudioPlays(practiceAudioQ))
        assertTrue(practiceT2.canPlayAudio(practiceAudioQ))

        // Group-preserving sampling never splits sharedGroupId bundles across multiple seeds
        for (seed in 1..15) {
            val compactT1 = ExamEngine.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK, rng = Random(seed))
            val sampledGroups = compactT1.questions.mapNotNull { it.sharedGroupId }.distinct()
            sampledGroups.forEach { gId ->
                val expectedCount = Topik1MockBank.allQuestions.count { it.sharedGroupId == gId }
                val actualCount = compactT1.questions.count { it.sharedGroupId == gId }
                assertEquals("Shared group $gId was split in seed $seed", expectedCount, actualCount)
            }
        }
    }

    @Test
    fun dualBlankAndEssayRubrics_gradeTasks51To54FairlyWithIndonesianLocalization() {
        val formatted = ExamEngine.formatDualBlankResponse("신청해 주시기 바랍니다", "문의해 주시기 바랍니다")
        val (g, n) = ExamEngine.parseDualBlankResponse(formatted)
        assertEquals("신청해 주시기 바랍니다", g)
        assertEquals("문의해 주시기 바랍니다", n)

        // Task 51 short dual-blank response should score strongly without being unfairly penalized for essay length
        val q51 = Topik2MockBank.writingTask51Variants.first()
        val r51 = LearningScoringEngine.evaluateKoreanOpenResponse(
            text = formatted,
            expectedKeywords = q51.expectedKeywords,
            targetMinHangul = q51.targetMinChars,
            expectedRegister = KoreanRegisterTarget.FORMAL_POLITE,
            targetMaxChars = q51.targetMaxChars,
            isShortDualBlank = true,
            isIndonesian = true
        )
        assertTrue("Well-written Task 51 dual blank should score >= 0.75, got ${r51.totalRatio}", r51.totalRatio >= 0.75)

        // Task 54 600~700자: short 2-sentence stub must be penalized on completeness, while full model answer excels
        val q54 = Topik2MockBank.writingTask54Variants.first()
        val stub54 = LearningScoringEngine.evaluateKoreanOpenResponse(
            text = "인공지능 기술은 편리하다. 하지만 문제도 있어서 노력이 필요하다.",
            expectedKeywords = q54.expectedKeywords,
            targetMinHangul = q54.targetMinChars,
            expectedRegister = KoreanRegisterTarget.FORMAL_WRITTEN,
            targetMaxChars = q54.targetMaxChars,
            isShortDualBlank = false,
            isIndonesian = true
        )
        val model54 = LearningScoringEngine.evaluateKoreanOpenResponse(
            text = q54.modelAnswerKo!!,
            expectedKeywords = q54.expectedKeywords,
            targetMinHangul = q54.targetMinChars,
            expectedRegister = KoreanRegisterTarget.FORMAL_WRITTEN,
            targetMaxChars = q54.targetMaxChars,
            isShortDualBlank = false,
            isIndonesian = true
        )
        assertTrue("Short stub on Task 54 must have completeness < 45, got ${stub54.completenessScore}", stub54.completenessScore < 45)
        assertTrue("Full model answer on Task 54 must score >= 0.85, got ${model54.totalRatio}", model54.totalRatio >= 0.85)
        assertTrue("Indonesian feedback expected", stub54.feedback.contains("karakter") || stub54.feedback.contains("panjang"))

        // Indonesian grading report uses Indonesian band & section labels
        val t1 = ExamEngine.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK, rng = Random(99))
        val allCorrect = t1.questions.associate { it.id to it.correctOptionIndex }
        val reportId = ExamEngine.gradeSession(t1.copy(selectedOptions = allCorrect), translationLang = "ID")
        assertNotNull(reportId.cutoffLadder.firstOrNull())
        assertTrue(reportId.estimatedLevel.contains("Latihan"))
        assertTrue(reportId.analysisSummary.contains("Menyimak") || reportId.analysisSummary.contains("Skor"))
    }
}
