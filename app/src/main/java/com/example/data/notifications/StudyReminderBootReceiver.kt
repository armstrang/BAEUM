package com.example.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.DataStoreUserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Restores the daily study reminder alarm after device reboot, app update,
 * clock adjustment, or timezone change so reminders persist reliably.
 */
class StudyReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in ALLOWED_ACTIONS) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val prefsRepo = DataStoreUserPreferencesRepository.create(appContext)
                val settings = prefsRepo.userSettingsFlow.first()
                if (settings.dailyReminderEnabled) {
                    val isIndonesian = settings.translationLanguage.equals("ID", ignoreCase = true)
                    StudyReminderScheduler.scheduleDailyReminder(
                        context = appContext,
                        hour = settings.dailyReminderHour,
                        minute = settings.dailyReminderMinute,
                        isIndonesian = isIndonesian
                    )
                } else {
                    StudyReminderScheduler.cancelDailyReminder(appContext)
                }
            } finally {
                pendingResult.finish()
                scope.cancel()
            }
        }
    }

    companion object {
        internal val ALLOWED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
    }
}
