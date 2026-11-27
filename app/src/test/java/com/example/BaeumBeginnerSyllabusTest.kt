package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import com.example.domain.curriculum.BossQuestionKind
import com.example.domain.curriculum.RomanizationVisibility
import com.example.domain.curriculum.StageBossEngine
import com.example.domain.curriculum.SyllabusMistakeItem
import com.example.domain.curriculum.SyllabusTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class BaeumBeginnerSyllabusTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createPrefsRepo(scope: TestScope, name: String): DataStoreUserPreferencesRepository {
        val ds = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tempFolder.root, "$name.preferences_pb") }
        )
        return DataStoreUserPreferencesRepository(ds)
    }

    @Test
    fun allThreeTracksThirtyLessonsAutoFadeListeningAndBossChallengesAreValid() {
        assertEquals(8, BeginnerSyllabusCatalog.allStages.size)
        assertEquals(30, BeginnerSyllabusCatalog.allLessons.size)

        // Track 1: Beginner (Zero -> Sentence): 4 stages × 3 lessons = 12 lessons
        assertEquals(4, BeginnerSyllabusCatalog.stages.size)
        assertEquals(12, BeginnerSyllabusCatalog.lessons.size)
        assertEquals(12, BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.BEGINNER).size)

        // Track 2: TOPIK I (1-2급): 2 stages = 10 lessons (incl. Irregular Verbs & Noun Modifiers!)
        assertEquals(2, BeginnerSyllabusCatalog.stagesForTrack(SyllabusTrack.TOPIK_I).size)
        assertEquals(10, BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_I).size)
        assertNotNull(BeginnerSyllabusCatalog.lessonById("t1_08_irregular_verbs"))
        assertNotNull(BeginnerSyllabusCatalog.lessonById("t1_09_noun_modifiers"))
        assertNotNull(BeginnerSyllabusCatalog.lessonById("t1_10_topik1_exam_strategy"))

        // Track 3: TOPIK II (3-6급): 2 stages × 4 lessons = 8 lessons
        assertEquals(2, BeginnerSyllabusCatalog.stagesForTrack(SyllabusTrack.TOPIK_II).size)
        assertEquals(8, BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_II).size)

        // Verify Auto-Fade Romanization Policy across all 3 tracks
        assertEquals(
            RomanizationVisibility.FULL,
            BeginnerSyllabusCatalog.romanizationPolicyFor(BeginnerSyllabusCatalog.lessonById("beg_01_basic_vowels")!!)
        )
        assertEquals(
            RomanizationVisibility.FULL,
            BeginnerSyllabusCatalog.romanizationPolicyFor(BeginnerSyllabusCatalog.lessonById("beg_06_tense_and_liaison")!!)
        )
        assertEquals(
            RomanizationVisibility.TAP_TO_PEEK,
            BeginnerSyllabusCatalog.romanizationPolicyFor(BeginnerSyllabusCatalog.lessonById("beg_08_sov_and_self_intro")!!)
        )
        assertEquals(
            RomanizationVisibility.TAP_TO_PEEK,
            BeginnerSyllabusCatalog.romanizationPolicyFor(BeginnerSyllabusCatalog.lessonById("t1_08_irregular_verbs")!!)
        )
        assertEquals(
            RomanizationVisibility.EXAM_HIDDEN,
            BeginnerSyllabusCatalog.romanizationPolicyFor(BeginnerSyllabusCatalog.lessonById("t2_05_formal_written_haerache")!!)
        )

        val seenIds = HashSet<String>()
        val seenBossSeals = HashSet<String>()
        BeginnerSyllabusCatalog.allStages.forEach { stage ->
            assertTrue(stage.titleKo.isNotBlank())
            assertTrue(stage.titleEn.isNotBlank())
            assertTrue(stage.titleId.isNotBlank())
            assertTrue("Duplicate boss seal ${stage.bossSealKo}", seenBossSeals.add(stage.bossSealKo))
            assertTrue(BeginnerSyllabusCatalog.lessonsForStage(stage.number).isNotEmpty())

            // Verify timed 6-round Stage Boss Gauntlet builds cleanly for EVERY stage 1..8
            val boss = StageBossEngine.buildChallenge(stage.number)
            assertNotNull("Stage ${stage.number} boss must exist", boss)
            assertEquals(6, boss!!.questions.size)
            assertTrue(boss.questions.any { it.kind == BossQuestionKind.AUDIO_DISCRIMINATION })
            assertTrue(boss.questions.any { it.kind == BossQuestionKind.SENTENCE_ORDER })
            assertTrue(boss.questions.any { it.kind == BossQuestionKind.RAPID_CHECK })
            assertTrue(StageBossEngine.isVictory(boss.passThreshold, boss))
            assertFalse(StageBossEngine.isVictory(boss.passThreshold - 1, boss))
        }

        SyllabusTrack.entries.forEach { track ->
            val trackLessons = BeginnerSyllabusCatalog.lessonsForTrack(track)
            trackLessons.forEachIndexed { idx, lesson ->
                assertTrue("Duplicate lesson id ${lesson.id}", seenIds.add(lesson.id))
                assertEquals(track, lesson.track)
                assertEquals(idx + 1, lesson.number)
                assertTrue(lesson.emblemKo.isNotBlank())
                assertTrue(lesson.titleEn.isNotBlank())
                assertTrue(lesson.titleId.isNotBlank())
                assertTrue(lesson.overviewEn.isNotBlank())
                assertTrue(lesson.overviewId.isNotBlank())
                assertTrue(lesson.keyRuleEn.isNotBlank())
                assertTrue(lesson.keyRuleId.isNotBlank())
                assertTrue("Lesson ${lesson.id} needs >= 5 study cards", lesson.studyCards.size >= 5)
                assertEquals("Lesson ${lesson.id} must have 2 sentence-building exercises", 2, lesson.sentenceExercises.size)
                assertEquals("Lesson ${lesson.id} must have 3 mini-checks", 3, lesson.miniChecks.size)
                assertTrue("Lesson ${lesson.id} must yield FSRS keywords", BeginnerSyllabusCatalog.coreKeywordsFor(lesson).isNotEmpty())

                // Listening-First active on 2nd sentence builder from Stage 2 onward
                if (lesson.stageNumber >= 2) {
                    assertTrue("Lesson ${lesson.id} exercise 1 should be Listening-First", BeginnerSyllabusCatalog.isAudioFirstExercise(lesson, 1))
                }

                lesson.sentenceExercises.forEach { ex ->
                    assertTrue(ex.promptEn.isNotBlank())
                    assertTrue(ex.promptId.isNotBlank())
                    assertTrue(ex.targetTokens.size >= 2)
                    assertTrue(ex.fullSentenceKo.isNotBlank())
                    assertTrue(ex.romanization.isNotBlank())
                    assertTrue(ex.hintEn.isNotBlank())
                    assertTrue(ex.hintId.isNotBlank())
                }

                lesson.miniChecks.forEach { check ->
                    assertEquals(4, check.options.size)
                    assertTrue(check.correctOption in check.options)
                    assertTrue(check.explanationEn.isNotBlank())
                    assertTrue(check.explanationId.isNotBlank())
                }
            }
        }

        // Verify Mistake Vault key serialization & parsing
        val buildKey = BeginnerSyllabusCatalog.mistakeKeyForBuild("t1_08_irregular_verbs", 1)
        val checkKey = BeginnerSyllabusCatalog.mistakeKeyForCheck("t1_09_noun_modifiers", 2)
        assertTrue(BeginnerSyllabusCatalog.resolveMistakeItem(buildKey) is SyllabusMistakeItem.SentenceBuildMistake)
        assertTrue(BeginnerSyllabusCatalog.resolveMistakeItem(checkKey) is SyllabusMistakeItem.MiniCheckMistake)
    }

    @Test
    fun progressionThreeStarsMistakeRedemptionAndStageBossXpAreIdempotent() =
        runTest(UnconfinedTestDispatcher()) {
            val repo = createPrefsRepo(this, "v01_beta1_syllabus_prefs")

            // Initially Beginner Lesson 1
            assertEquals(SyllabusTrack.BEGINNER, BeginnerSyllabusCatalog.activeTrackFor(emptySet()))
            assertEquals("beg_01_basic_vowels", BeginnerSyllabusCatalog.nextRecommendedLesson(emptySet()).id)
            assertFalse(BeginnerSyllabusCatalog.isGraduated(emptySet()))

            // 1. Lesson 1 non-perfect -> base XP once, then replay perfect -> +25 XP once
            assertTrue(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = false))
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = false))
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = true))
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = true))

            // 2. Mistake Redemption Vault: record mistake -> redeem once for +20 XP -> repeat returns false
            val mk = BeginnerSyllabusCatalog.mistakeKeyForBuild("beg_08_sov_and_self_intro", 0)
            repo.recordSyllabusMistake(mk)
            assertEquals(setOf(mk), repo.engagementProfileFlow.first().mistakeVaultKeys)
            assertTrue(repo.resolveSyllabusMistake(mk))
            assertFalse(repo.resolveSyllabusMistake(mk))
            assertTrue(repo.engagementProfileFlow.first().mistakeVaultKeys.isEmpty())

            // 3. Stage Boss defeat: awards +120 XP idempotently once per stage
            assertTrue(repo.recordStageBossDefeated(1))
            assertFalse(repo.recordStageBossDefeated(1))
            assertEquals(setOf(1), repo.engagementProfileFlow.first().defeatedStageBossNumbers)

            // 4. Complete all remaining 11 Beginner + 10 TOPIK I + 8 TOPIK II lessons
            BeginnerSyllabusCatalog.lessons.drop(1).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id, xpReward = lesson.xpReward, isPerfect = false))
            }
            val afterBeginner = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isGraduated(afterBeginner.completedBeginnerLessonIds))
            assertEquals(SyllabusTrack.TOPIK_I, BeginnerSyllabusCatalog.activeTrackFor(afterBeginner.completedBeginnerLessonIds))

            BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_I).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id, xpReward = lesson.xpReward, isPerfect = true))
            }
            val afterTopik1 = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isTrackGraduated(afterTopik1.completedBeginnerLessonIds, SyllabusTrack.TOPIK_I))
            assertEquals(SyllabusTrack.TOPIK_II, BeginnerSyllabusCatalog.activeTrackFor(afterTopik1.completedBeginnerLessonIds))
            assertEquals(10, BeginnerSyllabusCatalog.perfectCount(afterTopik1.perfectSyllabusLessonIds, SyllabusTrack.TOPIK_I))

            BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_II).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id, xpReward = lesson.xpReward, isPerfect = false))
            }
            val afterAll = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isAllTracksGraduated(afterAll.completedBeginnerLessonIds))
            assertEquals(30, BeginnerSyllabusCatalog.totalCompletionCount(afterAll.completedBeginnerLessonIds))
            assertEquals(1.0f, BeginnerSyllabusCatalog.totalCompletionFraction(afterAll.completedBeginnerLessonIds), 0.001f)
            assertNull(BeginnerSyllabusCatalog.nextTrackAfter(SyllabusTrack.TOPIK_II))

            val expectedTotalXp =
                (12 * BeginnerSyllabusCatalog.LESSON_XP_REWARD) +
                    (10 * BeginnerSyllabusCatalog.TOPIK_I_LESSON_XP) +
                    (8 * BeginnerSyllabusCatalog.TOPIK_II_LESSON_XP) +
                    (11 * BeginnerSyllabusCatalog.PERFECT_STAR_BONUS_XP) + // 1 Beginner + 10 TOPIK I perfect
                    BeginnerSyllabusCatalog.REDEMPTION_BONUS_XP +
                    BeginnerSyllabusCatalog.STAGE_BOSS_XP +
                    SyllabusTrack.BEGINNER.graduationBonusXp +
                    SyllabusTrack.TOPIK_I.graduationBonusXp +
                    SyllabusTrack.TOPIK_II.graduationBonusXp
            assertEquals(expectedTotalXp, afterAll.totalXp)
        }
}
