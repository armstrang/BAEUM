package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocabulary ORDER BY curriculum, subCategory, id")
    fun observeAllVocab(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocabulary ORDER BY curriculum, subCategory, id")
    suspend fun getAllVocabOnce(): List<VocabEntity>

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun countAll(): Int

    @Query("SELECT * FROM vocabulary WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<VocabEntity?>

    @Query("SELECT * FROM vocabulary WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VocabEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(items: List<VocabEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<VocabEntity>)

    @Update
    suspend fun updateVocab(item: VocabEntity)

    @Query("UPDATE vocabulary SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: String, bookmarked: Boolean)

    @Query(
        "UPDATE vocabulary SET quizCorrectCount = quizCorrectCount + :correctDelta, " +
            "quizWrongCount = quizWrongCount + :wrongDelta WHERE id = :id"
    )
    suspend fun recordQuizOutcome(id: String, correctDelta: Int, wrongDelta: Int)
}

@Dao
interface ReviewLogDao {
    @Query("SELECT * FROM review_logs ORDER BY timestampEpochMs DESC")
    fun observeAllLogs(): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs ORDER BY timestampEpochMs DESC")
    suspend fun getAllLogsOnce(): List<ReviewLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReviewLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ReviewLogEntity>)

    @Query("DELETE FROM review_logs")
    suspend fun clearAll()
}

@Dao
interface QuizRecordDao {
    @Query("SELECT * FROM quiz_records ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<QuizRecordEntity>>

    @Query("SELECT * FROM quiz_records ORDER BY timestampEpochMs DESC")
    suspend fun getAllOnce(): List<QuizRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: QuizRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<QuizRecordEntity>)

    @Query("DELETE FROM quiz_records")
    suspend fun clearAll()
}

@Dao
interface SpeakingSessionDao {
    @Query("SELECT * FROM speaking_sessions ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<SpeakingSessionEntity>>

    @Query("SELECT * FROM speaking_sessions ORDER BY timestampEpochMs DESC")
    suspend fun getAllOnce(): List<SpeakingSessionEntity>

    @Query("SELECT * FROM speaking_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SpeakingSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SpeakingSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SpeakingSessionEntity>)

    @Query("DELETE FROM speaking_sessions")
    suspend fun clearAll()
}

@Dao
interface ExamAttemptDao {
    @Query("SELECT * FROM exam_attempts ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts ORDER BY timestampEpochMs DESC")
    suspend fun getAllOnce(): List<ExamAttemptEntity>

    @Query("SELECT * FROM exam_attempts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExamAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: ExamAttemptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attempts: List<ExamAttemptEntity>)

    @Query("DELETE FROM exam_attempts")
    suspend fun clearAll()
}

@Dao
interface DailyStudyDao {
    @Query("SELECT * FROM daily_study_stats ORDER BY dayKey ASC")
    fun observeAll(): Flow<List<DailyStudyEntity>>

    @Query("SELECT * FROM daily_study_stats ORDER BY dayKey ASC")
    suspend fun getAllOnce(): List<DailyStudyEntity>

    @Query("SELECT * FROM daily_study_stats WHERE dayKey = :dayKey LIMIT 1")
    suspend fun getByDay(dayKey: String): DailyStudyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: DailyStudyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stats: List<DailyStudyEntity>)

    @Query("DELETE FROM daily_study_stats")
    suspend fun clearAll()
}

@Dao
interface GrammarBookmarkDao {
    @Query("SELECT * FROM grammar_bookmarks")
    fun observeAll(): Flow<List<GrammarBookmarkEntity>>

    @Query("SELECT * FROM grammar_bookmarks")
    suspend fun getAllOnce(): List<GrammarBookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: GrammarBookmarkEntity)

    @Query("DELETE FROM grammar_bookmarks WHERE grammarId = :grammarId")
    suspend fun delete(grammarId: String)

    @Query("DELETE FROM grammar_bookmarks")
    suspend fun clearAll()
}
