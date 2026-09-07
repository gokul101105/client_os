from fastapi import APIRouter, HTTPException

from app.llm.gemini_client import LlmError
from app.rag.pipeline import answer_question
from app.rag.search import RetrievalError
from app.schemas import ChatRequest, ChatResponse

router = APIRouter()


@router.post("/chat", response_model=ChatResponse)
def send_message(request: ChatRequest) -> ChatResponse:
    try:
        result = answer_question(request.client_id, request.message)
    except RetrievalError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except LlmError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

    return ChatResponse(
        reply=result["reply"],
        conversation_id=request.conversation_id,
        source_chunk_ids=result["source_chunk_ids"],
    )
