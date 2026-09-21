package com.jarvis.allinone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var speechResult: androidx.activity.result.ActivityResultLauncher<Intent>
    private var onSpeech: ((String) -> Unit)? = null
    private var pendingVoice: (() -> Unit)? = null
    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingVoice?.invoke()
        pendingVoice = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
                if (text.isNotBlank()) onSpeech?.invoke(text)
            }
        }
        val memory = MemoryStore(this)
        val tts = JarvisTts(this)
        val ai = AiBridge(this)
        setContent { JarvisApp(memory, tts, ai, ::startVoice) }
    }

    private fun startVoice(callback: (String) -> Unit) {
        onSpeech = callback
        val launch = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS listening…")
            }
            speechResult.launch(intent)
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingVoice = launch
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        } else launch()
    }
}

@Composable
private fun JarvisApp(
    memory: MemoryStore,
    tts: JarvisTts,
    ai: AiBridge,
    startVoice: ((String) -> Unit) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("JARVIS: Online. Voice, memory and AI bridge are ready.")) }
    var busy by remember { mutableStateOf(false) }
    var endpoint by remember { mutableStateOf(ai.endpoint) }
        val scope = rememberCoroutineScope()

    fun submit(q: String) {
        val text = q.trim()
        if (text.isEmpty() || busy) return
        messages = messages + "You: $text"
        input = ""
        val type = router.classify(text)
        if (type == TaskType.MEMORY) {
            val cleaned = text.replace(Regex("(?i)remember that|remember|याद रखना|याद रखो"), "").trim()
            if (cleaned.isNotBlank()) memory.put("memory_${System.currentTimeMillis()}", cleaned)
            val answer = "Memory saved."
            messages = messages + "JARVIS: $answer"
            tts.speak(answer)
            return
        }
        if (type == TaskType.WEB && (text.contains("http://") || text.contains("https://"))) {
            DeviceActions.openUrl(LocalContext.current, text.substringAfterLast(' '))
            return
        }
        busy = true
        scope.launch {
            val answer = withContext(Dispatchers.IO) { ai.chat(text, memory.all().takeLast(20)) }
            messages = messages + "JARVIS: $answer"
            tts.speak(answer)
            busy = false
        }
    }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("JARVIS") }) }) { pad ->
            Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) { items(messages) { Text(it, Modifier.padding(vertical = 7.dp)) } }
                if (busy) Text("JARVIS is thinking…")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(input, { input = it }, Modifier.fillMaxWidth(), placeholder = { Text("Ask JARVIS…") })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { submit(input) }, enabled = !busy) { Text("Send") }
                    OutlinedButton(onClick = { startVoice { input = it; submit(it) } }, enabled = !busy) { Text("🎙 Voice") }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(endpoint, { endpoint = it; ai.endpoint = it }, Modifier.fillMaxWidth(), label = { Text("AI endpoint") })
                Text("Memories: ${memory.all().size}", modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
