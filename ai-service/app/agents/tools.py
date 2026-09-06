"""The three data-gathering functions the meeting-brief agent calls.

Each is independent and side-effect-free -- pipeline.py calls all three
in a fixed order, none of them call each other or depend on another
tool's result. That independence is what would make them safe to run in
parallel later, even though pipeline.py currently calls them in sequence
for simplicity.
"""

from app.summary.retrieval import fetch_chunks_for_summary


def search_documents(client_id: int, max_chunks: int) -> list[dict]:
    # Reuses Module 13's broad, recency-ordered fetch -- NOT Module 11's
    # similarity search. Preparing for a meeting wants a broad sweep of
    # this client's recent content, not narrow similarity to one
    # question (there isn't one here).
    return fetch_chunks_for_summary(client_id, max_chunks=max_chunks)


def get_issues(open_issues_count: int) -> dict:
    # open_issues_count is passed in from Spring Boot (same pattern as
    # Module 15) -- Python never queries the clients table directly.
    # Honest limitation, same as Modules 14/15: no ticketing module
    # exists yet, so this is always 0 today.
    return {
        "open_issues_count": open_issues_count,
        "note": "No ticketing system exists yet in ClientOS; this count is a placeholder "
                "that is always 0 until one is built.",
    }


def get_meetings(client_id: int) -> dict:
    # Deliberate, honest stub: there is no meetings table anywhere in
    # ClientOS's schema (no meeting notes, no calendar integration,
    # nothing). This function exists so the orchestration sequence the
    # module asked for is structurally complete, but it never fabricates
    # meeting data -- it always returns this fixed result.
    return {
        "meetings": [],
        "note": "No meeting history is tracked in ClientOS yet -- this is a placeholder "
                "for a future meetings module.",
    }
