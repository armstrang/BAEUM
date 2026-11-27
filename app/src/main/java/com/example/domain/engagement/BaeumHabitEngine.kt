package com.example.domain.engagement

import com.example.data.local.DailyStudyEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SpeakingSessionEntity
import com.example.data.local.VocabEntity
import com.example.data.repository.BaeumRepository
import com.example.domain.fsrs.FsrsRating
import java.util.Calendar
import kotlin.math.abs

/**
 * Prestigious Korean cultural rank tier ("Seoul Atelier Mastery Ranks").
 */
data class AtelierRank(
    val level: Int,
    val titleKo: String,
    val titleEn: String,
    val titleId: String,
    val badgeGlyph: String,
    val minXp: Int
)

/**
 * Snapshot of the learner's current rank progression.
 */
data class RankProgress(
    val currentRank: AtelierRank,
    val nextRank: AtelierRank?,
    val totalXp: Int,
    val xpIntoLevel: Int,
    val xpForNextLevel: Int,
    val progressFraction: Float
)

/**
 * One of the 7 daily beads in the weekly rhythm strip (Monday..Sunday or rolling 7 days).
 */
data class RhythmDayBead(
    val dayKey: String,
    val shortLabelEn: String,
    val shortLabelId: String,
    val isToday: Boolean,
    val isCompleted: Boolean,
    val isShielded: Boolean
)

/**
 * Single daily micro-quest tracked strictly from real Room + DataStore progress.
 */
data class DailyAtelierQuest(
    val id: String,
    val titleEn: String,
    val titleId: String,
    val subtitleEn: String,
    val subtitleId: String,
    val current: Int,
    val target: Int,
    val xpReward: Int,
    val actionRoute: String
) {
    val isCompleted: Boolean get() = current >= target
    val progressFraction: Float get() = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

/**
 * Deterministic 1-tap playable "Daily Seoul Spark" (오늘의 한마디) challenge shown right on Home.
 */
data class DailySeoulSpark(
    val dayKey: String,
    val vocabId: String,
    val hangul: String,
    val romanization: String,
    val correctMeaning: String,
    val options: List<String>,
    val exampleKo: String,
    val exampleTranslation: String,
    val curriculumBadge: String,
    val xpReward: Int = 30
)

/**
 * Collectible traditional Korean Seal Stamp (도장 / Dojang) earned from real milestones.
 */
data class DojangStamp(
    val id: String,
    val sealKo: String,
    val titleEn: String,
    val titleId: String,
    val conditionEn: String,
    val conditionId: String,
    val accentTag: String // "gold" | "mint" | "primary" | "cherry"
)

/**
 * Persisted engagement & progression snapshot stored in Jetpack DataStore.
 */
data class EngagementProfileState(
    val totalXp: Int = 0,
    val streakShields: Int = 0,
    val lastShieldUsedDayKey: String = "",
    val blitzBestScore: Int = 0,
    val blitzGamesPlayed: Int = 0,
    val bestComboEver: Int = 0,
    val dailySparkCompletedDayKey: String = "",
    val dailySealClaimedDayKey: String = "",
    val unlockedStampIds: Set<String> = emptySet(),
    val soundEffectsEnabled: Boolean = true,
    val completedBeginnerLessonIds: Set<String> = emptySet()
)

/**
 * Global non-blocking celebration event surfaced as a luxury obsidian-gold banner.
 */
enum class CelebrationKind {
    LEVEL_UP,
    DAILY_SEAL,
    STAMP_UNLOCK,
    DAILY_SPARK,
    NEW_BLITZ_RECORD,
    STREAK_SHIELD_SAVED
}

data class CelebrationBannerEvent(
    val id: Long = System.currentTimeMillis(),
    val kind: CelebrationKind,
    val titleEn: String,
    val titleId: String,
    val subtitleEn: String,
    val subtitleId: String,
    val badgeKo: String = "배움",
    val xpDelta: Int = 0,
    val showConfetti: Boolean = true
)

/**
 * Consolidated reactive habit snapshot powering Home Dashboard, Progress Vault & Reminders.
 */
data class HabitDashboardSnapshot(
    val rankProgress: RankProgress = BaeumHabitEngine.computeRankProgress(0),
    val streakShields: Int = 0,
    val shieldSavedStreakRecently: Boolean = false,
    val weeklyRhythm: List<RhythmDayBead> = emptyList(),
    val dailySpark: DailySeoulSpark? = null,
    val isDailySparkCompleted: Boolean = false,
    val dailyQuests: List<DailyAtelierQuest> = emptyList(),
    val completedQuestsCount: Int = 0,
    val isDailySealClaimed: Boolean = false,
    val canClaimDailySeal: Boolean = false,
    val blitzBestScore: Int = 0,
    val blitzGamesPlayed: Int = 0,
    val bestComboEver: Int = 0,
    val unlockedStampIds: Set<String> = emptySet(),
    val allStamps: List<DojangStamp> = BaeumHabitEngine.allStamps,
    val soundEffectsEnabled: Boolean = true,
    val completedBeginnerLessonIds: Set<String> = emptySet()
)

/**
 * Pure deterministic engagement, XP progression, Daily Quests, Daily Spark & Dojang Stamp engine.
 * Grounded 100% in real learner study telemetry — zero fake progress or artificial timers.
 */
object BaeumHabitEngine {

    const val MAX_STREAK_SHIELDS = 3
    const val DAILY_SPARK_XP = 30
    const val DAILY_SEAL_BONUS_XP = 100
    const val BLITZ_NEW_RECORD_BONUS_XP = 50
    const val WRITING_PRACTICE_XP = 35
    const val PRONUNCIATION_PASS_XP = 25
    const val SPEAKING_SESSION_BASE_XP = 75
    const val EXAM_PRACTICE_XP = 120
    const val EXAM_MOCK_XP = 200

    val atelierRanks: List<AtelierRank> = listOf(
        AtelierRank(1, "새싹", "Seedling", "Tunas Hangul", "芽", 0),
        AtelierRank(2, "한글 탐험가", "Hangul Explorer", "Penjelajah Hangul", "가", 120),
        AtelierRank(3, "서울 여행자", "Seoul Traveler", "Pelancong Seoul", "길", 320),
        AtelierRank(4, "홍대 단골", "Hongdae Regular", "Pengunjung Hongdae", "별", 650),
        AtelierRank(5, "북촌 학자", "Bukchon Scholar", "Cendekiawan Bukchon", "글", 1150),
        AtelierRank(6, "인사동 장인", "Insadong Artisan", "Pengrajin Insadong", "결", 1850),
        AtelierRank(7, "남산 관찰자", "Namsan Luminary", "Bintang Namsan", "빛", 2800),
        AtelierRank(8, "경복궁 명인", "Gyeongbokgung Master", "Maestro Gyeongbokgung", "궁", 4100),
        AtelierRank(9, "한강 거장", "Han River Virtuoso", "Virtuoso Han River", "강", 5800),
        AtelierRank(10, "세종 대가", "Sejong Grandmaster", "Mahaguru Sejong", "훈", 8000)
    )

    val allStamps: List<DojangStamp> = listOf(
        DojangStamp(
            id = "stamp_first_step",
            sealKo = "첫걸음",
            titleEn = "First Step",
            titleId = "Langkah Pertama",
            conditionEn = "Complete your first Korean review or spark",
            conditionId = "Selesaikan ulasan atau tantangan pertamamu",
            accentTag = "primary"
        ),
        DojangStamp(
            id = "stamp_streak_3",
            sealKo = "삼일연속",
            titleEn = "3-Day Spark",
            titleId = "Api 3 Hari",
            conditionEn = "Maintain a 3-day study streak",
            conditionId = "Jaga runtutan belajar 3 hari berturut-turut",
            accentTag = "gold"
        ),
        DojangStamp(
            id = "stamp_streak_7",
            sealKo = "칠일불꽃",
            titleEn = "7-Day Flame",
            titleId = "Kobaran 7 Hari",
            conditionEn = "Reach a 7-day unbroken study streak",
            conditionId = "Capai runtutan belajar 7 hari tanpa putus",
            accentTag = "gold"
        ),
        DojangStamp(
            id = "stamp_streak_30",
            sealKo = "삼십정진",
            titleEn = "30-Day Devotion",
            titleId = "Dedikasi 30 Hari",
            conditionEn = "Reach a 30-day study streak",
            conditionId = "Capai runtutan belajar 30 hari",
            accentTag = "cherry"
        ),
        DojangStamp(
            id = "stamp_combo_10",
            sealKo = "연속열배",
            titleEn = "Flow Virtuoso",
            titleId = "Virtuoso Kombo",
            conditionEn = "Hit a 10x combo in Flashcards, Quiz, or Blitz",
            conditionId = "Capai kombo 10x di Kartu, Kuis, atau Blitz",
            accentTag = "mint"
        ),
        DojangStamp(
            id = "stamp_blitz_500",
            sealKo = "번개명인",
            titleEn = "Seoul Blitz Star",
            titleId = "Bintang Blitz",
            conditionEn = "Score 500+ points in 60s Seoul Blitz",
            conditionId = "Raih 500+ poin di 60d Seoul Blitz",
            accentTag = "gold"
        ),
        DojangStamp(
            id = "stamp_master_50",
            sealKo = "오십어휘",
            titleEn = "Word Collector",
            titleId = "Kolektor 50 Kata",
            conditionEn = "Study or master 50 Korean words",
            conditionId = "Pelajari atau kuasai 50 kosakata Korea",
            accentTag = "mint"
        ),
        DojangStamp(
            id = "stamp_master_250",
            sealKo = "이백오십",
            titleEn = "Lexicon Keeper",
            titleId = "Penjaga Leksikon",
            conditionEn = "Study or master 250 Korean words",
            conditionId = "Pelajari atau kuasai 250 kosakata Korea",
            accentTag = "primary"
        ),
        DojangStamp(
            id = "stamp_perfect_quiz",
            sealKo = "백점만점",
            titleEn = "Flawless Precision",
            titleId = "Akurasi Sempurna",
            conditionEn = "Score 100% on any Smart Quiz session",
            conditionId = "Raih 100% benar pada satu sesi Kuis Cerdas",
            accentTag = "mint"
        ),
        DojangStamp(
            id = "stamp_daily_seal",
            sealKo = "삼관도장",
            titleEn = "Triple Atelier Seal",
            titleId = "Stempel Tiga Misi",
            conditionEn = "Complete all 3 Daily Quests in a day",
            conditionId = "Selesaikan ke-3 Misi Harian dalam sehari",
            accentTag = "gold"
        ),
        DojangStamp(
            id = "stamp_voice_seoul",
            sealKo = "서울목소리",
            titleEn = "Voice of Seoul",
            titleId = "Suara Seoul",
            conditionEn = "Complete a live Korean speaking session",
            conditionId = "Selesaikan sesi percakapan bahasa Korea",
            accentTag = "cherry"
        ),
        DojangStamp(
            id = "stamp_exam_challenger",
            sealKo = "토픽도전",
            titleEn = "TOPIK Challenger",
            titleId = "Penantang TOPIK",
            conditionEn = "Complete an authentic TOPIK or EPS exam",
            conditionId = "Selesaikan satu ujian TOPIK atau EPS",
            accentTag = "primary"
        )
    )

    fun computeRankProgress(totalXp: Int): RankProgress {
        val safeXp = totalXp.coerceAtLeast(0)
        var currentIndex = 0
        for (i in atelierRanks.indices) {
            if (safeXp >= atelierRanks[i].minXp) {
                currentIndex = i
            } else {
                break
            }
        }
        val currentRank = atelierRanks[currentIndex]
        val nextRank = atelierRanks.getOrNull(currentIndex + 1)
        if (nextRank == null) {
            // Infinite prestige cadence every 2,500 XP past Lv.10
            val overflow = safeXp - currentRank.minXp
            val step = 2500
            val into = overflow % step
            return RankProgress(
                currentRank = currentRank,
                nextRank = null,
                totalXp = safeXp,
                xpIntoLevel = into,
                xpForNextLevel = step,
                progressFraction = (into.toFloat() / step).coerceIn(0f, 1f)
            )
        }
        val span = (nextRank.minXp - currentRank.minXp).coerceAtLeast(1)
        val into = (safeXp - currentRank.minXp).coerceIn(0, span)
        return RankProgress(
            currentRank = currentRank,
            nextRank = nextRank,
            totalXp = safeXp,
            xpIntoLevel = into,
            xpForNextLevel = span,
            progressFraction = (into.toFloat() / span).coerceIn(0f, 1f)
        )
    }

    fun computeFlashcardRatingXp(
        rating: FsrsRating,
        nextCombo: Int,
        newlyMastered: Boolean
    ): Int {
        val base = when (rating) {
            FsrsRating.AGAIN -> 6
            FsrsRating.HARD -> 12
            FsrsRating.GOOD -> 15
            FsrsRating.EASY -> 20
        }
        val comboBonus = when {
            nextCombo >= 10 -> 10
            nextCombo >= 5 -> 6
            nextCombo >= 3 -> 3
            else -> 0
        }
        val masteryBonus = if (newlyMastered) 25 else 0
        return base + comboBonus + masteryBonus
    }

    fun computeQuizCompletionXp(
        correctAnswers: Int,
        totalQuestions: Int,
        maxStreak: Int
    ): Int {
        if (totalQuestions <= 0) return 0
        val answerXp = correctAnswers.coerceAtLeast(0) * 12
        val streakBonus = (maxStreak.coerceAtLeast(0) / 3) * 10
        val completionBonus = if (correctAnswers == totalQuestions) 80 else 40
        return answerXp + streakBonus + completionBonus
    }

    fun computeBlitzXp(score: Int, isNewRecord: Boolean): Int {
        val base = (score / 10).coerceAtLeast(20)
        return base + if (isNewRecord) BLITZ_NEW_RECORD_BONUS_XP else 0
    }

    /**
     * Builds the rolling 7-day habit bead strip ending today (oldest → today).
     */
    fun buildWeeklyRhythm(
        dailyStats: List<DailyStudyEntity>,
        todayEpochMs: Long,
        shieldedDayKey: String
    ): List<RhythmDayBead> {
        val activeDays = HashSet<String>(dailyStats.size)
        for (stat in dailyStats) {
            if (stat.wordsReviewed > 0 || stat.quizzesCompleted > 0 || stat.speakingSessions > 0 || stat.examsCompleted > 0) {
                activeDays.add(stat.dayKey)
            }
        }
        val todayKey = BaeumRepository.localDayKey(todayEpochMs)
        val cal = Calendar.getInstance().apply {
            timeInMillis = todayEpochMs
            add(Calendar.DAY_OF_YEAR, -6)
        }
        val beads = ArrayList<RhythmDayBead>(7)
        for (i in 0 until 7) {
            val key = BaeumRepository.localDayKey(cal.timeInMillis)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val (en, id) = dayShortLabels(dow)
            val isCompleted = key in activeDays
            val isShielded = !isCompleted && key == shieldedDayKey && shieldedDayKey.isNotBlank()
            beads.add(
                RhythmDayBead(
                    dayKey = key,
                    shortLabelEn = en,
                    shortLabelId = id,
                    isToday = key == todayKey,
                    isCompleted = isCompleted,
                    isShielded = isShielded
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return beads
    }

    private fun dayShortLabels(dayOfWeek: Int): Pair<String, String> = when (dayOfWeek) {
        Calendar.SUNDAY -> "S" to "M"
        Calendar.MONDAY -> "M" to "S"
        Calendar.TUESDAY -> "T" to "S"
        Calendar.WEDNESDAY -> "W" to "R"
        Calendar.THURSDAY -> "T" to "K"
        Calendar.FRIDAY -> "F" to "J"
        else -> "S" to "S"
    }

    /**
     * Deterministically picks today's 1-tap playable "Daily Seoul Spark" challenge from the active pathway.
     */
    fun buildDailySeoulSpark(
        allVocab: List<VocabEntity>,
        activePathway: String,
        translationLang: String,
        dayKey: String
    ): DailySeoulSpark? {
        if (allVocab.size < 4) return null
        val pathwayPool = allVocab.filter {
            it.curriculum == activePathway && it.meaningFor(translationLang).isNotBlank()
        }
        val primaryPool = if (pathwayPool.size >= 4) pathwayPool else allVocab.filter {
            it.meaningFor(translationLang).isNotBlank()
        }
        if (primaryPool.size < 4) return null

        val seedHash = abs(dayKey.hashCode() * 31 + activePathway.hashCode())
        val target = primaryPool[seedHash % primaryPool.size]
        val correctMeaning = target.meaningFor(translationLang).trim()

        val distractors = ArrayList<String>(2)
        val used = hashSetOf(correctMeaning.lowercase())
        val stride = (primaryPool.size / 7).coerceAtLeast(1)
        var cursor = (seedHash + stride) % primaryPool.size
        var attempts = 0
        while (distractors.size < 2 && attempts < primaryPool.size) {
            val cand = primaryPool[cursor]
            val meaning = cand.meaningFor(translationLang).trim()
            if (cand.id != target.id && meaning.isNotBlank() && used.add(meaning.lowercase())) {
                distractors.add(meaning)
            }
            cursor = (cursor + stride) % primaryPool.size
            attempts++
        }
        if (distractors.size < 2) return null

        val orderedOptions = mutableListOf(correctMeaning, distractors[0], distractors[1])
        // Deterministic rotation so the correct option isn't always first
        val shift = seedHash % 3
        val rotated = List(3) { idx -> orderedOptions[(idx + shift) % 3] }

        return DailySeoulSpark(
            dayKey = dayKey,
            vocabId = target.id,
            hangul = target.hangul,
            romanization = target.romanization,
            correctMeaning = correctMeaning,
            options = rotated,
            exampleKo = target.exampleKo,
            exampleTranslation = target.exampleFor(translationLang),
            curriculumBadge = target.subCategory.ifBlank { target.curriculum },
            xpReward = DAILY_SPARK_XP
        )
    }

    /**
     * Evaluates today's 3 real data-driven Daily Atelier Quests.
     */
    fun buildDailyQuests(
        todayStat: DailyStudyEntity?,
        dailyGoalWords: Int,
        isSparkDoneToday: Boolean,
        todayKey: String,
        blitzGamesPlayedToday: Boolean = false
    ): List<DailyAtelierQuest> {
        val wordsToday = todayStat?.wordsReviewed ?: 0
        val quizzesToday = todayStat?.quizzesCompleted ?: 0
        val activeSkillToday = (todayStat?.speakingSessions ?: 0) + (todayStat?.examsCompleted ?: 0)
        val recallTarget = dailyGoalWords.coerceIn(5, 10)
        val sparkBoostWords = if (isSparkDoneToday) wordsToday + 2 else wordsToday

        val quest1 = DailyAtelierQuest(
            id = "quest_recall_$todayKey",
            titleEn = "Memory Warm-Up",
            titleId = "Pemanasan Memori",
            subtitleEn = "Review $recallTarget Korean cards today",
            subtitleId = "Ulas $recallTarget kartu Korea hari ini",
            current = sparkBoostWords.coerceAtMost(recallTarget),
            target = recallTarget,
            xpReward = 35,
            actionRoute = "smart_flashcard"
        )

        val quest2 = DailyAtelierQuest(
            id = "quest_sprint_$todayKey",
            titleEn = "Sharp Recall Sprint",
            titleId = "Sprint Daya Ingat",
            subtitleEn = "Finish 1 Smart Quiz or 60s Seoul Blitz",
            subtitleId = "Selesaikan 1 Kuis Cerdas atau 60d Seoul Blitz",
            current = if (quizzesToday >= 1 || blitzGamesPlayedToday) 1 else 0,
            target = 1,
            xpReward = 35,
            actionRoute = "seoul_blitz"
        )

        val masteryCurrent = if (activeSkillToday > 0 || wordsToday >= dailyGoalWords) 1 else 0
        val quest3 = DailyAtelierQuest(
            id = "quest_atelier_$todayKey",
            titleEn = "Atelier Commitment",
            titleId = "Komitmen Atelier",
            subtitleEn = "Hit daily word goal ($dailyGoalWords) or 1 Speak/Exam",
            subtitleId = "Capai target harian ($dailyGoalWords) atau 1 Bicara/Ujian",
            current = masteryCurrent,
            target = 1,
            xpReward = 35,
            actionRoute = "speaking_hub"
        )

        return listOf(quest1, quest2, quest3)
    }

    /**
     * Evaluates which traditional Dojang Stamps should be unlocked based on real learner records.
     */
    fun evaluateEligibleStampIds(
        profile: EngagementProfileState,
        currentStreak: Int,
        longestStreak: Int,
        reviewedVocabCount: Int,
        quizRecords: List<QuizRecordEntity>,
        speakingSessions: List<SpeakingSessionEntity>,
        examAttempts: List<ExamAttemptEntity>
    ): Set<String> {
        val earned = HashSet<String>(profile.unlockedStampIds)
        val bestStreak = maxOf(currentStreak, longestStreak)

        if (reviewedVocabCount > 0 || profile.totalXp > 0 || profile.dailySparkCompletedDayKey.isNotBlank()) {
            earned.add("stamp_first_step")
        }
        if (bestStreak >= 3) earned.add("stamp_streak_3")
        if (bestStreak >= 7) earned.add("stamp_streak_7")
        if (bestStreak >= 30) earned.add("stamp_streak_30")
        if (profile.bestComboEver >= 10) earned.add("stamp_combo_10")
        if (profile.blitzBestScore >= 500) earned.add("stamp_blitz_500")
        if (reviewedVocabCount >= 50) earned.add("stamp_master_50")
        if (reviewedVocabCount >= 250) earned.add("stamp_master_250")
        if (quizRecords.any { it.totalQuestions >= 5 && it.correctAnswers >= it.totalQuestions }) {
            earned.add("stamp_perfect_quiz")
        }
        if (profile.dailySealClaimedDayKey.isNotBlank()) {
            earned.add("stamp_daily_seal")
        }
        if (speakingSessions.isNotEmpty()) {
            earned.add("stamp_voice_seoul")
        }
        if (examAttempts.isNotEmpty()) {
            earned.add("stamp_exam_challenger")
        }
        return earned
    }
}
