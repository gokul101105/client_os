"""Turns text into vectors using a local embedding model.

Runs fully offline via fastembed (ONNX Runtime under the hood) — no API
key, no per-call cost, no network dependency once the model has been
downloaded once. See the Module 10 write-up for why this was chosen over
a hosted embeddings API — independent of whichever LLM provider handles
chat/summary/recommendations (originally Claude, now Gemini); embeddings
were always a separate, local decision.
"""

from functools import lru_cache

from fastembed import TextEmbedding

from app.core.config import settings

# Must match the vector(384) column in document_chunks (see
# database/migrations/V3__document_embeddings.sql). Changing the model to
# one with a different output size means a new migration too.
EMBEDDING_DIMENSIONS = 384


@lru_cache(maxsize=1)
def _get_model() -> TextEmbedding:
    # Loading TextEmbedding reads the ONNX model into memory, which is far
    # too expensive to redo per request — cache one instance per process.
    return TextEmbedding(model_name=settings.embedding_model_name)


def generate_embeddings(texts: list[str]) -> list[list[float]]:
    if not texts:
        return []
    model = _get_model()
    # fastembed yields numpy arrays; pgvector's psycopg adapter and JSON
    # serialization both want plain Python floats.
    return [vector.tolist() for vector in model.embed(texts)]
