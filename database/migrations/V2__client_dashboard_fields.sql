-- =========================================================
-- ClientOS — Module 5: Client Management (dashboard fields)
-- =========================================================

ALTER TABLE clients
    ADD COLUMN industry VARCHAR(100),

    -- FREE, PRO, ENTERPRISE — not enforced by a CHECK constraint yet,
    -- kept simple until real billing/plan logic exists.
    ADD COLUMN plan VARCHAR(50) NOT NULL DEFAULT 'FREE',

    -- Placeholder until an issue-tracking module exists.
    ADD COLUMN open_issues_count INTEGER NOT NULL DEFAULT 0,

    -- Placeholder until real activity tracking (documents, notes, etc.) exists.
    ADD COLUMN last_activity_date TIMESTAMP;
