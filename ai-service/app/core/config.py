import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    # Shared secret Spring Boot sends as the X-Internal-Api-Key header.
    # The default only exists so local dev works out of the box — real
    # deployments must override this via the environment.
    internal_api_key: str = os.getenv("INTERNAL_API_KEY", "dev-internal-key")


settings = Settings()
