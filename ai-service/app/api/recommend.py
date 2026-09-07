from fastapi import APIRouter, HTTPException

from app.llm.gemini_client import LlmError
from app.rag.search import RetrievalError
from app.recommend.pipeline import generate_recommendations
from app.schemas import Recommendation, RecommendRequest, RecommendResponse

router = APIRouter()


@router.post("/recommend", response_model=RecommendResponse)
def recommend(request: RecommendRequest) -> RecommendResponse:
    try:
        recommendations = generate_recommendations(
            client_id=request.client_id,
            client_name=request.client_name,
            industry=request.industry,
            plan=request.plan,
            health_score=request.health_score,
            health_band=request.health_band,
            health_breakdown_reasons=request.health_breakdown_reasons,
            current_situation=request.current_situation,
            major_problems=request.major_problems,
            sentiment=request.sentiment,
            attention_required=request.attention_required,
            attention_reason=request.attention_reason,
        )
    except RetrievalError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except LlmError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

    return RecommendResponse(
        client_id=request.client_id,
        recommendations=[Recommendation(**item) for item in recommendations],
    )
