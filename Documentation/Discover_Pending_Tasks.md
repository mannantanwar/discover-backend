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

## Recommendations

**"Experimental" recommendation mode with a risk score — Mannan's idea, noted 2026-09-26.**
A fourth recommendation type alongside past-reviews-based (built), taste-profile-based (Phase 3), and friends/similar-taste-based (Phase 5, Taste Network): deliberately surface dishes *outside* the user's usual pattern — the opposite of tag-overlap — tagged with some kind of "risk score" indicating how far it is from their known preferences. Not in the Product Spec or Build Plan under this name; a genuinely new idea, not yet designed. Would need its own scoping pass: what "risk score" actually means algorithmically, how it's computed, and how/where it surfaces in the API before it's buildable. Revisit once the `DishRecommendationStrategy` factory has a second real implementation and this becomes worth adding as a third.

**Recommendation impression + outcome logging — flagged 2026-09-30.**
Nothing records "we showed these dishes, the user tapped/ordered/rated this one and ignored the rest." That shown-vs-chosen record is the training data any future ML ranking (learning-to-rank, collaborative filtering) needs — starting it late means the model starts from zero late. Natural place: the home feed and recommendation endpoints, writing to `interaction_events`. Highest-leverage cheap item for Phase 5 readiness.

**Negative signals ignored — flagged 2026-09-30, expanded 2026-10-03 (Mannan).**
Both strategies only learn from 4★+ reviews; a 1★ review teaches the system nothing. Two parts:
- **The disliked dish itself** (rated ≤2★) should be filtered out of that user's recommendations entirely — no point re-suggesting something they said they didn't like.
- **Its tags** should push the user's weights *down* (negative contribution in the taste vector), so similar dishes drop too. Needs care: one bad dish shouldn't blacklist a whole tag like `north-indian` — a dislike should subtract less than a like adds, or only count after repeated dislikes.
Related, seen in testing 2026-10-03: dishes the user has *already* rated (liked) dominate "Recommended for you". Likely split: exclude already-rated dishes from recommendations, surface liked ones in a separate "Order again" section.

**Repeated per-request work in the home feed — noted 2026-10-01.**
Each context section re-runs `TASTE_PROFILE`, which re-fetches the user's reviews and taste profile — up to ~4 identical lookups per home-feed request. Negligible at 15 dishes; fix (compute user weights once and reuse) if the feed ever gets slow.

**"Must-try at this place" — a non-personalized signature dish per place — Mannan's idea, noted 2026-10-03.**
Every place gets one (or a few) dishes that almost everyone loves there, shown regardless of the viewer's taste — the opposite of the taste-profile strategies, which deliberately differ per user. Today a static `must-try` tag exists on some seeded dishes, but it's hand-picked and nothing surfaces it. Open design question: curated (keep the tag), computed (highest consensus per place — high average rating *and* enough reviews, so one 5★ doesn't win), or hybrid (curated until a place has enough reviews, then data-driven). Likely surfaces on the place detail page and as a section in the per-place recommendation response.

## Taste Profile

**Dietary preference as a hard filter (veg / non-veg) — Mannan, noted 2026-10-03. Highest priority of the taste items.**
A vegetarian user should never be shown a non-veg dish — not ranked lower, never shown at all. That makes diet a **hard constraint**, not a taste weight: it belongs in the candidate-generation stage (filter before ranking), the same place context tags already filter, and should be removed from the similarity math entirely.
Concrete bug this fixes, seen 2026-10-03: `vegetarian` is currently just another tag in the cosine similarity. After rating 5 vegetarian dishes it became one of the user's heaviest weights, so Mississippi Mud Pie (rated 5★, but untagged `vegetarian` because of egg) fell out of the top 10 — a dietary label was being scored like a flavour.
Needs: a dietary field on the user (set at onboarding — `VEG` / `NON_VEG`, later `EGGETARIAN`/`VEGAN`/`JAIN`), a dietary classification on **every** dish (today some dishes, e.g. egg-based desserts, have none), and the filter applied in both strategies and the home feed.

**Structured taste dimensions ("Taste Dimensions v2") — flagged 2026-09-30, expanded 2026-10-03 (Mannan).**
`Dish.tasteTags` and `TasteProfile.preferredTags` are one flat `List<String>` with everything mixed together. Split into separate categories:
- **Dietary** — veg / non-veg / egg (see hard-filter entry above; not part of similarity)
- **Cuisine** — north-indian, south-indian, italian, tex-mex, continental…
- **Dish type** — pasta, curry, burger, roll, dessert, beverage… (the *kind* of dish; the dish's own name stays the name, not a tag)
- **Main ingredients** — paneer, chicken, mushroom, chocolate, lentils…
- **Flavour** — sweet, spicy, tangy, savory…
- **Texture** — creamy, crispy, thick, soupy…
- (possibly) **Meal-time / temperature** — breakfast, dinner, hot, chilled — currently used by context rules

Why it matters: (1) a radar/pentagon Taste Profile chart needs per-category scores, which a single blended similarity can't be split back into; (2) per-category weighting — e.g. cuisine and flavour probably matter more than texture; (3) real "why am I seeing this" reasons ("matches your love of italian + creamy"). Taste Profile is also meant to eventually cover activities, hotels and cafes, not just dishes. Real redesign touching `Dish`, `TasteProfile`, both strategies, the context rules' tag lists and the seed data.

**~~Seed dish tags too thin to match context tags~~ — resolved 2026-10-01 by `V12__enrich_dish_tags.sql`** (existing dishes enriched + 11 new dishes covering every context rule). Big Chill (`V13`/`V14`) followed the same vocabulary. Note: Chili's Grill & Bar + 10 dishes were inserted **directly into the local DB on 2026-10-03, not via a migration** — they won't exist in CI, on other machines, or after the Docker volume is reset.

## Context

**Festival dates need yearly verification — flagged 2026-09-30.**
`FestivalRule` hardcodes 2026 dates. Janmashtami is set to 26 Aug 2026 but is likely ~4 Sep 2026 (2026 has an adhik maas). Holi (4 Mar) and Diwali (8 Nov) look right. Whole calendar needs a manual update every year.

**Context-matching radius vs. distance-sorted best match — flagged 2026-09-30.**
Context sections reuse the home feed's 5 km nearby-dish pool. Open question: should context matching search wider and sort by distance, since sometimes the right dish is worth a longer trip? Not evaluable yet — all seed data is one neighborhood (Connaught Place). Revisit once places span a real area.

**No weather caching.**
Every home-feed request calls OpenWeather. Fine for testing; free-tier rate limits will bite with real users. Cache per area for ~30–60 min when needed.

## Observability

**Logging is minimal** — just `@Slf4j` + a couple of `log.error(...)` calls in `OAuth2SuccessHandler`. No structured logging, no consistent logging strategy across the app yet. Fine for now at this scale; revisit if debugging production issues ever becomes hard with what's here.

---

## How this file gets used

Add an entry any time we say "let's defer this" or "not now, but later" instead of actually building something — include the reasoning, not just the task, so future-us knows *why* it was skipped, not just that it was.
