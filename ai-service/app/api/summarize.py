from fastapi import APIRouter

from app.schemas import SummarizeRequest, SummarizeResponse

router = APIRouter()


@router.post("/summarize", response_model=SummarizeResponse)
def summarize_document(request: SummarizeRequest) -> SummarizeResponse:
    # Stub only. A later module fetches the document's extracted text
    # (app/documents) and passes it to app/llm for a real summary.
    return SummarizeResponse(
        document_id=request.document_id,
        summary=f"[stub] Summary not yet generated for document {request.document_id}.",
    )
