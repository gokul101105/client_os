import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    # Shared secret Spring Boot sends as the X-Internal-Api-Key header.
    # The default only exists so local dev works out of the box — real
    # deployments must override this via the environment.
    internal_api_key: str = os.getenv("INTERNAL_API_KEY", "dev-internal-key")

    # Where uploaded documents live on disk, from this service's own
    # filesystem view. Default assumes both services run from their
    # conventional per-service directories on one machine — see
    # app/documents/paths.py.
    document_storage_dir: str = os.getenv("DOCUMENT_STORAGE_DIR", "../backend/storage/documents")

    # Chunking defaults (characters, not tokens — see app/documents/chunking.py).
    chunk_size: int = int(os.getenv("CHUNK_SIZE", "1000"))
    chunk_overlap: int = int(os.getenv("CHUNK_OVERLAP", "200"))

    # Postgres/pgvector connection — same database Spring Boot uses
    # (see docker/docker-compose.yml and backend/application.properties).
    database_url: str = os.getenv(
        "DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/clientos"
    )

    # Local embedding model (via fastembed). Must produce vectors matching
    # the vector(384) column in document_chunks — see
    # app/embeddings/generator.py and database/migrations/V3__document_embeddings.sql.
    embedding_model_name: str = os.getenv("EMBEDDING_MODEL", "BAAI/bge-small-en-v1.5")


settings = Settings()
