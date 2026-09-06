"""Splits cleaned text into overlapping, retrieval-sized chunks.

Character-based rather than token-based on purpose: it needs no tokenizer
dependency and is trivial to reason about and test by hand. Module 10 can
swap this for a token-aware splitter later without anything upstream
(extraction, cleaning) needing to change.
"""


def chunk_text(text: str, chunk_size: int = 1000, overlap: int = 200) -> list[str]:
    if chunk_size <= 0:
        raise ValueError("chunk_size must be positive")
    if overlap < 0 or overlap >= chunk_size:
        raise ValueError("overlap must be >= 0 and smaller than chunk_size")

    chunks: list[str] = []
    start = 0
    text_length = len(text)

    while start < text_length:
        end = min(start + chunk_size, text_length)

        # Don't cut a word in half: back off to the last space in range,
        # unless this chunk already reaches the end of the text.
        if end < text_length:
            last_space = text.rfind(" ", start, end)
            if last_space > start:
                end = last_space

        piece = text[start:end].strip()
        if piece:
            chunks.append(piece)

        if end >= text_length:
            break

        # Step back by `overlap` so the next chunk repeats the tail of this
        # one — that's what keeps a sentence that straddles a chunk
        # boundary from being fully lost to either chunk. Then snap
        # forward to the next word boundary so the overlap itself doesn't
        # start mid-word.
        next_start = end - overlap if end - overlap > start else end
        if 0 < next_start < end:
            word_boundary = text.find(" ", next_start, end)
            if word_boundary != -1:
                next_start = word_boundary + 1
        start = next_start

    return chunks
