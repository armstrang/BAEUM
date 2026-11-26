package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsCardSnapshot

object CurriculumPathway {
    const val HANGUL = "HANGUL"
    const val TOPIK = "TOPIK"
    const val EPS_TOPIK = "EPS_TOPIK"
    const val EVERYDAY = "EVERYDAY"
}

@Entity(
    tableName = "vocabulary",
    indices = [
        Index(value = ["curriculum", "subCategory"]),
        Index(value = ["stage"]),
        Index(value = ["dueEpochMs"]),
        Index(value = ["isBookmarked"])
    ]
)
data class VocabEntity(
    @PrimaryKey val id: String,
    val hangul: String,
    val romanization: String,
    val meaningEn: String,
    val meaningId: String,
    val partOfSpeech: String,
    val curriculum: String,
    val subCategory: String, // e.g., "1급", "2급", "Manufacturing", "Workplace safety", "Cafe & Dining"
    val exampleKo: String,
    val exampleEn: String,
    val exampleId: String,
    val pronunciationNote: String = "",
    val grammarNote: String = "",
    val conjugationsSerialized: String = "", // "먹어요::Polite present||먹습니다::Formal present"
    val formalityNote: String = "",
    val honorificForm: String = "",
    val hanja: String = "",
    val relatedWords: String = "",
    val isBookmarked: Boolean = false,
    // FSRS-4.5 State
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val elapsedDays: Double = 0.0,
    val scheduledDays: Double = 0.0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val stage: String = CardMemoryStage.NEW.name,
    val lastReviewEpochMs: Long = 0L,
    val dueEpochMs: Long = 0L,
    // Adaptive Quiz Telemetry
    val quizCorrectCount: Int = 0,
    val quizWrongCount: Int = 0
) {
    fun toFsrsSnapshot(): FsrsCardSnapshot = FsrsCardSnapshot(
        stability = stability,
        difficulty = difficulty,
        elapsedDays = elapsedDays,
        scheduledDays = scheduledDays,
        reps = reps,
        lapses = lapses,
        stage = runCatching { CardMemoryStage.valueOf(stage) }.getOrDefault(CardMemoryStage.NEW),
        lastReviewEpochMs = lastReviewEpochMs,
        dueEpochMs = dueEpochMs
    )

    fun parsedConjugations(): List<Pair<String, String>> {
        if (conjugationsSerialized.isBlank()) return emptyList()
        return conjugationsSerialized.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }
    }

    fun meaningFor(language: String): String =
        if (language == "ID" && meaningId.isNotBlank()) meaningId else meaningEn

    fun exampleFor(language: String): String =
        if (language == "ID" && exampleId.isNotBlank()) exampleId else exampleEn
}

@Entity(tableName = "review_logs", indices = [Index(value = ["dayKey"]), Index(value = ["vocabId"])])
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vocabId: String,
    val rating: Int,
    val stageBefore: String,
    val stageAfter: String,
    val intervalDays: Double,
    val timestampEpochMs: Long,
    val dayKey: String
)

@Entity(tableName = "quiz_records", indices = [Index(value = ["dayKey"])])
data class QuizRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val curriculum: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val durationSec: Int,
    val weakTopicNote: String,
    val timestampEpochMs: Long,
    val dayKey: String
)

@Entity(tableName = "speaking_sessions", indices = [Index(value = ["timestampEpochMs"])])
data class SpeakingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val scenarioTitle: String,
    val difficulty: String,
    val totalTurns: Int,
    val completedTurns: Int,
    val overallScore: Int, // 0..100
    val relevanceAvg: Float,
    val vocabAvg: Float,
    val grammarAvg: Float,
    val communicationAvg: Float,
    val strengths: String,
    val weaknesses: String,
    val correctionsSerialized: String, // "userText=>suggestedText=>note||..."
    val recommendations: String,
    val timestampEpochMs: Long,
    val dayKey: String
)

@Entity(tableName = "exam_attempts", indices = [Index(value = ["timestampEpochMs"])])
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examType: String, // TOPIK_I, TOPIK_II, EPS_TOPIK, TOPIK_SPEAKING
    val mode: String,     // PRACTICE, MOCK
    val totalScore: Int,
    val maxScore: Int,
    val sectionScoresSerialized: String, // "Listening::80::100||Reading::74::100"
    val estimatedLevel: String,
    val analysisSummary: String,
    val wrongCount: Int,
    val totalQuestions: Int,
    val durationSec: Int,
    val timestampEpochMs: Long,
    val dayKey: String
)

@Entity(tableName = "daily_study_stats")
data class DailyStudyEntity(
    @PrimaryKey val dayKey: String, // YYYY-MM-DD local timezone
    val wordsReviewed: Int = 0,
    val quizzesCompleted: Int = 0,
    val speakingSessions: Int = 0,
    val examsCompleted: Int = 0,
    val studySeconds: Int = 0
)

@Entity(tableName = "grammar_bookmarks")
data class GrammarBookmarkEntity(
    @PrimaryKey val grammarId: String,
    val isMastered: Boolean = false,
    val bookmarkedAtEpochMs: Long = System.currentTimeMillis()
)
