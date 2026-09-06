"""Turns text chunks into vectors and persists them in Postgres/pgvector.

generator.py wraps the local embedding model (fastembed), db.py manages
the pgvector-aware Postgres connection, store.py writes rows into
document_chunks, and pipeline.py wires those three together. Similarity
search over these rows (the actual retrieval half of RAG) is Module 11's
job — this package stops at storage.
"""
