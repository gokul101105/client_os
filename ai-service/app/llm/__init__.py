"""Wraps calls to Claude (Anthropic's Messages API).

claude_client.py is the only file that imports the Anthropic SDK — every
other module (including app/rag) calls generate_answer() with a plain
system prompt + user message string and never touches the SDK directly.
"""
