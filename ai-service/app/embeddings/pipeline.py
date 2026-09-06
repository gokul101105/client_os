"""Turns chunk dicts from app/documents into embedded, stored rows.

Deliberately the only place that knows about both subsystems:
app/documents produces plain chunk dicts with no idea embeddings exist,
and generator.py/store.py know nothing about text extraction. This
function is the seam between them.
"""

from app.embeddings.generator import generate_embeddings
from app.embeddings.store import store_chunk_embeddings


def embed_and_store_chunks(chunks: list[dict]) -> list[dict]:
    if not chunks:
        return []

    contents = [chunk["content"] for chunk in chunks]
    embeddings = generate_embeddings(contents)

    enriched_chunks = [
        {**chunk, "embedding": embedding}
        for chunk, embedding in zip(chunks, embeddings)
    ]

    inserted_ids = store_chunk_embeddings(enriched_chunks)
    for chunk, chunk_id in zip(enriched_chunks, inserted_ids):
        chunk["id"] = chunk_id

    return enriched_chunks
