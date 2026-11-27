package com.example.data.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar
import java.util.Locale

/**
 * Google Play-compliant daily study & FSRS due-card reminder scheduler.
 *
 * Uses [AlarmManager.setInexactRepeating] (no restricted SCHEDULE_EXACT_ALARM permission)
 * and Android NotificationChannel (API 26+) with graceful runtime POST_NOTIFICATIONS checks on API 33+.
 */
object StudyReminderScheduler {

    const val CHANNEL_ID = "baeum_daily_study_reminder"
    const val NOTIFICATION_ID = 4201
    private const val REQUEST_CODE_ALARM = 4200
    private const val REQUEST_CODE_OPEN_APP = 4202

    fun ensureNotificationChannel(context: Context, isIndonesian: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (isIndonesian) "Pengingat Belajar Harian" else "Daily Korean Study Reminders"
            val description = if (isIndonesian) {
                "Pengingat harian untuk mengulas kartu FSRS dan menjaga streak belajar bahasa Korea."
            } else {
                "Daily reminder for due FSRS Korean flashcards and streak continuity."
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                this.description = description
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Pure deterministic helper to compute the next local trigger timestamp for [hour]:[minute].
     * If the target time has already passed today (or equals [nowMillis]), schedules for tomorrow.
     */
    fun computeNextTriggerMillis(
        hour: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, safeHour)
            set(Calendar.MINUTE, safeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return target.timeInMillis
    }

    fun formatReminderTime(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, isIndonesian: Boolean = false) {
        val appContext = context.applicationContext
        ensureNotificationChannel(appContext, isIndonesian)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildAlarmPendingIntent(appContext)
        val triggerAt = computeNextTriggerMillis(hour, minute)

        alarmManager.cancel(pendingIntent)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildAlarmPendingIntent(appContext)
        alarmManager.cancel(pendingIntent)
    }

    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun postStudyReminderNotification(
        context: Context,
        dueCount: Int,
        todayWords: Int,
        dailyGoal: Int,
        streakDays: Int,
        isIndonesian: Boolean,
        streakShields: Int = 0,
        sparkPending: Boolean = true
    ): Boolean {
        val appContext = context.applicationContext
        if (!canPostNotifications(appContext)) return false
        ensureNotificationChannel(appContext, isIndonesian)

        val remaining = (dailyGoal - todayWords).coerceAtLeast(0)
        val title = when {
            streakDays >= 3 && todayWords == 0 && isIndonesian ->
                "🔥 Jangan biarkan runtutan $streakDays harimu padam!"
            streakDays >= 3 && todayWords == 0 ->
                "🔥 Protect your $streakDays-day Korean streak!"
            dueCount > 0 && isIndonesian ->
                "오늘도 한 걸음 · $dueCount kartu FSRS siap diulas"
            dueCount > 0 ->
                "오늘도 한 걸음 · $dueCount FSRS cards ready"
            sparkPending && isIndonesian ->
                "✨ Percikan Seoul hari ini menantimu (+30 XP)"
            sparkPending ->
                "✨ Today's Seoul Spark is waiting (+30 XP)"
            isIndonesian -> "Waktunya latihan bahasa Korea hari ini"
            else -> "Time for your daily Korean practice"
        }

        val shieldNoteId = if (streakShields > 0) " ($streakShields perisai aktif)" else ""
        val shieldNoteEn = if (streakShields > 0) " ($streakShields shield${if (streakShields == 1) "" else "s"} ready)" else ""

        val body = if (isIndonesian) {
            when {
                streakDays > 0 && todayWords == 0 ->
                    "Hanya 60 detik Seoul Blitz atau 1 Percikan Harian cukup untuk menjaga runtutan $streakDays harimu$shieldNoteId."
                dueCount > 0 && streakDays > 0 ->
                    "Jaga runtutan $streakDays harimu$shieldNoteId! $dueCount kartu FSRS siap diulas dan $remaining kata lagi menuju stempel harian."
                dueCount > 0 ->
                    "Ada $dueCount kartu kosakata siap diulas agar daya ingat FSRS tetap tajam."
                remaining > 0 ->
                    "Tinggal $remaining kata lagi menuju target harianmu ($todayWords/$dailyGoal) dan stempel misi harian!"
                else ->
                    "Target harian tercapai! Coba 60 detik Seoul Blitz untuk memecahkan rekor pribadimu."
            }
        } else {
            when {
                streakDays > 0 && todayWords == 0 ->
                    "Just one 60-second Seoul Blitz or Daily Spark keeps your $streakDays-day flame alive$shieldNoteEn."
                dueCount > 0 && streakDays > 0 ->
                    "Keep your $streakDays-day streak glowing$shieldNoteEn! $dueCount FSRS cards due and $remaining words left for today's seal."
                dueCount > 0 ->
                    "$dueCount Korean flashcards are ready at peak FSRS memory timing."
                remaining > 0 ->
                    "$remaining more words to complete today's goal ($todayWords/$dailyGoal) and earn your Daily Seal!"
                else ->
                    "Daily goal crushed! Jump into a 60s Seoul Blitz to beat your personal best."
            }
        }

        val launchIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            appContext,
            REQUEST_CODE_OPEN_APP,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_baeum_reminder)
            .setColor(0xFF5B46C8.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
            true
        }.getOrDefault(false)
    }

    const val ACTION_DAILY_STUDY_REMINDER = "com.example.action.DAILY_STUDY_REMINDER"

    private fun buildAlarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_STUDY_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
