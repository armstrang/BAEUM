package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VocabEntity::class,
        ReviewLogEntity::class,
        QuizRecordEntity::class,
        SpeakingSessionEntity::class,
        ExamAttemptEntity::class,
        DailyStudyEntity::class,
        GrammarBookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BaeumDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao
    abstract fun reviewLogDao(): ReviewLogDao
    abstract fun quizRecordDao(): QuizRecordDao
    abstract fun speakingSessionDao(): SpeakingSessionDao
    abstract fun examAttemptDao(): ExamAttemptDao
    abstract fun dailyStudyDao(): DailyStudyDao
    abstract fun grammarBookmarkDao(): GrammarBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: BaeumDatabase? = null

        fun getInstance(context: Context): BaeumDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BaeumDatabase::class.java,
                    "baeum_korean_learning.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
