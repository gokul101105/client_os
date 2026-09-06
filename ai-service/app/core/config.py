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


settings = Settings()
