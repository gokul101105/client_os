"""Wraps calls to Gemini (Google's Gen AI SDK).

gemini_client.py is the only file that imports the google-genai SDK --
every other module (app/rag, app/summary, app/recommend, app/agents)
calls generate_answer() / generate_structured_output() with a plain
system prompt + user message string and never touches the SDK directly.
Originally built against Claude (Anthropic); swapped to Gemini without
any caller needing to change, since both providers sit behind the same
two-function contract.
"""
