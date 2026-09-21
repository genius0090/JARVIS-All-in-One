package com.jarvis.allinone

data class AutomationRule(
    val id: String,
    val title: String,
    val triggerAtEpochMs: Long,
    val command: String,
    val enabled: Boolean = true
)

class AutomationStore(private val memory: MemoryStore) {
    fun save(rule: AutomationRule) {
        memory.put("automation_${rule.id}", "${rule.triggerAtEpochMs}|${rule.enabled}|${rule.command}")
    }
}
