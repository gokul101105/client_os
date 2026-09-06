"""Retrieval-augmented generation: query -> relevant chunks -> answer.

search.py runs the client-scoped pgvector similarity query, prompt.py
builds what actually gets sent to Claude, and pipeline.py wires those
together with the embedding step (app/embeddings) and the LLM call
(app/llm) into the single function api/chat.py calls.
"""
