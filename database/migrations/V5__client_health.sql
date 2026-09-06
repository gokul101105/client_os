-- =========================================================
-- ClientOS — Module 14: Client Health Score
-- =========================================================

-- History kept, same reasoning as client_summaries (V4): plain INSERT is
-- simpler than upsert, and lets score-over-time be shown later for free.
-- breakdown stores an array of {signal, points, reason} objects so a UI
-- can show exactly why a score is what it is, not just the number.
CREATE TABLE client_health (
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    score       INTEGER NOT NULL,
    band        VARCHAR(30) NOT NULL,
    breakdown   JSONB NOT NULL,
    computed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_client_health_client_id_computed_at
    ON client_health (client_id, computed_at DESC);
