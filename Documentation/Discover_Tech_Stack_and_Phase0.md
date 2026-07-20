# Discover — Tech Stack & Phase 0 Spec (Agent Instructions)

> **For the coding agent:** This document is the authoritative spec for the Discover project's technology stack, conventions, and the first build phase ("Phase 0 — Walking Skeleton"). Follow it exactly. Do not substitute libraries or add scope beyond what's listed. When a section says "later phase," do not build it now. Ask before deviating.

---

## 1. Project context

**Discover** is an AI-powered, mobile-first, location-based experience-discovery app (places, dishes, activities, personalized recommendations). It is being built **solo**. The immediate objective is a deployed **walking skeleton**: a user can register, log in with Google, and reach an (empty) home screen — backend and mobile app talking to each other end-to-end.

**Guiding principles (apply to every decision):**
- **Single-player first.** Every feature must be useful with one user and zero social graph. Do not build social/network features yet.
- **Log every interaction from day one.** An `interaction_events` table must exist and be written to from the first version, even though nothing consumes it yet.
- **Keep it boring and standard.** Prefer well-trodden, documented approaches over clever ones.

---

## 2. Locked technology stack

Do not substitute these without explicit approval.

### Backend
| Concern | Choice |
|---|---|
| Language / runtime | **Java 21** |
| Framework | **Spring Boot 3.x** |
| API style | **REST** (no GraphQL) |
| Web layer | Spring Web (spring-boot-starter-web) |
| Persistence | Spring Data JPA + Hibernate |
| Geospatial | **Hibernate Spatial** (PostGIS geo-queries via JPA) |
| Auth | Spring Security + **JWT**, plus **OAuth2** (Google Sign-In) |
| Migrations | **Flyway** |
| DTO mapping | **MapStruct** |
| API docs | **springdoc-openapi** (generates OpenAPI 3 spec) |
| Validation | Jakarta Bean Validation (spring-boot-starter-validation) |
| Build | **Gradle** (Kotlin DSL) |
| Testing | JUnit 5, Spring Boot Test, Testcontainers (Postgres) |

### Database & infra
| Concern | Choice |
|---|---|
| Database | **PostgreSQL 16 + PostGIS** extension |
| Cache (later phases) | Redis — **do not add in Phase 0** |
| Object storage (later) | Cloudflare R2 (photos/videos) — **do not add in Phase 0** |
| Backend hosting | Railway / Render / Fly.io (Docker deploy) |
| Containerization | Docker (simple Dockerfile) |
| CI | GitHub Actions (build + test on push) |
| Error monitoring | Sentry (free tier) |

### Frontend (mobile)
| Concern | Choice |
|---|---|
| Framework | **React Native via Expo (managed workflow)** |
| Language | **TypeScript** (strict mode on) |
| Navigation | **Expo Router** (file-based) |
| Server state / data fetching | **TanStack Query (React Query)** |
| Client/global state | **Zustand** (auth/session only) — not Redux |
| Styling | **NativeWind** (Tailwind for RN) |
| Maps (Phase 1+) | **@rnmapbox/maps** (Mapbox) — needs a dev build, not Expo Go |
| Auth storage | expo-secure-store (JWT) |
| Google Sign-In | expo-auth-session / Google provider |
| Push / camera / location (later) | expo-notifications, expo-camera, expo-image-picker, expo-location |

**Note on Expo Go vs dev builds:** Phase 0 runs fine in **Expo Go**. Mapbox (Phase 1) requires switching to an **expo-dev-client** development build. Do not add Mapbox in Phase 0.

---

## 3. The frontend↔backend contract (set up in Phase 0)

The seam between the two halves is the **OpenAPI spec** produced by springdoc-openapi.

- Backend exposes the spec at `/v3/api-docs` (JSON).
- Frontend generates typed API types from it using **`openapi-typescript`**, output to `src/api/schema.d.ts`.
- Add an npm script `gen:api` that regenerates types from the running backend (or a committed `openapi.json`).
- **Rule:** every new backend endpoint → regenerate types → the app consumes them type-safely. Never hand-write request/response types that duplicate backend DTOs.

---

## 4. Repository layout

Use **two repositories** (or a monorepo with two top-level folders — either is fine; two repos is simpler solo). Suggested structure:

### Backend (`discover-backend`)
```
discover-backend/
├── build.gradle.kts
├── Dockerfile
├── src/main/java/com/discover/
│   ├── DiscoverApplication.java
│   ├── config/            # security, CORS, OpenAPI, Jackson config
│   ├── security/          # JWT filter, token service, OAuth2 handlers
│   ├── user/              # User: entity, repo, service, controller, dto, mapper
│   ├── event/             # interaction_events: entity, repo, service (write-only for now)
│   └── common/            # base entities, error handling, response wrappers
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/      # Flyway: V1__init.sql, ...
└── src/test/java/com/discover/
```
Package-by-feature (as above), not package-by-layer. Each feature folder holds its own entity/repo/service/controller/dto/mapper.

### Frontend (`discover-app`)
```
discover-app/
├── app.json / app.config.ts
├── package.json
├── tsconfig.json          # strict: true
├── app/                   # Expo Router routes
│   ├── _layout.tsx
│   ├── index.tsx          # entry / redirect based on auth
│   ├── (auth)/login.tsx
│   └── (tabs)/home.tsx    # empty home for Phase 0
├── src/
│   ├── api/               # generated schema.d.ts + fetch client + query hooks
│   ├── auth/              # zustand auth store, secure token storage
│   ├── components/        # shared UI
│   └── lib/               # config, constants, helpers
```

---

## 5. Conventions

**Backend**
- REST resources are plural nouns: `/api/v1/users`, `/api/v1/places`.
- Version the API under `/api/v1`.
- Controllers return DTOs, never JPA entities. Map with MapStruct.
- All timestamps stored UTC; expose ISO-8601.
- Use `Instant` for timestamps, `UUID` for public entity IDs (avoid leaking sequential DB ids).
- Centralized exception handling via `@RestControllerAdvice` returning a consistent error shape:
  `{ "error": { "code": "...", "message": "...", "details": [...] } }`.
- Validate all request bodies with Bean Validation annotations.
- Every schema change is a new Flyway migration; never edit an applied migration.

**Frontend**
- TypeScript `strict` on; no `any` without a comment justifying it.
- All server calls go through TanStack Query hooks in `src/api/`; components never call `fetch` directly.
- JWT stored in `expo-secure-store`, attached via a shared fetch client / query default header.
- Keep global state minimal (Zustand for auth/session only); everything server-derived lives in React Query cache.

**Both**
- Small, focused commits. Conventional Commits style (`feat:`, `fix:`, `chore:`).
- No feature work beyond the current phase's scope.

---

## 6. Phase 0 — Walking Skeleton (build this now)

**Definition of done:** On a physical phone (Expo Go), the user signs in with Google, receives a JWT from the deployed backend, and lands on an empty authenticated home screen. Backend is deployed and reachable. CI runs on push.

### Backend tasks
1. Initialize Spring Boot 3 (Java 21, Gradle Kotlin DSL) with starters: web, security, data-jpa, validation, oauth2-client, oauth2-resource-server; plus flyway, postgresql, hibernate-spatial, mapstruct, springdoc-openapi, testcontainers.
2. Configure PostgreSQL + PostGIS connection in `application.yml` (use env vars for secrets). Enable PostGIS in the first migration: `CREATE EXTENSION IF NOT EXISTS postgis;`.
3. Flyway `V1__init.sql` creating the schema in Section 7.
4. `User` feature: entity, repository, service, DTO, mapper, controller (`GET /api/v1/users/me`).
5. Security: JWT issuance + validation filter; Google OAuth2 sign-in flow → on success, upsert the user and return an app JWT.
6. `interaction_events` feature: entity + repository + a simple `EventService.record(...)` (write path only; nothing reads it yet). Wire it so login writes a `LOGIN` event.
7. springdoc-openapi enabled; verify `/v3/api-docs` and Swagger UI load.
8. Global exception handler + standard error shape.
9. Dockerfile; deploy to Railway/Fly with a managed Postgres (enable PostGIS).
10. GitHub Actions: build + run tests (Testcontainers Postgres) on push.

### Frontend tasks
1. Create Expo app (TypeScript, Expo Router). Enable TS strict.
2. Install: @tanstack/react-query, zustand, nativewind (+ tailwind config), expo-secure-store, expo-auth-session.
3. Set up `openapi-typescript` + `gen:api` script; generate `src/api/schema.d.ts` from the backend spec.
4. Shared fetch client that injects the JWT from secure-store; React Query provider at the root.
5. Zustand auth store (token, user, sign-in/out actions).
6. Screens: `login` (Google Sign-In button) → on success store JWT, fetch `/users/me`, route to `home`. `home` is an empty authenticated screen showing the user's name.
7. Route guard in `app/index.tsx`: send authed users to home, others to login.
8. Point the app at the deployed backend via an env-configured base URL.

### Explicitly OUT of scope for Phase 0
Do not build: places, dishes, reviews, maps, search, feed, recommendations, taste profile, social, Redis, R2/media upload, push notifications. These belong to later phases.

---

## 7. Initial database schema (Flyway `V1__init.sql`)

Minimal but forward-compatible. Public IDs are UUIDs; `interaction_events` exists from day one.

```sql
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    display_name    VARCHAR(120),
    username        VARCHAR(60) UNIQUE,
    avatar_url      TEXT,
    auth_provider   VARCHAR(30) NOT NULL,          -- e.g. 'GOOGLE'
    provider_sub    VARCHAR(255),                  -- provider's subject id
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- The backbone of all future intelligence. Write to it from day one.
CREATE TABLE interaction_events (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(id),
    event_type   VARCHAR(50) NOT NULL,             -- LOGIN, VIEW_PLACE, SAVE, RATE, SEARCH, ...
    entity_type  VARCHAR(50),                      -- PLACE, DISH, REVIEW, ... (nullable)
    entity_id    BIGINT,                           -- nullable
    context      JSONB,                            -- freeform: {source, lat, lng, ...}
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_user_time ON interaction_events (user_id, created_at DESC);
CREATE INDEX idx_events_type      ON interaction_events (event_type);
```

> PostGIS is enabled now so later phases can add `geography(Point, 4326)` columns and spatial indexes without a schema-engine change. No spatial columns are needed in Phase 0.

---

## 8. What to hand back after Phase 0

- Deployed backend URL + working Swagger UI.
- Repo(s) with CI green.
- A short README per repo: how to run locally, env vars required, how to run `gen:api`.
- Confirmation of the end-to-end demo: Google sign-in on a phone → JWT → `/users/me` → home screen.

Then stop and await Phase 1 instructions (Places core).
