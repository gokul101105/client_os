from fastapi import APIRouter, HTTPException

from app.llm.claude_client import LlmError
from app.schemas import SummarizeRequest, SummarizeResponse
from app.summary.pipeline import generate_client_summary
from app.summary.retrieval import SummaryRetrievalError

router = APIRouter()


@router.post("/summarize", response_model=SummarizeResponse)
def summarize_client(request: SummarizeRequest) -> SummarizeResponse:
    try:
        result = generate_client_summary(
            client_id=request.client_id,
            client_name=request.client_name,
            industry=request.industry,
            plan=request.plan,
        )
    except SummaryRetrievalError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except LlmError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

    return SummarizeResponse(client_id=request.client_id, **result)
