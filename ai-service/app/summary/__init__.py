"""Generates a structured, storable summary of a client (Module 13).

A new package rather than folding this into app/rag: summary retrieval
isn't a similarity search (there's no question to embed and compare
against), and its output is a fixed structured schema rather than free
text, so both the retrieval strategy and the Gemini call shape genuinely
differ from app/rag's single-turn Q&A. retrieval.py fetches chunks,
prompt.py builds what's sent to Gemini, pipeline.py wires them together
via app/llm's structured-output call.
"""
