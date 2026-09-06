"""Pydantic request/response models shared across the api/ routers.

Plays the same role as the dto/ package on the Spring Boot side: it's the
one place that defines the exact JSON shape Spring Boot and this service
agree on, independent of whatever internal logic eventually produces it.
"""

from typing import List, Optional

from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    # Present on every request in this file on purpose: once retrieval
    # exists, every lookup gets filtered by client_id so one client's
    # documents can never leak into another client's context.
    client_id: int = Field(..., description="The client this request is scoped to")
    message: str = Field(..., min_length=1)
    conversation_id: Optional[str] = None


class ChatResponse(BaseModel):
    reply: str
    conversation_id: Optional[str] = None
    # document_chunks row ids the answer was actually grounded in — empty
    # when no relevant context was found. Lets a caller show "sources" or
    # debug why an answer looks off, without exposing the raw vectors.
    source_chunk_ids: List[int] = []


class SummarizeRequest(BaseModel):
    client_id: int
    document_id: int


class SummarizeResponse(BaseModel):
    document_id: int
    summary: str


class ProcessDocumentRequest(BaseModel):
    client_id: int
    document_id: int
    file_path: str


class DocumentChunk(BaseModel):
    # id is the document_chunks row id, present once the chunk has
    # actually been stored.
    id: Optional[int] = None
    document_id: int
    client_id: int
    chunk_index: int
    content: str
    char_count: int
    # The vector itself (384 floats) isn't returned — nothing on the
    # Spring Boot side needs it, and 384 floats per chunk would make the
    # response unreadable. This just confirms an embedding was generated.
    embedding_dimensions: Optional[int] = None


class ProcessDocumentResponse(BaseModel):
    document_id: int
    client_id: int
    status: str
    chunk_count: int
    chunks: List[DocumentChunk]
