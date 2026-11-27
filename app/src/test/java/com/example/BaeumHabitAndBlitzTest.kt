package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.local.CurriculumPathway
import com.example.data.local.DailyStudyEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SpeakingSessionEntity
import com.example.data.local.VocabEntity
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.domain.engagement.BaeumHabitEngine
import com.example.domain.engagement.EngagementProfileState
import com.example.domain.engagement.SeoulBlitzEngine
import com.example.domain.fsrs.FsrsRating
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class BaeumHabitAndBlitzTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createPrefsRepo(scope: TestScope, name: String): DataStoreUserPreferencesRepository {
        val ds = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tempFolder.root, "$name.preferences_pb") }
        )
        return DataStoreUserPreferencesRepository(ds)
    }

    private fun sampleBank(): List<VocabEntity> = listOf(
        VocabEntity("v1", "사랑", "sa-rang", "Love", "Cinta", "Noun", CurriculumPathway.TOPIK, "1급", "사랑해요.", "I love you.", "Aku cinta kamu."),
        VocabEntity("v2", "친구", "chin-gu", "Friend", "Teman", "Noun", CurriculumPathway.TOPIK, "1급", "친구를 만나요.", "I meet a friend.", "Saya bertemu teman."),
        VocabEntity("v3", "학교", "hak-gyo", "School", "Sekolah", "Noun", CurriculumPathway.TOPIK, "1급", "학교에 가요.", "I go to school.", "Saya pergi ke sekolah."),
        VocabEntity("v4", "시간", "si-gan", "Time", "Waktu", "Noun", CurriculumPathway.TOPIK, "1급", "시간이 없어요.", "No time.", "Tidak ada waktu."),
        VocabEntity("v5", "커피", "keo-pi", "Coffee", "Kopi", "Noun", CurriculumPathway.TOPIK, "1급", "커피 한 잔 주세요.", "One coffee please.", "Minta satu kopi."),
        VocabEntity("v6", "책", "chaek", "Book", "Buku", "Noun", CurriculumPathway.TOPIK, "1급", "책을 읽어요.", "I read a book.", "Saya membaca buku."),
        VocabEntity("v7", "하늘", "ha-neul", "Sky", "Langit", "Noun", CurriculumPathway.TOPIK, "1급", "하늘이 맑아요.", "The sky is clear.", "Langitnya cerah."),
        VocabEntity("v8", "노래", "no-rae", "Song", "Lagu", "Noun", CurriculumPathway.TOPIK, "1급", "노래를 불러요.", "I sing a song.", "Saya menyanyi lagu.")
    )

    @Test
    fun atelierRanksProgressSmoothlyFromSeedlingToSejongGrandmasterAndPrestige() {
        val lv1 = BaeumHabitEngine.computeRankProgress(0)
        assertEquals(1, lv1.currentRank.level)
        assertEquals("새싹", lv1.currentRank.titleKo)
        assertEquals(2, lv1.nextRank?.level)

        val lv3 = BaeumHabitEngine.computeRankProgress(400)
        assertEquals(3, lv3.currentRank.level)
        assertTrue(lv3.progressFraction in 0.1f..0.9f)

        val lv10 = BaeumHabitEngine.computeRankProgress(9250)
        assertEquals(10, lv10.currentRank.level)
        assertEquals("세종 대가", lv10.currentRank.titleKo)
        assertEquals(null, lv10.nextRank)
        assertEquals(0.5f, lv10.progressFraction, 0.01f)

        // Combo & mastery bonus XP scales cleanly
        val xpNormal = BaeumHabitEngine.computeFlashcardRatingXp(FsrsRating.GOOD, nextCombo = 1, newlyMastered = false)
        val xpFeverMastered = BaeumHabitEngine.computeFlashcardRatingXp(FsrsRating.EASY, nextCombo = 10, newlyMastered = true)
        assertEquals(15, xpNormal)
        assertEquals(55, xpFeverMastered)
    }

    @Test
    fun streakShieldAutoProtectsSingleMissedDayAndDailySealIsIdempotent() = runTest(UnconfinedTestDispatcher()) {
        val repo = createPrefsRepo(this, "shield_prefs")

        // Study 3 consecutive days
        repo.recordStudyActivity("2026-11-20")
        repo.recordStudyActivity("2026-11-21")
        val day3 = repo.recordStudyActivity("2026-11-22")
        assertEquals(3, day3.currentStreakDays)

        // Claim Daily Seal once -> earns +100 XP & +1 Streak Shield; second claim same day is idempotent
        assertTrue(repo.claimDailySealBonus("2026-11-22"))
        assertFalse(repo.claimDailySealBonus("2026-11-22"))
        val profileAfterSeal = repo.engagementProfileFlow.first()
        assertEquals(1, profileAfterSeal.streakShields)
        assertEquals(BaeumHabitEngine.DAILY_SEAL_BONUS_XP, profileAfterSeal.totalXp)

        // Skip 2026-11-23; evaluate continuity on 2026-11-24 -> Streak Shield automatically saves the 3-day streak!
        val protected = repo.evaluateStreakContinuity("2026-11-24")
        assertEquals(3, protected.currentStreakDays)
        assertEquals("2026-11-23", protected.lastStudyDayKey)
        assertTrue(protected.isAliveAsOf("2026-11-24"))

        val afterShield = repo.engagementProfileFlow.first()
        assertEquals(0, afterShield.streakShields)
        assertEquals("2026-11-23", afterShield.lastShieldUsedDayKey)

        // Studying on 2026-11-24 seamlessly extends streak to 4 days!
        val day4 = repo.recordStudyActivity("2026-11-24")
        assertEquals(4, day4.currentStreakDays)
    }

    @Test
    fun dailySeoulSparkAndDailyQuestsAndDojangStampsAreDeterministicAndGrounded() = runTest(UnconfinedTestDispatcher()) {
        val bank = sampleBank()
        val sparkA1 = BaeumHabitEngine.buildDailySeoulSpark(bank, CurriculumPathway.TOPIK, "ID", "2026-11-26")
        val sparkA2 = BaeumHabitEngine.buildDailySeoulSpark(bank, CurriculumPathway.TOPIK, "ID", "2026-11-26")
        assertNotNull(sparkA1)
        assertEquals(sparkA1, sparkA2)
        assertEquals(3, sparkA1!!.options.size)
        assertTrue(sparkA1.correctMeaning in sparkA1.options)

        // Daily Spark persistence is idempotent per day
        val repo = createPrefsRepo(this, "spark_prefs")
        assertTrue(repo.recordDailySparkCompleted("2026-11-26", 30))
        assertFalse(repo.recordDailySparkCompleted("2026-11-26", 30))
        assertEquals(30, repo.engagementProfileFlow.first().totalXp)

        // Daily Quests reflect real daily stats
        val incomplete = BaeumHabitEngine.buildDailyQuests(
            todayStat = DailyStudyEntity("2026-11-26", wordsReviewed = 3),
            dailyGoalWords = 15,
            isSparkDoneToday = true,
            todayKey = "2026-11-26"
        )
        assertEquals(3, incomplete.size)
        assertEquals(0, incomplete.count { it.isCompleted })

        val complete = BaeumHabitEngine.buildDailyQuests(
            todayStat = DailyStudyEntity("2026-11-26", wordsReviewed = 15, quizzesCompleted = 1, speakingSessions = 1),
            dailyGoalWords = 15,
            isSparkDoneToday = true,
            todayKey = "2026-11-26"
        )
        assertEquals(3, complete.count { it.isCompleted })

        // Dojang Seal Stamps evaluate cleanly from real records
        val eligible = BaeumHabitEngine.evaluateEligibleStampIds(
            profile = EngagementProfileState(
                totalXp = 1200,
                blitzBestScore = 650,
                bestComboEver = 12,
                dailySealClaimedDayKey = "2026-11-26"
            ),
            currentStreak = 7,
            longestStreak = 7,
            reviewedVocabCount = 60,
            quizRecords = listOf(QuizRecordEntity(1, "Recall", CurriculumPathway.TOPIK, 8, 8, 45, "", 0L, "2026-11-26")),
            speakingSessions = listOf(
                SpeakingSessionEntity(1, "s1", "Cafe", "Beginner", 3, 3, 88, 9f, 8f, 9f, 9f, "", "", "", "", 0L, "2026-11-26")
            ),
            examAttempts = listOf(
                ExamAttemptEntity(1, "TOPIK_I", "MOCK", 160, 200, "", "2급", "", 2, 20, 600, 0L, "2026-11-26")
            )
        )
        assertTrue("stamp_first_step" in eligible)
        assertTrue("stamp_streak_7" in eligible)
        assertTrue("stamp_combo_10" in eligible)
        assertTrue("stamp_blitz_500" in eligible)
        assertTrue("stamp_master_50" in eligible)
        assertTrue("stamp_perfect_quiz" in eligible)
        assertTrue("stamp_daily_seal" in eligible)
        assertTrue("stamp_voice_seoul" in eligible)
        assertTrue("stamp_exam_challenger" in eligible)
        assertFalse("stamp_streak_30" in eligible)

        val newlyUnlocked = repo.unlockStamps(eligible)
        assertEquals(eligible.size, newlyUnlocked.size)
        assertTrue(repo.unlockStamps(eligible).isEmpty())
    }

    @Test
    fun seoulBlitzEngineTracksFeverComboTimeBonusMissedWordsAndNewRecords() {
        val bank = sampleBank()
        val rng = Random(99)
        var state = SeoulBlitzEngine.startSession(
            allVocab = bank,
            activePathway = CurriculumPathway.TOPIK,
            translationLang = "EN",
            previousBestScore = 200,
            durationSec = 60,
            rng = rng
        )
        assertNotNull(state.currentPrompt)
        assertEquals(4, state.currentPrompt!!.options.size)

        // Hit 5 consecutive correct answers -> triggers Fever Mode & +2s time bonus on 5th hit!
        repeat(5) {
            val correct = state.currentPrompt!!.correctOption
            state = SeoulBlitzEngine.submitAnswer(state, correct, bank, rng)
        }
        assertEquals(5, state.combo)
        assertTrue(state.isFeverMode)
        assertEquals(3, state.multiplier)
        assertEquals(2, state.timeBonusEarnedSec)
        assertEquals(62, state.remainingSec) // 60 + 2s bonus
        assertTrue(state.score > 200)

        // Wrong answer resets combo, preserves score, and logs missed word
        val wrongOption = state.currentPrompt!!.options.first { it != state.currentPrompt!!.correctOption }
        val scoreBeforeMiss = state.score
        state = SeoulBlitzEngine.submitAnswer(state, wrongOption, bank, rng)
        assertEquals(0, state.combo)
        assertFalse(state.isFeverMode)
        assertEquals(scoreBeforeMiss, state.score)
        assertEquals(1, state.missedWords.size)

        // Finishing session detects new record and awards XP
        val finished = SeoulBlitzEngine.finishSession(state)
        assertTrue(finished.isFinished)
        assertTrue(finished.isNewRecord)
        assertTrue(finished.xpEarned >= BaeumHabitEngine.BLITZ_NEW_RECORD_BONUS_XP + 20)
    }
}
