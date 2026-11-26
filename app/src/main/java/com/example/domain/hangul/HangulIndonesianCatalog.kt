package com.example.domain.hangul

/**
 * Natural Bahasa Indonesia localization for Hangul Jamo notes, Sound Change Rules,
 * and Korean number system usage guides.
 */
data class SoundChangeRuleId(
    val titleId: String,
    val explanationId: String,
    val exampleMeaningId: String
)

data class JamoLocalizedId(
    val pronunciationNoteId: String,
    val sampleWordMeaningId: String
)

object HangulIndonesianCatalog {

    val soundChangeRulesId: Map<String, SoundChangeRuleId> = mapOf(
        "liaison" to SoundChangeRuleId(
            titleId = "Penghubungan Bunyi (Liaison)",
            explanationId = "Ketika konsonan akhir (batchim, selain ㅇ) diikuti suku kata yang diawali ㅇ senyap, bunyi konsonan akhir berpindah mulus menjadi konsonan awal suku kata berikutnya.",
            exampleMeaningId = "Bahasa Korea"
        ),
        "nasalization" to SoundChangeRuleId(
            titleId = "Nasalisasi (Asimilasi Sengau)",
            explanationId = "Konsonan akhir ㄱ, ㄷ, ㅂ berubah menjadi bunyi sengau ㅇ (ng), ㄴ (n), ㅁ (m) ketika diikuti langsung oleh ㄴ atau ㅁ.",
            exampleMeaningId = "Terima kasih (formal)"
        ),
        "tensification" to SoundChangeRuleId(
            titleId = "Penegangan Konsonan (Tensifikasi)",
            explanationId = "Konsonan dasar ㄱ, ㄷ, ㅂ, ㅅ, ㅈ berubah menjadi konsonan tegang (ㄲ, ㄸ, ㅃ, ㅆ, ㅉ) setelah bunyi henti akhir ㄱ, ㄷ, ㅂ.",
            exampleMeaningId = "Sekolah"
        ),
        "aspiration" to SoundChangeRuleId(
            titleId = "Aspirasi (Penggabungan ㅎ)",
            explanationId = "Saat ㅎ bertemu ㄱ, ㄷ, ㅂ, atau ㅈ dalam urutan mana pun, keduanya melebur menjadi konsonan berembus ㅋ, ㅌ, ㅍ, atau ㅊ.",
            exampleMeaningId = "Bagus / Baik"
        ),
        "liquidization" to SoundChangeRuleId(
            titleId = "Likuidasi (Asimilasi ㄹ)",
            explanationId = "Ketika ㄴ dan ㄹ bertemu (ㄴ+ㄹ atau ㄹ+ㄴ), keduanya melebur menjadi bunyi [ㄹ] ganda yang lembut.",
            exampleMeaningId = "Silla / Kontak"
        ),
        "palatalization" to SoundChangeRuleId(
            titleId = "Palatalisasi",
            explanationId = "Konsonan akhir ㄷ atau ㅌ yang diikuti akhiran gramatikal 이 bergeser bunyinya menjadi ㅈ atau ㅊ.",
            exampleMeaningId = "Bersama"
        )
    )

    val jamoId: Map<String, JamoLocalizedId> = mapOf(
        // Basic consonants
        "ㄱ" to JamoLocalizedId("Bunyi 'g' lembut di antara vokal; 'k' ringan di awal kata atau sebagai batchim.", "Tas"),
        "ㄴ" to JamoLocalizedId("Ujung lidah menyentuh belakang gigi atas, sama seperti 'n' pada 'nama'.", "Pohon"),
        "ㄷ" to JamoLocalizedId("Mirip 't' lembut di awal kata, 'd' di antara vokal, dan 't' tertahan di batchim.", "Jembatan / Kaki"),
        "ㄹ" to JamoLocalizedId("Bunyi 'r' ketukan singkat di antara vokal; bunyi 'l' jelas di akhir suku kata.", "Ramen"),
        "ㅁ" to JamoLocalizedId("Bunyi sengau bibir tertutup 'm', sama seperti 'm' dalam bahasa Indonesia.", "Hati / Pikiran"),
        "ㅂ" to JamoLocalizedId("Mirip 'p' ringan di awal kata, 'b' di antara vokal, dan 'p' tertahan di batchim.", "Laut"),
        "ㅅ" to JamoLocalizedId("Bunyi 's' lembut; berubah menjadi 'sy' sebelum vokal ㅣ, ㅑ, ㅕ, ㅛ, ㅠ.", "Orang"),
        "ㅇ" to JamoLocalizedId("Tidak berbunyi (penanda) di awal suku kata; berbunyi 'ng' sebagai batchim.", "Anak"),
        "ㅈ" to JamoLocalizedId("Bunyi 'j/c' tanpa embusan napas; berbunyi 't' tertahan di batchim.", "Mobil"),
        "ㅊ" to JamoLocalizedId("Bunyi 'ch' berembus kuat dengan hembusan udara yang jelas.", "Teman"),
        "ㅋ" to JamoLocalizedId("Bunyi 'k' berembus kuat dengan hembusan udara jelas.", "Kopi"),
        "ㅌ" to JamoLocalizedId("Bunyi 't' berembus kuat dengan hembusan udara jelas.", "Tomat"),
        "ㅍ" to JamoLocalizedId("Bunyi 'p' berembus kuat dengan hembusan udara jelas.", "Anggur"),
        "ㅎ" to JamoLocalizedId("Bunyi 'h' berembus; sering melemah atau membuat konsonan sebelahnya beraspirasi.", "Langit"),

        // Double (tense) consonants
        "ㄲ" to JamoLocalizedId("Bunyi 'k' tegang tanpa hembusan udara, nada awal sedikit lebih tinggi.", "Bunga"),
        "ㄸ" to JamoLocalizedId("Bunyi 't' tegang tanpa hembusan udara, dilepas tajam dengan pita suara menegang.", "Stroberi"),
        "ㅃ" to JamoLocalizedId("Bunyi 'p' tegang tanpa hembusan udara, kuat dan rapat di bibir.", "Roti"),
        "ㅆ" to JamoLocalizedId("Bunyi 's' tajam dan tegang dengan aliran udara lebih kuat.", "Menulis / Memakai"),
        "ㅉ" to JamoLocalizedId("Bunyi 'c/j' tegang tanpa hembusan udara dengan tekanan lidah kuat.", "Asin"),

        // Basic vowels
        "ㅏ" to JamoLocalizedId("Vokal 'a' terbuka seperti pada kata 'pagi'.", "Pagi"),
        "ㅑ" to JamoLocalizedId("Bunyi luncuran 'ya' seperti pada 'yakin'.", "Bisbol"),
        "ㅓ" to JamoLocalizedId("Bunyi 'eo' (mirip 'o' terbuka dengan bibir rileks tidak membulat).", "Ibu"),
        "ㅕ" to JamoLocalizedId("Gabungan bunyi 'y' + 'eo' (yeo).", "Musim panas"),
        "ㅗ" to JamoLocalizedId("Bunyi 'o' dengan bibir membulat rapat ke depan.", "Hari ini"),
        "ㅛ" to JamoLocalizedId("Gabungan bunyi 'y' + 'o' membulat (yo).", "Memasak"),
        "ㅜ" to JamoLocalizedId("Bunyi 'u' membulat seperti pada kata 'susu'.", "Susu"),
        "ㅠ" to JamoLocalizedId("Bunyi 'yu' seperti pada 'yuran'.", "Kaca"),
        "ㅡ" to JamoLocalizedId("Bunyi 'eu' seperti 'e' pada kata 'elang' dengan bibir ditarik datar.", "Musik"),
        "ㅣ" to JamoLocalizedId("Bunyi 'i' tinggi dan jelas seperti pada 'ibu'.", "Nama"),

        // Compound vowels
        "ㅐ" to JamoLocalizedId("Dalam bahasa Korea modern, ㅐ dan ㅔ terdengar sangat mirip dengan 'e' pada 'enak'.", "Anjing"),
        "ㅔ" to JamoLocalizedId("Bunyi 'e' seperti pada kata 'meja'.", "Dunia"),
        "ㅒ" to JamoLocalizedId("Bunyi 'ye' seperti pada 'yes'.", "Cerita / Obrolan"),
        "ㅖ" to JamoLocalizedId("Bunyi 'ye' seperti pada 'yey'.", "Reservasi"),
        "ㅘ" to JamoLocalizedId("Gabungan ㅗ + ㅏ menjadi 'wa'.", "Buah"),
        "ㅙ" to JamoLocalizedId("Gabungan ㅗ + ㅐ menjadi 'we'.", "Mengapa"),
        "ㅚ" to JamoLocalizedId("Dilafalkan mirip 'we' dalam dialek Seoul modern.", "Luar negeri"),
        "ㅝ" to JamoLocalizedId("Gabungan ㅜ + ㅓ menjadi 'wo'.", "Rumah sakit"),
        "ㅞ" to JamoLocalizedId("Gabungan ㅜ + ㅔ menjadi 'we'.", "Pernikahan"),
        "ㅟ" to JamoLocalizedId("Gabungan ㅜ + ㅣ menjadi 'wi'.", "Bahaya"),
        "ㅢ" to JamoLocalizedId("Gabungan ㅡ + ㅣ; sering dilafalkan 'i' atau 'e' saat menjadi partikel kepemilikan.", "Dokter"),

        // Batchim groups
        "ㄱ / ㅋ / ㄲ" to JamoLocalizedId("Bunyi akhir [k] tertahan tanpa dilepas (contoh: 책, 부엌, 밖).", "Buku"),
        "ㄷ / ㅅ / ㅆ / ㅈ / ㅊ / ㅌ / ㅎ" to JamoLocalizedId("Semua melebur menjadi bunyi [t] tertahan di akhir suku kata (contoh: 옷, 낮, 꽃, 끝).", "Bunga"),
        "ㅂ / ㅍ" to JamoLocalizedId("Bunyi akhir [p] tertahan dengan bibir menutup (contoh: 밥, 앞).", "Nasi / Makanan")
    )

    val batchimSingleId: Map<String, JamoLocalizedId> = mapOf(
        "ㄴ" to JamoLocalizedId("Bunyi sengau [n] di akhir suku kata (contoh: 산, 눈).", "Gunung"),
        "ㄹ" to JamoLocalizedId("Bunyi akhir [l] yang jelas (contoh: 물, 달).", "Air"),
        "ㅁ" to JamoLocalizedId("Bunyi sengau bibir tertutup [m] (contoh: 밤, 마음).", "Malam / Kastanye"),
        "ㅇ" to JamoLocalizedId("Bunyi sengau [ng] di belakang lidah (contoh: 방, 강).", "Kamar")
    )

    val koreanNumbersUsageId: Map<Int, String> = mapOf(
        1 to "Asli: menghitung benda, jam, umur. Sino: tanggal, menit, uang, lantai.",
        2 to "두 시 (jam 2) vs 이 분 (2 menit).",
        3 to "세 개 (3 buah) vs 삼 월 (Maret).",
        4 to "네 명 (4 orang) vs 사 층 (lantai 4).",
        5 to "다섯 시간 (5 jam) vs 오천 원 (5.000 won).",
        6 to "여섯 살 (6 tahun) vs 육 일 (tanggal 6).",
        7 to "일곱 시 (jam 7) vs 칠 월 (Juli).",
        8 to "여덟 권 (8 buku) vs 팔십 (80).",
        9 to "아홉 잔 (9 gelas) vs 구 호선 (Jalur 9).",
        10 to "열 명 (10 orang) vs 십 분 (10 menit).",
        20 to "스무 살 (20 tahun) vs 이십 일 (tanggal 20).",
        100 to "Angka Korea asli berhenti di 99; 100 ke atas selalu memakai Sino-Korea (백, 천, 만)."
    )
}

fun SoundChangeRule.localizedTitle(isIndonesian: Boolean): String =
    if (isIndonesian) HangulIndonesianCatalog.soundChangeRulesId[id]?.titleId ?: title else title

fun SoundChangeRule.localizedExplanation(isIndonesian: Boolean): String =
    if (isIndonesian) HangulIndonesianCatalog.soundChangeRulesId[id]?.explanationId ?: explanation else explanation

fun SoundChangeRule.localizedExampleMeaning(isIndonesian: Boolean): String =
    if (isIndonesian) HangulIndonesianCatalog.soundChangeRulesId[id]?.exampleMeaningId ?: exampleMeaning else exampleMeaning

fun HangulJamoInfo.localizedPronunciationNote(isIndonesian: Boolean): String {
    if (!isIndonesian) return pronunciationNote
    if (category == JamoCategory.BATCHIM) {
        HangulIndonesianCatalog.batchimSingleId[glyph]?.let { return it.pronunciationNoteId }
    }
    return HangulIndonesianCatalog.jamoId[glyph]?.pronunciationNoteId ?: pronunciationNote
}

fun HangulJamoInfo.localizedSampleMeaning(isIndonesian: Boolean): String {
    if (!isIndonesian) return sampleWordMeaning
    if (category == JamoCategory.BATCHIM) {
        HangulIndonesianCatalog.batchimSingleId[glyph]?.let { return it.sampleWordMeaningId }
    }
    return HangulIndonesianCatalog.jamoId[glyph]?.sampleWordMeaningId ?: sampleWordMeaning
}

fun KoreanNumberEntry.localizedUsageNote(isIndonesian: Boolean): String =
    if (isIndonesian) HangulIndonesianCatalog.koreanNumbersUsageId[value] ?: usageNote else usageNote
