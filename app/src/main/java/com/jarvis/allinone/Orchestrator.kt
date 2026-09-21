package com.jarvis.allinone

import android.content.Context

data class JarvisRequest(val text: String, val source: String = "text")
data class JarvisResult(val text: String, val task: TaskType)

class JarvisOrchestrator(private val context: Context) {
    private val router = TaskRouter()
    private val memory = MemoryStore(context)

    fun handle(request: JarvisRequest): JarvisResult {
        val task = router.classify(request.text)
        val text = when (task) {
            TaskType.CODING -> "Coding task routed to Coding Agent."
            TaskType.MOVIE_EDIT -> "Movie task routed to Movie Agent."
            TaskType.FILE -> "File task routed to File Agent."
            TaskType.WEB -> "Web task routed to Web Agent."
            TaskType.AUTOMATION -> "Automation task routed to Automation Agent."
            TaskType.CREATIVE -> "Creative task routed to Image/Creative Agent."
            TaskType.DEVICE -> handleMemory(request.text)
            TaskType.CHAT -> "Chat task routed to AI Brain."
        }
        return JarvisResult(text, task)
    }

    private fun handleMemory(text: String): String {
        val lower = text.lowercase()
        if (lower.contains("remember") || text.contains("याद")) {
            val value = text
                .replace(Regex("(?i)remember that"), "")
                .replace("याद रखना", "")
                .trim()
            if (value.isNotBlank()) {
                memory.put("memory_${System.currentTimeMillis()}", value)
                return "Saved to JARVIS memory."
            }
        }
        return "Memory command received."
    }
}
