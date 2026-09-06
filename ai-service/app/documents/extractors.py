"""Turns a file on disk into raw extracted text, per format.

Each function takes only a Path and returns a str — no cleaning, no
chunking, no knowledge of client_id/document_id. That separation means
adding a new format later (e.g. .md, .html) is just one more function
and one more registry entry, nothing else in the pipeline changes.
"""

from pathlib import Path

from docx import Document as DocxDocument
from pypdf import PdfReader


def extract_text_from_txt(path: Path) -> str:
    # UTF-8 first; Latin-1 can decode any byte sequence at all, so it's a
    # fallback that never raises rather than one bad encoding crashing the
    # whole pipeline.
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return path.read_text(encoding="latin-1")


def extract_text_from_pdf(path: Path) -> str:
    reader = PdfReader(str(path))
    # extract_text() can return None for a page with no extractable text
    # (e.g. a scanned image page with no embedded text layer) — OCR would
    # be needed to handle that, which is out of scope here.
    pages = [page.extract_text() or "" for page in reader.pages]
    return "\n".join(pages)


def extract_text_from_docx(path: Path) -> str:
    document = DocxDocument(str(path))
    paragraphs = [paragraph.text for paragraph in document.paragraphs]
    return "\n".join(paragraphs)


_EXTRACTORS = {
    "pdf": extract_text_from_pdf,
    "txt": extract_text_from_txt,
    "docx": extract_text_from_docx,
}


def extract_text(path: Path) -> str:
    # Module 7's FileStorageService always names the stored file
    # {uuid}.{validated-extension}, so the extension on disk is trustworthy
    # even though the file's own display name (in the DB) isn't used here.
    extension = path.suffix.lower().lstrip(".")
    extractor = _EXTRACTORS.get(extension)
    if extractor is None:
        raise ValueError(f"Unsupported file type: .{extension}")
    return extractor(path)
