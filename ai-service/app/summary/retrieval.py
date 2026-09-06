"""Fetches a client's document chunks for summary generation.

Unlike app/rag/search.py, this is not a similarity search -- there is no
query to compare against for "summarize this client." It's a plain,
client-scoped fetch, capped and ordered so the most recently uploaded
content wins if there's more than fits in one prompt. No vector operator
(<=>) is used here -- this query doesn't need pgvector's similarity math,
though it still selects chunk_index, which (like the embedding column)
was added by the same V3 migration that installs the vector extension.
"""

import psycopg

from app.embeddings.db import get_connection

_FETCH_SQL = """
    SELECT id, document_id, chunk_index, content, created_at
    FROM document_chunks
    WHERE client_id = %(client_id)s
    ORDER BY created_at DESC, document_id, chunk_index
    LIMIT %(max_chunks)s
"""


class SummaryRetrievalError(RuntimeError):
    """Raised when fetching a client's chunks for summarization fails."""


def fetch_chunks_for_summary(client_id: int, max_chunks: int) -> list[dict]:
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute(_FETCH_SQL, {"client_id": client_id, "max_chunks": max_chunks})
                columns = [col.name for col in cur.description]
                return [dict(zip(columns, row)) for row in cur.fetchall()]
    except psycopg.Error as exc:
        raise SummaryRetrievalError(f"Failed to fetch chunks for summary: {exc}") from exc
