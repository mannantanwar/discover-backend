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
- [x] GitHub Actions CI — `.github/workflows/ci.yml`, triggers on push/PR to `main`, checks out code, sets up JDK 21 (Temurin), runs `./gradlew build` (compiles + runs tests in one step), `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` wired in via GitHub repository secrets. **Verified actually green on GitHub** (first run failed — see Conventions below — second run passed after fixes).

---

## Stage E — Phase 1: Places core (Complete)

> Rough plan only, sketched 2026-07-31 — not a locked spec, expect this to be reordered/reshaped as we actually build it.

- [x] `Place` entity + migration (`com.discover.backend.place.Place`, `V2__create_places.sql`) — foundation everything else depends on
- [x] Repo/service/controller/DTO/mapper for `Place` — list all, get by ID (same shape as `User`)
- [x] Seed data — `V3__seed_places.sql`, 12 real places in Connaught Place, New Delhi. Deliberately kept smaller than the ~30–50 the Build Plan suggested — enough to build/test "near me," search, and filters against; can expand later if needed.
- [x] "Near me" query — `PlaceRepository.findWithinDistance` (native `ST_DWithin`/`ST_Distance` query), `PlaceService.getPlacesWithinDistance`, `GET /api/v1/places/nearby?lat=&lng=&radius=`
- [x] Search + filters — `pg_trgm` GIN index (`V4__enable_pg_trgm.sql`) accelerating `ILIKE` name search, plus category/budget filters, all optional (`PlaceRepository.search`, `PlaceService.getPlacesBySearchFilter`, `GET /api/v1/places/search?name=&category=&budgetLevel=`). "Open now" deferred — needs current-time-vs-`opening_hours` comparison, its own separate pass.
- [x] Save places — `V5__create_saved_places.sql`, `Saved` entity (`com.discover.backend.saved`), `SavedRepository`/`SavedService` (idempotent save/unsave, `getMySavedPlaces`), `SavedController` (`POST`/`DELETE`/`GET /api/v1/saved-places`), all keyed off `@AuthenticationPrincipal User` — not a client-supplied ID. `SecurityConfig` tightened so `/api/v1/saved-places/**` requires authentication (first crack in the previously-all-`permitAll()` setup — see Pending Tasks).
  **Collections deferred, deliberately** — Phase 1's own "done" bar is just "save some," and the Product Spec treats named/curated lists ("Curated Trails") as a distinct later feature. Building a general `Collection` model now would be solving problems (default-collection bootstrapping, naming, permissions) before anything needs them — YAGNI. Revisit if/when curated lists actually get scheduled.

---

## Stage F — Phase 2: Dish Intelligence v1 (Complete)

> Rough plan only, sketched 2026-08-20 — not a locked spec, expect this to be reordered/reshaped as we actually build it.

- [x] `Dish` entity + migration (`com.discover.backend.dish`) — belongs to a `Place` (`@ManyToOne`), `V6__create_dishes.sql`
- [x] Repo/service/controller/DTO/mapper for `Dish` — `GET /api/v1/places/{placePublicId}/dishes`, `GET /api/v1/dishes/{publicId}`. `DishService` resolves the `Place` entity first (via `PlaceService.getEntityByPublicId`) so a bad place ID 404s instead of silently returning an empty list. Also logs a `DISH_VIEW` event on every fetch (`EventService`) — the interaction log's second real consumer after `LOGIN`.
- [x] Seed dish data — `V7__seed_dishes.sql`, 15 real dishes across 5 of the 12 places
- [x] Dish reviews/ratings — `V9__create_dish_reviews.sql` (composite `UNIQUE(user_id, dish_id)` + `CHECK (rating BETWEEN 1 AND 5)`), `DishReview` entity (`com.discover.backend.dishreview`), `DishReviewRepository`/`DishReviewService`/`DishReviewController` (`GET`/`POST /api/v1/dishes/{dishPublicId}/reviews`, `@RequestBody DishReviewRequest`, upsert semantics — a second review from the same user updates the first instead of erroring).
- [x] Simple dish analytics — `DishReviewService.getStatsForDish` (average rating + review count, plain JPQL `AVG`/derived `countBy`), `GET /api/v1/dishes/{dishPublicId}/reviews/stats`.
- [x] "Recommended for you" — `com.discover.backend.recommendation`: `DishRecommendationStrategy` interface (with a `RecommendationType` enum + `DishRecommendationStrategyFactory` picking the implementation — currently just `RULE_BASED`, structured so a second type slots in later without touching callers), `RuleBasedDishRecommendationStrategy` (tag-overlap against the user's own 4★+ reviews, weighted by how often each tag shows up; falls back to popularity ranking for cold start — no rating history at all, or none rated highly), `DishRecommendationService`, `RecommendationController` (`GET /api/v1/places/{placePublicId}/recommendation/{type}`).

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

### Test containers need PostGIS too, not just plain Postgres
`TestcontainersConfiguration` (used by `@SpringBootTest` for the auto-generated `contextLoads()` test) originally used `postgres:latest`. First real CI run failed with `FlywayMigrateException` → `PSQLException`, because `V1__init.sql`'s `CREATE EXTENSION IF NOT EXISTS postgis;` has nothing to enable on a plain Postgres image. Fixed by pointing it at `postgis/postgis:16-3.4` (same image `docker-compose.yml` uses for local dev) via `DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")` — the `.asCompatibleSubstituteFor(...)` call is required because Testcontainers' typed `PostgreSQLContainer` normally only accepts images explicitly named "postgres."
**Why it matters generally:** any environment that runs our schema — local dev, tests, CI, production — needs the same real capabilities the migrations assume. This was flagged as a known risk back when the local dev docker-compose was first set up, and only actually surfaced once CI ran the migration for the first time.

### Secrets in CI use GitHub repository secrets, not files — same principle as local env vars, different vault
`GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` have no fallback in `application.yml` on purpose. Locally that's satisfied by Windows user environment variables; in CI, GitHub Actions has its own separate mechanism — repository secrets (Settings → Secrets and variables → Actions), referenced in `ci.yml` via `${{ secrets.GOOGLE_CLIENT_ID }}` and passed through as env vars on the build step. GitHub encrypts these, never displays them again after saving, and auto-masks them in log output if they ever appear — protections a plain value in a tracked YAML file would never get.

### Strategy + Factory, but only once a second implementation is a near-certainty — not for every interface
`DishRecommendationStrategy` got a real interface (not just a concrete class) specifically because Phase 3 is already documented to need a smarter, taste-profile-based implementation later — the interface exists to let that swap in without touching `DishRecommendationService`/the controller. `DishRecommendationStrategyFactory` + `RecommendationType` enum picks the implementation by type, currently just `RULE_BASED`.
**Why not build all the eventually-discussed types (taste-profile-based, friends/Taste-Network-based, experimental) now:** three of the four discussed types need infrastructure that doesn't exist yet (real Taste Profile, a social graph) — defining enum values with no real implementation behind them is a different, worse kind of premature than the interface itself, since there's nothing to actually dispatch to. Add each one when it's genuinely buildable, not before. The "experimental/risk score" idea is logged in `Discover_Pending_Tasks.md`.
**General rule this confirms:** an interface with only one implementation isn't automatically over-engineering — it depends on whether a second implementation is a documented near-certainty (this case) vs. a speculative "might need it someday" (which is premature).

### A strategy's own result type lives nested inside the interface that returns it
`RankedDish` (dish + reason) is a nested `class` inside `DishRecommendationStrategy`, not a standalone top-level file — it's the contract's own output shape, and every implementation (current and future) shares it automatically without a separate import chain. It's also explicitly an **internal** type: it holds a raw `Dish` entity, so it never leaves the recommendation package as-is — `DishRecommendationService` maps it to `RecommendedDishDto` (a real DTO, `DishDto` + `reason`) before anything reaches a controller. Same "controllers/DTOs never expose entities" rule as everywhere else in the project, just easy to miss on a brand-new feature's first pass.

### Cold start: check *after* filtering, not before fetching
`RuleBasedDishRecommendationStrategy`'s popularity fallback triggers when the user has no reviews rated 4★+ — checked **after** filtering by rating, not just "does the user have any reviews at all." A user who's reviewed plenty of dishes but never rated anything highly is still a cold-start case for this algorithm; checking too early would have let them fall through to a tag-overlap computation against an empty preference map (everything tying at score 0, no real fallback).

---

## How this file gets used

Update the checklists as items complete. Add a new bullet under "Conventions & Decisions" any time we settle something during actual building that isn't already written into the spec docs — especially anything that took real debugging effort to figure out (like the port 5433 issue), so we never have to rediscover it.
