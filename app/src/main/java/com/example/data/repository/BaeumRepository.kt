package com.example.data.repository

import android.content.res.AssetManager
import androidx.room.withTransaction
import com.example.data.local.BaeumDatabase
import com.example.data.local.DailyStudyEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.GrammarBookmarkEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.ReviewLogEntity
import com.example.data.local.SpeakingSessionEntity
import com.example.data.local.UserSettings
import com.example.data.local.VocabEntity
import com.example.data.seed.KoreanVocabSeed
import com.example.data.seed.VocabAssetLoader
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsRating
import com.example.domain.fsrs.FsrsScheduler
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayForecastPoint(
    val dayOffset: Int,
    val shortLabel: String,
    val dateKey: String,
    val dueCount: Int
)

@JsonClass(generateAdapter = true)
data class BaeumBackupPayload(
    val schemaVersion: Int = BaeumRepository.CURRENT_BACKUP_SCHEMA_VERSION,
    val exportedAtEpochMs: Long,
    val vocabStates: List<VocabProgressBackup>,
    val dailyStats: List<DailyStudyBackup>,
    val quizRecords: List<QuizRecordBackup>,
    val speakingSessions: List<SpeakingSessionBackup>,
    val examAttempts: List<ExamAttemptBackup>,
    val grammarBookmarks: List<GrammarBookmarkBackup> = emptyList(),
    val reviewLogs: List<ReviewLogBackup> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VocabProgressBackup(
    val id: String,
    val isBookmarked: Boolean,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Double,
    val scheduledDays: Double,
    val reps: Int,
    val lapses: Int,
    val stage: String,
    val lastReviewEpochMs: Long,
    val dueEpochMs: Long,
    val quizCorrectCount: Int,
    val quizWrongCount: Int
)

@JsonClass(generateAdapter = true)
data class DailyStudyBackup(
    val dayKey: String,
    val wordsReviewed: Int,
    val quizzesCompleted: Int,
    val speakingSessions: Int,
    val examsCompleted: Int,
    val studySeconds: Int
)

@JsonClass(generateAdapter = true)
data class QuizRecordBackup(
    val mode: String,
    val curriculum: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val durationSec: Int,
    val weakTopicNote: String,
    val timestampEpochMs: Long,
    val dayKey: String
)

@JsonClass(generateAdapter = true)
data class SpeakingSessionBackup(
    val scenarioId: String,
    val scenarioTitle: String,
    val difficulty: String,
    val totalTurns: Int,
    val completedTurns: Int,
    val overallScore: Int,
    val relevanceAvg: Float,
    val vocabAvg: Float,
    val grammarAvg: Float,
    val communicationAvg: Float,
    val strengths: String,
    val weaknesses: String,
    val correctionsSerialized: String,
    val recommendations: String,
    val timestampEpochMs: Long,
    val dayKey: String
)

@JsonClass(generateAdapter = true)
data class ExamAttemptBackup(
    val examType: String,
    val mode: String,
    val totalScore: Int,
    val maxScore: Int,
    val sectionScoresSerialized: String,
    val estimatedLevel: String,
    val analysisSummary: String,
    val wrongCount: Int,
    val totalQuestions: Int,
    val durationSec: Int,
    val timestampEpochMs: Long,
    val dayKey: String
)

@JsonClass(generateAdapter = true)
data class GrammarBookmarkBackup(
    val grammarId: String,
    val isMastered: Boolean = false,
    val bookmarkedAtEpochMs: Long = 0L
)

@JsonClass(generateAdapter = true)
data class ReviewLogBackup(
    val vocabId: String,
    val rating: Int,
    val stageBefore: String,
    val stageAfter: String,
    val intervalDays: Double,
    val timestampEpochMs: Long,
    val dayKey: String
)

class BaeumRepository(
    private val db: BaeumDatabase,
    val userPreferences: UserPreferencesRepository,
    private val assets: AssetManager? = null
) {
    private val moshi: Moshi = backupMoshi

    val settingsFlow: Flow<UserSettings> = userPreferences.userSettingsFlow
    val learningStreakFlow: Flow<LearningStreakState> = userPreferences.learningStreakFlow
    val allVocabFlow: Flow<List<VocabEntity>> = db.vocabDao().observeAllVocab()
    val reviewLogsFlow: Flow<List<ReviewLogEntity>> = db.reviewLogDao().observeAllLogs()
    val quizRecordsFlow: Flow<List<QuizRecordEntity>> = db.quizRecordDao().observeAll()
    val speakingSessionsFlow: Flow<List<SpeakingSessionEntity>> = db.speakingSessionDao().observeAll()
    val examAttemptsFlow: Flow<List<ExamAttemptEntity>> = db.examAttemptDao().observeAll()
    val dailyStatsFlow: Flow<List<DailyStudyEntity>> = db.dailyStudyDao().observeAll()
    val grammarBookmarksFlow: Flow<List<GrammarBookmarkEntity>> = db.grammarBookmarkDao().observeAll()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val count = db.vocabDao().countAll()
        if (count >= VocabAssetLoader.EXPECTED_MIN_VOCAB_COUNT) {
            return@withContext
        }
        val entries = if (assets != null) {
            VocabAssetLoader.loadMergedVocabulary(assets)
        } else {
            KoreanVocabSeed.allEntries()
        }
        db.withTransaction {
            entries.chunked(500).forEach { batch ->
                db.vocabDao().insertAllIgnore(batch)
            }
        }
    }

    suspend fun toggleVocabBookmark(vocabId: String, bookmarked: Boolean) = withContext(Dispatchers.IO) {
        db.vocabDao().setBookmark(vocabId, bookmarked)
    }

    suspend fun toggleGrammarBookmark(grammarId: String, currentlyBookmarked: Boolean) = withContext(Dispatchers.IO) {
        if (currentlyBookmarked) {
            db.grammarBookmarkDao().delete(grammarId)
        } else {
            db.grammarBookmarkDao().upsert(GrammarBookmarkEntity(grammarId = grammarId))
        }
    }

    /**
     * Atomically updates a card using genuine FSRS-4.5 scheduling, writes ReviewLog,
     * and increments today's local-timezone DailyStudyEntity.
     */
    suspend fun submitFlashcardRating(
        vocab: VocabEntity,
        rating: FsrsRating,
        desiredRetention: Double,
        studyDurationSec: Int = 15
    ): VocabEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dayKey = localDayKey(now)
        val result = FsrsScheduler.schedule(
            card = vocab.toFsrsSnapshot(),
            rating = rating,
            nowEpochMs = now,
            desiredRetention = desiredRetention
        )
        val next = result.nextState
        val updated = vocab.copy(
            stability = next.stability,
            difficulty = next.difficulty,
            elapsedDays = next.elapsedDays,
            scheduledDays = next.scheduledDays,
            reps = next.reps,
            lapses = next.lapses,
            stage = next.stage.name,
            lastReviewEpochMs = next.lastReviewEpochMs,
            dueEpochMs = next.dueEpochMs
        )
        db.withTransaction {
            db.vocabDao().updateVocab(updated)
            db.reviewLogDao().insertLog(
                ReviewLogEntity(
                    vocabId = vocab.id,
                    rating = rating.value,
                    stageBefore = vocab.stage,
                    stageAfter = updated.stage,
                    intervalDays = result.intervalDays,
                    timestampEpochMs = now,
                    dayKey = dayKey
                )
            )
            incrementDailyStatsInDb(
                dayKey = dayKey,
                wordsDelta = 1,
                secondsDelta = studyDurationSec.coerceIn(1, 300)
            )
        }
        userPreferences.recordStudyActivity(dayKey)
        updated
    }

    /**
     * Persists an already-computed FSRS schedule transition from [com.example.viewmodel.FlashcardFsrsViewModel],
     * writing the updated [VocabEntity], appending a [ReviewLogEntity], and updating daily study stats.
     */
    suspend fun persistFsrsReview(
        vocabBefore: VocabEntity,
        updatedVocab: VocabEntity,
        rating: FsrsRating,
        intervalDays: Double,
        studyDurationSec: Int,
        nowEpochMs: Long = System.currentTimeMillis()
    ): VocabEntity = withContext(Dispatchers.IO) {
        val dayKey = localDayKey(nowEpochMs)
        db.withTransaction {
            db.vocabDao().updateVocab(updatedVocab)
            db.reviewLogDao().insertLog(
                ReviewLogEntity(
                    vocabId = vocabBefore.id,
                    rating = rating.value,
                    stageBefore = vocabBefore.stage,
                    stageAfter = updatedVocab.stage,
                    intervalDays = intervalDays,
                    timestampEpochMs = nowEpochMs,
                    dayKey = dayKey
                )
            )
            incrementDailyStatsInDb(
                dayKey = dayKey,
                wordsDelta = 1,
                secondsDelta = studyDurationSec.coerceIn(2, 180)
            )
        }
        userPreferences.recordStudyActivity(dayKey)
        updatedVocab
    }

    /**
     * Restores a [VocabEntity] back to its pre-rating FSRS snapshot (used for Undo Last Rating).
     */
    suspend fun restoreVocabState(previousVocab: VocabEntity) = withContext(Dispatchers.IO) {
        db.vocabDao().updateVocab(previousVocab)
    }

    suspend fun recordQuizAnswerTelemetry(vocabId: String?, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        if (vocabId == null) return@withContext
        db.vocabDao().recordQuizOutcome(
            id = vocabId,
            correctDelta = if (isCorrect) 1 else 0,
            wrongDelta = if (isCorrect) 0 else 1
        )
    }

    suspend fun saveQuizRecord(
        mode: String,
        curriculum: String,
        totalQuestions: Int,
        correctAnswers: Int,
        durationSec: Int,
        weakTopicNote: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dayKey = localDayKey(now)
        db.withTransaction {
            db.quizRecordDao().insert(
                QuizRecordEntity(
                    mode = mode,
                    curriculum = curriculum,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers,
                    durationSec = durationSec,
                    weakTopicNote = weakTopicNote,
                    timestampEpochMs = now,
                    dayKey = dayKey
                )
            )
            incrementDailyStatsInDb(
                dayKey = dayKey,
                quizzesDelta = 1,
                secondsDelta = durationSec.coerceAtLeast(20)
            )
        }
        userPreferences.recordStudyActivity(dayKey)
    }

    suspend fun saveSpeakingSession(entity: SpeakingSessionEntity): Long = withContext(Dispatchers.IO) {
        val id = db.withTransaction {
            val insertedId = db.speakingSessionDao().insert(entity)
            incrementDailyStatsInDb(
                dayKey = entity.dayKey,
                speakingDelta = 1,
                secondsDelta = (entity.completedTurns * 40).coerceAtLeast(20)
            )
            insertedId
        }
        userPreferences.recordStudyActivity(entity.dayKey)
        userPreferences.recordRecentScenario(entity.scenarioId)
        id
    }

    suspend fun saveExamAttempt(entity: ExamAttemptEntity): Long = withContext(Dispatchers.IO) {
        val id = db.withTransaction {
            val insertedId = db.examAttemptDao().insert(entity)
            incrementDailyStatsInDb(
                dayKey = entity.dayKey,
                examsDelta = 1,
                secondsDelta = entity.durationSec.coerceAtLeast(10)
            )
            insertedId
        }
        userPreferences.recordStudyActivity(entity.dayKey)
        id
    }

    suspend fun recordStudySeconds(seconds: Int) = withContext(Dispatchers.IO) {
        if (seconds <= 0) return@withContext
        val dayKey = localDayKey()
        db.withTransaction {
            incrementDailyStatsInDb(dayKey = dayKey, secondsDelta = seconds)
        }
        userPreferences.recordStudyActivity(dayKey)
    }

    private suspend fun incrementDailyStatsInDb(
        dayKey: String,
        wordsDelta: Int = 0,
        quizzesDelta: Int = 0,
        speakingDelta: Int = 0,
        examsDelta: Int = 0,
        secondsDelta: Int = 0
    ) {
        val existing = db.dailyStudyDao().getByDay(dayKey) ?: DailyStudyEntity(dayKey = dayKey)
        db.dailyStudyDao().upsert(
            existing.copy(
                wordsReviewed = existing.wordsReviewed + wordsDelta,
                quizzesCompleted = existing.quizzesCompleted + quizzesDelta,
                speakingSessions = existing.speakingSessions + speakingDelta,
                examsCompleted = existing.examsCompleted + examsDelta,
                studySeconds = existing.studySeconds + secondsDelta
            )
        )
    }

    /**
     * Exports a versioned JSON backup of all user FSRS learning states, vocabulary & grammar bookmarks,
     * FSRS review logs, quiz records, speaking evaluations, exam history, and daily study analytics.
     */
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val vocab = db.vocabDao().getAllVocabOnce()
            .filter { it.reps > 0 || it.isBookmarked || it.quizCorrectCount > 0 || it.quizWrongCount > 0 }
            .map {
                VocabProgressBackup(
                    id = it.id, isBookmarked = it.isBookmarked, stability = it.stability,
                    difficulty = it.difficulty, elapsedDays = it.elapsedDays, scheduledDays = it.scheduledDays,
                    reps = it.reps, lapses = it.lapses, stage = it.stage,
                    lastReviewEpochMs = it.lastReviewEpochMs, dueEpochMs = it.dueEpochMs,
                    quizCorrectCount = it.quizCorrectCount, quizWrongCount = it.quizWrongCount
                )
            }
        val payload = BaeumBackupPayload(
            schemaVersion = CURRENT_BACKUP_SCHEMA_VERSION,
            exportedAtEpochMs = System.currentTimeMillis(),
            vocabStates = vocab,
            dailyStats = db.dailyStudyDao().getAllOnce().map {
                DailyStudyBackup(it.dayKey, it.wordsReviewed, it.quizzesCompleted, it.speakingSessions, it.examsCompleted, it.studySeconds)
            },
            quizRecords = db.quizRecordDao().getAllOnce().map {
                QuizRecordBackup(it.mode, it.curriculum, it.totalQuestions, it.correctAnswers, it.durationSec, it.weakTopicNote, it.timestampEpochMs, it.dayKey)
            },
            speakingSessions = db.speakingSessionDao().getAllOnce().map {
                SpeakingSessionBackup(
                    it.scenarioId, it.scenarioTitle, it.difficulty, it.totalTurns, it.completedTurns,
                    it.overallScore, it.relevanceAvg, it.vocabAvg, it.grammarAvg, it.communicationAvg,
                    it.strengths, it.weaknesses, it.correctionsSerialized, it.recommendations, it.timestampEpochMs, it.dayKey
                )
            },
            examAttempts = db.examAttemptDao().getAllOnce().map {
                ExamAttemptBackup(
                    it.examType, it.mode, it.totalScore, it.maxScore, it.sectionScoresSerialized,
                    it.estimatedLevel, it.analysisSummary, it.wrongCount, it.totalQuestions, it.durationSec,
                    it.timestampEpochMs, it.dayKey
                )
            },
            grammarBookmarks = db.grammarBookmarkDao().getAllOnce().map {
                GrammarBookmarkBackup(
                    grammarId = it.grammarId,
                    isMastered = it.isMastered,
                    bookmarkedAtEpochMs = it.bookmarkedAtEpochMs
                )
            },
            reviewLogs = db.reviewLogDao().getAllLogsOnce().map {
                ReviewLogBackup(
                    vocabId = it.vocabId,
                    rating = it.rating,
                    stageBefore = it.stageBefore,
                    stageAfter = it.stageAfter,
                    intervalDays = it.intervalDays,
                    timestampEpochMs = it.timestampEpochMs,
                    dayKey = it.dayKey
                )
            }
        )
        moshi.adapter(BaeumBackupPayload::class.java).indent("  ").toJson(payload)
    }

    /**
     * Validates backup schema version (v1 or v2) and atomically restores user learning progress
     * without corrupting seed vocabulary.
     */
    suspend fun restoreBackupJson(json: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = parseBackupJson(json) ?: error("Backup file could not be parsed.")
            require(payload.schemaVersion in 1..CURRENT_BACKUP_SCHEMA_VERSION) {
                "Unsupported backup version (${payload.schemaVersion}). Supported versions: 1..$CURRENT_BACKUP_SCHEMA_VERSION."
            }
            ensureSeeded()

            var restoredVocabCount = 0
            db.withTransaction {
                val allVocab = db.vocabDao().getAllVocabOnce()
                val savedById = payload.vocabStates.associateBy { it.id }
                val toUpsert = mutableListOf<VocabEntity>()

                allVocab.forEach { base ->
                    val saved = savedById[base.id]
                    if (saved != null) {
                        val safeStage = runCatching { CardMemoryStage.valueOf(saved.stage).name }
                            .getOrDefault(CardMemoryStage.NEW.name)
                        toUpsert.add(
                            base.copy(
                                isBookmarked = saved.isBookmarked,
                                stability = sanitizeNonNegativeDouble(saved.stability, 3650.0),
                                difficulty = if (saved.difficulty.isFinite()) saved.difficulty.coerceIn(0.0, 10.0) else 0.0,
                                elapsedDays = sanitizeNonNegativeDouble(saved.elapsedDays, 36500.0),
                                scheduledDays = sanitizeNonNegativeDouble(saved.scheduledDays, 3650.0),
                                reps = saved.reps.coerceAtLeast(0),
                                lapses = saved.lapses.coerceAtLeast(0),
                                stage = safeStage,
                                lastReviewEpochMs = saved.lastReviewEpochMs.coerceAtLeast(0L),
                                dueEpochMs = saved.dueEpochMs.coerceAtLeast(0L),
                                quizCorrectCount = saved.quizCorrectCount.coerceAtLeast(0),
                                quizWrongCount = saved.quizWrongCount.coerceAtLeast(0)
                            )
                        )
                        restoredVocabCount++
                    } else if (base.reps > 0 || base.isBookmarked || base.quizCorrectCount > 0 || base.quizWrongCount > 0 || base.stage != CardMemoryStage.NEW.name) {
                        toUpsert.add(
                            base.copy(
                                isBookmarked = false,
                                stability = 0.0,
                                difficulty = 0.0,
                                elapsedDays = 0.0,
                                scheduledDays = 0.0,
                                reps = 0,
                                lapses = 0,
                                stage = CardMemoryStage.NEW.name,
                                lastReviewEpochMs = 0L,
                                dueEpochMs = 0L,
                                quizCorrectCount = 0,
                                quizWrongCount = 0
                            )
                        )
                    }
                }
                toUpsert.chunked(500).forEach { batch ->
                    db.vocabDao().upsertAll(batch)
                }

                db.dailyStudyDao().clearAll()
                if (payload.dailyStats.isNotEmpty()) {
                    db.dailyStudyDao().upsertAll(payload.dailyStats.map {
                        DailyStudyEntity(
                            dayKey = it.dayKey,
                            wordsReviewed = it.wordsReviewed.coerceAtLeast(0),
                            quizzesCompleted = it.quizzesCompleted.coerceAtLeast(0),
                            speakingSessions = it.speakingSessions.coerceAtLeast(0),
                            examsCompleted = it.examsCompleted.coerceAtLeast(0),
                            studySeconds = it.studySeconds.coerceAtLeast(0)
                        )
                    })
                }

                db.quizRecordDao().clearAll()
                if (payload.quizRecords.isNotEmpty()) {
                    db.quizRecordDao().insertAll(payload.quizRecords.map {
                        QuizRecordEntity(
                            mode = it.mode,
                            curriculum = it.curriculum,
                            totalQuestions = it.totalQuestions.coerceAtLeast(0),
                            correctAnswers = it.correctAnswers.coerceAtLeast(0),
                            durationSec = it.durationSec.coerceAtLeast(0),
                            weakTopicNote = it.weakTopicNote,
                            timestampEpochMs = it.timestampEpochMs.coerceAtLeast(0L),
                            dayKey = it.dayKey
                        )
                    })
                }

                db.speakingSessionDao().clearAll()
                if (payload.speakingSessions.isNotEmpty()) {
                    db.speakingSessionDao().insertAll(payload.speakingSessions.map {
                        SpeakingSessionEntity(
                            scenarioId = it.scenarioId,
                            scenarioTitle = it.scenarioTitle,
                            difficulty = it.difficulty,
                            totalTurns = it.totalTurns.coerceAtLeast(0),
                            completedTurns = it.completedTurns.coerceAtLeast(0),
                            overallScore = it.overallScore.coerceIn(0, 100),
                            relevanceAvg = if (it.relevanceAvg.isFinite()) it.relevanceAvg else 0f,
                            vocabAvg = if (it.vocabAvg.isFinite()) it.vocabAvg else 0f,
                            grammarAvg = if (it.grammarAvg.isFinite()) it.grammarAvg else 0f,
                            communicationAvg = if (it.communicationAvg.isFinite()) it.communicationAvg else 0f,
                            strengths = it.strengths,
                            weaknesses = it.weaknesses,
                            correctionsSerialized = it.correctionsSerialized,
                            recommendations = it.recommendations,
                            timestampEpochMs = it.timestampEpochMs.coerceAtLeast(0L),
                            dayKey = it.dayKey
                        )
                    })
                }

                db.examAttemptDao().clearAll()
                if (payload.examAttempts.isNotEmpty()) {
                    db.examAttemptDao().insertAll(payload.examAttempts.map {
                        ExamAttemptEntity(
                            examType = it.examType,
                            mode = it.mode,
                            totalScore = it.totalScore.coerceAtLeast(0),
                            maxScore = it.maxScore.coerceAtLeast(1),
                            sectionScoresSerialized = it.sectionScoresSerialized,
                            estimatedLevel = it.estimatedLevel,
                            analysisSummary = it.analysisSummary,
                            wrongCount = it.wrongCount.coerceAtLeast(0),
                            totalQuestions = it.totalQuestions.coerceAtLeast(0),
                            durationSec = it.durationSec.coerceAtLeast(0),
                            timestampEpochMs = it.timestampEpochMs.coerceAtLeast(0L),
                            dayKey = it.dayKey
                        )
                    })
                }

                db.reviewLogDao().clearAll()
                if (payload.reviewLogs.isNotEmpty()) {
                    payload.reviewLogs.map {
                        ReviewLogEntity(
                            vocabId = it.vocabId,
                            rating = it.rating.coerceIn(1, 4),
                            stageBefore = it.stageBefore,
                            stageAfter = it.stageAfter,
                            intervalDays = sanitizeNonNegativeDouble(it.intervalDays, 3650.0),
                            timestampEpochMs = it.timestampEpochMs.coerceAtLeast(0L),
                            dayKey = it.dayKey
                        )
                    }.chunked(500).forEach { batch ->
                        db.reviewLogDao().insertAll(batch)
                    }
                }

                if (payload.schemaVersion >= 2 || payload.grammarBookmarks.isNotEmpty()) {
                    db.grammarBookmarkDao().clearAll()
                    if (payload.grammarBookmarks.isNotEmpty()) {
                        db.grammarBookmarkDao().upsertAll(payload.grammarBookmarks.map {
                            GrammarBookmarkEntity(
                                grammarId = it.grammarId,
                                isMastered = it.isMastered,
                                bookmarkedAtEpochMs = it.bookmarkedAtEpochMs.coerceAtLeast(0L)
                            )
                        })
                    }
                }
            }

            val activeDays = payload.dailyStats
                .filter { it.wordsReviewed > 0 || it.quizzesCompleted > 0 || it.speakingSessions > 0 || it.examsCompleted > 0 || it.studySeconds > 0 }
                .map { it.dayKey }
                .toSet()
            userPreferences.syncStreakFromDailyHistory(activeDays)

            "Restored $restoredVocabCount vocabulary states, ${payload.grammarBookmarks.size} grammar bookmarks, ${payload.reviewLogs.size} review logs, ${payload.dailyStats.size} study days, and ${payload.examAttempts.size} exam records."
        }
    }

    private fun sanitizeNonNegativeDouble(value: Double, maxVal: Double): Double =
        if (value.isFinite()) value.coerceIn(0.0, maxVal) else 0.0

    companion object {
        const val CURRENT_BACKUP_SCHEMA_VERSION: Int = 2

        private val backupMoshi: Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        fun serializeBackupPayload(payload: BaeumBackupPayload): String =
            backupMoshi.adapter(BaeumBackupPayload::class.java).indent("  ").toJson(payload)

        fun parseBackupJson(json: String): BaeumBackupPayload? =
            backupMoshi.adapter(BaeumBackupPayload::class.java).fromJson(json)

        fun localDayKey(epochMs: Long = System.currentTimeMillis()): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return fmt.format(Date(epochMs))
        }

        fun buildSevenDayForecast(vocab: List<VocabEntity>, nowEpochMs: Long = System.currentTimeMillis()): List<DayForecastPoint> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowEpochMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayFmt = SimpleDateFormat("EEE", Locale.US)

            val scheduledCards = vocab.filter { it.reps > 0 && it.dueEpochMs > 0L }
            return (0 until 7).map { offset ->
                val dayStart = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val dayEnd = cal.timeInMillis
                val count = if (offset == 0) {
                    // Today includes due today + overdue (counted once, never double-counted)
                    scheduledCards.count { it.dueEpochMs < dayEnd }
                } else {
                    scheduledCards.count { it.dueEpochMs in dayStart until dayEnd }
                }
                val label = if (offset == 0) "Today" else dayFmt.format(Date(dayStart))
                DayForecastPoint(
                    dayOffset = offset,
                    shortLabel = label,
                    dateKey = localDayKey(dayStart),
                    dueCount = count
                )
            }
        }

        fun computeStudyStreak(stats: List<DailyStudyEntity>, nowEpochMs: Long = System.currentTimeMillis()): Int {
            val activeDays = stats
                .filter { it.wordsReviewed > 0 || it.quizzesCompleted > 0 || it.speakingSessions > 0 || it.examsCompleted > 0 }
                .map { it.dayKey }
                .toSet()
            if (activeDays.isEmpty()) return 0

            val cal = Calendar.getInstance().apply { timeInMillis = nowEpochMs }
            val todayKey = localDayKey(cal.timeInMillis)
            if (todayKey !in activeDays) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                if (localDayKey(cal.timeInMillis) !in activeDays) return 0
            }

            var streak = 0
            while (localDayKey(cal.timeInMillis) in activeDays) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            return streak
        }

        fun countOverdue(vocab: List<VocabEntity>, nowEpochMs: Long = System.currentTimeMillis()): Int {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowEpochMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = cal.timeInMillis
            return vocab.count { it.reps > 0 && it.dueEpochMs in 1 until startOfToday }
        }

        fun countDueTodayOnly(vocab: List<VocabEntity>, nowEpochMs: Long = System.currentTimeMillis()): Int {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowEpochMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val endOfToday = cal.timeInMillis
            return vocab.count { it.reps > 0 && it.dueEpochMs in 1 until endOfToday }
        }

        fun countStage(vocab: List<VocabEntity>, stage: CardMemoryStage): Int =
            vocab.count { it.stage == stage.name }
    }
}
