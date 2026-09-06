from typing import Optional

from fastapi import Header, HTTPException, status

from app.core.config import settings


# Service-to-service auth: Spring Boot is the only caller of this API, so a
# single shared secret is enough — no user identity to validate here, that
# was already checked by Spring Security before this call was ever made.
# FastAPI maps the parameter name x_internal_api_key to the header
# "X-Internal-Api-Key" automatically.
#
# The header is declared optional (default None) so a missing key fails
# with the same 401 as a wrong one, instead of FastAPI's automatic 422 for
# a missing required header — callers shouldn't be able to distinguish
# "you forgot the key" from "you got it wrong".
async def verify_internal_api_key(x_internal_api_key: Optional[str] = Header(default=None)) -> None:
    if x_internal_api_key != settings.internal_api_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing internal API key",
        )
