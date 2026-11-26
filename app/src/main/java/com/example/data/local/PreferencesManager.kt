package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.data.repository.LearningStreakState
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

internal val Context.baeumDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "baeum_user_preferences"
)

enum class RomanizationMode(val label: String) {
    ALWAYS("Always show"),
    ON_BACK_ONLY("Show on answer"),
    HIDDEN("Hide romanization")
}

data class UserSettings(
    val onboardingCompleted: Boolean = false,
    val selectedGoal: String = CurriculumPathway.TOPIK,
    val startingLevel: String = "Beginner",
    val activePathway: String = CurriculumPathway.TOPIK,
    val dailyGoalWords: Int = 20,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val uiLanguage: String = "EN",
    val translationLanguage: String = "EN",
    val fontScale: Float = 1.0f,
    val romanizationMode: RomanizationMode = RomanizationMode.ALWAYS,
    val fsrsDesiredRetention: Double = 0.90,
    val speechRate: Float = 1.0f,
    val reducedMotion: Boolean = false,
    val lastScenarioIds: String = "",
    val learningStreak: LearningStreakState = LearningStreakState(),
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 19,
    val dailyReminderMinute: Int = 0
)

/**
 * Legacy-compatible adapter delegating to [DataStoreUserPreferencesRepository].
 * Prefer injecting [UserPreferencesRepository] directly for new components and tests.
 */
class PreferencesManager(
    private val delegate: UserPreferencesRepository
) : UserPreferencesRepository by delegate {

    constructor(context: Context) : this(DataStoreUserPreferencesRepository.create(context))

    val settingsFlow: Flow<UserSettings> get() = delegate.userSettingsFlow

    suspend fun setActivePathway(pathway: String) = delegate.setCurrentCurriculum(pathway)
}
