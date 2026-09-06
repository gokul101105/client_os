"""Fixed-sequence, multi-step orchestration (Module 16).

The one place in this service that gathers from more than one source
before a single Claude call. tools.py holds the three data-gathering
functions (search_documents, get_issues, get_meetings), prompt.py builds
the synthesis prompt, pipeline.py calls the tools in a fixed order --
decided by this Python code, not by Claude -- then synthesizes one final
result. Not an autonomous planning loop: see the Module 16 write-up for
why a fixed sequence is the right scope for this MVP.
"""
