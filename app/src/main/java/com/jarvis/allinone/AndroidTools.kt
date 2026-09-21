package com.jarvis.allinone

import android.content.Context
import android.content.Intent
import android.net.Uri

class OpenWebTool(private val context: Context) : JarvisTool {
    override val name = "open_web"
    override suspend fun run(request: ToolRequest): ToolResponse {
        val url = request.arguments["url"] ?: return ToolResponse(false, "URL missing")
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            ToolResponse(true, "Opened $url")
        } catch (e: Exception) {
            ToolResponse(false, e.message ?: "Unable to open URL")
        }
    }
}
