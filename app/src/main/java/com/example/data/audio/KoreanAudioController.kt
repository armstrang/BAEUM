package com.example.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.UUID

sealed interface TtsStatus {
    data object Initializing : TtsStatus
    data object Ready : TtsStatus
    data class Speaking(val text: String) : TtsStatus
    data class Unavailable(val reason: String) : TtsStatus
}

sealed interface SpeechCaptureState {
    data object Idle : SpeechCaptureState
    data class Listening(
        val elapsedSeconds: Int = 0,
        val partialTranscript: String = "",
        val amplitudes: List<Float> = emptyList()
    ) : SpeechCaptureState
    data class Captured(
        val transcript: String,
        val alternatives: List<String> = listOf(transcript),
        val durationSeconds: Int,
        val amplitudes: List<Float>,
        val audioFilePath: String?
    ) : SpeechCaptureState
    data class TechnicalFailure(
        val message: String,
        val messageId: String = message,
        val audioFilePath: String?,
        val durationSeconds: Int,
        val amplitudes: List<Float>
    ) : SpeechCaptureState
}

private enum class RecognitionConsumer {
    NONE,
    CONVERSATION,
    PRONUNCIATION
}

/**
 * Coordinates Android Korean TTS (`ko-KR`), [KoreanSpeechRecognitionService] (Android `SpeechRecognizer`),
 * fallback [MediaRecorder]/[MediaPlayer] capture & playback, and temporary audio cleanup.
 *
 * Strictly separates technical speech recognition errors from Korean language quality,
 * and routes speech recognition events cleanly between Live Conversation and Pronunciation Practice.
 */
class KoreanAudioController(context: Context) {

    private val appContext = context.applicationContext
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val speechRecognitionService: KoreanSpeechRecognitionService =
        KoreanSpeechRecognitionService(appContext)

    @Volatile
    private var isReleased: Boolean = false
    private var tts: TextToSpeech? = null
    private var pendingSpeakText: String? = null
    private var speechRate: Float = 1.0f

    private var activeConsumer: RecognitionConsumer = RecognitionConsumer.NONE
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activeRecordingFile: File? = null
    private var recorderTickerJob: Job? = null
    private var recordedAmplitudes = mutableListOf<Float>()
    private var elapsedSeconds = 0

    private val _ttsStatus = MutableStateFlow<TtsStatus>(TtsStatus.Initializing)
    val ttsStatus: StateFlow<TtsStatus> = _ttsStatus.asStateFlow()

    private val _captureState = MutableStateFlow<SpeechCaptureState>(SpeechCaptureState.Idle)
    val captureState: StateFlow<SpeechCaptureState> = _captureState.asStateFlow()

    private val _pronunciationRecognitionState =
        MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val pronunciationRecognitionState: StateFlow<SpeechRecognitionState> =
        _pronunciationRecognitionState.asStateFlow()

    private val _isPlayingRecording = MutableStateFlow(false)
    val isPlayingRecording: StateFlow<Boolean> = _isPlayingRecording.asStateFlow()

    init {
        initTts()
        observeSpeechRecognitionService()
    }

    private fun observeSpeechRecognitionService() {
        controllerScope.launch {
            speechRecognitionService.state.collect { st ->
                when (activeConsumer) {
                    RecognitionConsumer.CONVERSATION -> mapServiceStateToConversationCapture(st)
                    RecognitionConsumer.PRONUNCIATION -> _pronunciationRecognitionState.value = st
                    RecognitionConsumer.NONE -> Unit
                }
            }
        }
    }

    private fun mapServiceStateToConversationCapture(st: SpeechRecognitionState) {
        when (st) {
            SpeechRecognitionState.Idle -> {
                if (mediaRecorder == null) {
                    _captureState.value = SpeechCaptureState.Idle
                }
            }
            SpeechRecognitionState.Preparing -> {
                _captureState.value = SpeechCaptureState.Listening(
                    elapsedSeconds = 0,
                    partialTranscript = "",
                    amplitudes = emptyList()
                )
            }
            is SpeechRecognitionState.Listening -> {
                _captureState.value = SpeechCaptureState.Listening(
                    elapsedSeconds = st.elapsedSeconds,
                    partialTranscript = st.partialTranscript,
                    amplitudes = st.amplitudes
                )
            }
            is SpeechRecognitionState.Processing -> {
                _captureState.value = SpeechCaptureState.Listening(
                    elapsedSeconds = st.elapsedSeconds,
                    partialTranscript = st.lastPartialTranscript,
                    amplitudes = st.amplitudes
                )
            }
            is SpeechRecognitionState.Result -> {
                _captureState.value = SpeechCaptureState.Captured(
                    transcript = st.primaryTranscript,
                    alternatives = st.alternatives,
                    durationSeconds = st.durationSeconds,
                    amplitudes = st.amplitudes,
                    audioFilePath = activeRecordingFile?.absolutePath
                )
            }
            is SpeechRecognitionState.Error -> {
                _captureState.value = SpeechCaptureState.TechnicalFailure(
                    message = st.messageEn,
                    messageId = st.messageId,
                    audioFilePath = activeRecordingFile?.absolutePath,
                    durationSeconds = st.durationSeconds,
                    amplitudes = st.amplitudes
                )
            }
        }
    }

    private fun shutdownTtsEngine() {
        val existing = tts
        tts = null
        if (existing != null) {
            runCatching { existing.stop() }
            runCatching { existing.shutdown() }
        }
    }

    private fun initTts() {
        if (isReleased) return
        shutdownTtsEngine()
        _ttsStatus.value = TtsStatus.Initializing

        var createdEngine: TextToSpeech? = null
        val engine = TextToSpeech(appContext) { status ->
            val current = createdEngine
            if (isReleased || current == null || current !== tts) {
                runCatching { current?.stop() }
                runCatching { current?.shutdown() }
                return@TextToSpeech
            }
            if (status == TextToSpeech.SUCCESS) {
                val result = current.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    pendingSpeakText = null
                    _ttsStatus.value = TtsStatus.Unavailable(
                        "Korean (ko-KR) voice pack is not installed on this device. Install Korean TTS in Android Settings."
                    )
                } else {
                    current.setSpeechRate(speechRate)
                    current.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            if (!isReleased) {
                                _ttsStatus.value = TtsStatus.Ready
                            }
                        }
                        override fun onStop(utteranceId: String?, interrupted: Boolean) {
                            if (!isReleased && _ttsStatus.value is TtsStatus.Speaking) {
                                _ttsStatus.value = TtsStatus.Ready
                            }
                        }
                        override fun onError(utteranceId: String?, errorCode: Int) {
                            if (!isReleased) {
                                _ttsStatus.value = TtsStatus.Unavailable("Audio playback failed (code $errorCode). Tap to retry.")
                            }
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            if (!isReleased) {
                                _ttsStatus.value = TtsStatus.Unavailable("Audio playback failed. Tap to retry.")
                            }
                        }
                    })
                    _ttsStatus.value = TtsStatus.Ready
                    val queued = pendingSpeakText
                    pendingSpeakText = null
                    if (!queued.isNullOrBlank()) {
                        speakKorean(queued)
                    }
                }
            } else {
                pendingSpeakText = null
                _ttsStatus.value = TtsStatus.Unavailable("Text-to-Speech engine unavailable on this device.")
            }
        }
        createdEngine = engine
        if (isReleased) {
            runCatching { engine.stop() }
            runCatching { engine.shutdown() }
        } else {
            tts = engine
        }
    }

    fun updateSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.6f, 1.4f)
        tts?.setSpeechRate(speechRate)
    }

    fun speakKorean(text: String) {
        if (isReleased || text.isBlank()) return
        val engine = tts
        val status = _ttsStatus.value
        if (engine == null || status is TtsStatus.Unavailable) {
            pendingSpeakText = text
            initTts()
            return
        }
        if (status is TtsStatus.Initializing) {
            pendingSpeakText = text
            return
        }
        pendingSpeakText = null
        engine.setSpeechRate(speechRate)
        _ttsStatus.value = TtsStatus.Speaking(text)
        val utteranceId = UUID.randomUUID().toString()
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            _ttsStatus.value = TtsStatus.Unavailable("Could not play Korean audio. Tap to retry.")
        }
    }

    fun stopSpeaking() {
        pendingSpeakText = null
        runCatching { tts?.stop() }
        if (_ttsStatus.value is TtsStatus.Speaking) {
            _ttsStatus.value = TtsStatus.Ready
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean = speechRecognitionService.isAvailable()

    fun isOnDeviceSpeechRecognitionAvailable(): Boolean = speechRecognitionService.isOnDeviceAvailable()

    // ==================== PRONUNCIATION LAB SPEECH RECOGNITION ====================

    /**
     * Starts Android [android.speech.SpeechRecognizer] configured specifically for Korean
     * pronunciation practice drills (fast silence detection + n-best results + live partials).
     */
    fun startPronunciationRecognition(shortWordDrill: Boolean = true) {
        if (isReleased) return
        stopPlayback()
        stopSpeaking()
        stopLocalRecorderIfActive()
        activeConsumer = RecognitionConsumer.PRONUNCIATION
        _pronunciationRecognitionState.value = SpeechRecognitionState.Preparing

        val config = SpeechRecognitionConfig(
            languageTag = "ko-KR",
            maxResults = 5,
            enablePartialResults = true,
            completeSilenceLengthMs = if (shortWordDrill) 1200L else 1800L,
            possiblyCompleteSilenceLengthMs = if (shortWordDrill) 900L else 1400L,
            minimumInputLengthMs = 450L
        )
        speechRecognitionService.startListening(config)
    }

    fun stopPronunciationRecognition() {
        if (activeConsumer == RecognitionConsumer.PRONUNCIATION) {
            speechRecognitionService.stopListening()
        }
    }

    fun resetPronunciationRecognition() {
        if (activeConsumer == RecognitionConsumer.PRONUNCIATION) {
            speechRecognitionService.cancel()
            activeConsumer = RecognitionConsumer.NONE
        }
        _pronunciationRecognitionState.value = SpeechRecognitionState.Idle
    }

    // ==================== LIVE CONVERSATION CAPTURE ====================

    /**
     * Starts ko-KR SpeechRecognizer (with local audio recorder fallback when STT service is absent)
     * for Live Conversation turns.
     */
    fun startKoreanListening() {
        if (isReleased) return
        stopPlayback()
        stopSpeaking()
        cleanupTempAudio()
        stopLocalRecorderIfActive()
        recordedAmplitudes.clear()
        elapsedSeconds = 0
        activeConsumer = RecognitionConsumer.CONVERSATION

        if (!speechRecognitionService.isAvailable()) {
            val recorded = startLocalRecorderOnly()
            if (!recorded) {
                _captureState.value = SpeechCaptureState.TechnicalFailure(
                    message = "Korean speech recognition service is unavailable on this device. You can type your Korean response directly.",
                    messageId = "Layanan pengenalan suara Korea tidak tersedia di perangkat ini. Anda dapat langsung mengetik jawaban Korea.",
                    audioFilePath = null,
                    durationSeconds = 0,
                    amplitudes = emptyList()
                )
            }
            return
        }

        val config = SpeechRecognitionConfig(
            languageTag = "ko-KR",
            maxResults = 5,
            enablePartialResults = true,
            completeSilenceLengthMs = 2000L,
            possiblyCompleteSilenceLengthMs = 1500L,
            minimumInputLengthMs = 600L
        )
        speechRecognitionService.startListening(config)
    }

    private fun startLocalRecorderOnly(): Boolean {
        var recorder: MediaRecorder? = null
        return try {
            val file = File(appContext.cacheDir, "baeum_rec_${System.currentTimeMillis()}.m4a")
            activeRecordingFile = file
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(appContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            _captureState.value = SpeechCaptureState.Listening(0, "", emptyList())
            startRecorderTicker()
            true
        } catch (_: Exception) {
            runCatching { recorder?.reset() }
            runCatching { recorder?.release() }
            mediaRecorder = null
            cleanupTempAudio()
            false
        }
    }

    fun stopListening() {
        stopRecorderTicker()
        val recorder = mediaRecorder
        if (recorder != null) {
            mediaRecorder = null
            try {
                recorder.stop()
            } catch (_: Exception) {
            } finally {
                runCatching { recorder.reset() }
                runCatching { recorder.release() }
            }
            _captureState.value = SpeechCaptureState.TechnicalFailure(
                message = "Audio recorded (${elapsedSeconds}s). Speech-to-text is unavailable on this device — play back your recording and type your Korean response below.",
                messageId = "Audio terekam (${elapsedSeconds} dtk). Pengenalan suara belum tersedia di perangkat ini — putar ulang rekaman dan ketik jawaban Korea di bawah.",
                audioFilePath = activeRecordingFile?.absolutePath,
                durationSeconds = elapsedSeconds.coerceAtLeast(1),
                amplitudes = recordedAmplitudes.toList()
            )
            return
        }

        if (activeConsumer == RecognitionConsumer.CONVERSATION) {
            speechRecognitionService.stopListening()
        }
    }

    fun playLastRecording(path: String?) {
        if (isReleased || path.isNullOrBlank()) return
        val file = File(path)
        if (!file.exists()) return
        stopPlayback()
        var player: MediaPlayer? = null
        try {
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener { mp ->
                    _isPlayingRecording.value = false
                    runCatching { mp.release() }
                    if (mediaPlayer === mp) {
                        mediaPlayer = null
                    }
                }
                setOnErrorListener { mp, _, _ ->
                    _isPlayingRecording.value = false
                    runCatching { mp.release() }
                    if (mediaPlayer === mp) {
                        mediaPlayer = null
                    }
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = player
            _isPlayingRecording.value = true
        } catch (_: Exception) {
            runCatching { player?.release() }
            mediaPlayer = null
            _isPlayingRecording.value = false
        }
    }

    fun stopPlayback() {
        val player = mediaPlayer
        mediaPlayer = null
        if (player != null) {
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            runCatching { player.release() }
        }
        _isPlayingRecording.value = false
    }

    fun resetCapture() {
        stopRecorderTicker()
        stopPlayback()
        stopLocalRecorderIfActive()
        if (activeConsumer == RecognitionConsumer.CONVERSATION) {
            speechRecognitionService.cancel()
            activeConsumer = RecognitionConsumer.NONE
        }
        cleanupTempAudio()
        _captureState.value = SpeechCaptureState.Idle
    }

    private fun stopLocalRecorderIfActive() {
        stopRecorderTicker()
        val recorder = mediaRecorder
        mediaRecorder = null
        if (recorder != null) {
            runCatching { recorder.stop() }
            runCatching { recorder.reset() }
            runCatching { recorder.release() }
        }
    }

    private fun startRecorderTicker() {
        stopRecorderTicker()
        recorderTickerJob = controllerScope.launch {
            var tick = 0
            while (mediaRecorder != null && !isReleased) {
                delay(200L)
                tick++
                if (tick % 5 == 0) elapsedSeconds++
                val amp = runCatching { mediaRecorder?.maxAmplitude ?: 0 }.getOrDefault(0)
                val normalized = (amp / 16000f).coerceIn(0.08f, 1.0f)
                recordedAmplitudes.add(normalized)
                if (recordedAmplitudes.size > 36) recordedAmplitudes.removeAt(0)
                val cur = _captureState.value
                if (cur is SpeechCaptureState.Listening) {
                    _captureState.value = cur.copy(
                        elapsedSeconds = elapsedSeconds,
                        amplitudes = recordedAmplitudes.toList()
                    )
                }
            }
        }
    }

    private fun stopRecorderTicker() {
        recorderTickerJob?.cancel()
        recorderTickerJob = null
    }

    fun cleanupTempAudio() {
        try {
            activeRecordingFile?.let { if (it.exists()) it.delete() }
        } catch (_: Exception) {
        }
        activeRecordingFile = null
    }

    /**
     * Stops active audio playback, TTS, and microphone capture when the host Activity
     * enters the background without tearing down the reusable TTS engine.
     */
    fun onHostStop() {
        stopSpeaking()
        stopPlayback()
        stopLocalRecorderIfActive()
        if (activeConsumer != RecognitionConsumer.NONE) {
            speechRecognitionService.cancel()
            activeConsumer = RecognitionConsumer.NONE
        }
        if (_captureState.value is SpeechCaptureState.Listening) {
            _captureState.value = SpeechCaptureState.Idle
        }
        val pronState = _pronunciationRecognitionState.value
        if (pronState is SpeechRecognitionState.Preparing ||
            pronState is SpeechRecognitionState.Listening ||
            pronState is SpeechRecognitionState.Processing
        ) {
            _pronunciationRecognitionState.value = SpeechRecognitionState.Idle
        }
    }

    fun release() {
        isReleased = true
        pendingSpeakText = null
        resetCapture()
        resetPronunciationRecognition()
        shutdownTtsEngine()
        speechRecognitionService.destroy()
        controllerScope.cancel()
    }
}
