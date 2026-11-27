package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.DayForecastPoint
import com.example.data.repository.LearningStreakState
import com.example.data.repository.ThemePreferences
import com.example.data.repository.UserPreferencesRepository
import com.example.data.seed.ExamPaperLength
import com.example.data.seed.ExamType
import com.example.data.seed.SpeakingDifficulty
import com.example.data.seed.SpeakingScenario
import com.example.domain.exam.ActiveExamSession
import com.example.domain.exam.ExamEngine
import com.example.domain.exam.ExamReport
import com.example.domain.exam.ExamSessionMode
import com.example.domain.fsrs.CardMemoryStage
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
        val todayKey = BaeumRepository.localDayKey()
        val todayStat = stats.firstOrNull { it.dayKey == todayKey }
        val roomStreak = BaeumRepository.computeStudyStreak(stats)
        val persistedStreak = userSettings.learningStreak.effectiveCurrentStreak(todayKey)
        val streak = maxOf(persistedStreak, roomStreak)
        val longestStreak = maxOf(userSettings.learningStreak.longestStreakDays, streak)
        val forecast = BaeumRepository.buildSevenDayForecast(vocab)
        val dueToday = BaeumRepository.countDueTodayOnly(vocab)
        val overdue = BaeumRepository.countOverdue(vocab)
        val mastered = BaeumRepository.countStage(vocab, CardMemoryStage.MASTERED)
        val totalSecs = stats.sumOf { it.studySeconds }
        val totalQ = quizzes.sumOf { it.totalQuestions }
        val totalCorrect = quizzes.sumOf { it.correctAnswers }
        val quizAcc = if (totalQ > 0) ((totalCorrect.toFloat() / totalQ) * 100f).toInt() else null

        val lang = userSettings.translationLanguage
        val isId = lang.equals("ID", ignoreCase = true)
        val activeVocab = vocab.filter { it.curriculum == userSettings.activePathway }
        val activeLearned = activeVocab.count { it.reps > 0 }
        val pathwayLabel = when (userSettings.activePathway) {
            CurriculumPathway.HANGUL -> "Hangul"
            CurriculumPathway.TOPIK -> "TOPIK I"
            CurriculumPathway.EPS_TOPIK -> "EPS-TOPIK"
            CurriculumPathway.EVERYDAY -> if (isId) "Sehari-hari" else "Everyday Korean"
            else -> "TOPIK I"
        }

        val weakQuizVocab = vocab.count { it.quizWrongCount > it.quizCorrectCount }
        val recommendation = when {
            dueToday > 0 -> if (isId) "Ulas $dueToday kartu FSRS yang terjadwal hari ini" else "Review $dueToday scheduled FSRS card(s)"
            weakQuizVocab >= 2 -> if (isId) "Latih $weakQuizVocab kosakata lemah di Kuis Pintar" else "Practice $weakQuizVocab weak vocabulary item(s) in Smart Quiz"
            vocab.count { it.reps > 0 } == 0 -> if (isId) "Mulai sesi Kartu Pintar pertamamu" else "Start your first Smart Flashcard session"
            quizzes.isEmpty() -> if (isId) "Uji daya ingatmu di Pusat Kuis (Batchim & Kosakata)" else "Test your recall in Quiz Center (Batchim & Recall)"
            else -> if (isId) "Latih percakapan bahasa Korea langsung di tab Bicara" else "Practice a live Korean conversation in Speak"
        }

        DashboardSummary(
            todayWords = todayStat?.wordsReviewed ?: 0,
            dailyGoal = userSettings.dailyGoalWords,
            streakDays = streak,
            longestStreakDays = longestStreak,
            dueTodayCount = dueToday,
            overdueCount = overdue,
            sevenDayForecast = forecast,
            masteredCount = mastered,
            totalVocabCount = vocab.size,
            totalStudySeconds = totalSecs,
            quizAccuracyPercent = quizAcc,
            activePathwayLabel = pathwayLabel,
            activePathwayLearned = activeLearned,
            activePathwayTotal = activeVocab.size,
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

    // Active Session States
    val quizManager: QuizManager = QuizManager()
    val activeQuiz: StateFlow<QuizSessionState?> = quizManager.sessionState
    val quizModeProgress: StateFlow<Map<QuizMode, ModeProgressStats>> = quizManager.modeProgress

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
                            repository.recordStudySeconds(15)
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
    }

    fun reportStatusMessage(message: String) {
        _statusMessage.value = message
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
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
        viewModelScope.launch {
            repository.recordQuizAnswerTelemetry(
                vocabId = outcome.question.vocabId,
                isCorrect = outcome.evaluation.isCorrect
            )
        }
    }

    fun nextQuizQuestion() {
        val completion = quizManager.advanceToNext() ?: return
        viewModelScope.launch {
            repository.saveQuizRecord(
                mode = completion.mode.title,
                curriculum = settings.value.activePathway,
                totalQuestions = completion.totalQuestions,
                correctAnswers = completion.correctAnswers,
                durationSec = completion.durationSec.coerceAtLeast(10),
                weakTopicNote = completion.weakTopicNote
            )
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
            }
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
        viewModelScope.launch {
            repository.recordStudySeconds(45)
        }
    }

    override fun onCleared() {
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
