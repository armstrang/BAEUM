package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import com.example.domain.curriculum.SyllabusTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun allThreeTracksHaveDistinctBilingualLessonsSentenceBuildersAndMiniChecks() {
        assertEquals(8, BeginnerSyllabusCatalog.allStages.size)
        assertEquals(28, BeginnerSyllabusCatalog.allLessons.size)

        // Track 1: Beginner (Zero -> Sentence): 4 stages × 3 lessons = 12 lessons
        assertEquals(4, BeginnerSyllabusCatalog.stages.size)
        assertEquals(12, BeginnerSyllabusCatalog.lessons.size)
        assertEquals(12, BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.BEGINNER).size)

        // Track 2: TOPIK I (1-2급): 2 stages × 4 lessons = 8 lessons
        assertEquals(2, BeginnerSyllabusCatalog.stagesForTrack(SyllabusTrack.TOPIK_I).size)
        assertEquals(8, BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_I).size)

        // Track 3: TOPIK II (3-6급): 2 stages × 4 lessons = 8 lessons
        assertEquals(2, BeginnerSyllabusCatalog.stagesForTrack(SyllabusTrack.TOPIK_II).size)
        assertEquals(8, BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_II).size)

        val seenIds = HashSet<String>()
        BeginnerSyllabusCatalog.allStages.forEach { stage ->
            assertTrue(stage.titleKo.isNotBlank())
            assertTrue(stage.titleEn.isNotBlank())
            assertTrue(stage.titleId.isNotBlank())
            assertTrue(BeginnerSyllabusCatalog.lessonsForStage(stage.number).isNotEmpty())
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

                lesson.studyCards.forEach { card ->
                    assertTrue(card.hangul.isNotBlank())
                    assertTrue(card.meaningEn.isNotBlank())
                    assertTrue(card.meaningId.isNotBlank())
                    assertTrue(card.tipEn.isNotBlank())
                    assertTrue(card.tipId.isNotBlank())
                }

                lesson.sentenceExercises.forEach { ex ->
                    assertTrue("Sentence prompt EN blank in ${lesson.id}", ex.promptEn.isNotBlank())
                    assertTrue("Sentence prompt ID blank in ${lesson.id}", ex.promptId.isNotBlank())
                    assertTrue("Sentence targetTokens must have >= 2 tokens in ${lesson.id}", ex.targetTokens.size >= 2)
                    assertTrue("Full sentence KO blank in ${lesson.id}", ex.fullSentenceKo.isNotBlank())
                    assertTrue("Romanization blank in ${lesson.id}", ex.romanization.isNotBlank())
                    assertTrue("Hint EN blank in ${lesson.id}", ex.hintEn.isNotBlank())
                    assertTrue("Hint ID blank in ${lesson.id}", ex.hintId.isNotBlank())
                }

                lesson.miniChecks.forEach { check ->
                    assertEquals(4, check.options.size)
                    assertTrue(
                        "Correct option '${check.correctOption}' missing from options in ${lesson.id}",
                        check.correctOption in check.options
                    )
                    assertTrue(check.explanationEn.isNotBlank())
                    assertTrue(check.explanationId.isNotBlank())
                }
            }
        }
    }

    @Test
    fun threeTrackProgressionPerfectThreeStarBonusAndGraduationBonusesAreIdempotent() =
        runTest(UnconfinedTestDispatcher()) {
            val repo = createPrefsRepo(this, "three_track_syllabus_prefs")

            // Initially points to Beginner track Lesson 1
            assertEquals(SyllabusTrack.BEGINNER, BeginnerSyllabusCatalog.activeTrackFor(emptySet()))
            assertEquals("beg_01_basic_vowels", BeginnerSyllabusCatalog.nextRecommendedLesson(emptySet()).id)
            assertFalse(BeginnerSyllabusCatalog.isGraduated(emptySet()))

            // Completing Lesson 1 non-perfect grants base XP once; repeating non-perfect adds 0 XP
            assertTrue(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = false))
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = false))
            val afterFirst = repo.engagementProfileFlow.first()
            assertEquals(setOf("beg_01_basic_vowels"), afterFirst.completedBeginnerLessonIds)
            assertTrue(afterFirst.perfectSyllabusLessonIds.isEmpty())
            assertEquals(BeginnerSyllabusCatalog.LESSON_XP_REWARD, afterFirst.totalXp)

            // Replaying Lesson 1 with 0 mistakes upgrades to Gold ★★★ and awards +25 XP ONCE
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = true))
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels", isPerfect = true))
            val afterPerfect = repo.engagementProfileFlow.first()
            assertEquals(setOf("beg_01_basic_vowels"), afterPerfect.perfectSyllabusLessonIds)
            assertEquals(
                BeginnerSyllabusCatalog.LESSON_XP_REWARD + BeginnerSyllabusCatalog.PERFECT_STAR_BONUS_XP,
                afterPerfect.totalXp
            )
            assertEquals("beg_02_core_consonants", BeginnerSyllabusCatalog.nextRecommendedLesson(afterPerfect.completedBeginnerLessonIds).id)

            // Complete remaining 11 Beginner lessons -> graduates Beginner (+150 XP) and auto-advances activeTrackFor to TOPIK_I!
            BeginnerSyllabusCatalog.lessons.drop(1).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id, xpReward = lesson.xpReward, isPerfect = false))
            }
            val afterBeginner = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isGraduated(afterBeginner.completedBeginnerLessonIds))
            assertEquals(SyllabusTrack.TOPIK_I, BeginnerSyllabusCatalog.activeTrackFor(afterBeginner.completedBeginnerLessonIds))
            assertEquals(SyllabusTrack.TOPIK_I, BeginnerSyllabusCatalog.nextTrackAfter(SyllabusTrack.BEGINNER))
            assertEquals(
                12 * BeginnerSyllabusCatalog.LESSON_XP_REWARD +
                    BeginnerSyllabusCatalog.PERFECT_STAR_BONUS_XP +
                    SyllabusTrack.BEGINNER.graduationBonusXp,
                afterBeginner.totalXp
            )

            // Complete all 8 TOPIK I lessons -> graduates TOPIK I (+200 XP) and auto-advances activeTrackFor to TOPIK_II!
            BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_I).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id, xpReward = lesson.xpReward, isPerfect = true))
            }
            val afterTopik1 = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isTrackGraduated(afterTopik1.completedBeginnerLessonIds, SyllabusTrack.TOPIK_I))
            assertEquals(SyllabusTrack.TOPIK_II, BeginnerSyllabusCatalog.activeTrackFor(afterTopik1.completedBeginnerLessonIds))
            assertEquals(8, BeginnerSyllabusCatalog.perfectCount(afterTopik1.perfectSyllabusLessonIds, SyllabusTrack.TOPIK_I))

            // Complete all 8 TOPIK II lessons -> graduates all 3 tracks (+300 XP)!
            BeginnerSyllabusCatalog.lessonsForTrack(SyllabusTrack.TOPIK_II).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id, xpReward = lesson.xpReward, isPerfect = false))
            }
            val afterAll = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isAllTracksGraduated(afterAll.completedBeginnerLessonIds))
            assertEquals(28, BeginnerSyllabusCatalog.totalCompletionCount(afterAll.completedBeginnerLessonIds))
            assertEquals(1.0f, BeginnerSyllabusCatalog.totalCompletionFraction(afterAll.completedBeginnerLessonIds), 0.001f)
            assertNull(BeginnerSyllabusCatalog.nextTrackAfter(SyllabusTrack.TOPIK_II))

            val expectedTotalXp =
                (12 * BeginnerSyllabusCatalog.LESSON_XP_REWARD) +
                    (8 * BeginnerSyllabusCatalog.TOPIK_I_LESSON_XP) +
                    (8 * BeginnerSyllabusCatalog.TOPIK_II_LESSON_XP) +
                    (9 * BeginnerSyllabusCatalog.PERFECT_STAR_BONUS_XP) + // 1 Beginner + 8 TOPIK I perfect
                    SyllabusTrack.BEGINNER.graduationBonusXp +
                    SyllabusTrack.TOPIK_I.graduationBonusXp +
                    SyllabusTrack.TOPIK_II.graduationBonusXp
            assertEquals(expectedTotalXp, afterAll.totalXp)
        }
}
