# Discover Backend — Problems & Solutions

> A running history of real problems hit while building this project, and how each was actually solved — not typos or one-line review corrections, but issues that took real debugging or a genuine root-cause chain. Different from `Discover_Progress_Log.md` (tracks current status) and `Discover_Pending_Tasks.md` (tracks what's deliberately not done yet) — this is the "what went wrong and why" record. Add a new entry any time something like this comes up again.

---

## 1. Local Postgres connection failing with "password authentication failed" (despite correct credentials)

**Phase:** Stage C — Project Setup

**Problem:** The Spring Boot app couldn't connect to the Dockerized Postgres container — every attempt failed with a password authentication error, even though the username/password in `application.yml` exactly matched `docker-compose.yml`.

**Ruled out first** (each checked concretely before moving on): wrong password, needing a Docker restart, IPv6 resolution, SSL handshake preflight — none of these were the cause.

**Root cause:** a completely unrelated, native `postgres.exe` (leftover from a different project) was already running on the Windows machine and bound to port `5432` — the same port Docker was mapping its container to. That native process was silently intercepting the connection attempts meant for the Docker container, using different credentials, which is what produced the misleading auth error. Found via `Get-NetTCPConnection -LocalPort 5432`, which showed the native process holding the port.

**Fix:** changed `docker-compose.yml`'s host-side port mapping to `"5433:5432"` — the container's internal port stays `5432` (Postgres's fixed default), only the host-facing port moved, removing the collision entirely.

---

## 2. `JwtAuthFilter` had inverted authentication logic

**Phase:** Stage D — Development

**Problem:** The filter that reads the `Authorization: Bearer <token>` header had its condition backwards: `if (header != null && header.startsWith("Bearer "))` — which meant it would *skip* setting authentication exactly when a valid token *was* present, and would crash with a `NullPointerException` (calling `.substring(7)` on `null`) whenever no token was present at all.

**Root cause:** simple logical inversion — the condition needed to describe "when there's nothing valid to process," not "when there is."

**Fix:** flipped to `if (header == null || !header.startsWith("Bearer "))` → falls through to unauthenticated cleanly in that case, and only tries to parse the token when one genuinely exists.

---

## 3. Security classes were bypassing `UserService` and calling `UserRepository` directly

**Phase:** Stage D — Development

**Problem:** `JwtAuthFilter` and `OAuth2SuccessHandler` both called `UserRepository` directly instead of going through `UserService`, meaning "how do we look up or create a user" logic was duplicated and could drift between the two call sites.

**Root cause:** no architectural rule yet requiring repositories to only be called by their own service.

**Fix:** added `UserService.getEntityByPublicId(UUID)` (returns the real entity, for internal/infrastructure callers) and `UserService.upsertFromOAuth(...)` (the full lookup/link/create decision, previously duplicated). Both security classes now depend on `UserService` only. **Why this matters generally:** repositories are infrastructure — real business logic about user lookup belongs in exactly one place, or it silently drifts. Spring Security's own `UserDetailsService` pattern backs the same principle.

---

## 4. First CI run failed — test database had no PostGIS

**Phase:** Stage D — CI setup

**Problem:** The first GitHub Actions run failed with a `FlywayMigrateException` wrapping a `PSQLException`.

**Root cause:** `TestcontainersConfiguration` (used for the `@SpringBootTest` context in tests) was spinning up a throwaway test database from `postgres:latest` — plain Postgres, no PostGIS. `V1__init.sql`'s very first line, `CREATE EXTENSION IF NOT EXISTS postgis;`, had nothing to enable on that image, so the migration failed before the app could even start.

**Fix:** pointed the test container at `postgis/postgis:16-3.4` (the same image `docker-compose.yml` already used for local dev) via `DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")` — the `.asCompatibleSubstituteFor(...)` call is required because Testcontainers' typed `PostgreSQLContainer` normally only accepts images explicitly named "postgres."

---

## 5. CI couldn't resolve `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`

**Phase:** Stage D — CI setup

**Problem:** After fixing the PostGIS issue, the app still failed to start during CI because `application.yml` references `${GOOGLE_CLIENT_ID}`/`${GOOGLE_CLIENT_SECRET}` with no fallback value (deliberate, to keep real secrets out of any tracked file).

**Root cause:** locally, those placeholders resolve via Windows user environment variables — but GitHub's CI runner is a fresh, separate machine with no access to that. Nothing existed for the placeholders to resolve to.

**Fix:** added the real values as GitHub repository secrets (Settings → Secrets and variables → Actions), then wired them into `ci.yml`'s build step via `env: GOOGLE_CLIENT_ID: ${{ secrets.GOOGLE_CLIENT_ID }}` (same for the client secret). Same underlying principle as the local setup — secrets live in an environment-specific vault, never in a tracked file — just a different vault per environment.

---

## 6. MapStruct failed on explicit `@Mapping` targets for `PlaceDto` — "Unknown property"

**Phase:** Stage E — Phase 1 (Places core)

**Status:** diagnosed, fix not yet applied.

**Problem:** `PlaceMapper` compiled fine for every automatically-matched field (`name`, `address`, `category`, etc.), but failed with `Unknown property "latitude" in result type PlaceDto` / same for `longitude` — the two fields that needed an explicit `@Mapping(target = ..., expression = ...)` because they're derived from `Place.location` (a `Point`), not a direct same-name field.

**Root cause:** `PlaceDto` uses Lombok's `@Data`, so its setters (`setLatitude()`, `setLongitude()`) don't exist as real code until Lombok generates them — during the *same* annotation-processing pass MapStruct runs in. MapStruct's automatic name-matching has a lenient fallback that tolerates this, but its validation for an *explicit* `@Mapping` target checks for the setter before Lombok has necessarily finished generating it, which is why only the explicitly-mapped fields failed.

**Fix (standard, documented pattern for this exact Lombok+MapStruct combination):** add `org.projectlombok:lombok-mapstruct-binding` as an `annotationProcessor` dependency in `build.gradle.kts`, which forces Lombok's code generation to complete before MapStruct looks for properties.

**Reference:** [@Mapper explanation and the error we are getting currently](https://chatgpt.com/share/6a72321a-cb30-83ee-ae78-10a67f894d4d)

---

## How this file gets used

Add an entry whenever something breaks in a way that took real investigation to root-cause — not every small typo or review correction, those are just normal iteration. Include what the symptom looked like, what was ruled out (if anything), the actual root cause, and the fix — future-us should be able to recognize the same class of problem faster next time.
