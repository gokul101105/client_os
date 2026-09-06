"""Persists chunk embeddings into the document_chunks table.

One row per chunk. client_id is written on every row on purpose — it's
what lets Module 11's retrieval filter `WHERE client_id = :id` and
structurally never return another client's chunks, no matter what the
similarity search itself finds. See the Module 10 write-up on why
forgetting this column is a real cross-client data leak, not a
theoretical one.
"""

import psycopg

from app.embeddings.db import get_connection

_INSERT_SQL = """
    INSERT INTO document_chunks (document_id, client_id, chunk_index, content, embedding)
    VALUES (%(document_id)s, %(client_id)s, %(chunk_index)s, %(content)s, %(embedding)s)
    RETURNING id
"""


class EmbeddingStorageError(RuntimeError):
    """Raised when chunk embeddings can't be written to Postgres."""


def store_chunk_embeddings(chunks: list[dict]) -> list[int]:
    if not chunks:
        return []

    inserted_ids: list[int] = []
    try:
        # `with get_connection() as conn:` commits the transaction on a
        # clean exit and rolls back automatically if an exception is
        # raised inside the block — psycopg3's default connection context
        # manager behavior, so a failure partway through never leaves half
        # a document's chunks written.
        with get_connection() as conn:
            with conn.cursor() as cur:
                for chunk in chunks:
                    cur.execute(
                        _INSERT_SQL,
                        {
                            "document_id": chunk["document_id"],
                            "client_id": chunk["client_id"],
                            "chunk_index": chunk["chunk_index"],
                            "content": chunk["content"],
                            "embedding": chunk["embedding"],
                        },
                    )
                    inserted_ids.append(cur.fetchone()[0])
    except psycopg.Error as exc:
        raise EmbeddingStorageError(f"Failed to store embeddings: {exc}") from exc

    return inserted_ids
