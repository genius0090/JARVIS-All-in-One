package com.jarvis.allinone

/**
 * Movie editing domain layer.
 *
 * Pipeline:
 * 1. Import movie + voice-over
 * 2. Analyze voice-over transcript/timestamps
 * 3. Build scene candidates from the movie
 * 4. Match candidate scenes to narration
 * 5. Create an edit decision list
 * 6. Render with Media3 Transformer
 * 7. Add subtitles/BGM when enabled
 *
 * The Android UI and data model are ready for a concrete local/remote
 * transcription and semantic scene-analysis engine to be plugged in.
 */
data class Scene(val startMs: Long, val endMs: Long, val description: String = "")
data class NarrationSegment(val startMs: Long, val endMs: Long, val text: String)
data class EditDecision(val narration: NarrationSegment, val scene: Scene?)

class MovieEditPlanner {
    fun plan(narration: List<NarrationSegment>, scenes: List<Scene>): List<EditDecision> =
        narration.map { n ->
            val scene = scenes.minByOrNull { kotlin.math.abs(it.startMs - n.startMs) }
            EditDecision(n, scene)
        }
}
