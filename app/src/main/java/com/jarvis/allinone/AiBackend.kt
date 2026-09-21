package com.jarvis.allinone

data class AiMessage(val role: String, val content: String)

interface AiBackend {
    suspend fun chat(messages: List<AiMessage>): String
}

/**
 * Provider-neutral backend contract.
 * Configure a real provider implementation here without changing the UI/router.
 */
class ConfigurableAiBackend : AiBackend {
    override suspend fun chat(messages: List<AiMessage>): String {
        return "AI backend is not configured. Add a local or remote provider implementation."
    }
}
