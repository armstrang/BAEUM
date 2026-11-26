package com.example

import com.example.data.local.CurriculumPathway
import com.example.data.seed.VocabAssetLoader
import com.example.domain.hangul.KoreanLinguisticsEngine
import com.example.domain.quiz.AdaptiveQuizEngine
import com.example.domain.quiz.QuizMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VocabWordBankTest {

    private fun resolveAssetFile(relativePath: String): File {
        val candidates = listOf(
            File("src/main/assets/$relativePath"),
            File("app/src/main/assets/$relativePath")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Asset file not found: $relativePath (cwd=${File(".").absolutePath})")
    }

    @Test
    fun assetTsvFilesAreWellFormedAndMeetTopikBenchmarks() {
        val t1File = resolveAssetFile("vocab/topik_1.tsv")
        val t2File = resolveAssetFile("vocab/topik_2.tsv")

        val allowedPos = setOf("N", "V", "A", "Adv", "Pron", "Det", "Expr", "Num", "Part", "Counter")
        val hangulRegex = Regex("[\\uAC00-\\uD7A3]")

        listOf(t1File, t2File).forEach { file ->
            file.readLines(Charsets.UTF_8).forEachIndexed { idx, raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed
                val parts = line.split('|')
                assertTrue("${file.name}:${idx + 1} expected >=4 columns: $line", parts.size >= 4)
                assertTrue("${file.name}:${idx + 1} missing Hangul: $line", hangulRegex.containsMatchIn(parts[0]))
                assertTrue("${file.name}:${idx + 1} blank English meaning: $line", parts[1].isNotBlank())
                assertTrue("${file.name}:${idx + 1} blank Indonesian meaning: $line", parts[2].isNotBlank())
                assertTrue("${file.name}:${idx + 1} invalid POS '${parts[3]}': $line", parts[3].trim() in allowedPos)
            }
        }

        val merged = VocabAssetLoader.loadMergedVocabulary(
            topik1Stream = t1File.inputStream(),
            topik2Stream = t2File.inputStream()
        )

        // Unique IDs and unique Hangul across the entire merged bank
        assertEquals("Duplicate IDs detected", merged.size, merged.map { it.id }.distinct().size)
        assertEquals("Duplicate Hangul detected", merged.size, merged.map { it.hangul.trim() }.distinct().size)

        val topik = merged.filter { it.curriculum == CurriculumPathway.TOPIK }
        val byLevel = topik.groupBy { it.subCategory }

        val c1 = byLevel["1급"]?.size ?: 0
        val c2 = byLevel["2급"]?.size ?: 0
        val c3 = byLevel["3급"]?.size ?: 0
        val c4 = byLevel["4급"]?.size ?: 0
        val c5 = byLevel["5급"]?.size ?: 0
        val c6 = byLevel["6급"]?.size ?: 0

        val cumul1 = c1
        val cumul2 = cumul1 + c2
        val cumul3 = cumul2 + c3
        val cumul6 = cumul3 + c4 + c5 + c6

        println("TOPIK counts: 1급=$c1, 2급=$c2 (cumul=$cumul2), 3급=$c3 (cumul=$cumul3), 4급=$c4, 5급=$c5, 6급=$c6 (total TOPIK=$cumul6, grand total=${merged.size})")

        // Verify user's explicit TOPIK benchmarks:
        // • TOPIK I – Level 1: ~800 words
        // • TOPIK I – Level 2: ~1,500–2,000 words
        // • TOPIK II – Level 3: ~3,000 words
        // • TOPIK II – Levels 4–6: ~4,000–5,000+ words
        assertTrue("Level 1급 ($c1) should be >= 800", c1 >= 800)
        assertTrue("TOPIK I cumulative ($cumul2) should be >= 1500", cumul2 >= 1500)
        assertTrue("TOPIK 1–3급 cumulative ($cumul3) should be >= 3000", cumul3 >= 3000)
        assertTrue("TOPIK 1–6급 total ($cumul6) should be >= 5000", cumul6 >= 5000)
        assertTrue("Level 4급 ($c4) should be >= 800", c4 >= 800)
        assertTrue("Level 5급 ($c5) should be >= 800", c5 >= 800)
        assertTrue("Level 6급 ($c6) should be >= 800", c6 >= 800)

        // Core beginner staples must appear in 1급 (or curated non-TOPIK Hangul/Everyday)
        val topikByHangul = topik.associateBy { it.hangul }
        listOf("학교", "먹다", "가다", "사람", "시간", "가족", "친구", "오늘").forEach { core ->
            val entry = topikByHangul[core]
            assertTrue("Core word '$core' missing from TOPIK", entry != null)
            assertEquals("Core word '$core' should be in 1급", "1급", entry!!.subCategory)
        }
    }

    @Test
    fun linguisticsEngineHandlesRomanizationConjugationAndParticles() {
        assertEquals("han-gu-geo", KoreanLinguisticsEngine.romanize("한국어"))
        assertEquals("ga-chi", KoreanLinguisticsEngine.romanize("같이"))
        assertTrue(KoreanLinguisticsEngine.detectPronunciationNote("같이").contains("Palatalization"))
        assertTrue(KoreanLinguisticsEngine.detectPronunciationNote("한국어").contains("Liaison"))

        // Regular & irregular conjugations
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("먹다", "Verb").startsWith("먹어요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("공부하다", "Verb").startsWith("공부해요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("가깝다", "Adjective", "ㅂ").startsWith("가까워요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("돕다", "Verb", "ㅂ").startsWith("도와요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("걷다", "Verb", "ㄷ").startsWith("걸어요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("모르다", "Verb", "르").startsWith("몰라요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("흐르다", "Verb", "르").startsWith("흘러요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("빨갛다", "Adjective", "ㅎ").startsWith("빨개요::"))
        assertTrue(KoreanLinguisticsEngine.buildConjugationsSerialized("켜다", "Verb").startsWith("켜요::"))

        // Particle accuracy
        val (exWithBatchim, _, _) = KoreanLinguisticsEngine.buildContextualExample("책", "Book", "Buku", "Noun", "1급")
        assertTrue("Expected subject particle 이 for 책: $exWithBatchim", exWithBatchim.contains("책이"))
        val (exNoBatchim, _, _) = KoreanLinguisticsEngine.buildContextualExample("사과", "Apple", "Apel", "Noun", "1급")
        assertTrue("Expected subject particle 가 for 사과: $exNoBatchim", exNoBatchim.contains("사과가"))
    }

    @Test
    fun adaptiveQuizEnginePerformsFastAcrossFullWordBank() {
        val t1File = resolveAssetFile("vocab/topik_1.tsv")
        val t2File = resolveAssetFile("vocab/topik_2.tsv")
        val merged = VocabAssetLoader.loadMergedVocabulary(
            topik1Stream = t1File.inputStream(),
            topik2Stream = t2File.inputStream()
        )

        val startMs = System.currentTimeMillis()
        QuizMode.entries.forEach { mode ->
            val questions = AdaptiveQuizEngine.generateQuestions(
                mode = mode,
                allVocab = merged,
                translationLang = "EN",
                maxCount = 8
            )
            assertTrue("Mode $mode generated empty questions", questions.isNotEmpty())
            questions.forEach { q ->
                if (q.isArrangementMode) {
                    assertEquals("Mode $mode arrangement tokens must match canonical sentence", q.arrangedTokens.sorted(), q.options.sorted())
                    assertEquals("Mode $mode correctAnswer must equal joined tokens", q.arrangedTokens.joinToString(" "), q.correctAnswer)
                } else if (!q.isTypingMode) {
                    assertTrue("Mode $mode options must include correct answer", q.correctAnswer in q.options)
                    assertEquals("Mode $mode options must be unique", q.options.size, q.options.distinct().size)
                }
            }
        }
        val elapsedMs = System.currentTimeMillis() - startMs
        println("Generated all ${QuizMode.entries.size} quiz modes across ${merged.size} words in ${elapsedMs}ms")
        assertTrue("Quiz generation across full bank took too long: ${elapsedMs}ms", elapsedMs < 2000L)
    }
}
