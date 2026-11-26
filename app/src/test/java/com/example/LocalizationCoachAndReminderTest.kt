package com.example

import com.example.data.notifications.StudyReminderScheduler
import com.example.data.seed.KoreanSpeakingIndonesianCatalog
import com.example.data.seed.KoreanSpeakingSeed
import com.example.data.seed.localizedHint
import com.example.data.seed.localizedSetting
import com.example.data.seed.localizedTitle
import com.example.data.seed.localizedTranslation
import com.example.domain.hangul.HangulIndonesianCatalog
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.localizedExplanation
import com.example.domain.hangul.localizedPronunciationNote
import com.example.domain.hangul.localizedTitle
import com.example.domain.hangul.localizedUsageNote
import com.example.domain.speaking.AcousticCadenceAnalyzer
import com.example.domain.speaking.ConversationEngine
import com.example.domain.speaking.KoreanPronunciationEvaluator
import com.example.domain.speaking.LocalSampleKoreanAiCoachRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class LocalizationCoachAndReminderTest {

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
    fun conversationEngine_returnsIndonesianFeedbackAndAssessmentWhenEnabled() {
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
    }

    @Test
    fun localSampleAiCoach_providesCadenceAnalysisAndExplicitFirebaseNotice() = runTest {
        val coach = LocalSampleKoreanAiCoachRepository()
        val drill = KoreanPronunciationEvaluator.defaultDrills.first()
        val attempt = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf(drill.targetWrittenKo),
            confidenceScores = listOf(0.92f)
        )
        val voiceWave = listOf(0.18f, 0.42f, 0.65f, 0.54f, 0.38f, 0.25f)

        val feedback = coach.coachPronunciationAttempt(drill, attempt, voiceWave)
        assertTrue(feedback.isSampleMode)
        assertNotNull(feedback.energyScorePercent)
        assertTrue(feedback.serviceNoticeEn.contains("Firebase", ignoreCase = true))
        assertTrue(feedback.serviceNoticeId.contains("Firebase", ignoreCase = true))
        assertTrue(feedback.localizedCadence(true).contains("Energi suara"))

        val typedCadence = AcousticCadenceAnalyzer.analyze(emptyList(), 5, 0)
        assertNull(typedCadence.energyPercent)
        assertTrue(typedCadence.summaryId.contains("ketikan", ignoreCase = true))
    }

    @Test
    fun studyReminderScheduler_computesFutureTriggerAndFormatsTime() {
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
    }
}
