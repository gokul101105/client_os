from fastapi import APIRouter, HTTPException

from app.documents.pipeline import build_chunks_for_document
from app.schemas import ProcessDocumentRequest, ProcessDocumentResponse

router = APIRouter()


@router.post("/process-document", response_model=ProcessDocumentResponse)
def process_document(request: ProcessDocumentRequest) -> ProcessDocumentResponse:
    try:
        chunks = build_chunks_for_document(
            client_id=request.client_id,
            document_id=request.document_id,
            file_path=request.file_path,
        )
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    # No DB write yet — Module 10 persists these chunks (with embeddings)
    # into document_chunks. For now the chunks themselves are the response.
    return ProcessDocumentResponse(
        document_id=request.document_id,
        client_id=request.client_id,
        status="processed",
        chunk_count=len(chunks),
        chunks=chunks,
    )
