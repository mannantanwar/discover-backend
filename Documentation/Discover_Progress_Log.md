# Discover Backend — Progress Log

> Living record of what's actually been built, what's been decided along the way (beyond what the original spec docs say), and what's next. Update this as we go — check things off, add new decisions under "Conventions & Decisions Made While Building" whenever we settle something not already written down elsewhere. This is a companion to `Discover_Agent_Working_Agreement.md` (how we work) and `Discover_Tech_Stack_and_Phase0.md` (what Phase 0 requires) — this file tracks actual status, not process or spec.

---

## Stage C — Project Setup (Complete)

- [x] Git repo initialized, `.gitignore` in place, pushed to GitHub (`discover-backend`)
- [x] Local Postgres 16 + PostGIS running via Docker (`docker-compose.yml`)
- [x] `application.yml` created — datasource, JPA, Flyway config
- [x] Missing Phase 0 dependencies added to `build.gradle.kts`: `hibernate-spatial`, `mapstruct` (+ processor), `springdoc-openapi`, `lombok` (+ processor)
- [x] App verified booting cleanly end-to-end against the real database (Flyway validates, Hibernate connects, Tomcat starts)

## Stage D — Development (In Progress)

- [x] `V1__init.sql` — Flyway migration creating `users` + `interaction_events` tables, enabling PostGIS. Verified applied via `flyway_schema_history`.
- [x] `User` entity (`com.discover.backend.user.User`) — JPA entity mapping to the `users` table. Uses Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) instead of hand-written boilerplate.
- [x] `UserRepository` — Spring Data JPA repository interface (`findByEmail`, `findByProviderSub`)
- [x] `UserDto` + `UserMapper` (MapStruct) — controllers must never return the entity directly. `UserDto` uses Lombok `@Data` (fine for DTOs, unlike entities — see conventions below).
- [x] `UserService` — `getByPublicId(UUID)`, constructor-injected repo + mapper, `.orElseThrow(...)` on not-found
- [x] `UserController` — `GET /api/v1/users/{publicId}` (temporary, path-variable-based; swap for real `/me` reading off the JWT once security exists)
- [x] JWT issuance + validation — `JwtService` (`com.discover.backend.security`, JJWT 0.12.x, HMAC-SHA256, `generateToken`/`getPublicIdFromToken`), `JwtAuthFilter` (`OncePerRequestFilter`, reads `Authorization: Bearer`, sets `SecurityContextHolder`), registered into `SecurityConfig` via `.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`.
- [x] `interaction_events` feature — `Event` entity (`com.discover.backend.event`, `@Table(name = "interaction_events")`, `@ManyToOne`/`@JoinColumn` to `User`, `@JdbcTypeCode(SqlTypes.JSON)` for the `context` JSONB column), `EventRepository`, `EventService.record(...)` (write-only). Now actually called from `OAuth2SuccessHandler` on every login.
- [x] Google OAuth2 sign-in flow — Google Cloud Console project + OAuth2 Client ID/Secret created (`GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` as Windows user env vars, never committed), `application.yml` registration (`scope: openid,email,profile` — `openid` required for Google to issue an ID token/OIDC), `OAuth2SuccessHandler` (upserts by `providerSub`, falls back to linking by `email` for a pre-existing account, logs a `LOGIN` event, issues our own JWT, writes it as JSON), wired into `SecurityConfig` via `.oauth2Login(...)`. Wrapped in try/catch with `@Slf4j` logging + the tech spec's standard error shape, since this path runs inside the security filter chain and won't be reached by the global exception handler once that's built.
- [x] Verify `/v3/api-docs` and Swagger UI actually load — confirmed manually.
- [x] Global exception handler — `com.discover.backend.common`: `ErrorDetail`/`ErrorResponse` (Java records, not Lombok — the more idiomatic choice for a small immutable data holder), `ResourceNotFoundException`, `GlobalExceptionHandler` (`@RestControllerAdvice`, one handler for `ResourceNotFoundException` → 404, one catch-all → 500 with a safe generic message). `UserService` updated to throw `ResourceNotFoundException` instead of a plain `RuntimeException`.
- [x] Dockerfile — multi-stage build (`eclipse-temurin:21-jdk` build stage runs `./gradlew bootJar`, `eclipse-temurin:21-jre` final stage just copies the jar out). Built and verified locally with `docker build` — succeeds, image `discover-backend:latest` (179MB content). **Actual cloud deployment (Railway/Fly) deferred** — see `Discover_Pending_Tasks.md`, no mobile client exists yet to justify it.
- [x] GitHub Actions CI — `.github/workflows/ci.yml`, triggers on push/PR to `main`, checks out code, sets up JDK 21 (Temurin), runs `./gradlew build` (compiles + runs tests in one step). **Not yet verified running for real** — this can only actually be confirmed once pushed to GitHub and the Actions tab shows a run; commit and push, then check.

---

## Conventions & Decisions Made While Building

Things settled during actual implementation that aren't (or aren't yet) written into the original spec docs — check here before assuming a default.

### Lombok on entities: explicit annotations, never `@Data`
Use `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` together on JPA entities.
**Do not use `@Data`** on entities specifically — its auto-generated `equals`/`hashCode` (based on all fields, including mutable ones) breaks hash-based collections when a field changes after insertion, and its auto-generated `toString()` risks infinite recursion across bidirectional relationships. `@Data` is fine for simple DTOs with no persistence identity and no relationships — just not entities.
Why `@NoArgsConstructor` + `@AllArgsConstructor` + `@Builder` all three together: Hibernate requires a no-args constructor to instantiate entities via reflection; Lombok's `@Builder` needs an all-args constructor to build from. Using `@Builder` alone (without explicitly adding the other two) can quietly break Hibernate's requirement.

### `@Column` is only added to override a default — not on every field
If a column has no constraint in the migration (nullable, non-unique), leave the Java field unannotated — Hibernate's defaults already match. Only add `@Column(nullable = false, unique = true, ...)` when the schema requires something stricter than the default. Adding empty/redundant `@Column` annotations everywhere is noise, not documentation.

### Local Postgres runs on host port `5433`, not `5432`
`docker-compose.yml` maps `"5433:5432"`. This was forced by a real bug we spent a long time diagnosing: a separate, unrelated native `postgres.exe` (leftover from another project) was already bound to port `5432` on Windows, silently intercepting connections meant for our Docker container and causing persistent, misleading "password authentication failed" errors — even though our container's credentials were always correct. Moving our container's **host-side** port to `5433` (the container's internal port stays `5432`, since that's just Postgres's fixed default) resolved it by removing the ambiguity. If this project ever moves to another machine, check for the same class of conflict before assuming `5432` is free.

### Package-by-feature, strictly
Confirmed from `Discover_Tech_Stack_and_Phase0.md` §4 and being followed exactly: every feature (`user`, later `event`, `place`, etc.) gets one package containing its own entity/repo/service/controller/dto/mapper — never a global `controller/`/`service/`/`repository/` split by technical layer.

### Timestamp fields use `@CreationTimestamp` / `@UpdateTimestamp`, never set manually
Every entity's `createdAt` should be annotated `@CreationTimestamp`, and `updatedAt` (where it exists) should be `@UpdateTimestamp` — both from `org.hibernate.annotations`. Never set these fields manually (e.g. `.createdAt(Instant.now())`) in service code.
**Why:** the migrations set `DEFAULT now()` at the database level, but that default only applies when a column is *omitted* from the `INSERT` — since these are mapped entity fields, Hibernate always includes them (as `null` if unset), so the DB default never actually fires and you'd hit a `NOT NULL` violation. `@CreationTimestamp`/`@UpdateTimestamp` make Hibernate itself assign the value at insert/update time, guaranteed, everywhere the entity is ever created or saved — not a rule that has to be remembered per call-site.
Applied to `User` (`createdAt`/`updatedAt`) and `Event` (`createdAt`) on 2026-07-25.

### Package names are always lowercase
Caught and fixed once already: a feature folder was created as `User/` (capital) while the `package` declaration said `com.discover.backend.user` (lowercase). Windows hid the mismatch (case-insensitive filesystem); this would have broken on Linux CI. Always lowercase, matching the package declaration exactly.

### Repositories are only ever called by their own service — never by controllers, filters, or handlers
`JwtAuthFilter` and `OAuth2SuccessHandler` originally called `UserRepository` directly, skipping `UserService`. Fixed 2026-07-29: `UserService` gained `getEntityByPublicId(UUID)` (returns the real entity, for internal/infrastructure callers that need more than a DTO) and `upsertFromOAuth(...)` (the full account-lookup/link/create decision, previously duplicated across `OAuth2SuccessHandler`'s branches). Both security classes now depend on `UserService` only.
**Why:** this is close to universal convention in layered backend architecture, not a stylistic preference — repositories are infrastructure, and any real logic around "how a user gets looked up" belongs in exactly one place so it isn't duplicated or drifts between call sites. Spring Security's own intended pattern for this (`UserDetailsService`) backs the same principle.
**Also decided:** `UserService.upsertFromOAuth(...)` takes plain `String` parameters (`email`, `providerSub`, `displayName`, `avatarUrl`, `authProvider`), not the raw `OAuth2User` object — extracting those values is OAuth2-specific and stays in `OAuth2SuccessHandler`, keeping the `user` package free of any dependency on Spring Security/OAuth2 types.

---

## How this file gets used

Update the checklists as items complete. Add a new bullet under "Conventions & Decisions" any time we settle something during actual building that isn't already written into the spec docs — especially anything that took real debugging effort to figure out (like the port 5433 issue), so we never have to rediscover it.
