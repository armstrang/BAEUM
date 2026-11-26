package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.local.CurriculumPathway
import com.example.data.local.RomanizationMode
import com.example.data.local.UserSettings
import com.example.data.local.baeumDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Persisted learning streak snapshot stored in Jetpack DataStore.
 */
data class LearningStreakState(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastStudyDayKey: String = "",
    val totalActiveDays: Int = 0
) {
    fun isAliveAsOf(todayDayKey: String): Boolean {
        if (lastStudyDayKey.isBlank() || currentStreakDays <= 0) return false
        return lastStudyDayKey == todayDayKey || lastStudyDayKey == DataStoreUserPreferencesRepository.previousDayKey(todayDayKey)
    }

    fun effectiveCurrentStreak(todayDayKey: String): Int =
        if (isAliveAsOf(todayDayKey)) currentStreakDays else 0
}

/**
 * Focused slice of appearance and accessibility preferences.
 */
data class ThemePreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val fontScale: Float = 1.0f,
    val reducedMotion: Boolean = false
)

/**
 * Focused slice of active learning curriculum and target preferences.
 */
data class CurriculumPreferences(
    val activeCurriculum: String = CurriculumPathway.TOPIK,
    val selectedGoal: String = CurriculumPathway.TOPIK,
    val startingLevel: String = "Beginner",
    val dailyGoalWords: Int = 20
)

/**
 * Repository abstraction over Jetpack DataStore for reactive user preferences,
 * appearance settings, active Korean curriculum state, and learning streak tracking.
 */
interface UserPreferencesRepository {
    val userSettingsFlow: Flow<UserSettings>
    val themePreferencesFlow: Flow<ThemePreferences>
    val curriculumPreferencesFlow: Flow<CurriculumPreferences>
    val learningStreakFlow: Flow<LearningStreakState>

    // Theme & display settings
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setFontScale(scale: Float)
    suspend fun setReducedMotion(enabled: Boolean)

    // Curriculum & onboarding settings
    suspend fun setCurrentCurriculum(curriculum: String)
    suspend fun setDailyGoal(words: Int)
    suspend fun completeOnboarding(goal: String, level: String, dailyGoal: Int)
    suspend fun resetOnboarding()

    // Language, audio & FSRS study settings
    suspend fun setRomanizationMode(mode: RomanizationMode)
    suspend fun setTranslationLanguage(lang: String)
    suspend fun setSpeechRate(rate: Float)
    suspend fun setFsrsRetention(retention: Double)
    suspend fun recordRecentScenario(scenarioId: String)
    suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int)

    // Learning streak persistence & continuity
    suspend fun recordStudyActivity(dayKey: String = DataStoreUserPreferencesRepository.todayDayKey()): LearningStreakState
    suspend fun evaluateStreakContinuity(todayDayKey: String = DataStoreUserPreferencesRepository.todayDayKey()): LearningStreakState
    suspend fun syncStreakFromDailyHistory(
        activeDayKeys: Set<String>,
        todayDayKey: String = DataStoreUserPreferencesRepository.todayDayKey()
    ): LearningStreakState

    suspend fun clearAllPreferences()
}

/**
 * Production Jetpack Preferences DataStore implementation of [UserPreferencesRepository].
 * Injects [DataStore] directly so it is resilient to [IOException] and fully unit-testable on the JVM.
 */
class DataStoreUserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val SELECTED_GOAL = stringPreferencesKey("selected_goal")
        val STARTING_LEVEL = stringPreferencesKey("starting_level")
        val ACTIVE_PATHWAY = stringPreferencesKey("active_pathway")
        val DAILY_GOAL = intPreferencesKey("daily_goal_words")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val TRANSLATION_LANGUAGE = stringPreferencesKey("translation_language")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val ROMANIZATION_MODE = stringPreferencesKey("romanization_mode")
        val FSRS_RETENTION = doublePreferencesKey("fsrs_desired_retention")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val LAST_SCENARIO_IDS = stringPreferencesKey("last_scenario_ids")

        // Learning streak persistence keys
        val CURRENT_STREAK_DAYS = intPreferencesKey("learning_streak_current_days")
        val LONGEST_STREAK_DAYS = intPreferencesKey("learning_streak_longest_days")
        val LAST_STUDY_DAY_KEY = stringPreferencesKey("learning_streak_last_day_key")
        val TOTAL_ACTIVE_STUDY_DAYS = intPreferencesKey("learning_streak_total_active_days")

        // Daily study reminder keys
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
    }

    private val safePreferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    override val userSettingsFlow: Flow<UserSettings> = safePreferencesFlow
        .map { prefs -> mapUserSettings(prefs) }
        .distinctUntilChanged()

    override val themePreferencesFlow: Flow<ThemePreferences> = safePreferencesFlow
        .map { prefs ->
            ThemePreferences(
                themeMode = parseThemeMode(prefs[Keys.THEME_MODE]),
                fontScale = (prefs[Keys.FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.25f),
                reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false
            )
        }
        .distinctUntilChanged()

    override val curriculumPreferencesFlow: Flow<CurriculumPreferences> = safePreferencesFlow
        .map { prefs ->
            CurriculumPreferences(
                activeCurriculum = prefs[Keys.ACTIVE_PATHWAY] ?: CurriculumPathway.TOPIK,
                selectedGoal = prefs[Keys.SELECTED_GOAL] ?: CurriculumPathway.TOPIK,
                startingLevel = prefs[Keys.STARTING_LEVEL] ?: "Beginner",
                dailyGoalWords = (prefs[Keys.DAILY_GOAL] ?: 20).coerceIn(5, 100)
            )
        }
        .distinctUntilChanged()

    override val learningStreakFlow: Flow<LearningStreakState> = safePreferencesFlow
        .map { prefs -> mapStreakState(prefs) }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setFontScale(scale: Float) {
        dataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.25f) }
    }

    override suspend fun setReducedMotion(enabled: Boolean) {
        dataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    override suspend fun setCurrentCurriculum(curriculum: String) {
        val sanitized = curriculum.trim().ifBlank { CurriculumPathway.TOPIK }
        dataStore.edit { it[Keys.ACTIVE_PATHWAY] = sanitized }
    }

    override suspend fun setDailyGoal(words: Int) {
        dataStore.edit { it[Keys.DAILY_GOAL] = words.coerceIn(5, 100) }
    }

    override suspend fun completeOnboarding(goal: String, level: String, dailyGoal: Int) {
        val cleanGoal = goal.trim().ifBlank { CurriculumPathway.TOPIK }
        val cleanLevel = level.trim().ifBlank { "Beginner" }
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_DONE] = true
            prefs[Keys.SELECTED_GOAL] = cleanGoal
            prefs[Keys.ACTIVE_PATHWAY] = cleanGoal
            prefs[Keys.STARTING_LEVEL] = cleanLevel
            prefs[Keys.DAILY_GOAL] = dailyGoal.coerceIn(5, 100)
        }
    }

    override suspend fun resetOnboarding() {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = false }
    }

    override suspend fun setRomanizationMode(mode: RomanizationMode) {
        dataStore.edit { it[Keys.ROMANIZATION_MODE] = mode.name }
    }

    override suspend fun setTranslationLanguage(lang: String) {
        val normalized = lang.trim().uppercase(Locale.US).ifBlank { "EN" }
        dataStore.edit {
            it[Keys.TRANSLATION_LANGUAGE] = normalized
            it[Keys.UI_LANGUAGE] = normalized
        }
    }

    override suspend fun setSpeechRate(rate: Float) {
        dataStore.edit { it[Keys.SPEECH_RATE] = rate.coerceIn(0.6f, 1.4f) }
    }

    override suspend fun setFsrsRetention(retention: Double) {
        dataStore.edit { it[Keys.FSRS_RETENTION] = retention.coerceIn(0.80, 0.95) }
    }

    override suspend fun recordRecentScenario(scenarioId: String) {
        val trimmed = scenarioId.trim()
        if (trimmed.isBlank()) return
        dataStore.edit { prefs ->
            val current = (prefs[Keys.LAST_SCENARIO_IDS] ?: "")
                .split(",")
                .filter { it.isNotBlank() && it != trimmed }
                .takeLast(6)
            prefs[Keys.LAST_SCENARIO_IDS] = (current + trimmed).joinToString(",")
        }
    }

    override suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.DAILY_REMINDER_ENABLED] = enabled
            prefs[Keys.DAILY_REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.DAILY_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    override suspend fun recordStudyActivity(dayKey: String): LearningStreakState {
        var updated = LearningStreakState()
        dataStore.edit { prefs ->
            val prev = mapStreakState(prefs)
            val yesterday = previousDayKey(dayKey)

            updated = when {
                prev.lastStudyDayKey == dayKey -> {
                    val ensuredCurrent = prev.currentStreakDays.coerceAtLeast(1)
                    val ensuredTotal = prev.totalActiveDays.coerceAtLeast(1)
                    prev.copy(
                        currentStreakDays = ensuredCurrent,
                        longestStreakDays = maxOf(prev.longestStreakDays, ensuredCurrent),
                        totalActiveDays = ensuredTotal
                    )
                }

                prev.lastStudyDayKey == yesterday -> {
                    val nextCurrent = prev.currentStreakDays.coerceAtLeast(0) + 1
                    LearningStreakState(
                        currentStreakDays = nextCurrent,
                        longestStreakDays = maxOf(prev.longestStreakDays, nextCurrent),
                        lastStudyDayKey = dayKey,
                        totalActiveDays = prev.totalActiveDays + 1
                    )
                }

                prev.lastStudyDayKey.isNotBlank() && dayKey < prev.lastStudyDayKey -> {
                    // Historical backfill date older than latest recorded study day: preserve active streak head
                    prev.copy(totalActiveDays = prev.totalActiveDays + 1)
                }

                else -> {
                    // First study day or streak restarted after a gap of >= 2 days
                    LearningStreakState(
                        currentStreakDays = 1,
                        longestStreakDays = maxOf(prev.longestStreakDays, 1),
                        lastStudyDayKey = dayKey,
                        totalActiveDays = prev.totalActiveDays + 1
                    )
                }
            }
            writeStreakState(prefs, updated)
        }
        return updated
    }

    override suspend fun evaluateStreakContinuity(todayDayKey: String): LearningStreakState {
        var updated = LearningStreakState()
        dataStore.edit { prefs ->
            val prev = mapStreakState(prefs)
            val alive = prev.isAliveAsOf(todayDayKey)
            updated = if (!alive && prev.currentStreakDays > 0) {
                prev.copy(currentStreakDays = 0)
            } else {
                prev
            }
            if (updated != prev) {
                writeStreakState(prefs, updated)
            }
        }
        return updated
    }

    override suspend fun syncStreakFromDailyHistory(
        activeDayKeys: Set<String>,
        todayDayKey: String
    ): LearningStreakState {
        var updated = LearningStreakState()
        dataStore.edit { prefs ->
            val prev = mapStreakState(prefs)
            val validKeys = activeDayKeys
                .map { it.trim() }
                .filter { DAY_KEY_REGEX.matches(it) }
                .sorted()

            if (validKeys.isEmpty()) {
                updated = prev
                return@edit
            }

            // Compute historical longest consecutive run
            var longestRun = 1
            var running = 1
            for (i in 1 until validKeys.size) {
                if (previousDayKey(validKeys[i]) == validKeys[i - 1]) {
                    running++
                    if (running > longestRun) longestRun = running
                } else {
                    running = 1
                }
            }

            // Compute current active run anchored at today or yesterday
            val keySet = validKeys.toHashSet()
            var anchor = todayDayKey
            if (anchor !in keySet) {
                val yesterday = previousDayKey(todayDayKey)
                anchor = if (yesterday in keySet) yesterday else ""
            }
            var currentRun = 0
            while (anchor.isNotBlank() && anchor in keySet) {
                currentRun++
                anchor = previousDayKey(anchor)
            }

            val latestDay = maxOf(prev.lastStudyDayKey, validKeys.last())
            updated = LearningStreakState(
                currentStreakDays = currentRun,
                longestStreakDays = maxOf(prev.longestStreakDays, longestRun, currentRun),
                lastStudyDayKey = latestDay,
                totalActiveDays = maxOf(prev.totalActiveDays, validKeys.size)
            )
            writeStreakState(prefs, updated)
        }
        return updated
    }

    override suspend fun clearAllPreferences() {
        dataStore.edit { it.clear() }
    }

    private fun mapUserSettings(prefs: Preferences): UserSettings {
        val defaultLang = com.example.ui.theme.defaultDeviceTranslationLanguage()
        return UserSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false,
            selectedGoal = prefs[Keys.SELECTED_GOAL] ?: CurriculumPathway.TOPIK,
            startingLevel = prefs[Keys.STARTING_LEVEL] ?: "Beginner",
            activePathway = prefs[Keys.ACTIVE_PATHWAY] ?: CurriculumPathway.TOPIK,
            dailyGoalWords = (prefs[Keys.DAILY_GOAL] ?: 20).coerceIn(5, 100),
            themeMode = parseThemeMode(prefs[Keys.THEME_MODE]),
            uiLanguage = prefs[Keys.UI_LANGUAGE] ?: defaultLang,
            translationLanguage = prefs[Keys.TRANSLATION_LANGUAGE] ?: defaultLang,
            fontScale = (prefs[Keys.FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.25f),
            romanizationMode = parseRomanizationMode(prefs[Keys.ROMANIZATION_MODE]),
            fsrsDesiredRetention = (prefs[Keys.FSRS_RETENTION] ?: 0.90).coerceIn(0.80, 0.95),
            speechRate = (prefs[Keys.SPEECH_RATE] ?: 1.0f).coerceIn(0.6f, 1.4f),
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            lastScenarioIds = prefs[Keys.LAST_SCENARIO_IDS] ?: "",
            learningStreak = mapStreakState(prefs),
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderHour = (prefs[Keys.DAILY_REMINDER_HOUR] ?: 19).coerceIn(0, 23),
            dailyReminderMinute = (prefs[Keys.DAILY_REMINDER_MINUTE] ?: 0).coerceIn(0, 59)
        )
    }

    private fun mapStreakState(prefs: Preferences): LearningStreakState {
        val current = (prefs[Keys.CURRENT_STREAK_DAYS] ?: 0).coerceAtLeast(0)
        val longest = (prefs[Keys.LONGEST_STREAK_DAYS] ?: 0).coerceAtLeast(current)
        val lastDay = prefs[Keys.LAST_STUDY_DAY_KEY] ?: ""
        val totalDays = (prefs[Keys.TOTAL_ACTIVE_STUDY_DAYS] ?: 0).coerceAtLeast(0)
        return LearningStreakState(
            currentStreakDays = current,
            longestStreakDays = longest,
            lastStudyDayKey = lastDay,
            totalActiveDays = totalDays
        )
    }

    private fun writeStreakState(prefs: MutablePreferences, state: LearningStreakState) {
        prefs[Keys.CURRENT_STREAK_DAYS] = state.currentStreakDays.coerceAtLeast(0)
        prefs[Keys.LONGEST_STREAK_DAYS] = state.longestStreakDays.coerceAtLeast(state.currentStreakDays)
        prefs[Keys.LAST_STUDY_DAY_KEY] = state.lastStudyDayKey
        prefs[Keys.TOTAL_ACTIVE_STUDY_DAYS] = state.totalActiveDays.coerceAtLeast(0)
    }

    private fun parseThemeMode(raw: String?): ThemeMode =
        runCatching { ThemeMode.valueOf(raw ?: ThemeMode.DARK.name) }.getOrDefault(ThemeMode.DARK)

    private fun parseRomanizationMode(raw: String?): RomanizationMode =
        runCatching { RomanizationMode.valueOf(raw ?: RomanizationMode.ALWAYS.name) }.getOrDefault(RomanizationMode.ALWAYS)

    companion object {
        private val DAY_KEY_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

        fun create(context: Context): DataStoreUserPreferencesRepository =
            DataStoreUserPreferencesRepository(context.applicationContext.baeumDataStore)

        fun todayDayKey(epochMs: Long = System.currentTimeMillis()): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return fmt.format(Date(epochMs))
        }

        fun previousDayKey(dayKey: String): String {
            if (!DAY_KEY_REGEX.matches(dayKey)) return ""
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val parsed = runCatching { fmt.parse(dayKey) }.getOrNull() ?: return ""
            val cal = Calendar.getInstance().apply {
                time = parsed
                add(Calendar.DAY_OF_YEAR, -1)
            }
            return fmt.format(cal.time)
        }
    }
}
