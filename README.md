# Mini Ticket Tracker

A small support-ticket tracker: **Spring Boot (Java 17) REST API**, **PostgreSQL**, **React (Vite) UI**.

```
mini-ticket-tracker/
├── docker-compose.yml        # PostgreSQL 16
├── backend/                  # Spring Boot API
│   └── src/main/java/com/lodwar/tickets/
│       ├── domain/           # Ticket entity + status/priority enums
│       ├── repository/       # Spring Data JPA
│       ├── service/          # business rules (validation, pagination, stats)
│       ├── dto/              # request/response records
│       └── web/              # controller + global error handler
└── frontend/                 # React single page
```

## Prerequisites
- JDK 17+ and Maven 3.9+
- Node 18+
- Docker (for PostgreSQL) — or any local Postgres with a `tickets` database/user

## Setup & run

```bash
# 1. Database
docker compose up -d

# 2. API  (http://localhost:8080) — Flyway creates the schema on startup
cd backend
mvn spring-boot:run

# 3. UI   (http://localhost:5173) — proxies /tickets to the API
cd frontend
npm install
npm run dev
```

DB settings can be overridden with `DB_URL`, `DB_USER`, `DB_PASSWORD`.



If port 8080 is already in use, run the API on another port and point the UI at it:

```bash
SERVER_PORT=8081 mvn spring-boot:run          # in backend
API_URL=http://localhost:8081 npm run dev     # in frontend
```

## Tests
```bash
cd backend
mvn test
```
Integration tests (MockMvc, full Spring context) run against in-memory H2 in PostgreSQL mode, so no database is needed. They cover create (+ validation), list filter/pagination, PATCH (+ 404 / empty body), and stats.

## API

| Method | Path | Notes |
|--------|------|-------|
| POST   | `/tickets` | `{ "title": "...", "description"?: "...", "priority"?: "low\|medium\|high" }` → `201`. Status starts as `open`; priority defaults to `medium`. |
| GET    | `/tickets?status=&page=&limit=` | `page` is 1-based (default 1), `limit` default 10, max 100. Newest first. Returns `{ items, page, limit, total, totalPages }`. |
| PATCH  | `/tickets/{id}` | `{ "status"?: ..., "priority"?: ... }` — at least one required. `404` if unknown id. |
| GET    | `/tickets/stats` | `{ "open": n, "in_progress": n, "closed": n }` (zeros included). |

Errors always look like `{ "error": "...", "details": [...] }` with `400` (validation / bad enum / bad paging) or `404`.

```bash
curl -X POST localhost:8080/tickets -H 'Content-Type: application/json' \
  -d '{"title":"VPN down","priority":"high"}'
curl 'localhost:8080/tickets?status=open&page=1&limit=5'
curl -X PATCH localhost:8080/tickets/<id> -H 'Content-Type: application/json' -d '{"status":"in_progress"}'
curl localhost:8080/tickets/stats
```

## Decisions & Tradeoffs
I used a layered Spring Boot design (controller → service → repository) and kept business rules such as paging limits and "PATCH needs at least one field" in the service so they are testable without HTTP. The schema is managed by Flyway with DB-level CHECK constraints and an index on `(status, created_at)`, and Hibernate only validates it, so the schema is explicit and reviewable rather than auto-generated. IDs are UUIDs, which avoid enumerable IDs and are safe to generate in the app. Enums are lowercase in the API (`in_progress`) but stored uppercase; invalid values return a clear `400`. Assumptions: new tickets always start as `open`, `priority` is optional on create (default `medium`), and PATCH only changes status/priority (not title/description). Shortcuts: tests use H2 in PostgreSQL mode instead of Testcontainers (faster to run, but not identical to Postgres); there is no authentication, no status-transition rules (any status can move to any other), and no frontend tests. The UI refetches the list and stats after every change instead of updating local state optimistically, which is simpler and always correct but costs an extra request. With more time I would add Testcontainers, OpenAPI docs, optimistic concurrency (`@Version`) for concurrent edits, status-transition rules, auth, and component tests for the UI.
