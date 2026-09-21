# Automatic Movie Editing Pipeline

Input:
- full movie
- voice-over
- optional subtitle style
- optional BGM
- output aspect ratio

Stages:
1. Extract media metadata.
2. Transcribe voice-over with timestamps.
3. Detect shot/scene boundaries.
4. Generate scene descriptions/embeddings.
5. Match narration segments to relevant scenes.
6. Build an edit decision list.
7. Trim/crop/scale clips.
8. Replace or mix original audio.
9. Generate subtitles.
10. Render final 16:9 or 9:16 output.
11. Validate duration, audio, and output integrity.

The current source contains the domain model and planner contract; concrete AI/media
implementations are intentionally separated from the UI.
