"""Fixed-sequence orchestration for the meeting-brief agent.

The sequence below -- documents, then issues, then meetings, then one
synthesis call -- is always the same, always in this order, decided here
by plain Python, not by Claude choosing what to call. See the Module 16
write-up for why that's the appropriate scope for this MVP rather than
an autonomous planning loop.
"""

from app.agents.prompt import SYSTEM_PROMPT, build_user_content
from app.agents.tools import get_issues, get_meetings, search_documents
from app.core.config import settings
from app.llm.claude_client import generate_structured_output

TOOL_NAME = "record_meeting_brief"

TOOL_SCHEMA = {
    "type": "object",
    "properties": {
        "agenda_suggestions": {
            "type": "array",
            "items": {"type": "string"},
            "description": "Topics worth raising in the meeting.",
        },
        "key_context": {
            "type": "string",
            "description": "A short paragraph of what the account manager should know walking in.",
        },
        "open_issues_to_address": {
            "type": "array",
            "items": {"type": "string"},
            "description": "Specific open problems or complaints to bring up.",
        },
        "risks_or_watchouts": {
            "type": "array",
            "items": {"type": "string"},
            "description": "Things that could go wrong in this meeting or with this account.",
        },
    },
    "required": ["agenda_suggestions", "key_context", "open_issues_to_address", "risks_or_watchouts"],
}


def prepare_meeting_brief(
    client_id: int,
    client_name: str,
    industry: str | None,
    plan: str,
    open_issues_count: int,
) -> dict:
    # Step 1, 2, 3: independent data gathering, fixed order, no branching
    # on what any step returns.
    documents = search_documents(client_id, max_chunks=settings.agent_max_chunks)
    issues = get_issues(open_issues_count)
    meetings = get_meetings(client_id)

    # Step 4: the one and only synthesis call, after every source has
    # already been gathered.
    user_content = build_user_content(client_name, industry, plan, documents, issues, meetings)
    brief = generate_structured_output(
        system_prompt=SYSTEM_PROMPT,
        user_content=user_content,
        tool_name=TOOL_NAME,
        tool_description="Records a structured meeting-preparation brief for this client.",
        tool_schema=TOOL_SCHEMA,
    )

    return {
        "brief": brief,
        "sources_used": {
            "documents_reviewed": len(documents),
            "issues_data_available": False,  # honest: always False until a ticketing module exists
            "meetings_data_available": False,  # honest: always False, no meetings data source exists
        },
    }
