package com.example.category3.utils

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MorphicSpeechTranslator(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalAudioMode = audioManager.mode

    /**
     * Start listening when the user PRESSES the mic button.
     */
    fun startListening(
        onStatusChange: (String) -> Unit,
        onListeningStateChange: (Boolean) -> Unit,
        onTranslatingStateChange: (Boolean) -> Unit,
        onResultReceived: (String) -> Unit
    ) {
        cleanup()

        // 🎧 NOISE HACK: Tell Android we are in a "Voice Call".
        // This forces the phone to activate hardware noise suppression and the bottom microphone.
        originalAudioMode = audioManager.mode
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        onStatusChange("INITIALIZING MULTI-LANG MIC...")
        onListeningStateChange(true)

        (context as? android.app.Activity)?.runOnUiThread {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    resetAudioMode()
                    onStatusChange("SYSTEM ERROR: STT RUNTIME MISSING")
                    onListeningStateChange(false)
                    return@runOnUiThread
                }

                val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN"))
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

                    // ⏱️ NOISE HACK: Shorten silence timeouts so it doesn't wait and listen to background noise
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            onStatusChange("► HOLD TO SPEAK - MIC ACTIVE")
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            onListeningStateChange(false)
                            onTranslatingStateChange(true)
                            onStatusChange("AI: PROCESSING SPEECH...")
                        }

                        override fun onError(error: Int) {
                            resetAudioMode()
                            onListeningStateChange(false)
                            onTranslatingStateChange(false)
                            onStatusChange("MIC ERROR CODE: $error")
                            cleanup()
                        }

                        override fun onResults(results: Bundle?) {
                            resetAudioMode()
                            val spokenMatches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val transcribedText = spokenMatches?.get(0) ?: ""

                            if (transcribedText.isNotBlank()) {
                                val sourceLanguage = when {
                                    transcribedText.any { it.code in 0x0B80..0x0BFF } -> TranslateLanguage.TAMIL
                                    transcribedText.any { it.code in 0x0900..0x097F } -> TranslateLanguage.HINDI
                                    else -> TranslateLanguage.ENGLISH
                                }

                                if (sourceLanguage == TranslateLanguage.ENGLISH) {
                                    onResultReceived(transcribedText)
                                    onStatusChange("")
                                    onTranslatingStateChange(false)
                                    cleanup()
                                } else {
                                    onStatusChange("TRANSLATING TO ENGLISH...")
                                    val options = TranslatorOptions.Builder()
                                        .setSourceLanguage(sourceLanguage)
                                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                                        .build()
                                    val translator = Translation.getClient(options)
                                    val conditions = DownloadConditions.Builder().build()

                                    translator.downloadModelIfNeeded(conditions)
                                        .addOnSuccessListener {
                                            translator.translate(transcribedText)
                                                .addOnSuccessListener { translatedText ->
                                                    onResultReceived(translatedText)
                                                    onStatusChange("")
                                                    onTranslatingStateChange(false)
                                                    cleanup()
                                                }
                                                .addOnFailureListener {
                                                    onResultReceived(transcribedText)
                                                    onStatusChange("TRANSLATION DICTIONARY FAIL")
                                                    onTranslatingStateChange(false)
                                                    cleanup()
                                                }
                                        }
                                        .addOnFailureListener {
                                            onResultReceived(transcribedText)
                                            onStatusChange("DOWNLOAD BLOCK: CONNECT TO WIFI")
                                            onTranslatingStateChange(false)
                                            cleanup()
                                        }
                                }
                            } else {
                                onTranslatingStateChange(false)
                                cleanup()
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                    startListening(speechIntent)
                }

            } catch (e: Exception) {
                resetAudioMode()
                onStatusChange("RECOVERY ERROR: ${e.localizedMessage}")
                onListeningStateChange(false)
                onTranslatingStateChange(false)
                cleanup()
            }
        }
    }

    /**
     * Call this when the user RELEASES the mic button.
     * This stops the mic instantly, ignoring background noise that happens after they finish talking.
     */
    fun stopListeningEarly() {
        try {
            speechRecognizer?.stopListening()
            resetAudioMode()
        } catch (e: Exception) {
            Log.e("MorphicSpeech", "Error stopping listener", e)
        }
    }

    private fun resetAudioMode() {
        // Return phone audio back to normal (so media/videos play correctly)
        audioManager.mode = originalAudioMode
    }

    fun cleanup() {
        try {
            resetAudioMode()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}