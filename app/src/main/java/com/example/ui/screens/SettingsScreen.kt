package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.RomanizationMode
import com.example.data.local.UserSettings
import com.example.data.notifications.StudyReminderScheduler
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.bText
import com.example.ui.theme.localizedLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// SCREEN 24 / 24 — Settings (Appearance, Learning, Daily Reminder, Audio & Speech, Data & Privacy, JSON Backup/Restore)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    statusMessage: String?,
    onBack: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onTranslationLangChange: (String) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onRomanizationChange: (RomanizationMode) -> Unit,
    onFsrsRetentionChange: (Double) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    onDailyReminderChange: (Boolean, Int, Int) -> Unit,
    onSendTestReminder: () -> Unit,
    onExportBackupJson: suspend () -> String,
    onRestoreBackupJson: (String) -> Unit,
    onReportStatus: (String) -> Unit = {},
    onReplayOnboarding: () -> Unit,
    onClearStatus: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extended = LocalBaeumColors.current
    val isId = settings.translationLanguage.equals("ID", ignoreCase = true)
    var notifPermissionDenied by remember { mutableStateOf(false) }
    var pendingTestAfterGrant by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notifPermissionDenied = false
            if (pendingTestAfterGrant) {
                pendingTestAfterGrant = false
                onSendTestReminder()
            } else {
                onDailyReminderChange(true, settings.dailyReminderHour, settings.dailyReminderMinute)
            }
        } else {
            pendingTestAfterGrant = false
            notifPermissionDenied = true
        }
    }

    fun requestNotificationPermissionAndRun(isTestAction: Boolean, onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                notifPermissionDenied = false
                onGranted()
            } else {
                pendingTestAfterGrant = isTestAction
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            notifPermissionDenied = false
            onGranted()
        }
    }

    // Zero-permission Storage Access Framework (SAF) Backup & Restore — Google Play compliant!
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = onExportBackupJson()
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openOutputStream(uri)
                            ?: error("Unable to open selected destination file.")
                        stream.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                            out.flush()
                        }
                    }
                }.onSuccess {
                    onReportStatus(
                        if (isId) "Cadangan JSON lengkap berhasil diekspor."
                        else "Complete JSON backup exported successfully."
                    )
                }.onFailure { err ->
                    onReportStatus(
                        if (isId) "Gagal mengekspor cadangan: ${err.localizedMessage ?: "Kesalahan I/O"}"
                        else "Backup export failed: ${err.localizedMessage ?: "I/O error"}"
                    )
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: error("Unable to open selected backup file.")
                        stream.use { input ->
                            input.bufferedReader(Charsets.UTF_8).readText()
                        }
                    }
                }.onSuccess { content ->
                    if (content.isBlank()) {
                        onReportStatus(
                            if (isId) "File cadangan kosong."
                            else "Selected backup file is empty."
                        )
                    } else {
                        onRestoreBackupJson(content)
                    }
                }.onFailure { err ->
                    onReportStatus(
                        if (isId) "Gagal membaca file cadangan: ${err.localizedMessage ?: "Kesalahan I/O"}"
                        else "Could not read backup file: ${err.localizedMessage ?: "I/O error"}"
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Settings", "Pengaturan"),
            subtitle = bText("Personalize your learning", "Sesuaikan pengalaman belajarmu"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!statusMessage.isNullOrBlank()) {
                SeoulCard(borderColor = extended.mint, onClick = onClearStatus) {
                    Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = extended.mint)
                }
            }

            // APPEARANCE
            SettingsSectionHeader(bText("APPEARANCE", "TAMPILAN"))
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                SettingsCycleRow(
                    title = bText("Theme", "Tema"),
                    value = when (settings.themeMode) {
                        ThemeMode.DARK -> bText("Seoul Night (Dark)", "Malam Seoul (Gelap)")
                        ThemeMode.LIGHT -> bText("Seoul Morning (Light)", "Pagi Seoul (Terang)")
                        ThemeMode.SYSTEM -> bText("System", "Ikuti Sistem")
                    },
                    testTag = "settings_theme_row",
                    onClick = {
                        val next = when (settings.themeMode) {
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.SYSTEM
                            ThemeMode.SYSTEM -> ThemeMode.DARK
                        }
                        onThemeChange(next)
                    }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("App & translation language", "Bahasa aplikasi & terjemahan"),
                    value = if (settings.translationLanguage == "ID") "Bahasa Indonesia" else "English",
                    testTag = "settings_lang_row",
                    onClick = {
                        onTranslationLangChange(if (settings.translationLanguage == "EN") "ID" else "EN")
                    }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(bText("Font size", "Ukuran huruf"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${(settings.fontScale * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.fontScale,
                        onValueChange = onFontScaleChange,
                        valueRange = 0.85f..1.20f,
                        steps = 6
                    )
                }
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bText("Reduced motion", "Kurangi animasi"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = settings.reducedMotion, onCheckedChange = onReducedMotionChange)
                }
            }

            // LEARNING
            SettingsSectionHeader(bText("LEARNING", "BELAJAR"))
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                SettingsCycleRow(
                    title = bText("Daily goal", "Target harian"),
                    value = bText("${settings.dailyGoalWords} words", "${settings.dailyGoalWords} kata"),
                    testTag = "settings_goal_row",
                    onClick = {
                        val targets = listOf(10, 20, 35, 50)
                        val idx = targets.indexOf(settings.dailyGoalWords)
                        onDailyGoalChange(targets[(idx + 1) % targets.size])
                    }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("Romanization", "Romanisasi"),
                    value = settings.romanizationMode.localizedLabel(),
                    testTag = "settings_romanization_row",
                    onClick = {
                        val all = RomanizationMode.entries
                        val next = all[(all.indexOf(settings.romanizationMode) + 1) % all.size]
                        onRomanizationChange(next)
                    }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("FSRS target retention", "Target retensi FSRS"),
                    value = "${(settings.fsrsDesiredRetention * 100).toInt()}%",
                    testTag = "settings_fsrs_row",
                    onClick = {
                        val opts = listOf(0.85, 0.90, 0.94)
                        val idx = opts.indexOfFirst { kotlin.math.abs(it - settings.fsrsDesiredRetention) < 0.02 }
                        onFsrsRetentionChange(opts[(idx + 1) % opts.size])
                    }
                )
            }

            // DAILY STUDY REMINDER
            SettingsSectionHeader(bText("DAILY REMINDER", "PENGINGAT HARIAN"))
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                val formattedTime = StudyReminderScheduler.formatReminderTime(
                    settings.dailyReminderHour,
                    settings.dailyReminderMinute
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = bText("Daily study reminder", "Pengingat belajar harian"),
                            tint = if (settings.dailyReminderEnabled) extended.mint else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = bText("FSRS & streak reminder", "Pengingat kartu FSRS & streak"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.dailyReminderEnabled) {
                                    bText("Scheduled daily around $formattedTime", "Terjadwal setiap hari sekitar $formattedTime")
                                } else {
                                    bText("Get a gentle nudge when cards are due", "Dapatkan pengingat saat kartu FSRS jatuh tempo")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = settings.dailyReminderEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                requestNotificationPermissionAndRun(isTestAction = false) {
                                    onDailyReminderChange(true, settings.dailyReminderHour, settings.dailyReminderMinute)
                                }
                            } else {
                                onDailyReminderChange(false, settings.dailyReminderHour, settings.dailyReminderMinute)
                            }
                        },
                        modifier = Modifier.testTag("settings_reminder_switch")
                    )
                }

                if (notifPermissionDenied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bText(
                            "Notification permission was denied. Enable notifications for Baeum in Android Settings to receive daily reminders.",
                            "Izin notifikasi ditolak. Aktifkan notifikasi untuk Baeum di Pengaturan Android agar pengingat harian dapat muncul."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.gold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = bText("Preferred reminder time", "Waktu pengingat pilihan"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val presets = listOf(
                    Pair(8, 0) to bText("08:00 Morning", "08:00 Pagi"),
                    Pair(12, 30) to bText("12:30 Midday", "12:30 Siang"),
                    Pair(19, 0) to bText("19:00 Evening", "19:00 Malam"),
                    Pair(21, 0) to bText("21:00 Night", "21:00 Larut")
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (timePair, label) ->
                        val selected = settings.dailyReminderHour == timePair.first &&
                            settings.dailyReminderMinute == timePair.second
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (settings.dailyReminderEnabled) {
                                    onDailyReminderChange(true, timePair.first, timePair.second)
                                } else {
                                    requestNotificationPermissionAndRun(isTestAction = false) {
                                        onDailyReminderChange(true, timePair.first, timePair.second)
                                    }
                                }
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("settings_reminder_time_${timePair.first}_${timePair.second}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("Send test notification", "Kirim notifikasi uji coba"),
                    value = bText("Test now ›", "Uji sekarang ›"),
                    testTag = "settings_reminder_test",
                    onClick = {
                        requestNotificationPermissionAndRun(isTestAction = true) {
                            onSendTestReminder()
                        }
                    }
                )
            }

            // AUDIO & SPEECH
            SettingsSectionHeader(bText("AUDIO & SPEECH", "AUDIO & BICARA"))
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                SettingsCycleRow(
                    title = bText("Speech speed", "Kecepatan suara"),
                    value = String.format(java.util.Locale.US, "%.1f×", settings.speechRate),
                    testTag = "settings_speech_speed",
                    onClick = {
                        val rates = listOf(0.8f, 1.0f, 1.2f)
                        val idx = rates.indexOfFirst { kotlin.math.abs(it - settings.speechRate) < 0.05f }
                        onSpeechRateChange(rates[(idx + 1) % rates.size])
                    }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("Korean recognition locale", "Bahasa pengenalan suara Korea"),
                    value = bText("ko-KR (On-device / System)", "ko-KR (Perangkat / Sistem)"),
                    testTag = "settings_locale_row",
                    onClick = {}
                )
            }

            // DATA & PRIVACY
            SettingsSectionHeader(bText("DATA & PRIVACY", "DATA & PRIVASI"))
            SeoulCard(modifier = Modifier.fillMaxWidth()) {
                SettingsCycleRow(
                    title = bText("Offline content", "Konten luring (offline)"),
                    value = bText("Local SQLite Ready", "SQLite Lokal Siap"),
                    testTag = "settings_offline_row",
                    onClick = {}
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("Export JSON backup", "Ekspor cadangan JSON"),
                    value = bText("Save file ›", "Simpan file ›"),
                    testTag = "settings_export_backup",
                    onClick = { exportLauncher.launch("baeum_korean_backup_v2.json") }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("Restore JSON backup", "Pulihkan cadangan JSON"),
                    value = bText("Select file ›", "Pilih file ›"),
                    testTag = "settings_restore_backup",
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = bText("Replay onboarding", "Ulangi pengenalan awal"),
                    value = bText("Restart ›", "Mulai ulang ›"),
                    testTag = "settings_replay_onboarding",
                    onClick = onReplayOnboarding
                )
                HorizontalDivider(color = extended.cardBorder.copy(alpha = 0.5f))
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(bText("Privacy & Independence", "Privasi & Kemandirian"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bText(
                            "Baeum stores all study history, FSRS intervals, and exam logs locally on your device. No mandatory account, no cloud tracking, and temporary voice recordings are cleared automatically after each turn.",
                            "Baeum menyimpan seluruh riwayat belajar, jadwal FSRS, dan catatan ujian secara lokal di perangkatmu. Tanpa akun wajib, tanpa pelacakan cloud, dan rekaman suara sementara dihapus otomatis setelah setiap giliran."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsCycleRow(
    title: String,
    value: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}
