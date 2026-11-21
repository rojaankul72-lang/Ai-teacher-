package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

private const val TAG = "TeacherAudioEngine"
private const val PRIMARY_TTS_MODEL = "gemini-3.8-flash-tts"
private const val SECONDARY_TTS_MODEL = "gemini-3.1-flash-tts-preview"

enum class ActiveVoiceEngine(val label: String) {
    GEMINI_TTS("Gemini TTS ($PRIMARY_TTS_MODEL • Aoede)"),
    GEMINI_TTS_PREVIEW("Gemini TTS ($SECONDARY_TTS_MODEL • Aoede)"),
    ANDROID_DEVICE_TTS("Device Speech (Android TTS Fallback)"),
    TEXT_CAPTIONS_ONLY("Captions Only (Speech Unavailable)")
}

data class AudioPlaybackState(
    val isSpeaking: Boolean = false,
    val isMusicPlaying: Boolean = false,
    val isListening: Boolean = false,
    val activeVoiceEngine: ActiveVoiceEngine = ActiveVoiceEngine.GEMINI_TTS,
    val voiceStatusNote: String = "Connected: $PRIMARY_TTS_MODEL (with offline cache & device fallback)",
    val lastSpokenCaption: String = "",
    val recognizedTranscript: String = "",
    val speechConfidenceUncertain: Boolean = false,
    val speechError: String? = null
)

class TeacherAudioEngine(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val cacheDir = File(appContext.cacheDir, "tts_audio").apply { mkdirs() }

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private var currentNarrationVol = 0.9f
    private var currentMusicVol = 0.35f
    private var currentSpeed = 1.0f

    private var mediaPlayer: MediaPlayer? = null
    private var androidTts: TextToSpeech? = null
    private var androidTtsReady = false
    private var speakJob: Job? = null
    private var musicJob: Job? = null
    private var musicTrack: AudioTrack? = null
    private var speechRecognizer: SpeechRecognizer? = null

    @OptIn(PublicPreviewAPI::class)
    private val primaryTtsModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_TTS_MODEL,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.AUDIO)
                speechConfig = SpeechConfig(Voice("Aoede"))
            }
        )
    }

    @OptIn(PublicPreviewAPI::class)
    private val secondaryTtsModel by lazy {
        Firebase.ai.generativeModel(
            modelName = SECONDARY_TTS_MODEL,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.AUDIO)
                speechConfig = SpeechConfig(Voice("Aoede"))
            }
        )
    }

    init {
        initAndroidTts()
    }

    private fun initAndroidTts() {
        try {
            androidTts = TextToSpeech(appContext) { status ->
                androidTtsReady = (status == TextToSpeech.SUCCESS)
                if (androidTtsReady) {
                    androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _state.value = _state.value.copy(isSpeaking = true)
                            updateMusicDucking()
                        }

                        override fun onDone(utteranceId: String?) {
                            _state.value = _state.value.copy(isSpeaking = false)
                            updateMusicDucking()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _state.value = _state.value.copy(isSpeaking = false)
                            updateMusicDucking()
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Android TextToSpeech initialization failed", e)
        }
    }

    fun updateAudioControls(speed: Float, narrationVolume: Float, musicVolume: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        currentNarrationVol = narrationVolume.coerceIn(0f, 1f)
        currentMusicVol = musicVolume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.setVolume(currentNarrationVol, currentNarrationVol)
                    mp.playbackParams = mp.playbackParams.setSpeed(currentSpeed)
                }
            }
        } catch (_: Exception) {
        }
        updateMusicDucking()
    }

    fun speak(
        text: String,
        language: AppLanguage,
        preferGeminiTts: Boolean = true,
        isTenderMoment: Boolean = false
    ) {
        if (text.isBlank()) return
        stopNarration()
        _state.value = _state.value.copy(lastSpokenCaption = text, isSpeaking = true)
        updateMusicDucking()

        speakJob = scope.launch {
            val cacheKey = hashKey("${language.name}:$text")
            val cachedWav = File(cacheDir, "$cacheKey.wav")

            if (cachedWav.exists() && cachedWav.length() > 44L) {
                playWavFile(cachedWav, isTenderMoment)
                return@launch
            }

            if (preferGeminiTts) {
                val synthesized = trySynthesizeWithGeminiTts(text, language, cachedWav)
                if (synthesized != null) {
                    _state.value = _state.value.copy(
                        activeVoiceEngine = synthesized.first,
                        voiceStatusNote = "Connected: ${synthesized.first.label}"
                    )
                    playWavFile(synthesized.second, isTenderMoment)
                    return@launch
                }
            }

            // Honest fallback to Android TextToSpeech
            speakWithDeviceTts(text, language)
        }
    }

    fun preloadLine(text: String, language: AppLanguage) {
        if (text.isBlank()) return
        scope.launch(Dispatchers.IO) {
            val cacheKey = hashKey("${language.name}:$text")
            val cachedWav = File(cacheDir, "$cacheKey.wav")
            if (!cachedWav.exists()) {
                trySynthesizeWithGeminiTts(text, language, cachedWav)
            }
        }
    }

    private suspend fun trySynthesizeWithGeminiTts(
        text: String,
        language: AppLanguage,
        outputFile: File
    ): Pair<ActiveVoiceEngine, File>? {
        val styledPrompt = when (language) {
            AppLanguage.ENGLISH -> "Speak warmly and gently like an Indian female schoolteacher talking to a child: $text"
            AppLanguage.HINDI -> "बच्चों से प्यार और स्पष्ट उच्चारण के साथ एक भारतीय महिला शिक्षिका की तरह बोलें: $text"
            AppLanguage.ODIA -> "ଶିଶୁଙ୍କ ସହିତ ସ୍ନେହପୂର୍ଣ୍ଣ ଓ ସ୍ପଷ୍ଟ ଭାବେ ଜଣେ ଭାରତୀୟ ଶିକ୍ଷୟିତ୍ରୀଙ୍କ ପରି କୁହନ୍ତୁ: $text"
        }

        val attempts = listOf(
            ActiveVoiceEngine.GEMINI_TTS to primaryTtsModel,
            ActiveVoiceEngine.GEMINI_TTS_PREVIEW to secondaryTtsModel
        )

        for ((engine, model) in attempts) {
            try {
                val response = withTimeout(30_000L) {
                    model.generateContent(styledPrompt)
                }
                val audioPart = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.filterIsInstance<InlineDataPart>()
                    ?.firstOrNull { it.mimeType.startsWith("audio/") }

                if (audioPart != null && audioPart.inlineData.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        saveInlineAudioAsPlayableFile(
                            bytes = audioPart.inlineData,
                            mimeType = audioPart.mimeType,
                            outputFile = outputFile
                        )
                    }
                    return engine to outputFile
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Gemini TTS attempt failed for ${engine.label}: ${e.message}")
                _state.value = _state.value.copy(
                    voiceStatusNote = "Gemini TTS unavailable (${e.localizedMessage ?: "network/model"}); using Device Speech fallback"
                )
            }
        }
        return null
    }

    private fun saveInlineAudioAsPlayableFile(
        bytes: ByteArray,
        mimeType: String,
        outputFile: File
    ) {
        if (mimeType.contains("wav", ignoreCase = true) ||
            mimeType.contains("mpeg", ignoreCase = true) ||
            mimeType.contains("mp3", ignoreCase = true)
        ) {
            FileOutputStream(outputFile).use { it.write(bytes) }
            return
        }
        // Raw PCM (audio/L16;rate=24000 etc.) -> wrap in 44-byte WAV header
        val rateMatch = Regex("rate=(\\d+)").find(mimeType)
        val sampleRate = rateMatch?.groupValues?.get(1)?.toIntOrNull() ?: 24000
        val wavHeader = createWavHeader(bytes.size, sampleRate, channels = 1, bitsPerSample = 16)
        FileOutputStream(outputFile).use { fos ->
            fos.write(wavHeader)
            fos.write(bytes)
        }
    }

    private fun playWavFile(file: File, isTenderMoment: Boolean) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
                val vol = if (isTenderMoment) (currentNarrationVol * 0.85f) else currentNarrationVol
                setVolume(vol, vol)
                try {
                    playbackParams = playbackParams.setSpeed(currentSpeed)
                } catch (_: Exception) {
                }
                setOnCompletionListener {
                    _state.value = _state.value.copy(isSpeaking = false)
                    updateMusicDucking()
                }
                setOnErrorListener { _, _, _ ->
                    _state.value = _state.value.copy(isSpeaking = false)
                    updateMusicDucking()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed for cached TTS", e)
            _state.value = _state.value.copy(isSpeaking = false)
            updateMusicDucking()
        }
    }

    private fun speakWithDeviceTts(text: String, language: AppLanguage) {
        val tts = androidTts
        if (!androidTtsReady || tts == null) {
            _state.value = _state.value.copy(
                activeVoiceEngine = ActiveVoiceEngine.TEXT_CAPTIONS_ONLY,
                isSpeaking = false,
                voiceStatusNote = "Captions active (Device TTS engine not initialized)"
            )
            updateMusicDucking()
            return
        }
        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale("en", "IN")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.ODIA -> Locale("or", "IN")
        }
        val langResult = tts.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale("en", "IN"))
        }
        tts.setSpeechRate(currentSpeed)
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, currentNarrationVol)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        _state.value = _state.value.copy(
            activeVoiceEngine = ActiveVoiceEngine.ANDROID_DEVICE_TTS,
            isSpeaking = true
        )
        updateMusicDucking()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "ut_${System.currentTimeMillis()}")
    }

    fun stopNarration() {
        speakJob?.cancel()
        speakJob = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (_: Exception) {
        }
        try {
            androidTts?.stop()
        } catch (_: Exception) {
        }
        _state.value = _state.value.copy(isSpeaking = false)
        updateMusicDucking()
    }

    // Soft separate background music loop with automatic speech ducking
    fun startAmbientMusic() {
        if (musicJob?.isActive == true) return
        _state.value = _state.value.copy(isMusicPlaying = true)
        musicJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack ambient music unavailable", e)
                _state.value = _state.value.copy(isMusicPlaying = false)
                return@launch
            }

            musicTrack = track
            try {
                track.play()
                updateMusicDucking()
                val chunkSamples = sampleRate / 4
                val buffer = ShortArray(chunkSamples)
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0
                // Warm tanpura-inspired Sa-Pa-Sa drone (196 Hz, 293.66 Hz, 392 Hz)
                val f1 = 196.0
                val f2 = 293.66
                val f3 = 392.0
                while (isActive) {
                    for (i in buffer.indices) {
                        val s = (sin(phase1) * 0.45 + sin(phase2) * 0.30 + sin(phase3) * 0.15)
                        buffer[i] = (s * 2400).toInt().toShort()
                        phase1 += 2.0 * PI * f1 / sampleRate
                        phase2 += 2.0 * PI * f2 / sampleRate
                        phase3 += 2.0 * PI * f3 / sampleRate
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {
                }
                musicTrack = null
            }
        }
    }

    fun stopAmbientMusic() {
        musicJob?.cancel()
        musicJob = null
        _state.value = _state.value.copy(isMusicPlaying = false)
    }

    private fun updateMusicDucking() {
        val duckFactor = if (_state.value.isSpeaking) 0.2f else 1.0f
        val effectiveVol = (currentMusicVol * duckFactor).coerceIn(0f, 1f)
        try {
            musicTrack?.setVolume(effectiveVol)
        } catch (_: Exception) {
        }
    }

    // Soft musical note per lamp (Sa=261.6Hz, Ga=329.6Hz, Pa=392Hz) or chime
    fun playLampNote(lampIndex: Int) {
        val freqs = listOf(261.63, 329.63, 392.00, 523.25)
        playSoftBellTone(freqs[lampIndex.coerceIn(0, freqs.lastIndex)], durationMs = 280)
    }

    fun playWaterDropChime() {
        playSoftBellTone(587.33, durationMs = 220)
    }

    fun playCelebrationChime() {
        scope.launch(Dispatchers.Default) {
            playSoftBellToneBlocking(392.00, 140)
            playSoftBellToneBlocking(493.88, 140)
            playSoftBellToneBlocking(587.33, 260)
        }
    }

    private fun playSoftBellTone(freqHz: Double, durationMs: Int) {
        scope.launch(Dispatchers.Default) {
            playSoftBellToneBlocking(freqHz, durationMs)
        }
    }

    private fun playSoftBellToneBlocking(freqHz: Double, durationMs: Int) {
        val sampleRate = 22050
        val count = (sampleRate * durationMs) / 1000
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val env = (1.0 - i.toDouble() / count)
            val wave = sin(2.0 * PI * freqHz * i / sampleRate) * env
            samples[i] = (wave * 11000).toInt().toShort()
        }
        try {
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
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 40L)
            track.release()
        } catch (_: Exception) {
        }
    }

    // Two-way voice input: immediately stops teacher narration when child starts speaking!
    fun startChildVoiceInput(language: AppLanguage) {
        stopNarration()
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _state.value = _state.value.copy(
                isListening = false,
                speechError = "Device speech recognition is unavailable here—please type or tap a choice below."
            )
            return
        }
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = _state.value.copy(isListening = true, speechError = null)
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _state.value = _state.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        _state.value = _state.value.copy(
                            isListening = false,
                            speechError = "Could not catch that clearly—try again or type below."
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                        val top = matches?.firstOrNull().orEmpty()
                        val confidence = scores?.firstOrNull() ?: 0.8f
                        _state.value = _state.value.copy(
                            isListening = false,
                            recognizedTranscript = top,
                            speechConfidenceUncertain = top.isNotBlank() && confidence < 0.72f
                        )
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            _state.value = _state.value.copy(recognizedTranscript = partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.speechLocale)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isListening = false,
                speechError = "Microphone voice recognition failed: ${e.localizedMessage ?: "unavailable"}"
            )
        }
    }

    fun stopChildVoiceInput() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
        _state.value = _state.value.copy(isListening = false)
    }

    fun clearRecognizedTranscript() {
        _state.value = _state.value.copy(
            recognizedTranscript = "",
            speechConfidenceUncertain = false,
            speechError = null
        )
    }

    fun release() {
        stopNarration()
        stopAmbientMusic()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        try {
            androidTts?.shutdown()
        } catch (_: Exception) {
        }
        scope.cancel()
    }

    private fun hashKey(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.take(16).joinToString("") { "%02x".format(it) }
    }

    private fun createWavHeader(
        pcmDataSize: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val totalDataLen = pcmDataSize + 36
        val buffer = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat PCM = 1
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort((channels * bitsPerSample / 8).toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray())
        buffer.putInt(pcmDataSize)
        return buffer.array()
    }
}
