from fastapi import APIRouter, HTTPException

from app.agents.pipeline import prepare_meeting_brief
from app.llm.claude_client import LlmError
from app.schemas import MeetingBriefRequest, MeetingBriefResponse
from app.summary.retrieval import SummaryRetrievalError

# Mounted under the /ai prefix (see router.py) with its own /agent
# sub-prefix, so the final path is /ai/agent/meeting-brief.
router = APIRouter(prefix="/agent")


@router.post("/meeting-brief", response_model=MeetingBriefResponse)
def meeting_brief(request: MeetingBriefRequest) -> MeetingBriefResponse:
    try:
        result = prepare_meeting_brief(
            client_id=request.client_id,
            client_name=request.client_name,
            industry=request.industry,
            plan=request.plan,
            open_issues_count=request.open_issues_count,
        )
    except SummaryRetrievalError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except LlmError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

    return MeetingBriefResponse(
        client_id=request.client_id,
        brief=result["brief"],
        sources_used=result["sources_used"],
    )
