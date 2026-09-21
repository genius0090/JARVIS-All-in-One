package com.jarvis.allinone

data class CodeTask(
    val language: String?,
    val instruction: String,
    val files: List<String> = emptyList()
)

data class CodeResult(
    val summary: String,
    val changedFiles: List<String> = emptyList(),
    val diagnostics: List<String> = emptyList()
)

/**
 * Provider-neutral coding-agent contract.
 * A real provider can implement this with a local model, remote API,
 * or a sandboxed execution service.
 */
interface CodingAgent {
    suspend fun execute(task: CodeTask): CodeResult
}

class LocalCodingAgent : CodingAgent {
    override suspend fun execute(task: CodeTask): CodeResult =
        CodeResult("Coding task received: ${task.instruction}")
}
