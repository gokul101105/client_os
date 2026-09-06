"""Orchestrates extract -> clean -> chunk for one document.

This is the only function api/documents.py calls into — it doesn't know
or care how extraction/cleaning/chunking work internally, just their
input/output contract. That's what lets each of those three pieces be
tested and changed independently.
"""

from app.core.config import settings
from app.documents.chunking import chunk_text
from app.documents.cleaning import clean_text
from app.documents.extractors import extract_text
from app.documents.paths import resolve_document_path


def build_chunks_for_document(client_id: int, document_id: int, file_path: str) -> list[dict]:
    resolved_path = resolve_document_path(file_path)
    raw_text = extract_text(resolved_path)
    cleaned_text = clean_text(raw_text)
    pieces = chunk_text(cleaned_text, chunk_size=settings.chunk_size, overlap=settings.chunk_overlap)

    # document_id and client_id are stamped onto every chunk here, not left
    # for the caller to attach later — the same shape Module 10 will persist
    # into the document_chunks table, whose client_id column exists
    # specifically to keep one client's chunks from ever being retrievable
    # by another.
    return [
        {
            "document_id": document_id,
            "client_id": client_id,
            "chunk_index": index,
            "content": piece,
            "char_count": len(piece),
        }
        for index, piece in enumerate(pieces)
    ]
