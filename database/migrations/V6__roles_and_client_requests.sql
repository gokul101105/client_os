-- =========================================================
-- ClientOS — Roles + client create/delete approval workflow
-- =========================================================

-- Role model: every existing user becomes ACCOUNT_MANAGER, except the
-- account already used to log into this app throughout development
-- (test@clientos.dev), which becomes the first ADMIN. There is no seed
-- script in this project -- every user so far was created by hand through
-- the (now-removed) self-registration endpoint -- so this is the only way
-- to end up with an Admin account at all once that endpoint is gone.
ALTER TABLE users
    ALTER COLUMN role SET DEFAULT 'ACCOUNT_MANAGER';

UPDATE users SET role = 'ACCOUNT_MANAGER' WHERE role <> 'ADMIN';
UPDATE users SET role = 'ADMIN' WHERE email = 'test@clientos.dev';

-- One row covers both CREATE and DELETE requests (see
-- ClientApprovalRequest.java for why): snapshot_* is populated for CREATE,
-- target_client_id for DELETE, and whichever type a row isn't stays null.
CREATE TABLE client_requests (
    id                BIGSERIAL PRIMARY KEY,
    type              VARCHAR(20) NOT NULL,               -- CREATE | DELETE
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by      BIGINT NOT NULL REFERENCES users(id),
    requested_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    decided_by        BIGINT REFERENCES users(id),
    decided_at        TIMESTAMP,
    decision_note     VARCHAR(500),

    -- CREATE snapshot (the proposed client -- not a real row yet)
    snapshot_name     VARCHAR(255),
    snapshot_industry VARCHAR(100),
    snapshot_plan     VARCHAR(50),

    -- DELETE target. ON DELETE SET NULL (not RESTRICT, the column's
    -- default) because approving this very request deletes the client it
    -- points at -- without SET NULL that delete would fail with a foreign
    -- key violation against this row. The request row survives as history;
    -- only the now-meaningless pointer to the deleted client goes null.
    target_client_id  BIGINT REFERENCES clients(id) ON DELETE SET NULL
);

CREATE INDEX idx_client_requests_status ON client_requests(status);
CREATE INDEX idx_client_requests_requested_by ON client_requests(requested_by);
