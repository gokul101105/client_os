"""Retrieval-augmented generation: query -> relevant chunks -> context.

Given a user question, fetches the most relevant document chunks (via
embeddings/ + a vector search) for the requesting client only, and
assembles them into the context passed to llm/. This is the layer
responsible for keeping one client's documents from ever leaking into
another client's answers. Added when RAG is implemented.
"""
