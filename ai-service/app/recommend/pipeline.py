"""Orchestrates recommendation generation: build a retrieval query from
already-known problems -> reuse app/rag/search.py for supporting context
-> Gemini (structured output) -> structured recommendations.
"""

from typing import List, Optional

from app.core.config import settings
from app.embeddings.generator import generate_embeddings
from app.llm.gemini_client import generate_structured_output
from app.rag.search import find_relevant_chunks
from app.recommend.prompt import (
    SYSTEM_PROMPT,
    build_user_content,
    format_context,
    format_health,
    format_summary,
)

TOOL_NAME = "record_recommendations"

TOOL_SCHEMA = {
    "type": "object",
    "properties": {
        "recommendations": {
            "type": "array",
            "minItems": 3,
            "maxItems": 5,
            "items": {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "description": "A specific, concrete action to take -- an imperative instruction, not an observation.",
                    },
                    "reason": {
                        "type": "string",
                        "description": "Which input signal this is based on (a health factor, a summary field, or a document excerpt).",
                    },
                    "priority": {"type": "string", "enum": ["High", "Medium", "Low"]},
                },
                "required": ["action", "reason", "priority"],
            },
        },
    },
    "required": ["recommendations"],
}

# Keeps retrieval useful even before a summary has ever been generated
# for this client, when there are no identified problems to search for.
_FALLBACK_QUERY = "client risks, problems, dissatisfaction, renewal, engagement"


def _build_retrieval_query(major_problems: List[str], attention_reason: Optional[str]) -> str:
    parts = list(major_problems or [])
    if attention_reason:
        parts.append(attention_reason)
    return " ".join(parts) if parts else _FALLBACK_QUERY


def generate_recommendations(
    client_id: int,
    client_name: str,
    industry: Optional[str],
    plan: str,
    health_score: Optional[int],
    health_band: Optional[str],
    health_breakdown_reasons: List[str],
    current_situation: Optional[str],
    major_problems: List[str],
    sentiment: Optional[str],
    attention_required: Optional[bool],
    attention_reason: Optional[str],
) -> list[dict]:
    query_text = _build_retrieval_query(major_problems, attention_reason)
    (query_embedding,) = generate_embeddings([query_text])
    # Reused directly from Module 11 -- same client_id-scoped SQL, same
    # guarantee that this can never surface another client's chunks.
    chunks = find_relevant_chunks(client_id, query_embedding, top_k=settings.recommend_top_k)

    user_content = build_user_content(
        client_name,
        industry,
        plan,
        format_health(health_score, health_band, health_breakdown_reasons),
        format_summary(current_situation, major_problems, sentiment, attention_required, attention_reason),
        format_context(chunks),
    )

    result = generate_structured_output(
        system_prompt=SYSTEM_PROMPT,
        user_content=user_content,
        tool_name=TOOL_NAME,
        tool_description="Records 3-5 concrete recommended actions for this client.",
        tool_schema=TOOL_SCHEMA,
    )
    return result["recommendations"]
