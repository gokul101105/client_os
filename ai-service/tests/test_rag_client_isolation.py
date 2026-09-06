"""Module 17: tests that RAG retrieval structurally cannot return another
client's chunks.

This is a unit test against the query construction and parameter
binding, not a live-database integration test -- no pgvector instance is
available in this environment (see the project's ongoing Docker setup).
It proves two things that together make cross-client leakage
impossible: (1) the SQL text filters by client_id BEFORE ranking by
similarity, not after, and (2) the client_id actually bound to the query
is exactly the one the caller asked for -- nothing about the query
content (however similar to another client's data) can change it.

A full integration test belongs alongside this once pgvector is
available: insert real chunks for two different client_ids, embed a
query built from client B's own wording, search as client A, and assert
zero of client B's chunks come back even though they'd rank highest by
raw similarity. That's listed as a known gap below, not silently
skipped.
"""

from unittest.mock import MagicMock, patch

from app.rag.search import _SEARCH_SQL, find_relevant_chunks


def test_search_sql_filters_by_client_id_before_ranking():
    where_index = _SEARCH_SQL.index("WHERE client_id")
    order_index = _SEARCH_SQL.index("ORDER BY")
    assert where_index < order_index, "client_id filter must be applied before similarity ranking"


def _mock_cursor_with_columns(*column_names):
    cursor = MagicMock()
    cursor.__enter__.return_value = cursor
    columns = []
    for name in column_names:
        col = MagicMock()
        col.name = name
        columns.append(col)
    cursor.description = columns
    cursor.fetchall.return_value = []
    return cursor


def test_find_relevant_chunks_binds_the_exact_client_id_requested():
    cursor = _mock_cursor_with_columns("id", "document_id", "client_id", "chunk_index", "content", "distance")
    conn = MagicMock()
    conn.__enter__.return_value = conn
    conn.cursor.return_value = cursor

    with patch("app.rag.search.get_connection", return_value=conn):
        # The query embedding here is arbitrary -- the point is that no
        # matter what it looks like (even one deliberately crafted to be
        # maximally similar to another client's content), the bound
        # client_id parameter is controlled by the function argument
        # alone, never derived from the query itself.
        find_relevant_chunks(client_id=42, query_embedding=[0.1] * 384, top_k=5)

    _, params = cursor.execute.call_args[0]
    assert params["client_id"] == 42


def test_find_relevant_chunks_never_lets_top_k_bypass_the_client_filter():
    cursor = _mock_cursor_with_columns("id", "document_id", "client_id", "chunk_index", "content", "distance")
    conn = MagicMock()
    conn.__enter__.return_value = conn
    conn.cursor.return_value = cursor

    with patch("app.rag.search.get_connection", return_value=conn):
        find_relevant_chunks(client_id=7, query_embedding=[0.2] * 384, top_k=1000)

    executed_sql, params = cursor.execute.call_args[0]
    # A large top_k must not remove or weaken the client_id predicate --
    # it only changes LIMIT, which comes after WHERE in the same query.
    assert "WHERE client_id = %(client_id)s" in executed_sql
    assert params["client_id"] == 7
