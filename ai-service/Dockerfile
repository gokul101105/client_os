# Module 18. Secrets (ANTHROPIC_API_KEY, INTERNAL_API_KEY, DATABASE_URL)
# are deliberately NOT copied into this image -- no .env file is baked
# in. They're supplied at container-start time via docker-compose's
# `environment:` block, so they never end up inside an image layer that
# could be inspected or pushed to a registry.
#
# Note for first run: fastembed downloads its embedding model (~130MB)
# from Hugging Face on first use, so this container needs outbound
# internet access the first time /ai/process-document (or anything that
# embeds text) actually runs. Subsequent runs reuse the cached model
# inside the container's filesystem.
FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app app
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
