# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Pixiv-inspired art platform built as a Java Spring Cloud microservices backend + Vue 3 frontend monorepo.

## Commands

### Backend (Maven)
```bash
# Build all modules
mvn clean install -DskipTests

# Run tests for a specific service
mvn test -pl artwork-service

# Checkstyle validation
mvn checkstyle:check

# Run a single test class
mvn test -pl user-service -Dtest=UserServiceTest
```

### Frontend
```bash
# User SPA (frontend/user)
cd frontend/user
npm install
npm run dev        # dev server on :3000
npm run build
npm run lint       # ESLint --fix on .vue/.js
npm run format     # Prettier on src/

# Admin SPA (frontend/admin) — same commands
cd frontend/admin
```

### Full Stack
```bash
# Start all services via Docker Compose
docker compose up -d

# AI service (Python)
cd ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

## Architecture

### Services
| Service | Port | Responsibility |
|---------|------|----------------|
| `gateway-service` | 8080 | Single entry point, JWT validation, routing |
| `user-service` | 8081 | Auth, users, artists, follows |
| `artwork-service` | — | Artworks, tags, comments, contests |
| `commission-service` | — | Commissions, payments (Alipay), chat |
| `notification-service` | — | Notifications, WebSocket/STOMP |
| `file-service` | — | File upload to Aliyun OSS |
| `ai-service` | 8000 | Python FastAPI + DeepDanbooru auto-tagging |

All services register with **Nacos** (service discovery + config). The gateway routes `/api/**` to `lb://service-name` URIs.

### Common Module
`common/` is a shared library providing:
- `Result<T>` — unified API response wrapper (code/message/data/timestamp)
- `PageResult<T>` — paginated response
- Shared DTOs: `UserDTO`, `ArtworkDTO`, `ArtistDTO`
- `BusinessException` + `GlobalExceptionHandler` + `ErrorCode` enum

All services depend on `common/` — use its types for inter-service contracts.

### Inter-Service Communication
- **OpenFeign** for synchronous service-to-service HTTP calls (each client has a fallback factory for circuit-breaker degradation)
- **RabbitMQ** for async events: artwork tagging requests, notification delivery, chat messages
- `notification-service` consumes RabbitMQ queues and pushes to clients over WebSocket (STOMP + SockJS at `/ws/notifications`)
- `artwork-service` has a `TaggingConsumer` that calls the Python AI service after artwork upload

### Auth Flow
- JWT-based: `JwtTokenProvider` in `user-service` issues access + refresh tokens
- `gateway-service` has `AuthenticationFilter` that validates JWTs before forwarding requests
- `InternalServiceAuthenticationFilter` handles service-to-service calls separately
- Frontend stores token/refreshToken in `localStorage` via Pinia user store (supports up to 5 saved accounts)

### Frontend Architecture
- **Axios** instance in `utils/request.js` — injects `Authorization: Bearer <token>`, handles 401 with automatic token refresh (queues concurrent requests during refresh)
- **Pinia** stores use Composition API style (`defineStore` with setup function)
- **Vue Router** guards check `userStore.isAuthenticated` and `userStore.isArtist`
- Two layout contexts in user SPA: `MainLayout` (public) and `StudioLayout` (artist dashboard)
- API base URL from `VITE_API_BASE_URL` env var (defaults to `http://localhost:8080`)

### Databases
4 separate MySQL databases — one per domain service:
- `user_db`, `artwork_db`, `commission_db`, `notification_db`
- JPA with `ddl-auto: validate` — schema changes require explicit migrations
