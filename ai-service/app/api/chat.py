from fastapi import APIRouter

from app.schemas import ChatRequest, ChatResponse

router = APIRouter()


@router.post("/chat", response_model=ChatResponse)
def send_message(request: ChatRequest) -> ChatResponse:
    # Stub only. A later module routes this through app/llm (the Claude
    # client) and, once app/rag exists, through client-scoped retrieved
    # context before the model ever sees the question.
    return ChatResponse(
        reply=f'[stub] Received your message for client {request.client_id}: "{request.message}"',
        conversation_id=request.conversation_id,
    )
