package com.jarvis.allinone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

enum class TaskType { CHAT, CODING, MOVIE_EDIT, FILE, WEB, DEVICE, AUTOMATION, CREATIVE, MEMORY }

data class Memory(val key: String, val value: String, val timestamp: Long = System.currentTimeMillis())

class MemoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("jarvis_memory", Context.MODE_PRIVATE)
    fun put(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun search(term: String): List<Memory> = prefs.all.mapNotNull { (k, v) ->
        val s = v as? String ?: return@mapNotNull null
        if (k.contains(term, true) || s.contains(term, true)) Memory(k, s) else null
    }
    fun all(): List<Memory> = prefs.all.mapNotNull { (k, v) -> (v as? String)?.let { Memory(k, it) } }
    fun clear() = prefs.edit().clear().apply()
}

class TaskRouter {
    fun classify(text: String): TaskType {
        val s = text.lowercase()
        return when {
            listOf("remember", "याद", "memory", "bhoolna mat").any(s::contains) -> TaskType.MEMORY
            listOf("python","java","kotlin","javascript","typescript","c++","html","css","sql","code","coding","debug","compile","project","program").any(s::contains) -> TaskType.CODING
            listOf("movie","video","subtitle","voice over","voiceover","clip","scene","edit video").any(s::contains) -> TaskType.MOVIE_EDIT
            listOf("file","folder","document","pdf").any(s::contains) -> TaskType.FILE
            listOf("search","web","internet","research","google").any(s::contains) -> TaskType.WEB
            listOf("remind","schedule","alarm","automate").any(s::contains) -> TaskType.AUTOMATION
            listOf("thumbnail","image","design").any(s::contains) -> TaskType.CREATIVE
            listOf("battery","wifi","settings","camera","phone").any(s::contains) -> TaskType.DEVICE
            else -> TaskType.CHAT
        }
    }
}

class JarvisTts(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale("hi", "IN")
    }
    fun speak(text: String) { tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-${System.currentTimeMillis()}") }
    fun shutdown() { tts.shutdown() }
}

class AiBridge(context: Context) {
    private val prefs = context.getSharedPreferences("jarvis_ai", Context.MODE_PRIVATE)
    var endpoint: String
        get() = prefs.getString("endpoint", "http://127.0.0.1:8080/v1/chat/completions") ?: "http://127.0.0.1:8080/v1/chat/completions"
        set(v) = prefs.edit().putString("endpoint", v).apply()
    var model: String
        get() = prefs.getString("model", "local") ?: "local"
        set(v) = prefs.edit().putString("model", v).apply()

    fun chat(prompt: String, memory: List<Memory>): String {
        return try {
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 60000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val system = "You are JARVIS, a helpful Android assistant. Be concise. Memory: " + memory.joinToString("; ") { it.value }
            val body = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().put(JSONObject().put("role", "system").put("content", system)).put(JSONObject().put("role", "user").put("content", prompt)))
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                ?: json.optString("response", "AI backend returned no text.")
        } catch (e: Exception) {
            "AI backend unavailable: ${e.message ?: "connection failed"}"
        }
    }
}

object DeviceActions {
    fun openUrl(context: Context, url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    fun openSettings(context: Context) = context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
}
