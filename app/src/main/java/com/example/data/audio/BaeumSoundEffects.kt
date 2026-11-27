package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Zero-asset Korean Pentatonic (`궁상각치우`) tactile micro-sound synthesizer.
 *
 * Uses lightweight static PCM [AudioTrack] buffers synthesized on a single background audio worker
 * so UI feedback feels velvety and musical (marimba / singing-bowl timbre) without APK bloat or main-thread jank.
 * Gracefully no-ops in JVM unit tests or when muted in Settings.
 */
class BaeumSoundEffects {

    @Volatile
    var enabled: Boolean = true

    @Volatile
    private var released: Boolean = false

    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "baeum-sfx-synth").apply { isDaemon = true }
    }

    // Korean pentatonic scale ascending (C5, D5, E5, G5, A5, C6, D6, E6, G6)
    private val pentatonicHz = doubleArrayOf(
        523.25, 587.33, 659.25, 783.99, 880.00, 1046.50, 1174.66, 1318.51, 1567.98
    )

    fun playCardFlip() {
        if (!enabled || released) return
        enqueueToneSequence(
            notes = listOf(ToneNote(freqHz = 440.0, durationMs = 42, gain = 0.14)),
            softHarmonic = 0.12
        )
    }

    /**
     * Ascending pentatonic chime: pitch climbs naturally with every consecutive combo step!
     */
    fun playCorrect(combo: Int = 1) {
        if (!enabled || released) return
        val idx = (combo.coerceAtLeast(1) - 1).coerceAtMost(pentatonicHz.lastIndex)
        val root = pentatonicHz[idx]
        if (combo >= 5 && combo % 5 == 0) {
            // Luminous harmonic fifth spark on every 5-streak milestone
            enqueueToneSequence(
                notes = listOf(
                    ToneNote(freqHz = root, durationMs = 95, gain = 0.24),
                    ToneNote(freqHz = root * 1.498, durationMs = 150, gain = 0.26)
                ),
                softHarmonic = 0.28
            )
        } else {
            enqueueToneSequence(
                notes = listOf(ToneNote(freqHz = root, durationMs = 115, gain = 0.22)),
                softHarmonic = 0.24
            )
        }
    }

    /**
     * Warm muted low wood tap — gentle, never harsh or punishing.
     */
    fun playGentleMiss() {
        if (!enabled || released) return
        enqueueToneSequence(
            notes = listOf(
                ToneNote(freqHz = 220.0, durationMs = 85, gain = 0.18),
                ToneNote(freqHz = 196.0, durationMs = 110, gain = 0.14)
            ),
            softHarmonic = 0.08
        )
    }

    /**
     * Radiant 3-note Korean pentatonic arpeggio for Daily Spark, Daily Seal, or Dojang Stamp unlock.
     */
    fun playStampUnlock() {
        if (!enabled || released) return
        enqueueToneSequence(
            notes = listOf(
                ToneNote(freqHz = 523.25, durationMs = 85, gain = 0.22),
                ToneNote(freqHz = 659.25, durationMs = 90, gain = 0.24),
                ToneNote(freqHz = 1046.50, durationMs = 200, gain = 0.27)
            ),
            softHarmonic = 0.30
        )
    }

    /**
     * Celebratory 4-note ascending chime for Rank Level-Up or New Blitz Record.
     */
    fun playLevelUp() {
        if (!enabled || released) return
        enqueueToneSequence(
            notes = listOf(
                ToneNote(freqHz = 523.25, durationMs = 80, gain = 0.22),
                ToneNote(freqHz = 659.25, durationMs = 80, gain = 0.24),
                ToneNote(freqHz = 783.99, durationMs = 95, gain = 0.26),
                ToneNote(freqHz = 1318.51, durationMs = 240, gain = 0.28)
            ),
            softHarmonic = 0.32
        )
    }

    fun release() {
        released = true
        runCatching { executor.shutdownNow() }
    }

    private data class ToneNote(
        val freqHz: Double,
        val durationMs: Int,
        val gain: Double
    )

    private fun enqueueToneSequence(notes: List<ToneNote>, softHarmonic: Double) {
        if (released) return
        runCatching {
            executor.execute {
                if (released || !enabled) return@execute
                playPcmSequence(notes, softHarmonic)
            }
        }
    }

    private fun playPcmSequence(notes: List<ToneNote>, softHarmonic: Double) {
        val sampleRate = 24_000
        val totalSamples = notes.sumOf { (sampleRate * it.durationMs) / 1000 }
        if (totalSamples <= 0) return

        val pcm = ShortArray(totalSamples)
        var offset = 0
        for (note in notes) {
            val count = (sampleRate * note.durationMs) / 1000
            val attackSamples = (sampleRate * 0.008).toInt().coerceAtLeast(1)
            val omega1 = 2.0 * PI * note.freqHz / sampleRate
            val omega2 = omega1 * 2.0
            for (i in 0 until count) {
                val envAttack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
                val tNorm = i.toDouble() / count.coerceAtLeast(1)
                val envDecay = exp(-4.2 * tNorm)
                val wave = sin(omega1 * i) + softHarmonic * sin(omega2 * i)
                val sample = (wave * note.gain * envAttack * envDecay * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                pcm[offset + i] = sample.toShort()
            }
            offset += count
        }

        var track: AudioTrack? = null
        try {
            val byteLen = pcm.size * 2
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteLen)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            val sleepMs = (totalSamples * 1000L / sampleRate) + 25L
            Thread.sleep(sleepMs)
        } catch (_: Throwable) {
            // Silently ignore in headless/JVM test environments or audio focus restrictions
        } finally {
            runCatching { track?.stop() }
            runCatching { track?.release() }
        }
    }
}
