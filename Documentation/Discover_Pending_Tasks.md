# Discover Backend — Pending / Deferred Tasks

> Things we've consciously decided to skip, defer, or leave incomplete for now — with the reasoning, so nothing gets silently forgotten or accidentally treated as "done" just because it isn't blocking anything today. Different from `Discover_Progress_Log.md`, which tracks the current phase's checklist — this is the backlog of "not now, but don't lose track of it" items. Add to this any time we explicitly decide "later" instead of "now."

---

## Deployment

**Actual cloud deployment (Railway/Fly) — deferred 2026-07-29.**
The Dockerfile itself is being written now, but pushing an actual live deployment is being held off. Reasoning: the whole point of deploying in Phase 0 was to prove a mobile app can talk to a live backend end-to-end — but no mobile app exists yet in this project (backend-only so far), so a real deployment wouldn't actually be exercised by anything right now. Revisit once a client (mobile app, or even just manual testing needs) actually requires a live URL.

## Security

**`SecurityConfig`'s authorization rules are still `.anyRequest().permitAll()`.**
Deliberately temporary since before OAuth2/JWT existed — every endpoint is currently open regardless of authentication. Needs tightening once we're confident the login flow is solid: protected routes should require authentication, only login/public routes should stay open.

**`application.yml`'s DB `username`/`password` are hardcoded plain values (`user`/`root`), not the `${DB_USERNAME:user}` env-var-with-fallback pattern used originally.**
Flagged once when it happened, never reverted. Low risk (throwaway local credential), but inconsistent with the pattern used for the JWT secret and OAuth2 credentials. Worth reverting for consistency, not urgent.

## Auth

**Email/password registration & login (traditional, non-OAuth2).**
Listed in the long-term Product Spec's MVP feature list, but explicitly out of Phase 0's actual task list — only Google OAuth2 is in scope right now. Would need its own `LoginDto`/`SignupDto`/password-hashing logic if/when it's actually scheduled.

## Testing

**No automated tests exist yet**, despite JUnit 5, Spring Boot Test, and Testcontainers already being set up as dependencies since the very start of the project. Everything so far has been verified manually (curl/Swagger/psql/logs). Given the "industry-standard, scalable" standard we're holding this code to, real test coverage is a genuine gap, not just a nice-to-have — worth prioritizing once the current feature work stabilizes.

## Observability

**Logging is minimal** — just `@Slf4j` + a couple of `log.error(...)` calls in `OAuth2SuccessHandler`. No structured logging, no consistent logging strategy across the app yet. Fine for now at this scale; revisit if debugging production issues ever becomes hard with what's here.

---

## How this file gets used

Add an entry any time we say "let's defer this" or "not now, but later" instead of actually building something — include the reasoning, not just the task, so future-us knows *why* it was skipped, not just that it was.
