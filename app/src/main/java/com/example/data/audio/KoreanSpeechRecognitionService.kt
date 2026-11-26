package com.example.data.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
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
import java.util.concurrent.atomic.AtomicLong

/**
 * Configuration options for Android's [SpeechRecognizer] targeting Korean (`ko-KR`).
 */
data class SpeechRecognitionConfig(
    val languageTag: String = "ko-KR",
    val maxResults: Int = 5,
    val enablePartialResults: Boolean = true,
    val completeSilenceLengthMs: Long = 1600L,
    val possiblyCompleteSilenceLengthMs: Long = 1200L,
    val minimumInputLengthMs: Long = 500L,
    val preferOffline: Boolean = false
)

enum class SpeechRecognitionErrorType {
    SERVICE_UNAVAILABLE,
    PERMISSION_DENIED,
    AUDIO_ERROR,
    NETWORK_ERROR,
    NO_MATCH,
    SPEECH_TIMEOUT,
    RECOGNIZER_BUSY,
    LANGUAGE_UNAVAILABLE,
    UNKNOWN
}

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState

    data object Preparing : SpeechRecognitionState

    data class Listening(
        val elapsedSeconds: Int = 0,
        val isSpeechDetected: Boolean = false,
        val partialTranscript: String = "",
        val rmsNormalized: Float = 0.08f,
        val amplitudes: List<Float> = emptyList()
    ) : SpeechRecognitionState

    data class Processing(
        val elapsedSeconds: Int = 0,
        val lastPartialTranscript: String = "",
        val amplitudes: List<Float> = emptyList()
    ) : SpeechRecognitionState

    data class Result(
        val primaryTranscript: String,
        val alternatives: List<String>,
        val confidenceScores: List<Float>,
        val durationSeconds: Int,
        val amplitudes: List<Float>
    ) : SpeechRecognitionState

    data class Error(
        val errorType: SpeechRecognitionErrorType,
        val errorCode: Int,
        val messageEn: String,
        val messageId: String,
        val durationSeconds: Int = 0,
        val amplitudes: List<Float> = emptyList()
    ) : SpeechRecognitionState {
        fun localizedMessage(isIndonesian: Boolean): String = if (isIndonesian) messageId else messageEn
    }
}

/**
 * Dedicated Android Speech Recognition service built on [SpeechRecognizer] for Korean (`ko-KR`).
 *
 * Guarantees:
 * - All [SpeechRecognizer] calls execute strictly on [Dispatchers.Main].
 * - Stale callbacks from previously cancelled sessions are ignored via a monotonic session ID.
 * - Captures live RMS waveform levels, partial Korean transcripts, n-best alternatives, and
 *   engine-provided confidence arrays (never misrepresenting STT confidence as acoustic pitch/phonetic accuracy).
 */
class KoreanSpeechRecognitionService(context: Context) {

    private val appContext: Context = context.applicationContext
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var speechRecognizer: SpeechRecognizer? = null
    private var tickerJob: Job? = null
    private val sessionCounter = AtomicLong(0L)
    private var activeSessionId: Long = 0L

    private val amplitudesBuffer = ArrayDeque<Float>(MAX_AMPLITUDE_BARS)
    private var elapsedSeconds: Int = 0
    private var lastPartialText: String = ""

    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    fun isAvailable(): Boolean = runCatching {
        SpeechRecognizer.isRecognitionAvailable(appContext)
    }.getOrDefault(false)

    fun isOnDeviceAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        return runCatching {
            SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)
        }.getOrDefault(false)
    }

    fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Starts a Korean (`ko-KR`) speech recognition session using Android's [SpeechRecognizer].
     */
    fun startListening(config: SpeechRecognitionConfig = SpeechRecognitionConfig()) {
        serviceScope.launch {
            if (!hasAudioPermission()) {
                _state.value = SpeechRecognitionState.Error(
                    errorType = SpeechRecognitionErrorType.PERMISSION_DENIED,
                    errorCode = SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
                    messageEn = "Microphone permission is required for Korean speech recognition.",
                    messageId = "Izin mikrofon diperlukan untuk pengenalan suara bahasa Korea."
                )
                return@launch
            }

            if (!isAvailable()) {
                _state.value = SpeechRecognitionState.Error(
                    errorType = SpeechRecognitionErrorType.SERVICE_UNAVAILABLE,
                    errorCode = -1,
                    messageEn = "Android Speech Recognition service is unavailable on this device.",
                    messageId = "Layanan Pengenalan Suara Android tidak tersedia di perangkat ini."
                )
                return@launch
            }

            val sessionId = sessionCounter.incrementAndGet()
            activeSessionId = sessionId
            stopTicker()
            amplitudesBuffer.clear()
            elapsedSeconds = 0
            lastPartialText = ""
            _state.value = SpeechRecognitionState.Preparing

            try {
                destroyRecognizerInternal()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(createListener(sessionId))

                val intent = buildRecognizerIntent(config)
                _state.value = SpeechRecognitionState.Listening(
                    elapsedSeconds = 0,
                    isSpeechDetected = false,
                    partialTranscript = "",
                    rmsNormalized = 0.08f,
                    amplitudes = emptyList()
                )
                startTicker(sessionId)
                recognizer.startListening(intent)
            } catch (e: Exception) {
                stopTicker()
                destroyRecognizerInternal()
                _state.value = SpeechRecognitionState.Error(
                    errorType = SpeechRecognitionErrorType.UNKNOWN,
                    errorCode = -2,
                    messageEn = "Could not start Korean speech recognition. Please try again.",
                    messageId = "Tidak dapat memulai pengenalan suara Korea. Silakan coba lagi."
                )
            }
        }
    }

    /**
     * Signals the recognizer to finish listening and emit final recognition results.
     */
    fun stopListening() {
        serviceScope.launch {
            val cur = _state.value
            if (cur is SpeechRecognitionState.Listening) {
                _state.value = SpeechRecognitionState.Processing(
                    elapsedSeconds = elapsedSeconds,
                    lastPartialTranscript = lastPartialText,
                    amplitudes = amplitudesBuffer.toList()
                )
            }
            stopTicker()
            runCatching { speechRecognizer?.stopListening() }
        }
    }

    /**
     * Immediately cancels the active session without delivering results and resets state to [SpeechRecognitionState.Idle].
     */
    fun cancel() {
        serviceScope.launch {
            activeSessionId = sessionCounter.incrementAndGet()
            stopTicker()
            runCatching { speechRecognizer?.cancel() }
            amplitudesBuffer.clear()
            elapsedSeconds = 0
            lastPartialText = ""
            _state.value = SpeechRecognitionState.Idle
        }
    }

    /**
     * Resets any completed or error state back to [SpeechRecognitionState.Idle].
     */
    fun reset() {
        cancel()
    }

    /**
     * Releases all Android [SpeechRecognizer] resources and cancels internal coroutines.
     */
    fun destroy() {
        activeSessionId = sessionCounter.incrementAndGet()
        stopTicker()
        runCatching {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        }
        speechRecognizer = null
        serviceScope.cancel()
    }

    private fun destroyRecognizerInternal() {
        runCatching {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        }
        speechRecognizer = null
    }

    private fun buildRecognizerIntent(config: SpeechRecognitionConfig): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, config.languageTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, config.languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, config.enablePartialResults)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, config.maxResults.coerceIn(1, 10))
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                config.completeSilenceLengthMs
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                config.possiblyCompleteSilenceLengthMs
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                config.minimumInputLengthMs
            )
            if (config.preferOffline) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }
    }

    private fun createListener(sessionId: Long): RecognitionListener = object : RecognitionListener {
        private fun isActive(): Boolean = activeSessionId == sessionId

        override fun onReadyForSpeech(params: Bundle?) {
            if (!isActive()) return
            val cur = _state.value
            if (cur !is SpeechRecognitionState.Listening) {
                _state.value = SpeechRecognitionState.Listening(
                    elapsedSeconds = elapsedSeconds,
                    isSpeechDetected = false,
                    partialTranscript = lastPartialText,
                    rmsNormalized = 0.10f,
                    amplitudes = amplitudesBuffer.toList()
                )
            }
        }

        override fun onBeginningOfSpeech() {
            if (!isActive()) return
            val cur = _state.value
            if (cur is SpeechRecognitionState.Listening) {
                _state.value = cur.copy(isSpeechDetected = true)
            }
        }

        override fun onRmsChanged(rmsdB: Float) {
            if (!isActive()) return
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1.0f)
            if (amplitudesBuffer.size >= MAX_AMPLITUDE_BARS) {
                amplitudesBuffer.removeFirst()
            }
            amplitudesBuffer.addLast(normalized)

            val cur = _state.value
            if (cur is SpeechRecognitionState.Listening) {
                _state.value = cur.copy(
                    rmsNormalized = normalized,
                    amplitudes = amplitudesBuffer.toList()
                )
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            if (!isActive()) return
            stopTicker()
            _state.value = SpeechRecognitionState.Processing(
                elapsedSeconds = elapsedSeconds,
                lastPartialTranscript = lastPartialText,
                amplitudes = amplitudesBuffer.toList()
            )
        }

        override fun onError(error: Int) {
            if (!isActive()) return
            stopTicker()
            val mapped = mapAndroidError(error)
            _state.value = SpeechRecognitionState.Error(
                errorType = mapped.type,
                errorCode = error,
                messageEn = mapped.messageEn,
                messageId = mapped.messageId,
                durationSeconds = elapsedSeconds,
                amplitudes = amplitudesBuffer.toList()
            )
        }

        override fun onResults(results: Bundle?) {
            if (!isActive()) return
            stopTicker()
            val matches = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.distinct()
                .orEmpty()

            val confidences = results
                ?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                ?.toList()
                .orEmpty()

            val primary = matches.firstOrNull().orEmpty()
            if (primary.isBlank()) {
                _state.value = SpeechRecognitionState.Error(
                    errorType = SpeechRecognitionErrorType.NO_MATCH,
                    errorCode = SpeechRecognizer.ERROR_NO_MATCH,
                    messageEn = "No clear Korean speech was detected. Try speaking closer to the microphone.",
                    messageId = "Suara bahasa Korea belum terdeteksi jelas. Coba bicara lebih dekat ke mikrofon.",
                    durationSeconds = elapsedSeconds,
                    amplitudes = amplitudesBuffer.toList()
                )
            } else {
                _state.value = SpeechRecognitionState.Result(
                    primaryTranscript = primary,
                    alternatives = matches,
                    confidenceScores = confidences,
                    durationSeconds = elapsedSeconds.coerceAtLeast(1),
                    amplitudes = amplitudesBuffer.toList()
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            if (!isActive()) return
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (partial.isNotBlank()) {
                lastPartialText = partial
                val cur = _state.value
                if (cur is SpeechRecognitionState.Listening) {
                    _state.value = cur.copy(
                        isSpeechDetected = true,
                        partialTranscript = partial
                    )
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun startTicker(sessionId: Long) {
        stopTicker()
        tickerJob = serviceScope.launch {
            var tick = 0
            while (activeSessionId == sessionId) {
                delay(200L)
                if (activeSessionId != sessionId) break
                tick++
                if (tick % 5 == 0) {
                    elapsedSeconds++
                    val cur = _state.value
                    if (cur is SpeechRecognitionState.Listening) {
                        _state.value = cur.copy(elapsedSeconds = elapsedSeconds)
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private data class MappedSpeechError(
        val type: SpeechRecognitionErrorType,
        val messageEn: String,
        val messageId: String
    )

    private fun mapAndroidError(error: Int): MappedSpeechError = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> MappedSpeechError(
            type = SpeechRecognitionErrorType.AUDIO_ERROR,
            messageEn = "Microphone audio error. Check that no other app is using the mic and retry.",
            messageId = "Terjadi kendala audio mikrofon. Pastikan tidak ada aplikasi lain memakai mikrofon lalu coba lagi."
        )
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> MappedSpeechError(
            type = SpeechRecognitionErrorType.NETWORK_ERROR,
            messageEn = "Network timeout during Korean speech recognition. Check your connection or install offline Korean speech data.",
            messageId = "Koneksi terputus saat pengenalan suara Korea. Periksa internet atau pasang paket suara Korea offline."
        )
        SpeechRecognizer.ERROR_NO_MATCH -> MappedSpeechError(
            type = SpeechRecognitionErrorType.NO_MATCH,
            messageEn = "No Korean speech matched clearly. Speak at a natural pace and try again.",
            messageId = "Ucapan bahasa Korea belum cocok dengan jelas. Ucapkan dengan tempo natural dan coba lagi."
        )
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> MappedSpeechError(
            type = SpeechRecognitionErrorType.SPEECH_TIMEOUT,
            messageEn = "Listening timed out before speech was detected. Tap Speak and say the phrase clearly.",
            messageId = "Waktu mendengarkan habis sebelum suara terdengar. Ketuk Bicara lalu ucapkan frasa dengan jelas."
        )
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> MappedSpeechError(
            type = SpeechRecognitionErrorType.PERMISSION_DENIED,
            messageEn = "Microphone permission is required for Korean voice recognition.",
            messageId = "Izin mikrofon diperlukan untuk pengenalan suara bahasa Korea."
        )
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> MappedSpeechError(
            type = SpeechRecognitionErrorType.RECOGNIZER_BUSY,
            messageEn = "Speech recognizer is busy. Wait a moment and tap Speak again.",
            messageId = "Layanan pengenalan suara sedang sibuk. Tunggu sebentar lalu ketuk Bicara lagi."
        )
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> MappedSpeechError(
            type = SpeechRecognitionErrorType.LANGUAGE_UNAVAILABLE,
            messageEn = "Korean (ko-KR) speech recognition pack is unavailable on this device.",
            messageId = "Paket pengenalan suara Korea (ko-KR) belum tersedia di perangkat ini."
        )
        else -> MappedSpeechError(
            type = SpeechRecognitionErrorType.UNKNOWN,
            messageEn = "Speech recognition could not complete (code $error). Please tap Retry.",
            messageId = "Pengenalan suara belum dapat diselesaikan (kode $error). Silakan coba lagi."
        )
    }

    companion object {
        private const val MAX_AMPLITUDE_BARS = 36
    }
}
