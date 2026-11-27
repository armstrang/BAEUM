package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.audio.BaeumSoundEffects
import com.example.data.audio.KoreanAudioController
import com.example.data.audio.SpeechCaptureState
import com.example.data.audio.SpeechRecognitionState
import com.example.data.audio.TtsStatus
import com.example.data.local.CurriculumPathway
import com.example.data.local.DailyStudyEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.GrammarBookmarkEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.RomanizationMode
import com.example.data.local.SpeakingSessionEntity
import com.example.data.local.UserSettings
import com.example.data.local.VocabEntity
import com.example.data.repository.BaeumRepository
import com.example.data.repository.CurriculumPreferences
import com.example.data.repository.DataStoreUserPreferencesRepository
import com.example.data.repository.DayForecastPoint
import com.example.data.repository.LearningStreakState
import com.example.data.repository.ThemePreferences
import com.example.data.repository.UserPreferencesRepository
import com.example.data.seed.ExamPaperLength
import com.example.data.seed.ExamType
import com.example.data.seed.SpeakingDifficulty
import com.example.data.seed.SpeakingScenario
import com.example.domain.engagement.BaeumHabitEngine
import com.example.domain.engagement.CelebrationBannerEvent
import com.example.domain.engagement.CelebrationKind
import com.example.domain.engagement.HabitDashboardSnapshot
import com.example.domain.engagement.SeoulBlitzEngine
import com.example.domain.engagement.SeoulBlitzState
import com.example.domain.exam.ActiveExamSession
import com.example.domain.exam.ExamEngine
import com.example.domain.exam.ExamReport
import com.example.domain.exam.ExamSessionMode
import com.example.domain.fsrs.CardMemoryStage
import com.example.domain.fsrs.FsrsRating
import com.example.domain.quiz.ModeProgressStats
import com.example.domain.quiz.QuizManager
import com.example.domain.quiz.QuizMode
import com.example.domain.quiz.QuizSessionState
import com.example.domain.scoring.LearnerReadinessProfile
import com.example.domain.scoring.LearningScoringEngine
import com.example.data.seed.localizedTitle
import com.example.domain.speaking.ConversationEngine
import com.example.domain.speaking.ConversationPhase
import com.example.domain.speaking.FinalSpeakingAssessment
import com.example.domain.speaking.KoreanPronunciationEvaluator
import com.example.domain.speaking.LiveConversationState
import com.example.domain.speaking.PronunciationAttemptResult
import com.example.domain.speaking.PronunciationDrillCategory
import com.example.domain.speaking.PronunciationDrillItem
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardSummary(
    val todayWords: Int = 0,
    val dailyGoal: Int = 20,
    val streakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val dueTodayCount: Int = 0,
    val overdueCount: Int = 0,
    val sevenDayForecast: List<DayForecastPoint> = emptyList(),
    val masteredCount: Int = 0,
    val totalVocabCount: Int = 0,
    val totalStudySeconds: Int = 0,
    val quizAccuracyPercent: Int? = null,
    val activePathwayLabel: String = "TOPIK I",
    val activePathwayLearned: Int = 0,
    val activePathwayTotal: Int = 0,
    val recommendedFocus: String = "Start Hangul or TOPIK I flashcards"
)

data class PronunciationPracticeState(
    val drills: List<PronunciationDrillItem> = KoreanPronunciationEvaluator.defaultDrills,
    val selectedCategory: PronunciationDrillCategory? = null,
    val activeDrill: PronunciationDrillItem = KoreanPronunciationEvaluator.defaultDrills.first(),
    val recognitionState: SpeechRecognitionState = SpeechRecognitionState.Idle,
    val lastAttempt: PronunciationAttemptResult? = null,
    val passedDrillIds: Set<String> = emptySet(),
    val totalAttempts: Int = 0,
    val isSpeechServiceAvailable: Boolean = true,
    val isOnDeviceAvailable: Boolean = false
)

class BaeumViewModel(
    val repository: BaeumRepository,
    val audioController: KoreanAudioController,
    val userPreferencesRepository: UserPreferencesRepository = repository.userPreferences,
    private val reminderScheduler: (Boolean, Int, Int, Boolean) -> Unit = { _, _, _, _ -> },
    private val testReminderPoster: (Int, Int, Int, Int, Boolean) -> Boolean = { _, _, _, _, _ -> false }
) : ViewModel() {

    val soundEffects: BaeumSoundEffects = BaeumSoundEffects()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _initError = MutableStateFlow<String?>(null)
    val initError: StateFlow<String?> = _initError.asStateFlow()

    val settings: StateFlow<UserSettings> = userPreferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val themePreferences: StateFlow<ThemePreferences> = userPreferencesRepository.themePreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreferences())

    val curriculumPreferences: StateFlow<CurriculumPreferences> = userPreferencesRepository.curriculumPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, CurriculumPreferences())

    val learningStreak: StateFlow<LearningStreakState> = userPreferencesRepository.learningStreakFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LearningStreakState())

    val allVocab: StateFlow<List<VocabEntity>> = repository.allVocabFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dailyStats: StateFlow<List<DailyStudyEntity>> = repository.dailyStatsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val quizRecords: StateFlow<List<QuizRecordEntity>> = repository.quizRecordsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val speakingSessions: StateFlow<List<SpeakingSessionEntity>> = repository.speakingSessionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val examAttempts: StateFlow<List<ExamAttemptEntity>> = repository.examAttemptsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val grammarBookmarks: StateFlow<List<GrammarBookmarkEntity>> = repository.grammarBookmarksFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val ttsStatus: StateFlow<TtsStatus> = audioController.ttsStatus
    val speechCaptureState: StateFlow<SpeechCaptureState> = audioController.captureState
    val isPlayingRecording: StateFlow<Boolean> = audioController.isPlayingRecording

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        settings, allVocab, dailyStats, quizRecords
    ) { userSettings, vocab, stats, quizzes ->
        val nowMs = System.currentTimeMillis()
        val todayKey = BaeumRepository.localDayKey(nowMs)
        val todayStat = stats.firstOrNull { it.dayKey == todayKey }
        val roomStreak = BaeumRepository.computeStudyStreak(stats, nowMs)
        val persistedStreak = userSettings.learningStreak.effectiveCurrentStreak(todayKey)
        val streak = maxOf(persistedStreak, roomStreak)
        val longestStreak = maxOf(userSettings.learningStreak.longestStreakDays, streak)

        // Single O(N) pass across 6,600+ vocabulary records instead of 8 separate scans
        val agg = BaeumRepository.computeVocabAggregate(
            vocab = vocab,
            activePathway = userSettings.activePathway,
            nowEpochMs = nowMs
        )
        val totalSecs = stats.sumOf { it.studySeconds }
        val totalQ = quizzes.sumOf { it.totalQuestions }
        val totalCorrect = quizzes.sumOf { it.correctAnswers }
        val quizAcc = if (totalQ > 0) ((totalCorrect.toFloat() / totalQ) * 100f).toInt() else null

        val lang = userSettings.translationLanguage
        val isId = lang.equals("ID", ignoreCase = true)
        val pathwayLabel = when (userSettings.activePathway) {
            CurriculumPathway.HANGUL -> "Hangul"
            CurriculumPathway.TOPIK -> "TOPIK I"
            CurriculumPathway.EPS_TOPIK -> "EPS-TOPIK"
            CurriculumPathway.EVERYDAY -> if (isId) "Sehari-hari" else "Everyday Korean"
            else -> "TOPIK I"
        }

        val recommendation = when {
            agg.dueTodayCount > 0 -> if (isId) "Ulas ${agg.dueTodayCount} kartu FSRS yang terjadwal hari ini" else "Review ${agg.dueTodayCount} scheduled FSRS card(s)"
            agg.weakQuizCount >= 2 -> if (isId) "Latih ${agg.weakQuizCount} kosakata lemah di Kuis Pintar" else "Practice ${agg.weakQuizCount} weak vocabulary item(s) in Smart Quiz"
            agg.reviewedCount == 0 -> if (isId) "Mulai sesi Kartu Pintar pertamamu" else "Start your first Smart Flashcard session"
            quizzes.isEmpty() -> if (isId) "Uji daya ingatmu di Pusat Kuis (Batchim & Kosakata)" else "Test your recall in Quiz Center (Batchim & Recall)"
            else -> if (isId) "Latih percakapan bahasa Korea langsung di tab Bicara" else "Practice a live Korean conversation in Speak"
        }

        DashboardSummary(
            todayWords = todayStat?.wordsReviewed ?: 0,
            dailyGoal = userSettings.dailyGoalWords,
            streakDays = streak,
            longestStreakDays = longestStreak,
            dueTodayCount = agg.dueTodayCount,
            overdueCount = agg.overdueCount,
            sevenDayForecast = agg.sevenDayForecast,
            masteredCount = agg.masteredCount,
            totalVocabCount = agg.totalCount,
            totalStudySeconds = totalSecs,
            quizAccuracyPercent = quizAcc,
            activePathwayLabel = pathwayLabel,
            activePathwayLearned = agg.activePathwayLearned,
            activePathwayTotal = agg.activePathwayTotal,
            recommendedFocus = recommendation
        )
    }
        .conflate()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, DashboardSummary())

    val readinessProfile: StateFlow<LearnerReadinessProfile> = combine(
        allVocab, quizRecords, examAttempts, speakingSessions
    ) { vocab, quizzes, exams, speaking ->
        LearningScoringEngine.computeReadinessProfile(
            vocab = vocab,
            quizRecords = quizzes,
            examAttempts = exams,
            speakingSessions = speaking
        )
    }
        .conflate()
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            LearningScoringEngine.computeReadinessProfile(emptyList(), emptyList(), emptyList(), emptyList())
        )

    val habitSnapshot: StateFlow<HabitDashboardSnapshot> = combine(
        settings,
        allVocab,
        dailyStats,
        quizRecords
    ) { userSettings, vocab, stats, quizzes ->
        val nowMs = System.currentTimeMillis()
        val todayKey = BaeumRepository.localDayKey(nowMs)
        val yesterdayKey = DataStoreUserPreferencesRepository.previousDayKey(todayKey)
        val eng = userSettings.engagement
        val todayStat = stats.firstOrNull { it.dayKey == todayKey }
        val isSparkDone = eng.dailySparkCompletedDayKey == todayKey
        val blitzToday = quizzes.any { it.dayKey == todayKey && it.mode == "Seoul Blitz" }
        val quests = BaeumHabitEngine.buildDailyQuests(
            todayStat = todayStat,
            dailyGoalWords = userSettings.dailyGoalWords,
            isSparkDoneToday = isSparkDone,
            todayKey = todayKey,
            blitzGamesPlayedToday = blitzToday
        )
        val completedQuests = quests.count { it.isCompleted }
        val sealClaimed = eng.dailySealClaimedDayKey == todayKey

        HabitDashboardSnapshot(
            rankProgress = BaeumHabitEngine.computeRankProgress(eng.totalXp),
            streakShields = eng.streakShields,
            shieldSavedStreakRecently = eng.lastShieldUsedDayKey.isNotBlank() && eng.lastShieldUsedDayKey == yesterdayKey,
            weeklyRhythm = BaeumHabitEngine.buildWeeklyRhythm(
                dailyStats = stats,
                todayEpochMs = nowMs,
                shieldedDayKey = eng.lastShieldUsedDayKey
            ),
            dailySpark = BaeumHabitEngine.buildDailySeoulSpark(
                allVocab = vocab,
                activePathway = userSettings.activePathway,
                translationLang = userSettings.translationLanguage,
                dayKey = todayKey
            ),
            isDailySparkCompleted = isSparkDone,
            dailyQuests = quests,
            completedQuestsCount = completedQuests,
            isDailySealClaimed = sealClaimed,
            canClaimDailySeal = completedQuests >= quests.size && !sealClaimed,
            blitzBestScore = eng.blitzBestScore,
            blitzGamesPlayed = eng.blitzGamesPlayed,
            bestComboEver = eng.bestComboEver,
            unlockedStampIds = eng.unlockedStampIds,
            soundEffectsEnabled = eng.soundEffectsEnabled,
            completedBeginnerLessonIds = eng.completedBeginnerLessonIds,
            perfectSyllabusLessonIds = eng.perfectSyllabusLessonIds,
            defeatedStageBossNumbers = eng.defeatedStageBossNumbers,
            mistakeVaultKeys = eng.mistakeVaultKeys
        )
    }
        .conflate()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, HabitDashboardSnapshot())

    // Active Session States
    val quizManager: QuizManager = QuizManager()
    val activeQuiz: StateFlow<QuizSessionState?> = quizManager.sessionState
    val quizModeProgress: StateFlow<Map<QuizMode, ModeProgressStats>> = quizManager.modeProgress

    private val _blitzSession = MutableStateFlow<SeoulBlitzState?>(null)
    val blitzSession: StateFlow<SeoulBlitzState?> = _blitzSession.asStateFlow()
    private var blitzTimerJob: Job? = null

    private val _celebrationEvent = MutableStateFlow<CelebrationBannerEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationBannerEvent?> = _celebrationEvent.asStateFlow()
    private var celebrationDismissJob: Job? = null
    private var observedRankLevel: Int? = null
    private var stampObserverHydrated: Boolean = false

    private val _liveConversation = MutableStateFlow<LiveConversationState?>(null)
    val liveConversation: StateFlow<LiveConversationState?> = _liveConversation.asStateFlow()

    private val _lastSpeakingAssessment = MutableStateFlow<FinalSpeakingAssessment?>(null)
    val lastSpeakingAssessment: StateFlow<FinalSpeakingAssessment?> = _lastSpeakingAssessment.asStateFlow()

    private val _pronunciationPractice = MutableStateFlow(
        PronunciationPracticeState(
            isSpeechServiceAvailable = audioController.isSpeechRecognitionAvailable(),
            isOnDeviceAvailable = audioController.isOnDeviceSpeechRecognitionAvailable()
        )
    )
    val pronunciationPractice: StateFlow<PronunciationPracticeState> = _pronunciationPractice.asStateFlow()

    private val _activeExam = MutableStateFlow<ActiveExamSession?>(null)
    val activeExam: StateFlow<ActiveExamSession?> = _activeExam.asStateFlow()

    private val _lastExamReport = MutableStateFlow<ExamReport?>(null)
    val lastExamReport: StateFlow<ExamReport?> = _lastExamReport.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        initializeApp()
        viewModelScope.launch {
            userPreferencesRepository.evaluateStreakContinuity()
        }
        viewModelScope.launch {
            dailyStats.collect { stats ->
                if (stats.isNotEmpty() && learningStreak.value.lastStudyDayKey.isBlank()) {
                    val activeDays = stats
                        .filter { it.wordsReviewed > 0 || it.quizzesCompleted > 0 || it.speakingSessions > 0 || it.examsCompleted > 0 }
                        .map { it.dayKey }
                        .toSet()
                    if (activeDays.isNotEmpty()) {
                        userPreferencesRepository.syncStreakFromDailyHistory(activeDays)
                    }
                }
            }
        }
        viewModelScope.launch {
            settings.collect { s ->
                audioController.updateSpeechRate(s.speechRate)
                soundEffects.enabled = s.engagement.soundEffectsEnabled
                val currentRank = BaeumHabitEngine.computeRankProgress(s.engagement.totalXp).currentRank
                val prevLevel = observedRankLevel
                if (prevLevel != null && currentRank.level > prevLevel) {
                    soundEffects.playLevelUp()
                    emitCelebration(
                        CelebrationBannerEvent(
                            kind = CelebrationKind.LEVEL_UP,
                            titleEn = "Rank Up · Lv.${currentRank.level} ${currentRank.titleEn}!",
                            titleId = "Naik Peringkat · Lv.${currentRank.level} ${currentRank.titleId}!",
                            subtitleEn = "${currentRank.titleKo} — Your Seoul Atelier mastery grows.",
                            subtitleId = "${currentRank.titleKo} — Penguasaan Seoul Atelier-mu meningkat!",
                            badgeKo = currentRank.badgeGlyph,
                            showConfetti = true
                        )
                    )
                }
                observedRankLevel = currentRank.level
            }
        }
        // Automatically detect and unlock earned Dojang Seal Stamps from real learner records
        viewModelScope.launch {
            combine(
                settings,
                dashboardSummary,
                allVocab,
                quizRecords,
                combine(speakingSessions, examAttempts) { s, e -> s to e }
            ) { s, dash, vocab, quizzes, (speaking, exams) ->
                val reviewedCount = vocab.count { it.reps > 0 }
                BaeumHabitEngine.evaluateEligibleStampIds(
                    profile = s.engagement,
                    currentStreak = dash.streakDays,
                    longestStreak = dash.longestStreakDays,
                    reviewedVocabCount = reviewedCount,
                    quizRecords = quizzes,
                    speakingSessions = speaking,
                    examAttempts = exams
                )
            }
                .conflate()
                .flowOn(Dispatchers.Default)
                .collect { eligible ->
                    val newlyUnlocked = userPreferencesRepository.unlockStamps(eligible)
                    if (stampObserverHydrated && newlyUnlocked.isNotEmpty()) {
                        val stamp = BaeumHabitEngine.allStamps.firstOrNull { it.id in newlyUnlocked }
                        if (stamp != null) {
                            soundEffects.playStampUnlock()
                            emitCelebration(
                                CelebrationBannerEvent(
                                    kind = CelebrationKind.STAMP_UNLOCK,
                                    titleEn = "Seal Unlocked · ${stamp.titleEn}",
                                    titleId = "Stempel Terbuka · ${stamp.titleId}",
                                    subtitleEn = stamp.conditionEn,
                                    subtitleId = stamp.conditionId,
                                    badgeKo = stamp.sealKo,
                                    showConfetti = true
                                )
                            )
                        }
                    }
                    stampObserverHydrated = true
                }
        }
        viewModelScope.launch {
            quizRecords.collect { records ->
                quizManager.hydrateHistoricalProgress(
                    records.map { Triple(it.mode, it.totalQuestions, it.correctAnswers) }
                )
            }
        }
        viewModelScope.launch {
            audioController.captureState.collect { cap ->
                if (_liveConversation.value == null) {
                    val activeExamSession = _activeExam.value
                    val activeExamQ = activeExamSession?.currentQuestion
                    if (activeExamSession != null && !activeExamSession.isSubmitted &&
                        activeExamQ != null && activeExamQ.section == "Speaking" &&
                        cap is SpeechCaptureState.Captured && cap.transcript.isNotBlank()
                    ) {
                        val existing = activeExamSession.openResponses[activeExamQ.id].orEmpty().trim()
                        val merged = if (existing.isBlank()) cap.transcript.trim() else "$existing ${cap.transcript.trim()}"
                        updateExamOpenResponse(activeExamQ.id, merged)
                        audioController.resetCapture()
                    }
                    return@collect
                }
                val isId = settings.value.translationLanguage.equals("ID", ignoreCase = true)
                when (cap) {
                    is SpeechCaptureState.Listening -> {
                        _liveConversation.update {
                            it?.copy(
                                phase = ConversationPhase.RECORDING,
                                livePartialTranscript = cap.partialTranscript,
                                lastRecordingSeconds = cap.elapsedSeconds,
                                lastWaveform = cap.amplitudes,
                                recognitionErrorMessage = null
                            )
                        }
                    }
                    is SpeechCaptureState.Captured -> {
                        _liveConversation.update {
                            it?.copy(
                                phase = ConversationPhase.PREVIEW_TRANSCRIPT,
                                draftTranscript = cap.transcript,
                                livePartialTranscript = "",
                                draftEditedManually = false,
                                lastRecordingPath = cap.audioFilePath,
                                lastRecordingSeconds = cap.durationSeconds,
                                lastWaveform = cap.amplitudes,
                                recognitionErrorMessage = null
                            )
                        }
                    }
                    is SpeechCaptureState.TechnicalFailure -> {
                        _liveConversation.update {
                            it?.copy(
                                phase = ConversationPhase.PREVIEW_TRANSCRIPT,
                                livePartialTranscript = "",
                                lastRecordingPath = cap.audioFilePath,
                                lastRecordingSeconds = cap.durationSeconds,
                                lastWaveform = cap.amplitudes,
                                recognitionErrorMessage = if (isId) cap.messageId else cap.message
                            )
                        }
                    }
                    SpeechCaptureState.Idle -> Unit
                }
            }
        }
        viewModelScope.launch {
            audioController.pronunciationRecognitionState.collect { recState ->
                when (recState) {
                    is SpeechRecognitionState.Result -> {
                        val currentDrill = _pronunciationPractice.value.activeDrill
                        val evaluation = KoreanPronunciationEvaluator.evaluate(
                            drill = currentDrill,
                            alternatives = recState.alternatives,
                            confidenceScores = recState.confidenceScores
                        )
                        val updatedPassed = if (evaluation.passed) {
                            _pronunciationPractice.value.passedDrillIds + currentDrill.id
                        } else {
                            _pronunciationPractice.value.passedDrillIds
                        }
                        _pronunciationPractice.update {
                            it.copy(
                                recognitionState = recState,
                                lastAttempt = evaluation,
                                passedDrillIds = updatedPassed,
                                totalAttempts = it.totalAttempts + 1
                            )
                        }
                        if (evaluation.passed) {
                            soundEffects.playCorrect(3)
                            repository.recordStudySeconds(15)
                            userPreferencesRepository.awardXp(BaeumHabitEngine.PRONUNCIATION_PASS_XP)
                        }
                    }
                    else -> {
                        _pronunciationPractice.update {
                            it.copy(
                                recognitionState = recState,
                                isSpeechServiceAvailable = audioController.isSpeechRecognitionAvailable(),
                                isOnDeviceAvailable = audioController.isOnDeviceSpeechRecognitionAvailable()
                            )
                        }
                    }
                }
            }
        }
    }

    fun initializeApp() {
        viewModelScope.launch {
            _initError.value = null
            runCatching {
                repository.ensureSeeded()
            }.onSuccess {
                _isInitialized.value = true
            }.onFailure { err ->
                _initError.value = err.localizedMessage ?: "Failed to open local Korean learning database."
            }
        }
    }

    fun onHostStop() {
        audioController.onHostStop()
        blitzTimerJob?.cancel()
    }

    fun reportStatusMessage(message: String) {
        _statusMessage.value = message
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun dismissCelebration() {
        celebrationDismissJob?.cancel()
        _celebrationEvent.value = null
    }

    private fun emitCelebration(event: CelebrationBannerEvent) {
        celebrationDismissJob?.cancel()
        _celebrationEvent.value = event
        celebrationDismissJob = viewModelScope.launch {
            delay(4200L)
            if (_celebrationEvent.value?.id == event.id) {
                _celebrationEvent.value = null
            }
        }
    }

    // ==================== ONBOARDING & USER PREFERENCES (DataStore Repository) ====================
    fun completeOnboarding(goal: String, level: String, dailyGoal: Int) {
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding(goal, level, dailyGoal)
        }
    }

    fun replayOnboarding() {
        viewModelScope.launch { userPreferencesRepository.resetOnboarding() }
    }

    fun setActivePathway(pathway: String) {
        viewModelScope.launch { userPreferencesRepository.setCurrentCurriculum(pathway) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { userPreferencesRepository.setThemeMode(mode) }
    }

    fun setDailyGoal(words: Int) {
        viewModelScope.launch { userPreferencesRepository.setDailyGoal(words) }
    }

    fun setRomanizationMode(mode: RomanizationMode) {
        viewModelScope.launch { userPreferencesRepository.setRomanizationMode(mode) }
    }

    fun setTranslationLanguage(lang: String) {
        viewModelScope.launch { userPreferencesRepository.setTranslationLanguage(lang) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { userPreferencesRepository.setFontScale(scale) }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { userPreferencesRepository.setSpeechRate(rate) }
    }

    fun setFsrsRetention(retention: Double) {
        viewModelScope.launch { userPreferencesRepository.setFsrsRetention(retention) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setReducedMotion(enabled) }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        soundEffects.enabled = enabled
        viewModelScope.launch { userPreferencesRepository.setSoundEffectsEnabled(enabled) }
        if (enabled) soundEffects.playCardFlip()
    }

    fun playCardFlipSound() {
        soundEffects.playCardFlip()
    }

    fun playFlashcardRatingSound(rating: FsrsRating, nextCombo: Int) {
        when (rating) {
            FsrsRating.AGAIN -> soundEffects.playGentleMiss()
            FsrsRating.HARD -> soundEffects.playCorrect(1)
            FsrsRating.GOOD -> soundEffects.playCorrect(nextCombo.coerceAtLeast(1))
            FsrsRating.EASY -> soundEffects.playCorrect((nextCombo + 1).coerceAtLeast(2))
        }
    }

    // ==================== DAILY SEOUL SPARK, DAILY SEAL & 60s SEOUL BLITZ ====================
    fun completeDailySpark(isCorrect: Boolean) {
        if (!isCorrect) {
            soundEffects.playGentleMiss()
            return
        }
        val spark = habitSnapshot.value.dailySpark ?: return
        val todayKey = BaeumRepository.localDayKey()
        soundEffects.playStampUnlock()
        viewModelScope.launch {
            val newlyCompleted = userPreferencesRepository.recordDailySparkCompleted(
                dayKey = todayKey,
                xpReward = spark.xpReward
            )
            if (newlyCompleted) {
                repository.recordStudySeconds(20)
                userPreferencesRepository.recordStudyActivity(todayKey)
                emitCelebration(
                    CelebrationBannerEvent(
                        kind = CelebrationKind.DAILY_SPARK,
                        titleEn = "Daily Spark Lit · +${spark.xpReward} XP!",
                        titleId = "Percikan Harian Menyala · +${spark.xpReward} XP!",
                        subtitleEn = "${spark.hangul} (${spark.correctMeaning}) added to your momentum.",
                        subtitleId = "${spark.hangul} (${spark.correctMeaning}) memicu momentum belajarmu.",
                        badgeKo = "불꽃",
                        xpDelta = spark.xpReward,
                        showConfetti = true
                    )
                )
            }
        }
    }

    fun claimDailySeal() {
        val snap = habitSnapshot.value
        if (!snap.canClaimDailySeal) return
        val todayKey = BaeumRepository.localDayKey()
        soundEffects.playLevelUp()
        viewModelScope.launch {
            val claimed = userPreferencesRepository.claimDailySealBonus(
                dayKey = todayKey,
                bonusXp = BaeumHabitEngine.DAILY_SEAL_BONUS_XP
            )
            if (claimed) {
                emitCelebration(
                    CelebrationBannerEvent(
                        kind = CelebrationKind.DAILY_SEAL,
                        titleEn = "Daily Atelier Seal Stamped!",
                        titleId = "Stempel Harian Atelier Tercapai!",
                        subtitleEn = "+${BaeumHabitEngine.DAILY_SEAL_BONUS_XP} Bonus XP & +1 Streak Shield earned.",
                        subtitleId = "+${BaeumHabitEngine.DAILY_SEAL_BONUS_XP} Bonus XP & +1 Perisai Runtutan diperoleh!",
                        badgeKo = "도장",
                        xpDelta = BaeumHabitEngine.DAILY_SEAL_BONUS_XP,
                        showConfetti = true
                    )
                )
            }
        }
    }

    fun playBeginnerCheckSound(isCorrect: Boolean, step: Int = 1) {
        if (isCorrect) {
            soundEffects.playCorrect(step.coerceAtLeast(1))
        } else {
            soundEffects.playGentleMiss()
        }
    }

    fun completeBeginnerLesson(lessonId: String, isPerfect: Boolean = false) {
        val catalog = com.example.domain.curriculum.BeginnerSyllabusCatalog
        val lesson = catalog.lessonById(lessonId) ?: return
        val prevDone = settings.value.engagement.completedBeginnerLessonIds
        val prevPerfect = settings.value.engagement.perfectSyllabusLessonIds
        val track = lesson.track
        val wasTrackGraduated = catalog.isTrackGraduated(prevDone, track)
        val todayKey = BaeumRepository.localDayKey()
        soundEffects.playStampUnlock()

        viewModelScope.launch {
            val newlyCompleted = userPreferencesRepository.recordBeginnerLessonCompleted(
                lessonId = lesson.id,
                xpReward = lesson.xpReward,
                isPerfect = isPerfect
            )
            val newlyPerfected = isPerfect && lesson.id !in prevPerfect
            repository.recordStudySeconds(lesson.estimatedMinutes * 45)
            // Feed newly learned syllabus keywords straight into FSRS daily review queue!
            repository.seedSyllabusWordsIntoFsrs(catalog.coreKeywordsFor(lesson))
            userPreferencesRepository.recordStudyActivity(todayKey)

            val nowDone = prevDone + lesson.id
            val justGraduatedTrack = !wasTrackGraduated && catalog.isTrackGraduated(nowDone, track)
            val perfectBonus = if (newlyPerfected) catalog.PERFECT_STAR_BONUS_XP else 0
            val baseEarned = if (newlyCompleted) lesson.xpReward else 0
            val totalEarned = baseEarned + perfectBonus + if (justGraduatedTrack) track.graduationBonusXp else 0

            if (justGraduatedTrack) {
                soundEffects.playLevelUp()
                val nextTrack = catalog.nextTrackAfter(track)
                val nextPromptEn = if (nextTrack != null) "Ready for ${nextTrack.shortLabelEn}!" else "All 3 BAEUM syllabi mastered!"
                val nextPromptId = if (nextTrack != null) "Siap lanjut ke ${nextTrack.shortLabelId}!" else "Seluruh 3 Silabus BAEUM tuntas!"
                emitCelebration(
                    CelebrationBannerEvent(
                        kind = CelebrationKind.DAILY_SEAL,
                        titleEn = "${track.shortLabelEn} Syllabus Graduated!",
                        titleId = "Lulus Silabus ${track.shortLabelId}!",
                        subtitleEn = "+$totalEarned XP earned! $nextPromptEn",
                        subtitleId = "Meraih +$totalEarned XP! $nextPromptId",
                        badgeKo = "수료",
                        xpDelta = totalEarned,
                        showConfetti = true
                    )
                )
            } else if (newlyCompleted || newlyPerfected) {
                if (newlyPerfected) soundEffects.playCorrect(10)
                val starBadge = if (isPerfect) "★★★ " else ""
                emitCelebration(
                    CelebrationBannerEvent(
                        kind = CelebrationKind.DAILY_SPARK,
                        titleEn = "${starBadge}${track.shortLabelEn} #${lesson.number} · +$totalEarned XP!",
                        titleId = "${starBadge}${track.shortLabelId} #${lesson.number} · +$totalEarned XP!",
                        subtitleEn = if (newlyPerfected && !newlyCompleted) {
                            "3-Star Perfect Mastery unlocked!"
                        } else {
                            lesson.titleEn
                        },
                        subtitleId = if (newlyPerfected && !newlyCompleted) {
                            "Stempel Sempurna ★★★ terbuka!"
                        } else {
                            lesson.titleId
                        },
                        badgeKo = lesson.emblemKo,
                        xpDelta = totalEarned,
                        showConfetti = true
                    )
                )
            }
        }
    }

    fun recordSyllabusMistake(mistakeKey: String) {
        if (mistakeKey.isBlank()) return
        viewModelScope.launch {
            userPreferencesRepository.recordSyllabusMistake(mistakeKey)
        }
    }

    fun redeemSyllabusMistake(mistakeKey: String) {
        if (mistakeKey.isBlank()) return
        val bonus = com.example.domain.curriculum.BeginnerSyllabusCatalog.REDEMPTION_BONUS_XP
        val todayKey = BaeumRepository.localDayKey()
        soundEffects.playCorrect(6)
        viewModelScope.launch {
            val resolved = userPreferencesRepository.resolveSyllabusMistake(mistakeKey, bonus)
            repository.recordStudySeconds(25)
            userPreferencesRepository.recordStudyActivity(todayKey)
            if (resolved) {
                emitCelebration(
                    CelebrationBannerEvent(
                        kind = CelebrationKind.DAILY_SPARK,
                        titleEn = "Mistake Redeemed · +$bonus XP!",
                        titleId = "Kesalahan Ditaklukkan · +$bonus XP!",
                        subtitleEn = "Your Korean memory just got stronger!",
                        subtitleId = "Memori bahasa Koreamu makin tajam!",
                        badgeKo = "극복",
                        xpDelta = bonus,
                        showConfetti = false
                    )
                )
            }
        }
    }

    fun completeStageBoss(stageNumber: Int, passed: Boolean, correctCount: Int, totalCount: Int) {
        val stage = com.example.domain.curriculum.BeginnerSyllabusCatalog.stageByNumber(stageNumber) ?: return
        val todayKey = BaeumRepository.localDayKey()
        val reward = com.example.domain.curriculum.BeginnerSyllabusCatalog.STAGE_BOSS_XP
        if (passed) {
            soundEffects.playLevelUp()
        } else {
            soundEffects.playStampUnlock()
        }
        viewModelScope.launch {
            repository.recordStudySeconds(65)
            userPreferencesRepository.recordStudyActivity(todayKey)
            if (passed) {
                val newlyDefeated = userPreferencesRepository.recordStageBossDefeated(stageNumber, reward)
                if (newlyDefeated) {
                    emitCelebration(
                        CelebrationBannerEvent(
                            kind = CelebrationKind.DAILY_SEAL,
                            titleEn = "Stage Boss Defeated · +$reward XP!",
                            titleId = "Bos Tahap Tumbang · +$reward XP!",
                            subtitleEn = "Unlocked Dojang Seal '${stage.bossSealKo}' ($correctCount/$totalCount)!",
                            subtitleId = "Stempel Dojang '${stage.bossSealKo}' terbuka ($correctCount/$totalCount)!",
                            badgeKo = stage.bossSealKo,
                            xpDelta = reward,
                            showConfetti = true
                        )
                    )
                }
            }
        }
    }

    fun startSeoulBlitz() {
        blitzTimerJob?.cancel()
        val curSettings = settings.value
        val initial = SeoulBlitzEngine.startSession(
            allVocab = allVocab.value,
            activePathway = curSettings.activePathway,
            translationLang = curSettings.translationLanguage,
            previousBestScore = curSettings.engagement.blitzBestScore
        )
        _blitzSession.value = initial
        if (!initial.isFinished) {
            soundEffects.playCardFlip()
            blitzTimerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000L)
                    val cur = _blitzSession.value ?: break
                    if (cur.isFinished) break
                    val ticked = SeoulBlitzEngine.tickSecond(cur)
                    _blitzSession.value = ticked
                    if (ticked.isFinished) {
                        persistCompletedBlitz(ticked)
                        break
                    }
                }
            }
        }
    }

    fun answerBlitzOption(selectedOption: String) {
        val cur = _blitzSession.value ?: return
        if (cur.isFinished) return
        val activePrompt = cur.currentPrompt ?: return
        val next = SeoulBlitzEngine.submitAnswer(
            state = cur,
            selectedOption = selectedOption,
            allVocab = allVocab.value
        )
        _blitzSession.value = next

        val wasCorrect = next.lastAnswerWasCorrect == true
        if (wasCorrect) {
            soundEffects.playCorrect(next.combo)
        } else {
            soundEffects.playGentleMiss()
        }

        viewModelScope.launch {
            repository.recordQuizAnswerTelemetry(
                vocabId = activePrompt.vocabId,
                isCorrect = wasCorrect
            )
            if (next.combo >= 3) {
                userPreferencesRepository.recordBestCombo(next.combo)
            }
        }
    }

    fun abandonSeoulBlitz() {
        blitzTimerJob?.cancel()
        blitzTimerJob = null
        _blitzSession.value = null
    }

    private fun persistCompletedBlitz(finished: SeoulBlitzState) {
        blitzTimerJob?.cancel()
        blitzTimerJob = null
        if (finished.answeredCount <= 0) return

        if (finished.isNewRecord) {
            soundEffects.playLevelUp()
        } else {
            soundEffects.playStampUnlock()
        }

        viewModelScope.launch {
            val (isNewRecord, _) = userPreferencesRepository.recordBlitzResult(
                score = finished.score,
                maxCombo = finished.maxCombo
            )
            val xp = BaeumHabitEngine.computeBlitzXp(finished.score, isNewRecord)
            userPreferencesRepository.awardXp(xp)
            repository.saveQuizRecord(
                mode = "Seoul Blitz",
                curriculum = finished.activePathway.ifBlank { settings.value.activePathway },
                totalQuestions = finished.answeredCount,
                correctAnswers = finished.correctCount,
                durationSec = finished.totalDurationSec + finished.timeBonusEarnedSec,
                weakTopicNote = if (finished.missedWords.isNotEmpty()) {
                    finished.missedWords.take(3).joinToString(", ") { it.hangul }
                } else {
                    "Clean 60s Blitz"
                }
            )
            if (isNewRecord) {
                emitCelebration(
                    CelebrationBannerEvent(
                        kind = CelebrationKind.NEW_BLITZ_RECORD,
                        titleEn = "New Seoul Blitz Record · ${finished.score} pts!",
                        titleId = "Rekor Baru Seoul Blitz · ${finished.score} poin!",
                        subtitleEn = "Peak combo ${finished.maxCombo}x • +$xp XP earned.",
                        subtitleId = "Kombo puncak ${finished.maxCombo}x • +$xp XP diperoleh.",
                        badgeKo = "신기록",
                        xpDelta = xp,
                        showConfetti = true
                    )
                )
            }
        }
    }

    fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            val isId = settings.value.translationLanguage.equals("ID", ignoreCase = true)
            userPreferencesRepository.setDailyReminder(enabled, hour, minute)
            reminderScheduler(enabled, hour, minute, isId)
            val timeStr = String.format(java.util.Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            _statusMessage.value = if (enabled) {
                if (isId) "Pengingat belajar harian aktif pukul $timeStr." else "Daily study reminder scheduled for $timeStr."
            } else {
                if (isId) "Pengingat belajar harian dimatikan." else "Daily study reminder turned off."
            }
        }
    }

    fun sendTestStudyReminder() {
        val summary = dashboardSummary.value
        val isId = settings.value.translationLanguage.equals("ID", ignoreCase = true)
        val posted = testReminderPoster(
            summary.dueTodayCount,
            summary.todayWords,
            summary.dailyGoal,
            summary.streakDays,
            isId
        )
        _statusMessage.value = if (posted) {
            if (isId) "Notifikasi uji coba dikirim! Periksa bilah notifikasi perangkat." else "Test reminder sent! Check your notification shade."
        } else {
            if (isId) "Izin notifikasi belum aktif. Aktifkan notifikasi Android terlebih dahulu." else "Notification permission is not enabled yet. Please allow notifications first."
        }
    }

    // ==================== VOCABULARY & GRAMMAR ====================
    fun toggleBookmark(vocab: VocabEntity) {
        viewModelScope.launch {
            repository.toggleVocabBookmark(vocab.id, !vocab.isBookmarked)
        }
    }

    fun toggleGrammarBookmark(grammarId: String) {
        viewModelScope.launch {
            val isBookmarked = grammarBookmarks.value.any { it.grammarId == grammarId }
            repository.toggleGrammarBookmark(grammarId, isBookmarked)
        }
    }

    fun speakKorean(text: String) {
        audioController.speakKorean(text)
    }

    // ==================== ADAPTIVE QUIZ ENGINE (QuizManager) ====================
    fun startQuiz(mode: QuizMode, weakOnly: Boolean = false, focusGrammarId: String? = null) {
        quizManager.startSession(
            mode = mode,
            allVocab = allVocab.value,
            translationLang = settings.value.translationLanguage,
            maxCount = 8,
            weakOnly = weakOnly,
            focusGrammarId = focusGrammarId,
            activeCurriculum = settings.value.activePathway
        )
    }

    fun selectQuizOption(option: String) {
        quizManager.selectOption(option)
    }

    fun updateQuizTypedInput(text: String) {
        quizManager.updateTypedInput(text)
    }

    fun appendArrangementToken(optionIndex: Int) {
        quizManager.appendArrangementToken(optionIndex)
    }

    fun removeArrangementToken(placedPosition: Int) {
        quizManager.removeArrangementToken(placedPosition)
    }

    fun clearArrangement() {
        quizManager.clearArrangement()
    }

    fun checkQuizAnswer() {
        val outcome = quizManager.submitCurrentAnswer() ?: return
        val currentStreak = quizManager.sessionState.value?.currentStreak ?: 0
        if (outcome.evaluation.isCorrect) {
            soundEffects.playCorrect(currentStreak.coerceAtLeast(1))
        } else {
            soundEffects.playGentleMiss()
        }
        viewModelScope.launch {
            repository.recordQuizAnswerTelemetry(
                vocabId = outcome.question.vocabId,
                isCorrect = outcome.evaluation.isCorrect
            )
            if (currentStreak >= 3) {
                userPreferencesRepository.recordBestCombo(currentStreak)
            }
        }
    }

    fun nextQuizQuestion() {
        val maxStreakBeforeComplete = quizManager.sessionState.value?.maxStreak ?: 0
        val completion = quizManager.advanceToNext() ?: return
        val earnedXp = BaeumHabitEngine.computeQuizCompletionXp(
            correctAnswers = completion.correctAnswers,
            totalQuestions = completion.totalQuestions,
            maxStreak = maxStreakBeforeComplete
        )
        if (completion.correctAnswers * 10 >= completion.totalQuestions * 7) {
            soundEffects.playStampUnlock()
        }
        viewModelScope.launch {
            repository.saveQuizRecord(
                mode = completion.mode.title,
                curriculum = settings.value.activePathway,
                totalQuestions = completion.totalQuestions,
                correctAnswers = completion.correctAnswers,
                durationSec = completion.durationSec.coerceAtLeast(10),
                weakTopicNote = completion.weakTopicNote
            )
            userPreferencesRepository.awardXp(earnedXp)
            if (maxStreakBeforeComplete >= 3) {
                userPreferencesRepository.recordBestCombo(maxStreakBeforeComplete)
            }
        }
    }

    // ==================== KOREAN SPEAKING & CONVERSATION ENGINE ====================
    fun startAutoConversation(difficulty: SpeakingDifficulty, categoryFilter: String? = null) {
        audioController.resetCapture()
        val recent = settings.value.lastScenarioIds.split(",").filter { it.isNotBlank() }
        val scenario = ConversationEngine.pickAutoScenario(difficulty, recent, categoryFilter)
        startSpecificScenario(scenario)
    }

    fun startSpecificScenario(scenario: SpeakingScenario) {
        audioController.resetCapture()
        val initial = ConversationEngine.startScenario(scenario)
        _liveConversation.value = initial
        _lastSpeakingAssessment.value = null
        initial.currentPrompt?.let { audioController.speakKorean(it.promptKo) }
    }

    fun startVoiceCapture() {
        audioController.startKoreanListening()
    }

    fun stopVoiceCapture() {
        audioController.stopListening()
    }

    fun redoVoiceCapture() {
        audioController.resetCapture()
        _liveConversation.update {
            it?.copy(
                phase = ConversationPhase.AWAITING_INPUT,
                draftTranscript = "",
                livePartialTranscript = "",
                draftEditedManually = false,
                lastRecordingPath = null,
                lastRecordingSeconds = 0,
                lastWaveform = emptyList(),
                recognitionErrorMessage = null
            )
        }
    }

    // ==================== KOREAN PRONUNCIATION SPEECH PRACTICE ====================
    fun selectPronunciationCategory(category: PronunciationDrillCategory?) {
        audioController.resetPronunciationRecognition()
        _pronunciationPractice.update { cur ->
            val filtered = if (category == null) cur.drills else cur.drills.filter { it.category == category }
            val nextActive = if (cur.activeDrill in filtered) cur.activeDrill else (filtered.firstOrNull() ?: cur.activeDrill)
            cur.copy(
                selectedCategory = category,
                activeDrill = nextActive,
                lastAttempt = null,
                recognitionState = SpeechRecognitionState.Idle
            )
        }
    }

    fun selectPronunciationDrill(drill: PronunciationDrillItem, autoSpeakModel: Boolean = true) {
        audioController.resetPronunciationRecognition()
        _pronunciationPractice.update { cur ->
            val updatedDrills = if (cur.drills.any { it.id == drill.id }) cur.drills else cur.drills + drill
            cur.copy(
                drills = updatedDrills,
                activeDrill = drill,
                lastAttempt = null,
                recognitionState = SpeechRecognitionState.Idle
            )
        }
        if (autoSpeakModel) {
            audioController.speakKorean(drill.targetWrittenKo)
        }
    }

    fun selectNextPronunciationDrill() {
        val state = _pronunciationPractice.value
        val pool = if (state.selectedCategory == null) {
            state.drills
        } else {
            state.drills.filter { it.category == state.selectedCategory }.ifEmpty { state.drills }
        }
        val currentIdx = pool.indexOfFirst { it.id == state.activeDrill.id }
        val nextDrill = pool[(currentIdx + 1) % pool.size]
        selectPronunciationDrill(nextDrill, autoSpeakModel = true)
    }

    fun startPronunciationListening() {
        val activeDrill = _pronunciationPractice.value.activeDrill
        val isShortWord = activeDrill.targetWrittenKo.length <= 5
        _pronunciationPractice.update { it.copy(lastAttempt = null) }
        audioController.startPronunciationRecognition(shortWordDrill = isShortWord)
    }

    fun stopPronunciationListening() {
        audioController.stopPronunciationRecognition()
    }

    fun cancelPronunciationListening() {
        audioController.resetPronunciationRecognition()
    }

    fun clearPronunciationResult() {
        audioController.resetPronunciationRecognition()
        _pronunciationPractice.update {
            it.copy(
                lastAttempt = null,
                recognitionState = SpeechRecognitionState.Idle
            )
        }
    }

    fun enterManualTranscriptMode() {
        audioController.resetCapture()
        _liveConversation.update {
            it?.copy(
                phase = ConversationPhase.PREVIEW_TRANSCRIPT,
                draftEditedManually = true,
                recognitionErrorMessage = null
            )
        }
    }

    fun updateDraftTranscript(text: String) {
        _liveConversation.update {
            it?.copy(draftTranscript = text, draftEditedManually = true)
        }
    }

    fun toggleConversationHint() {
        _liveConversation.update { it?.copy(showOptionalHint = !it.showOptionalHint) }
    }

    fun playDraftRecording() {
        audioController.playLastRecording(_liveConversation.value?.lastRecordingPath)
    }

    fun submitConversationTurn(): Boolean {
        val state = _liveConversation.value ?: return false
        val prompt = state.currentPrompt ?: return false
        if (state.phase == ConversationPhase.EVALUATING || state.phase == ConversationPhase.COMPLETED) return false

        val isId = settings.value.translationLanguage.equals("ID", ignoreCase = true)

        val evaluated = ConversationEngine.evaluateResponse(
            prompt = prompt,
            userText = state.draftTranscript,
            wasManualOrEdited = state.draftEditedManually,
            isIndonesian = isId
        )
        audioController.resetCapture()
        val nextState = ConversationEngine.commitTurnAndAdvance(state, evaluated)
        _liveConversation.value = nextState

        if (nextState.phase == ConversationPhase.COMPLETED) {
            val assessment = ConversationEngine.buildFinalAssessment(nextState, isIndonesian = isId)
            _lastSpeakingAssessment.value = assessment
            viewModelScope.launch {
                val now = System.currentTimeMillis()
                repository.saveSpeakingSession(
                    SpeakingSessionEntity(
                        scenarioId = nextState.scenario.id,
                        scenarioTitle = nextState.scenario.localizedTitle(isId),
                        difficulty = nextState.scenario.difficulty.label,
                        totalTurns = nextState.totalTurns,
                        completedTurns = assessment.completedTurns,
                        overallScore = assessment.totalScoreOutOf100,
                        relevanceAvg = assessment.relevanceAvg,
                        vocabAvg = assessment.vocabularyAvg,
                        grammarAvg = assessment.grammarAvg,
                        communicationAvg = assessment.communicationAvg,
                        strengths = assessment.strengths.joinToString("||"),
                        weaknesses = assessment.weaknesses.joinToString("||"),
                        correctionsSerialized = assessment.improvements.joinToString("||") { "${it.first}=>${it.second}=>${it.third}" },
                        recommendations = assessment.recommendations.joinToString("||"),
                        timestampEpochMs = now,
                        dayKey = BaeumRepository.localDayKey(now)
                    )
                )
                val speakingXp = BaeumHabitEngine.SPEAKING_SESSION_BASE_XP + (assessment.totalScoreOutOf100 / 4)
                userPreferencesRepository.awardXp(speakingXp)
            }
            soundEffects.playStampUnlock()
            return true
        } else {
            nextState.currentPrompt?.let { audioController.speakKorean(it.promptKo) }
            return false
        }
    }

    fun loadHistoricalSpeakingAssessment(entity: SpeakingSessionEntity) {
        val isId = settings.value.translationLanguage.equals("ID", ignoreCase = true)
        val corrections = entity.correctionsSerialized.split("||").mapNotNull { row ->
            val p = row.split("=>")
            if (p.size == 3) Triple(p[0], p[1], p[2]) else null
        }
        val diff = SpeakingDifficulty.entries.firstOrNull { it.label == entity.difficulty } ?: SpeakingDifficulty.BEGINNER
        _lastSpeakingAssessment.value = FinalSpeakingAssessment(
            scenarioTitle = entity.scenarioTitle,
            difficulty = diff,
            totalScoreOutOf100 = entity.overallScore,
            bandLabel = when {
                entity.overallScore >= 70 && isId -> "Komunikasi baik"
                entity.overallScore >= 70 -> "Good communication"
                isId -> "Tinjauan latihan"
                else -> "Practice review"
            },
            completedTurns = entity.completedTurns,
            relevanceAvg = entity.relevanceAvg,
            vocabularyAvg = entity.vocabAvg,
            grammarAvg = entity.grammarAvg,
            communicationAvg = entity.communicationAvg,
            strengths = entity.strengths.split("||").filter { it.isNotBlank() },
            weaknesses = entity.weaknesses.split("||").filter { it.isNotBlank() },
            improvements = corrections,
            recommendations = entity.recommendations.split("||").filter { it.isNotBlank() },
            turns = emptyList()
        )
    }

    // ==================== EXAMINATION ENGINE ====================
    fun startExam(
        examType: ExamType,
        mode: ExamSessionMode,
        paperLength: ExamPaperLength = ExamPaperLength.STANDARD
    ) {
        audioController.stopSpeaking()
        audioController.resetCapture()
        _liveConversation.value = null
        _activeExam.value = ExamEngine.startExam(
            examType = examType,
            mode = mode,
            allVocab = allVocab.value,
            translationLang = settings.value.translationLanguage,
            paperLength = paperLength
        )
        _lastExamReport.value = null
    }

    fun playExamAudio(questionId: String) {
        val session = _activeExam.value ?: return
        if (session.isSubmitted) return
        val q = session.questions.firstOrNull { it.id == questionId } ?: return
        val script = q.audioTranscriptKo ?: return
        if (!session.canPlayAudio(q)) return

        val key = session.audioQuotaKey(q)
        val nextCount = (session.audioPlayCounts[key] ?: 0) + 1
        _activeExam.update { cur ->
            if (cur == null || cur.isSubmitted) cur
            else cur.copy(audioPlayCounts = cur.audioPlayCounts + (key to nextCount))
        }
        audioController.speakKorean(script)
    }

    fun stopExamAudio() {
        audioController.stopSpeaking()
    }

    fun startExamVoiceAnswer() {
        val session = _activeExam.value ?: return
        if (session.isSubmitted || session.currentQuestion?.section != "Speaking") return
        audioController.stopSpeaking()
        audioController.startKoreanListening()
    }

    fun stopExamVoiceAnswer() {
        audioController.stopListening()
    }

    fun selectExamOption(questionId: String, optionIndex: Int) {
        _activeExam.update { session ->
            if (session == null || session.isSubmitted) session
            else {
                val updatedElims = session.eliminatedOptions[questionId]?.minus(optionIndex)
                val nextElimMap = if (updatedElims.isNullOrEmpty()) {
                    session.eliminatedOptions - questionId
                } else {
                    session.eliminatedOptions + (questionId to updatedElims)
                }
                session.copy(
                    selectedOptions = session.selectedOptions + (questionId to optionIndex),
                    eliminatedOptions = nextElimMap
                )
            }
        }
    }

    fun toggleEliminateExamOption(questionId: String, optionIndex: Int) {
        _activeExam.update { session ->
            if (session == null || session.isSubmitted) session
            else {
                val curSet = session.eliminatedOptions[questionId].orEmpty().toMutableSet()
                val added = curSet.add(optionIndex)
                if (!added) curSet.remove(optionIndex)
                val nextElimMap = if (curSet.isEmpty()) {
                    session.eliminatedOptions - questionId
                } else {
                    session.eliminatedOptions + (questionId to curSet)
                }
                val nextSelected = if (added && session.selectedOptions[questionId] == optionIndex) {
                    session.selectedOptions - questionId
                } else {
                    session.selectedOptions
                }
                session.copy(
                    selectedOptions = nextSelected,
                    eliminatedOptions = nextElimMap
                )
            }
        }
    }

    fun updateExamOpenResponse(questionId: String, text: String) {
        _activeExam.update { session ->
            if (session == null || session.isSubmitted) session
            else session.copy(openResponses = session.openResponses + (questionId to text))
        }
    }

    fun updateExamDualBlankResponse(questionId: String, blankG: String, blankN: String) {
        val formatted = ExamEngine.formatDualBlankResponse(blankG, blankN)
        updateExamOpenResponse(questionId, formatted)
    }

    fun toggleExamFlag(questionId: String) {
        _activeExam.update { session ->
            if (session == null || session.isSubmitted) session
            else {
                val flags = session.flaggedQuestionIds.toMutableSet()
                if (!flags.add(questionId)) flags.remove(questionId)
                session.copy(flaggedQuestionIds = flags)
            }
        }
    }

    fun jumpToExamQuestion(index: Int) {
        audioController.stopSpeaking()
        audioController.resetCapture()
        _activeExam.update { session ->
            if (session == null) null
            else if (!session.isQuestionAccessible(index)) session
            else session.copy(currentIndex = index.coerceIn(0, session.questions.lastIndex.coerceAtLeast(0)))
        }
    }

    fun enterExamPeriodTransition() {
        audioController.stopSpeaking()
        audioController.resetCapture()
        _activeExam.update { session ->
            if (session == null) null else ExamEngine.enterPeriodTransition(session)
        }
    }

    fun startNextExamPeriod() {
        audioController.stopSpeaking()
        audioController.resetCapture()
        _activeExam.update { session ->
            if (session == null) null else ExamEngine.startNextPeriod(session)
        }
    }

    fun abandonActiveExam() {
        audioController.stopSpeaking()
        audioController.resetCapture()
        _activeExam.value = null
    }

    fun submitActiveExam(): ExamReport? {
        val session = _activeExam.value ?: return null
        if (session.isSubmitted) return _lastExamReport.value

        audioController.stopSpeaking()
        audioController.resetCapture()

        val report = ExamEngine.gradeSession(
            session = session,
            translationLang = settings.value.translationLanguage
        )
        _activeExam.value = session.copy(isSubmitted = true, awaitingPeriodTransition = false)
        _lastExamReport.value = report

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val serializedSections = report.sectionScores.joinToString("||") { "${it.sectionName}::${it.earned}::${it.max}" }
            repository.saveExamAttempt(
                ExamAttemptEntity(
                    examType = report.examType.name,
                    mode = report.mode.name,
                    totalScore = report.totalScore,
                    maxScore = report.maxScore,
                    sectionScoresSerialized = serializedSections,
                    estimatedLevel = report.estimatedLevel,
                    analysisSummary = report.analysisSummary,
                    wrongCount = report.questionReviews.count { !it.isCorrect },
                    totalQuestions = report.questionReviews.size,
                    durationSec = report.durationSec,
                    timestampEpochMs = now,
                    dayKey = BaeumRepository.localDayKey(now)
                )
            )
            val examXp = if (report.mode == ExamSessionMode.MOCK) {
                BaeumHabitEngine.EXAM_MOCK_XP
            } else {
                BaeumHabitEngine.EXAM_PRACTICE_XP
            }
            userPreferencesRepository.awardXp(examXp)
        }
        return report
    }

    // ==================== BACKUP & RESTORE ====================
    suspend fun generateBackupJson(): String = repository.exportBackupJson()

    fun restoreFromBackupJson(json: String) {
        viewModelScope.launch {
            val isId = settings.value.translationLanguage.equals("ID", ignoreCase = true)
            repository.restoreBackupJson(json)
                .onSuccess { msg -> _statusMessage.value = msg }
                .onFailure { err ->
                    _statusMessage.value = if (isId) {
                        "Gagal memulihkan cadangan: ${err.localizedMessage ?: "Format JSON tidak valid."}"
                    } else {
                        "Restore failed: ${err.localizedMessage ?: "Invalid JSON format."}"
                    }
                }
        }
    }

    fun recordWritingPracticeCompleted() {
        soundEffects.playStampUnlock()
        viewModelScope.launch {
            repository.recordStudySeconds(45)
            userPreferencesRepository.awardXp(BaeumHabitEngine.WRITING_PRACTICE_XP)
        }
    }

    override fun onCleared() {
        blitzTimerJob?.cancel()
        celebrationDismissJob?.cancel()
        soundEffects.release()
        audioController.release()
        super.onCleared()
    }

    class Factory(
        private val repository: BaeumRepository,
        private val audioControllerFactory: () -> KoreanAudioController,
        private val userPreferencesRepository: UserPreferencesRepository = repository.userPreferences,
        private val reminderScheduler: (Boolean, Int, Int, Boolean) -> Unit = { _, _, _, _ -> },
        private val testReminderPoster: (Int, Int, Int, Int, Boolean) -> Boolean = { _, _, _, _, _ -> false }
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BaeumViewModel(
                repository = repository,
                audioController = audioControllerFactory(),
                userPreferencesRepository = userPreferencesRepository,
                reminderScheduler = reminderScheduler,
                testReminderPoster = testReminderPoster
            ) as T
        }
    }
}
