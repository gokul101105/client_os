-- =========================================================
-- ClientOS — Module 2: Database Design
-- Core tables: users, clients, documents, document_chunks
-- =========================================================

-- ---------------------------------------------------------
-- 1. USERS
-- Employees who log in and manage clients
-- ---------------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------
-- 2. CLIENTS
-- Companies managed by an employee (user)
-- ---------------------------------------------------------
CREATE TABLE clients (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    owner_id     BIGINT NOT NULL REFERENCES users(id),
    health_score INTEGER,                     -- populated later, Module 14
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clients_owner_id ON clients(owner_id);

-- ---------------------------------------------------------
-- 3. DOCUMENTS
-- Files uploaded for a specific client
-- ---------------------------------------------------------
CREATE TABLE documents (
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT NOT NULL REFERENCES clients(id),
    file_name   VARCHAR(255) NOT NULL,
    file_type   VARCHAR(50) NOT NULL,          -- PDF, TXT, DOCX
    file_path   VARCHAR(500) NOT NULL,
    uploaded_by BIGINT NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_client_id ON documents(client_id);

-- ---------------------------------------------------------
-- 4. DOCUMENT_CHUNKS
-- Split pieces of a document, prepared for embeddings (Module 10)
-- client_id is denormalized here on purpose: it lets RAG queries
-- filter chunks by client directly, without joining through
-- documents every time — this is what guarantees client isolation
-- at the query level (see Module 17: Security).
--
-- ON DELETE CASCADE on document_id: a chunk has no meaning without
-- its parent document, so deleting a document should always take
-- its chunks with it automatically.
-- ---------------------------------------------------------
CREATE TABLE document_chunks (
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    client_id   BIGINT NOT NULL REFERENCES clients(id),
    content     TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
    -- embedding VECTOR(1536) column is added in Module 10,
    -- once the pgvector extension is installed. Left out here
    -- to keep this module scoped to core relational design.
);

CREATE INDEX idx_chunks_client_id ON document_chunks(client_id);
CREATE INDEX idx_chunks_document_id ON document_chunks(document_id);
