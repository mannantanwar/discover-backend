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
- [x] JWT issuance + validation — `JwtService` (`com.discover.backend.security`, JJWT 0.12.x, HMAC-SHA256, `generateToken`/`getPublicIdFromToken`), `SecurityConfig` (CSRF disabled, temporarily `permitAll()` on everything until OAuth2 exists), `JwtAuthFilter` (`OncePerRequestFilter`, reads `Authorization: Bearer`, sets `SecurityContextHolder`). **Not yet done:** wiring `JwtAuthFilter` into `SecurityConfig`'s filter chain (`.addFilterBefore(...)`).
- [ ] Google OAuth2 sign-in flow → upsert user → issue app JWT (needs a Google Cloud Console OAuth2 Client ID first — external step, not done yet)
- [x] `interaction_events` feature — `Event` entity (`com.discover.backend.event`, `@Table(name = "interaction_events")`, `@ManyToOne`/`@JoinColumn` to `User`, `@JdbcTypeCode(SqlTypes.JSON)` for the `context` JSONB column), `EventRepository`, `EventService.record(...)` (write-only). Not yet wired to anything — login doesn't exist yet to call it.
- [ ] Verify `/v3/api-docs` and Swagger UI actually load
- [ ] Global exception handler (`@RestControllerAdvice`, standard error shape)
- [ ] Dockerfile
- [ ] GitHub Actions CI (build + test on push, Testcontainers Postgres)

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

---

## How this file gets used

Update the checklists as items complete. Add a new bullet under "Conventions & Decisions" any time we settle something during actual building that isn't already written into the spec docs — especially anything that took real debugging effort to figure out (like the port 5433 issue), so we never have to rediscover it.
