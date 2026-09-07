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

    # Gemini (Module 11 RAG; originally built against Claude, swapped
    # later -- see app/llm/gemini_client.py). Empty by default -- the
    # client raises a clear error at call time rather than at import
    # time, so the service still starts fine without a key configured.
    gemini_api_key: str = os.getenv("GEMINI_API_KEY", "")
    gemini_model: str = os.getenv("GEMINI_MODEL", "gemini-3.6-flash")
    # 500 was fine for a short chat reply but too small for structured
    # output (recommendations/summary/meeting-brief return several
    # fields, some of them arrays) -- too low a limit truncates Gemini's
    # JSON mid-string, which fails to parse. Raised to 2048 for that reason,
    # then hit the same failure again on meeting-brief for a content-rich
    # client (it aggregates the most context of any feature -- health,
    # summary, and document chunks together -- so its output is the
    # largest). One shared limit across summarize/recommend/meeting-brief
    # by design; raised again rather than splitting a separate, higher
    # budget just for the agent.
    gemini_max_tokens: int = int(os.getenv("GEMINI_MAX_TOKENS", "4096"))

    # Retrieval tuning (Module 11). rag_max_distance is a starting
    # heuristic, not a derived constant — cosine-distance cutoffs for
    # "is this actually relevant" are dataset-dependent and should be
    # tuned against real usage, not trusted blindly.
    rag_top_k: int = int(os.getenv("RAG_TOP_K", "5"))
    rag_max_distance: float = float(os.getenv("RAG_MAX_DISTANCE", "0.6"))

    # Client summary generation (Module 13). A simple recency cap rather
    # than smarter selection (e.g. one chunk per document) -- see
    # app/summary/retrieval.py.
    summary_max_chunks: int = int(os.getenv("SUMMARY_MAX_CHUNKS", "40"))

    # Recommendations (Module 15). Independently tunable from rag_top_k --
    # supporting evidence for recommendations may want a different count
    # than chat answers.
    recommend_top_k: int = int(os.getenv("RECOMMEND_TOP_K", "5"))

    # Meeting-brief agent (Module 16). Reuses the same recency-cap
    # strategy as summary_max_chunks (see app/agents/tools.py).
    agent_max_chunks: int = int(os.getenv("AGENT_MAX_CHUNKS", "40"))


settings = Settings()
