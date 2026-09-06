"""Connection helper for the embeddings package's Postgres/pgvector access.

A plain per-call connection, no pooling — appropriate at this project's
current traffic level. A connection pool (psycopg_pool) is the natural
upgrade if/when this becomes a bottleneck.
"""

import psycopg
from pgvector.psycopg import register_vector

from app.core.config import settings


def get_connection() -> psycopg.Connection:
    conn = psycopg.connect(settings.database_url)
    # Teaches this connection how to adapt a Python list[float] to/from
    # Postgres's `vector` type automatically, so callers just pass a plain
    # list as a query parameter.
    register_vector(conn)
    return conn
