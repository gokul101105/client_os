"""The only place in this service that talks to Gemini directly.

Everything else (app/rag, app/summary, app/recommend, app/agents) builds
a system prompt and a user message and hands them here -- those modules
know nothing about which LLM provider is behind generate_answer() /
generate_structured_output(), only their input/output contract. That's
what makes this file swappable: the provider changed (Claude -> Gemini),
nothing upstream of it did.
"""

import json
from functools import lru_cache

from google import genai
from google.genai import errors, types

from app.core.config import settings


class LlmError(RuntimeError):
    """Raised when a Gemini API call can't be completed (bad/missing key,
    network failure, rate limit, malformed structured output, etc.)."""


@lru_cache(maxsize=1)
def _get_client() -> genai.Client:
    if not settings.gemini_api_key:
        raise LlmError("GEMINI_API_KEY is not set — see ai-service/.env.example")
    return genai.Client(api_key=settings.gemini_api_key)


def generate_answer(system_prompt: str, user_content: str) -> str:
    client = _get_client()
    try:
        response = client.models.generate_content(
            model=settings.gemini_model,
            contents=user_content,
            config=types.GenerateContentConfig(
                system_instruction=system_prompt,
                max_output_tokens=settings.gemini_max_tokens,
            ),
        )
    except errors.APIError as exc:
        raise LlmError(f"Gemini API call failed: {exc}") from exc

    return response.text


def generate_structured_output(
    system_prompt: str,
    user_content: str,
    tool_name: str,
    tool_description: str,
    tool_schema: dict,
) -> dict:
    # tool_name/tool_description are vestigial from the Claude forced-
    # tool-use design this replaced (kept as parameters so every caller
    # in app/summary, app/recommend, app/agents needed zero changes).
    # Gemini's equivalent mechanism is response_schema + a JSON mime
    # type -- it constrains the *output shape* directly rather than
    # framing the request as "call this named tool", so there's no
    # analogous slot for a tool name/description here.
    client = _get_client()
    try:
        response = client.models.generate_content(
            model=settings.gemini_model,
            contents=user_content,
            config=types.GenerateContentConfig(
                system_instruction=system_prompt,
                max_output_tokens=settings.gemini_max_tokens,
                response_mime_type="application/json",
                response_schema=tool_schema,
            ),
        )
    except errors.APIError as exc:
        raise LlmError(f"Gemini API call failed: {exc}") from exc

    try:
        return json.loads(response.text)
    except (json.JSONDecodeError, TypeError) as exc:
        raise LlmError(f"Gemini did not return valid structured JSON: {exc}") from exc
