# ClientOS

A client relationship platform with an AI assistant, built as a Spring Boot + React + PostgreSQL/pgvector + FastAPI/Claude system.

## Running the whole stack (Docker)

Requires Docker Desktop (or Docker Engine + Compose) installed.

```bash
# 1. From the repo root, create your local secrets file
cp .env.example .env
# then edit .env and fill in JWT_SECRET, INTERNAL_API_KEY, ANTHROPIC_API_KEY
# (see the comments in .env.example for how to generate each one)

# 2. Build and start everything
docker compose -f docker/docker-compose.yml up --build
```

That single command builds and starts all four services:

| Service | URL | Purpose |
|---|---|---|
| `frontend` | http://localhost:5173 | React app |
| `backend` | http://localhost:8080 | Spring Boot API |
| `ai-service` | http://localhost:8000 | FastAPI + Claude (internal-only in a real deployment — see below) |
| `postgres` | localhost:5432 | PostgreSQL + pgvector |

On first startup, Flyway applies every migration automatically, and `ai-service` downloads its local embedding model (~130MB) the first time it's actually used — that first document upload or chat message will be slower than subsequent ones.

Open http://localhost:5173, register a user, and go.

To stop everything: `Ctrl+C`, then `docker compose -f docker/docker-compose.yml down` (add `-v` to also wipe the database and uploaded-document volumes).

## Running without Docker (local development)

Each service can also run directly on your machine — see the setup notes in `backend/`, `ai-service/`, and `frontend/` respectively. This is how the project was built and tested module by module; Docker Compose is the packaged, "run it in one command" path for demos and deployment.

## Deployment note

For anything beyond a laptop demo, only the `frontend` (and optionally `backend`, if you want the API reachable directly) ports should ever be exposed publicly. `postgres` and `ai-service` should stay reachable only from inside the Docker network — the AI service in particular has no user-level authorization of its own (see Module 12/17's design notes); it trusts any caller holding the shared `INTERNAL_API_KEY`, which is only meant to be Spring Boot.
