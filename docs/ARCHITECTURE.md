# Architecture

UI -> TaskRouter -> Agent layer -> Provider/tool layer -> persistent stores.

### Coding
CodingAgent is intentionally provider-neutral. Implementations can connect to a local
model or a remote model and must execute code only inside an explicit sandbox.

### Movie editing
MovieEditPlanner converts narration segments + scene candidates into edit decisions.
A production implementation should use timestamped transcription and semantic scene
matching rather than simple proximity.

### Memory
MemoryStore currently provides durable local storage. A production semantic memory
layer can add embeddings/indexing while retaining local privacy controls.

### Security
API keys should be stored in Android Keystore-backed storage or injected securely.
Never commit secrets into source control.
