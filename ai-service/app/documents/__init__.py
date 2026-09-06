"""Document ingestion pipeline: extraction, cleaning, and chunking.

extractors.py pulls raw text out of PDF/TXT/DOCX files, cleaning.py
normalizes it, chunking.py splits it into overlapping pieces, and
pipeline.py wires those three together. Embedding those chunks and
storing them in the document_chunks table is Module 10's job — this
package stops at producing chunk dicts in memory.
"""
