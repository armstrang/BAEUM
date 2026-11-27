package com.example.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.BaeumDatabase
import com.example.data.repository.BaeumRepository
import com.example.data.repository.DataStoreUserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives daily study reminder alarms, queries real FSRS due cards & today's review count
 * from Room + DataStore, and posts a bilingual (EN/ID) study reminder notification.
 */
class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action != null && action != StudyReminderScheduler.ACTION_DAILY_STUDY_REMINDER) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val prefsRepo = DataStoreUserPreferencesRepository.create(appContext)
                val settings = prefsRepo.userSettingsFlow.first()
                if (!settings.dailyReminderEnabled) {
                    return@launch
                }

                val db = BaeumDatabase.getInstance(appContext)
                val allVocab = db.vocabDao().getAllVocabOnce()
                val dueCount = BaeumRepository.countDueTodayOnly(allVocab)
                val todayKey = BaeumRepository.localDayKey()
                val todayWords = db.dailyStudyDao().getByDay(todayKey)?.wordsReviewed ?: 0
                val streak = settings.learningStreak.effectiveCurrentStreak(todayKey)
                val isIndonesian = settings.translationLanguage.equals("ID", ignoreCase = true)
                val sparkPending = settings.engagement.dailySparkCompletedDayKey != todayKey

                StudyReminderScheduler.postStudyReminderNotification(
                    context = appContext,
                    dueCount = dueCount,
                    todayWords = todayWords,
                    dailyGoal = settings.dailyGoalWords,
                    streakDays = streak,
                    isIndonesian = isIndonesian,
                    streakShields = settings.engagement.streakShields,
                    sparkPending = sparkPending
                )
            } finally {
                pendingResult.finish()
                scope.cancel()
            }
        }
    }
}
