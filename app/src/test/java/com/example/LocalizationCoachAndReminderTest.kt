package com.example

import com.example.data.local.CurriculumPathway
import com.example.data.local.VocabEntity
import com.example.data.notifications.StudyReminderBootReceiver
import com.example.data.notifications.StudyReminderScheduler
import com.example.data.repository.BaeumBackupPayload
import com.example.data.repository.BaeumRepository
import com.example.data.repository.GrammarBookmarkBackup
import com.example.data.repository.ReviewLogBackup
import com.example.data.repository.VocabProgressBackup
import com.example.data.seed.ExamType
import com.example.data.seed.KoreanSpeakingIndonesianCatalog
import com.example.data.seed.KoreanSpeakingSeed
import com.example.data.seed.VocabAssetLoader
import com.example.data.seed.localizedHint
import com.example.data.seed.localizedSetting
import com.example.data.seed.localizedTitle
import com.example.data.seed.localizedTranslation
import com.example.domain.exam.ExamEngine
import com.example.domain.exam.ExamSessionMode
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsCardSnapshot
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduler
import com.example.domain.hangul.HangulIndonesianCatalog
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.KoreanLinguisticsEngine
import com.example.domain.hangul.localizedExplanation
import com.example.domain.hangul.localizedPronunciationNote
import com.example.domain.hangul.localizedTitle
import com.example.domain.hangul.localizedUsageNote
import com.example.domain.quiz.AdaptiveQuizEngine
import com.example.domain.quiz.QuizMode
import com.example.domain.speaking.ConversationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Calendar
import kotlin.random.Random

class LocalizationCoachAndReminderTest {

    private fun resolveAsset(rel: String): File =
        listOf(File("src/main/assets/$rel"), File("app/src/main/assets/$rel"))
            .first { it.exists() }

    private fun loadWordBank(): List<VocabEntity> = VocabAssetLoader.loadMergedVocabulary(
        topik1Stream = resolveAsset("vocab/topik_1.tsv").inputStream(),
        topik2Stream = resolveAsset("vocab/topik_2.tsv").inputStream()
    )

    @Test
    fun indonesianSpeakingCatalog_coversAll30ScenariosAndAllTurns() {
        val scenarios = KoreanSpeakingSeed.allScenarios
        assertEquals(30, scenarios.size)

        scenarios.forEach { scenario ->
            assertTrue(
                "Missing Indonesian scenario mapping for ${scenario.id}",
                KoreanSpeakingIndonesianCatalog.scenarioMap.containsKey(scenario.id)
            )
            assertTrue(scenario.localizedTitle(true).isNotBlank())
            assertTrue(scenario.localizedSetting(true).isNotBlank())
            assertNotEquals(
                "Indonesian title should differ from English for ${scenario.id}",
                scenario.title,
                scenario.localizedTitle(true)
            )

            scenario.turns.forEach { turn ->
                assertTrue(
                    "Missing Indonesian turn mapping for ${turn.turnId}",
                    KoreanSpeakingIndonesianCatalog.turnMap.containsKey(turn.turnId)
                )
                assertTrue(turn.localizedTranslation(true).isNotBlank())
                assertTrue(turn.localizedHint(true).isNotBlank())
                assertNotEquals(
                    "Indonesian translation should differ from English for ${turn.turnId}",
                    turn.translationEn,
                    turn.localizedTranslation(true)
                )
            }
        }
    }

    @Test
    fun indonesianHangulCatalog_coversAllSoundRulesJamoAndNumbers() {
        HangulUnicodeEngine.soundChangeRules.forEach { rule ->
            assertTrue(HangulIndonesianCatalog.soundChangeRulesId.containsKey(rule.id))
            assertNotEquals(rule.title, rule.localizedTitle(true))
            assertNotEquals(rule.explanation, rule.localizedExplanation(true))
        }

        val allJamo = HangulUnicodeEngine.basicConsonants +
            HangulUnicodeEngine.doubleConsonants +
            HangulUnicodeEngine.basicVowels +
            HangulUnicodeEngine.compoundVowels +
            HangulUnicodeEngine.batchimGroups

        allJamo.forEach { jamo ->
            assertNotEquals(
                "Missing Indonesian note for ${jamo.glyph}",
                jamo.pronunciationNote,
                jamo.localizedPronunciationNote(true)
            )
        }

        HangulUnicodeEngine.koreanNumbers.forEach { num ->
            assertNotEquals(num.usageNote, num.localizedUsageNote(true))
        }
    }

    @Test
    fun conversationEngine_returnsIndonesianFeedbackAndEnforcesLinguisticAccuracy() {
        val scenario = KoreanSpeakingSeed.allScenarios.first { it.id == "beg_cafe" }
        val prompt = scenario.turns.first()
        val evaluated = ConversationEngine.evaluateResponse(
            prompt = prompt,
            userText = "아이스 아메리카노 한 잔 주세요.",
            wasManualOrEdited = false,
            isIndonesian = true
        )

        assertTrue(evaluated.scoreOutOf10 >= 8)
        assertTrue(
            "Expected Indonesian turn feedback, got: ${evaluated.feedbackNote}",
            evaluated.feedbackNote.contains("konteks") || evaluated.feedbackNote.contains("kesopanan")
        )

        val state = ConversationEngine.commitTurnAndAdvance(
            ConversationEngine.startScenario(scenario),
            evaluated
        )
        val assessment = ConversationEngine.buildFinalAssessment(state, isIndonesian = true)
        assertEquals("Di Kafe Korea", assessment.scenarioTitle)
        assertTrue(assessment.bandLabel.contains("Komunikasi"))
        assertTrue(assessment.pronunciationStatus.contains("Tidak dinilai"))

        // Bare noun list should not trigger false-positive particle/grammar inflation
        val bareNouns = ConversationEngine.evaluateResponse(
            prompt = prompt,
            userText = "사과 고기 라면 가방",
            wasManualOrEdited = true,
            isIndonesian = false
        )
        assertTrue("Bare nouns must not get high grammar score, got ${bareNouns.grammarScore}", bareNouns.grammarScore <= 5)

        // Repeated-syllable spam must be heavily penalized
        val spam = ConversationEngine.evaluateResponse(
            prompt = prompt,
            userText = "가가가가가가가가가가가가",
            wasManualOrEdited = true,
            isIndonesian = false
        )
        assertTrue("Spam response must score <= 3, got ${spam.scoreOutOf10}", spam.scoreOutOf10 <= 3)

        // Formal 합니다체 prompt with 감사합니다! gets full speech level credit
        val formalPrompt = KoreanSpeakingSeed.allScenarios
            .flatMap { it.turns }
            .first { it.expectedSpeechLevel == "합니다체" }
        val formalEval = ConversationEngine.evaluateResponse(
            prompt = formalPrompt,
            userText = "알겠습니다. 정말 감사합니다!",
            wasManualOrEdited = false,
            isIndonesian = false
        )
        assertEquals(10, formalEval.speechLevelScore)
    }

    @Test
    fun koreanLinguisticsEngine_handlesPhonologyConjugationParticlesAndDiscourseAccurately() {
        // Liquidization, 2-way aspiration, ㅎ-drop, double-batchim liaison, ㄹ-nasalization
        assertEquals("sil-la", KoreanLinguisticsEngine.romanize("신라"))
        assertEquals("seol-lal", KoreanLinguisticsEngine.romanize("설날"))
        assertEquals("jo-ta", KoreanLinguisticsEngine.romanize("좋다"))
        assertEquals("jo-a-yo", KoreanLinguisticsEngine.romanize("좋아요"))
        assertEquals("man-ta", KoreanLinguisticsEngine.romanize("많다"))
        assertEquals("sil-ta", KoreanLinguisticsEngine.romanize("싫다"))
        assertEquals("il-geo-yo", KoreanLinguisticsEngine.romanize("읽어요"))
        assertEquals("eop-sseo-yo", KoreanLinguisticsEngine.romanize("없어요"))
        assertEquals("eum-nyo-su", KoreanLinguisticsEngine.romanize("음료수"))
        assertEquals("dong-nip", KoreanLinguisticsEngine.romanize("독립"))
        assertEquals("hyeom-nyeok", KoreanLinguisticsEngine.romanize("협력"))

        // ㅚ -> ㅙ contraction, auto-detected irregulars, and suppletive honorifics
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("되다", "Verb").startsWith("돼요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("춥다", "Adjective").startsWith("추워요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("듣다", "Verb").startsWith("들어요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("먹다", "Verb").contains("드세요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("자다", "Verb").contains("주무세요::"))

        // ㄹ-batchim 으로/로 particle harmony
        assertEquals("서울로", KoreanLinguisticsEngine.attachParticle("서울", "으로", "로"))
        assertEquals("집으로", KoreanLinguisticsEngine.attachParticle("집", "으로", "로"))
        assertEquals("학교로", KoreanLinguisticsEngine.attachParticle("학교", "으로", "로"))

        // Token-boundary particle & Jamo ending checks
        assertEquals(0, KoreanLinguisticsEngine.countWordFinalParticles("사과 고기 라면 가방"))
        assertTrue(KoreanLinguisticsEngine.countWordFinalParticles("친구가 학교에서 책을 읽어요") >= 3)
        assertTrue(KoreanLinguisticsEngine.hasFormalPoliteEnding("갑니다!"))
        assertTrue(KoreanLinguisticsEngine.hasFormalPoliteEnding("있습니까?"))
        assertTrue(KoreanLinguisticsEngine.hasPoliteHaeyoEnding("고마워요!"))
        assertTrue(KoreanLinguisticsEngine.hasWrittenPlainEnding("인구가 줄어든다."))
    }

    @Test
    fun fsrsScheduler_sanitizesCorruptedStatesAndCapsStabilityAndIntervals() {
        val now = 1_750_000_000_000L

        // Corrupted NaN/Infinity/zero-difficulty card must never propagate NaN or crash
        val corrupted = FsrsCardSnapshot(
            stability = Double.NaN,
            difficulty = Double.POSITIVE_INFINITY,
            reps = 3,
            lapses = -2,
            stage = CardMemoryStage.REVIEW,
            lastReviewEpochMs = now - 86_400_000L,
            dueEpochMs = now
        )
        FsrsRating.entries.forEach { rating ->
            val res = FsrsScheduler.schedule(corrupted, rating, nowEpochMs = now, desiredRetention = Double.NaN)
            assertTrue(res.nextState.stability.isFinite())
            assertTrue(res.nextState.difficulty.isFinite())
            assertTrue(res.intervalDays.isFinite())
            assertTrue(res.nextState.stability in 0.1..3650.0)
            assertTrue(res.nextState.difficulty in 1.0..10.0)
            assertTrue(res.intervalDays in 0.0..3650.0)
            assertTrue(res.nextState.lapses >= 0)
        }

        // Same-day AGAIN must never increase stability
        val sameDayCard = FsrsCardSnapshot(
            stability = 12.0,
            difficulty = 5.0,
            reps = 2,
            stage = CardMemoryStage.REVIEW,
            lastReviewEpochMs = now - 300_000L,
            dueEpochMs = now
        )
        val againRes = FsrsScheduler.schedule(sameDayCard, FsrsRating.AGAIN, nowEpochMs = now)
        assertTrue(againRes.nextState.stability <= 12.0)

        // Extreme stability + EASY multiplier must still obey 3650-day hard cap
        val maxCard = FsrsCardSnapshot(
            stability = 3650.0,
            difficulty = 1.0,
            reps = 20,
            stage = CardMemoryStage.MASTERED,
            lastReviewEpochMs = now - 3000L * 86_400_000L,
            dueEpochMs = now
        )
        val easyRes = FsrsScheduler.schedule(maxCard, FsrsRating.EASY, nowEpochMs = now)
        assertTrue(easyRes.nextState.stability <= 3650.0)
        assertTrue(easyRes.intervalDays <= 3650.0)
    }

    @Test
    fun quizAndExamEngines_enforceLinguisticClozeAlignmentAndNoListeningAnswerLeak() {
        val bank = loadWordBank()

        // Cloze distractor alignment attaches matching batchim-aware particle
        val target = VocabEntity(
            id = "t1", hangul = "학교", romanization = "hak-gyo", meaningEn = "School", meaningId = "Sekolah",
            partOfSpeech = "Noun", curriculum = CurriculumPathway.TOPIK, subCategory = "1급",
            exampleKo = "저는 학교로 갑니다.", exampleEn = "I go to school.", exampleId = "Saya pergi ke sekolah.",
            pronunciationNote = ""
        )
        val seoul = target.copy(id = "d1", hangul = "서울")
        val house = target.copy(id = "d2", hangul = "집")
        assertEquals("서울로", AdaptiveQuizEngine.alignDistractorToClozeToken(target, "학교로", seoul))
        assertEquals("집으로", AdaptiveQuizEngine.alignDistractorToClozeToken(target, "학교로", house))

        // Conjugation quiz prioritizes same-verb inflections
        val conjQs = AdaptiveQuizEngine.generateQuestions(
            mode = QuizMode.CONJUGATION,
            allVocab = bank,
            translationLang = "EN",
            maxCount = 6,
            rng = Random(12)
        )
        assertTrue(conjQs.isNotEmpty())
        conjQs.forEach { q ->
            assertEquals(4, q.options.size)
            assertTrue(q.correctAnswer in q.options)
        }

        // Dynamic listening exam items must NOT leak the target word in stimulusKo
        val topik1Mock = ExamEngine.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK, bank, Random(77))
        val dynamicListening = topik1Mock.questions.filter { it.section == "Listening" && it.id.startsWith("dyn_") }
        assertTrue("Expected dynamic listening items", dynamicListening.isNotEmpty())
        dynamicListening.forEach { q ->
            val correctWord = q.options[q.correctOptionIndex]
            assertFalse(
                "Listening stimulus '${q.stimulusKo}' leaked answer '$correctWord'",
                q.stimulusKo.contains(correctWord)
            )
            assertNotNull(q.audioTranscriptKo)
        }
    }

    @Test
    fun backupPayload_v2RoundTripPreservesBookmarksAndReviewLogsAndSupportsV1() {
        val payload = BaeumBackupPayload(
            schemaVersion = BaeumRepository.CURRENT_BACKUP_SCHEMA_VERSION,
            exportedAtEpochMs = 1_750_000_000_000L,
            vocabStates = listOf(
                VocabProgressBackup("v1", true, 12.5, 4.8, 3.0, 12.0, 4, 0, "REVIEW", 1_749_900_000_000L, 1_751_000_000_000L, 3, 1)
            ),
            dailyStats = emptyList(),
            quizRecords = emptyList(),
            speakingSessions = emptyList(),
            examAttempts = emptyList(),
            grammarBookmarks = listOf(GrammarBookmarkBackup("gr_eun_neun", true, 1_749_950_000_000L)),
            reviewLogs = listOf(ReviewLogBackup("v1", 3, "LEARNING", "REVIEW", 4.0, 1_749_900_000_000L, "2026-11-25"))
        )

        val json = BaeumRepository.serializeBackupPayload(payload)
        val parsed = BaeumRepository.parseBackupJson(json)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.schemaVersion)
        assertEquals(1, parsed.grammarBookmarks.size)
        assertEquals("gr_eun_neun", parsed.grammarBookmarks.first().grammarId)
        assertEquals(1, parsed.reviewLogs.size)
        assertEquals("v1", parsed.reviewLogs.first().vocabId)

        // Legacy v1 JSON without grammarBookmarks or reviewLogs parses cleanly with empty lists
        val legacyV1 = """
            {
              "schemaVersion": 1,
              "exportedAtEpochMs": 1750000000000,
              "vocabStates": [],
              "dailyStats": [],
              "quizRecords": [],
              "speakingSessions": [],
              "examAttempts": []
            }
        """.trimIndent()
        val parsedV1 = BaeumRepository.parseBackupJson(legacyV1)
        assertNotNull(parsedV1)
        assertEquals(1, parsedV1!!.schemaVersion)
        assertTrue(parsedV1.grammarBookmarks.isEmpty())
        assertTrue(parsedV1.reviewLogs.isEmpty())
    }

    @Test
    fun studyReminderSchedulerAndBootReceiver_handleFutureTriggerAndSystemBroadcasts() {
        val base = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 26, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 19:00 on the same day should be 5 hours ahead
        val sameDayTrigger = StudyReminderScheduler.computeNextTriggerMillis(19, 0, base)
        assertEquals(5L * 60L * 60L * 1000L, sameDayTrigger - base)

        // 08:00 (already passed today at 14:00) should roll to tomorrow (18 hours ahead)
        val nextDayTrigger = StudyReminderScheduler.computeNextTriggerMillis(8, 0, base)
        assertEquals(18L * 60L * 60L * 1000L, nextDayTrigger - base)

        assertEquals("08:05", StudyReminderScheduler.formatReminderTime(8, 5))
        assertEquals("19:00", StudyReminderScheduler.formatReminderTime(19, 0))

        // Boot receiver covers reboot, package update, clock change, and timezone change
        assertTrue("android.intent.action.BOOT_COMPLETED" in StudyReminderBootReceiver.ALLOWED_ACTIONS)
        assertTrue("android.intent.action.MY_PACKAGE_REPLACED" in StudyReminderBootReceiver.ALLOWED_ACTIONS)
        assertTrue("android.intent.action.TIME_SET" in StudyReminderBootReceiver.ALLOWED_ACTIONS)
        assertTrue("android.intent.action.TIMEZONE_CHANGED" in StudyReminderBootReceiver.ALLOWED_ACTIONS)
    }
}
