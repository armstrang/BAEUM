package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.local.CurriculumPathway
import com.example.data.local.RomanizationMode
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.ui.theme.ThemeMode
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
class UserPreferencesRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(
        testScope: TestScope,
        fileName: String
    ): DataStoreUserPreferencesRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { File(tempFolder.root, "$fileName.preferences_pb") }
        )
        return DataStoreUserPreferencesRepository(dataStore)
    }

    @Test
    fun emitsExpectedDefaultPreferencesAndUpdatesThemeSettings() = runTest(UnconfinedTestDispatcher()) {
        val repo = createRepository(this, "theme_prefs")

        val initial = repo.userSettingsFlow.first()
        assertEquals(ThemeMode.DARK, initial.themeMode)
        assertEquals(CurriculumPathway.TOPIK, initial.activePathway)
        assertEquals(20, initial.dailyGoalWords)
        assertEquals(0, initial.learningStreak.currentStreakDays)
        assertFalse(initial.reducedMotion)

        repo.setThemeMode(ThemeMode.LIGHT)
        repo.setFontScale(2.0f) // Clamps to 1.25f
        repo.setReducedMotion(true)

        val themePrefs = repo.themePreferencesFlow.first()
        assertEquals(ThemeMode.LIGHT, themePrefs.themeMode)
        assertEquals(1.25f, themePrefs.fontScale, 0.001f)
        assertTrue(themePrefs.reducedMotion)
    }

    @Test
    fun managesCurriculumOnboardingAndStudySettings() = runTest(UnconfinedTestDispatcher()) {
        val repo = createRepository(this, "curriculum_prefs")

        repo.completeOnboarding(
            goal = CurriculumPathway.EPS_TOPIK,
            level = "Intermediate",
            dailyGoal = 35
        )

        var curriculum = repo.curriculumPreferencesFlow.first()
        assertEquals(CurriculumPathway.EPS_TOPIK, curriculum.activeCurriculum)
        assertEquals(CurriculumPathway.EPS_TOPIK, curriculum.selectedGoal)
        assertEquals("Intermediate", curriculum.startingLevel)
        assertEquals(35, curriculum.dailyGoalWords)
        assertTrue(repo.userSettingsFlow.first().onboardingCompleted)

        // Switching active curriculum preserves original goal while updating active pathway
        repo.setCurrentCurriculum(CurriculumPathway.EVERYDAY)
        repo.setDailyGoal(150) // Clamps to 100
        repo.setRomanizationMode(RomanizationMode.ON_BACK_ONLY)
        repo.setTranslationLanguage("id")
        repo.setFsrsRetention(0.99) // Clamps to 0.95
        repo.setSpeechRate(0.85f)

        curriculum = repo.curriculumPreferencesFlow.first()
        assertEquals(CurriculumPathway.EVERYDAY, curriculum.activeCurriculum)
        assertEquals(100, curriculum.dailyGoalWords)

        val full = repo.userSettingsFlow.first()
        assertEquals(RomanizationMode.ON_BACK_ONLY, full.romanizationMode)
        assertEquals("ID", full.translationLanguage)
        assertEquals(0.95, full.fsrsDesiredRetention, 0.0001)
        assertEquals(0.85f, full.speechRate, 0.001f)
    }

    @Test
    fun learningStreakIncrementsConsecutivelyIsIdempotentSameDayAndPreservesLongestOnReset() =
        runTest(UnconfinedTestDispatcher()) {
            val repo = createRepository(this, "streak_prefs")

            // Day 1
            val day1 = repo.recordStudyActivity("2026-11-20")
            assertEquals(1, day1.currentStreakDays)
            assertEquals(1, day1.longestStreakDays)
            assertEquals(1, day1.totalActiveDays)
            assertEquals("2026-11-20", day1.lastStudyDayKey)

            // Repeated study on same day is idempotent
            val day1Again = repo.recordStudyActivity("2026-11-20")
            assertEquals(1, day1Again.currentStreakDays)
            assertEquals(1, day1Again.totalActiveDays)

            // Consecutive days 2 & 3
            val day2 = repo.recordStudyActivity("2026-11-21")
            assertEquals(2, day2.currentStreakDays)
            assertEquals(2, day2.longestStreakDays)

            val day3 = repo.recordStudyActivity("2026-11-22")
            assertEquals(3, day3.currentStreakDays)
            assertEquals(3, day3.longestStreakDays)
            assertEquals(3, day3.totalActiveDays)

            // Evaluating continuity on day 4 (yesterday = day 3) keeps streak alive
            val stillAlive = repo.evaluateStreakContinuity("2026-11-23")
            assertEquals(3, stillAlive.currentStreakDays)
            assertTrue(stillAlive.isAliveAsOf("2026-11-23"))

            // Evaluating continuity after skipping day 4 resets currentStreakDays to 0 while preserving longest
            val broken = repo.evaluateStreakContinuity("2026-11-24")
            assertEquals(0, broken.currentStreakDays)
            assertEquals(3, broken.longestStreakDays)
            assertEquals(3, broken.totalActiveDays)

            // Studying again on day 5 restarts current streak at 1 while retaining longest = 3
            val restarted = repo.recordStudyActivity("2026-11-24")
            assertEquals(1, restarted.currentStreakDays)
            assertEquals(3, restarted.longestStreakDays)
            assertEquals(4, restarted.totalActiveDays)

            // Verify persisted in learningStreakFlow
            val emitted = repo.learningStreakFlow.first()
            assertEquals(restarted, emitted)
        }

    @Test
    fun syncStreakFromDailyHistoryComputesCurrentAndLongestAcrossMonthBoundary() =
        runTest(UnconfinedTestDispatcher()) {
            val repo = createRepository(this, "history_sync")

            val activeDays = setOf(
                "2026-10-29",
                "2026-10-30",
                "2026-10-31",
                "2026-11-01", // 4-day historical run across Oct -> Nov
                "2026-11-24",
                "2026-11-25"  // 2-day active run ending today
            )

            val synced = repo.syncStreakFromDailyHistory(
                activeDayKeys = activeDays,
                todayDayKey = "2026-11-25"
            )
            assertEquals(2, synced.currentStreakDays)
            assertEquals(4, synced.longestStreakDays)
            assertEquals(6, synced.totalActiveDays)
            assertEquals("2026-11-25", synced.lastStudyDayKey)

            // Clear preferences resets back to clean defaults
            repo.clearAllPreferences()
            val afterClear = repo.learningStreakFlow.first()
            assertEquals(0, afterClear.currentStreakDays)
            assertEquals(0, afterClear.longestStreakDays)
        }
}
