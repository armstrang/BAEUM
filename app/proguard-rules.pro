# BAEUM (배움) — Production R8 / ProGuard Rules for Play Store Release Builds

# Preserve Kotlin metadata & Moshi reflection / generated adapters for JSON Backup & Restore
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, *Annotation*
-keep class kotlin.Metadata { *; }

-keep class com.example.data.repository.BaeumBackupPayload { *; }
-keep class com.example.data.repository.EngagementBackup { *; }
-keep class com.example.data.repository.VocabProgressBackup { *; }
-keep class com.example.data.repository.DailyStudyBackup { *; }
-keep class com.example.data.repository.QuizRecordBackup { *; }
-keep class com.example.data.repository.SpeakingSessionBackup { *; }
-keep class com.example.data.repository.ExamAttemptBackup { *; }
-keep class com.example.data.repository.GrammarBookmarkBackup { *; }
-keep class com.example.data.repository.ReviewLogBackup { *; }
-keep class com.example.data.repository.*JsonAdapter { *; }

# Keep Room entities & generated database implementations
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep DataStore & Kotlinx Coroutines internals cleanly
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn kotlinx.coroutines.debug.**

# Preserve clean stack traces for beta diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
