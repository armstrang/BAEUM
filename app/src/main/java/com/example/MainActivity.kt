package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.audio.KoreanAudioController
import com.example.data.local.BaeumDatabase
import com.example.data.notifications.StudyReminderScheduler
import com.example.data.repository.BaeumRepository
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.ui.navigation.BaeumAppRoot
import com.example.ui.theme.BaeumTheme
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.BaeumViewModel
import com.example.viewmodel.FlashcardFsrsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BaeumViewModel
    private lateinit var flashcardViewModel: FlashcardFsrsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val db = BaeumDatabase.getInstance(applicationContext)
        val userPreferencesRepository = DataStoreUserPreferencesRepository.create(applicationContext)
        val repository = BaeumRepository(db, userPreferencesRepository, applicationContext.assets)
        StudyReminderScheduler.ensureNotificationChannel(applicationContext)

        viewModel = ViewModelProvider(
            this,
            BaeumViewModel.Factory(
                repository = repository,
                audioControllerFactory = { KoreanAudioController(applicationContext) },
                userPreferencesRepository = userPreferencesRepository,
                reminderScheduler = { enabled, hour, minute, isId ->
                    if (enabled) {
                        StudyReminderScheduler.scheduleDailyReminder(applicationContext, hour, minute, isId)
                    } else {
                        StudyReminderScheduler.cancelDailyReminder(applicationContext)
                    }
                },
                testReminderPoster = { dueCount, todayWords, dailyGoal, streakDays, isId ->
                    StudyReminderScheduler.postStudyReminderNotification(
                        context = applicationContext,
                        dueCount = dueCount,
                        todayWords = todayWords,
                        dailyGoal = dailyGoal,
                        streakDays = streakDays,
                        isIndonesian = isId
                    )
                }
            )
        )[BaeumViewModel::class.java]

        flashcardViewModel = ViewModelProvider(
            this,
            FlashcardFsrsViewModel.Factory(repository, userPreferencesRepository)
        )[FlashcardFsrsViewModel::class.java]

        setContent {
            val themePrefs by viewModel.themePreferences.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themePrefs.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }

            LaunchedEffect(isDark) {
                val style = if (isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            BaeumTheme(
                themeMode = themePrefs.themeMode,
                fontScale = themePrefs.fontScale,
                reducedMotion = themePrefs.reducedMotion
            ) {
                BaeumAppRoot(
                    viewModel = viewModel,
                    flashcardViewModel = flashcardViewModel
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && ::viewModel.isInitialized) {
            viewModel.onHostStop()
        }
    }
}
