"""Wraps calls to the underlying LLM provider (Claude).

Will hold the API client wrapper, prompt templates, and model
configuration — the "how we talk to the model" layer that api/ routes
call into instead of calling Anthropic's SDK directly. Added starting
the module that implements real /ai/chat and /ai/summarize logic.
"""
