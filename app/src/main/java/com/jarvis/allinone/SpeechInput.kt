package com.jarvis.allinone

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechInput(private val context: Context) {
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun intent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
}
