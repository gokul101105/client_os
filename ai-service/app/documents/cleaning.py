"""Normalizes raw extracted text before it gets chunked.

Extraction is lossy and messy by nature (PDFs especially): irregular
whitespace, stray control characters, and repeated header/footer lines
like page numbers. None of that is real document content, and leaving it
in would waste chunk space and pollute what gets embedded in Module 10.
"""

import re
import unicodedata

_MULTIPLE_BLANK_LINES = re.compile(r"\n\s*\n+")
_MULTIPLE_SPACES = re.compile(r"[ \t]+")
# Matches a line that is *only* a page marker: "12", "Page 3", "Page 3 of 10".
_PAGE_NUMBER_LINE = re.compile(r"^(page\s+)?\d+(\s*(of|/)\s*\d+)?$", re.IGNORECASE)


def clean_text(text: str) -> str:
    # Normalize unicode so visually-identical characters (curly vs.
    # straight quotes, ligatures) compare and chunk consistently.
    text = unicodedata.normalize("NFKC", text)

    # Drop control characters (null bytes, form feeds) that extraction
    # sometimes leaves behind, while keeping the newlines/tabs we need to
    # find line and paragraph breaks.
    text = "".join(
        ch for ch in text
        if ch in ("\n", "\t") or not unicodedata.category(ch).startswith("C")
    )

    kept_lines = []
    for raw_line in text.splitlines():
        stripped = raw_line.strip()
        if not stripped:
            kept_lines.append("")  # preserve the paragraph break, collapsed below
            continue
        if _PAGE_NUMBER_LINE.match(stripped):
            continue  # drop header/footer page-number lines
        kept_lines.append(_MULTIPLE_SPACES.sub(" ", stripped))

    text = "\n".join(kept_lines)
    text = _MULTIPLE_BLANK_LINES.sub("\n\n", text)
    return text.strip()
