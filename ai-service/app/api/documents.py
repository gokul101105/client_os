from fastapi import APIRouter

from app.schemas import ProcessDocumentRequest, ProcessDocumentResponse

router = APIRouter()


@router.post("/process-document", response_model=ProcessDocumentResponse)
def process_document(request: ProcessDocumentRequest) -> ProcessDocumentResponse:
    # Stub only. A later module parses the file at file_path (app/documents),
    # chunks it, and generates embeddings (app/embeddings) for retrieval.
    return ProcessDocumentResponse(
        document_id=request.document_id,
        status="stub-received",
    )
