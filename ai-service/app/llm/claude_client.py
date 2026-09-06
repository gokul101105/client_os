"""The only place in this service that talks to Claude directly.

Everything else (app/rag) builds a system prompt and a user message and
hands them here — this module knows nothing about retrieval, chunks, or
client_id, only "how do I call the Anthropic API."
"""

from functools import lru_cache

from anthropic import Anthropic, APIError

from app.core.config import settings


class LlmError(RuntimeError):
    """Raised when a Claude API call can't be completed (bad/missing key,
    network failure, rate limit, etc.)."""


@lru_cache(maxsize=1)
def _get_client() -> Anthropic:
    if not settings.anthropic_api_key:
        raise LlmError("ANTHROPIC_API_KEY is not set — see ai-service/.env.example")
    return Anthropic(api_key=settings.anthropic_api_key)


def generate_answer(system_prompt: str, user_content: str) -> str:
    client = _get_client()
    try:
        response = client.messages.create(
            model=settings.claude_model,
            max_tokens=settings.claude_max_tokens,
            system=system_prompt,
            messages=[{"role": "user", "content": user_content}],
        )
    except APIError as exc:
        raise LlmError(f"Claude API call failed: {exc}") from exc

    return "".join(block.text for block in response.content if block.type == "text")
