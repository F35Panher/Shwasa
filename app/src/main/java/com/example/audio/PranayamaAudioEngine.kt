package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.model.BeepTone
import com.example.model.StageType
import com.example.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface AudioFeedback {
    fun updateSettings(settings: UserSettings)
    fun playIntervalBeep()
    fun playStageTransition(stageType: StageType, spokenInstruction: String? = null)
    fun playKapalbhatiPulse()
    fun playCompletionChime(spokenSummary: String? = null)
    fun previewBeep(tone: BeepTone, frequencyHz: Int, volume: Float)
    fun release()
}

class PranayamaAudioEngine(context: Context) : AudioFeedback {

    private val appContext = context.applicationContext
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var settings = UserSettings()

    @Volatile
    private var ttsReady = false
    private var tts: TextToSpeech? = null
    private var lastCueTimestampMs = 0L
    private var activeToneJob: Job? = null

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.getDefault())
                    ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                    tts?.setSpeechRate(0.9f)
                    tts?.setPitch(0.95f)
                }
            }
        } catch (e: Exception) {
            Log.w("PranayamaAudio", "TTS initialization skipped: ${e.message}")
        }
    }

    override fun updateSettings(settings: UserSettings) {
        this.settings = settings
    }

    override fun playIntervalBeep() {
        if (!settings.soundEnabled) return
        val now = System.currentTimeMillis()
        // Prevent overlap if a stage transition or completion cue just played
        if (now - lastCueTimestampMs < 450L) return
        lastCueTimestampMs = now

        val baseFreq = settings.beepFrequencyHz.toDouble().coerceIn(220.0, 960.0)
        val vol = (settings.soundVolume * 0.45f).coerceIn(0.05f, 1f)
        playSoftTone(freqHz = baseFreq, durationMs = 110, volume = vol, decayRate = 12.0)
    }

    override fun playStageTransition(stageType: StageType, spokenInstruction: String?) {
        val now = System.currentTimeMillis()
        lastCueTimestampMs = now

        if (settings.soundEnabled) {
            val base = settings.beepFrequencyHz.toDouble().coerceIn(220.0, 960.0)
            val freq = when (stageType) {
                StageType.INHALE -> base * 1.125
                StageType.HOLD_IN, StageType.HOLD_OUT -> base
                StageType.EXHALE, StageType.HUMMING -> base * 0.85
                StageType.KAPALBHATI_ACTIVE -> base * 1.2
                StageType.REST -> base * 0.75
                StageType.CUSTOM -> base * 1.05
            }
            val duration = when (settings.beepTone) {
                BeepTone.SINGING_BOWL -> 380
                BeepTone.GENTLE_BELL -> 320
                BeepTone.SOFT_CHIME -> 240
                BeepTone.WOOD_BLOCK -> 140
            }
            val decay = when (settings.beepTone) {
                BeepTone.SINGING_BOWL -> 5.5
                BeepTone.GENTLE_BELL -> 7.0
                BeepTone.SOFT_CHIME -> 9.0
                BeepTone.WOOD_BLOCK -> 18.0
            }
            playSoftTone(
                freqHz = freq,
                durationMs = duration,
                volume = settings.soundVolume.coerceIn(0f, 1f),
                decayRate = decay
            )
        }

        if (settings.voiceGuidanceEnabled && !spokenInstruction.isNullOrBlank()) {
            speakText(spokenInstruction)
        }
    }

    override fun playKapalbhatiPulse() {
        if (!settings.soundEnabled) return
        val base = (settings.beepFrequencyHz.toDouble() * 0.8).coerceIn(220.0, 720.0)
        val vol = (settings.soundVolume * 0.35f).coerceIn(0.05f, 0.7f)
        playSoftTone(freqHz = base, durationMs = 75, volume = vol, decayRate = 24.0)
    }

    override fun playCompletionChime(spokenSummary: String?) {
        lastCueTimestampMs = System.currentTimeMillis()
        if (settings.soundEnabled) {
            val base = settings.beepFrequencyHz.toDouble().coerceIn(220.0, 880.0)
            val vol = settings.soundVolume.coerceIn(0f, 1f)
            activeToneJob?.cancel()
            activeToneJob = audioScope.launch {
                playToneBlocking(base, 260, vol * 0.8f, 6.0)
                playToneBlocking(base * 1.25, 340, vol * 0.85f, 5.0)
                playToneBlocking(base * 1.5, 520, vol * 0.9f, 4.0)
            }
        }
        if (settings.voiceGuidanceEnabled && !spokenSummary.isNullOrBlank()) {
            speakText(spokenSummary)
        }
    }

    override fun previewBeep(tone: BeepTone, frequencyHz: Int, volume: Float) {
        val duration = when (tone) {
            BeepTone.SINGING_BOWL -> 380
            BeepTone.GENTLE_BELL -> 320
            BeepTone.SOFT_CHIME -> 240
            BeepTone.WOOD_BLOCK -> 140
        }
        val decay = when (tone) {
            BeepTone.SINGING_BOWL -> 5.5
            BeepTone.GENTLE_BELL -> 7.0
            BeepTone.SOFT_CHIME -> 9.0
            BeepTone.WOOD_BLOCK -> 18.0
        }
        playSoftTone(
            freqHz = frequencyHz.toDouble().coerceIn(220.0, 960.0),
            durationMs = duration,
            volume = volume.coerceIn(0.05f, 1f),
            decayRate = decay
        )
    }

    private fun speakText(text: String) {
        if (!ttsReady) return
        try {
            tts?.setSpeechRate(0.92f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pranayama_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.w("PranayamaAudio", "TTS speak failed: ${e.message}")
        }
    }

    private fun playSoftTone(
        freqHz: Double,
        durationMs: Int,
        volume: Float,
        decayRate: Double
    ) {
        if (volume <= 0.01f) return
        activeToneJob?.cancel()
        activeToneJob = audioScope.launch {
            playToneBlocking(freqHz, durationMs, volume, decayRate)
        }
    }

    private fun playToneBlocking(
        freqHz: Double,
        durationMs: Int,
        volume: Float,
        decayRate: Double
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(256)
            val buffer = ShortArray(numSamples)
            val attackSamples = (sampleRate * 0.015).toInt().coerceAtLeast(1)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
                val decay = exp(-decayRate * t)
                val endFade = if (i > numSamples - attackSamples) {
                    (numSamples - i).toDouble() / attackSamples
                } else 1.0

                // Fundamental + subtle soft 2nd harmonic for warm chime quality
                val wave = 0.85 * sin(2.0 * PI * freqHz * t) + 0.15 * sin(2.0 * PI * (freqHz * 2.0) * t)
                val sample = (wave * attack * decay * endFade * volume * Short.MAX_VALUE * 0.65)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = sample.toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
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
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(maxOf(buffer.size * 2, minBuf))
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 30L)
            track.stop()
            track.release()
        } catch (_: InterruptedException) {
            // Cancelled cleanly when next cue triggers
        } catch (e: Exception) {
            Log.w("PranayamaAudio", "AudioTrack playback skipped: ${e.message}")
        }
    }

    override fun release() {
        activeToneJob?.cancel()
        audioScope.cancel()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
