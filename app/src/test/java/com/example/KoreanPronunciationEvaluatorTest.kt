package com.example

import com.example.domain.speaking.KoreanPronunciationEvaluator
import com.example.domain.speaking.PhoneticIssueType
import com.example.domain.speaking.SyllableMatchLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanPronunciationEvaluatorTest {

    @Test
    fun exactMatch_returns100AndPassed() {
        val drill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "ep_annyeong" }
        val result = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf("안녕하세요")
        )
        assertEquals(100, result.matchScorePercent)
        assertTrue(result.passed)
        assertTrue(result.isExactMatch)
        assertEquals("안녕하세요", result.bestRecognizedText)
        assertTrue(result.syllableBreakdown.all { it.matchLevel == SyllableMatchLevel.EXACT })
    }

    @Test
    fun soundChange_pronouncedFormMatchesCleanly() {
        val gachiDrill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "sc_gachi" }
        val resultGachi = KoreanPronunciationEvaluator.evaluate(
            drill = gachiDrill,
            alternatives = listOf("가치")
        )
        assertEquals(100, resultGachi.matchScorePercent)
        assertTrue(resultGachi.passed)
        assertTrue(resultGachi.isExactMatch)

        val hangugeoDrill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "sc_hangugeo" }
        val resultHangugeo = KoreanPronunciationEvaluator.evaluate(
            drill = hangugeoDrill,
            alternatives = listOf("한구거")
        )
        assertEquals(100, resultHangugeo.matchScorePercent)
        assertTrue(resultHangugeo.passed)
    }

    @Test
    fun nBestAlternatives_picksBestMatchingCandidate() {
        val drill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "mc_ttal" }
        val result = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf("달", "딸", "탈")
        )
        assertEquals(100, result.matchScorePercent)
        assertEquals("딸", result.bestRecognizedText)
        assertTrue(result.passed)
    }

    @Test
    fun minimalPairConsonantConfusion_detectsConsonantSeriesIssue() {
        val drill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "mc_kkada" }
        val result = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf("가다")
        )
        assertFalse(result.isExactMatch)
        val firstSyllable = result.syllableBreakdown.first()
        assertEquals('까', firstSyllable.targetSyllable)
        assertEquals('가', firstSyllable.recognizedSyllable)
        assertEquals(PhoneticIssueType.CONSONANT_SERIES, firstSyllable.issueType)
        assertTrue(result.feedbackId.contains("konsonan", ignoreCase = true))
    }

    @Test
    fun batchimOmission_flagsBatchimDifference() {
        val drill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "mc_dal" }
        val result = KoreanPronunciationEvaluator.evaluate(
            drill = drill,
            alternatives = listOf("다")
        )
        assertFalse(result.passed)
        assertEquals(PhoneticIssueType.BATCHIM_DIFFERENCE, result.syllableBreakdown.first().issueType)
    }

    @Test
    fun emptyOrNonKoreanInput_handledSafely() {
        val drill = KoreanPronunciationEvaluator.defaultDrills.first { it.id == "mc_gada" }
        val emptyResult = KoreanPronunciationEvaluator.evaluate(drill, emptyList())
        assertEquals(0, emptyResult.matchScorePercent)
        assertFalse(emptyResult.passed)

        val latinResult = KoreanPronunciationEvaluator.evaluate(drill, listOf("hello world"))
        assertEquals(0, latinResult.matchScorePercent)
        assertFalse(latinResult.passed)
    }
}
