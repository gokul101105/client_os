"""Vector similarity search over document_chunks, hard-scoped to one client.

client_id sits in the SQL WHERE clause itself, evaluated before ranking —
not applied afterward in Python. That ordering is what makes it
architecturally impossible for this query to return another client's
row: non-matching rows are excluded before similarity is ever computed
against them, not filtered out of a result set that briefly held them.
"""

import psycopg
from pgvector import Vector

from app.embeddings.db import get_connection

# <=> is pgvector's cosine distance operator — chosen because it's what
# the HNSW index built in Module 10 (vector_cosine_ops) actually
# accelerates, and because cosine distance is the metric this embedding
# model was designed around (direction matters, magnitude doesn't).
# Lower distance = more similar; 0 is identical.
_SEARCH_SQL = """
    SELECT id, document_id, client_id, chunk_index, content,
           embedding <=> %(query_embedding)s AS distance
    FROM document_chunks
    WHERE client_id = %(client_id)s
    ORDER BY embedding <=> %(query_embedding)s
    LIMIT %(top_k)s
"""


class RetrievalError(RuntimeError):
    """Raised when the similarity search against Postgres fails."""


def find_relevant_chunks(client_id: int, query_embedding: list[float], top_k: int) -> list[dict]:
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    _SEARCH_SQL,
                    {
                        "client_id": client_id,
                        # register_vector only registers a dumper for
                        # pgvector.Vector (and numpy.ndarray) -- a plain
                        # Python list falls back to psycopg's default
                        # array adaptation (double precision[]), which
                        # <=> doesn't have an operator for. Wrapping it
                        # is what actually makes it a `vector` parameter.
                        "query_embedding": Vector(query_embedding),
                        "top_k": top_k,
                    },
                )
                columns = [col.name for col in cur.description]
                return [dict(zip(columns, row)) for row in cur.fetchall()]
    except psycopg.Error as exc:
        raise RetrievalError(f"Failed to search for relevant chunks: {exc}") from exc
