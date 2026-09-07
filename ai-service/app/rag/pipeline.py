"""Orchestrates the full RAG flow: question -> embed -> retrieve -> answer.

This is the only function api/chat.py calls into. Both "no relevant
context" cases are handled here, before Gemini is ever called — neither
is an error, both are legitimate answers that don't require the LLM.
"""

from app.core.config import settings
from app.embeddings.generator import generate_embeddings
from app.llm.gemini_client import generate_answer
from app.rag.prompt import SYSTEM_PROMPT, build_user_content
from app.rag.search import find_relevant_chunks

NO_DOCUMENTS_MESSAGE = "I don't have any processed documents for this client yet, so I can't answer that."
NO_RELEVANT_CONTEXT_MESSAGE = "I couldn't find anything in this client's documents relevant to that question."


def answer_question(client_id: int, question: str) -> dict:
    (query_embedding,) = generate_embeddings([question])

    chunks = find_relevant_chunks(client_id, query_embedding, top_k=settings.rag_top_k)

    if not chunks:
        # This client has zero processed chunks at all — distinct from
        # "chunks exist but none are relevant," so the user gets an
        # accurate reason rather than a generic non-answer.
        return {"reply": NO_DOCUMENTS_MESSAGE, "source_chunk_ids": []}

    best_distance = min(chunk["distance"] for chunk in chunks)
    if best_distance > settings.rag_max_distance:
        # Chunks exist, but even the closest one isn't actually close —
        # answering anyway risks the model politely hallucinating from
        # weakly related text instead of admitting it doesn't know.
        return {"reply": NO_RELEVANT_CONTEXT_MESSAGE, "source_chunk_ids": []}

    user_content = build_user_content(question, chunks)
    reply = generate_answer(SYSTEM_PROMPT, user_content)

    return {
        "reply": reply,
        "source_chunk_ids": [chunk["id"] for chunk in chunks],
    }
