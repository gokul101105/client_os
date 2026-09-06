"""Resolves a document's stored relative path against the shared disk root.

Spring Boot and this service currently share one filesystem (see the
Module 9 write-up on why `file_path` — not bytes or a URL — is what
crosses the wire). That assumption only holds for local/single-host
deployments; moving storage off local disk later replaces this module,
nothing upstream of it.
"""

from pathlib import Path

from app.core.config import settings


def resolve_document_path(relative_path: str) -> Path:
    base_dir = Path(settings.document_storage_dir).resolve()
    candidate = (base_dir / relative_path).resolve()

    # Defense in depth, mirroring FileStorageService's containment check on
    # the Spring Boot side: even though relative_path comes from a trusted
    # caller (Spring Boot, authenticated via the internal API key), never
    # let a malformed or malicious path escape the storage root.
    if not candidate.is_relative_to(base_dir):
        raise ValueError("Resolved path escapes the storage root")

    if not candidate.is_file():
        raise FileNotFoundError(f"No document found at {relative_path}")

    return candidate
