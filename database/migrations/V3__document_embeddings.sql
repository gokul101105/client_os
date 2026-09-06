-- =========================================================
-- ClientOS — Module 10: Embeddings + pgvector
-- =========================================================

-- pgvector adds the `vector` column type and the distance operators
-- (<->, <#>, <=>) that similarity search (Module 11) queries with.
-- Requires a Postgres build with the extension compiled in — see
-- docker/docker-compose.yml (pgvector/pgvector:pg16), since stock
-- postgres images don't include it.
CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE document_chunks
    -- 384 dimensions matches the local embedding model the AI service
    -- runs (BAAI/bge-small-en-v1.5, via fastembed) — chosen over a paid
    -- hosted API (e.g. OpenAI's 1536-dim text-embedding-3-small, guessed
    -- at in the original V1 migration comment before a model was
    -- actually picked) so embedding generation is free and works fully
    -- offline during development. This dimension is coupled to that
    -- model choice: changing the model later means a new migration.
    ADD COLUMN embedding vector(384),

    -- Position of this chunk within its source document — lets Module 11
    -- reconstruct reading order and cite results back to a specific
    -- location ("chunk 3 of 12"), and lets a chunk be regenerated in
    -- place without losing its ordering relative to its siblings.
    ADD COLUMN chunk_index INTEGER NOT NULL DEFAULT 0;

-- HNSW builds an incremental graph index with no training/clustering
-- step, so it works correctly on this empty table today — unlike
-- IVFFlat, which clusters vectors at build time and needs real data
-- present (and a `lists` value tuned to row count) to be effective.
-- vector_cosine_ops matches the <=> operator Module 11's retrieval
-- queries will use.
CREATE INDEX idx_document_chunks_embedding
    ON document_chunks
    USING hnsw (embedding vector_cosine_ops);
