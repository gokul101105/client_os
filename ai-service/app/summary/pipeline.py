"""Orchestrates client summary generation: fetch chunks -> prompt -> Gemini
-> structured dict. The only function api/summarize.py calls into.
"""

from app.core.config import settings
from app.llm.gemini_client import generate_structured_output
from app.summary.prompt import SYSTEM_PROMPT, build_user_content
from app.summary.retrieval import fetch_chunks_for_summary

TOOL_NAME = "record_client_summary"

TOOL_SCHEMA = {
    "type": "object",
    "properties": {
        "company": {"type": "string", "description": "The client company's name, as mentioned in the documents."},
        "current_situation": {"type": "string", "description": "A short paragraph on where things stand with this client right now."},
        "major_problems": {
            "type": "array",
            "items": {"type": "string"},
            "description": "Distinct problems or complaints this client has raised.",
        },
        "recent_activity": {"type": "string", "description": "A short summary of recent activity or interactions."},
        "sentiment": {
            "type": "string",
            "enum": ["Positive", "Neutral", "Mixed", "Negative"],
            "description": "The overall tone of this client's recent communications.",
        },
        "attention_required": {
            "type": "boolean",
            "description": "Whether this client needs proactive attention from their account manager soon.",
        },
        "attention_reason": {
            "type": "string",
            "description": "Why attention is (or isn't) required -- one or two sentences.",
        },
    },
    "required": [
        "company",
        "current_situation",
        "major_problems",
        "recent_activity",
        "sentiment",
        "attention_required",
        "attention_reason",
    ],
}

# Returned without ever calling Gemini when a client has no processed
# documents yet -- there's nothing to summarize, and an LLM asked to
# summarize "(no documents available)" would either refuse unhelpfully or
# invent content neither behavior is useful here.
NO_DOCUMENTS_SUMMARY = {
    "company": None,
    "current_situation": "No documents have been uploaded for this client yet.",
    "major_problems": [],
    "recent_activity": "No activity recorded.",
    "sentiment": "Neutral",
    "attention_required": False,
    "attention_reason": "Not enough information to assess.",
}


def generate_client_summary(client_id: int, client_name: str, industry: str | None, plan: str) -> dict:
    chunks = fetch_chunks_for_summary(client_id, max_chunks=settings.summary_max_chunks)

    if not chunks:
        return dict(NO_DOCUMENTS_SUMMARY)

    user_content = build_user_content(client_name, industry, plan, chunks)
    return generate_structured_output(
        system_prompt=SYSTEM_PROMPT,
        user_content=user_content,
        tool_name=TOOL_NAME,
        tool_description="Records a structured summary of a client based on their uploaded documents.",
        tool_schema=TOOL_SCHEMA,
    )
