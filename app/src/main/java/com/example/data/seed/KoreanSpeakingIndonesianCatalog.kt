package com.example.data.seed

/**
 * Full Bahasa Indonesia localization catalog for all 30 Speaking Scenarios and every turn
 * in [KoreanSpeakingSeed], keeping scenario seed data clean and modular.
 */
data class ScenarioLocalizedId(
    val titleId: String,
    val settingDescriptionId: String,
    val partnerRoleId: String
)

data class TurnLocalizedId(
    val translationId: String,
    val hintId: String,
    val modelResponseMeaningId: String
)

object KoreanSpeakingIndonesianCatalog {

    val scenarioMap: Map<String, ScenarioLocalizedId> = mapOf(
        // ==================== BEGINNER (10) ====================
        "beg_cafe" to ScenarioLocalizedId(
            titleId = "Di Kafe Korea",
            settingDescriptionId = "Pesan minuman, pilih ukuran, dan tentukan minum di tempat atau bawa pulang di Seoul.",
            partnerRoleId = "Barista"
        ),
        "beg_intro" to ScenarioLocalizedId(
            titleId = "Perkenalan Diri Pertama",
            settingDescriptionId = "Perkenalkan nama, asal negara, dan hobi dengan sopan kepada teman baru.",
            partnerRoleId = "Teman Baru (민지)"
        ),
        "beg_restaurant" to ScenarioLocalizedId(
            titleId = "Memesan di Restoran Korea",
            settingDescriptionId = "Minta meja, pesan hidangan Korea, dan minta air minum dengan sopan.",
            partnerRoleId = "Pelayan"
        ),
        "beg_subway" to ScenarioLocalizedId(
            titleId = "Bertanya Arah Stasiun Kereta Bawah Tanah",
            settingDescriptionId = "Tanyakan kepada warga lokal cara menuju stasiun kereta bawah tanah terdekat.",
            partnerRoleId = "Warga Seoul"
        ),
        "beg_shop" to ScenarioLocalizedId(
            titleId = "Belanja Pakaian di Myeongdong",
            settingDescriptionId = "Tanyakan harga, minta ukuran lain, dan selesaikan pembayaran di toko.",
            partnerRoleId = "Staf Toko"
        ),
        "beg_convenience" to ScenarioLocalizedId(
            titleId = "Di Minimarket (Pyoni-jeom)",
            settingDescriptionId = "Beli camilan, tolak kantong plastik dengan sopan, dan isi saldo kartu T-money.",
            partnerRoleId = "Kasir"
        ),
        "beg_taxi" to ScenarioLocalizedId(
            titleId = "Naik Taksi di Seoul",
            settingDescriptionId = "Beri tahu tujuan kepada sopir taksi dan minta berhenti di pintu masuk.",
            partnerRoleId = "Sopir Taksi"
        ),
        "beg_weather" to ScenarioLocalizedId(
            titleId = "Obrolan Ringan Tentang Cuaca",
            settingDescriptionId = "Berbincang santai dengan teman sekelas tentang cuaca hari ini dan musim favorit.",
            partnerRoleId = "Teman Sekelas (준호)"
        ),
        "beg_pharmacy" to ScenarioLocalizedId(
            titleId = "Di Apotek Korea",
            settingDescriptionId = "Jelaskan gejala flu ringan dan pahami aturan minum obat dari apoteker.",
            partnerRoleId = "Apoteker"
        ),
        "beg_work_greet" to ScenarioLocalizedId(
            titleId = "Hari Pertama Bekerja (EPS)",
            settingDescriptionId = "Sapa kepala tim pabrik dengan hormat pada hari pertama kerja di Korea.",
            partnerRoleId = "Kepala Tim (반장님)"
        ),

        // ==================== INTERMEDIATE (10) ====================
        "int_clinic" to ScenarioLocalizedId(
            titleId = "Berobat di Klinik Korea",
            settingDescriptionId = "Jelaskan gejala yang berlangsung beberapa hari dan tanyakan pantangan selama pemulihan.",
            partnerRoleId = "Dokter"
        ),
        "int_work_report" to ScenarioLocalizedId(
            titleId = "Melaporkan Masalah Mesin (EPS)",
            settingDescriptionId = "Laporkan suara mesin pabrik yang tidak normal dengan tenang dan konfirmasi langkah keselamatan.",
            partnerRoleId = "Supervisor (팀장님)"
        ),
        "int_schedule" to ScenarioLocalizedId(
            titleId = "Mengubah Jadwal Janji Temu",
            settingDescriptionId = "Minta maaf dengan sopan karena ada bentrok jadwal mendadak dan ajukan waktu pengganti.",
            partnerRoleId = "Rekan Kerja (수진)"
        ),
        "int_exchange" to ScenarioLocalizedId(
            titleId = "Menukar Produk Cacat",
            settingDescriptionId = "Jelaskan kerusakan barang yang baru dibeli dan ajukan penukaran dengan struk pembelian.",
            partnerRoleId = "Manajer Toko"
        ),
        "int_bank" to ScenarioLocalizedId(
            titleId = "Membuka Rekening Bank Korea",
            settingDescriptionId = "Buka rekening gaji dan ajukan layanan mobile banking serta pengiriman uang ke luar negeri.",
            partnerRoleId = "Petugas Bank"
        ),
        "int_housing" to ScenarioLocalizedId(
            titleId = "Menyewa Officetel / Studio di Seoul",
            settingDescriptionId = "Diskusikan uang jaminan (보증금), sewa bulanan (월세), dan jarak ke stasiun bersama agen properti.",
            partnerRoleId = "Agen Properti"
        ),
        "int_topik_spk_1" to ScenarioLocalizedId(
            titleId = "TOPIK Berbicara: Rutinitas Akhir Pekan",
            settingDescriptionId = "Latih jawaban terstruktur TOPIK Speaking untuk menjelaskan kebiasaan dan alasannya.",
            partnerRoleId = "Penguji TOPIK"
        ),
        "int_leave_req" to ScenarioLocalizedId(
            titleId = "Mengajukan Cuti Tahunan (EPS)",
            settingDescriptionId = "Ajukan cuti tahunan dengan sopan dan jelaskan rencana serah terima tugas.",
            partnerRoleId = "Manajer (과장님)"
        ),
        "int_travel_plan" to ScenarioLocalizedId(
            titleId = "Merencanakan Liburan ke Pulau Jeju",
            settingDescriptionId = "Atur rencana transportasi, kuliner lokal, dan wisata bersama teman Korea.",
            partnerRoleId = "Teman (지훈)"
        ),
        "int_delivery" to ScenarioLocalizedId(
            titleId = "Konfirmasi Pengiriman Paket (Taekbae)",
            settingDescriptionId = "Hubungi kurir untuk menitipkan paket di depan pintu saat sedang di luar rumah.",
            partnerRoleId = "Kurir"
        ),

        // ==================== ADVANCED (10) ====================
        "adv_interview" to ScenarioLocalizedId(
            titleId = "Wawancara Kerja Perusahaan Teknologi Korea",
            settingDescriptionId = "Jawab pertanyaan perilaku dan kecocokan peran menggunakan bahasa formal (합니다체).",
            partnerRoleId = "Pewawancara (면접관)"
        ),
        "adv_remote_work" to ScenarioLocalizedId(
            titleId = "Diskusi Kerja Jarak Jauh vs. Kantor",
            settingDescriptionId = "Sampaikan argumen berimbang dengan kata hubung formal (-는 반면에, -뿐만 아니라).",
            partnerRoleId = "Moderator Panel"
        ),
        "adv_ai_education" to ScenarioLocalizedId(
            titleId = "AI dalam Pendidikan Modern",
            settingDescriptionId = "Analisis peluang dan risiko etis penggunaan AI dalam pembelajaran dengan bahasa formal.",
            partnerRoleId = "Profesor"
        ),
        "adv_presentation" to ScenarioLocalizedId(
            titleId = "Laporan Proyek Kuartalan",
            settingDescriptionId = "Paparkan metrik capaian, analisis akar masalah, dan rencana perbaikan kuartal berikutnya.",
            partnerRoleId = "Direktur Eksekutif (이사님)"
        ),
        "adv_environment" to ScenarioLocalizedId(
            titleId = "Mobilitas Perkotaan Ramah Lingkungan",
            settingDescriptionId = "Bahas netralitas karbon, insentif transportasi umum, dan partisipasi warga.",
            partnerRoleId = "Pewawancara Kebijakan"
        ),
        "adv_culture_diff" to ScenarioLocalizedId(
            titleId = "Memahami Budaya Kerja Korea",
            settingDescriptionId = "Refleksikan hierarki, pengambilan keputusan bersama, dan komunikasi lintas budaya secara bijak.",
            partnerRoleId = "Mentor SDM"
        ),
        "adv_negotiation" to ScenarioLocalizedId(
            titleId = "Negosiasi Kontrak Mitra Pemasok",
            settingDescriptionId = "Negosiasikan jadwal pengiriman dan jaminan mutu secara diplomatis dalam bahasa formal.",
            partnerRoleId = "Perwakilan Mitra"
        ),
        "adv_media_literacy" to ScenarioLocalizedId(
            titleId = "Media Digital & Cek Fakta",
            settingDescriptionId = "Bahas misinformasi di platform video pendek dan kebiasaan konsumsi media yang bertanggung jawab.",
            partnerRoleId = "Jurnalis"
        ),
        "adv_aging_society" to ScenarioLocalizedId(
            titleId = "Perubahan Demografi & Masyarakat Menua",
            settingDescriptionId = "Bahas penuaan penduduk Korea, pembelajaran sepanjang hayat, dan empati antargenerasi.",
            partnerRoleId = "Pembawa Acara Sosiologi"
        ),
        "adv_korean_wave" to ScenarioLocalizedId(
            titleId = "Dampak Global Budaya Korea (Hallyu)",
            settingDescriptionId = "Analisis mengapa film, musik, dan bahasa Korea mendunia serta cara menjaga pertukaran budaya.",
            partnerRoleId = "Kritikus Budaya"
        )
    )

    val turnMap: Map<String, TurnLocalizedId> = mapOf(
        // beg_cafe
        "beg_cafe_1" to TurnLocalizedId(
            translationId = "Selamat datang! Anda ingin memesan apa?",
            hintId = "Sebutkan nama minuman + 주세요 (mis. 아이스 아메리카노 주세요).",
            modelResponseMeaningId = "Tolong satu gelas es Americano."
        ),
        "beg_cafe_2" to TurnLocalizedId(
            translationId = "Ukurannya mau yang bagaimana?",
            hintId = "Ucapkan 큰 사이즈로 주세요 (besar) atau 보통 사이즈로 주세요 (sedang).",
            modelResponseMeaningId = "Tolong ukuran besar."
        ),
        "beg_cafe_3" to TurnLocalizedId(
            translationId = "Minum di tempat atau dibungkus bawa pulang?",
            hintId = "Pilih 매장에서 마실게요 (di tempat) atau 포장해 주세요 (bawa pulang).",
            modelResponseMeaningId = "Tolong dibungkus bawa pulang."
        ),
        "beg_cafe_4" to TurnLocalizedId(
            translationId = "Totalnya 4.500 won. Pembayarannya mau pakai apa?",
            hintId = "Sebutkan 카드로 (dengan kartu) atau 현금으로 (tunai) + 계산할게요.",
            modelResponseMeaningId = "Saya bayar pakai kartu. Terima kasih."
        ),

        // beg_intro
        "beg_intro_1" to TurnLocalizedId(
            translationId = "Halo! Saya Minji. Siapa nama Anda?",
            hintId = "Sapa dengan sopan lalu sebut nama: 저는 [nama]예요/이에요.",
            modelResponseMeaningId = "Halo! Saya Alex. Senang bertemu dengan Anda."
        ),
        "beg_intro_2" to TurnLocalizedId(
            translationId = "Senang bertemu! Anda berasal dari negara mana?",
            hintId = "Gunakan pola [Negara]에서 왔어요.",
            modelResponseMeaningId = "Saya berasal dari Indonesia."
        ),
        "beg_intro_3" to TurnLocalizedId(
            translationId = "Sudah berapa lama Anda belajar bahasa Korea?",
            hintId = "Sebutkan durasi + 공부했어요 atau 배우고 있어요.",
            modelResponseMeaningId = "Saya sudah belajar bahasa Korea selama enam bulan."
        ),
        "beg_intro_4" to TurnLocalizedId(
            translationId = "Saat akhir pekan biasanya Anda melakukan apa?",
            hintId = "Ceritakan hobi akhir pekan dengan akhiran sopan -아요/어요.",
            modelResponseMeaningId = "Saat akhir pekan saya bertemu teman atau berolahraga."
        ),

        // beg_restaurant
        "beg_rest_1" to TurnLocalizedId(
            translationId = "Selamat datang! Untuk berapa orang?",
            hintId = "Gunakan angka Korea asli + 명이에요 (한 명, 두 명, 세 명).",
            modelResponseMeaningId = "Untuk dua orang."
        ),
        "beg_rest_2" to TurnLocalizedId(
            translationId = "Silakan duduk di sebelah sini. Sudah siap memesan?",
            hintId = "Sebutkan nama makanan Korea + 주세요.",
            modelResponseMeaningId = "Tolong satu bibimbap dan satu sup kimchi."
        ),
        "beg_rest_3" to TurnLocalizedId(
            translationId = "Apakah perlu minuman atau tambahan lainnya?",
            hintId = "Minta air dengan 물 주세요 atau tolak halus dengan 괜찮아요.",
            modelResponseMeaningId = "Tolong minta air minum. Terima kasih."
        ),

        // beg_subway
        "beg_sub_1" to TurnLocalizedId(
            translationId = "Ya, ada apa? Apakah Anda memerlukan bantuan?",
            hintId = "Tanyakan lokasi stasiun dengan pola 어디에 있어요?",
            modelResponseMeaningId = "Permisi, di mana stasiun kereta bawah tanah terdekat?"
        ),
        "beg_sub_2" to TurnLocalizedId(
            translationId = "Jalan lurus ke depan lalu belok kanan, nanti terlihat stasiun Jalur 2.",
            hintId = "Tanyakan waktu tempuh dengan 몇 분 걸려요? atau ucapkan terima kasih.",
            modelResponseMeaningId = "Kira-kira butuh berapa menit kalau jalan kaki?"
        ),
        "beg_sub_3" to TurnLocalizedId(
            translationId = "Sekitar 5 menit jalan kaki. Hati-hati di jalan!",
            hintId = "Ucapkan terima kasih dengan sopan (감사합니다 / 고맙습니다).",
            modelResponseMeaningId = "Terima kasih banyak sudah memberi tahu dengan ramah!"
        ),

        // beg_shop
        "beg_shop_1" to TurnLocalizedId(
            translationId = "Selamat datang! Sedang mencari barang tertentu?",
            hintId = "Tanyakan barang atau harganya dengan 얼마예요?",
            modelResponseMeaningId = "Jaket ini harganya berapa?"
        ),
        "beg_shop_2" to TurnLocalizedId(
            translationId = "Produk ini harganya 39.000 won. Mau dicoba dulu?",
            hintId = "Jawab 네, 입어 볼게요 atau tanyakan ukuran/warna lain.",
            modelResponseMeaningId = "Ya, saya coba dulu. Apakah ada ukuran yang lebih besar?"
        ),
        "beg_shop_3" to TurnLocalizedId(
            translationId = "Cocok sekali dengan Anda! Mau ambil yang ini?",
            hintId = "Konfirmasi pembelian dengan 이걸로 살게요 / 주세요.",
            modelResponseMeaningId = "Ya, saya beli yang ini. Tolong bayar pakai kartu."
        ),

        // beg_convenience
        "beg_conv_1" to TurnLocalizedId(
            translationId = "Halo! Apakah Anda perlu kantong plastik?",
            hintId = "Jawab 네, 주세요 (ya, tolong) atau 아니요, 괜찮아요 (tidak, terima kasih).",
            modelResponseMeaningId = "Tidak, tidak perlu kantong plastik."
        ),
        "beg_conv_2" to TurnLocalizedId(
            translationId = "Apakah kimbap segitiganya mau dihangatkan?",
            hintId = "Jawab 네, 데워 주세요 atau 아니요, 괜찮아요.",
            modelResponseMeaningId = "Ya, tolong dihangatkan."
        ),
        "beg_conv_3" to TurnLocalizedId(
            translationId = "Apakah ada lagi yang Anda butuhkan?",
            hintId = "Minta isi saldo 교통카드 atau jawab 괜찮아요, 감사합니다.",
            modelResponseMeaningId = "Tolong isi saldo kartu transportasi 10.000 won."
        ),

        // beg_taxi
        "beg_taxi_1" to TurnLocalizedId(
            translationId = "Silakan naik. Mau diantar ke mana?",
            hintId = "Sebutkan tujuan + 으로 가 주세요.",
            modelResponseMeaningId = "Tolong antar ke Stasiun Hongdae (Hongik Univ.)."
        ),
        "beg_taxi_2" to TurnLocalizedId(
            translationId = "Baik, mengerti. Jalannya agak macet, tidak apa-apa?",
            hintId = "Jawab dengan sopan lalu tanyakan perkiraan waktu tempuh.",
            modelResponseMeaningId = "Ya, tidak apa-apa. Kira-kira butuh berapa lama?"
        ),
        "beg_taxi_3" to TurnLocalizedId(
            translationId = "Sudah sampai! Mau turun di sini?",
            hintId = "Konfirmasi berhenti di sini dan ucapkan terima kasih kepada sopir.",
            modelResponseMeaningId = "Ya, tolong berhenti di sini. Saya bayar pakai kartu."
        ),

        // beg_weather
        "beg_wea_1" to TurnLocalizedId(
            translationId = "Cuaca hari ini cerah sekali ya! Tadi pagi tidak dingin?",
            hintId = "Jelaskan suhu cuaca (추웠어요 / 따뜻해요 / 시원해요).",
            modelResponseMeaningId = "Tadi pagi agak dingin, tetapi sekarang hangat."
        ),
        "beg_wea_2" to TurnLocalizedId(
            translationId = "Di antara empat musim di Korea, musim mana yang paling Anda suka?",
            hintId = "Pilih 봄 (semi), 여름 (panas), 가을 (gugur), atau 겨울 (dingin) + 좋아해요.",
            modelResponseMeaningId = "Saya paling suka musim gugur karena udaranya sejuk."
        ),
        "beg_wea_3" to TurnLocalizedId(
            translationId = "Kalau cuaca akhir pekan bagus, mau pergi ke Taman Sungai Han?",
            hintId = "Terima ajakan dengan antusias atau sarankan waktu lain secara sopan.",
            modelResponseMeaningId = "Ya, boleh! Ayo pergi bersama."
        ),

        // beg_pharmacy
        "beg_phar_1" to TurnLocalizedId(
            translationId = "Selamat datang. Bagian mana yang terasa tidak enak?",
            hintId = "Jelaskan gejala: 머리가 아파요 / 목이 아파요 / 감기약 주세요.",
            modelResponseMeaningId = "Saya kena flu, jadi tenggorokan sakit dan batuk."
        ),
        "beg_phar_2" to TurnLocalizedId(
            translationId = "Tidak demam? Apakah ada alergi terhadap obat tertentu?",
            hintId = "Konfirmasi apakah Anda mengalami demam/alergi (없어요).",
            modelResponseMeaningId = "Saya tidak demam dan tidak punya alergi obat."
        ),
        "beg_phar_3" to TurnLocalizedId(
            translationId = "Minum obat ini tiga kali sehari, 30 menit sesudah makan.",
            hintId = "Konfirmasi pemahaman Anda dengan sopan.",
            modelResponseMeaningId = "Baik, saya mengerti dengan jelas. Terima kasih."
        ),

        // beg_work_greet
        "beg_wk_1" to TurnLocalizedId(
            translationId = "Selamat datang! Hari ini hari pertama masuk kerja ya? Coba perkenalkan diri.",
            hintId = "Gunakan ragam formal 합니다체: 안녕하십니까, 저는 [nama]입니다. 잘 부탁드립니다.",
            modelResponseMeaningId = "Halo! Saya Alex, karyawan baru. Mohon bimbingannya."
        ),
        "beg_wk_2" to TurnLocalizedId(
            translationId = "Senang bertemu. Helm keselamatan dan sepatu safety sudah diterima semua?",
            hintId = "Jawab dengan 네, 받았습니다 atau 아직 못 받았습니다.",
            modelResponseMeaningId = "Ya, saya sudah menerima helm dan sepatu keselamatan."
        ),
        "beg_wk_3" to TurnLocalizedId(
            translationId = "Saat bekerja, kalau ada yang belum dipahami langsung tanyakan saja ya.",
            hintId = "Konfirmasi dengan sopan dan formal menggunakan 네, 알겠습니다.",
            modelResponseMeaningId = "Baik, saya mengerti! Saya akan bekerja dengan aman dan sungguh-sungguh."
        ),

        // int_clinic
        "int_cl_1" to TurnLocalizedId(
            translationId = "Halo, keluhan apa yang membuat Anda datang hari ini?",
            hintId = "Jelaskan kapan gejala dimulai (-부터) dan bagian yang sakit.",
            modelResponseMeaningId = "Sejak dua hari lalu perut saya tidak enak dan sakit kepala terus."
        ),
        "int_cl_2" to TurnLocalizedId(
            translationId = "Apakah akhir-akhir ini terlalu lelah atau makan sesuatu yang berbeda dari biasanya?",
            hintId = "Berikan alasan dengan penghubung -아/어서 atau -기 때문에.",
            modelResponseMeaningId = "Akhir-akhir ini banyak lembur sehingga saya kurang tidur."
        ),
        "int_cl_3" to TurnLocalizedId(
            translationId = "Istirahat yang cukup beberapa hari dan minum obat resep ini secara teratur. Ada pertanyaan?",
            hintId = "Ajukan pertanyaan lanjutan praktis dengan -(으)ㄹ까요 atau -나요?",
            modelResponseMeaningId = "Apakah ada makanan yang harus dihindari selama minum obat?"
        ),

        // int_work_report
        "int_wr_1" to TurnLocalizedId(
            translationId = "Ada apa? Apakah terjadi masalah mendesak?",
            hintId = "Laporkan masalah secara formal memakai -아/어서 보고드립니다.",
            modelResponseMeaningId = "Pak Supervisor, saya melapor karena mesin nomor 3 mengeluarkan suara tidak biasa."
        ),
        "int_wr_2" to TurnLocalizedId(
            translationId = "Apakah daya listrik mesinnya sudah dimatikan sekarang?",
            hintId = "Konfirmasi pemadaman daya dan tindakan keselamatan dalam 합니다체.",
            modelResponseMeaningId = "Ya, demi keselamatan saya langsung mematikan daya dan memasang tanda berhenti."
        ),
        "int_wr_3" to TurnLocalizedId(
            translationId = "Penanganan yang tepat. Bisa bantu di jalur lain sampai tim teknisi datang?",
            hintId = "Konfirmasi kesiapan Anda dengan sopan memakai -겠습니다.",
            modelResponseMeaningId = "Baik, mengerti. Saya segera pindah ke jalur 2 untuk bekerja."
        ),

        // int_schedule
        "int_sch_1" to TurnLocalizedId(
            translationId = "Halo? Anda menelepon soal janji besok malam ya?",
            hintId = "Minta maaf, jelaskan alasannya, lalu tanya -(으)ㄹ 수 있을까요?",
            modelResponseMeaningId = "Maaf, besok mendadak ada rapat—bolehkah kita mengubah jam janji kita?"
        ),
        "int_sch_2" to TurnLocalizedId(
            translationId = "Tidak masalah! Kalau begitu minggu ini kapan Anda sempat?",
            hintId = "Ajukan hari dan jam spesifik dengan -은/는 어떠세요?",
            modelResponseMeaningId = "Bagaimana kalau hari Jumat jam 7 malam?"
        ),
        "int_sch_3" to TurnLocalizedId(
            translationId = "Jumat jam 7 cocok! Tempatnya tetap di Stasiun Gangnam?",
            hintId = "Konfirmasi lokasi dan ucapkan terima kasih atas pengertiannya.",
            modelResponseMeaningId = "Ya, setuju! Terima kasih atas pengertiannya. Sampai jumpa hari Jumat."
        ),

        // int_exchange
        "int_ex_1" to TurnLocalizedId(
            translationId = "Halo, pelanggan. Ada yang bisa saya bantu?",
            hintId = "Sebutkan barang yang dibeli, kerusakannya, dan 교환하고 싶어요.",
            modelResponseMeaningId = "Earphone yang saya beli di sini kemarin mati sebelah, jadi saya ingin menukarnya."
        ),
        "int_ex_2" to TurnLocalizedId(
            translationId = "Mohon maaf atas ketidaknyamanannya. Apakah Anda membawa struk dan kartu pembayarannya?",
            hintId = "Konfirmasi bahwa Anda membawa struk dan kartu.",
            modelResponseMeaningId = "Ya, struk dan kartunya ada di sini keduanya."
        ),
        "int_ex_3" to TurnLocalizedId(
            translationId = "Sudah saya tukar dengan produk baru. Mau dicoba dulu di sini?",
            hintId = "Konfirmasi barang berfungsi baik dan ucapkan terima kasih.",
            modelResponseMeaningId = "Ya, setelah dicek berfungsi dengan baik. Terima kasih sudah membantu dengan cepat."
        ),

        // int_bank
        "int_bk_1" to TurnLocalizedId(
            translationId = "Halo, transaksi perbankan apa yang ingin Anda lakukan?",
            hintId = "Sebutkan 통장/계좌 만들기 dan tujuannya (월급/생활비).",
            modelResponseMeaningId = "Saya datang karena ingin membuka rekening tabungan gaji."
        ),
        "int_bk_2" to TurnLocalizedId(
            translationId = "Apakah Anda membawa Kartu Registrasi Orang Asing (ARC) dan surat keterangan kerja?",
            hintId = "Konfirmasi dokumen yang Anda bawa.",
            modelResponseMeaningId = "Ya, saya membawa ARC dan surat keterangan kerja dari perusahaan."
        ),
        "int_bk_3" to TurnLocalizedId(
            translationId = "Apakah sekalian ingin mendaftar mobile banking dan fitur transfer luar negeri?",
            hintId = "Setujui pendaftaran dan ajukan pertanyaan praktis soal bank.",
            modelResponseMeaningId = "Ya, tolong daftarkan keduanya. Berapa biaya transfer ke luar negeri?"
        ),

        // int_housing
        "int_hs_1" to TurnLocalizedId(
            translationId = "Selamat datang! Kriteria kamar seperti apa yang sedang Anda cari?",
            hintId = "Jelaskan preferensi kamar (역 근처, 월세, 조용한 곳).",
            modelResponseMeaningId = "Saya mencari kamar studio yang dekat stasiun kereta bawah tanah dan pencahayaannya bagus."
        ),
        "int_hs_2" to TurnLocalizedId(
            translationId = "Berapa perkiraan anggaran Anda untuk uang jaminan dan sewa bulanan?",
            hintId = "Sebutkan anggaran lalu tanyakan tentang 관리비 (biaya pemeliharaan).",
            modelResponseMeaningId = "Sekitar jaminan 5 juta won dan sewa 600 ribu won per bulan. Apakah biaya perawatan terpisah?"
        ),
        "int_hs_3" to TurnLocalizedId(
            translationId = "Ada dua kamar yang sesuai kriteria, mau langsung lihat sekarang?",
            hintId = "Setujui untuk melihat kamar sekarang.",
            modelResponseMeaningId = "Ya, boleh! Ayo langsung lihat sekarang."
        ),

        // int_topik_spk_1
        "int_ts1_1" to TurnLocalizedId(
            translationId = "Saat stres, biasanya cara apa yang paling sering Anda gunakan untuk mengatasinya?",
            hintId = "Berikan 1–2 kebiasaan nyata memakai -거나 dengan akhiran sopan/formal.",
            modelResponseMeaningId = "Saat stres saya berjalan santai di Taman Sungai Han atau mendengarkan musik favorit."
        ),
        "int_ts1_2" to TurnLocalizedId(
            translationId = "Menurut Anda, mengapa cara tersebut efektif bagi diri Anda?",
            hintId = "Jelaskan alasan secara jelas menggunakan -기 때문입니다.",
            modelResponseMeaningId = "Karena berjalan sambil menghirup udara segar membuat pikiran jernih dan suasana hati membaik."
        ),
        "int_ts1_3" to TurnLocalizedId(
            translationId = "Jika ada hobi baru yang ingin Anda coba di masa depan, silakan perkenalkan.",
            hintId = "Gunakan pola -아/어 보고 싶습니다 disertai alasan singkat.",
            modelResponseMeaningId = "Ke depan saya ingin belajar masakan tradisional Korea agar bisa memasak dan berbagi dengan teman."
        ),

        // int_leave_req
        "int_lv_1" to TurnLocalizedId(
            translationId = "Tadi Anda bilang ingin bicara sebentar? Ada perlu apa?",
            hintId = "Sampaikan alasan dan lama cuti dengan sopan dalam 합니다체.",
            modelResponseMeaningId = "Pak Manajer, bulan depan ada acara keluarga jadi saya ingin mengajukan cuti 3 hari."
        ),
        "int_lv_2" to TurnLocalizedId(
            translationId = "Bagaimana dengan jadwal tugas yang sedang Anda tangani selama periode tersebut?",
            hintId = "Yakinkan atasan mengenai serah terima pekerjaan (인수인계).",
            modelResponseMeaningId = "Sebelum berangkat saya akan menyelesaikan tugas lebih dulu dan menyerahkannya kepada rekan kerja."
        ),
        "int_lv_3" to TurnLocalizedId(
            translationId = "Rencananya sudah tertata baik. Isi formulir cuti dan serahkan sore ini ya.",
            hintId = "Ucapkan terima kasih dan konfirmasi pengumpulan formulir.",
            modelResponseMeaningId = "Terima kasih, Pak Manajer. Saya akan mengisi dan menyerahkan formulirnya sebelum sore ini."
        ),

        // int_travel_plan
        "int_tr_1" to TurnLocalizedId(
            translationId = "Tidak sabar menunggu liburan ke Jeju bulan depan! Tempat mana yang paling ingin Anda kunjungi pertama kali?",
            hintId = "Sebutkan tempat di Jeju + 가고 싶어요.",
            modelResponseMeaningId = "Begitu tiba, saya ingin pergi ke kafe dengan pemandangan laut yang indah."
        ),
        "int_tr_2" to TurnLocalizedId(
            translationId = "Setuju! Untuk transportasi, lebih baik sewa mobil atau naik bus saja?",
            hintId = "Bandingkan pilihan memakai -(으)니까 dan -(으)ㄹ 것 같아요.",
            modelResponseMeaningId = "Karena banyak tempat yang ingin dikunjungi, sepertinya menyewa mobil lebih praktis."
        ),
        "int_tr_3" to TurnLocalizedId(
            translationId = "Benar juga! Penginapan biar saya yang cari, daftar restoran kita cari bersama ya.",
            hintId = "Nyatakan komitmen tugas Anda memakai -(으)ㄹ게요.",
            modelResponseMeaningId = "Terima kasih! Saya akan mencari restoran terkenal di Jeju lalu membagikannya besok."
        ),

        // int_delivery
        "int_dl_1" to TurnLocalizedId(
            translationId = "Halo, ini kurir paket. Apakah Anda sedang di rumah sekarang?",
            hintId = "Jelaskan Anda sedang di luar dan minta ditaruh di depan pintu (문 앞).",
            modelResponseMeaningId = "Maaf, saya sedang di kantor sekarang, mohon letakkan saja di depan pintu."
        ),
        "int_dl_2" to TurnLocalizedId(
            translationId = "Baik. Apakah saya memerlukan sandi pintu masuk utama gedung?",
            hintId = "Tawarkan kirim via SMS (문자로 보내 드릴게요) atau arahkan ke pos satpam (경비실).",
            modelResponseMeaningId = "Ya, saya akan langsung mengirimkan sandi pintu lobi lewat pesan singkat sekarang."
        ),
        "int_dl_3" to TurnLocalizedId(
            translationId = "Sudah dikonfirmasi! Setelah paket terkirim saya akan kirimkan fotonya.",
            hintId = "Ucapkan terima kasih kepada kurir dengan sopan.",
            modelResponseMeaningId = "Terima kasih atas kerja kerasnya di cuaca panas ini. Sangat saya hargai!"
        ),

        // adv_interview
        "adv_iv_1" to TurnLocalizedId(
            translationId = "Selamat pagi/siang. Silakan perkenalkan diri secara singkat serta motivasi Anda melamar di perusahaan kami.",
            hintId = "Hubungkan pengalaman (경험) dengan kontribusi (기여) dalam ragam formal 합니다체.",
            modelResponseMeaningId = "Halo. Saya Alex, pengembang yang fokus pada solusi pengguna. Saya melamar karena sejalan dengan filosofi inovasi perusahaan ini."
        ),
        "adv_iv_2" to TurnLocalizedId(
            translationId = "Ceritakan pengalaman Anda saat menyelesaikan perbedaan pendapat dalam kerja tim.",
            hintId = "Gunakan struktur STAR: mendengarkan (경청) → dasar objektif (객관적 근거) → hasil solusi.",
            modelResponseMeaningId = "Saya mendengarkan pandangan rekan terlebih dahulu, lalu membandingkan data pengguna secara objektif untuk mencapai kompromi bersama."
        ),
        "adv_iv_3" to TurnLocalizedId(
            translationId = "Target konkret apa yang ingin Anda capai dalam satu tahun pertama setelah bergabung?",
            hintId = "Nyatakan target terukur dalam 1 tahun dengan akhiran -겠습니다.",
            modelResponseMeaningId = "Saya akan cepat memahami alur kerja tim, meningkatkan stabilitas layanan, dan berkontribusi pada proyek UX global."
        ),

        // adv_remote_work
        "adv_rw_1" to TurnLocalizedId(
            translationId = "Akhir-akhir ini sistem kerja hibrida banyak dibahas. Menurut Anda, apa kelebihan terbesar kerja jarak jauh?",
            hintId = "Sampaikan tesis yang jelas memakai -다는 점입니다 atau -기 때문입니다.",
            modelResponseMeaningId = "Kelebihan terbesarnya adalah menghemat waktu perjalanan sehingga fokus kerja dan keseimbangan hidup meningkat."
        ),
        "adv_rw_2" to TurnLocalizedId(
            translationId = "Di sisi lain, ada pendapat bahwa kerja tatap muka tetap sangat penting. Bagaimana pandangan Anda?",
            hintId = "Akui sudut pandang lawan memakai -는 측면이 있다.",
            modelResponseMeaningId = "Saya setuju komunikasi tatap muka jelas menguntungkan untuk brainstorming intensif dan adaptasi karyawan baru."
        ),
        "adv_rw_3" to TurnLocalizedId(
            translationId = "Kalau begitu, kebijakan kerja paling ideal dari sudut pandang perusahaan seperti apa?",
            hintId = "Tutup dengan rekomendasi kebijakan memakai -는 것이 바람직합니다.",
            modelResponseMeaningId = "Model hibrida fleksibel 2–3 hari ke kantor sesuai peran dan evaluasi berbasis hasil adalah yang paling ideal."
        ),

        // adv_ai_education
        "adv_ai_1" to TurnLocalizedId(
            translationId = "Penggunaan alat AI di dunia pendidikan berkembang pesat. Perubahan positif apa yang dirasakan pelajar?",
            hintId = "Gunakan kosakata formal: 맞춤형 학습, 즉각적 피드백, 효율성.",
            modelResponseMeaningId = "Pelajar bisa langsung memperoleh umpan balik personal sesuai level dan kelemahan masing-masing sehingga efisiensi belajar meningkat."
        ),
        "adv_ai_2" to TurnLocalizedId(
            translationId = "Namun kekhawatiran atas ketergantungan berlebihan juga besar. Dampak negatif apa yang harus diwaspadai?",
            hintId = "Soroti risiko daya pikir kritis atau akurasi informasi secara jelas.",
            modelResponseMeaningId = "Kemampuan berpikir kritis bisa melemah dan pelajar berisiko menerima informasi keliru tanpa verifikasi."
        ),
        "adv_ai_3" to TurnLocalizedId(
            translationId = "Ke depan, bagaimana seharusnya peran guru berubah?",
            hintId = "Bandingkan peran lama dan baru menggunakan -을/를 넘어.",
            modelResponseMeaningId = "Guru perlu bertransformasi melampaui penyampai informasi menjadi mentor kreativitas, kolaborasi, dan etika digital."
        ),

        // adv_presentation
        "adv_pr_1" to TurnLocalizedId(
            translationId = "Bisakah Anda paparkan singkat pencapaian utama proyek kuartal ini?",
            hintId = "Ringkas hasil yang terukur secara padat dan profesional.",
            modelResponseMeaningId = "Kuartal ini kami meningkatkan retensi pengguna sebesar 18% lewat pembaruan fitur inti dan menyelesaikan jadwal tepat waktu."
        ),
        "adv_pr_2" to TurnLocalizedId(
            translationId = "Jika ada target yang melambat dari rencana, jelaskan penyebab dan langkah penanganannya.",
            hintId = "Jelaskan penyebab secara jujur disertai langkah pencegahan konkret.",
            modelResponseMeaningId = "Terjadi kendala kompatibilitas saat pengujian multibahasa, sehingga kami menambahkan pengujian otomatis agar tidak terulang."
        ),
        "adv_pr_3" to TurnLocalizedId(
            translationId = "Di mana prioritas utama Anda untuk kuartal berikutnya?",
            hintId = "Tutup dengan pernyataan prioritas yang tegas berakhiran -계획입니다.",
            modelResponseMeaningId = "Kami berencana memusatkan fokus pada stabilitas sistem dan perbaikan kegunaan yang paling banyak diminta pengguna."
        ),

        // adv_environment
        "adv_en_1" to TurnLocalizedId(
            translationId = "Bagaimana cara mendorong penggunaan transportasi umum untuk menekan polusi udara kota besar?",
            hintId = "Usulkan kebijakan transportasi yang aplikatif dan nyata.",
            modelResponseMeaningId = "Kita perlu meringankan biaya tiket langganan dan mempersingkat jeda kedatangan jam sibuk agar lebih praktis dari mobil pribadi."
        ),
        "adv_en_2" to TurnLocalizedId(
            translationId = "Apa yang paling penting agar warga mau berpartisipasi sukarela dalam kebijakan ramah lingkungan?",
            hintId = "Bandingkan antara pembatasan ketat dan insentif positif.",
            modelResponseMeaningId = "Memberikan manfaat nyata bagi praktik ramah lingkungan dan membagikan dampak positifnya secara transparan sangat penting."
        ),
        "adv_en_3" to TurnLocalizedId(
            translationId = "Kebiasaan pribadi apa yang paling efektif untuk diterapkan sehari-hari?",
            hintId = "Hubungkan kebiasaan harian kecil dengan dampak jangka panjang.",
            modelResponseMeaningId = "Berjalan kaki atau bersepeda jarak dekat serta membiasakan gelas pakai ulang dan pemilahan sampah membawa perubahan besar."
        ),

        // adv_culture_diff
        "adv_cu_1" to TurnLocalizedId(
            translationId = "Saat bekerja di lingkungan kerja Korea, budaya apa yang paling berkesan atau butuh penyesuaian?",
            hintId = "Sampaikan refleksi budaya yang sopan dan berimbang.",
            modelResponseMeaningId = "Sapaan jabatan dan budaya pelaporan formal awalnya terasa asing, tetapi saya memahami hal itu mendukung rasa hormat dan kerja cepat."
        ),
        "adv_cu_2" to TurnLocalizedId(
            translationId = "Jika muncul kesalahpahaman akibat perbedaan budaya, bagaimana sebaiknya pekerja asing menyikapinya?",
            hintId = "Tekankan klarifikasi proaktif dan sikap saling menghormati.",
            modelResponseMeaningId = "Daripada berasumsi sendiri, bertanya konteks secara sopan dan menyamakan ekspektasi kerja secara terbuka adalah cara terbaik."
        ),
        "adv_cu_3" to TurnLocalizedId(
            translationId = "Nilai inti apa yang diperlukan agar tim multinasional bisa menciptakan sinergi?",
            hintId = "Sampaikan prinsip kepemimpinan atau kerja tim yang jelas.",
            modelResponseMeaningId = "Sikap inklusif yang melihat perbedaan sebagai kekuatan serta kesamaan tujuan adalah kunci utama sinergi."
        ),

        // adv_negotiation
        "adv_ng_1" to TurnLocalizedId(
            translationId = "Harga satuan yang Anda ajukan cukup ketat mengingat kenaikan bahan baku. Bisakah disesuaikan?",
            hintId = "Akui kendala mitra, lalu tawarkan solusi saling menguntungkan.",
            modelResponseMeaningId = "Kami memahami tekanan bahan baku. Kami mengusulkan kenaikan volume pesanan dalam kontrak jangka panjang agar harga tetap wajar."
        ),
        "adv_ng_2" to TurnLocalizedId(
            translationId = "Jika volume terjamin akan kami pertimbangkan, namun waktu produksi minimal butuh 3 minggu.",
            hintId = "Ajukan kompromi pengiriman bertahap (분할 배송).",
            modelResponseMeaningId = "Total pengiriman bisa 3 minggu, tetapi bisakah 30% batch awal dikirim dalam 2 minggu untuk kebutuhan mendesak?"
        ),
        "adv_ng_3" to TurnLocalizedId(
            translationId = "Pengiriman bertahap untuk batch awal memungkinkan. Mau kita tuangkan kesepakatan akhirnya dalam dokumen?",
            hintId = "Konfirmasi langkah lanjutan secara formal.",
            modelResponseMeaningId = "Ya, draf kontrak revisi sesuai kesepakatan hari ini akan kami kirimkan besok pagi. Terima kasih atas kerja samanya."
        ),

        // adv_media_literacy
        "adv_md_1" to TurnLocalizedId(
            translationId = "Tren konsumsi berita lewat video pendek makin meningkat. Apa kelebihan dan kekurangannya menurut Anda?",
            hintId = "Seimbangkan kelebihan (장점) dan kekurangan (단점) memakai -는 반면.",
            modelResponseMeaningId = "Kelebihannya cepat memahami inti informasi, namun kekurangannya konteks sering terpotong atau terdistorsi secara sensasional."
        ),
        "adv_md_2" to TurnLocalizedId(
            translationId = "Kebiasaan verifikasi apa yang perlu dimiliki individu agar tidak terjebak informasi palsu?",
            hintId = "Sebutkan cek sumber (출처 확인) dan verifikasi silang (교차 검증).",
            modelResponseMeaningId = "Kita perlu selalu mengecek sumber serta tanggal terbit, lalu memverifikasi silang lewat minimal dua media terpercaya."
        ),
        "adv_md_3" to TurnLocalizedId(
            translationId = "Tanggung jawab sosial apa yang seharusnya diemban perusahaan platform digital?",
            hintId = "Rumuskan tanggung jawab institusi secara jelas dan terstruktur.",
            modelResponseMeaningId = "Platform wajib meningkatkan transparansi algoritma rekomendasi dan memberi label jelas pada konten yang belum terverifikasi."
        ),

        // adv_aging_society
        "adv_ag_1" to TurnLocalizedId(
            translationId = "Korea termasuk negara dengan penuaan penduduk tercepat di dunia. Apa tantangan paling mendesak?",
            hintId = "Identifikasi prioritas sosial-ekonomi yang konkret.",
            modelResponseMeaningId = "Memperluas lapangan kerja berkualitas bagi lansia serta membangun sistem perawatan dan pensiun berkelanjutan adalah yang paling mendesak."
        ),
        "adv_ag_2" to TurnLocalizedId(
            translationId = "Adakah solusi untuk mengatasi kesenjangan digital di kalangan warga lanjut usia?",
            hintId = "Padukan pelatihan langsung dan desain antarmuka ramah lansia.",
            modelResponseMeaningId = "Pelatihan praktik di pusat komunitas perlu diperluas sekaligus mewajibkan antarmuka ramah lansia sejak tahap desain aplikasi dan kios."
        ),
        "adv_ag_3" to TurnLocalizedId(
            translationId = "Upaya apa yang diperlukan agar generasi muda dan lansia saling memahami dan hidup berdampingan?",
            hintId = "Tutup dengan visi sosial yang konstruktif.",
            modelResponseMeaningId = "Melihat satu sama lain sebagai mitra berbagi pengalaman, bukan beban, serta memperbanyak ruang interaksi antargenerasi sangat penting."
        ),

        // adv_korean_wave
        "adv_kw_1" to TurnLocalizedId(
            translationId = "Menurut Anda, apa alasan mendasar K-pop dan drama Korea sangat dicintai di seluruh dunia?",
            hintId = "Analisis beberapa faktor pendukung secara terstruktur.",
            modelResponseMeaningId = "Hal itu berkat alur cerita yang menangkap emosi universal manusia, kualitas produksi tinggi, dan interaksi erat dengan penggemar."
        ),
        "adv_kw_2" to TurnLocalizedId(
            translationId = "Agar Hallyu bertahan lama melampaui tren sesaat, hal apa yang paling penting?",
            hintId = "Tekankan pertukaran dua arah (상호 교류) dan keberagaman kreator.",
            modelResponseMeaningId = "Penting membangun pertukaran dua arah dengan budaya lokal dan menjaga ekosistem tempat kreator beragam bebas bereksperimen."
        ),
        "adv_kw_3" to TurnLocalizedId(
            translationId = "Bagaimana pendapat Anda tentang peran belajar bahasa dalam memahami suatu budaya?",
            hintId = "Kaitkan proses belajar bahasa dengan empati budaya.",
            modelResponseMeaningId = "Karena bahasa menyimpan cara berpikir dan perasaan suatu masyarakat, belajar bahasa Korea adalah pintu paling tulus untuk memahami budayanya."
        )
    )

    private val speakerRoleIdMap: Map<String, String> = mapOf(
        "Barista" to "Barista",
        "Server" to "Pelayan",
        "Local" to "Warga Lokal",
        "Clerk" to "Staf Toko",
        "Cashier" to "Kasir",
        "Driver" to "Sopir Taksi",
        "Pharmacist" to "Apoteker",
        "Doctor" to "Dokter",
        "Manager" to "Manajer Toko",
        "Teller" to "Petugas Bank",
        "Agent" to "Agen Properti",
        "Examiner" to "Penguji TOPIK",
        "Courier" to "Kurir",
        "Moderator" to "Moderator",
        "Professor" to "Profesor",
        "Interviewer" to "Pewawancara",
        "HR Mentor" to "Mentor SDM",
        "Partner" to "Mitra Usaha",
        "Journalist" to "Jurnalis",
        "Host" to "Pembawa Acara",
        "Critic" to "Kritikus Budaya"
    )

    fun localizedSpeakerRole(role: String, isIndonesian: Boolean): String {
        if (!isIndonesian) return role
        return speakerRoleIdMap[role] ?: role
    }
}

fun SpeakingScenario.localizedTitle(isIndonesian: Boolean): String =
    if (isIndonesian) KoreanSpeakingIndonesianCatalog.scenarioMap[id]?.titleId ?: title else title

fun SpeakingScenario.localizedSetting(isIndonesian: Boolean): String =
    if (isIndonesian) KoreanSpeakingIndonesianCatalog.scenarioMap[id]?.settingDescriptionId ?: settingDescription else settingDescription

fun SpeakingScenario.localizedPartnerRole(isIndonesian: Boolean): String =
    if (isIndonesian) KoreanSpeakingIndonesianCatalog.scenarioMap[id]?.partnerRoleId ?: partnerRole else partnerRole

fun ConversationTurnPrompt.localizedTranslation(isIndonesian: Boolean): String =
    if (isIndonesian) KoreanSpeakingIndonesianCatalog.turnMap[turnId]?.translationId ?: translationEn else translationEn

fun ConversationTurnPrompt.localizedHint(isIndonesian: Boolean): String =
    if (isIndonesian) KoreanSpeakingIndonesianCatalog.turnMap[turnId]?.hintId ?: hintEn else hintEn

fun ConversationTurnPrompt.localizedModelMeaning(isIndonesian: Boolean): String =
    if (isIndonesian) KoreanSpeakingIndonesianCatalog.turnMap[turnId]?.modelResponseMeaningId ?: modelResponseMeaning else modelResponseMeaning

fun ConversationTurnPrompt.localizedSpeakerRole(isIndonesian: Boolean): String =
    KoreanSpeakingIndonesianCatalog.localizedSpeakerRole(speakerRole, isIndonesian)
