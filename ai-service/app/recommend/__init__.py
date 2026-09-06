"""Generates 3-5 action-oriented recommendations for a client (Module 15).

Combines signals Spring Boot already owns (health score, latest summary)
with a RAG retrieval step reused directly from app/rag/search.py -- the
query for that retrieval is synthetic (built from the client's own
identified problems), not a user-typed question, which is what makes this
a distinct package from app/rag rather than a variation of it.
"""
