package com.example.category3.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MorphicSpeechTranslator(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(
        onStatusChange: (String) -> Unit,
        onListeningStateChange: (Boolean) -> Unit,
        onTranslatingStateChange: (Boolean) -> Unit,
        onResultReceived: (String) -> Unit
    ) {
        cleanup()
        onStatusChange("INITIALIZING MULTI-LANG MIC...")
        onListeningStateChange(true)

        (context as? android.app.Activity)?.runOnUiThread {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    onStatusChange("SYSTEM ERROR: STT RUNTIME MISSING")
                    onListeningStateChange(false)
                    return@runOnUiThread
                }

                val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

                    // Default to listen for Tamil, but allow fallback recognition configurations
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN"))

                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    // Set to false for emulator testing; true for strict offline testing on physical phones
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)

                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            onStatusChange("► MIC ACTIVE - SPEAK IN TAMIL, HINDI, OR ENGLISH")
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            onListeningStateChange(false)
                            onTranslatingStateChange(true)
                            onStatusChange("AI: PROCESSING SPEECH STREAM...")
                        }

                        override fun onError(error: Int) {
                            onListeningStateChange(false)
                            onTranslatingStateChange(false)
                            onStatusChange("MIC ERROR CODE: $error")
                            cleanup()
                        }

                        override fun onResults(results: Bundle?) {
                            val spokenMatches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val transcribedText = spokenMatches?.get(0) ?: ""

                            if (transcribedText.isNotBlank()) {
                                // 🤖 MULTI-LANGUAGE DETECTOR ROUTING MATRIX
                                // Check if characters match Tamil or Hindi scripts, otherwise route as English
                                val sourceLanguage = when {
                                    transcribedText.any { it.code in 0x0B80..0x0BFF } -> TranslateLanguage.TAMIL
                                    transcribedText.any { it.code in 0x0900..0x097F } -> TranslateLanguage.HINDI
                                    else -> TranslateLanguage.ENGLISH
                                }

                                if (sourceLanguage == TranslateLanguage.ENGLISH) {
                                    // Already English, skip translation step completely
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
                                                    onResultReceived(translatedText) // Sets text box value to English
                                                    onStatusChange("")
                                                    onTranslatingStateChange(false)
                                                    cleanup()
                                                }
                                                .addOnFailureListener {
                                                    onResultReceived(transcribedText) // Fallback to raw script on translation failure
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
                onStatusChange("RECOVERY ERROR: ${e.localizedMessage}")
                onListeningStateChange(false)
                onTranslatingStateChange(false)
                cleanup()
            }
        }
    }

    fun cleanup() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}