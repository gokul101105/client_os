-- =========================================================
-- ClientOS — Module 13: Client Summary
-- =========================================================

-- Owned and written by Spring Boot (unlike document_chunks, which the AI
-- service writes directly) -- a generated client summary is ordinary
-- structured business data, the same category as clients/documents, not
-- infra tightly coupled to the embedding pipeline.
--
-- History is kept intentionally: every "Generate" click inserts a new
-- row rather than upserting one row per client. Simpler than upsert
-- logic, and lets sentiment/attention trend over time be shown later for
-- free.
CREATE TABLE client_summaries (
    id                  BIGSERIAL PRIMARY KEY,
    client_id           BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    company             VARCHAR(255),
    current_situation   TEXT NOT NULL,
    major_problems      JSONB NOT NULL DEFAULT '[]',
    recent_activity     TEXT NOT NULL,
    sentiment           VARCHAR(20) NOT NULL,
    attention_required  BOOLEAN NOT NULL DEFAULT FALSE,
    attention_reason    TEXT,
    generated_by        BIGINT NOT NULL REFERENCES users(id),
    generated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Shaped for the "fetch latest" access pattern: WHERE client_id = ?
-- ORDER BY generated_at DESC LIMIT 1.
CREATE INDEX idx_client_summaries_client_id_generated_at
    ON client_summaries (client_id, generated_at DESC);
