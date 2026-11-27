package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun beginnerSyllabusCatalogHasFourStagesTwelveBilingualLessonsAndValidMiniChecks() {
        assertEquals(4, BeginnerSyllabusCatalog.stages.size)
        assertEquals(12, BeginnerSyllabusCatalog.lessons.size)

        val seenIds = HashSet<String>()
        BeginnerSyllabusCatalog.stages.forEach { stage ->
            assertTrue(stage.titleKo.isNotBlank())
            assertTrue(stage.titleEn.isNotBlank())
            assertTrue(stage.titleId.isNotBlank())
            assertEquals(3, BeginnerSyllabusCatalog.lessonsForStage(stage.number).size)
        }

        BeginnerSyllabusCatalog.lessons.forEachIndexed { idx, lesson ->
            assertTrue("Duplicate lesson id ${lesson.id}", seenIds.add(lesson.id))
            assertEquals(idx + 1, lesson.number)
            assertTrue(lesson.emblemKo.isNotBlank())
            assertTrue(lesson.titleEn.isNotBlank())
            assertTrue(lesson.titleId.isNotBlank())
            assertTrue(lesson.overviewEn.isNotBlank())
            assertTrue(lesson.overviewId.isNotBlank())
            assertTrue(lesson.keyRuleEn.isNotBlank())
            assertTrue(lesson.keyRuleId.isNotBlank())
            assertTrue("Lesson ${lesson.number} needs >= 5 study cards", lesson.studyCards.size >= 5)
            assertEquals("Lesson ${lesson.number} must have 3 mini-checks", 3, lesson.miniChecks.size)

            lesson.studyCards.forEach { card ->
                assertTrue(card.hangul.isNotBlank())
                assertTrue(card.meaningEn.isNotBlank())
                assertTrue(card.meaningId.isNotBlank())
                assertTrue(card.tipEn.isNotBlank())
                assertTrue(card.tipId.isNotBlank())
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

    @Test
    fun beginnerSyllabusProgressionAndDataStoreCompletionAwardsXpAndGraduationBonus() =
        runTest(UnconfinedTestDispatcher()) {
            val repo = createPrefsRepo(this, "beginner_syllabus_prefs")

            // Initially points to Lesson 1
            assertEquals("beg_01_basic_vowels", BeginnerSyllabusCatalog.nextRecommendedLesson(emptySet()).id)
            assertFalse(BeginnerSyllabusCatalog.isGraduated(emptySet()))

            // Completing Lesson 1 once awards +60 XP; repeating Lesson 1 is idempotent (no duplicate XP)
            assertTrue(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels"))
            assertFalse(repo.recordBeginnerLessonCompleted("beg_01_basic_vowels"))

            val afterOne = repo.engagementProfileFlow.first()
            assertEquals(setOf("beg_01_basic_vowels"), afterOne.completedBeginnerLessonIds)
            assertEquals(BeginnerSyllabusCatalog.LESSON_XP_REWARD, afterOne.totalXp)
            assertEquals("beg_02_core_consonants", BeginnerSyllabusCatalog.nextRecommendedLesson(afterOne.completedBeginnerLessonIds).id)

            // Completing remaining 11 lessons grants Graduation Bonus XP on the 12th lesson!
            BeginnerSyllabusCatalog.lessons.drop(1).forEach { lesson ->
                assertTrue(repo.recordBeginnerLessonCompleted(lesson.id))
            }

            val graduated = repo.engagementProfileFlow.first()
            assertTrue(BeginnerSyllabusCatalog.isGraduated(graduated.completedBeginnerLessonIds))
            assertEquals(12, BeginnerSyllabusCatalog.completionCount(graduated.completedBeginnerLessonIds))
            assertEquals(1.0f, BeginnerSyllabusCatalog.completionFraction(graduated.completedBeginnerLessonIds), 0.001f)
            assertEquals(
                12 * BeginnerSyllabusCatalog.LESSON_XP_REWARD + BeginnerSyllabusCatalog.GRADUATION_BONUS_XP,
                graduated.totalXp
            )
        }
}
