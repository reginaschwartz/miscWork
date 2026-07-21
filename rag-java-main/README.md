# RAG-JAVA-MAIN

Spring Boot port of the Python `rag-main` project. Provides the same RAG API using LangChain4j, OpenAI, and PostgreSQL + pgvector.

## What this project does

- Indexes text or PDF content into a PGVector table.
- Stores chunk metadata (including optional `context_tag`) for filtered retrieval.
- Answers questions by retrieving relevant chunks and sending them to an LLM.
- Supports API-based ingestion (`/index`) and optional bulk ingestion from `data/books/*.md`.

## Project structure

- `pom.xml` Maven build and dependencies
- `src/main/java/com/rag/main/controller/RagController.java` REST endpoints
- `src/main/java/com/rag/main/service/` indexing, querying, and vector store logic
- `src/main/java/com/rag/main/config/` Spring and LangChain4j beans
- `docker-compose.yml` app + pgvector Postgres services
- `init.sql` creates the `vector` extension in Postgres

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker and Docker Compose (for container workflow)
- OpenAI API key

## Environment variables

```bash
OPENAI_API_KEY=your_openai_key
PGVECTOR_COLLECTION=default

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=ragdb
POSTGRES_USER=raguser
POSTGRES_PASSWORD=ragpass
SERVER_PORT=8000
```

## Run with Docker (recommended)

```bash
export OPENAI_API_KEY=your_openai_key
docker compose up --build
```

API:

```text
http://localhost:8000
```

Swagger UI (FastAPI `/docs` equivalent):

```text
http://localhost:8000/docs
```

## Run locally (without Docker)

1. Start Postgres with pgvector and run `init.sql`.
2. Build and run:

```bash
mvn spring-boot:run
```

## API usage

### Index a document

```bash
curl -X POST "http://localhost:8000/index?reset_collection=true&context_tag=book&metadata_json=%7B%22source%22%3A%22alice_in_wonderland.md%22%7D" \
  -F "file=@data/books/alice_in_wonderland.md" \
  -H "accept: application/json"
```

### Query

```bash
curl -X POST "http://localhost:8000/query" \
  -H "Content-Type: application/json" \
  -d '{
    "query_text": "Who is Alice?",
    "k": 3,
    "min_relevance": 0.7,
    "context_tag": "book"
  }'
```

## Bulk index markdown files

Equivalent to Python `create_database.py`:

```bash
mvn spring-boot:run -Drag.bulk-index=true
```

This indexes `data/books/*.md` and resets the collection first.

## Python vs Java mapping

| Python | Java |
|--------|------|
| `api.py` | `RagController` |
| `models.py` | `dto/*` |
| `vector_store.py` | `VectorStoreService`, `RagConfig` |
| `create_database.py` | `IndexService`, `BulkIndexRunner` |
| `query_data.py` | `QueryService` |
