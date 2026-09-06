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
    # client_id and metadata Spring Boot already has on hand from its own
    # Client entity -- Python never queries the clients table directly
    # (same boundary as everything else in this service: it only ever
    # touches document_chunks).
    client_id: int
    client_name: str
    industry: Optional[str] = None
    plan: str


class SummarizeResponse(BaseModel):
    client_id: int
    company: Optional[str] = None
    current_situation: str
    major_problems: List[str]
    recent_activity: str
    sentiment: str
    attention_required: bool
    attention_reason: str


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


class RecommendRequest(BaseModel):
    # Everything here is data Spring Boot already owns and fetched before
    # this call -- Python never queries clients/client_health/
    # client_summaries directly (same boundary held since Module 13).
    client_id: int
    client_name: str
    industry: Optional[str] = None
    plan: str
    health_score: Optional[int] = None
    health_band: Optional[str] = None
    health_breakdown_reasons: List[str] = []
    current_situation: Optional[str] = None
    major_problems: List[str] = []
    sentiment: Optional[str] = None
    attention_required: Optional[bool] = None
    attention_reason: Optional[str] = None


class Recommendation(BaseModel):
    action: str
    reason: str
    priority: str


class RecommendResponse(BaseModel):
    client_id: int
    recommendations: List[Recommendation]


class MeetingBriefRequest(BaseModel):
    client_id: int
    client_name: str
    industry: Optional[str] = None
    plan: str
    # Same honest placeholder as Modules 14/15: always 0 until a
    # ticketing module exists.
    open_issues_count: int = 0


class MeetingBrief(BaseModel):
    agenda_suggestions: List[str]
    key_context: str
    open_issues_to_address: List[str]
    risks_or_watchouts: List[str]


class MeetingBriefSources(BaseModel):
    documents_reviewed: int
    issues_data_available: bool
    meetings_data_available: bool


class MeetingBriefResponse(BaseModel):
    client_id: int
    brief: MeetingBrief
    sources_used: MeetingBriefSources
