package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.local.RomanizationMode
import com.example.data.seed.ExamType
import com.example.data.seed.SpeakingDifficulty
import com.example.domain.exam.ExamSessionMode
import com.example.domain.quiz.QuizMode
import java.util.Locale

/**
 * Active UI & translation language across the entire Baeum experience ("EN" or "ID").
 * Automatically follows the user's choice in Onboarding or Settings, and defaults to
 * Indonesian ("ID") on Indonesian-locale Android devices.
 */
val LocalBaeumLang = staticCompositionLocalOf { "EN" }

fun defaultDeviceTranslationLanguage(): String {
    val lang = Locale.getDefault().language.lowercase(Locale.US)
    return if (lang == "id" || lang == "in") "ID" else "EN"
}

@Composable
fun isIndonesian(): Boolean = LocalBaeumLang.current.equals("ID", ignoreCase = true)

@Composable
fun bText(en: String, id: String): String = if (isIndonesian()) id else en

fun bStr(lang: String, en: String, id: String): String =
    if (lang.equals("ID", ignoreCase = true)) id else en

@Composable
fun QuizMode.localizedTitle(): String = if (isIndonesian()) {
    when (this) {
        QuizMode.RECALL -> "Kosakata"
        QuizMode.SENTENCE_ARRANGEMENT -> "Susun Kalimat"
        QuizMode.LISTENING -> "Menyimak"
        QuizMode.BATCHIM -> "Batchim"
        QuizMode.SENTENCE -> "Isi Kalimat"
        QuizMode.GRAMMAR -> "Tata Bahasa"
        QuizMode.MIXED -> "Kuis Campuran"
        QuizMode.WRITING -> "Mengetik"
        QuizMode.CONJUGATION -> "Konjugasi"
    }
} else title

@Composable
fun QuizMode.localizedSubtitle(): String = if (isIndonesian()) {
    when (this) {
        QuizMode.RECALL -> "Ingat arti & kata Korea"
        QuizMode.SENTENCE_ARRANGEMENT -> "Susun urutan kalimat Korea"
        QuizMode.LISTENING -> "Kenali kata dari audio"
        QuizMode.BATCHIM -> "Konsonan akhir (받침)"
        QuizMode.SENTENCE -> "Lengkapi bagian rumpang"
        QuizMode.GRAMMAR -> "Pola tata bahasa Korea"
        QuizMode.MIXED -> "Latihan adaptif terpadu"
        QuizMode.WRITING -> "Ketik kata dalam Hangul"
        QuizMode.CONJUGATION -> "Perubahan kata kerja"
    }
} else subtitle

@Composable
fun QuizMode.localizedBadge(): String = if (isIndonesian()) {
    when (this) {
        QuizMode.RECALL -> "Kosakata"
        QuizMode.SENTENCE_ARRANGEMENT -> "어순"
        QuizMode.LISTENING -> "Audio"
        QuizMode.BATCHIM -> "받침"
        QuizMode.SENTENCE -> "Konteks"
        QuizMode.GRAMMAR -> "문법"
        QuizMode.MIXED -> "Adaptif"
        QuizMode.WRITING -> "Ketik"
        QuizMode.CONJUGATION -> "활용"
    }
} else shortBadge

@Composable
fun ExamType.localizedSubtitle(): String = if (isIndonesian()) {
    when (this) {
        ExamType.TOPIK_I -> "Menyimak + Membaca • Level 1–2급"
        ExamType.TOPIK_II -> "Menyimak + Membaca + Menulis • Level 3–6급"
        ExamType.EPS_TOPIK -> "Menyimak + Membaca • Keselamatan & Dunia Kerja"
        ExamType.TOPIK_SPEAKING -> "Tugas Berbicara Terstruktur • Respons Lisan"
    }
} else subtitle

@Composable
fun ExamSessionMode.localizedLabel(): String = if (isIndonesian()) {
    when (this) {
        ExamSessionMode.PRACTICE -> "Mode latihan"
        ExamSessionMode.MOCK -> "Ujian simulasi"
    }
} else label

@Composable
fun RomanizationMode.localizedLabel(): String = if (isIndonesian()) {
    when (this) {
        RomanizationMode.ALWAYS -> "Selalu tampilkan"
        RomanizationMode.ON_BACK_ONLY -> "Hanya di sisi jawaban"
        RomanizationMode.HIDDEN -> "Sembunyikan (Khusus Hangul)"
    }
} else label

@Composable
fun SpeakingDifficulty.localizedLabel(): String = if (isIndonesian()) {
    when (this) {
        SpeakingDifficulty.BEGINNER -> "Pemula"
        SpeakingDifficulty.INTERMEDIATE -> "Menengah"
        SpeakingDifficulty.ADVANCED -> "Mahir"
    }
} else label

@Composable
fun SpeakingDifficulty.localizedDescription(): String = if (isIndonesian()) {
    when (this) {
        SpeakingDifficulty.BEGINNER -> "Romanisasi, terjemahan & panduan bicara tersedia"
        SpeakingDifficulty.INTERMEDIATE -> "Bantuan opsional tersedia saat diperlukan"
        SpeakingDifficulty.ADVANCED -> "Fokus penuh bahasa Korea & ragam bahasa formal"
    }
} else assistDescription
