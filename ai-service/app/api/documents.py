from fastapi import APIRouter, HTTPException

from app.documents.pipeline import build_chunks_for_document
from app.embeddings.pipeline import embed_and_store_chunks
from app.embeddings.store import EmbeddingStorageError
from app.schemas import DocumentChunk, ProcessDocumentRequest, ProcessDocumentResponse

router = APIRouter()


@router.post("/process-document", response_model=ProcessDocumentResponse)
def process_document(request: ProcessDocumentRequest) -> ProcessDocumentResponse:
    try:
        chunks = build_chunks_for_document(
            client_id=request.client_id,
            document_id=request.document_id,
            file_path=request.file_path,
        )
        stored_chunks = embed_and_store_chunks(chunks)
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except EmbeddingStorageError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

    response_chunks = [
        DocumentChunk(
            id=chunk.get("id"),
            document_id=chunk["document_id"],
            client_id=chunk["client_id"],
            chunk_index=chunk["chunk_index"],
            content=chunk["content"],
            char_count=chunk["char_count"],
            embedding_dimensions=len(chunk["embedding"]),
        )
        for chunk in stored_chunks
    ]

    return ProcessDocumentResponse(
        document_id=request.document_id,
        client_id=request.client_id,
        status="processed",
        chunk_count=len(response_chunks),
        chunks=response_chunks,
    )
