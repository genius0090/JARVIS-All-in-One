package com.jarvis.allinone

data class ToolRequest(val name: String, val arguments: Map<String, String> = emptyMap())
data class ToolResponse(val success: Boolean, val message: String)

interface JarvisTool {
    val name: String
    suspend fun run(request: ToolRequest): ToolResponse
}

class ToolRegistry {
    private val tools = mutableMapOf<String, JarvisTool>()
    fun register(tool: JarvisTool) { tools[tool.name] = tool }
    fun get(name: String): JarvisTool? = tools[name]
    fun names(): List<String> = tools.keys.sorted()
}
