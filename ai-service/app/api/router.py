from fastapi import APIRouter, Depends

from app.api import chat, documents, summarize
from app.core.security import verify_internal_api_key

# Every /ai/* route requires the shared internal API key. /health (mounted
# directly on the app in main.py) deliberately stays outside this router so
# infra checks (Docker, a load balancer) can probe it without a secret.
api_router = APIRouter(prefix="/ai", dependencies=[Depends(verify_internal_api_key)])
api_router.include_router(chat.router)
api_router.include_router(summarize.router)
api_router.include_router(documents.router)
